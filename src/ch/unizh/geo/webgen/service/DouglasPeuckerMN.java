package ch.unizh.geo.webgen.service;

import java.util.HashMap;
import java.util.Iterator;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.task.TaskMonitor;

/*
 * Created on 11.01.2005
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
public class DouglasPeuckerMN extends AWebGenAlgorithm implements IWebGenAlgorithm  {
	
	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		double tolerance = wgreq.getParameterDouble("tolerance");
		FeatureCollection fcnew = dpc(fc, tolerance);
		wgreq.addResult("result", fcnew);
	}
	
	private FeatureCollection dpc(FeatureCollection fc, double tolerance) {
        FeatureCollection fcnew = new FeatureDataset(fc.getFeatureSchema());
        for (Iterator i = fc.iterator(); i.hasNext();) {
            Feature f = (Feature)((Feature)i.next()).clone();
            f.setGeometry(dp(f.getGeometry(), tolerance));
            fcnew.add(f);
        }
        return fcnew;
    }
    
    private Geometry dp(Geometry geom, double tolerance) {
    	GeometryFactory geof = new GeometryFactory();
    	if(geom instanceof LineString) {
    		return dpLineString((LineString)geom, geof, tolerance);
        }
        else if(geom instanceof Polygon) {
        	return dpPolygon((Polygon)geom, geof, tolerance);
        }
        else if(geom instanceof MultiPolygon) {
            MultiPolygon mpoly = (MultiPolygon)geom;
            Polygon[] polys = new Polygon[mpoly.getNumGeometries()];
            for(int numi=0; numi<mpoly.getNumGeometries(); numi++) {
                polys[numi] = dpPolygon((Polygon)mpoly.getGeometryN(numi), geof, tolerance);
            }
            return geof.createMultiPolygon(polys);
        }
        else if(geom instanceof MultiLineString) {
        	MultiLineString mline = (MultiLineString)geom;
        	LineString[] lines = new LineString[mline.getNumGeometries()];
            for(int numi=0; numi<mline.getNumGeometries(); numi++) {
            	lines[numi] = dpLineString((LineString)mline.getGeometryN(numi), geof, tolerance);
            }
            return geof.createMultiLineString(lines);
        }
        else {
        	return (Geometry)geom.clone();
        }
     }
    
    
    private Polygon dpPolygon(Polygon poly, GeometryFactory geof, double tolerance) {
    	LineString ering = poly.getExteriorRing();
    	CoordinateList ercl = new CoordinateList();
    	ercl.add(ering.getCoordinates(), true);
    	GNdp_simplification(ercl, tolerance);
    	
    	LinearRing[] irings = new LinearRing[poly.getNumInteriorRing()];
        for(int numi=0; numi<poly.getNumInteriorRing(); numi++) {
        	LineString iring = poly.getInteriorRingN(numi);
        	CoordinateList ircl = new CoordinateList();
        	ircl.add(iring.getCoordinates(), true);
        	GNdp_simplification(ircl, tolerance);
        	irings[numi] = geof.createLinearRing(ircl.toCoordinateArray());
        }
        return geof.createPolygon(geof.createLinearRing(ercl.toCoordinateArray()), irings);
    }
    
    private LineString dpLineString(LineString line, GeometryFactory geof, double tolerance) {
    	CoordinateList dpCoordinateList = new CoordinateList();
        dpCoordinateList.add(line.getCoordinates(), true);
        GNdp_simplification(dpCoordinateList, tolerance);
        return geof.createLineString(dpCoordinateList.toCoordinateArray());
    }
    
    
    /*private FeatureCollection jtsdp(TaskMonitor monitor, FeatureCollection fc, double tolerance) {
        monitor.allowCancellationRequests();
        monitor.report("Computing Douglas Peucker...");

        GeometryFactory geof = new GeometryFactory();
        FeatureCollection fcnew = new FeatureDataset(fc.getFeatureSchema());
        
        Geometry currDP = null;
        int size = fc.size();
        int count = 1;

        for (Iterator i = fc.iterator(); i.hasNext();) {
            Feature f = (Feature)((Feature)i.next()).clone();
            currDP = DouglasPeuckerSimplifier.simplify(f.getGeometry(), tolerance);
            f.setGeometry(currDP);
            fcnew.add(f);
            monitor.report(count++, size, "features");
        }
        return fcnew;
    }*/
    
    public static int GNdp_simplification(CoordinateList polyLine, double abweichung) {
		int dp[] = new int[polyLine.size()];
		int deletedPoints = 0;		
		//Coordinate p1  = new Coordinate(0.0, 0.0);
		//Coordinate p2  = new Coordinate(0.0, 0.0);
		//Coordinate pos  = new Coordinate(0.0, 0.0);
		
		int	dp_a = 0, 
			dp_e = 0;
		int	dp_r = 0, 
			dp_akt = 0;
			
		// 1. Anfangs- und Endpunkt sind Douglas-Peucker-Punkte (dp)
		dp_a = 0;
		dp_e = polyLine.size()-1; 

		dp_akt = dp_a;
		dp[dp_a] = 1; 

		// 2. Einfuegen weiterer dp's, solange Punkte ausserhalb eines
		//    vorgegebenen Mindestabstand zu dp-linien liegen */
		while(dp_a != dp_e) {
			  dp_r = dp_e;
			  while(dp_akt != dp_r) {
			    dp_akt = dp_r;
			    /*
				 * Suche Stuetzstelle der Linie, die am weitesten von den beiden
				 * Randpunkten entfernt ist. Funktion, gibt deren Index zurueck,
				 * solange diese ausserhalb von 'abweichung' liegt, ansonsten
				 * wird dp_akt zurueckgegeben -> Abbruch
				 */
			    int	i_a = dp_a;
			    int i_e = dp_akt;
			    double dist = abweichung;			    
			    double	max_dist = 0.0;
			    double	akt_dist = 0.0;  	  
			    
			    int i_akt = i_e;
			    for(int i = i_a+1; i<i_e; i++)	{
			    	LineSegment ls = new LineSegment(polyLine.getCoordinate(i_a), polyLine.getCoordinate(i_e));
			    	akt_dist = ls.distance(polyLine.getCoordinate(i));
			    	if(akt_dist > max_dist)	{
			    		max_dist  = akt_dist;
			    		if(max_dist > dist) i_akt = i;
			    	}	
			    }
			    dp_r = i_akt;
			  }    
			  dp_a = dp_r;      
			  dp[dp_a] = 1;  
		}	

		// 3. Uebergabe der Koordinaten
		deletedPoints = 0;
		for(int i=0; i<polyLine.size(); i++) {
			if(dp[i+deletedPoints] == 0) {
				polyLine.remove(i);
				deletedPoints++;
				i--;
			} 
		}
		
		return(deletedPoints);
	}
    
    public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("DouglasPeuckerMN", "neun", "support",
				"",
				"DouglasPeuckerMN",
				"Douglas-Peucker algorithm for Lines and Polygons",
				"1.0");
		
		//add input parameters
		String[] allowed = {"LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries to buffer");
		id.addInputParameter("tolerance", "DOUBLE", "20.0", "dp tolerance");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
