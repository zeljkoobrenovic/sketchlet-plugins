/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins;

import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.context.SketchletGraphicsContext;
import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.plugin.WidgetPlugin;
import net.sf.sketchlet.plugin.WidgetPluginProperty;
import net.sf.sketchlet.plugins.tetris.BoardEvent;
import net.sf.sketchlet.plugins.tetris.BoardListener;
import net.sf.sketchlet.plugins.tetris.ScoreEvent;
import net.sf.sketchlet.plugins.tetris.ScoreListener;
import net.sf.sketchlet.plugins.tetris.TetrisBoard;
import net.sf.sketchlet.plugins.tetris.TetrisGame;
import net.sf.sketchlet.plugins.tetris.TetrisPiece;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

/**
 * @author zobrenovic
 */
@PluginInfo(name = "Tetris", type = "widget", group = "Games")
public class WidgetGameTetris extends WidgetPlugin implements BoardListener {

    private final TetrisGame tetrisGame = new TetrisGame();
    private TetrisBoard tetrisBoard;

    private BufferedImage image;
    private double speed = 1.0;

    @WidgetPluginProperty(name = "variable score", initValue = "tetris-score")
    private String variableScoreName = "tetris-score";

    @WidgetPluginProperty(name = "variable lines", initValue = "tetris-lines")
    private String variableLinesName = "tetris-lines";

    @WidgetPluginProperty(name = "variable status", initValue = "tetris-status")
    private String variableStatusName = "tetris-status";

    @WidgetPluginProperty(name = "variable action", initValue = "tetris-action")
    private String variableActionName = "tetris-action";

    @WidgetPluginProperty(name = "variable speed", initValue = "tetris-speed")
    private String variableSpeedName = "tetris-speed";

    private boolean updating = false;
    private long lastKeyTime = 0;

    public WidgetGameTetris(final ActiveRegionContext region) {
        super(region);

        if (!variableSpeedName.isEmpty()) {
            this.variableUpdated(variableSpeedName, VariablesBlackboardContext.getInstance().getVariableValue(variableSpeedName));
        }
        WidgetPlugin.setActiveWidget(this);
        tetrisGame.addBoardListener(this);
        updateVariable(variableScoreName, "0");
        updateVariable(variableLinesName, "0");
        updateVariable(variableStatusName, "not started");
        updateVariable(variableActionName, "");
        updateVariable(variableSpeedName, "" + speed);
        tetrisGame.addScoreListener(new ScoreListener() {

            public void scoreChange(ScoreEvent e) {
                updateVariable(getActiveRegionContext().getWidgetProperty("variable score"), (e.getScore() / 100) + "");
                updateVariable(getActiveRegionContext().getWidgetProperty("variable lines"), (tetrisGame.getTotalLines()) + "");
            }
        });
    }

    @Override
    public void boardChange(BoardEvent e) {
        tetrisBoard = (TetrisBoard) e.getSource();
        repaint();
    }

    @Override
    public void paint(Graphics2D g2) {
        final int width = getActiveRegionContext().getWidth();
        final int height = getActiveRegionContext().getHeight();

        // Set up double buffering.
        image = SketchletGraphicsContext.getInstance().createCompatibleImage(width, height, image);
        final Graphics2D gImage = image.createGraphics();
        gImage.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw the board pieces if board not null.
        if (this.getActiveRegionContext() != null) {
            final int numCols = 10;
            final int numRows = 20;
            if (tetrisBoard != null) {
                for (int cols = 0; cols < numCols; cols++) {
                    for (int rows = 0; rows < numRows; rows++) {
                        final int piece = tetrisBoard.getPieceAt(cols, rows);

                        if (piece != TetrisBoard.EMPTY_BLOCK) {
                            gImage.setColor(getPieceColor(piece));
                            drawBlock(gImage, (cols * width / numCols) + 1, (rows * height / numRows) + 1, (width / numCols) - 1, (height / numRows) - 1);
                        }
                    }
                }
            } else {
                gImage.setColor(Color.BLUE);
                drawBlock(gImage, (0 * width / numCols) + 1, (0 * height / numRows) + 1, (width / numCols) - 1, (height / numRows) - 1);
                gImage.setColor(Color.BLUE);
                drawBlock(gImage, (0 * width / numCols) + 1, (1 * height / numRows) + 1, (width / numCols) - 1, (height / numRows) - 1);
                gImage.setColor(Color.BLUE);
                drawBlock(gImage, (0 * width / numCols) + 1, (2 * height / numRows) + 1, (width / numCols) - 1, (height / numRows) - 1);
                gImage.setColor(Color.BLUE);
                drawBlock(gImage, (1 * width / numCols) + 1, (2 * height / numRows) + 1, (width / numCols) - 1, (height / numRows) - 1);
            }
        }

        gImage.setColor(Color.BLACK);
        gImage.drawRect(0, 0, width - 1, height - 1);
        g2.drawImage(image, 0, 0, width, height, null);
    }

