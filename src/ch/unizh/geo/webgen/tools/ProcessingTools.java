package ch.unizh.geo.webgen.tools;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Vector;

import org.jmat.data.AbstractMatrix;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollectionSorted;
import ch.unizh.geo.webgen.model.ConstraintSpace;
import ch.unizh.geo.webgen.model.ConstraintSpaceArrayItem;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.server.WebGenRequestExecuter;

public class ProcessingTools {
	
	private static final String path = "C:\\java\\";
	
	public static final int NbrOperations = 11;
	public static final int NbrConstraints = 8;
	
	public static final String[] operationNameArray = {"AreaScalingRelative", "BuildingSimplifyGN", 
		"EnlargeToRectangle", "DisplaceConstrainedNewFast",
		"BuildingTypification (number features x 0.9)", "BuildingTypification (number features x 0.7)",
		"AreaFeatureRemoval", "AggregateBuiltUpArea", "ShrinkPartition (size x 0.9)", "ShrinkAllPolygons",
		"CompressPartitionConstrained"};
	public static String lookupOperation(int operationNames) {    	
		try {return operationNameArray[operationNames];}
		catch (ArrayIndexOutOfBoundsException e) {return null;}
	}
	
	public static final String[] operationShortNameArray = {"Scal", "Simp", "Rect", "Disp", "Ty10", "Ty30", "Del", "Agg", "Shri", "ShrA", "Comp"};
	public static String lookupOperationShort(int operationId) {
		try {return operationShortNameArray[operationId];}
		catch (ArrayIndexOutOfBoundsException e) {return null;}
	}
	
