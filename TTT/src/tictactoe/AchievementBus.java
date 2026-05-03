package tictactoe;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Floating toast for achievements. Slides in from the top-right of its parent,
 * holds for ~2.4s, then slides back out. Queued so multiple toasts don't overlap.
 */
public class AchievementBus extends JPanel {
    private static class Toast {
        final String title, sub;
        Toast(String t, String s) { title = t; sub = s; }
    }

    private final Deque<Toast> queue = new ArrayDeque<>();
    private Toast current;
    private float progress = 0f;     // 0=offscreen, 1=fully in
    private long  shownAt = 0L;
    private Timer animTimer;

    private static final int W = 240, H = 64;
    private static final int HOLD_MS = 2400;

    public AchievementBus() {
        setOpaque(false);
        setLayout(null);
    }

    /** Pass mouse events through — this is a decorative overlay. */
    @Override public boolean contains(int x, int y) { return false; }

    public void toast(String title, String sub) {
        queue.add(new Toast(title, sub));
        if (current == null) playNext();
    }

    private void playNext() {
        current = queue.poll();
        if (current == null) return;
        progress = 0f;
        shownAt = 0L;
        if (animTimer != null) animTimer.stop();
        animTimer = new Timer(16, e -> step());
        animTimer.start();
        repaint();
    }

    private void step() {
        long now = System.currentTimeMillis();
        if (shownAt == 0L) {
            // Slide in
            progress = Math.min(1f, progress + 0.10f);
            if (progress >= 1f) shownAt = now;
        } else if (now - shownAt > HOLD_MS) {
            // Slide out
            progress -= 0.08f;
            if (progress <= 0f) {
                animTimer.stop();
                current = null;
                repaint();
                playNext();
                return;
            }
        }
        repaint();
    }

    @Override protected void paintComponent(Graphics g) {
        if (current == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Position from top-right
        int margin = 16;
        int targetX = getWidth() - W - margin;
        int hiddenX = getWidth() + 8;
        int x = (int)(hiddenX + (targetX - hiddenX) * progress);
        int y = margin;

        // Card with gradient background
        GradientPaint gp = new GradientPaint(
            x, y,         new Color(120, 80, 255, 240),
            x + W, y + H, new Color(0, 180, 255, 240));
        g2.setPaint(gp);
        g2.fill(new RoundRectangle2D.Float(x, y, W, H, 16, 16));

        // Subtle inner border
        g2.setColor(new Color(255, 255, 255, 60));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, W - 1, H - 1, 16, 16));

        // Icon (simple star)
        int ix = x + 16, iy = y + H/2;
        g2.setColor(new Color(255, 220, 60));
        drawStar(g2, ix, iy, 10);

        // Text
        g2.setColor(Color.WHITE);
        g2.setFont(Theme.titleFont(14f));
        g2.drawString(current.title, x + 38, y + 26);
        g2.setFont(Theme.labelFont(11f));
        g2.setColor(new Color(230, 230, 255));
        g2.drawString(current.sub, x + 38, y + 44);

        g2.dispose();
    }

    private void drawStar(Graphics2D g2, int cx, int cy, int r) {
        int[] xs = new int[10];
        int[] ys = new int[10];
        for (int i = 0; i < 10; i++) {
            double ang = -Math.PI / 2 + i * Math.PI / 5;
            int rad = (i % 2 == 0) ? r : r / 2;
            xs[i] = cx + (int)(Math.cos(ang) * rad);
            ys[i] = cy + (int)(Math.sin(ang) * rad);
        }
        g2.fillPolygon(xs, ys, 10);
    }
}
