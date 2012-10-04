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

import ch.unizh.geo.measures.OrientationMBR;
import ch.unizh.geo.webgen.model.ConstrainedFeature;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.model.Constraint;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.registry.ParameterDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

public class Eval_GMFeat_Diff_Orientation extends AWebGenAlgorithm implements IWebGenAlgorithm {

	double average;
	double min;
	double max;
	
	/**
	 * Input: 	FeatureCollection	- geometry of building
	 * 			minlength			- threshold for minimal edge length
	 * 
	 * Output:  shortEdgeCount		- number of edges below threshold standardised with number of edges
	 */
	public void run(WebGenRequest wgreq) {
		Object fco = wgreq.getFeatureCollection("geom");
		if(fco instanceof ConstrainedFeatureCollection) {
			ConstrainedFeatureCollection fc = (ConstrainedFeatureCollection) fco;
			double severityAvg = calculateSeverityAvg(fc);
			wgreq.addResult("severity", new Double(severityAvg));
			wgreq.addResult("result", fc);
		}
	}

	private double calculateSeverityAvg(ConstrainedFeatureCollection fc) {
		double cost = 0.0;
		double severityAvg = 0.0;
		double toleranceindegrees = 45;
		double tolerance = (toleranceindegrees/180)*Math.PI;
		List fclist = fc.getFeatures();
		for(int i=0; i< fclist.size(); i++) {
			ConstrainedFeature feat = (ConstrainedFeature)fclist.get(i);
			OrientationMBR myMbrcalc = new OrientationMBR(feat.getGeometry());
			Constraint wgc = feat.getConstraint();
			double origOrientation = wgc.getOrigOrientation();
			double newOrientatione = myMbrcalc.getStatOrientation();
			double diffOrientation = Math.abs(origOrientation - newOrientatione);
			
			if((wgc.getOrigWLRatio() > 0.9) && (diffOrientation > tolerance)) {
				diffOrientation = Math.abs(diffOrientation - (Math.PI/2));
			}
			if(diffOrientation > (Math.PI/2)) {
				diffOrientation = Math.PI - diffOrientation;
			}
			//Übertragungsfunktion
			if(diffOrientation > tolerance)
				cost = 1.0;
			else
				cost = diffOrientation/tolerance;
			if(cost < 0.0) cost = 0.0;
			wgc.setSeverityDiffOrientation(cost);
			severityAvg += cost;
		}
		return severityAvg/fclist.size();
	}		

	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("Eval_GMFeat_Diff_Orientation", "neun", "support",
				"",
				"Eval_GMFeat_Diff_Orientation",
				"Evaluate Orientation Difference Constraint",
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