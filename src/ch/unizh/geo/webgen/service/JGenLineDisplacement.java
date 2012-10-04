package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

//import cache.GenLineDisplacement;
import com.axpand.jaxpand.genoperator.line.GenLineDisplacement;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;

public class JGenLineDisplacement extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection lines = wgreq.getFeatureCollection("geom");
		//FeatureCollection areas = wgreq.getFeatureCollection("congeom");
		
		double dSigWidth = wgreq.getParameterDouble("dSigWidth");
		double dMinDist = wgreq.getParameterDouble("dMinDist");
		
		int nMinCoords = wgreq.getParameterInt("nMinCoords");
		int nMaxIterations = wgreq.getParameterInt("nMaxIterations");
		double dEnergyThreshold = wgreq.getParameterDouble("dEnergyThreshold");
		int nNofBorderVertices = wgreq.getParameterInt("nNofBorderVertices");
		double dRatioInternalExternalEnergy = wgreq.getParameterDouble("dRatioInternalExternalEnergy");
		double dCoordinateStepVariation = wgreq.getParameterDouble("dCoordinateStepVariation");
		boolean bHandleCrosses = wgreq.getParameterBoolean("bHandleCrosses");
		boolean bSelfDisplacement = wgreq.getParameterBoolean("bSelfDisplacement");
		
		GenLineDisplacement gld = new GenLineDisplacement(null);
		gld.init(nMinCoords, nMaxIterations, dEnergyThreshold, nNofBorderVertices, 
				dRatioInternalExternalEnergy, dCoordinateStepVariation, bHandleCrosses, bSelfDisplacement);
		Feature f; Polygon p; LineString l;
		for(Iterator iter = lines.iterator(); iter.hasNext();) {
			try {
				f = (Feature) iter.next();
				l = (LineString) f.getGeometry();
				gld.addDataLine(l, dSigWidth, dMinDist, true, 5, false);
			}
			catch (ClassCastException e) {
				this.addError("only lines can be displaced");
				return;
			}
		}
		/*if (areas.size() > 0){
			System.out.println("true????");
			for(Iterator iter = areas.iterator(); iter.hasNext();) {
				try {
					f = (Feature) iter.next();
					p = (Polygon) f.getGeometry();
					gld.addDataPolygon(p, 0.0, dMinDist);
				}
				catch (ClassCastException e) {
					this.addError("only areas can cause displacement");
					return;
				}
			}
		}*/
		
		FeatureCollection linesDisplaced = new FeatureDataset(lines.getFeatureSchema());
		FeatureSchema schema = linesDisplaced.getFeatureSchema();		
		linesDisplaced.clear();
		if(gld.execute()) {
			
			for(int i = 0; i < lines.size(); i++) {
				Geometry geom;
				LineString line;
				line = gld.getDataLine(i);
				geom = line;
				Feature feat = new BasicFeature(schema);
				feat.setGeometry(geom);
				linesDisplaced.add(feat);
				
			}			
		}
		wgreq.addResult("result", linesDisplaced);
	}

	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("JGenLineDisplacement", "neun", "operator",
				"",
				"JGenLineDisplacement",
				"GenLineDisplacement from jaxpand-genoperators",
				"1.0",
				new String[] {"ica.genops.cartogen.Displacement"});
		id.visible = true;
		
		//add input parameters
		String[] allowedL = {"LineString"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedL), "lines to displace");
		//String[] allowedA = {"Polygon"};
		//id.addInputParameter("congeom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedA), "areas causing displacements");
		
		id.addInputParameter("dSigWidth", "DOUBLE", "0.0", "The signature width of the lines.");
		id.addInputParameter("dMinDist", "DOUBLE", "0.0", "The minimum object distance for beeing legible.");
		
		id.addInputParameter("nMinCoords", "INTEGER", "3", "The minimal number of coordinates for a line to displace.");
		id.addInputParameter("nMaxIterations", "INTEGER", "5", "The maximal number of iterations.");
		id.addInputParameter("dEnergyThreshold", "DOUBLE", "-1.0", "The aspired energy difference between two iterations to break the iteration. -1 when nMaxIterations is used.");
		id.addInputParameter("nNofBorderVertices", "INTEGER", "2", "The number of fix border vertices for each line.");
		id.addInputParameter("dRatioInternalExternalEnergy", "DOUBLE", "0.3", "The ratio between internal and external energy.");
		id.addInputParameter("dCoordinateStepVariation", "DOUBLE", "5.0", "The distance for coordinate steps in the variation method.");
		id.addInputParameter("bHandleCrosses", "BOOLEAN", "true", "Crosses will be handled.");
		id.addInputParameter("bSelfDisplacement", "BOOLEAN", "false", "Lines can displace themselfes.");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedL), "displaced lines");
		return id;
	}
}
