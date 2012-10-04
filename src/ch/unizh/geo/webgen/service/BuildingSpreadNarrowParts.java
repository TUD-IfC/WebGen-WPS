package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
import java.util.Iterator;

import ch.unizh.geo.algorithms.polygons.BuildingEnlargeWidthLocaly;
import ch.unizh.geo.constraints.buildings.BuildingLocalWidth;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;


public class BuildingSpreadNarrowParts extends AWebGenAlgorithm implements IWebGenAlgorithm  {

    //private FeatureCollection problematicEdges = null;
    private final String newAttributString="BuildingSpread";
    private double flexibility = 10.0; //percent
    private int iterMax=50;
    private boolean solveIter = true;
    
    public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		double minDist = wgreq.getParameterDouble("minDistanceInM");
		boolean fixVertices = wgreq.getParameterBoolean("shouldVerticesBeFixedAndOnlyEdgesMoved");
		try {
			FeatureCollection fcnew = changeWidth(fc, minDist, this.solveIter, fixVertices);
			wgreq.addResult("result", fcnew);
		}
		catch(Exception e) {e.printStackTrace();}
	}

	private FeatureCollection changeWidth(FeatureCollection features, double minDist, 
	                           boolean solveIterative, boolean fixVertex) throws Exception{
	    
	    System.gc(); //flush garbage collector
	    // --------------------------	    
	    int count=0; //int noItems = features.size(); Geometry resultgeom = null;
	    FeatureDataset resultFeatures = null;
	    ArrayList problematicEdges = new ArrayList();
       	FeatureSchema fs = new FeatureSchema();
	    //--get single object in selection to analyse
      	for (Iterator iter = features.iterator(); iter.hasNext();) {
      		count++;
      		Feature f = (Feature)iter.next();
      		System.out.println("========== Item featureID: " + f.getID() + " ==========");
      		//-- set schema by using the first selected item
      		//check if attribute name exists
      		boolean attributeExists = false;
      		if (count == 1){      			
      			//-- not sure to do that, since feature schemas of selected objects might be different 
      			fs = copyFeatureSchema(f.getSchema());
       			attributeExists = fs.hasAttribute(this.newAttributString);          			
       			if (attributeExists == false){	      		      			
	      			fs.addAttribute(this.newAttributString, AttributeType.STRING);
	      			//problematicFeatures = new FeatureDataset(fs);
       			}
      			resultFeatures = new FeatureDataset(fs);
      		}
      		//--create new Feature with one new attribute and copy attributvalues
      		Feature fnew = new BasicFeature(fs);
      		Object[] attribs = f.getAttributes();
      		if (attributeExists == false){
	      		Object[] attribsnew = new Object[attribs.length+1];
	      		for (int i = 0; i < attribs.length; i++) {
					attribsnew[i] = attribs[i];
				}
	      		attribsnew[attribs.length]= "init";
	      		fnew.setAttributes(attribsnew);
	      	}
      		else{
      			fnew.setAttributes(attribs);
      		}
      		//--
      		
	   		Geometry geom = f.getGeometry(); //= erste Geometrie
	       	Polygon poly = null;       	
	       	if ( geom instanceof Polygon){
	       		poly = (Polygon) geom; //= erste Geometrie
	    	    // --------------------------
	       		/*
	           	List resultList = new ArrayList();
	           	List conflictListA = new ArrayList();
	           	List conflictListB = new ArrayList();
	           	*/
	           	//---- detect conflicts
	           	BuildingLocalWidth plw = new BuildingLocalWidth(poly, 
	           	        	minDist,this.flexibility);
	           	//---
	           	if(plw.measure.hasConflicts() == true){
		           	//context.getWorkbenchFrame().setStatusMessage("conflicts detected!");           	
		           	//conflictListA.addAll(plw.measure.getDispVecPointEdgeLStringList());
		           	//conflictListB.addAll(plw.measure.getDispVecPointPointLStringList());
		           	//--- solve conflicts ---	           	
		           	if (solveIterative == false){
		           	    BuildingEnlargeWidthLocaly enlarge = new BuildingEnlargeWidthLocaly(poly,
		           	        							plw.measure.getMwclist(),
		           	        							fixVertex);
		           	    //resultList.add(enlarge.getOutPolygon());
		           	    fnew.setGeometry(enlarge.getOutPolygon());		           	    
		           	    plw = new BuildingLocalWidth(enlarge.getOutPolygon(), 
	           	        	minDist,this.flexibility);
		           	    if (plw.measure.hasConflicts() == true){
		           	    	fnew.setAttribute(this.newAttributString, "not solved");
		           	    	problematicEdges.addAll(plw.measure.getMinEdgeLineStringList());
		           	    	problematicEdges.addAll(plw.measure.getMinVertexPointList());
		           	    }
		           	    else{
		           	    	fnew.setAttribute(this.newAttributString, "enlarged");
		           	    }
		           	    
		           	    /**
		           	    transaction.setGeometry(count-1,enlarge.getOutPolygon());
		           	    **/
		           	}
		           	else{
		           	 //====================================   
		           	 // if solution should be done iterative
		           	 //====================================
		           	    BuildingEnlargeWidthLocaly enlarge = null;
		           	    int j = 0;
		           	    boolean tosolve = plw.measure.hasConflicts();
		           	    while(tosolve == true){
			           	    enlarge = new BuildingEnlargeWidthLocaly(poly,
	       							plw.measure.getMwclist(),
	       							fixVertex);		        
			           	    poly = enlarge.getOutPolygon();
			           	    plw = new BuildingLocalWidth(poly, 
			           	        	minDist,this.flexibility);
			           	    fnew.setAttribute(this.newAttributString, "enlarged");
			           	    tosolve = plw.measure.hasConflicts();
			           	    //--notbremse:
			           	    j = j + 1;
			           	    if(j == this.iterMax){
			           	        tosolve = false;
			           	        //context.getWorkbenchFrame().warnUser("stopped at step: " + j);
				           	    fnew.setAttribute(this.newAttributString, "not solved");
			           	    	problematicEdges.addAll(plw.measure.getMinEdgeLineStringList());
			           	    	problematicEdges.addAll(plw.measure.getMinVertexPointList());
			           	    }
			           	    
			           	    //-- visualisation
			           	    /*
			               	List stepList = new ArrayList();
			           	    stepList.add(0,enlarge.getOutPolygon());
				    	    FeatureCollection myCollD = FeatureDatasetFactory.createFromGeometry(stepList);
				    	    if (myCollD.size() > 0){
				    		    context.addLayer(StandardCategoryNames.WORKING, "stepList", myCollD);
				    		    }	    	  
				    	    */
		           	    }
		           	    //resultList.add(enlarge.getOutPolygon());
		           	    fnew.setGeometry(enlarge.getOutPolygon());
		           	    /**
		           	    transaction.setGeometry(count-1,enlarge.getOutPolygon());
		           	    **/
		           	}
		           	//resultList.addAll(enlarge.getIntersectionPoints());
		           	// ===== visulisation =====
		           	/*
		    	    FeatureCollection myCollA = FeatureDatasetFactory.createFromGeometry(conflictListA);
		    	    if (myCollA.size() > 0){
		    		    context.addLayer(StandardCategoryNames.WORKING, "point-edge conflicts", myCollA);
		    		    }	           	
	           		FeatureCollection myCollC = FeatureDatasetFactory.createFromGeometry(conflictListB);
	           		if (myCollC.size() > 0){
	           		    context.addLayer(StandardCategoryNames.WORKING, "point-point conflicts", myCollC);
	    		    }           		           	           	
		    	    FeatureCollection myCollB = FeatureDatasetFactory.createFromGeometry(resultList);
		    	    if (myCollB.size() > 0){
		    		    context.addLayer(StandardCategoryNames.WORKING, "result", myCollB);
		    		    }
		    		*/
		        }// ========================       		
	           	else{
	           	    //context.getWorkbenchFrame().setStatusMessage("no conflict detected!");
	           	    fnew.setAttribute(this.newAttributString, "no conflict");
	           	}
	       	}
	       	else{
	       	    //context.getWorkbenchFrame().warnUser("no polygon selected");
	       	    fnew.setAttribute(this.newAttributString, "no polygon");
	       	}
       	    resultFeatures.add(fnew);
		    //String mytext = "item: " + count + " / " + noItems + " : squaring finalized";
		    //monitor.report(mytext);	       	
      	}//  end loop for selection
      	/**
       	transaction.commit();
       	**/
	    /*if (problematicEdges.size() > 0){
		    FeatureCollection myCollE = FeatureDatasetFactory.createFromGeometry(problematicEdges);
		    //context.addLayer(StandardCategoryNames.WORKING, "problematic edges", myCollE);
		    this.problematicEdges = myCollE; 
		    }*/

        return resultFeatures;        
	}

	private FeatureSchema copyFeatureSchema(FeatureSchema oldSchema){
		FeatureSchema fs = new FeatureSchema();
		for (int i = 0; i < oldSchema.getAttributeCount(); i++) {
			AttributeType at = oldSchema.getAttributeType(i);
			String aname = oldSchema.getAttributeName(i);
			fs.addAttribute(aname,at);
			fs.setCoordinateSystem(oldSchema.getCoordinateSystem());
		}		
		return fs;
	}

	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("BuildingSpreadNarrowParts", "sstein", "operator",
				"",
				"BuildingSpreadNarrowParts",
				"spread narrow building parts",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with buildings");
		id.addInputParameter("minDistanceInM", "DOUBLE", "10.0", "buffer width");
		id.addInputParameter("shouldVerticesBeFixedAndOnlyEdgesMoved", "BOOLEAN", "false", "should Vertices Be Fixed And Only Edges Moved");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
  
}
