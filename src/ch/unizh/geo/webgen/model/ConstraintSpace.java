package ch.unizh.geo.webgen.model;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import org.jmat.data.AbstractMatrix;
import org.jmat.data.Matrix;

import ch.unizh.geo.webgen.tools.PCA;

public class ConstraintSpace implements Serializable {
	
	static final long serialVersionUID = 123456;

	int NbrConstraints;
	int NbrOperations;
	int NbrFeatures;
	ArrayList<Double[]> featureCostArray = new ArrayList<Double[]>();	
	ArrayList<Double[]> featureOperationArray = new ArrayList<Double[]>();
	
	public ConstraintSpace() {}	

	public ConstraintSpace(int nbrOperations, int nbrConstraints) {
		NbrOperations = nbrOperations;		
		NbrConstraints = nbrConstraints;
	}	
	
	public int getNbrFeatures() {
		return NbrFeatures;
	}

	public Double[] getFeatureCostById(int id) {
		Double[] featureCost = (Double[])featureCostArray.get(id);
		return (featureCost);
	}

	public Double[] getFeatureOperationById(int id) {
		return ((Double[])featureOperationArray.get(id));
	}
	
	public void addFeatureCostAndOperation(Double[] featureCostVector, Double[] featureOperationVector) {
		featureCostArray.add(featureCostVector);
		featureOperationArray.add(featureOperationVector);
		NbrFeatures++;
		return;
	}
	
	public Vector getOperationVectorFromFeatureCost(Double[] partionCost) {
		
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
		ConstraintSpaceArrayItemComparator comparator = new ConstraintSpaceArrayItemComparator(); 											
		Vector<ConstraintSpaceArrayItem> sortedOperations = new Vector<ConstraintSpaceArrayItem>(); 	
		Double[] featureOperationVec = (Double[])featureOperationArray.get(similarest);

		// 1. Create sorted vector				
		for(int i=0; i<featureOperationVec.length; i++) {
			ConstraintSpaceArrayItem operationCostSorted = new ConstraintSpaceArrayItem(featureOperationVec[i].doubleValue(), i);
			sortedOperations.add(operationCostSorted);			
		}		
		// 2. Sort Vector
		Collections.sort(sortedOperations, comparator);
		return(sortedOperations);
	}
	
	
	
	
	
//	 input ist ein double Array, welches für alle Objekte 
	// die constraint values bzw. cost values enthält
	public static Matrix calculateCorrelation(double[][] constraintValues) {
		
		// Constract names for constraints from evaluation serivces
		String[] 	constraintNames 	= createConstNames();
		Matrix 		constraintMatrix 	= new Matrix(constraintValues);
		Matrix 		correlationMatrix 	= null;
		
		PCA constraintDependencies = new PCA();				// Hauptkomponentenanalyse für Gruppe		
		correlationMatrix = constraintDependencies.makePCA(constraintMatrix, false, constraintNames);			
		
		System.out.println("Correlation =");
		System.out.println(correlationMatrix.toString());			
		
		formatierteAusgabe(correlationMatrix);
		
		return(correlationMatrix);				
	}	
	
	/**
	 * @return Returns the consmeasureNames.
	 */	
	public static String[] createConstNames() {
		String[]	constNames = null;
		return constNames;
	}
	

	static public void formatierteAusgabe(AbstractMatrix matrix) {
		for(int i = 0; i < matrix.getRowDimension();  i++) {
			for(int j = 0; j < matrix.getColumnDimension();  j++) {
				if(i==j /*|| i<j*/) {
					System.out.print(" 0,000" + "\t");		
					
				} else {
					DecimalFormat df;
					if(matrix.get(i,j) < 0)
						df = new DecimalFormat("#0.000");				
					else
						df = new DecimalFormat(" #0.000");			
					if (Double.isNaN(matrix.get(i,j)))
						System.out.print(" 0,000" + "\t");							
					else
						System.out.print(df.format(matrix.get(i,j)) + "\t");							
				}
			}		
			System.out.println( "");		
		}
	}
	
	static public void formatierteAusgabe(AbstractMatrix matrix, String[] names) {
		for(int i = 0; i < matrix.getRowDimension();  i++) {
			if(names != null)
				System.out.print("\n " + names[i] + "\t");
				
			for(int j = 0; j < matrix.getColumnDimension();  j++) {
				DecimalFormat df;
				if(matrix.get(i,j) < 0)
					df = new DecimalFormat("#0.000");				
				else
					df = new DecimalFormat(" #0.000");				
				System.out.print("\t" + df.format(matrix.get(i,j)));		
			}		
		}
		System.out.println();		
	}

	
	/**
	 * @return the nbrConstraints
	 */
	public int getNbrConstraints() {
		return NbrConstraints;
	}

	
	/**
	 * @return the nbrOperations
	 */
	public int getNbrOperations() {
		return NbrOperations;
	}
}
