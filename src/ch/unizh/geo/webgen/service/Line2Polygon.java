package ch.unizh.geo.webgen.service;

import java.util.Collection;
import java.util.Iterator;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDatasetFactory;


public class Line2Polygon extends AWebGenAlgorithm implements IWebGenAlgorithm  {
	
	public void run(WebGenRequest wgreq) {
		try {
			FeatureCollection fcA = (FeatureCollection) wgreq.getFeatureCollection("geom");
			FeatureCollection fcnew = Line2PolygonFast(fcA);
			wgreq.addResult("result", fcnew);
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	/*FeatureCollection Lines2Polygons(FeatureCollection fc) throws Exception {
		//FeatureCollection fcnew = new ConstrainedFeatureCollection(fc.getFeatureSchema());
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
		
        if(!(currUnion instanceof MultiLineString)) return null;
		MultiLineString mls = (MultiLineString) currUnion;

        // dont need to do this (its slow), but it will node for you!!
        //mls = (MultiLineString) mls.union(mls);

        Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(mls);

        Collection c = polygonizer.getPolygons();
        
        FeatureCollection fcnew =  FeatureDatasetFactory.createFromGeometry(c);
		return fcnew;
	}*/
	
	FeatureCollection Line2PolygonFast(FeatureCollection fc) throws Exception {
		Polygonizer polygonizer = new Polygonizer();
        for (Iterator i = fc.iterator(); i.hasNext();) {
            Feature f = (Feature) i.next();
            Geometry geom = f.getGeometry();
            polygonizer.add(geom);
        }
        Collection c = polygonizer.getPolygons();
        FeatureCollection fcnew =  FeatureDatasetFactory.createFromGeometry(c);
		return fcnew;
	}
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("Line2Polygon", "neun", "support",
				"http://localhost:8080/webgen/execute",
				"Line2Polygon",
				"Creates polygons from LineString networks (e.g. roads or rivers)",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"LineString","MultiLineString"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with linestrings");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}

}
