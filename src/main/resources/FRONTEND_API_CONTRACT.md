# ArchChess frontend API contract

This document describes the **current Java HTTP interface** used by the frontend for live board and move-history updates.

## Current route availability

| HTTP | Route | Current Java handler | Availability |
| --- | --- | --- | --- |
| `POST` | `/api/sessions` | `PostCreateSession` | Registered. `201` for every mode. |
| `POST` | `/api/sessions/{sessionId}/legal-moves` | `PostLegalMoves` | Registered. Returns indexed movement rules and tiles. |
| `POST` | `/api/sessions/{sessionId}/moves` | `PostMovePiece` | Registered. On success, returns an **empty** `200` body. |
| `GET` | `/api/sessions/{sessionId}` | `GetReadCurrentState` | Registered. Returns current FEN and newest-first history encoded as a JSON string. |
| `POST` | `/api/sessions/{sessionId}/join` | `PostJoinSession` | Registered. Seats the invited player (section 5). |
| `POST` | `/api/sessions/{sessionId}/promote` | `PostPromotePiece` | Registered. On success, returns an **empty** `200` body. |
| `GET` | `/api/server` | `GetServerAddress` | Registered. Host address for invitations (section 5). |

All registered requests use JSON request bodies. `Context.result(Json.toJson(...))` may send JSON with a `text/plain` content type, so the frontend parses JSON from the response body rather than relying on `Content-Type`.

## HTTP status codes

### Codes used by each endpoint

Status codes currently set by each handler. "Implicit" means that the handler sets no status and Javalin answers with its default. Unhandled exceptions are answered by Javalin with `500`.

| Endpoint | Handler | Success | Explicit errors | Cases ending in an unhandled exception (`500`) |
| --- | --- | --- | --- | --- |
| `POST /api/sessions` | `PostCreateSession` | `201` | — | Malformed JSON; unknown `gamemode` or `difficulty`. |
| `GET /api/sessions/{sessionId}` | `GetReadCurrentState` | `200` (implicit) | `404` unknown session | `sessionId` that is not a UUID. |
| `POST /api/sessions/{sessionId}/join` | `PostJoinSession` | `200` | `409` both seats taken | Unknown session (no `null` check); `sessionId` that is not a UUID. |
| `POST /api/sessions/{sessionId}/legal-moves` | `PostLegalMoves` | `200` | `404` unknown session | `sessionId` that is not a UUID; malformed `from` square. |
| `POST /api/sessions/{sessionId}/moves` | `PostMovePiece` | `200` (implicit, empty body) | `404` unknown session | `sessionId` that is not a UUID; malformed square; `moveId` outside the moveset; no piece on `from`. |
| `POST /api/sessions/{sessionId}/promote` | `PostPromotePiece` | `200` (empty body) | `404` unknown session | `sessionId` that is not a UUID; unknown `newType`; no piece on `tile`. |
| `GET /api/server` | `GetServerAddress` | `200` (implicit) | `503` address lookup failed | — |
| `GET /api/sessions/{sessionId}/promote` | `GetServerAddress` | `200` (implicit) | `503` address lookup failed | — |

A move that `GameUpdater.movePieceAndUpdate` ignores (game not `ACTIVE`) is answered with `200`. Error responses carry no body.

### Codes recognized by the frontend

`modules/api.js` declares the error statuses in `HttpStatus`:

| Code | Name | Default message when the body has no `message` |
| --- | --- | --- |
| `400` | `BAD_REQUEST` | "El servidor ha rechazado una petición no válida." |
| `403` | `FORBIDDEN` | "No puedes hacer eso en esta partida." |
| `404` | `NOT_FOUND` | "La partida no existe en el servidor." |
| `409` | `CONFLICT` | "Esa acción no es posible en el estado actual de la partida." |
| `503` | `SERVICE_UNAVAILABLE` | "El servidor no está disponible ahora mismo." |

