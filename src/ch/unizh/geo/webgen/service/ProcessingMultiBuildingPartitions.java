package ch.unizh.geo.webgen.service;

import java.util.HashMap;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.registry.ParameterDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.service.parallel.ProcessingPartitionThread;
import ch.unizh.geo.webgen.service.parallel.ProcessingSettings2Deep;

import com.vividsolutions.jump.feature.FeatureCollection;

public class ProcessingMultiBuildingPartitions extends AWebGenAlgorithm implements IWebGenAlgorithm {
		
	String webgenserver = "local";
	HashMap<String,Object> globalParameters = new HashMap<String,Object>();
	
	public void run(WebGenRequest wgreq) {
		String operatorNb = wgreq.getParameter("operatorNb").toString();
		if(operatorNb.equals("7 operators (1 deep)")) ProcessingSettings2Deep.NbrOperations = 7;
		else if(operatorNb.equals("56 operators (2 deep)")) ProcessingSettings2Deep.NbrOperations = 56;
		webgenserver = wgreq.getParameter("webgenserver").toString();
		globalParameters.put("webgenserver", webgenserver);
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
			//this.addError("please submit only ConstrainedFeatureCollections");
			//return;
		}
		
		
		//result_all contains finally all generalized buildings
    	ConstrainedFeatureCollection result_all = new ConstrainedFeatureCollection(geom.getFeatureSchema());
    	HashMap<String,Object> other_results = new HashMap<String,Object>();
    	other_results.put("statistics", "part\tinitalCost\tfinalCost\n");
		
		//partioning support service
    	WebGenRequest preq = new WebGenRequest();
    	preq.addFeatureCollection("geom", geom);
    	preq.addFeatureCollection("congeom", congeom);
    	(new AreaPartitioningFlexible()).run(preq);
    	//preq.addFeatureCollection("selection", congeom);
    	//(new AreaPartitioning()).run(preq);
    	HashMap fc_partitions = preq.getResults();
    	
    	// Schleife über Partionen    	
    	int partition_cnt = (int)fc_partitions.size()/2;
    	ProcessingPartitionThread[] threads = new ProcessingPartitionThread[partition_cnt];
    	//int[] iterationNr = new int[partition_cnt];
    	for(int i=0; i<partition_cnt; i++) {
    		//get hashmap entry
    		//LOGGER.info("Processing partition" + (i+1) + "...");
    		System.out.println("\nProcessing partition" + (i+1) + "...");
    		ConstrainedFeatureCollection partgeom = (ConstrainedFeatureCollection)fc_partitions.get("partition" + (i+1));
    		ConstrainedFeatureCollection partcongeom = (ConstrainedFeatureCollection)fc_partitions.get("partpoly" + (i+1));
    		HashMap<String,Object> tparams = new HashMap<String,Object>();
    		tparams.putAll(this.globalParameters);
    		tparams.put("geom", partgeom);
    		tparams.put("congeom", partcongeom);
    		threads[i] = new ProcessingPartitionThread(i+1, tparams, webgenserver, result_all, other_results);
	 		threads[i].start();
	 		try {
				threads[i].join(); // make it non parallel
			} catch (InterruptedException e) {}
			System.gc();
    	}
    	/*for(int i=0; i<partition_cnt; i++) {
    		try {
				threads[i].join();
			} catch (InterruptedException e) {}
    	}*/
		
    	System.out.println("\noperators ("+operatorNb+"):\n" + other_results.get("usedOperators").toString()+"\n\n");
    	System.out.println("\nstatistics ("+operatorNb+"):\n" + other_results.get("statistics").toString()+"\n");
		//WebGenRequest resreq = WebGenRequestExecuter.callService(wgreq.getParameters(), "http://141.30.137.195:8080/webgen_core/execute", "BufferFeatures");
		//wgreq.addResult("result", resreq.getParameter("result"));
		wgreq.addResult("result", result_all);
		//wgreq.addResults(other_results);
	}
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("ProcessingMultiBuildingPartitions", "neun", "processing",
				"",
				"ProcessingMultiBuildingPartitions",
				"Split and Process Partitions",
				"1.0");
		
		//add input parameters
		ParameterDescription webgenserver = new ParameterDescription("webgenserver", "STRING", "localcloned", "webgenserver");
		webgenserver.addSupportedValue("localcloned");
		webgenserver.addSupportedValue("http://141.30.137.195:8080/webgen_core/execute");
		webgenserver.addSupportedValue("http://loclhost:8080/webgen/execute");
		webgenserver.addSupportedValue("http://loclhost:8383/execute");
		webgenserver.setChoiced();
		id.addInputParameter(webgenserver);
		ParameterDescription operatorNb = new ParameterDescription("operatorNb", "STRING", "7 operators (1 deep)", "webgenserver");
		operatorNb.addSupportedValue("7 operators (1 deep)");
		operatorNb.addSupportedValue("56 operators (2 deep)");
		operatorNb.setChoiced();
		id.addInputParameter(operatorNb);
		String[] allowedGeom = {"Polygon"};
		String[] allowedConGeom = {"LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedGeom), "buildings");
		id.addInputParameter("congeom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedConGeom), "roads");
		id.addInputParameter("minarea", "DOUBLE", "200.0", "minarea");
		id.addInputParameter("minlength", "DOUBLE", "10.0", "minlength");
		id.addInputParameter("mindist", "DOUBLE", "10.0", "mindist");
		id.addInputParameter("roaddist", "DOUBLE", "0.0", "roaddist");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
