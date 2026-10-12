# Frontend ↔ backend API contract

This contract defines HTTP payloads around the existing Java domain. `ChessAPI.java` currently serves static files and registers `POST /api/sessions`. `PostLegalMoves` is incomplete and unregistered; join, state-read, and move routes are proposed. This revision changes documentation only; backend classes and the frontend adapter remain unchanged.

## Backend operations this contract adapts

```java
GameUpdater updater = new GameUpdater(session);
Map<Move, List<Tile>> legalMoves = updater.pieceMoves(from);
updater.movePieceAndUpdate(move, from, to);
```

Selection returns movement rules paired with their legal tiles, not simply a flat list of destinations. Execution needs a specific existing `Move` plus origin and destination `Tile` objects. `GameSession` does not expose the previously assumed `legalPieceMoves`, `move`, or `promotePieceIn` methods.

`Move` contains `x`, `y`, `repetitions`, `conditions`, and `effect`. Conditions and effects are Java behavior (including lambdas and anonymous implementations), so raw `Move` objects must not be serialized to JSON or constructed from frontend input. A DTO identifies an existing backend movement rule by its index in the selected piece's `moveset()`.

## Identity and random assignment

On session creation, the backend generates `sessionId`, `whiteId`, and `blackId` as UUID strings. Player IDs are opaque session tokens, not account IDs. The backend owns their mapping to White and Black.

| Mode | Player IDs and permissions |
| --- | --- |
| `PVP` | Distinct white/black UUIDs. The creating frontend randomly chooses one for its user and gives the remaining ID to the other participant. |
| `PRACTICE` | `whiteId === blackId`. One UUID controls both sides, subject to the current turn and legal moves. |
| `PVAI`, `PVCPU` | The actual ID values do not affect permissions; IDs may be omitted. The backend permits human actions only for the human-controlled side. |
| `AIVAI` | IDs are irrelevant. The frontend observes; manual selection and movement are rejected. |

Random assignment happens **once**, in the creating frontend. The joining browser does not independently draw a UUID, and the frontend does not swap the backend's white/black mapping. Choosing `blackId` means the creator plays Black.

Selection and movement requests send the acting user's `playerId`, never `color` or `playerColor`. The backend resolves the acting side from that ID. Board responses still contain colors for rendering and turn indicators.

Store the local `{ sessionId, playerId, gamemode }` in `sessionStorage`. A locally derived color is a display preference, not authorization. The old `playerToken`, response `playerColor`, and `X-Player-Token` header are removed from this contract. IDs are sent in JSON bodies.

## Routes

| Method | Endpoint | Purpose |
| --- | --- | --- |
| POST | `/api/sessions` | Create a session and its player UUIDs |
| POST | `/api/sessions/{sessionId}/join` | Register a PvP player ID or reconnect |
| GET | `/api/sessions/{sessionId}` | Read current state and complete move history |
| POST | `/api/sessions/{sessionId}/legal-moves` | Select a piece and obtain legal destinations |
| POST | `/api/sessions/{sessionId}/moves` | Execute a selected movement rule on a destination tile |

All bodies use JSON. UUIDs are strings; squares use lowercase algebraic notation such as `e2`.

### Response structure

Keep immutable session configuration separate from mutable game state:

- Creation returns the generated IDs and initial `state`. It does not echo `gamemode` or `difficulty`, which the creator already supplied.
- Joining returns `gamemode`, `difficulty`, and `state` so the new browser learns the configuration. It does not echo `sessionId` from the URL or `playerId` from the request.
- State reads and move responses contain only mutable game state. `state` never contains `sessionId`, `gamemode`, `difficulty`, or player IDs.

The frontend retains session metadata locally rather than receiving it on every poll or move. The equal white/black UUIDs in practice are intentional: they express that one player controls both roles.

## 1. Create a session

`POST /api/sessions`

```json
{
  "gamemode": "PVP",
  "difficulty": "NOT_CPU"
}
```

