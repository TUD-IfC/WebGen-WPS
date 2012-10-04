package ch.unizh.geo.webgen.service;

import java.util.HashMap;
import java.util.Iterator;

import ch.unizh.geo.webgen.model.ConstrainedFeature;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.server.WebGenRequestExecuter;


public class StatefulTypificationAndDisplacement extends AWebGenAlgorithm implements IWebGenAlgorithm  {
		
	public void run(WebGenRequest wgreq) {
		ConstrainedFeatureCollection fcA = (ConstrainedFeatureCollection)wgreq.getFeatureCollection("geom");
		ConstrainedFeatureCollection fcC = (ConstrainedFeatureCollection)wgreq.getFeatureCollection("congeom");
		double mindist = wgreq.getParameterDouble("mindist");
		double roaddist = wgreq.getParameterDouble("roaddist");
		int maxnumber = wgreq.getParameterInt("maxnumber");
		initProxyGraph("gx0", fcA, fcC, mindist);
		
		long timestateful; long timenormal;
		
		for(int tmd = 5; tmd <= 20; tmd+=5) {
			for(int trd = 5; trd <= 20; trd+=5) {
				for(int tmn = 70; tmn <= 126; tmn+=10) {
					timestateful = System.currentTimeMillis();
					wgreq.addResult("rs "+ tmd + "/" + trd + "/" + tmn, testStateful(fcA, fcC, tmd, trd, tmn));
					timestateful = System.currentTimeMillis()-timestateful;
					
					timenormal = System.currentTimeMillis();
					wgreq.addResult("rn "+ tmd + "/" + trd + "/" + tmn, testNormal(fcA, fcC, tmd, trd, tmn));
					timenormal = System.currentTimeMillis()-timenormal;
					
					double gain = (double)timestateful/timenormal;
					gain *= 100;
					System.out.println("parameters " + tmd + "/" + trd + "/" + tmn + " --> stateful/normal = " + timestateful + "ms/"+ timenormal + "ms --> " + gain + "%");
				}
			}
		}
	}
	
	private ConstrainedFeatureCollection testStateful(ConstrainedFeatureCollection fc, ConstrainedFeatureCollection fco, double mindist, double roaddist, int maxnumber) {
		setUIDAsc(fc);
		setUIDVal(fco, -1);
		initProxyGraph("gx1", fc, fco, mindist);
		ConstrainedFeatureCollection fcnew = callTypification("gx1", fc, maxnumber);
		//setUIDAsc(fcnew);
		fcnew = callDisplacement("gx1", fcnew, fco, mindist, roaddist);
		return fcnew;
	}
	
	private ConstrainedFeatureCollection testNormal(ConstrainedFeatureCollection fc, ConstrainedFeatureCollection fco, double mindist, double roaddist, int maxnumber) {
		setUIDAsc(fc);
		setUIDVal(fco, -1);
		initProxyGraph("gx2", fc, fco, mindist);
		ConstrainedFeatureCollection fcnew = callTypification("gx2", fc, maxnumber);
		setUIDAsc(fcnew);
		initProxyGraph("gx3", fcnew, fco, mindist);
		fcnew = callDisplacement("gx3", fcnew, fco, mindist, roaddist);
		return fcnew;
	}
	
	
	private ConstrainedFeatureCollection callTypification(String graphid, ConstrainedFeatureCollection fc, int maxnumber) {
		HashMap<String, Object> tparams = new HashMap<String, Object>();
		tparams.put("graphid", graphid);
		tparams.put("geom", fc);
		tparams.put("maxnumber", maxnumber);
		WebGenRequest twgreq = WebGenRequestExecuter.callService(tparams, "localcloned", "BuildingTypificationStateful");
		ConstrainedFeatureCollection fcnew = (ConstrainedFeatureCollection)twgreq.getResult("result");
		return fcnew;
	}
	
	private ConstrainedFeatureCollection callDisplacement(String graphid, ConstrainedFeatureCollection fc, ConstrainedFeatureCollection fco, double mindist, double roaddist) {
		HashMap<String, Object> tparams = new HashMap<String, Object>();
		tparams.put("graphid", graphid);
		tparams.put("geom", fc);
		tparams.put("congeom", fco);
		tparams.put("mindist", mindist);
		tparams.put("roaddist", roaddist);
		WebGenRequest twgreq2 = WebGenRequestExecuter.callService(tparams, "localcloned", "DisplaceConstrainedStateful");
		ConstrainedFeatureCollection fcnew = (ConstrainedFeatureCollection)twgreq2.getResult("result");
		return fcnew;
	}
	
	
	private boolean initProxyGraph(String graphid, ConstrainedFeatureCollection fc, ConstrainedFeatureCollection fco, double mindist) {
		HashMap<String, Object> tparams = new HashMap<String, Object>();
		tparams.put("graphid", graphid);
		ConstrainedFeatureCollection fct = new ConstrainedFeatureCollection(fc.getFeatureSchema());
		fct.addAll(fc.getFeatures());
		fct.addAll(fco.getFeatures());
		tparams.put("geom", fct);
		if(fco != null) tparams.put("congeom", fco);
		tparams.put("mindist", new Double(mindist*3));
		tparams.put("action", "create");
		WebGenRequestExecuter.callService(tparams, "localcloned", "ProximityGraphStateful");
		return true;
	}
	
	private void setUIDAsc(ConstrainedFeatureCollection fc) {
		int i=0;
		for(Iterator iter=fc.iterator(); iter.hasNext();){
			((ConstrainedFeature)iter.next()).setUID(i);
			i++;
		}
	}
	private void setUIDVal(ConstrainedFeatureCollection fc, int val) {
		for(Iterator iter=fc.iterator(); iter.hasNext();){
			((ConstrainedFeature)iter.next()).setUID(val);
		}
	}

	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("StatefulTypificationAndDisplacement", "neun", "operator",
				"",
				"StatefulTypificationAndDisplacement",
				"Stateful Typification And Displacement",
				"1.0");
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("congeom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with constraining geometries such as roads");
		id.addInputParameter("mindist", "DOUBLE", "10.0", "minimum distance between buildings");
		id.addInputParameter("roaddist", "DOUBLE", "5.0", "minimum distance to constraining features such as roads");
		id.addInputParameter("maxnumber", "INTEGER", "", "number to reduce to");
		
		//id.addInputParameter("graphid", "STRING", "g1", "id of stored graph");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
