import software.ulpgc.gamelogic.Difficulty;
import software.ulpgc.gamelogic.GameSession;
import software.ulpgc.gamelogic.GameStatus;
import software.ulpgc.gamelogic.Gamemode;
import software.ulpgc.model.Piece;
import software.ulpgc.model.PieceColor;
import software.ulpgc.model.PieceType;
import software.ulpgc.model.Tile;
import software.ulpgc.model.movement.Move;
import software.ulpgc.model.movement.MoveData;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/**
 * Small turn-based console client for the chess engine.
 *
 * Supported commands:
 *   select e2   - select the piece on E2 and print its legal destinations
 *   move e4     - move the previously selected piece to E4 when legal
 *   undo        - undo the latest executed move
 *   history     - print executed moves from most recent to oldest
 *   captured    - print all currently captured pieces grouped by type/color
 *
 * Extra convenience commands:
 *   board       - print the current board
 *   help        - print the command list
 *   exit        - close the CLI
 */
public final class ChessCLI {

    private final GameSession game;
    private final Scanner scanner;
    private Tile selectedTile;
    private Map<Move, List<Tile>> selectedMoves = Map.of();

    public ChessCLI() {
        this(new GameSession(Gamemode.PVP, Difficulty.NOT_CPU), new Scanner(System.in));
    }

    ChessCLI(GameSession game, Scanner scanner) {
        this.game = game;
        this.scanner = scanner;
    }

    public static void main(String[] args) {
        new ChessCLI().run();
    }

    public void run() {
        printWelcome();
        game.printBoard();

        while(game.status() == GameStatus.ACTIVE) {
            System.out.print(game.colorTurn() + "> ");

            if(!scanner.hasNextLine()) {
                System.out.println();
                return;
            }

            String input = scanner.nextLine().trim();
            if(input.isEmpty()) continue;

            if(!execute(input)) return;

            if(game.status() != GameStatus.ACTIVE) {
                game.printBoard();
                printGameResult();
                return;
            }
        }

        printGameResult();
    }

    private boolean execute(String input) {
        String[] parts = input.trim().split("\\s+");
        String command = parts[0].toLowerCase(Locale.ROOT);

        switch(command) {
            case "select" -> select(parts);
            case "move" -> move(parts);
            case "undo" -> undo(parts);
            case "history" -> history(parts);
            case "captured" -> captured(parts);
            case "board" -> board(parts);
            case "help" -> help(parts);
            case "exit", "quit" -> {
                System.out.println("Game closed.");
                return false;
            }
            default -> System.out.println("Unknown command. Type 'help' to see the available commands.");
        }

        return true;
    }

    private void select(String[] parts) {
        if(parts.length != 2) {
            System.out.println("Usage: select <tile>, for example: select e2");
            return;
        }

        Optional<Tile> tile = parseTile(parts[1]);
        if(tile.isEmpty()) return;

        Piece piece = game.pieceAt(tile.get());
        if(piece == null) {
            clearSelection();
            System.out.println("There is no piece on " + tile.get() + ".");
            return;
        }

        if(piece.color() != game.colorTurn()) {
            clearSelection();
            System.out.println("It is " + game.colorTurn() + "'s turn. You cannot select "
                    + piece.color() + "'s piece on " + tile.get() + ".");
            return;
        }

        selectedTile = tile.get();
        selectedMoves = game.legalPieceMoves(selectedTile);

        List<Tile> destinations = selectedMoves.values().stream()
                .flatMap(List::stream)
                .distinct()
                .sorted(Comparator.comparing(Tile::name))
                .toList();

        System.out.println("Selected " + piece + " on " + selectedTile + ".");
        if(destinations.isEmpty()) {
            System.out.println("Possible moves: none");
        } else {
            System.out.println("Possible moves: " + String.join(", ", destinations.stream().map(Tile::name).toList()));
        }
    }

    private void move(String[] parts) {
        if(parts.length != 2) {
            System.out.println("Usage: move <destination>, for example: move e4");
            return;
        }

        if(selectedTile == null) {
            System.out.println("Select a piece first, for example: select e2");
            return;
        }

        Optional<Tile> destination = parseTile(parts[1]);
        if(destination.isEmpty()) return;

        Piece movingPiece = game.pieceAt(selectedTile);
        if(movingPiece == null || movingPiece.color() != game.colorTurn()) {
            clearSelection();
            System.out.println("The selected piece is no longer valid. Select a piece again.");
            return;
        }

        Optional<Move> legalMove = selectedMoves.entrySet().stream()
                .filter(entry -> entry.getValue().contains(destination.get()))
                .map(Map.Entry::getKey)
                .findFirst();

        if(legalMove.isEmpty()) {
            System.out.println("Illegal move: " + selectedTile + " -> " + destination.get() + ".");
            return;
        }

        Tile from = selectedTile;
        Tile to = destination.get();
        boolean promotionMove = movingPiece.type() == PieceType.PAWN && reachesPromotionRank(movingPiece, to);

        game.move(legalMove.get(), from, to);

        if(promotionMove) {
            PieceType promotion = askPromotion();
            game.promotePieceIn(to, promotion);
            System.out.println("Promoted pawn on " + to + " to " + promotion + ".");
        }

        System.out.println("Moved " + from + " -> " + to + ".");
        clearSelection();
        game.printBoard();
    }

