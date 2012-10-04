package ch.unizh.geo.webgen.service;

import java.util.HashMap;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.registry.ParameterDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jump.feature.FeatureCollection;

public class ProcessBuildings extends AWebGenAlgorithm implements IWebGenAlgorithm {
	
	//static int NbrOperations = ProcessingTools.NbrOperations;
	//static int NbrConstraints = ProcessingTools.NbrConstraints;
	
	String webgenserver = "localcloned";
	HashMap<String,Object> globalParameters = new HashMap<String,Object>();
	
	public void run(WebGenRequest wgreq) {
		webgenserver = wgreq.getParameter("webgenserver").toString();
		globalParameters.put("webgenserver", webgenserver);
		globalParameters.put("parallel", wgreq.getParameter("parallel"));
		globalParameters.put("minarea", wgreq.getParameter("minarea"));
		globalParameters.put("minlength", wgreq.getParameter("minlength"));
		globalParameters.put("mindist", wgreq.getParameter("mindist"));
		globalParameters.put("roaddist", wgreq.getParameter("roaddist"));
		
		ConstrainedFeatureCollection geom; ConstrainedFeatureCollection congeom;
		try {
			geom = (ConstrainedFeatureCollection)wgreq.getParameter("geom");
			congeom = (ConstrainedFeatureCollection)wgreq.getParameter("congeom");
		}
		catch (Exception e) {
			geom = new ConstrainedFeatureCollection((FeatureCollection)wgreq.getParameter("geom"));
			congeom = new ConstrainedFeatureCollection((FeatureCollection)wgreq.getParameter("congeom"));
		}
		
		//esult_all contains finally all generalized buildings
    	ConstrainedFeatureCollection result_all = new ConstrainedFeatureCollection(geom.getFeatureSchema());
		
		//partioning support service
    	WebGenRequest preq = new WebGenRequest();
    	preq.addFeatureCollection("geom", geom);
    	preq.addFeatureCollection("congeom", congeom);
    	(new AreaPartitioningFlexible()).run(preq);
    	HashMap fc_partitions = preq.getResults();
    	
    	// Schleife über Partionen    	
    	int partition_cnt = (int)fc_partitions.size()/2;
    	for(int i=0; i<partition_cnt; i++) {
    		//
    	}
		
    	
		wgreq.addResult("result", result_all);
	}
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("ProcessBuildings", "neun", "processing",
				"",
				"ProcessBuildings",
				"Split and Process Partitions with different search algorithms (as presented in GeoInformatica)",
				"1.0");
		id.visible = true;
		
		//add input parameters
		ParameterDescription webgenserver = new ParameterDescription("webgenserver", "STRING", "localcloned", "webgenserver");
		webgenserver.addSupportedValue("localcloned");
		webgenserver.addSupportedValue("http://webgen.geo.uzh.ch/webgen/execute");
		webgenserver.setChoiced();
		id.addInputParameter(webgenserver);
		String[] allowedGeom = {"Polygon"};
		String[] allowedConGeom = {"LineString","Polygon"};
		id.addInputParameter("parallel", "BOOLEAN", "false", "execute in parallel or sequential");
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedGeom), "buildings");
		id.addInputParameter("congeom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedConGeom), "roads");
		id.addInputParameter("minarea", "DOUBLE", "306.0", "minarea");
		id.addInputParameter("minlength", "DOUBLE", "10.0", "minlength");
		id.addInputParameter("mindist", "DOUBLE", "10.0", "mindist");
		id.addInputParameter("roaddist", "DOUBLE", "15.0", "roaddist");
		
		ParameterDescription searchalgorithm = new ParameterDescription("search algorithm", "STRING", "gradient std", "search algorithms used in the processing strategy");
		searchalgorithm.addSupportedValue("gradient std");
		searchalgorithm.addSupportedValue("simulated anealing");
		searchalgorithm.addSupportedValue("recursive genetic");
		searchalgorithm.addSupportedValue("recursive fully");
		searchalgorithm.addSupportedValue("recursive gradient");
		searchalgorithm.addSupportedValue("recursive 2deep");
		searchalgorithm.addSupportedValue("simulated anealing 1,3");
		searchalgorithm.addSupportedValue("simulated anealing 3");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
