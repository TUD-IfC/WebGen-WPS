package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import ch.unizh.geo.webgen.model.ConstrainedFeature;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDatasetFactory;

public class AreaPartitioningFlexible extends AWebGenAlgorithm implements IWebGenAlgorithm {
	
	public void run(WebGenRequest wgreq) {
		FeatureCollection selection = wgreq.getFeatureCollection("congeom");
		FeatureCollection buildings = wgreq.getFeatureCollection("geom");
		runPartitioning(wgreq, selection, buildings);
		
		/*WebGenRequest tw = new WebGenRequest();
		ConstrainedFeatureCollection b2 = new ConstrainedFeatureCollection(buildings);
		runPartitioning(tw, selection, b2.clone());
		runPartitioning(tw, selection, b2.clone());
		runPartitioning(tw, selection, b2.clone());
		runPartitioning(tw, selection, b2.clone());
		runPartitioning(tw, selection, b2.clone());
		runPartitioning(tw, selection, b2.clone());
		runPartitioning(tw, selection, b2.clone());
		runPartitioning(tw, selection, b2.clone());
		runPartitioning(tw, selection, b2.clone());
		runPartitioning(tw, selection, b2.clone());*/
	}

	private void runPartitioning(WebGenRequest wgreq, FeatureCollection selection, FeatureCollection buildings) {
		FeatureCollection roads = null;
		try {
			boolean nopolygons = false;
			for (Iterator ia = selection.iterator(); ia.hasNext();) {
				Geometry gs = ((Feature) ia.next()).getGeometry();
				if(!(gs instanceof Polygon)) {
					nopolygons = true;
					break;
				}
			}
			if(nopolygons) {
				roads = selection;
				selection = Line2PolygonFast(roads);
				wgreq.addResult("polys", selection);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		//long acttime = System.currentTimeMillis();
		
		int i=1;
		for (Iterator ia = selection.iterator(); ia.hasNext();) {
			Feature fs = (Feature) ia.next();
			Geometry gs = fs.getGeometry();
			ArrayList<Feature> buildings2remove = new ArrayList<Feature>();
			ConstrainedFeatureCollection fct = new ConstrainedFeatureCollection(buildings.getFeatureSchema());
			for (Iterator ib = buildings.iterator(); ib.hasNext();) {
				Feature fb = (Feature) ib.next();
				if(!(fb instanceof ConstrainedFeature)) fb = new ConstrainedFeature(fb);
				Geometry gb = fb.getGeometry();
				if(gs.contains(gb.getCentroid())) {
					fct.add(fb);
					buildings2remove.add(fb);
				}
			}
			buildings.removeAll(buildings2remove);
			if(fct.size() > 0) {
				wgreq.addResult("partition"+i, fct);
				ConstrainedFeatureCollection fcsres = new ConstrainedFeatureCollection(selection.getFeatureSchema());
				//fcsres.add(fs);
				for (Iterator ib = roads.iterator(); ib.hasNext();) {
					Feature fb = (Feature) ib.next();
					if(!(fb instanceof ConstrainedFeature)) fb = new ConstrainedFeature(fb);
					Geometry gb = fb.getGeometry();
					if(gs.touches(gb) || gs.contains(gb)) fcsres.add(fb);
				}
				wgreq.addResult("partpoly"+i, fcsres);
				i++;
			}
		}
		
		//System.out.println("Time: " + (System.currentTimeMillis() - acttime) + "ms");
	}
	
	FeatureCollection Line2PolygonFast(FeatureCollection fc) throws Exception {
		Polygonizer polygonizer = new Polygonizer();
        for (Iterator i = fc.iterator(); i.hasNext();) {
            Feature f = (Feature) i.next();
            Geometry geom = f.getGeometry();
            polygonizer.add(geom);
        }
        Collection c = polygonizer.getPolygons();
        FeatureCollection fcnew =  FeatureDatasetFactory.createFromGeometry(c);
		return fcnew;
	}
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("AreaPartitioningFlexible", "neun", "support",
				"",
				"AreaPartitioningFlexible",
				"Partitioning of Features through overlay polygons!",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "buildings or other features");
		
		String[] allowedS = {"LineString","Polygon"};
		id.addInputParameter("congeom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedS), "selection polygons");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
