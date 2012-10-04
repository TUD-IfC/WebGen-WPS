package ch.unizh.geo.webgen.service;

import java.util.ArrayList;

import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.tools.ProcessingTools;

public class ConstraintSpaceKnowledgeBase extends AWebGenAlgorithm implements IWebGenAlgorithm {
	
	int NbrConstraints = ProcessingTools.NbrConstraints;
	int NbrOperations = ProcessingTools.NbrOperations;
	int NbrFeatures;
	ArrayList<Double[]> featureCostArray = new ArrayList<Double[]>();	
	ArrayList<Double[]> featureOperationArray = new ArrayList<Double[]>();
	
	public ConstraintSpaceKnowledgeBase() {
		Double[] initCosts = new Double[NbrConstraints];
		for(int i=0;i<NbrConstraints;i++) initCosts[i]=1.0;
		featureCostArray.add(initCosts);
		Double[] initOps = new Double[NbrOperations];
		for(int i=0;i<NbrOperations;i++) initOps[i]=1.0;
		featureOperationArray.add(initOps);
	}
	
	public void run(WebGenRequest wgreq) {
		boolean update = wgreq.getParameterBoolean("update");
		if(update) {
			Double[] costarray = (Double[])wgreq.getParameter("costarray");
			Double[] operatorarray = (Double[])wgreq.getParameter("operatorarray");
			addFeatureCostAndOperation(costarray, operatorarray);
			wgreq.addMessage("update","ok");
		}
		else {
			Double[] costarray = (Double[])wgreq.getParameter("costarray");
			getOperationVectorFromFeatureCost(costarray, wgreq);
		}
	}
	
	
	public void addFeatureCostAndOperation(Double[] featureCostVector, Double[] featureOperationVector) {
		featureCostArray.add(featureCostVector);
		featureOperationArray.add(featureOperationVector);
		NbrFeatures++;
		return;
	}
	
	
	public void getOperationVectorFromFeatureCost(Double[] partionCost, WebGenRequest wgreq) {
		int similarest = 0;
		double alpha = 0.0;
		for(int i=0; i<featureCostArray.size(); i++) {
			// Skalarprodukt
			double cosinus = 0.0;
			double normA = 0.0;
			double normB = 0.0;
			Double[] featureCostVec = (Double[])featureCostArray.get(i);
			for(int k=0; k<NbrConstraints; k++) {
				cosinus += featureCostVec[k].doubleValue() * partionCost[k].doubleValue();
				normA += Math.pow(featureCostVec[k].doubleValue(),2.0);
				normB += Math.pow(partionCost[k].doubleValue(),2.0);
			}
			cosinus /= Math.sqrt(normA)*Math.sqrt(normB);
			if(cosinus > alpha) {
				alpha = cosinus;
				similarest = i;
			}
		}
		// Verwendung des erfolgreichsten Operators des ähnlichsten Feature
		Double[] operationValueArray = (Double[])featureOperationArray.get(similarest);
		Integer[] operationIdArray = new Integer[NbrOperations];
		for(int i=0; i<NbrOperations; i++) operationIdArray[i] = i;
		sortOperations(operationValueArray, operationIdArray);

		wgreq.addResult("opvalues", operationValueArray);
		wgreq.addResult("opids", operationIdArray);
	}
	
	
	
	private Double[] valuesArray;
	private Integer[] opidArray;
	
	private void sortOperations(Double[] valuesArray, Integer[] opidArray) {
		if(valuesArray.length != opidArray.length) return;
		this.valuesArray = valuesArray;
		this.opidArray = opidArray;
		quicksort(0, valuesArray.length-1);
	}
	
	private void quicksort(int p, int r) {
		if ( p < r ) {
			int q = partition( p, r);
			if ( q == r ) { q--; }
			quicksort( p, q );
			quicksort( q+1, r);
		} // end if
	} // end quicksort

	//	Partition by splitting this chunk to sort in two and
	//	get all big elements on one side of the pivot and all
	//	the small elements on the other.
	private int partition ( int lo, int hi ) {
		Double pivot = valuesArray[lo];
		while ( true ) {
			while ( valuesArray[hi] >= pivot && lo < hi ) { hi--; }
			while ( valuesArray[lo] < pivot && lo < hi ) { lo++; }
			if ( lo < hi ) {
				// exchange objects on either side of the pivot
				Double Tv = valuesArray[lo];
				Integer Ti = opidArray[lo];
				valuesArray[lo] = valuesArray[hi];
				opidArray[lo] = opidArray[hi];
				valuesArray[hi] = Tv;
				opidArray[hi] = Ti;
			}
			else return hi;
		} // end while
	} // end partition


	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("ConstraintSpaceKnowledgeBase", "neun", "support",
				"",
				"ConstraintSpaceKnowledgeBase",
				"KnowledgeBase with ConstraintSpace",
				"1.0");
		
		//add input parameters
		id.addInputParameter("update", "BOOLEAN", "true", "update");
		id.addInputParameter("costarray", "DOUBLEARRAY", "", "costarray");
		id.addInputParameter("operatorarray", "DOUBLEARRAY", "", "operationarray");
		
		//add output parameters
		id.addOutputParameter("opvalues", "DOUBLEARRAY");
		id.addOutputParameter("opids", "INTEGERARRAY");
		return id;
	}
}
