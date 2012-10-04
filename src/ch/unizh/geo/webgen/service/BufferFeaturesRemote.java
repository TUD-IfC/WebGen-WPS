package ch.unizh.geo.webgen.service;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.server.WebGenRequestExecuter;

public class BufferFeaturesRemote extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		//FeatureCollection fc = wgreq.getFeatureCollection("geom");
		//double width = wgreq.getParameterDouble("width");
		//FeatureCollection fcnew = runBuffer(fc, width);
		WebGenRequest resreq = WebGenRequestExecuter.callService(wgreq.getParameters(), "http://141.30.137.195:8080/webgen_core/execute", "BufferFeatures");
		wgreq.addResult("result", resreq.getParameter("result"));
		
	}
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("BufferFeaturesRemote", "neun", "support",
				"",
				"BufferFeaturesRemote",
				"Buffer function on tropo",
				"1.0");
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("width", "DOUBLE", "10.0", "buffer width");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
