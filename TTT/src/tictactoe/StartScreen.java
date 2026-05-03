package tictactoe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class StartScreen extends JDialog {
    public static final String[] AVATARS = {
        "★", "♥", "♦", "♣", "♠", "◆",
        "▲", "●", "☀", "❄", "⚡", "♛"
    };

    private String playerXName = "Player X";
    private String playerOName = "Player O";
    private String xAvatar = AVATARS[0];
    private String oAvatar = AVATARS[1];
    private boolean vsAI = false;
    private int aiDifficulty = 1;
    private char humanChar = 'X';
    private int firstPlayerMode = 0; // 0=You first(X), 1=AI first(O), 2=Random — vsAI only
    private boolean confirmed = false;

    // UI state
    private int selectedMode = 0;
    private int selectedFirst = 0;
    private int xAvatarIdx = 0;
    private int oAvatarIdx = 1;
    private JTextField xNameField, oNameField;
    private JLabel oNameLabel;
    private JPanel modePanel;
    private JButton[] modeButtons;
    private JButton[] firstButtons;
    private JPanel firstPanel;
    private JLabel firstLabel;
    private JPanel namePanel;
    private JButton[] xAvatarButtons, oAvatarButtons;

    // Animated background particles
    private float[] px, py, pvx, pvy, palpha;
    private Color[] pcolors;
    private Timer bgTimer;
    private static final int PARTICLE_COUNT = 18;

    public StartScreen() {
        super((Frame) null, true);
        setTitle(TicTacToeApp.APP_NAME);
        if (TicTacToeApp.APP_ICON != null) setIconImage(TicTacToeApp.APP_ICON);
        setUndecorated(true);
        setSize(540, 760);
        setLocationRelativeTo(null);
        setBackground(new Color(0, 0, 0, 0));

        initParticles();
        buildUI();

        // Slow repaint — fast (33fps) on a transparent undecorated dialog causes
        // macOS to recomposite each frame, producing visible flicker.
        bgTimer = new Timer(90, e -> repaint());
        bgTimer.start();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) {
                if (bgTimer != null) bgTimer.stop();
            }
        });
    }

    private void initParticles() {
        px     = new float[PARTICLE_COUNT];
        py     = new float[PARTICLE_COUNT];
        pvx    = new float[PARTICLE_COUNT];
        pvy    = new float[PARTICLE_COUNT];
        palpha = new float[PARTICLE_COUNT];
        pcolors = new Color[PARTICLE_COUNT];
        Color[] palette = {Theme.X, Theme.O, Theme.ACCENT, Theme.WIN_FLASH};
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            px[i]     = (float)(Math.random() * 520);
            py[i]     = (float)(Math.random() * 640);
            pvx[i]    = (float)(Math.random() * 0.6 - 0.3);
            pvy[i]    = (float)(Math.random() * 0.6 - 0.3);
            palpha[i] = (float)(0.2 + Math.random() * 0.5);
            pcolors[i] = palette[i % palette.length];
        }
    }

    private void updateParticles() {
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            px[i] += pvx[i];
            py[i] += pvy[i];
            if (px[i] < 0)   px[i] = 520;
            if (px[i] > 520) px[i] = 0;
            if (py[i] < 0)   py[i] = 640;
            if (py[i] > 640) py[i] = 0;
        }
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background gradient
                GradientPaint grad = new GradientPaint(
                    0, 0,           new Color(8, 8, 20),
                    0, getHeight(), new Color(20, 10, 40)
                );
                g2.setPaint(grad);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);

                // Animated glowing orbs
                updateParticles();
                for (int i = 0; i < PARTICLE_COUNT; i++) {
                    int size = 60 + (i % 5) * 30;
                    Color c = pcolors[i];
                    g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(),
                        (int)(palpha[i] * 40)));
                    g2.fillOval((int)px[i] - size/2, (int)py[i] - size/2, size, size);
                }

                // Subtle grid lines
                g2.setColor(new Color(255, 255, 255, 8));
                g2.setStroke(new BasicStroke(1f));
                for (int x = 0; x < getWidth(); x += 40)
                    g2.drawLine(x, 0, x, getHeight());
                for (int y = 0; y < getHeight(); y += 40)
                    g2.drawLine(0, y, getWidth(), y);

                // Border
                g2.setColor(new Color(255, 255, 255, 20));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 22, 22);

                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(40, 44, 36, 44));
        setContentPane(root);

        // ── Logo section ───────────────────────────────────────────
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setOpaque(false);

        // X O symbols
        JPanel symbolRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        symbolRow.setOpaque(false);
        JLabel xSym = makeLabel("X", Theme.symbolFont(52f), Theme.X);
        JLabel dash  = makeLabel("vs", Theme.labelFont(18f), new Color(180, 180, 220));
        JLabel oSym = makeLabel("O", Theme.symbolFont(52f), Theme.O);
        symbolRow.add(xSym);
        symbolRow.add(dash);
        symbolRow.add(oSym);

        JLabel title = makeLabel("TIC TAC TOE", Theme.titleFont(32f), Color.WHITE);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = makeLabel("PRO EDITION", Theme.labelFont(13f), new Color(150, 120, 255));
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 25));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        logoPanel.add(symbolRow);
        logoPanel.add(Box.createVerticalStrut(8));
        logoPanel.add(title);
        logoPanel.add(Box.createVerticalStrut(4));
        logoPanel.add(subtitle);
        logoPanel.add(Box.createVerticalStrut(24));
        logoPanel.add(sep);

        root.add(logoPanel, BorderLayout.NORTH);

        // ── Center: mode + name fields ─────────────────────────────
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        // Mode label
        JLabel modeLabel = makeLabel("GAME MODE", Theme.labelFont(11f), new Color(140, 140, 180));
        modeLabel.setAlignmentX(CENTER_ALIGNMENT);
        center.add(Box.createVerticalStrut(20));
        center.add(modeLabel);
        center.add(Box.createVerticalStrut(10));

        // Mode buttons
        modePanel = new JPanel(new GridLayout(2, 2, 8, 8));
        modePanel.setOpaque(false);
        modePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        String[] modeNames = {"2 Players", "AI  Easy", "AI  Medium", "AI  Hard"};
        modeButtons = new JButton[4];
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            modeButtons[i] = makeModeButton(modeNames[i], i == 0);
            modeButtons[i].addActionListener(e -> selectMode(idx));
        }
        for (JButton b : modeButtons) modePanel.add(b);
        center.add(modePanel);

        // First-move picker (only meaningful in vsAI; hidden for PvP)
        center.add(Box.createVerticalStrut(18));
        firstLabel = makeLabel("WHO MOVES FIRST", Theme.labelFont(11f), new Color(140, 140, 180));
        firstLabel.setAlignmentX(CENTER_ALIGNMENT);
        center.add(firstLabel);
        center.add(Box.createVerticalStrut(8));
        firstPanel = new JPanel(new GridLayout(1, 3, 8, 0));
        firstPanel.setOpaque(false);
        firstPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        String[] firstNames = {"You (X)", "Bot (X)", "Random"};
        firstButtons = new JButton[3];
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            firstButtons[i] = makeModeButton(firstNames[i], i == 0);
            firstButtons[i].addActionListener(e -> selectFirst(idx));
            firstPanel.add(firstButtons[i]);
        }
        center.add(firstPanel);
        firstLabel.setVisible(false);
        firstPanel.setVisible(false);

        // Name fields
        center.add(Box.createVerticalStrut(24));
        JLabel namesLabel = makeLabel("PLAYER NAMES", Theme.labelFont(11f), new Color(140, 140, 180));
        namesLabel.setAlignmentX(CENTER_ALIGNMENT);
        center.add(namesLabel);
        center.add(Box.createVerticalStrut(10));

        namePanel = new JPanel(new GridLayout(2, 2, 10, 10));
        namePanel.setOpaque(false);
        namePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel xLbl = makeLabel("X  (Cyan)", Theme.labelFont(12f), Theme.X);
        oNameLabel   = makeLabel("O  (Pink)", Theme.labelFont(12f), Theme.O);

        xNameField = makeTextField("Player X");
        oNameField = makeTextField("Player O");

        namePanel.add(xLbl);
        namePanel.add(oNameLabel);
        namePanel.add(xNameField);
        namePanel.add(oNameField);
        center.add(namePanel);

        // Avatar pickers
        center.add(Box.createVerticalStrut(20));
        JLabel avatarsLabel = makeLabel("AVATARS", Theme.labelFont(11f), new Color(140, 140, 180));
        avatarsLabel.setAlignmentX(CENTER_ALIGNMENT);
        center.add(avatarsLabel);
        center.add(Box.createVerticalStrut(8));

        JPanel avatarPanel = new JPanel(new GridLayout(2, 1, 0, 6));
        avatarPanel.setOpaque(false);
        avatarPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        xAvatarButtons = buildAvatarRow(true);
        oAvatarButtons = buildAvatarRow(false);
        avatarPanel.add(wrapAvatarRow(xAvatarButtons));
        avatarPanel.add(wrapAvatarRow(oAvatarButtons));
        center.add(avatarPanel);

        root.add(center, BorderLayout.CENTER);

        // ── Start button ───────────────────────────────────────────
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);

        JButton startBtn = new JButton("START GAME") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0,          new Color(100, 60, 220),
                    getWidth(), 0, new Color(0, 180, 255)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        startBtn.setFont(Theme.statusFont(16f));
        startBtn.setForeground(Color.WHITE);
        startBtn.setContentAreaFilled(false);
        startBtn.setBorderPainted(false);
        startBtn.setFocusPainted(false);
        startBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        startBtn.setAlignmentX(CENTER_ALIGNMENT);
        startBtn.addActionListener(e -> confirm());
        startBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { startBtn.repaint(); }
            public void mouseExited(MouseEvent e)  { startBtn.repaint(); }
        });

        bottom.add(Box.createVerticalStrut(24));
        bottom.add(startBtn);

        root.add(bottom, BorderLayout.SOUTH);
    }

    private void selectMode(int idx) {
        selectedMode = idx;
        for (int i = 0; i < modeButtons.length; i++) {
            boolean sel = (i == idx);
            modeButtons[i].setBackground(sel ? Theme.ACCENT : new Color(30, 30, 55));
            modeButtons[i].setForeground(sel ? Color.WHITE : new Color(160, 160, 200));
        }
        boolean ai = (idx > 0);
        firstLabel.setVisible(ai);
        firstPanel.setVisible(ai);
        // Update O name field label / placeholder
        if (idx == 0) {
            oNameLabel.setText("O  (Pink)");
            oNameField.setText("Player O");
            oNameField.setEnabled(true);
            oNameField.setForeground(Color.WHITE);
        } else {
            String[] botNames = {"Bot (Easy)", "Bot (Medium)", "Bot (Hard)"};
            oNameLabel.setText("O  (AI)");
            oNameField.setText(botNames[idx - 1]);
            oNameField.setEnabled(false);
            oNameField.setForeground(new Color(120, 120, 160));
        }
    }

    private void selectFirst(int idx) {
        selectedFirst = idx;
        for (int i = 0; i < firstButtons.length; i++) {
            boolean sel = (i == idx);
            firstButtons[i].setBackground(sel ? Theme.ACCENT : new Color(30, 30, 55));
            firstButtons[i].setForeground(sel ? Color.WHITE : new Color(160, 160, 200));
        }
    }

    private JButton[] buildAvatarRow(boolean isX) {
        JButton[] btns = new JButton[AVATARS.length];
        Color color = isX ? Theme.X : Theme.O;
        int initialIdx = isX ? xAvatarIdx : oAvatarIdx;
        for (int i = 0; i < AVATARS.length; i++) {
            final int idx = i;
            JButton b = new JButton(AVATARS[i]);
            b.setFont(Theme.avatarFont(20f));
            b.setForeground(i == initialIdx ? Color.WHITE : color);
            b.setBackground(i == initialIdx ? color : new Color(30, 30, 55));
            b.setFocusPainted(false);
            b.setBorderPainted(false);
            b.setMargin(new Insets(0, 0, 0, 0));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> selectAvatar(isX, idx));
            btns[i] = b;
        }
        return btns;
    }

    private JPanel wrapAvatarRow(JButton[] btns) {
        JPanel row = new JPanel(new GridLayout(1, btns.length, 4, 0));
        row.setOpaque(false);
        for (JButton b : btns) row.add(b);
        return row;
    }

    private void selectAvatar(boolean isX, int idx) {
        JButton[] btns = isX ? xAvatarButtons : oAvatarButtons;
        Color color = isX ? Theme.X : Theme.O;
        if (isX) xAvatarIdx = idx; else oAvatarIdx = idx;
        for (int i = 0; i < btns.length; i++) {
            boolean sel = (i == idx);
            btns[i].setBackground(sel ? color : new Color(30, 30, 55));
            btns[i].setForeground(sel ? Color.WHITE : color);
        }
    }

    private void confirm() {
        playerXName = xNameField.getText().trim().isEmpty() ? "Player X" : xNameField.getText().trim();
        xAvatar = AVATARS[xAvatarIdx];
        oAvatar = AVATARS[oAvatarIdx];
        vsAI = selectedMode > 0;
        if (vsAI) {
            aiDifficulty = selectedMode - 1;
            firstPlayerMode = selectedFirst;
            if (selectedFirst == 0)      humanChar = 'X';
            else if (selectedFirst == 1) humanChar = 'O';
            else humanChar = (Math.random() < 0.5) ? 'X' : 'O';
            String[] botNames = {"Bot (Easy)", "Bot (Medium)", "Bot (Hard)"};
            playerOName = botNames[aiDifficulty];
            if (humanChar == 'O') {
                // Swap so name & avatar follow the human onto the O card
                playerOName = playerXName;
                playerXName = botNames[aiDifficulty];
                String tmp = oAvatar; oAvatar = xAvatar; xAvatar = tmp;
            }
        } else {
            playerOName = oNameField.getText().trim().isEmpty() ? "Player O" : oNameField.getText().trim();
            firstPlayerMode = 0;
        }
        confirmed = true;
        bgTimer.stop();
        dispose();
    }

    // ── Helpers ────────────────────────────────────────────────────
    private JLabel makeLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    private JButton makeModeButton(String text, boolean selected) {
        JButton b = new JButton(text);
        b.setFont(Theme.labelFont(13f));
        b.setForeground(selected ? Color.WHITE : new Color(160, 160, 200));
        b.setBackground(selected ? Theme.ACCENT : new Color(30, 30, 55));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return b;
    }

    private JTextField makeTextField(String placeholder) {
        JTextField f = new JTextField(placeholder);
        f.setFont(Theme.labelFont(14f));
        f.setForeground(Color.WHITE);
        f.setBackground(new Color(30, 30, 55));
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 130), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { f.selectAll(); }
        });
        return f;
    }

    // ── Getters ────────────────────────────────────────────────────
    public boolean isConfirmed()   { return confirmed; }
    public String getPlayerXName() { return playerXName; }
    public String getPlayerOName() { return playerOName; }
    public boolean isVsAI()        { return vsAI; }
    public int getAIDifficulty()   { return aiDifficulty; }
    public char getHumanChar()     { return humanChar; }
    public int getFirstPlayerMode(){ return firstPlayerMode; }
    public String getXAvatar()     { return xAvatar; }
    public String getOAvatar()     { return oAvatar; }
}