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

public class ProximityGraphGeoms extends AWebGenAlgorithm implements IWebGenAlgorithm  {
    
	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		double mindist = wgreq.getParameterDouble("mindist");
		FeatureCollection fcnew = proximityGraph(fc, mindist);
		if(fcnew != null) {
			wgreq.addResult("result", fcnew);
		}
	}
	
	private FeatureCollection proximityGraph(FeatureCollection fc, double mindist) {
		try {
			ProxyGraphWebGen pg = new ProxyGraphWebGen();
			Collection obstructFs = new ArrayList();
			pg.ProxyGraphPopulate(fc.getFeatures(), obstructFs, mindist);
			
			GeometryFactory gfactory = new GeometryFactory() ;
			ArrayList<LineString> edgelist = new ArrayList<LineString>();
			for (int i = 0; i < pg.edges.size(); i++) {
				GraphEdge te = (GraphEdge) pg.edges.get(i);
				Coordinate[] coo = new Coordinate[2];
	        	coo[0] = new Coordinate(te.x1, te.y1);
	        	coo[1] = new Coordinate(te.x2, te.y2);
	        	edgelist.add(gfactory.createLineString(coo));
			}
	        return FeatureDatasetFactory.createFromGeometry(edgelist);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("ProximityGraphGeoms", "neun", "support",
				"",
				"ProximityGraphGeoms",
				"ProximityGraph delivers Geometries",
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
