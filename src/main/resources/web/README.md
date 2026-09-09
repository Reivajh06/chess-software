# Knightboard frontend

Files:

- `index.html` - application layout.
- `styles.css` - responsive chess-app styling.
- `app.js` - board rendering, selection, legal-move highlighting, API communication, history, captures, promotion, and end-game UI.
- `API_ENDPOINTS.md` - complete Java REST API contract and recommended DTO/enums.

## Run

The frontend expects the API at:

```text
http://localhost:7070/api
```

Change `API_BASE_URL` at the top of `app.js` if necessary.

For development, serve the directory with any static HTTP server rather than opening `index.html` directly with `file://`.

Example with Python:

```bash
python -m http.server 5500
```

Then open:

```text
http://localhost:5500
```

If the frontend and backend use different origins, configure CORS in Javalin. An alternative is to let Javalin serve the static frontend and set `API_BASE_URL = "/api"`.
