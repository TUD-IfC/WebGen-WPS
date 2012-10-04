package ch.unizh.geo.webgen.service;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollectionSorted;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollectionSortedComparator;
import ch.unizh.geo.webgen.model.ConstraintSpace;
import ch.unizh.geo.webgen.model.ConstraintSpaceArrayItem;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.registry.ParameterDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.service.parallel.ProcessingOperator;
import ch.unizh.geo.webgen.service.parallel.ProcessingOperatorThread;
import ch.unizh.geo.webgen.service.parallel.ProcessingSettings2Deep;

public class ProcessingSingleBuildingPartition extends AWebGenAlgorithm implements IWebGenAlgorithm {
	
	//static int NbrOperations = ProcessingSettings2Deep.NbrOperations;
	//static int NbrConstraints = ProcessingSettings2Deep.NbrConstraints;
	
	String webgenserver = "local";
	HashMap<String,Object> globalParameters = new HashMap<String,Object>();
	
	public void run(WebGenRequest wgreq) {
		//NbrOperations = ProcessingSettings2Deep.NbrOperations;
		webgenserver = wgreq.getParameter("webgenserver").toString();
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
			this.addError("please submit only ConstrainedFeatureCollections");
			return;
		}
		
    	// Zuweisung von feature - constraint - operation als Trainingsersatz 
    	ConstraintSpace trainedConstraintSpace = ProcessingSettings2Deep.getConstraintSpace();
		
    	double initalCost = -1;
    	double finalCost = -1;
    	StringBuffer usedOperators = new StringBuffer();
    	
		//moritz GenOL zwei Constraint arrays fuer vorher und nachher
		/*Double[] genolbefore = null;
		Double[] genoldiff = null;
		String genoloperationbefore = null;
		HashMap genolopstat = new HashMap();
		Vector genolactop;*/
		
		WebGenRequest partwgreq = new WebGenRequest();
		partwgreq.addParameters(this.globalParameters);
		partwgreq.addParameter("geom", geom);
		partwgreq.addParameter("congeom", congeom);
		
