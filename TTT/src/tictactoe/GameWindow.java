package tictactoe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class GameWindow extends JFrame {
    private BoardPanel boardPanel;
    private GameController controller;

    // Layout pieces
    private JPanel titleBar, centerRow, bottomPanel, boardHolder;
    private JPanel xCard, oCard;
    private JPanel statsStrip;
    private JLabel statsLabel;
    private JLabel titleLabel;
    private JLabel statusLabel;
    private JLabel xScoreLabel, oScoreLabel;
    private JLabel xNameLabel,  oNameLabel;
    private JLabel xSymLabel,   oSymLabel;
    private JTextArea logArea;
    private JScrollPane logScroll;
    private JButton resetButton, undoButton, hintButton, muteButton, themeButton, backButton;
    private JLabel xAvatarLabel, oAvatarLabel;
    private Runnable backAction;
    private ConfettiPanel confettiPanel;
    private AchievementBus achievementBus;

    // AI thinking indicator
    private boolean aiThinking = false;
    private String aiThinkingDots = "";
    private Timer aiThinkingTimer;

    // Game state mirrored for display
    private final String playerXName;
    private final String playerOName;
    private final String playerXAvatar;
    private final String playerOAvatar;
    private int xScore = 0, oScore = 0;
    private final boolean vsAI;
    private final int aiDifficulty;
    private final char humanChar;
    private final int firstPlayerMode;
    private char lastStatusPlayer = 'X';

    public GameWindow(StartScreen start) {
        playerXName     = start.getPlayerXName();
        playerOName     = start.getPlayerOName();
        playerXAvatar   = start.getXAvatar();
        playerOAvatar   = start.getOAvatar();
        vsAI            = start.isVsAI();
        aiDifficulty    = start.getAIDifficulty();
        humanChar       = start.getHumanChar();
        firstPlayerMode = start.getFirstPlayerMode();

        setTitle(TicTacToeApp.APP_NAME);
        if (TicTacToeApp.APP_ICON != null) setIconImage(TicTacToeApp.APP_ICON);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(560, 640));

        buildUI();
        installKeybindings();
        setSize(760, 800);
        setLocationRelativeTo(null);
        setVisible(true);

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                Theme.updateFonts(getHeight());
                refreshFonts();
                confettiPanel.setBounds(0, 0, getWidth(), getHeight());
                achievementBus.setBounds(0, 0, getWidth(), getHeight());
                revalidate();
                repaint();
            }
        });
    }

    public void attachController(GameController c) {
        this.controller = c;
        refreshActionButtons();
        refreshStatsStrip();
    }

    public AchievementBus achievements() { return achievementBus; }

    private void buildUI() {
        JLayeredPane layered = new JLayeredPane();
        layered.setLayout(new OverlayLayout(layered));
        setContentPane(layered);

        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setBackground(Theme.bg());
        main.setOpaque(true);
        layered.add(main, JLayeredPane.DEFAULT_LAYER);

        confettiPanel = new ConfettiPanel();
        confettiPanel.setOpaque(false);
        layered.add(confettiPanel, JLayeredPane.PALETTE_LAYER);

        achievementBus = new AchievementBus();
        achievementBus.setOpaque(false);
        layered.add(achievementBus, JLayeredPane.POPUP_LAYER);

        // ── Title bar ──────────────────────────────────────────────
        // BorderLayout with both wings forced to equal width = title sits at true window center.
        titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(Theme.panelBg());
        titleBar.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        // Left: back button
        final JPanel leftTitleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leftTitleRow.setOpaque(false);
        backButton = new JButton("← Back");
        backButton.setFont(Theme.LABEL_FONT);
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(Theme.ACCENT);
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        backButton.setToolTipText("Back to menu (Esc)");
        backButton.addActionListener(e -> { if (backAction != null) backAction.run(); });
        leftTitleRow.add(backButton);

        // Center: title
        titleLabel = new JLabel("TIC  TAC  TOE", SwingConstants.CENTER);
        titleLabel.setFont(Theme.TITLE_FONT);
        titleLabel.setForeground(Theme.textBright());

        // Right: mute + theme
        final JPanel rightTitleRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightTitleRow.setOpaque(false);
        muteButton = makeIconButton(SoundEngine.isMuted() ? "🔇" : "🔊");
        muteButton.setToolTipText("Mute (M)");
        muteButton.addActionListener(e -> toggleMute());
        rightTitleRow.add(muteButton);

        themeButton = new JButton(Theme.isDark ? "Light" : "Dark");
        themeButton.setFont(Theme.LABEL_FONT);
        themeButton.setForeground(Color.WHITE);
        themeButton.setBackground(Theme.ACCENT);
        themeButton.setBorderPainted(false);
        themeButton.setFocusPainted(false);
        themeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        themeButton.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        themeButton.setToolTipText("Toggle theme (T)");
        themeButton.addActionListener(e -> toggleTheme());
        rightTitleRow.add(themeButton);

        titleBar.add(leftTitleRow,  BorderLayout.WEST);
        titleBar.add(titleLabel,    BorderLayout.CENTER);
        titleBar.add(rightTitleRow, BorderLayout.EAST);

        // Equalize wing widths so the title is geometrically centered.
        SwingUtilities.invokeLater(() -> {
            int wingW = Math.max(leftTitleRow.getPreferredSize().width,
                                 rightTitleRow.getPreferredSize().width);
            Dimension lWing = new Dimension(wingW, leftTitleRow.getPreferredSize().height);
            Dimension rWing = new Dimension(wingW, rightTitleRow.getPreferredSize().height);
            leftTitleRow.setPreferredSize(lWing);
            rightTitleRow.setPreferredSize(rWing);
            titleBar.revalidate();
        });

        main.add(titleBar, BorderLayout.NORTH);

        // ── Center ─────────────────────────────────────────────────
        centerRow = new JPanel(new BorderLayout(8, 0));
        centerRow.setBackground(Theme.bg());
        centerRow.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        xCard = buildPlayerCard('X');
        oCard = buildPlayerCard('O');

        JPanel xWrap = new JPanel(new BorderLayout());
        xWrap.setBackground(Theme.bg());
        xWrap.add(xCard, BorderLayout.NORTH);

        JPanel oWrap = new JPanel(new BorderLayout());
        oWrap.setBackground(Theme.bg());
        oWrap.add(oCard, BorderLayout.NORTH);

        centerRow.add(xWrap, BorderLayout.WEST);
        centerRow.add(oWrap, BorderLayout.EAST);

        boardHolder = new JPanel(new GridBagLayout());
        boardHolder.setBackground(Theme.bg());
        centerRow.add(boardHolder, BorderLayout.CENTER);

        main.add(centerRow, BorderLayout.CENTER);

        // ── Bottom ─────────────────────────────────────────────────
        bottomPanel = new JPanel(new BorderLayout(0, 6));
        bottomPanel.setBackground(Theme.panelBg());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 14, 14, 14));

        statusLabel = new JLabel(playerXName + "'s turn  (X)", SwingConstants.CENTER);
        statusLabel.setFont(Theme.STATUS_FONT);
        statusLabel.setForeground(Theme.X);
        bottomPanel.add(statusLabel, BorderLayout.NORTH);

        // Center: log area + stats strip
        JPanel midCol = new JPanel(new BorderLayout(0, 6));
        midCol.setOpaque(false);

        logArea = new JTextArea(5, 44);
        logArea.setEditable(false);
        logArea.setBackground(Theme.logBg());
        logArea.setForeground(Theme.textDim());
        logArea.setFont(Theme.LOG_FONT);
        logArea.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createLineBorder(Theme.cellBorder(), 1));
        logScroll.getViewport().setBackground(Theme.logBg());
        midCol.add(logScroll, BorderLayout.CENTER);

        statsStrip = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 4));
        statsStrip.setBackground(Theme.panelBg());
        statsLabel = new JLabel("");
        statsLabel.setFont(Theme.labelFont(12f));
        statsLabel.setForeground(Theme.textDim());
        statsStrip.add(statsLabel);
        midCol.add(statsStrip, BorderLayout.SOUTH);

        bottomPanel.add(midCol, BorderLayout.CENTER);

        // Action button row
        undoButton  = makePillButton("↻ Undo",  Theme.cellBorder());
        hintButton  = makePillButton("💡 Hint", Theme.ACCENT);
        resetButton = makePillButton("New Game", Theme.ACCENT);
        undoButton.setToolTipText("Undo last move (U)");
        hintButton.setToolTipText("Show best move (H)");
        resetButton.setToolTipText("New game (R)");
        undoButton.addActionListener(e -> doUndo());
        hintButton.addActionListener(e -> doHint());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        btnRow.setBackground(Theme.panelBg());
        btnRow.add(undoButton);
        btnRow.add(hintButton);
        btnRow.add(resetButton);
        bottomPanel.add(btnRow, BorderLayout.SOUTH);

        main.add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel buildPlayerCard(char player) {
        Color borderCol = player == 'X' ? Theme.X : Theme.O; // border stays vivid for identity
        Color textCol   = Theme.statusColor(player);
        String name     = player == 'X' ? playerXName : playerOName;
        String avatar   = player == 'X' ? playerXAvatar : playerOAvatar;

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.panelBg());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderCol, 2),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        card.setPreferredSize(new Dimension(120, 180));
        card.setMaximumSize(new Dimension(120, 180));

        JLabel avatarLabel = new JLabel(avatar, SwingConstants.CENTER);
        avatarLabel.setFont(Theme.avatarFont(28f));
        avatarLabel.setForeground(textCol);
        avatarLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sym = new JLabel(String.valueOf(player), SwingConstants.CENTER);
        sym.setFont(Theme.scoreFont(34f));
        sym.setForeground(textCol);
        sym.setAlignmentX(CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(Theme.LABEL_FONT);
        nameLabel.setForeground(Theme.textDim());
        nameLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel scoreLabel = new JLabel("0", SwingConstants.CENTER);
        scoreLabel.setFont(Theme.SCORE_FONT);
        scoreLabel.setForeground(Theme.textBright());
        scoreLabel.setAlignmentX(CENTER_ALIGNMENT);

        if (player == 'X') {
            xScoreLabel = scoreLabel; xNameLabel = nameLabel; xSymLabel = sym; xAvatarLabel = avatarLabel;
        } else {
            oScoreLabel = scoreLabel; oNameLabel = nameLabel; oSymLabel = sym; oAvatarLabel = avatarLabel;
        }

        card.add(avatarLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(sym);
        card.add(Box.createVerticalStrut(4));
        card.add(nameLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(scoreLabel);
        return card;
    }

    private JButton makePillButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.STATUS_FONT);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (btn.isEnabled()) btn.setBackground(bg.brighter()); }
            public void mouseExited(MouseEvent e)  { if (btn.isEnabled()) btn.setBackground(bg); }
        });
        return btn;
    }

    private JButton makeIconButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.labelFont(14f));
        btn.setForeground(Theme.textBright());
        btn.setBackground(Theme.panelBg());
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        return btn;
    }

    // ── Keyboard ───────────────────────────────────────────────────
    private void installKeybindings() {
        JRootPane root = getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        int[] codes = {
            KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3,
            KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6,
            KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9
        };
        // Numpad uses the same VK_1..9 in newer JVMs as well, but bind both for safety
        int[] padCodes = {
            KeyEvent.VK_NUMPAD1, KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD3,
            KeyEvent.VK_NUMPAD4, KeyEvent.VK_NUMPAD5, KeyEvent.VK_NUMPAD6,
            KeyEvent.VK_NUMPAD7, KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD9
        };
        // Numpad layout: 7 8 9 / 4 5 6 / 1 2 3 (matches a calculator and visually maps to the board)
        int[][] numpadCells = {
            {2,0},{2,1},{2,2}, {1,0},{1,1},{1,2}, {0,0},{0,1},{0,2}
        };
        for (int i = 0; i < 9; i++) {
            final int r = numpadCells[i][0], c = numpadCells[i][1];
            String name = "cell" + i;
            im.put(KeyStroke.getKeyStroke(codes[i],    0), name);
            im.put(KeyStroke.getKeyStroke(padCodes[i], 0), name);
            am.put(name, new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    if (controller != null) controller.handleCellClick(r, c);
                }
            });
        }
        bindKey(im, am, KeyEvent.VK_U, "undo",  e -> doUndo());
        bindKey(im, am, KeyEvent.VK_H, "hint",  e -> doHint());
        bindKey(im, am, KeyEvent.VK_R, "reset", e -> resetButton.doClick());
        bindKey(im, am, KeyEvent.VK_T, "theme", e -> toggleTheme());
        bindKey(im, am, KeyEvent.VK_M, "mute",  e -> toggleMute());
        bindKey(im, am, KeyEvent.VK_SPACE, "newgame", e -> resetButton.doClick());
        bindKey(im, am, KeyEvent.VK_ESCAPE, "back", e -> { if (backAction != null) backAction.run(); });
    }

    public void setBackAction(Runnable r) { this.backAction = r; }

    private void bindKey(InputMap im, ActionMap am, int key, String name, java.util.function.Consumer<ActionEvent> fn) {
        im.put(KeyStroke.getKeyStroke(key, 0), name);
        am.put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { fn.accept(e); }
        });
    }

    // ── Theme / Mute toggles ───────────────────────────────────────
    private void toggleTheme() {
        Theme.isDark = !Theme.isDark;
        themeButton.setText(Theme.isDark ? "Light" : "Dark");
        getContentPane().setBackground(Theme.bg());
        titleBar.setBackground(Theme.panelBg());
        titleLabel.setForeground(Theme.textBright());
        centerRow.setBackground(Theme.bg());
        boardHolder.setBackground(Theme.bg());
        bottomPanel.setBackground(Theme.panelBg());
        statsStrip.setBackground(Theme.panelBg());
        statsLabel.setForeground(Theme.textDim());
        muteButton.setBackground(Theme.panelBg());
        muteButton.setForeground(Theme.textBright());
        logArea.setBackground(Theme.logBg());
        logArea.setForeground(Theme.textDim());
        if (logScroll != null) {
            logScroll.setBorder(BorderFactory.createLineBorder(Theme.cellBorder(), 1));
            logScroll.getViewport().setBackground(Theme.logBg());
        }
        if (undoButton != null) {
            undoButton.setBackground(Theme.cellBorder());
        }
        for (JPanel card : new JPanel[]{xCard, oCard}) card.setBackground(Theme.panelBg());
        for (Component p : centerRow.getComponents())
            if (p instanceof JPanel) p.setBackground(Theme.bg());
        if (xScoreLabel != null) xScoreLabel.setForeground(Theme.textBright());
        if (oScoreLabel != null) oScoreLabel.setForeground(Theme.textBright());
        if (xNameLabel  != null) xNameLabel.setForeground(Theme.textDim());
        if (oNameLabel  != null) oNameLabel.setForeground(Theme.textDim());
        if (xAvatarLabel != null) xAvatarLabel.setForeground(Theme.statusColor('X'));
        if (oAvatarLabel != null) oAvatarLabel.setForeground(Theme.statusColor('O'));
        if (xSymLabel    != null) xSymLabel.setForeground(Theme.statusColor('X'));
        if (oSymLabel    != null) oSymLabel.setForeground(Theme.statusColor('O'));
        if (statusLabel != null) statusLabel.setForeground(Theme.statusColor(lastStatusPlayer));
        if (boardPanel != null) boardPanel.refreshTheme();
        refreshFonts();
        repaint();
    }

    private void toggleMute() {
        SoundEngine.toggleMuted();
        muteButton.setText(SoundEngine.isMuted() ? "🔇" : "🔊");
    }

    private void doUndo() {
        if (controller == null) return;
        if (!controller.canUndo()) return;
        controller.undo();
    }

    private void doHint() {
        if (controller == null) return;
        if (!controller.canHint()) return;
        int[] move = controller.computeHint();
        if (move != null && boardPanel != null) {
            SoundEngine.playHint();
            boardPanel.pulseHint(move[0], move[1]);
            addLog("💡 Hint: try " + cellName(move[0], move[1]));
        }
    }

    private String cellName(int r, int c) {
        // numpad style: top-left=7, top=8, top-right=9, ... bottom-right=3
        int[][] m = {{7,8,9},{4,5,6},{1,2,3}};
        return "cell " + m[r][c];
    }

    // ── Public API used by controller ──────────────────────────────
    public void refreshActionButtons() {
        if (controller == null) return;
        boolean canUndo = controller.canUndo();
        boolean canHint = controller.canHint();
        undoButton.setEnabled(canUndo);
        hintButton.setEnabled(canHint);
        undoButton.setBackground(canUndo ? Theme.cellBorder() : dim(Theme.cellBorder()));
        hintButton.setBackground(canHint ? Theme.ACCENT      : dim(Theme.ACCENT));
    }

    private Color dim(Color c) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), 80);
    }

    public void setAIThinking(boolean on) {
        this.aiThinking = on;
        if (on) {
            aiThinkingDots = "";
            if (aiThinkingTimer != null) aiThinkingTimer.stop();
            aiThinkingTimer = new Timer(280, e -> {
                aiThinkingDots = aiThinkingDots.length() >= 3 ? "" : aiThinkingDots + ".";
                statusLabel.setText("AI thinking" + aiThinkingDots);
            });
            aiThinkingTimer.setInitialDelay(0);
            aiThinkingTimer.start();
            statusLabel.setForeground(Theme.ACCENT);
            statusLabel.setText("AI thinking");
        } else {
            if (aiThinkingTimer != null) aiThinkingTimer.stop();
        }
    }

    public void refreshStatsStrip() {
        if (statsLabel == null) return;
        Stats.Bucket b = vsAI ? Stats.ai(aiDifficulty) : Stats.pvp();
        if (vsAI) {
            String diff = new String[]{"Easy","Medium","Hard"}[aiDifficulty];
            statsLabel.setText(String.format(
                "vs AI %s   ·   W %d  L %d  D %d   ·   Streak %d (best %d)",
                diff, b.wins, b.losses, b.draws, b.currentStreak, b.bestStreak));
        } else {
            statsLabel.setText(String.format(
                "PvP   ·   X %d   O %d   Draws %d",
                b.wins, b.losses, b.draws));
        }
    }

    private void refreshFonts() {
        if (statusLabel != null) statusLabel.setFont(Theme.STATUS_FONT);
        if (xScoreLabel != null) xScoreLabel.setFont(Theme.SCORE_FONT);
        if (oScoreLabel != null) oScoreLabel.setFont(Theme.SCORE_FONT);
        if (xNameLabel  != null) xNameLabel.setFont(Theme.LABEL_FONT);
        if (oNameLabel  != null) oNameLabel.setFont(Theme.LABEL_FONT);
        if (xSymLabel   != null) xSymLabel.setFont(Theme.scoreFont(40f));
        if (oSymLabel   != null) oSymLabel.setFont(Theme.scoreFont(40f));
        if (resetButton != null) resetButton.setFont(Theme.STATUS_FONT);
        if (undoButton  != null) undoButton.setFont(Theme.STATUS_FONT);
        if (hintButton  != null) hintButton.setFont(Theme.STATUS_FONT);
        if (statsLabel  != null) statsLabel.setFont(Theme.labelFont(12f));
        if (titleLabel  != null) titleLabel.setFont(Theme.TITLE_FONT);
        revalidate();
    }

    public void setBoardPanel(BoardPanel p) {
        this.boardPanel = p;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        boardHolder.add(p, gbc);
        revalidate(); repaint();
    }

    public BoardPanel getBoardPanel() { return boardPanel; }

    public void setStatus(String text, char player) {
        if (aiThinking) return; // don't clobber the thinking indicator
        lastStatusPlayer = player;
        statusLabel.setText(text);
        statusLabel.setForeground(Theme.statusColor(player));
    }

    public void addLog(String msg) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.append("[" + time + "] " + msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public void recordWin(char player) {
        String name = player == 'X' ? playerXName : playerOName;
        if (player == 'X') { xScore++; xScoreLabel.setText(String.valueOf(xScore)); }
        else               { oScore++; oScoreLabel.setText(String.valueOf(oScore)); }
        addLog("  " + name + " wins!");
        setStatus(name + " wins!", player);
    }

    public void recordDraw() {
        addLog("  Draw!");
        setStatus("It's a draw!", 'E');
    }

    public void updateTurn(char player) {
        String name = player == 'X' ? playerXName : playerOName;
        setStatus(name + "'s turn  (" + player + ")", player);
    }

    public void showConfetti() { confettiPanel.bigBurst(); }

    public void setResetAction(Runnable r) {
        for (ActionListener al : resetButton.getActionListeners())
            resetButton.removeActionListener(al);
        resetButton.addActionListener(e -> r.run());
    }

    public boolean isVsAI()        { return vsAI; }
    public int getAIDifficulty()   { return aiDifficulty; }
    public char getHumanChar()     { return humanChar; }
    public int getFirstPlayerMode(){ return firstPlayerMode; }
    public String getPlayerXName() { return playerXName; }
    public String getPlayerOName() { return playerOName; }
}
