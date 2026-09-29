import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import software.ulpgc.gamelogic.Difficulty;
import software.ulpgc.gamelogic.GameSession;
import software.ulpgc.gamelogic.GameStatus;
import software.ulpgc.gamelogic.Gamemode;
import software.ulpgc.model.PieceType;
import software.ulpgc.model.Tile;
import software.ulpgc.model.movement.Move;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Twenty-five complete chess-match simulations starting from the normal initial board.
 *
 * The purpose of this class is deliberately end-to-end rather than microscopic:
 * each test describes a complete game, enables GameSession debug mode, lets
 * GameSession print the board after every successful move, prints the session
 * status after every ply, and finally checks that the match reaches the expected
 * winner/draw.
 *
 * Moves use coordinate notation:
 *     e2e4   = move from E2 to E4
 *     e1g1   = white kingside castling
 *     e1c1   = white queenside castling
 *     b7a8q  = reach A8 and promote to queen
 *
 * IMPORTANT ABOUT PROMOTION:
 * Promotion is represented by a fifth character in the encoded move:
 *     q = queen, r = rook, b = bishop, n = knight.
 * After the pawn move reaches the final rank, the runner calls
 * GameSession.promotePieceIn(destination, requestedType), matching the way the
 * frontend/application is expected to complete promotion.
 *
 * IMPORTANT ABOUT DRAWS:
 * Matches 19 and 20 end by threefold repetition. Matches 21 through 25 end
 * by stalemate. All seven draw matches expect GameStatus.DRAW. If draw or
 * stalemate detection has not been implemented in GameSession yet, the related
 * tests are expected to remain red until it is implemented.
 */
@TestMethodOrder(MethodOrderer.DisplayName.class)
class ChessMatchSimulationTests {

    // ---------------------------------------------------------------------
    // 25 independent JUnit matches
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("01 - Fool's Mate - Black wins")
    void match01FoolsMate() {
        runMatch(match01());
    }

    @Test
    @DisplayName("02 - Scholar's Mate - White wins")
    void match02ScholarsMate() {
        runMatch(match02());
    }

    @Test
    @DisplayName("03 - Legal's Mate - White wins through a pin tactic")
    void match03LegalsMate() {
        runMatch(match03());
    }

    @Test
    @DisplayName("04 - Blackburne Shilling Trap - Black wins")
    void match04BlackburneShilling() {
        runMatch(match04());
    }

    @Test
    @DisplayName("05 - White castles kingside and wins")
    void match05WhiteKingsideCastle() {
        runMatch(match05());
    }

    @Test
    @DisplayName("06 - Black castles kingside and wins")
    void match06BlackKingsideCastle() {
        runMatch(match06());
    }

    @Test
    @DisplayName("07 - White castles queenside and wins")
    void match07WhiteQueensideCastle() {
        runMatch(match07());
    }

    @Test
    @DisplayName("08 - Black castles queenside; White later wins")
    void match08BlackQueensideCastle() {
        runMatch(match08());
    }

    @Test
    @DisplayName("09 - White executes en passant; Black wins")
    void match09WhiteEnPassant() {
        runMatch(match09());
    }

    @Test
    @DisplayName("10 - Black executes en passant; White wins")
    void match10BlackEnPassant() {
        runMatch(match10());
    }

    @Test
    @DisplayName("11 - Promotion to queen occurs; Black eventually wins")
    void match11Promotion() {
        runMatch(match11());
    }

    @Test
    @DisplayName("12 - Italian-style game; both sides castle; Black wins")
    void match12ItalianBothCastle() {
        runMatch(match12());
    }

    @Test
    @DisplayName("13 - Absolute pin occurs; White wins")
    void match13AbsolutePin() {
        runMatch(match13());
    }

    @Test
    @DisplayName("14 - Caro-Kann style game - Black wins")
    void match14CaroKann() {
        runMatch(match14());
    }

    @Test
    @DisplayName("15 - Scandinavian style game - Black wins")
    void match15Scandinavian() {
        runMatch(match15());
    }

    @Test
    @DisplayName("16 - Pirc style game - White wins")
    void match16Pirc() {
        runMatch(match16());
    }

    @Test
    @DisplayName("17 - English opening style game - White wins")
    void match17English() {
        runMatch(match17());
    }

    @Test
    @DisplayName("18 - Dutch style game - White wins")
    void match18Dutch() {
        runMatch(match18());
    }

    @Test
    @DisplayName("19 - Threefold repetition from the initial position - Draw")
    void match19ThreefoldRepetition() {
        runMatch(match19());
    }

    @Test
    @DisplayName("20 - Threefold repetition after e4 e5 - Draw")
    void match20ThreefoldAfterOpening() {
        runMatch(match20());
    }

    @Test
    @DisplayName("21 - Sam Loyd ten-move stalemate - Draw")
    void match21SamLoydStalemate() {
        runMatch(match21());
    }

