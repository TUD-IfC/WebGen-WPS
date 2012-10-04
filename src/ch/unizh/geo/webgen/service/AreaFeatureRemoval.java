package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
import java.util.Iterator;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;


public class AreaFeatureRemoval extends AWebGenAlgorithm implements IWebGenAlgorithm  {
	
	public void run(WebGenRequest wgreq) {
		try {
			FeatureCollection fc = wgreq.getFeatureCollection("geom");
			double toosmall = wgreq.getParameterDouble("toosmall");
			FeatureCollection fcnew = areaRemove(fc, toosmall);
			wgreq.addResult("result", fcnew);
		}
		catch (Exception e) {}
	}
	

	private FeatureCollection areaRemove(FeatureCollection features, double minSize) throws Exception{
	    ArrayList<Feature> toremove = new ArrayList<Feature>();
	    //--get single object in selection to analyse
      	for (Iterator iter = features.iterator(); iter.hasNext();) {
      		Feature f = (Feature)iter.next();
	   		Geometry geom = f.getGeometry();
	       	if ( geom instanceof Polygon){
	       		if(((Polygon)geom).getArea() < minSize) {
	       			toremove.add(f);
	       		}
	       	}
      	}// end loop over item selection
      	features.removeAll(toremove);
        return features;        
	}
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("AreaFeatureRemoval", "neun", "operator",
				"",
				"AreaFeatureRemoval",
				"deletes features which are smaller than the submitted tosmall value",
				"1.0",
				new String[] {"ica.genops.cartogen.Elimination"});
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "building polygons");
		id.addInputParameter("toosmall", "DOUBLE", "50.0", "building minimum size");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "left building polygons");
		return id;
	}

}
