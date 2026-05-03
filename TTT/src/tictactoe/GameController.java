package tictactoe;

import javax.swing.*;
import java.util.ArrayDeque;
import java.util.Deque;

public class GameController {
    private Board board;
    private final GameLogic logic;
    private final GameWindow window;
    private AIPlayer ai;
    private boolean gameOver = false;
    private boolean vsAI = false;
    private char humanChar = 'X';
    private int firstPlayerMode = 0; // 0=X first, 1=O first, 2=random

    // Move history for undo. Each entry: {row, col, player}
    private final Deque<int[]> moveHistory = new ArrayDeque<>();

    // Whether AI is currently scheduled to move (so we can debounce undo / clicks)
    private boolean aiPending = false;
    private Timer aiTimer;

    public GameController(Board board, GameLogic logic, GameWindow window) {
        this.board = board;
        this.logic = logic;
        this.window = window;
    }

    public void setAI(AIPlayer ai, char humanChar) {
        this.ai = ai;
        this.vsAI = (ai != null);
        this.humanChar = humanChar;
    }

    public void setFirstPlayerMode(int mode) { this.firstPlayerMode = mode; }

    public boolean isVsAI()         { return vsAI; }
    public boolean isGameOver()     { return gameOver; }
    public boolean isAIPending()    { return aiPending; }
    public char    getHumanChar()   { return humanChar; }
    public AIPlayer getAI()         { return ai; }

    public void handleCellClick(int row, int col) {
        if (gameOver || aiPending) return;
        if (vsAI && logic.getCurrentPlayer(board) != humanChar) return;

        char current = logic.getCurrentPlayer(board);
        if (!logic.makeMove(board, row, col)) return;

        moveHistory.push(new int[]{row, col, current});
        SoundEngine.playPlace();
        window.getBoardPanel().updateBoard(board.getGrid());
        window.getBoardPanel().setLastMove(row, col);
        window.refreshActionButtons();

        if (checkAndHandleEnd(current)) return;

        if (vsAI) {
            window.addLog("💬 AI: \"" + TrashTalk.getForAI(ai.getDifficulty()) + "\"");
            scheduleAIMove();
        } else {
            char next = logic.getCurrentPlayer(board);
            window.updateTurn(next);
            if (Math.random() < 0.4) {
                window.addLog("💬 \"" + TrashTalk.getForPlayer() + "\"");
            }
        }
    }

    /** Public entry for "AI moves first" at game start. */
    public void kickoffIfAIFirst() {
        if (vsAI && logic.getCurrentPlayer(board) != humanChar) scheduleAIMove();
    }

    private void scheduleAIMove() {
        aiPending = true;
        window.setAIThinking(true);
        window.refreshActionButtons();
        if (aiTimer != null) aiTimer.stop();
        aiTimer = new Timer(700, e -> {
            aiPending = false;
            window.setAIThinking(false);
            doAIMove();
            window.refreshActionButtons();
        });
        aiTimer.setRepeats(false);
        aiTimer.start();
    }

    private void doAIMove() {
        if (gameOver) return;
        char aiChar = ai.getAIChar();
        int[] move = ai.getBestMove(board);
        if (move == null) return;
        logic.makeMove(board, move[0], move[1]);
        moveHistory.push(new int[]{move[0], move[1], aiChar});
        SoundEngine.playPlace();
        window.getBoardPanel().updateBoard(board.getGrid());
        window.getBoardPanel().setLastMove(move[0], move[1]);
        if (!checkAndHandleEnd(aiChar)) {
            window.updateTurn(humanChar);
        }
    }

    /** Undo last move (or last 2 if vs AI so it's the human's turn again). */
    public boolean undo() {
        // Cancel any pending AI move first.
        if (aiPending) {
            if (aiTimer != null) aiTimer.stop();
            aiPending = false;
            window.setAIThinking(false);
        }
        if (moveHistory.isEmpty()) return false;

        if (gameOver) {
            gameOver = false;
            window.getBoardPanel().clearWinLine();
        }

        popOne();
        // In vs-AI mode, also undo the move below if it leaves the AI to play
        // (so we always end on the human's turn).
        if (vsAI && !moveHistory.isEmpty()) {
            char nowToPlay = logic.getCurrentPlayer(board);
            if (nowToPlay != humanChar) popOne();
        }

        SoundEngine.playUndo();
        window.getBoardPanel().updateBoard(board.getGrid());
        if (!moveHistory.isEmpty()) {
            int[] last = moveHistory.peek();
            window.getBoardPanel().setLastMove(last[0], last[1]);
        } else {
            window.getBoardPanel().clearLastMove();
        }
        window.updateTurn(logic.getCurrentPlayer(board));
        window.addLog("↻ Undo");
        window.refreshActionButtons();
        return true;
    }

    private int popOne() {
        int[] m = moveHistory.pop();
        board.setCell(m[0], m[1], 'E');
        return m[2];
    }

