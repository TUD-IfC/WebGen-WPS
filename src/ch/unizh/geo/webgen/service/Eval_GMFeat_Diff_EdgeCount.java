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

public class Eval_GMFeat_Diff_EdgeCount extends AWebGenAlgorithm implements IWebGenAlgorithm {

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
		List fclist = fc.getFeatures();
		for(int i=0; i< fclist.size(); i++) {
			ConstrainedFeature feat = (ConstrainedFeature)fclist.get(i);
			Constraint wgc = (Constraint)feat.getConstraint();
			int edgeCountOri = wgc.getOrigEdgeCount();
			int edgeCount = feat.getGeometry().getNumPoints() - 1 ;
			
			// Übertragungsfunktion
			cost = 1.0 - ((double) edgeCount/edgeCountOri);
			if(cost < 0.0) cost = 0.0;
			wgc.setSeverityDiffEdgeCount(cost);
			wgc.setNewEdgeCount(edgeCount);
			severityAvg += cost;
		}
		return severityAvg/fclist.size();
	}
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("Eval_GMFeat_Diff_EdgeCount", "neun", "support",
				"",
				"Eval_GMFeat_Diff_EdgeCount",
				"Evaluate Edge Count Difference Constraint",
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