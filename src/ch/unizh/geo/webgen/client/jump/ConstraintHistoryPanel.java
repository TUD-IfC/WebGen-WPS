package ch.unizh.geo.webgen.client.jump;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import ch.unizh.geo.webgen.model.CollectionConstraint;
import ch.unizh.geo.webgen.model.ConstrainedFeature;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.model.Constraint;

import com.vividsolutions.jump.feature.Feature;

public class ConstraintHistoryPanel extends JPanel {
	
	static final long serialVersionUID = 12345;
	List fclist;
	String[] names = {"Min Size","Edge Length","Min Dist","Local Width","Diff Pos","Diff EdgeCount","Diff WidthLen","Diff Orientation", "Diff BWRatio"};
	//Color[] colors = {Color.CYAN, Color.GRAY, Color.GREEN, Color.LIGHT_GRAY, Color.MAGENTA, Color.ORANGE, Color.WHITE, Color.YELLOW};
	double[] average;
	public Line2D[][] lines;
	//public int selectedpos = -1;
	public ArrayList<Integer> selectedposs = new ArrayList<Integer>();
	public int actstate = 0;
	public int laststate = 0;
	CollectionConstraint cconstraint;
	
	public ConstraintHistoryPanel(ConstrainedFeatureCollection fc) {
		super();
		this.fclist = fc.getFeatures();
		this.cconstraint = fc.getCollectionConstraint();
		laststate = ((Constraint)((Feature)fclist.get(0)).getAttribute("constraint")).getHistorySize()-1;
		actstate = laststate;
		this.setBackground(Color.BLACK);
		lines = new Line2D[fclist.size()][8];
		//this.setBackground(Color.WHITE);
	}
	
	public void paintComponent(Graphics g) {
	    clear(g);
	    Graphics2D g2d = (Graphics2D)g;
	    g2d.setBackground(Color.WHITE);
	    g2d.setPaint(Color.YELLOW);
	    //g2d.setPaint(Color.BLACK);
	    int xpos, ywpos;
	    for(int i=1; i<=9; i++) {
	    	xpos = (i*100)-60;
	    	g2d.drawString(names[i-1], xpos-5, 40);
	    	g2d.drawLine(xpos,50,xpos,450);
	    	for(int j=0; j<=10; j++) {
	    		ywpos = (j*40)+50;
	    		g2d.drawLine(xpos-2,ywpos,xpos+2,ywpos);
	    	}
	    }
	    g2d.drawString("1.0", 18, 53);
	    g2d.drawString("0.5", 18, 253);
	    g2d.drawString("0.0", 18, 453);
	    
	    //draw history navigation
	    g2d.setPaint(Color.WHITE);
	    g2d.drawString("State: ", 325, 480);
	    g2d.drawRect(370, 470, 12, 12);
	    g2d.drawString("-", 375, 480);
	    g2d.drawString(actstate+"", 400, 480);
	    g2d.drawRect(420, 470, 12, 12);
	    g2d.drawString("+", 423, 480);
	    
	    //draw values:
	    average = new double[9];
	    int xposA=0, xposB=0, yposA=0, yposB=0;
	    double[] tvalues; double[] cvalues;
	    String tmessage = "";
	    g2d.setStroke(new BasicStroke(1));
	    for(int i=0; i<fclist.size(); i++) {
	    	Constraint wgc = ((ConstrainedFeature)fclist.get(i)).getConstraint();
    		//tvalues = wgc.getHistoryLast();
	    	tvalues = wgc.getStateFromHistory(actstate);
	    	tmessage = wgc.getStateMessageFromHistory(actstate);
	    	//if(selectedpos == i) g2d.setStroke(new BasicStroke(4));
	    	if(selectedposs.contains(new Integer(i))) g2d.setStroke(new BasicStroke(4));
	    	g2d.setPaint(new Color(((int)(Math.random()*100)+150),((int)(Math.random()*100)+150),((int)(Math.random()*100)+150)));
	    	for(int j=0; j<tvalues.length-1; j++) {
	    		xposA = ((j+1)*100)-60;
		    	yposA = 450 - ((int)(tvalues[j]*400));
	    		xposB = ((j+2)*100)-60;
	    		yposB = 450 - ((int)(tvalues[j+1]*400));
	    		lines[i][j] = new Line2D.Double(xposA,yposA,xposB,yposB);
	    		g2d.draw(lines[i][j]);
	    		//g2d.drawLine(xposA,yposA,xposB,yposB);
	    		average[j] += tvalues[j];
	    	}
	    	average[tvalues.length-1] += tvalues[tvalues.length-1];
	    	//draw BWRatio
	    	try {cvalues = cconstraint.getStateFromHistory(actstate);}
	    	catch(Exception e) {cvalues = new double[]{0.0};}
	    	int xposC = 840;
    		int yposC = 450 - ((int)(cvalues[0]*400));
    		average[8] = cvalues[0];
    		//create line
	    	lines[i][7] = new Line2D.Double(xposB,yposB,xposC,yposC);
    		g2d.draw(lines[i][7]);
	    	//if(selectedpos == i) g2d.setStroke(new BasicStroke(1));
	    	g2d.setStroke(new BasicStroke(1));
	    }
	    //display wgc message
	    g2d.drawString(tmessage, 450, 480);
	    //display average
	    DecimalFormat df = new DecimalFormat("#0.000");
	    g2d.setPaint(Color.RED);
	    g2d.setStroke(new BasicStroke(4));
	    average[0] = average[0] / fclist.size();
	    for(int i=0; i<average.length-1; i++) {
	    	average[i+1] = average[i+1] / fclist.size();
    		xposA = ((i+1)*100)-60;
	    	yposA = 450 - ((int)(average[i]*400));
    		xposB = ((i+2)*100)-60;
    		yposB = 450 - ((int)(average[i+1]*400));
    		g2d.drawLine(xposA,yposA,xposB,yposB);
    		g2d.drawString(df.format(average[i]), xposA+5, 60);
	    }
	    g2d.drawString(df.format(average[average.length-1]), xposB+5, 60);
	    //System.out.println("drawing finished!");
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
