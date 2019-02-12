class Piece {
    boolean isAI;
    boolean isKing;
    int ID;
    int boardX;
    int boardY;

    Piece(int id, boolean isPieceAI) {
        isAI = isPieceAI;
        isKing = false;
        ID = id;
    }
    Piece(Piece p) {
        this.isAI = p.isAI;
        this.isKing = p.isKing;
        this.ID = p.ID;
        this.boardX = p.boardX;
        this.boardY = p.boardY;
    }
}
