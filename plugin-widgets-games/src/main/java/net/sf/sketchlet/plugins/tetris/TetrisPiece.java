package net.sf.sketchlet.plugins.tetris;

import java.awt.Point;

/**
 * A generic Tetris piece with no GUI.
 * 
 * @author Scott Clee
 */
public class TetrisPiece {

    public static final int L_PIECE = 0;
    public static final int J_PIECE = 1;
    public static final int I_PIECE = 2;
    public static final int Z_PIECE = 3;
    public static final int S_PIECE = 4;
    public static final int O_PIECE = 5;
    public static final int T_PIECE = 6;
    public static final int LEFT = 10;
    public static final int RIGHT = 11;
    public static final int ROTATE = 12;
    public static final int DOWN = 13;
    public static final int FALL = 14;
    private int fType;
    private int fRotation;
    private int fMaxRotate;
    private Point fCentrePoint = new Point();
    private Point[] fBlocks = new Point[4];
    private TetrisBoard fBoard;

    /**
     * Create a TetrisPiece object.
     * 
     * @param type   The type/shape of the piece.
     * @param board  The board the piece is going to move around in.
     */
    public TetrisPiece(int type, TetrisBoard board) {
        fType = type;
        fBoard = board;
        initializeBlocks();
    }

    /**
     * Move this piece in the given direction.
     * 
     * @param direction The direction of the move.
     *                  This should be of the form:
     *                  TetrisPiece.LEFT           
     *                  TetrisPiece.RIGHT          
     *                  TetrisPiece.ROTATE         
     *                  TetrisPiece.DOWN           
     *                  TetrisPiece.FALL
     * @return true if the move was completed.
     */
    public boolean move(int direction) {
        boolean result = true;

        if (direction == FALL) {
            boolean loop = true;

            while (loop) {
                fBoard.removePiece(this);
                fCentrePoint.y++; // Drop

                if (fBoard.willFit(this)) {
                    fBoard.addPiece(this, false);
                } else {
                    fCentrePoint.y--; // Undrop
                    fBoard.addPiece(this, true);
                    loop = false;
                    result = false;
                }
            }
        } else {
            fBoard.removePiece(this);

            switch (direction) {
                case LEFT:
                    fCentrePoint.x--;
                    break; // Move left
                case RIGHT:
                    fCentrePoint.x++;
                    break; // Move right
                case DOWN:
                    fCentrePoint.y++;
                    break; // Drop
                case ROTATE:
                    rotateClockwise();
                    break;
            }

            if (fBoard.willFit(this)) {
                fBoard.addPiece(this, true);
            } else // Undo the move
            {
                switch (direction) {
                    case LEFT:
                        fCentrePoint.x++;
                        break; // Move right
                    case RIGHT:
                        fCentrePoint.x--;
                        break; // Move left
                    case DOWN:
                        fCentrePoint.y--;
                        break; // Undrop
                    case ROTATE:
                        rotateAntiClockwise();
                        break;
                }

                fBoard.addPiece(this, true);
                result = false;
            }
        }

        return result;
    }

    /**
     * Returns the centre coordinate of this piece.
     * 
     * @return The centre point.			
     */
    public Point getCentrePoint() {
        return fCentrePoint;
    }

    /**
     * Set the Centre point of this piece.
     * 
     */
    public void setCentrePoint(Point point) {
        fCentrePoint = point;
    }

    /**
     * Returns an array containing the relative point positions
     * around the centre piece. i.e. (0, -1) for 1 block above.
     * 
     * @return A Point array of relative points.					 
     */
    public Point[] getRelativePoints() {
        return fBlocks;
    }

    /**
     * Set the relative centre points.
     * 
     * @param blocks The relative centre points.
     */
    public void setRelativePoints(Point[] blocks) {
        if (blocks != null) {
            fBlocks = blocks;
        }
    }

    /**
     * Returns the type of this piece.
     * 
     * @return The type of this piece.
     */
    public int getType() {
        return fType;
    }

    /**
     * Set the type of this piece.
     * 
     * @param type   The type of this piece.
     */
    public void setType(int type) {
        fType = type;
        initializeBlocks();
    }

    private void initializeBlocks() {
        switch (fType) {
            case I_PIECE:
                fBlocks[0] = new Point(0, 0);
                fBlocks[1] = new Point(-1, 0);
                fBlocks[2] = new Point(1, 0);
                fBlocks[3] = new Point(2, 0);
                fMaxRotate = 2;
                break;

            case L_PIECE:
                fBlocks[0] = new Point(0, 0);
                fBlocks[1] = new Point(-1, 0);
                fBlocks[2] = new Point(-1, 1);
                fBlocks[3] = new Point(1, 0);
                fMaxRotate = 4;
                break;

            case J_PIECE:
                fBlocks[0] = new Point(0, 0);
                fBlocks[1] = new Point(-1, 0);
                fBlocks[2] = new Point(1, 0);
                fBlocks[3] = new Point(1, 1);
                fMaxRotate = 4;
                break;

            case Z_PIECE:
                fBlocks[0] = new Point(0, 0);
                fBlocks[1] = new Point(-1, 0);
                fBlocks[2] = new Point(0, 1);
                fBlocks[3] = new Point(1, 1);
                fMaxRotate = 2;
                break;

            case S_PIECE:
                fBlocks[0] = new Point(0, 0);
                fBlocks[1] = new Point(1, 0);
                fBlocks[2] = new Point(0, 1);
                fBlocks[3] = new Point(-1, 1);
                fMaxRotate = 2;
                break;

            case O_PIECE:
                fBlocks[0] = new Point(0, 0);
                fBlocks[1] = new Point(0, 1);
                fBlocks[2] = new Point(-1, 0);
                fBlocks[3] = new Point(-1, 1);
                fMaxRotate = 1;
                break;

            case T_PIECE:
                fBlocks[0] = new Point(0, 0);
                fBlocks[1] = new Point(-1, 0);
                fBlocks[2] = new Point(1, 0);
                fBlocks[3] = new Point(0, 1);
                fMaxRotate = 4;
                break;
        }
    }

    private void rotateClockwise() {
        if (fMaxRotate > 1) // If the piece is allowed to rotate 
        {
            fRotation++;

            if (fMaxRotate == 2 && fRotation == 2) {
                // Rotate Anti-Clockwise
                rotateClockwiseNow();
                rotateClockwiseNow();
                rotateClockwiseNow();
            } else {
                rotateClockwiseNow();
            }
        }

        fRotation = fRotation % fMaxRotate;
    }

    private void rotateAntiClockwise() {
        rotateClockwise();
        rotateClockwise();
        rotateClockwise();
    }

    private void rotateClockwiseNow() {
        for (int count = 0; count < 4; count++) {
            final int temp = fBlocks[count].x;

            fBlocks[count].x = -fBlocks[count].y;
            fBlocks[count].y = temp;
        }
    }

    /**
     * Returns a random piece to use in the 
     * given board.
     * 
     * @param board  The board the piece will be in.
     * @return A random piece.
     */
    public static TetrisPiece getRandomPiece(TetrisBoard board) {
        return new TetrisPiece((int) (Math.random() * 7), board);
    }
}
