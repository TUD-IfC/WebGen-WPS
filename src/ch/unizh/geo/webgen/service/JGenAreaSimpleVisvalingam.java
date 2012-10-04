package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.axpand.jaxpand.genoperator.area.GenAreaSimpleVisvalingam;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;

public class JGenAreaSimpleVisvalingam extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		int maxcorner = wgreq.getParameterInt("maxcorner");
		double dimfactor = wgreq.getParameterDouble("dimfactor");
		GenAreaSimpleVisvalingam gasv = new GenAreaSimpleVisvalingam();
		Feature f; Polygon p;
		for(Iterator iter = fc.iterator(); iter.hasNext();) {
			try {
				f = (Feature) iter.next();
				p = (Polygon) f.getGeometry();
				gasv.addDataPolygon(p);
				if(dimfactor != 0.0) gasv.init((int)Math.round(p.getNumPoints()*dimfactor));
				else if(maxcorner != 0) gasv.init(p.getNumPoints()-maxcorner-1);
				else gasv.init(p.getNumPoints()-5);
				if(gasv.execute()) {
					f.setGeometry(gasv.getDataPolygon());
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
		InterfaceDescription id = new InterfaceDescription("JGenAreaSimpleVisvalingam", "neun", "operator",
				"",
				"JGenAreaSimpleVisvalingam",
				"GenAreaSimpleVisvalingam from jaxpand-genoperators",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("maxcorner", "INTEGER", "4", "number of maximum corners (all polygons will be rduced to this number)");
		id.addInputParameter("dimfactor", "DOUBLE", "0.0", "factor to reduce number of corners (between 0 and 1)");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
