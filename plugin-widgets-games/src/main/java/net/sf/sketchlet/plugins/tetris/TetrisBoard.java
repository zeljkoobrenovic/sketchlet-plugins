package net.sf.sketchlet.plugins.tetris;

import java.util.*;
import java.awt.*;

/**
 * A generic Tetris board with no GUI.
 * 
 * @author Scott Clee
 */
public class TetrisBoard
{
    /**
     * The value of an empty block.
     */
    public static final int EMPTY_BLOCK = -1;

    private Vector  fBoardListeners = new Vector();
    private int[][] fMatrix;
    private int     fColumns;
    private int     fRows;

    /**
     * Create a TetrisBoard with the desired number
     * of columns and rows.
     * 
     * @param cols   The number of columns.
     * @param rows   The number of rows.
     */
    public TetrisBoard(int cols, int rows)
    {
        fColumns = cols;
        fRows    = rows;

        resetBoard();
    }

    /**
     * Sets all entries in the board matrix to the
     * empty piece value.
     */
    public void resetBoard()
    {
        fMatrix  = new int[fColumns][fRows];
       	
        for (int cols = 0; cols < fColumns; cols++) 
    	    for (int rows = 0; rows < fRows; rows++) 
    	    	fMatrix[cols][rows] = EMPTY_BLOCK;
    }

    /**
     * Returns the number of columns in the board.
     * 
     * @return The number of columns in the board.
     */
    public int getColumns()
    {
    	return fColumns;
    }

    /**
     * Set the number of columns in the board.  The board
     * will be reset after this is done.
     * 
     * @param columns The number of desired columns.
     */
    public void setColumns(int columns)
    {
        fColumns = columns;
        resetBoard();
    }

    /**
     * Returns the number of rows in the board.
     * 
     * @return The number of rows in the board.
     */
    public int getRows()
    {
    	return fRows;
    }

    /**
     * Set the number of rows in the board.  The board
     * will be reset after this is done.
     * 
     * @param rowss The number of desired rows.
     */
    public void setRows(int rows)
    {
        fRows = rows;
        resetBoard();
    }   

    /**
     * Returns the value of the block at the given 
     * coordinates.
     * 
     * @param x      The x coordinate.
     * @param y      The y coordinate.
     * @return The value at the given coordinates.
     */
    public int getPieceAt(int x, int y)
    {
    	return fMatrix[x][y];
    }

    /**
     * Sets the piece at the given coordinates to the
     * given value.
     * 
     * @param x      The x coordinate.
     * @param y      The y coordinate.
     * @param value  The value to be set.
     */
    public void setPieceAt(int x, int y, int value)
    {
        fMatrix[x][y] = value;
    }

    /**
     * Add a piece to the board.
     * 
     * The notify parameter is there to supress events in
     * such cases as when performing a fall which will involve
     * multiple add/removes.
     * 
     * @param piece  The piece to add.
     * @param notify If true then fire a BoardEvent once the piece
     *               is added.
     */
    public void addPiece(TetrisPiece piece, boolean notify)
    {
        if (piece != null)
        {
            final Point   centre = piece.getCentrePoint();
            final Point[] blocks = piece.getRelativePoints();

        	for (int count = 0; count < 4; count++) 
        	{
        	    int x = centre.x + blocks[count].x;
        	    int y = centre.y + blocks[count].y;
        
        	    fMatrix[x][y] = piece.getType();
        	}
                    
        	if (notify) fireBoardEvent();
        }
    }

    /**
     * Remove the piece from the board.
     * 
     * @param piece  The piece to remove.
     */
    public void removePiece(TetrisPiece piece)
    {
        if (piece != null)	
        {
            final Point   centre = piece.getCentrePoint();
            final Point[] blocks = piece.getRelativePoints();

            for (int count = 0; count < 4; count++) 
        	{
        	    int x = centre.x + blocks[count].x;
        	    int y = centre.y + blocks[count].y;
            
        	    fMatrix[x][y] = EMPTY_BLOCK;
        	}
        }
    }

    /**
     * Removes the row at the given row index.  Rows
     * which are above the given one are then moved
     * downwards one place.
     * 
     * A BoardEvent will be fired after the rows are
     * moved.
     * 
     * @param row    The index of the row to remove.
     */
    public void removeRow(int row)
    {
    	for (int tempRow = row; tempRow > 0; tempRow--) 
    	{
    	    for (int tempCol = 0; tempCol < fColumns; tempCol++) 
    	    {
    		    fMatrix[tempCol][tempRow] = fMatrix[tempCol][tempRow - 1];
    	    }
    	}
    
    	for (int tempCol = 0; tempCol < fColumns; tempCol++) 
    	{
    	    fMatrix[tempCol][0] = EMPTY_BLOCK;
    	}
    
    	fireBoardEvent();
    }

    /**
     * Tests to see if the given piece will fit in this
     * board.
     * 
     * @param piece  The piece to test against.
     * @return true if the piece fits else false.
     */
    public boolean willFit(TetrisPiece piece)
    {
        boolean result = true;

        if (piece != null)
        {
            final Point   centre = piece.getCentrePoint();
            final Point[] blocks = piece.getRelativePoints();
    
        	for (int count = 0; count < 4 && result == true; count++) 
        	{
        	    int x = centre.x + blocks[count].x;
        	    int y = centre.y + blocks[count].y;
        	    
                // Ensure it's within the boundaries
        	    if (x < 0 || x >= fColumns || y < 0 || y >= fRows)
        		    result = false;
        	     
                if (result && fMatrix[x][y] != EMPTY_BLOCK) result = false;
        	}
        }

    	return result;
    }

    /**
     * Adds a BoardListener to this board.
     * 
     * @param listener The listener to add.
     */
    public void addBoardListener(BoardListener listener)
    {
        fBoardListeners.addElement(listener);
    }

    /**
     * Remove a BoardListener from this board.
     * 
     * @param listener The listener to remove.
     */
    public void removeBoardListener(BoardListener listener)
    {
        fBoardListeners.removeElement(listener);
    }

    private void fireBoardEvent()
    {
        // Generate the event.
        final BoardEvent event = new BoardEvent(this);

        // Now tell everyone.
        for (int count = 0; count < fBoardListeners.size(); count++)
            ((BoardListener)fBoardListeners.elementAt(count)).boardChange(event);
    }
}
