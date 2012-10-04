package ch.unizh.geo.webgen.service;

import java.util.HashMap;
import java.util.Iterator;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.task.TaskMonitor;

/*
 * Created on 11.01.2005
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
public class JTSDouglasPeucker extends AWebGenAlgorithm implements IWebGenAlgorithm  {
	
	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		double tolerance = wgreq.getParameterDouble("tolerance");
		FeatureCollection fcnew = jtsdp(fc, tolerance);
		wgreq.addResult("result", fcnew);
	}    
    
    private FeatureCollection jtsdp(FeatureCollection fc, double tolerance) {
        GeometryFactory geof = new GeometryFactory();
        FeatureCollection fcnew = new FeatureDataset(fc.getFeatureSchema());
        
        Geometry currDP = null;
        int size = fc.size();
        int count = 1;

        for (Iterator i = fc.iterator(); i.hasNext();) {
            Feature f = (Feature)((Feature)i.next()).clone();
            currDP = DouglasPeuckerSimplifier.simplify(f.getGeometry(), tolerance);
            f.setGeometry(currDP);
            fcnew.add(f);
        }
        return fcnew;
    }
    

    
    public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("JTSDouglasPeucker", "neun", "support",
				"",
				"JTSDouglasPeucker",
				"Douglas-Peucker algorithm from JTS for Lines and Polygons",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries to buffer");
		id.addInputParameter("tolerance", "DOUBLE", "20.0", "dp tolerance");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
