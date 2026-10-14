# Chess gameplay diagnostic

The current backend accepts illegal move requests and can misclassify king safety, checkmate, stalemate, and draws. These are blockers for reliable PvP. Backend implementation files are unchanged by this diagnostic; the accompanying changes repair the tests and remove unnecessary frontend selection warnings.

## Verification results

Verified on 14 October 2026 with the installed JDK 25 and Node.js 24.

| Suite | Tests | Passed | Failed | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: | ---: |
| ChessRulesTest | 15 | 3 | 12 | 0 | 0 |
| ChessTestSupportTest | 9 | 9 | 0 | 0 | 0 |
| FenParserTest | 5 | 3 | 2 | 0 | 0 |
| GameSimulationTest | 36 | 19 | 17 | 0 | 0 |
| GameUpdaterCastlingTest | 12 | 8 | 4 | 0 | 0 |
| MoveValidationTest | 7 | 2 | 5 | 0 | 0 |
| Java total | 84 | 44 | 40 | 0 | 0 |
| Frontend | 16 | 16 | 0 | 0 | 0 |

The 40 Java failures are assertions against the unchanged backend, rather than compilation errors or broken move-ID casts. Several tests reproduce the same underlying defect. A passing test at a claim threshold only establishes that the game does not end automatically; it does not establish that draw claims are implemented.

Run from the project root with JDK 25 selected:

```powershell
mvn test
node --test --test-isolation=none src/test/frontend/*.test.mjs
```

Run one area with, for example, `mvn '-Dtest=ChessRulesTest' test`. Maven returns a failing exit code while backend regressions remain. Detailed assertions are in `target/surefire-reports/`.

