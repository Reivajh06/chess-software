# Chess Web API Contract

This document describes the REST API expected by the provided HTML/CSS/JavaScript frontend.

The frontend intentionally does **not** calculate legal chess moves. Move legality, turn validation, captures, check/checkmate/stalemate/draw detection, color assignment, CPU decisions, and future AI decisions belong to the Java backend.

Base URL used by `app.js`:

```text
http://localhost:7070/api
```

If the frontend is served directly by Javalin, changing `API_BASE_URL` to `/api` is usually cleaner and avoids cross-origin configuration during development.

---

## Endpoint summary

| Method | Endpoint |
|---|---|
| `GET` | `/api/game-options` |
| `POST` | `/api/games` |
| `GET` | `/api/games/{gameId}` |
| `GET` | `/api/games/{gameId}/legal-moves?from={square}` |
| `POST` | `/api/games/{gameId}/moves` |
| `POST` | `/api/games/{gameId}/turns/advance` |

The six endpoints above are enough for the current frontend. A resignation endpoint, draw-offer endpoint, matchmaking endpoint, authentication, game persistence, clocks, and WebSockets can be added later without changing the basic game-state model.

---

# 1. `GET /api/game-options`

## Why it is needed

Provides the configuration values supported by the backend. This prevents the frontend from becoming the source of truth for Java enums such as `GameMode` and `Difficulty`.

The frontend contains fallback values so the setup form can still render while the backend is offline, but the backend response should be authoritative.

## Receives

Nothing.

## Sends to the frontend

```json
{
  "gameModes": [
    { "value": "PLAYER_VS_CPU", "label": "Player vs CPU" },
    { "value": "PLAYER_VS_PLAYER", "label": "Player vs Player" },
    { "value": "PLAYER_VS_AI", "label": "Player vs AI" },
    { "value": "AI_VS_AI", "label": "AI vs AI" }
  ],
  "difficulties": [
    { "value": "EASY", "label": "Easy" },
    { "value": "MEDIUM", "label": "Medium" },
    { "value": "HARD", "label": "Hard" },
    { "value": "EXPERT", "label": "Expert" }
  ]
}
```

## Values and types

### `gameModes`

Type: `Array<OptionData>`

Each item:

| Field | JSON type | Java suggestion | Meaning |
|---|---|---|---|
| `value` | `string` | `GameMode` serialized as text | Machine-readable enum value. |
| `label` | `string` | `String` | Human-readable UI text. |

### `difficulties`

Type: `Array<OptionData>`

Each item has the same structure as above, but `value` corresponds to `Difficulty`.

---

# 2. `POST /api/games`

## Why it is needed

Creates a new chess game using the chosen game mode and optional difficulty.

This is also the right place for the backend to randomize the human player's piece color. The frontend should not choose White or Black itself unless you intentionally add a color preference later.

## Receives

```json
{
  "gameMode": "PLAYER_VS_CPU",
  "difficulty": "MEDIUM"
}
```

## Request fields

| Field | JSON type | Java type | Required | Possible values / notes |
|---|---|---|---|---|
| `gameMode` | `string` | `GameMode` | Yes | `PLAYER_VS_CPU`, `PLAYER_VS_PLAYER`, `PLAYER_VS_AI`, `AI_VS_AI`. |
| `difficulty` | `string` or `null` | `Difficulty` | Depends on mode | Required for CPU/AI modes. `null` is acceptable for `PLAYER_VS_PLAYER`. |

## Sends to the frontend

A complete `GameStateData` object.

Example:

```json
{
  "gameId": "c40ae3b7-07b8-448c-b584-250e8df44862",
  "mode": "PLAYER_VS_CPU",
  "difficulty": "MEDIUM",
  "status": "ACTIVE",
  "drawReason": null,
  "turn": "WHITE",
  "humanColor": "BLACK",
  "boardOrientation": "BLACK",
  "currentController": "CPU",
  "winnerColor": null,
  "controllers": {
    "WHITE": "CPU",
    "BLACK": "HUMAN"
  },
  "board": [
    {
      "id": "white-king",
      "type": "KING",
      "color": "WHITE",
      "square": "e1",
      "hasMoved": false
    }
  ],
  "moveHistory": [],
  "capturedByWhite": [],
  "capturedByBlack": []
}
```

The `board` example contains only one piece to keep the example short. A real initial position should contain all 32 pieces.

---

