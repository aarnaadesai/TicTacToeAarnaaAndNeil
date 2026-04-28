package tictactoe;
import javax.swing.*;
import java.awt.*;

public class BoardPanel extends JPanel {
    private CellComponent[][] cells = new CellComponent[3][3];

    public BoardPanel(GameController controller) {
        setLayout(new GridLayout(3, 3, 5, 5));
        setBackground(Theme.BG);
        for(int r=0; r<3; r++) {
            for(int c=0; c<3; c++) {
                final int row = r, col = c;
                cells[r][c] = new CellComponent(() -> controller.handleCellClick(row, col));
                add(cells[r][c]);
            }
        }
    }

    public void updateBoard(char[][] grid) {
        for(int r=0; r<3; r++) for(int c=0; c<3; c++) cells[r][c].setState(grid[r][c]);
    }
}
