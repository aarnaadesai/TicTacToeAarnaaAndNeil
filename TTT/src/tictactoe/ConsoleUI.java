package tictactoe;

import java.util.Scanner;

public class ConsoleUI {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=======================");
        System.out.println("    TIC  TAC  TOE");
        System.out.println("=======================");
        System.out.println();

        // Get player names
        System.out.print("Enter name for X: ");
        String xName = scanner.nextLine().trim();
        if (xName.isEmpty()) xName = "Player X";

        System.out.print("Enter name for O: ");
        String oName = scanner.nextLine().trim();
        if (oName.isEmpty()) oName = "Player O";

        System.out.println();
        System.out.println(xName + " is X   |   " + oName + " is O");
        System.out.println("Rows and columns are numbered 0, 1, 2.");
        System.out.println();

        Board board       = new Board("board.csv");
        GameLogic logic   = new GameLogic();
        boolean gameOver  = false;

        while (!gameOver) {
            printBoard(board);

            char current = logic.getCurrentPlayer(board);
            String currentName = (current == 'X') ? xName : oName;
            System.out.println(currentName + "'s turn (" + current + ")");

            int row = promptInt(scanner, "  Enter row (0-2): ", 0, 2);
            int col = promptInt(scanner, "  Enter col (0-2): ", 0, 2);

            if (board.getCell(row, col) != 'E') {
                System.out.println("  That cell is already taken! Try again.");
                System.out.println();
                continue;
            }

            logic.makeMove(board, row, col);

            if (logic.checkWin(board, current)) {
                printBoard(board);
                System.out.println("=============================");
                System.out.println("  " + currentName + " (" + current + ") wins!");
                System.out.println("=============================");
                gameOver = true;
            } else if (logic.isDraw(board)) {
                printBoard(board);
                System.out.println("=============================");
                System.out.println("  It's a draw!");
                System.out.println("=============================");
                gameOver = true;
            } else {
                System.out.println();
            }
        }

        // Play again?
        System.out.println();
        System.out.print("Play again? (y/n): ");
        String again = scanner.nextLine().trim().toLowerCase();
        if (again.equals("y") || again.equals("yes")) {
            main(args); // restart
        } else {
            System.out.println("Thanks for playing!");
        }

        scanner.close();
    }

    private static void printBoard(Board board) {
        System.out.println();
        System.out.println("     0   1   2");
        System.out.println("   +---+---+---+");
        for (int r = 0; r < 3; r++) {
            System.out.print(" " + r + " |");
            for (int c = 0; c < 3; c++) {
                char cell = board.getCell(r, c);
                String sym = (cell == 'E') ? "   " : " " + cell + " ";
                System.out.print(sym + "|");
            }
            System.out.println();
            System.out.println("   +---+---+---+");
        }
        System.out.println();
    }

    private static int promptInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int val = Integer.parseInt(scanner.nextLine().trim());
                if (val >= min && val <= max) return val;
                System.out.println("  Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  Invalid input. Please enter a number.");
            }
        }
    }
}