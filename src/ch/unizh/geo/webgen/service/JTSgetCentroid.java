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

public class JTSgetCentroid extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		FeatureCollection fcnew = getCentroid(fc);
		wgreq.addResult("result", fcnew);
	}

	private FeatureCollection getCentroid(FeatureCollection fcA) {
		Collection resultColl = new ArrayList();

		for (Iterator ia = fcA.iterator(); ia.hasNext();) {
			Feature fa = (Feature) ia.next();
			Geometry ga = fa.getGeometry();
			Geometry result = ga.getCentroid();
			if (result != null)	resultColl.add(result);
		}
		return FeatureDatasetFactory.createFromGeometry(resultColl);
	}
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("JTSgetCentroid", "neun", "support",
				"",
				"JTSgetCentroid",
				"JTSgetCentroid",
				"1.0",
				new String[] {"ica.genops.cartogen.Enhancement"});
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", new String[] {"Point"}), "centroids");
		return id;
	}
}