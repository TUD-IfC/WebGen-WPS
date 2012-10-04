package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.axpand.jaxpand.genoperator.area.GenAreaEnlargeToRectangle;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;

public class JGenAreaEnlargeToRectangle extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		double dFinalArea = wgreq.getParameterDouble("dFinalArea");
		double dElongationThreshold = wgreq.getParameterDouble("dElongationThreshold");
		GenAreaEnlargeToRectangle gaetr = new GenAreaEnlargeToRectangle();
		gaetr.init(dFinalArea, dElongationThreshold);
		Feature f; Polygon p;
		for(Iterator iter = fc.iterator(); iter.hasNext();) {
			try {
				f = (Feature) iter.next();
				p = (Polygon) f.getGeometry();
				gaetr.addDataPolygon(p);
				if(gaetr.execute()) {
					f.setGeometry(gaetr.getDataPolygon());
				}
			}
			catch (ClassCastException e) {
				this.addError("only polygons can be aggregated");
				return;
			}
		}
		wgreq.addResult("result", fc);
	}

	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("JGenAreaEnlargeToRectangle", "neun", "operator",
				"",
				"JGenAreaEnlargeToRectangle",
				"GenAreaEnlargeToRectangle from jaxpand-genoperators",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("dFinalArea", "DOUBLE", "0.0", "The minimum area");
		id.addInputParameter("dElongationThreshold", "DOUBLE", "0.0", "The minimum value of elongation (ratio: witdh/length = between 0 and 1)");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
