package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.tools.GeometryHelper;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;


public class AreaScalingRelative extends AWebGenAlgorithm implements IWebGenAlgorithm  {
		
	public void run(WebGenRequest wgreq) {
		try {
			FeatureCollection fc = wgreq.getFeatureCollection("geom");
			double minSize = wgreq.getParameterDouble("minarea");
			FeatureCollection fcnew = areaScale(fc, minSize);
			wgreq.addResult("result", fcnew);
		}
		catch (Exception e) {}
	}
	
	private FeatureCollection areaScale(FeatureCollection features, double minSize) throws Exception{
	    // Minimale und maximale Fläche bestimmen
	    double minArea = minSize;
	    double maxArea = 0.0;
      	for (Iterator iter = features.iterator(); iter.hasNext();) {
      		Feature f = (Feature)iter.next();
	   		Geometry geom = f.getGeometry(); //= erste Geometrie
	   		Polygon poly = null;
	       	if ( geom instanceof Polygon){
	       		poly = (Polygon) geom; //= erste Geometrie
	       		double currentArea = poly.getArea(); 
	       		if(currentArea < minArea) {
	       			minArea = currentArea; 
	       		}
	       		if(currentArea > maxArea) {
	       			maxArea = currentArea; 
	       		}	       		
	       	}
      	}

      	// Abbruch, wenn kleinstes Feature (minArea) schon Mindestgrösse (minSize) hat
      	boolean doscale = true;
      	if(minSize - minArea < 0.001 )  {
      		return features;
      		//doscale = false;
      	}
	    
	    int count=0;
	    //--get single object in selection to analyse
      	for (Iterator iter = features.iterator(); iter.hasNext();) {
      		count++;
      		Feature f = (Feature)iter.next();
      		//Feature fnew = new BasicFeature(features.getFeatureSchema());
      		//FeatureUtil.copyAttributes(f, fnew);
      		if(doscale) {
      			//Geometry geom = fnew.getGeometry(); //= erste Geometrie
      			Geometry geom = f.getGeometry(); //= erste Geometrie
	   			Polygon poly = null;
	       		if ( geom instanceof Polygon){
	       			poly = (Polygon) geom; //= erste Geometrie
	       			double currentArea = poly.getArea(); 
	       			double targetSize = currentArea + (minSize-minArea)*(maxArea-currentArea)/(maxArea-minArea);
	       			poly = (Polygon)GeometryHelper.skaleSize(poly, targetSize);
					//fnew.setGeometry(poly);
					f.setGeometry(poly);
	       		}
      		}
      	}// end loop over item selection
      	return features;
	}
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("AreaScalingRelative", "burg", "operator",
				"",
				"AreaScalingRelative",
				"enlarges all buildings in a layer relative to their original size",
				"1.0",
				new String[] {"ica.genops.cartogen.Enhancement"});
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "building polygons");
		id.addInputParameter("minarea", "DOUBLE", "200.0", "building minimum size");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "scaled building polygons");
		return id;
	}

}
