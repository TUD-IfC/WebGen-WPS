package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.axpand.jaxpand.genoperator.area.GenAreaAggregation;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;

public class JGenAreaAggregation extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		double tolerance = wgreq.getParameterDouble("tolerance");
		GenAreaAggregation gaa = new GenAreaAggregation();
		gaa.init(tolerance);
		Feature f; Polygon p;
		for(Iterator iter = fc.iterator(); iter.hasNext();) {
			try {
				f = (Feature) iter.next();
				p = (Polygon) f.getGeometry();
				gaa.addDataPolygon(p, f, 0.0);
			}
			catch (ClassCastException e) {
				this.addError("only polygons can be aggregated");
				return;
			}
		}
		if(gaa.execute()) {
			ConstrainedFeatureCollection fcr = new ConstrainedFeatureCollection(fc.getFeatureSchema());
			for(int i = 0; i < gaa.getDataCount() ; i++) {
				f = (Feature)gaa.getDataLinkedObject(i);
				f.setGeometry(gaa.getDataPolygon(i));
				fcr.add(f);
			}
			wgreq.addResult("result", fcr);
		}
	}

	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("JGenAreaAggregation", "neun", "operator",
				"",
				"JGenAreaAggregation",
				"GenAreaAggregation from jaxpand-genoperators",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("tolerance", "DOUBLE", "10.0", "aggregation distance tolerance");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
