package ch.unizh.geo.webgen.tools;

import java.text.DecimalFormat;

import org.jmat.data.AbstractMatrix;
import org.jmat.data.Matrix;
import org.jmat.data.RandomMatrix;


/**
 * @author Dirk Burghardt
 */

public class PCA {

	private AbstractMatrix 	variance;
	private AbstractMatrix 	deviation;			// Abweichung (x-x_m)/x_m in Prozent
	private AbstractMatrix 	deviationMean;
	private AbstractMatrix 	deviationMax;
	private PcaKernel 		pcaKernel;
	
	public PCA() {
	}
	
	public Matrix makePCA(AbstractMatrix X0, boolean cov, String[] constNames) {
		
		// 0. Vorarbeiten
//		boolean zentrieren	 	= true;		// Daten zentrieren
//		boolean standardisieren = true;		// Daten standardisieren
		boolean zentrieren	 	= false;		// Daten zentrieren
		boolean standardisieren = false;		// Daten standardisieren
		
//		AbstractMatrix X = X0.transpose();
		AbstractMatrix X = X0.copy();
		//AbstractMatrix S;
		System.out.println("Daten =");
		System.out.println(X.toString());		
		int nbrMerkmal = X.getColumnDimension();
		int nbrObjekte = X.getRowDimension();

		// 1. Zentrierung
		//    durch Multiplikation mit M_n = I_n - 1/n 11'
		//    von rechts für Spalten
		//    von links für Reihen
		//
		if (zentrieren == true) {
			// a) Erzeugen der Zentrierungsmatrix M_n = I_n - 1/n 11'
			AbstractMatrix I = org.jmat.data.Matrix.identity(nbrObjekte, nbrObjekte);
			AbstractMatrix Eins = new Matrix(nbrObjekte, 1, 1);		
			AbstractMatrix M = I.minus(Eins.times(Eins.transpose()).times(1.0/(double)nbrObjekte));
			// b) Zentrierungsmatrix auf Datenmatrix anwenden
			X = M.times(X);
			System.out.println("Datenmatrix mit Zentrierung =");
			System.out.println(X.toString());					
		}

		// 2. Standardisieren - 
		//    Dividiert man die Werte eines zentrierten Merkmals noch durch die 
		//    Standardabweichung dieses Merkmals, so erhält man standardisierte Merkmale
		//    Standardabweichung dieses Merkmals = Wurzel aus der Varianz des Merkmals
		if (standardisieren == true) {
			// s^2 - Stichprobenvarianz
			variance = new RandomMatrix(X).variance();
			// Wurzel aus der Varianz für jedes Element liefert die Standardabweichung
			System.out.print("Standardabweichung (sigma) der zentrierten Daten =");
			formatierteAusgabe(variance.ebeSqrt().transpose(), constNames);
			
			// Kontrolle, dass Standardabweichung nicht irgendwo null ist, 
			// da sonst Division durch null erfolgt
			boolean go = true;
			for(int i=0; i<variance.getColumnDimension(); i++) {
				if(variance.get(0, i) < 0.000001) {
					go = false;
					i = variance.getColumnDimension();
				}
			}						
			if(go == true) {
				// Wurzel ziehen			
				Matrix v = org.jmat.data.Matrix.diagonal(variance.ebeSqrt().toDoubleArray());
				// Jede Spalte durch Standardabweichung dividieren
				X = X.divide(v);				
				System.out.println("Datenmatrix mit Standardisierung =");
				System.out.println(X.toString());							
			} else {
				System.out.println("Keine Standardisierung, da Standardabweichung gleich null.\n");											
			}
		}		
//		new FrameView(X.toPlot2DPanel("Originaldaten", PlotPanel.SCATTER));		
		
		// 3. PCA
		pcaKernel = new PcaKernel(X, cov, constNames);				 
		
		// 4.  Anteile der Merkmale an der Hauptkomponente
		int hauptkomponente = 0;
		for(int i = nbrMerkmal-1; i>-1; i--) {
			AbstractMatrix V = pcaKernel.getVectors().getColumn(i);
			AbstractMatrix H = org.jmat.data.Matrix.incrementRows(V.getRowDimension(), 1, 1, 1);
			H.mergeColumnsEquals(V);
			//String title = "H" + java.lang.String.valueOf(++hauptkomponente);
			//new FrameView(new Plot2DPanel(H, title, PlotPanel.BAR));
			System.out.println("H" + hauptkomponente + " =");
			System.out.println(H.toString());						
			System.out.println(V.toString());
/*			switch (hauptkomponente) {
				case 1:
					Out.println(Out.PCA_FILE_OUT1, V.transpose().toString());
					break;
				case 2:
					Out.println(Out.PCA_FILE_OUT2, V.transpose().toString());
					break;
				case 3:
					Out.println(Out.PCA_FILE_OUT3, V.transpose().toString());
					break;
				case 4:
					Out.println(Out.PCA_FILE_OUT4, V.transpose().toString());
					break;
				case 5:
					Out.println(Out.PCA_FILE_OUT5, V.transpose().toString());
					break;
				case 6:
					Out.println(Out.PCA_FILE_OUT6, V.transpose().toString());
					break;
				case 7:
					Out.println(Out.PCA_FILE_OUT7, V.transpose().toString());
					break;
				case 8:
					Out.println(Out.PCA_FILE_OUT8, V.transpose().toString());
					break;
			}
*/		}
		
//		// 4.1 Anteil der 1. Hauptkomponente
//		AbstractMatrix V1 = pcaKernel.getVectors().getColumn(nbrMerkmal-1);
//		AbstractMatrix H1 = org.jmat.data.Matrix.incrementRows(V1.getRowDimension(), 1, 1, 1);
//		H1.mergeColumnsEquals(V1);
//		new FrameView(new Plot2DPanel(H1, "1. Hauptkomponente", PlotPanel.BAR));
//		System.out.println("H1 =");
//		System.out.println(H1.toString());			
//
//		// 4.2 Hauptkomponente
//		AbstractMatrix V2 = pcaKernel.getVectors().getColumn(nbrMerkmal-2);
//		AbstractMatrix H2 = org.jmat.data.Matrix.incrementRows(V2.getRowDimension(), 1, 1, 1);
//		H2.mergeColumnsEquals(V2);
//		new FrameView(new Plot2DPanel(H2, "2. Hauptkomponente", PlotPanel.BAR));
//		System.out.println("H2 =");
//		System.out.println(H2.toString());			
		
		// 5. Darstellung der Merkmale bzgl. 1. und 2. Hauptkomponente
		AbstractMatrix Z1 = X.times(pcaKernel.getVectors().getColumn(nbrMerkmal-1));
		AbstractMatrix Z2 = X.times(pcaKernel.getVectors().getColumn(nbrMerkmal-2));
		Z1.mergeColumnsEquals(Z2);		
		//new FrameView(Z1.toPlot2DPanel("[Z1,Z2]", PlotPanel.SCATTER));		

		// 6. Scores - Projektion der Werte auf Hauptkomponenten
		System.out.println("Scores bezüglich der ersten Hauptkomponente =");
		System.out.println(X.times(pcaKernel.getVectors().getColumn(nbrMerkmal-1)).toString());	
		
		return(new Matrix(pcaKernel.getCorrelation().getArrayCopy()));
//		return;
	}	

