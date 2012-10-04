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

import com.vividsolutions.jts.geom.Geometry;

public class Eval_GMFeat_Diff_WidthLengthRatio extends AWebGenAlgorithm implements IWebGenAlgorithm {

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
			Constraint wgc = feat.getConstraint();
			double widthLengthRatioOri = wgc.getOrigWLRatio();
			double diffElongation = getDiffElongation(feat.getGeometry(), widthLengthRatioOri);
			
			//Übertragungsfunktion
			double threshold = 0.5;	// wenn Differenz der Elongationen grösser 0.5 dann unakzeptabel
			if(diffElongation > threshold)
				cost = 1.0;
			else
				cost = 0.0 + diffElongation;
			if(cost < 0.0) cost = 0.0;
			wgc.setSeverityDiffWLRatio(cost);
			severityAvg += cost;
			//wgc.setNewWLRatio(diffElongation);
		}
		return severityAvg/fclist.size();
	}
	
	private double getDiffElongation(Geometry geom, double elongationOri) {
	    double diffElongation = 0.0; 
       	OrientationMBR myMbrcalc = new OrientationMBR(geom);
	    double elongation = myMbrcalc.getMbrWidth()/myMbrcalc.getMbrLength();
	    
	    if(elongationOri > 1.0)
	    	elongationOri = 1.0/elongationOri;
	    if(elongation > 1.0)
	    	elongation = 1.0/elongation;
	    
	    if(elongationOri > elongation)
	    	diffElongation = elongationOri - elongation;
	    else
	    	diffElongation = elongation - elongationOri;	    	
		return diffElongation;
	}		
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("Eval_GMFeat_Diff_WidthLengthRatio", "neun", "support",
				"",
				"Eval_GMFeat_Diff_WidthLengthRatio",
				"Evaluate WidthLengthRatio Constraint",
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