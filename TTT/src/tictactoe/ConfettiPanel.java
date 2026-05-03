package tictactoe;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConfettiPanel extends JPanel {
    private final List<Particle> particles = new ArrayList<>();
    private Timer timer;
    private static final Random rng = new Random();

    private static final Color[] COLORS = {
        new Color(0, 220, 255), new Color(255, 60, 180),
        new Color(120, 80, 255), new Color(255, 220, 60),
        new Color(60, 255, 120), new Color(255, 140, 0)
    };

    public ConfettiPanel() {
        setOpaque(false);
        setLayout(null);
    }

    public void burst(int x, int y) {
        for (int i = 0; i < 40; i++) {
            particles.add(new Particle(x, y));
        }
        if (timer == null || !timer.isRunning()) {
            timer = new Timer(16, e -> {
                particles.removeIf(p -> !p.update());
                repaint();
                if (particles.isEmpty()) timer.stop();
            });
            timer.start();
        }
    }

    public void bigBurst() {
        int w = getWidth(), h = getHeight();
        for (int i = 0; i < 120; i++) {
            particles.add(new Particle(rng.nextInt(Math.max(1, w)), rng.nextInt(Math.max(1, h) / 2)));
        }
        if (timer == null || !timer.isRunning()) {
            timer = new Timer(16, e -> {
                particles.removeIf(p -> !p.update());
                repaint();
                if (particles.isEmpty()) timer.stop();
            });
            timer.start();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (Particle p : particles) p.draw(g2);
        g2.dispose();
    }

    private static class Particle {
        float x, y, vx, vy, alpha, rotation, rotSpeed, size;
        Color color;

        Particle(int sx, int sy) {
            x = sx; y = sy;
            double angle = rng.nextDouble() * 2 * Math.PI;
            float speed = 3f + rng.nextFloat() * 7f;
            vx = (float)(Math.cos(angle) * speed);
            vy = (float)(Math.sin(angle) * speed) - 4f;
            alpha = 1f;
            rotation = rng.nextFloat() * 360f;
            rotSpeed = (rng.nextFloat() - 0.5f) * 12f;
            size = 6f + rng.nextFloat() * 8f;
            color = COLORS[rng.nextInt(COLORS.length)];
        }

        boolean update() {
            x += vx; y += vy;
            vy += 0.25f; // gravity
            vx *= 0.98f;
            alpha -= 0.018f;
            rotation += rotSpeed;
            return alpha > 0;
        }

        void draw(Graphics2D g2) {
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(),
                Math.max(0, Math.min(255, (int)(alpha * 255)))));
            Graphics2D g3 = (Graphics2D) g2.create();
            g3.translate(x, y);
            g3.rotate(Math.toRadians(rotation));
            g3.fillRect(-(int)(size/2), -(int)(size/4), (int)size, (int)(size/2));
            g3.dispose();
        }
    }
}