	public void calculateDeviations(AbstractMatrix X0) {
		// Abweichung berechnen (x-x_m)/x_m
		// unter Verwendung des zweiten Teiles der Zentrierungsmatrix (1/n 11')
		AbstractMatrix X_t = X0.copy();
		AbstractMatrix X = X0.copy();
		int nbrObjekte = X0.getRowDimension();
		AbstractMatrix Eins = new Matrix(nbrObjekte, 1, 1);		
		AbstractMatrix T = Eins.times(Eins.transpose()).times(1.0/(double)nbrObjekte);
		T = T.times(X_t);
		
		// Zentrierte Matrix mal Inverse von 1/n*11'*X = (1/n*11'*X)^(-1) 
		X = X.ebeDivide(T); 
		deviation = new Matrix(nbrObjekte, nbrObjekte, 0.0);	
		deviation = X.copy();
		deviation = deviation.ebeAbs();	// Betrag von jedem Element
		
		// Maximalwert pro Spalte (Constraint)
		deviationMax = deviation.max().copy();
		
		// Mittelwert pro Spalte
		deviationMean = deviation.mean().copy();
	}
			
	static public void formatierteAusgabe(AbstractMatrix matrix, String[] names) {
		for(int i = 0; i < matrix.getRowDimension();  i++) {
			if(names != null)
				System.out.print("\n " + names[i].substring(0, 15));
			for(int j = 0; j < matrix.getColumnDimension();  j++) {
				DecimalFormat df;
				if(matrix.get(i,j) < 0)
					df = new DecimalFormat("#0.00");				
				else
					df = new DecimalFormat(" #0.00");				
				System.out.print("\t" + df.format(matrix.get(i,j)));		
			}		
		}
		System.out.println("");		
	}
	
	/**
	 * @return Returns the pcaKernel.
	 */
	public PcaKernel getPcaKernel() {
		return pcaKernel;
	}

	/**
	 * @return Returns the variance.
	 */
	public AbstractMatrix getVariance() {
		return variance;
	}
	
	/**
	 * @return Returns the deviation.
	 */
	public AbstractMatrix getDeviation() {
		return deviation;
	}
	
	/**
	 * @return Returns the deviationMax.
	 */
	public AbstractMatrix getDeviationMax() {
		return deviationMax;
	}
	
	/**
	 * @return Returns the deviationMean.
	 */
	public AbstractMatrix getDeviationMean() {
		return deviationMean;
	}
}