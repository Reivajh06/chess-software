# ArchChess frontend API contract

This document describes the HTTP interface between the web frontend (`src/main/resources/web`) and the Java backend (`reivajh06.api`). It covers:

- What the frontend sends and expects on every route.
- What each Java handler does today.
- What is still **pending backend**: mainly the plugin engines that play the CPU/AI seats.

Last updated: 2026-10-26, for the plugin redesign (`plugins` on creation, `playerType` on moves).

## Overview

### Routes

| HTTP | Route | Handler | Status |
| --- | --- | --- | --- |
| `POST` | `/api/sessions` | `PostCreateSession` | Implemented. Ignores `plugins` for now (pending, section 1). |
| `GET` | `/api/sessions/{sessionId}` | `GetReadCurrentState` | Implemented. |
| `POST` | `/api/sessions/{sessionId}/join` | `PostJoinSession` | Implemented. |
| `POST` | `/api/sessions/{sessionId}/legal-moves` | `PostLegalMoves` | Implemented. |
| `POST` | `/api/sessions/{sessionId}/moves` | `PostMovePiece` | `PLAYER` moves are implemented. Machine moves (`CPU`/`AI`) are pending (section 4). |
| `POST` | `/api/sessions/{sessionId}/promote` | `PostPromotePiece` | Implemented. |
| `GET` | `/api/server` | `GetServerAddress` | Implemented. |
| `GET` | `/api/sessions/{sessionId}/promote` | `GetServerAddress` | Registered by mistake: answers the server address. The frontend never calls it. |

### Conventions

- Request bodies are JSON. Handlers answer with `Context.result(Json.toJson(...))`, which may arrive as `text/plain`, so the frontend parses the body as JSON and ignores `Content-Type`.
- Squares are lowercase algebraic strings (`"e2"`). The frontend also accepts Gson-serialized `Tile` records (`{ "file": "E", "rank": "R2" }`) wherever the server sends a square.
- Colours are `WHITE` / `BLACK`. Game statuses are `ACTIVE`, `WHITE_WIN`, `BLACK_WIN` and `DRAW`.
- `playerId` is the only player credential. The frontend sends it with `legal-moves`, `moves` and `promote` when it has one. The server never has to return another seat's ID.
- Every request times out after 10 s in the frontend (`ChessApi.request`).

## HTTP status codes

### Codes each handler uses today

"Implicit" means the handler sets no status and Javalin answers with its default. Unhandled exceptions become `500`.

| Endpoint | Success | Explicit errors | Ends in `500` when… |
| --- | --- | --- | --- |
| `POST /api/sessions` | `201` | — | Malformed JSON; unknown `gamemode` or `difficulty`. |
| `GET /api/sessions/{id}` | `200` (implicit) | `404` unknown session | `id` is not a UUID. |
| `POST /api/sessions/{id}/join` | `200` | `409` both seats taken | Unknown session (no `null` check); `id` is not a UUID. |
| `POST /api/sessions/{id}/legal-moves` | `200` | `404` unknown session | `id` is not a UUID; malformed `from`. |
| `POST /api/sessions/{id}/moves` | `200` (implicit, empty) | `404` unknown session | `id` is not a UUID; malformed square; `moveId` outside the moveset; no piece on `from`; any turn whose colour has no registered player type (section 9). |
| `POST /api/sessions/{id}/promote` | `200` (empty) | `404` unknown session | `id` is not a UUID; unknown `newType`; no piece on `tile`. |
| `GET /api/server` | `200` (implicit) | `503` address lookup failed | — |

A move that `GameDirector.movePieceAndUpdate` ignores (game not `ACTIVE`, no piece on `from`) still answers `200`. Error responses carry no body today.

### Codes the frontend recognizes

`modules/api.js` declares the error statuses in `HttpStatus`:

| Code | Name | Default message when the body has no `message` |
| --- | --- | --- |
| `400` | `BAD_REQUEST` | "El servidor ha rechazado una petición no válida." |
| `403` | `FORBIDDEN` | "No puedes hacer eso en esta partida." |
| `404` | `NOT_FOUND` | "La partida no existe en el servidor." |
| `409` | `CONFLICT` | "Esa acción no es posible en el estado actual de la partida." |
| `503` | `SERVICE_UNAVAILABLE` | "El servidor no está disponible ahora mismo." |

Any other non-`2xx` status is shown as "Error del servidor (N).". If an error body like `{ "error": "<CODE>", "message": "<text>" }` is present, `message` replaces the default text and `error` is kept as `ApiError.code`. The backend can use this to explain plugin failures (for example, "El motor no responde.").

