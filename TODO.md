# PvP implementation checklist

The frontend already provides invitations, player IDs, board orientation, state polling, and indexed move requests. The backend currently returns `501` for PvP creation and has no join endpoint. Complete the rule corrections in [the gameplay diagnostic](CHESS_DIAGNOSTIC.md) before treating a match result as reliable.

## Chess rules and authoritative moves

- [ ] Validate each submitted move against the current position on the server: origin, rule ID, destination, blockers, friendly occupancy, turn, king safety, and active status.
- [ ] Correct king-square tracking during simulation and undo; prevent king captures and adjacent kings.
- [ ] Correct checkmate, stalemate, dead-position detection, and the state used for adjudication.
- [ ] Revoke each castling right independently when its king or original rook moves, and when that rook is captured. Correct castling attack checks.
- [ ] Correct en passant targets and expiration; require the pawn's starting rank for a double push.
- [ ] Add a promotion choice to the domain and HTTP contract, implement all four choices for both colors, and connect the frontend promotion dialog.
- [ ] Implement threefold and fifty-move claims, fivefold and seventy-five-move automatic draws, and checkmate precedence. Repetition identity must include the side to move, rights, and relevant en passant availability.
- [ ] Preserve king locations and movement counters in board copies. Define complete session serialization if restoring matches is supported.
- [ ] Make the existing Java regression suite pass through backend fixes, then add coverage for the new promotion and claim APIs.

## Creating and joining a match

- [ ] Make `POST /api/sessions` accept `PVP` and return `201` only for a successfully created room. Reject unsupported modes before storing a session.
- [ ] Implement `POST /api/sessions/{sessionId}/join` to match `SessionAccess.open` and `ChessApi.join`.
- [ ] Record which white and black assignments have joined. Make repeat joins by the same participant idempotent and reject an occupied seat or an invalid invitation.
- [ ] Keep a room waiting until both participants join, prevent moves while waiting, and return authoritative `waitingForOpponent` in state responses.
- [ ] Confirm the creator's assigned color, guest assignment, and invitation behavior with the frontend. Define when invites expire and whether they can be reused.

## Identity and request validation

- [ ] Enforce the acting `playerId` on move and legal-move requests, resolving its color on the server. Define spectator read access separately.
- [ ] Decide how player IDs act as credentials, how a returning player recovers their seat, and how IDs are kept out of public state/history responses.
- [ ] Validate JSON types, enum values, UUIDs, square strings, integer rule IDs, session existence, and lifecycle state before any mutation.
- [ ] Define consistent JSON errors for invalid requests, unauthorized actors, full rooms, missing sessions, stale positions, and completed games; align frontend handling with those errors.

## Synchronization and recovery

- [ ] Serialize moves, legal queries, and consistent reads within each session. A concurrent session map alone does not protect its game state.
- [ ] Commit accepted moves atomically, including captures, counters, rights, history, and result; leave the session unchanged when an update fails.
- [ ] Add a position revision to state and move requests. Reject stale requests and prevent duplicate submissions or retries from executing twice.
- [ ] Keep the frontend's current POST state-polling route working, or update the client and contract together if adopting GET, server events, or WebSockets.
- [ ] Ensure both clients receive matching FEN, chronological display history, turn, check, result, and waiting state after every accepted move.
- [ ] Define room expiration, abandoned-match cleanup, refresh/reconnect behavior, and whether matches survive a server restart.

## Ending a match and optional features

- [ ] Add resignation and draw offer/accept/decline actions, with actor and lifecycle validation.
- [ ] Publish the terminal result to both clients and reject subsequent moves.
- [ ] Choose a disconnect policy and expose enough state for clients to explain it.
- [ ] If timed games are in scope, implement server-owned clocks, increments, timeout results, and reconnect reconciliation. Otherwise explicitly support untimed PvP first.
- [ ] If rematches are in scope, define rematch consent and new color assignments.

## PvP acceptance checks

- [ ] Create a room in one browser and join its invitation in another; both show opposite assigned colors and leave the waiting state together.
- [ ] Verify that an unregistered caller cannot move, each participant controls only their assigned color, and the inactive player cannot move early.
- [ ] Submit forged destinations and rule IDs directly to HTTP endpoints; verify an unchanged position, history, captures, and turn.
- [ ] Race two moves and a legal query against one session; verify exactly one valid transition and matching client state.
- [ ] Reload and reconnect each participant without losing or stealing a seat, duplicating a move, or replaying stale state.
- [ ] Complete captures, both castling sides, en passant, all promotion choices, mate, stalemate, resignation, agreed draws, and rule-based draws from both clients.
- [ ] Verify missing/expired rooms, duplicate joins, a third participant, malformed requests, and post-result moves return the documented errors.
- [ ] Keep ordinary piece selection silent when no destinations are available, and verify legal tile highlights remain visible when moves exist.

The frontend API tests currently mock PvP creation and joining. Their passing results verify client expectations; they do not establish that those server endpoints exist. Add real HTTP and two-client integration tests as the backend gains these features.
