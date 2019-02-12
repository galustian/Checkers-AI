import java.awt.*;
import java.util.List;

class Helpers {
    private static int blockSize = 512 / 8;
    private static int blockHalf = blockSize / 2;

    static void drawBoard(Board board) {
        StdDraw.picture(256, 256, "images/board.png", 512, 512);

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                var piece = board.board[i][j];
                if (piece != null) {
                    drawCircleXY(piece.boardX, piece.boardY, piece.isAI ? Color.LIGHT_GRAY : Color.RED, piece.isKing);
                }
            }
        }
    }

    private static void drawCircleXY(int x, int y, Color color, boolean isKing) {
        int realX = x * blockSize + blockHalf;
        int realY = y * blockSize + blockHalf;

        StdDraw.setPenColor(color);
        StdDraw.filledCircle(realX, realY, blockHalf - (float)(blockHalf / 8));
        if (isKing) {
            StdDraw.setPenColor(StdDraw.YELLOW);
            StdDraw.filledCircle(realX, realY, 8);
        }
    }

    static void highlightLegalMoves(List<int[]> legalMoves) {
        for (int[] move: legalMoves) {
            drawCircleXY(move[0], move[1], Color.DARK_GRAY, false);
        }
    }

    static int numPieces(Board board, boolean isAI, boolean kingPiece) {
        int num = 0;
        var allPieces = isAI ? board.aiPieces : board.playerPieces;

        for (var p : allPieces) {
            if (p.isAI == isAI && p.isKing == kingPiece) num++;
        }
        return num;
    }

    static int realToBlockSize(float real) {
        return (int) (real / (float) blockSize);
    }
}