| Code | Request | Frontend behaviour |
| --- | --- | --- |
| `404` | State polling | Polling stops and "La partida ya no existe en el servidor…" is shown. |
| `404` | State read while restoring a saved session | The saved seat is forgotten and the menu is shown. |
| `404` | `/join` | "La sala no existe." |
| `409` | `/join` | "La sala ya está completa." |
| any | `/moves` (`PLAYER`), `/promote` | The message is shown, the selection is cleared and the state is read again. |
| any | `/moves` (`CPU`/`AI`) | The message is shown, the state is read again and the machine move is retried after a growing wait (1 s, 2 s, 4 s… up to 15 s). |
| any | `/server` | The invitation link is left empty and the copy button stays disabled. |

## 1. Create a session

`POST /api/sessions`

```json
{ "gamemode": "PVCPU", "difficulty": "EASY", "plugins": ["stockfish"] }
```

| Field | Values |
| --- | --- |
| `gamemode` | `PRACTICE`, `PVP`, `PVCPU`, `PVAI`, `AIVAI`. The frontend's local two-player mode (`LOCAL`) is sent as `PVP`. |
| `difficulty` | `PRACTICE` for practice, `EASY` / `MEDIUM` / `HARD` for PvCPU, `NOT_CPU` for everything else. |
| `plugins` | Always sent, possibly empty. See below. |

### Plugins (pending backend)

`plugins` lists the engines that play the machine seats, by plugin name. Plugins are `.jar` files in `C:/Program Files (x86)/Common Files/archchess/plugins` (`PluginsManager.pluginsFolder`). The frontend defaults live in `modePlugins` (`modules/modes.js`):

| `gamemode` | `plugins` sent | Backend use |
| --- | --- | --- |
| `PVP`, `PRACTICE` | `[]` | Ignored. |
| `PVCPU` | `["stockfish"]` | Only the first element. |
| `PVAI` | `["ollama"]` | Only the first element. |
| `AIVAI` | `["ollama", "stockfish"]` | One per machine seat, in order. Future AI/CPU mixes use the same list. |

- Names are sent **without** `.jar`. `PluginsManager.reloadPlugins` keys plugins by `File.getName()`, which keeps the extension, so the lookup must strip it or add it.
- An unknown plugin should be rejected here with `400` and a `message`. The error then shows up in the menu instead of on the first CPU move.

### Response (`201 Created`)

