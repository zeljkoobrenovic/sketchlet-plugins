/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.widgets;

import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.context.SketchletGraphicsContext;
import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.plugin.ImageCachingWidgetPlugin;
import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.plugin.WidgetPluginLinks;
import net.sf.sketchlet.plugin.WidgetPluginTextItems;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.StringReader;

/**
 * @author zobrenovic
 */
@PluginInfo(name = "SVG Renderer", type = "widget", group="HTML, SVG")
@WidgetPluginTextItems(initValue = "<?xml version=\"1.0\" standalone=\"no\"?>\n"
        + "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n"
        + "<svg width=\"100%\" height=\"100%\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\">\n"
        + "<rect x=\"20\" y=\"20\" rx=\"20\" ry=\"20\" width=\"250\" height=\"100\"\n"
        + "style=\"fill:red;stroke:black;stroke-width:5;opacity:0.5\"/>\n"
        + "</svg>")
@WidgetPluginLinks(links = {
        "SVG Tutorial; http://www.w3schools.com/svg/"
})
public class WidgetSVG extends ImageCachingWidgetPlugin {

    private String strPrevInfo = "";

    public WidgetSVG(ActiveRegionContext region) {
        super(region);
    }

    @Override
    public void paintImage(Graphics2D g2) {
        String strText = getActiveRegionContext().getWidgetItemText();
        if (strText.isEmpty()) {
            return;
        }
        String strSVG = getActiveRegionContext().getWidgetItemText();
        if (strSVG.length() > 0) {
            if (strSVG.startsWith("<svg")) {
                strSVG = "<?xml version=\"1.0\" standalone=\"no\"?>\r\n<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\r\n" + strSVG;
            }
            int x1 = 0;
            int y1 = 0;
            int w = getActiveRegionContext().getWidth();
            int h = getActiveRegionContext().getHeight();

            BufferedImage image = getImage(strSVG, w, h, this.getCachedImage());

            g2.drawImage(image, x1, y1, w, h, null, null);
        }
    }

    private BufferedImage getImage(String strSVG, int w, int h, BufferedImage oldImage) {
        BufferedImage image = null;
        try {
            BufferedImageTranscoder t = new BufferedImageTranscoder(w, h);
            TranscoderInput input = new TranscoderInput(new StringReader(strSVG));
            t.setErrorHandler(null);
            t.transcode(input, null);
            image = t.getImage();
        } catch (Throwable e) {
            image = SketchletGraphicsContext.getInstance().createCompatibleImage(w, h);
            Graphics2D g2 = image.createGraphics();
            g2.setColor(Color.RED);
            g2.drawString("SVG ERROR ", 10, 30);
            g2.dispose();
            e.printStackTrace();
            System.err.println(strSVG);
            return image;
        }

        BufferedImage img = SketchletGraphicsContext.getInstance().createCompatibleImage(w, h, oldImage);
        Graphics2D g2 = img.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        image.flush();

        return img;
    }

    @Override
    public boolean isRegionChanged() {
        String strText = getActiveRegionContext().getWidgetItemText();
        strText = VariablesBlackboardContext.getInstance().populateTemplate(strText.trim());
        if (!strText.equals(strPrevInfo)) {
            strPrevInfo = strText;
            return true;
        }
        return super.isRegionChanged();
    }
}

class BufferedImageTranscoder extends ImageTranscoder {

    private BufferedImage image;

    public BufferedImageTranscoder(int width, int height) {
        this.setImageSize(width, height);
    }

    @Override
    public BufferedImage createImage(int width, int height) {
        return SketchletGraphicsContext.getInstance().createCompatibleImage(width, height);
    }

    @Override
    public void writeImage(BufferedImage image, TranscoderOutput output) throws TranscoderException {
        this.image = image;
    }

    public BufferedImage getImage() {
        return image;
    }
}
