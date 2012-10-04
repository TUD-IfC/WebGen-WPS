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
import java.util.List;

import ch.unizh.geo.webgen.model.ConstrainedFeature;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.registry.ParameterDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Geometry;


public class Eval_GMFeat_MinSize extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		Object fco = wgreq.getFeatureCollection("geom");
		if(fco instanceof ConstrainedFeatureCollection) {
			ConstrainedFeatureCollection fc = (ConstrainedFeatureCollection) fco;
			double minsize = ((Double)wgreq.getParameter("minarea")).doubleValue();
			double severityAvg = calculateSeverityAvg(fc, minsize);
			wgreq.addResult("severity", new Double(severityAvg));
			wgreq.addResult("result", fc);
		}
	}
	
	private double calculateSeverityAvg(ConstrainedFeatureCollection fc, double minsize) {
		double cost;
		double area;
		double severityAvg = 0.0;
		List fclist = fc.getFeatures();
		for(int i=0; i< fclist.size(); i++) {
			ConstrainedFeature feat = (ConstrainedFeature)fclist.get(i);
			Geometry geom = feat.getGeometry();
			area = geom.getArea();
			cost = 1 - area/minsize;
			if(cost < 0.0) cost = 0.0;
			feat.getConstraint().setSeverityMinSize(cost);
			severityAvg += cost;
		}
		return severityAvg/fclist.size();
	}
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("Eval_GMFeat_MinSize", "neun", "support",
				"",
				"Eval_GMFeat_MinSize",
				"Evaluate Minium Size Constraint",
				"1.0");
		id.visible = true;
		
		//add input parameters
		id.addInputParameter(new ParameterDescription("geom", "FeatureCollection", null, true, "layer with geometries"));
		id.addInputParameter("minarea", "DOUBLE", "200.0", "minimum size");
		
		//add output parameters
		id.addOutputParameter("severity", "DOUBLE");
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}