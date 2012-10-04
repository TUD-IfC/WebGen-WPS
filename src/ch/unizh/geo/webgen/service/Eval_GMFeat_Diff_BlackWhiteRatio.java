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
import ch.unizh.geo.webgen.model.CollectionConstraint;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.registry.ParameterDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.tools.CollectionHelper;

import com.vividsolutions.jump.feature.FeatureCollection;

public class Eval_GMFeat_Diff_BlackWhiteRatio extends AWebGenAlgorithm implements IWebGenAlgorithm {

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
		if(!(fco instanceof ConstrainedFeatureCollection)) return;
		ConstrainedFeatureCollection fc = (ConstrainedFeatureCollection) fco;
		if(fc.getCollectionConstraint() == null) return;
		
		CollectionConstraint cc = fc.getCollectionConstraint();
		FeatureCollection cfc = wgreq.getFeatureCollection("congeom");
		double bwRatio = CollectionHelper.calculateBWRatio(fc, cfc);
		double severity = Math.abs(cc.getOrigBWRatio() - bwRatio);
		if(severity > 1.0) severity = 1.0;
		wgreq.addResult("bwratio", bwRatio);
		cc.setSeverityDiffBWRatio(severity);
		cc.setActBWRatio(bwRatio);
		wgreq.addResult("severity", severity);
		wgreq.addResult("result", fc);
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