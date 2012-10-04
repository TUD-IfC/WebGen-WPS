package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import madge.structures.Graph.GraphEdge;
import madge.structures.Graph.GraphNode;
import madge.structures.Graph.ProxyGraphWebGen;
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


public class DisplaceConstrainedNew extends AWebGenAlgorithm implements IWebGenAlgorithm  {
	
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
			ConstrainedFeature tfnew = new ConstrainedFeature(fcs);
			LineString tline = null;
			if(tgeom instanceof LineString) {
				tline = (LineString) tgeom;
			}
			else if(tgeom instanceof Polygon) {
				tline = new LineString(((Polygon)tgeom).getExteriorRing().getCoordinateSequence(), geomfact);
			}
			if(tline != null) {
				tfnew.setGeometry(tline);
				fcnew.add(tfnew);
				fcstreets.add(tfnew);
				streetlines[streetcount] = tline;
				streetcount++;
			}
		}
		MultiLineString streetline = new MultiLineString(streetlines, geomfact);
		
		int iterations = 0;
		int maxiterations = fcnew.size() * 5; // iteration number dependent on feature count
		if(maxiterations > 400)	maxiterations = 400;
		//System.out.println("Iterating through displacement " + maxiterations + "times ...");
		try {
			ProxyGraphWebGen pg = new ProxyGraphWebGen();
			Collection obstructFs = new ArrayList();
			if(mindist > roaddist) pg.ProxyGraphPopulate(fcnew.getFeatures(), obstructFs, mindist);
			else pg.ProxyGraphPopulate(fcnew.getFeatures(), obstructFs, roaddist);
			while (iterations < maxiterations) {
				if (pg.edges.size() == 0)
					iterations = maxiterations;
				//System.out.print("iteration " + iterations);
				for (int i = 0; i < pg.edges.size(); i++) {
					GraphEdge te = (GraphEdge) pg.edges.get(i);
					Feature nf1 = te.node1.rwo;
					Feature nf2 = te.node2.rwo;
					Geometry g1 = nf1.getGeometry();
					Geometry g2 = nf2.getGeometry();
					
					if((g1 instanceof LineString) && (g2 instanceof LineString)) {
						//do nothing if both are lines
					}
					else if(g2 instanceof LineString) { //resolve a close or overlaping polygon on a line
						Geometry ng1 = (Geometry) g1.clone();
						if(g1.intersects(g2)) {
							pg.ProxyGraphRemoveFeature(nf1);
							Geometry newg1 = solveLineOverlap(ng1, g2);
							nf1.setGeometry(newg1);
							pg.ProxyGraphAddFeature(nf1);
						}
						else if (te.weight < roaddist) {
							pg.ProxyGraphRemoveFeature(nf1);
							Geometry newg1 = solveLine2Close(te.node2, te.node1, te.x2, te.x1, te.y2, te.y1, ng1, te.weight, roaddist);
							nf1.setGeometry(newg1);
							pg.ProxyGraphAddFeature(nf1);
						}
					}
					else if(g1 instanceof LineString) { //resolve a close or overlaping polygon on a line
						Geometry ng2 = (Geometry) g2.clone();
						if(g1.intersects(g2)) {
							pg.ProxyGraphRemoveFeature(nf2);
							Geometry newg2 = solveLineOverlap(ng2, g1);
							nf2.setGeometry(newg2);
							pg.ProxyGraphAddFeature(nf2);
						}
						else if (te.weight < roaddist) {
							pg.ProxyGraphRemoveFeature(nf2);
							Geometry newg2 = solveLine2Close(te.node1, te.node2, te.x1, te.x2, te.y1, te.y2, ng2, te.weight, roaddist);
							nf2.setGeometry(newg2);
							pg.ProxyGraphAddFeature(nf2);
						}
					}
					else { //resolve two close or overlaping polygons
						Geometry ng1 = (Geometry) g1.clone();
						Geometry ng2 = (Geometry) g2.clone();
						if(g1.intersects(g2)) {
							pg.ProxyGraphRemoveFeature(nf1);
							pg.ProxyGraphRemoveFeature(nf2);
							Coordinate newpos1; Coordinate newpos2;
							Geometry newg1 = ng1;
							Geometry newg2 = ng2;
							int ti = 0;
							while(true) {
								try { if(!newg1.intersects(newg2)) break; }
								catch(Exception e) { break; }
								Point cent1 = newg1.getCentroid();
								Point cent2 = newg2.getCentroid();
								double xdist = cent1.getX() - cent2.getX();
								double ydist = cent1.getY() - cent2.getY();
								newpos1 = calcTransWR(cent1.getCoordinate(), xdist*0.1, ydist*0.1);
								newpos2 = calcTransWR(cent2.getCoordinate(), xdist*-0.1, ydist*-0.1);
								newg1 = moveGeometry(newg1, newpos1);
								newg2 = moveGeometry(newg2, newpos2);
								ti++;
								if(ti == 200) break;
							}
							nf1.setGeometry(newg1);
							nf2.setGeometry(newg2);
							pg.ProxyGraphAddFeature(nf1);
							pg.ProxyGraphAddFeature(nf2);
						}
						else if (te.weight < mindist) {
							pg.ProxyGraphRemoveFeature(nf1);
							pg.ProxyGraphRemoveFeature(nf2);
							//move buildings
							Coordinate newposn1, newposn2;
							Coordinate nc1 = ng1.getCentroid().getCoordinate();
							Coordinate nc2 = ng2.getCentroid().getCoordinate();
							if(te.weight == 0.0) {
								double x12dist = te.node1.x - te.node2.x;
								double y12dist = te.node1.y - te.node2.y;
								double n1n2dist = Math.abs(Math.sqrt((x12dist*x12dist)+ (y12dist*y12dist)));
								double fact4move2 = mindist / n1n2dist;
								newposn1 = calcTrans(nc1, x12dist * fact4move2, y12dist * fact4move2);
								newposn2 = calcTrans(nc2, (te.node2.x - te.node1.x) * fact4move2, (te.node2.y - te.node1.y) * fact4move2);
							}
							else {
								double fact4move = 1 - (te.weight / mindist);
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
					}
				}
				iterations++;
				//System.out.println("Iteration " + iterations + "/" + maxiterations);
				//System.out.print("|");
				if (iterations > maxiterations) break;
				//if (!haszerodist) break;
			}
			
			/*int unsolved = 0; double unsolvedSmallerAvg = 0;
			for (int i = 0; i < pg.edges.size(); i++) {
				double smallerAsMindist = mindist - ((GraphEdge) pg.edges.get(i)).weight;
				if (smallerAsMindist > 0) {
					unsolvedSmallerAvg += smallerAsMindist;
					unsolved++;
				}
			}
			unsolvedSmallerAvg /= unsolved;
			System.out.println("displacement unsolved " + unsolved + "/" + pg.edges.size() + " " + Math.round((unsolvedSmallerAvg/mindist)*100) + "% smaller");*/
			//FeatureCollection myCollC = FeatureDatasetFactory.createFromGeometry(pg.getEdgesAsLineStrings());
			//fcnew.addAll(myCollC.getFeatures());
		}
		catch (Exception e) {e.printStackTrace();}
		fcnew.removeAll(fcstreets.getFeatures());
		return fcnew;
	}
	
	public Coordinate calcTrans(Coordinate oldcoord, double xdist, double ydist) {
		Coordinate newcoord = (Coordinate)oldcoord.clone();
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
	
	public Geometry solveLineOverlap(Geometry feat, Geometry linefeat) {
		Coordinate newpos;
		Geometry newg1 = feat;
		Geometry ng2section;
		int ti=0;
		while(true) {
			ng2section = newg1.intersection(linefeat);
			if(ng2section.getNumGeometries() == 0) break;
			Point cent1 = newg1.getCentroid();
			Point cent2 = ng2section.getCentroid();
			double xdist = cent1.getX() - cent2.getX();
			double ydist = cent1.getY() - cent2.getY();
			newpos = calcTransWR(cent1.getCoordinate(), xdist*0.3, ydist*0.3);
			newg1 = moveGeometry(newg1, newpos);
			ti++;
			if(ti == 200) break;
		}
		return newg1;
	}
	
	public Geometry solveLine2Close(GraphNode n1, GraphNode n2, double x1, double x2, double y1, double y2,
			Geometry fg, double weight, double roaddist) {
		Coordinate newpos;
		if(weight == 0.0) {
			double x12dist = n1.x - n2.x;
			double y12dist = n1.y - n2.y;
			double n1n2dist = Math.abs(Math.sqrt((x12dist*x12dist)+ (y12dist*y12dist)));
			double fact4move2 = roaddist / n1n2dist;
			newpos = calcTransWR(fg.getCentroid().getCoordinate(), (n2.x - n1.x) * fact4move2, (n2.y - n1.y) * fact4move2);
		}
		else {
			double fact4move = 1 - (weight / roaddist);
			newpos = calcTransWR(fg.getCentroid().getCoordinate(), (x2-x1)*fact4move, (y2-y1)*fact4move);
		}
		return moveGeometry(fg, newpos);
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
		InterfaceDescription id = new InterfaceDescription("DisplaceConstrainedNew", "neun", "operator",
				"",
				"DisplaceConstrainedNew",
				"displace features to minimum distance and care about distance to constraining features such as roads",
				"1.0");
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("congeom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with constraining geometries such as roads");
		id.addInputParameter("mindist", "DOUBLE", "10.0", "minimum distance between buildings");
		id.addInputParameter("roaddist", "DOUBLE", "5.0", "minimum distance to constraining features such as roads");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
