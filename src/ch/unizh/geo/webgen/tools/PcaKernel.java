package ch.unizh.geo.webgen.tools;

import org.jmat.data.AbstractMatrix;
import org.jmat.data.RandomMatrix;
import org.jmat.data.matrixDecompositions.EigenvalueDecomposition;


/**
 * @author burg
 *
 */
public class PcaKernel {

	private AbstractMatrix covariance;
	private AbstractMatrix correlation;
	private AbstractMatrix variance;
	private AbstractMatrix EigenVectors;
	private AbstractMatrix EigenValues;
	
	// Constructor for PCA, whether out of data matrix,
	// or if cov == true, then out of covariance matrix
	public PcaKernel(AbstractMatrix X, boolean cov, String[] constNames) {
		
		// Berechnung der Kovarianz-Matrix
		if(cov == false) {
			RandomMatrix rd = new RandomMatrix(X);
			rd.setIsSample(true);
			
			covariance = rd.covariance();
			// hier erfolgt eine Berechnung der empirischen Varianz-Kovarianz-Matrix
			// aus den zentrierten Merkmalen analog
			// covariance = X.transpose().times(X).times(1.0/(double)(X.getRowDimension()-1));
			correlation = rd.correlation();
			
			variance = rd.variance();

			System.out.println("Covariance =");
			System.out.println(covariance.toString());
			System.out.println("Correlation =");
			System.out.println(correlation.toString());			
//			PCA.formatierteAusgabe(correlation, constNames);
			
			System.out.println("Variance =");
			System.out.println(variance.toString());							
					
		} else {
			covariance = X;
			System.out.println("Covariance =");
			System.out.println(covariance.toString());
		} 		
		
		EigenvalueDecomposition e = covariance.eig();
		EigenVectors = e.getV();
		EigenValues = e.getD();

		System.out.println("EigenVectors =");
		System.out.println(EigenVectors.toString());
		System.out.println("EigenValues =");
		System.out.println(EigenValues.toString());	
		
		if(constNames != null) {
			System.out.println("Merkmale =");	
			for(int i=0; i<constNames.length; i++) {
				System.out.println(constNames[i]);	
			}			
			System.out.println("");				
		}
	}
	
	public AbstractMatrix getVectors() {
		return EigenVectors;
	}

	public AbstractMatrix getValues() {
		return EigenValues;
	}

	/**
	 * @return Returns the correlation.
	 */
	public AbstractMatrix getCorrelation() {
		return correlation;
	}
	
}
