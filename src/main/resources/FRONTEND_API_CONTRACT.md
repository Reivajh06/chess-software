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
| `DELETE` | `/api/sessions/{sessionId}` | `DeleteSession` | A player abandons the game; remove it when no player is left. |

### Conventions

- Request bodies are JSON. The frontend parses every response body as JSON regardless of its `Content-Type`.
- Squares are lowercase algebraic strings (`"e2"`). The frontend also accepts Gson-serialized `Tile` records (`{ "file": "E", "rank": "R2" }`) wherever the server sends a square.
- Colours are `WHITE` / `BLACK`. Game statuses are `ACTIVE`, `WHITE_WIN`, `BLACK_WIN` and `DRAW`.
- Identity colours exist only in the browser: plugins and guests get a random colour for each game; a registered player gets a random colour when the account is created and keeps it in localStorage under `archchess.account-tones`. Nothing about these colours is sent to the server.
- The player is identified by the HTTP session cookie, which the browser sends by itself on every request. The `playerId` in the creation and join responses is that HTTP session's ID: the frontend ignores it, does not store it and never sends it. Two tabs of one browser share the same identity.
- Every request times out after 10 s in the frontend (`ChessApi.request`).

## Leaving a game

The page sends `DELETE /api/sessions/{sessionId}`, without a body and ignoring the answer, when a player drops a game: going to the main menu, the account or profile screens, rematch, starting or joining another game, a finished game left for the menu, logging out. A watcher never sends it.

There is no "continue game": leaving ends the game, and a game in progress (`ACTIVE` with at least one move) asks for confirmation first.

Server contract: the request means "the player of this HTTP session left". The server removes the session and what it keeps for it (for example machine turns) once no human player remains (machines do not count), so a game with one person is removed at once. Answer `2xx`, or `404` when it is already gone. Limitation: closing the tab cannot be told apart from a reload, so a game abandoned that way stays on the server; an idle timeout on the server is recommended.

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
| any | `/request-move` | The page retries after a growing wait (1 s, 2 s, 4 s… up to 15 s). Failed attempts are not announced or logged in the panel; a notice appears only after 4 consecutive failures. |
| any | `/server` | The invitation link is left empty and the copy button stays disabled. |

## Saved account check

The page saves the account in localStorage. At startup, if an account is saved, it calls `GET /api/history`. A `403` means the server no longer knows the login (for example after a restart), so the page forgets the saved account. Any other error, including no connection, keeps it.

## 1. Create a session

The backend enum is `PRACTICE`, `PVP`, `PVM` (player vs machine) and `MVM` (machine vs machine). Internally the frontend keeps `PVCPU` / `AIVAI`; `modules/api.js` translates at the boundary: it sends `PVCPU`/`PVAI` as `PVM` and `AIVAI` as `MVM`, and reads any `gamemode` of `PVM`/`MVM` back as `PVCPU`/`AIVAI`. A name the server does not know becomes a `null` gamemode, which breaks machine vs machine (its first plugin seat is never created).

`POST /api/sessions`

```json
{ "gamemode": "PVCPU", "difficulty": "EASY" }
```

| Field | Values |
| --- | --- |
| `gamemode` | `PRACTICE`, `PVP`, `PVCPU`, `PVAI`, `AIVAI`. The frontend's local two-player mode (`LOCAL`) is sent as `PVP`. |
| `difficulty` | `PRACTICE` for practice; a plugin difficulty (`EASY` / `MEDIUM` / `HARD` / `MAX`) for the modes with a plugin: the chosen one for PvCPU (default `EASY`), `MAX` for `PVAI`, `AIVAI` and Alfi (`???`); `NOT_PLUGIN` for everything else. `HARD` and `MAX` are ordinary CPU levels; difficulty never identifies Alfi. |
| `pluginNames` | Only for `PVCPU`, `PVAI` (one name) and `AIVAI` (white, then black; both may be the same). Names come from `GET /api/plugins`. |

### Response (`201 Created`)

