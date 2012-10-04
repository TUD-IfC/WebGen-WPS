package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.PolygonExtracter;
import com.vividsolutions.jts.operation.buffer.BufferOp;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDatasetFactory;
import com.vividsolutions.jump.io.DriverProperties;

public class PatchesGeneralization extends AWebGenAlgorithm implements
		IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection featCol = wgreq.getFeatureCollection("patchesclass");
		double T1 = wgreq.getParameterDouble("T1");
		double T2 = wgreq.getParameterDouble("T2");
		double T3 = wgreq.getParameterDouble("T3");
		double T4 = wgreq.getParameterDouble("T4");
		double T5 = wgreq.getParameterDouble("T5");
		double T6 = wgreq.getParameterDouble("T6");
		double T7 = wgreq.getParameterDouble("T7");
		double Ws = wgreq.getParameterDouble("Ws");
		double t = wgreq.getParameterDouble("t");
		double close = wgreq.getParameterDouble("close");
		double cp = wgreq.getParameterDouble("cp");

		FeatureCollection res = null;
		try {
			res = patchesalgorithm(featCol, T1, T2, T3, T4, T5, T6, T7, Ws, t,
					close, cp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		wgreq.addResult("lres", res);
	}

	public static FeatureCollection patchesalgorithm(FeatureCollection preColl,
			double T1, double T2, double T3, double T4, double T5, double T6,
			double T7, double Ws, double t, double close, double cp)
			throws Exception {

		//Declaring the variables 

		Geometry geom = null;

		FeatureCollection secColl, cleanColl, unionColl, featColl, otherfeatColl, surColl, ellColl, extraColl, finalColl, finalFcoll, patColl, endColl;
		Feature feat, f1, f;
		Feature finalfeature, finalfeat;
		DriverProperties dp = new DriverProperties();
		Geometry jtsgeom;

		Collection cole = new ArrayList();
		FeatureDatasetFactory fdfa = new FeatureDatasetFactory();
		secColl = fdfa.createFromGeometry(cole);

		//Create new collection iterator
		Iterator itero = preColl.iterator();

		// Do a first preselection and buffer the surviving patches by 3			    			    
		while (itero.hasNext()) {
			f = (Feature) itero.next();
			Geometry g = f.getGeometry();
			double a = g.getArea();

			if (a > 5000) {

				Geometry bg = g.buffer(5);
				Feature Fe = f;
				Fe.setGeometry(bg);

				secColl.add(Fe);
			}

		}

		System.out.println("The first vector size is  = " + secColl.size());

		// Union the Overlapping and touching patches and extract the new polygon			    
		unionColl = polygonExtract(secColl);

		//Create new collection iterator
		Iterator i = unionColl.iterator();

		//Create two new empty Collections			       
		otherfeatColl = fdfa.createFromGeometry(cole);
		featColl = fdfa.createFromGeometry(cole);

		//Do second preselection and load 2 newly created collections
		while (i.hasNext()) {
			f1 = (Feature) i.next();
			Geometry g1 = f1.getGeometry();
			double area = g1.getArea();
			if (area > 25000) {
				featColl.add(f1);
				otherfeatColl.add(f1);

			}

		}
		System.out.println("The new vector size is  = " + featColl.size());

		//Set the path for the output shapefiles	    

		/*DriverProperties out4 = new DriverProperties();
		out4
				.set(
						"DefaultValue",
						"C://Documents and Settings\\Administrator\\Desktop\\myresults\\85b\\cleantestAll.shp");*/
		// Declare some of the Thresholds

		// double T = SimpleIO.readDouble("Please insert the Value for Theshold = T");
		// double Ws = SimpleIO.readDouble("Please insert the Minimum Area for Elimination = Ws");
		// double t = SimpleIO.readDouble("Please insert the constant K width parameter = t"); 
		// int smin = SimpleIO.readInt("Please insert number of minimum close patches = s"); 

		double T = 125000.00;
		Ws = 50000.00;
		t = 250;
		int smin = 2;

		//Calculate the Minimum and the Maximum area within the Collection

		double Max = getMaxarea(featColl);
		double K = t / Math.sqrt(Max - T);
		close = 15;

		//Create some empty Feature Collections for use later
		Collection col = new ArrayList();
		FeatureDatasetFactory fdf = new FeatureDatasetFactory();

		surColl = fdf.createFromGeometry(col);
		surColl.removeAll(col);
		ellColl = fdf.createFromGeometry(col);
		ellColl.removeAll(col);
		extraColl = fdf.createFromGeometry(col);
		extraColl.removeAll(col);
		finalColl = fdf.createFromGeometry(col);
		finalColl.removeAll(col);
		finalFcoll = fdf.createFromGeometry(col);
		finalFcoll.removeAll(col);
		patColl = fdf.createFromGeometry(col);
		patColl.removeAll(col);
		endColl = fdf.createFromGeometry(col);
		endColl.removeAll(col);
		int size = featColl.size();
		System.out.println("Feat Coll vector size = " + size);
		//Create new collection iterator
		Iterator itera = featColl.iterator();

		//Start the main selection process 
		while (itera.hasNext()) {

			feat = (Feature) itera.next();
			geom = feat.getGeometry();

			double blw = 0;
			//Get the Blanket Width for each patch
			blw = getBlWidth(feat, K, T);
			double finalbuffer = blw;
			double area = geom.getArea();
			System.out.println("The area is:" + area);
			//Declaring Variables
			Geometry newgeom;
			Feature newfeat = feat;

			//Get the number of close pathces given a threshold for "closeness"
			int s = Vsize(feat, otherfeatColl, close);

			//Main selection process with buffering based on Blanket Width
			if (area >= T) {
				System.out.println(finalbuffer);
				newgeom = geom.buffer(finalbuffer);

				newfeat.setGeometry(newgeom);

				surColl.add(newfeat);
			}

			else if (area < T & area > 3 * Ws / 4 & s >= smin) {
				newgeom = geom.buffer(finalbuffer);

				newfeat.setGeometry(newgeom);

				surColl.add(newfeat);
			}

			else if (area < T) {
				double negbuffer = finalbuffer * (-1);
				geom = geom.buffer(negbuffer);
				double newarea = geom.getArea();

				if (newarea >= Ws) {
					feat.setGeometry(geom);
					surColl.add((Feature) feat);
				} else if (newarea < Ws) {
					feat.setGeometry(geom);
					ellColl.add(feat);

				}
			}

		}

		System.out.println(" The surColl size is = " + surColl.size());

		//Extract the new polygons after unioning the overlapping and touching patches    
		finalColl = polygonExtract(surColl);
		Iterator newit = finalColl.iterator();
		Geometry geoma;

		//New selection stage based on Threshold T 
		while (newit.hasNext()) {
			finalfeat = (Feature) newit.next();
			geoma = finalfeat.getGeometry();
			double farea = geoma.getArea();
			System.out.println("the final features area =" + farea);
			if (farea > T) {
				finalFcoll.add((Feature) finalfeat);
			}
		}

		//Get the Feature Colection with the patches to cover the holes of the input collection
		patColl = getCollectionPathces(finalFcoll, T + T / 2);

		//If patches for holes exsisted cover the holes and create a new collection
		if (patColl.size() > 0) {

			endColl = unionCollections(finalFcoll, patColl);

		}

		else if (patColl.size() == 0) {

			endColl = finalFcoll;

		}

		System.out.println("We proceed to face number 2 !!!!!!");
		//Create new collection iterator
		Iterator iterator = endColl.iterator();

		//Declaring Variables
		FeatureCollection bufColl, bpaColl, unColl, buf2Coll, un2Coll, clColl, cl2Coll, preEndColl, theendColl;
		Collection c = new ArrayList();
		FeatureDatasetFactory fd = new FeatureDatasetFactory();
		//Create new collections
		bufColl = fd.createFromGeometry(c);
		bufColl.removeAll(c);
		bpaColl = fd.createFromGeometry(c);

		unColl = fd.createFromGeometry(c);
		unColl.removeAll(c);
		buf2Coll = fd.createFromGeometry(c);
		un2Coll = fd.createFromGeometry(c);
		clColl = fd.createFromGeometry(c);
		cl2Coll = fd.createFromGeometry(c);
		theendColl = fd.createFromGeometry(c);
		theendColl.removeAll(c);
		preEndColl = fd.createFromGeometry(c);

		Collection buf = new ArrayList();

		//Buffer all polygons by 40m
		while (iterator.hasNext()) {
			Feature fe = (Feature) iterator.next();
			Geometry ge = fe.getGeometry();
			Geometry geo = ge.buffer(40);

			buf.add(geo);
		}
		//Create new collection
		bufColl = fd.createFromGeometry(buf);

		//Get the patches for holes created after the buffering
		bpaColl = getCollectionPathces(bufColl, T + T / 2);

		//If patches for wholes exsisted cover the holes and create a new collection
		if (bpaColl.size() > 0) {
			unColl = unionCollections(bufColl, bpaColl);

		} else if (bpaColl.size() == 0) {
			unColl = polygonExtract(bufColl);
		}

		System.out.println("We are currently in phase 4.just before buffering");
		Collection fin = new ArrayList();
		//Create new collection iterator
		Iterator itf = unColl.iterator();

		//Buffer all polygons by -80
		while (itf.hasNext()) {

			Feature fea = (Feature) itf.next();
			Geometry gea = fea.getGeometry();
			DouglasPeuckerSimplifier dps = new DouglasPeuckerSimplifier(gea);
			Geometry gdp = dps.simplify(gea, 10);

			double time1 = System.currentTimeMillis();
			Geometry geon = gdp.buffer(-80);
			double time2 = System.currentTimeMillis();
			System.out.println((time2 - time1) / 1000
					+ " seconds to make a negative buffer");
			fin.add(geon);

			System.gc();
		}

		System.out
				.println("We are currently in phase 4 After negative buffering");
		//Create new collection
		buf2Coll = fd.createFromGeometry(fin);

		//Get the polygons created after the negative buffering 					
		un2Coll = polygonExtract(buf2Coll);

		//Create new collection iterator
		Iterator un2 = un2Coll.iterator();
		System.out
				.println("We are currently in phase 4.Before second buffering");
		Collection f2 = new ArrayList();

		//Final selection,cleaning and buffering by 40
		while (un2.hasNext()) {
			Feature F = (Feature) un2.next();
			Geometry g = F.getGeometry();
			double ar = g.getArea();

			if (ar > 40000) {
				Geometry gn = g;

				BufferOp bufOp = new BufferOp(g);
				bufOp.setEndCapStyle(BufferOp.CAP_SQUARE);
				Geometry buffer = bufOp.getResultGeometry(40);
				f2.add(buffer);

			}

		}

		clColl = fd.createFromGeometry(f2);

		theendColl = polygonExtract(clColl);

		return theendColl;
	}

	public static int Vsize(Feature feat, FeatureCollection featColl, double d) {
		double dist;
		Geometry geom, othergeom;
		Feature otherfeat;

		int size;
		Iterator i;
		dist = 0;
		i = featColl.iterator();
		FeatureCollection remColl;
		geom = feat.getGeometry();
		Collection col = new ArrayList();
		FeatureDatasetFactory fdf = new FeatureDatasetFactory();
		remColl = fdf.createFromGeometry(col);
		remColl.removeAll(col);
		while (i.hasNext()) {

			otherfeat = (Feature) i.next();

			othergeom = otherfeat.getGeometry();
			dist = geom.distance(othergeom);
			//System.out.println("The distance between geomerees is" + geom.distance(othergeom));
			if (dist < d) {

				remColl.add(otherfeat);

			}

		}
		size = remColl.size();
		//System.out.println("The number of close patches is "+ remColl.size());
		return size;
	}

	public static double findMindist(Feature feat, FeatureCollection featColl) {
		Iterator i;
		double dist = 0;
		i = featColl.iterator();
		double Mindist = Double.MAX_VALUE;
		Geometry geom = feat.getGeometry();
		//Object ob=feat.getAttribute("Toid");
		// String temp=ob.toString();

		//System.out.println("here " +temp);
		while (i.hasNext()) {

			Feature otherfeat = (Feature) i.next();
			// ob=otherfeat.getAttribute("Toid");
			//String temp2=ob.toString();

			//System.out.println("here " +temp2);
			Geometry othergeom = otherfeat.getGeometry();
			dist = geom.distance(othergeom);
			//System.out.println(geom.distance(othergeom));
			if (dist != 0 & dist < Mindist) {
				Mindist = dist;
			}

		}
		// System.out.println("The min dist of this patch is  "+ Mindist);
		return Mindist;
	}

	public static double getBlWidth(Feature feat, double K, double T) {

		Geometry geom = feat.getGeometry();
		double area = geom.getArea();
		double perim = geom.getLength();
		double comp = area / ((perim * perim) / (4 * Math.PI));
		double BlanketWidth = comp * K * Math.sqrt(Math.abs(area - T));
		//System.out.println("The BlanketWidth is " + BlanketWidth);
		return BlanketWidth;

	}

	public static Geometry PolygonUnion(FeatureCollection featColl) {

		int nfeat;
		nfeat = featColl.size();
		Iterator it = featColl.iterator();
		//array Geometry jts
		int i = 0;
		Geometry geomarr[] = new Geometry[nfeat];
		while (it.hasNext()) {
			Feature feature = (Feature) it.next();

			geomarr[i++] = feature.getGeometry();

		}

		Geometry geom = geomarr[0];

		for (i = 1; i < nfeat; i++) {
			geom = geom.union(geomarr[i]);
		}
		System.out.println("Multipolygon created!");

		return geom;
	}

	public static FeatureCollection getCollectionPathces(
			FeatureCollection featColl, double T) {

		FeatureCollection otherColl, finalColl;
		Feature feat, fr, nf;
		Geometry geom, ngeom;
		otherColl = featColl;
		Iterator i = featColl.iterator();
		Iterator it = otherColl.iterator();

		List mylist;
		Collection col = new ArrayList();
		FeatureDatasetFactory fdf = new FeatureDatasetFactory();
		finalColl = fdf.createFromGeometry(col);
		finalColl.removeAll(col);

		GeometryFactory gf = new GeometryFactory();
		Polygon polug;
		LineString ls;
		Polygon npg;
		int counter = 0;

		while (it.hasNext()) {
			feat = (Feature) it.next();
			polug = (Polygon) feat.getGeometry();
			int rings = polug.getNumInteriorRing();
			counter = counter + rings;
			//System.out.println("the number of internal rings = " + rings);
		}
		System.out.println("The total number of rings = " + counter);
		Collection Colle = new ArrayList();
		while (i.hasNext()) {
			System.out.println("we are here");
			fr = (Feature) i.next();

			polug = (Polygon) fr.getGeometry();
			geom = fr.getGeometry();

			int newrings = polug.getNumInteriorRing();

			int in = 0;

			for (in = 0; in < newrings; in++) {
				ls = polug.getInteriorRingN(in);
				double ringarea = ls.getLength();
				boolean Closed = ls.isClosed();
				String s;
				s = ls.getGeometryType();
				System.out.println(s);
				npg = new Polygon((LinearRing) ls, null, gf);
				double parea = npg.getArea();

				//System.out.println("the new polygon area is" + parea);
				if (Closed = true) {
					//System.out.println("this ring is  closed");
				} else if (Closed = false) {
					//System.out.println("This ring is not closed");
				}
				Geometry geomnew = npg;
				System.out.println(geomnew.toString());
				double garea = geomnew.getArea();
				System.out.println("The area of geomnew is" + garea);

				if (garea < T) {

					Colle.add(geomnew);
				}

			}
			FeatureDatasetFactory fdfa = new FeatureDatasetFactory();
			finalColl = fdf.createFromGeometry(Colle);

		}

		return finalColl;

	}

	public static FeatureCollection polygonExtract(FeatureCollection featColl) {

		FeatureCollection finalColl;
		Iterator i = featColl.iterator();
		Collection col = new ArrayList();
		FeatureDatasetFactory fdf = new FeatureDatasetFactory();
		finalColl = fdf.createFromGeometry(col);
		finalColl.removeAll(col);
		Collection cole;

		int size = featColl.size();
		System.out.println("the vector size = " + size);

		if (featColl.size() == 0)
			return finalColl;

		Geometry temp = PolygonUnion(featColl);
		double temaarea = temp.getArea();
		System.out.println("The final Area = " + temaarea);

		Collection Colec = new ArrayList();

		Colec = PolygonExtracter.getPolygons(temp);
		int colesize = Colec.size();
		System.out.println(" The list size = " + colesize);

		FeatureDatasetFactory fdfa = new FeatureDatasetFactory();
		finalColl = fdfa.createFromGeometry(Colec);

		return finalColl;
	}

	public static FeatureCollection unionCollections(
			FeatureCollection featColl, FeatureCollection otherColl) {
		FeatureCollection interColl, finalColl;
		Collection Colec = new ArrayList();
		FeatureDatasetFactory fdf = new FeatureDatasetFactory();
		finalColl = fdf.createFromGeometry(Colec);
		finalColl.removeAll(Colec);
		interColl = fdf.createFromGeometry(Colec);
		interColl.removeAll(Colec);

		Geometry geom;
		Iterator i = featColl.iterator();

		while (i.hasNext()) {
			Feature feat = (Feature) i.next();
			interColl.add(feat);
		}
		Iterator it = otherColl.iterator();
		while (it.hasNext()) {
			Feature feature = (Feature) it.next();
			interColl.add(feature);
		}

		finalColl = polygonExtract(interColl);
		System.out.println(" The final collection before being extracted is "
				+ interColl.size());

		return finalColl;
	}

	public static double getMaxarea(FeatureCollection featColl) {

		Iterator i = featColl.iterator();
		double max = 0;
		while (i.hasNext())

		{
			Feature feat = (Feature) i.next();
			Geometry geom = feat.getGeometry();
			double area = geom.getArea();
			//Calculate the total area of patches
			if (area > max) {

				max = area;

			}

		}
		System.out.println("The Maximum area is " + max);
		return max;

	}

	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription(
				"PatchesGeneralization", "nico", "operator", "",
				"PatchesGeneralization", "does PatchesGeneralization", "1.0");
		id.visible = true;

		//add input parameters
		String[] allowed = { "Point", "LineString", "Polygon" };
		id.addInputParameter("patchesclass", "FeatureCollection",
				new AttributeDescription("GEOMETRY", "GEOMETRY", allowed),
				"layer with geometries");
		id.addInputParameter("T1", "DOUBLE", "0.0", "");
		id.addInputParameter("T2", "DOUBLE", "0.0", "");
		id.addInputParameter("T3", "DOUBLE", "0.0", "");
		id.addInputParameter("T4", "DOUBLE", "0.0", "");
		id.addInputParameter("T5", "DOUBLE", "0.0", "");
		id.addInputParameter("T6", "DOUBLE", "0.0", "");
		id.addInputParameter("T7", "DOUBLE", "0.0", "");
		id.addInputParameter("Ws", "DOUBLE", "0.0", "");
		id.addInputParameter("t", "DOUBLE", "0.0", "");
		id.addInputParameter("close", "DOUBLE", "0.0", "");
		id.addInputParameter("cp", "DOUBLE", "0.0", "");

		//add output parameters
		id.addOutputParameter("lres", "FeatureCollection");
		return id;
	}
}
