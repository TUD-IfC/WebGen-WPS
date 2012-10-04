/*
 * Created on 10.08.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author neun
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */

package ch.unizh.geo.webgen.service;
import java.util.ArrayList;
import java.util.Collection;
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
import com.vividsolutions.jump.feature.IndexedFeatureCollection;

public class JTSIntersection extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection fcA = wgreq.getFeatureCollection("geomA");
		FeatureCollection fcB = wgreq.getFeatureCollection("geomB");
		FeatureCollection fcnew = runGeometryMethod(fcA, fcB);
		wgreq.addResult("result", fcnew);
	}

	private FeatureCollection runGeometryMethod(FeatureCollection fcA, FeatureCollection fcB) {
		Collection resultColl = new ArrayList();
		FeatureCollection index = new IndexedFeatureCollection(fcB);
		for (Iterator ia = fcA.iterator(); ia.hasNext();) {
			Feature fa = (Feature) ia.next();
			Geometry ga = fa.getGeometry();
			Collection queryResult = index.query(ga.getEnvelopeInternal());
			for (Iterator ib = queryResult.iterator(); ib.hasNext();) {
				Feature fb = (Feature) ib.next();
				Geometry gb = fb.getGeometry();
				Geometry result = ga.intersection(gb);
				if (result != null)
					resultColl.add(result);
			}
		}
		return FeatureDatasetFactory.createFromGeometry(resultColl);
	}
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("JTSIntersection", "neun", "support",
				"",
				"JTSIntersection",
				"JTSIntersection",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("geomA", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("geomB", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
