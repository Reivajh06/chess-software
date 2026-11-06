import reivajh06.architecture.domain.services.FenParser;
import reivajh06.architecture.domain.model.game.Difficulty;
import reivajh06.architecture.domain.model.game.GameSession;
import reivajh06.architecture.domain.model.game.GameState;
import reivajh06.architecture.domain.model.game.Gamemode;
import reivajh06.architecture.domain.model.Color;
import reivajh06.architecture.domain.model.PieceType;
import reivajh06.architecture.domain.model.Tile;
import reivajh06.architecture.domain.logic.GameDirector;
import reivajh06.architecture.domain.model.movement.MoveEffect;
import reivajh06.architecture.domain.model.movement.MoveEvent;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;


public final class ChessDebugCli {
    private ChessDebugCli() {}

    public static void main(String[] args) {
        GameSession session = freshSession();
        System.out.println("Chess game-logic debugger (no HTTP). Type 'help'.");
        if (args.length > 0) {
            try {
                for (String uci : args) {
                    ChessTestSupport.playMove(session, uci);
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
                            ChessTestSupport.playMove(session, argument.replaceAll("\\s+", ""));
                            printState(session);
                        }
                        case "play" -> {
                            for (String uci : argument.split("\\s+")) {
                                if (!uci.isBlank()) {
                                    ChessTestSupport.playMove(session, uci);
                                    System.out.println(uci + " -> " + safeFen(session));
                                }
                            }
                        }
                        case "load" -> {
                            ChessTestSupport.loadFen(session, argument);
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


    private static String safeFen(GameSession session) {
        try { return session.toFEN(); }
        catch (RuntimeException e) { return "<FEN error: " + e + "> board=" + FenParser.boardToFen(session.gameState().board()); }
    }

    private static void printState(GameSession session) {
        GameState state = session.gameState();
        state.board().printBoard();
        System.out.println("session=" + session.id() + " turn=" + state.turnColor()
                + " checked=" + state.checkedColor() + " status=" + state.status());
        System.out.println("FEN: " + safeFen(session));
        System.out.println("castling: " + FenParser.castlingRightsToFen(state.castlingRights())
                + " en-passant=" + state.enPassantDestination()
                + " halfmove=" + state.halfMoveClock() + " fullmove=" + state.fullMoves()
                + " history=" + state.movesHistory().size());
    }

    private static void printLegal(GameSession session, String arg) {
        if (!arg.matches("(?i)[a-h][1-8]")) throw new IllegalArgumentException("Usage: legal e2");
        Tile from = new Tile(arg);
        GameDirector updater = new GameDirector(session);
        List<String> moves = updater.pieceMoves(from).entrySet().stream()
                .flatMap(e -> e.getValue().stream()
                        .map(to -> from.name().toLowerCase(Locale.ROOT) + to.name().toLowerCase(Locale.ROOT)
                                + " (" + effectName(session.gameState().board().pieceAt(from)
                                        .moveset().get(e.getKey()).effect()) + ")"))
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
        Iterator<MoveEvent> history = session.gameState().movesHistory().descendingIterator();
        while (history.hasNext()) {
            MoveEvent event = history.next();
            System.out.printf("%3d. %s%s  %s  [stored position: %s]%n", ++ply,
                    event.from(), event.to(), event.pieceMoved().name(), event.gameStateFen());
        }
    }

    private static void diagnose(GameSession session) {
        GameState state = session.gameState();
        for (Color color : Color.values()) {
            List<Tile> actual = state.board().piecesTilesByColor(color).stream()
                    .filter(t -> state.board().pieceAt(t).type() == PieceType.KING).toList();
            Tile tracked = state.board().kingTile(color);
            System.out.println(color + " king tracked=" + tracked + " actual=" + actual
                    + (actual.size() == 1 && Objects.equals(tracked, actual.getFirst()) ? " OK" : " MISMATCH"));
        }
        MoveEvent latest = state.movesHistory().peekFirst();
        System.out.println("lastMove()=" + (state.lastMove() == null ? "none" : state.lastMove().from() + "->" + state.lastMove().to()));
        System.out.println("history head=" + (latest == null ? "none" : latest.from() + "->" + latest.to()));
        if (latest != null && latest != state.lastMove()) System.out.println("MISMATCH: lastMove is not latest event");
        if (latest != null && !latest.gameStateFen().equals(session.toFEN())) {
            System.out.println("MISMATCH: last event position is not current position");
        }
        System.out.println("FEN=" + safeFen(session));
        System.out.println("Captured pieces: " + state.eatenPieces().entrySet().stream()
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
