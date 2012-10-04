package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.tools.GeometryHelper;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;


public class AreaScalingSimple extends AWebGenAlgorithm implements IWebGenAlgorithm  {
	
	public void run(WebGenRequest wgreq) {
		try {
			FeatureCollection fc = wgreq.getFeatureCollection("geom");
			double minSize = wgreq.getParameterDouble("minarea");
			FeatureCollection fcnew = areaScale(fc, minSize);
			wgreq.addResult("result", fcnew);
		}
		catch (Exception e) {}
	}
	

	private FeatureCollection areaScale(FeatureCollection features, double minSize) throws Exception{
	     
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
	       		if(poly.getArea() < minSize) {
					poly = (Polygon)GeometryHelper.skaleSize(poly, minSize);
					fnew.setGeometry(poly);	       			
	       		}
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
		InterfaceDescription id = new InterfaceDescription("AreaScalingSimple", "burg", "operator",
				"",
				"AreaScalingSimple",
				"enlarges all buildings to the minimum size",
				"1.0",
				new String[] {"ica.genops.cartogen.Enhancement"});
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "building polygons");
		id.addInputParameter("minarea", "DOUBLE", "200.0", "building minimum size");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "scaled building polygons");
		return id;
	}

}
