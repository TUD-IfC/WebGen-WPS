/*
 * Created on 13.01.2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author neun
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */

package ch.unizh.geo.webgen.service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ch.unizh.geo.measures.LineInflexionPoints;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDatasetFactory;

public class LineInflectionPoints extends AWebGenAlgorithm implements IWebGenAlgorithm {
	
	FeatureCollection myCollE;   
    FeatureCollection myCollB;
    FeatureCollection myCollC;
    
    public void run(WebGenRequest wgreq) {
		FeatureCollection features = wgreq.getFeatureCollection("geom");
		try {
			calcInflectionPoints(features);
			wgreq.addResult("full inflecionpoints", myCollE);
			wgreq.addResult("simple inflecionpoints", myCollB);
			wgreq.addResult("filtered inflecionpoints", myCollC);
		}
		catch (Exception e) {
			addErrorStack(e.getLocalizedMessage(), e.getStackTrace());
		}
	}
	
	private void calcInflectionPoints(FeatureCollection fcA) throws Exception{
		Iterator i = fcA.iterator();
		while(i.hasNext()) {
			calcInflectionPoints((Feature)i.next()); //= erste Geometrie
		}
	}
	
	private void calcInflectionPoints(Feature f) throws Exception{
		Geometry selGeom = f.getGeometry();
       	Geometry myHull = selGeom.convexHull();
       	Geometry myEnv = selGeom.getEnvelope();
       	List myList = new ArrayList();
       	myList.add(myHull);
       	myList.add(myEnv);
	    FeatureCollection myCollA = FeatureDatasetFactory.createFromGeometry(myList);
	    //context.addLayer(StandardCategoryNames.WORKING, "envelope and hull", myCollA);

	    List fullList = new ArrayList();
	    List smoothList = new ArrayList();
	    List simpleList = new ArrayList();
	    
	    if((selGeom instanceof LineString) || (selGeom instanceof Polygon)){	        
	        LineString mL = null;
	        Polygon mP = null;
	        //-- init InflectionPoints measure
		    LineInflexionPoints myInfPoints = null;
		    if(selGeom instanceof LineString){
		        mL = (LineString)selGeom;
		        myInfPoints = new LineInflexionPoints(mL);
		    }
		    if(selGeom instanceof Polygon){
		        mP = (Polygon)selGeom;
		        myInfPoints = new LineInflexionPoints(mP);
		    }
		    //-- number of points
		    int nrSimplePt = myInfPoints.getIndexListSimple().length;
		    int nrFilteredPt = myInfPoints.getIndexListwMean().length;
		    int nrFullPt = myInfPoints.getIndexListFull().length;
		    //-- number of points per area (area = area of buffer with min dim)
		    // minDim = 0.03cm : taken from SGK => for 1:25.000 : diameter=7.5m
		    double minDim = 7.50;
		    double area = 1;
		    Geometry bufferGeom = null;
		    if(selGeom instanceof LineString){
		        bufferGeom = selGeom.buffer(minDim/2.0);
		        area = bufferGeom.getArea();
		    }
		    if(selGeom instanceof Polygon){
		        mP = (Polygon)selGeom;
		        bufferGeom =mP.getExteriorRing().buffer(minDim/2.0);
		        area = bufferGeom.getArea();
		    }	
	       	List bufferList = new ArrayList();
		    bufferList.add(bufferGeom);
		    FeatureCollection myCollD = FeatureDatasetFactory.createFromGeometry(bufferList);
		    //context.addLayer(StandardCategoryNames.WORKING, "buffer", myCollD);
		    
		    // calculate ratio per km^2
		    double simpleRatio = nrSimplePt / (area/1000000.0);
		    double filteredRatio = nrFilteredPt / (area /1000000.0);
		    
		    //-- get points for plot
		    int[] fullindices = myInfPoints.getIndexListFull();
		    int[] simpleindices = myInfPoints.getIndexListSimple();
		    int[] smoothedindices = myInfPoints.getIndexListwMean();		    
		    int idx =0;
		    Point pt = null;
		    //--full version (inlcuding zickzack lines)
		    for (int j = 0; j < fullindices.length; j++) {
		        idx=fullindices[j];
		        if(selGeom instanceof LineString){
		            pt = mL.getPointN(idx);
		        }
		        if(selGeom instanceof Polygon){
		            pt = mP.getExteriorRing().getPointN(idx); 		            
		        }
		        fullList.add(pt.clone());		        		        
            }		    
		    //--filtered version
		    for (int j = 0; j < smoothedindices.length; j++) {
		        idx=smoothedindices[j];
		        if(selGeom instanceof LineString){
		            pt = mL.getPointN(idx);
		        }
		        if(selGeom instanceof Polygon){
		            pt = mP.getExteriorRing().getPointN(idx); 		            
		        }
		        smoothList.add(pt.clone());		        		        
            }
		    //--simple version
		    for (int j = 0; j < simpleindices.length; j++) {
		        idx=simpleindices[j];
		        if(selGeom instanceof LineString){
		            pt = mL.getPointN(idx);
		        }
		        if(selGeom instanceof Polygon){
		            pt = mP.getExteriorRing().getPointN(idx); 		            
		        }
		        simpleList.add(pt.clone());		        		        
            }
		    
		    if(myCollE == null) {
		    	//full Version Inflexionpoints
		    	myCollE = FeatureDatasetFactory.createFromGeometry(fullList);    
		    	//simple Version Inflexionpoints
		    	myCollB = FeatureDatasetFactory.createFromGeometry(simpleList);
		    	//filtered Version Inflexionpoints
		    	myCollC = FeatureDatasetFactory.createFromGeometry(smoothList);
		    }
		    else {
		    	myCollE.addAll(FeatureDatasetFactory.createFromGeometry(fullList).getFeatures());
		    	myCollB.addAll(FeatureDatasetFactory.createFromGeometry(simpleList).getFeatures());
		    	myCollC.addAll(FeatureDatasetFactory.createFromGeometry(smoothList).getFeatures());
		    }
		    
	    } //end if
	}
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("LineInflectionPoints", "sstein", "support",
				"",
				"LineInflectionPoints",
				"Line Inflection-Points",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"LineString"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		
		//add output parameters
		id.addOutputParameter("full inflecionpoints", "FeatureCollection");
		id.addOutputParameter("simple inflecionpoints", "FeatureCollection");
		id.addOutputParameter("filtered inflecionpoints", "FeatureCollection");
		return id;
	}
}