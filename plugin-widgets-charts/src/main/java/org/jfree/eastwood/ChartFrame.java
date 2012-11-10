/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jfree.eastwood;

import java.awt.Font;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import javax.swing.JFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

public class ChartFrame extends JFrame {

	/** The chart panel in which the chart is displayed. */
	private ChartPanel chartPanel;

	/**
	 * Creates a new applet instance.
	 */
	public ChartFrame() {
		this.chartPanel = new ChartPanel(null);
		this.chartPanel.setPopupMenu(null);
		add(this.chartPanel);
        start();
        pack();
        this.setVisible(true);
	}

    public static void main(String args[]) {
        new ChartFrame();
    }

	/**
	 * Starts the applet.
	 */
	public void start() {
	    String chartSpec = "cht=lc&chd=s:9gounjqGJD&chco=008000&chls=2.0,4.0,1.0&chxt=x,y&chxl=0:|Sep|Oct|Nov|Dec|1:||50|100&chs=200x125&chm=R,A0BAE9,0,0.75,0.25|R,ff0000,0,0.1,0.11";

	    try {
    	    Map params = Parameters.parseQueryString(chartSpec);
    	    JFreeChart chart = ChartEngine.buildChart(params, new Font("Dialog", Font.PLAIN, 14));
    	    this.chartPanel.setChart(chart);
	    }
	    catch (UnsupportedEncodingException e) {
	        e.printStackTrace();
	    }
	}

}
