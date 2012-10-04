package ch.unizh.geo.webgen.test;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollectionSorted;
import ch.unizh.geo.webgen.model.ConstraintSpace;
import ch.unizh.geo.webgen.model.ConstraintSpaceArrayItem;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.service.AreaPartitioning;
import ch.unizh.geo.webgen.service.AreaScalingRelative;
import ch.unizh.geo.webgen.service.BuildingSimplifyGN;
import ch.unizh.geo.webgen.service.BuildingTypification;
import ch.unizh.geo.webgen.service.DisplaceFeaturesFast;
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

public class ProcessingServiceNew extends AbstractPlugIn implements ThreadedPlugIn {

	private static Logger LOGGER = Logger.getLogger(ProcessingServiceNew.class);
	
	private MultiInputDialog dialog;
	
	static HashMap globalParams =  new HashMap();

	int NbrOperations = 6;
	int NbrConstraints = 8;
	
	public ProcessingServiceNew() {
    }

    public void initialize(PlugInContext context) throws Exception {
    	context.getFeatureInstaller().addMainMenuItem(
                this, "GenProcess", "WG06 Processing Service New",
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
 		dialog.addDoubleField("minarea", 0.0, 5);
        dialog.addDoubleField("minlength", 0.0, 5);
        dialog.addDoubleField("mindist", 0.0, 5);
        GUIUtil.centreOnWindow(dialog);
    }

    public void run(TaskMonitor monitor, PlugInContext context)
        throws Exception {
    	globalParams.put("minarea",dialog.getDouble("minarea"));
    	globalParams.put("minlength",dialog.getDouble("minlength"));
    	globalParams.put("mindist",dialog.getDouble("mindist"));
    	//globalParams.put("roaddist",dialog.getDouble("roaddist"));
    	
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
    	
    	// Schleife über Partionen    	
    	int partition_cnt = fc_partitions.size()/2;
    	int[] iterationNr = new int[partition_cnt];
    	for(int i=0; i<partition_cnt; i++) {
    		//get hashmap entry
    		System.out.println();
    		System.out.println("Processing partition" + (i+1) + "...");
    		
    		ConstrainedFeatureCollection partgeom = (ConstrainedFeatureCollection)fc_partitions.get("partition" + (i+1));
    		
        	//Evaluation / Conflict analysis
    		int iterations = 0;
    		boolean run = true;
    		while(run == true) {
    			Double[] featureCostVec = evalPartitions(partgeom);
    			double costAllCurrent = getCostFromCostVector(featureCostVec);
    			if(iterations == 0) {
    				//ProcessingTools.makeHistoryStep(partgeom, "Iteration " + iterations + " - Evaluation before");
    				partgeom.makeConstraintHistoryStep("Iteration " + iterations + " - Evaluation before");
    			}
    	    	Vector operationVectorBefore = ProcessingTools.getConstraintSpace().getOperationVectorFromFeatureCost(featureCostVec);
    			
    			Vector featureCollectionVector = processPartition(partgeom, ProcessingTools.getConstraintSpace(), featureCostVec);
    			Double[] featureOperationVec = ProcessingTools.getFeatureOperationVector(featureCollectionVector);
    			ProcessingTools.getConstraintSpace().addFeatureCostAndOperation(featureCostVec, featureOperationVec);
    			
    			OperationComparator comparator = new OperationComparator(); 											
    			Collections.sort(featureCollectionVector, comparator);    			
    			
    			double costAllNew = 0.0;
    			if(featureCollectionVector.size() > 0) {
    				costAllNew = ((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(0)).getCost();
        			if(costAllCurrent - costAllNew > 0) {
        				partgeom = ((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(0)).getFeatureCollection();
        				//ProcessingTools.makeHistoryStep(partgeom, "Operation " + ((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(0)).getOperation()+ " - Evaluation after");
        				partgeom.makeConstraintHistoryStep("Operation " + ((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(0)).getOperation()+ " - Evaluation after");
        				DecimalFormat df = new DecimalFormat("#0.0000");				
        		   		System.out.println("Operation  >>> " + ((ConstrainedFeatureCollectionSorted)featureCollectionVector.get(0)).getOperation() + " <<<  successful (" + df.format(costAllNew) + ")");
            			
            			// Vergleich der Costen pro Operator für ähnliche und aktuelle Partion
            	    	Vector operationVectorAfter = ProcessingTools.getConstraintSpace().getOperationVectorFromFeatureCost(featureCostVec);
            	    	ProcessingTools.evaluatePrognose(operationVectorBefore, operationVectorAfter);
            			
            			// Correlation für Änderung der Constraints
            			/*if(iterations > 0) {
            				HashMap correlation = (new WebGen_Eval_Constraint_Correlation_Diff()).run(act_layers, params);
            				Matrix correlationMatrix = (Matrix)correlation.get("correlation");
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
    		
    		context.addLayer(StandardCategoryNames.WORKING, "result_part"+i, partgeom);
			// speicherung aller uebrigen gebaeude in der output collection result_all
			result_all.addAll(partgeom.getFeatures());
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
    	for(int i=0; i<ProcessingTools.getConstraintSpace().getNbrFeatures(); i++ ) {
    		Double[] featureCost = ProcessingTools.getConstraintSpace().getFeatureCostById(i);
    		System.out.print("\nFeatureConstraints ");	
    		for(int j=0; j<featureCost.length; j++) {
    			DecimalFormat df = new DecimalFormat(" #0.0000");				
        		System.out.print(" " + df.format(featureCost[j]));	    			
    		}
    		Double[] featureOperation = ProcessingTools.getConstraintSpace().getFeatureOperationById(i);
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
    	context.addLayer(StandardCategoryNames.WORKING, "result_all", result_all);
    	ProcessingTools.saveConstraintSpace();
    }
    
    public Double[] evalPartitions(ConstrainedFeatureCollection fc) {
    	WebGenRequest wgreq = new WebGenRequest();
    	wgreq.addParameters(this.globalParams);
    	wgreq.addParameter("geom", fc);
    	(new Eval_GMFeat_All()).run(wgreq);
    	Double[] partitionCost = (Double[])wgreq.getResult("severities");
    	for(int i=4; i<NbrConstraints; i++) partitionCost[i] *= 0.25;
    	return partitionCost;
    }

    public double getCostFromCostVector(Double[] costVec) {
    	double costAll = 0.0;
    	for(int i=0; i<costVec.length; i++) {
    		costAll += costVec[i].doubleValue();
    	}
    	return costAll;    
    }
    
    
    public Vector processPartition(ConstrainedFeatureCollection fc, ConstraintSpace trainedConstraintSpace, Double[] partionCost) {
		Vector sortedFeatureCollectionVector = new Vector();
		WebGenRequest twgreq = new WebGenRequest();
		twgreq.addParameters(this.globalParams);
    	// alle Operationen ausführen für die das Vergleichsfeature Kosten senken konnte 
		for(int testedOperations = 0; testedOperations < NbrOperations; testedOperations++) {
			twgreq.addParameter("geom", fc.clone());
			twgreq.addResult("result", null);
			int operation = ((ConstraintSpaceArrayItem)trainedConstraintSpace.getOperationVectorFromFeatureCost(partionCost).get(testedOperations)).getId();
			switch (operation) {
			case 0: 
				(new AreaScalingRelative()).run(twgreq);
				break;
			case 1:
				(new BuildingSimplifyGN()).run(twgreq);
				break;
			case 2:
				(new EnlargeToRectangle()).run(twgreq);
				break;
			case 3:
				(new DisplaceFeaturesFast()).run(twgreq); //ohne constraining roads
				break;
			case 4:
				int maxnumber10 = (int)Math.floor(fc.size()*0.9);	// Anzahl feature um 10% reduzieren
				twgreq.addParameter("maxnumber", new Integer(maxnumber10));
				(new BuildingTypification()).run(twgreq);
				break;
			case 5:
				int maxnumber30 = (int)Math.floor(fc.size()*0.7);	// Anzahl feature um 30% reduzieren
				twgreq.addParameter("maxnumber", new Integer(maxnumber30));
				(new BuildingTypification()).run(twgreq);
				break;
			}
			String operationName = ProcessingTools.lookupOperation(operation);            	
			LOGGER.info(operationName+" done");
			ConstrainedFeatureCollection fcNew = (ConstrainedFeatureCollection)twgreq.getResult("result");
			if(fcNew != null ) {
				Double[] costVec = evalPartitions(fcNew);
				double costAllCurrent = getCostFromCostVector(costVec);            		
				ConstrainedFeatureCollectionSorted sortedFeatureCollection = new ConstrainedFeatureCollectionSorted(costAllCurrent, fcNew, operationName);
				sortedFeatureCollectionVector.add(sortedFeatureCollection);		
			}   
		}
    	    	
    	return sortedFeatureCollectionVector;
    }
    
    class OperationComparator implements Comparator {
     	public int compare(Object o1, Object o2) {
     		ConstrainedFeatureCollectionSorted a = (ConstrainedFeatureCollectionSorted)o1;
     		ConstrainedFeatureCollectionSorted b = (ConstrainedFeatureCollectionSorted)o2;
     		return (a.compareTo(b));
     	}     	  
     }
}
