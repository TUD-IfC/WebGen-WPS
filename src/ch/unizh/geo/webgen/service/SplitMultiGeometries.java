package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.webgen.model.ConstrainedFeature;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.registry.ParameterDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.buffer.BufferOp;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureUtil;

public class SplitMultiGeometries extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		FeatureCollection fcnew = split(fc);
		wgreq.addResult("result", fcnew);
	}

	private FeatureCollection split(FeatureCollection fc) {
		FeatureCollection fcnew = new FeatureDataset(fc.getFeatureSchema());
		for (Iterator ia = fc.iterator(); ia.hasNext();) {
			Feature fa = (Feature) ia.next();
			Geometry ga = fa.getGeometry();
			if(ga instanceof MultiPolygon) {
				for(int i=0; i < ((MultiPolygon)ga).getNumGeometries(); i++) {
	        		Geometry tgeom = ((MultiPolygon)ga).getGeometryN(i);
	        		Feature fn = fa.clone(true);
	        		fn.setGeometry(tgeom);
	        		fcnew.add(fn);
	        	}
			}
			else {
				fcnew.add(fa);
			}
		}
		return fcnew;
	}

	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("SplitMultiGeometries", "neun", "support",
				"",
				"SplitMultiGeometries",
				"Split MultiGeometries",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"MultiPoint","MultiLineString","MultiPolygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");

		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
