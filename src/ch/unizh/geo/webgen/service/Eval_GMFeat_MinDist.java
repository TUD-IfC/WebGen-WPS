/*
 * Created on 20.07.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author neun
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import madge.structures.Graph.GraphEdge;
import madge.structures.Graph.GraphNode;
import madge.structures.Graph.ProxyGraph;
import ch.unizh.geo.webgen.model.ConstrainedFeature;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.model.Constraint;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.registry.ParameterDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jump.feature.Feature;

public class Eval_GMFeat_MinDist extends AWebGenAlgorithm implements IWebGenAlgorithm {
		
	public void run(WebGenRequest wgreq) {
		Object fco = wgreq.getFeatureCollection("geom");
		if(fco instanceof ConstrainedFeatureCollection) {
			ConstrainedFeatureCollection fc = (ConstrainedFeatureCollection) fco;
			double mindist = ((Double)wgreq.getParameter("mindist")).doubleValue();
			double severityAvg = 0.0;
			try {
				severityAvg = calculateSeverityAvg(fc, mindist);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			wgreq.addResult("severity", new Double(severityAvg));
			wgreq.addResult("result", fc);
		}
	}
	
	public double calculateSeverityAvg(ConstrainedFeatureCollection fc, double mindist) throws Exception {
		double severityAvg = 0.0;
		//--- create proxygraph
		Collection obstructFs = new ArrayList();
		ProxyGraph pg = new ProxyGraph();
		//-- obstructFeatures is not allowed to be null!
		//obstructFs = features; // if this is done the graph is not correct (edges are missed)
		pg.ProxyGraphPopulate(fc.getFeatures(), obstructFs, mindist);
		ArrayList nodes = pg.nodes;
		Iterator iter = nodes.iterator();
		while(iter.hasNext()) {
			GraphNode tn = (GraphNode)iter.next();
			addShortEdges(tn, mindist);
		}
		
		//finalize and calculate severity from short edges collection
		int fccount = 0;
		for(Iterator fciter = fc.iterator(); fciter.hasNext();) {
			Constraint twgc = ((ConstrainedFeature)fciter.next()).getConstraint();
			double cost = 1-(twgc.getMinDistAvg() / mindist);
			if((cost < 0.0) || Double.isNaN(cost)) cost = 0.0;
			if(cost > 1.0) cost = 1.0;
			twgc.setSeverityMinDist(cost);
			severityAvg += cost;
			twgc.resetMinDistAvg();
			fccount++;
		}
		
		return severityAvg/fccount;
	}
	
	public void addShortEdges(GraphNode gn, double mindist) {
		Feature feat = gn.rwo;
		Constraint wgc = (Constraint)feat.getAttribute("constraint");
		if(wgc == null) wgc = new Constraint();
		ArrayList edges = gn.edges;
		Iterator iter = edges.iterator();
		while(iter.hasNext()) {
			GraphEdge te = (GraphEdge)iter.next();
			if(te.weight <= mindist) {
				//wgc.addMinDist(mindist, te.weight);
				wgc.addMinDist(te.weight);
			}
		}
	}
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("Eval_GMFeat_MinDist", "neun", "support",
				"",
				"Eval_GMFeat_MinDist",
				"Evaluate Minium Distance Constraint",
				"1.0");
		id.visible = true;
		
		//add input parameters
		id.addInputParameter(new ParameterDescription("geom", "FeatureCollection", null, true, "layer with geometries"));
		id.addInputParameter("mindist", "DOUBLE", "10.0", "minimum distance");
		
		//add output parameters
		id.addOutputParameter("severity", "DOUBLE");
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
	
}

