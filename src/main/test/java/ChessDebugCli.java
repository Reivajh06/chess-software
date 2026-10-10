import reivajh06.FenParser;
import reivajh06.gamelogic.*;
import reivajh06.model.*;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.MoveEffect;
import reivajh06.model.movement.MoveEvent;

import java.util.*;
import java.util.stream.Collectors;


public final class ChessDebugCli {
    private ChessDebugCli() {}

    public static void main(String[] args) {
        GameSession session = freshSession();
        System.out.println("Chess game-logic debugger (no HTTP). Type 'help'.");
        if (args.length > 0) {
            try {
                for (String uci : args) {
                    applyCoordinateMove(session, uci);
                    System.out.println(uci + " -> " + safeFen(session));
                }
            } catch (RuntimeException ex) {
                System.err.println("Move failed: " + ex);
                System.exit(1);
            }
            return;
        }
        printState(session);
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("chess> ");
                if (!scanner.hasNextLine()) break;
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;
                String[] parts = input.split("\\s+", 2);
                String command = parts[0].toLowerCase(Locale.ROOT);
                String argument = parts.length == 2 ? parts[1].trim() : "";
                try {
                    switch (command) {
                        case "help", "?" -> help();
                        case "new", "reset" -> {
                            session = freshSession();
                            printState(session);
                        }
                        case "board" -> session.gameState().board().printBoard();
                        case "fen" -> System.out.println(safeFen(session));
                        case "state", "status" -> printState(session);
                        case "legal" -> printLegal(session, argument);
                        case "move" -> {
                            applyCoordinateMove(session, argument.replaceAll("\\s+", ""));
                            printState(session);
                        }
                        case "play" -> {
                            for (String uci : argument.split("\\s+")) {
                                if (!uci.isBlank()) {
                                    applyCoordinateMove(session, uci);
                                    System.out.println(uci + " -> " + safeFen(session));
                                }
                            }
                        }
                        case "load" -> {
                            loadFen(session, argument);
                            printState(session);
                        }
                        case "history" -> printHistory(session);
                        case "diagnose" -> diagnose(session);
                        case "exit", "quit" -> { return; }
                        default -> System.out.println("Unknown command: " + command + ". Type help.");
                    }
                } catch (RuntimeException ex) {
                    System.err.println("ERROR " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                    System.err.println("Current state (may have been partially mutated by the engine):");
                    printState(session);
                }
            }
        }
    }

    private static GameSession freshSession() {
        return new GameSession(Gamemode.PVP, Difficulty.NOT_CPU);
    }


    public static void applyCoordinateMove(GameSession session, String uci) {
        String normalized = uci.trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-h][1-8][a-h][1-8][qrbn]?")) {
            throw new IllegalArgumentException("Expected coordinate move such as e2e4, e1g1 or a7a8q: " + uci);
        }
        if (normalized.length() == 5) {
            throw new UnsupportedOperationException(
                    "Promotion choice '" + normalized.charAt(4) + "' cannot be represented by the current Move API. " +
                    "Add promotion to the game logic and update this driver; do not ignore the suffix.");
        }
        if (session.gameState().status() != GameStatus.ACTIVE) {
            throw new IllegalStateException("Game is already " + session.gameState().status());
        }
        Tile from = new Tile(normalized.substring(0, 2));
        Tile to = new Tile(normalized.substring(2, 4));
        Piece piece = session.gameState().board().pieceAt(from);
        if (piece == null) throw new IllegalArgumentException("No piece at " + from);
        if (piece.color() != session.gameState().turnColor()) {
            throw new IllegalArgumentException("Wrong side to move: " + piece.color());
        }
        GameUpdater updater = new GameUpdater(session);
        List<Move> matching = updater.pieceMoves(from).entrySet().stream()
                .filter(e -> e.getValue().contains(to))
                .map(Map.Entry::getKey)
                .toList();
        if (matching.isEmpty()) throw new IllegalArgumentException("Illegal move: " + uci);
        if (matching.size() != 1) {
            throw new IllegalStateException("Ambiguous rule selection for " + uci + ": " + matching.size() + " moves");
        }
        updater.movePieceAndUpdate(matching.getFirst(), from, to);
    }

    public static void loadFen(GameSession session, String fullFen) {
        String[] fields = fullFen.trim().split("\\s+");
        if (fields.length != 6) throw new IllegalArgumentException("FEN must have six fields");
        Board board = new Board();
        for (Rank r : Rank.values()) for (File f : File.values()) {
            board.addPiece(new Tile(f, r), null);
        }
        String[] rows = fields[0].split("/");
        if (rows.length != 8) throw new IllegalArgumentException("FEN must have eight ranks");
        int whiteKings = 0, blackKings = 0;
        for (int row = 0; row < 8; row++) {
            int file = 0;
            for (char ch : rows[row].toCharArray()) {
                if (ch >= '1' && ch <= '8') {
                    file += ch - '0';
                } else {
                    if (file >= 8 || "KQRBNPkqrbnp".indexOf(ch) < 0) {
                        throw new IllegalArgumentException("Bad FEN piece/rank: " + rows[row]);
                    }
                    Tile square = new Tile(File.values()[file], Rank.values()[7 - row]);
                    Piece piece = PieceFactory.createPieceByFENCode(ch);
                    board.addPiece(square, piece);
                    if (ch == 'K') { board.whiteKingTile(square); whiteKings++; }
                    if (ch == 'k') { board.blackKingTile(square); blackKings++; }
                    file++;
                }
                if (file > 8) throw new IllegalArgumentException("FEN rank too wide: " + rows[row]);
            }
            if (file != 8) throw new IllegalArgumentException("FEN rank not eight files: " + rows[row]);
        }
        if (whiteKings != 1 || blackKings != 1) {
            throw new IllegalArgumentException("Fixture must contain exactly one king per color");
        }
        PieceColor turn = switch (fields[1]) {
            case "w" -> PieceColor.WHITE;
            case "b" -> PieceColor.BLACK;
            default -> throw new IllegalArgumentException("Bad active color in FEN");
        };
        String rights = fields[2];
        if (!rights.matches("-|K?Q?k?q?")) throw new IllegalArgumentException("Bad castling rights in FEN");
        board.whiteKingSideRook(rights.contains("K") ? new Tile("h1") : null);
        board.whiteQueenSideRook(rights.contains("Q") ? new Tile("a1") : null);
        board.blackKingSideRook(rights.contains("k") ? new Tile("h8") : null);
        board.blackQueenSideRook(rights.contains("q") ? new Tile("a8") : null);
        Tile ep = fields[3].equals("-") ? null : new Tile(fields[3]);
        int halfmove = Integer.parseInt(fields[4]);
        int fullmove = Integer.parseInt(fields[5]);
        if (halfmove < 0 || fullmove < 1) throw new IllegalArgumentException("Invalid move clocks");
        Map<PieceColor, List<Piece>> captures = new EnumMap<>(PieceColor.class);
        captures.put(PieceColor.WHITE, new ArrayList<>());
        captures.put(PieceColor.BLACK, new ArrayList<>());
        GameState state = new GameStateBuilder()
                .board(board).turnColor(turn).checkedColor(null).status(GameStatus.ACTIVE)
                .whiteCastling(rightsList(rights.contains("K"), rights.contains("Q")))
                .blackCastling(rightsList(rights.contains("k"), rights.contains("q")))
                .enPassantDestination(ep).halfMoveClock(halfmove).fullMoves(fullmove)
                .movesHistory(new ArrayDeque<>()).eatenPieces(captures).build();
        session.gameState(state);
    }

    private static List<Castling> rightsList(boolean king, boolean queen) {
        return new ArrayList<>(Arrays.asList(
                king ? Castling.KINGSIDE : null, queen ? Castling.QUEENSIDE : null));
    }

    private static String safeFen(GameSession session) {
        try { return session.toFEN(); }
        catch (RuntimeException e) { return "<FEN error: " + e + "> board=" + FenParser.boardToFen(session.gameState().board()); }
    }

    private static void printState(GameSession session) {
        GameState s = session.gameState();
        s.board().printBoard();
        System.out.println("session=" + session.id() + " turn=" + s.turnColor()
                + " checked=" + s.checkedColor() + " status=" + s.status());
        System.out.println("FEN: " + safeFen(session));
        System.out.println("castling: W=" + s.whiteCastling() + " B=" + s.blackCastling()
                + " en-passant=" + s.enPassantDestination()
                + " halfmove=" + s.halfMoveClock() + " fullmove=" + s.fullMoves()
                + " history=" + s.movesHistory().size());
    }

    private static void printLegal(GameSession session, String arg) {
        if (!arg.matches("(?i)[a-h][1-8]")) throw new IllegalArgumentException("Usage: legal e2");
        Tile from = new Tile(arg);
        GameUpdater updater = new GameUpdater(session);
        List<String> moves = updater.pieceMoves(from).entrySet().stream()
                .flatMap(e -> e.getValue().stream()
                        .map(to -> from.name().toLowerCase(Locale.ROOT) + to.name().toLowerCase(Locale.ROOT)
                                + " (" + effectName(e.getKey().effect()) + ")"))
                .sorted().toList();
        System.out.println(moves.isEmpty() ? "No legal moves." : String.join("  ", moves));
    }

    private static String effectName(MoveEffect effect) {
        if (effect == MoveEffect.DIRECT_CAPTURE) return "normal/capture";
        if (effect == MoveEffect.EN_PASSANT_CAPTURE) return "en-passant";
        if (effect == MoveEffect.KINGSIDE_CASTLING) return "castle-K";
        if (effect == MoveEffect.QUEENSIDE_CASTLING) return "castle-Q";
        return "other";
    }

    private static void printHistory(GameSession session) {
        int ply = 0;
        for (MoveEvent event : session.gameState().movesHistory()) {
            System.out.printf("%3d. %s%s  %s  [stored position: %s]%n", ++ply,
                    event.from(), event.to(), event.pieceMoved().name(), event.gameStateFen());
        }
    }

    private static void diagnose(GameSession session) {
        GameState s = session.gameState();
        for (PieceColor color : PieceColor.values()) {
            List<Tile> actual = s.board().piecesTilesByColor(color).stream()
                    .filter(t -> s.board().pieceAt(t).type() == PieceType.KING).toList();
            Tile tracked = s.board().kingTile(color);
            System.out.println(color + " king tracked=" + tracked + " actual=" + actual
                    + (actual.size() == 1 && tracked.equals(actual.getFirst()) ? " OK" : " MISMATCH"));
        }
        MoveEvent latest = s.movesHistory().peekLast();
        System.out.println("lastMove()=" + (s.lastMove() == null ? "none" : s.lastMove().from() + "->" + s.lastMove().to()));
        System.out.println("history tail=" + (latest == null ? "none" : latest.from() + "->" + latest.to()));
        if (latest != null && latest != s.lastMove()) System.out.println("MISMATCH: lastMove is not latest event");
        if (latest != null && !latest.gameStateFen().equals(FenParser.boardToFen(s.board()))) {
            System.out.println("MISMATCH: last event board position is not current board");
        }
        System.out.println("FEN=" + safeFen(session));
        System.out.println("Captured pieces: " + s.eatenPieces().entrySet().stream()
                .map(e -> e.getKey() + "=" + (e.getValue() == null ? "null" : e.getValue().size()))
                .collect(Collectors.joining(", ")));
    }

    private static void help() {
        System.out.println("Commands:");
        System.out.println("  board                 print board");
        System.out.println("  fen                   print six-field FEN");
        System.out.println("  state                 board + current state details");
        System.out.println("  legal e2              inspect legal moves for square");
        System.out.println("  move e2e4             attempt one coordinate move (castling e1g1/e1c1)");
        System.out.println("  play e2e4 e7e5 ...    play a sequence; stops at the first failure");
        System.out.println("  history               print recorded move events");
        System.out.println("  diagnose              compare cached king squares and move history");
        System.out.println("  load <6-field FEN>    load debug position fixture; resets history");
        System.out.println("  new                   reset to starting session");
        System.out.println("  quit                  exit");
        System.out.println("Promotion UCI suffixes (q/r/b/n) intentionally report missing domain support.");
    }
}
