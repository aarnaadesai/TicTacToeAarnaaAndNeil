package tictactoe;

import java.awt.*;

public class Theme {
    public static boolean isDark = true;

    // Dark mode
    private static final Color D_BG          = new Color(10, 10, 18);
    private static final Color D_PANEL_BG    = new Color(16, 16, 28);
    private static final Color D_CELL        = new Color(22, 22, 38);
    private static final Color D_CELL_HOVER  = new Color(32, 32, 58);
    private static final Color D_CELL_BORDER = new Color(50, 50, 90);
    private static final Color D_TEXT_DIM    = new Color(120, 120, 160);
    private static final Color D_TEXT_BRIGHT = new Color(220, 220, 255);
    private static final Color D_LOG_BG      = new Color(14, 14, 24);

    // Light mode
    private static final Color L_BG          = new Color(240, 240, 255);
    private static final Color L_PANEL_BG    = new Color(225, 225, 245);
    private static final Color L_CELL        = new Color(255, 255, 255);
    private static final Color L_CELL_HOVER  = new Color(210, 210, 240);
    private static final Color L_CELL_BORDER = new Color(180, 180, 220);
    private static final Color L_TEXT_DIM    = new Color(90, 90, 130);
    private static final Color L_TEXT_BRIGHT = new Color(20, 20, 60);
    private static final Color L_LOG_BG      = new Color(248, 248, 255);

    public static Color bg()         { return isDark ? D_BG          : L_BG;          }
    public static Color panelBg()    { return isDark ? D_PANEL_BG    : L_PANEL_BG;    }
    public static Color cell()       { return isDark ? D_CELL        : L_CELL;        }
    public static Color cellHover()  { return isDark ? D_CELL_HOVER  : L_CELL_HOVER;  }
    public static Color cellBorder() { return isDark ? D_CELL_BORDER : L_CELL_BORDER; }
    public static Color textDim()    { return isDark ? D_TEXT_DIM    : L_TEXT_DIM;    }
    public static Color textBright() { return isDark ? D_TEXT_BRIGHT : L_TEXT_BRIGHT; }
    public static Color logBg()      { return isDark ? D_LOG_BG      : L_LOG_BG;      }
    // Darker variants used for small text on light backgrounds (better contrast).
    private static final Color X_TEXT_LIGHT = new Color(0, 120, 180);
    private static final Color O_TEXT_LIGHT = new Color(200, 30, 130);
    public static Color statusColor(char player) {
        if (player == 'X') return isDark ? X : X_TEXT_LIGHT;
        if (player == 'O') return isDark ? O : O_TEXT_LIGHT;
        return isDark ? WIN_FLASH : new Color(200, 140, 0);
    }

    // Always vivid
    public static final Color X         = new Color(0, 220, 255);
    public static final Color X_GLOW    = new Color(0, 180, 255, 60);
    public static final Color O         = new Color(255, 60, 180);
    public static final Color O_GLOW    = new Color(255, 60, 180, 60);
    public static final Color ACCENT    = new Color(120, 80, 255);
    public static final Color WIN_FLASH = new Color(255, 220, 60);
    public static final Color HOVER     = D_CELL_HOVER;
    public static final Color BG        = D_BG;
    public static final Color CELL      = D_CELL;
    public static final Color CELL_BORDER = D_CELL_BORDER;
    public static final Color CELL_HOVER  = D_CELL_HOVER;

    // Base font sizes — scaled dynamically via scaleFont()
    public static final int BASE_SYMBOL_SIZE = 64;
    public static final int BASE_TITLE_SIZE  = 22;
    public static final int BASE_STATUS_SIZE = 15;
    public static final int BASE_SCORE_SIZE  = 28;
    public static final int BASE_LABEL_SIZE  = 13;

    // Loaded font faces (unsized — derive size dynamically)
    private static Font outfitExtraBold;
    private static Font outfitSemiBold;
    private static Font outfitRegular;

    static {
    	outfitExtraBold = loadFace("Outfit-ExtraBold.ttf");
    	outfitSemiBold  = loadFace("Outfit-SemiBold.ttf");
    	outfitRegular   = loadFace("Outfit-Regular.ttf");
    }

    private static Font loadFace(String path) {
        try {
            java.io.InputStream is = Theme.class.getResourceAsStream(path);
            if (is == null) throw new Exception("Not found: " + path);
            Font f = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
            return f;
        } catch (Exception e) {
            System.out.println("⚠ Font fallback for " + path + ": " + e.getMessage());
            return new Font("SansSerif", Font.BOLD, 12);
        }
    }

    // Call these to get correctly sized fonts — use in paintComponent or after resize
    public static Font symbolFont(float size)  { return outfitExtraBold.deriveFont(Font.BOLD,  size); }
    public static Font titleFont(float size)   { return outfitSemiBold.deriveFont(Font.BOLD,   size); }
    public static Font statusFont(float size)  { return outfitSemiBold.deriveFont(Font.BOLD,   size); }
    public static Font scoreFont(float size)   { return outfitExtraBold.deriveFont(Font.BOLD,  size); }
    public static Font labelFont(float size)   { return outfitRegular.deriveFont(Font.PLAIN,   size); }
    public static Font logFont()               { return outfitRegular.deriveFont(Font.PLAIN,  13f);   }
    /** Font for avatar symbols — uses logical SansSerif so JRE falls back to a font with dingbat glyphs. */
    public static Font avatarFont(float size)  { return new Font(Font.SANS_SERIF, Font.PLAIN, (int) size); }

    // Convenience: scale a base size by a ratio (window height / reference height)
    public static float scale(int baseSize, int windowHeight) {
        return Math.max(8f, baseSize * (windowHeight / 640f));
    }

    // Static font constants for places that don't have window size context
    public static Font FONT        = symbolFont(BASE_SYMBOL_SIZE);
    public static Font TITLE_FONT  = titleFont(BASE_TITLE_SIZE);
    public static Font STATUS_FONT = statusFont(BASE_STATUS_SIZE);
    public static Font SCORE_FONT  = scoreFont(BASE_SCORE_SIZE);
    public static Font LABEL_FONT  = labelFont(BASE_LABEL_SIZE);
    public static Font LOG_FONT    = logFont();

    // Call this when window is resized to refresh static fonts
    public static void updateFonts(int windowHeight) {
        FONT        = symbolFont(scale(BASE_SYMBOL_SIZE, windowHeight));
        TITLE_FONT  = titleFont(scale(BASE_TITLE_SIZE,   windowHeight));
        STATUS_FONT = statusFont(scale(BASE_STATUS_SIZE, windowHeight));
        SCORE_FONT  = scoreFont(scale(BASE_SCORE_SIZE,   windowHeight));
        LABEL_FONT  = labelFont(scale(BASE_LABEL_SIZE,   windowHeight));
    }
}