    @Test
    @DisplayName("22 - Sam Loyd reordered stalemate - Draw")
    void match22SamLoydReorderedStalemate() {
        runMatch(match22());
    }

    @Test
    @DisplayName("23 - Alternate opening into ten-move stalemate - Draw")
    void match23AlternateOpeningStalemate() {
        runMatch(match23());
    }

    @Test
    @DisplayName("24 - Frederick Rhine ten-move stalemate - Draw")
    void match24FrederickRhineStalemate() {
        runMatch(match24());
    }

    @Test
    @DisplayName("25 - All-pieces stalemate - Draw")
    void match25AllPiecesStalemate() {
        runMatch(match25());
    }

    // ---------------------------------------------------------------------
    // Match runner
    // ---------------------------------------------------------------------

    private void runMatch(MatchSpec spec) {
        printMatchPlan(spec);

        GameSession game = new GameSession(Gamemode.PVP, Difficulty.NOT_CPU);
        game.setDebug(true);

        System.out.println("INITIAL BOARD");
        game.printBoard();
        printState(game, 0, "Before the first move");

        for (int index = 0; index < spec.moves().size(); index++) {
            int ply = index + 1;
            String encoded = spec.moves().get(index);
            String fromText = encoded.substring(0, 2);
            String toText = encoded.substring(2, 4);
            boolean finalMove = ply == spec.moves().size();

            if (game.status() != GameStatus.ACTIVE) {
                fail("Match ended too early before ply " + ply + " (" + formatMove(ply, encoded, spec) + "). "
                        + "Expected the game to remain ACTIVE until the planned ending, but status was "
                        + game.status() + ".");
            }

            System.out.println();
            System.out.println("------------------------------------------------------------");
            System.out.println("APPLYING " + formatMove(ply, encoded, spec));
            System.out.println("------------------------------------------------------------");

            Tile from = new Tile(fromText);
            Tile to = new Tile(toText);

            Map<Move, List<Tile>> legalMoves;
            try {
                legalMoves = game.legalPieceMoves(from);
            } catch (RuntimeException exception) {
                fail("Legal move generation threw " + exception.getClass().getSimpleName()
                        + " before ply " + ply + " (" + fromText + " -> " + toText + "): "
                        + exception.getMessage(), exception);
                return;
            }

            Optional<Move> selectedMove = legalMoves.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().contains(to))
                    .map(Map.Entry::getKey)
                    .findFirst();

            if (selectedMove.isEmpty()) {
                List<Tile> legalTargets = legalMoves.values()
                        .stream()
                        .flatMap(List::stream)
                        .distinct()
                        .toList();

                fail("Planned move " + ply + " is not legal according to GameSession: "
                        + fromText.toUpperCase() + " -> " + toText.toUpperCase()
                        + ".\nExpected because: " + explanationFor(spec, ply, encoded)
                        + "\nActual legal targets from " + fromText.toUpperCase() + ": " + legalTargets);
            }

            try {
                game.move(selectedMove.orElseThrow(), from, to);

                Optional<PieceType> promotion = promotionTypeFor(encoded);
                if (promotion.isPresent()) {
                    PieceType promotedType = promotion.orElseThrow();
                    System.out.println("PROMOTION REQUEST: " + toText.toUpperCase()
                            + " -> " + promotedType);
                    game.promotePieceIn(to, promotedType);
                }
            } catch (RuntimeException exception) {
                fail("Execution threw " + exception.getClass().getSimpleName()
                        + " on ply " + ply + " (" + fromText + " -> " + toText + "): "
                        + exception.getMessage(), exception);
            }

            // GameSession itself printed the board because debug mode is enabled.
            // Promotion is then applied through the public GameSession API, just
            // as the frontend/application would do after the pawn reaches rank 8/1.
            // We add the non-board state immediately afterwards.
            printState(game, ply, explanationFor(spec, ply, encoded));

            runCheckpoints(spec, game, ply);

            if (!finalMove && game.status() != GameStatus.ACTIVE) {
                fail("Game reached terminal status " + game.status() + " after ply " + ply
                        + ", but more moves were expected. Next planned move was "
                        + spec.moves().get(index + 1) + ".");
            }
        }

        System.out.println();
        System.out.println("============================================================");
        System.out.println("FINAL RESULT CHECK");
        System.out.println("Expected: " + spec.expectedStatus());
        System.out.println("Actual:   " + game.status());
        System.out.println("Reason:   " + spec.expectedResultExplanation());
        System.out.println("============================================================");

