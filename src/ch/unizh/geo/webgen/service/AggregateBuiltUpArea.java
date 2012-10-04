package ch.unizh.geo.webgen.service;

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
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;

public class AggregateBuiltUpArea extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		System.out.println("1 sha debug, AggregateBuiltUpArea.run()");
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		ConstrainedFeatureCollection buildings;
		if(fc instanceof ConstrainedFeatureCollection) buildings = (ConstrainedFeatureCollection) fc;
		else buildings = new ConstrainedFeatureCollection(fc);
		double minsize = wgreq.getParameterDouble("minsize");
		double mindist = wgreq.getParameterDouble("mindist");
		runAlgo(wgreq, buildings, minsize, mindist);
	}

	private void runAlgo(WebGenRequest wgreq, ConstrainedFeatureCollection buildings, double minsize, double mindist) {
		System.out.println("2 sha debug, AggregateBuiltUpArea.run()");
		FeatureSchema bfs = buildings.getFeatureSchema();

		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("geom", buildings);
		WebGenRequest twgreq = WebGenRequestExecuter.callService(parameters, "localcloned", "JTSUnionSingleLayer");
		FeatureCollection buildingsUnion = (FeatureCollection)twgreq.getResult("result");

		parameters.clear();
		parameters.put("geom", buildingsUnion);
		parameters.put("width", mindist);
		twgreq = WebGenRequestExecuter.callService(parameters, "localcloned", "BufferFeaturesRectified");
		FeatureCollection buildingsBuffered = (FeatureCollection)twgreq.getResult("result");
		//wgreq.addResult("result buffered", buildingsBuffered);

		parameters.clear();
		parameters.put("geom", buildingsBuffered);
		parameters.put("width", 0-mindist);
		parameters.put("quadrants", 2);
		twgreq = WebGenRequestExecuter.callService(parameters, "localcloned", "BufferFeatures");
		FeatureCollection buildingsUnBuffered = (FeatureCollection)twgreq.getResult("result");
		wgreq.addResult("result unbuffered q2", buildingsUnBuffered);

		//tests ----------------
		/*
		parameters.clear();
		parameters.put("geom", buildingsUnBuffered);
		double t = mindist*0.4;
		parameters.put("tolerance", t);
		twgreq = WebGenRequestExecuter.callService(parameters, "localcloned", "JGenAreaSimpleDouglasPeucker");
		FeatureCollection tests0 = (FeatureCollection)twgreq.getResult("result");
		wgreq.addResult("result q2 dp", tests0);

		parameters.clear();
		parameters.put("geom", buildingsUnBuffered);
		t = mindist*0.4;
		parameters.put("minlength", t);
		twgreq = WebGenRequestExecuter.callService(parameters, "localcloned", "JGenBuildSimpleStaufenbiel");
		FeatureCollection tests1 = (FeatureCollection)twgreq.getResult("result");
		wgreq.addResult("result q2 sb", tests1);

		parameters.clear();
		parameters.put("geom", buildingsUnBuffered);
		t = mindist*0.4;
		parameters.put("minLengtInM", t);
		parameters.put("Iterations", 20);
		twgreq = WebGenRequestExecuter.callService(parameters, "localcloned", "BuildingSimplifyOutline");
		FeatureCollection tests2 = (FeatureCollection)twgreq.getResult("result");
		wgreq.addResult("result q2 bs", tests2);

		parameters.clear();
		parameters.put("geom", buildingsBuffered);
		parameters.put("width", 0-mindist);
		parameters.put("quadrants", 1);
		twgreq = WebGenRequestExecuter.callService(parameters, "localcloned", "BufferFeatures");
		FeatureCollection buildingsUnBuffered1 = (FeatureCollection)twgreq.getResult("result");
		wgreq.addResult("result unbuffered q1", buildingsUnBuffered1);

		parameters.clear();
		parameters.put("geom", buildingsUnBuffered1);
		t = mindist*0.4;
		parameters.put("tolerance", t);
		twgreq = WebGenRequestExecuter.callService(parameters, "localcloned", "JGenAreaSimpleDouglasPeucker");
		FeatureCollection tests3 = (FeatureCollection)twgreq.getResult("result");
		wgreq.addResult("result q1 dp", tests3);

		parameters.clear();
		parameters.put("geom", buildingsUnBuffered1);
		t = mindist*0.4;
		parameters.put("minlength", t);
		twgreq = WebGenRequestExecuter.callService(parameters, "localcloned", "JGenBuildSimpleStaufenbiel");
		FeatureCollection tests4 = (FeatureCollection)twgreq.getResult("result");
		wgreq.addResult("result q1 sb", tests4);

		parameters.clear();
		parameters.put("geom", buildingsUnBuffered1);
		t = mindist*0.4;
		parameters.put("minLengtInM", t);
		parameters.put("Iterations", 20);
		twgreq = WebGenRequestExecuter.callService(parameters, "localcloned", "BuildingSimplifyOutline");
		FeatureCollection tests5 = (FeatureCollection)twgreq.getResult("result");
		wgreq.addResult("result q1 bs", tests5);
		*/

		ConstrainedFeatureCollection buildingsUnBufferedSplit = new ConstrainedFeatureCollection(bfs);
		buildingsUnBufferedSplit.setCollectionConstraint(buildings.getCollectionConstraint());
		for(Iterator iter = buildingsUnBuffered.iterator(); iter.hasNext();) {
			Feature feat = (Feature) iter.next();
			Geometry geom = feat.getGeometry();
			if(geom instanceof MultiPolygon) {
				MultiPolygon mpoly = (MultiPolygon) geom;
				for(int i=0; i < mpoly.getNumGeometries(); i++) {
					Geometry newgeom = mpoly.getGeometryN(i);
					ConstrainedFeature tf = getLargestPredecessor(buildings, newgeom).clone();
					tf.setGeometry(newgeom);
					buildingsUnBufferedSplit.add(tf);
				}
			}
			else {
				ConstrainedFeature tf = getLargestPredecessor(buildings, geom).clone();
				tf.setGeometry(geom);
				buildingsUnBufferedSplit.add(tf);
			}
		}

		parameters.clear();
		parameters.put("geom", buildingsUnBufferedSplit);
		double tolerance = mindist*0.4;
		parameters.put("tolerance", tolerance);
		twgreq = WebGenRequestExecuter.callService(parameters, "localcloned", "JGenAreaSimpleDouglasPeucker");
		FeatureCollection buildingsSimplified = (FeatureCollection)twgreq.getResult("result");
		wgreq.addResult("result", buildingsSimplified);
	}


	public ConstrainedFeature getLargestPredecessor(ConstrainedFeatureCollection buildings, Geometry newgeom) {
		ConstrainedFeature f; Geometry g; Double overlapArea;
		ConstrainedFeature largestF = null; Double largestOverlapArea = 0.0;
		for(Iterator iter = buildings.iterator(); iter.hasNext();) {
			f= (ConstrainedFeature) iter.next();
			g = f.getGeometry();
			try {
				overlapArea = g.intersection(newgeom).getArea();
			}
			catch (Exception e) {
				overlapArea = 0.0;
			}
			if(largestF == null) {
				largestF = f;
			}
			if(overlapArea >= largestOverlapArea) {
				largestF = f;
				largestOverlapArea = overlapArea;
			}
		}
		return largestF;
	}


	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("AggregateBuiltUpArea", "neun", "operator",
				"",
				"AggregateBuiltUpArea",
				"Creates a built up area polygon from buildings in a building block.",
				"1.0");
		id.visible = true;

		//add input parameters
		String[] allowedP = {"Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedP), "partition polygons");
		id.addInputParameter("minsize", "DOUBLE", 200.0, 0.0, Double.POSITIVE_INFINITY, "building minimum distance");
		id.addInputParameter("mindist", "DOUBLE", 10.0, 0.0, Double.POSITIVE_INFINITY, "building minimum distance");

		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}