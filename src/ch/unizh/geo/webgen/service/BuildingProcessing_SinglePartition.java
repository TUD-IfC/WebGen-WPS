package ch.unizh.geo.webgen.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import ch.unizh.geo.webgen.model.ConstrainedFeature;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollectionSorted;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollectionSortedComparator;
import ch.unizh.geo.webgen.model.Constraint;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.registry.ParameterDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.test.ProcessingStatistics;
import ch.unizh.geo.webgen.test.ProcessingStatisticsParallel;
import ch.unizh.geo.webgen.tools.ProcessingTools;

public class BuildingProcessing_SinglePartition extends AWebGenAlgorithm implements IWebGenAlgorithm {
	
	static int NbrOperations = ProcessingTools.NbrOperations;
	//static int NbrConstraints = ProcessingTools.NbrConstraints;
	
	ArrayList<Integer> usableOperators;
	
	StringBuffer localStat = new StringBuffer();
	
	String webgenserver = "localcloned";
	HashMap<String,Object> globalParameters = new HashMap<String,Object>();
	boolean parallel = false;
	int partitionNr;

	public void run(WebGenRequest wgreq) {
		webgenserver = wgreq.getParameterString("webgenserver");
		globalParameters.put("minarea", wgreq.getParameter("minarea"));
		globalParameters.put("minlength", wgreq.getParameter("minlength"));
		globalParameters.put("mindist", wgreq.getParameter("mindist"));
		globalParameters.put("roaddist", wgreq.getParameter("roaddist"));
		globalParameters.put("parallel", wgreq.getParameter("parallel"));
		parallel = wgreq.getParameterBoolean("parallel");
		ConstrainedFeatureCollection geom; ConstrainedFeatureCollection congeom;
		try {
			geom = (ConstrainedFeatureCollection)wgreq.getParameter("geom");
			congeom = (ConstrainedFeatureCollection)wgreq.getParameter("congeom");
		}
		catch (Exception e) {
			this.addError("please submit only ConstrainedFeatureCollections");
			return;
		}
		
		partitionNr = wgreq.getParameterInt("partitionNr");
		String searchMethod = wgreq.getParameterString("search method");
		
		usableOperators = new ArrayList<Integer>();
		int opCount = 0;
		for(String opn : ProcessingTools.operationNameArray) {
			if(wgreq.getParameterBoolean("use " + opn)) usableOperators.add(opCount);
			opCount++;
		}
		
		//initialisiere den CollectionConstraint bei geom mit der bwRatio der Partition
		geom.initCollectionConstraint(congeom);
		
		WebGenRequest partwgreq = new WebGenRequest();
		partwgreq.addParameters(this.globalParameters);
		partwgreq.addParameter("geom", geom);
		partwgreq.addParameter("congeom", congeom);
		
		//Evaluation / Conflict analysis
		Double[] initialFeatureCostVec = evalPartitions(partwgreq);
		double initialCostAll = getCostFromCostVector(initialFeatureCostVec);
		Double[] featureCostVec = initialFeatureCostVec;
		double costAllCurrent = initialCostAll;
		geom.makeConstraintHistoryStep("initial evaluation#"+costAllCurrent);
		
		
		if(searchMethod.equals("simulated anealing")) doProcessingSimAnealing(partwgreq, costAllCurrent, featureCostVec, 2);
		else if(searchMethod.equals("simulated anealing 1.3")) doProcessingSimAnealing(partwgreq, costAllCurrent, featureCostVec, 1.3);
		else if(searchMethod.equals("simulated anealing 3")) doProcessingSimAnealing(partwgreq, costAllCurrent, featureCostVec, 3);
		else if(searchMethod.equals("gradient random")) doProcessingGradientRandom(partwgreq, costAllCurrent, featureCostVec);
		else if(searchMethod.equals("recursive 1deep")) doProcessingRecursive1deep(partwgreq, costAllCurrent, featureCostVec, 0);
		else if(searchMethod.equals("recursive 2deep")) doProcessingRecursiveXdeep(partwgreq, costAllCurrent, featureCostVec, 2, "r", 0);
		else if(searchMethod.equals("recursive 3deep")) doProcessingRecursiveXdeep(partwgreq, costAllCurrent, featureCostVec, 3, "r", 0);
		else if(searchMethod.equals("recursive 4deep")) doProcessingRecursiveXdeep(partwgreq, costAllCurrent, featureCostVec, 4, "r", 0);
		else if(searchMethod.equals("recursive 5deep")) doProcessingRecursiveXdeep(partwgreq, costAllCurrent, featureCostVec, 5, "r", 0);
		else if(searchMethod.equals("recursive fully")) doProcessingRecursiveFull(partwgreq, costAllCurrent, featureCostVec);
		else if(searchMethod.equals("recursive gradient")) doProcessingRecursiveGradient(partwgreq, costAllCurrent, featureCostVec);
		else if(searchMethod.equals("recursive genetic")) doProcessingRecursiveGenetic(partwgreq, costAllCurrent, featureCostVec);
		else doProcessingGradientStd(partwgreq, costAllCurrent, featureCostVec);
		
		
		DecimalFormat df = new DecimalFormat("#0.00000000");
		Double[] costVec = BuildingProcessing_SinglePartition.evalPartitions(partwgreq);
		double costAllEnd = BuildingProcessing_SinglePartition.getCostFromCostVector(costVec);
		ProcessingTools.finalCostSum += costAllEnd;
		//cost before; minsize; minlength; mindist; minlocaldist; diffpos; difforient; diffedgecount; diffwlratio; cost after; minsize ...
		localStat.append("p"+partitionNr+"before;"+df.format(initialCostAll));
		for(Double ac : initialFeatureCostVec) localStat.append(";"+df.format(ac));
		ProcessingTools.initalCostSum += initialCostAll;
		localStat.append("\np"+partitionNr+"after;"+df.format(costAllEnd));
		for(Double ac : costVec) localStat.append(";"+df.format(ac));
		localStat.append("\n");
		ProcessingTools.globalStat.append(localStat);
		
		wgreq.addResult("initial cost", initialCostAll+0);
		wgreq.addResult("final cost", costAllEnd+0);
		wgreq.addResult("iterations", ProcessingTools.totalNbrIterations+0);
		ProcessingTools.totalNbrIterations = 0;
		
		wgreq.addResult("returnstates_min", partwgreq.getParameter("returnstates_min"));
		wgreq.addResult("returnstates_max", partwgreq.getParameter("returnstates_max"));
		wgreq.addResult("returnstates_size", partwgreq.getParameter("returnstates_size"));
		
		wgreq.addResult("result", partwgreq.getParameter("geom"));
	}
	