Any other non-`2xx` status is shown as "Error del servidor (N)." An error body of the form `{ "error": "<CODE>", "message": "<text>" }` is read when present: `message` replaces the default text and `error` is kept as `ApiError.code`.

Status-specific behavior:

| Code | Request | Frontend behavior |
| --- | --- | --- |
| `404` | State polling | Polling stops and the notice "La partida ya no existe en el servidor…" is shown. |
| `404` | State read when restoring a saved session | The saved seat is forgotten and the menu is shown. |
| `404` | `/join` | "La sala no existe." |
| `409` | `/join` | "La sala ya está completa." |
| any | `/moves`, `/promote` | The message is shown, the selection is cleared and the state is read again. |
| any | `/server` | The invitation link is left empty. |

The backend stores a session as `GameSession` (`id`, `gamemode`, `difficulty`, `whiteId`, `blackId`, `gameState`) and the current position in `GameState`. The frontend retains session IDs, the player ID and colour the server assigned on `/join`, gamemode, difficulty, and board orientation; a state poll should not redefine session identity or player assignment.

## 1. Create a session (already implemented)

`POST /api/sessions`

```json
{ "gamemode": "PRACTICE", "difficulty": "PRACTICE" }
```

Current success response (`201 Created`):

```json
{
  "sessionId": "6e26bb12-02ae-4e08-a99f-c0af94d67608",
  "gamemode": "PRACTICE",
  "difficulty": "PRACTICE",
  "whiteId": "f3435c46-1a64-40c5-9b03-4b0bb3390020",
  "blackId": "f3435c46-1a64-40c5-9b03-4b0bb3390020",
  "state": {
    "status": "ACTIVE",
    "turn": "WHITE",
    "checkedColor": null,
    "waitingForOpponent": true,
    "fen": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
    "moves": []
  }
}
```

This shows what **`PostCreateSession` actually returns**. In particular, it unconditionally sets `waitingForOpponent: true`, even for practice. The frontend overrides that display-only value to `false` for `PRACTICE`, since its IDs are equal and no opponent must join. The response is a *creation DTO*, not a serialized `GameState`.

For PvP the session starts **with no players** (`whiteId` and `blackId` are `null`) and creation seats the creator. The creation response must then carry only the creator's seat, never `whiteId`/`blackId`:

```json
{
  "sessionId": "6e26bb12-02ae-4e08-a99f-c0af94d67608",
  "gamemode": "PVP",
  "difficulty": "NOT_CPU",
  "playerId": "0fbf132e-638b-434c-a55d-38d61160afcb",
  "playerColor": "BLACK",
  "state": { "status": "ACTIVE", "turn": "WHITE", "checkedColor": null, "waitingForOpponent": true, "fen": "…", "moves": [] }
}
```

The colour is random; the other seat stays `null` until the invited player calls `/join` (section 5). Requests must never infer movement authority from board orientation. For `PRACTICE` one ID moves both colours: the frontend takes it from `playerId` or, as today, from `whiteId`.

## 2. Read a session's current state and move events (already implemented)

`GET /api/sessions/{sessionId}` (no request body).

The endpoint returns current state with field names reflecting `GameState` and `MoveEvent`. Its `movesHistory` field currently contains a **JSON-encoded string**, which the frontend parses before reversing its newest-first events. The example below shows that decoded history for readability; the frontend also accepts an array if the backend returns structured history:

```json
{
  "status": "ACTIVE",
  "turnColor": "WHITE",
  "checkedColor": null,
  "fen": "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2",
  "eatenPieces": { "WHITE": [], "BLACK": [] },
  "movesHistory": [
    {
      "pieceMoved": { "color": "BLACK", "type": "PAWN" },
      "from": "e7",
      "to": "e5",
      "pieceCaptured": null,
      "gameStateFen": "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2"
    },
    {
      "pieceMoved": { "color": "WHITE", "type": "PAWN" },
      "from": "e2",
      "to": "e4",
      "pieceCaptured": null,
      "gameStateFen": "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1"
    }
  ]
}
```

