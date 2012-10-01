package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
import java.util.Iterator;

import ch.unizh.geo.algorithms.snakes.SnakesSmoothingLineNew;
import ch.unizh.geo.geomutilities.InterpolateLinePoints;
import ch.unizh.geo.measures.TAFandCurvature;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;

/**
 * @descrption:
 * 		smoothes lines and polygons with a snakes algorithm 
 * 		needed parameters: 
 * 			FeatureCollection => Feature => Geometry  : LineString or Polygon  
 * 			tolerance : maximum displacement
 * 			segmentate : should the line be segmentated
 * 		further params initilized in this file:
 * 			start params Snakes: alpha und beta (init = 1)
 * 			segmentation value: curvature (init = Pi/3)
 * 			
 * @author sstein
 *
 * 
 */
public class LineSmoothing extends AWebGenAlgorithm implements IWebGenAlgorithm  {

    private double segmentCurvThreshold = Math.PI/3;
    double alpha = 1;
    double beta = 1;
    double minPoints = 6;
	
    public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		double tolerance = wgreq.getParameterDouble("tolerance");
		boolean segmentate = wgreq.getParameterBoolean("segmentation");
		try {
			FeatureCollection fcnew = smoothFC(fc, tolerance, segmentate);
			wgreq.addResult("result", fcnew);
		}
		catch(Exception e) {}
	}

	
	public FeatureCollection smoothFC(FeatureCollection fc, double tolerance, boolean doSegmentation) throws Exception {
	    System.gc(); //flush garbage collector
		FeatureCollection fcnew = new FeatureDataset(fc.getFeatureSchema());
		for (Iterator i = fc.iterator(); i.hasNext();) {
		    Geometry newgeo;
            Feature f = (Feature)((Feature)i.next()).clone();
            newgeo = this.smoothSingle(f.getGeometry(),tolerance, doSegmentation);
            f.setGeometry(newgeo);
            fcnew.add(f);
        }
		return fcnew;
	}
	
	private Geometry smoothSingle(Geometry geom, double maxDisp, boolean segmentate) throws Exception{
	    	Geometry resultgeom = null;
	    	int geomType = 0;
	    	int polyRing = 0;	    	
	   		LineString line = null;
	   		Polygon poly = null;
	   		if(geom instanceof LineString){
	   			line = (LineString)geom;
	   			geomType = 1;
	   		}
	   		else if(geom instanceof Polygon){
	   			poly = (Polygon)geom;
	   			line = poly.getExteriorRing();
	   			geomType = 2;
	   			polyRing = 0;
	   		}
	      	else{
	      		geomType = 0;	      		
	      	}
		    /****************************************/
	       	if (geomType > 0){
      			if(line.getNumPoints() <= this.minPoints){
      				line = InterpolateLinePoints.addMiddlePoints(line);
      				System.out.println("LineSmoothSimpleVersion.smooth1: to short " +
      						"line found with" + line.getNumPoints() + "points. Points added");
      			}
		       	int[] pointidx = null;       	
		       	if(segmentate==true){       	    
		       	    System.out.println("segmentation  = true");
		       	 System.out.println("angle criteria in rad: " + this.segmentCurvThreshold);
		       	    pointidx = this.calcSegments(line, this.segmentCurvThreshold); 
		       	}
		       	else{
		       	    System.out.println("segmentation  = false");
		       	}
		       	//-- smoothing
		   	    SnakesSmoothingLineNew ssmooth = new SnakesSmoothingLineNew(line, maxDisp, this.alpha, this.beta, segmentate, pointidx);
		   	    LineString result = ssmooth.getSmoothedLine();	   	    
		   	    //-- update geometry --------
		   	    if (geomType == 1){	//linestring
		   	   	    Coordinate[] coords =line.getCoordinates();
		   	   	    for (int j=0; j < coords.length; j++){
		   	   	    		coords[j] = result.getCoordinateN(j);
		   	   	    }
		   	   	    resultgeom = line;
		   	    }
		   	    else if (geomType == 2){ //polygon
		   	    	LineString extring = poly.getExteriorRing(); 
		   	   	    Coordinate[] coords =extring.getCoordinates();
		   	   	    for (int j=0; j < coords.length; j++){
			   	   	    		coords[j] = result.getCoordinateN(j);
		   	   	    }	   	   	    
		   	   	    //-- smooth innner rings if exists, and update as well
		   	    	if (poly.getNumInteriorRing() > 0){
		   	    		for(int j=0; j < poly.getNumInteriorRing(); j++){
		   	    			line = poly.getInteriorRingN(j);
		   	      			if(line.getNumPoints() <= this.minPoints){
		   	      				line = InterpolateLinePoints.addMiddlePoints(line);
		   	      				System.out.println("LineSmoothSimpleVersion.smooth1: to short " +
		   	      						"line found with" + line.getNumPoints() + "points. Points added");
		   	      			}		   	    			
			   	 	       	pointidx = null;       	
			   		       	if(segmentate==true){       	    
			   		       	    //System.out.println("segmentation  = true");
			   		       	    pointidx = this.calcSegments(line, this.segmentCurvThreshold); 
			   		       	}
			   		       	else{
			   		       	    //System.out.println("segmentation  = false");
			   		       	}
			   		       	//-- smoothing	   	    			
		   	    	   	    ssmooth = new SnakesSmoothingLineNew(line, maxDisp, this.alpha, this.beta, segmentate, pointidx);
		   	    	   	    result = ssmooth.getSmoothedLine();
		   	    	   	    coords =line.getCoordinates();
		   	    	   	    for (int u=0; u < coords.length; u++){
		   	 	   	   	    		coords[u] = result.getCoordinateN(u);
		   	    	   	    }
		   	    		}
		   	    	}
		   	    	resultgeom = poly;
		   	    }	   	     
			    String mytext = "item : smoothing finalized";
			    if (ssmooth.isCouldNotSmooth() == true){mytext = "item: not smoothed since to small threshold!!!";}
			    //context.getWorkbenchFrame().setStatusMessage(mytext);
			    //System.out.println(mytext);
	       	}//end if : polygon or linestring
        return resultgeom;        
	}

	private int[] calcSegments(LineString line, double segmentationValue){
	    TAFandCurvature myTafCalc = new TAFandCurvature(line);
	    double[] curv = myTafCalc.getCurvature();
	    ArrayList myPoints = new ArrayList();
	    System.out.println("curvature values:");
	    for(int j=0; j < curv.length; j++){	        
	        System.out.print(curv[j] + ", ");
	        if ( Math.abs(curv[j]) > segmentationValue){
	        	if ((j > 0) && (j < curv.length-1)){
	        		myPoints.add(new Integer(j));
	        	}
	        }
	    }
	    System.out.println(" ");
	    System.out.println("no. of segmentation points: " + myPoints.size());
	    int[] pointIdxList= new int[myPoints.size()];
	    int j=0;
	    for (Iterator iter = myPoints.iterator(); iter.hasNext();) {
	        Integer element = (Integer) iter.next();            
	        pointIdxList[j] = element.intValue();
	        System.out.print(element.intValue() + "  ");
	        j=j+1;
	    }
	    System.out.println("  ");
	    return pointIdxList;
	}
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("LineSmoothing", "sstein", "operator",
				"",
				"LineSmoothing",
				"Line Smoothing",
				"1.0",
				new String[] {"ica.genops.cartogen.Enhancement"});
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"LineString"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("tolerance", "DOUBLE", "10.0", "tolerance");
		id.addInputParameter("segmentation", "BOOLEAN", "false", "segmentation");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "smoothed lines");
		return id;
	}
}
