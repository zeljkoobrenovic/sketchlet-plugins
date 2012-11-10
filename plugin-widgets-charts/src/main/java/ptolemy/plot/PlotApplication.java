/* A standalone plot application.

 Copyright (c) 1998-2010 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package ptolemy.plot;

import com.microstar.xml.XmlException;
import ptolemy.plot.plotml.PlotBoxMLParser;
import ptolemy.plot.plotml.PlotMLParser;
import ptolemy.util.StringUtilities;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

///////////////////////////////////////////////////////////////////
//// PlotApplication

/**
 * PlotApplication is a versatile two-dimensional data plotter application.
 * It can read files compatible with the Ptolemy plot
 * file format (currently only ASCII).  For a description of the file
 * format, see the Plot and PlotBox classes.
 * Command-line options include:
 * <dl>
 * <p/>
 * <dt><code>-help</code></a>
 * <dt>Print the usage, including all command-line options
 * that exist for backward compatibility and then exit.
 * The help menu choice will display the same information.
 * <dt><code>-test</code></a>
 * <dt>Display the plot, then exit after 2 seconds.
 * <p/>
 * <dt><code>-version</code></a>
 * <dt>Print the program version and then exit.
 * While ptplot is running,
 * the version menu choice will display the same information.
 * <dt><code>-</code></a>
 * <dt>Read the plot data from standard in.
 * </dl>
 * <p/>
 * <p/>
 * For compatibility with historical applications, this application has
 * a limited ability to read pxgraph files.  The command line arguments
 * must be used, and the options that are understood are exactly those
 * of the pxgraph application, plus some more to allow for cross-platform
 * reading.  It is not possible to read pxgraph files
 * using the "Open" menu command (because of the cross-platform problems).
 * The additional command-line arguments are:
 * <dl>
 * <p/>
 * <dt><code>-bigendian</code></a>
 * <dt>Data files are in big-endian, or network binary format.
 * If you are on a little-endian machine, such as a machine
 * with an Intel x86 chip, and you would like to read a binary
 * format file created on a big-endian machine, such as a Sun SPARC,
 * use the <code>-bigendian</code> flag.
 * <p/>
 * <dt><code>-littleendian</code></a>
 * <dt>Data files are in little-endian, or x86 binary format.
 * If you are on a big-endian machine, such as a Sun Sparc,
 * and you would like to read a binary
 * format file created on a little-endian machine, such as Intel x86
 * machine, then use the <code>-littleendian</code> flag.
 * </dl>
 * <p/>
 * To compile and run this application, do the following:
 * <pre>
 * javac -classpath ../.. PlotApplication.java
 * java -classpath ../.. ptolemy.plot.PlotApplication
 * </pre>
 * <p/>
 * This assumes a particular directory structure.  If this is not what you
 * have, then alter the above accordingly.
 *
 * @author Christopher Brooks and Edward A. Lee
 * @version $Id: PlotApplication.java 57040 2010-01-27 20:52:32Z cxh $
 * @Pt.ProposedRating Yellow (cxh)
 * @Pt.AcceptedRating Yellow (cxh)
 * @see Plot
 * @see PlotBox
 * @since Ptolemy II 0.2
 */
public class PlotApplication extends PlotFrame {
    /**
     * Construct a plot with no command-line arguments.
     * It initially displays a sample plot.
     *
     * @throws Exception Not thrown in this base class.
     */
    public PlotApplication() throws Exception {
        this(new String[0]);
    }

    /**
     * Construct a plot with the specified command-line arguments.
     *
     * @param args The command-line arguments.
     * @throws Exception If command line arguments have problems.
     */
    public PlotApplication(String[] args) throws Exception {
        this(new Plot(), args);
    }

