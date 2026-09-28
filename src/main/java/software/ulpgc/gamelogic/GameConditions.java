package software.ulpgc.gamelogic;

import software.ulpgc.model.Piece;
import software.ulpgc.model.PieceColor;
import software.ulpgc.model.Tile;
import software.ulpgc.model.movement.Move;

import java.util.List;
import java.util.Map;

public class GameConditions {

    public static boolean isKingCheckedBy(GameController controller, Tile rivalTile) {
        Piece rivalPiece = controller.getPieceIn(rivalTile);
        Tile kingTile = controller.kingTile(PieceColor.rivalColor(rivalPiece.color()));

        return rivalPiece.moveset().stream()
                .anyMatch(m -> controller.calculateMoveTiles(rivalPiece.color(), rivalTile, m).contains(kingTile));
    }

    public static boolean isCheckmate(GameController controller, PieceColor kingColor) {
        Tile kingTile = controller.kingTile(kingColor);

        return controller.getPieceIn(kingTile).moveset().stream()
                .anyMatch(m -> !controller.calculateMoveTiles(kingColor, kingTile, m).isEmpty());
    }

    public static boolean isKingChecked(GameController controller, PieceColor kingColor) {
        PieceColor rivalColor = PieceColor.rivalColor(kingColor);
        List<Tile> rivalTiles = controller.piecesTilesByColor(rivalColor);

        return rivalTiles.stream().anyMatch(t -> isKingCheckedBy(controller, t));
    }

    private boolean moveUnchecksKing(GameController controller, Move move, Tile location) {
        Piece p = controller.getPieceIn(location);

        return p.moveset().stream().anyMatch(m -> {


            return !isKingChecked(controller, p.color());
        });
    }

    public static boolean isAbsolutelyPinned(GameController controller, Tile location, Map<Move, List<Tile>> movesetMap) {
        return false;
    }
}
