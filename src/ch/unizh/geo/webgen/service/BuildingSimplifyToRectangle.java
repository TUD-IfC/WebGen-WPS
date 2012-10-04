package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.algorithms.polygons.BuildingEnlargeToRectangle;
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


public class BuildingSimplifyToRectangle extends AWebGenAlgorithm implements IWebGenAlgorithm  {
	
	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		try {
			FeatureCollection fcnew = changeToRectangle(fc);
			wgreq.addResult("result", fcnew);
		}
		catch(Exception e) {e.printStackTrace();}
	}

	private FeatureCollection changeToRectangle(FeatureCollection features) throws Exception{
	     
	    FeatureCollection newFeatures = new FeatureDataset(features.getFeatureSchema());
	    
	    System.gc(); //flush garbage collector
	    // --------------------------	    
	    
	    int count=0;
	    //--get single object in selection to analyse
      	for (Iterator iter = features.iterator(); iter.hasNext();) {
      		count++;
      		Feature f = (Feature)iter.next();
      		Feature fnew = (Feature)f.clone();
	   		Geometry geom = f.getGeometry(); //= erste Geometrie
	   		Polygon poly = null;
	       	if ( geom instanceof Polygon){
	       		poly = (Polygon) geom; //= erste Geometrie
           	    BuildingEnlargeToRectangle enlarge = new BuildingEnlargeToRectangle(poly);
           	    fnew.setGeometry(enlarge.getOutPolygon());
	       	}
	       	else{
	       	    System.out.println("no polygon selected");
	       	}
		    //String mytext = "item: " + count + " / " + noItems + " : squaring finalized";
		    //monitor.report(mytext);
	       	newFeatures.add(fnew);
      	}// end loop over item selection
        return newFeatures;        
	}
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("BuildingSimplifyToRectangle", "neun", "operator",
				"",
				"BuildingSimplifyToRectangle",
				"simplify buildings to rectangles",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "buildings");
		
		//add output parameters
		//id.addOutputParameter("result", "FeatureCollection");
		id.addOutputParameter("result", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "buffered geometries");
		return id;
	}

}
