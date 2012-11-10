package net.sf.sketchlet.plugins.tetris;

import java.util.Vector;
import java.awt.Point;

/**
 * A generic Tetris game with no GUI.
 * 
 * @author Scott Clee
 */
public class TetrisGame {

    private EventHandler fEventHandler;
    private TetrisBoard fBoard;
    private TetrisPiece fCurrPiece;
    private Thread fGameThread;
    private int fScore;
    private int fTotalLines;
    private int fDelay;
    public double speed = 1.0;
    private boolean fPlaying;
    private boolean fPaused;

    /**
     * Create a TetrisGame.
     */
    public TetrisGame() {
        fEventHandler = new EventHandler();
        fBoard = new TetrisBoard(10, 20);
        fPlaying = false;
    }

    /**
     * Start a Tetris game if one is not already playing.
     */
    public void startGame() {
        if (fPlaying == false) {
            fBoard.resetBoard();
            fTotalLines = 0;
            fScore = 0;
            fDelay = 500;
            fPlaying = true;
            fPaused = false;
            fCurrPiece = null;
            fEventHandler.fireScoreEvent();
            fEventHandler.fireGameEvent(new GameEvent(GameEvent.START));

            // Initialize the game thread.
            fGameThread = new GameThread();
            fGameThread.start();
        }
    }

    /**
     * Stop the current game.
     */
    public void stopGame() {
        fPlaying = false;
        fEventHandler.fireGameEvent(new GameEvent(GameEvent.END));
    }

    /**
     * Returns a copy of the current piece.
     * 
     * @return A copy of the current piece.
     */
    public TetrisPiece getCurrentPiece() {
        return fCurrPiece;
    }

    /**
     * Sets the current piece.
     * 
     * @param currPiece The current piece.
     */
    public void setCurrentPiece(TetrisPiece currPiece) {
        fCurrPiece = currPiece;
    }

    /**
     * Queries if the game is paused.
     * 
     * @return true is the game is paused.
     */
    public boolean isPaused() {
        return fPaused;
    }

    /**
     * Pause the current game.
     * 
     * @param pauseIt Toggles the pause state.
     */
    public void setPaused(boolean pauseIt) {
        if (fPlaying) {
            fPaused = pauseIt;
        }
    }

    /**
     * Queries if the game is in play.
     * 
     * @return true is the game is in play.
     */
    public boolean isPlaying() {
        return fPlaying;
    }

    /**
     * If the game is currently not playing then
     * a value of true will start the game.  A value
     * of false stops the current game.
     * 
     * @param playing true - start the game if not already started.
     *                false - stop the current game.
     */
    public void setPlaying(boolean playing) {
        if (playing) {
            fPlaying = false;
        } else {
            startGame();
        }
    }

    /**
     * If a move is possible then the move call is
     * passed through to the current piece.
     * 
     * @param direction The direction to move the current piece. These are
     *                  the constants from the TetrisPiece class.
     * @return true if the piece moved else false.
     */
    public boolean move(int direction) {
        boolean result = false;

        if (fCurrPiece != null && fPlaying == true && fPaused == false) {
            if (direction == TetrisPiece.DOWN || direction == TetrisPiece.FALL) {
                // If it won't go any further then drop it there.
                if (fCurrPiece.move(direction) == false) {
                    fCurrPiece = null;
                } else {
                    result = true;
                }
            } else {
                result = fCurrPiece.move(direction);
            }
        }

        return result;
    }

    /**
     * Returns the current score.
     * 
     * @return The current score.
     */
    public int getScore() {
        return fScore;
    }

    /**
     * Set the current score.
     * 
     * @param score  The current score.
     */
    public void setScore(int score) {
        fScore = score;
    }

    /**
     * Returns the number of completed lines so far in
     * the game.
     * 
     * @return Total number of completed lines.
     */
    public int getTotalLines() {
        return fTotalLines;
    }

    /**
     * Sets the number of total lines.
     * 
     * @param totalLines The number of total lines.
     */
    public void setTotalLines(int totalLines) {
        fTotalLines = totalLines;
    }

    /**
     * Add a BoardListener to this game.
     * When the board is altered, usually by a piece
     * being moved, then the listeners are told.
     * 
     * @param listener BoardListener
     */
    public void addBoardListener(BoardListener listener) {
        fEventHandler.addBoardListener(listener);
    }

