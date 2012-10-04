package ch.unizh.geo.webgen.service;

import java.text.DecimalFormat;
import java.util.HashMap;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.registry.ParameterDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.server.WebGenRequestExecuter;
import ch.unizh.geo.webgen.tools.ProcessingTools;

import com.vividsolutions.jump.feature.FeatureCollection;

public class BuildingProcessing extends AWebGenAlgorithm implements IWebGenAlgorithm {
	
	//static int NbrOperations = ProcessingTools.NbrOperations;
	//static int NbrConstraints = ProcessingTools.NbrConstraints;
	
	String webgenserver = "local";
	HashMap<String,Object> globalParameters = new HashMap<String,Object>();
	boolean parallel = false;
	
	public void run(WebGenRequest wgreq) {
		ProcessingTools.statisticsReset();
		
		webgenserver = wgreq.getParameter("webgenserver").toString();
		globalParameters.put("webgenserver", webgenserver);
		globalParameters.put("minarea", wgreq.getParameter("minarea"));
		globalParameters.put("minlength", wgreq.getParameter("minlength"));
		globalParameters.put("mindist", wgreq.getParameter("mindist"));
		globalParameters.put("roaddist", wgreq.getParameter("roaddist"));
		globalParameters.put("parallel", wgreq.getParameter("parallel"));
		parallel = wgreq.getParameterBoolean("parallel");
		globalParameters.put("search method", wgreq.getParameter("search method"));
		for(String opn : ProcessingTools.operationNameArray) {
			globalParameters.put("use " + opn, wgreq.getParameter("use " + opn));
		}
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
		
		//partioning support service
    	WebGenRequest preq = new WebGenRequest();
    	preq.addFeatureCollection("geom", geom);
    	preq.addFeatureCollection("congeom", congeom);
    	(new AreaPartitioningFlexible()).run(preq);
    	HashMap fc_partitions = preq.getResults();
    	
    	// Schleife über Partionen (parallel or sequential)
    	int partition_cnt = (int)fc_partitions.size()/2;
    	ProcessingTools.nbrPartitions = partition_cnt;
    	if(parallel) {
        	PartitionThread[] threads = new PartitionThread[partition_cnt];
        	//int[] iterationNr = new int[partition_cnt];
        	for(int i=0; i<partition_cnt; i++) {
        		//LOGGER.info("Processing partition" + (i+1) + "...");
        		ProcessingTools.prognoseOut.append("\n");
        		System.out.println("Processing partition" + (i+1) + "...");
        		ConstrainedFeatureCollection partgeom = (ConstrainedFeatureCollection)fc_partitions.get("partition" + (i+1));
        		ConstrainedFeatureCollection partcongeom = (ConstrainedFeatureCollection)fc_partitions.get("partpoly" + (i+1));
        		HashMap<String,Object> tparams = new HashMap<String,Object>();
        		tparams.putAll(this.globalParameters);
        		tparams.put("geom", partgeom);
        		tparams.put("congeom", partcongeom);
        		tparams.put("partitionNr", i);
        		threads[i] = new PartitionThread(tparams, webgenserver, result_all);
    	 		threads[i].start();
        	}
        	for(int i=0; i<partition_cnt; i++) {
        		try {
    				threads[i].join();
    			} catch (InterruptedException e) {}
        	}
    	}
    	else {
        	for(int i=0; i<partition_cnt; i++) {
        		//LOGGER.info("Processing partition" + (i+1) + "...");
        		ProcessingTools.prognoseOut.append("\n");
        		System.out.println("Processing partition" + (i+1) + "...");
        		ConstrainedFeatureCollection partgeom = (ConstrainedFeatureCollection)fc_partitions.get("partition" + (i+1));
        		ConstrainedFeatureCollection partcongeom = (ConstrainedFeatureCollection)fc_partitions.get("partpoly" + (i+1));
        		HashMap<String,Object> tparams = new HashMap<String,Object>();
        		tparams.putAll(this.globalParameters);
        		tparams.put("geom", partgeom);
        		tparams.put("congeom", partcongeom);
        		tparams.put("partitionNr", i);
        		try {
        			WebGenRequest twgreq = WebGenRequestExecuter.callService(tparams, webgenserver, "BuildingProcessing_SinglePartition");
            		ConstrainedFeatureCollection result = (ConstrainedFeatureCollection)twgreq.getParameter("result");
            		if(result == null ) result = (ConstrainedFeatureCollection)twgreq.getResult("result");
            		if(result != null ) {
                		//LOGGER.info("Evaluation for - " + operationName);
            			//wgreq.addResult("result partition " + (i+1), result);
             			result_all.addAll(result.getFeatures());
                	}
        		}
        		catch(Exception e) {}
        	}
    	}
		

		wgreq.addResult("result", result_all);
		wgreq.addResult("initial cost", new Double(ProcessingTools.initalCostSum/ProcessingTools.nbrPartitions));
		wgreq.addResult("final cost", new Double(ProcessingTools.finalCostSum/ProcessingTools.nbrPartitions));
		wgreq.addResult("iterations", new Integer(ProcessingTools.totalNbrIterations-ProcessingTools.nbrPartitions));
		ProcessingTools.writePrognose();
		ProcessingTools.saveConstraintSpace();
		
		DecimalFormat df = new DecimalFormat("#0.00000000");
		System.out.println("\n\nStatistics: ");
		System.out.println("cost before; minsize; minlength; mindist; minlocaldist; diffpos; difforient; diffedgecount; diffwlratio; cost after; minsize; minlength; mindist; minlocaldist; diffpos; difforient; diffedgecount; diffwlratio");
		System.out.println(ProcessingTools.globalStat);
		System.out.println("Search method: "+wgreq.getParameter("search method"));
		System.out.println("Total Initial Cost: "+df.format(ProcessingTools.initalCostSum/ProcessingTools.nbrPartitions));
		System.out.println("Total Final Cost:   "+df.format(ProcessingTools.finalCostSum/ProcessingTools.nbrPartitions));
		System.out.println("Total Number of Iterations: "+(ProcessingTools.totalNbrIterations-ProcessingTools.nbrPartitions)+"\n\n");
	}
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("BuildingProcessing", "neun", "processing",
				"",
				"BuildingProcessing",
				"Split and Process Building Partition, Sequential or Parallel",
				"1.0");
		id.visible = true;
		
