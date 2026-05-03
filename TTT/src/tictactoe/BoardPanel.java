package tictactoe;

import javax.swing.*;
import java.awt.*;

public class BoardPanel extends JPanel {
    private CellComponent[][] cells = new CellComponent[3][3];
    private int[] winStart = null, winEnd = null;
    private float winLineProgress = 0f;
    private Timer winLineTimer;

    public BoardPanel(GameController controller) {
        setLayout(new GridLayout(3, 3, 5, 5));
        setBackground(Theme.bg());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                final int row = r, col = c;
                cells[r][c] = new CellComponent(() -> controller.handleCellClick(row, col));
                add(cells[r][c]);
            }
        }
    }

    // Tell layout managers this panel wants to grow
    @Override public Dimension getPreferredSize() {
        int size = Math.min(
            getParent() != null ? getParent().getWidth()  - 10 : 400,
            getParent() != null ? getParent().getHeight() - 10 : 400
        );
        size = Math.max(size, 200);
        return new Dimension(size, size);
    }

    @Override public Dimension getMinimumSize()   { return new Dimension(200, 200); }
    @Override public Dimension getMaximumSize()   { return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE); }

    public void updateBoard(char[][] grid) {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                cells[r][c].setState(grid[r][c]);
        repaint();
    }

    public void setLastMove(int row, int col) {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                cells[r][c].setLastMove(r == row && c == col);
        repaint();
    }

    public void clearLastMove() {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                cells[r][c].setLastMove(false);
        repaint();
    }

    public void pulseHint(int row, int col) {
        if (row < 0 || col < 0) return;
        cells[row][col].pulseHint();
    }

    public CellComponent cell(int row, int col) { return cells[row][col]; }

    public void animateWinLine(int[] start, int[] end) {
        winStart = start; winEnd = end; winLineProgress = 0f;
        if (winLineTimer != null) winLineTimer.stop();
        winLineTimer = new Timer(16, null);
        winLineTimer.addActionListener(e -> {
            winLineProgress = Math.min(1f, winLineProgress + 0.06f);
            repaint();
            if (winLineProgress >= 1f) winLineTimer.stop();
        });
        winLineTimer.start();
    }

    public void clearWinLine() {
        winStart = null; winEnd = null; winLineProgress = 0f; repaint();
    }

    public void refreshTheme() {
        setBackground(Theme.bg());
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                cells[r][c].refreshTheme();
    }

    private Point cellCenter(int row, int col) {
        int gap  = 5, pad = 5;
        int cellW = (getWidth()  - pad * 2 - gap * 2) / 3;
        int cellH = (getHeight() - pad * 2 - gap * 2) / 3;
        int x = pad + col * (cellW + gap) + cellW / 2;
        int y = pad + row * (cellH + gap) + cellH / 2;
        return new Point(x, y);
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        if (winStart != null && winEnd != null && winLineProgress > 0f) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Point s = cellCenter(winStart[0], winStart[1]);
            Point e = cellCenter(winEnd[0],   winEnd[1]);
            int ex = s.x + (int)((e.x - s.x) * winLineProgress);
            int ey = s.y + (int)((e.y - s.y) * winLineProgress);
            g2.setColor(new Color(255, 220, 60, 70));
            g2.setStroke(new BasicStroke(14f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(s.x, s.y, ex, ey);
            g2.setColor(Theme.WIN_FLASH);
            g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(s.x, s.y, ex, ey);
            g2.dispose();
        }
    }
}