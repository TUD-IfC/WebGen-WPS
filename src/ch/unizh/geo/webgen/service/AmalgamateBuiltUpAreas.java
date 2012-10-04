package ch.unizh.geo.webgen.service;

import java.util.HashMap;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.server.WebGenRequestExecuter;

import com.vividsolutions.jump.feature.FeatureCollection;

public class AmalgamateBuiltUpAreas extends AWebGenAlgorithm implements IWebGenAlgorithm {
	
	public void run(WebGenRequest wgreq) {
		FeatureCollection streets = wgreq.getFeatureCollection("congeom");
		FeatureCollection buildings = wgreq.getFeatureCollection("geom");
		double minsize = wgreq.getParameterDouble("minsize");
		double mindist = wgreq.getParameterDouble("mindist");
		double roaddist = wgreq.getParameterDouble("roaddist");
		
		
		//partioning support service
    	WebGenRequest preq = new WebGenRequest();
    	preq.addFeatureCollection("geom", buildings);
    	preq.addFeatureCollection("congeom", streets);
    	(new AreaPartitioningFlexible()).run(preq);
    	HashMap fc_partitions = preq.getResults();
    	
    	
    	ConstrainedFeatureCollection result_all = new ConstrainedFeatureCollection(buildings.getFeatureSchema());
    	// Schleife über Partionen    	
    	int partition_cnt = (int)fc_partitions.size()/2;
    	for(int i=0; i<partition_cnt; i++) {
    		ConstrainedFeatureCollection partgeom = (ConstrainedFeatureCollection)fc_partitions.get("partition" + (i+1));
    		ConstrainedFeatureCollection partcongeom = (ConstrainedFeatureCollection)fc_partitions.get("partpoly" + (i+1));
    		HashMap tparams = new HashMap();
    		tparams.put("minsize", minsize);
    		tparams.put("mindist", mindist);
    		tparams.put("roaddist", roaddist);
    		tparams.put("geom", partgeom);
    		tparams.put("congeom", partcongeom);
    		WebGenRequest twgreq = WebGenRequestExecuter.callService(tparams, "localcloned", "AmalgamateABuiltUpArea");
    		ConstrainedFeatureCollection result = (ConstrainedFeatureCollection)twgreq.getParameter("result");
    		if(result == null ) result = (ConstrainedFeatureCollection)twgreq.getResult("result");
    		if(result != null ) {
     			result_all.addAll(result.getFeatures());
        	}
    	}
		
		wgreq.addResult("result", result_all);
	}
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("AmalgamateBuiltUpAreas", "neun", "support",
				"",
				"AmalgamateBuiltUpAreas",
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