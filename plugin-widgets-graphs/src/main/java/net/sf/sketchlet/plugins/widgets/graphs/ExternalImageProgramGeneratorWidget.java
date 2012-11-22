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
 * @author zobrenovic
 */
public class ExternalImageProgramGeneratorWidget extends WidgetPlugin {

    private String oldCacheKey = "";
    private BufferedImage image;
    private boolean creating = false;
    private boolean waiting = false;
    private boolean timeout = false;
    private Object lock = new Object();
    private CountDownLatch create = null;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private boolean scaling = false;
    private long lastNanoTime = 0;
    private boolean processing = false;

    public ExternalImageProgramGeneratorWidget(ActiveRegionContext region) {
        super(region);
    }

    protected void callImageGenerator() {
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public BufferedImage getImage() {
        return this.image;
    }

    /**
     * Calls external program and generates image.
     */
    protected void createImage() {
        if (!SketchletContext.getInstance().isInBatchMode()) {
            synchronized (lock) {
                if (waiting) {
                    return;
                }
                if (creating || System.nanoTime() - lastNanoTime < 1000000000) {
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
                        if (isProcessing()) {
                            return;
                        }
                        if (create != null) {
                            while (create.getCount() > 0) {
                                create.countDown();
                            }
                        }
                        create = new CountDownLatch(1);
                        try {
                            setProcessing(true);
                            callImageGenerator();
                        } finally {
                            lastNanoTime = System.nanoTime();
                            creating = false;
                            create.countDown();
                            setProcessing(false);
                            SketchletContext.getInstance().repaint();
                        }
                    }
                });
            } else {
                callImageGenerator();
                lastNanoTime = System.nanoTime();
                creating = false;
                setProcessing(false);
                setTimeout(false);
            }
        }
        if (!SketchletContext.getInstance().isInBatchMode()) {
            SketchletContext.getInstance().repaint();
        }
    }

    /**
     * Returns the widget items text.
     *
     * @return  the widget items text.
     */
    protected String getText() {
        return getActiveRegionContext().getWidgetItemText();
    }

    @Override
    public void paint(Graphics2D g2) {
        String text = getText();
        if (text.isEmpty()) {
            return;
        }
        if (text.length() > 0) {
            int x1 = 0;
            int y1 = 0;

            if (image == null || !oldCacheKey.equals(this.getCacheKey())) {
                createImage();
                oldCacheKey = this.getCacheKey();
            }

            if (isTimeout()) {
                g2.drawString("Timeout.", x1 + 5, y1 + 15);
            } else {
                if (image != null) {
                    if (!isScaling()) {
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

    /**
     * Return the cache key for generated image. If cache key was not changed from
     * previous call, image is returned from cache, and external program is not called.
     *
     * @return  the cache key for image generator
     */
    protected String getCacheKey() {
        ActiveRegionContext r = getActiveRegionContext();
        if (r.isAdjusting()) {
            return this.oldCacheKey;
        } else {
            String strProperties = r.getWidgetPropertiesString(true);
            String strText = r.getWidgetItemText();
            return strProperties + ";" + strText + ";" + r.getWidth() + ";" + r.getHeight();
        }
    }

    public boolean isScaling() {
        return scaling;
    }

    public void setScaling(boolean scaling) {
        this.scaling = scaling;
    }

    public boolean isProcessing() {
        return processing;
    }

    public void setProcessing(boolean processing) {
        this.processing = processing;
    }

    public boolean isTimeout() {
        return timeout;
    }

    public void setTimeout(boolean timeout) {
        this.timeout = timeout;
    }
}
