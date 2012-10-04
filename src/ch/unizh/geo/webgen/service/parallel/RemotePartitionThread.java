package ch.unizh.geo.webgen.service.parallel;

import java.util.HashMap;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.server.WebGenRequestExecuter;

public class RemotePartitionThread extends Thread {
	
	HashMap parameters;
	String webgenserver;
	ConstrainedFeatureCollection result_all;
	
	
	public RemotePartitionThread(HashMap parameters, String webgenserver, ConstrainedFeatureCollection result_all) {
		this.parameters = parameters;
		this.webgenserver = webgenserver;
		this.result_all = result_all;
	}
	
	public void run() {
		WebGenRequest twgreq = WebGenRequestExecuter.callService(parameters, webgenserver, "ProcessBuildingPartitionParallel");
		ConstrainedFeatureCollection result = (ConstrainedFeatureCollection)twgreq.getParameter("result");
		if(result == null ) result = (ConstrainedFeatureCollection)twgreq.getResult("result");
 		if(result != null ) {
    		//LOGGER.info("Evaluation for - " + operationName);
 			result_all.addAll(result.getFeatures());
    	}
	}
}
