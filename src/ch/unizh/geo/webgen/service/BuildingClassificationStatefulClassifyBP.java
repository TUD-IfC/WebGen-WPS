package ch.unizh.geo.webgen.service;

import java.util.Collection;
import java.util.HashMap;

import ch.unizh.geo.algorithms.structures.classification.ClassificationModelParams;
import ch.unizh.geo.algorithms.structures.classification.UrbanClassification;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.server.WebGenRequestExecuter;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;

/**
 * @descrption:
 * 		smoothes lines and polygons with a snakes algorithm 
 * 		needed parameters: 
 * 			FeatureCollection => Feature => Geometry  : LineString or Polygon  
 * 			tolerance : maximum displacement
 * 			segmentate : should the line be segmentated
 * 		further params initilized in this file:
 * 			start params Snakes: alpha und beta (init = 1)
 * 			segmentation value: curvature (init = Pi/3)
 * 			
 * @author sstein
 *
 * 
 */
public class BuildingClassificationStatefulClassifyBP extends AWebGenAlgorithm implements IWebGenAlgorithm  {
	
	public void run(WebGenRequest wgreq) {
	    String matrixID = wgreq.getParameter("matrixID").toString();
	    classify(wgreq, matrixID);
	}
	
	
	private void classify(WebGenRequest wgreq, String matrixID) {
		//-- get input params
		ClassificationModelParams model = getModel(matrixID);
		if(model == null) return;
	    FeatureCollection featuresNN = wgreq.getFeatureCollection("buildings to classify");
    	
    	int algoType = 2;
    	boolean acceptAllMasures = wgreq.getParameterBoolean("accept all measures");
    	double radius = wgreq.getParameterDouble("buffer radius in m");
    	boolean applySpatialMedian = wgreq.getParameterBoolean("apply spatial median");
    	    	
    	//-- prepare input params
	    final Collection nnBdgs = featuresNN.getFeatures();
	    
	    boolean[] measureSet = new boolean[12];
	    if (acceptAllMasures == true){
	        //-- first two entries are x,y coords
	        measureSet[0] = false; measureSet[1] = false; //dont use the coordinates
	        measureSet[2] = true; measureSet[3] = true;
	        measureSet[4] = true; measureSet[5] = true;
	        measureSet[6] = true; measureSet[7] = true;
	        measureSet[8] = true; measureSet[9] = true;
	        measureSet[10] = true; measureSet[11] = true;	        
	    }
	    else{
	        System.out.println("use only two buffer indices");
	        //-- first two entries are x,y coords
	        measureSet[0] = false; measureSet[1] = false; //dont use the coordinates
	        measureSet[2] = false; measureSet[3] = false;
	        measureSet[4] = false; measureSet[5] = false;
	        measureSet[6] = false; measureSet[7] = false;
	        measureSet[8] = true; measureSet[9] = false;
	        measureSet[10] = true; measureSet[11] = false;	        	        
	    }
	    
	    //-- prepare output
	    FeatureSchema fs = new FeatureSchema();
	    fs.addAttribute("Geometry", AttributeType.GEOMETRY);	    
	    FeatureCollection fcnew = new FeatureDataset(fs);
		//-- calculate
				
	    UrbanClassification classify = new UrbanClassification();
	    boolean success = classify.classifyByModel(model, nnBdgs, 
	            algoType, measureSet, radius, applySpatialMedian, null);
	    
	    if(success){
	        fcnew = classify.getClassifiedBuildings();
	        String msg = "kappa: " + classify.getKappa();
	        this.addMessage(msg);
	    }
	    else{
	        this.addMessage(classify.getMessage());
	    }       		    	
    	//-- output
		wgreq.addResult("result", fcnew);
	}
	
	
	private ClassificationModelParams getModel(String matrixID) {
		HashMap<String, Object> tparams = new HashMap<String, Object>();
		tparams.put("matrixID", matrixID);
		tparams.put("action", "getmodel");
		WebGenRequest twgreq = WebGenRequestExecuter.callService(tparams, "localcloned", "BuildingClassificationStatefulLearningBP");
		return (ClassificationModelParams)twgreq.getResult("model");
	}
	
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("BuildingClassificationStatefulClassifyBP", "sstein", "support",
				"",
				"BuildingClassificationStatefulClassifyBP",
				"Classify buildings into rural, suburban, urban ...",
				"1.0");
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("buildings to classify", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "buildings");
		
		id.addInputParameter("accept all measures", "BOOLEAN", "true", "accept all measures");
		id.addInputParameter("buffer radius in m", "DOUBLE", "200.0", "buffer radius in m");
		id.addInputParameter("apply spatial median", "BOOLEAN", "false", "apply spatial median");
		
		id.addInputParameter("matrixID", "STRING", "m1", "id of stateful matrix");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
			
}