    /**
     * Construct a plot with the specified command-line arguments
     * and instance of plot.  If there are no command-line arguments,
     * then display a sample plot.
     *
     * @param plot The instance of Plot to use.
     * @param args The command-line arguments.
     * @throws Exception If command line arguments have problems.
     */
    public PlotApplication(PlotBox plot, String[] args) throws Exception {
        // invoke the base class constructor and pass in the argument a Plot
        // object. This makes sure that the plot field is an instance of
        // Plot class.
        super("PlotApplication", plot);

        _parseArgs(args);

        if ((args == null) || (args.length == 0)) {
            samplePlot();
        }

        String str = "TitleText: Software Downloads\n" +
                "XRange: 0,10\n" +
                "YRange: 0,10000\n" +
                "# Manually specify X ticks\n" +
                "# Note that the 0 and 10 point result in clipping of the rectangles.\n" +
                "XTicks: 1993 0, 1994 1, 1995 2, 1996 3, 1997 4, 1998 5, 1999 6, 2000 7, 2001 8, 2002 9, 2003 10\n" +
                "XLabel: Year\n" +
                "YLabel: Downloads\n" +
                "Marks: none\n" +
                "Lines: off\n" +
                "# Width and offset of bars\n" +
                "Bars: 0.5, 0.2\n" +
                "NumSets: 3\n" +
                "\n" +
                "DataSet: program a\n" +
                "0, 100\n" +
                "1, 300\n" +
                "2, 600\n" +
                "3, 1000\n" +
                "4, 4000\n" +
                "5, 6000\n" +
                "6, 3000\n" +
                "7, 1000\n" +
                "8, 400\n" +
                "9, 0\n" +
                "10, 0\n" +
                "\n" +
                "DataSet: program b\n" +
                "0, 0\n" +
                "1, 0\n" +
                "2, 50\n" +
                "3, 100\n" +
                "4, 800\n" +
                "5, 400\n" +
                "6, 1000\n" +
                "7, 5000\n" +
                "8, 2000\n" +
                "9, 300\n" +
                "10, 0\n" +
                "\n" +
                "DataSet: program c\n" +
                "0, 0\n" +
                "1, 0\n" +
                "2, 0\n" +
                "3, 10\n" +
                "4, 100\n" +
                "5, 400\n" +
                "6, 2000\n" +
                "7, 5000\n" +
                "8, 9000\n" +
                "9, 7000\n" +
                "10, 1000\n";

        _read(null, new ByteArrayInputStream(str.getBytes()));

        //setVisible(true);

        if (_printPDF) {
            _printPDF();

        }
    }

    public PlotApplication(PlotBox plot, String code, int w, int h) throws Exception {
        // invoke the base class constructor and pass in the argument a Plot
        // object. This makes sure that the plot field is an instance of
        // Plot class.
        super("PlotApplication", plot);

        code = code.trim();

        if (code.startsWith("<?xml") || code.startsWith("<plot")) {
            PlotBoxMLParser parser = _newParser();
            if (code.startsWith("<?xml")) {
                // file is an XML file.
                int indexOfDotType = code.indexOf("<!DOCTYPE");

                if (indexOfDotType > 0) {
                    int indexOfDocTypeEnd = code.indexOf(">", indexOfDotType + 1);
                    if (indexOfDocTypeEnd > 0) {
                        code = code.substring(0, indexOfDotType)
                                + "<!DOCTYPE plot SYSTEM \"http://ptolemy.berkeley.edu/java/ptplot/standalone/plotml.dtd\">"
                                + code.substring(indexOfDocTypeEnd + 1);
                    }
                } else {
                    int indexOfXmlDeaderEnd = code.indexOf(">", indexOfDotType + 1);
                    code = code.substring(0, indexOfXmlDeaderEnd + 1)
                            + "<!DOCTYPE plot SYSTEM \"http://ptolemy.berkeley.edu/java/ptplot/standalone/plotml.dtd\">"
                            + code.substring(indexOfXmlDeaderEnd + 1);
                }
            } else if (code.startsWith("<plot")) {
                code = "<?xml version=\"1.0\" standalone=\"no\"?>\n"
                        + "<!DOCTYPE plot SYSTEM \"http://ptolemy.berkeley.edu/java/ptplot/standalone/plotml.dtd\">"
                        + code;
            }
            ByteArrayInputStream bin = new ByteArrayInputStream(code.getBytes());
            try {
                parser.parse(null, bin);
            } catch (Exception ex) {
                String msg;

                if (ex instanceof XmlException) {
                    XmlException xmlex = (XmlException) ex;
                    msg = "PlotMLApplication: failed to parse PlotML data:\n"
                            + "line: " + xmlex.getLine() + ", column: "
                            + xmlex.getColumn() + "\nIn entity: "
                            + xmlex.getSystemId() + "\n";
                } else {
                    msg = "PlotMLApplication: failed to parse PlotML data:\n";
                }

                System.err.println(msg + ex.toString());
                ex.printStackTrace();
            }
        } else {
            ByteArrayInputStream bin = new ByteArrayInputStream(code.getBytes());
            _read(null, bin);
        }

        //setVisible(true);

        bim = plot.exportImage(new Rectangle(w, h));
    }

    private BufferedImage bim;

