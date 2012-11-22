package net.sf.sketchlet.plugins.widgets;

import net.sf.sketchlet.context.SketchletGraphicsContext;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import java.awt.image.BufferedImage;

/**
 * Created with IntelliJ IDEA.
 * User: zeljko
 * Date: 22-11-12
 * Time: 10:14
 * To change this template use File | Settings | File Templates.
 */
public class BufferedImageTranscoder extends ImageTranscoder {

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
