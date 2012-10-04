package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ch.unizh.geo.webgen.model.ConstrainedFeature;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.server.WebGenRequestExecuter;
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


	public class BuildingTypificationStateful extends AWebGenAlgorithm implements IWebGenAlgorithm {
		
		double EPSILON = 0.00001;
		HashMap hashMap = new HashMap();
		ConstrainedFeatureCollection fc;
		ConstrainedFeatureCollection fcnew;
		ArrayList<ConstrainedFeature> removables = new ArrayList<ConstrainedFeature>();
		
		String graphid;
		
		WebGenRequest wgreq;
		
		private boolean isArea = false;
		private boolean isLine = false;
		private boolean isPoint = false;
		
		public void run(WebGenRequest wgreq) {
			this.wgreq = wgreq;
			ConstrainedFeatureCollection fc = (ConstrainedFeatureCollection)wgreq.getFeatureCollection("geom");
			//ConstrainedFeatureCollection fco = (ConstrainedFeatureCollection)wgreq.getFeatureCollection("congeom");
			//double mindist = wgreq.getParameterDouble("mindist");
			int maxnumber = wgreq.getParameterInt("maxnumber");
			if(maxnumber == 0) {
				double dimpercent = wgreq.getParameterDouble("dimpercent");
				maxnumber = (int)Math.floor(fc.size()*dimpercent);
			}
			
			graphid = wgreq.getParameter("graphid").toString();
			//initProxyGraph(fc, fco, mindist);
			this.fc = fc;
			
			wgreq.addResult("proxys", this.getProxyGraphGeoms());
			ConstrainedFeatureCollection fcnew = typify(fc, maxnumber);
			if(fcnew != null) wgreq.addResult("result", fcnew);
			
			/*String graphid = "g1";
			initProxyGraph(fc, mindist, graphid);
			FeatureCollection fcnew = getProxyGraphGeoms(graphid);
			if(fcnew != null) wgreq.addResult("result", fcnew);
			FeatureCollection fcnew2 = getShortestEdgeGeoms(graphid);
			if(fcnew != null) wgreq.addResult("result2", fcnew2);*/
		}
		
		
		private boolean initProxyGraph(ConstrainedFeatureCollection fc, ConstrainedFeatureCollection fco, double mindist) {
			int i=0;
			for(Iterator iter=fc.iterator(); iter.hasNext();){
				((ConstrainedFeature)iter.next()).setUID(i);
				i++;
			}
			this.fc = fc;
			HashMap<String, Object> tparams = new HashMap<String, Object>();
			tparams.put("graphid", graphid);
			tparams.put("geom", fc);
			if(fco != null) tparams.put("congeom", fco);
			tparams.put("mindist", new Double(mindist*3));
			tparams.put("action", "create");
			WebGenRequestExecuter.callService(tparams, "localcloned", "ProximityGraphStateful");
			return true;
		}
		
		private FeatureCollection getProxyGraphGeoms() {
			HashMap<String, Object> tparams = new HashMap<String, Object>();
			tparams.put("graphid", graphid);
			tparams.put("action", "getgeom");
			WebGenRequest twgreq2 = WebGenRequestExecuter.callService(tparams, "localcloned", "ProximityGraphStateful");
			FeatureCollection fcnew = (FeatureCollection)twgreq2.getResult("result");
			return fcnew;
		}
		
		private FeatureCollection getShortestEdgeGeoms() {
			ConstrainedFeatureCollection fcnew = new ConstrainedFeatureCollection(fc.getFeatureSchema());
			ConstrainedFeature[] secf = getShortestEdge();
			fcnew.add(secf[0]);
			fcnew.add(secf[1]);
			return fcnew;
		}
		
		private ConstrainedFeature[] getShortestEdge() {
			HashMap<String, Object> tparams = new HashMap<String, Object>();
			tparams.put("graphid", graphid);
			tparams.put("action", "shortestedge");
			WebGenRequest twgreq = WebGenRequestExecuter.callService(tparams, "localcloned", "ProximityGraphStateful");
			int n1 = twgreq.getResultInteger("node1");
			int n2 = twgreq.getResultInteger("node2");
			return new ConstrainedFeature[] {(ConstrainedFeature)fc.getFeature(n1), (ConstrainedFeature)fc.getFeature(n2)};
		}
		
		private void removeNode(int uid) {
			HashMap<String, Object> tparams = new HashMap<String, Object>();
			tparams.put("graphid", graphid);
			tparams.put("action", "removenode");
			tparams.put("node", new Integer(uid));
			WebGenRequest twgreq = WebGenRequestExecuter.callService(tparams, "localcloned", "ProximityGraphStateful");
		}
		
		private void insertNode(ConstrainedFeature newnode) {
			HashMap<String, Object> tparams = new HashMap<String, Object>();
			tparams.put("graphid", graphid);
			tparams.put("action", "insertnode");
			tparams.put("newnode", newnode);
			WebGenRequest twgreq = WebGenRequestExecuter.callService(tparams, "localcloned", "ProximityGraphStateful");
		}
		
		private ConstrainedFeatureCollection typify(ConstrainedFeatureCollection fc, int maxnumber) {
			haveSameGeomTypes(fc);
			//if(haveSameGeomTypes(fc)) {
				if (maxnumber > 1) {
					fcnew = fc;
		    		typifyCombined(maxnumber);
		    		return fcnew;
		    	}
				else if(maxnumber == 1) {
					fcnew = fc;	    			
		    		typifyCombined(2);
		    		List feats = fcnew.getFeatures();
		    		if(feats.size() == 2) createFeatureStv((ConstrainedFeature)feats.get(0),(ConstrainedFeature)feats.get(1));
		    		return fcnew;
				}
			//}
			return null;
		}
		

	    /**
		 * Typifies GenAreaObjects.
		 * 1.) Auswahl der Objekte
		 * 2.) Grössenanpassung und Positionierung
		 * 
		 * @param genAreaObjectsInput - Gebäude für Typisierung
		 */
		private void typifyCombined(int targetNbrObjects) {
			int targetremove = fcnew.size() - targetNbrObjects;
	        while(removables.size() < targetremove) {
	        	ConstrainedFeature[] shortesEdgeFeatures = this.getShortestEdge();
	    		calculateFeatureStv(shortesEdgeFeatures[0], shortesEdgeFeatures[1]);
	    		wgreq.addResult("proxy" + targetremove, this.getProxyGraphGeoms());
	    	}
	        fcnew.removeAll(removables);
		}
		
		
		protected ConstrainedFeature calculateFeatureStv(ConstrainedFeature ft1, ConstrainedFeature ft2) {
			ConstrainedFeature fnew = createFeatureStv(ft1, ft2);
			try {
				//Stellvertreter aus PersistentTriangulation löschen
				removeNode(ft1.getUID());
				removeNode(ft2.getUID());
				//neuen Node in PersistentTriangulation einfügen
				insertNode(fnew);
			}
			catch (Exception e) {e.printStackTrace();}
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
			
			// neuen Stellvertreter erzeugen - der grössere überlebt
			if(ft1.getGeometry().getArea() < ft2.getGeometry().getArea()) {
				//fnew = (ConstrainedFeature)ft2.clone();
				fnew = ft2;
				removables.add(ft1);
				fnew.getConstraint().addOrigEdgeCount(ft1.getGeometry().getNumPoints()-1); //bestraft typification
				}
			else {
				//fnew = (ConstrainedFeature)ft1.clone();
				fnew = ft1;
				removables.add(ft2);
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
				// Grösse anpassen wobei schwarz-weiss-Verhältnis beibehalten wird
				double size = allgeoms.getArea(); // Summe der Fläche aller repräsentierten Objekte
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
			//fcnew.remove(ft1);
			//fcnew.remove(ft2);
			
			//neues Feature hinzufügen
			//fcnew.add(fnew);
			
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
			InterfaceDescription id = new InterfaceDescription("BuildingTypificationStateful", "neun", "operator",
					"",
					"BuildingTypificationStateful",
					"typify buildings or other features",
					"1.0");
			
			//add input parameters
			String[] allowed = {"Point","LineString","Polygon"};
			id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
			id.addInputParameter("congeom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with obstructing geometries");
			id.addInputParameter("maxnumber", "INTEGER", "", "number to reduce to");
			//id.addInputParameter("mindist", "DOUBLE", "10.0", "minimum distance between buildings");
			
			id.addInputParameter("graphid", "STRING", "g1", "id of stored graph");
			
			//add output parameters
			id.addOutputParameter("result", "FeatureCollection");
			return id;
		}
	}