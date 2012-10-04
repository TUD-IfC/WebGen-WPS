/*
 * Created on 19.08.2005
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

import java.util.HashMap;
import java.util.Iterator;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;

public class ReclassFeatures extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		String classfield = wgreq.getParameter("CLASSFIELD").toString();
		HashMap matrix = (HashMap)wgreq.getParameter("MATRIX");
		FeatureCollection fcnew = reclass(fc, classfield, matrix);
		wgreq.addResult("result", fcnew);
	}
	
	private FeatureCollection reclass(FeatureCollection fc, String classfield, HashMap classhierarchy) {
        FeatureCollection fcnew = new FeatureDataset(fc.getFeatureSchema());
        for (Iterator i = fc.iterator(); i.hasNext();) {
            Feature f = (Feature)((Feature)i.next()).clone();
        	Object ic = f.getAttribute(classfield);
        	if(classhierarchy.containsKey(ic)) {
        		//f.setAttribute(classfield,((WebGenWeightedMatrixElement)classhierarchy.get(ic)).getBest());
        	}
        	fcnew.add(f);
        }
        return fcnew;
    }
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("ReclassFeatures", "neun", "support",
				"",
				"ReclassFeatures",
				"Reclass Features according to supplied asignment matrix",
				"1.0");
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries and classes");
		id.addInputParameter("CLASSFIELD", "STRING", "CLASS", "CLASSFIELD");
		id.addInputParameter("MATRIX", "WeightedMatrix", "", "MATRIX");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}