```json
{
  "sessionId": "6e26bb12-02ae-4e08-a99f-c0af94d67608",
  "gamemode": "PVCPU",
  "difficulty": "EASY",
  "playerId": "node01gq7x2k9d0b3a1",
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

- `playerColor` is the **creator's** seat and is required. Without a valid colour the frontend fails with "El servidor no ha asignado un color al creador de la sala.".
- `state` may use `turn` or `turnColor`, and `moves` or `movesHistory`: the frontend accepts both shapes.
- For `PRACTICE` and the machine modes the frontend treats `waitingForOpponent` as `false`, since nobody else has to join.

### Seats per mode

| Mode | Seats on creation (server) | What the frontend does next |
| --- | --- | --- |
| `PRACTICE` | As `PVP`. | Calls `/join` itself to fill the second seat with the same player. Moves both colours and ignores the assigned ones; the board starts with `WHITE` at the bottom. |
| `PVP` | The creator gets a random colour; the other seat stays free. | Shows the invitation (section 5). |
| `LOCAL` (sent as `PVP`) | As `PVP`. | Calls `/join` itself to fill the second seat. Both colours are played on this device. |
| `PVCPU`, `PVAI` | The creator gets a colour; the machine takes the other one. | Never calls `/join`. The machine plays the colour opposite `playerColor`. |
| `AIVAI` | Both seats are machines. | Never calls `/join`. Ignores `playerId`/`playerColor` and only watches; the board is not interactive. |

Alfi uses the creator's assigned colour, like other PvCPU games. No difficulty implies an Alfi seat or mode (section 7).

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
{ "from": "e2" }
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

The frontend asks for legal moves one piece at a time. Alfi also uses this route for fresh fallback moves after repeated engine failures (section 7).

## 4. Make a move

`POST /api/sessions/{sessionId}/moves`. Each request plays **exactly one** move. The frontend does not say who is moving: the server detects it from the colours assigned in the session.

### Player move

```json
{
  "from": "e2",
  "to": "e4",
  "moveId": 1
}
```

- The server applies `moveset().get(moveId)` from `from` to `to` and answers `200`, with an empty body.
- The frontend then reads the state (section 2). If the response carries a state DTO (`{ "state": … }` or a direct state), the frontend applies it without the extra read.
- Every move the frontend computes itself is sent with explicit squares: the human's moves and also Alfi's cheats (section 7).

### Machine move

When `turnColor` belongs to a machine seat (a plugin), the frontend waits 0.7–1.3 s and sends `GET /api/sessions/{sessionId}/request-move` (no body).

The server asks the plugin for a move on the current position, resolves its `moveId` from the legal moves, applies it (promotion included) and answers `200` with the move it played. This request waits up to 120 s, because AI plugins can be slow.

```json
{
  "from": "e7", "to": "e8", "promote_to": "QUEEN",
  "pluginName": "Stockfish",
  "details": { "depth": "25", "nodes": "1174167", "time": "1000" }
}
```

- `promote_to` (also accepted as `promoteTo` or `promotion`) is `null` or missing when there is no promotion.
- Squares are read case-insensitively.
- `pluginName` is optional and names the plugin that played; the frontend also accepts `plugin` or `details.plugin`.
- `details` is an optional flat map of string values reported by the plugin about its calculation. It may be wrapped once more as `details.details`. The frontend shows these details under each turn in the "Pensando" tab; empty or missing details show only the move. In Alfi mode all details and plugin names are suppressed, including before his reveal.
- A reply without two valid squares is shown as "El motor ha devuelto una jugada no válida.".
- After the reply the frontend reads the state and animates the move from the history.

Plugins must not reveal strategy in details (no expected line and no evaluation), because the player can read them. Common keys are `depth`, `nodes`, `nps`, `time` for engines and `reason`, `thinking` for AI models. `thinking` is shown only in machine-vs-machine games and hidden in games against a person.

A failed machine request is retried by the page with growing waits (1 s, 2 s, 4 s… up to 15 s). Failed attempts are not announced or logged in the panel; a notice appears only after 4 consecutive failures.

Alfi's explicit-square cheats need backend support described in section 7. The visual board flip changes frontend seats only.

### Promotion

`POST /api/sessions/{sessionId}/promote`

```json
{ "tile": "a8", "newType": "QUEEN" }
```

`newType` is `QUEEN`, `ROOK`, `BISHOP` or `KNIGHT`. The server replaces the pawn on `tile` and answers `200` with an empty body.

When a player's pawn reaches rank 1 or 8, the frontend shows the piece picker. It then posts the move, then `/promote` with the move's `to` square, then reads the state. Machine moves do not call `/promote`: the server promotes as part of `request-move`.

## 5. PvP: join, server address and invitation

### Join

`POST /api/sessions/{sessionId}/join` with `{}` as the body. The server assigns the seat:

1. The creator is seated on creation (section 1) and never calls `/join`.
2. The invited player calls `/join` and gets the free seat.
3. Any later `/join` gets `409 Conflict` ("La sala ya está completa.").

Response (`200`), sent only to the caller that got the seat:

```json
{
  "playerId": "node01gq7x2k9d0b3a1",
  "playerColor": "BLACK",
  "gamemode": "PVP",
  "difficulty": "NOT_PLUGIN",
  "state": { "status": "ACTIVE", "turnColor": "WHITE", "waitingForOpponent": false, "fen": "…", "moves": [] }
}
```

- `playerColor` is required.
- The frontend keeps its seat in `sessionStorage`. A reload reads the state without joining again.
- Machine modes never call `/join`. `LOCAL` and `PRACTICE` call it once, from the creator's own device.

While a state read says `waitingForOpponent: true`, the frontend locks the board, shows "Esperando rival" and has the mascot chat with `waiting` lines every 11 s. When the field flips to `false`, the mascot announces the rival and the board unlocks.

### Server address

`GET /api/server`

```json
{ "address": "192.168.1.69", "port": 5000 }
```

`address` is the IPv4 address of the interface with the default route. The server finds it by "connecting" a UDP socket to `8.8.8.8:53`, which sends no packet. If the lookup fails, the server answers `503`.

### Invitation link

The link is `http://<host>:<port>/?session=<sessionId>`. If `port` is missing or `0`, the frontend uses the port the page was served from. When the page was opened through `localhost`, `127.x` or `[::1]`, the frontend asks `/api/server` for `<host>` every time the invitation dialog opens and never stores it. A page opened through a LAN address or a domain uses its own origin.