	/** processing Strategies - standard gradient
	 */
	private void doProcessingGradientStd(WebGenRequest partwgreq, double costAllCurrent, Double[] featureCostVec) {
		int iterations = 0;
		boolean run = true;
		while(run == true) {
	    	Vector<ConstrainedFeatureCollectionSorted> featureCollectionVector = processPartition(partwgreq, featureCostVec);		
			if(featureCollectionVector.size() > 0) {
				ConstrainedFeatureCollectionSorted bestGeom = this.getBestResult(featureCollectionVector);
    			if(costAllCurrent - bestGeom.getCost() > 0.0000001) { //stop if amelioration lower than 0.0000001
    				ConstrainedFeatureCollection geom = (ConstrainedFeatureCollection)bestGeom.getFeatureCollection();
    				String bestOperation = bestGeom.getOperation();
    				partwgreq.addParameter("geom", geom);
    				//geom.makeConstraintHistoryStep("Operation " + bestOperation + " - Evaluation after");
    		   		giveInfo(bestOperation, bestGeom.getCost());
    		   		/*if(bestGeom.getOperationID() ==9 || bestGeom.getOperationID() == 10) {
    		   			System.out.println();
    		   		}*/
    		   		ProcessingStatistics.addOpteratorPosition(bestGeom.getOperationID(),iterations);
    		   		ProcessingStatisticsParallel.addOpteratorPosition(bestGeom.getOperationID(),iterations);
    		   		costAllCurrent = bestGeom.getCost();
    		   		featureCostVec = bestGeom.getCostVec();
        			if(bestGeom.getCost() < 0.001) run = false; // Abbruch, wenn costen quasi null
    			}
    			else run = false;        			
			}
			else run = false;
			if(iterations > 20) run = false;
			iterations++;
		}// Ende while(run == true)
		ProcessingTools.totalNbrIterations += iterations;
	}
	
	
	/** processing Strategies - ramdomized gradient
	 */
	private void doProcessingGradientRandom(WebGenRequest partwgreq, double costAllCurrent, Double[] featureCostVec) {
		int iterations = 0;
		boolean run = true;
		while(run == true) {
	    	Vector<ConstrainedFeatureCollectionSorted> featureCollectionVector = processPartition(partwgreq, featureCostVec);		
			if(featureCollectionVector.size() > 0) {
				ConstrainedFeatureCollectionSorted bestGeom = getBestRandomResult(featureCollectionVector);
    			if(costAllCurrent - bestGeom.getCost() > 0.0000001) {
    				ConstrainedFeatureCollection geom = (ConstrainedFeatureCollection)bestGeom.getFeatureCollection();
    				String bestOperation = bestGeom.getOperation();
    				partwgreq.addParameter("geom", geom);
    				//geom.makeConstraintHistoryStep("Operation " + bestOperation + " - Evaluation after");
    		   		giveInfo(bestOperation, bestGeom.getCost());
    		   		costAllCurrent = bestGeom.getCost();
    		   		featureCostVec = bestGeom.getCostVec();
        			if(bestGeom.getCost() < 0.001) run = false; // Abbruch, wenn costen quasi null
    			}
    			else run = false;        			
			}
			else run = false;
			if(iterations > 20) run = false;
			iterations++;
		}// Ende while(run == true)
		ProcessingTools.totalNbrIterations += iterations;
	}
	
	
	/** processing Strategies - Simulated Anealing
	 */
	private void doProcessingSimAnealing(WebGenRequest partwgreq, double costAllCurrent, Double[] featureCostVec, double decreaseFactor) {
		int iterations = 0;
		int randomSetSize = NbrOperations;
		Vector<ConstrainedFeatureCollectionSorted> results = new Vector<ConstrainedFeatureCollectionSorted>();
		double actMinCost = 100.0;
		while(iterations < 20 && costAllCurrent > 0.001) {
			Vector<ConstrainedFeatureCollectionSorted> featureCollectionVector = processPartition(partwgreq, featureCostVec);		
			if(featureCollectionVector.size() <= 0) break;
			if(randomSetSize > featureCollectionVector.size()) randomSetSize = featureCollectionVector.size();
			if(randomSetSize < 1) randomSetSize = 1;
			
			Collections.sort(featureCollectionVector, new ConstrainedFeatureCollectionSortedComparator());
			for(ConstrainedFeatureCollectionSorted actFcS : featureCollectionVector) {
				if(actFcS.getCost() < actMinCost) {
					results.add(actFcS);
					actMinCost = actFcS.getCost();
				}
			}
			
			ConstrainedFeatureCollectionSorted bestGeom = featureCollectionVector.get(0);
			if(randomSetSize > 1) { // do sim anealing
				int sampleId = (int)(Math.random() * (randomSetSize-1)); //samples from pos 0..7
				bestGeom = featureCollectionVector.get(sampleId);
				randomSetSize = (int) Math.floor(randomSetSize/decreaseFactor); // decrease sim anealing function
			}
			else { // do normal hill climbing (after random set size 8-4-2-1)
				if(costAllCurrent - bestGeom.getCost() <= 0) break; //stop if no amelioration anymore
			}
			partwgreq.addParameter("geom", bestGeom.getFeatureCollection());
			giveInfo(bestGeom.getOperation(), bestGeom.getCost());
	   		costAllCurrent = bestGeom.getCost();
	   		featureCostVec = bestGeom.getCostVec();
			
			iterations++;
		}
		if(results.size() > 0) {
			Collections.sort(results, new ConstrainedFeatureCollectionSortedComparator());
			ConstrainedFeatureCollectionSorted bestResult = results.get(0);
			partwgreq.addParameter("geom", bestResult.getFeatureCollection());
	   		costAllCurrent = bestResult.getCost();
	   		featureCostVec = bestResult.getCostVec();
	   		System.out.println("best result found ("+costAllCurrent+")");
		}
		ProcessingTools.totalNbrIterations += iterations;
	}
	
	
	/** processing Strategies - recursive 1 deep
	 */
	private void doProcessingRecursive1deep(WebGenRequest partwgreq, double costAllCurrent, Double[] featureCostVec, int recursions) {
		Vector<ConstrainedFeatureCollectionSorted> featureCollectionVector = processPartition(partwgreq, featureCostVec);		
		if(featureCollectionVector.size() > 0) {
			ConstrainedFeatureCollectionSorted bestGeom = this.getBestResult(featureCollectionVector);
			if(costAllCurrent - bestGeom.getCost() > 0.0000001) { //stop if amelioration lower than 0.0000001
				ConstrainedFeatureCollection geom = (ConstrainedFeatureCollection)bestGeom.getFeatureCollection();
				String bestOperation = bestGeom.getOperation();
				partwgreq.addParameter("geom", geom);
				//geom.makeConstraintHistoryStep("Operation " + bestOperation + " - Evaluation after");
		   		giveInfo(bestOperation, bestGeom.getCost());
		   		costAllCurrent = bestGeom.getCost();
		   		featureCostVec = bestGeom.getCostVec();
    			if(bestGeom.getCost() < 0.001) return; // Abbruch, wenn costen quasi null
			}
			else return;
		}
		if(recursions > 20) return;
		recursions++;
		ProcessingTools.totalNbrIterations++;
		//recursion
		doProcessingRecursive1deep(partwgreq, costAllCurrent, featureCostVec, recursions);
	}
	
	
	/** processing Strategies - recursive X deep
	 */
	private void doProcessingRecursiveXdeep(WebGenRequest partwgreq, double costAllCurrent, Double[] featureCostVec, int deep, String branch, int recursions) {
		remparams = new HashMap<String,Object>();
		remparams.putAll(partwgreq.getParameters());
		remparams.remove("geom");
		ConstrainedFeatureCollectionSorted sGeom = new ConstrainedFeatureCollectionSorted(costAllCurrent, (ConstrainedFeatureCollection)partwgreq.getParameter("geom"), "inital", 0, featureCostVec);
		ConstrainedFeatureCollectionSorted newGeom = recursionXdeep(sGeom, deep, branch, recursions, costAllCurrent);
		partwgreq.addParameter("geom", newGeom.getFeatureCollection());
		addReturnStateInfo(partwgreq);
		System.out.println("Partition "+partitionNr+" processed with "+newGeom.getProcessingSteps()+" steps!");
	}
	
