package ch.unizh.geo.webgen.service;

import ch.unizh.geo.algorithms.snakes.LineDisplacementSnakes;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
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
public class LineDisplacement extends AWebGenAlgorithm implements IWebGenAlgorithm  {

    //private double segmentCurvThreshold = Math.PI/3;
    double alpha = 1;
    double beta = 1;
    double minPoints = 6;
    
    public void run(WebGenRequest wgreq) {
		FeatureCollection features = wgreq.getFeatureCollection("geom");
		int mapScale = wgreq.getParameterInt("scale");
		int iterations = wgreq.getParameterInt("iterations");
		double signatureDiameternMM = wgreq.getParameterDouble("signatureDiameternMM");

		LineDisplacementSnakes dispSnakes = new LineDisplacementSnakes(features, iterations, mapScale, signatureDiameternMM);
		
		FeatureCollection fcnew = dispSnakes.getDisplacedLines();
		FeatureCollection fcNrgPoints = dispSnakes.getInitialPointEnergies();
		FeatureCollection fcBuffers = dispSnakes.getMinDistAndSignatureBuffers();
		wgreq.addResult("result", fcnew);
		wgreq.addResult("initial Energy of Vertices", fcNrgPoints);
		wgreq.addResult("mindist and signature buffers", fcBuffers);
	}
    
    public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("LineSmoothing", "sstein", "operator",
				"",
				"LineSmoothing",
				"Line Smoothing",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"LineString"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("scale", "INTEGER", "25000", "map scale 1:x");
		id.addInputParameter("iterations", "INTEGER", "5", "iterations");
		id.addInputParameter("signatureDiameternMM", "DOUBLE", "10.0", "signatureDiameternMM");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		id.addOutputParameter("initial Energy of Vertices", "FeatureCollection");
		id.addOutputParameter("mindist and signature buffers", "FeatureCollection");
		return id;
	}
			
}