### Mapping from the actual Java records

| Frontend state DTO field | Backend source | Frontend use |
| --- | --- | --- |
| `fen` | `FenParser.gameStateToFen(session.gameState())` | **Authoritative current board** and side/castling/en-passant/clock state. |
| `turnColor` | `GameState.turnColor()` | Whose turn it is (`WHITE` or `BLACK`). |
| `checkedColor` | `GameState.checkedColor()` | `WHITE`, `BLACK`, or `null` for highlighting and check sound. |
| `status` | `GameState.status()` | `ACTIVE`, `WHITE_WIN`, `BLACK_WIN`, or `DRAW`. |
| `eatenPieces` | `GameState.eatenPieces()` | Map keyed by *captured piece color*; use `{color,type}` for each piece. |
| `movesHistory` | `GameState.movesHistory()` via `movesHistoryAsJson` | JSON string containing a `Deque<MoveEvent>`; **newest event first**. |
| `movesHistory[].pieceMoved` | `MoveEvent.pieceMoved()` | `{color,type}` describing the piece that moved. |
| `movesHistory[].from`, `.to` | `MoveEvent.from()`, `.to()` | Algebraic square strings (`"e2"`) or Java Tile record JSON. |
| `movesHistory[].pieceCaptured` | `MoveEvent.pieceCaptured()` | `null` or `{color,type}`; a non-null value identifies a capture. |
| `movesHistory[].gameStateFen` | `MoveEvent.gameStateFen()` | Board snapshot **after** that event; required for history review. |

`GameUpdater.updateMovesHistory` calls `movesHistory.addFirst(...)`, so the raw deque is in reverse chronological order. **Do not reverse it in the backend** when using this DTO: the frontend recognizes `movesHistory` and reverses it for chronological move display. If a future backend instead chooses chronological `moves`, it must name the property `moves` and use the frontend's creation-style ordering convention. Do not emit both history collections.

`GameDirector` stores the post-move FEN by calling `FenParser.gameStateToFen(updatedBoard, ...)`; `MoveEvent.gameStateFen` is therefore the correct source for each historical position. The frontend builds ply 0 from the standard initial FEN and ply N from the Nth chronological event's `gameStateFen`. It does not need full copied `Board` objects for history.

**Do not serialize `MoveEvent.move` or the entire `Piece` object.** `Move` contains behavioral conditions and effects (including lambdas); its JSON representation is not a wire contract. Read only the event's identity/piece/color/from/to/capture/FEN fields. Similarly, no need to send `GameState.board` if `fen` is already present; FEN also covers castling rights, en passant, half-move clock, and full-move number.

A Java `Tile(File.E, Rank.R4)` serialized directly through Gson is an object like `{ "file": "E", "rank": "R4" }`. The frontend also accepts these for `from`, `to`, and legal destinations, but lowercase square strings are simpler for a final HTTP DTO. `pieceMoved` and `pieceCaptured` must contain `type` and `color` at minimum.

`waitingForOpponent` is **not part of the Java `GameState` record**; it is session/registration information. A future PvP state-read response can optionally add it when the backend actually tracks registrations. It must not be derived from FEN or the length of `movesHistory`. State reads must not include `whiteId`, `blackId`, or another player's ID. `sessionId`, `gamemode`, and `difficulty` are locally retained from creation/join.

### Reading in the frontend

`normalizeState` handles both existing creation DTOs (`state.turn`, `state.moves: []`) and the state-read DTO (`turnColor`, newest-first `movesHistory`, FEN snapshots). It decodes JSON-string history or accepts array history, normalizes each event to a chronological display entry, converts Tile records to squares, extracts capture from `pieceCaptured`, and obtains board pieces from `fen`. Invalid history produces an error instead of silently clearing the move list. The UI highlights checks, reconstructs move history, and updates the board from the resulting state.

