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
import ch.unizh.geo.webgen.model.Constraint;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.registry.ParameterDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Point;

public class Eval_GMFeat_Diff_Position extends AWebGenAlgorithm implements IWebGenAlgorithm {
	
	/**
	 * Input: 	FeatureCollection	- geometry of building
	 * 
	 * Output:  FeatureCollection	- with constraints
	 */
	public void run(WebGenRequest wgreq) {
		Object fco = wgreq.getFeatureCollection("geom");
		if(fco instanceof ConstrainedFeatureCollection) {
			ConstrainedFeatureCollection fc = (ConstrainedFeatureCollection) fco;
			double threshold = Math.sqrt(wgreq.getParameterDouble("minsize"));
			double severityAvg = calculateSeverityAvg(fc, threshold);
			wgreq.addResult("severity", new Double(severityAvg));
			wgreq.addResult("result", fc);
		}
	}

	private double calculateSeverityAvg(ConstrainedFeatureCollection fc, double threshold) {
		if(fc.size() == 0) return 0.0;
		if(threshold == 0.0) {
			threshold = Math.sqrt(fc.getFeature(0).getGeometry().getArea());
			threshold += Math.sqrt(fc.getFeature(fc.size()-1).getGeometry().getArea());
			threshold /= 2;
		}
		double cost = 0.0;
		double severityAvg = 0.0;
		List fclist = fc.getFeatures();
		for(int i=0; i< fclist.size(); i++) {
			ConstrainedFeature feat = (ConstrainedFeature)fclist.get(i);
			Constraint wgc = feat.getConstraint();
			Point centroidOri = wgc.getOrigPos();
			Point centroid = feat.getGeometry().getCentroid();
			double distance = centroid.distance(centroidOri);
			
			// Übertragungsfunktion
			if(distance > threshold)
				cost = 1.0;
			else
				cost = distance/threshold;
			wgc.setSeverityDiffPos(cost);
			wgc.setNewPos(centroid);
			severityAvg += cost;
		}
		return severityAvg/fclist.size();
	}
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("Eval_GMFeat_Diff_Position", "neun", "support",
				"",
				"Eval_GMFeat_Diff_Position",
				"Evaluate Position Difference Constraint",
				"1.0");
		id.visible = true;
		
		//add input parameters
		id.addInputParameter(new ParameterDescription("geom", "FeatureCollection", null, true, "layer with geometries"));
		
		//add output parameters
		id.addOutputParameter("severity", "DOUBLE");
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}