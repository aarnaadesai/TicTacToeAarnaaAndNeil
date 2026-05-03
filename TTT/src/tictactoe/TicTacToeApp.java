package tictactoe;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class TicTacToeApp {
    public static final String APP_NAME = "TicTacToe Pro";
    public static BufferedImage APP_ICON;

    public static void main(String[] args) {
        // Must set BEFORE any AWT class is touched, so macOS picks them up.
        System.setProperty("apple.awt.application.name", APP_NAME);
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_NAME);

        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}

        // Build the icon and save a copy next to the project so the user has the file.
        APP_ICON = AppIcon.create(512);
        try {
            File out = new File(System.getProperty("user.dir"), "app-icon.png");
            AppIcon.save(APP_ICON, out);
        } catch (Exception ignored) {}

        // macOS dock icon (com.apple.eawt.Application) — reflective so it doesn't
        // break the build on non-mac JDKs.
        try {
            Class<?> appClass = Class.forName("com.apple.eawt.Application");
            Object app = appClass.getMethod("getApplication").invoke(null);
            appClass.getMethod("setDockIconImage", java.awt.Image.class).invoke(app, APP_ICON);
        } catch (Throwable ignored) {}

        SwingUtilities.invokeLater(TicTacToeApp::launchSession);
    }

    /** One full session: start screen → game window. Back button restarts the loop. */
    private static void launchSession() {
        StartScreen start = new StartScreen();
        start.setVisible(true);
        if (!start.isConfirmed()) return;

        Board board = new Board("board.csv");
        board.clearBoard();

        GameLogic logic   = new GameLogic();
        GameWindow window = new GameWindow(start);

        GameController controller = new GameController(board, logic, window);
        controller.setFirstPlayerMode(window.getFirstPlayerMode());

        if (window.isVsAI()) {
            char aiChar = window.getHumanChar() == 'X' ? 'O' : 'X';
            AIPlayer ai = new AIPlayer(window.getAIDifficulty(), aiChar);
            controller.setAI(ai, window.getHumanChar());
            window.addLog("AI opponent: " +
                new String[]{"Easy","Medium","Hard"}[window.getAIDifficulty()]);
        }

        BoardPanel boardPanel = new BoardPanel(controller);
        window.setBoardPanel(boardPanel);
        boardPanel.updateBoard(board.getGrid());
        window.setResetAction(controller::reset);
        window.setBackAction(() -> {
            window.dispose();
            SwingUtilities.invokeLater(TicTacToeApp::launchSession);
        });
        window.attachController(controller);

        char first = logic.getCurrentPlayer(board);
        window.updateTurn(first);
        window.addLog("-- Game started: " + window.getPlayerXName() +
                      " vs " + window.getPlayerOName() + " --");

        controller.kickoffIfAIFirst();
    }
}
