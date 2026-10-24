import reivajh06.gamelogic.CastlingRights;
import reivajh06.gamelogic.Difficulty;
import reivajh06.gamelogic.GameConditions;
import reivajh06.gamelogic.GameSession;
import reivajh06.gamelogic.GameState;
import reivajh06.gamelogic.GameStateBuilder;
import reivajh06.gamelogic.GameStatus;
import reivajh06.gamelogic.GameDirector;
import reivajh06.gamelogic.Gamemode;
import reivajh06.model.Board;
import reivajh06.model.Castling;
import reivajh06.model.File;
import reivajh06.model.Piece;
import reivajh06.model.Color;
import reivajh06.model.PieceFactory;
import reivajh06.model.Rank;
import reivajh06.model.Tile;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class ChessTestSupport {
    private ChessTestSupport() {}

    static GameSession sessionAt(String fen) {
        GameSession session = new GameSession(Gamemode.PVP, Difficulty.NOT_CPU);
        loadFen(session, fen);
        return session;
    }

    static void playMove(GameSession session, String uci) {
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
        GameDirector updater = new GameDirector(session);
        List<Integer> matching = updater.pieceMoves(from).entrySet().stream()
                .filter(e -> e.getValue().contains(to))
                .map(Map.Entry::getKey)
                .toList();
        if (matching.isEmpty()) throw new IllegalArgumentException("Illegal move: " + uci);
        if (matching.size() != 1) {
            throw new IllegalStateException("Ambiguous rule selection for " + uci + ": " + matching.size() + " moves");
        }
        updater.movePieceAndUpdate(matching.getFirst(), from, to);
    }

    static void loadFen(GameSession session, String fullFen) {
        String[] fields = fullFen.trim().split("\\s+");
        if (fields.length != 6) throw new IllegalArgumentException("FEN must have six fields");
        Board board = new Board();
        String[] rows = fields[0].split("/");
        if (rows.length != 8) throw new IllegalArgumentException("FEN must have eight ranks");
        int whiteKings = 0;
        int blackKings = 0;
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
                    if (ch == 'K') {
                        board.whiteKingTile(square);
                        whiteKings++;
                    }
                    if (ch == 'k') {
                        board.blackKingTile(square);
                        blackKings++;
                    }
                    file++;
                }
                if (file > 8) throw new IllegalArgumentException("FEN rank too wide: " + rows[row]);
            }
            if (file != 8) throw new IllegalArgumentException("FEN rank not eight files: " + rows[row]);
        }
        if (whiteKings != 1 || blackKings != 1) {
            throw new IllegalArgumentException("Fixture must contain exactly one king per color");
        }
        Color turn = switch (fields[1]) {
            case "w" -> Color.WHITE;
            case "b" -> Color.BLACK;
            default -> throw new IllegalArgumentException("Bad active color in FEN");
        };
        String rights = fields[2];
        if (rights.isEmpty() || !rights.matches("-|K?Q?k?q?")) {
            throw new IllegalArgumentException("Bad castling rights in FEN");
        }
        if (!fields[3].matches("-|[a-h][36]")) {
            throw new IllegalArgumentException("Bad en passant target in FEN");
        }
        Tile ep = fields[3].equals("-") ? null : new Tile(fields[3]);
        int halfmove = Integer.parseInt(fields[4]);
        int fullmove = Integer.parseInt(fields[5]);
        if (halfmove < 0 || fullmove < 1) throw new IllegalArgumentException("Invalid move clocks");
        Map<Color, List<Piece>> captures = new EnumMap<>(Color.class);
        captures.put(Color.WHITE, new ArrayList<>());
        captures.put(Color.BLACK, new ArrayList<>());
        GameStateBuilder stateBuilder = new GameStateBuilder()
                .board(board).turnColor(turn).checkedColor(null).status(GameStatus.ACTIVE)
                .castlingRights(castlingRights(rights))
                .enPassantDestination(ep).halfMoveClock(halfmove).fullMoves(fullmove)
                .movesHistory(new ArrayDeque<>()).eatenPieces(captures);
        GameState state = stateBuilder.build();
        Color checkedColor = GameConditions.isKingChecked(state, turn) ? turn : null;
        session.gameState(stateBuilder.checkedColor(checkedColor).build());
    }

    private static CastlingRights castlingRights(String fenRights) {
        CastlingRights rights = new CastlingRights();
        if (fenRights.contains("K")) rights.grantRight(Color.WHITE, Castling.KINGSIDE);
        if (fenRights.contains("Q")) rights.grantRight(Color.WHITE, Castling.QUEENSIDE);
        if (fenRights.contains("k")) rights.grantRight(Color.BLACK, Castling.KINGSIDE);
        if (fenRights.contains("q")) rights.grantRight(Color.BLACK, Castling.QUEENSIDE);
        return rights;
    }

}
