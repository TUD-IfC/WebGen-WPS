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
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public class Eval_GMFeat_MinLength extends AWebGenAlgorithm implements IWebGenAlgorithm {

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
			double minlength = ((Double)wgreq.getParameter("minlength")).doubleValue();
			double severityAvg = calculateSeverityAvg(fc, minlength);
			wgreq.addResult("severity", new Double(severityAvg));
			wgreq.addResult("result", fc);
		}
	}

	private double calculateSeverityAvg(ConstrainedFeatureCollection fc, double minlength) {
		double cost = 0.0;
		double severityAvg = 0.0;
		List fclist = fc.getFeatures();
		for(int i=0; i< fclist.size(); i++) {
			ConstrainedFeature feat = (ConstrainedFeature)fclist.get(i);
			Geometry geom = feat.getGeometry();
			cost = getShortEdgeCountPerc(geom, minlength);
			feat.getConstraint().setSeverityMinLength(cost);
			severityAvg += cost;
		}
		return severityAvg/fclist.size();
	}
	
	private double getShortEdgeCountPerc(Geometry geom, double minlength) {
		if(geom instanceof LineString) {
			LineString lineString = (LineString)geom;
			return(getShortEdgeCountPerc(lineString, minlength));				
		} 
		if(geom instanceof Polygon) {
			Polygon polygon = (Polygon)geom;
			LineString lineString = polygon.getExteriorRing();
			return(getShortEdgeCountPerc(lineString, minlength));				
		} 			
		return 0.0;
	}

	private double getShortEdgeCountPerc(LineString lineString, double minlength) {
		int 	shortEdgeCount = 0;
		double 	shortEdgeCountPerc = 0.0;
		if (lineString.getNumPoints() < 2)
				return(0.0);
        for (int i = 1; i < lineString.getNumPoints(); i++) {
        	double shortEdge = java.awt.geom.Point2D.distance(
        			lineString.getPointN(i-1).getX(),
		        	lineString.getPointN(i-1).getY(),
		        	lineString.getPointN(i).getX(),
		        	lineString.getPointN(i).getY());
        	if(shortEdge < minlength) {
        		shortEdgeCount++;
        	}
        }						        	
        shortEdgeCountPerc = (double)shortEdgeCount/lineString.getNumPoints();
		return shortEdgeCountPerc;
	}
	
	/*private FeatureCollection getConflictingFeatures(FeatureCollection fc, double minlength) {
		FeatureCollection result = new FeatureDataset(fc.getFeatureSchema());
		double average = 0.0;
		double max = 0.0;
		double min = 1.0;
		Iterator iter = fc.iterator();
		while(iter.hasNext()) {
			Feature feat = (Feature)iter.next();
			Geometry geom = feat.getGeometry();
			double shortEdgeCountStand = getShortEdgeCountPerc(geom, minlength);
			if(shortEdgeCountStand > 0.0) {
				result.add(feat);
				average += shortEdgeCountStand;
				if(shortEdgeCountStand > max) max = shortEdgeCountStand;
				if(shortEdgeCountStand < min) min = shortEdgeCountStand;
			}
		}
		average /= result.size();		
		return result;
	}*/
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("Eval_GMFeat_MinLength", "neun", "support",
				"",
				"Eval_GMFeat_MinLength",
				"Evaluate Minium Length Constraint",
				"1.0");
		id.visible = true;
		
		//add input parameters
		id.addInputParameter(new ParameterDescription("geom", "FeatureCollection", null, true, "layer with geometries"));
		id.addInputParameter("minlength", "DOUBLE", "10.0", "minimum length");
		
		//add output parameters
		id.addOutputParameter("severity", "DOUBLE");
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}

}