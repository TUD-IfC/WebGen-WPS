package ch.unizh.geo.webgen.test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class HistogrammBubbleChart {
	
	static String[] statdata = new String[]{
			"Area Enlargement;0.444444444;0.111111111;0.111111111;0.055555556;0.055555556;0.166666667;0;0;0;0;0;0;0;0;0.055555556;0;0;0;0;0",
			"Simplification;0.115384615;0.179487179;0.230769231;0.128205128;0.102564103;0.102564103;0.025641026;0.064102564;0.012820513;0.025641026;0;0.012820513;0;0;0;0;0;0;0;0",
			"Rectify;0.509090909;0.127272727;0.109090909;0.127272727;0.072727273;0;0.036363636;0;0.018181818;0;0;0;0;0;0;0;0;0;0;0",
			"Displacement;0.181818182;0.177489177;0.077922078;0.112554113;0.0995671;0.082251082;0.082251082;0.03030303;0.025974026;0.025974026;0.021645022;0.017316017;0.017316017;0.008658009;0.008658009;0.008658009;0.008658009;0.004329004;0.004329004;0.004329004",
			"Typification (10% less);0.051282051;0.230769231;0.256410256;0.153846154;0.102564103;0.102564103;0.038461538;0.038461538;0.012820513;0;0.012820513;0;0;0;0;0;0;0;0;0",
			"Aggregation;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0",
			"Area Shrinking;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0",
			"Compress Partition;0.205128205;0.179487179;0.256410256;0.102564103;0.076923077;0.076923077;0.025641026;0.051282051;0;0;0;0.025641026;0;0;0;0;0;0;0;0"
	};

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new HistogrammBubbleChart();
	}
	
	public HistogrammBubbleChart() {
		JFrame visframe = new JFrame("Operator Position Visualisation");
		
		//visframe.setJMenuBar(menubar);
    	Container viscontainer = visframe.getContentPane();
    	JPanel vispanel = new HistogrammBubbleChartPanel();
    	//vispanel.addMouseListener(new VisContainterMouseListener());
    	viscontainer.add(vispanel);
    	
        visframe.setSize(new Dimension(800,400));
        //visframe.setTitle(dialog.getLayer("buildings").getName());
    	visframe.setVisible(true);
		
	}

}


class HistogrammBubbleChartPanel extends JPanel {
	public HistogrammBubbleChartPanel() {
		this.setBackground(Color.WHITE);
	}
	
	public void paintComponent(Graphics g) {
	    clear(g);
	    Graphics2D g2d = (Graphics2D)g;
	    g2d.setBackground(Color.WHITE);
	    g2d.setPaint(Color.BLACK);
	    //g2d.drawString("Test: ", 100, 100);
	    //g2d.drawOval(200, 100, 10, 10);
	    g2d.setStroke(new BasicStroke(1));
	    
	    int actX;
	    int actY = 40;
	    double actValue; int actDiameter;
	    String[] lineparts;
	    for(String line :  HistogrammBubbleChart.statdata) {
	    	lineparts = line.split(";");
	    	actX = 30;
	    	g2d.drawString(lineparts[0]+": ", actX, actY);
	    	actX += 170;
	    	for(int i=1; i<lineparts.length; i++) {
	    		actValue = Double.parseDouble(lineparts[i]);
	    		actDiameter = (int)Math.floor(80*actValue);
	    		if(actDiameter == 0) actDiameter = 1;
	    		g2d.drawOval(actX-(actDiameter/2), actY-(actDiameter/2), actDiameter, actDiameter);
	    		actX += 40;
	    	}
	    	actY += 40;
	    }
	}
	
	// super.paintComponent clears offscreen pixmap,
	// since we're using double buffering by default.
	protected void clear(Graphics g) {
		super.paintComponent(g);
		}
	  
	  
	// for saving image as png
	public RenderedImage getImage() {
		BufferedImage total = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D gi = total.createGraphics();
		this.paintComponent(gi);
		return total;
		}
}