	public static ConstrainedFeatureCollection executeOperation(int operation, HashMap<String, Object> parameters, String webgenserver) {
		ConstrainedFeatureCollection fcNew = null;
    	WebGenRequest twgreq = null;
		switch (operation) {
		case 0:
			twgreq = WebGenRequestExecuter.callService(parameters, webgenserver, "AreaScalingRelative");
			break;
		case 1:
			twgreq = WebGenRequestExecuter.callService(parameters, webgenserver, "BuildingSimplifyGN");
			break;
		case 2:
			twgreq = WebGenRequestExecuter.callService(parameters, webgenserver, "EnlargeToRectangle");
			break;
		case 3:
			twgreq = WebGenRequestExecuter.callService(parameters, webgenserver, "DisplaceConstrainedNewFast");
			break;
		case 4:
			parameters.put("maxnumber", new Integer((int) Math.floor(((ConstrainedFeatureCollection)parameters.get("geom")).size() * 0.9))); // 10% Reduktion
			twgreq = WebGenRequestExecuter.callService(parameters, webgenserver, "BuildingTypification");
			parameters.remove("maxnumber");
			break;
		case 5:
			parameters.put("maxnumber", new Integer((int) Math.floor(((ConstrainedFeatureCollection)parameters.get("geom")).size() * 0.7))); // 30% Reduktion
			twgreq = WebGenRequestExecuter.callService(parameters, webgenserver, "BuildingTypification");
			parameters.remove("maxnumber");
			break;
		case 6:
			parameters.put("toosmall", new Double(((Double)parameters.get("minarea")) / 2));
			twgreq = WebGenRequestExecuter.callService(parameters, webgenserver, "AreaFeatureRemoval");
			parameters.remove("toosmall");
			break;
		case 7:
			twgreq = WebGenRequestExecuter.callService(parameters, webgenserver, "AggregateBuiltUpArea");
			break;
		case 8:
			parameters.put("factor", new Double(0.9));
			twgreq = WebGenRequestExecuter.callService(parameters, webgenserver, "ShrinkPartition");
			parameters.remove("factor");
			break;
		case 9:
			parameters.put("factor", new Double(0.9));
			twgreq = WebGenRequestExecuter.callService(parameters, webgenserver, "ShrinkAllPolygons");
			parameters.remove("factor");
			break;
		case 10:
			twgreq = WebGenRequestExecuter.callService(parameters, webgenserver, "CompressPartitionConstrained");
			break;
		}
		fcNew = (ConstrainedFeatureCollection)twgreq.getParameter("result");
    	if(fcNew == null ) fcNew = (ConstrainedFeatureCollection)twgreq.getResult("result");
    	return fcNew;
	}
	
	
	public static final String[] constraintNameArray = {"Size", "Leng", "Dist", "LWid", "DPos", "DEdg", "DWLR", "DOrie", "BWRat"};
	public static String lookupConstraint(int constraintId) {
		try {return constraintNameArray[constraintId];}
		catch (ArrayIndexOutOfBoundsException e) {return null;}
	}
	
	
	public static double getCostFromCostVector(Double[] costVec) {
    	double costAll = 0.0;
    	for(int i=0; i<costVec.length; i++) {
    		costAll += costVec[i].doubleValue();
    	}
    	return costAll;    
    }
    
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
				if( ((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(i)).getOperation() == ProcessingTools.lookupOperation(j)) {
					featureOperationVec[j] = new Double(((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(i)).getCost());
					j = NbrOperations; // Abbruch
				}							
			}
		}
    	return featureOperationVec;    
    }
    
    
    /*public static void makeHistoryStep(ConstrainedFeatureCollection fc, String message) {
    	for(Iterator iter = fc.iterator(); iter.hasNext();) {
    		((ConstrainedFeature) iter.next()).getConstraint().updateHistory(message);
    	}
    }*/
    
    
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
    		String nameOperationBefore = ProcessingTools.lookupOperationShort(idOperationBefore);
    		operationCostProg[idOperationBefore] = costOperationBefore;
    		operationPosProg[idOperationBefore] = testedOperations;
    		
    		ConstraintSpaceArrayItem operationAfter = (ConstraintSpaceArrayItem)operationVectorAfter.get(testedOperations);
        	double costOperationAfter = operationAfter.getValue();
        	int idOperationAfter = operationAfter.getId();
    		String nameOperationAfter = ProcessingTools.lookupOperationShort(idOperationAfter);
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
	
    public static StringBuffer prognoseOut = new StringBuffer();
    
    public static void writePrognose() {
    	try {
    		FileWriter fw = new FileWriter(path+"testprognose.txt"); 
    		BufferedWriter bfw = new BufferedWriter(fw);
			bfw.write(prognoseOut.toString());
			bfw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		prognoseOut = new StringBuffer();
    }
    
    public static StringBuffer statisticOut = new StringBuffer();
    
    public static void writeStatistics(String filename) {
    	try {
    		FileWriter fw = new FileWriter(path+filename+".txt"); 
    		BufferedWriter bfw = new BufferedWriter(fw);
			bfw.write(prognoseOut.toString());
			bfw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		prognoseOut = new StringBuffer();
    }
	
    
	private static final String constraintSpaceFileName = path+"constrspace.ser";
	
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
    		ConstraintSpace ts = trainedConstraintSpace;
    		os.writeObject(ts);
    		os.close();
    	}
    	catch(IOException e) {
    		e.printStackTrace();
    	}
    }
    
    
    public static void showCorrelations(AbstractMatrix matrix) {
		/*for(int i = 0; i < matrix.getRowDimension();  i++) {
			for(int j = 0; j < i;  j++) {		
				DecimalFormat df;
				df = new DecimalFormat("#0.000");
				if(i!=j) {
					if (Double.isNaN(matrix.get(i,j)) == false) {
						if(matrix.get(i,j) > 0.80) {
							System.out.println("Constraint -" + lookupConstraint(j) + "- korreliert mit Constraint -" + lookupConstraint(i) + "- (" +  df.format(matrix.get(i,j)) + ")");
						}
						if(matrix.get(i,j) < -0.80) {
							System.out.println("Constraint -" + lookupConstraint(j) + "- antikorreliert mit Constraint -" + lookupConstraint(i) + "- (" +  df.format(matrix.get(i,j)) + ")");
						}
					}
				}
			}		
		}*/
	}
    
    
    public static StringBuffer globalStat = new StringBuffer();
    public static double initalCostSum = 0.0;
    public static double finalCostSum = 0.0;
    public static int nbrPartitions = 0;
    public static int totalNbrIterations = 0;
    public static void statisticsReset() {
    	ProcessingTools.globalStat = new StringBuffer();
		ProcessingTools.initalCostSum = 0.0;
		ProcessingTools.finalCostSum = 0.0;
		ProcessingTools.nbrPartitions = 0;
		ProcessingTools.totalNbrIterations = 0;
    }
	
}
