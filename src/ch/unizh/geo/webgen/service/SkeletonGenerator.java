package ch.unizh.geo.webgen.service;

import geom.skeleton.Skeleton;

import java.util.HashMap;
import java.util.Iterator;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;

/*
 * Created on 01.06.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author neun
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SkeletonGenerator extends AWebGenAlgorithm implements IWebGenAlgorithm  {
	
	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		double tolerance = wgreq.getParameterDouble("tolerance");
		boolean inner = wgreq.getParameterBoolean("innerskel");
		try {
			//FeatureCollection fcnew = skeletonize(fc, tolerance, inner);
			//wgreq.addResult("result", fcnew);
		}
		catch(Exception e) {}
		
	}
	
	public FeatureCollection skeletonize(FeatureCollection fc, double tolerance, boolean inner) throws Exception {
		FeatureCollection fcnew = new FeatureDataset(fc.getFeatureSchema());
		for (Iterator i = fc.iterator(); i.hasNext();) {
            Feature f = (Feature)((Feature)i.next()).clone();
            
            Skeleton currentSkeleton = new Skeleton();
    		currentSkeleton = currentSkeleton.calculateSkeleton((Polygon)f.getGeometry(), 0.0);
    		
    		Geometry newgeo;
    		if(inner) newgeo = currentSkeleton.getSkeletonEdgesAsMultiLineString();
    		else newgeo = currentSkeleton.getInnerSkeletonEdgesAsMultiLineString();
            
            f.setGeometry(newgeo);
            fcnew.add(f);
        }
		return fcnew;
	}
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("SkeletonGenerator", "petzold", "support",
				"",
				"SkeletonGenerator",
				"Skeleton Generator for Polygons",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("tolerance", "DOUBLE", "10.0", "tolerance");
		id.addInputParameter("innerskel", "BOOLEAN", "false", "deliver only inner-skeletion");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
