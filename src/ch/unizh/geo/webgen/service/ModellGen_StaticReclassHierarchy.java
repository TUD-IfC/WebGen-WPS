package ch.unizh.geo.webgen.service;

import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

public class ModellGen_StaticReclassHierarchy extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		//wgreq.addResult("result", hierarchy);
	}

	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("ModellGen_StaticReclassHierarchy", "neun", "support",
				"",
				"ModellGen_StaticReclassHierarchy",
				"delivers a Static Reclass-Hierarchy for Modell Generalisation",
				"1.0");
		
		//add input parameters
		/*String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("width", "DOUBLE", "10.0", "buffer width");
		
		ParameterDescription capstyleparam = new ParameterDescription("capstyle", "STRING", "CAP_ROUND", "cap style of the buffer ends");
		capstyleparam.addSupportedValue("CAP_ROUND");
		capstyleparam.addSupportedValue("CAP_BUTT");
		capstyleparam.addSupportedValue("CAP_SQUARE");
		capstyleparam.setChoiced();
		id.addInputParameter(capstyleparam);
		
		id.addInputParameter("quadrants", "INTEGER", "8", "quadrant segments for rounding of circles");*/
		
		//add output parameters
		id.addOutputParameter("hierarchy", "ClassHierarchy");
		return id;
	}
}
