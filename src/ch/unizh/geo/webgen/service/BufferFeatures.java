package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.registry.ParameterDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.buffer.BufferOp;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;

public class BufferFeatures extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		double width = wgreq.getParameterDouble("width");
		String capstyle;
		try {capstyle = wgreq.getParameter("capstyle").toString();}
		catch (Exception e) {capstyle = "CAP_ROUND";}
		int quadrants;
		try {quadrants = wgreq.getParameterInt("quadrants");}
		catch (Exception e) {quadrants = 8;}
		FeatureCollection fcnew = runBuffer(fc, width, capstyle, quadrants);
		wgreq.addResult("result", fcnew);
	}

	private FeatureCollection runBuffer(FeatureCollection fcA, double width, String capstyle, int quadrants) {
		int capstyleint;
		if(capstyle.equals("CAP_BUTT")) capstyleint = BufferOp.CAP_BUTT;
		else if(capstyle.equals("CAP_SQUARE")) capstyleint = BufferOp.CAP_SQUARE;
		else capstyleint = BufferOp.CAP_ROUND;
		for (Iterator ia = fcA.iterator(); ia.hasNext();) {
			Feature fa = (Feature) ia.next();
			Geometry ga = fa.getGeometry();
			BufferOp bufOp = new BufferOp(ga);
			bufOp.setEndCapStyle(capstyleint);
			bufOp.setQuadrantSegments(quadrants);
			Geometry result = bufOp.getResultGeometry(width);
			result.getEnvelopeInternal();
			if (result != null) fa.setGeometry(result);
		}
		fcA.getEnvelope();
		return fcA;
	}

	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription interfaceDescription = new InterfaceDescription("BufferFeatures", "neun", "support",
				"",
				"BufferFeatures",
				"Buffer function for Points, Lines and Polygons",
				"1.0",
				new String[] {"ica.genops.cartogen.Enhancement"});
		interfaceDescription.visible = true;
		
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		interfaceDescription.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		interfaceDescription.addInputParameter("width", "DOUBLE", "10.0", "buffer width");
		
		ParameterDescription capstyleparam = new ParameterDescription("capstyle", "STRING", "CAP_ROUND", "cap style of the buffer ends");
		capstyleparam.addSupportedValue("CAP_ROUND");
		capstyleparam.addSupportedValue("CAP_BUTT");
		capstyleparam.addSupportedValue("CAP_SQUARE");
		capstyleparam.setChoiced();
		interfaceDescription.addInputParameter(capstyleparam);
		
		interfaceDescription.addInputParameter("quadrants", "INTEGER", "8", "quadrant segments for rounding of circles");
		
		//add output parameters
		interfaceDescription.addOutputParameter("result", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "buffered geometries");
		return interfaceDescription;
	}
}
