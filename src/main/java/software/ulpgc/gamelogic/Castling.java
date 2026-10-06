package software.ulpgc.gamelogic;

public enum Castling {

    KINGSIDE,
    QUEENSIDE;

    public String firstLetter() {
        return name().substring(0, 1);
    }
}
