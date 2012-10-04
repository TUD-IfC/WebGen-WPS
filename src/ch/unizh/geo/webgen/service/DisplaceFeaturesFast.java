package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import madge.structures.Graph.GraphEdge;
import madge.structures.Graph.ProxyGraphWebGen;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.tools.GeometryHelper;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureUtil;


public class DisplaceFeaturesFast extends AWebGenAlgorithm implements IWebGenAlgorithm  {
	
	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		double mindist = wgreq.getParameterDouble("mindist");
		ConstrainedFeatureCollection fcnew = displace(fc, mindist);
		wgreq.addResult("result", fcnew);
	}
		
	public ConstrainedFeatureCollection displace(FeatureCollection fc, double mindist) {
		ConstrainedFeatureCollection fcnew;
		if(fc instanceof ConstrainedFeatureCollection) fcnew = (ConstrainedFeatureCollection)fc;
		else fcnew = new ConstrainedFeatureCollection(fc, true);
		
		int iterations = 0;
		int maxiterations = fcnew.size() * 10; // iteration number dependent on
												// feature count
		if(maxiterations > 400)	maxiterations = 400;
//		System.out.println("Iterating displacement " + maxiterations + "times: ");
		try {
			ProxyGraphWebGen pg = new ProxyGraphWebGen();
			Collection obstructFs = new ArrayList();
			pg.ProxyGraphPopulate(fcnew.getFeatures(), obstructFs, mindist);
			while (iterations < maxiterations) {
				if (pg.edges.size() == 0)
					iterations = maxiterations;
				//System.out.print("iteration " + iterations);
				for (int i = 0; i < pg.edges.size(); i++) {
					GraphEdge te = (GraphEdge) pg.edges.get(i);
					//System.out.println("weight: " + te.weight);
					if (te.weight < mindist) {
						Feature nf1 = te.node1.rwo;
						Feature nf2 = te.node2.rwo;
						//remove nodes
						pg.ProxyGraphRemoveFeature(nf1);
						pg.ProxyGraphRemoveFeature(nf2);
						// move buildings
						// double dist2move = (mindist-te.weight)+0.0001;
						double fact4move = 1 - (te.weight / mindist);
						//System.out.println("fact4move: " + fact4move);
						Geometry ng1 = (Geometry) nf1.getGeometry().clone();
						Coordinate nc1 = ng1.getCentroid().getCoordinate();
						Geometry ng2 = (Geometry) nf2.getGeometry().clone();
						Coordinate nc2 = ng2.getCentroid().getCoordinate();
						Coordinate newposn1, newposn2;
						if(te.weight == 0.0) {
							double x12dist = te.node1.x - te.node2.x;
							double y12dist = te.node1.y - te.node2.y;
							double n1n2dist = Math.abs(Math.sqrt((x12dist*x12dist)+ (y12dist*y12dist)));
							double fact4move2 = mindist / n1n2dist;
							newposn1 = calcTrans(nc1, x12dist * fact4move2, y12dist * fact4move2);
							newposn2 = calcTrans(nc2, (te.node2.x - te.node1.x) * fact4move2, (te.node2.y - te.node1.y) * fact4move2);
						}
						else {
							newposn1 = calcTrans(nc1, (te.x1 - te.x2) * fact4move, (te.y1 - te.y2) * fact4move);
							newposn2 = calcTrans(nc2, (te.x2 - te.x1) * fact4move, (te.y2 - te.y1) * fact4move);
						}
						Geometry newg1 = moveGeometry(ng1, newposn1);
						nf1.setGeometry(newg1);
						Geometry newg2 = moveGeometry(ng2, newposn2);
						nf2.setGeometry(newg2);
						
						//add new nodes
						pg.ProxyGraphAddFeature(nf1);
						pg.ProxyGraphAddFeature(nf2);
					}
				}
				iterations++;
				// System.out.print("\fIteration " + iterations + "/" +
				// maxiterations);
//				System.out.print("|");
				if (iterations > maxiterations) break;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			}
//		System.out.print("\n");
		return fcnew;
	}
	
	public Coordinate calcTrans(Coordinate oldcoord, double xdist, double ydist) {
		Coordinate newcoord = (Coordinate)oldcoord.clone();
		//double xdist = newcoord.x - awayfrom.x;
		//double ydist = newcoord.y - awayfrom.y;
		newcoord.x += (xdist/2);
		newcoord.y += (ydist/2);
		return newcoord;
	}
	
	public Geometry moveGeometry(Geometry oldgeom, Coordinate newcenter) {
		//newcoord.x -=  sp_x;
		//newcoord.y -=  sp_y;
		//newcoord.x = 0-newcoord.x;
		//newcoord.y = 0-newcoord.y;
		Coordinate spTrans =(Coordinate)oldgeom.getCentroid().getCoordinate().clone();
		spTrans.x -=  newcenter.x;
		spTrans.y -=  newcenter.y;
		spTrans.x = 0-spTrans.x;
		spTrans.y = 0-spTrans.y;
		if (oldgeom instanceof Polygon) {
			// verschieben:
			Polygon polygon = (Polygon)oldgeom;
			polygon = GeometryHelper.translatePolygon(polygon, spTrans);
			// Grösse anpassen wobei schwarz-weiss-Verhältnis beibehalten wird
			return polygon;
		} else if (oldgeom instanceof LineString) {
			// verschieben:
			LineString lineString = (LineString)oldgeom;
			lineString = (LineString)GeometryHelper.translateLineString(lineString, spTrans);
			return lineString;
		} else if (oldgeom instanceof Point) {
			// verschieben:
			Point point = (Point)oldgeom;
			point = (Point)GeometryHelper.translatePoint(point, spTrans);
			return point;
		} else {
			return oldgeom;
		}
	}

	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("DisplaceFeaturesFast", "neun", "operator",
				"",
				"DisplaceFeaturesFast",
				"displace features to minimum distance",
				"1.0",
				new String[] {"ica.genops.cartogen.Displacement"});
		
		// id.visible = true;
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries to buffer");
		id.addInputParameter("mindist", "DOUBLE", "10.0", "minimum distance");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "displaced geometries");
		return id;
	}
}