## Spectating

The "Invitar a ver" button in the match actions copies `/?watch=<sessionId>`, built like the PvP invitation with the address from `GET /api/server` (section 5).

The watching page never calls `/join`: it reads `GET /api/sessions/{id}` and polls every second. Its frontend session has `gamemode: "SPECTATE"` and `watching: true`. The board is not interactive, the page never requests machine moves, and there is no "Pensando" tab or rematch button.

Machine moves are requested by the creator's page, so a machine game stops advancing if that page is closed. Watchers do not see the machines' details. The `request-move` endpoint is not restricted to the players.

## 6. Modes in the frontend

| Mode | Menu | Who moves | Machine moves |
| --- | --- | --- | --- |
| `PRACTICE` | Available | The player, both colours | — |
| `PVP` (invitation) | Available | One player per device | — |
| `LOCAL` | Available (from the PvP dialog) | Both players on one device; the board turns to the side to move | — |
| `PVCPU` | Available (plugin dialog, then difficulty dialog) | The player vs. the CPU | `GET /request-move` |
| `PVAI` | Available (plugin dialog) | The player vs. the AI | `GET /request-move` |
| `AIVAI` | Available (plugin dialog, once per colour) | Two machines; the page only watches | `GET /request-move` for each colour |

The plugin dialog reads `GET /api/plugins`, which answers `{ "plugins": ["stockfish.jar"] }`, every time it opens. If the list is empty it says that no plugins are installed and no session can be created in that mode. A rematch reuses the selected plugins, except PvCPU MAX and Alfi, which recheck the installed engines and select the highest-priority one.

## 7. PvCPU and the Alfi mode

| Step | Route | Notes |
| --- | --- | --- |
| Create normal CPU | `POST /api/sessions` | `{ "gamemode": "PVCPU", "difficulty": "EASY" \| "MEDIUM" \| "HARD" \| "MAX", "pluginNames": ["stockfish.jar"] }` |
| Create Alfi (`???`) | `POST /api/sessions` | `{ "gamemode": "PVCPU", "difficulty": "MAX", "pluginNames": [chosenEngine] }`. `ALFI` is a frontend choice only and is never sent as a difficulty. |
| Seat the CPU | — | Seated on creation. It plays the colour opposite `playerColor`. |
| CPU / Alfi move | `GET /api/sessions/{id}/request-move` | Uses the current authoritative position whenever `turnColor` is the CPU's colour and no other CPU action is in progress. |

The difficulty dialog has EASY, MEDIUM, HARD and MAX (Spanish labels), plus `???` only when the latest `GET /api/plugins` list contains a recognized engine. Selecting `???` starts Alfi immediately without another difficulty prompt. HARD is a normal CPU. MAX chooses the highest-priority installed engine, or keeps the selected plugin if no recognized engine exists.

`modules/engine-plugins.js` is the single filename allowlist and fixed priority: Stockfish > Berserk > Archon > the remaining known engines in the module's order. Matching is case-insensitive, excludes Ollama/LLM names and rejects unknown plugins; no plugin metadata is assumed. Creation of Alfi rechecks the list and fails without creating a session if no recognized engine remains. Alfi never selects an LLM.

