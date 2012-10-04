package ch.unizh.geo.webgen.service;

import graph.GraphEdge;
import graph.ProxyGraphWebGen;

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
import ch.unizh.geo.webgen.tools.GeometryHelper;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;


public class DisplaceFeaturesFastConstrained extends AWebGenAlgorithm implements IWebGenAlgorithm  {
	
	public void run(WebGenRequest wgreq) {
		FeatureCollection fcA = wgreq.getFeatureCollection("geom");
		FeatureCollection fcC = wgreq.getFeatureCollection("congeom");
		double mindist = wgreq.getParameterDouble("mindist");
		double roaddist = wgreq.getParameterDouble("roaddist");
		FeatureCollection fcnew = displace(fcA, fcC, mindist, roaddist);
		wgreq.addResult("result", fcnew);
	}
		
	public FeatureCollection displace(FeatureCollection fc, FeatureCollection streets, double mindist, double roaddist) {
		//adding buildings to the working collection
		ConstrainedFeatureCollection fcnew;
		if(!(fc instanceof ConstrainedFeatureCollection)) fcnew = new ConstrainedFeatureCollection(fc, true);
		else fcnew = (ConstrainedFeatureCollection) fc;
		FeatureSchema fcs = fcnew.getFeatureSchema();
		ConstrainedFeatureCollection fcstreets = new ConstrainedFeatureCollection(fcs);
		
		//adding streets to the working collection
		GeometryFactory geomfact = new GeometryFactory();
		LineString[] streetlines = new LineString[streets.size()];
		int streetcount=0;
		Iterator fciter = streets.iterator();
		while (fciter.hasNext()) {
			Geometry tgeom = ((Feature) fciter.next()).getGeometry();
			if(tgeom instanceof LineString) {
				ConstrainedFeature tfnew = new ConstrainedFeature(fcs);
				tfnew.setGeometry(tgeom);
				//tfnew.initConstraint(); // !!!!!!!!!!!
				fcnew.add(tfnew);
				fcstreets.add(tfnew);
				streetlines[streetcount] = (LineString)tgeom;
			}
			else if(tgeom instanceof Polygon) {
				Polygon tpoly = (Polygon) tgeom;
				double buffdist = mindist - roaddist;
				if(buffdist != 0) tpoly = (Polygon)tpoly.buffer(buffdist);
				ConstrainedFeature tfnew = new ConstrainedFeature(fcs);
				tfnew.setGeometry(tpoly.getExteriorRing());
				//tfnew.initConstraint(); // !!!!!!!!!!!!!!
				fcnew.add(tfnew);
				fcstreets.add(tfnew);
				streetlines[streetcount] = new LineString(tpoly.getExteriorRing().getCoordinateSequence(), geomfact);
			}
			streetcount++;
		}
		MultiLineString streetline = new MultiLineString(streetlines, geomfact);
		
		boolean haszerodist = false;
		int iterations = 0;
		int maxiterations = fcnew.size() * 5; // iteration number dependent on
												// feature count
		if(maxiterations > 400)	maxiterations = 400;
		//System.out.println("Iterating through displacement " + maxiterations + "times ...");
		try {
			ProxyGraphWebGen pg = new ProxyGraphWebGen();
			Collection obstructFs = new ArrayList();
			if(mindist > roaddist) pg.ProxyGraphPopulate(fcnew.getFeatures(), obstructFs, mindist);
			else pg.ProxyGraphPopulate(fcnew.getFeatures(), obstructFs, roaddist);
			while (iterations < maxiterations) {
				haszerodist = false;
				if (pg.edges.size() == 0)
					iterations = maxiterations;
				//System.out.print("iteration " + iterations);
				for (int i = 0; i < pg.edges.size(); i++) {
					GraphEdge te = (GraphEdge) pg.edges.get(i);
					//System.out.println("weight: " + te.weight);
					if (te.weight < mindist) {
						Feature nf1 = te.node1.rwo;
						Feature nf2 = te.node2.rwo;
						// move buildings
						double fact4move = 1 - (te.weight / mindist);
						//System.out.println("fact4move: " + fact4move);
						Geometry ng1 = (Geometry) nf1.getGeometry().clone();
						Coordinate nc1 = ng1.getCentroid().getCoordinate();
						Geometry ng2 = (Geometry) nf2.getGeometry().clone();
						Coordinate nc2 = ng2.getCentroid().getCoordinate();
						Coordinate newposn1, newposn2;
						
						if((ng1 instanceof LineString) && (ng2 instanceof LineString)) {
							// do nothing
						}
						else if(ng1 instanceof LineString) {
							pg.ProxyGraphRemoveFeature(nf2);
							if(te.weight == 0.0) {
								double x12dist = te.node1.x - te.node2.x;
								double y12dist = te.node1.y - te.node2.y;
								double n1n2dist = Math.abs(Math.sqrt((x12dist*x12dist)+ (y12dist*y12dist)));
								double fact4move2 = mindist / n1n2dist;
								newposn2 = calcTransWR(nc2, (te.node2.x - te.node1.x) * fact4move2, (te.node2.y - te.node1.y) * fact4move2);
								haszerodist = true;
							}
							else {
								newposn2 = calcTransWR(nc2, (te.x2-te.x1)*fact4move, (te.y2-te.y1)*fact4move);
							}
							Geometry newg2 = moveGeometry(ng2, newposn2);
							nf2.setGeometry(newg2);
							pg.ProxyGraphAddFeature(nf2);
						}
						else if(ng2 instanceof LineString) {
							pg.ProxyGraphRemoveFeature(nf1);
							if(te.weight == 0.0) {
								double x12dist = te.node1.x - te.node2.x;
								double y12dist = te.node1.y - te.node2.y;
								double n1n2dist = Math.abs(Math.sqrt((x12dist*x12dist)+ (y12dist*y12dist)));
								double fact4move2 = mindist / n1n2dist;
								newposn1 = calcTransWR(nc1, x12dist * fact4move2, y12dist * fact4move2);
								haszerodist = true;
							}
							else {
								newposn1 = calcTransWR(nc1, (te.x1-te.x2)*fact4move, (te.y1-te.y2)*fact4move);
							}
							Geometry newg1 = moveGeometry(ng1, newposn1);
							nf1.setGeometry(newg1);
							pg.ProxyGraphAddFeature(nf1);
						}
						else {
							//if both features are buildings
							//remove nodes
							pg.ProxyGraphRemoveFeature(nf1);
							pg.ProxyGraphRemoveFeature(nf2);
							//move buildings
							if(te.weight == 0.0) {
								double x12dist = te.node1.x - te.node2.x;
								double y12dist = te.node1.y - te.node2.y;
								double n1n2dist = Math.abs(Math.sqrt((x12dist*x12dist)+ (y12dist*y12dist)));
								double fact4move2 = mindist / n1n2dist;
								newposn1 = calcTrans(nc1, x12dist * fact4move2, y12dist * fact4move2);
								newposn2 = calcTrans(nc2, (te.node2.x - te.node1.x) * fact4move2, (te.node2.y - te.node1.y) * fact4move2);
								haszerodist = true;
							}
							else {
								newposn1 = calcTrans(nc1, (te.x1 - te.x2) * fact4move, (te.y1 - te.y2) * fact4move);
								newposn2 = calcTrans(nc2, (te.x2 - te.x1) * fact4move, (te.y2 - te.y1) * fact4move);
							}
							Geometry newg1 = moveGeometry(ng1, newposn1);
							if(!newg1.intersects(streetline))
								nf1.setGeometry(newg1);
							Geometry newg2 = moveGeometry(ng2, newposn2);
							if(!newg2.intersects(streetline))
								nf2.setGeometry(newg2);
							//add new nodes
							pg.ProxyGraphAddFeature(nf1);
							pg.ProxyGraphAddFeature(nf2);
						}
						//System.out.println(nf1.getAttribute("constraint"));
					}
				}
				iterations++;
				// System.out.print("\fIteration " + iterations + "/" +
				// maxiterations);
				//System.out.print("|");
				if (iterations > maxiterations) break;
				//if (!haszerodist) break;
			}
			
			int unsolved = 0; double unsolvedSmallerAvg = 0;
			for (int i = 0; i < pg.edges.size(); i++) {
				double smallerAsMindist = mindist - ((GraphEdge) pg.edges.get(i)).weight;
				if (smallerAsMindist > 0) {
					unsolvedSmallerAvg += smallerAsMindist;
					unsolved++;
				}
			}
			unsolvedSmallerAvg /= unsolved;
			//System.out.println("displacement unsolved " + unsolved + "/" + pg.edges.size() + " " + Math.round((unsolvedSmallerAvg/mindist)*100) + "% smaller");
			//FeatureCollection myCollC = FeatureDatasetFactory.createFromGeometry(pg.getEdgesAsLineStrings());
			//fcnew.addAll(myCollC.getFeatures());
		}
		catch (Exception e) {
			e.printStackTrace();
			}
//		System.out.print("\n");
		//System.out.println("\nhaszerodist: "+haszerodist);
		fcnew.removeAll(fcstreets.getFeatures());
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
	
	public Coordinate calcTransWR(Coordinate oldcoord, double xdist, double ydist) {
		Coordinate newcoord = (Coordinate)oldcoord.clone();
		newcoord.x += (xdist);
		newcoord.y += (ydist);
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
		InterfaceDescription id = new InterfaceDescription("DisplaceFeaturesFastConstrained", "neun", "operator",
				"",
				"DisplaceFeaturesFastConstrained",
				"displace features to minimum distance and care about distance to constraining features such as roads",
				"1.0");
		
		// id.visible = true;
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("congeom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with constraining geometries such as roads");
		id.addInputParameter("mindist", "DOUBLE", "10.0", "minimum distance between buildings");
		id.addInputParameter("roaddist", "DOUBLE", "0.0", "minimum distance to constraining features such as roads");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
