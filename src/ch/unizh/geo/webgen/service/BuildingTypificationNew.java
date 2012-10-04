package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import triangulation.TEdge;
import triangulation.TNode;
import triangulation.TWeightedEdge;
import triangulation.TriangulationException;
import triangulation.TriangulationStringIDs;
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
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;


	public class BuildingTypificationNew extends AWebGenAlgorithm implements IWebGenAlgorithm {
		
		double EPSILON = 0.00001;
		//ArrayList featureStvs = new ArrayList(); 	// Liste mit typisierten Geb�uden vom Typ Feature;
		HashMap hashMap = new HashMap();
		TriangulationStringIDs tristrid;
		ConstrainedFeatureCollection fcnew;
		
		private boolean isArea = false;
		private boolean isLine = false;
		private boolean isPoint = false;
		
		public void run(WebGenRequest wgreq) {
			ConstrainedFeatureCollection fc = (ConstrainedFeatureCollection)wgreq.getFeatureCollection("geom");
			int maxnumber = wgreq.getParameterInt("maxnumber");
			if(maxnumber == 0) {
				double dimpercent = wgreq.getParameterDouble("dimpercent");
				maxnumber = (int)Math.floor(fc.size()*dimpercent);
			}
			if(haveSameGeomTypes(fc)) {
				if (maxnumber > 1) {
					fcnew = fc;
					//init Magen PersistentTriangulation
		    		initTriangulation(fcnew);			    			
		    		//start Typification
		    		typifyCombined(maxnumber, 0.0, true, false);
		    		wgreq.addResult("result", fcnew);
		    	}
				else if(maxnumber == 1) {
					fcnew = fc;
		    		initTriangulation(fcnew);			    			
		    		typifyCombined(2, 0.0, true, false);
		    		List feats = fcnew.getFeatures();
		    		if(feats.size() == 2) createFeatureStv((ConstrainedFeature)feats.get(0),(ConstrainedFeature)feats.get(1));
		    		wgreq.addResult("result", fcnew);
				}
			}
		}
		

	    /**
		 * Typifies GenAreaObjects.
		 * 1.) Auswahl der Objekte
		 * 2.) Gr�ssenanpassung und Positionierung
		 * 
		 * @param genAreaObjectsInput - Geb�ude f�r Typisierung
		 */
		public void typifyCombined(
				int 		targetNbrObjects, 
				double 		minimumShortestLength, 
				boolean 	considerTargetNbfObjects, 
				boolean		considerMinimumShortestLength
				) {
			
			//double targetDistance = (double)targetNbrObjects;
			//double currentDistance = targetDistance + 1.0;
			double actualLength = minimumShortestLength - 1.0;
//			System.out.println("actualLength: "+actualLength+"; featureStvs.size(): "+fcnew.size());
			
			// 1. Auswahl der Objekte, die gel�scht werden
	        while(considerTargetNbfObjects && (fcnew.size() > targetNbrObjects) || 
	        		considerMinimumShortestLength && (minimumShortestLength > actualLength)) {

//	        	System.out.println("In Schleife: actualLength: "+actualLength+"; genObjectStvs.size(): "+fcnew.size());

	        	// Delaunay-PersistentTriangulation �ber Geb�udeschwerpunkte und billigste Kante kollabieren
	        	TEdge shortestEdge = calculateShortestEdge(tristrid.edgeList);
	        	actualLength = shortestEdge.length;
	    		//currentDistance = shortestDelaunayEdge.getWeightedDistance();

				// Objekte an den Stellvertreterplatz verschieben
	        	// alte Objecte l�schen, neues hinzuf�gen
	    		calculateFeatureStv(shortestEdge);
	    		
//	    		System.out.println("Number of objects:\t" + fcnew.size()+"; actual minimum length:\t "+actualLength);    		
	        }        
		}
	    
	    
	    
		public void initTriangulation(ConstrainedFeatureCollection fc) {
			//initialisierung der Magen PersistentTriangulation
			tristrid = new TriangulationStringIDs();
	        try {
				Iterator it = fc.iterator();
				while (it.hasNext()) {
					ConstrainedFeature feature = (ConstrainedFeature) it.next();
					Geometry geometry = (Geometry) feature.getGeometry();
					Point center = geometry.getCentroid();
					ArrayList tns = tristrid.BuildNodesForGeom("obj" + feature.getID(), center.getCoordinates(), feature);
					tristrid.nodeList.addAll(tns);
				}
				tristrid.BuildFromNodes();
			}
	        catch (TriangulationException e) {
				//addErrorStack(e.getLocalizedMessage(), e.getStackTrace());
	        	e.printStackTrace();
			}
		}
		
		
		protected TEdge calculateShortestEdge(ArrayList triedges) {
			double weightSize			= 0.0;
			double influenceSize 		= 1.0;
			double weight 				= 1.0;	// Gewichtungsfaktor f�r Platzierung
			
			boolean COMPARE = true;
			boolean WEIGHTED_DELAUNAY = false;		// Typisierung mit Gewichtung durch Groesse
	        
	        ArrayList triweightedges = new ArrayList();
	        
	        if(WEIGHTED_DELAUNAY == true  || COMPARE == true) {
	        	Iterator triit = triedges.iterator();
	        	while(triit.hasNext()) {
	        		TEdge te = (TEdge)triit.next();
	        		if((te.node1.featureList.size() > 1) && (te.node1.featureList.size() > 1)) {
	        			ConstrainedFeature n1 = (ConstrainedFeature)te.node1.featureList.get(1);
	        			ConstrainedFeature n2 = (ConstrainedFeature)te.node2.featureList.get(1);
	        			double size1 = n1.getGeometry().getArea();
	        			double size2 = n2.getGeometry().getArea();
	        			weightSize = size1 + size2;
	        			weight = influenceSize*weightSize;
	        			//TWeightedEdge wte = new TWeightedEdge(te.node1, te.node2, weight, n1.getGeometry(), n2.getGeometry());
	        			TWeightedEdge wte = new TWeightedEdge(te.node1, te.node2, 1.0, n1.getGeometry(), n2.getGeometry());
	        			//wte.setWeight(weight);
	        			triweightedges.add(wte);
	        		}
	        	}
	        	Collections.sort(triweightedges);
	        }
	        /*if(WEIGHTED_DELAUNAY == false || COMPARE == true) {
	        	Collections.sort(triedges);
	        }*/
	        //System.out.println("Number of TEdges: " + triedges.size());
	             	
			
	        // Falls Geb�ude �berlagern -> mehrere weightedEdges = 0 wird auch nonWeightedEdges mit ber�cksichtigt
	        /*if(WEIGHTED_DELAUNAY == true) {        	
	    		double dist1 = getDistanceFromGNweightedEdge(triweightedges, 0);
	    		double dist2 = getDistanceFromGNweightedEdge(triweightedges, 1);
	    		if(dist1 < EPSILON && dist2 < EPSILON ) {// falls mehrere Kante gleich null sind 
	    												 // suche Kante mit k�rzesten Schwerpunktabstand
	    			TWeightedEdge edge = getZeroWeightedEdgeWhereNonWeightedEdgeIsShortest(triedges, triweightedges);
	        		System.out.println("\tmehrere Kanten mit L�nge null: " + edge.toString());
	    			return(edge);
	    		}
	        }*/
	        
	        //if(WEIGHTED_DELAUNAY == true) return((TEdge) triweightedges.get(0));
	        //else return((TEdge) triedges.get(0));
	        return (TEdge) triweightedges.get(0);
		}
		
		
		protected ConstrainedFeature calculateFeatureStv(TEdge shortestEdge) {
			//Stellverter 1 & 2
			ConstrainedFeature ft1 = (ConstrainedFeature)shortestEdge.node1.featureList.get(1);
			ConstrainedFeature ft2 = (ConstrainedFeature)shortestEdge.node2.featureList.get(1);
			
			ConstrainedFeature fnew = createFeatureStv(ft1, ft2);
			
			try {
				//Stellvertreter aus PersistentTriangulation l�schen
				tristrid.RemoveNode(shortestEdge.node1, null, null);
				tristrid.RemoveNode(shortestEdge.node2, null, null);
				//neuen Node in PersistentTriangulation einf�gen
				Point newcenter = fnew.getGeometry().getCentroid();
				TNode newnode = new TNode(newcenter.getX(), newcenter.getY(), "newobj"+fnew.getID());
				newnode.featureList.add(fnew);
				if(tristrid.nodeList.size() > 2)
					tristrid.InsertNode(newnode, new ArrayList(), new ArrayList());
			}
			catch (TriangulationException e) {
				
			}
			catch (Exception e) {}
			return(fnew);
		}
		
		
		protected ConstrainedFeature createFeatureStv(ConstrainedFeature ft1, ConstrainedFeature ft2) {
			ConstrainedFeature fnew = null;
			ArrayList fts = new ArrayList();
			fts.add(ft1);
			fts.add(ft2);
			//Berechne neuen Schwerpunkt
			GeometryCollection allgeoms = getMultiPolygonFromFeatures(fts);
			double sp_x = allgeoms.getCentroid().getX();
			double sp_y = allgeoms.getCentroid().getY();
			
			// neuen Stellvertreter erzeugen - der gr�ssere �berlebt
			if(ft1.getGeometry().getArea() < ft2.getGeometry().getArea()) {
				fnew = (ConstrainedFeature)ft2.clone();
				fnew.getConstraint().addOrigEdgeCount(ft1.getGeometry().getNumPoints()-1); //bestraft typification
				}
			else {
				fnew = (ConstrainedFeature)ft1.clone();
				fnew.getConstraint().addOrigEdgeCount(ft2.getGeometry().getNumPoints()-1); //bestraft typification
				}
			
			//Objekte an den Stellvertreterplatz verschieben
			Coordinate spTrans = (Coordinate)fnew.getGeometry().getCentroid().getCoordinate().clone();
			spTrans.x -=  sp_x;
			spTrans.y -=  sp_y;
			spTrans.x = 0-spTrans.x;
			spTrans.y = 0-spTrans.y;
			
			if (isArea) {
				// verschieben:
				Polygon polygon = (Polygon)fnew.getGeometry();
				polygon = GeometryHelper.translatePolygon(polygon, spTrans);
				// Gr�sse anpassen wobei schwarz-weiss-Verh�ltnis beibehalten wird
				double size = allgeoms.getArea(); // Summe der Fl�che aller repr�sentierten Objekte
				polygon = (Polygon)GeometryHelper.skaleSize(polygon, size);
				fnew.setGeometry(polygon);
			} else if (isLine) {
				// verschieben:
				LineString lineString = (LineString)fnew.getGeometry();
				lineString = (LineString)GeometryHelper.translateLineString(lineString, spTrans);
				fnew.setGeometry(lineString);
			} else if (isPoint) {
				// verschieben:
				Point point = (Point)fnew.getGeometry();
				point = (Point)GeometryHelper.translatePoint(point, spTrans);
				fnew.setGeometry(point);
			}
			
			//alte Features loeschen
			fcnew.remove(ft1);
			fcnew.remove(ft2);
			
			//neues Feature hinzuf�gen
			fcnew.add(fnew);
			
			return fnew;
		}
		
		
		protected GeometryCollection getMultiPolygonFromFeatures(ArrayList fts) {
			Geometry[] geometries = new Geometry[fts.size()];
			GeometryFactory geometryFactory = new GeometryFactory();
			int cntGeometries = 0;

			Iterator ftsit = fts.iterator();
			while (ftsit.hasNext()) {
				Feature ft = (Feature) ftsit.next();
				geometries[cntGeometries] = ft.getGeometry();
				cntGeometries++;
			}
			GeometryCollection geometryCollection = new GeometryCollection(geometries, geometryFactory);
			
			return(geometryCollection);
		}
	    
	    
	    // Test, ob alle genObjectsSource aus einer Klasse sind:
	    public boolean haveSameGeomTypes(FeatureCollection fc) {
			Iterator itGenObjectsSource = fc.iterator();
			while (itGenObjectsSource.hasNext()) {
				Geometry geom = ((Feature)itGenObjectsSource.next()).getGeometry();
				if (geom instanceof Polygon) 
					isArea = true;
				else if (geom instanceof LineString) 
					isLine = true;
				else if (geom instanceof Point) 
					isPoint = true;
			}
			if (isArea && isLine || isArea && isPoint || isLine && isPoint) {
				System.out.println("Input Dataset is of mixed geometry type! Terminating...");
				return false;
			}
			return true;
	    }
	    
	    
	    public InterfaceDescription getInterfaceDescription() {
			InterfaceDescription id = new InterfaceDescription("BuildingTypificationNew", "neun", "operator",
					"",
					"BuildingTypificationNew",
					"typify buildings or other features",
					"1.0");
			
			//add input parameters
			String[] allowed = {"Point","LineString","Polygon"};
			id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
			id.addInputParameter("maxnumber", "INTEGER", "", "number to reduce to");
			
			//add output parameters
			id.addOutputParameter("result", "FeatureCollection");
			return id;
		}
	}