## 3. Request a piece's legal moves (already implemented)

`POST /api/sessions/{sessionId}/legal-moves`

```json
{ "from": "e2", "playerId": "f3435c46-1a64-40c5-9b03-4b0bb3390020" }
```

`PostLegalMoves` currently reads only `from`. The optional `playerId` is for future identity enforcement, not currently validated by this handler. The handler constructs `GameUpdater(session)` and calls `pieceMoves(new Tile(from))`. It returns an array of **canonical piece-moveset indices**, not map iteration indices:

```json
{
  "moves": [
    { "moveId": 0, "tiles": [{ "file": "E", "rank": "R3" }] },
    { "moveId": 1, "tiles": [{ "file": "E", "rank": "R4" }] }
  ]
}
```

The order of `moves` is unspecified. `moveId` comes from `GameController.pieceMovesetTiles` / `legalPieceMoves`, where each key is the integer index in `piece.moveset()`. It **must not** be calculated by the frontend from the array position. `tiles` currently contains Gson-serialized `Tile` records, though the frontend also accepts lowercase strings. Empty legal rule lists are filtered out, and an immobile piece returns `moves: []`.

The frontend keeps a `destination → [{moveId, optional label}]` mapping for the selected position. Multiple rules may share one destination; the UI offers an explicit choice rather than guessing the index. Clicking a destination that was not returned simply deselects (there is no `moveId` to send). The frontend does not check turn or piece ownership: any piece can be selected while the game is active, and the server decides which moves it may make by answering `legal-moves` (an empty list for a piece that cannot move now) and by validating `/moves`.

## 4. Execute a move and refresh the board (already implemented)

`POST /api/sessions/{sessionId}/moves`

```json
{
  "from": "e2",
  "to": "e4",
  "moveId": 1,
  "playerId": "f3435c46-1a64-40c5-9b03-4b0bb3390020"
}
```

`PostMovePiece` currently extracts only `from`, `to`, and the numeric `moveId` (Gson may deserialize it as `Double`). It executes `updater.movePieceAndUpdate(moveId, fromTile, toTile)` and sends **no JSON or FEN** on success; the response is an empty `200 OK`. `playerId` is presently ignored. Promotion is a separate request (see below).

### Promotion

`POST /api/sessions/{sessionId}/promote`

```json
{ "tile": "a8", "newType": "QUEEN", "playerId": "f3435c46-1a64-40c5-9b03-4b0bb3390020" }
```

`PostPromotePiece` reads `tile` and `newType` (`QUEEN`, `ROOK`, `BISHOP` or `KNIGHT`) and calls `GameUpdater.promotePiece`, replacing the pawn on its last rank. It answers an empty `200`. When a pawn destination is on rank 1 or 8 the frontend shows the piece picker; once a piece is chosen it posts the move first, then `/promote` with the move's `to` square, and finally reads the state.

The frontend execution sequence is:

1. Select a piece and fetch legal rules; keep the returned canonical `moveId` attached to the selected destination.
2. On a legal click, send `{from,to,moveId}` (and locally retained `playerId` if available) with no client-generated `Move` object.
3. If the move response contains a future `{state: ...}` or direct state DTO, apply it immediately. For today's **empty `200`**, call `GET /api/sessions/{sessionId}` and apply the returned state.
4. Rebuild board pieces from `fen`, history from `movesHistory`, checked king from `checkedColor`, and game result from `status`. Clear the stale legal-moves selection.
5. Continue polling the state endpoint to pick up opponent moves. Discard outdated responses when changing sessions.

The frontend uses the state handler's **GET** route for post-move refreshes, polling, and restoring saved sessions. Each successful refresh redraws the board and history from the authoritative state.

