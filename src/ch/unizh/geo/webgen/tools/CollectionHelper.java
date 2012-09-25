package ch.unizh.geo.webgen.tools;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import ch.unizh.geo.webgen.model.ConstrainedFeature;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;

public class CollectionHelper {

	public static double calculateBWRatio(ConstrainedFeatureCollection fc, FeatureCollection cfc) {
		double fcArea = 0.0;
		List fclist = fc.getFeatures();
		for(int i=0; i< fclist.size(); i++) {
			ConstrainedFeature feat = (ConstrainedFeature)fclist.get(i);
			fcArea += feat.getGeometry().getArea();
		}
		double cfcArea = computePolygonizedArea(cfc);
		double bwRatio = fcArea/cfcArea;
		return bwRatio;
	}
	
	public static double computePolygonizedArea(FeatureCollection fc) {
		Polygonizer polygonizer = new Polygonizer();
        for (Iterator i = fc.iterator(); i.hasNext();) {
            Feature f = (Feature) i.next();
            Geometry geom = f.getGeometry();
            polygonizer.add(geom);
        }
        Collection c = polygonizer.getPolygons();
        Polygon p;
        double areaSum = 0.0;
        for(Iterator iter = c.iterator(); iter.hasNext();) {
        	p = (Polygon)iter.next();
        	areaSum += p.getArea();
        }
		return areaSum;
	}

}
