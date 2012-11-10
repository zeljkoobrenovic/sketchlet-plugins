/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.widgets.graphs;

import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.plugin.WidgetPlugin;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author zobrenovic
 */
public class ExternalImageProgramCallerWidget extends WidgetPlugin {

    String strOldSignature = "";
    private BufferedImage image;
    boolean creating = false;
    boolean waiting = false;
    boolean timeout = false;
    long prevTime = 0;
    private Object lock = new Object();
    private CountDownLatch create = null;
    ExecutorService executor = Executors.newCachedThreadPool();
    protected boolean bScale = false;

    public ExternalImageProgramCallerWidget(ActiveRegionContext region) {
        super(region);
    }

    protected void callImageGenerator() {
    }
    long lastTime = 0;

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public BufferedImage getImage() {
        return this.image;
    }

    protected void createImage() {
        if (!SketchletContext.getInstance().isInBatchMode()) {
            synchronized (lock) {
                if (waiting) {
                    return;
                }
                if (creating || System.nanoTime() - lastTime < 1000000000) {
                    waiting = true;
                } else {
                    creating = true;
                }
            }
        }

        if (waiting && !SketchletContext.getInstance().isInBatchMode()) {
            executor.execute(new Runnable() {

                public void run() {
                    try {
                        Thread.sleep(500);
                        if (create != null) {
                            create.await();
                        }
                        createImage();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        waiting = false;
                        SketchletContext.getInstance().repaint();
                    }
                }
            });
        } else if (creating || SketchletContext.getInstance().isInBatchMode()) {
            if (!SketchletContext.getInstance().isInBatchMode()) {
                executor.execute(new Runnable() {

                    public void run() {
                        if (inProcess) {
                            return;
                        }
                        if (create != null) {
                            while (create.getCount() > 0) {
                                create.countDown();
                            }
                        }
                        create = new CountDownLatch(1);
                        try {
                            inProcess = true;
                            callImageGenerator();
                        } finally {
                            lastTime = System.nanoTime();
                            creating = false;
                            create.countDown();
                            inProcess = false;
                            SketchletContext.getInstance().repaint();
                        }
                    }
                });
            } else {
                callImageGenerator();
                lastTime = System.nanoTime();
                creating = false;
                inProcess = false;
                timeout = false;
            }
        }
        if (!SketchletContext.getInstance().isInBatchMode()) {
            SketchletContext.getInstance().repaint();
        }
    }
    private boolean inProcess = false;

    protected String getText() {
        return getActiveRegionContext().getWidgetItemText();
    }

    @Override
    public void paint(Graphics2D g2) {
        //long time = System.currentTimeMillis();
        String strText = getText();
        if (strText.isEmpty()) {
            return;
        }
        if (strText.length() > 0) {
            int x1 = 0;
            int y1 = 0;

            if (image == null || !strOldSignature.equals(this.getSignature())) {
                createImage();
                strOldSignature = this.getSignature();
            }

            if (timeout) {
                g2.drawString("Timeout.", x1 + 5, y1 + 15);
            } else {
                if (image != null) {
                    if (!bScale) {
                        g2.drawImage(image, x1, y1, null);
                    } else {
                        g2.drawImage(image, x1, y1, getActiveRegionContext().getWidth(), getActiveRegionContext().getHeight(), null, null);
                    }
                }
                if (creating || waiting) {
                    g2.setColor(new Color(255, 255, 255, 150));
                    g2.fillRect(0, 0, getActiveRegionContext().getWidth(), getActiveRegionContext().getHeight());
                    g2.setColor(Color.BLACK);
                    g2.drawString("Image is being generated...", x1 + 5, y1 + 15);
                }
            }
        } else {
            if (image != null) {
                image.flush();
                image = null;
            }
        }
    }

    protected String getSignature() {
        ActiveRegionContext r = getActiveRegionContext();
        if (r.isAdjusting()) {
            return this.strOldSignature;
        } else {
            String strProperties = r.getWidgetPropertiesString(true);
            String strText = r.getWidgetItemText();
            return strProperties + ";" + strText + ";" + r.getWidth() + ";" + r.getHeight();
        }
    }
}
