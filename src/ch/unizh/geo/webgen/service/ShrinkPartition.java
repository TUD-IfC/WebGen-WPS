package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;

public class ShrinkPartition extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		double factor = wgreq.getParameterDouble("factor");
		
		/*Feature f;
		Geometry g;
		Polygon p;
		for(Iterator iter = fc.iterator(); iter.hasNext();) {
			f = (Feature) iter.next();
			g = f.getGeometry();
			if(g instanceof Polygon) {
				p = GeometryHelper.skaleSize((Polygon)g, g.getArea()*factor);
				f.setGeometry(p);
			}
		}
		wgreq.addResult("result", fc);*/
		
		/*Geometry mp = union(fc);
		MultiPolygon mpn = skaleMultiPolygon(mp, factor);
		Feature f = (Feature)fc.iterator().next();
		fc.clear();
		f.setGeometry(mpn);
		fc.add(f);*/
		
		skaleAllPolygons(fc, factor);
		
		wgreq.addResult("result", fc);
	}
	
	
	public static FeatureCollection skaleAllPolygons(FeatureCollection fc, double factor) {
		Geometry mp = union(fc);
		Point orig_centroid = mp.getCentroid();
		double orig_centroid_x = orig_centroid.getX();
		double orig_centroid_y = orig_centroid.getY();
		
		Feature f;
		Polygon p;
		Coordinate[] p_coords;
		for(Iterator iter=fc.iterator(); iter.hasNext();) {
			f = (Feature) iter.next();
			p = (Polygon) f.getGeometry();
			p_coords = p.getCoordinates();
			for(int i=0; i<p_coords.length; i++) {
				p_coords[i].x *= factor;
				p_coords[i].y *= factor;
			}
			p.geometryChanged();
		}
		
		mp = union(fc);
		Point tmp_centroid = mp.getCentroid();
		double tmp_centroid_x = tmp_centroid.getX();
		double tmp_centroid_y = tmp_centroid.getY();
		
		double trans_x = tmp_centroid_x - orig_centroid_x;
		double trans_y = tmp_centroid_y - orig_centroid_y;
		
		
		for(Iterator iter=fc.iterator(); iter.hasNext();) {
			f = (Feature) iter.next();
			p = (Polygon) f.getGeometry();
			p_coords = p.getCoordinates();
			for(int i=0; i<p_coords.length; i++) {
				p_coords[i].x -= trans_x;
				p_coords[i].y -= trans_y;
			}
			p.geometryChanged();
		}
		
		return fc;
	}
	
	
	public static MultiPolygon skaleMultiPolygon(MultiPolygon mp, double factor) {
		Point orig_centroid = mp.getCentroid();
		double orig_centroid_x = orig_centroid.getX();
		double orig_centroid_y = orig_centroid.getY();
		
		Coordinate[] all_coords = mp.getCoordinates();
		for(int i=0; i<all_coords.length; i++) {
			all_coords[i].x *= factor;
			all_coords[i].y *= factor;
		}
		mp.geometryChanged();
		Point tmp_centroid = mp.getCentroid();
		double tmp_centroid_x = tmp_centroid.getX();
		double tmp_centroid_y = tmp_centroid.getY();
		
		double trans_x = tmp_centroid_x - orig_centroid_x;
		double trans_y = tmp_centroid_y - orig_centroid_y;
		
		all_coords = mp.getCoordinates();
		for(int i=0; i<all_coords.length; i++) {
			all_coords[i].x -= trans_x;
			all_coords[i].y -= trans_y;
		}
		
		return mp;
	}
	
	private static Geometry union(FeatureCollection fc) {
		Geometry currUnion = null;
        for (Iterator i = fc.iterator(); i.hasNext();) {
            Feature f = (Feature) i.next();
            Geometry p = f.getGeometry();
            if (currUnion == null) {
                currUnion = p;
            } else {
                currUnion = currUnion.union(p);
            }
        }
        return currUnion;
    }

	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("ShrinkPartition", "neun", "operator",
				"",
				"ShrinkPartition",
				"Shrink polygons",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("factor", "DOUBLE", "0.9", "factor for shrinking");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