    /** Suggest the best move for the current player (uses minimax). */
    public int[] computeHint() {
        if (gameOver) return null;
        char player = logic.getCurrentPlayer(board);
        if (vsAI && player != humanChar) return null;
        return AIPlayer.computeBestMove(board, player);
    }

    public boolean canUndo() {
        if (gameOver && moveHistory.isEmpty()) return false;
        if (aiPending) return true; // we'll cancel + undo
        if (moveHistory.isEmpty()) return false;
        if (vsAI) {
            // Need at least one human move in history
            for (int[] m : moveHistory) if (m[2] == humanChar) return true;
            return false;
        }
        return true;
    }

    public boolean canHint() {
        if (gameOver || aiPending) return false;
        if (vsAI && logic.getCurrentPlayer(board) != humanChar) return false;
        return hasEmptyCell();
    }

    private boolean hasEmptyCell() {
        for (int r = 0; r < 3; r++) for (int c = 0; c < 3; c++)
            if (board.getCell(r, c) == 'E') return true;
        return false;
    }

    private boolean checkAndHandleEnd(char lastPlayer) {
        if (logic.checkWin(board, lastPlayer)) {
            gameOver = true;
            int[][] winCells = findWinCells(board, lastPlayer);
            if (winCells != null) {
                window.getBoardPanel().animateWinLine(winCells[0], winCells[winCells.length - 1]);
            }
            Timer t = new Timer(400, e -> {
                SoundEngine.playWin();
                window.recordWin(lastPlayer);
                window.showConfetti();
                recordStatsForGame(lastPlayer);
                window.refreshActionButtons();
            });
            t.setRepeats(false);
            t.start();
            return true;
        }
        if (logic.isDraw(board)) {
            gameOver = true;
            SoundEngine.playDraw();
            window.recordDraw();
            recordStatsForGame('D');
            window.refreshActionButtons();
            return true;
        }
        return false;
    }

    private void recordStatsForGame(char winnerOrDraw) {
        int movesPlayed = moveHistory.size();
        if (vsAI) {
            char result;
            if (winnerOrDraw == 'D') result = 'D';
            else result = (winnerOrDraw == humanChar) ? 'W' : 'L';
            Stats.recordAI(ai.getDifficulty(), result);

            // Achievement checks
            AchievementBus bus = window.achievements();
            if (result == 'W') {
                if (Stats.unlock("first_win")) bus.toast("First Win!", "Achievement unlocked");
                int s = Stats.ai(ai.getDifficulty()).currentStreak;
                if (s == 3 && Stats.unlock("streak_3_d" + ai.getDifficulty()))
                    bus.toast(s + " Win Streak", "Keep going");
                if (s == 5 && Stats.unlock("streak_5_d" + ai.getDifficulty()))
                    bus.toast(s + " Win Streak!", "On fire");
                if (s == 10 && Stats.unlock("streak_10_d" + ai.getDifficulty()))
                    bus.toast("10 Win Streak!", "Unstoppable");
                if (ai.getDifficulty() == 2 && Stats.unlock("beat_hard"))
                    bus.toast("Beat the Bot", "You beat Hard mode");
                if (movesPlayed <= 5 && Stats.unlock("speed_win"))
                    bus.toast("Speed Win", "Won in 5 moves or fewer");
            } else if (result == 'D' && ai.getDifficulty() == 2) {
                if (Stats.unlock("draw_hard"))
                    bus.toast("Draw vs Hard", "A perfect game");
            }
        } else {
            Stats.recordPvP(winnerOrDraw);
        }
        window.refreshStatsStrip();
    }

    private int[][] findWinCells(Board board, char player) {
        char[][] g = board.getGrid();
        for (int r = 0; r < 3; r++)
            if (g[r][0]==player && g[r][1]==player && g[r][2]==player)
                return new int[][]{{r,0},{r,1},{r,2}};
        for (int c = 0; c < 3; c++)
            if (g[0][c]==player && g[1][c]==player && g[2][c]==player)
                return new int[][]{{0,c},{1,c},{2,c}};
        if (g[0][0]==player && g[1][1]==player && g[2][2]==player)
            return new int[][]{{0,0},{1,1},{2,2}};
        if (g[0][2]==player && g[1][1]==player && g[2][0]==player)
            return new int[][]{{0,2},{1,1},{2,0}};
        return null;
    }

    public void reset() {
        if (aiTimer != null) aiTimer.stop();
        aiPending = false;
        window.setAIThinking(false);

        board = new Board("board.csv");
        board.clearBoard();
        moveHistory.clear();
        gameOver = false;
        window.getBoardPanel().clearWinLine();
        window.getBoardPanel().clearLastMove();
        window.getBoardPanel().updateBoard(board.getGrid());

        char first = logic.getCurrentPlayer(board); // always X on a cleared board
        window.updateTurn(first);
        window.addLog("── New game started ──");
        window.refreshActionButtons();

        // If AI is X, it moves first.
        if (vsAI && first != humanChar) scheduleAIMove();
    }

    public Board getBoard()    { return board; }
    public GameLogic getLogic(){ return logic; }
}
