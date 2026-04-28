package tictactoe;

public class GameController {
    private Board board;
    private GameLogic logic;
    private GameWindow window;

    public GameController(Board board, GameLogic logic, GameWindow window) {
        this.board = board;
        this.logic = logic;
        this.window = window;
    }

    public void handleCellClick(int row, int col) {
        if (logic.makeMove(board, row, col)) {
            window.getBoardPanel().updateBoard(board.getGrid());
            if (logic.isGameOver(board)) {
                System.out.println("Game Over!");
            }
        }
    }
}