The current `PostMovePiece` does not revalidate `to` against the legal destination set, and `movePieceAndUpdate` can silently return before mutation if the turn or status is wrong. Frontend legal highlighting is UX protection, **not** server-side move authorization. These backend limitations are recorded here, not modified in this frontend-only change.

## 5. Join a PvP session and wait for the rival (pending backend)

`POST /api/sessions/{sessionId}/join` with `{}` as the request body.

Only the invited player calls it, to say it wants to join that session. **The server assigns the seat**, so nobody chooses a colour and no player ID travels in the invitation:

1. `POST /api/sessions` creates a `GameSession` with `whiteId = null` and `blackId = null`.
2. In the same request it seats the creator: both seats are free, so the server picks the colour at random, generates a new player ID (UUID), stores it in that seat and returns it as `playerId` + `playerColor` (section 1). The creator **never calls `/join`**.
3. The invited player opens the invitation and calls `/join`, the only caller of this route. The server gives it the remaining free seat with a new ID.
4. Any later `/join` finds both seats taken and fails with **`409 Conflict`**. The frontend shows "La sala ya está completa."

Successful response (`200`), only ever sent to the caller that got the seat:

```json
{
  "playerId": "f3435c46-1a64-40c5-9b03-4b0bb3390020",
  "playerColor": "BLACK",
  "gamemode": "PVP",
  "difficulty": "NOT_CPU",
  "state": { "status": "ACTIVE", "turnColor": "WHITE", "waitingForOpponent": false, "fen": "…", "movesHistory": "[]" }
}
```

- `playerId` and `playerColor` are required. The frontend keeps them and sends `playerId` with every legal-moves and move request; it is that player's only credential, so **never return the other seat's ID** and never put either ID in state reads.
- `state` uses the same DTO as the state read. `gamemode` and `difficulty` are optional (the guest assumes `PVP`).
- Unknown session: `404`.
- Check-and-assign must be **atomic** (for example `synchronized` on the `GameSession`): two guests joining at the same time must never both get the last seat.
- No player records are needed: the two seats are the registry, and `waitingForOpponent` is simply "a seat is still `null`".
- The frontend keeps its seat in `sessionStorage`, so a page reload reads the state without joining again. A player who loses that tab (closes it, opens the link elsewhere) gets `409` once the room is full; reclaiming a seat would need player records later.

`waitingForOpponent` must then appear in **every** `GET /api/sessions/{sessionId}` state read for PvP sessions: `true` while `whiteId` or `blackId` is still `null`, `false` once both seats are taken. Omitting it means `false` to the frontend, so a missing field unlocks the board.

While `waitingForOpponent` is `true` the frontend:

- Locks the board for the creator (`boardInteractive` returns `false`), so the game cannot start early.
- Shows "Esperando rival" in the turn card and the opponent panel.
- Has Alfi chat with `waiting` lines every few seconds.

When a state read flips it to `false`, Alfi announces the rival with an `opponentJoined` line and the board unlocks. White moves first as usual.

### Server address (pending backend)

`GET /api/server`

```json
{ "address": "192.168.1.69", "port": 5000 }
```

- `address`: the IPv4 address of the network interface with the default route, the one other machines on the same network use to reach this server. One string, never a loopback address.
- `port`: the port the server listens on (`5000`).

Get the address by "connecting" a UDP socket. UDP `connect()` sends no packet; it only makes the operating system pick the outgoing interface, which skips Docker, WSL, VMware and similar virtual adapters (`InetAddress.getLocalHost()` may return any of those):

```java
try (DatagramSocket socket = new DatagramSocket()) {
    socket.connect(InetAddress.getByName("8.8.8.8"), 53);
    String address = socket.getLocalAddress().getHostAddress(); // e.g. 192.168.1.69
}
```

It works offline as long as the machine has a default route (a router). Compute it on every request and never cache it: the machine may change networks. If it fails or yields `0.0.0.0`, respond `503`; the frontend then shows that the invitation cannot be built.

### Invitation link

