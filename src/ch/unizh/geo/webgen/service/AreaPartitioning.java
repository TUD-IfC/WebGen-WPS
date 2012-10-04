package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;

public class AreaPartitioning extends AWebGenAlgorithm implements IWebGenAlgorithm {
	
	public void run(WebGenRequest wgreq) {
		FeatureCollection selection = wgreq.getFeatureCollection("selection");
		FeatureCollection buildings = wgreq.getFeatureCollection("geom");
		runPartitioning(wgreq, selection, buildings);
	}

	private void runPartitioning(WebGenRequest wgreq, FeatureCollection selection, FeatureCollection buildings) {
		int i=1;
		for (Iterator ia = selection.iterator(); ia.hasNext();) {
			Feature fs = (Feature) ia.next();
			Geometry gs = fs.getGeometry();
			ConstrainedFeatureCollection fct = new ConstrainedFeatureCollection(buildings.getFeatureSchema());
			for (Iterator ib = buildings.iterator(); ib.hasNext();) {
				Feature fb = (Feature) ib.next();
				Geometry gb = fb.getGeometry();
				if(gs.contains(gb)) fct.add(fb);
			}
			if(fct.size() > 0) {
				wgreq.addResult("partition"+i, fct);
				ConstrainedFeatureCollection fcsres = new ConstrainedFeatureCollection(selection.getFeatureSchema());
				fcsres.add(fs);
				wgreq.addResult("partpoly"+i, fcsres);
				i++;
			}
		}
	}
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("AreaPartitioning", "neun", "support",
				"http://localhost:8080/webgen/execute",
				"AreaPartitioning",
				"Partitioning of Features through overlay polygons!",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "buildings or other features");
		
		String[] allowedS = {"Polygon"};
		id.addInputParameter("selection", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedS), "selection polygons");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
