/*
 * Project Sudoku
 * @author Jian Liang Zhao
 * @author Hoeun Sim
 * 
 * Graphical UI
 * 
 * known bugs
 * #1 maximize will not change H and V gap, and subsequent normalize will set the gap as if they were at maximum frame  not going to fix this
 */ 
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.text.BadLocationException;
import java.awt.event.*;


public class REDengine5 extends JComponent
implements  KeyListener, ActionListener 
{
	/**
	 * some sore of serial ID eclipse generated
	 */
	private static final long serialVersionUID = 8290145679077231909L;

	//^ + ^ + v + v + <- + -> + <- + -> + B + A + *  
	private boolean _debug = false;//set to true if you what to see more stuff in the text box

	SudokuPuzzle _sp = new SudokuPuzzle();
	//settings frame
	private int Vgap = 5,Hgap = 5;//default gap
	private int messageBoardDepth = 2;//number of lines will be shown in the text box
	private final Dimension MinFrameSize = new Dimension(550, 550);
	//settings color
	private final Color C_cellBackGround = new Color(0xf4c107),
						C_cellEnabledTxt = new Color(0x033530),
						C_cellDisabled	 = new Color(0x5f019e),
						C_cellHighLined	 = new Color(0x0493e0),
						C_cellDisabledTxt= new Color(0xF0F0AF),
						C_seperationLine = new Color(0x06b8f4),
						C_baordBackGround= new Color(0x490268);
	
	//user interface stuff
	private int _uix, _uiy, _numPastVal, undoBufferCurPos;
	private boolean _numInputEnabled;
	
	//undo button
	private final int undoBufferSize = 5;
	private JButton[] undoBuffer = new JButton[undoBufferSize];
	private JButton B_undo; 
	
	//graphics
	private JButton[][] cells = new JButton[9][9];
	private JButton B_clear, B_newGame;
	private JFrame frame;
	private JPanel windowContent = new JPanel();
	private GridLayout boardLayout = new GridLayout(9,9);
	private JTextArea messageBoard;
	@SuppressWarnings("serial")
	//----------------------------------------------------------end ver declaration
	
	//sets up the board panel
	class boardPanel extends JPanel {
		public boardPanel() {
			//changes the default disabled text color
			UIManager.put("Button.disabledText", new ColorUIResource(C_cellDisabledTxt));
			
			//sets up cells[9][9]
			for(int y=0; y<9; y++) {
				for(int x=0; x<9; x++) {
			        JButton tempBut = new JButton();
					tempBut.setFont(new Font("Arial", Font.PLAIN, 36));
					tempBut.setName( String.valueOf((y)*9 + x) );//used to index the button
					tempBut.setBackground(C_baordBackGround);
					tempBut.setActionCommand(tempBut.getName()); //send index if button is pressed
					add(tempBut);
					cells[y][x] = tempBut;
				}
			}
	        setLayout(boardLayout);
		}
	    @Override
	    protected void paintComponent(Graphics g) {
	        super.paintComponent(g);

	        g.setColor(C_baordBackGround);//backGround of the board
	        g.fillRect(0, 0, frame.getWidth(), frame.getHeight());
	        
	        g.setColor(C_seperationLine);//draw 4 lines to separate each 3x3 square
	        for(int i=1; i<=2; i++) {
	        	g.fillRect((int)cells[0][i*3].getLocation().getX() - Hgap, 0, Hgap, frame.getHeight());
	        	g.fillRect(0, (int)cells[i*3][0].getLocation().getY() - Vgap, frame.getWidth(), Vgap);
	        }
	    }
	}
	
	//----------------------------------------------------------default constructor
	public REDengine5() {
		super();

		_numInputEnabled = false;
		
		//sets up messageBoard
		if(_debug)messageBoardDepth += 10;//need to see more
		messageBoard = new JTextArea("", messageBoardDepth, 40);
		messageBoard.setFont(new Font("Arial", Font.PLAIN, 20));
		messageBoard.setEditable(false);
		messageBoard.setLineWrap(false);
		msg("Use your keyboard to enter the number in");
		msg("May the odds be ever in your favor");//and don't be killed paying this

		//sets up clear, undo and new game button
		JPanel optionsPanel = new JPanel(new GridLayout(1,3));
		B_clear = new JButton("Clear");B_clear.setActionCommand("clear"); B_clear.addActionListener(this);optionsPanel.add(B_clear);
		B_undo = new JButton("Undo");B_undo.setActionCommand("undo"); B_undo.addActionListener(this);optionsPanel.add(B_undo);
		B_newGame = new JButton("New Game");B_newGame.setActionCommand("newG"); B_newGame.addActionListener(this);optionsPanel.add(B_newGame);
		//sets up undo
		B_undo.setEnabled(false);
		_numPastVal = undoBufferCurPos = 0;
		for(int i=0; i<undoBufferSize; i++) {
			undoBuffer[i] = new JButton();
		}
		
		//sets up the board
        windowContent.setLayout(new BorderLayout());
		boardLayout.setVgap(Vgap);
		boardLayout.setHgap(Hgap);
		windowContent.add(BorderLayout.CENTER,new boardPanel());

		frame = new JFrame("SUDOKU");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setMinimumSize(MinFrameSize);
		frame.pack();
		frame.setSize(800, 800);
	    frame.setVisible(true);
	    
		frame.setContentPane(windowContent);
		frame.getContentPane().add(optionsPanel, BorderLayout.PAGE_START);
		frame.getContentPane().add(new JTextField("Event Log"), BorderLayout.SOUTH);
		frame.getContentPane().add(messageBoard, BorderLayout.AFTER_LAST_LINE);
		
		//adaptive cell/button gap
	    frame.addComponentListener(new ComponentAdapter() {
	    	public void componentResized(ComponentEvent evt) {
	    		Vgap = frame.getHeight()/150;
	    		Hgap = frame.getWidth() /150;
	            boardLayout.setVgap(Vgap);
	            boardLayout.setHgap(Hgap);
	        }
	    });
	    delay(200);
	    updateCellsDiagonal();
	}
	//----------------------------------------------------------end default constructor

	//----------------------------------------------------------button event
	/**where the actions' at*/
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand() == "clear") {//user pressed the clear button
			msg("Clearing nonpermanent squares");
			_sp.reset();
			reflash();
		}
		else if(e.getActionCommand() == "undo") {//user pressed the undo button
			if (_numPastVal != 0){//what's happening is just pure magic
				int pos = Integer.valueOf(undoBuffer[undoBufferCurPos].getName() );//get where the last button is in index form
				//debugging stuff
				if(_debug)msg("undo. change#"+pos+" >" + cells[pos/9][pos%9].getText() + "< to " + undoBuffer[undoBufferCurPos].getText());
				
				cells[pos/9][pos%9].setText(undoBuffer[undoBufferCurPos].getText());
				
				if(!undoBuffer[undoBufferCurPos].getText().isEmpty())
					_sp.addGuess(pos/9, pos%9, Integer.valueOf(undoBuffer[undoBufferCurPos].getText()));
				else
					_sp.addGuess(pos/9, pos%9, 0);

				//undo heap management
				_numPastVal--;
				undoBufferCurPos--;
				if(undoBufferCurPos < 0)
					undoBufferCurPos = undoBufferSize-1;
				if(_numPastVal == 0)
					B_undo.setEnabled(false);
			}
		}
		else if(e.getActionCommand() == "newG") {//user pressed the new game button
			msg("NG+...");
			_sp.generateNewBoard();
			B_clear.setEnabled(true);
			reflash();
		}
		else {//send button index to cellSelectionEvent
			cellSelectionEvent( Integer.parseInt(e.getActionCommand()) );
		}//end else
	}//end action
	//----------------------------------------------------------end button event
	
	//----------------------------------------------------------keyboard event
    public void keyTyped(KeyEvent e) {
    	//accept number input and a number is entered
    	if(_numInputEnabled && Character.isDigit(e.getKeyChar())) {//don't think _numInputEnabled is necessary but it can't hurt
    		int num = e.getKeyChar() - '0';
    		
    		if(_debug)msg("passing " + num + " to SudokuPuzzle.addGuess");
    		_sp.addGuess(_uiy, _uix, num);
    		
    		if(_sp.isFull()) {
    			if(_sp.checkPuzzle()) {//win condition: isFull() and checkPuzzle() returns true
    				msg("you have solved this puzzle");
    				disableAllCells(Color.GREEN);
    				B_clear.setEnabled(false);
    				B_undo.setEnabled(false);
    			}
    			else {
    				msg("something is not so right here...");
    			}
    		}
    		
    		cells[_uiy][_uix].removeKeyListener(this);
			cells[_uiy][_uix].setBackground(C_cellBackGround);

			pastVal(cells[_uiy][_uix]);
    		updateBoard(_uiy, _uix, num);
    		_numInputEnabled = false;
    	}
    	else if(e.getKeyCode() == KeyEvent.VK_Z)
        {  
            undo();
        }
    }
	public void keyPressed(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}
    //----------------------------------------------------------end button e

    //----------------------------------------------------------setups
	/**change the entire board. useful for resetting or initialize get game*/
    private void updateBoard(int row, int col, int value) {
		if(value == 0)
			cells[row][col].setText("");
		else
			cells[row][col].setText(String.valueOf(value));
	}
	
	/**change all cells to sudoku puzzle's board value and disable cells that cann't be changed*/
	private void updateCells() {
		for(int y=0; y<9; y++)
			for(int x=0; x<9; x++)
				updateCells(y, x);
	}
	
	/**same as updateCells(null) but fancier*/
	private void updateCellsDiagonal() {
		//shame this didn't really work with button event... don't really feel like leaning how to do threads
		for(int i=0,temp=0; i<18; i++) {
			if(i>8) {//last half
				temp--;
				for(int j=0; j<temp; j++) 
					updateCells(8-j,j+9-temp);
			}
			else {//first half
				for(int j=0; j<temp+1; j++) 
					updateCells(temp-j,j);
				temp++;
			}
			delay(40);
		}
	}//end updateCellsDiagonal
	
	/**change individual cells to sudoku puzzle's board value and disable cells that cann't be changed*/
	private void updateCells(int y, int x) {
		cells[y][x].setEnabled(_sp.canChange(y, x)); 
		updateBoard(y,x,_sp.getValueIn(y, x));
		
		//change cell appearance and enable base on whether or not it can be changed
		if(_sp.canChange(y, x)) {
			cells[y][x].setForeground(C_cellEnabledTxt);
			cells[y][x].setBackground(C_cellBackGround);
			cells[y][x].setToolTipText("(" + x + "," + y + ")");
			cells[y][x].addActionListener(this);
		}
		else {
			cells[y][x].setBackground(C_cellDisabled);
			cells[y][x].setToolTipText("You can't change this");
		}
	}
	//----------------------------------------------------------end setups
	
	//----------------------------------------------------------helpers
	/**handles the magic stuff then user cilck on the button or what not*/
	private void cellSelectionEvent(int cellNum) {
		if(cellNum < 0 && cellNum > 80) return;//input protection
		
		if(_numInputEnabled)
			cells[_uiy][_uix].setBackground(C_cellBackGround);
		
		_uix = cellNum%9;
		_uiy = cellNum/9;
		
		//show some values
		if(_debug)msg("butten#" + cells[_uiy][_uix].getName() + ">" + cells[_uiy][_uix].getText() + "< at pos(" + _uix + "," + _uiy + ") is selected");
		
		cells[_uiy][_uix].setBackground(C_cellHighLined);
		cells[_uiy][_uix].addKeyListener(this);
		
		_numInputEnabled = true;
	}//end cellSelectedEvent
	// + you won!!!
	private void disableAllCells(Color c) {
		B_undo.setEnabled(false);//no point for it
		
		for(int x=0; x<9; x++) 
			for(int y=0; y<9; y++) {
				cells[y][x].removeActionListener(this);
				cells[y][x].removeKeyListener(this);
				cells[y][x].setEnabled(false);
				cells[y][x].setBackground(c);
			}
	}
	/**pretty self explanatory*/
	public void reflash() {
		if(_debug)msg("how reflashing");
		B_undo.setEnabled(false);
		_numPastVal = undoBufferCurPos = 0;
		updateCells();
	}
	/**in milliseconds*/
	private void delay(int ms) {//oh... the old arduino days..
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**store past values to the nudo heap array*/
	private void pastVal(JButton val) {
		undoBufferCurPos++;
		if(undoBufferCurPos == undoBufferSize)
			undoBufferCurPos = 0;

		if(undoBufferCurPos != undoBufferSize)
			_numPastVal++;
		
		undoBuffer[undoBufferCurPos].setName(val.getName());
		undoBuffer[undoBufferCurPos].setText(val.getText());
		B_undo.setEnabled(true);
	}
	/**restore vals*/
	private void undo() {
		if (_numPastVal != 0){
			int pos = Integer.valueOf(undoBuffer[undoBufferCurPos].getName() );
			
			//show where and what value is bing pass to undo
			if(_debug)msg("undo. change#"+pos+" >" + cells[pos/9][pos%9].getText() + "< to " + undoBuffer[undoBufferCurPos].getText());
			
			cells[pos/9][pos%9].setText(undoBuffer[undoBufferCurPos].getText());
			if(!undoBuffer[undoBufferCurPos].getText().isEmpty())
				_sp.addGuess(pos/9, pos%9, Integer.valueOf(undoBuffer[undoBufferCurPos].getText()));

			_numPastVal--;
			undoBufferCurPos--;
			if(undoBufferCurPos < 0)
				undoBufferCurPos = undoBufferSize-1;
			
			if(_numPastVal == 0)
				B_undo.setEnabled(false);
		}
	}
	/**show string s on the text box. will only show messageBoardDepth lines at once*/
	public void msg(String s)  {
		messageBoard.insert(s+"\n", 0);
		
		if(messageBoard.getLineCount() > messageBoardDepth) {
			try {
				messageBoard.setText(messageBoard.getText().substring(0, messageBoard.getLineStartOffset(messageBoardDepth)-1));
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				messageBoard.setText("ERR!!!BadLocationException in msg");
				e.printStackTrace();
			}
		}//end if
	}//end msg
}//end class
