package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
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

public class InflectionPoints extends AWebGenAlgorithm implements IWebGenAlgorithm  {
    
	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		
		List myList = new ArrayList();
		List bufferList = new ArrayList();
		List fullList = new ArrayList();
	    List smoothList = new ArrayList();
	    List simpleList = new ArrayList();
		
		for(Iterator fciter = fc.iterator(); fciter.hasNext();) {
			Feature f = (Feature)fciter.next();
	       	Geometry selGeom = f.getGeometry();
	       	Geometry myHull = selGeom.convexHull();
	       	Geometry myEnv = selGeom.getEnvelope();
	       	myList.add(myHull);
	       	myList.add(myEnv);

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
		       	
			    bufferList.add(bufferGeom);
			    
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
		    }
		}
		FeatureCollection myCollA = FeatureDatasetFactory.createFromGeometry(myList);
	    if(myCollA != null) wgreq.addResult("envelope and hull", myCollA);
	    FeatureCollection myCollD = FeatureDatasetFactory.createFromGeometry(bufferList);
	    if(myCollD != null) wgreq.addResult("buffer", myCollD);
		FeatureCollection myCollE = FeatureDatasetFactory.createFromGeometry(fullList);
	    if(myCollE != null) wgreq.addResult("full Version Inflexionpoints", myCollE);
	    FeatureCollection myCollB = FeatureDatasetFactory.createFromGeometry(simpleList);
	    if(myCollB != null) wgreq.addResult("simple Version Inflexionpoints", myCollB);
	    FeatureCollection myCollC = FeatureDatasetFactory.createFromGeometry(smoothList);
	    if(myCollC != null) wgreq.addResult("filtered Version Inflexionpoints", myCollC);
	}
	
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("InflectionPoints", "neun", "support",
				"",
				"InflectionPoints",
				"InflectionPoints",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
