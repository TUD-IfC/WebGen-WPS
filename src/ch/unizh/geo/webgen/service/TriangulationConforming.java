package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
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
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDatasetFactory;

public class TriangulationConforming extends AWebGenAlgorithm implements IWebGenAlgorithm  {
    
	public void run(WebGenRequest wgreq) {
		FeatureCollection fcp = wgreq.getFeatureCollection("points");
		FeatureCollection fcl = wgreq.getFeatureCollection("lines");
		double mindist = wgreq.getParameterDouble("mindist");
		FeatureCollection fcnew = dlC(fcp, fcl, mindist);
		if(fcnew != null) {
			wgreq.addResult("result", fcnew);
		}
	}
	
	private FeatureCollection dlC(FeatureCollection fcp, FeatureCollection fcl, double mindist) {     
        List<LineString> edgelist = new ArrayList<LineString>();
        TriangulationStringIDs tristrid = new TriangulationStringIDs();
        try {
			int i = 0;
			Iterator iter = fcp.getFeatures().iterator();
			while (iter.hasNext()) {
				Geometry tg = ((Feature) iter.next()).getGeometry();
				ArrayList tns = tristrid.BuildNodesForGeom("obj" + i, tg.getCoordinates());
				tristrid.nodeList.addAll(tns);
				i++;
			}
			ArrayList edges2force = new ArrayList();
			ArrayList tns;
			TEdge ted = null; TNode tn1 = null; TNode tn2 = null;
			Coordinate[] tcoords;
			iter = fcl.getFeatures().iterator();
			while (iter.hasNext()) {
				Geometry tg = ((Feature) iter.next()).getGeometry();
				/*tcoords = tg.getCoordinates();
				tns = new ArrayList();
				tn1 = new TNode(tcoords[0].x,tcoords[0].y, "obj" + i);
				tns.add(tn1);
				for(int j=1; j<tcoords.length; j++) {
					tn2 = new TNode(tcoords[j].x,tcoords[j].y, "obj" + i);
					tns.add(tn2);
					ted = new TEdge(tn1, tn2);
					edges2force.add(ted);
					tn1 = tn2;
				}
				tristrid.FeatureNodeRefs.put("obj" + i, tns);
				tristrid.nodeList.addAll(tns);*/
				tns = tristrid.BuildNodesForGeom("obj" + i, tg.getCoordinates());
				for(int j=0; j<tns.size()-1; j++) {
					//ted = new TEdge((TNode)tns.get(j), (TNode)tns.get(j+1));
					//edges2force.add(ted);
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
        	if(tl.getLength() <= mindist) edgelist.add(tl);
        }
        return FeatureDatasetFactory.createFromGeometry(edgelist);
	}
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("TriangulationConforming", "neun", "support",
				"",
				"TriangulationConforming",
				"TriangulationConforming",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("points", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with points (nodes)");
		String[] allowedL = {"Point","LineString","Polygon"};
		id.addInputParameter("lines", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedL), "layer with lines");
		id.addInputParameter("mindist", "DOUBLE", "10.0", "minimum distance");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