    /**
     * Remove a BoardListener from this game.
     * 
     * @param listener BoardListener
     */
    public void removeBoardListener(BoardListener listener) {
        fEventHandler.removeBoardListener(listener);
    }

    /**
     * Add a ScoreListener to this game.
     * When the score changes the listeners are told.
     * 
     * @param listener ScoreListener
     */
    public void addScoreListener(ScoreListener listener) {
        fEventHandler.addScoreListener(listener);
    }

    /**
     * Remove a ScoreListener from this game.
     * 
     * @param listener ScoreListener
     */
    public void removeScoreListener(ScoreListener listener) {
        fEventHandler.removeScoreListener(listener);
    }

    private class GameThread extends Thread {

        public void run() {
            while (fPlaying) {
                if (!fPaused) {
                    if (fCurrPiece == null) {
                        int completeLines = 0;

                        // First check for any complete lines.
                        for (int rows = fBoard.getRows() - 1; rows >= 0; rows--) {
                            boolean same = true;

                            for (int cols = 0; cols < fBoard.getColumns(); cols++) {
                                if (fBoard.getPieceAt(cols, rows) == TetrisBoard.EMPTY_BLOCK) {
                                    same = false;
                                }
                            }

                            if (same) {
                                // Remove the completed row.
                                fBoard.removeRow(rows);

                                // Set it so we check this row again since
                                // the rows above have been moved down.
                                rows++;

                                // Increment values for scoring.
                                completeLines++;
                                fTotalLines++;

                                checkTotalLinesTarget();
                            }
                        }

                        if (completeLines > 0) {
                            // The more lines completed at once the bigger
                            // the score increment.

                            fScore += completeLines * completeLines * 100;
                            fEventHandler.fireScoreEvent();
                        }

                        fCurrPiece = TetrisPiece.getRandomPiece(fBoard);
                        fCurrPiece.setCentrePoint(new Point(fBoard.getColumns() / 2, 1));

                        if (fBoard.willFit(fCurrPiece)) {
                            // If it fits then add it.
                            fBoard.addPiece(fCurrPiece, true);
                        } else {
                            // If it doesn't then add it anyway
                            // but call the game to an end.
                            fBoard.addPiece(fCurrPiece, true);
                            stopGame();
                            break;
                        }
                    } else {
                        // Drop the piece down by one.
                        if (speed > 0) {
                            move(TetrisPiece.DOWN);
                        }
                    }
                }

                if (fCurrPiece != null) {
                    try {
                        if (speed > 0) {
                            sleep((long) (fDelay / speed));
                        } else {
                            sleep(100);
                        }
                    } catch (InterruptedException e) {
                        System.err.println("Exception e: " + e);
                    }
                }
            }
        }
    }

    public void checkTotalLinesTarget() {
        // If we've hit a target then speed things up.
        if (fTotalLines == 10) {
            fDelay = 400;
        }
        if (fTotalLines == 20) {
            fDelay = 300;
        }
        if (fTotalLines == 30) {
            fDelay = 200;
        }
        if (fTotalLines == 40) {
            fDelay = 150;
        }
        if (fTotalLines == 50) {
            fDelay = 120;
        }
    }

    private class EventHandler {

        private Vector fGameListeners = new Vector();
        private Vector fBoardListeners = new Vector();
        private Vector fScoreListeners = new Vector();

        public void addGameListener(GameListener listener) {
            fGameListeners.addElement(listener);
        }

        public void removeGameListener(GameListener listener) {
            fGameListeners.removeElement(listener);
        }

        public void fireGameEvent(GameEvent event) {
            for (int count = 0; count < fGameListeners.size(); count++) {
                ((GameListener) fGameListeners.elementAt(count)).gameChange(event);
            }
        }

        public void addBoardListener(BoardListener listener) {
            fBoard.addBoardListener(listener);
        }

        public void removeBoardListener(BoardListener listener) {
            fBoard.removeBoardListener(listener);
        }

        public void addScoreListener(ScoreListener listener) {
            fScoreListeners.addElement(listener);
        }

        public void removeScoreListener(ScoreListener listener) {
            fScoreListeners.removeElement(listener);
        }

        public void fireScoreEvent() {
            for (int count = 0; count < fScoreListeners.size(); count++) {
                final ScoreEvent event = new ScoreEvent(fScore);

                ((ScoreListener) fScoreListeners.elementAt(count)).scoreChange(event);
            }
        }
    }
}
