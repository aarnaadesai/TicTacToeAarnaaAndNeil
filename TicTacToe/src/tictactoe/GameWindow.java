package tictactoe;
import javax.swing.*;

public class GameWindow extends JFrame {
    private BoardPanel boardPanel;
    public GameWindow() {
        setTitle("Tic-Tac-Toe Pro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 430);
        setLocationRelativeTo(null);
    }
    public void setBoardPanel(BoardPanel p) { this.boardPanel = p; add(p); setVisible(true); }
    public BoardPanel getBoardPanel() { return boardPanel; }
}