```json
{
  "sessionId": "6e26bb12-02ae-4e08-a99f-c0af94d67608",
  "gamemode": "PVCPU",
  "difficulty": "EASY",
  "playerId": "0fbf132e-638b-434c-a55d-38d61160afcb",
  "playerColor": "WHITE",
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

- `playerId` and `playerColor` are the **creator's** seat and are required. Without a valid pair the frontend fails with "El servidor no ha asignado un color al creador de la sala.".
- `state` is a creation DTO (`turn`, `moves`), not the state-read DTO. The frontend accepts both.
- `waitingForOpponent` is always `true` here. The frontend overrides it to `false` for `PRACTICE` and for the machine modes, where nobody else has to join.

### Seats per mode

| Mode | Seats on creation (server) | What the frontend does next |
| --- | --- | --- |
| `PRACTICE` | Both seats share one ID; the creator gets `WHITE`. | Moves both colours with that ID. |
| `PVP` | The creator gets a random colour; the other seat stays `null`. | Shows the invitation (section 5). |
| `LOCAL` (sent as `PVP`) | As `PVP`. | Calls `/join` itself to fill the second seat. Both colours are played on this device. |
| `PVCPU`, `PVAI` | The creator gets a random colour; the server seats the machine on the other one. | **Never calls `/join`.** The machine plays the colour opposite `playerColor`. |
| `AIVAI` | The server seats both machines. | Never calls `/join`. Ignores `playerId`/`playerColor` and only watches; the board is not interactive. |

With `difficulty: "HARD"` (Alfi, section 7) the player must get `WHITE`. The frontend can no longer arrange that, so **the server should give the creator `WHITE` in that case**.

## 2. Read the current state

`GET /api/sessions/{sessionId}` (no body).

```json
{
  "status": "ACTIVE",
  "waitingForOpponent": false,
  "turnColor": "WHITE",
  "checkedColor": null,
  "fen": "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2",
  "eatenPieces": { "WHITE": [], "BLACK": [] },
  "movesHistory": "[{\"pieceMoved\":{…},\"from\":…,\"to\":…,\"pieceCaptured\":null,\"gameStateFen\":\"…\"}, …]"
}
```

| Field | Backend source | Frontend use |
| --- | --- | --- |
| `fen` | `FenParser.gameStateToFen(gameState)` | **Authoritative board**, plus side to move, castling, en passant and clocks. |
| `turnColor` | `GameState.turnColor()` | Whose turn it is. Also decides when a machine seat must move. |
| `checkedColor` | `GameState.checkedColor()` | `WHITE`, `BLACK` or `null`: check highlight and sound. |
| `status` | `GameState.status()` | Ends the game and shows the result. |
| `waitingForOpponent` | `whiteId == null \|\| blackId == null` | Locks the board while a PvP seat is empty. A missing field means `false`. |
| `eatenPieces` | `GameState.eatenPieces()` | Map keyed by the captured piece's colour. Each entry needs `{ color, type }`. |
| `movesHistory` | `GameState.movesHistory()` | JSON **string** (an array is also accepted) with the move events, **newest first**. The frontend reverses it. |

Each history event carries `pieceMoved` (`{ color, type }`), `from`, `to`, `pieceCaptured` (`null` or `{ color, type }`) and `gameStateFen` (the position **after** the move). The frontend rebuilds history review from `gameStateFen`. Do not serialize `MoveEvent.move` (it holds lambdas) or whole `Piece` objects.

Rules:

- Do not reverse the history in the backend, and do not send both `movesHistory` and `moves`.
- State reads must not include `whiteId`, `blackId` or any player ID.
- `sessionId`, `gamemode`, `difficulty` and the seat are kept by the frontend from creation/join. A state read never redefines them.

The frontend polls this route every second while a game is open (backing off to 15 s on errors) and after every move. It discards responses that belong to a previous session.

## 3. Request a piece's legal moves

`POST /api/sessions/{sessionId}/legal-moves`

```json
{ "from": "e2", "playerId": "0fbf132e-638b-434c-a55d-38d61160afcb" }
```

`PostLegalMoves` reads only `from`. Outside practice, a piece whose colour does not have the turn gets no moves (`GameDirector.pieceMoves`). Response:

```json
{
  "moves": [
    { "moveId": 0, "tiles": [{ "file": "E", "rank": "R3" }] },
    { "moveId": 1, "tiles": [{ "file": "E", "rank": "R4" }] }
  ]
}
```

- `moveId` is the index of the rule in `piece.moveset()`. The frontend never derives it from the array position.
- Rules with no tiles are left out, and an immobile piece returns `moves: []`.
- When several rules reach the same square, the frontend asks the player which one to use.
- Clicking a square that was not returned just clears the selection.

The frontend asks for legal moves one piece at a time (see section 9). Since the plugin redesign, the CPU no longer uses this route. Only Alfi's double move still calls it (section 7).

## 4. Make a move

`POST /api/sessions/{sessionId}/moves`. Each request plays **exactly one** move, never a move plus the reply. `playerType` says who computes it.

### Player move (`playerType: "PLAYER"`)

```json
{
  "playerType": "PLAYER",
  "from": "e2",
  "to": "e4",
  "moveId": 1,
  "playerId": "0fbf132e-638b-434c-a55d-38d61160afcb"
}
```

- The server applies `moveset().get(moveId)` from `from` to `to` and answers an **empty `200`**. `moveId` may arrive as a `Double` through Gson.
- The frontend then reads the state (section 2). If a future response carries a state DTO (`{ "state": … }` or a direct state), the frontend applies it without the extra read.
- Every move the frontend sends with explicit squares is a `PLAYER` move: the human's moves and also Alfi's cheats (section 7).

### Machine move (`playerType: "CPU"` or `"AI"`, pending backend)

When `turnColor` belongs to a machine seat, the frontend waits 0.7–1.3 s and sends:

```json
{ "playerType": "CPU" }
```

There are no squares and no `playerId`, because the machine's seat ID never reaches the frontend. `PVCPU` sends `CPU`; `PVAI` and `AIVAI` send `AI` (`machineTypes` in `modules/modes.js`).

The server should:

1. Ask the session's plugin for one move for the current FEN (`PluginExecutor.requestMove(session.toFEN())`).
2. Find the `moveId` whose legal tiles from `from` contain `to` (`GameController.legalPieceMoves`), and reject the move if there is none. Never apply an engine move without checking it.
3. Apply it with `movePieceAndUpdate` and, if there is a promotion, `promotePiece(to, type)`.
4. Answer `200` with the move it played:

```json
{ "from": "e7", "to": "e8", "promote_to": "QUEEN" }
```

- `promote_to` (also accepted as `promotion` or `promoteTo`) is `null` or missing when there is no promotion.
- Squares are read case-insensitively.
- The frontend rejects a reply without two valid squares ("El motor ha devuelto una jugada no válida.").
- After the reply the frontend reads the state and animates the move from the history.

Decide the move type from the body's `playerType`, **not** from the player type registered for `turnColor`. Alfi's rewind and board flip send `PLAYER` moves for squares and turns that the server has registered as `cpu` (section 7).

### Promotion

`POST /api/sessions/{sessionId}/promote`

```json
{ "tile": "a8", "newType": "QUEEN", "playerId": "0fbf132e-638b-434c-a55d-38d61160afcb" }
```

`newType` is `QUEEN`, `ROOK`, `BISHOP` or `KNIGHT`. `PostPromotePiece` replaces a pawn standing on its last rank and answers an empty `200`.

When a player's pawn reaches rank 1 or 8, the frontend shows the piece picker. It then posts the move, then `/promote` with the move's `to` square, then reads the state. Machine moves do **not** call `/promote`: the server promotes as part of the machine move.

## 5. PvP: join, server address and invitation

### Join

`POST /api/sessions/{sessionId}/join` with `{}` as the body. The server assigns the seat:

1. The creator is seated on creation (section 1) and **never calls `/join`**.
2. The invited player calls `/join` and gets the free seat with a new ID.
3. Any later `/join` gets **`409 Conflict`** ("La sala ya está completa.").

Response (`200`), sent only to the caller that got the seat:

```json
{
  "playerId": "f3435c46-1a64-40c5-9b03-4b0bb3390020",
  "playerColor": "BLACK",
  "gamemode": "PVP",
  "difficulty": "NOT_CPU",
  "state": { "status": "ACTIVE", "turnColor": "WHITE", "waitingForOpponent": false, "fen": "…", "moves": [] }
}
```

- `playerId` and `playerColor` are required. Never return the other seat's ID.
- Check-and-assign should be atomic (`synchronized` on the session). `GameSession.assignColor` is not synchronized yet.
- The frontend keeps its seat in `sessionStorage`. A reload reads the state without joining again.
- Machine modes never call `/join` (section 1). `LOCAL` calls it once, from the creator's own device.

While a state read says `waitingForOpponent: true`, the frontend:

- Locks the board.
- Shows "Esperando rival".
- Has the mascot chat with `waiting` lines every 11 s.

When the field flips to `false`, the mascot announces the rival and the board unlocks.

### Server address

`GET /api/server`

```json
{ "address": "192.168.1.69", "port": 5000 }
```

`address` is the IPv4 address of the interface with the default route. The server finds it by "connecting" a UDP socket to `8.8.8.8:53`, which sends no packet. If the lookup fails, the server answers `503`.

### Invitation link

The link is `http://<host>:<port>/?session=<sessionId>`. When the page was opened through `localhost`, `127.x` or `[::1]`, the frontend asks `/api/server` for `<host>` every time the invitation dialog opens and never stores it. A page opened through a LAN address or a domain uses its own origin.