The test expectations for king movement, castling, material, mate, stalemate, automatic draws, and opening FEN were independently checked using python-chess 1.11.2, installed only under the ignored `target/` directory. Rule expectations follow the relevant [FIDE Laws of Chess](https://handbook.fide.com/chapter/E012023). Standard FEN expectations use the passed square after every double pawn advance, including when no capture is available; this is distinct from the default legal-capture-only convention in [python-chess FEN serialization](https://python-chess.readthedocs.io/en/latest/core.html#chess.Board.fen).

## Confirmed gameplay defects

### Illegal requests bypass move validation

**Priority 1.** `GameUpdater.movePieceAndUpdate` applies the requested piece rule without confirming that its destination is legal, that the path is clear, that the target is an opponent, or that the mover has the turn. `PostMovePiece` calls this method directly. Looking up legal moves beforehand is only a client convention.

`MoveValidationTest` submits these requests directly to the updater from the starting position:

| Request | Expected | Observed |
| --- | --- | --- |
| `e2e5`, rule 0 | Reject the three-square pawn move | Pawn moves to e5 |
| `e7e6`, rule 0 | Reject Black moving on White's turn | Black moves and advances the full-move counter |
| `a1a3`, rule 2 | Reject jumping over the pawn on a2 | Rook jumps to a3 |
| `a1b1`, rule 1 | Reject capturing a friendly knight | Rook replaces the knight |

A pinned rook also moves sideways through this entry point. Invalid requests add history events and change the position. Fixing only legal-move highlighting will not close this path.

### King safety is evaluated at the old square

**Priority 1.** `GameConditions.moveUnchecksKing` simulates the piece effect, but the effect does not update `Board.kingTile`. The king cache is updated later by `GameUpdater`, only for an accepted move. Simulations therefore test the king's origin.

`ChessRulesTest.kingSafetyUsesItsDestination` reproduces both directions of the error:

- `4k3/8/8/8/8/8/3r4/4K3 w - - 0 1`: `e1d1` is offered even though the rook on d2 attacks d1.
- `4r1k1/8/8/8/8/8/8/4K3 w - - 0 1`: the legal escape `e1f1` is rejected.
- `8/8/8/8/8/4k3/8/4K3 w - - 0 1`: `e1e2` is offered, placing the kings next to each other.

The same defect gives losing kings false escape squares. Fool's Mate, Scholar's Mate, and a back-rank rook mate all remain `ACTIVE` in `GameSimulationTest.winsByCheckmate`. The stalemate after `c7b6` from `k7/2Q5/2K5/8/8/8/8/8 w - - 0 1` is also missed.

### Movement generation permits capturing a king

**Priority 1.** `MovesValidator.filterMoveTiles` excludes friendly pieces, but never excludes an opposing king as a destination. The direct-capture effect can remove it.

`ChessRulesTest.aPieceCannotCaptureTheOpposingKing` queries the color-independent controller for a white rook on e2 with the black king on e8. The position correctly has Black to move in check; the test exercises the generator's prohibition on king captures independently of turn gating.

### Queenside castling rights survive rook movement and capture

**Priority 1.** `GameUpdater.updateCastlingRights` checks queenside rights through an `else if`. While kingside rights remain, the queenside rook is never checked. Capturing a rook also does not update the opponent's rights.

From `r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1`, `a1a2` incorrectly leaves `KQkq`; it should leave `Kkq`. `a1a8` incorrectly leaves all four flags; it should leave `Kk`. The mirrored black queenside case also fails. See `GameUpdaterCastlingTest`.

### Castling safety rejects legal castles

**Priority 2.** `MoveRules.QUEENSIDE_CASTLING` requires b1 or b8 to be safe even though the king never crosses that square. In `1r5k/8/8/8/8/8/8/R3K3 w Q - 0 1`, White's queenside castle is wrongly rejected because b1 is attacked.

`MoveRules.SAFE_MOVE` also treats every raw movement vector as an attack. It includes pawn forward and double-step vectors. In `4k3/8/8/8/8/5p2/8/4K2R w K - 0 1`, the pawn on f3 incorrectly prevents `e1g1`. See the two castling safety tests in `ChessRulesTest`.

### En passant metadata points to the wrong square and persists

**Priority 2.** `GameUpdater.updateEnPassantTile` returns an occupied neighboring square rather than the square the pawn passed. It preserves that value on later ordinary moves.

After `e2e4 a7a6 e4e5 d7d5`, the target is e5 instead of d6. After `e5d6`, e5 still remains in the metadata. Without an adjacent piece, a double push produces no target at all, such as `-` instead of e3 after `e2e4`.

The immediate en passant capture itself removes the correct pawn in this sequence, and expired captures are rejected through `lastMove`. The defect is in the stored/exported target, which can corrupt FEN consumers and future repetition keys. See `enPassantTargetAfterDoublePush`, `enPassantCapture`, and `enPassantTargetExpiresAfterAnotherMove`.

### Imported pawns can advance two squares from a middle rank

**Priority 2.** `MoveRules.START_TWO_FORWARD` checks `timesMoved == 0`, but does not require rank 2 or 7. Imported pieces start with zero counters. White's e4 pawn is offered e6 and Black's e5 pawn is offered e3 in the two `pawnDoublePushRequiresItsStartingRank` fixtures. This affects loaded or reconstructed positions; ordinary pawn counters prevent the same error during an uninterrupted opening.

### Promotion is absent

**Priority 1.** A pawn reaching the last rank remains a pawn. `Move`, `PostMovePiece`, and the move effects have no promotion-choice field or replacement operation. The frontend currently blocks promotion submissions, so a real game cannot complete such a move through the board.

`GameSimulationTest.pawnCannotRemainOnLastRank` covers both colors. It permits rejection of an incomplete promotion request without state changes, or acceptance with a valid promoted piece. It requires neither an invented API nor automatic promotion to a particular piece.

### Opposite-color bishops trigger a false dead-position draw

**Priority 1.** `GameConditions.onDeadPosition` treats every king-and-bishop versus king-and-bishop position as dead without checking bishop square colors. `7k/8/8/8/8/8/b7/2B1K3 w - - 0 1` is incorrectly considered dead. `ChessRulesTest.bishopDeadPositionsDependOnSquareColors` includes a passing same-color control.

### Automatic repetition and move-count draws are absent

**Priority 2.** `onThreefoldRepetition` always returns false. Fivefold repetition and the 150-half-move threshold are not implemented. Four repetitions of `g1f3 g8f6 f3g1 f6g8` leave the starting position on its fifth occurrence, but the game stays active. A quiet move from half-move clock 149 also leaves the game active.

Keep claim-based threefold and 100-half-move handling separate from automatic termination. The tests cover the automatic outcomes and checkmate precedence without assuming a nonexistent claim endpoint.

### Board import and copying discard derived state

**Priority 2.** `FenParser.fenToBoard` places kings without initializing their tracked squares. `Board.copy` round-trips through placement-only FEN, losing both king caches and movement counters. `FenParserTest` checks each property independently: a copied rook with one move returns with zero moves, and both king locations are null.

## Additional findings from source inspection

These findings are separate from the reproduced assertions above.

- **State evaluation order:** `GameUpdater.updateGameState` computes the result using the existing state's turn, history, clocks, and castling metadata after mutating its board. In particular, stalemate is evaluated for the player who just moved. Build the complete candidate state before adjudication when implementing the backend repairs.
- **Session authorization:** move and legal-move handlers ignore `playerId`. They do not bind requests to the assigned color or a registered participant.
- **Concurrency and rollback:** `Sessions` uses a concurrent map, but each stored board, deque, and capture list is mutable. Legal queries temporarily mutate the same live board that moves and reads use. Per-session synchronization and transactional updates are absent; the tests for separate sessions do not establish thread safety.
- **Request errors:** unknown sessions, malformed squares, empty origins, and out-of-range move indices can reach unchecked exceptions. Numeric `moveId` values are truncated from `Double`, so a fractional rule ID can select an integer rule. The HTTP layer needs explicit validation and consistent rejection responses.
- **Condition composition:** `MovesValidator` accepts a destination as soon as any condition returns true. Current factory rules each use a single condition, so this is not classified as a reproduced gameplay bug. Define whether a condition list means alternatives or cumulative requirements before extending it.
- **Castling assumptions:** the conditions rely on cached check status and do not independently enforce original king squares or matching rook colors. These are robustness gaps for inconsistent imported state, rather than failures demonstrated from a valid starting game here.
- **FEN restoration:** the production parser supports placement only, not complete session restoration. It also lacks structural input validation. The test fixture loader supplies the remaining state explicitly and is not a new backend import feature.

## Test repairs and scope

Java sources moved from `src/main/test/java` to Maven's standard `src/test/java`. The helpers and tests now use the real integer movement-rule indices. `ChessTestSupport` owns position fixtures and coordinate moves, and nine passing helper tests cover its basic invariants. Fixtures initialize king tracking and active check status; they do not fabricate complete game history or movement counters from FEN.

The CLI now prints history chronologically, compares `lastMove` with the deque head, and compares saved snapshots with complete FEN. These changes remove misleading debugger mismatches. Tests no longer catch arbitrary `Throwable`, assume eight unsupported promotion requests can be executed, or label sequential session checks as concurrency tests. Rejection tests assert unchanged state without prescribing an HTTP status or exception type that has not been defined.

One explicitly defensive fixture retains a stale castling flag with its rook missing. It tests that the absent rook cannot be used; it is not evidence of a valid playable FEN. En passant capture tests establish the preceding double push or supply its last-move event, rather than expecting history to be reconstructed from a target square alone.

The frontend now silently handles selections with no legal destinations and clicks outside highlighted destinations. Selection outlines and available tile highlights remain. Network errors, unsupported promotion, and stale move-choice notices still report actionable problems. No code comments were added.

The next implementation steps are tracked in [the PvP checklist](TODO.md).