        assertEquals(
                spec.expectedStatus(),
                game.status(),
                "Final GameStatus differs from the expected result for: " + spec.name()
        );
    }

    private void runCheckpoints(MatchSpec spec, GameSession game, int ply) {
        for (Checkpoint checkpoint : spec.checkpoints()) {
            if (checkpoint.afterPly() != ply) {
                continue;
            }

            System.out.println();
            System.out.println("CHECKPOINT after ply " + ply + ": " + checkpoint.explanation());

            Map<Move, List<Tile>> moves;
            try {
                moves = game.legalPieceMoves(new Tile(checkpoint.tile()));
            } catch (RuntimeException exception) {
                fail("Checkpoint legal-move generation threw " + exception.getClass().getSimpleName()
                        + " for " + checkpoint.tile().toUpperCase() + ": " + exception.getMessage(), exception);
                return;
            }

            List<Tile> targets = moves.values()
                    .stream()
                    .flatMap(List::stream)
                    .distinct()
                    .toList();

            System.out.println("Legal targets for " + checkpoint.tile().toUpperCase() + ": " + targets);

            if (checkpoint.expectNoLegalMoves()) {
                assertFalse(
                        !targets.isEmpty(),
                        "Expected no legal moves from " + checkpoint.tile().toUpperCase()
                                + " because: " + checkpoint.explanation()
                                + ". Actual targets: " + targets
                );
            }
        }
    }

    private void printMatchPlan(MatchSpec spec) {
        System.out.println();
        System.out.println();
        System.out.println("############################################################");
        System.out.println("MATCH: " + spec.name());
        System.out.println("EXPECTED RESULT: " + spec.expectedStatus());
        System.out.println("WHY: " + spec.expectedResultExplanation());
        System.out.println("FEATURES: " + String.join(", ", spec.features()));
        System.out.println("############################################################");
        System.out.println("PLANNED MOVES");

        for (int index = 0; index < spec.moves().size(); index++) {
            int ply = index + 1;
            System.out.println(formatMove(ply, spec.moves().get(index), spec));
        }

        System.out.println("############################################################");
    }

    private void printState(GameSession game, int ply, String context) {
        System.out.println("[SESSION STATE AFTER PLY " + ply + "]");
        System.out.println("  Context:      " + context);
        System.out.println("  GameStatus:   " + game.status());
        System.out.println("  Next turn:    " + game.colorTurn());
        System.out.println("  Moves stored: " + game.movesHistory().size());
    }

    private String formatMove(int ply, String encoded, MatchSpec spec) {
        String side = ply % 2 == 1 ? "White" : "Black";
        int fullMove = (ply + 1) / 2;
        String prefix = String.format("%02d%s", fullMove, ply % 2 == 1 ? "." : "...");
        String from = encoded.substring(0, 2).toUpperCase();
        String to = encoded.substring(2, 4).toUpperCase();

        String promotionSuffix = promotionTypeFor(encoded)
                .map(type -> " = " + type)
                .orElse("");

        return prefix + " " + side + " " + from + " -> " + to + promotionSuffix
                + " | " + explanationFor(spec, ply, encoded);
    }

    private Optional<PieceType> promotionTypeFor(String encoded) {
        if (encoded.length() == 4) {
            return Optional.empty();
        }

        if (encoded.length() != 5) {
            throw new IllegalArgumentException(
                    "Moves must use four characters, or five when a promotion is requested: " + encoded
            );
        }

        return Optional.of(switch (Character.toLowerCase(encoded.charAt(4))) {
            case 'q' -> PieceType.QUEEN;
            case 'r' -> PieceType.ROOK;
            case 'b' -> PieceType.BISHOP;
            case 'n' -> PieceType.KNIGHT;
            default -> throw new IllegalArgumentException(
                    "Unsupported promotion suffix in move " + encoded
                            + ". Use q, r, b, or n."
            );
        });
    }

    private String explanationFor(MatchSpec spec, int ply, String encoded) {
        String explicit = spec.moveExplanations().get(ply);
        if (explicit != null) {
            return explicit;
        }

        String side = ply % 2 == 1 ? "White" : "Black";
        String from = encoded.substring(0, 2).toUpperCase();
        String to = encoded.substring(2, 4).toUpperCase();

        return side + " moves the piece from " + from + " to " + to
                + "; the move is part of the legal continuation of this match.";
    }

    // ---------------------------------------------------------------------
    // Match definitions
    // ---------------------------------------------------------------------

    private MatchSpec match01() {
        return match(
                "Fool's Mate",
                GameStatus.BLACK_WIN,
                "Black's queen reaches H4 and checkmates the exposed white king.",
                List.of("fast checkmate", "queen checkmate", "pawn two-square move"),
                "f2f3 e7e5 g2g4 d8h4",
                notes(
                        1, "White weakens the E1-H4 diagonal with F2-F3.",
                        2, "Black takes the center with E7-E5, opening the queen's diagonal.",
                        3, "White weakens the king further with G2-G4.",
                        4, "Black plays Qd8-H4#, delivering checkmate along the diagonal to E1."
                )
        );
    }

    private MatchSpec match02() {
        return match(
                "Scholar's Mate",
                GameStatus.WHITE_WIN,
                "White's queen captures on F7 with support from the bishop on C4, producing checkmate.",
                List.of("fast checkmate", "queen capture", "bishop support", "check"),
                "e2e4 e7e5 f1c4 b8c6 d1h5 g8f6 h5f7",
                notes(
                        1, "White advances E2-E4, freeing the queen and F1 bishop.",
                        2, "Black answers symmetrically with E7-E5.",
                        3, "White develops the bishop to C4 and targets F7.",
                        4, "Black develops the B8 knight to C6.",
                        5, "White queen moves to H5, joining the attack on F7.",
                        6, "Black develops the G8 knight but does not stop the mating threat.",
                        7, "White plays Qh5xF7#, capturing on F7 and checkmating the black king."
                )
        );
    }

    private MatchSpec match03() {
        return match(
                "Legal's Mate",
                GameStatus.WHITE_WIN,
                "White sacrifices the queen after the bishop's apparent pin and finishes with Nd5#.",
                List.of("pin tactic", "queen sacrifice", "bishop check", "knight checkmate"),
                "e2e4 e7e5 g1f3 b8c6 f1c4 d7d6 b1c3 c8g4 f3e5 g4d1 c4f7 e8e7 c3d5",
                notes(
                        8, "Black bishop goes to G4 and pins the F3 knight to White's queen on D1 (a relative pin, not an absolute king pin).",
                        9, "White deliberately moves the apparently pinned knight to E5, allowing the queen to be taken.",
                        10, "Black accepts the queen sacrifice with Bg4xD1.",
                        11, "White bishop captures F7 with check, forcing the black king.",
                        12, "Black king moves E8-E7 to escape the bishop check.",
                        13, "White knight C3-D5# completes the mating net."
                )
        );
    }

    private MatchSpec match04() {
        return match(
                "Blackburne Shilling Trap",
                GameStatus.BLACK_WIN,
                "Black's knight reaches F3 and checkmates after White's king position is opened.",
                List.of("opening trap", "queen invasion", "knight checkmate", "checks"),
                "e2e4 e7e5 g1f3 b8c6 f1c4 c6d4 f3e5 d8g5 e5f7 g5g2 h1f1 g2e4 c4e2 d4f3",
                notes(
                        6, "Black's C6 knight jumps to D4, offering the characteristic trap.",
                        8, "Black queen moves to G5, attacking the exposed kingside and G2.",
                        10, "Black queen captures G2 and penetrates the white position.",
                        12, "Black queen captures E4 with check.",
                        14, "Black knight D4-F3# delivers the final checkmate."
                )
        );
    }

    private MatchSpec match05() {
        return match(
                "White kingside castling tactical match",
                GameStatus.WHITE_WIN,
                "White castles kingside on move 4 and eventually mates with the queen on D6.",
                List.of("WHITE O-O", "king movement", "rook activation", "checks", "checkmate"),
                "e2e4 e7e5 g1f3 b8c6 f1c4 g8f6 e1g1 f8c5 c4f7 e8e7 f3e5 e7f8 b1c3 c5f2 g1f2 c6e5 d1e2 e5d3 f2g3 d3c1 f1f6 c1a2 f7a2 f8e7 h2h4 h8f8 c3d5 e7e8 e2h5 f8f7 d5c7 e8e7 h5c5 e7f6 c5d6",
                notes(
                        7, "White castles kingside: king E1-G1 and rook H1-F1 must move as one castling move.",
                        35, "White queen C5-D6# supplies the final checkmate."
                )
        );
    }

    private MatchSpec match06() {
        return match(
                "Black kingside castling tactical match",
                GameStatus.BLACK_WIN,
                "Both sides castle kingside; Black eventually mates with Qf3#.",
                List.of("WHITE O-O", "BLACK O-O", "captures", "checks", "black checkmate"),
                "e2e4 e7e5 g1f3 b8c6 f1c4 g8f6 e1g1 f8e7 d2d3 e8g8 c4f7 g8f7 c1e3 d7d5 f3e5 c6e5 d1h5 f6h5 e3a7 a8a7 c2c3 e5f3 g2f3 d5e4 f3f4 e4d3 f1d1 b7b5 b1d2 h5f4 a2a4 a7a4 d2b1 f4e2 g1h1 d8d5 f2f3 e2c3 a1a4 c3a4 h1g1 e7c5 g1h1 d5f3",
                notes(
                        7, "White castles kingside.",
                        10, "Black castles kingside: king E8-G8 and rook H8-F8.",
                        44, "Black queen D5-F3# ends the game with checkmate."
                )
        );
    }

    private MatchSpec match07() {
        return match(
                "White queenside castling tactical match",
                GameStatus.WHITE_WIN,
                "White castles queenside and later checkmates with Qf6-E6#.",
                List.of("WHITE O-O-O", "long castling", "checks", "queen checkmate"),
                "d2d4 d7d5 b1c3 g8f6 c1f4 e7e6 d1d2 f8e7 e1c1 e7d6 e2e4 d6f4 c3d5 f6d5 e4d5 f4h2 f1b5 e8e7 d5e6 e7e6 d1e1 h2e5 b5c4 e6d6 d2h6 g7g6 h6g6 f7f6 d4e5 d6c6 g6f6 c8e6 c4d5 c6d7 f6e6",
                notes(
                        9, "White castles queenside: king E1-C1 and rook A1-D1.",
                        35, "White queen F6-E6# gives the final mate."
                )
        );
    }

    private MatchSpec match08() {
        return match(
                "Both sides queenside castle; White wins",
                GameStatus.WHITE_WIN,
                "White and Black both castle queenside; White later invades with Qd7#.",
                List.of("WHITE O-O-O", "BLACK O-O-O", "piece captures", "queen checkmate"),
                "d2d4 d7d5 b1c3 b8c6 c1f4 c8f5 d1d2 d8d7 e1c1 e8c8 f4c7 f5c2 c7d8 c6d4 c3d5 d4b3 a2b3 c2b3 d5b6 c8d8 d2d7",
                notes(
                        9, "White castles queenside.",
                        10, "Black also castles queenside.",
                        21, "White queen D2-D7# penetrates the seventh rank and checkmates."
                )
        );
    }

    private MatchSpec match09() {
        return match(
                "White en passant match",
                GameStatus.BLACK_WIN,
                "White performs E5xD6 en passant immediately after D7-D5; Black later checkmates on G2.",
                List.of("white en passant", "pawn double-step", "WHITE O-O", "checks", "black checkmate"),
                "e2e4 a7a6 e4e5 d7d5 e5d6 b8d7 d6c7 e7e5 f1a6 b7a6 g1f3 c8b7 f3e5 g8h6 e5d7 e8d7 e1g1 d8c7 d1g4 d7e8 g4e6 c7e7 e6f7 e7f7 b1c3 f8e7 f1d1 b7g2 h2h4 f7f2 g1h2 g2c6 h2h3 f2g2",
                notes(
                        4, "Black advances D7-D5 two squares beside the white pawn on E5, creating a one-move en-passant opportunity.",
                        5, "White executes en passant E5xD6: the pawn moves to D6 while the black pawn that reached D5 must be removed.",
                        17, "White castles kingside.",
                        34, "Black queen F2-G2# delivers checkmate."
                )
        );
    }

    private MatchSpec match10() {
        return match(
                "Black en passant match",
                GameStatus.WHITE_WIN,
                "Black performs E4xD3 en passant immediately after White's D2-D4; White later checkmates with Qg7#.",
                List.of("black en passant", "pawn double-step", "checks", "white checkmate"),
                "a2a3 e7e5 a3a4 e5e4 d2d4 e4d3 e2d3 f8b4 b1c3 b8c6 g1f3 d8e7 e1d2 e7e2 f1e2 b4a5 f3e5 g8h6 e5f7 e8f7 d2e1 h8f8 c1h6 g7h6 a1b1 c6e7 h1f1 e7c6 g2g4 f8e8 g4g5 a5c3 b2c3 e8d8 g5h6 c6e5 e2h5 f7g8 d1g4 g8f8 g4g8 f8e7 g8g5 e7f8 g5g7",
                notes(
                        5, "White advances D2-D4 two squares beside Black's pawn on E4.",
                        6, "Black immediately captures en passant E4xD3; the white pawn from D4 must be removed.",
                        45, "White queen G5-G7# gives checkmate."
                )
        );
    }

    private MatchSpec match11() {
        return match(
                "Promotion to queen match",
                GameStatus.BLACK_WIN,
                "White's A-pawn captures through B7 and reaches A8. The test then calls GameSession.promotePieceIn(A8, QUEEN), matching the frontend-driven promotion flow; Black later mates on H2.",
                List.of("promotion", "pawn captures", "new queen movement", "checkmate"),
                "a2a4 h7h5 a4a5 h5h4 a5a6 h4h3 a6b7 h3g2 b7a8q g2h1q a8b8 h1g1 a1a7 g1f2 e1f2 e7e5 a7c7 f8c5 f2g3 d8h4 g3g2 h4f2 g2h1 f2h2",
                notes(
                        9, "White pawn B7xA8 reaches the final rank; immediately afterwards the test requests QUEEN promotion through GameSession.promotePieceIn(A8, QUEEN).",
                        10, "Black pawn G2xH1 also reaches its final rank; the runner explicitly requests QUEEN promotion on H1, matching the same frontend-driven flow for Black.",
                        11, "The newly promoted white queen must now be able to move A8-B8, confirming that promotion changed the piece's usable movement behavior as well as its type.",
                        12, "The newly promoted black queen moves H1-G1, confirming Black promotion also replaced the pawn with a usable queen.",
                        24, "Black queen F2-H2# gives checkmate."
                )
        );
    }

    private MatchSpec match12() {
        return match(
                "Italian-style double-castling tactical game",
                GameStatus.BLACK_WIN,
                "Both kings castle kingside and Black ultimately checkmates with a rook on F1.",
                List.of("Italian development", "WHITE O-O", "BLACK O-O", "rook checkmate"),
                "e2e4 e7e5 g1f3 b8c6 f1c4 g8f6 d2d3 f8c5 e1g1 e8g8 c4f7 g8h8 c1d2 c5f2 g1h1 f2b6 f3e5 f8f7 e5g6 h8g8 d1g4 c6a5 g6e7 g8h8 g4g7 h8g7 f1f6 f7f6 e7c8 a8c8 d2a5 f6f1",
                notes(
                        9, "White castles kingside after normal Italian-style development.",
                        10, "Black also castles kingside.",
                        32, "Black rook F6-F1# reaches the first rank and checkmates."
                )
        );
    }

    private MatchSpec match13() {
        List<Checkpoint> checkpoints = List.of(
                new Checkpoint(
                        6,
                        "c6",
                        true,
                        "The black knight on C6 is absolutely pinned to the king on E8 by White's bishop on B5 after Black moved the D-pawn away. Moving that knight would expose the king to the bishop."
                )
        );

        return match(
                "Absolute pin followed by a White win",
                GameStatus.WHITE_WIN,
                "The game creates an absolute pin on C6, verifies that the pinned knight has no legal move, then continues to Qh5#.",
                List.of("absolute pin", "pinned-piece validation", "WHITE O-O", "king chase", "checkmate"),
                "e2e4 e7e5 g1f3 b8c6 f1b5 d7d6 e1g1 c8h3 b5c6 e8e7 c6b7 h3g4 f3e5 g4c8 e5c6 e7f6 c6d8 g8h6 d1f3 f6g5 d8f7 g5h4 g2g3 h4h3 f3h5",
                notes(
                        5, "White bishop moves to B5 and lines up against the black king through C6 and D7.",
                        6, "Black moves D7-D6, clearing D7; the C6 knight is now the only blocker between bishop B5 and king E8 and becomes absolutely pinned.",
                        7, "White castles kingside after the pin checkpoint has been evaluated.",
                        25, "White queen F3-H5# gives the final checkmate."
                ),
                checkpoints
        );
    }

    private MatchSpec match14() {
        return match(
                "Caro-Kann style tactical game",
                GameStatus.BLACK_WIN,
                "The game begins from a Caro-Kann structure and ends with Black's bishop moving to C4 for checkmate.",
                List.of("Caro-Kann structure", "captures", "king displacement", "bishop checkmate"),
                "e2e4 c7c6 d2d4 d7d5 b1c3 d5e4 c3e4 c8f5 e4f6 e7f6 f1c4 f8b4 e1e2 d8e7 c4e6 f5g4 g1f3 g4e6 c1g5 e6c4",
                notes(
                        2, "Black establishes the Caro-Kann with C7-C6.",
                        4, "Black challenges the center with D7-D5.",
                        20, "Black bishop E6-C4# finishes the game."
                )
        );
    }

    private MatchSpec match15() {
        return match(
                "Scandinavian style tactical game",
                GameStatus.BLACK_WIN,
                "The game begins with the Scandinavian Defense and ends with a black rook checkmate on D1.",
                List.of("Scandinavian Defense", "queen movement", "rook file invasion", "rook checkmate"),
                "e2e4 d7d5 e4d5 d8d5 b1c3 d5d8 d2d4 g8f6 f1d3 f6e4 c3e4 c8f5 g1e2 b8c6 c1d2 c6d4 e2d4 f5e4 d3e4 d8d4 e4h7 d4d2 e1f1 d2d1 a1d1 a8d8 f1g1 f7f6 b2b3 d8d1",
                notes(
                        2, "Black answers E4 with the Scandinavian D7-D5.",
                        4, "Black queen recaptures on D5.",
                        30, "Black rook D8-D1# invades the first rank and checkmates."
                )
        );
    }

    private MatchSpec match16() {
        return match(
                "Pirc-style attacking game",
                GameStatus.WHITE_WIN,
                "Black castles kingside from a Pirc-like setup; White later finishes with Qh8#.",
                List.of("Pirc structure", "BLACK O-O", "king attack", "queen checkmate"),
                "e2e4 d7d6 d2d4 g8f6 b1c3 g7g6 f2f4 f8g7 g1f3 c8d7 d4d5 e8g8 c1d2 e7e5 f4e5 d6e5 a2a3 b8a6 f3e5 f6d5 c3d5 g7e5 d5f6 d8f6 f1d3 e5g3 e1e2 f6f1 e2f1 g3e5 d2a5 e5h2 a5c3 b7b6 h1h2 a6b8 h2h7 g8h7 d1h5 h7g8 h5h8",
                notes(
                        12, "Black castles kingside.",
                        41, "White queen H5-H8# completes the kingside attack."
                )
        );
    }

    private MatchSpec match17() {
        return match(
                "English-opening tactical game",
                GameStatus.WHITE_WIN,
                "The match begins with C2-C4 and ends with White's queen reaching D7 for mate.",
                List.of("English Opening", "central captures", "king movement", "queen checkmate"),
                "c2c4 e7e5 b1c3 g8f6 g2g3 d7d5 c4d5 f6d5 f1g2 d5c3 d2c3 d8d2 e1d2 b8c6 g2c6 e8d8 d2e3 c8d7 d1d7",
                notes(
                        1, "White opens with C2-C4, entering an English-opening structure.",
                        19, "White queen D1-D7# ends the game."
                )
        );
    }

    private MatchSpec match18() {
        return match(
                "Dutch-style tactical game",
                GameStatus.WHITE_WIN,
                "Black starts with F7-F5 and both sides castle kingside; White eventually mates with Qg7#.",
                List.of("Dutch structure", "WHITE O-O", "BLACK O-O", "many checks", "queen checkmate"),
                "d2d4 f7f5 g2g3 g8f6 f1g2 g7g6 g1f3 f8g7 e1g1 d7d5 e2e4 f6e4 c1g5 e4f2 f1f2 e8g8 b1c3 b8a6 c3d5 g7d4 f3d4 f5f4 d5f6 g8h8 g2b7 c8b7 f6h7 e7e5 f2f4 h8h7 f4f7 h7g8 g5d8 g8f7 d8c7 a8d8 c7d8 b7e4 d1f1 f7g7 d8f6 g7h6 d4f5 e4f5 f1c1 g6g5 c1g5 h6h7 g5g7",
                notes(
                        2, "Black plays F7-F5, the characteristic Dutch Defense pawn advance.",
                        9, "White castles kingside.",
                        16, "Black castles kingside.",
                        49, "White queen G5-G7# delivers checkmate."
                )
        );
    }

    private MatchSpec match19() {
        return match(
                "Threefold repetition with both king knights",
                GameStatus.DRAW,
                "Both sides return their G-knights to the starting position twice, causing the initial position with White to move to occur for the third time.",
                List.of("threefold repetition", "draw", "knight movement", "position history"),
                "g1f3 g8f6 f3g1 f6g8 g1f3 g8f6 f3g1 f6g8",
                notes(
                        4, "After both knights return, the complete initial position has occurred a second time.",
                        8, "After the second full knight cycle, the identical initial position occurs for the third time; the game should be declared a draw by threefold repetition."
                )
        );
    }

    private MatchSpec match20() {
        return match(
                "Threefold repetition after E4 E5",
                GameStatus.DRAW,
                "After E4 E5, both knights return to their starting squares twice. Because no en-passant capture is actually available after E7-E5, the repeated positions have the same legal possibilities and the third occurrence should produce a draw.",
                List.of("threefold repetition", "draw", "position identity", "knight movement"),
                "e2e4 e7e5 g1f3 g8f6 f3g1 f6g8 g1f3 g8f6 f3g1 f6g8",
                notes(
                        1, "White plays E2-E4.",
                        2, "Black plays E7-E5. No white pawn is in position to capture E5 en passant, so there is no usable en-passant move that distinguishes this position for repetition purposes.",
                        6, "Both knights have returned once; the E4/E5 position with White to move occurs for the second time.",
                        10, "Both knights return again; the same position occurs for the third time and GameStatus.DRAW is expected."
                )
        );
    }

    private MatchSpec match21() {
        return match(
                "Sam Loyd ten-move stalemate",
                GameStatus.DRAW,
                "White's queen reaches E6 on move 10. Black's king on G6 is not in check, but Black has no legal move, so the game is stalemate.",
                List.of("stalemate", "draw", "queen sweep", "king confinement", "no legal moves"),
                "e2e3 a7a5 d1h5 a8a6 h5a5 h7h5 a5c7 a6h6 h2h4 f7f6 c7d7 e8f7 d7b7 d8d3 b7b8 d3h7 b8c8 f7g6 c8e6",
                notes(
                        11, "White queen captures D7 with check.",
                        12, "Black king moves E8-F7 to escape the check.",
                        17, "White queen captures the bishop on C8.",
                        18, "Black king moves F7-G6 into the final confined position.",
                        19, "White queen C8-E6 does not check the king on G6, but Black has no legal move: stalemate."
                )
        );
    }

    private MatchSpec match22() {
        return match(
                "Sam Loyd reordered ten-move stalemate",
                GameStatus.DRAW,
                "This reaches the same stalemate pattern as match 21, but swaps the order of Qxc7 and h4 to exercise a different legal move history.",
                List.of("stalemate", "draw", "move-order variation", "queen sweep", "no legal moves"),
                "e2e3 a7a5 d1h5 a8a6 h5a5 h7h5 h2h4 a6h6 a5c7 f7f6 c7d7 e8f7 d7b7 d8d3 b7b8 d3h7 b8c8 f7g6 c8e6",
                notes(
                        7, "White plays H2-H4 before taking on C7; this is the intentional move-order variation.",
                        8, "Black rook A6-H6 keeps the rook move legal before F7-F6 blocks the sixth rank.",
                        9, "White queen A5xC7 resumes the standard stalemate construction.",
                        18, "Black king reaches G6.",
                        19, "White queen C8-E6 leaves Black with no legal move while the king is not in check: stalemate."
                )
        );
    }

    private MatchSpec match23() {
        return match(
                "Alternate opening into the ten-move stalemate",
                GameStatus.DRAW,
                "A different first three moves transpose into the same queen-and-rook stalemate mechanism, ending with Qe6 and no legal Black move.",
                List.of("stalemate", "draw", "alternate opening", "queen sweep", "no legal moves"),
                "c2c4 h7h5 h2h4 a7a5 d1a4 a8a6 a4a5 a6h6 a5c7 f7f6 c7d7 e8f7 d7b7 d8d3 b7b8 d3h7 b8c8 f7g6 c8e6",
                notes(
                        1, "White starts C2-C4 instead of E2-E3.",
                        5, "White queen moves D1-A4 and the game now enters the familiar stalemate construction.",
                        18, "Black king moves F7-G6.",
                        19, "White queen C8-E6 leaves Black not in check and without a legal move: stalemate."
                )
        );
    }

    private MatchSpec match24() {
        return match(
                "Frederick Rhine ten-move stalemate",
                GameStatus.DRAW,
                "White removes Black's queenside material with the queen and finishes with Qe6. The black king on G6 is not checked, but Black has no legal move.",
                List.of("stalemate", "draw", "queen captures", "rook movement", "no legal moves"),
                "d2d4 c7c5 d4c5 f7f6 d1d7 e8f7 d7d8 c8f5 d8b8 h7h5 b8a8 h8h6 a8b7 a7a6 b7a6 f5h7 h2h4 f7g6 a6e6",
                notes(
                        5, "White queen D1xD7 gives check.",
                        6, "Black king E8-F7 escapes the check.",
                        11, "White queen B8xA8 captures Black's queenside rook.",
                        18, "Black king F7-G6 enters the final confined square.",
                        19, "White queen A6-E6 creates stalemate: the king is not in check and Black has no legal move."
                )
        );
    }

    private MatchSpec match25() {
        return match(
                "All-pieces stalemate",
                GameStatus.DRAW,
                "No captures occur. After Black plays F5-F4, every White piece is blocked or pinned and the white king is not in check, so White has no legal move.",
                List.of("stalemate", "draw", "all 32 pieces", "no captures", "blocked position", "no legal moves"),
                "d2d4 d7d6 d1d2 e7e5 a2a4 e5e4 d2f4 f7f5 h2h3 f8e7 f4h2 c8e6 a1a3 c7c5 a3g3 d8a5 b1d2 e7h4 f2f3 e6b3 d4d5 e4e3 c2c4 f5f4",
                notes(
                        15, "White rook A3-G3 helps seal White's kingside movement.",
                        16, "Black queen D8-A5 gives check.",
                        17, "White knight B1-D2 answers the check by blocking the queen's diagonal toward E2 and the king area.",
                        20, "Black's C8 bishop, now on E6, moves E6-B3 and further restricts White.",
                        22, "Black pawn E4-E3 closes additional White movement squares.",
                        24, "Black pawn F5-F4 completes the cage. White is not in check but has no legal move: stalemate."
                )
        );
    }

    // ---------------------------------------------------------------------
    // Small data helpers
    // ---------------------------------------------------------------------

    private MatchSpec match(
            String name,
            GameStatus expectedStatus,
            String expectedResultExplanation,
            List<String> features,
            String encodedMoves,
            Map<Integer, String> moveExplanations
    ) {
        return match(
                name,
                expectedStatus,
                expectedResultExplanation,
                features,
                encodedMoves,
                moveExplanations,
                List.of()
        );
    }

    private MatchSpec match(
            String name,
            GameStatus expectedStatus,
            String expectedResultExplanation,
            List<String> features,
            String encodedMoves,
            Map<Integer, String> moveExplanations,
            List<Checkpoint> checkpoints
    ) {
        List<String> moves = List.of(encodedMoves.trim().split("\\s+"));
        return new MatchSpec(
                name,
                expectedStatus,
                expectedResultExplanation,
                features,
                moves,
                moveExplanations,
                checkpoints
        );
    }

    private Map<Integer, String> notes(Object... entries) {
        Map<Integer, String> notes = new LinkedHashMap<>();

        for (int i = 0; i < entries.length; i += 2) {
            notes.put((Integer) entries[i], (String) entries[i + 1]);
        }

        return notes;
    }

    private record MatchSpec(
            String name,
            GameStatus expectedStatus,
            String expectedResultExplanation,
            List<String> features,
            List<String> moves,
            Map<Integer, String> moveExplanations,
            List<Checkpoint> checkpoints
    ) {
    }

    private record Checkpoint(
            int afterPly,
            String tile,
            boolean expectNoLegalMoves,
            String explanation
    ) {
    }
}