For this proposed contract, use `NOT_CPU` for PvP and AI modes, `PRACTICE` for practice, and `EASY`, `MEDIUM`, or `HARD` for CPU. The existing Java enums contain these names; that does not imply that the automated modes are implemented. Return `501` for an unavailable mode.

Response: `201 Created`.

```json
{
  "sessionId": "6e26bb12-02ae-4e08-a99f-c0af94d67608",
  "whiteId": "f3435c46-1a64-40c5-9b03-4b0bb3390020",
  "blackId": "68eab6b1-974f-4e83-9c16-1ad28bd98c87",
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

For practice, return the same generated UUID in `whiteId` and `blackId`, with `waitingForOpponent: false`. Practice has no second-player registration step. AI/CPU modes also do not wait for a second human.

## 2. Distribute IDs and join a PvP session

This contract proposes an explicit registration step so two browsers share one consistent assignment:

1. The creator receives both IDs, randomly selects its own, and registers it through `/join`.
2. The creator gives the other person the session UUID and the remaining player UUID through an invitation.
3. The second browser registers that remaining ID and stores it as its local `playerId`.

A session UUID alone does not convey the player UUID selected for the joining person. One frontend invitation format is `/?session={sessionId}#playerId={remainingPlayerId}`. The joining frontend reads the fragment locally and sends the UUID in the join body. It must not obtain both player tokens from a public state response.

`POST /api/sessions/{sessionId}/join`

```json
{
  "playerId": "68eab6b1-974f-4e83-9c16-1ad28bd98c87"
}
```

Response: `200 OK`, with configuration once at the top level and mutable game state beneath it:

