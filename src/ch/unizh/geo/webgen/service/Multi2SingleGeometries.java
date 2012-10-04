package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.webgen.model.ConstrainedFeature;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;

public class Multi2SingleGeometries extends AWebGenAlgorithm implements IWebGenAlgorithm  {
    
	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		FeatureCollection fcnew = simlify(fc);
		if(fcnew != null) {
			wgreq.addResult("result", fcnew);
		}
	}
	
	private FeatureCollection simlify(FeatureCollection fc) {
		ConstrainedFeatureCollection fcnew = new ConstrainedFeatureCollection(fc.getFeatureSchema());
		Feature f; Geometry g;
		ConstrainedFeature cf;
		for(Iterator iter=fc.iterator(); iter.hasNext();) {
			f = (Feature)iter.next();
			g = f.getGeometry();
			for(int i=0; i<g.getNumGeometries(); i++) {
				cf = new ConstrainedFeature(f);
				cf.setGeometry(g.getGeometryN(i));
				fcnew.add(cf);
			}
		}
		return fcnew;
	}
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("Multi2SingleGeometries", "neun", "support",
				"",
				"Multi2SingleGeometries",
				"Multi2SingleGeometries",
				"1.0");
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon","MultiPoint","MultiLineString","MultiPolygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
