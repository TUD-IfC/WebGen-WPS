package ch.unizh.geo.webgen.xml;

import java.util.List;
import java.util.StringTokenizer;

import org.dom4j.Element;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/*
 * Created on 20.07.2005
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
public class GMLGeometryParser {
	
	GeometryFactory geometryFactory = new GeometryFactory();
	
	public GMLGeometryParser() {
		//xr = new org.apache.xerces.parsers.SAXParser();
	}
	
	public Geometry read(Element gnode) {
		Geometry result = null;
		try {
			String gtype = gnode.getName();
			if(gtype.compareToIgnoreCase("point") == 0) result = readPoint(gnode);
			if(gtype.compareToIgnoreCase("linestring") == 0) result = readLineString(gnode);
			if(gtype.compareToIgnoreCase("linearring") == 0) result = readLinearRing(gnode);
			if(gtype.compareToIgnoreCase("polygon") == 0) result = readPolygon(gnode);
			if(gtype.compareToIgnoreCase("multipoint") == 0) result = readMultiPoint(gnode);
			if(gtype.compareToIgnoreCase("multilinestring") == 0) result = readMultiLineString(gnode);
			if(gtype.compareToIgnoreCase("multipolygon") == 0) result = readMultiPolygon(gnode);
			if(gtype.compareToIgnoreCase("multigeometry") == 0) result = readMultiGeometry(gnode);
			//System.out.println(result.toString());
		}
		catch (NullPointerException e) {
			result = geometryFactory.createGeometryCollection(new Geometry[0]);
		}
		return result;
	}
	
	public Point readPoint(Element gnode) {
		Point result = null;
		String coordstr = gnode.elementText("coordinates");
		Coordinate[] coordlist = parseCoordinates(coordstr, geometryFactory);
		result = geometryFactory.createPoint(coordlist[0]);
		return result;
	}
	
	public LineString readLineString(Element gnode) {
		LineString result = null;
		String coordstr = gnode.elementText("coordinates");
		Coordinate[] coordlist = parseCoordinates(coordstr, geometryFactory);
		result = geometryFactory.createLineString(coordlist);
		return result;
	}
	
	public LinearRing readLinearRing(Element gnode) {
		LinearRing result = null;
		String coordstr = gnode.elementText("coordinates");
		Coordinate[] coordlist = parseCoordinates(coordstr, geometryFactory);
		result = geometryFactory.createLinearRing(coordlist);
		return result;
	}
	
	public Polygon readPolygon(Element gnode) {
		Polygon result = null;
		//extract shell
		Element shellel = gnode.element("outerBoundaryIs");
		LinearRing shell = readLinearRing(shellel.element("LinearRing"));
		//extract holes
		List holelist = gnode.elements("innerBoundaryIs");
		int nbholes = holelist.size();
		LinearRing[] holes = null;
		if(nbholes > 0) {
			holes = new LinearRing[holelist.size()];
			for(int i=0; i < nbholes; i++) {
				holes[i] = readLinearRing(((Element)holelist.get(i)).element("LinearRing"));
			}
		}
		result = geometryFactory.createPolygon(shell, holes);
		return result;
	}
	
	
	public MultiPoint readMultiPoint(Element gnode) {
		MultiPoint result = null;
		List pointmembers = gnode.elements();
		int pointnum = pointmembers.size();
		Point[] pointarray = new Point[pointnum];
		for(int i=0; i<pointnum; i++) {
			Element pointgeo = ((Element)pointmembers.get(i)).element("Point");
			pointarray[i] = readPoint(pointgeo);
		}
		result = geometryFactory.createMultiPoint(pointarray);
		return result;
	}
	
	public MultiLineString readMultiLineString(Element gnode) {
		MultiLineString result = null;
		List linestringmembers = gnode.elements();
		int linestringnum = linestringmembers.size();
		LineString[] linestringarray = new LineString[linestringnum];
		for(int i=0; i<linestringnum; i++) {
			Element linestringgeo = ((Element)linestringmembers.get(i)).element("LineString");
			linestringarray[i] = readLineString(linestringgeo);
		}
		result = geometryFactory.createMultiLineString(linestringarray);
		return result;
	}
	
	public MultiPolygon readMultiPolygon(Element gnode) {
		MultiPolygon result = null;
		List polygonmembers = gnode.elements();
		int polygonnum = polygonmembers.size();
		Polygon[] polygonarray = new Polygon[polygonnum];
		for(int i=0; i<polygonnum; i++) {
			Element polygongeo = ((Element)polygonmembers.get(i)).element("Polygon");
			polygonarray[i] = readPolygon(polygongeo);
		}
		result = geometryFactory.createMultiPolygon(polygonarray);
		return result;
	}
	
	public GeometryCollection readMultiGeometry(Element gnode) {
		GeometryCollection result = null;
		List geometrymembers = gnode.elements();
		int geometrynum = geometrymembers.size();
		Geometry[] geometryarray = new Polygon[geometrynum];
		for(int i=0; i<geometrynum; i++) {
			Element geometrygeo = (Element)((Element)geometrymembers.get(i)).elementIterator().next();
			geometryarray[i] = read(geometrygeo);
		}
		result = geometryFactory.createGeometryCollection(geometryarray);
		return result;
	}
	
    
    
    /**
     *  Parse a bunch of points - stick them in pointList. Handles 2d and 3d.
     *
     *@param  ptString         string containing a bunch of coordinates
     *@param  geometryFactory  JTS point/coordinate factory
     */
    private Coordinate[] parseCoordinates(String ptString, GeometryFactory geometryFactory) {
    	//ArrayList pointList = new ArrayList(); // list of accumulated points (Coordinate)
    	String aPoint;
        StringTokenizer stokenizerPoint;
        Coordinate coord = new Coordinate();
        int dim;
        String numb;
        StringBuffer sb;
        int t;
        char ch;

        //remove \n and \r and replace with spaces
        sb = new StringBuffer(ptString);

        for (t = 0; t < sb.length(); t++) {
            ch = sb.charAt(t);

            if ((ch == '\n') || (ch == '\r')) {
                sb.setCharAt(t, ' ');
            }
        }

        StringTokenizer stokenizer = new StringTokenizer(new String(sb), " ", false);
        
        Coordinate[] coordarray = new Coordinate[stokenizer.countTokens()];
        int ic = 0;

        while (stokenizer.hasMoreElements()) {
            //have a point in memory - handle the single point
            aPoint = stokenizer.nextToken();
            stokenizerPoint = new StringTokenizer(aPoint, ",", false);
            coord.x = coord.y = coord.z = Double.NaN;
            dim = 0;

            while (stokenizerPoint.hasMoreElements()) {
                numb = stokenizerPoint.nextToken();

                if (dim == 0) {
                    coord.x = Double.parseDouble(numb);
                } else if (dim == 1) {
                    coord.y = Double.parseDouble(numb);
                } else if (dim == 3) {
                    coord.z = Double.parseDouble(numb);
                }

                dim++;
            }

            //pointList.add(coord); //remember it
            coordarray[ic] = coord;
            ic++;
            coord = new Coordinate();
            stokenizerPoint = null;
        }
        
        //return pointList;
        return coordarray;
    }

}
