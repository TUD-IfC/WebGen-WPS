package ch.unizh.geo.webgen.service.parallel;

import java.util.HashMap;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.server.WebGenRequestExecuter;

public class ProcessingPartitionThread extends Thread {
	
	int partNb;
	HashMap<String,Object> parameters;
	String webgenserver;
	HashMap<String,Object> otherresults;
	ConstrainedFeatureCollection result_all;
	
	
	public ProcessingPartitionThread(int partNb, HashMap<String,Object> parameters, String webgenserver, ConstrainedFeatureCollection result_all, HashMap<String,Object> otherresults) {
		this.partNb = partNb;
		this.parameters = parameters;
		this.webgenserver = webgenserver;
		this.result_all = result_all;
		this.otherresults = otherresults;
	}
	
	public void run() {
		WebGenRequest twgreq = WebGenRequestExecuter.callService(parameters, webgenserver, "ProcessingSingleBuildingPartition");
		ConstrainedFeatureCollection result = (ConstrainedFeatureCollection)twgreq.getParameter("result");
		if(result == null ) result = (ConstrainedFeatureCollection)twgreq.getResult("result");
 		if(result != null ) {
    		//LOGGER.info("Evaluation for - " + operationName);
 			otherresults.put("result part"+partNb,result);
 			String statistics = otherresults.get("statistics").toString();
 			statistics = statistics + partNb + "\t" + twgreq.getResult("initalCost") + "\t" + twgreq.getResult("finalCost")+"\n";
 			otherresults.put("statistics", statistics);
 			otherresults.put("usedOperators", otherresults.get("usedOperators") + "\n" + twgreq.getResult("usedOperators"));
 			result_all.addAll(result.getFeatures());
    	}
 		System.gc();
	}
}
