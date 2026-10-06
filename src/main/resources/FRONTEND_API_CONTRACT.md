# Frontend ↔ backend contract

The current `ChessAPI.java` only serves `/web` as static content. The generated frontend is ready for multiple simultaneous games, but the backend still needs session-scoped API endpoints.

The key rule is: **do not keep one static `GameSession` in `ChessAPI`**. Keep a concurrent registry such as `ConcurrentHashMap<String, GameSessionContext>` keyed by `GameSession.id()`.

## Session identity

A PvP user only needs to type/paste the session UUID. The server should still return an opaque `playerToken` after create/join so it knows whether a request belongs to White or Black. The frontend stores that token in `sessionStorage` and automatically sends it in:

```http
X-Player-Token: <opaque-token>
```

This keeps the UI simple while preventing a client from choosing its own color in requests.

## 1. Create a session

`POST /api/sessions`

Request:

```json
{
  "gamemode": "PVP",
  "difficulty": "NOT_CPU"
}
```

For all non-PvP modes, send `EASY`, `MEDIUM`, or `HARD`. For `PVP`, always send `Difficulty.NOT_CPU` as the string `"NOT_CPU"`.

Response:

```json
{
  "sessionId": "6e26bb12-02ae-4e08-a99f-c0af94d67608",
  "playerToken": "opaque-token-for-white",
  "playerColor": "WHITE",
  "state": { "...": "GameState below" }
}
```

For `AIVAI`, `playerColor` may be `null` because the browser is only observing.

## 2. Join a PvP session

`POST /api/sessions/{sessionId}/join`

Request body can be `{}`.

Response:

```json
{
  "sessionId": "6e26bb12-02ae-4e08-a99f-c0af94d67608",
  "playerToken": "opaque-token-for-black",
  "playerColor": "BLACK",
  "state": { "...": "GameState below" }
}
```

Return `404` if the session does not exist and `409` if the PvP session already has two players.

## 3. Read current state

`GET /api/sessions/{sessionId}`

Preferred response:

```json
{
  "sessionId": "6e26bb12-02ae-4e08-a99f-c0af94d67608",
  "gamemode": "PVP",
  "difficulty": "NOT_CPU",
  "status": "ACTIVE",
  "turn": "WHITE",
  "checkedColor": null,
  "playerColor": "WHITE",
  "waitingForOpponent": false,
  "pieces": [
    { "square": "a1", "color": "WHITE", "type": "ROOK" },
    { "square": "b1", "color": "WHITE", "type": "KNIGHT" }
  ],
  "moves": [
    {
      "ply": 1,
      "color": "WHITE",
      "piece": "PAWN",
      "from": "e2",
      "to": "e4",
      "capture": false,
      "promotion": null,
      "notation": "e4"
    }
  ]
}
```

`status` maps directly to the existing `GameStatus` enum:

- `ACTIVE`
- `WHITE_WIN`
- `BLACK_WIN`
- `DRAW`

When `WHITE_WIN` or `BLACK_WIN` is returned, the frontend shows **Checkmate**. When `DRAW` is returned, it shows **Draw**.

`checkedColor` is optional, but if you expose the existing private `checkedColor` from `GameSession`, the frontend highlights the checked king and displays a check message.

## 4. Ask for legal moves

`GET /api/sessions/{sessionId}/legal-moves?from=e2`

Response:

```json
{
  "moves": ["e3", "e4"]
}
```

The server should call `GameSession.legalPieceMoves(new Tile(from))`, flatten its tile lists, and return only target squares. Keep the Java `Move` object on the server; the browser should not be responsible for serializing `MoveCondition` or `MoveEffect`.

The frontend also accepts this form if you later need metadata:

```json
{
  "moves": [
    { "to": "e3" },
    { "to": "e4" }
  ]
}
```

## 5. Make a move

`POST /api/sessions/{sessionId}/moves`

Request:

```json
{
  "from": "e2",
  "to": "e4"
}
```

For promotion:

```json
{
  "from": "e7",
  "to": "e8",
  "promotion": "QUEEN"
}
```

The backend should:

1. Validate the token/player color for human-controlled modes.
2. Verify that the piece at `from` belongs to `GameSession.colorTurn()`.
3. Get `GameSession.legalPieceMoves(from)`.
4. Find the legal `Move` whose target list contains `to`.
5. Call `GameSession.move(move, from, to)`.
6. If promotion is needed, call `GameSession.promotePieceIn(to, promotion)`.
7. Run the CPU/AI move if the selected mode requires it.
8. Return the new `GameState`.

A valid response can be either the `GameState` directly or:

```json
{ "state": { "...": "GameState" } }
```

## Live updates / simultaneous sessions

The supplied JavaScript polls `GET /api/sessions/{sessionId}` once per second. This makes PvP work without WebSockets and is simple to add to the existing Javalin server. Each browser only polls its own UUID, so many games can run concurrently.

You can later replace polling with a WebSocket/SSE push channel without changing the rendered board/state model.

Recommended backend storage shape:

```java
private final ConcurrentMap<String, GameSessionContext> sessions = new ConcurrentHashMap<>();
```

`GameSessionContext` can hold the `GameSession`, White/Black player tokens, whether a second PvP player joined, timestamps, and any CPU/AI coordination state. Remove abandoned/finished sessions after a suitable timeout.

## Static file locations

The generated files are placed in the project where the current Javalin static-file configuration already expects them:

```text
src/main/resources/web/index.html
src/main/resources/web/styles.css
src/main/resources/web/app.js
```

`index.html` loads `/styles.css` and `/app.js`. With `config.staticFiles.add("/web", Location.CLASSPATH)`, Javalin serves these from the site root.
