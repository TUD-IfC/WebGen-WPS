package ch.unizh.geo.webgen.service;

import java.util.HashMap;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.server.WebGenRequestExecuter;

/**
 * @descrption:
 * 		filter features of one or multiple classes (,separated)
 *      --> returns FeatureCollection without those classes
 * 			
 * @author neun
 *
 * 
 */
public class NodeAndCombineLineStrings extends AWebGenAlgorithm implements IWebGenAlgorithm  {

    public void run(WebGenRequest wgreq) {
		Object geom = wgreq.getFeatureCollection("geom");
		try {
			HashMap params = wgreq.getParameters();
			
			WebGenRequest twgreq = WebGenRequestExecuter.callService(params, "localcloned", "NodeLineStrings");
			geom = twgreq.getResult("result");
			if(geom == null) geom = twgreq.getParameter("geom");
			params.put("geom", geom);
			
			twgreq = WebGenRequestExecuter.callService(params, "localcloned", "CombineLineStrings");
			geom = twgreq.getResult("result");
			if(geom == null) geom = twgreq.getParameter("geom");
			params.put("geom", geom);
			
			twgreq = WebGenRequestExecuter.callService(params, "localcloned", "FilterDuplexLineStrings");
			geom = twgreq.getResult("result");
			if(geom == null) geom = twgreq.getParameter("geom");
			
			wgreq.addResult("result", geom);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("NodeAndCombineLineStrings", "neun", "operator",
				"",
				"NodeAndCombineLineStrings",
				"Node and Combine LineStrings",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowedS = {"LineString"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedS), "all streets");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
