package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
public class BuildingClassification extends AWebGenAlgorithm implements IWebGenAlgorithm  {

	public void run(WebGenRequest wgreq) {
	    //-- get input params
	    FeatureCollection featuresA = wgreq.getFeatureCollection("rural buildings");
	    FeatureCollection featuresB = wgreq.getFeatureCollection("industrial and commercial buildings");
	    FeatureCollection featuresC = wgreq.getFeatureCollection("inner city buildings");
	    FeatureCollection featuresD = wgreq.getFeatureCollection("urban buildings");
	    FeatureCollection featuresE = wgreq.getFeatureCollection("suburban buildings");
	    FeatureCollection featuresNN = wgreq.getFeatureCollection("buildings to classify");
    	
    	String algoTypeS = wgreq.getParameter("algorithm type").toString();
    	int algoType;
    	if(algoTypeS.equals("PLSM")) algoType = 1;
    	else algoType = 2;
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
	    boolean success = classify.classifyUrbanStructures(classBuildings, nnBdgs, 
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
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("BuildingClassification", "sstein", "support",
				"",
				"BuildingClassification",
				"Classify buildings into rural, suburban, urban ...",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("rural buildings", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "buildings");
		id.addInputParameter("industrial and commercial buildings", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "buildings");
		id.addInputParameter("inner city buildings", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "buildings");
		id.addInputParameter("urban buildings", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "buildings");
		id.addInputParameter("suburban buildings", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "buildings");
		id.addInputParameter("buildings to classify", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "buildings");
		
		ParameterDescription algoTypeParam = new ParameterDescription("algorithm type", "STRING", "BatchPerceptron", "algorithm type 1:PseudoinversLMS, 2:BatchPerceptron");
		algoTypeParam.addSupportedValue("PseudoinversLMS");
		algoTypeParam.addSupportedValue("BatchPerceptron");
		algoTypeParam.setChoiced();
		id.addInputParameter(algoTypeParam);
		id.addInputParameter("accept all measures", "BOOLEAN", "true", "accept all measures");
		id.addInputParameter("buffer radius in m", "DOUBLE", "200.0", "buffer radius in m");
		id.addInputParameter("apply spatial median", "BOOLEAN", "false", "apply spatial median");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
			
}
