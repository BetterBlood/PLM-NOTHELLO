package heigvd.plm.nothello.game;

public enum PieceColor {

    BLACK,
    WHITE,
    NONE;

    public PieceColor opposite() {
        if (this == BLACK) {
            return WHITE;
        } else if (this == WHITE) {
            return BLACK;
        } else {
            return NONE;
        }
    }

    public boolean isNone() {
        return this == NONE;
    }

    public boolean isBlack() {
        return this == BLACK;
    }

    public boolean isWhite() {
        return this == WHITE;
    }
    
    public char getChar() {
        return switch (this) {
            case BLACK -> '○';
            case WHITE -> '●';
            default -> ' ';
        };
    } 
}
