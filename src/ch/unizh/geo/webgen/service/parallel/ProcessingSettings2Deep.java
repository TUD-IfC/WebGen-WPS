package ch.unizh.geo.webgen.service.parallel;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Vector;

import ch.unizh.geo.webgen.model.ConstrainedFeature;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollectionSorted;
import ch.unizh.geo.webgen.model.ConstraintSpace;
import ch.unizh.geo.webgen.model.ConstraintSpaceArrayItem;

public class ProcessingSettings2Deep {

	private static final ProcessingOperator[] operations = {
		new ProcessingOperator("AreaScalingRelative", "Scal", new String[]{"AreaScalingRelative"}, "localcloned"),
		new ProcessingOperator("BuildingSimplifyGN", "Simp", new String[]{"BuildingSimplifyGN"}, "localcloned"),
		new ProcessingOperator("EnlargeToRectangle", "Enla", new String[]{"EnlargeToRectangle"}, "localcloned"),
		new ProcessingOperator("DisplaceConstrainedNew", "Disp", new String[]{"DisplaceConstrainedNew"}, "localcloned"),
		new ProcessingOperator("BuildingTypificationNew (10% Reduktion)", "Ty10", new String[]{"BuildingTypificationNew"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.9)}}),
		new ProcessingOperator("BuildingTypificationNew (30% Reduktion)", "Ty30", new String[]{"BuildingTypificationNew"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.7)}}),
		new ProcessingOperator("AreaFeatureRemoval", "Del", new String[]{"AreaFeatureRemoval"}, "localcloned"),
		new ProcessingOperator("AreaScaling-AreaScaling", "ScSc", new String[]{"AreaScalingRelative","AreaScalingRelative"}, "localcloned"),
		new ProcessingOperator("AreaScaling-Simplify", "ScSi", new String[]{"AreaScalingRelative","BuildingSimplifyGN"}, "localcloned"),
		new ProcessingOperator("AreaScaling-EnlargeRect", "ScEn", new String[]{"AreaScalingRelative","EnlargeToRectangle"}, "localcloned"),
		new ProcessingOperator("AreaScaling-Displacement", "ScDi", new String[]{"AreaScalingRelative","DisplaceConstrainedNew"}, "localcloned"),
		new ProcessingOperator("AreaScaling-Typify10", "ScT1", new String[]{"AreaScalingRelative","BuildingTypificationNew"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.9)}}),
		new ProcessingOperator("AreaScaling-Typify30", "ScT3", new String[]{"AreaScalingRelative","BuildingTypificationNew"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.7)}}),
		new ProcessingOperator("AreaScaling-FeatRemove", "ScDe", new String[]{"AreaScalingRelative","AreaFeatureRemoval"}, "localcloned"),
		new ProcessingOperator("Simplify-AreaScaling", "SiSc", new String[]{"BuildingSimplifyGN","AreaScalingRelative"}, "localcloned"),
		new ProcessingOperator("Simplify-Simplify", "SiSi", new String[]{"BuildingSimplifyGN","BuildingSimplifyGN"}, "localcloned"),
		new ProcessingOperator("Simplify-EnlargeRect", "SiEn", new String[]{"BuildingSimplifyGN","EnlargeToRectangle"}, "localcloned"),
		new ProcessingOperator("Simplify-Displacement", "SiDi", new String[]{"BuildingSimplifyGN","DisplaceConstrainedNew"}, "localcloned"),
		new ProcessingOperator("Simplify-Typify10", "SiT1", new String[]{"BuildingSimplifyGN","BuildingTypificationNew"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.9)}}),
		new ProcessingOperator("Simplify-Typify30", "SiT3", new String[]{"BuildingSimplifyGN","BuildingTypificationNew"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.7)}}),
		new ProcessingOperator("Simplify-FeatRemove", "SiDe", new String[]{"BuildingSimplifyGN","AreaFeatureRemoval"}, "localcloned"),
		new ProcessingOperator("EnlargeRect-AreaScaling", "EnSc", new String[]{"EnlargeToRectangle","AreaScalingRelative"}, "localcloned"),
		new ProcessingOperator("EnlargeRect-Simplify", "EnSi", new String[]{"EnlargeToRectangle","BuildingSimplifyGN"}, "localcloned"),
		new ProcessingOperator("EnlargeRect-EnlargeRect", "EnEn", new String[]{"EnlargeToRectangle","EnlargeToRectangle"}, "localcloned"),
		new ProcessingOperator("EnlargeRect-Displacement", "EnDi", new String[]{"EnlargeToRectangle","DisplaceConstrainedNew"}, "localcloned"),
		new ProcessingOperator("EnlargeRect-Typify10", "EnT1", new String[]{"EnlargeToRectangle","BuildingTypificationNew"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.9)}}),
		new ProcessingOperator("EnlargeRect-Typify30", "EnT3", new String[]{"EnlargeToRectangle","BuildingTypificationNew"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.7)}}),
		new ProcessingOperator("EnlargeRect-FeatRemove", "EnDe", new String[]{"EnlargeToRectangle","AreaFeatureRemoval"}, "localcloned"),
		new ProcessingOperator("Displacement-AreaScaling", "DiSc", new String[]{"DisplaceConstrainedNew","AreaScalingRelative"}, "localcloned"),
		new ProcessingOperator("Displacement-Simplify", "DiSi", new String[]{"DisplaceConstrainedNew","BuildingSimplifyGN"}, "localcloned"),
		new ProcessingOperator("Displacement-EnlargeRect", "DiEn", new String[]{"DisplaceConstrainedNew","EnlargeToRectangle"}, "localcloned"),
		new ProcessingOperator("Displacement-Displacement", "DiDi", new String[]{"DisplaceConstrainedNew","DisplaceConstrainedNew"}, "localcloned"),
		new ProcessingOperator("Displacement-Typify10", "DiT1", new String[]{"DisplaceConstrainedNew","BuildingTypificationNew"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.9)}}),
		new ProcessingOperator("Displacement-Typify30", "DiT3", new String[]{"DisplaceConstrainedNew","BuildingTypificationNew"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.7)}}),
		new ProcessingOperator("Displacement-FeatRemove", "DiDe", new String[]{"DisplaceConstrainedNew","AreaFeatureRemoval"}, "localcloned"),
		new ProcessingOperator("Typify10-AreaScaling", "T1Sc", new String[]{"BuildingTypificationNew","AreaScalingRelative"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.9)}}),
		new ProcessingOperator("Typify10-Simplify", "T1Si", new String[]{"BuildingTypificationNew","BuildingSimplifyGN"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.9)}}),
		new ProcessingOperator("Typify10-EnlargeRect", "T1En", new String[]{"BuildingTypificationNew","EnlargeToRectangle"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.9)}}),
		new ProcessingOperator("Typify10-Displacement", "T1Di", new String[]{"BuildingTypificationNew","DisplaceConstrainedNew"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.9)}}),
		new ProcessingOperator("Typify10-Typify10", "T1T1", new String[]{"BuildingTypificationNew"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.81)}}),
		new ProcessingOperator("Typify10-Typify30", "T1T3", new String[]{"BuildingTypificationNew"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.63)}}),
		new ProcessingOperator("Typify10-FeatRemove", "T1De", new String[]{"BuildingTypificationNew","AreaFeatureRemoval"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.9)}}),
		new ProcessingOperator("Typify10-AreaScaling", "T3Sc", new String[]{"BuildingTypificationNew","AreaScalingRelative"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.7)}}),
		new ProcessingOperator("Typify10-Simplify", "T3Si", new String[]{"BuildingTypificationNew","BuildingSimplifyGN"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.7)}}),
		new ProcessingOperator("Typify10-EnlargeRect", "T3En", new String[]{"BuildingTypificationNew","EnlargeToRectangle"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.7)}}),
		new ProcessingOperator("Typify10-Displacement", "T3Di", new String[]{"BuildingTypificationNew","DisplaceConstrainedNew"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.7)}}),
		new ProcessingOperator("Typify10-Typify10", "T3T1", new String[]{"BuildingTypificationNew"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.63)}}),
		new ProcessingOperator("Typify10-Typify30", "T3T3", new String[]{"BuildingTypificationNew"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.49)}}),
		new ProcessingOperator("Typify10-FeatRemove", "T3De", new String[]{"BuildingTypificationNew","AreaFeatureRemoval"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.7)}}),
		new ProcessingOperator("FeatRemove-AreaScaling", "DeSc", new String[]{"AreaFeatureRemoval","AreaScalingRelative"}, "localcloned"),
		new ProcessingOperator("FeatRemove-Simplify", "DeSi", new String[]{"AreaFeatureRemoval","BuildingSimplifyGN"}, "localcloned"),
		new ProcessingOperator("FeatRemove-EnlargeRect", "DeEn", new String[]{"AreaFeatureRemoval","EnlargeToRectangle"}, "localcloned"),
		new ProcessingOperator("FeatRemove-Displacement", "DeDi", new String[]{"AreaFeatureRemoval","DisplaceConstrainedNew"}, "localcloned"),
		new ProcessingOperator("FeatRemove-Typify10", "DeT1", new String[]{"AreaFeatureRemoval","BuildingTypificationNew"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.9)}}),
		new ProcessingOperator("FeatRemove-Typify30", "DeT3", new String[]{"AreaFeatureRemoval","BuildingTypificationNew"}, "localcloned", new Object[][]{{"dimpercent", new Double(0.7)}}),
		new ProcessingOperator("FeatRemove-FeatRemove", "DeDe", new String[]{"AreaFeatureRemoval","AreaFeatureRemoval"}, "localcloned")
	};
	
	public static int NbrOperations = operations.length;
	public static final int NbrConstraints = 8;
	
	public static ProcessingOperator lookupOperation(int operationNames) {    	
		try {return operations[operationNames];}
		catch (ArrayIndexOutOfBoundsException e) {return null;}
	}
	
	public static String lookupOperationName(int operationNames) {    	
		try {return operations[operationNames].name;}
		catch (ArrayIndexOutOfBoundsException e) {return null;}
	}
	
	public static String lookupOperationShort(int operationId) {
		try {return operations[operationId].shortName;}
		catch (ArrayIndexOutOfBoundsException e) {return null;}
	}
	
	public static final String[] constraintNameArray = {"Size", "Leng", "Dist", "LWid", "DPos", "DEdg", "DWLR", "DOrie"};
	public static String lookupConstraint(int constraintId) {
		try {return constraintNameArray[constraintId];}
		catch (ArrayIndexOutOfBoundsException e) {return null;}
	}
	
	
	// +++++++++++++++++++++++ constraint space functions +++++++++++++++++++++++++++++++++++++++
	
	private static final String constraintSpaceFileName = "C:\\java\\constrspace2Deep.ser";
	private static ConstraintSpace trainedConstraintSpace = null;
	
	public static ConstraintSpace getConstraintSpace() {
		if(trainedConstraintSpace == null) openConstraintSpace();
		return trainedConstraintSpace;
	}
	
	public static ConstraintSpace trainConstraintSpace() {
    	trainedConstraintSpace = new ConstraintSpace(NbrOperations, NbrConstraints);
    	
    	// MinSize - 0; MinLength - 1; MinDist - 2; LocalWidth - 3
    	Double[] featureCost0 = new Double[NbrConstraints];
    	for(int i=0; i<NbrConstraints;i++) featureCost0[i] = new Double(1.0); // constraint values for feature 1
    	
    	// Scaling - 0; BuildingSimplification - 1, BuildingEnlargeToRectangle - 2; Displacement - 3; Typification - 4
    	Double[] featureOperat0 = new Double[NbrOperations];
    	for(int i=0; i<NbrOperations;i++) featureOperat0[i] = new Double(1.0); // constaint value diff of feature 1 for several operations
    	trainedConstraintSpace.addFeatureCostAndOperation(featureCost0, featureOperat0);
    	return(trainedConstraintSpace);
    }
    
    public static void openConstraintSpace() {
    	try {
    		FileInputStream fis = new FileInputStream(constraintSpaceFileName);
        	ObjectInputStream ois = new ObjectInputStream(fis);
        	trainedConstraintSpace = (ConstraintSpace)ois.readObject();
        	ois.close();
        	//return trainedConstraintSpace;
    	}
    	catch(Exception e) {
    		//e.printStackTrace();
    		//return trainConstraintSpace();
    		trainConstraintSpace();
    	}
    }
    
    public static void saveConstraintSpace() {
    	try {
    		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(constraintSpaceFileName));
    		os.writeObject(trainedConstraintSpace);
    		os.close();
    	}
    	catch(IOException e) {}
    }
    
    
    // +++++++++++++++++++++++ operation vector functions +++++++++++++++++++++++++++++++++++++++++
    
    public static Double[] getFeatureOperationVector(Vector featureCollectionVector) {
    	Double[] featureOperationVec = new Double[NbrOperations];
    	
		for(int i=0; i<NbrOperations; i++) {
			featureOperationVec[i] = new Double(9.9999);
		}
		// featureCollectionVector enthält Kosten für ausgeführte Operationen
		// Reihenfolge der ausgeführten Operationen ist abhängig von ähnlichstem Feature und dessen Bewertung der ausgeführten Operationen
		// hier erfolgt Sortierung, dass Kosten für gleiche Operationen immer an gleicher Stelle stehen
		for(int i=0; i<featureCollectionVector.size(); i++) {
			for(int j=0; j<NbrOperations; j++) {
				if( ((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(i)).getOperation() == lookupOperation(j).name) {
					featureOperationVec[j] = new Double(((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(i)).getCost());
					j = NbrOperations; // Abbruch
				}							
			}
		}
    	return featureOperationVec;    
    }
    
    
    // +++++++++++++++++++++++ constraint history functions ++++++++++++++++++++++++++++++++++++++
    
    public static void makeHistoryStep(ConstrainedFeatureCollection fc, String message) {
    	for(Iterator iter = fc.iterator(); iter.hasNext();) {
    		((ConstrainedFeature) iter.next()).getConstraint().updateHistory(message);
    	}
    }
    
    
    
    //+++++++++++++++++++++++ prognose functions +++++++++++++++++++++++++++++++++++++++++++++++++
    
    public static StringBuffer prognoseOut = new StringBuffer();
    
    public static void evaluatePrognose(Vector operationVectorBefore, Vector operationVectorAfter) {
		DecimalFormat df = new DecimalFormat("#0.0000");				
    	double[] operationCostProg = new double[NbrOperations];
    	double[] operationCostReal = new double[NbrOperations];
    	double[] operationPosProg = new double[NbrOperations];
    	double[] operationPosReal = new double[NbrOperations];
    	for(int testedOperations = 0; testedOperations < NbrOperations; testedOperations++) {
    		ConstraintSpaceArrayItem operationBefore = (ConstraintSpaceArrayItem)operationVectorBefore.get(testedOperations);
        	double costOperationBefore = operationBefore.getValue();
        	int idOperationBefore = operationBefore.getId();
    		String nameOperationBefore = lookupOperationShort(idOperationBefore);
    		operationCostProg[idOperationBefore] = costOperationBefore;
    		operationPosProg[idOperationBefore] = testedOperations;
    		
    		ConstraintSpaceArrayItem operationAfter = (ConstraintSpaceArrayItem)operationVectorAfter.get(testedOperations);
        	double costOperationAfter = operationAfter.getValue();
        	int idOperationAfter = operationAfter.getId();
    		String nameOperationAfter = lookupOperationShort(idOperationAfter);
    		operationCostReal[idOperationAfter] = costOperationAfter;
    		operationPosReal[idOperationAfter] = testedOperations;
    		
    		System.out.println(nameOperationBefore + "(" + df.format(costOperationBefore) + ")\t" + nameOperationAfter + "(" + df.format(costOperationAfter) + ")\t");
    	}
    	
		// Skalarprodukt für Kosten
		double cosinus1 = 0.0;
		double normA1 = 0.0;
		double normB1 = 0.0;
		for(int k=0; k<NbrOperations; k++) {
			cosinus1 += operationCostProg[k]*operationCostReal[k];
			normA1 += Math.pow(operationCostProg[k],2.0);
			normB1 += Math.pow(operationCostReal[k],2.0);
		}
		cosinus1 /= Math.sqrt(normA1)*Math.sqrt(normB1);
		System.out.println("Aehnlichkeit von Prognose und Realitaet fuer Kosten     : " + df.format(cosinus1));	
		
		// Skalarprodukt für Reihenfolge
		double cosinus2 = 0.0;
		double normA2 = 0.0;
		double normB2 = 0.0;
		for(int k=0; k<NbrOperations; k++) {
			cosinus2 += operationPosProg[k]*operationPosReal[k];
			normA2 += Math.pow(operationPosProg[k],2.0);
			normB2 += Math.pow(operationPosReal[k],2.0);
		}
		cosinus2 /= Math.sqrt(normA2)*Math.sqrt(normB2);
		System.out.println("Aehnlichkeit von Prognose und Realitaet fuer Reihenfolge: " + df.format(cosinus2));		
		prognoseOut.append(df.format(cosinus2) + " ");
    }
	
    public static void writePrognose() {
    	try {
    		FileWriter fw = new FileWriter("C:\\java\\prognose2Deep.txt"); 
    		BufferedWriter bfw = new BufferedWriter(fw);
			bfw.write(prognoseOut.toString());
			bfw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		prognoseOut = new StringBuffer();
    }
	
}
