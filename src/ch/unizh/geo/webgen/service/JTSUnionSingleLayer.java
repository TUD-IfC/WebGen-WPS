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
import java.util.Iterator;
import java.util.List;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDatasetFactory;

public class JTSUnionSingleLayer extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection fcA = wgreq.getFeatureCollection("geom");
		FeatureCollection fcnew = union(fcA);
		wgreq.addResult("result", fcnew);
	}

	private FeatureCollection union(FeatureCollection fc) {
        List unionGeometryList = new ArrayList();
        Geometry currUnion = null;
        for (Iterator i = fc.iterator(); i.hasNext();) {
            Feature f = (Feature) i.next();
            Geometry geom = f.getGeometry();
            if (currUnion == null) {
                currUnion = geom;
            } else {
                currUnion = currUnion.union(geom);
            }
        }
        unionGeometryList.add(currUnion);
        return FeatureDatasetFactory.createFromGeometry(unionGeometryList);
    }
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("JTSUnionSingleLayer", "neun", "support",
				"",
				"JTSUnionSingleLayer",
				"JTSUnionSingleLayer",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
