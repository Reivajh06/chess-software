# ArchChess frontend API contract

This document describes the HTTP interface between the web frontend (`src/main/resources/web`) and the Java backend (`reivajh06.api`): the routes the frontend calls, what it sends and what it expects back.

## Overview

### Routes

| HTTP | Route | Handler | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/sessions` | `PostCreateSession` | Create a session and seat its creator. |
| `GET` | `/api/sessions/{sessionId}` | `GetReadCurrentState` | Read the current game state. |
| `POST` | `/api/sessions/{sessionId}/join` | `PostJoinSession` | Take the free seat of a PvP session. |
| `POST` | `/api/sessions/{sessionId}/legal-moves` | `PostLegalMoves` | Legal moves of one piece. |
| `POST` | `/api/sessions/{sessionId}/moves` | `PostMovePiece` | Play one move (player or machine). |
| `POST` | `/api/sessions/{sessionId}/promote` | `PostPromotePiece` | Promote a pawn. |
| `GET` | `/api/server` | `GetServerAddress` | LAN address of the server, for invitations. |

### Conventions

- Request bodies are JSON. The frontend parses every response body as JSON regardless of its `Content-Type`.
- Squares are lowercase algebraic strings (`"e2"`). The frontend also accepts Gson-serialized `Tile` records (`{ "file": "E", "rank": "R2" }`) wherever the server sends a square.
- Colours are `WHITE` / `BLACK`. Game statuses are `ACTIVE`, `WHITE_WIN`, `BLACK_WIN` and `DRAW`.
- `playerId` is the only player credential. The frontend sends it with `legal-moves`, `moves` and `promote` when it has one, and never receives another seat's ID.
- Every request times out after 10 s in the frontend (`ChessApi.request`).

## HTTP status codes

`modules/api.js` declares the error statuses the frontend recognizes in `HttpStatus`:

| Code | Name | Default message when the body has no `message` |
| --- | --- | --- |
| `400` | `BAD_REQUEST` | "El servidor ha rechazado una petición no válida." |
| `403` | `FORBIDDEN` | "No puedes hacer eso en esta partida." |
| `404` | `NOT_FOUND` | "La partida no existe en el servidor." |
| `409` | `CONFLICT` | "Esa acción no es posible en el estado actual de la partida." |
| `503` | `SERVICE_UNAVAILABLE` | "El servidor no está disponible ahora mismo." |

Any other non-`2xx` status is shown as "Error del servidor (N).". When an error body like `{ "error": "<CODE>", "message": "<text>" }` is present, `message` replaces the default text and `error` is kept as `ApiError.code`.

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
{ "gamemode": "PVCPU", "difficulty": "EASY" }
```

| Field | Values |
| --- | --- |
| `gamemode` | `PRACTICE`, `PVP`, `PVCPU`, `PVAI`, `AIVAI`. The frontend's local two-player mode (`LOCAL`) is sent as `PVP`. |
| `difficulty` | `PRACTICE` for practice, `EASY` / `MEDIUM` / `HARD` for PvCPU, `NOT_CPU` for everything else. |

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
- `state` may use `turn` or `turnColor`, and `moves` or `movesHistory`: the frontend accepts both shapes.
- For `PRACTICE` and the machine modes the frontend treats `waitingForOpponent` as `false`, since nobody else has to join.

### Seats per mode

| Mode | Seats on creation (server) | What the frontend does next |
| --- | --- | --- |
| `PRACTICE` | Both seats share one ID; the creator gets `WHITE`. | Moves both colours with that ID. |
| `PVP` | The creator gets a random colour; the other seat stays free. | Shows the invitation (section 5). |
| `LOCAL` (sent as `PVP`) | As `PVP`. | Calls `/join` itself to fill the second seat. Both colours are played on this device. |
| `PVCPU`, `PVAI` | The creator gets a colour; the machine takes the other one. | Never calls `/join`. The machine plays the colour opposite `playerColor`. |
| `AIVAI` | Both seats are machines. | Never calls `/join`. Ignores `playerId`/`playerColor` and only watches; the board is not interactive. |

With `difficulty: "HARD"` (Alfi, section 7) the creator plays `WHITE`.

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
| `waitingForOpponent` | Whether a seat is still free | Locks the board while a PvP seat is empty. A missing field means `false`. |
| `eatenPieces` | `GameState.eatenPieces()` | Map keyed by the captured piece's colour. Each entry carries `{ color, type }`. |
| `movesHistory` | `GameState.movesHistory()` | JSON string or array with the move events, **newest first**. The frontend reverses it. |

Each history event carries `pieceMoved` (`{ color, type }`), `from`, `to`, `pieceCaptured` (`null` or `{ color, type }`) and `gameStateFen` (the position **after** the move). The frontend rebuilds history review from `gameStateFen`.

- State reads carry no player IDs.
- `sessionId`, `gamemode`, `difficulty` and the seat are kept by the frontend from creation/join; a state read never redefines them.

The frontend polls this route every second while a game is open (backing off to 15 s on errors) and after every move. It discards responses that belong to a previous session.

## 3. Request a piece's legal moves

`POST /api/sessions/{sessionId}/legal-moves`

```json
{ "from": "e2", "playerId": "0fbf132e-638b-434c-a55d-38d61160afcb" }
```

Outside practice, a piece whose colour does not have the turn gets no moves (`GameDirector.pieceMoves`). Response:

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

The frontend asks for legal moves one piece at a time. Besides the player's selection, only Alfi's double move uses this route (section 7).

## 4. Make a move