## 6. Modes in the frontend

| Mode | Menu | Who moves | Machine moves |
| --- | --- | --- | --- |
| `PRACTICE` | Available | The player, both colours | — |
| `PVP` (invitation) | Available | One player per device | — |
| `LOCAL` | Available (from the PvP dialog) | Both players on one device; the board turns to the side to move | — |
| `PVCPU` | Available (difficulty dialog) | The player vs. the CPU | `{ "playerType": "CPU" }` |
| `PVAI` | **Disabled** until the backend supports it | The player vs. the AI | `{ "playerType": "AI" }` |
| `AIVAI` | **Disabled** until the backend supports it | Two machines; the page only watches | `{ "playerType": "AI" }` for each colour |

`PVAI` and `AIVAI` already follow the flow above. Turning them on only requires `available: true` in `modules/modes.js`.

## 7. PvCPU and the Alfi mode

| Step | Route | Notes |
| --- | --- | --- |
| Create | `POST /api/sessions` | `{ "gamemode": "PVCPU", "difficulty": "EASY" \| "MEDIUM" \| "HARD", "plugins": ["stockfish"] }` |
| Seat the CPU | — | The server seats it on creation. It plays the colour opposite `playerColor`. |
| CPU move | `POST /moves` with `{ "playerType": "CPU" }` | Sent whenever `turnColor` is the CPU's colour and no other CPU action is in progress. |

