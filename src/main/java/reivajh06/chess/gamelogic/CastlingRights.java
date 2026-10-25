package reivajh06.chess.gamelogic;

import reivajh06.chess.model.Castling;
import reivajh06.chess.model.Color;

import java.util.HashMap;
import java.util.Map;

public class CastlingRights {

    public static CastlingRights allRights() {
        CastlingRights castlingRights = new CastlingRights();
        castlingRights.grantRights(Color.WHITE);
        castlingRights.grantRights(Color.BLACK);

        return castlingRights;
    }

    private final Map<Color, Map<Castling, Boolean>> rights;

    public CastlingRights() {
        rights = new HashMap<>();

        rights.put(Color.WHITE, new HashMap<>());
        rights.put(Color.BLACK, new HashMap<>());

        rights.get(Color.WHITE).put(Castling.KINGSIDE, false);
        rights.get(Color.WHITE).put(Castling.QUEENSIDE, false);
        rights.get(Color.BLACK).put(Castling.KINGSIDE, false);
        rights.get(Color.BLACK).put(Castling.QUEENSIDE, false);
    }

    public void revokeRight(Color color, Castling castling) {
        rights.get(color).put(castling, false);
    }

    public void revokeRights(Color color) {
        revokeRight(color, Castling.KINGSIDE);
        revokeRight(color, Castling.QUEENSIDE);
    }

    public void grantRights(Color color) {
        grantRight(color, Castling.KINGSIDE);
        grantRight(color, Castling.QUEENSIDE);
    }

    public void grantRight(Color color, Castling castling) {
        rights.get(color).put(castling, true);
    }

    public boolean hasRight(Color color, Castling castling) {
        return rights.get(color).get(castling);
    }
}