	LinkedList<Double> returnStates = new LinkedList<Double>();
	private ConstrainedFeatureCollectionSorted returnIt(ConstrainedFeatureCollectionSorted sfc) {
		returnStates.add(sfc.getCost());
		return sfc;
	}
	private void addReturnStateInfo(WebGenRequest partwgreq) {
		int size = 0;
		double minCost = returnStates.getFirst();
		double maxCost = minCost;
		for(Double actC : returnStates) {
			if(actC < minCost) minCost = actC;
			if(actC > maxCost) maxCost = actC;
			size++;
		}
		partwgreq.addParameter("returnstates_min", minCost);
		partwgreq.addParameter("returnstates_max", maxCost);
		partwgreq.addParameter("returnstates_size", size);
	}
	
	private HashMap<String,Object> remparams;
	private ConstrainedFeatureCollectionSorted recursionXdeep(ConstrainedFeatureCollectionSorted formerGeom, int deep, String branch, int recursions, double beforeFormerCost) {
		if(recursions > deep*10) return returnIt(formerGeom);
		if(formerGeom.getProcessingSteps() > 63) return returnIt(formerGeom);
		recursions++;
		ProcessingTools.totalNbrIterations++;
		
		WebGenRequest twr = new WebGenRequest();
		twr.addParameters(remparams);
		twr.addParameter("geom", formerGeom.getFeatureCollection());
		Vector<ConstrainedFeatureCollectionSorted> fcVec = processPartition(twr, formerGeom.getCostVec());		
		Collections.sort(fcVec, new ConstrainedFeatureCollectionSortedComparator());
		if(fcVec.size() == 0) return returnIt(formerGeom);
		
		ConstrainedFeatureCollectionSorted bestGeom = fcVec.get(0);
		bestGeom.setProcessingSteps(formerGeom.getProcessingSteps());
		double amelioration = formerGeom.getCost() - bestGeom.getCost();
		if(amelioration < 0.0000001) return returnIt(bestGeom); //stop if amelioration lower than 0.0000001
		if(beforeFormerCost - bestGeom.getCost() < 0.0000001) return returnIt(bestGeom); //stop if amelioration compared to the before last step is lower than 0.0000001
		if(bestGeom.getCost() < 0.001) return returnIt(bestGeom);
		
		//System.out.println("step "+branch + " " + formerGeom.getCost() + " r0="+fcVec.get(0).getCost() + " r1="+fcVec.get(1).getCost()  + " r1="+beforeFormerCost);
		
		//fcVec.get(0).getFeatureCollection().makeConstraintHistoryStep("Operation " + fcVec.get(0).getOperation() + " - Evaluation after");
		//fcVec.get(1).getFeatureCollection().makeConstraintHistoryStep("Operation " + fcVec.get(1).getOperation() + " - Evaluation after");
		fcVec.get(0).setProcessingSteps(formerGeom.getProcessingSteps()*deep);
		fcVec.get(1).setProcessingSteps(formerGeom.getProcessingSteps()*deep);
		
		ConstrainedFeatureCollectionSorted newGeom0 = recursionXdeep(fcVec.get(0), deep, branch+"-0", recursions, formerGeom.getCost());
		ConstrainedFeatureCollectionSorted newGeom = newGeom0;
		double secondAmelioration = beforeFormerCost - fcVec.get(1).getCost();
		if(secondAmelioration > 0.0000001) { // call second best result only if changed since 2 steps
			ConstrainedFeatureCollectionSorted newGeom1 = recursionXdeep(fcVec.get(1), deep, branch+"-1", recursions, formerGeom.getCost());
			if(newGeom1.getCost() < newGeom0.getCost()) newGeom = newGeom1;
		}
		
		ProcessingStatisticsParallel.addOpteratorPosition(newGeom.getOperationID(),recursions);
		//newGeom.getFeatureCollection().makeConstraintHistoryStep("after " + newGeom.getOperation());
		return newGeom;
   		//giveInfo("branch " + branch + " " + bestOperation, bestGeom.getCost());
	}
	
	
	/** processing Strategies - recursive full deep
	 */
	int recRecSteps = 0;
	private void doProcessingRecursiveFull(WebGenRequest partwgreq, double costAllCurrent, Double[] featureCostVec) {
		recRecSteps++;
		remparams = new HashMap<String,Object>();
		remparams.putAll(partwgreq.getParameters());
		remparams.remove("geom");
		ConstrainedFeatureCollectionSorted sGeom = new ConstrainedFeatureCollectionSorted(costAllCurrent, (ConstrainedFeatureCollection)partwgreq.getParameter("geom"), "inital", 0, featureCostVec);
		doProcessingRecursiveFull(sGeom, 0);
		if(resultsRecursive.size() > 0) {
			Collections.sort(resultsRecursive, new ConstrainedFeatureCollectionSortedComparator());
			ConstrainedFeatureCollectionSorted newGeom = resultsRecursive.get(0);
			//newGeom.getFeatureCollection().makeConstraintHistoryStep("final evaluation");
			partwgreq.addParameter("geom", newGeom.getFeatureCollection());
			System.out.println("Partition "+partitionNr+" processed with "+newGeom.getProcessingSteps()+" steps!");
			
			//make it twice - experimental
			if(recRecSteps < 1) doProcessingRecursiveFull(partwgreq, newGeom.getCost(), newGeom.getCostVec());
			
		}
	}	
	Vector<ConstrainedFeatureCollectionSorted> resultsRecursive = new Vector<ConstrainedFeatureCollectionSorted>();
	double actCostMinimum = 100.0;
	private void doProcessingRecursiveFull(ConstrainedFeatureCollectionSorted formerGeom, int recursions) {
		if(recursions >= 4) return; //max 5
		recursions++;
		WebGenRequest twr = new WebGenRequest();
		twr.addParameters(remparams);
		twr.addParameter("geom", formerGeom.getFeatureCollection());
		Vector<ConstrainedFeatureCollectionSorted> fcVec = processPartition(twr, formerGeom.getCostVec());
		ConstrainedFeatureCollectionSorted actSfc;
		for(int i=0; i<fcVec.size(); i++) {
			actSfc = fcVec.get(i);
			if(actSfc.getCost() < actCostMinimum) {
				resultsRecursive.add(actSfc);
				actCostMinimum = actSfc.getCost();
			}
			//recursion
			//actSfc.getFeatureCollection().makeConstraintHistoryStep("Operation " + actSfc.getOperation() + " - Evaluation after");
			doProcessingRecursiveFull(actSfc, recursions);
		}
	}
	
	
	/** processing Strategies - recursive gradient
	 */
	private void doProcessingRecursiveGradient(WebGenRequest partwgreq, double costAllCurrent, Double[] featureCostVec) {
		remparams = new HashMap<String,Object>();
		remparams.putAll(partwgreq.getParameters());
		remparams.remove("geom");
		ConstrainedFeatureCollectionSorted sGeom = new ConstrainedFeatureCollectionSorted(costAllCurrent, (ConstrainedFeatureCollection)partwgreq.getParameter("geom"), "inital", 0, featureCostVec);
		doProcessingRecursiveGradient(sGeom, 0, NbrOperations);
		if(resultsRecursiveGradient.size() > 0) {
			Collections.sort(resultsRecursiveGradient, new ConstrainedFeatureCollectionSortedComparator());
			ConstrainedFeatureCollectionSorted newGeom = resultsRecursiveGradient.get(0);
			//newGeom.getFeatureCollection().makeConstraintHistoryStep("final evaluation");
			partwgreq.addParameter("geom", newGeom.getFeatureCollection());
			System.out.println("Partition "+partitionNr+" processed with totally "+totalRecursions+" recursion steps!");
		}
	}	
	Vector<ConstrainedFeatureCollectionSorted> resultsRecursiveGradient = new Vector<ConstrainedFeatureCollectionSorted>();
	double actCostMinimumGradient = 100.0;
	int totalRecursions = 0;
	private void doProcessingRecursiveGradient(ConstrainedFeatureCollectionSorted formerGeom, int recursions, int branchNbr) {
		if(recursions >= 15) return; //max depth (chain 10-5-2-1-1-1-1-1-1-1-1-1-1-1)
		recursions++;
		totalRecursions++;
		WebGenRequest twr = new WebGenRequest();
		twr.addParameters(remparams);
		twr.addParameter("geom", formerGeom.getFeatureCollection());
		Vector<ConstrainedFeatureCollectionSorted> fcVec = processPartition(twr, formerGeom.getCostVec());
		Collections.sort(fcVec, new ConstrainedFeatureCollectionSortedComparator());
		ConstrainedFeatureCollectionSorted actSfc;
		if(branchNbr == 0) branchNbr = 1;
		if(branchNbr > fcVec.size()) branchNbr = fcVec.size();
		int i;
		for(i=0; i<branchNbr; i++) {
			actSfc = fcVec.get(i);
			if(actSfc.getCost() < actCostMinimumGradient) {
				resultsRecursiveGradient.add(actSfc);
				actCostMinimumGradient = actSfc.getCost();
			}
			if(branchNbr == 1 && actSfc.getCost() > formerGeom.getCost()) return;
			//recursion
			doProcessingRecursiveGradient(actSfc, recursions, (int)Math.floor(branchNbr/2));
		}
	}
	
	
	