```json
{
  "gamemode": "PVP",
  "difficulty": "NOT_CPU",
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

This example represents the first registration. Do not echo the session/player IDs or return the other player's UUID or `playerColor`.

`waitingForOpponent` remains true after the first distinct ID registers and becomes false after both register. Rejoining with an already registered ID is an idempotent reconnect, not a third participant. Registration must be atomic per session. Return `403` for an ID outside this session and `409` for joining a non-PvP session.

## 3. Read current state and history

`GET /api/sessions/{sessionId}`

Response: `200 OK`, with the state directly:

```json
{
  "status": "ACTIVE",
  "turn": "BLACK",
  "checkedColor": null,
  "waitingForOpponent": false,
  "fen": "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
  "moves": [
    {
      "ply": 1,
      "color": "WHITE",
      "piece": "PAWN",
      "from": "e2",
      "to": "e4",
      "capture": false,
      "promotion": null,
      "notation": "e4",
      "fen": "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
    }
  ]
}
```

`fen` is the current position. `moves` is the complete chronological history, starting at ply 1. Each move's `fen` represents the position after that move, allowing history review. The initial position is the standard starting position.

An explicit `pieces` array can replace current-state `fen`, using entries such as `{ "square": "a1", "color": "WHITE", "type": "ROOK" }`. Return one current-position representation, not both. Per-move FEN snapshots represent historical positions and support history review.

`status` matches `GameStatus`: `ACTIVE`, `WHITE_WIN`, `BLACK_WIN`, or `DRAW`. A win identifies the winner; it does not by itself identify checkmate as the cause. `checkedColor` is `WHITE`, `BLACK`, or `null`.

State reads require no player ID and never expose `whiteId`, `blackId`, `playerId`, or `playerColor`. The frontend derives orientation from the original assignment, keeping any display color locally.

## 4. Select a piece / request legal moves

`POST /api/sessions/{sessionId}/legal-moves`

```json
{
  "playerId": "f3435c46-1a64-40c5-9b03-4b0bb3390020",
  "from": "e2"
}
```

Response: `200 OK`.

```json
{
  "moves": [
    { "moveId": 0, "tiles": ["e3"] },
    { "moveId": 1, "tiles": ["e4"] }
  ]
}
```

The backend resolves the side from `playerId`, checks that the session permits interaction, converts `from` to `new Tile(from)`, and calls `new GameUpdater(session).pieceMoves(fromTile)`.

Adapt each nonempty `Map<Move, List<Tile>>` entry into `{ moveId, tiles }`:

- `moveId` is the zero-based index of the **existing Move** in `session.gameState().board().pieceAt(fromTile).moveset()`. It is not an index into the returned `HashMap`, whose iteration order is unspecified, or into the filtered JSON array.
- `tiles` is that entry's destinations serialized as lowercase square names, using `Tile.name().toLowerCase(Locale.ROOT)`. Keep each tile attached to its movement rule.
- Empty destination lists are omitted. If no entry has destinations, return `moves: []`.
- IDs are scoped to the selected piece at `from` in the current position, not globally to the session. A sliding movement rule can have multiple destination tiles.

The pawn example matches `PieceFactory.createPawn`: index 0 is a single forward step and index 1 is the initial double step. Two rules can share the same direction but have different conditions/effects; for example, diagonal capture and en passant. Therefore direction or destination alone must not replace the rule identifier.

The frontend can flatten `tiles` to draw highlights, but must retain the mapping from each destination to its `moveId`. If multiple rules are returned for the same tile, preserve them and explicitly select a rule instead of relying on map order. Clear this mapping on deselection, session changes, history review, or a changed board position.

For practice, the shared ID permits selecting the side whose turn it is. For PvP, the ID must belong to that side and must have registered. For AI/CPU, omit `playerId`; if present, its value is ignored. Automated sides cannot be selected by a human request. Java movement conditions and effects remain on the backend.

This replaces `GET .../legal-moves?from=e2` with a POST body carrying the requesting player's UUID, without a color parameter. Selection does not commit a move or create a server-side selection reservation; the move request includes `from` again because it is a separate operation.

## 5. Request a move

`POST /api/sessions/{sessionId}/moves`

```json
{
  "playerId": "f3435c46-1a64-40c5-9b03-4b0bb3390020",
  "from": "e2",
  "moveId": 1,
  "to": "e4"
}
```

`moveId` selects the existing movement rule; `to` selects one legal tile for that rule. Both are required. PvP and practice require `playerId`; AI/CPU requests can omit it.

This request deliberately does not contain raw `Move`, `MoveCondition`, `MoveEffect`, piece color, or a Java `Tile` record. The HTTP adapter translates square strings to `Tile` and resolves the move on the backend.

The backend validates and executes each request atomically within the session:

1. Resolve the session and ensure it is active and ready.
2. For PvP, check that `playerId` belongs to the current side and has registered.
3. For practice, check that `playerId` equals the shared ID. Resolve the acting side from the current turn, not the first matching UUID.
4. For AI/CPU, ignore player ID values and permit human requests only for the human-controlled turn. Reject manual actions in `AIVAI`.
5. Validate `from`/`to` as `[a-h][1-8]` before constructing tiles. Verify that the source piece exists and `moveId` is an integer within its `moveset()` bounds.
6. Resolve `Move move = piece.moveset().get(moveId)`, recompute `updater.pieceMoves(fromTile)`, and require that the list for that exact `move` contains `toTile`. A prior selection response is not authorization; revalidate against the current position.
7. Call `updater.movePieceAndUpdate(move, fromTile, toTile)`, then serialize `session.gameState()`. The updater applies the move's effect and records a `MoveEvent`; the handler must not move pieces directly or append a second event.

The essential rule-resolution step is:

```java
// After validating session, identity, turn, source piece, coordinates and index:
Tile fromTile = new Tile(from);
Tile toTile = new Tile(to);
Piece piece = session.gameState().board().pieceAt(fromTile);
Move move = piece.moveset().get(moveId);
GameUpdater updater = new GameUpdater(session);
List<Tile> destinations = updater.pieceMoves(fromTile).get(move);
if (destinations == null || !destinations.contains(toTile)) {
    // Respond 422; do not execute the move.
    return;
}
updater.movePieceAndUpdate(move, fromTile, toTile);
```

Run validation and execution under the same per-session coordination boundary. `movePieceAndUpdate` checks game status, source presence, and turn, but does not itself check whether `to` is in the legal destination list. Its silent early returns must not be treated as successful HTTP moves.

### Promotion limitation

The current updater has no promotion operation or promotion parameter. Do not advertise an implemented `promotion` request field or silently accept and ignore one. Requests containing unsupported promotion data return `501`; moves reaching a promotion rank also return `501` before mutation until promotion is implemented. A future promotion contract must define validation, replacement, and history updates as one complete move.

Response: `200 OK`, as `{ "state": <state object from section 3> }`. The state contains the updated position and complete history; no player tokens or caller color are returned.

Automated replies, when implemented, can occur after this response and appear in later state polls. Until supported, return `501` rather than accepting an automated game that cannot run.

## Errors

Return consistent JSON errors:

```json
{
  "error": "INVALID_PLAYER_ID",
  "message": "The player ID does not belong to this session."
}
```

| Status | Meaning |
| --- | --- |
| `400` | Missing required player ID or move ID, malformed UUID/square, non-integer/out-of-range move ID, or invalid mode/difficulty |
| `403` | ID belongs to another session, PvP ID has not registered, selected piece belongs to the other player, or manual actions are forbidden in spectator mode |
| `404` | Session does not exist |
| `409` | Wrong turn, waiting for the other player, finished game, or joining a non-PvP session |
| `422` | Source is empty or the chosen destination is not legal for the selected movement rule |
| `501` | Mode, promotion, or operation is not implemented |

Rejected selections/moves leave the position and history unchanged. IDs from another session never authorize PvP/practice actions. AI/CPU modes ignore any supplied player ID.

## Frontend migration and live updates

The JavaScript now accepts text/plain JSON creation responses and uses player IDs in action bodies. It still expects flat legal destinations and submits moves without `moveId`, so it needs a further migration before these proposed movement endpoints can be consumed:

- Creation: read `whiteId` and `blackId`, make the single PvP random assignment, and store the local ID alongside the configuration from the creation request.
- Joining: register through the join endpoint, read the assigned UUID from the invitation, and retain `gamemode`/`difficulty` from the join response. A session-only invitation needs to be replaced or augmented.
- Selection: send `{ playerId, from }`, read `{ moves: [{ moveId, tiles }] }`, and retain the selected rule for each highlighted tile.
- Movement: send `{ playerId, from, moveId, to }` for PvP/practice. Omit the player ID for AI/CPU. Invalidate cached rule/tile pairs when the position changes.
- Identity: keep locally derived color only for board rendering and local interaction hints; do not send it as authority.
- Practice: use the shared UUID for both sides without changing IDs when the turn changes.
- State: continue polling `GET /api/sessions/{sessionId}` for mutable state and history, using locally retained session metadata. Each browser polls its own session.

UI interaction delegates communication to the frontend API adapter. This document does not implement routes, automated opponents, persistence, or backend changes.

## Current implementation differences

- `POST /api/sessions` is registered and returns `201` only for practice; other modes return `501`. Its current response also echoes `gamemode` and `difficulty`, and hardcodes `waitingForOpponent: true`, including for practice. The target contract above avoids those metadata echoes and does not wait for a second player in practice.
- `PostLegalMoves` is unfinished; it must construct `GameUpdater(session)`, not a no-argument updater, and adapt its move-to-tiles map to the DTO above.
- Join, state-read, and move handlers are not registered yet. Their contracts are proposed, not existing capabilities.
- `GameUpdater` inserts `MoveEvent` records at the front of `movesHistory`. The HTTP history DTO must reverse that order to expose chronological moves and use each event's `gameStateFen` for its snapshot. Do not serialize the event's behavioral `Move` object into history.
- Promotion and AI/CPU execution are not implemented in the inspected updater. The contract must not imply that those features already work.
