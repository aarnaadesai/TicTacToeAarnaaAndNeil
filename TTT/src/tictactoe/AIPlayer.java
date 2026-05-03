package tictactoe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIPlayer {
    private final int difficulty; // 0=easy, 1=medium, 2=hard
    private final char aiChar;
    private final char humanChar;
    private static final Random rng = new Random();

    public AIPlayer(int difficulty, char aiChar) {
        this.difficulty = difficulty;
        this.aiChar = aiChar;
        this.humanChar = (aiChar == 'X') ? 'O' : 'X';
    }

    public int[] getBestMove(Board board) {
        switch (difficulty) {
            case 0: return getRandomMove(board);
            case 1: return getMediumMove(board);
            default: return getMinimaxMove(board);
        }
    }

    // Easy: completely random
    private int[] getRandomMove(Board board) {
        List<int[]> empty = getEmptyCells(board);
        return empty.isEmpty() ? null : empty.get(rng.nextInt(empty.size()));
    }

    // Medium: win if can, block if can, else random
    private int[] getMediumMove(Board board) {
        // Try to win
        int[] win = findWinningMove(board, aiChar);
        if (win != null) return win;
        // Try to block
        int[] block = findWinningMove(board, humanChar);
        if (block != null) return block;
        // Random
        return getRandomMove(board);
    }

    private int[] findWinningMove(Board board, char player) {
        for (int[] cell : getEmptyCells(board)) {
            board.setCell(cell[0], cell[1], player);
            GameLogic logic = new GameLogic();
            boolean wins = logic.checkWin(board, player);
            board.setCell(cell[0], cell[1], 'E');
            if (wins) return cell;
        }
        return null;
    }

    // Hard: minimax (unbeatable)
    private int[] getMinimaxMove(Board board) {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;
        for (int[] cell : getEmptyCells(board)) {
            board.setCell(cell[0], cell[1], aiChar);
            int score = minimax(board, 0, false);
            board.setCell(cell[0], cell[1], 'E');
            if (score > bestScore) { bestScore = score; bestMove = cell; }
        }
        return bestMove;
    }

    private int minimax(Board board, int depth, boolean isMaximizing) {
        GameLogic logic = new GameLogic();
        if (logic.checkWin(board, aiChar))    return 10 - depth;
        if (logic.checkWin(board, humanChar)) return depth - 10;
        if (logic.isDraw(board))              return 0;

        if (isMaximizing) {
            int best = Integer.MIN_VALUE;
            for (int[] cell : getEmptyCells(board)) {
                board.setCell(cell[0], cell[1], aiChar);
                best = Math.max(best, minimax(board, depth + 1, false));
                board.setCell(cell[0], cell[1], 'E');
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int[] cell : getEmptyCells(board)) {
                board.setCell(cell[0], cell[1], humanChar);
                best = Math.min(best, minimax(board, depth + 1, true));
                board.setCell(cell[0], cell[1], 'E');
            }
            return best;
        }
    }

    private List<int[]> getEmptyCells(Board board) {
        List<int[]> list = new ArrayList<>();
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (board.getCell(r, c) == 'E') list.add(new int[]{r, c});
        return list;
    }

    public int getDifficulty() { return difficulty; }
    public char getAIChar()    { return aiChar; }

    /** Best move for `player` on `board`, using full minimax. Used by hint feature. */
    public static int[] computeBestMove(Board board, char player) {
        char opp = (player == 'X') ? 'O' : 'X';
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;
        GameLogic logic = new GameLogic();
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board.getCell(r, c) != 'E') continue;
                board.setCell(r, c, player);
                int score = staticMinimax(board, logic, 0, false, player, opp);
                board.setCell(r, c, 'E');
                if (score > bestScore) { bestScore = score; bestMove = new int[]{r, c}; }
            }
        }
        return bestMove;
    }

    private static int staticMinimax(Board board, GameLogic logic, int depth,
                                     boolean isMax, char me, char opp) {
        if (logic.checkWin(board, me))  return 10 - depth;
        if (logic.checkWin(board, opp)) return depth - 10;
        if (logic.isDraw(board))        return 0;

        if (isMax) {
            int best = Integer.MIN_VALUE;
            for (int r = 0; r < 3; r++) for (int c = 0; c < 3; c++) {
                if (board.getCell(r, c) != 'E') continue;
                board.setCell(r, c, me);
                best = Math.max(best, staticMinimax(board, logic, depth + 1, false, me, opp));
                board.setCell(r, c, 'E');
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int r = 0; r < 3; r++) for (int c = 0; c < 3; c++) {
                if (board.getCell(r, c) != 'E') continue;
                board.setCell(r, c, opp);
                best = Math.min(best, staticMinimax(board, logic, depth + 1, true, me, opp));
                board.setCell(r, c, 'E');
            }
            return best;
        }
    }
}