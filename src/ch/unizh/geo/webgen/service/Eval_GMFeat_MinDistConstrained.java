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

import graph.GraphEdge;
import graph.GraphNode;
import graph.ProxyGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import ch.unizh.geo.webgen.model.ConstrainedFeature;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.model.Constraint;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.registry.ParameterDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;

public class Eval_GMFeat_MinDistConstrained extends AWebGenAlgorithm implements IWebGenAlgorithm {
		
	public void run(WebGenRequest wgreq) {
		Object fco = wgreq.getFeatureCollection("geom");
		if(fco instanceof ConstrainedFeatureCollection) {
			ConstrainedFeatureCollection fc = (ConstrainedFeatureCollection) fco;
			FeatureCollection cfc = wgreq.getFeatureCollection("congeom");
			double mindist = wgreq.getParameterDouble("mindist");
			double roaddist = wgreq.getParameterDouble("roaddist");
			double severityAvg = 0.0;
			try {
				severityAvg = calculateSeverityAvg(fc, cfc, mindist, roaddist);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			wgreq.addResult("severity", new Double(severityAvg));
			wgreq.addResult("result", fc);
		}
	}
	
	public double calculateSeverityAvg(ConstrainedFeatureCollection fc, FeatureCollection cfc, double mindist, double roaddist) throws Exception {
		//adding streets to the working collection
		FeatureSchema fcs = fc.getFeatureSchema();
		ConstrainedFeatureCollection fcremove = new ConstrainedFeatureCollection(fcs);
		for (Iterator fciter = cfc.iterator(); fciter.hasNext();) {
			Geometry tgeom = ((Feature) fciter.next()).getGeometry();
			ConstrainedFeature tfnew = new ConstrainedFeature(fcs);
			if(tgeom instanceof LineString) {
				tfnew.setGeometry(tgeom);
			}
			else if(tgeom instanceof Polygon) {
				Polygon tpoly = (Polygon) tgeom;
				GeometryFactory geomfact = new GeometryFactory();
				LineString tline = new LineString(tpoly.getExteriorRing().getCoordinateSequence(), geomfact);
				tfnew.setGeometry(tline);
			}
			fc.add(tfnew);
			fcremove.add(tfnew);
		}
		
		double severityAvg = 0.0;
		//--- create proxygraph
		Collection obstructFs = new ArrayList();
		ProxyGraph pg = new ProxyGraph();
		//-- obstructFeatures is not allowed to be null!
		//obstructFs = features; // if this is done the graph is not correct (edges are missed)
		if(mindist > roaddist) pg.ProxyGraphPopulate(fc.getFeatures(), obstructFs, mindist);
		else pg.ProxyGraphPopulate(fc.getFeatures(), obstructFs, roaddist);
		ArrayList nodes = pg.nodes;
		Iterator iter = nodes.iterator();
		while(iter.hasNext()) {
			GraphNode tn = (GraphNode)iter.next();
			if(!(tn.rwo.getGeometry() instanceof LineString)) addShortEdges(tn, mindist, roaddist);
		}
		
		//remove streets
		fc.removeAll(fcremove.getFeatures());
		
		//finalize and calculate severity from short edges collection
		int fccount = 0;
		for(Iterator fciter = fc.iterator(); fciter.hasNext();) {
			Constraint twgc = ((ConstrainedFeature)fciter.next()).getConstraint();
			//double cost = 1-twgc.getMinDistAvgQuot();
			double cost = twgc.getMinDistMaxQuot(); //neu 27.10.06 durch moritz wegen gewichtung mindist
			if((cost < 0.0) || Double.isNaN(cost)) cost = 0.0;
			if(cost > 1.0) cost = 1.0;
			twgc.setSeverityMinDist(cost);
			severityAvg += cost;
			twgc.resetMinDistAvgQuot();
			fccount++;
		}
		
		return severityAvg/fccount;
	}
	
	public void addShortEdges(GraphNode gn, double mindist, double roaddist) {
		Feature feat = gn.rwo;
		Constraint wgc = (Constraint)feat.getAttribute("constraint");
		if(wgc == null) wgc = new Constraint();
		ArrayList edges = gn.edges;
		Iterator iter = edges.iterator();
		while(iter.hasNext()) {
			GraphEdge te = (GraphEdge)iter.next();
			GraphNode neighbour = te.node1;
			if(neighbour == gn) neighbour = te.node2;
			if(neighbour.rwo.getGeometry() instanceof LineString) {
				if(te.weight <= roaddist) {
					wgc.addMinDistQuot(1-(te.weight/roaddist)); // roaddist wird bestraft
					}
			}
			else {
				if(te.weight <= mindist) {
					wgc.addMinDistQuot(1-(te.weight/mindist));
				}
			}
		}
	}
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("Eval_GMFeat_MinDistConstrained", "neun", "support",
				"",
				"Eval_GMFeat_MinDistConstrained",
				"Evaluate Minium Distance Constraint with road distances",
				"1.0");
		id.visible = true;
		
		//add input parameters
		id.addInputParameter(new ParameterDescription("geom", "FeatureCollection", null, true, "layer with geometries"));
		id.addInputParameter(new ParameterDescription("congeom", "FeatureCollection", null, true, "layer with geometries (roads)"));
		id.addInputParameter("mindist", "DOUBLE", "10.0", "minimum distance");
		id.addInputParameter("roaddist", "DOUBLE", "10.0", "minimum road distance");
		
		//add output parameters
		id.addOutputParameter("severity", "DOUBLE");
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
	
}

