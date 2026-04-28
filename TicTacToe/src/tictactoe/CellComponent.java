package tictactoe;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CellComponent extends JComponent {
    private char state = 'E';
    private boolean hovered = false;

    public CellComponent(Runnable onClick) {
        setPreferredSize(new Dimension(100, 100));
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { onClick.run(); }
            public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
            public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
        });
    }

    public void setState(char s) { this.state = s; repaint(); }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(hovered ? Theme.HOVER : Theme.CELL);
        g2.fillRect(0, 0, getWidth(), getHeight());
        if(state != 'E') {
            g2.setColor(state == 'X' ? Theme.X : Theme.O);
            g2.setFont(Theme.FONT);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(String.valueOf(state), (getWidth()-fm.stringWidth(String.valueOf(state)))/2, 
                         ((getHeight()-fm.getHeight())/2)+fm.getAscent());
        }
    }
}
