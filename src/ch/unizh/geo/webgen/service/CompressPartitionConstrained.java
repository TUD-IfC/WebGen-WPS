package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import tools.Distance;

import ch.unizh.geo.algorithms.Rotate;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.server.WebGenRequestExecuter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDatasetFactory;

public class CompressPartitionConstrained extends AWebGenAlgorithm implements IWebGenAlgorithm {

	double direction = 0.0;
	double distance = 0.0;
	
	//ArrayList<Geometry> debugData = new ArrayList<Geometry>();
	
	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		FeatureCollection cfc = wgreq.getFeatureCollection("congeom");
		double roaddist = wgreq.getParameterDouble("roaddist");
		
		int iterations = 0;
		while(iterations < 10) {
			if(findConflict(fc, cfc, roaddist)) compressPartition(fc, direction, distance);
			else break;
		}

		wgreq.addResult("result", fc);
		//wgreq.addResult("debug", FeatureDatasetFactory.createFromGeometry(debugData));
	}
	
	
	public boolean findConflict(FeatureCollection fc, FeatureCollection cfc, double roaddist) {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("geom", cfc);
		Geometry roadring;
		FeatureCollection roads;
		WebGenRequest twgreq;
		try {
			twgreq = WebGenRequestExecuter.callService(parameters, "localcloned", "CombineLineStrings");
			roads = (FeatureCollection)twgreq.getResult("result");
		}
		catch (Exception e) {roads = cfc;}
		if(roads == null) roads = cfc;
		if(roads.getFeatures().size() == 1 && !(((Feature)roads.getFeatures().get(0)).getGeometry() instanceof MultiLineString)) {
			roadring = ((Feature)roads.getFeatures().get(0)).getGeometry();
		}
		else {
			twgreq = WebGenRequestExecuter.callService(parameters, "localcloned", "JTSUnionSingleLayer");
			roads = (FeatureCollection)twgreq.getResult("result");
			twgreq = WebGenRequestExecuter.callService(parameters, "localcloned", "Line2Polygon");
			roads = (FeatureCollection)twgreq.getResult("result");
			if(roads.getFeatures().size() != 1) {
				this.addError("only road partitions allowed");
				return false;
			}
			roadring = ((Polygon)((Feature)roads.getFeatures().get(0)).getGeometry()).getExteriorRing();
		}
		Geometry roadbuffer = roadring.buffer(roaddist);
		Geometry buildings = union(fc);
		//debugData.add((Geometry)roadbuffer.clone());
		
		Geometry sect = buildings.intersection(roadbuffer);
		//debugData.add((Geometry)sect.clone());
		if(sect != null) {
			if(sect.getNumGeometries() == 0) return false;
			Geometry largestSect = sect.getGeometryN(0);
			double largestSectArea = largestSect.getArea();
			double ta;
			for(int i=1; i<sect.getNumGeometries(); i++) {
				ta = sect.getGeometryN(i).getArea();
				if(ta > largestSectArea) {
					largestSect = sect.getGeometryN(i);
					largestSectArea = ta;
				}
			}
			//debugData.add((Geometry)largestSect.clone());
			Point largestSectCenter = largestSect.getCentroid();
			//debugData.add((Geometry)largestSectCenter.clone());
			double lSSx = largestSectCenter.getX();
			try {
				double[] minDist = Distance.MinDistBetween(largestSectCenter, roadring); //[0]=weight, [1]=x1, [2]=y1, [3]=x2, [4]=y2, 
				if((minDist[0] > 0) && (minDist[0]<roaddist))distance = (roaddist - minDist[0])*1.2;
				else distance = roaddist;
				//debugData.add(new Point(new Coordinate(minDist[1], minDist[2]), new PrecisionModel(), -1));
				//debugData.add(new Point(new Coordinate(minDist[3], minDist[4]), new PrecisionModel(), -1));
				double diffX = (minDist[1] - minDist[3]);
				double diffY = (minDist[2] - minDist[4]);
				if(lSSx == minDist[1]) {
					//direction = Math.atan(diffY / diffX);
					direction = Math.atan2(diffY, diffX);
				}
				else if(lSSx == minDist[3]) {
					//direction = Math.atan(-diffY / -diffX);
					direction = Math.atan2(-diffY, -diffX);
				}
				else {return false;}
				return true;
			}
			catch(Exception e) {}
		}
		return false;
	}
	
	public static FeatureCollection compressPartition(FeatureCollection fc, double stauchrichtung, double distanz) {
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
		
		double scale = 1-(distanz/widthInDirection);
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
		InterfaceDescription id = new InterfaceDescription("CompressPartitionConstrained", "neun", "operator",
				"",
				"CompressPartitionConstrained",
				"Compress buildings in  partition away from to close roads",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		String[] allowedR = {"LineString"};
		id.addInputParameter("congeom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedR), "layer with roads");
		id.addInputParameter("roaddist", "DOUBLE", "5.0", "minimum road distance");

		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
