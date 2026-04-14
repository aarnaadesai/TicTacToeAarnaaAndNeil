package tictactoe;

class GameLogic {
	
	public boolean checkWin(Board board, char player) {
		
		// Check each row
    	for (char[] row : board.getGrid()) {
    		if (row[0] == player && row[0] == row[1] && row[0] == row[2])
    			return true;
    	}
    	
    	// Check each column
    	for (int col = 0; col < board.getGrid()[0].length; col++) {
	    		if (board.getCell(0, col) == player && board.getCell(0, col) == board.getCell(1, col) && board.getCell(0, col) == board.getCell(2, col))
	    			return true;
    	}
    	
    	// Diagonal #1
    	if (board.getCell(0, 0) == player && board.getCell(0, 0) == board.getCell(1, 1) && board.getCell(0, 0) == board.getCell(2, 2)) {
    		return true;
    	}
    	
    	// Diagonal #2
    	if (board.getCell(0, 2) == player && board.getCell(0, 2) == board.getCell(1, 1) && board.getCell(0, 2) == board.getCell(2, 0)) {
    		return true;
    	}
    	
    	// If the player hasn't won yet, then 
    	return false;

    }
	
	public boolean isDraw(Board board) {
	    if (checkWin(board, 'X') || checkWin(board, 'O')) {
	        return false;
	    }

	    for (char[] row : board.getGrid()) {
	        for (char cell : row) {
	            if (cell == 'E') {
	                return false;
	            }
	        }
	    }
	    return true;
	   
	}
	
	public boolean isGameOver(Board board) {
	    return checkWin(board, 'X') ||
	           checkWin(board, 'O') ||
	           isDraw(board);
	}
}