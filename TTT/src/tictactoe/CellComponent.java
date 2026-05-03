package tictactoe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;

public class CellComponent extends JPanel {
    private char state = 'E';

    // Drawing-stroke animation (0 → 1 traces the symbol)
    private float drawProgress = 1f;
    private Timer drawTimer;

    // Glow burst on placement
    private float glowAlpha = 0f;
    private Timer glowTimer;

    // Last-move ring pulse
    private boolean isLastMove = false;
    private float lastMovePhase = 0f;
    private Timer lastMoveTimer;

    // Hint pulse (pulses to show suggested move)
    private boolean isHint = false;
    private float hintPhase = 0f;
    private Timer hintTimer;

    // Slide-in on appear
    private float slideY = 0f;
    private Timer slideTimer;

    public CellComponent(Runnable onClick) {
        setOpaque(true);
        setBackground(Theme.cell());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createLineBorder(Theme.cellBorder(), 1));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (state == 'E') { SoundEngine.playClick(); onClick.run(); }
            }
            @Override public void mouseEntered(MouseEvent e) {
                if (state == 'E') { setBackground(Theme.cellHover()); repaint(); }
            }
            @Override public void mouseExited(MouseEvent e) {
                setBackground(Theme.cell()); repaint();
            }
        });
    }

    public void setState(char s) {
        char prev = this.state;
        this.state = s;
        setBackground(Theme.cell());

        if (s != 'E' && prev == 'E') {
            // Slide in
            slideY = -getHeight() * 0.25f;
            if (slideTimer != null) slideTimer.stop();
            slideTimer = new Timer(16, null);
            slideTimer.addActionListener(e -> {
                slideY += (0 - slideY) * 0.30f;
                if (Math.abs(slideY) < 0.5f) { slideY = 0; slideTimer.stop(); }
                repaint();
            });
            slideTimer.start();

            // Stroke draw
            drawProgress = 0f;
            if (drawTimer != null) drawTimer.stop();
            drawTimer = new Timer(16, null);
            drawTimer.addActionListener(e -> {
                drawProgress = Math.min(1f, drawProgress + 0.10f);
                repaint();
                if (drawProgress >= 1f) drawTimer.stop();
            });
            drawTimer.start();

            // Glow
            glowAlpha = 1f;
            if (glowTimer != null) glowTimer.stop();
            glowTimer = new Timer(30, null);
            glowTimer.addActionListener(e -> {
                glowAlpha = Math.max(0f, glowAlpha - 0.05f);
                repaint();
                if (glowAlpha <= 0f) glowTimer.stop();
            });
            glowTimer.start();
        } else if (s == 'E') {
            // Cleared (e.g. undo / reset)
            slideY = 0;
            drawProgress = 1f;
            glowAlpha = 0f;
        }
        repaint();
    }

    public char getState() { return state; }

    public void setLastMove(boolean v) {
        this.isLastMove = v;
        if (v) {
            lastMovePhase = 0f;
            if (lastMoveTimer != null) lastMoveTimer.stop();
            lastMoveTimer = new Timer(40, null);
            lastMoveTimer.addActionListener(e -> {
                lastMovePhase += 0.08f;
                repaint();
            });
            lastMoveTimer.start();
        } else {
            if (lastMoveTimer != null) lastMoveTimer.stop();
        }
        repaint();
    }

    public void pulseHint() {
        isHint = true;
        hintPhase = 0f;
        if (hintTimer != null) hintTimer.stop();
        hintTimer = new Timer(30, null);
        hintTimer.addActionListener(e -> {
            hintPhase += 0.08f;
            if (hintPhase > Math.PI * 2 * 2) {  // ~2 full pulses
                isHint = false;
                hintTimer.stop();
            }
            repaint();
        });
        hintTimer.start();
    }

    public void refreshTheme() {
        setBackground(Theme.cell());
        setBorder(BorderFactory.createLineBorder(Theme.cellBorder(), 1));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,    RenderingHints.VALUE_STROKE_PURE);

        int w = getWidth(), h = getHeight();
        int offsetY = (int) slideY;

        // Last-move soft ring (under symbol)
        if (isLastMove && state != 'E') {
            float pulse = 0.5f + 0.5f * (float)Math.sin(lastMovePhase);
            int alpha = (int)(40 + pulse * 50);
            Color base = state == 'X' ? Theme.X : Theme.O;
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha));
            g2.setStroke(new BasicStroke(2f));
            int inset = 4;
            g2.drawRoundRect(inset, inset, w - inset*2 - 1, h - inset*2 - 1, 12, 12);
        }

        // Hint pulse (blue/accent ring on empty cell)
        if (isHint && state == 'E') {
            float pulse = 0.5f + 0.5f * (float)Math.sin(hintPhase);
            int alpha = (int)(80 + pulse * 120);
            Color a = Theme.ACCENT;
            g2.setColor(new Color(a.getRed(), a.getGreen(), a.getBlue(), alpha));
            g2.setStroke(new BasicStroke(2.5f));
            int inset = 6;
            g2.drawRoundRect(inset, inset, w - inset*2 - 1, h - inset*2 - 1, 12, 12);
        }

        if (state != 'E') {
            // Glow halo
            if (glowAlpha > 0f) {
                Color base = state == 'X' ? Theme.X_GLOW : Theme.O_GLOW;
                int a = Math.min(255, (int)(glowAlpha * 110));
                g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), a));
                int gSize = (int)(Math.min(w, h) * 0.85f);
                g2.fillOval(w/2 - gSize/2, h/2 - gSize/2 + offsetY, gSize, gSize);
            }

            Color sym = state == 'X' ? Theme.X : Theme.O;
            float strokeW = Math.max(4f, Math.min(w, h) * 0.10f);
            int pad = (int)(Math.min(w, h) * 0.22f);
            int x0 = pad, y0 = pad + offsetY;
            int x1 = w - pad, y1 = h - pad + offsetY;

            // Drop-shadow tint behind symbol (so it pops on light bg)
            g2.setColor(new Color(0, 0, 0, Theme.isDark ? 90 : 25));

            if (state == 'X') {
                drawX(g2, x0, y0, x1, y1, strokeW + 2f, drawProgress, true);
                g2.setColor(sym);
                drawX(g2, x0, y0, x1, y1, strokeW, drawProgress, false);
            } else {
                drawO(g2, x0, y0, x1, y1, strokeW + 2f, drawProgress, true);
                g2.setColor(sym);
                drawO(g2, x0, y0, x1, y1, strokeW, drawProgress, false);
            }
        }
        g2.dispose();
    }

    /**
     * Animates two strokes: first slash 0..0.5 progress, second 0.5..1.
     * `shadow` flag offsets the strokes by 2px.
     */
    private void drawX(Graphics2D g2, int x0, int y0, int x1, int y1,
                       float strokeW, float progress, boolean shadow) {
        Stroke saved = g2.getStroke();
        g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int dx = shadow ? 2 : 0, dy = shadow ? 2 : 0;
        // Stroke 1: top-left to bottom-right, drawn over progress 0..0.5
        float p1 = Math.min(1f, progress * 2f);
        if (p1 > 0f) {
            float ex = x0 + (x1 - x0) * p1;
            float ey = y0 + (y1 - y0) * p1;
            g2.draw(new Line2D.Float(x0 + dx, y0 + dy, ex + dx, ey + dy));
        }
        // Stroke 2: top-right to bottom-left, drawn over progress 0.5..1
        float p2 = Math.max(0f, Math.min(1f, (progress - 0.5f) * 2f));
        if (p2 > 0f) {
            float ex = x1 + (x0 - x1) * p2;
            float ey = y0 + (y1 - y0) * p2;
            g2.draw(new Line2D.Float(x1 + dx, y0 + dy, ex + dx, ey + dy));
        }
        g2.setStroke(saved);
    }

    private void drawO(Graphics2D g2, int x0, int y0, int x1, int y1,
                       float strokeW, float progress, boolean shadow) {
        Stroke saved = g2.getStroke();
        g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int dx = shadow ? 2 : 0, dy = shadow ? 2 : 0;
        // Arc starts at top (-90°), sweeps clockwise (negative extent)
        float extent = -360f * progress;
        Arc2D arc = new Arc2D.Float(
            x0 + dx, y0 + dy, x1 - x0, y1 - y0,
            90f, extent, Arc2D.OPEN);
        g2.draw(arc);
        g2.setStroke(saved);
    }
}
