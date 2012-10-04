package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import ch.unizh.geo.algorithms.jts17qtree.Quadtree;
import ch.unizh.geo.algorithms.polygons.PolygonMerge;
import ch.unizh.geo.algorithms.polygons.PolygonSetMerger;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.tools.GeometryHelper;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureDatasetFactory;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public class MergeTouchingPolygons extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		try {
			fc = merge(fc, false);
			wgreq.addResult("result", fc);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private FeatureCollection merge(FeatureCollection features, boolean byAttribute) throws Exception {  
		if(byAttribute) {
			/*************************
			 * Features should be merged according to similar attribute value
			 ************************/
			/*features = this.srcLayer.getFeatureCollectionWrapper().getFeatures();
			FeatureCollection myCollA = PolygonSetMerger.mergePolySetByType(features, this.attrName,context, monitor);
			context.addLayer(StandardCategoryNames.WORKING, "mergedPolygons", myCollA);*/
		}
		else {
			/*************************
			 * Two features should be merged
			 ************************/	    
			if (features.size() == 2){
				Iterator iter = features.iterator();
				Feature f1 = (Feature)iter.next();
				Feature f2 = (Feature)iter.next();
				PolygonMerge merge = new PolygonMerge(f1.getGeometry(), f2.getGeometry()); 
				if(merge.isMergeSuccesfull() == 1){
					Geometry g = merge.getOutPolygon();
					f1.setGeometry(g);
					features.remove(f2);
				}
				else if(merge.isMergeSuccesfull() == 2){
					System.out.println("Multipolygon created");
					Geometry g = merge.getOutPolygon();
					f1.setGeometry(g);
					features.remove(f2);		    		
				}
				else if(merge.isMergeSuccesfull() == 0){
					System.out.println("polygons don't touch");
				}
			}
			else {
				/*************************
				 * set of poly geometries should be merged
				 ************************/
				ArrayList resultGeoms = null;
				ArrayList geoms = new ArrayList();
				Quadtree qtree = new Quadtree();
				//put all geoms in a tree for faster search
				for (Iterator iter = features.iterator(); iter.hasNext();) {
					Feature element = (Feature) iter.next();				
					if (element.getGeometry() instanceof Polygon){
						geoms.add(element.getGeometry());
						element.getGeometry().setUserData(element);
						Polygon poly = (Polygon)element.getGeometry();
						qtree.insert(poly.getEnvelopeInternal(), poly);
					}
					else{
						System.out.println("no polygon");
					}
				}
				if (geoms.size() > 0){
					resultGeoms = PolygonSetMerger.mergeGeoms(geoms, qtree, null);
					features.clear();
					Feature f; Geometry g;
					for(Iterator iter = resultGeoms.iterator(); iter.hasNext();) {
						g = (Geometry) iter.next();
						f = (Feature) g.getUserData();
						f.setGeometry(g);
						g.setUserData(null);
						features.add(f);
					}
				}
			}
		}
		return features;
	}
	

	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("MergeTouchingPolygons", "neun", "support",
				"",
				"MergeTouchingPolygons",
				"MergeTouchingPolygons",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
