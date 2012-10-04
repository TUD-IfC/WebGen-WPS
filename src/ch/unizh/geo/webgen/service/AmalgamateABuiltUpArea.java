package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import ch.unizh.geo.webgen.model.ConstrainedFeature;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.server.WebGenRequestExecuter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;

public class AmalgamateABuiltUpArea extends AWebGenAlgorithm implements IWebGenAlgorithm {
	
	public void run(WebGenRequest wgreq) {
		FeatureCollection streets = wgreq.getFeatureCollection("congeom");
		FeatureCollection buildings = wgreq.getFeatureCollection("geom");
		double minsize = wgreq.getParameterDouble("minsize");
		double mindist = wgreq.getParameterDouble("mindist");
		double roaddist = wgreq.getParameterDouble("roaddist");
		ConstrainedFeatureCollection fcnew = runAlgo(buildings, streets, minsize, mindist, roaddist);
		wgreq.addResult("result", fcnew);
	}

	private ConstrainedFeatureCollection runAlgo(FeatureCollection buildings, FeatureCollection streets, double minsize, double mindist, double roaddist) {
		FeatureSchema bfs = buildings.getFeatureSchema();
		
		ConstrainedFeature largestBuilding = null;
		double area = 0.0;
		Feature tf; Feature lf = null;
		for(Iterator i = buildings.iterator(); i.hasNext();) {
			tf = (Feature)i.next();
			if(tf.getGeometry().getArea() > area) lf = tf;
		}
		largestBuilding = new ConstrainedFeature(lf);
		
		//create initial polygon
		Polygonizer polygonizer = new Polygonizer();
        for(Iterator i = streets.iterator(); i.hasNext();) {
            polygonizer.add(((Feature) i.next()).getGeometry());
        }
        Collection polygons = polygonizer.getPolygons();
        Geometry builtupArea = (Geometry)polygons.iterator().next();
		
        //create remaining area
        Geometry remainingArea = (Geometry)builtupArea.clone();
        Geometry tg; Geometry tgd;
        for(Iterator i = buildings.iterator(); i.hasNext();) {
        	tg = ((Feature) i.next()).getGeometry();
        	tgd = remainingArea.difference(tg);
        	remainingArea = tgd;
		}
        
        
        //subtract street buffers
        for(Iterator i = streets.iterator(); i.hasNext();) {
            builtupArea = builtupArea.difference(((Feature) i.next()).getGeometry().buffer(roaddist));
        }
        
        HashMap parameters = new HashMap();
        ConstrainedFeatureCollection remAreaColl = new ConstrainedFeatureCollection(bfs);
        if(remainingArea instanceof MultiPolygon) {
        	for(int i=0; i < ((MultiPolygon)remainingArea).getNumGeometries(); i++) {
        		Geometry tgeom = ((MultiPolygon)remainingArea).getGeometryN(i);
        		if((tgeom instanceof Polygon) && (tgeom.getArea() >= minsize)) {
        			tgeom = removeSmallHoles((Polygon)tgeom, minsize);
        			ConstrainedFeature remArea = new ConstrainedFeature(bfs);
                    remArea.setGeometry(tgeom);
                    remArea.initConstraint();
                    remAreaColl.add(remArea);
        		}
        	}
        }
        else {
        	if (remainingArea.getArea() >= minsize) {
        		remainingArea = removeSmallHoles((Polygon)remainingArea, minsize);
        		ConstrainedFeature remArea = new ConstrainedFeature(bfs);
                remArea.setGeometry(remainingArea);
                remArea.initConstraint();
                remAreaColl.add(remArea);
        	}
        }
        parameters.put("geom", remAreaColl);
        parameters.put("minLengtInM", mindist);
        parameters.put("Iterations", new Integer(5));
        WebGenRequest twgreq = WebGenRequestExecuter.callService(parameters, "localcloned", "BuildingSimplifyOutline");
        FeatureCollection remAreaSimplifiedResult = (FeatureCollection)twgreq.getResult("result");
        
        //finally subtract remainingArea
        for(Iterator i = remAreaSimplifiedResult.iterator(); i.hasNext();) {
        	tg = ((Feature) i.next()).getGeometry();
        	builtupArea = builtupArea.difference(tg);
		}
        
        //add geometry to largest building feature
		ConstrainedFeatureCollection result = new ConstrainedFeatureCollection(bfs);
		largestBuilding.setGeometry(builtupArea);
		result.add(largestBuilding);
		//result.addAll(remAreaSimplifiedResult.getFeatures());
		return result;
	}
	
	
	private Polygon removeSmallHoles(Polygon poly, double minsize) {
		GeometryFactory geomfact = new GeometryFactory();
		LinearRing iring; Polygon hole;
		LinearRing outer = geomfact.createLinearRing(poly.getExteriorRing().getCoordinates());
		ArrayList<LinearRing> inners = new ArrayList<LinearRing>();
		for(int i=0; i<poly.getNumInteriorRing(); i++) {
			iring = geomfact.createLinearRing(poly.getInteriorRingN(i).getCoordinates());
			hole = geomfact.createPolygon(iring, null);
			if(hole.getArea() > minsize) inners.add(iring);
		}
		LinearRing[] innersA = new LinearRing[inners.size()];
		int i=0;
		for(LinearRing lr : inners) {
			innersA[i] = lr;
			i++;
		}
		return geomfact.createPolygon(outer, innersA);
	}
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("AmalgamateABuiltUpArea", "neun", "support",
				"",
				"AmalgamateABuiltUpArea",
				"Creates a built up area polygon from partitions and dead end roads",
				"1.0");
		
		//add input parameters
		String[] allowedP = {"Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedP), "partition polygons");
		String[] allowedS = {"LineString"};
		id.addInputParameter("congeom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedS), "all streets (with dead ends)");
		id.addInputParameter("minsize", "DOUBLE", 200.0, 0.0, Double.POSITIVE_INFINITY, "building minimum distance");
		id.addInputParameter("mindist", "DOUBLE", 10.0, 0.0, Double.POSITIVE_INFINITY, "building minimum distance");
		id.addInputParameter("roaddist", "DOUBLE", 5.0, 0.0, Double.POSITIVE_INFINITY, "street minimum distance");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}