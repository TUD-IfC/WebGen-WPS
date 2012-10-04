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

public class BuildingSimplificationTest extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		double minlength = wgreq.getParameterDouble("minlength");
		double fc = 0;
		fc = minlength;
		wgreq.addResult("result", fc);
	}

	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("BuildingSimplificationTest", "neun", "operator",
				"",
				"BuildingSimplificationTest",
				"Building Simplification to Test cURL Whoo",
				"1.0",
				new String[] {"ica.genops.cartogen.Enhancement"});
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("minlength", "DOUBLE", "10.0", "minimum length");
		
		//add output parameters
		id.addOutputParameter("result", "DOUBLE", new AttributeDescription("double", "DOUBLE", allowed), "simplified building polygons");
		return id;
	}
}
