package tictactoe;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class Board 
{
	//holds game play data in an instance variable
    private char[][] grid;
    
    //holds game play data in a CSV file
    private String filename;
        
    
    //non-default constructor - [5 points]
    public Board(String filename)
    {
    	this.filename = filename;
    	if (isValidBoardFile()) 
    	{
    		clearBoard();
    		loadBoardFromFile();
    	}
    	else
    	{
    		grid = new char[3][3];
    		clearBoard();
    	}
    }

	public char getCell(int row, int col) {
		return grid[row][col];
	}
	
	public void setCell(int row, int col, char player)
	{
		grid[row][col] = player;
		saveBoardToFile();
	}
	
	public char[][] getGrid() {
		return this.grid;
	}
	
	public void setGrid(char[][] newGrid)
	{
		this.grid = newGrid;
		saveBoardToFile();
	}
		
    
    //loads the grid with the file contents - [5 points]
    public void loadBoardFromFile()
    {
		try {
			File file = new File("src/tictactoe/"+this.filename);
    		Scanner scanner = new Scanner(file);

			int row = 0;
			while (scanner.hasNextLine()) {
				if (row < 3) {
					String line = scanner.nextLine().trim();
					String[] parts = line.split(",");
					grid[row][0] = parts[0].charAt(0);
					grid[row][1] = parts[1].charAt(0);
					grid[row][2] = parts[2].charAt(0);
					row++;
				}
			}
			scanner.close();
		}
		catch (Exception error) {
			error.printStackTrace();
		}
    }

    
    //valid if it resembles a 3x3 board that contains only E, X, O
    public boolean isValidBoardFile()
    {
    	try
    	{
    		File file = new File("src/tictactoe/"+this.filename);
    		Scanner scanner = new Scanner(file);
    		int xCount = 0, oCount = 0;
    		while(scanner.hasNextLine())
    		{
    			String line = scanner.nextLine().trim();
    			if(!line.matches("[EXO],[EXO],[EXO]"))
    			{
    				scanner.close();
    				return false;
    			}

    			// count X and O
    			for (int i = 0; i < line.length(); i++) {
    				char c = line.charAt(i);
    				if (c == 'X') xCount++;
    				else if (c == 'O') oCount++;
    			}
    		}
    		scanner.close();
    		return xCount == oCount || xCount == oCount + 1;
    	}
    	catch(Exception error)
    	{
    		error.printStackTrace();
    		return false;
    	}
    }
    
    
    //saves the grid to the file in the proper format (CSV)
    public void saveBoardToFile()
    {
    	try
    	{
    		File file = new File("src/tictactoe/"+this.filename);
    		FileWriter writer = new FileWriter(file);
    		
    		String boardContents = "";
    		for(int row = 0; row < grid.length; row++)
    		{
    			for(int col = 0; col < grid[0].length; col++)
    			{
    				if(col < 2) boardContents += grid[row][col] + ",";
    				else boardContents += grid[row][col];
    			}
    			if(row < 2) boardContents += "\n";
    		}
    		
    		writer.write(boardContents);
    		writer.close();
    	}
    	catch(Exception error)
    	{
    		error.printStackTrace();
    	}
    }
    
    
    /***These are the methods used to test those above***/
    //prints the current grid
    public void printGrid()
    {
    	for (int row = 0; row < grid.length; row++) {
    		for (int col = 0; col < grid[0].length; col++) {
    			System.out.print(grid[row][col] + "");
    		}
    		System.out.println();
    	}
    }
    
    //create a random board
    public void createRandomBoard()
    {
    	char options[] = {'E', 'X', 'O'};
    	for (int row = 0; row < grid.length; row++) {
    		for (int col = 0; col < grid[0].length; col++) {
    			int index = (int)(Math.random() * options.length); // 0, 1, or 2
    			grid[row][col] = options[index];
    		}
    	}
    	saveBoardToFile();
    }
    
    //clears the grid by placing E in every cell
    public void clearBoard()
    {
    	char clearedBoard[][] = {
    						{'E', 'E', 'E'},
    						{'E', 'E', 'E'},
    						{'E', 'E', 'E'}};
    	this.grid = clearedBoard;
    	saveBoardToFile();
    }

    
    public static void main(String args[])
    {
    	Board b = new Board("board.csv");
    	System.out.println(b.isValidBoardFile());
    	b.createRandomBoard();
    	b.printGrid();
    	b.saveBoardToFile();
    	b.loadBoardFromFile();
    	System.out.println();
    	b.printGrid();
    }
}