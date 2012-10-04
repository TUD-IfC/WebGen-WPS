package ch.unizh.geo.webgen.service.parallel;

import java.util.HashMap;
import java.util.Vector;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollectionSorted;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.server.WebGenRequestExecuter;
import ch.unizh.geo.webgen.service.Eval_GMFeat_All;

public class RemoteOperatorThread extends Thread {
	
	HashMap parameters;
	String webgenserver;
	String operation;
	String operationName;
	Vector sortedFeatureCollectionVector;
	
	
	public RemoteOperatorThread(HashMap parameters, String webgenserver, String operation, String operationName, Vector sortedFeatureCollectionVector) {
		this.parameters = parameters;
		this.webgenserver = webgenserver;
		this.operation = operation;
		this.operationName = operationName;
		this.sortedFeatureCollectionVector = sortedFeatureCollectionVector;
	}
	
	public void run() {
		WebGenRequest twgreq = WebGenRequestExecuter.callService(parameters, webgenserver, operation);
		ConstrainedFeatureCollection fcNew = (ConstrainedFeatureCollection)twgreq.getParameter("result");
		if(fcNew == null ) fcNew = (ConstrainedFeatureCollection)twgreq.getResult("result");
 		if(fcNew != null ) {
    		//LOGGER.info("Evaluation for - " + operationName);
 			WebGenRequest ewgreq = new WebGenRequest();
 			ewgreq.addParameters(parameters);
 			ewgreq.addFeatureCollection("geom", fcNew);
			Double[] costVec = evalPartitions(ewgreq);
			double costAllCurrent = getCostFromCostVector(costVec);            		
			ConstrainedFeatureCollectionSorted sortedFeatureCollection = new ConstrainedFeatureCollectionSorted(costAllCurrent, fcNew, operationName);
			sortedFeatureCollectionVector.add(sortedFeatureCollection);
    	}
	}
	
	private Double[] evalPartitions(WebGenRequest wgreq) {
    	(new Eval_GMFeat_All()).run(wgreq);
    	Double[] partitionCost = (Double[])wgreq.getResult("severities");
    	for(int i=4; i<8; i++) partitionCost[i] *= 0.25;
    	return partitionCost;
    }
	
	private double getCostFromCostVector(Double[] costVec) {
    	double costAll = 0.0;
    	for(int i=0; i<costVec.length; i++) {
    		costAll += costVec[i].doubleValue();
    	}
    	return costAll;    
    }
	
}