    /**
     * Display the given plot.  Unlike the two argument constructor,
     * this does not take command-line arguments, and does not fill
     * the plot with a sample plot.
     *
     * @param plot The instance of Plot to display.
     */
    public PlotApplication(PlotBox plot) {
        // Invoke the base class constructor and pass in the argument a Plot
        // object. This makes sure that the plot field is an instance of
        // Plot class.
        super("PlotApplication", plot);

        // Handle window closing by exiting the application.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                // Strangely, calling _close() here sends javac into
                // an infinite loop (in jdk 1.1.4).
                //              _close();
                System.exit(0);
            }
        });

        setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Create a new plot window and map it to the screen.
     */
    public static void main(final String[] args) {
        try {
            // Run this in the Swing Event Thread.
            Runnable doActions = new Runnable() {
                public void run() {
                    try {
                        String str = "TitleText: Software Downloads\n" +
                                "XRange: 0,10\n" +
                                "YRange: 0,10000\n" +
                                "# Manually specify X ticks\n" +
                                "# Note that the 0 and 10 point result in clipping of the rectangles.\n" +
                                "XTicks: 1993 0, 1994 1, 1995 2, 1996 3, 1997 4, 1998 5, 1999 6, 2000 7, 2001 8, 2002 9, 2003 10\n" +
                                "XLabel: Year\n" +
                                "YLabel: Downloads\n" +
                                "Marks: none\n" +
                                "Lines: off\n" +
                                "# Width and offset of bars\n" +
                                "Bars: 0.5, 0.2\n" +
                                "NumSets: 3\n" +
                                "\n" +
                                "DataSet: program a\n" +
                                "0, 100\n" +
                                "1, 300\n" +
                                "2, 600\n" +
                                "3, 1000\n" +
                                "4, 4000\n" +
                                "5, 6000\n" +
                                "6, 3000\n" +
                                "7, 1000\n" +
                                "8, 400\n" +
                                "9, 0\n" +
                                "10, 0\n" +
                                "\n" +
                                "DataSet: program b\n" +
                                "0, 0\n" +
                                "1, 0\n" +
                                "2, 50\n" +
                                "3, 100\n" +
                                "4, 800\n" +
                                "5, 400\n" +
                                "6, 1000\n" +
                                "7, 5000\n" +
                                "8, 2000\n" +
                                "9, 300\n" +
                                "10, 0\n" +
                                "\n" +
                                "DataSet: program c\n" +
                                "0, 0\n" +
                                "1, 0\n" +
                                "2, 0\n" +
                                "3, 10\n" +
                                "4, 100\n" +
                                "5, 400\n" +
                                "6, 2000\n" +
                                "7, 5000\n" +
                                "8, 9000\n" +
                                "9, 7000\n" +
                                "10, 1000\n";
                        ImageIO.write(getImage(str, 300, 150), "png", new File("c:/temp/temp.png"));
                    } catch (Exception ex) {
                        System.err.println(ex.toString());
                        ex.printStackTrace();
                    }
                }
            };

            SwingUtilities.invokeAndWait(doActions);
        } catch (Exception ex) {
            System.err.println(ex.toString());
            ex.printStackTrace();
        }

        // If the -test arg was set, then exit after 2 seconds.
        if (_test) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }

            System.exit(0);
        }
    }

    public static BufferedImage getImage(String content, int w, int h) {
        try {
            return new PlotApplication(new Plot(), content, w, h).bim;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    protected void _about() {
        JOptionPane
                .showMessageDialog(
                        this,
                        "PlotApplication class\n"
                                + "By: Edward A. Lee "
                                + "and Christopher Brooks\n"
                                + "Version "
                                + PlotBox.PTPLOT_RELEASE
                                + ", Build: $Id: PlotApplication.java 57040 2010-01-27 20:52:32Z cxh $\n\n"
                                + "For more information, see\n"
                                + "http://ptolemy.eecs.berkeley.edu/java/ptplot\n\n"
                                + "Copyright (c) 1997-2010, "
                                + "The Regents of the University of California.",
                        "About Ptolemy Plot", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Exit the application.
     */
    protected void _close() {
        StringUtilities.exit(0);
    }

    /**
     * Display more detailed information than given by _about().
     */
    protected void _help() {
        JOptionPane.showMessageDialog(this,
                "PlotApplication is a standalone plot " + " application.\n"
                        + "  File formats understood: Ptplot ASCII.\n"
                        + "  Left mouse button: Zooming.\n\n" + _usage(),
                "About Ptolemy Plot", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Parse the command-line
     * arguments and make calls to the Plot class accordingly.
     *
     * @return The number of arguments read.
     * @throws CmdLineArgException   If a command line argument cannot
     *                               be parsed.
     * @throws FileNotFoundException If an input file cannot be found.
     * @throws IOException           If there is a problem reading an input.
     */
    protected int _parseArgs(String[] args) throws CmdLineArgException,
            FileNotFoundException, IOException {
        int i = 0;
        int argumentsRead;
        String arg;
        String title = "Ptolemy plot";

        int width = 500; // Default width of the graph
        int height = 300; // Default height of the graph

        while ((args != null) && (i < args.length)) {
            arg = args[i++];

            if (arg.equals("-height")) {
                if (i > (args.length - 1)) {
                    throw new CmdLineArgException(_usage());
                }

                height = Integer.valueOf(args[i++]).intValue();
                continue;
            } else if (arg.equals("-help")) {
                System.out.println(_usage());
                StringUtilities.exit(0);
                continue;
            } else if (arg.equals("-printPDF")) {
                _printPDF = true;
                continue;
            } else if (arg.equals("-test")) {
                _test = true;
                continue;
            } else if (arg.equals("-version")) {
                System.out
                        .println("Version "
                                + PlotBox.PTPLOT_RELEASE
                                + ", Build $Id: PlotApplication.java 57040 2010-01-27 20:52:32Z cxh $");
                StringUtilities.exit(0);
                continue;
            } else if (arg.equals("-width")) {
                if (i > (args.length - 1)) {
                    throw new CmdLineArgException(
                            "-width requires an integer argument");
                }

                width = Integer.valueOf(args[i++]).intValue();
                continue;
            } else if (arg.equals("")) {
                // Ignore blank argument.
            } else if (arg.equals("-")) {
                // read from standard input
                URL base = new URL("file", null, "standard input");
                _read(base, System.in);
            } else if (!arg.startsWith("-")) {
                // Have a filename.  First attempt to open it as a URL.
                InputStream instream;
                URL base;

                try {
                    // First argument is null because we are only
                    // processing absolute URLs this way.  Relative
                    // URLs are opened as ordinary files.
                    URL inurl = new URL(null, arg);
                    base = inurl;
                    instream = inurl.openStream();
                } catch (MalformedURLException ex) {
                    File file = new File(arg);
                    instream = new FileInputStream(file);
                    _file = new File(file.getAbsolutePath());
                    title = _file.getName();
                    _directory = _file.getParentFile();
                    base = new URL("file", null, _directory.getAbsolutePath());
                }

                _read(base, instream);
            } else {
                // Unrecognized option.
                throw new CmdLineArgException("Unrecognized option: " + arg);
            }
        }

        setSize(width, height);
        setTitle(title);

        argumentsRead = i;

        return argumentsRead;
    }

    /**
     * Return a string summarizing the command-line arguments.
     *
     * @return A usage string.
     */
    protected String _usage() {
        // We use a table here to keep things neat.
        // If we have:
        //  {"-bd",  "<color>", "Border",  "White", "(Unsupported)"},
        // -bd       - The argument
        // <color>   - The description of the value of the argument
        // Border    - The Xgraph file directive (not supported at this time).
        // White     - The default (not supported at this time)
        // "(Unsupported)" - The string that is printed to indicate if
        //                   a option is unsupported.
        String[][] commandOptions = {{"-height", "<pixels>"},
                {"-width", "<pixels>"},};

        String[] commandFlags = {"-help", "-printPDF", "-test", "-version",
                "-",};
        StringBuffer result = new StringBuffer(
                "Usage: ptplot [ options ] [file ...]\n\n"
                        + "Options that take values:\n");

        int i;

        for (i = 0; i < commandOptions.length; i++) {
            result.append(" " + commandOptions[i][0] + " "
                    + commandOptions[i][1] + "\n");
        }

        result.append("\nBoolean flags:\n");

        for (i = 0; i < commandFlags.length; i++) {
            result.append(" " + commandFlags[i]);
        }

        return result.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**
     * If true, then print to PDF.
     */
    protected static boolean _printPDF = false;

    /**
     * If true, then auto exit after a few seconds.
     */
    protected static boolean _test = false;

    protected PlotBoxMLParser _newParser() {
        if (plot instanceof Plot) {
            return new PlotMLParser((Plot) plot);
        } else {
            return new PlotBoxMLParser(plot);
        }
    }
}
