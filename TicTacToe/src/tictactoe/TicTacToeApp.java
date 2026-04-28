package tictactoe;
import javax.swing.SwingUtilities;

public class TicTacToeApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Board board = new Board("board.csv");
            GameLogic logic = new GameLogic();
            GameWindow window = new GameWindow();
            GameController controller = new GameController(board, logic, window);
            
            window.setBoardPanel(new BoardPanel(controller));
            window.getBoardPanel().updateBoard(board.getGrid());
        });
    }
}
