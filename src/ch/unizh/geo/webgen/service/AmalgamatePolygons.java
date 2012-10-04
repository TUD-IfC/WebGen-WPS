package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
import java.util.Collection;

import madge.structures.Graph.GraphEdge;
import madge.structures.Graph.ProxyGraphWebGen;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;

public class AmalgamatePolygons extends AWebGenAlgorithm implements IWebGenAlgorithm  {
    
	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		double mindist = wgreq.getParameterDouble("mindist");
		FeatureCollection fcnew = amalgamatePolygons(fc, mindist);
		if(fcnew != null) {
			wgreq.addResult("result", fcnew);
		}
	}
	
	private FeatureCollection amalgamatePolygons(FeatureCollection fc, double mindist) {
		try {
			ProxyGraphWebGen pg = new ProxyGraphWebGen();
			Collection obstructFs = new ArrayList();
			pg.ProxyGraphPopulate(fc.getFeatures(), obstructFs, mindist);
			
			for (int i = 0; i < pg.edges.size(); i++) {
				GraphEdge te = (GraphEdge) pg.edges.get(i);
				try {
					Feature tf1 = te.node1.rwo;
					Feature tf2 = te.node2.rwo;
					if((tf1 != null) && (tf2 != null)) {
						Polygon tp1 = (Polygon)tf1.getGeometry();
						Polygon tp2 = (Polygon)tf2.getGeometry();
						//Coordinate tc1 = new Coordinate(te.x1, te.y1);
						//Coordinate tc2 = new Coordinate(te.x2, te.y2);
						//Polygon result = sawPolygons(tp1, tp2, tc1, tc2);
						Geometry result = sawPolygons(tp1, tp2, mindist);
						tf1.setGeometry(result);
						//fc.add(tf1);
						tf2.setGeometry(null);
						fc.remove(tf2);
					}
				}
				catch(Exception e) {}
			}
			return fc;
			
			/*GeometryFactory gfactory = new GeometryFactory() ;
			ArrayList<LineString> edgelist = new ArrayList<LineString>();
			for (int i = 0; i < pg.edges.size(); i++) {
				GraphEdge te = (GraphEdge) pg.edges.get(i);
				Coordinate[] coo = new Coordinate[2];
	        	coo[0] = new Coordinate(te.x1, te.y1);
	        	coo[1] = new Coordinate(te.x2, te.y2);
	        	edgelist.add(gfactory.createLineString(coo));
			}
	        return FeatureDatasetFactory.createFromGeometry(edgelist);*/
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private Geometry sawPolygons(Polygon p1, Polygon p2, double mindist) {
		double buffdist = mindist / 1.8;
		//p1 = (Polygon) GeometryHelper.expandPolygon(p1, buffdist);
		Polygon tp1 = (Polygon) p1.buffer(buffdist, 1);
		//p2 = (Polygon) GeometryHelper.expandPolygon(p2, buffdist);
		Polygon tp2 = (Polygon) p2.buffer(buffdist, 1);
		Geometry result = tp1.union(tp2);
		//result = GeometryHelper.expandPolygon((Polygon)result, -buffdist);
		result = (Polygon) result.buffer(-buffdist, 1);
		result = result.union(p1);
		result = result.union(p2);
		return result;
	}
	
	private Geometry sawPolygons(Polygon p1, Polygon p2) {
		Coordinate[] ca1 = p1.getCoordinates();
		Coordinate[] ca2 = p2.getCoordinates();
		GeometryFactory geomfact = new GeometryFactory();
		Coordinate[] segcoords = new Coordinate[2];
		segcoords[0] = new Coordinate(ca1[0]);
		segcoords[1] = new Coordinate(ca1[1]);
		LineString tls = geomfact.createLineString(segcoords);
		Geometry tgeom = tls.union(tls);
		for(int i=2; i<ca1.length; i++) {
			segcoords[0] = new Coordinate(ca1[i-1]);
			segcoords[1] = new Coordinate(ca1[i]);
			tls = geomfact.createLineString(segcoords);
			tgeom = tgeom.union(tls);
			segcoords[0] = null;
			segcoords[1] = null;
		}
		for(int i=1; i<ca2.length; i++) {
			segcoords[0] = new Coordinate(ca2[i-1]);
			segcoords[1] = new Coordinate(ca2[i]);
			tgeom = tgeom.union(geomfact.createLineString(segcoords));
		}
		return tgeom;
	}
	
	private Polygon sawPolygons(Polygon p1, Polygon p2, Coordinate c1, Coordinate c2) {
		Coordinate[] ca1 = p1.getCoordinates();
		Coordinate[] ca2 = p2.getCoordinates();
		ArrayList<Coordinate> newcoords = new ArrayList<Coordinate>();
		
		int i1 = getCoordinatePos(ca1, c1);
		int i2 = getCoordinatePos(ca2, c2);
		if(i1 == -1) {
			i1 = getCoordinatePos(ca1, c2);
			i2 = getCoordinatePos(ca2, c1);
		}
		
		int i;
		for(i=0; i<i1; i++) newcoords.add(ca1[i]);
		for(i=i2; i<ca2.length-1; i++) newcoords.add(ca2[i]);
		for(i=0; i<i2; i++) newcoords.add(ca2[i]);
		for(i=i1; i<ca1.length; i++) newcoords.add(ca1[i]);
		
		Coordinate[] newcoordarray = new Coordinate[newcoords.size()];
		for(i=0; i<newcoordarray.length; i++) newcoordarray[i] = (Coordinate)newcoords.get(i);
		
		GeometryFactory geomfact = new GeometryFactory();
		
		int hi = 0;
		LinearRing[] holes = new LinearRing[p1.getNumInteriorRing()+p2.getNumInteriorRing()];
		for(i=0; i<p1.getNumInteriorRing(); i++, hi++) holes[hi] = geomfact.createLinearRing(p1.getInteriorRingN(i).getCoordinates());
		for(i=0; i<p2.getNumInteriorRing(); i++, hi++) holes[hi] = geomfact.createLinearRing(p2.getInteriorRingN(i).getCoordinates());
		
		Polygon result = geomfact.createPolygon(geomfact.createLinearRing(newcoordarray), holes);
		return result;
	}
	
	private int getCoordinatePos(Coordinate[] coords, Coordinate c) {
		for(int i=0; i<coords.length; i++) {
			if(coords[i].equals2D(c)) return i;
		}
		return -1;
	}
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("AmalgamatePolygons", "neun", "support",
				"",
				"AmalgamatePolygons",
				"AmalgamatePolygons",
				"1.0",
				new String[] {"ica.genops.cartogen.Amalgamation"});
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("mindist", "DOUBLE", "10.0", "minimum distance");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "amalgamated polygons");
		return id;
	}
}