		//Evaluation / Conflict analysis
		int iterations = 0;
		boolean run = true;
		while(run == true) {
			Double[] featureCostVec = evalPartitions(partwgreq);
			double costAllCurrent = getCostFromCostVector(featureCostVec);
			if(iterations == 0) {
				ProcessingSettings2Deep.makeHistoryStep(geom, "Eval original data (Cost: "+ costAllCurrent + ")");
				initalCost = costAllCurrent;
			}
			
	    	Vector operationVectorBefore = trainedConstraintSpace.getOperationVectorFromFeatureCost(featureCostVec);
			Vector featureCollectionVector = processPartition(partwgreq, trainedConstraintSpace, featureCostVec);
			Double[] featureOperationVec = ProcessingSettings2Deep.getFeatureOperationVector(featureCollectionVector);
			trainedConstraintSpace.addFeatureCostAndOperation(featureCostVec, featureOperationVec);							
			Collections.sort(featureCollectionVector, new ConstrainedFeatureCollectionSortedComparator());
			
			//gebe alle resultate aus
			/*for(Iterator iter=featureCollectionVector.iterator(); iter.hasNext();) {
				ConstrainedFeatureCollectionSorted cfcs = (ConstrainedFeatureCollectionSorted) iter.next();
				context.addLayer(StandardCategoryNames.WORKING, "part"+i+" "+cfcs.getOperation() + " " + cfcs.getCost(), cfcs.getFeatureCollection());
			}*/
			
			//moritz GenOL
	   		/*if(genolbefore != null) {
	   			genoldiff = new Double[NbrConstraints];
	   			for(int j = 0; j< featureCostVec.length; j++) {
	   				genoldiff[j] = new Double(genolbefore[j].doubleValue() - featureCostVec[j].doubleValue());
	   			}
	   			if(genolopstat.containsKey(genoloperationbefore)) {
		   			genolactop = (Vector) genolopstat.get(genoloperationbefore);
		   			genolactop.add(genoldiff);
		   		}
	   			else {
	   				genolactop = new Vector();
	   				genolactop.add(genoldiff);
	   				genolopstat.put(genoloperationbefore, genolactop);
	   			}
	   		}
	   		genolbefore = featureCostVec;
   			genoloperationbefore = ((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(0)).getOperation();
			*/
			
			double costAllNew = 0.0;
			if(featureCollectionVector.size() > 0) {
				costAllNew = ((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(0)).getCost();
    			if(costAllCurrent - costAllNew > 0) {
    				ConstrainedFeatureCollection fcNew = (ConstrainedFeatureCollection)((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(0)).getFeatureCollection();
    				partwgreq.addParameter("geom", fcNew);
    				ProcessingSettings2Deep.makeHistoryStep(fcNew, "Eval after " + ((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(0)).getOperation()+ " (Cost: "+ costAllNew + ")");
    				finalCost = costAllNew;
    		   		geom = fcNew;
    		   		DecimalFormat df = new DecimalFormat("#0.0000");
    		   		String successfulOp = ((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(0)).getOperation();
    		   		LOGGER.info("Operation  >>> " + successfulOp + " <<<  successful (" + df.format(costAllNew) + ")");
    		   		usedOperators.append(successfulOp+"\n");

        			// Vergleich der Costen pro Operator für ähnliche und aktuelle Partion
        	    	//Vector operationVectorAfter = trainedConstraintSpace.getOperationVectorFromFeatureCost(featureCostVec);
        	    	//ProcessingSettings2Deep.evaluatePrognose(operationVectorBefore, operationVectorAfter);
        			
        			// Correlation für Änderung der Constraints
        			/*if(iterations > 0) {
        				(new Eval_Constraint_Correlation_Diff()).run(partwgreq);
        				Matrix correlationMatrix = (Matrix)partwgreq.getResult("correlation");
        				showCorrelations(correlationMatrix);
        			}*/
        			
        			if(costAllNew < 0.001) // Abbruch, wenn costen quasi null
        				run = false;
    			} else run = false;        			
			} else
				run = false;
			
			if(iterations > 20) 
				run = false;
			iterations++;
		}// Ende while(run == true) 
		
		wgreq.addResult("initalCost", new Double(initalCost));
		wgreq.addResult("finalCost", new Double(finalCost));
		wgreq.addResult("usedOperators", usedOperators.toString());
		wgreq.addResult("result", partwgreq.getParameter("geom"));
	}
	
	
	public Vector processPartition(WebGenRequest wgreq, ConstraintSpace trainedConstraintSpace, Double[] partionCost) {
    	HashMap parameters = wgreq.getParameters();
    	Vector operationSequence = ProcessingSettings2Deep.getConstraintSpace().getOperationVectorFromFeatureCost(partionCost);
    	System.out.println("Operator-Reihenfolge: " + operationSequence.toString());
    	
    	Vector sortedFeatureCollectionVector = new Vector();
    	ProcessingOperatorThread[] threads = new ProcessingOperatorThread[ProcessingSettings2Deep.NbrOperations];
    	for(int testedOperations = 0; testedOperations < ProcessingSettings2Deep.NbrOperations; testedOperations++) {
    		HashMap<String,Object> tparams = (HashMap)parameters.clone();
        	int operation = ((ConstraintSpaceArrayItem)operationSequence.get(testedOperations)).getId();
        	ProcessingOperator operationSettings = ProcessingSettings2Deep.lookupOperation(operation);
        	threads[operation] = new ProcessingOperatorThread(tparams, operationSettings, sortedFeatureCollectionVector);
	 		threads[operation].start();
    	}
    	System.out.println("|-- executing 56 operators ------------------------------|");
    	System.out.print("|");
    	for(int i=0; i<ProcessingSettings2Deep.NbrOperations; i++) {
    		try {
				threads[i].join();
				System.out.print("-");
			} catch (InterruptedException e) {}
    	}
    	System.out.print("|\n");
    	return sortedFeatureCollectionVector;
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
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("ProcessingSingleBuildingPartition", "neun", "processing",
				"",
				"ProcessingSingleBuildingPartition",
				"Processing Single Building Partition",
				"1.0");
		
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
		id.addInputParameter("roaddist", "DOUBLE", "0.0", "roaddist");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
