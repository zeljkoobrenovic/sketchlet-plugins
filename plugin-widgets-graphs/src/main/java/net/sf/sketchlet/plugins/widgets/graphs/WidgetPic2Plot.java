/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.widgets.graphs;

import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.context.SketchletGraphicsContext;
import net.sf.sketchlet.uml.ExternalPrograms;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zobrenovic
 */
public class WidgetPic2Plot extends ExternalImageProgramGeneratorWidget {
    private double scale = 1.34;
    private String cmdLineParams = "";

    public WidgetPic2Plot(ActiveRegionContext region) {
        super(region);
    }

    @Override
    public void callImageGenerator() {
        if (getActiveRegionContext() == null) {
            return;
        }
        String strPic = getPicText();
        if (getActiveRegionContext() == null) {
            return;
        }
        int w = getActiveRegionContext().getWidth();
        int h = getActiveRegionContext().getHeight();
        File picFile = null;
        try {
            picFile = File.createTempFile("pic2plot", ".pic");
            FileUtils.saveFileText(picFile, strPic);

            List<String> plotutilsParams = new ArrayList<String>();

            plotutilsParams.add(ExternalPrograms.getPlotUtilsPath());
            plotutilsParams.add("-T");
            plotutilsParams.add("svg");
            String params[] = getCmdLineParams().split(" ");
            for (String param : params) {
                if (!param.trim().isEmpty()) {
                    plotutilsParams.add(param.trim());
                }
            }
            plotutilsParams.add(picFile.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(plotutilsParams.toArray(new String[plotutilsParams.size()]));

            processBuilder.directory(new File(ExternalPrograms.getPlotUtilsPath()).getParentFile());
            Process theProcess = processBuilder.start();
            BufferedReader bri = new BufferedReader(new InputStreamReader(theProcess.getInputStream()));
            String strSVG = "";
            String line;
            while ((line = bri.readLine()) != null) {
                strSVG += line + "\n";
            }
            bri.close();
            theProcess.waitFor();

            this.setImage(getImageFromSVGCode(strSVG, w, h, this.getImage()));
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (picFile != null) {
                picFile.delete();
            }
        }
    }

    protected String getPicText() {
        return getActiveRegionContext().getWidgetItemText();
    }

    private String replaceTagValue(String code, String tag, String value) {
        String start = tag + "=\"";
        int n1 = code.indexOf(start);
        int n2 = code.indexOf("\"", n1 + start.length() + 1);
        if (n1 > 0 && n2 > n1) {
            code = code.substring(0, n1) + tag + "=\"" + value + "\"" + code.substring(n2 + 1);
        }
        return code;
    }

    private BufferedImage getImageFromSVGCode(String strSVG, int w, int h, BufferedImage oldImage) {
        BufferedImage image = null;
        strSVG = strSVG.replace("fill-rule:even-odd;", "");
        strSVG = strSVG.replace("fill-rule:even-odd", "");
        strSVG = replaceTagValue(strSVG, "width", "" + w + "px");
        strSVG = replaceTagValue(strSVG, "height", "" + h + "px");

        int n1 = strSVG.indexOf("<g ");
        int n2 = strSVG.lastIndexOf("</g>");
        if (n1 > 0 && n2 > n1) {
            double t = (1 - getScale()) / 2;
            String strT = "<g transform=\"translate(" + t + "," + t + ") scale(" + getScale() + "," + getScale() + ")\">\n";
            strSVG = strSVG.substring(0, n1) + strT + strSVG.substring(n1, n2) + "</g>\n" + strSVG.substring(n2);
        }

        strSVG = strSVG.replace("<rect x=\"0\" y=\"0\" width=\"1\" height=\"1\" style=\"stroke:none;fill:white;\"/>", "");
        strSVG = replaceTagValue(strSVG, "preserveAspectRatio", "xMidYMid");

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
            g2.drawString("Pic2Plot SVG ERROR ", 10, 30);
            g2.dispose();
            e.printStackTrace();
            return image;
        }

        BufferedImage img = SketchletGraphicsContext.getInstance().createCompatibleImage(w, h, oldImage);
        Graphics2D g2 = img.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        image.flush();

        return img;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public String getCmdLineParams() {
        return cmdLineParams;
    }

    public void setCmdLineParams(String cmdLineParams) {
        this.cmdLineParams = cmdLineParams;
    }

    static class BufferedImageTranscoder extends ImageTranscoder {

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
}