The creator shares `http://<host>:<port>/?session=<sessionId>`. The link names only the session: whoever opens it calls `/join` and gets the free seat.

`<host>` is never a loopback address. It depends on the machine hosting the server, so the frontend discovers it every time the invitation dialog opens and **never stores it**. When the page was opened through `localhost`, `127.x` or `[::1]`:

It asks the server with `GET /api/server` (above) and builds the link with the reply. There is no manual entry: if the request fails, the dialog shows an error and the copy button stays disabled.

Javalin listens on every interface, so other machines can reach port `5000` if the firewall allows it. A page already opened through a LAN address or domain uses its own origin.

## 6. UI behavior connected to this contract

- A selected piece is visually outlined and its legal destinations are highlighted. Selecting a piece of the other color or one with no legal moves produces no warning; an empty legal-moves response simply leaves no destination highlights. Clicking an empty square outside the available destinations is also silent.
- Board movement uses **click/tap**, not drag and drop. Native piece/tile image dragging is disabled; the board uses the regular mouse cursor rather than the hand cursor.
- The Web Audio module uses the supplied 8-bit and 16-bit samples for piece selection, ordinary moves, king moves, captures, checks, and win/draw/lose. The soundset selector sits alongside the board theme in settings and persists with volume and mute. Samples are cached and decoded after a user gesture; switching sets or muting stops queued sounds. A move/check/result sound is triggered by an actual new state/history transition, not every state poll.
- Live moves slide into place (knights hop). During a capture the attacker makes contact and briefly recoils, then the captured piece tips onto its side and slides away before disappearing. The capture sound is timed to contact. Animations respect reduced-motion preferences and are skipped for history review or session restoration.
- Board redraw and history updates are tied to the authoritative returned state. On an empty move response the frontend requests the current state immediately; it does not assume the POST response has a body.
- The backend still needs the PvP join endpoint. This frontend patch does **not** add backend HTTP routes, game-rule fixes, promotion, or artificial opponents.

## 7. PvCPU and the Alfi mode (frontend mock on the existing routes)

Until the server integrates an engine, the CPU opponent is simulated in the frontend with the existing routes only.

### What the frontend does today

| Step | Route | Notes |
| --- | --- | --- |
| Create the game | `POST /api/sessions` | `{ "gamemode": "PVCPU", "difficulty": "EASY" \| "MEDIUM" \| "HARD" }`. |
| Seat the CPU | `POST /api/sessions/{id}/join` | The frontend occupies the second seat itself, so `waitingForOpponent` becomes `false`. With `HARD` the player always gets the seat whose colour is `WHITE` (creator or joined seat). |
| CPU move | `POST /legal-moves` for each of its pieces, then `POST /moves` | A random legal move. Pawns reaching the last rank promote to a queen through `/promote`. |
| Alfi's opening cheat (`HARD`) | `POST /moves` only | Before White's first move a black pawn advances with `moveId` 0 or 1. `legal-moves` returns nothing for the colour without the turn (`GameUpdater.pieceMoves`), but `/moves` applies any moveset index and `TurnColorUpdater` hands the turn back to White. |

The `HARD` level is the Alfi easter egg: the rival is shown as "Stockfish" until that cheat, then Alfi reveals himself.

### Alfi's cheats

**Teleport.** `POST /moves` does not check the turn, the path or the move's condition: it takes `moveset().get(moveId)` and applies its effect from `from` to `to`. The effect of index 0 is `DIRECT_CAPTURE` for every piece except the pawn (`NO_CAPTURE`), so `moveId: 0` moves any piece to any **empty** square. `TurnColorUpdater` then gives the turn to the rival of whoever moved. The cheats rely on this; the backend must keep that behaviour (or offer an equivalent route) for them to keep working.