# 3. `GET /api/games/{gameId}`

## Why it is needed

Returns the current state of an existing game.

This is useful for reloading the page, reconnecting after a temporary network problem, restoring a persisted game, or synchronizing the UI if another client changes the game state later.

## Receives

### Path parameter

| Parameter | JSON/URL type | Java type | Meaning |
|---|---|---|---|
| `gameId` | `string` | `UUID` or `String` | Unique identifier of the game. `UUID` is recommended. |

## Sends to the frontend

A complete `GameStateData` object with exactly the same shape returned by `POST /api/games`.

---

# 4. `GET /api/games/{gameId}/legal-moves?from={square}`

## Why it is needed

When the user selects a piece, the frontend asks the backend where that piece can legally move.

The frontend uses the response to draw a dot in the center of legal empty destination squares and a ring around legal captures.

The backend must account for all chess rules before returning a move:

- Current turn.
- Piece movement rules.
- Blocking pieces.
- Checks and pins.
- Castling restrictions.
- En passant.
- Promotion.
- Any other rule that makes a move legal or illegal.

## Receives

### Path parameter

| Parameter | Type | Java suggestion | Meaning |
|---|---|---|---|
| `gameId` | `string` | `UUID` or `String` | Existing game identifier. |

### Query parameter

| Parameter | Type | Java suggestion | Required | Meaning |
|---|---|---|---|---|
| `from` | `string` | `Square` value object or `String` | Yes | Algebraic board coordinate such as `e2`, `a1`, or `h8`. |

## Sends to the frontend

```json
{
  "from": "e2",
  "moves": [
    {
      "to": "e3",
      "kind": "NORMAL",
      "promotionRequired": false,
      "promotionOptions": []
    },
    {
      "to": "e4",
      "kind": "NORMAL",
      "promotionRequired": false,
      "promotionOptions": []
    }
  ]
}
```

## Response fields

### Root object

| Field | JSON type | Java suggestion | Meaning |
|---|---|---|---|
| `from` | `string` | `Square` | Origin square requested by the frontend. |
| `moves` | `array` | `List<LegalMoveData>` | All legal moves for that piece. Empty list means the piece has no legal moves. |

### `LegalMoveData`

| Field | JSON type | Java suggestion | Possible values / meaning |
|---|---|---|---|
| `to` | `string` | `Square` | Destination square. |
| `kind` | `string` | `MoveKind` | `NORMAL`, `CAPTURE`, `CASTLE_KING_SIDE`, `CASTLE_QUEEN_SIDE`, `EN_PASSANT`, `PROMOTION`. |
| `promotionRequired` | `boolean` | `boolean` | `true` when the frontend must ask which piece the pawn becomes. |
| `promotionOptions` | `array<string>` | `List<PieceType>` | Usually `QUEEN`, `ROOK`, `BISHOP`, `KNIGHT`. Empty when promotion is not needed. |

The frontend does not need the complete future board for every legal move. Returning destinations plus move metadata is enough.

---

# 5. `POST /api/games/{gameId}/moves`

## Why it is needed

Submits the move chosen by a human player.

The backend must validate the move again even if the destination came from `/legal-moves`. Never trust the browser as an authority. Between the selection request and the move request, the game state could theoretically have changed.

## Receives

### Path parameter

| Parameter | Type | Java suggestion | Meaning |
|---|---|---|---|
| `gameId` | `string` | `UUID` or `String` | Existing game identifier. |

### Request body

```json
{
  "from": "e2",
  "to": "e4",
  "promotion": null
}
```

## Request fields

| Field | JSON type | Java type | Required | Meaning |
|---|---|---|---|---|
| `from` | `string` | `Square` or `String` | Yes | Origin square. |
| `to` | `string` | `Square` or `String` | Yes | Destination square. |
| `promotion` | `string` or `null` | `PieceType` | Only for promotion | `QUEEN`, `ROOK`, `BISHOP`, or `KNIGHT`. |

A Java record could be:

```java
public record MoveRequest(
    String from,
    String to,
    PieceType promotion
) {}
```

If `Square` is a custom value object, using `Square from` and `Square to` is even better.

## Sends to the frontend

The **updated complete `GameStateData`** after applying the move.

Returning the complete state is slightly more verbose than returning only the changed squares, but it keeps the frontend simple and avoids synchronization bugs. For a normal chess game the payload remains small.

---

