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
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.server.WebGenRequestExecuter;
import ch.unizh.geo.webgen.tools.ProcessingTools;

public class ProcessBuildingPartitionSequential extends AWebGenAlgorithm implements IWebGenAlgorithm {
	
	static int NbrOperations = ProcessingTools.NbrOperations;
	static int NbrConstraints = ProcessingTools.NbrConstraints;
	
	String webgenserver = "local";
	HashMap<String,Object> globalParameters = new HashMap<String,Object>();
	
	public void run(WebGenRequest wgreq) {
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
		
		//initialisiere den CollectionConstraint bei geom mit der bwRatio der Partition
		geom.initCollectionConstraint(congeom);
		
    	// Zuweisung von feature - constraint - operation als Trainingsersatz 
    	//ConstraintSpace trainedConstraintSpace = trainConstraintSpace();
    	//ConstraintSpace trainedConstraintSpace = openConstraintSpace();
		
		//moritz GenOL zwei Constraint arrays fuer vorher und nachher
		Double[] genolbefore = null;
		Double[] genoldiff = null;
		String genoloperationbefore = null;
		HashMap<String,Vector<Double[]>> genolopstat = new HashMap<String,Vector<Double[]>>();
		Vector<Double[]> genolactop;
		
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
				//ProcessingTools.makeHistoryStep(geom, "Iteration " + iterations + " - Evaluation before");
				geom.makeConstraintHistoryStep("Iteration " + iterations + " - Evaluation before");
			}
			
	    	Vector operationVectorBefore = ProcessingTools.getConstraintSpace().getOperationVectorFromFeatureCost(featureCostVec);
			Vector featureCollectionVector = processPartition(partwgreq, ProcessingTools.getConstraintSpace(), featureCostVec);
			Double[] featureOperationVec = ProcessingTools.getFeatureOperationVector(featureCollectionVector);
			ProcessingTools.getConstraintSpace().addFeatureCostAndOperation(featureCostVec, featureOperationVec);							
			Collections.sort(featureCollectionVector, new ConstrainedFeatureCollectionSortedComparator());
			
			//gebe alle resultate aus
			/*for(Iterator iter=featureCollectionVector.iterator(); iter.hasNext();) {
				ConstrainedFeatureCollectionSorted cfcs = (ConstrainedFeatureCollectionSorted) iter.next();
				context.addLayer(StandardCategoryNames.WORKING, "part"+i+" "+cfcs.getOperation() + " " + cfcs.getCost(), cfcs.getFeatureCollection());
			}*/
			
			//moritz GenOL
	   		if(genolbefore != null) {
	   			genoldiff = new Double[NbrConstraints];
	   			for(int j = 0; j< featureCostVec.length; j++) {
	   				genoldiff[j] = new Double(genolbefore[j].doubleValue() - featureCostVec[j].doubleValue());
	   			}
	   			if(genolopstat.containsKey(genoloperationbefore)) {
	   				//@SuppressWarnings("unchecked")
		   			genolactop = genolopstat.get(genoloperationbefore);
		   			genolactop.add(genoldiff);
		   		}
	   			else {
	   				genolactop = new Vector<Double[]>();
	   				genolactop.add(genoldiff);
	   				genolopstat.put(genoloperationbefore, genolactop);
	   			}
	   		}
	   		genolbefore = featureCostVec;
   			genoloperationbefore = ((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(0)).getOperation();
			
			double costAllNew = 0.0;
			if(featureCollectionVector.size() > 0) {
				costAllNew = ((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(0)).getCost();
    			if(costAllCurrent - costAllNew > 0) {
    				geom = (ConstrainedFeatureCollection)((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(0)).getFeatureCollection();
    				partwgreq.addParameter("geom", geom);
    				geom.makeConstraintHistoryStep("Operation " + ((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(0)).getOperation()+ " - Evaluation after");
    		   		DecimalFormat df = new DecimalFormat("#0.0000");				
    		   		LOGGER.info("Operation  >>> " + ((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(0)).getOperation() + " <<<  successful (" + df.format(costAllNew) + ")");

        			// Vergleich der Costen pro Operator für ähnliche und aktuelle Partion
        	    	Vector operationVectorAfter = ProcessingTools.getConstraintSpace().getOperationVectorFromFeatureCost(featureCostVec);
        	    	ProcessingTools.evaluatePrognose(operationVectorBefore, operationVectorAfter);
        			
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
		
		wgreq.addResult("result", partwgreq.getParameter("geom"));
	}
	
	
	public Vector<ConstrainedFeatureCollectionSorted> processPartition(WebGenRequest wgreq, ConstraintSpace trainedConstraintSpace, Double[] partionCost) {
    	Vector<ConstrainedFeatureCollectionSorted> sortedFeatureCollectionVector = new Vector<ConstrainedFeatureCollectionSorted>();
    	HashMap<String,Object> parameters = wgreq.getParameters();
    	//ConstrainedFeatureCollection geom = (ConstrainedFeatureCollection) wgreq.getParameter("geom");
    	ConstrainedFeatureCollection fcNew = null;
    	//WebGenRequest twgreq = null;
		//alle Operationen ausführen für die das Vergleichsfeature Kosten senken konnte
    	Vector operationSequence = ProcessingTools.getConstraintSpace().getOperationVectorFromFeatureCost(partionCost);
    	//System.out.println("Operator-Reihenfolge: " + operationSequence.toString());
    	for(int testedOperations = 0; testedOperations < NbrOperations; testedOperations++) {
        	int operation = ((ConstraintSpaceArrayItem)operationSequence.get(testedOperations)).getId();
        	String operationName = ProcessingTools.lookupOperation(operation);
        	fcNew = ProcessingTools.executeOperation(operation, parameters, webgenserver);
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
    	return sortedFeatureCollectionVector;
    }
	
	
	private Double[] evalPartitions(WebGenRequest wgreq) {
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
	
	
	public ConstraintSpace trainConstraintSpace() {
    	ConstraintSpace trainedConstraintSpace = new ConstraintSpace(NbrOperations, NbrConstraints);
    	//MinSize - 0; MinLength - 1; MinDist - 2; LocalWidth - 3
    	Double[] featureCost0 = new Double[NbrConstraints];
    	for(int i=0; i<NbrConstraints;i++) featureCost0[i] = new Double(1.0); // constraint values for feature 1
    	
    	// Scaling - 0; BuildingSimplification - 1, BuildingEnlargeToRectangle - 2; Displacement - 3; Typification - 4
    	Double[] featureOperat0 = new Double[NbrOperations];
    	for(int i=0; i<NbrOperations;i++) featureOperat0[i] = new Double(1.0); // constaint value diff of feature 1 for several operations
    	trainedConstraintSpace.addFeatureCostAndOperation(featureCost0, featureOperat0);
    	return(trainedConstraintSpace);
    }
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("ProcessBuildingPartitionSequential", "neun", "processing",
				"",
				"ProcessBuildingPartitionSequential",
				"Process Partition Sequential",
				"1.0");
		
		//add input parameters
		id.addInputParameter("webgenserver", "STRING", "http://141.30.137.195:8080/webgen_core/execute", "webgenserver");
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