	/** processing Strategies - recursive genetic
	 */
	private void doProcessingRecursiveGenetic(WebGenRequest partwgreq, double costAllCurrent, Double[] featureCostVec) {
		remparams = new HashMap<String,Object>();
		remparams.putAll(partwgreq.getParameters());
		remparams.remove("geom");
		ConstrainedFeatureCollectionSorted sGeom = new ConstrainedFeatureCollectionSorted(costAllCurrent, (ConstrainedFeatureCollection)partwgreq.getParameter("geom"), "inital", 0, featureCostVec);
		doProcessingRecursiveGenetic(sGeom, 0);
		if(resultsRecursiveGenetic.size() > 0) {
			Collections.sort(resultsRecursiveGenetic, new ConstrainedFeatureCollectionSortedComparator());
			ConstrainedFeatureCollectionSorted newGeom = resultsRecursiveGenetic.get(0);
			//newGeom.getFeatureCollection().makeConstraintHistoryStep("final evaluation");
			partwgreq.addParameter("geom", newGeom.getFeatureCollection());
			System.out.println("Partition "+partitionNr+" processed with totally "+totalRecursionsGenetic+" recursion steps!");
		}
	}	
	Vector<ConstrainedFeatureCollectionSorted> resultsRecursiveGenetic = new Vector<ConstrainedFeatureCollectionSorted>();
	double actCostMinimumGenetic = 100.0;
	int totalRecursionsGenetic = 0;
	int nbrGeneticSamples = 3;
	private void doProcessingRecursiveGenetic(ConstrainedFeatureCollectionSorted formerGeom, int recursions) {
		if(recursions >= 8) return; //max depth
		recursions++;
		totalRecursionsGenetic++;
		WebGenRequest twr = new WebGenRequest();
		twr.addParameters(remparams);
		twr.addParameter("geom", formerGeom.getFeatureCollection());
		Vector<ConstrainedFeatureCollectionSorted> fcVec = processPartition(twr, formerGeom.getCostVec());
		Collections.sort(fcVec, new ConstrainedFeatureCollectionSortedComparator());
		ConstrainedFeatureCollectionSorted actSfc;
		int sampleId;
		nbrGeneticSamples = 3;
		if(nbrGeneticSamples > fcVec.size()) nbrGeneticSamples = fcVec.size();
		for(int i=0; i < nbrGeneticSamples; i++) {
			if(i > 0) sampleId = 1 + (int)(Math.random() * (fcVec.size()-1)); //samples from pos 1..10
			else sampleId = i;
			actSfc = fcVec.get(sampleId);
			if(actSfc.getCost() < actCostMinimumGenetic) {
				resultsRecursiveGenetic.add(actSfc);
				actCostMinimumGenetic = actSfc.getCost();
			}
			if(sampleId == 0 && actSfc.getCost() > formerGeom.getCost()) return;
			//recursion
			doProcessingRecursiveGenetic(actSfc, recursions);
		}
	}
	
	
	
