/*
 * Created on 19.08.2005
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

import java.util.Iterator;
import java.util.List;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;

public class MergeFeaturesByAttribute extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		try {
			FeatureCollection fc = wgreq.getFeatureCollection("geom");
			String classfield = wgreq.getParameter("classfield").toString();
			FeatureCollection fcnew = reclass(fc, classfield);
			wgreq.addResult("result", fcnew);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private FeatureCollection reclass(FeatureCollection fc, String classfield) throws Exception {
		Quadtree qtree = new Quadtree();
		for (Iterator is = fc.iterator(); is.hasNext();) {
			Feature fs = (Feature) is.next();
			Geometry gs = fs.getGeometry();
			qtree.insert(gs.getEnvelope().getEnvelopeInternal(),fs);
		}
		
		int fcsize = fc.size();
		int i = 1;
		for (Iterator is = fc.iterator(); is.hasNext();) {
			System.out.println("processing feature " + i + " of " + fcsize);
			Feature fs = (Feature) is.next();
			String as = fs.getAttribute(classfield).toString().trim();
			Geometry gs = fs.getGeometry();
			Envelope genv = gs.getEnvelope().getEnvelopeInternal();
			if(qtree.remove(genv, fs)) {
				List instreet = qtree.query(genv);
				for(Iterator iin = instreet.iterator(); iin.hasNext();) {
					Feature fiin = (Feature) iin.next();
					Geometry giin = fiin.getGeometry();
					if(as.equals(fiin.getAttribute(classfield).toString().trim()) && gs.touches(giin)) {
						gs = gs.union(giin);
						qtree.remove(fiin.getGeometry().getEnvelope().getEnvelopeInternal(), fiin);
					}
				}
				fs.setGeometry(gs);
				qtree.insert(gs.getEnvelope().getEnvelopeInternal(),fs);
			}
			i++;
		}
		
		FeatureCollection fcnew = new FeatureDataset(fc.getFeatureSchema());
		fcnew.addAll(qtree.queryAll());
		return fcnew;
    }
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("MergeFeaturesByAttribute", "neun", "support",
				"",
				"MergeFeaturesByAttribute",
				"MergeFeaturesByAttribute",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries and classes");
		id.addInputParameter("classfield", "STRING", "OBJECTVAL", "classfield");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}