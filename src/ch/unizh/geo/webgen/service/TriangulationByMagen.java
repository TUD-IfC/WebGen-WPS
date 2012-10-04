package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDatasetFactory;

public class TriangulationByMagen extends AWebGenAlgorithm implements IWebGenAlgorithm  {
    
	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		FeatureCollection fcnew = dlMagen(fc);
		if(fcnew != null) {
			wgreq.addResult("result", fcnew);
		}
	}
	public HashMap run(HashMap layers, HashMap parameters) {
		FeatureCollection fcA = (FeatureCollection) layers.get("fcA");
		FeatureCollection fcnew = dlMagen(fcA);
		HashMap resulthash = null;
		if(fcnew != null) {
			resulthash = new HashMap();
			resulthash.put("result", fcnew);
		}
		return resulthash;			
	}
	
	private FeatureCollection dlMagen(FeatureCollection fc) {		       
        List edgelist = new ArrayList();
        TriangulationStringIDs tristrid = new TriangulationStringIDs();
        try {
			int i = 0;
			Iterator iter = fc.getFeatures().iterator();
			while (iter.hasNext()) {
				Geometry tg = ((Feature) iter.next()).getGeometry();
				ArrayList tns = tristrid.BuildNodesForGeom("obj" + i, tg.getCoordinates());
				tristrid.nodeList.addAll(tns);
				i++;
			}

			tristrid.BuildFromNodes();
		}
        catch (TriangulationException e) {
			addErrorStack(e.getLocalizedMessage(), e.getStackTrace());
		}
        GeometryFactory gfactory = new GeometryFactory() ;
        ArrayList edges = tristrid.edgeList;
        for(int i=0; i<edges.size(); i++) {
        	TEdge te = (TEdge)edges.get(i);
        	Coordinate[] coo = new Coordinate[2];
        	coo[0] = new Coordinate(te.node1.xy.x, te.node1.xy.y);
        	coo[1] = new Coordinate(te.node2.xy.x, te.node2.xy.y);
        	edgelist.add(gfactory.createLineString(coo));
        }
        return FeatureDatasetFactory.createFromGeometry(edgelist);
	}
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("TriangulationByMagen", "neun", "support",
				"",
				"TriangulationByMagen",
				"Triangulation from the Magen Library",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