	/** give info - print current state
	 */
	private void giveInfo(String message, double actCost) {
		DecimalFormat df = new DecimalFormat("#0.00000000");
   		LOGGER.info("p"+partitionNr+";"+message + "(" + df.format(actCost) + ")");
   		localStat.append("p"+partitionNr+";"+message+";"+df.format(actCost)+"\n");
	}
	
	
	//search function (currently steepest gradient), here could also simulated anealing be applied
	private ConstrainedFeatureCollectionSorted getBestResult(Vector<ConstrainedFeatureCollectionSorted> results) {
		Collections.sort(results, new ConstrainedFeatureCollectionSortedComparator());
		/*System.out.println("p"+partitionNr+" next step:");
		for(ConstrainedFeatureCollectionSorted cfcs : results) {
			System.out.println(cfcs.getCost() + " " +cfcs.getOperation());
		}*/
		return results.get(0);
	}
	
	private ConstrainedFeatureCollectionSorted getBestRandomResult(Vector<ConstrainedFeatureCollectionSorted> results) {
		Vector<ConstrainedFeatureCollectionSorted> best10perc = new Vector<ConstrainedFeatureCollectionSorted>();
		Collections.sort(results, new ConstrainedFeatureCollectionSortedComparator());
		double bestCost = results.get(0).getCost();
		double bestCost10perc = bestCost/5;
		for(ConstrainedFeatureCollectionSorted cfcs : results) {
			if(cfcs.getCost()-bestCost < bestCost10perc) best10perc.add(cfcs);
		}
		int randomResult = (int)Math.round(Math.random()*(best10perc.size()-1));
		return best10perc.get(randomResult);
	}
	
	
	/** processing Partiton - sequentially or parallely
	 */
	public Vector<ConstrainedFeatureCollectionSorted> processPartition(WebGenRequest wgreq, Double[] partionCost) {
		if(parallel) return processPartitionParallely(wgreq, partionCost);
		else return processPartitionSequentially(wgreq, partionCost);	
	}
	
