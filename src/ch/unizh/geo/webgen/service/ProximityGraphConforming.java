package ch.unizh.geo.webgen.service;

import graph.GraphEdge;
import graph.ProxyGraphWebGen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


import triangulation.TEdge;
import triangulation.TNode;
import triangulation.TriangulationException;
import triangulation.TriangulationStringIDs;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDatasetFactory;

public class ProximityGraphConforming extends AWebGenAlgorithm implements IWebGenAlgorithm  {
    
	TriangulationStringIDs tristrid;
	ProxyGraphWebGen pg;
	
	public void run(WebGenRequest wgreq) {
		FeatureCollection fcp = wgreq.getFeatureCollection("geom");
		FeatureCollection fcl = wgreq.getFeatureCollection("congeom");
		double mindist = wgreq.getParameterDouble("mindist");
		FeatureCollection fctri = dlC(fcp, fcl, mindist);
		if(fctri != null) wgreq.addResult("result triangulation", fctri);
		FeatureCollection fcprox = proxyTri();
		if(fcprox != null) wgreq.addResult("result proxygraph", fcprox);
		FeatureCollection fcproxb = proxyBuildings();
		if(fcproxb != null) wgreq.addResult("result proxybuildings", fcproxb);
	}
	
	private FeatureCollection dlC(FeatureCollection fcp, FeatureCollection fcl, double mindist) {     
        List<LineString> edgelist = new ArrayList<LineString>();
        tristrid = new TriangulationStringIDs();
        try {
			int i = 0;
			Iterator iter = fcp.getFeatures().iterator();
			while (iter.hasNext()) {
				Feature tf = (Feature) iter.next();
				Geometry tg = tf.getGeometry();
				//ArrayList tns = tristrid.BuildNodesForGeom("obj" + i, tg.getCoordinates());
				//ArrayList tns = tristrid.BuildNodesForGeom("obj" + i, tg.getCentroid().getCoordinates());
				ArrayList tns = tristrid.BuildNodesForGeom("obj" + i, tg.getCentroid().getCoordinates(), tf);
				tristrid.nodeList.addAll(tns);
				i++;
			}
			ArrayList edges2force = new ArrayList();
			ArrayList tns;
			TEdge ted = null; TNode tn1 = null; TNode tn2 = null;
			Coordinate[] tcoords;
			iter = fcl.getFeatures().iterator();
			while (iter.hasNext()) {
				Feature tf = (Feature) iter.next();
				Geometry tg = tf.getGeometry();
				//tns = tristrid.BuildNodesForGeom("obj" + i, tg.getCoordinates());
				tns = tristrid.BuildNodesForGeom("obj" + i, tg.getCoordinates(), tf);
				for(int j=0; j<tns.size()-1; j++) {
					edges2force.add(new TNode[]{(TNode)tns.get(j), (TNode)tns.get(j+1)});
				}
				tristrid.nodeList.addAll(tns);
				i++;
			}
			tristrid.BuildFromNodes();
			
			ArrayList in = new ArrayList();
			ArrayList out = new ArrayList();
			TNode[] tnt;
			iter = edges2force.iterator();
			while (iter.hasNext()) {
				//ted = (TEdge) iter.next();
				//tristrid.ForceEdge(ted, in, out);
				tnt = (TNode[]) iter.next();
				tristrid.ForceEdge(new TEdge(tnt[0], tnt[1]), in, out);
			}
		}
        catch (TriangulationException e) {
			addErrorStack(e.getLocalizedMessage(), e.getStackTrace());
		}
        GeometryFactory gfactory = new GeometryFactory() ;
        ArrayList edges = tristrid.edgeList;
        LineString tl;
        for(int i=0; i<edges.size(); i++) {
        	TEdge te = (TEdge)edges.get(i);
        	Coordinate[] coo = new Coordinate[2];
        	coo[0] = new Coordinate(te.node1.xy.x, te.node1.xy.y);
        	coo[1] = new Coordinate(te.node2.xy.x, te.node2.xy.y);
        	tl = gfactory.createLineString(coo);
        	//if(tl.getLength() <= mindist) edgelist.add(tl);
        	edgelist.add(tl);
        }
        return FeatureDatasetFactory.createFromGeometry(edgelist);
	}
	
	
	private FeatureCollection proxyTri() {
		pg = new ProxyGraphWebGen();
		Collection obstructFs = new ArrayList();
		
		try {
			pg.ProxyGraphPopulate(tristrid);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		GeometryFactory geomfact = new GeometryFactory();
		GraphEdge ge;
		Coordinate[] tcoords;
		ArrayList<LineString> edgeLines = new ArrayList<LineString>();
		for(int i=0; i < pg.edges.size(); i++) {
			ge = (GraphEdge) pg.edges.get(i);
			tcoords = new Coordinate[]{new Coordinate(ge.x1, ge.y1),new Coordinate(ge.x2, ge.y2)};
			edgeLines.add(geomfact.createLineString(tcoords));
		}
        return FeatureDatasetFactory.createFromGeometry(edgeLines);
	}
	
	
	private FeatureCollection proxyBuildings() {
		GeometryFactory geomfact = new GeometryFactory();
		GraphEdge ge;
		Coordinate[] tcoords;
		ArrayList<LineString> edgeLines = new ArrayList<LineString>();
		for(int i=0; i < pg.edges.size(); i++) {
			ge = (GraphEdge) pg.edges.get(i);
			if((ge.node1.rwo.getGeometry() instanceof Polygon) || (ge.node2.rwo.getGeometry() instanceof Polygon)) {
				tcoords = new Coordinate[]{new Coordinate(ge.x1, ge.y1),new Coordinate(ge.x2, ge.y2)};
				edgeLines.add(geomfact.createLineString(tcoords));
			}
		}
        return FeatureDatasetFactory.createFromGeometry(edgeLines);
	}
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("ProximityGraphConforming", "neun", "support",
				"",
				"ProximityGraphConforming",
				"ProximityGraphConforming",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with points or polygons (nodes)");
		String[] allowedL = {"Point","LineString","Polygon"};
		id.addInputParameter("congeom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedL), "layer with lines");
		id.addInputParameter("mindist", "DOUBLE", "10.0", "minimum distance");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