# 6. `POST /api/games/{gameId}/turns/advance`

## Why it is needed

Tells the backend to perform the current turn when that side is controlled by `CPU` or future `AI` logic.

Examples:

- Player vs CPU: after the human move, the frontend calls this endpoint once for the CPU response.
- Player vs AI: same flow once an AI implementation exists.
- AI vs AI: the frontend may call this endpoint repeatedly, manually or on a timer, to advance the match one turn at a time.

This separation is useful because `POST /moves` remains strictly a human move request, while CPU/AI move generation stays on the server.

## Receives

### Path parameter

| Parameter | Type | Java suggestion | Meaning |
|---|---|---|---|
| `gameId` | `string` | `UUID` or `String` | Existing game identifier. |

### Request body

The current frontend sends an empty JSON object:

```json
{}
```

No move coordinates are needed because the backend chooses the move.

## Sends to the frontend

The updated complete `GameStateData` after the CPU/AI move.

If the current controller is `HUMAN`, the backend should reject this request, for example with HTTP `409 Conflict`.

---

# Shared values and Java objects

## `enum PieceColor`

Recommended values:

```java
public enum PieceColor {
    WHITE,
    BLACK
}
```

JSON type: `string`

Possible JSON values:

- `"WHITE"`
- `"BLACK"`

---

## `enum PieceType`

Recommended values:

```java
public enum PieceType {
    KING,
    QUEEN,
    ROOK,
    BISHOP,
    KNIGHT,
    PAWN
}
```

JSON type: `string`

---

## `enum Difficulty`

A simple version:

```java
public enum Difficulty {
    EASY,
    MEDIUM,
    HARD,
    EXPERT
}
```

JSON type: `string`

For a minimax CPU, these values can map to different search depths, time budgets, evaluation quality, or a combination of them.

Do **not** expose a raw minimax depth to the frontend unless you specifically want a developer/debug mode. The public UI should work with meaningful difficulty names.

---

## `enum GameMode`

Recommended values:

```java
public enum GameMode {
    PLAYER_VS_CPU,
    PLAYER_VS_PLAYER,
    PLAYER_VS_AI,
    AI_VS_AI
}
```

JSON type: `string`

### Meaning

| Value | White/Black controllers |
|---|---|
| `PLAYER_VS_CPU` | One `HUMAN`, one `CPU`. Human color is randomized. |
| `PLAYER_VS_PLAYER` | Both `HUMAN`. |
| `PLAYER_VS_AI` | One `HUMAN`, one `AI`. Human color is randomized. |
| `AI_VS_AI` | Both `AI`. |

---

## `enum PlayerController`

I recommend adding this enum even though `GameMode` already exists:

```java
public enum PlayerController {
    HUMAN,
    CPU,
    AI
}
```

Why add it?

`GameMode` explains how the game was configured. `PlayerController` answers a different runtime question: **who is allowed to make the current move?**

That lets the frontend rely on:

```json
"currentController": "HUMAN"
```

instead of duplicating rules such as "in `PLAYER_VS_CPU`, if the human is Black and it is White's turn, then call the CPU endpoint".

The backend remains the source of truth.

---

## `enum GameStatus`

Recommended values:

```java
public enum GameStatus {
    ACTIVE,
    CHECK,
    CHECKMATE,
    STALEMATE,
    DRAW,
    RESIGNED,
    ABORTED
}
```

JSON type: `string`

`ACTIVE` and `CHECK` are non-terminal. The others are terminal in the current frontend.

If you prefer stronger domain modeling, `CHECK` can instead be a separate boolean such as `inCheck`, leaving `GameStatus` only for lifecycle state. That approach is arguably cleaner because check is a property of a position, not a game lifecycle state.

---

## `enum DrawReason`

Recommended values:

```java
public enum DrawReason {
    STALEMATE,
    INSUFFICIENT_MATERIAL,
    THREEFOLD_REPETITION,
    FIFTY_MOVE_RULE,
    AGREEMENT
}
```

JSON type: `string` or `null`

`null` when the game is not a draw.

Note: if you keep `STALEMATE` as a separate `GameStatus`, it is acceptable for `drawReason` to be `null` for stalemate. The frontend already supports that distinction.

---

## `enum MoveKind`

Recommended values:

```java
public enum MoveKind {
    NORMAL,
    CAPTURE,
    CASTLE_KING_SIDE,
    CASTLE_QUEEN_SIDE,
    EN_PASSANT,
    PROMOTION
}
```