	public Vector<ConstrainedFeatureCollectionSorted> processPartitionSequentially(WebGenRequest wgreq, Double[] partionCost) {
    	Vector<ConstrainedFeatureCollectionSorted> sortedFeatureCollectionVector = new Vector<ConstrainedFeatureCollectionSorted>();
    	HashMap<String,Object> parameters = wgreq.getParameters();
    	ConstrainedFeatureCollection fcNew = null;
    	for(Integer i : usableOperators) {
        	//int operation = ((ConstraintSpaceArrayItem)operationSequence.get(i)).getId();
        	String operationName = ProcessingTools.lookupOperation(i);
        	fcNew = ProcessingTools.executeOperation(i, parameters, webgenserver);
        	if(fcNew != null ) {
        		WebGenRequest ewgreq = new WebGenRequest();
     			ewgreq.addParameters(parameters);
     			ewgreq.addFeatureCollection("geom", fcNew);
    			Double[] costVec = evalPartitions(ewgreq);
    			double costAllCurrent = getCostFromCostVector(costVec);            		
    			ConstrainedFeatureCollectionSorted sortedFeatureCollection = new ConstrainedFeatureCollectionSorted(costAllCurrent, fcNew, operationName, i, costVec);
    			sortedFeatureCollectionVector.add(sortedFeatureCollection);
        	}
    	}	
    	return sortedFeatureCollectionVector;
    }
	