`HARD` is the Alfi easter egg. The rival is presented as "Stockfish" until Alfi reveals himself, and from then on his moves still come from the plugin.

### Alfi's cheats

The cheats rely on `POST /moves` **not** checking the turn, the path or the move's condition. It takes `moveset().get(moveId)` and applies its effect from `from` to `to`. For every piece except the pawn, index 0 has the `DIRECT_CAPTURE` effect, so `moveId: 0` moves that piece to any **empty** square. `TurnColorUpdater` then gives the turn to the rival of whoever moved. All cheats are `PLAYER` moves.

| Cheat | How it works | When it is sent |
| --- | --- | --- |
| Opening push | Before White's first move, a black pawn advances with `moveId` 0 or 1. | Player's turn. |
| Double move | Before his plugin move, Alfi reads his legal moves with `legal-moves`. After the plugin move, he plays one of those moves with another piece. | Player's turn. |
| Dodge | When the player is about to capture one of his pieces (not the king), that piece first teleports to a free neighbouring square. | Player's turn. |
| Rewind | If the player's move gives check (no capture, promotion or castling), that piece teleports back. | **CPU's turn**. |
| Board flip | Once per game, when the player leads by 5 points: the frontend swaps `playerColor` and the CPU colour. Nothing is sent. From then on, the human moves pieces of the colour the server registered as `cpu`. | Any turn. |

Rewind and the board flip only keep working if the server applies `PLAYER` moves as sent, whatever player type it registered for `turnColor` (section 4).

Each cheat costs Alfi energy out of 100: double 20, dodge 15, rewind 25, flip 40. At zero he falls asleep and plays clean.

### Cheats that need a new route (not built)

These cheats edit pieces in ways no current route allows:

- Turn a player's piece into a bishop.
- Steal a piece (change its colour).
- Turn a piece from his captured pool into one of his.
- Bring back one of his captured pieces.

A single generic route would cover all of them:

`POST /api/sessions/{id}/edit` with `{ "changes": [{ "tile": "e4", "piece": { "type": "BISHOP", "color": "BLACK" } | null }], "revive": { "color": "BLACK", "type": "ROOK" } }`

It would return the same DTO as `GET /api/sessions/{id}`. `revive` removes one matching piece from `eatenPieces`.

## 8. UI behaviour tied to this contract

- Moves are made by click/tap, not drag and drop. A selected piece is outlined and its legal destinations are highlighted.
- Board, history, check and result are always redrawn from the authoritative state. Move, capture, check and result sounds are triggered by a new history entry, not by every poll.
- Live moves are animated: slides, knight hops, and captures that knock the piece over. History review and session restoration are not animated, and animations respect reduced motion.
- While a machine is thinking, the board is locked for the player.

## 9. Known backend issues (2026-10-26)

1. **`PostMovePiece` throws `NullPointerException` for any colour without a registered player type.** It calls `session.playerType(turnColor).equals("cpu")`, but `PostCreateSession` only registers the creator's rival (and, in `AIVAI`, the creator). So the creator's own colour in `PVP`, `PVCPU` and `PVAI` has no type, and neither does `WHITE` in `PRACTICE`. Every move of the creator would fail with `500`. Use `"cpu".equals(session.playerType(...))`, or register both colours.
2. **`PRACTICE` registers its black side as `cpu`.** The condition `gamemode != Gamemode.PVP` includes practice.
3. **The machine-move branch of `PostMovePiece` is empty.** It also needs the body's `playerType` (section 4), and the response with the played move.
4. **`plugins` is not read on creation yet.** The session does not store which plugin plays each seat.
5. **The creation response always says `waitingForOpponent: true`.** The frontend works around it, but for machine modes and practice it should be `false`.
6. **`legal-moves` still simulates moves on the live board.** `GameConditions.moveUnchecksKing` applies each candidate move to the session's `Board` and undoes it in a `finally`. Concurrent requests, or an exception mid-simulation, can corrupt the position. Nothing serialises requests per session. With machine moves computed on the server while the frontend polls, the fix is more pressing: check legality on a copy of the board, and `synchronized (session)` in the move, promote and join handlers.
7. **Plugin package:**
   - `PluginReader.read` returns the line with its `$archchess:` prefix, which `PluginResponse` then cannot parse as JSON.
   - `readLine()` has no timeout, so a hung engine would hang the HTTP request (the frontend gives up after 10 s).
   - `requestMove` still returns `Object` instead of `PluginResponse`.
