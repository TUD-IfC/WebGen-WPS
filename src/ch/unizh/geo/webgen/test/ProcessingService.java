package ch.unizh.geo.webgen.test;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollectionSorted;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollectionSortedComparator;
import ch.unizh.geo.webgen.model.ConstraintSpace;
import ch.unizh.geo.webgen.model.ConstraintSpaceArrayItem;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.service.AreaFeatureRemoval;
import ch.unizh.geo.webgen.service.AreaPartitioning;
import ch.unizh.geo.webgen.service.AreaScalingRelative;
import ch.unizh.geo.webgen.service.BuildingSimplifyGN;
import ch.unizh.geo.webgen.service.BuildingTypification;
import ch.unizh.geo.webgen.service.DisplaceFeaturesFastConstrained;
import ch.unizh.geo.webgen.service.EnlargeToRectangle;
import ch.unizh.geo.webgen.service.Eval_GMFeat_All;
import ch.unizh.geo.webgen.tools.ProcessingTools;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;

public class ProcessingService extends AbstractPlugIn implements ThreadedPlugIn {

	private static Logger LOGGER = Logger.getLogger(ProcessingService.class);
	
	static int NbrOperations = ProcessingTools.NbrOperations;
	static int NbrConstraints = ProcessingTools.NbrConstraints;
	
	private MultiInputDialog dialog;
	Double minarea;
	Double minlength;
	Double mindist;
	Double roaddist;
	
	StringBuffer prognoseOut;
	
	public ProcessingService() {
    }

    public void initialize(PlugInContext context) throws Exception {
    	context.getFeatureInstaller().addMainMenuItem(
                this, "GenProcess", "WG06 Processing Service",
    			null, null);
    }

    public boolean execute(PlugInContext context) throws Exception {
    	try {
    		initDialog(context);
        	dialog.setVisible(true);
        	if (!dialog.wasOKPressed()) {return false;}
        	return true;
    	}
    	catch (java.lang.IndexOutOfBoundsException e) {return false;}
    }

    private void initDialog(PlugInContext context) {
        dialog = new MultiInputDialog(context.getWorkbenchFrame(), "Processing Service", true);
        dialog.setSideBarDescription("Processing Service");
        dialog.addLayerComboBox("selection", context.getCandidateLayer(0), null, context.getLayerManager());
        dialog.addLayerComboBox("buildings", context.getCandidateLayer(0), null, context.getLayerManager());    
 		dialog.addDoubleField("minarea", 200.0, 5);
        dialog.addDoubleField("minlength", 10.0, 5);
        dialog.addDoubleField("mindist", 10.0, 5);
        dialog.addDoubleField("roaddist", 0.0, 5);
        GUIUtil.centreOnWindow(dialog);
    }