	public Vector<ConstrainedFeatureCollectionSorted> processPartitionParallely(WebGenRequest wgreq, Double[] partionCost) {
    	Vector<ConstrainedFeatureCollectionSorted> sortedFeatureCollectionVector = new Vector<ConstrainedFeatureCollectionSorted>();
    	HashMap<String,Object> parameters = wgreq.getParameters();
    	OperatorThread[] threads = new OperatorThread[NbrOperations];
    	//Vector operationSequence = ProcessingTools.getConstraintSpace().getOperationVectorFromFeatureCost(partionCost);
    	//for(int i = 0; i < NbrOperations; i++) {
    	for(Integer i : usableOperators) {
        	//int operationId = ((ConstraintSpaceArrayItem)operationSequence.get(i)).getId();
        	String operationName = ProcessingTools.lookupOperation(i);
        	threads[i] = new OperatorThread(i, operationName, parameters, webgenserver, sortedFeatureCollectionVector);
	 		threads[i].start();
    	}
    	for(Integer i : usableOperators) {
    		try {
				threads[i].join();
			} catch (InterruptedException e) {}
    	}
    	return sortedFeatureCollectionVector;
    }
	
	
	public static Double[] evalPartitions(WebGenRequest wgreq) {
    	(new Eval_GMFeat_All_Constrained()).run(wgreq);
    	Double[] partitionCost = (Double[])wgreq.getResult("severities");
    	for(int i=4; i<8; i++) partitionCost[i] *= 0.25;
    	return partitionCost;
    }
	
