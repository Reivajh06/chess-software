package reivajh06.model;

public enum Castling {

    KINGSIDE,
    QUEENSIDE;

    public String firstLetter() {
        return name().substring(0, 1);
    }
}
