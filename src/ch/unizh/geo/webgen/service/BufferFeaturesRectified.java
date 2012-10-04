package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.axpand.jaxpand.genoperator.common.GNBufferBuilder;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;

public class BufferFeaturesRectified extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		double width = wgreq.getParameterDouble("width");
		FeatureCollection fcnew = runBuffer(fc, width);
		wgreq.addResult("result", fcnew);
	}

	private FeatureCollection runBuffer(FeatureCollection fcA, double width) {
		GNBufferBuilder gnbuff = new GNBufferBuilder();
		for (Iterator ia = fcA.iterator(); ia.hasNext();) {
			Feature fa = (Feature) ia.next();
			Geometry ga = fa.getGeometry();
			Geometry result = gnbuff.buffer(ga, width);
			result.getEnvelopeInternal();
			if (result != null) fa.setGeometry(result);
		}
		fcA.getEnvelope();
		return fcA;
	}

	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("BufferFeaturesRectified", "neun", "support",
				"",
				"BufferFeaturesRectified",
				"Buffer function for Points, Lines and Polygons. Creates rectified angles.",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("width", "DOUBLE", "10.0", "buffer width");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
