package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDatasetFactory;


public class MultiGeometryGenerator extends AWebGenAlgorithm implements IWebGenAlgorithm  {
		
	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		try {
			FeatureCollection fcnew = makeMG(fc);
			wgreq.addResult("result", fcnew);
		}
		catch (Exception e) {}
	}
	
	FeatureCollection makeMG(FeatureCollection fc) throws Exception {
		Geometry currUnion = null;
        for (Iterator i = fc.iterator(); i.hasNext();) {
            Feature f = (Feature) i.next();
            Geometry geom = f.getGeometry();
            if (currUnion == null) {
                currUnion = geom;
            }
            else {
                currUnion = currUnion.union(geom);
            }
        }
		
        ArrayList c = new ArrayList();
        c.add(currUnion);
        
        FeatureCollection fcnew =  FeatureDatasetFactory.createFromGeometry(c);
		return fcnew;
	}
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("MultiGeometryGenerator", "neun", "support",
				"",
				"MultiGeometryGenerator",
				"MultiGeometry Generator",
				"1.0");
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
