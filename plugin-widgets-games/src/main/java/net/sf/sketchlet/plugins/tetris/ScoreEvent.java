package net.sf.sketchlet.plugins.tetris;

import java.awt.*;

/**
 * The ScoreEvent class.
 * 
 * @author Scott Clee
 */
public class ScoreEvent extends Event
{
    private int fScore;

    /**
     * Create a ScoreEvent.
     * 
     * @param score  The new score.
     */
    public ScoreEvent(int score)
    {
	    super(null, 0, null);
	    fScore = score;
    }

    /**
     * Returns the new score.
     * 
     * @return The new score.
     */
    public int getScore()
    {
	    return fScore;
    }
}
