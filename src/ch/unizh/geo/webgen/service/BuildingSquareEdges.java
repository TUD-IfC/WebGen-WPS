package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.algorithms.polygons.BuildingSquaring;
import ch.unizh.geo.constraints.buildings.BuildingSquareness;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;


public class BuildingSquareEdges extends AWebGenAlgorithm implements IWebGenAlgorithm  {

    //private double flexibility = 10.0; //percent
    //private boolean solveIter = true;
    
    public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		double maxAngle = wgreq.getParameterDouble("maxChangeOfWallAngleInDeg");
		double maxMove = wgreq.getParameterDouble("maxMovementOfEdgeInM");
		try {
			FeatureCollection fcnew = square(fc, maxAngle, maxMove);
			wgreq.addResult("result", fcnew);
		}
		catch(Exception e) {e.printStackTrace();}
	}
	
	private FeatureCollection square(FeatureCollection features, 
	        double maxAngle, double maxMove) throws Exception{
	    
	    System.gc(); //flush garbage collector
	    // --------------------------	    
	    FeatureCollection newFeatures = new FeatureDataset(features.getFeatureSchema());
	    
	    int count=0; //int noItems = features.size(); Geometry resultgeom = null;
	    //--get single object in selection to analyse
	    for (Iterator iter = features.iterator(); iter.hasNext();) {
	        count++;
	        Feature f = (Feature)iter.next();
            Feature newF = (Feature)f.clone();
	        Geometry geom = newF.getGeometry(); //= erste Geometrie
	        Polygon poly = null;
	        if ( geom instanceof Polygon){
	            poly = (Polygon) geom;
	            // --------------------------
	            //-- tolerance value is set to zero tolerance
	            BuildingSquareness bs = new BuildingSquareness(poly, 0.0);
	            //---
	            if( (bs.getSeverity() > 0)){
	                //context.getWorkbenchFrame().setStatusMessage("conflicts detected!");           	
	                BuildingSquaring squaring = new BuildingSquaring(poly,maxAngle, maxMove);
	                newF.setGeometry(squaring.getOutPolygon());
	                //BuildingSquaring squaring = new BuildingSquaring(poly,this.maxAngle);           	    
	            }       		
	            else{
	                //context.getWorkbenchFrame().setStatusMessage("no conflict detected!");
	            }
	        }
	        else{
	            //context.getWorkbenchFrame().warnUser("item is not a polygon");
	        }
	        //String mytext = "item: " + count + " / " + noItems + " : squaring finalized";
	        //monitor.report(mytext);
	        newFeatures.add(newF);
	    }//end loop for selection	    
	    return newFeatures;        
	}
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("BuildingSquareEdges", "neun", "operator",
				"",
				"BuildingSquareEdges",
				"square building edges",
				"1.0",
				new String[] {"ica.genops.cartogen.Enhancement"});
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with buildings");
		id.addInputParameter("maxChangeOfWallAngleInDeg", "DOUBLE", "20.0", "maxChangeOfWallAngleInDeg");
		id.addInputParameter("maxMovementOfEdgeInM", "DOUBLE", "10.0", "maxMovementOfEdgeInM");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "squared building polygons");
		return id;
	}
	
}