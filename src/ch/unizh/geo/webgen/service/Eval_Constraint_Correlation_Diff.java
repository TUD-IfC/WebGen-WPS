package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import org.jmat.data.Matrix;

import ch.unizh.geo.webgen.model.Constraint;
import ch.unizh.geo.webgen.model.ConstraintSpace;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.registry.ParameterDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;


public class Eval_Constraint_Correlation_Diff extends AWebGenAlgorithm implements IWebGenAlgorithm {

	/**
	 * Input: 	FeatureCollection 1	- enthaelt als Attribut "costen" fuer N constraints
	 * 			FeatureCollection 2	- enthaelt als Attribut "costen" fuer N constraints  
	 * Output:  N x N array			- Correlationen der N constraints 
	 */
	
	public void run(WebGenRequest wgreq) {
		try {
			FeatureCollection fc = wgreq.getFeatureCollection("geom");
			Matrix correlationMatrix = calculateCorrelation(fc);
			wgreq.addResult("correlation", correlationMatrix);
		}
		catch (Exception e) {}
	}

	private Matrix calculateCorrelation(FeatureCollection fc) {
		
		if(!fc.getFeatureSchema().hasAttribute("constraint")) return null;
		if(fc.size() < 1) return null;
		
		int nbrConstraint_before = ((Constraint)((Feature)fc.getFeatures().get(0)).getAttribute("constraint")).getNbrConstraint();
		double[][] costen_before = new double[fc.size()][nbrConstraint_before];
		double[][] costen_after  = new double[fc.size()][nbrConstraint_before];
		double[][] costen        = new double[fc.size()][nbrConstraint_before];
		
		// Constraint Space für Constraint-Aenderung aufbauen
		int i = 0;
		Iterator iter = fc.iterator();
		while(iter.hasNext()) {
			Feature feat = (Feature)iter.next();			
			Constraint tconstraint = (Constraint) feat.getAttribute("constraint");
			costen_before[i] = tconstraint.getHistorySecondLast();
			costen_after[i]  = tconstraint.getHistoryLast();
			for(int j=0; j<costen_before[i].length; j++) {
				costen[i][j] = costen_before[i][j] - costen_after[i][j];
			}
			i++;
		}					
		
		// Constraint Space initialisieren
		//ConstraintSpace gnConstSpace = new ConstraintSpace();
		Matrix 		correlationMatrix  = ConstraintSpace.calculateCorrelation(costen);		
						
		return correlationMatrix;
	}
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("Eval_Constraint_Correlation_Diff", "sstein", "support",
				"",
				"Eval_Constraint_Correlation_Diff",
				"Evaluate Constraint Correlation Differences",
				"1.0");
		
		//add input parameters
		id.addInputParameter(new ParameterDescription("geom", "FeatureCollection", null, true, "layer with geometries to buffer"));
		
		//add output parameters
		id.addOutputParameter("correlation", "Matrix");
		return id;
	}
	
}