    public void run(TaskMonitor monitor, PlugInContext context)
        throws Exception {
    	prognoseOut = new StringBuffer();
    	
    	minarea = dialog.getDouble("minarea");
    	minlength = dialog.getDouble("minlength");
    	mindist = dialog.getDouble("mindist");
    	roaddist = dialog.getDouble("roaddist");
    	
    	FeatureCollection fc_selection = dialog.getLayer("selection").getFeatureCollectionWrapper();
    	FeatureCollection fc_buildings = dialog.getLayer("buildings").getFeatureCollectionWrapper();
    	if(!(fc_selection instanceof ConstrainedFeatureCollection)) fc_selection = new ConstrainedFeatureCollection(fc_selection, true);
    	if(!(fc_buildings instanceof ConstrainedFeatureCollection)) fc_buildings = new ConstrainedFeatureCollection(fc_buildings, true);
    	
    	// GENERALISATION SERVICE
    	// partioning support service
    	WebGenRequest wgreq = new WebGenRequest();
    	wgreq.addFeatureCollection("selection", fc_selection);
    	wgreq.addFeatureCollection("geom", fc_buildings);
    	(new AreaPartitioning()).run(wgreq);
    	HashMap fc_partitions = wgreq.getResults();
    	
    	//result_all contains finally all generalized buildings
    	ConstrainedFeatureCollection result_all = new ConstrainedFeatureCollection(fc_buildings.getFeatureSchema());

    	// Zuweisung von feature - constraint - operation als Trainingsersatz 
    	//TrainedConstraintSpace trainedConstraintSpace = trainConstraintSpace();
    	ConstraintSpace trainedConstraintSpace = openConstraintSpace();
    	
    	//moritz GenOL zwei Constraint arrays fŸr vorher und nachher
		Double[] genolbefore = null;
		Double[] genoldiff = null;
		String genoloperationbefore = null;
		HashMap genolopstat = new HashMap();
		Vector genolactop;
    	
    	// Schleife über Partionen    	
    	int partition_cnt = (int)fc_partitions.size()/2;
    	int[] iterationNr = new int[partition_cnt];
    	for(int i=0; i<partition_cnt; i++) {
    		//get hashmap entry
    		LOGGER.info("Processing partition" + (i+1) + "...");
    		prognoseOut.append("\n");
    		Object partition_value = fc_partitions.get("partition" + (i+1));
    		Object partpoly_value = fc_partitions.get("partpoly" + (i+1));
    		//check if partition is featurecollection
    		if(!(partition_value instanceof ConstrainedFeatureCollection)) throw new Exception();
    		if((partpoly_value != null) && !(partpoly_value instanceof ConstrainedFeatureCollection)) throw new Exception();
    		
    		//get partition features and load into wgrequest for services
    		ConstrainedFeatureCollection partfc = (ConstrainedFeatureCollection) partition_value;
    		WebGenRequest partwgreq = new WebGenRequest();
    		partwgreq.addParameter("minarea", minarea);
    		partwgreq.addParameter("minlength", minlength);
    		partwgreq.addParameter("mindist", mindist);
    		partwgreq.addParameter("roaddist", roaddist);
    		partwgreq.addParameter("geom", partfc);
    		partwgreq.addParameter("congeom", (ConstrainedFeatureCollection) partpoly_value);
    		
        	//Evaluation / Conflict analysis
    		int iterations = 0;
    		boolean run = true;
    		while(run == true) {
    			Double[] featureCostVec = evalPartitions(partwgreq);
    			double costAllCurrent = getCostFromCostVector(featureCostVec);
    			if(iterations == 0) {
    				//ProcessingTools.makeHistoryStep(partfc, "Iteration " + iterations + " - Evaluation before");
    				partfc.makeConstraintHistoryStep("Iteration " + iterations + " - Evaluation before");
    			}
    			
    	    	Vector operationVectorBefore = trainedConstraintSpace.getOperationVectorFromFeatureCost(featureCostVec);
    			Vector featureCollectionVector = processPartition(partwgreq, trainedConstraintSpace, featureCostVec);
    			Double[] featureOperationVec = ProcessingTools.getFeatureOperationVector(featureCollectionVector);
    			trainedConstraintSpace.addFeatureCostAndOperation(featureCostVec, featureOperationVec);							
    			Collections.sort(featureCollectionVector, new ConstrainedFeatureCollectionSortedComparator());
    			
    			
				//moritz GenOL
		   		if(genolbefore != null) {
		   			genoldiff = new Double[NbrConstraints];
		   			for(int j = 0; j< featureCostVec.length; j++) {
		   				genoldiff[j] = new Double(genolbefore[j].doubleValue() - featureCostVec[j].doubleValue());
		   			}
		   			if(genolopstat.containsKey(genoloperationbefore)) {
			   			genolactop = (Vector) genolopstat.get(genoloperationbefore);
			   			genolactop.add(genoldiff);
			   		}
		   			else {
		   				genolactop = new Vector();
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
        				ConstrainedFeatureCollection fcNew = (ConstrainedFeatureCollection)((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(0)).getFeatureCollection();
        				partwgreq.addParameter("geom", fcNew);
        				//ProcessingTools.makeHistoryStep(fcNew, "Operation " + ((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(0)).getOperation()+ " - Evaluation after");
        				fcNew.makeConstraintHistoryStep("Operation " + ((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(0)).getOperation()+ " - Evaluation after");
        				partfc = fcNew;
        		   		DecimalFormat df = new DecimalFormat("#0.0000");				
        		   		LOGGER.info("Operation  >>> " + ((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(0)).getOperation() + " <<<  successful (" + df.format(costAllNew) + ")");

            			// Vergleich der Costen pro Operator für ähnliche und aktuelle Partion
            	    	Vector operationVectorAfter = trainedConstraintSpace.getOperationVectorFromFeatureCost(featureCostVec);
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
    		
    		context.addLayer(StandardCategoryNames.WORKING, "result_part"+i, (ConstrainedFeatureCollection)partwgreq.getParameter("geom"));
			// speicherung aller uebrigen gebaeude in der output collection result_all
			result_all.addAll(((FeatureCollection)partwgreq.getParameter("geom")).getFeatures());
			iterationNr[i] = iterations;
    	}// Ende for(int i=0; i<partition_cnt; i++)

		System.out.print("\nFeatureConstraints");	
		for(int j=0; j<NbrConstraints; j++) {
			System.out.print("    " + ProcessingTools.lookupConstraint(j));
		}
		System.out.print("\nFeatureOperation  ");	
		for(int j=0; j<NbrOperations; j++) {
			System.out.print("    " + ProcessingTools.lookupOperationShort(j));
		}
		System.out.println("\n-----------------------------------------------------------------");	
    	
		// Kontrollausgabe
		int iterationNrSummed = iterationNr[0];
		int partionNr = 0;
    	for(int i=0; i<trainedConstraintSpace.getNbrFeatures(); i++ ) {
    		Double[] featureCost = trainedConstraintSpace.getFeatureCostById(i);
    		System.out.print("\nFeatureConstraints ");	
    		for(int j=0; j<featureCost.length; j++) {
    			DecimalFormat df = new DecimalFormat(" #0.0000");				
        		System.out.print(" " + df.format(featureCost[j]));	    			
    		}
    		Double[] featureOperation = trainedConstraintSpace.getFeatureOperationById(i);
    		System.out.print("\nFeatureOperation   ");	
    		for(int j=0; j<featureOperation.length; j++) {
    			DecimalFormat df = new DecimalFormat(" #0.0000");				
        		System.out.print(" " + df.format(featureOperation[j]));	    			
    		}    		
    		System.out.println();
    		if(i == iterationNrSummed) {
    			System.out.println("-----------------------------------------------------------------");
    			partionNr++;
    			if(partionNr < iterationNr.length)
    				iterationNrSummed += iterationNr[partionNr];
    		}
    	}
    	
    	//Ausgabe GenOL
		System.out.println("\n\nGenOL Auswertung: ");
		Iterator genoliter = genolopstat.entrySet().iterator();
		while(genoliter.hasNext()) {
			Map.Entry top = (Map.Entry)genoliter.next();
			System.out.println("\n\n" + top.getKey());
			double[] topavg = new double[NbrConstraints];
			int topavgi = 0;
			for(Iterator topiter = ((Vector)top.getValue()).iterator(); topiter.hasNext();) {
				Double[] topvals = (Double[]) topiter.next();
				for(int k=0; k<topavg.length; k++) topavg[k] += topvals[k].doubleValue();
				topavgi++;
			}
			DecimalFormat df = new DecimalFormat("#0.0000");
			for(int k=0; k<topavg.length; k++) {
				topavg[k] /= topavgi;
				System.out.print(df.format(topavg[k]) + "\t");
			}
		}
    	
    	context.addLayer(StandardCategoryNames.WORKING, "result_all", result_all);
    	saveConstraintSpace(trainedConstraintSpace);
    	FileWriter fw = new FileWriter("C:\\java\\test.txt"); 
    	//C:\Dokumente und Einstellungen\burg\Eigene Dateien\withSave\Experimente\Steuerung\test.txt
    	BufferedWriter bfw = new BufferedWriter(fw);
    	bfw.write(prognoseOut.toString());
    	bfw.close();
    }
    
    
    public Double[] evalPartitions(WebGenRequest wgreq) {
    	(new Eval_GMFeat_All()).run(wgreq);
    	Double[] partitionCost = (Double[])wgreq.getResult("severities");
    	for(int i=4; i<NbrConstraints; i++) partitionCost[i] *= 0.25;
    	return partitionCost;
    }

    
    public static double getCostFromCostVector(Double[] costVec) {
    	double costAll = 0.0;
    	for(int i=0; i<costVec.length; i++) {
    		costAll += costVec[i].doubleValue();
    	}
    	return costAll;    
    }
    
    public static ConstraintSpace trainConstraintSpace() {
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
    
    public static ConstraintSpace openConstraintSpace() {
    	try {
    		FileInputStream fis = new FileInputStream("X:\\neun\\constrspace.ser");
    		//C:\Dokumente und Einstellungen\burg\Eigene Dateien\withSave\Experimente\Steuerung\constrspace.ser
        	ObjectInputStream ois = new ObjectInputStream(fis);
        	ConstraintSpace tcs = (ConstraintSpace)ois.readObject();
        	ois.close();
        	return tcs;
    	}
    	catch(Exception e) {
    		//e.printStackTrace();
    		return trainConstraintSpace();
    	}
    }
    
    public static void saveConstraintSpace(ConstraintSpace tcs) {
    	try {
    		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("C:\\Dokumente und Einstellungen\\burg\\Eigene Dateien\\withSave\\Experimente\\Steuerung\\constrspace.ser"));
    		os.writeObject(tcs);
    		os.close();
    	}
    	catch(IOException e) {}
    }
    
    public Vector processPartition(WebGenRequest wgreq, ConstraintSpace trainedConstraintSpace, Double[] partionCost) {
		Vector sortedFeatureCollectionVector = new Vector(); 	    	
		
    	ConstrainedFeatureCollection fcNew = null;
    	WebGenRequest twgreq = null;
		//alle Operationen ausführen für die das Vergleichsfeature Kosten senken konnte
    	Vector operationSequence = ProcessingTools.getConstraintSpace().getOperationVectorFromFeatureCost(partionCost);
    	System.out.println("Operator-Reihenfolge: " + operationSequence.toString());
    	for(int testedOperations = 0; testedOperations < NbrOperations; testedOperations++) {
        	int operation = ((ConstraintSpaceArrayItem)operationSequence.get(testedOperations)).getId();
        	String operationName = ProcessingTools.lookupOperation(operation);
        	twgreq = wgreq.cloneWithoutResults(); //clone because of local call
	 		twgreq.addFeatureCollection("geom", ((ConstrainedFeatureCollection)wgreq.getFeatureCollection("geom")).clone());
            	switch (operation) {
            	 	case 0: 
            	 		(new AreaScalingRelative()).run(twgreq);
            	 		fcNew = (ConstrainedFeatureCollection)twgreq.getResult("result");
            	 		break;
            	 	case 1:
            	 		(new BuildingSimplifyGN()).run(twgreq);
            	 		fcNew = (ConstrainedFeatureCollection)twgreq.getResult("result");
            	 		break;
            	 	case 2:
            	 		(new EnlargeToRectangle()).run(twgreq);
            	 		fcNew = (ConstrainedFeatureCollection)twgreq.getResult("result");
                		break;
            	 	case 3:
            	 		(new DisplaceFeaturesFastConstrained()).run(twgreq);
            	 		fcNew = (ConstrainedFeatureCollection)twgreq.getResult("result");
            	 		break;
            	 	case 4:
            	 		int maxnumber10 = (int)Math.floor((twgreq.getFeatureCollection("geom")).size()*0.9);	// Anzahl feature um 10% reduzieren
            	 		twgreq.addParameter("maxnumber", new Integer(maxnumber10));
            	 		(new BuildingTypification()).run(twgreq);
            	 		try {fcNew = (ConstrainedFeatureCollection)twgreq.getResult("result");}
                		catch (Exception e) { fcNew = (ConstrainedFeatureCollection)twgreq.getFeatureCollection("geom");}
            	 		break;
            	 	case 5:
            	 		int maxnumber30 = (int)Math.floor((twgreq.getFeatureCollection("geom")).size()*0.7);	// Anzahl feature um 30% reduzieren
            	 		twgreq.addParameter("maxnumber", new Integer(maxnumber30));
            	 		(new BuildingTypification()).run(twgreq);
                		try {fcNew = (ConstrainedFeatureCollection)twgreq.getResult("result");}
                		catch (Exception e) { fcNew = (ConstrainedFeatureCollection)twgreq.getFeatureCollection("geom");}
            	 		break;
            	 	case 6:
            	 		twgreq.addParameter("toosmall", new Double(wgreq.getParameterDouble("minsize")/2));
            	 		(new AreaFeatureRemoval()).run(twgreq);
            	 		fcNew = (ConstrainedFeatureCollection)twgreq.getResult("result");
                		break;
            	}    	
            	if(fcNew != null ) {
            		//System.out.println("\nEvaluation for - " + operationName);
            		wgreq.addParameter("geom", fcNew);
        			Double[] costVec = evalPartitions(wgreq);
        			double costAllCurrent = getCostFromCostVector(costVec);            		
        			ConstrainedFeatureCollectionSorted sortedFeatureCollection = new ConstrainedFeatureCollectionSorted(costAllCurrent, fcNew, operationName);
        			sortedFeatureCollectionVector.add(sortedFeatureCollection);
            	}
    	}
    	return sortedFeatureCollectionVector;
    }
}
