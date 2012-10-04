package ch.unizh.geo.webgen.service.parallel;

import java.util.HashMap;
import java.util.Vector;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollectionSorted;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.server.WebGenRequestExecuter;
import ch.unizh.geo.webgen.service.Eval_GMFeat_All_Constrained;

public class ProcessingOperatorThread extends Thread {
	
	HashMap<String, Object> parameters;
	ProcessingOperator operator;
	Vector sortedFeatureCollectionVector;
	
	
	public ProcessingOperatorThread(HashMap<String,Object> parameters, ProcessingOperator operator, Vector sortedFeatureCollectionVector) {
		this.parameters = parameters;
		this.operator = operator;
		this.parameters.putAll(operator.additionalParameters);
		this.sortedFeatureCollectionVector = sortedFeatureCollectionVector;
	}
	
	public void run() {
		WebGenRequest twgreq;
		ConstrainedFeatureCollection fcNew = null;
		for(String operatorClass : operator.operationClasses) {
			twgreq = WebGenRequestExecuter.callService(parameters, operator.server, operatorClass);
			fcNew = twgreq.getResultFeatureCollection("result");
			if(fcNew == null) fcNew = (ConstrainedFeatureCollection)parameters.get("geom");
			parameters.put("geom", fcNew);
		}
 		if(fcNew != null ) {
    		//LOGGER.info("Evaluation for - " + operationName);
 			WebGenRequest ewgreq = new WebGenRequest();
 			ewgreq.addParameters(parameters);
 			ewgreq.addFeatureCollection("geom", fcNew);
			Double[] costVec = evalPartitions(ewgreq);
			double costAllCurrent = getCostFromCostVector(costVec);            		
			ConstrainedFeatureCollectionSorted sortedFeatureCollection = new ConstrainedFeatureCollectionSorted(costAllCurrent, fcNew, operator.name);
			sortedFeatureCollectionVector.add(sortedFeatureCollection);
    	}
	}
	
	private Double[] evalPartitions(WebGenRequest wgreq) {
    	//(new Eval_GMFeat_All()).run(wgreq);
    	(new Eval_GMFeat_All_Constrained()).run(wgreq);
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
