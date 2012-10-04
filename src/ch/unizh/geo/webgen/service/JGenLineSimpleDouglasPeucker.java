package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.axpand.jaxpand.genoperator.line.GenLineSimpleDouglasPeucker;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;

public class JGenLineSimpleDouglasPeucker extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		double tolerance = wgreq.getParameterDouble("tolerance");
		GenLineSimpleDouglasPeucker glsdp = new GenLineSimpleDouglasPeucker();
		glsdp.init(tolerance);
		Feature f; LineString l;
		for(Iterator iter = fc.iterator(); iter.hasNext();) {
			try {
				f = (Feature) iter.next();
				l = (LineString) f.getGeometry();
				glsdp.addDataLine(l);
				if(glsdp.execute()) {
					f.setGeometry(glsdp.getDataLine());
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
		InterfaceDescription id = new InterfaceDescription("JGenLineSimpleDouglasPeucker", "neun", "operator",
				"",
				"JGenLineSimpleDouglasPeucker",
				"GenLineSimpleDouglasPeucker from jaxpand-genoperators",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"LineString"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("tolerance", "DOUBLE", "10.0", "dDistanceTolerance");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
