import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

class Board {
    Piece[][] board;
    List<Piece> playerPieces;
    List<Piece> aiPieces;

    Board(Board board) {
        this.board = new Piece[8][8];
        this.playerPieces = new ArrayList<>();
        this.aiPieces = new ArrayList<>();

        for (var p : board.playerPieces) {
            var pieceCopy = new Piece(p);
            this.board[p.boardX][p.boardY] = pieceCopy;
            this.playerPieces.add(pieceCopy);
        }
        for (var p : board.aiPieces) {
            var pieceCopy = new Piece(p);
            this.board[p.boardX][p.boardY] = pieceCopy;
            this.aiPieces.add(pieceCopy);
        }
    }

    Board() {
        board = new Piece[8][8];

        playerPieces = new ArrayList<>();
        aiPieces = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            var playerPiece = new Piece(i, false);
            var aiPiece = new Piece(i+12,true);

            playerPieces.add(playerPiece);
            aiPieces.add(aiPiece);

            playerPiece.boardY = i / 4;
            aiPiece.boardY = 7 - i / 4;

            playerPiece.boardX = (i/4==1) ? 1 + (i - 4*(i/4))*2 : (i - 4*(i/4))*2;
            aiPiece.boardX = (i/4==1) ? (i - 4*(i/4))*2 : 1 + (i - 4*(i/4))*2;

            board[playerPiece.boardX][playerPiece.boardY] = playerPiece;
            board[aiPiece.boardX][aiPiece.boardY] = aiPiece;
        }
    }

    private void removePieces(List<PossibleCapture> possibleCaptures, int[] toXY) {
        for (var capture : possibleCaptures) {
            if (Arrays.equals(capture.endPosition, toXY)) {

                for (var remPiece : capture.toRemovePieces) {
                    if (remPiece.isAI) {
                        aiPieces.removeIf(p -> p.ID == remPiece.ID);
                    } else {
                        playerPieces.removeIf(p -> p.ID == remPiece.ID);
                    }
                    board[remPiece.boardX][remPiece.boardY] = null;
                }

                return;
            }
        }
    }

    private void removePossibleCapturesFromBoard(int[] fromXY, int[] toXY) {
        var piece = board[fromXY[0]][fromXY[1]];
        List<PossibleCapture> possibleCaptures;

        if (piece.isKing) {
            if (!kingCapturePossible(fromXY[0], fromXY[1], piece.isAI)) return;
            possibleCaptures = getResultsAfterKingCapture(fromXY[0], fromXY[1], piece.isAI, new ArrayList<>(), new ArrayList<>());
        } else {
            if (piece.isAI) {
                if (!bottomCapturePossible(fromXY[0], fromXY[1], true)) return;
                possibleCaptures = getResultsAfterBottomCapture(fromXY[0], fromXY[1], true, new ArrayList<>());
            } else {
                if (!topCapturePossible(fromXY[0], fromXY[1], false)) return;
                possibleCaptures = getResultsAfterTopCapture(fromXY[0], fromXY[1], false, new ArrayList<>());
            }
        }
        removePieces(possibleCaptures, toXY);
    }

    void makeMove(int[] fromXY, int[] toXY) {
        removePossibleCapturesFromBoard(fromXY, toXY);

        var piece = board[fromXY[0]][fromXY[1]];

        board[toXY[0]][toXY[1]] = piece;
        board[fromXY[0]][fromXY[1]] = null;

        piece.boardX = toXY[0];
        piece.boardY = toXY[1];

        if (piece.isAI && piece.boardY == 0) piece.isKing = true;
        if (!piece.isAI && piece.boardY == 7) piece.isKing = true;
    }

    private boolean kingCapturePossible(int x, int y, boolean isAI) {
        return topCapturePossible(x, y, isAI) || bottomCapturePossible(x, y, isAI);
    }
    private boolean topCapturePossible(int x, int y, boolean isAI) {
        return topLeftCapturePossible(x, y, isAI) || topRightCapturePossible(x, y, isAI);
    }
    private boolean bottomCapturePossible(int x, int y, boolean isAI) {
        return bottomLeftCapturePossible(x, y, isAI) || bottomRightCapturePossible(x, y, isAI);
    }
    private boolean topLeftCapturePossible(int x, int y, boolean isAI) {
        return (x > 1 && y < 6) && (board[x-1][y+1] != null && board[x-2][y+2] == null) && board[x-1][y+1].isAI != isAI;
    }
    private boolean topRightCapturePossible(int x, int y, boolean isAI) {
        return (x < 6 && y < 6) && (board[x+1][y+1] != null && board[x+2][y+2] == null) && board[x+1][y+1].isAI != isAI;
    }
    private boolean bottomLeftCapturePossible(int x, int y, boolean isAI) {
        return (x > 1 && y > 1) && (board[x-1][y-1] != null && board[x-2][y-2] == null) && board[x-1][y-1].isAI != isAI;
    }
    private boolean bottomRightCapturePossible(int x, int y, boolean isAI) {
        return (x < 6 && y > 1) && (board[x+1][y-1] != null && board[x+2][y-2] == null) && board[x+1][y-1].isAI != isAI;
    }

    // FIRST CAPTURE MUST BE POSSIBLE IN ORDER FOR THIS METHOD TO FUNCTION CORRECTLY
    private List<PossibleCapture> getResultsAfterKingCapture(int x, int y, boolean isAI, List<int[]> noJumpList, List<Piece> capturedPieces) {
        var topRightOK = topRightCapturePossible(x, y, isAI) && !multArrContainsArray(noJumpList, new int[]{x+2, y+2});
        var topLeftOK = topLeftCapturePossible(x, y, isAI) && !multArrContainsArray(noJumpList, new int[]{x-2, y+2});
        var bottomRightOK = bottomRightCapturePossible(x, y, isAI) && !multArrContainsArray(noJumpList, new int[]{x+2, y-2});
        var bottomLeftOK = bottomLeftCapturePossible(x, y, isAI) && !multArrContainsArray(noJumpList, new int[]{x-2, y-2});

        // Kings must not jump back and forth between two fields (stack overflow error)
        // {x, y} is current field. Next field must not jump on this one.
        noJumpList.add(new int[]{x, y});

        var possibleCaptures = new ArrayList<PossibleCapture>();

        if (topRightOK) {
            var capturedPiecesCopy = new ArrayList<>(capturedPieces);
            capturedPiecesCopy.add(board[x+1][y+1]);
            possibleCaptures.addAll(getResultsAfterKingCapture(x+2, y+2, isAI, noJumpList, capturedPiecesCopy));
        }
        if (topLeftOK) {
            var capturedPiecesCopy = new ArrayList<>(capturedPieces);
            capturedPiecesCopy.add(board[x-1][y+1]);
            possibleCaptures.addAll(getResultsAfterKingCapture(x-2, y+2, isAI, noJumpList, capturedPiecesCopy));
        }
        if (bottomRightOK) {
            var capturedPiecesCopy = new ArrayList<>(capturedPieces);
            capturedPiecesCopy.add(board[x+1][y-1]);
            possibleCaptures.addAll(getResultsAfterKingCapture(x+2, y-2, isAI, noJumpList, capturedPiecesCopy));
        }
        if (bottomLeftOK) {
            var capturedPiecesCopy = new ArrayList<>(capturedPieces);
            capturedPiecesCopy.add(board[x-1][y-1]);
            possibleCaptures.addAll(getResultsAfterKingCapture(x-2, y-2, isAI, noJumpList, capturedPiecesCopy));
        }

        if (!(topRightOK || topLeftOK || bottomRightOK || bottomLeftOK)) {
            possibleCaptures.add(new PossibleCapture(new int[]{x, y}, capturedPieces));
        }

        return possibleCaptures;
    }
    // FIRST CAPTURE MUST BE POSSIBLE IN ORDER FOR THIS METHOD TO FUNCTION CORRECTLY
    private List<PossibleCapture> getResultsAfterTopCapture(int x, int y, boolean isAI, List<Piece> capturedPieces) {
        var topRightOK = topRightCapturePossible(x, y, isAI);
        var topLeftOK = topLeftCapturePossible(x, y, isAI);

        var possibleCaptures = new ArrayList<PossibleCapture>();

        if (topRightOK) {
            var capturedPiecesCopy = new ArrayList<>(capturedPieces);
            capturedPiecesCopy.add(board[x+1][y+1]);
            possibleCaptures.addAll(getResultsAfterTopCapture(x+2, y+2, isAI, capturedPiecesCopy));
        }
        if (topLeftOK) {
            var capturedPiecesCopy = new ArrayList<>(capturedPieces);
            capturedPiecesCopy.add(board[x-1][y+1]);
            possibleCaptures.addAll(getResultsAfterTopCapture(x-2, y+2, isAI, capturedPiecesCopy));
        }

        if (!(topRightOK || topLeftOK)) {
            possibleCaptures.add(new PossibleCapture(new int[]{x, y}, capturedPieces));
        }

        return possibleCaptures;
    }
    // FIRST CAPTURE MUST BE POSSIBLE IN ORDER FOR THIS METHOD TO FUNCTION CORRECTLY
    private List<PossibleCapture> getResultsAfterBottomCapture(int x, int y, boolean isAI, List<Piece> capturedPieces) {
        var bottomRightOK = bottomRightCapturePossible(x, y, isAI);
        var bottomLeftOK = bottomLeftCapturePossible(x, y, isAI);

        var possibleCaptures = new ArrayList<PossibleCapture>();

        if (bottomRightOK) {
            var capturedPiecesCopy = new ArrayList<>(capturedPieces);
            capturedPiecesCopy.add(board[x+1][y-1]);
            possibleCaptures.addAll(getResultsAfterBottomCapture(x+2, y-2, isAI, capturedPiecesCopy));
        }
        if (bottomLeftOK) {
            var capturedPiecesCopy = new ArrayList<>(capturedPieces);
            capturedPiecesCopy.add(board[x-1][y-1]);
            possibleCaptures.addAll(getResultsAfterBottomCapture(x-2, y-2, isAI, capturedPiecesCopy));
        }

        if (!(bottomRightOK || bottomLeftOK)) {
            possibleCaptures.add(new PossibleCapture(new int[]{x, y}, capturedPieces));
        }

        return possibleCaptures;
    }

    private boolean canMoveBottomLeft(int x, int y) {
        return x > 0 && y > 0 && board[x-1][y-1] == null;
    }
    private boolean canMoveBottomRight(int x, int y) {
        return x < 7 && y > 0 && board[x+1][y-1] == null;
    }
    private boolean canMoveTopLeft(int x, int y) {
        return x > 0 && y < 7 && board[x-1][y+1] == null;
    }
    private boolean canMoveTopRight(int x, int y) {
        return x < 7 && y < 7 && board[x+1][y+1] == null;
    }

    private List<int[]> getTopMovesBesidesCapture(int x, int y) {
        var moves = new ArrayList<int[]>();
        if (canMoveTopLeft(x, y)) moves.add(new int[]{x-1, y+1});
        if (canMoveTopRight(x, y)) moves.add(new int[]{x+1, y+1});
        return moves;
    }
    private List<int[]> getBottomMovesBesidesCapture(int x, int y) {
        var moves = new ArrayList<int[]>();
        if (canMoveBottomLeft(x, y)) moves.add(new int[]{x-1, y-1});
        if (canMoveBottomRight(x, y)) moves.add(new int[]{x+1, y-1});
        return moves;
    }
    private List<int[]> getKingMovesBesidesCapture(int x, int y) {
        var moves = new ArrayList<int[]>();
        moves.addAll(getTopMovesBesidesCapture(x, y));
        moves.addAll(getBottomMovesBesidesCapture(x, y));
        return moves;
    }

    List<Piece> getPiecesWhichMustCapture(boolean isAIsTurn) {
        var mustCapturePieces = new ArrayList<Piece>();
        var allPieces = isAIsTurn ? aiPieces : playerPieces;

        for (var p : allPieces) {
            if (p.isKing && kingCapturePossible(p.boardX, p.boardY, isAIsTurn)) {
                mustCapturePieces.add(p);
            } else if (!p.isKing) {
                if (isAIsTurn && bottomCapturePossible(p.boardX, p.boardY, true)) {
                    mustCapturePieces.add(p);
                } else if (!isAIsTurn && topCapturePossible(p.boardX, p.boardY, false)) {
                    mustCapturePieces.add(p);
                }
            }
        }
        return mustCapturePieces;
    }

    private boolean pieceNotMoveableBecauseOtherCanCapture(Piece piece) {
        var piecesWhichMustCapture = getPiecesWhichMustCapture(piece.isAI);
        if (piecesWhichMustCapture.size() > 0) {
            // if pieces can capture, but current piece is none of them, return empty possible moves for that piece.
            piecesWhichMustCapture.removeIf(p -> !(p.boardX == piece.boardX && p.boardY == piece.boardY));
            return piecesWhichMustCapture.size() == 0;
        }
        return false;
    }

    // -1 => game not over, 0 => player won, 1 => AI won
    int gameIsOver(boolean isAIsTurn) {
        var allPieces = isAIsTurn ? aiPieces : playerPieces;

        for (var piece : allPieces) {
            if (pieceNotMoveableBecauseOtherCanCapture(piece)) return -1;
            if (genAllPossibleMoves(piece.boardX, piece.boardY).size() > 0) return -1;
        }
        return isAIsTurn ? 0 : 1;
    }

    List<int[]> genAllPossibleMoves(int x, int y) {
        var piece = board[x][y];

        if (pieceNotMoveableBecauseOtherCanCapture(piece)) return new ArrayList<>();

        if (piece.isKing) {
            if (kingCapturePossible(x, y, piece.isAI)) {
                var captureResults = getResultsAfterKingCapture(x, y, piece.isAI, new ArrayList<>(), new ArrayList<>());
                return extractPossibleCapturePositions(captureResults);
            }
            return getKingMovesBesidesCapture(x, y);
        }

        if (piece.isAI) {
            if (bottomCapturePossible(x, y, true)) {
                var captureResults = getResultsAfterBottomCapture(x, y, true, new ArrayList<>());
                return extractPossibleCapturePositions(captureResults);
            }
            return getBottomMovesBesidesCapture(x, y);
        }

        if (topCapturePossible(x, y, false)) {
            var captureResults = getResultsAfterTopCapture(x, y, false, new ArrayList<>());
            return extractPossibleCapturePositions(captureResults);
        }
        return getTopMovesBesidesCapture(x, y);
    }

    boolean isLegalMove(int[] fromXY, int[] toXY) {
        if (board[toXY[0]][toXY[1]] != null) return false;

        var possibleMoves = genAllPossibleMoves(fromXY[0], fromXY[1]);

        return multArrContainsArray(possibleMoves, toXY);
    }

    private static boolean multArrContainsArray(List<int[]> multArray, int[] array) {
        for (int[] arr : multArray) {
            if (Arrays.equals(arr, array)) return true;
        }
        return false;
    }

    boolean fieldHasPlayersPiece(int x, int y) {
        return board[x][y] != null && !board[x][y].isAI;
    }

    private List<int[]> extractPossibleCapturePositions(List<PossibleCapture> possibleCaptures) {
        var positionsArray = new ArrayList<int[]>();
        for (var capture : possibleCaptures) {
            positionsArray.add(capture.endPosition);
        }
        return positionsArray;
    }
}

class PossibleCapture {
    int[] endPosition;
    List<Piece> toRemovePieces;

    PossibleCapture(int[] endPosition, List<Piece> toRemovePieces) {
        this.endPosition = endPosition;
        this.toRemovePieces = toRemovePieces;
    }
}