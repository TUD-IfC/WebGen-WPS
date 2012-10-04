package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.axpand.jaxpand.genoperator.area.GenAreaSimpleDouglasPeucker;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;

public class JGenAreaSimpleDouglasPeucker extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		double tolerance = wgreq.getParameterDouble("tolerance");
		GenAreaSimpleDouglasPeucker gasdp = new GenAreaSimpleDouglasPeucker();
		gasdp.init(tolerance);
		Feature f; Polygon p;
		for(Iterator iter = fc.iterator(); iter.hasNext();) {
			try {
				f = (Feature) iter.next();
				p = (Polygon) f.getGeometry();
				gasdp.addDataPolygon(p);
				if(gasdp.execute()) {
					f.setGeometry(gasdp.getDataPolygon());
				}
			}
			catch (ClassCastException e) {
				this.addError("only polygons can be simplified with this algorithm");
				return;
			}
		}
		wgreq.addResult("result", fc);
	}

	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("JGenAreaSimpleDouglasPeucker", "neun", "operator",
				"",
				"JGenAreaSimpleDouglasPeucker",
				"GenAreaSimpleDouglasPeucker from jaxpand-genoperators",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("tolerance", "DOUBLE", "10.0", "dDistanceTolerance");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