JSON type: `string`

This is more useful than making the frontend infer special moves from coordinates.

---

## `record PieceData`

I recommend sending a DTO/record to the browser rather than serializing your internal chess `Piece` domain object directly.

```java
public record PieceData(
    String id,
    PieceType type,
    PieceColor color,
    String square,
    boolean hasMoved
) {}
```

### Fields

| Field | JSON type | Java type | Meaning |
|---|---|---|---|
| `id` | `string` | `String` or `UUID` | Stable piece identifier. Useful for animation/debugging. |
| `type` | `string` | `PieceType` | Piece kind. |
| `color` | `string` | `PieceColor` | White or Black. |
| `square` | `string` | `Square` or `String` | Current position, for example `e4`. |
| `hasMoved` | `boolean` | `boolean` | Useful for king/rook/pawn rule state. The frontend does not use it directly, but it can be useful in debugging or persistence. |

If your engine already stores castling rights and en-passant state elsewhere, `hasMoved` does not have to be exposed to the frontend.

---

## `record MoveData`

Recommended DTO:

```java
public record MoveData(
    int ply,
    String from,
    String to,
    PieceType pieceType,
    PieceColor pieceColor,
    PieceType capturedPieceType,
    PieceType promotion,
    MoveKind kind,
    String san
) {}
```

### Fields

| Field | JSON type | Java type | Nullable | Meaning |
|---|---|---|---|---|
| `ply` | `number` | `int` | No | Half-move number: 1 for White's first move, 2 for Black's first move, etc. |
| `from` | `string` | `Square` or `String` | No | Origin square. |
| `to` | `string` | `Square` or `String` | No | Destination square. |
| `pieceType` | `string` | `PieceType` | No | Piece moved. |
| `pieceColor` | `string` | `PieceColor` | No | Color that moved. |
| `capturedPieceType` | `string` or `null` | `PieceType` | Yes | Captured piece, if any. |
| `promotion` | `string` or `null` | `PieceType` | Yes | Promotion result, if any. |
| `kind` | `string` | `MoveKind` | No | Type of move. |
| `san` | `string` | `String` | Preferably no | Standard Algebraic Notation, such as `Nf3`, `exd5`, `O-O`, `Qh7#`. |

### Why `san` is worth adding

The frontend can technically display `e2-e4`, but proper SAN makes the move history look like a real chess application. Generating SAN in the backend is better because it already knows check, mate, captures, promotion, and disambiguation.

---

## `record CapturedPieceData`

You can simply reuse `PieceData`, but the frontend only needs the captured piece's type and color.

A smaller record is enough:

```java
public record CapturedPieceData(
    PieceType type,
    PieceColor color
) {}
```

---

## `record GameStateData`

This is the most important DTO in the API.

A Java shape could be:

```java
public record GameStateData(
    UUID gameId,
    GameMode mode,
    Difficulty difficulty,
    GameStatus status,
    DrawReason drawReason,
    PieceColor turn,
    PieceColor humanColor,
    PieceColor boardOrientation,
    PlayerController currentController,
    PieceColor winnerColor,
    Map<PieceColor, PlayerController> controllers,
    List<PieceData> board,
    List<MoveData> moveHistory,
    List<CapturedPieceData> capturedByWhite,
    List<CapturedPieceData> capturedByBlack
) {}
```

### Field reference

| Field | JSON type | Java type | Nullable | Meaning |
|---|---|---|---|---|
| `gameId` | `string` | `UUID` | No | Unique game identifier. |
| `mode` | `string` | `GameMode` | No | Game configuration. |
| `difficulty` | `string` or `null` | `Difficulty` | Yes | No difficulty is needed for normal PvP. |
| `status` | `string` | `GameStatus` | No | Current game/position status. |
| `drawReason` | `string` or `null` | `DrawReason` | Yes | Reason for a draw. |
| `turn` | `string` | `PieceColor` | No while game exists | Side whose turn it is. |
| `humanColor` | `string` or `null` | `PieceColor` | Yes | Human color for one-human modes. `null` for AI vs AI. |
| `boardOrientation` | `string` | `PieceColor` | No | Which color should appear at the bottom of the board. |
| `currentController` | `string` | `PlayerController` | No | Who must produce the current move. |
| `winnerColor` | `string` or `null` | `PieceColor` | Yes | Winner for decisive terminal results. |
| `controllers` | `object` | `Map<PieceColor, PlayerController>` | No | Controller assigned to each side. |
| `board` | `array` | `List<PieceData>` | No | Every piece currently on the board. |
| `moveHistory` | `array` | `List<MoveData>` | No | Ordered move history. |
| `capturedByWhite` | `array` | `List<CapturedPieceData>` | No | Black pieces captured by White. |
| `capturedByBlack` | `array` | `List<CapturedPieceData>` | No | White pieces captured by Black. |