		//add input parameters
		ParameterDescription webgenserver = new ParameterDescription("webgenserver", "STRING", "localcloned", "webgenserver");
		webgenserver.addSupportedValue("localcloned");
		webgenserver.addSupportedValue("http://141.30.137.195:8080/webgen_core/execute");
		webgenserver.addSupportedValue("http://loclhost:8080/webgen/execute");
		webgenserver.addSupportedValue("http://loclhost:8383/execute");
		webgenserver.setChoiced();
		id.addInputParameter(webgenserver);
		String[] allowedGeom = {"Polygon"};
		String[] allowedConGeom = {"LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedGeom), "buildings");
		id.addInputParameter("congeom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedConGeom), "roads");
		id.addInputParameter("minarea", "DOUBLE", "200.0", "minarea");
		id.addInputParameter("minlength", "DOUBLE", "10.0", "minlength");
		id.addInputParameter("mindist", "DOUBLE", "10.0", "mindist");
		id.addInputParameter("roaddist", "DOUBLE", "5.0", "roaddist");
		
		id.addInputParameter("parallel", "BOOLEAN", "false", "execute multithreaded");
		
		ParameterDescription searchmethod = new ParameterDescription("search method", "STRING", "gradient std", "search method");
		searchmethod.addSupportedValue("gradient std");
		searchmethod.addSupportedValue("simulated anealing");
		searchmethod.addSupportedValue("gradient random");
		searchmethod.addSupportedValue("recursive 1deep");
		searchmethod.addSupportedValue("recursive 2deep");
		searchmethod.addSupportedValue("recursive 3deep");
		searchmethod.addSupportedValue("recursive 4deep");
		searchmethod.addSupportedValue("recursive 5deep");
		searchmethod.setChoiced();
		id.addInputParameter(searchmethod);
		
		for(String opn : ProcessingTools.operationNameArray) {
			id.addInputParameter("use " + opn, "BOOLEAN", "true", "execute multithreaded");
		}
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}


class PartitionThread extends Thread {
	
	HashMap<String,Object> parameters;
	String webgenserver;
	ConstrainedFeatureCollection result_all;
	
	public PartitionThread(HashMap<String,Object> parameters, String webgenserver, ConstrainedFeatureCollection result_all) {
		this.parameters = parameters;
		this.webgenserver = webgenserver;
		this.result_all = result_all;
	}
	
	public void run() {
 		try {
			WebGenRequest twgreq = WebGenRequestExecuter.callService(parameters, webgenserver, "BuildingProcessing_SinglePartition");
    		ConstrainedFeatureCollection result = (ConstrainedFeatureCollection)twgreq.getParameter("result");
    		if(result == null ) result = (ConstrainedFeatureCollection)twgreq.getResult("result");
    		if(result != null ) {
        		//LOGGER.info("Evaluation for - " + operationName);
     			result_all.addAll(result.getFeatures());
        	}
		}
		catch(Exception e) {}
	}
	
}
