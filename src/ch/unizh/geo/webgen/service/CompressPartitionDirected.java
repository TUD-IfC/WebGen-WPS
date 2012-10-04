package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.algorithms.Rotate;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;

public class CompressPartitionDirected extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		double direction = wgreq.getParameterDouble("direction");
		double factor = wgreq.getParameterDouble("factor");
		compressPartition(fc, direction, factor);
		
		wgreq.addResult("result", fc);
	}
	
	
	public static FeatureCollection compressPartition(FeatureCollection fc, double stauchrichtung, double factor) {
		Geometry mp = union(fc);
		Point orig_centroid = mp.getCentroid();
		double orig_centroid_x = orig_centroid.getX();
		double orig_centroid_y = orig_centroid.getY();
		
		double rotateAngle = 0-stauchrichtung;
		Point ancorPoint = mp.getCentroid();
		Feature f;
		Polygon p;
		Coordinate[] p_coords;
		for(Iterator iter=fc.iterator(); iter.hasNext();) {
			f = (Feature) iter.next();
			p = (Polygon) f.getGeometry();
			Rotate.rotate(ancorPoint, rotateAngle, p);
			p.geometryChanged();
		}
		
		
		//get left stop-point
		mp = union(fc);
		double x0 = mp.getEnvelopeInternal().getMinX();
		double widthInDirection = mp.getEnvelopeInternal().getWidth();
		
		double scale = 0.9;
		for(Iterator iter=fc.iterator(); iter.hasNext();) {
			f = (Feature) iter.next();
			p = (Polygon) f.getGeometry();
			Coordinate[] coord=p.getExteriorRing().getCoordinates();
			for(int i=0;i<coord.length;i++){			
				coord[i].x=x0+scale*(coord[i].x-x0);
			}
			//--stretch the holes of the polygon along the X axis
			for(int j=0;j<p.getNumInteriorRing();j++){
				Coordinate[] holeCoord=p.getInteriorRingN(j).getCoordinates();
				for(int i=0;i<holeCoord.length;i++){
					holeCoord[i].x=x0+scale*(holeCoord[i].x-x0);
				}
			}
			p.geometryChanged();
		}

		
		//rotate back
		ancorPoint = mp.getCentroid();
		for(Iterator iter=fc.iterator(); iter.hasNext();) {
			f = (Feature) iter.next();
			p = (Polygon) f.getGeometry();
			Rotate.rotate(ancorPoint, -rotateAngle, p);
			p.geometryChanged();
		}
		
		
		//move to correct center point (10%/2 of the width in the direction of compression)
		double final_centroid_x = orig_centroid_x + ((widthInDirection*(1-scale))/2)*Math.cos(stauchrichtung);
		double final_centroid_y = orig_centroid_y + ((widthInDirection*(1-scale))/2)*Math.sin(stauchrichtung);
		
		mp = union(fc);
		Point tmp_centroid = mp.getCentroid();
		double tmp_centroid_x = tmp_centroid.getX();
		double tmp_centroid_y = tmp_centroid.getY();
		
		double trans_x = tmp_centroid_x - final_centroid_x;
		double trans_y = tmp_centroid_y - final_centroid_y;
		
		
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
		InterfaceDescription id = new InterfaceDescription("CompressPartitionDirected", "neun", "operator",
				"",
				"CompressPartitionDirected",
				"Compress buildings in  partition in one direction",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("factor", "DOUBLE", "0.9", "scaling factor");
		id.addInputParameter("direction", "DOUBLE", "45", "direction in which the compression is done (in degrees)");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
