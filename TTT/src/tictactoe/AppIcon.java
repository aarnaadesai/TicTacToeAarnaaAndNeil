package tictactoe;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Generates the TicTacToe Pro app icon programmatically at any size.
 * Design: rounded-square purple/cyan gradient, 3x3 grid, winning diagonal X-O-X.
 */
public class AppIcon {

    public static BufferedImage create(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,  RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,   RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // Rounded square background
        int radius = (int)(size * 0.22);
        RoundRectangle2D.Float rect =
            new RoundRectangle2D.Float(0, 0, size, size, radius, radius);
        g.setClip(rect);

        // Diagonal gradient
        GradientPaint bg = new GradientPaint(
            0,    0,    new Color( 70,  35, 145),
            size, size, new Color(  0, 165, 230));
        g.setPaint(bg);
        g.fillRect(0, 0, size, size);

        // Soft top-left highlight
        RadialGradientPaint glow = new RadialGradientPaint(
            new Point2D.Float(size * 0.30f, size * 0.25f),
            size * 0.65f,
            new float[]{0f, 1f},
            new Color[]{new Color(255, 255, 255, 55), new Color(255, 255, 255, 0)});
        g.setPaint(glow);
        g.fillRect(0, 0, size, size);

        // Grid
        float gridStroke = Math.max(2f, size * 0.022f);
        g.setStroke(new BasicStroke(gridStroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(255, 255, 255, 110));

        int pad = (int)(size * 0.18);
        int avail = size - 2 * pad;
        int cell = avail / 3;
        int gx0 = pad, gy0 = pad;
        // 2 vertical lines
        g.drawLine(gx0 + cell,     gy0,            gx0 + cell,     gy0 + 3 * cell);
        g.drawLine(gx0 + 2 * cell, gy0,            gx0 + 2 * cell, gy0 + 3 * cell);
        // 2 horizontal lines
        g.drawLine(gx0,            gy0 + cell,     gx0 + 3 * cell, gy0 + cell);
        g.drawLine(gx0,            gy0 + 2 * cell, gx0 + 3 * cell, gy0 + 2 * cell);

        // Symbols: winning diagonal X (top-left) → O (middle) → X (bottom-right)
        float symStroke = size * 0.065f;
        g.setStroke(new BasicStroke(symStroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int symInset = (int)(cell * 0.22);

        Color X = new Color(0, 220, 255);
        Color O = new Color(255, 60, 180);

        // Drop-shadow tint behind symbols
        Color shadow = new Color(0, 0, 0, 70);

        drawXShadow(g, gx0,            gy0,            cell, symInset, shadow, X);
        drawOShadow(g, gx0 + cell,     gy0 + cell,     cell, symInset, shadow, O);
        drawXShadow(g, gx0 + 2 * cell, gy0 + 2 * cell, cell, symInset, shadow, X);

        // Subtle outer rim for depth
        g.setStroke(new BasicStroke(Math.max(1f, gridStroke * 0.7f)));
        g.setColor(new Color(255, 255, 255, 35));
        g.draw(rect);

        g.dispose();
        return img;
    }

    private static void drawXShadow(Graphics2D g, int x, int y, int cell, int inset,
                                    Color shadow, Color color) {
        int sx0 = x + inset,        sy0 = y + inset;
        int sx1 = x + cell - inset, sy1 = y + cell - inset;
        int dx = Math.max(2, cell / 30);
        // Shadow
        g.setColor(shadow);
        g.drawLine(sx0 + dx, sy0 + dx, sx1 + dx, sy1 + dx);
        g.drawLine(sx1 + dx, sy0 + dx, sx0 + dx, sy1 + dx);
        // Symbol
        g.setColor(color);
        g.drawLine(sx0, sy0, sx1, sy1);
        g.drawLine(sx1, sy0, sx0, sy1);
    }

    private static void drawOShadow(Graphics2D g, int x, int y, int cell, int inset,
                                    Color shadow, Color color) {
        int dim = cell - 2 * inset;
        int dx = Math.max(2, cell / 30);
        g.setColor(shadow);
        g.drawOval(x + inset + dx, y + inset + dx, dim, dim);
        g.setColor(color);
        g.drawOval(x + inset, y + inset, dim, dim);
    }

    public static void save(BufferedImage img, File f) throws Exception {
        ImageIO.write(img, "PNG", f);
    }
}