The frontend saves `session.alfiBoss: true` alongside the reveal phase (`session.alfi`), energy and other session fields in sessionStorage. Resume and rematch retain the boss choice independently of difficulty. Older saved explicit reveal phases are accepted; HARD alone never enables Alfi. The rival is `???` until the reveal and `Alfi` afterwards. Plugin names and all plugin details stay hidden, and no thinking log is populated for Alfi.

All board-mutating requests (`request-move` and explicit `/moves`, including promotion/state-read completion) pass through one frontend promise queue. Cheats are awaited before a fresh engine request is scheduled on Alfi's next authoritative turn. No pre-cheat engine move is reused. After four consecutive failed engine requests, the frontend first rereads the authoritative state, then chooses a fresh random legal move only if Alfi still has the turn. It tries the engine again on the next turn. If state/legal reads or the fallback also fail, retries continue; loss of connectivity or backend rejection cannot be repaired by the frontend.

### Alfi's cheats

The existing cheat presentation, fatigue, hints, colour twist and endings remain. Explicit-square cheat attempts still use `/moves` with a `moveId`. **The current backend validates both the caller's turn and legal destinations: it does not provide arbitrary teleport or position-edit support.** `TurnColorUpdater` gives the turn to the rival of the moved piece. The following describes the intended cheats and their current limitations.

| Cheat | How it works | When it is sent |
| --- | --- | --- |
| Opening push | During the reveal, a pawn of Alfi's assigned colour attempts to advance with `moveId` 0 or 1. Needs an authorized out-of-turn move. | Reveal. |
| Double move | A second fresh `request-move`, awaited after the first. It is attempted only if the authoritative turn still belongs to Alfi; no energy is charged otherwise. The current backend always passes the turn to the human, so a true engine double move is blocked pending the needed support below. | Alfi's turn only. |
| Dodge | When the player is about to capture one of his pieces (not the king), that piece first teleports to a free neighbouring square. | Player's turn. |
| Rewind | If the player's move gives check (no capture, promotion or castling), that piece teleports back. | CPU's turn. |
| Board flip | Once per game, when the player leads by 5 points: the frontend swaps `playerColor` and the CPU colour and persists them. The next frontend CPU turn asks the engine afresh. Server seat reassignment is still needed for the swap to be playable. | CPU action. |

Each cheat costs Alfi energy out of 100: double 20, dodge 15, rewind 25, flip 40. At zero he falls asleep and plays clean.

### Backend support — NEEDED (not implemented in this frontend task)

- **NEEDED: authorized Alfi position/turn editing.** Opening pushes, dodge and rewind cannot rely on `/moves` bypassing validation. Provide a documented atomic operation that returns authoritative state, with explicit turn handling. After an edit, the frontend must request a new engine move only when the resulting turn belongs to Alfi.
- **NEEDED: authorized CPU fallback submission.** The random legal fallback is wired in the frontend, but the current `/moves` route rejects a human caller on the machine's turn. A documented way to submit a fallback for the machine seat is required for the fallback to advance a game when the engine is unavailable.
- **NEEDED: engine double-move support.** `request-move` selects the plugin from the current server seat and applies its move immediately. It cannot calculate/apply an extra Alfi move on the human's turn. Provide authorized extra-turn control or an atomic engine extra-move operation; never make the frontend call the existing route on the human's turn or reuse stale/random suggestions for the second move.
- **NEEDED: atomic seat swap for the gameplay flip.** Update the human/plugin colour assignments on the server before requesting the engine for its new colour. A browser-only swap cannot change which plugin `request-move` chooses or which human `/moves` authorizes.
- **NEEDED: mutation completion/idempotency for timed-out requests.** A frontend queue prevents overlapping browser calls, but a timed-out HTTP request may still run on the server. The backend must serialize mutations per session or expose completion/idempotency so a late engine result cannot race a retry or fallback.
- **NEEDED for authoritative plugin classification:** expose an engine/AI type in `/api/plugins` in a future version. Until then the frontend uses only the filename allowlist above; no new response field or endpoint is assumed or implemented here.

## 8. UI behaviour tied to this contract

- Moves are made by click/tap, not drag and drop. A selected piece is outlined and its legal destinations are highlighted.
- Board, history, check and result are always redrawn from the authoritative state. Move, capture, check and result sounds are triggered by a new history entry, not by every poll.
- Live moves are animated: slides, knight hops, and captures that knock the piece over. History review and session restoration are not animated, and animations respect reduced motion.
- While a machine is thinking, the board is locked for the player.