---

# Objects I would add or change

Your proposed objects are a good base, but I would make a few changes.

## 1. Keep internal `Piece`, but expose `PieceData`

`Object Piece` should probably be a domain abstraction, not a generic `Object`.

Common options:

- An abstract/sealed `Piece` type with subclasses for `King`, `Queen`, etc.
- One immutable `Piece` class containing `PieceType` + `PieceColor`, while movement logic lives elsewhere.
- Separate movement-strategy objects.

For the REST API, avoid serializing the engine object directly. Use `PieceData` so the API is stable even if the engine design changes.

## 2. Add `Square`

Passing strings such as `"e4"` everywhere is convenient for JSON but weak inside Java.

A small value object can validate coordinates once:

```java
public record Square(char file, int rank) {
    // validate a-h and 1-8
}
```

You can configure Jackson to serialize it as `"e4"`, or simply convert to/from strings at the API boundary.

## 3. Add `GameStatus`

Do not make the frontend determine checkmate, stalemate, or draw. The backend should return an explicit status.

## 4. Add `PlayerController`

This removes controller-selection logic from JavaScript and will make future modes much easier.

## 5. Add `MoveKind`

The frontend should not infer whether a legal move is a normal move, capture, castling, en passant, or promotion.

## 6. Add `DrawReason`

A generic `DRAW` result is not enough if you want the UI to explain what happened.

## 7. Consider `PlayerSettings` later

A single `Difficulty` field is fine for the modes in the current frontend. However, `AI_VS_AI` may eventually need separate settings for each side.

At that point, prefer something like:

```java
public record PlayerSettings(
    PlayerController controller,
    Difficulty difficulty
) {}
```

and a game configuration containing separate White/Black settings.

That is more flexible than continuously adding fields such as `whiteDifficulty`, `blackDifficulty`, `whiteAiModel`, and `blackAiModel`.

---

# HTTP status codes

Recommended behavior:

| Status | When to use it |
|---|---|
| `200 OK` | Successful GET, successful move, successful CPU/AI turn. |
| `201 Created` | New game created by `POST /api/games`. |
| `400 Bad Request` | Invalid JSON, malformed square, missing required field, invalid enum value. |
| `404 Not Found` | Unknown `gameId`. |
| `409 Conflict` | Move is not legal in the current state, wrong player's turn, human tries to use AI-turn endpoint, game already finished. |
| `500 Internal Server Error` | Unexpected backend failure. |

A useful error body:

```json
{
  "code": "ILLEGAL_MOVE",
  "message": "The move e2-e5 is not legal in the current position."
}
```

Suggested Java record:

```java
public record ApiError(
    String code,
    String message
) {}
```

---

# Important backend rules

1. **The backend owns legality.** The browser only displays legal moves returned by the backend.
2. **Validate every submitted move again.** Never trust the frontend merely because it previously asked for legal moves.
3. **The backend owns turn order.** Return `turn` and `currentController` after every state change.
4. **The backend owns game termination.** Checkmate, stalemate, draw conditions, resignation, and future timeout logic should be decided server-side.
5. **Randomize the human color when creating the game.** Return the result in `humanColor` and `boardOrientation`.
6. **Prefer complete game-state responses after mutations.** Chess state is small, and this greatly simplifies frontend synchronization.
7. **Keep domain objects separate from API DTOs.** Your engine can evolve without breaking the frontend contract.

---

# Optional future endpoints

These are not required by the current frontend, but are reasonable later:

```text
POST   /api/games/{gameId}/resign
POST   /api/games/{gameId}/draw-offers
POST   /api/games/{gameId}/draw-offers/accept
POST   /api/games/{gameId}/draw-offers/decline
DELETE /api/games/{gameId}
GET    /api/games/{gameId}/pgn
```

For real-time remote PvP, prefer adding a WebSocket/SSE channel rather than aggressively polling `GET /games/{gameId}`.
