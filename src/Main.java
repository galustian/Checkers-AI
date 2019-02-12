import java.awt.*;
import java.util.Arrays;

public class Main {
    private static int MIN_DEPTH = 5;
    private static float MAX_SEARCH_TIME = 10.3f;
    private static boolean AI_STARTS = false;

    public static void main(String[] args) {
        StdDraw.setCanvasSize(512, 512);
        StdDraw.setXscale(0, 512);
        StdDraw.setYscale(0, 512);
        startGame();
    }

    private static void startGame() {
        var board = new Board();
        Helpers.drawBoard(board);

        var isAIsTurn = AI_STARTS;
        var gameOver = -1;

        var fromXY = new int[]{-1, -1};
        var toXY = new int[]{-1, -1};

        while (gameOver == -1) {
            if (isAIsTurn) {
                makeBestMove(board);
                Helpers.drawBoard(board);

                isAIsTurn = false;
                gameOver = board.gameIsOver(false);
            } else {
                if (StdDraw.isMousePressed()) {
                    int x = Helpers.realToBlockSize((float)StdDraw.mouseX());
                    int y = Helpers.realToBlockSize((float)StdDraw.mouseY());

                    if (board.fieldHasPlayersPiece(x, y)) {
                        fromXY[0] = x;
                        fromXY[1] = y;

                        var possibleMoves = board.genAllPossibleMoves(x, y);
                        if (possibleMoves.size() > 0) {
                            Helpers.drawBoard(board);
                            Helpers.highlightLegalMoves(possibleMoves);
                        }
                    } else if (fromXY[0] != -1 && board.isLegalMove(fromXY, new int[]{x, y})) {
                        toXY[0] = x;
                        toXY[1] = y;

                        board.makeMove(fromXY, toXY);
                        Helpers.drawBoard(board);

                        fromXY = new int[]{-1, 1};
                        toXY = new int[]{-1, 1};

                        isAIsTurn = true;
                        gameOver = board.gameIsOver(true);
                    }
                    StdDraw.pause(230);
                }
            }
        }

        StdDraw.setPenColor(StdDraw.PRINCETON_ORANGE);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 80));
        StdDraw.text(256, 256, gameOver == 0 ? "You Won!" : "AI Won");
    }

    private static void makeBestMove(Board board) {
        int[] bestFromXY = new int[]{-1, -1};
        int[] bestToXY = new int[]{-1, -1};

        var depth = MIN_DEPTH;
        var bestValue = -1e9f;
        var startTime = System.nanoTime();

        while (true) {
            // used for Alpha-Beta optimization
            var nodePathMaxMinScores = new float[]{-1e9f, 1e9f}; // AI max-score idx: 0
            int[] currBestFromXY = new int[]{-1, -1};
            int[] currBestToXY = new int[]{-1, -1};

            try {
                for (var p : board.aiPieces) {
                    var allMoves = board.genAllPossibleMoves(p.boardX, p.boardY);
                    var fromXY = new int[]{p.boardX, p.boardY};

                    for (var toXY : allMoves) {
                        var boardAfterMove = new Board(board);
                        boardAfterMove.makeMove(fromXY, toXY);

                        var moveValue = getMoveValue(boardAfterMove, false, nodePathMaxMinScores.clone(), depth-1, startTime);

                        if (moveValue > nodePathMaxMinScores[0]) {
                            nodePathMaxMinScores[0] = moveValue;

                            currBestFromXY = fromXY;
                            currBestToXY = toXY;
                        }
                        if (moveValue >= bestValue) {
                            bestValue = moveValue;
                            bestFromXY = fromXY;
                            bestToXY = toXY;
                        }
                    }
                }

                bestFromXY = currBestFromXY;
                bestToXY = currBestToXY;

                System.out.println("At depth " + depth + ":");
                System.out.println("Score: " + nodePathMaxMinScores[0]);
                System.out.println("From: " + Arrays.toString(bestFromXY));
                System.out.println("To: " + Arrays.toString(bestToXY));
                System.out.println();

                if (nodePathMaxMinScores[0] == 1e5f) break; // AI won for sure

            } catch (RuntimeException e) {
                if (e.getMessage().equals("search-timeout")) break;
                else System.out.println(e.getMessage().toUpperCase());
            }
            depth++;
        }
        board.makeMove(bestFromXY, bestToXY);
    }

    // optimization: evaluate moves which capture first => more cutoffs
    private static float getMoveValue(Board board, boolean isAIsTurn, float[] nodePathMaxMinScores, int depth, long startTime) throws RuntimeException {
        if (depth > MIN_DEPTH && ((System.nanoTime() - startTime) / 1e9 > MAX_SEARCH_TIME)) {
            throw new RuntimeException("search-timeout");
        }

        var allPieces = isAIsTurn ? board.aiPieces : board.playerPieces;
        var optValue = isAIsTurn ? -1e5f : 1e5f;

        // optimization possible: boolean anyPieceMustCapture()
        // while depth is 0, but pieces can be captured: do so, stop if no pieces can be captured anymore (useful heuristic)
        if (depth <= 0 && board.getPiecesWhichMustCapture(isAIsTurn).size() == 0) return getBoardScore(board);

        for (var p : allPieces) {
            var allMoves = board.genAllPossibleMoves(p.boardX, p.boardY);

            for (var moveToXY : allMoves) {
                var boardAfterMove = new Board(board);
                boardAfterMove.makeMove(new int[]{p.boardX, p.boardY}, moveToXY);

                var moveValue = getMoveValue(boardAfterMove, !isAIsTurn, nodePathMaxMinScores.clone(), depth-1, startTime);

                if (isAIsTurn) {
                    // Alpha-Beta tree pruning
                    // previous Node is Player (minimizer) =>
                    // if that previous Node or other previous connected Player Nodes have minimums,
                    // which are smaller than the current AI move (AI chooses maximum), then all other AI moves don't matter,
                    // since all others the current AI's maximum will be >= current_value.
                    if (moveValue > nodePathMaxMinScores[1]) return moveValue;
                    if (moveValue > nodePathMaxMinScores[0]) {
                        nodePathMaxMinScores[0] = moveValue;
                    }
                    optValue = Math.max(optValue, moveValue);
                } else {
                    if (moveValue < nodePathMaxMinScores[0]) return moveValue;
                    if (moveValue < nodePathMaxMinScores[1]) {
                        nodePathMaxMinScores[1] = moveValue;
                    }
                    optValue = Math.min(optValue, moveValue);
                }
            }
        }
        return optValue;
    }

    // good for ai => high score
    private static float getBoardScore(Board board) {
        var numKingsAI = Helpers.numPieces(board, true, true);
        var numKingsPlayer = Helpers.numPieces(board, false, true);

        var numNormalPiecesAI = Helpers.numPieces(board, true, false);
        var numNormalPiecesPlayer = Helpers.numPieces(board, false, false);

        float kingWeight = 2;
        float normalWeight = 1;

        return normalWeight * (numNormalPiecesAI - numNormalPiecesPlayer) + kingWeight * (numKingsAI - numKingsPlayer);
    }
}
