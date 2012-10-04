package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.axpand.jaxpand.genoperator.area.GNmove;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;

public class BuildingSimplificationOSM extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		double minlength = wgreq.getParameterDouble("minlength");
		int id = wgreq.getParameterInt("ID");
		Feature f; Polygon p; Polygon ep;
		for(Iterator iter = fc.iterator(); iter.hasNext();) {
			f = (Feature)iter.next();
			p = (Polygon) f.getGeometry();
			GNmove gnmove = new GNmove(p);
			gnmove.simplify(minlength, 0.0, null, null);
			ep = gnmove.getPolygon();
			f.setGeometry(ep);
		}
		wgreq.addResult("result", fc);
	}

	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("BuildingSimplificationOSM", "neun", "operator",
				"",
				"BuildingSimplificationOSM",
				"Building Simplification OpenStreetMap-Version",
				"1.0",
				new String[] {"ica.genops.cartogen.Enhancement"});
		id.visible = false;
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("ID", "FeatureCollection", new AttributeDescription("ID", "INTEGER", allowed), "layer with geometries");
		id.addInputParameter("minlength", "DOUBLE", "10.0", "minimum length");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "simplified building polygons");
		return id;
	}
}
