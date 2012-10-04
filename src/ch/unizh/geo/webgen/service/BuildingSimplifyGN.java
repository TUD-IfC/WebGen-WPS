package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import jtsExtension.JTSPolygon;
import jumpExtension.Jump2Gen;
import model.generalisation.GenAreaObject;
import model.generalisation.GenObjectClass;
import model.generalisation.GenObjectClassArea;
import model.generalisation.GenObjectClassSchema;
import model.generalisation.GenResolution;
import topology.PlanarGraph;
import utilities.ExceptionList;
import buildingGeneralisation.GNmove;
import buildingGeneralisation.GNobject;
import ch.unizh.geo.webgen.model.ConstrainedFeature;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Polygon;

/*
 * Created on 28.02.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author neun
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BuildingSimplifyGN extends AWebGenAlgorithm implements IWebGenAlgorithm {
	
	public void run(WebGenRequest wgreq) {
		ConstrainedFeatureCollection fc = (ConstrainedFeatureCollection)wgreq.getFeatureCollection("geom");
		double tolerance = wgreq.getParameterDouble("minlength");
		try {
			simplifyC(fc, tolerance);
			wgreq.addResult("result", fc);
		}
		catch (Exception e) {e.printStackTrace();}
		
	}
	
	public void simplifyC(ConstrainedFeatureCollection fc, double minlength) {
		GenResolution resolution = new GenResolution(PlanarGraph.FULL_TOPOLOGY, false);
		GenObjectClassSchema genObjectClassSchema = Jump2Gen.generateGenObjectClassSchema(fc.getFeatureSchema());
		GenObjectClassArea genObjectClassArea = resolution.generateGenObjectClassArea("buildings", GenObjectClass.generateKey(), genObjectClassSchema);
		for(Iterator iter = fc.iterator(); iter.hasNext();) {
			ConstrainedFeature cf = (ConstrainedFeature)iter.next();
        	try {
        		JTSPolygon jtsPolygon = new JTSPolygon((Polygon)cf.getGeometry());
				GenAreaObject genAreaObject = resolution.generateGenAreaObject(genObjectClassArea, 
						Jump2Gen.generateGenAttributesFromFeature(cf), jtsPolygon);
				GNobject gnObject = new GNobject(genAreaObject);
				GNmove gnMove = new GNmove(gnObject, jtsPolygon);
				gnMove.simplify(true, minlength);
				Polygon ergebnisPolygon = gnMove.getPolygon();
				cf.setGeometry(ergebnisPolygon);
			} catch (ExceptionList e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("BuildingSimplifyGN", "burg", "operator",
				"",
				"BuildingSimplifyGN",
				"Simplify Buildings from JAxpand Package",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with buildings");
		id.addInputParameter("minlength", "DOUBLE", "10.0", "buiding segment minimum length");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}

}