	public static double getCostFromCostVector(double[] costVec) {
		double costAll = 0.0;
    	for(int i=0; i<costVec.length; i++) {
    		costAll += costVec[i];
    	}
    	return costAll;    
	}
	public static double getCostFromCostVector(Double[] costVec) {
    	double costAll = 0.0;
    	for(int i=0; i<costVec.length; i++) {
    		costAll += costVec[i].doubleValue();
    	}
    	return costAll;    
    }
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("BuildingProcessing_SinglePartition", "neun", "processing",
				"",
				"BuildingProcessing_SinglePartition",
				"Process Building Partition, Sequential or Parallel",
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
		
		id.addInputParameter("parallel", "BOOLEAN", "false", "execute multithreaded");
		
		ParameterDescription searchmethod = new ParameterDescription("search method", "STRING", "gradient std", "search method");
		searchmethod.addSupportedValue("gradient std");
		searchmethod.addSupportedValue("gradient random");
		searchmethod.addSupportedValue("simulated anealing");
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


class OperatorThread extends Thread {
	HashMap<String,Object> parameters = new HashMap<String,Object>();
	String webgenserver;
	int operationId;
	String operationName;
	Vector<ConstrainedFeatureCollectionSorted> sortedFeatureCollectionVector;

	public OperatorThread(int operationId, String operationName, HashMap<String,Object> parameters, String webgenserver, Vector<ConstrainedFeatureCollectionSorted> sortedFeatureCollectionVector) {
		this.parameters.putAll(parameters);
		this.webgenserver = webgenserver;
		this.operationId = operationId;
		this.operationName = operationName;
		this.sortedFeatureCollectionVector = sortedFeatureCollectionVector;
	}
	
	public void run() {
		ConstrainedFeatureCollection fcNew = ProcessingTools.executeOperation(operationId, parameters, webgenserver);
 		if(fcNew != null ) {
 			WebGenRequest ewgreq = new WebGenRequest();
 			ewgreq.addParameters(parameters);
 			ewgreq.addFeatureCollection("geom", fcNew);
			Double[] costVec = BuildingProcessing_SinglePartition.evalPartitions(ewgreq);
			double costAllCurrent = BuildingProcessing_SinglePartition.getCostFromCostVector(costVec);
			fcNew.makeConstraintHistoryStep(operationName+"#"+costAllCurrent);
			ConstrainedFeatureCollectionSorted sortedFeatureCollection = new ConstrainedFeatureCollectionSorted(costAllCurrent, fcNew, operationName, operationId, costVec);
			sortedFeatureCollectionVector.add(sortedFeatureCollection);
    	}
	}
}