`POST /api/sessions/{sessionId}/moves`. Each request plays **exactly one** move. `playerType` says who computes it.

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

- The server applies `moveset().get(moveId)` from `from` to `to` and answers `200`, with an empty body.
- The frontend then reads the state (section 2). If the response carries a state DTO (`{ "state": … }` or a direct state), the frontend applies it without the extra read.
- Every move the frontend sends with explicit squares is a `PLAYER` move: the human's moves and also Alfi's cheats (section 7).

### Machine move (`playerType: "CPU"` or `"AI"`)

When `turnColor` belongs to a machine seat, the frontend waits 0.7–1.3 s and sends:

```json
{ "playerType": "CPU" }
```

There are no squares and no `playerId`: the server computes the machine's move for the current position, applies it (promotion included) and answers `200` with the move it played. `PVCPU` sends `CPU`; `PVAI` and `AIVAI` send `AI` (`machineTypes` in `modules/modes.js`).

```json
{ "from": "e7", "to": "e8", "promote_to": "QUEEN" }
```

- `promote_to` (also accepted as `promotion` or `promoteTo`) is `null` or missing when there is no promotion.
- Squares are read case-insensitively.
- A reply without two valid squares is shown as "El motor ha devuelto una jugada no válida.".
- After the reply the frontend reads the state and animates the move from the history.

The move type comes from the body's `playerType`, not from the seat whose turn it is: Alfi's rewind and board flip send `PLAYER` moves on the machine's turn or with its pieces (section 7).

### Promotion

`POST /api/sessions/{sessionId}/promote`

```json
{ "tile": "a8", "newType": "QUEEN", "playerId": "0fbf132e-638b-434c-a55d-38d61160afcb" }
```

`newType` is `QUEEN`, `ROOK`, `BISHOP` or `KNIGHT`. The server replaces the pawn on `tile` and answers `200` with an empty body.

When a player's pawn reaches rank 1 or 8, the frontend shows the piece picker. It then posts the move, then `/promote` with the move's `to` square, then reads the state. Machine moves do not call `/promote`: the server promotes as part of the machine move.

## 5. PvP: join, server address and invitation

### Join

`POST /api/sessions/{sessionId}/join` with `{}` as the body. The server assigns the seat:

1. The creator is seated on creation (section 1) and never calls `/join`.
2. The invited player calls `/join` and gets the free seat with a new ID.
3. Any later `/join` gets `409 Conflict` ("La sala ya está completa.").

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

- `playerId` and `playerColor` are required.
- The frontend keeps its seat in `sessionStorage`. A reload reads the state without joining again.
- Machine modes never call `/join`. `LOCAL` calls it once, from the creator's own device.

While a state read says `waitingForOpponent: true`, the frontend locks the board, shows "Esperando rival" and has the mascot chat with `waiting` lines every 11 s. When the field flips to `false`, the mascot announces the rival and the board unlocks.

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
| `PVAI` | Disabled | The player vs. the AI | `{ "playerType": "AI" }` |
| `AIVAI` | Disabled | Two machines; the page only watches | `{ "playerType": "AI" }` for each colour |

`PVAI` and `AIVAI` already follow the flow above; they are enabled with `available: true` in `modules/modes.js`.

## 7. PvCPU and the Alfi mode

| Step | Route | Notes |
| --- | --- | --- |
| Create | `POST /api/sessions` | `{ "gamemode": "PVCPU", "difficulty": "EASY" \| "MEDIUM" \| "HARD" }` |
| Seat the CPU | — | Seated on creation. It plays the colour opposite `playerColor`. |
| CPU move | `POST /moves` with `{ "playerType": "CPU" }` | Sent whenever `turnColor` is the CPU's colour and no other CPU action is in progress. |

`HARD` is the Alfi easter egg. The rival is presented as "Stockfish" until Alfi reveals himself; from then on his regular moves are still the server's CPU moves.

### Alfi's cheats

The cheats use the fact that `POST /moves` applies `moveset().get(moveId)` from `from` to `to` without checking the turn, the path or the move's condition. For every piece except the pawn, index 0 has the `DIRECT_CAPTURE` effect, so `moveId: 0` moves that piece to any empty square. `TurnColorUpdater` then gives the turn to the rival of whoever moved. All cheats are `PLAYER` moves.

| Cheat | How it works | When it is sent |
| --- | --- | --- |
| Opening push | Before White's first move, a black pawn advances with `moveId` 0 or 1. | Player's turn. |
| Double move | Before his CPU move, Alfi reads his legal moves with `legal-moves`. After the CPU move, he plays one of those moves with another piece. | Player's turn. |
| Dodge | When the player is about to capture one of his pieces (not the king), that piece first teleports to a free neighbouring square. | Player's turn. |
| Rewind | If the player's move gives check (no capture, promotion or castling), that piece teleports back. | CPU's turn. |
| Board flip | Once per game, when the player leads by 5 points: the frontend swaps `playerColor` and the CPU colour. Nothing is sent. From then on, the human moves the pieces of the CPU's original colour. | Any turn. |

Each cheat costs Alfi energy out of 100: double 20, dodge 15, rewind 25, flip 40. At zero he falls asleep and plays clean.

## 8. UI behaviour tied to this contract

- Moves are made by click/tap, not drag and drop. A selected piece is outlined and its legal destinations are highlighted.
- Board, history, check and result are always redrawn from the authoritative state. Move, capture, check and result sounds are triggered by a new history entry, not by every poll.
- Live moves are animated: slides, knight hops, and captures that knock the piece over. History review and session restoration are not animated, and animations respect reduced motion.
- While a machine is thinking, the board is locked for the player.
