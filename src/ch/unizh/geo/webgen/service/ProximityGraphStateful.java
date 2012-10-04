package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import madge.structures.Graph.GraphEdge;
import madge.structures.Graph.GraphNode;
import madge.structures.Graph.ProxyGraphWebGen;
import ch.unizh.geo.webgen.model.ConstrainedFeature;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.registry.ParameterDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.FeatureDatasetFactory;

public class ProximityGraphStateful extends AWebGenAlgorithm implements IWebGenAlgorithm  {
    
	static HashMap<String, ProxyGraphWebGen> graphs = new HashMap<String,ProxyGraphWebGen>();
	
	public void run(WebGenRequest wgreq) {
		try {
			String action = wgreq.getParameter("action").toString();
			if(action.equals("create")) doCreate(wgreq);
			else if(action.equals("delete")) doDelete(wgreq);
			else if(action.equals("getgeom")) doGetGeom(wgreq);
			else if(action.equals("shortestedge")) doShortestEdge(wgreq);
			else if(action.equals("shortestdist")) doShortestDist(wgreq);
			else if(action.equals("edgelist")) doEdgeList(wgreq);
			else if(action.equals("removenode")) doRemoveNode(wgreq);
			else if(action.equals("insertnode")) doInsertNode(wgreq);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void doCreate(WebGenRequest wgreq) throws Exception {
		ConstrainedFeatureCollection fc = (ConstrainedFeatureCollection)wgreq.getFeatureCollection("geom");
		ConstrainedFeatureCollection fco = (ConstrainedFeatureCollection)wgreq.getFeatureCollection("congeom");
		double mindist = wgreq.getParameterDouble("mindist");
		String graphid = wgreq.getParameter("graphid").toString();
		ProxyGraphWebGen pg = new ProxyGraphWebGen();
		Collection obstructFs;
		if(fco == null) obstructFs = new ArrayList();
		else obstructFs = fco.getFeatures();
		pg.ProxyGraphPopulate(fc.getFeatures(), obstructFs, mindist);
		graphs.put(graphid, pg);
	}
	
	private void doDelete(WebGenRequest wgreq) throws Exception {
		String graphid = wgreq.getParameter("graphid").toString();
		graphs.remove(graphid);
	}
	
	private void doGetGeom(WebGenRequest wgreq) throws Exception {
		String graphid = wgreq.getParameter("graphid").toString();
		ProxyGraphWebGen pg = graphs.get(graphid);
		GeometryFactory geomfact = new GeometryFactory();
		GraphEdge ge;
		Coordinate[] tcoords;
		ArrayList<LineString> edgeLines = new ArrayList<LineString>();
		for(int i=0; i < pg.edges.size(); i++) {
			ge = (GraphEdge) pg.edges.get(i);
			tcoords = new Coordinate[]{new Coordinate(ge.x1, ge.y1),new Coordinate(ge.x2, ge.y2)};
			edgeLines.add(geomfact.createLineString(tcoords));
		}
        wgreq.addResult("result", FeatureDatasetFactory.createFromGeometry(edgeLines));
	}
	
	
	private void doShortestEdge(WebGenRequest wgreq) throws Exception {
		String graphid = wgreq.getParameter("graphid").toString();
		ProxyGraphWebGen pg = graphs.get(graphid);
		//GraphEdge le = pg.getLowestEdge(pg.edges);
		GraphEdge le = getLowestTriangleEdgeBB(pg.edges);
		/*ConstrainedFeatureCollection twofeats = new ConstrainedFeatureCollection(le.node1.rwo.getSchema());
		twofeats.add(le.node1.rwo);
		twofeats.add(le.node2.rwo);
		wgreq.addResult("result", twofeats);*/
		wgreq.addResult("node1", new Integer(((ConstrainedFeature)le.node1.rwo).getUID()));
		wgreq.addResult("node2", new Integer(((ConstrainedFeature)le.node2.rwo).getUID()));
	}
	
	private void doShortestDist(WebGenRequest wgreq) throws Exception {
		String graphid = wgreq.getParameter("graphid").toString();
		ProxyGraphWebGen pg = graphs.get(graphid);
		GraphEdge le = getLowestProxyEdge(pg.edges);
		wgreq.addResult("node1", new Integer(((ConstrainedFeature)le.node1.rwo).getUID()));
		wgreq.addResult("node2", new Integer(((ConstrainedFeature)le.node2.rwo).getUID()));
	}
	
	
	private void doEdgeList(WebGenRequest wgreq) throws Exception {
		String graphid = wgreq.getParameter("graphid").toString();
		ProxyGraphWebGen pg = graphs.get(graphid);
		double mindist = wgreq.getParameterDouble("mindist");
		if(mindist == 0.0) {
			wgreq.addResult("edges", pg.edges);
		}
		else {
			ArrayList sedges = new ArrayList();
			GraphEdge te;
			for(Iterator iter = pg.edges.iterator(); iter.hasNext();) {
				te = (GraphEdge)iter.next();
				if(te.weight < mindist) sedges.add(te);
			}
			wgreq.addResult("edges", sedges);
		}
	}
	
	
	private GraphEdge getLowestProxyEdge(ArrayList edgeList) {
		double currentWeight, minWeight = -1;
		GraphEdge lowestEdge = (GraphEdge)edgeList.get(0);
		minWeight = ((GraphEdge)edgeList.get(0)).weight;
		for (int i = 1; i< edgeList.size(); i++) {
			currentWeight = ((GraphEdge)edgeList.get(i)).weight;
			if (minWeight > currentWeight)  {
				lowestEdge = (GraphEdge)edgeList.get(i);
				minWeight = currentWeight;
			} 
		}
		return lowestEdge;
	}
	
	private GraphEdge getLowestTriangleEdgeBB(ArrayList edgeList) {
		double currentWeight, minWeight = -1;
		int i = 0;
		GraphEdge actEdge = (GraphEdge)edgeList.get(0);
		GraphEdge lowestEdge;
		for (; i< edgeList.size(); i++){
			actEdge = (GraphEdge)edgeList.get(i);
			if((actEdge.node1.rwo.getGeometry() instanceof Polygon) && (actEdge.node2.rwo.getGeometry() instanceof Polygon)) break;
		}
		lowestEdge = actEdge;
		minWeight = getDist(actEdge.node1, actEdge.node2);
		for (; i< edgeList.size(); i++){
			actEdge = (GraphEdge)edgeList.get(i);
			currentWeight = getDist(actEdge.node1, actEdge.node2);
			if ((currentWeight < minWeight) &&
				(actEdge.node1.rwo.getGeometry() instanceof Polygon) && 
				(actEdge.node2.rwo.getGeometry() instanceof Polygon)) {
				lowestEdge = actEdge;
				minWeight = currentWeight;
			} 
		}
		if((lowestEdge.node1.rwo.getGeometry() instanceof LineString) ||
				(lowestEdge.node2.rwo.getGeometry() instanceof LineString)) {
			System.out.println("aha");
		}
		return lowestEdge;
	}
	
	private GraphEdge getLowestTriangleEdge(ArrayList edgeList) {
		double currentWeight, minWeight = -1;
		GraphEdge actEdge = (GraphEdge)edgeList.get(0);
		GraphEdge lowestEdge = actEdge;
		minWeight = getDist(actEdge.node1, actEdge.node2);
		for (int i = 1; i< edgeList.size(); i++){
			actEdge = (GraphEdge)edgeList.get(i);
			currentWeight = getDist(actEdge.node1, actEdge.node2);
			if (minWeight > currentWeight) {
				lowestEdge = actEdge;
				minWeight = currentWeight;
			} 
		}
		return lowestEdge;
	}
	
	private double getDist(GraphNode n1, GraphNode n2) {
		return Math.sqrt(Math.pow((n2.x-n1.x), 2) + Math.pow((n2.y-n1.y), 2));
	}
	
	private void doRemoveNode(WebGenRequest wgreq) throws Exception {
		String graphid = wgreq.getParameter("graphid").toString();
		int node = wgreq.getParameterInt("node");
		ProxyGraphWebGen pg = graphs.get(graphid);
		ConstrainedFeature nodecf = getFeatureFromUID(pg.nodes, node);
		if(nodecf != null) pg.ProxyGraphRemoveFeature(nodecf);
	}
	
	private ConstrainedFeature getFeatureFromUID(ArrayList nodes, int uid) {
		for(int i=0; i<nodes.size(); i++) {
			ConstrainedFeature cf = (ConstrainedFeature)((GraphNode)nodes.get(i)).rwo;
			if(cf.getUID() == uid) return cf;
		}
		return null;
	}
	
	
	private void doInsertNode(WebGenRequest wgreq) throws Exception {
		String graphid = wgreq.getParameter("graphid").toString();
		ConstrainedFeature newnode = (ConstrainedFeature)wgreq.getParameter("newnode");
		ProxyGraphWebGen pg = graphs.get(graphid);
		pg.ProxyGraphAddFeature(newnode);
	}
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("ProximityGraphStateful", "neun", "support",
				"",
				"ProximityGraphStateful",
				"ProximityGraphStateful",
				"1.0");
		
		//add input parameters
		ParameterDescription actionparam = new ParameterDescription("action", "STRING", "getgeom", "");
		actionparam.addSupportedValue("create");
		actionparam.addSupportedValue("shortestedge");
		actionparam.addSupportedValue("shortestdist");
		actionparam.addSupportedValue("edgelist");
		actionparam.addSupportedValue("removenode");
		actionparam.addSupportedValue("insertnode");
		actionparam.addSupportedValue("getgeom");
		actionparam.addSupportedValue("delete");
		actionparam.setChoiced();
		id.addInputParameter(actionparam);
		id.addInputParameter("graphid", "STRING", "g1", "id of stored graph");
		String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("congeom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with obstructing geometries");
		id.addInputParameter("mindist", "DOUBLE", "10.0", "minimum distance");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
