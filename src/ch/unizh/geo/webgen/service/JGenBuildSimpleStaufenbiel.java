package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.axpand.jaxpand.genoperator.building.GenBuildSimpleStaufenbiel;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;

public class JGenBuildSimpleStaufenbiel extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		double minlength = wgreq.getParameterDouble("minlength");
		GenBuildSimpleStaufenbiel gbss = new GenBuildSimpleStaufenbiel(null);
		gbss.init(minlength);
		Feature f; Polygon p;
		for(Iterator iter = fc.iterator(); iter.hasNext();) {
			try {
				f = (Feature) iter.next();
				p = (Polygon) f.getGeometry();
				gbss.addDataPolygon(p, 0.0);
				if(gbss.execute()) {
					f.setGeometry(gbss.getDataPolygon());
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
		InterfaceDescription id = new InterfaceDescription("JGenBuildSimpleStaufenbiel", "neun", "operator",
				"",
				"JGenBuildSimpleStaufenbiel",
				"GenBuildSimpleStaufenbiel from jaxpand-genoperators",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("minlength", "DOUBLE", "10.0", "minlength");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
