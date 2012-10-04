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

public class HistogrammBubbleChartAbsolute {
	
	static String[] statdataBG;
	static String[] statdataFG;
	
	static String[] statdataDeep = new String[]{
		"enlarge buildings;10;17;5;1;3;5;3;3;2;2;4;1;1;4;2",
		"simplification;5;18;15;8;13;7;9;6;7;4;5;6;5;2;3",
		"rectify corners;4;13;8;4;4;4;5;5;10;8;5;7;4;4;1",
		"displacement;22;18;25;34;17;24;15;22;17;15;13;6;7;5;7",
		"building typification;20;19;16;21;26;22;20;14;10;7;5;3;1;2;0",
		"aggregate buildings;6;5;9;10;4;7;3;2;1;1;0;3;2;2;3",
		"shrink buildings;23;5;13;12;18;12;16;14;12;11;8;10;8;4;6",
		"compress partition;9;4;5;5;9;9;12;13;12;9;8;10;9;9;5"
	};
	
	static String[] statdataHc = new String[]{
		"enlarge buildings;5;3;2;2;3;4;1;1;0;0;0;0;0;0;0",
		"simplification;neun;3;4;7;4;2;0;0;0;0;0;0",
		"rectify corners;27;9;4;4;4;1;2;0;0;0;0;0;0;0;0",
		"displacement;41;34;17;17;22;21;11;5;4;4;3;2;2;1;2",
		"building typification;9;20;24;15;10;5;3;0;1;0;0;0;0;0;0",
		"aggregate buildings;5;5;8;4;3;2;0;0;0;0;0;0;0;0;0",
		"shrink buildings;1;3;6;5;9;4;2;2;0;0;0;0;0;0;0",
		"compress partition;1;4;7;8;6;7;10;8;5;6;4;4;2;2;1"
	};
	
	static String[] statdataSa = new String[]{
		"enlarge buildings;13;5;5;9;4;4;5;3;5;3;2;3;3;2;1",
		"simplification;12;14;15;16;13;8;4;4;8;5;4;0;1;1;0",
		"rectify corners;22;15;15;12;7;4;7;2;6;5;3;3;1;1;0",
		"displacement;21;28;24;17;25;20;21;17;7;6;3;4;3;3;1",
		"building typification;12;18;16;19;14;20;8;8;4;1;2;0;0;0;1",
		"aggregate buildings;7;6;4;1;2;1;1;3;0;1;0;0;0;0;0",
		"shrink buildings;7;6;8;6;9;10;5;6;0;2;1;3;1;0;0",
		"compress partition;5;4;4;7;6;7;12;8;7;4;5;3;4;2;2"
	};
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		statdataBG = statdataHc;
		statdataFG = statdataSa;
		//statdataFG = statdataDeep;
		new HistogrammBubbleChartAbsolute();
	}
	
	public HistogrammBubbleChartAbsolute() {
		JFrame visframe = new JFrame("Operator Position Visualisation");
		
		//visframe.setJMenuBar(menubar);
    	Container viscontainer = visframe.getContentPane();
    	JPanel vispanel = new HistogrammBubbleChartPanelAbsolute();
    	//vispanel.addMouseListener(new VisContainterMouseListener());
    	viscontainer.add(vispanel);
    	
        visframe.setSize(new Dimension(800,400));
        //visframe.setTitle(dialog.getLayer("buildings").getName());
    	visframe.setVisible(true);
		
	}

}


class HistogrammBubbleChartPanelAbsolute extends JPanel {
	public HistogrammBubbleChartPanelAbsolute() {
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
	    int actDiameterBG, actDiameterFG;
	    String[] linepartsBG, linepartsFG;
	    for(int l=0; l < HistogrammBubbleChartAbsolute.statdataBG.length; l++) {
	    	linepartsBG = HistogrammBubbleChartAbsolute.statdataBG[l].split(";");
	    	linepartsFG = HistogrammBubbleChartAbsolute.statdataFG[l].split(";");
	    	actX = 30;
	    	g2d.drawString(linepartsBG[0]+": ", actX, actY);
	    	
	    	actX += 170;
	    	g2d.setPaint(Color.LIGHT_GRAY);
	    	for(int i=1; i<linepartsBG.length; i++) {
	    		actDiameterBG = Integer.parseInt(linepartsBG[i]);
	    		g2d.fillOval(actX-(actDiameterBG/2), actY-(actDiameterBG/2), actDiameterBG, actDiameterBG);
	    		actX += 40;
	    	}
	    	
	    	actX = 200;
	    	g2d.setPaint(Color.BLACK);
	    	for(int i=1; i<linepartsFG.length; i++) {
	    		actDiameterFG = Integer.parseInt(linepartsFG[i]);
	    		g2d.drawOval(actX-(actDiameterFG/2), actY-(actDiameterFG/2), actDiameterFG, actDiameterFG);
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
