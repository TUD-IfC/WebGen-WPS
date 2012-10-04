package ch.unizh.geo.webgen.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.service.AreaPartitioning;
import ch.unizh.geo.webgen.test.parallel.PartitionThread;
import ch.unizh.geo.webgen.tools.ProcessingTools;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;

public class ProcessingServiceParallel extends AbstractPlugIn implements ThreadedPlugIn {

	private static Logger LOGGER = Logger.getLogger(ProcessingServiceParallel.class);
	
	int NbrOperations = ProcessingTools.NbrOperations;
	int NbrConstraints = ProcessingTools.NbrConstraints;
	
	private MultiInputDialog dialog;
	static HashMap<String,Object> globalParameters = new HashMap<String,Object>();
	
	StringBuffer prognoseOut;
	
	public ProcessingServiceParallel() {
    }

    public void initialize(PlugInContext context) throws Exception {
    	context.getFeatureInstaller().addMainMenuItem(
                this, "GenProcess", "WG06 Processing Service Parallel",
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
    	
    	globalParameters.put("minarea", dialog.getDouble("minarea"));
    	globalParameters.put("minlength", dialog.getDouble("minlength"));
    	globalParameters.put("mindist", dialog.getDouble("mindist"));
    	globalParameters.put("roaddist", dialog.getDouble("roaddist"));
    	globalParameters.put("toosmall", dialog.getDouble("minarea")/2); //remove buildings which have less than half the minsize
    	
    	//HashMap layers = new HashMap();
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
    	
    	//moritz GenOL zwei Constraint arrays fŸr vorher und nachher
		HashMap<String, Vector<Double[]>> genolopstat = new HashMap<String, Vector<Double[]>>();
    	
		//Schleife über Partionen    	
    	int partition_cnt = (int)fc_partitions.size()/2;
    	Thread[] partThreads = new Thread[partition_cnt];
    	Integer[] iterationNr = new Integer[partition_cnt];
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
    		ConstrainedFeatureCollection partgeom = (ConstrainedFeatureCollection) partition_value;
    		ConstrainedFeatureCollection partcongeom = (ConstrainedFeatureCollection) partpoly_value;
    		
    		//start partition threads
    		partThreads[i] = new PartitionThread(globalParameters, partgeom, partcongeom,
    						result_all, context,
    						iterationNr, i, genolopstat);
    	}// Ende for(int i=0; i<partition_cnt; i++)

    	for(int i=0; i<partition_cnt; i++) {
    		partThreads[i].start();
    	}
    	
    	for(int i=0; i<partition_cnt; i++) {
    		try {partThreads[i].join();} catch (InterruptedException e) {}
    	}
    	
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
    	ProcessingTools.saveConstraintSpace();
    	FileWriter fw = new FileWriter("X:\\neun\\test.txt");
    	//C:\Dokumente und Einstellungen\burg\Eigene Dateien\withSave\Experimente\Steuerung\test.txt
    	BufferedWriter bfw = new BufferedWriter(fw);
    	bfw.write(prognoseOut.toString());
    	bfw.close();
    }
}
