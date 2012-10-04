package ch.unizh.geo.webgen.test.parallel;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollectionSorted;
import ch.unizh.geo.webgen.model.ConstraintSpaceArrayItem;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.service.Eval_GMFeat_All;
import ch.unizh.geo.webgen.tools.ProcessingTools;

import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public class PartitionThread extends Thread {
	
	private Logger LOGGER;

	int NbrOperations = ProcessingTools.NbrOperations;
	int NbrConstraints = ProcessingTools.NbrConstraints;
	
	OperatorThreadSimple[] threads = new OperatorThreadSimple[NbrOperations];
	
	ConstrainedFeatureCollection geom;
	ConstrainedFeatureCollection congeom;
	HashMap<String,Object> globalParameters;
	ConstrainedFeatureCollection result_all;
	PlugInContext context;
	Integer[] iterationNr;
	int partNr;
	HashMap<String, Vector<Double[]>> genolopstat;

	public PartitionThread(HashMap<String,Object> globalParameters, 
		    ConstrainedFeatureCollection geom, ConstrainedFeatureCollection congeom,
		    ConstrainedFeatureCollection result_all, PlugInContext context,
			Integer[] iterationNr, int partNr,
			HashMap<String, Vector<Double[]>> genolopstat) {
		this.globalParameters = globalParameters;
		this.geom = geom;
		this.congeom = congeom;
		this.result_all = result_all;
		this.context = context;
		this.iterationNr = iterationNr;
		this.partNr = partNr;
		this.genolopstat = genolopstat;
		
		//initOperatorThreads();
		
		LOGGER = Logger.getLogger("PartitionThread " + partNr);
		LOGGER.info("initializing complete");
    }

    public void run() {
    	LOGGER.info("run() started ...");
    	
		Double[] genolbefore = null;
		Double[] genoldiff = null;
		String genoloperationbefore = null;
		Vector<Double[]> genolactop;
		//ConstrainedFeatureCollection result = null;
		
    	//Evaluation / Conflict analysis
		int iterations = 0;
		boolean run = true;
		while(run == true) {
			LOGGER.info("run == true");
			WebGenRequest partreq = new WebGenRequest();
			partreq.addParameters(globalParameters);
			partreq.addParameter("geom", geom);
        	(new Eval_GMFeat_All()).run(partreq);
        	Double[] featureCostVec = (Double[])partreq.getResult("severities");
	    	for(int i=4; i<8; i++) featureCostVec[i] *= 0.25;
			double costAllCurrent = ProcessingTools.getCostFromCostVector(featureCostVec);
			if(iterations == 0) {
				//ProcessingTools.makeHistoryStep(geom, "Iteration " + iterations + " - Evaluation before");
				geom.makeConstraintHistoryStep("Iteration " + iterations + " - Evaluation before");
				}
	    	Vector operationVectorBefore = ProcessingTools.getConstraintSpace().getOperationVectorFromFeatureCost(featureCostVec);
			
			Vector<ConstrainedFeatureCollectionSorted> featureCollectionVector = processPartition(geom, congeom, featureCostVec);
			Double[] featureOperationVec = ProcessingTools.getFeatureOperationVector(featureCollectionVector);
			ProcessingTools.getConstraintSpace().addFeatureCostAndOperation(featureCostVec, featureOperationVec);
			
			ThreadOperationComparator comparator = new ThreadOperationComparator(); 											
			Collections.sort(featureCollectionVector, comparator);
			
			//moritz GenOL
	   		if(genolbefore != null) {
	   			genoldiff = new Double[NbrConstraints];
	   			for(int j = 0; j< featureCostVec.length; j++) {
	   				genoldiff[j] = new Double(genolbefore[j].doubleValue() - featureCostVec[j].doubleValue());
	   			}
	   			if(genolopstat.containsKey(genoloperationbefore)) {
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
    				geom = ((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(0)).getFeatureCollection();
    				//ProcessingTools.makeHistoryStep(geom, "Operation " + ((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(0)).getOperation()+ " - Evaluation after");
    				geom.makeConstraintHistoryStep("Operation " + ((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(0)).getOperation()+ " - Evaluation after");
    				DecimalFormat df = new DecimalFormat("#0.0000");				
    		   		LOGGER.info("Operation  >>> " + ((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(0)).getOperation() + " <<<  successful (" + df.format(costAllNew) + ")");

        			// Vergleich der Costen pro Operator für ähnliche und aktuelle Partion
        	    	Vector operationVectorAfter = ProcessingTools.getConstraintSpace().getOperationVectorFromFeatureCost(featureCostVec);
        	    	ProcessingTools.evaluatePrognose(operationVectorBefore, operationVectorAfter);
        			
        			// Correlation für Änderung der Constraints
        			/*if(iterations > 0) {
        				(new Eval_Constraint_Correlation_Diff()).run(wgreq);
        				Matrix correlationMatrix = (Matrix)wgreq.getResult("correlation");
        				ProcessingServiceParallel.showCorrelations(correlationMatrix);
        			}*/
        			
        			if(costAllNew < 0.001) run = false; // Abbruch, wenn kosten quasi null
    			}
    			else run = false;        			
			}
			else run = false;
			
			if(iterations > 20) 
				run = false;
			iterations++;
		}// Ende while(run == true) 
		
		//context.addLayer(StandardCategoryNames.WORKING, "result_part"+partNr, (FeatureCollection)act_layers.get("geom"));
		// speicherung aller uebrigen gebaeude in der output collection result_all
		if(geom != null) result_all.addAll(geom.getFeatures());
		//stopOperatorThreads();
		iterationNr[partNr] = new Integer(iterations);
		LOGGER.info("run() finished ("+iterations+" iterations)");
    }
    
    /*private void initOperatorThreads() {
    	for(int i = 0; i < NbrOperations; i++) {
    		threads[i] = new OperatorThread(partNr,ProcessingTools.lookupOperationClass(i), ProcessingTools.lookupOperation(i), globalParameters);
    	}
    	threads[4].addParameter("maxnumber", new Integer((int)Math.floor(geom.size()*0.9))); // Anzahl feature um 10% reduzieren
		threads[5].addParameter("maxnumber", new Integer((int)Math.floor(geom.size()*0.7))); // Anzahl feature um 30% reduzieren
    }
    
    private void stopOperatorThreads() {
    	for(int i = 0; i < NbrOperations; i++) {
    		threads[i].stop();
    	}
    }*/
    
    public static final String[] operationClassNameArray = {
		"ch.unizh.geo.webgen.service.AreaScalingRelative",
		"ch.unizh.geo.webgen.service.BuildingSimplifyGN",
		"ch.unizh.geo.webgen.service.EnlargeToRectangle",
		"ch.unizh.geo.webgen.service.DisplaceConstrainedNewFast",
		"ch.unizh.geo.webgen.service.BuildingTypification",  //10%
		"ch.unizh.geo.webgen.service.BuildingTypification",   //30%
		"ch.unizh.geo.webgen.service.AreaFeatureRemoval",
		"ch.unizh.geo.webgen.service.AggregateBuiltUpArea",
		"ch.unizh.geo.webgen.service.ShrinkPartition"
		};
	public static String lookupOperationClass(int operationClassNames) {    	
		try {return operationClassNameArray[operationClassNames];}
		catch (ArrayIndexOutOfBoundsException e) {return null;}
	}
    
    public Vector<ConstrainedFeatureCollectionSorted> processPartition(ConstrainedFeatureCollection geom, ConstrainedFeatureCollection congeom,
    		Double[] partionCost) {
    	Vector<ConstrainedFeatureCollectionSorted> sortedFeatureCollectionVector = new Vector<ConstrainedFeatureCollectionSorted>(); 	    	
    	
    	int operation; String operationName;
    	Vector operationSequence = ProcessingTools.getConstraintSpace().getOperationVectorFromFeatureCost(partionCost);
    	System.out.println("Operator-Reihenfolge: " + operationSequence.toString());
    	for(Iterator iter = operationSequence.iterator(); iter.hasNext();) {
    		operation = ((ConstraintSpaceArrayItem) iter.next()).getId();
    		operationName = ProcessingTools.lookupOperation(operation);
    		LOGGER.info(operationName +"...");
    		WebGenRequest twgreq = new WebGenRequest();
    		twgreq.addParameters(this.globalParameters);
    		twgreq.addParameter("geom", geom.clone());
    		twgreq.addParameter("congeom", congeom.clone());
    		if(operation == 4) twgreq.addParameter("maxnumber", new Integer((int)Math.floor(geom.size()*0.9)));
    		if(operation == 5) twgreq.addParameter("maxnumber", new Integer((int)Math.floor(geom.size()*0.7)));
    		threads[operation] = new OperatorThreadSimple(partNr,lookupOperationClass(operation), ProcessingTools.lookupOperation(operation), twgreq, sortedFeatureCollectionVector);
    		/*threads[operation].reLoad(geom.clone(), congeom.clone());*/
    		threads[operation].start();
    	}
    	//LOGGER.info("processing started");
    	for(int testedOperations = 0; testedOperations < NbrOperations; testedOperations++) {
    		try {threads[testedOperations].join();} catch (InterruptedException e) {}
    		/*while(threads[testedOperations].status == 1) {
    			try {this.wait();}
    			catch (InterruptedException e) {}
    			catch (IllegalMonitorStateException e) {}
    		}
    		LOGGER.info("..."+ ProcessingTools.lookupOperation(testedOperations));
    		sortedFeatureCollectionVector.add(threads[testedOperations].getResult());*/
    	}
    	LOGGER.debug("processing complete: "+sortedFeatureCollectionVector.toString());
    	return sortedFeatureCollectionVector;
    }
    
    class ThreadOperationComparator implements Comparator {
     	public int compare(Object o1, Object o2) {
     		ConstrainedFeatureCollectionSorted a = (ConstrainedFeatureCollectionSorted)o1;
     		ConstrainedFeatureCollectionSorted b = (ConstrainedFeatureCollectionSorted)o2;
     		return (a.compareTo(b));
     	}
     	/*public int compare(ConstrainedFeatureCollectionSorted a, ConstrainedFeatureCollectionSorted b) {
     		return (a.compareTo(b));
     	}*/
     }
	
}