    @Override
    public void variableUpdated(String triggerVariable, String value) {
        if (triggerVariable.equalsIgnoreCase(getActiveRegionContext().getWidgetProperty("variable status"))) {
            if (value.equalsIgnoreCase("play") || value.equalsIgnoreCase("start")) {
                if (!tetrisGame.isPlaying()) {
                    tetrisGame.startGame();
                } else {
                    if (tetrisGame.isPaused()) {
                        tetrisGame.setPaused(false);
                    }
                }
                this.requestFocus();
            } else if (value.equalsIgnoreCase("stop")) {
                if (tetrisGame.isPlaying()) {
                    tetrisGame.stopGame();
                }
            } else if (value.equalsIgnoreCase("pause")) {
                tetrisGame.setPaused(true);
            }
        } else if (triggerVariable.equalsIgnoreCase(getActiveRegionContext().getWidgetProperty("variable lines"))) {
            try {
                int lines = (int) Double.parseDouble(value);
                tetrisGame.setTotalLines(lines);
                tetrisGame.checkTotalLinesTarget();
            } catch (Exception e) {
            }
        } else if (triggerVariable.equalsIgnoreCase(getActiveRegionContext().getWidgetProperty("variable score"))) {
            try {
                int score = (int) Double.parseDouble(value);
                tetrisGame.setScore(score * 100);
            } catch (Exception e) {
            }
        } else if (triggerVariable.equalsIgnoreCase(getActiveRegionContext().getWidgetProperty("variable speed"))) {
            try {
                speed = Double.parseDouble(value);
                tetrisGame.speed = speed;
            } catch (Exception e) {
            }
        } else if (!updating && triggerVariable.equalsIgnoreCase(getActiveRegionContext().getWidgetProperty("variable action"))) {
            if (value.equalsIgnoreCase("left")) {
                tetrisGame.move(TetrisPiece.LEFT);
            } else if (value.equalsIgnoreCase("right")) {
                tetrisGame.move(TetrisPiece.RIGHT);
            } else if (value.equalsIgnoreCase("rotate")) {
                tetrisGame.move(TetrisPiece.ROTATE);
            } else if (value.equalsIgnoreCase("down")) {
                tetrisGame.move(TetrisPiece.DOWN);
            } else if (value.equalsIgnoreCase("fall")) {
                tetrisGame.move(TetrisPiece.FALL);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!tetrisGame.isPlaying() && System.currentTimeMillis() - lastKeyTime > 3000) {
            tetrisGame.startGame();
            updateVariable(getActiveRegionContext().getWidgetProperty("variable status"), "play");
        }
        lastKeyTime = System.currentTimeMillis();
        updating = true;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                tetrisGame.move(TetrisPiece.LEFT);
                updateVariable(getActiveRegionContext().getWidgetProperty("variable action"), "left");
                break;
            case KeyEvent.VK_RIGHT:
                tetrisGame.move(TetrisPiece.RIGHT);
                updateVariable(getActiveRegionContext().getWidgetProperty("variable action"), "right");
                break;
            case KeyEvent.VK_UP:
                tetrisGame.move(TetrisPiece.ROTATE);
                updateVariable(getActiveRegionContext().getWidgetProperty("variable action"), "rotate");
                break;
            case KeyEvent.VK_DOWN:
                tetrisGame.move(TetrisPiece.DOWN);
                updateVariable(getActiveRegionContext().getWidgetProperty("variable action"), "down");
                break;
            case KeyEvent.VK_SHIFT:
                tetrisGame.move(TetrisPiece.FALL);
                updateVariable(getActiveRegionContext().getWidgetProperty("variable action"), "fall");
                break;
            case KeyEvent.VK_SPACE:
                tetrisGame.move(TetrisPiece.FALL);
                updateVariable(getActiveRegionContext().getWidgetProperty("variable action"), "fall");
                break;
        }
        updating = false;
    }

    private void updateVariable(String name, String value) {
        if (!name.isEmpty() && !value.isEmpty()) {
            VariablesBlackboardContext.getInstance().updateVariable(name, value);
        }
    }

    private Color getPieceColor(int color) {
        Color result = null;

        switch (color) {
            case TetrisPiece.L_PIECE:
                result = new Color(24, 105, 198);  // turquoise
                break;
            case TetrisPiece.J_PIECE:
                result = new Color(206, 56, 173);  // purple
                break;
            case TetrisPiece.I_PIECE:
                result = new Color(41, 40, 206);   // blue
                break;
            case TetrisPiece.Z_PIECE:
                result = new Color(212, 0, 0);     // red
                break;
            case TetrisPiece.S_PIECE:
                result = new Color(82, 154, 16);   // green
                break;
            case TetrisPiece.O_PIECE:
                result = new Color(123, 121, 123); // gray
                break;
            case TetrisPiece.T_PIECE:
                result = new Color(156, 142, 8);   // yellow
                break;
        }

        return result;
    }

    private void drawBlock(Graphics g, int x, int y, int width, int height) {
        g.fillRect(x, y, width, height);
        g.setColor(Color.black);
        g.drawRect(x, y, width, height);
    }
}
