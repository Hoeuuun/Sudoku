import java.util.Random;
/**
 * Project Sudoku
 *
 * Core internal logic
 * main() is here at the bottom
 *
 * @author Jian Liang Zhao
 * @author Hoeun Sim
 */
public class SudokuPuzzle {
	/*
	boardPresets--uses two preset test boards: 1) nearly solved, 2) canvas example
	to ensure board works
	*/
	private int[][] boardPresets = { {0,7,2, 9,8,3, 5,6,4,
							 		  4,6,8, 5,7,2, 9,1,3,
							 		  5,3,9, 6,1,4, 8,7,2,

							 		  2,1,3, 8,5,6, 4,9,7,
							 		  8,4,6, 3,9,7, 2,5,1,
							 		  9,5,7, 2,4,1, 3,8,6,

							 		  6,8,4, 7,2,5, 1,3,9,
							 		  3,9,1, 4,6,8, 7,2,5,
							 		  7,2,5, 1,3,9, 6,4,8},


									 {1,2,3, 4,9,7, 8,6,5,
									  4,5,9, 0,0,0, 0,0,0,
									  6,7,8, 0,0,0, 0,0,0,

									  3,0,0, 0,1,0, 0,0,0,
									  2,0,0, 0,0,0, 0,0,0,
									  9,0,0, 0,0,0, 5,0,0,

									  8,0,0, 0,0,0, 0,0,0,
									  7,0,0, 0,0,0, 0,0,0,
									  5,0,0, 9,0,0, 0,0,0} };

	//ßto keep track of presets
	private int boardNum = 0;
	/*
  	board--a 9 by 9 array of integers that represents the current state of the
  	puzzle, where 0 indicates a blank square
  	*/
	private int[][] board = new int[9][9];

	/*
	start--a 9 by 9 array of boolean values that indicates which squares in
	board are given values that cannot be changed and the following methods:
	*/
	private boolean[][] start = new boolean[9][9];

	private void initializeStart() {
		for (int i = 0; i < start.length; i++) {
			for (int j = 0; j < start[i].length; j++) {
				start[i][j] = getValueIn(i, j) == 0;
			}
		}
	}

	//SudokuPuzzle--a constructor that creates an empty puzzle
	public SudokuPuzzle() {generateNewBoard();}

	//***don't need this***toString--returns a string representation of the puzzle that can be printed
	//getBoard--returns board, replaces toString
	public int[][] getBoard() {return board;}

	/*
	addInitial(row, col, value)--sets the given square to the given value as an
	initial value that cannot be changed by the puzzle solver
	*/

	/*
	addGuess(row, col, value)--sets the given square to the given value; the
	value can be changed later by another call to addGuess
	*/
	public void addGuess(int row, int col, int value) {
		if (canChange(row, col) && value >=1 && value <= 9
			&& row >= 0 && row <= 9
			&& col >= 0 && col <= 9)
				board[row][col] = value;
	}

	//following 4 methods are helper functions for checkPuzzle()
	//distinctNums()--checks if array contains only distinct numbers
	private boolean distinctNums(int[] count) {
		for (int i = 0; i < 9; i++) {
			for (int j = i+1; j <= 9; j++) {
			    if (count[j] == 0) continue;
				if (count[i] == count[j]) {
					return false;
				}
			}
		}
		return true;
	}

	//checkRow(int row)--checks if row contains only unique numbers
	private boolean checkRow(int row) {
		int count[] = new int[10];

		for (int i = 0; i < 9; i++) {
			count[i] = board[row][i];
		}

		return distinctNums(count);

	}

	//checkCol(int col)--checks if column contains only unique numbers
	private boolean checkCol(int col) {
		int count[] = new int[10];

		for (int i = 0; i < 9; i++) {
			count[i] = board[i][col];
		}

		return distinctNums(count);
	}

	//checkSubArray(int row, int col)--checks that subArray contains only unique numbers
	private boolean checkSubArray(int row, int col) {
		int count [] = new int[10];
		int k = 0;

		for (int i = row; i < row + 3; i++) {
			for (int j = col; j < col + 3; j++) {
				count[k++] = board[i][j];
			}
		}

		return distinctNums(count);
	}

	//checkPuzzle--returns true if the values in the puzzle do not violate the restrictions
	public boolean checkPuzzle() {
		for (int i = 0; i < 9; i++) {
			if (!checkRow(i)) {
				return false;
			}
			if (!checkCol(i)) {
				return false;
			}
		}

		for (int i = 0; i < 9; i += 3) {
			for (int j = 0; j < 9; j += 3) {
				if (!checkSubArray(i,j)) {
					return false;
				}
			}
		}

		return true;
	}

	//getValueIn(row, col)--returns the value in the given square
	public int getValueIn(int row, int col) {return board[row][col];}

	//canChange(int row, int col)--checks if cell can change
	public boolean canChange(int row, int col) {return start[row][col];}

	/*
	getAllowedValues(row, col)--returns a one-dimensional array of nine booleans,
	each of which corresponds to a digit and is true if the digit can be placed
	in the given square without violating the restrictions
	*/
	public boolean[] getAllowedValues(int row, int col) {
		boolean[] b = {true, true, true, true, true, true, true, true, true};

		for (int i = 0; i < 9; i++) {
			int val = board[row][i];
			if (val > 0) {
				b[val-1] = false;
			}
		}

		for (int i = 0; i < 9; i++) {
			int val = board[i][col];
			if (val > 0) {
				b[val - 1 ] = false;
			}
		}

		int i = (row / 3) * 3;
		int j = (col / 3) * 3;

		for (int ii = i; ii < i + 3; ii++) {
			for (int jj = j; jj < j + 3; jj++) {
				int val = board[ii][jj];
				if (val > 0) {
					b[val - 1] = false;
				}
			}
		}

		return b;
	};

	//isFull--returns true if every square has a value
	public boolean isFull() {
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				if (board[i][j] == 0) {
					return false;
				}
			}
		}
		return true;
	}

	//reset--changes all of the nonpermanent squares back to blanks (0s)
	public void reset() {
		for (int i = 0; i < 81; i++) {
			if(canChange(i/9, i%9))
				board[i/9][i%9] = 0;
		}
	}

	//gernerateNewBoard--creates a new random board if outs of presets
	public void generateNewBoard() {
		if(boardNum < boardPresets.length) {
			for(int i=0; i<81; i++)
				board[i/9][i%9] = boardPresets[boardNum][i];

			boardNum++;
		}
		else {
			Random rand = new Random();
			int numPerfilled = rand.nextInt(20) + 15;
			int rx, ry;

			for(int i=0; i<9; i++)//”empty” the board
				for(int j=0; j<9; j++)
					board[i][j] = 0;

			for(int i=0; i<numPerfilled; i++) {
				do {//check if this element is empty
					rx = rand.nextInt(9);
					ry = rand.nextInt(9);
				}while(board[rx][ry] != 0);

				do {//generate a new value
					board[rx][ry] = rand.nextInt(9);
				}while(!checkPuzzle());//check if set value is compliant with the rules
			}
		}
		initializeStart();
	}

	public static void main(String[] args) {
		REDengine5 TW4 = new REDengine5();
	}
}
