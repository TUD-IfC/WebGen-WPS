package ch.unizh.geo.webgen.service;


import java.util.List;

import ch.unizh.geo.algorithms.spatialAttributeOps.AttributeOp;
import ch.unizh.geo.algorithms.spatialAttributeOps.JoinAttributes;
import ch.unizh.geo.algorithms.spatialAttributeOps.SpatialRelationOp;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.registry.ParameterDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jump.feature.FeatureCollection;

/**
 * @descrption:
 * 		smoothes lines and polygons with a snakes algorithm 
 * 		needed parameters: 
 * 			FeatureCollection => Feature => Geometry  : LineString or Polygon  
 * 			tolerance : maximum displacement
 * 			segmentate : should the line be segmentated
 * 		further params initilized in this file:
 * 			start params Snakes: alpha und beta (init = 1)
 * 			segmentation value: curvature (init = Pi/3)
 * 			
 * @author sstein
 *
 * 
 */
public class JoinStypeByMajority extends AWebGenAlgorithm implements IWebGenAlgorithm  {
    
    private String attrName= "stype";
	
    public void run(WebGenRequest wgreq) {
		FeatureCollection featuresA = wgreq.getFeatureCollection("source features");
		FeatureCollection featuresB = wgreq.getFeatureCollection("target features");
		double radius = wgreq.getParameterDouble("buffer radius in m");
		
		//-- calculate
		List srcFeatures = featuresA.getFeatures();
		List targetFeatures = featuresB.getFeatures();	
		FeatureCollection results = JoinAttributes.joinAttributes(srcFeatures, targetFeatures,
		        						attrName, AttributeOp.MAJORITY, 
		        						SpatialRelationOp.CONTAINS, radius, null);
		if(results.size() > 0){
			wgreq.addResult("majority join", results);
		}
	}
    
    public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("JoinStypeByMajority", "neun", "support",
				"",
				"JoinStypeByMajority",
				"JoinStypeByMajority",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		ParameterDescription sourceParam = new ParameterDescription("source features", "FeatureCollection", "source features with stype attribute");
		sourceParam.addAttribute(new AttributeDescription("GEOMETRY", "GEOMETRY", allowed));
		sourceParam.addAttribute(new AttributeDescription(attrName, "STRING"));
		id.addInputParameter(sourceParam);
		id.addInputParameter("target features", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "target features");
		id.addInputParameter("buffer radius in m", "DOUBLE", "10.0", "buffer radius in meters");
		
		//add output parameters
		id.addOutputParameter("majority join", "FeatureCollection");
		return id;
	}
}