    private void undo(String[] parts) {
        if(parts.length != 1) {
            System.out.println("Usage: undo");
            return;
        }

        if(game.movesHistory().isEmpty()) {
            System.out.println("There is no move to undo.");
            return;
        }

        MoveData lastMove = game.movesHistory().peek();
        game.undoMove();
        clearSelection();

        System.out.println("Undid " + formatMove(lastMove) + ".");
        game.printBoard();
    }

    private void history(String[] parts) {
        if(parts.length != 1) {
            System.out.println("Usage: history");
            return;
        }

        if(game.movesHistory().isEmpty()) {
            System.out.println("History is empty.");
            return;
        }

        System.out.println("Move history (most recent first):");
        for(int i = game.movesHistory().size() - 1, n = 1; i >= 0; i--, n++) {
            System.out.println(n + ". " + formatMove(game.movesHistory().get(i)));
        }
    }

    private void captured(String[] parts) {
        if(parts.length != 1) {
            System.out.println("Usage: captured");
            return;
        }

        Map<PieceColor, List<Piece>> captured = game.capturedPieces();
        if(captured.values().stream().allMatch(List::isEmpty)) {
            System.out.println("Captured pieces: none");
            return;
        }

        System.out.println("Captured pieces:");
        for(PieceColor color : PieceColor.values()) {
            Map<PieceType, Integer> counts = new EnumMap<>(PieceType.class);
            for(Piece piece : captured.getOrDefault(color, List.of())) {
                counts.merge(piece.type(), 1, Integer::sum);
            }

            List<PieceType> order = List.of(
                    PieceType.QUEEN,
                    PieceType.ROOK,
                    PieceType.BISHOP,
                    PieceType.KNIGHT,
                    PieceType.PAWN,
                    PieceType.KING
            );

            for(PieceType type : order) {
                int count = counts.getOrDefault(type, 0);
                if(count == 0) continue;

                Piece representative = captured.get(color).stream()
                        .filter(piece -> piece.type() == type)
                        .findFirst()
                        .orElseThrow();

                System.out.println(representative + " x " + count);
            }
        }
    }

    private void board(String[] parts) {
        if(parts.length != 1) {
            System.out.println("Usage: board");
            return;
        }
        game.printBoard();
    }

    private void help(String[] parts) {
        if(parts.length != 1) {
            System.out.println("Usage: help");
            return;
        }
        printHelp();
    }

    private Optional<Tile> parseTile(String text) {
        String normalized = text.toLowerCase(Locale.ROOT);
        if(!normalized.matches("[a-h][1-8]")) {
            System.out.println("Invalid tile '" + text + "'. Use a tile from a1 to h8.");
            return Optional.empty();
        }
        return Optional.of(new Tile(normalized));
    }

    private boolean reachesPromotionRank(Piece pawn, Tile destination) {
        return (pawn.color() == PieceColor.WHITE && destination.name().endsWith("8"))
                || (pawn.color() == PieceColor.BLACK && destination.name().endsWith("1"));
    }

    private PieceType askPromotion() {
        while(true) {
            System.out.print("Promote to queen, rook, bishop or knight: ");
            if(!scanner.hasNextLine()) return PieceType.QUEEN;

            String choice = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
            switch(choice) {
                case "q", "queen" -> { return PieceType.QUEEN; }
                case "r", "rook" -> { return PieceType.ROOK; }
                case "b", "bishop" -> { return PieceType.BISHOP; }
                case "n", "knight" -> { return PieceType.KNIGHT; }
                default -> System.out.println("Invalid promotion. Choose queen, rook, bishop or knight.");
            }
        }
    }

    private String formatMove(MoveData move) {
        StringBuilder result = new StringBuilder()
                .append(move.pieceMoved())
                .append(' ')
                .append(move.from())
                .append(" -> ")
                .append(move.to());

        if(move.pieceCaptured() != null) {
            result.append(" x ").append(move.pieceCaptured());
        }

        Piece currentPiece = game.pieceAt(move.to());
        if(currentPiece != null
                && move.pieceMoved().type() == PieceType.PAWN
                && currentPiece.type() != PieceType.PAWN
                && reachesPromotionRank(move.pieceMoved(), move.to())) {
            result.append(" = ").append(currentPiece.type());
        }

        return result.toString();
    }

    private void clearSelection() {
        selectedTile = null;
        selectedMoves = Map.of();
    }

    private void printWelcome() {
        System.out.println("Chess CLI");
        System.out.println("---------");
        printHelp();
        System.out.println();
    }

    private void printHelp() {
        System.out.println("Commands:");
        System.out.println("  select <tile>   Select a piece and list its legal destinations");
        System.out.println("  move <tile>     Move the selected piece to that destination");
        System.out.println("  undo            Undo the most recent move");
        System.out.println("  history         Show moves from most recent to oldest");
        System.out.println("  captured        Show captured pieces grouped by piece/color");
        System.out.println("  board           Print the board");
        System.out.println("  help            Show this help");
        System.out.println("  exit            Close the CLI");
    }

    private void printGameResult() {
        switch(game.status()) {
            case WHITE_WIN -> System.out.println("Game over: White wins.");
            case BLACK_WIN -> System.out.println("Game over: Black wins.");
            case DRAW -> System.out.println("Game over: draw.");
            case ACTIVE -> { }
        }
    }
}
