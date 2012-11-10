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
import net.sf.sketchlet.plugins.tetris.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * @author zobrenovic
 */
@PluginInfo(name = "Tetris", type = "widget", group = "Games")
public class WidgetGameTetris extends WidgetPlugin implements BoardListener {

    private final TetrisGame fGame = new TetrisGame();
    private boolean bPressed = false;
    private BufferedImage image;
    private double speed = 1.0;
    @WidgetPluginProperty(name="variable score", initValue = "tetris-score")
    private String  variableScore = "tetris-score";
    @WidgetPluginProperty(name="variable lines", initValue = "tetris-lines")
    private String  variableLines = "tetris-lines";
    @WidgetPluginProperty(name="variable status", initValue = "tetris-status")
    private String  variableStatus = "tetris-status";
    @WidgetPluginProperty(name="variable action", initValue = "tetris-action")
    private String  variableAction = "tetris-action";
    @WidgetPluginProperty(name="variable speed", initValue = "tetris-speed")
    private String  variableSpeed = "tetris-speed";

    public WidgetGameTetris(final ActiveRegionContext region) {
        super(region);

        if (!variableSpeed.isEmpty()) {
            this.variableUpdated(variableSpeed, VariablesBlackboardContext.getInstance().getVariableValue(variableSpeed));
        }
        WidgetPlugin.setActiveWidget(this);
        fGame.addBoardListener(this);
        updateVariable(variableScore, "0");
        updateVariable(variableLines, "0");
        updateVariable(variableStatus, "not started");
        updateVariable(variableAction, "");
        updateVariable(variableSpeed, "" + speed);
        fGame.addScoreListener(new ScoreListener() {

            public void scoreChange(ScoreEvent e) {
                updateVariable(getActiveRegionContext().getWidgetProperty("variable score"), (e.getScore() / 100) + "");
                updateVariable(getActiveRegionContext().getWidgetProperty("variable lines"), (fGame.getTotalLines()) + "");
            }
        });
    }

    private TetrisBoard fBoard;

    public boolean hasTextItems() {
        return false;
    }

    private void updateVariable(String name, String value) {
        if (!name.isEmpty() && !value.isEmpty()) {
            VariablesBlackboardContext.getInstance().updateVariable(name, value);
        }
    }

    public void boardChange(BoardEvent e) {
        fBoard = (TetrisBoard) e.getSource();
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
            if (fBoard != null) {
                for (int cols = 0; cols < numCols; cols++) {
                    for (int rows = 0; rows < numRows; rows++) {
                        final int piece = fBoard.getPieceAt(cols, rows);

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

    private void drawBlock(Graphics g, int x, int y, int width, int height) {
        g.fillRect(x, y, width, height);
        g.setColor(Color.black);
        g.drawRect(x, y, width, height);
    }

    @Override
    public void variableUpdated(String triggerVariable, String value) {
        if (triggerVariable.equalsIgnoreCase(getActiveRegionContext().getWidgetProperty("variable status"))) {
            if (value.equalsIgnoreCase("play") || value.equalsIgnoreCase("start")) {
                if (!fGame.isPlaying()) {
                    fGame.startGame();
                } else {
                    if (fGame.isPaused()) {
                        fGame.setPaused(false);
                    }
                }
                this.requestFocus();
            } else if (value.equalsIgnoreCase("stop")) {
                if (fGame.isPlaying()) {
                    fGame.stopGame();
                }
            } else if (value.equalsIgnoreCase("pause")) {
                fGame.setPaused(true);
            }
        } else if (triggerVariable.equalsIgnoreCase(getActiveRegionContext().getWidgetProperty("variable lines"))) {
            try {
                int lines = (int) Double.parseDouble(value);
                fGame.setTotalLines(lines);
                fGame.checkTotalLinesTarget();
            } catch (Exception e) {
            }
        } else if (triggerVariable.equalsIgnoreCase(getActiveRegionContext().getWidgetProperty("variable score"))) {
            try {
                int score = (int) Double.parseDouble(value);
                fGame.setScore(score * 100);
            } catch (Exception e) {
            }
        } else if (triggerVariable.equalsIgnoreCase(getActiveRegionContext().getWidgetProperty("variable speed"))) {
            try {
                speed = Double.parseDouble(value);
                fGame.speed = speed;
            } catch (Exception e) {
            }
        } else if (!bUpdating && triggerVariable.equalsIgnoreCase(getActiveRegionContext().getWidgetProperty("variable action"))) {
            if (value.equalsIgnoreCase("left")) {
                fGame.move(TetrisPiece.LEFT);
            } else if (value.equalsIgnoreCase("right")) {
                fGame.move(TetrisPiece.RIGHT);
            } else if (value.equalsIgnoreCase("rotate")) {
                fGame.move(TetrisPiece.ROTATE);
            } else if (value.equalsIgnoreCase("down")) {
                fGame.move(TetrisPiece.DOWN);
            } else if (value.equalsIgnoreCase("fall")) {
                fGame.move(TetrisPiece.FALL);
            }
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

    @Override
    public void mousePressed(MouseEvent me) {
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        repaint();
    }

    private boolean bUpdating = false;
    private long lastKeyTime = 0;

    @Override
    public void keyPressed(KeyEvent e) {
        if (!fGame.isPlaying() && System.currentTimeMillis() - lastKeyTime > 3000) {
            fGame.startGame();
            updateVariable(getActiveRegionContext().getWidgetProperty("variable status"), "play");
        }
        lastKeyTime = System.currentTimeMillis();
        bUpdating = true;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                fGame.move(TetrisPiece.LEFT);
                updateVariable(getActiveRegionContext().getWidgetProperty("variable action"), "left");
                break;
            case KeyEvent.VK_RIGHT:
                fGame.move(TetrisPiece.RIGHT);
                updateVariable(getActiveRegionContext().getWidgetProperty("variable action"), "right");
                break;
            case KeyEvent.VK_UP:
                fGame.move(TetrisPiece.ROTATE);
                updateVariable(getActiveRegionContext().getWidgetProperty("variable action"), "rotate");
                break;
            case KeyEvent.VK_DOWN:
                fGame.move(TetrisPiece.DOWN);
                updateVariable(getActiveRegionContext().getWidgetProperty("variable action"), "down");
                break;
            case KeyEvent.VK_SHIFT:
                fGame.move(TetrisPiece.FALL);
                updateVariable(getActiveRegionContext().getWidgetProperty("variable action"), "fall");
                break;
            case KeyEvent.VK_SPACE:
                fGame.move(TetrisPiece.FALL);
                updateVariable(getActiveRegionContext().getWidgetProperty("variable action"), "fall");
                break;
        }
        bUpdating = false;
    }
}
