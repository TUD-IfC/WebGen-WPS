package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import madge.structures.Graph.GraphEdge;
import madge.structures.Graph.ProxyGraphWebGen;

import triangulation.TEdge;
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
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDatasetFactory;

public class TriangulationProxyGraph extends AWebGenAlgorithm implements IWebGenAlgorithm  {
    
	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		double mindist = wgreq.getParameterDouble("mindist");
		FeatureCollection fcnew = dlMagen(fc, mindist);
		if(fcnew != null) {
			wgreq.addResult("result", fcnew);
		}
	}
	
	private FeatureCollection dlMagen(FeatureCollection fc, double mindist) {
		ProxyGraphWebGen pg = new ProxyGraphWebGen();
		Collection obstructFs = new ArrayList();
		try {
			pg.ProxyGraphPopulate(fc.getFeatures(), obstructFs, mindist);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		GeometryFactory geomfact = new GeometryFactory();
		GraphEdge ge;
		Coordinate[] tcoords;
		ArrayList edgeLines = new ArrayList();
		for(int i=0; i < pg.edges.size(); i++) {
			ge = (GraphEdge) pg.edges.get(i);
			//tcoords = new Coordinate[]{new Coordinate(ge.node1.x, ge.node1.y),new Coordinate(ge.node2.x, ge.node2.y)};
			tcoords = new Coordinate[]{new Coordinate(ge.x1, ge.y1),new Coordinate(ge.x2, ge.y2)};
			edgeLines.add(geomfact.createLineString(tcoords));
		}
        return FeatureDatasetFactory.createFromGeometry(edgeLines);
	}
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("TriangulationProxyGraph", "neun", "support",
				"",
				"TriangulationProxyGraph",
				"TriangulationProxyGraph",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("mindist", "DOUBLE", "10.0", "minimum distance");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