| Cheat | How it works | Route |
| --- | --- | --- |
| Double move | After his normal move, Alfi sends a second legal move (from the list `legal-moves` gave before the first one, for another piece). His piece moved, so the turn stays with the player | `POST /moves` |
| Dodge | When the player captures one of his pieces (not the king), the piece is teleported to a free neighbouring square first; the player's move lands on the empty square | `POST /moves` with `moveId: 0` |
| Rewind | If the player's move gives check (without capture, promotion or castling), that piece is teleported back to its square; the player's piece moved, so the turn goes to Alfi | `POST /moves` with `moveId: 0` |
| Flip the board | Once per game, when the player leads by 5 points of material: the two seats swap in the frontend (`playerColor` and the CPU colour) | None (seats are not validated by the server) |

Each cheat costs Alfi energy (double 20, dodge 15, rewind 25, flip 40, out of 100). At zero he falls asleep and plays clean.

### Cheats that need a new route

`POST /promote` now only accepts a pawn on the last rank (`GameUpdater.promotePiece`), so it can no longer turn other pieces into a bishop. These cheats edit pieces in a way no current route allows; a **single** generic route would cover all of them:

| Cheat | What it needs |
| --- | --- |
| Turn a player's piece into a bishop | Change a piece's type |
| Steal a piece (change its colour) | Change a piece's colour |
| When Alfi captures, turn a piece from his captured pool into one of his | Change a captured piece's colour and put it on the board; update `eatenPieces` |
| Bring back one of his captured pieces | Put a piece from `eatenPieces` back on the board; update `eatenPieces` |

Proposed route: `POST /api/sessions/{id}/edit` with `{ "changes": [{ "tile": "e4", "piece": { "type": "BISHOP", "color": "BLACK" } | null }], "revive": { "color": "BLACK", "type": "ROOK" } }`. It returns the same state as `GET /api/sessions/{id}`; `revive` removes one matching piece from `eatenPieces`. These four cheats are not built yet; they will be once the route exists.

### Backend issues found while testing (2026-10-23)

1. **Every `POST /moves` fails with 500 at `HEAD`.** `GameUpdater.updateGameState` builds the new `GameState` only from the six `Updater`s, and none of them sets `board`, `movesHistory`, `status` or `checkedColor` on the `GameStateBuilder`. `board` ends up `null` and `FenParser.boardToFen` (line 17, called from `GameUpdater.java:95`) throws `NullPointerException`. The history would also restart empty on every move.
2. **`legal-moves` corrupts the live board.** `MovesValidator`/`GameController.legalPieceMoves` call `GameConditions.moveUnchecksKing`, which applies each candidate move to the session's real `Board` and undoes it in a `finally`. Two consequences:
   - **Concurrent requests race.** 32 `legal-moves` requests sent at once, 10 times, returned 80 errors (500) and left the starting position as `1nb1kbnp/ppppppp1/8/8/8/8/PPP1PPPP/RNBP2NR` (queens, a king and rooks gone, pawns where the queen stood). The same requests one after another left it intact. Any two clients asking at once (PvP, or a CPU asking for all its pieces) can trigger it.
   - **Exceptions mid-simulation leave it broken even without concurrency.** With requests one at a time, 40 random moves still lost a black bishop (f8) with no capture. The log shows `NullPointerException` in `GameConditions.isKingCheckedBy` (`GameConditions.java:32`, reached from `isKingChecked` line 39) and in `MoveRules.SAFE_MOVE` (`MoveRules.java:52`): both iterate a list of piece tiles taken before nested simulations moved those pieces, so `pieceAt(tile)` returns `null`. When that happens inside `moveUnchecksKing`, `undoMove` runs with a half-applied move (and `pieceCaptured` still `null`), so pieces vanish.
   - Suggested fix: check legality on a **copy** of the board (or of the `GameState`) instead of the live one, and serialise writes per session (e.g. `synchronized (session)` in the move/promote handlers).

The frontend now asks for legal moves one piece at a time, which avoids the race but not the second problem.
