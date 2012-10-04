package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jmat.data.Matrix;

import ch.unizh.geo.algorithms.structures.classification.ClassificationModelParams;
import ch.unizh.geo.algorithms.structures.classification.UrbanClassification;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.registry.ParameterDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

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
public class BuildingClassificationStatefulLearningBP extends AWebGenAlgorithm implements IWebGenAlgorithm  {

	private static HashMap matrices = new HashMap();
	
	public void run(WebGenRequest wgreq) {
	    String action = wgreq.getParameter("action").toString();
	    String matrixID = wgreq.getParameter("matrixID").toString();
	    if(action.equals("trainmodel")) trainModel(wgreq, matrixID);
	    else if(action.equals("getmodel")) getModel(wgreq, matrixID);
	}
	
	private void getModel(WebGenRequest wgreq, String matrixID) {
		ClassificationModelParams model = (ClassificationModelParams)matrices.get(matrixID);
		if(model != null) wgreq.addResult("model", model);
	}
	
	private void trainModel(WebGenRequest wgreq, String matrixID) {
		//-- get input params
	    FeatureCollection featuresA = wgreq.getFeatureCollection("rural buildings");
	    FeatureCollection featuresB = wgreq.getFeatureCollection("industrial and commercial buildings");
	    FeatureCollection featuresC = wgreq.getFeatureCollection("inner city buildings");
	    FeatureCollection featuresD = wgreq.getFeatureCollection("urban buildings");
	    FeatureCollection featuresE = wgreq.getFeatureCollection("suburban buildings");

	    int algoType = 2; //use BatchPerceptron
    	boolean acceptAllMasures = wgreq.getParameterBoolean("accept all measures");
    	double radius = wgreq.getParameterDouble("buffer radius in m");
    	boolean applySpatialMedian = wgreq.getParameterBoolean("apply spatial median");
    	    	
    	//-- prepare input params
	    ArrayList<List> classBuildings = new ArrayList<List>();
	    //final Collection features = featuresA.getFeatures();
	    classBuildings.add(featuresA.getFeatures());
	    classBuildings.add(featuresB.getFeatures());
	    classBuildings.add(featuresC.getFeatures());
	    classBuildings.add(featuresD.getFeatures());
	    classBuildings.add(featuresE.getFeatures());

	    //final Collection nnBdgs = featuresNN.getFeatures();
	    
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
	    
	    ClassificationModelParams model = classify.trainModel(classBuildings, algoType, measureSet, radius, applySpatialMedian, null); 	
    	this.matrices.put(matrixID, model);
	    //-- output
		if(model != null) wgreq.addResult("model", model);
	}
	
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("BuildingClassificationStatefulLearningBP", "sstein", "support",
				"",
				"BuildingClassificationStatefulLearningBP",
				"Classify buildings into rural, suburban, urban ...",
				"1.0");
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("rural buildings", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "buildings");
		id.addInputParameter("industrial and commercial buildings", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "buildings");
		id.addInputParameter("inner city buildings", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "buildings");
		id.addInputParameter("urban buildings", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "buildings");
		id.addInputParameter("suburban buildings", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "buildings");

		id.addInputParameter("accept all measures", "BOOLEAN", "true", "accept all measures");
		id.addInputParameter("buffer radius in m", "DOUBLE", "200.0", "buffer radius in m");
		id.addInputParameter("apply spatial median", "BOOLEAN", "false", "apply spatial median");
		
		id.addInputParameter("matrixID", "STRING", "m1", "id of stateful matrix");
		
		ParameterDescription actionparam = new ParameterDescription("action", "STRING", "trainmodel", "");
		actionparam.addSupportedValue("trainmodel");
		actionparam.addSupportedValue("getmodel");
		actionparam.setChoiced();
		id.addInputParameter(actionparam);
		
		//add output parameters
		id.addOutputParameter("model", "ClassificationModelParams");
		return id;
	}
			
}
