package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.tools.AngleFunctions;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;

public class ExpandAreas extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		double width = wgreq.getParameterDouble("width");
		FeatureCollection fcnew = runScaling(fc, width);
		wgreq.addResult("result", fcnew);
	}

	private FeatureCollection runScaling(FeatureCollection fcA, double width) {
		for (Iterator ia = fcA.iterator(); ia.hasNext();) {
			Feature fa = (Feature) ia.next();
			Geometry result = scalePolygon((Polygon)fa.getGeometry(), width);
			result.getEnvelopeInternal();
			if (result != null) fa.setGeometry(result);
		}
		fcA.getEnvelope();
		return fcA;
	}
	
	private Geometry scalePolygon(Polygon p, double width) {
		Coordinate[] coords = p.getCoordinates();
		Coordinate[] ncoords = new Coordinate[coords.length];
		double alpha, a1, a2, aext, abase;
		double r, dx, dy;
		double abaser, alphadiff;
		int il;
		// es wird immer am punkt i-1 gearbeitet
		for(int i=1; i<coords.length; i++) {
			il = i-2;
			if(i==1) il = coords.length-2;
			
			a1 = AngleFunctions.angle(coords[i-1], coords[il]);
			a2 = AngleFunctions.angle(coords[i-1], coords[i]);
			aext = a2 - a1;
			if(aext<0) aext += 2*Math.PI;
			aext = 2*Math.PI - aext;
			
			abase = AngleFunctions.angle(coords[i-1], coords[il]);
			
			//if(abase >= 0) alpha = abase + aext/2;
			//else alpha = abase - aext/2;
			alpha = abase - aext/2;
			if(alpha > Math.PI) alpha = alpha - 2*Math.PI;
			if(alpha < -Math.PI) alpha = alpha + 2*Math.PI;
			
		    //System.out.println("abase="+AngleFunctions.toDegrees(abase) + " --> aext="+AngleFunctions.toDegrees(aext) + " --> alpha=" + AngleFunctions.toDegrees(alpha) +  " ("+coords[il]+coords[i-1]+")");
		    
		    abaser = abase - Math.PI/2;
		    if(abaser < -Math.PI) abaser = abaser + 2*Math.PI;
		    alphadiff = Math.abs(alpha - abaser);
		    if(alphadiff > Math.PI) alphadiff = 2*Math.PI - alphadiff;
		    //System.out.println("abaser="+AngleFunctions.toDegrees(abaser));
		    //System.out.println("alphadiff="+AngleFunctions.toDegrees(alphadiff));
		    
		    r = width / Math.cos(alphadiff);
		    
		    dx = r * Math.cos(alpha);
		    dy = r * Math.sin(alpha);
		    ncoords[i-1] = new Coordinate(coords[i-1].x + dx, coords[i-1].y + dy);
		    //System.out.println("r="+r+" --> dx/dy " + dx + "/" + dy);
		}
		ncoords[coords.length-1] = new Coordinate(ncoords[0]);
		GeometryFactory geomfact = new GeometryFactory();
		Polygon pn =  geomfact.createPolygon(geomfact.createLinearRing(ncoords), null);
		return pn.buffer(0);
	}

	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("ExpandAreas", "neun", "support",
				"",
				"ExpandAreas",
				"Expand Areas",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("width", "DOUBLE", "10.0", "buffer width");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
