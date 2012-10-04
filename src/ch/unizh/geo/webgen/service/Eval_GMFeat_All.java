/*
 * Created on 10.08.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author neun
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */

package ch.unizh.geo.webgen.service;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.registry.ParameterDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;


public class Eval_GMFeat_All extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		Object fco = wgreq.getFeatureCollection("geom");
		if(fco instanceof ConstrainedFeatureCollection) {
			ConstrainedFeatureCollection fc = (ConstrainedFeatureCollection) fco;
			callServices(wgreq);
			try {
				String historymsg = wgreq.getParameter("historymsg").toString();
				fc.makeConstraintHistoryStep(historymsg);
			}
			catch(Exception e) {}
			wgreq.addResult("result", fc);
		}
		else {
			wgreq.addMessage("error","Please submit a ConstrainedFeatureCollection!");
		}
	}
	
	private void callServices(WebGenRequest wgreq) {
		Double[] partitionCost = new Double[8];
		
		(new Eval_GMFeat_MinSize()).run(wgreq);
		partitionCost[0] = wgreq.getResultDouble("severity");
		wgreq.addResult("severityMinSize", wgreq.getResult("severity"));
		if(wgreq.getResult("result") != null) wgreq.addParameter("geom", wgreq.getResult("result"));
		
		(new Eval_GMFeat_MinDist()).run(wgreq);
		partitionCost[1] = wgreq.getResultDouble("severity");
		wgreq.addResult("severityMinLength", wgreq.getResult("severity"));
		if(wgreq.getResult("result") != null) wgreq.addParameter("geom", wgreq.getResult("result"));
		
		(new Eval_GMFeat_MinLength()).run(wgreq);
		partitionCost[2] = wgreq.getResultDouble("severity");
		wgreq.addResult("severityMinDist", wgreq.getResult("severity"));
		if(wgreq.getResult("result") != null) wgreq.addParameter("geom", wgreq.getResult("result"));
		
		(new Eval_GMFeat_LocalWidth()).run(wgreq);
		partitionCost[3] = wgreq.getResultDouble("severity");
		wgreq.addResult("severityLocalWidth", wgreq.getResult("severity"));
		if(wgreq.getResult("result") != null) wgreq.addParameter("geom", wgreq.getResult("result"));
		
		(new Eval_GMFeat_Diff_Position()).run(wgreq);
		partitionCost[4] = wgreq.getResultDouble("severity");
		wgreq.addResult("severityDiffPosition", wgreq.getResult("severity"));
		if(wgreq.getResult("result") != null) wgreq.addParameter("geom", wgreq.getResult("result"));
		
		(new Eval_GMFeat_Diff_Orientation()).run(wgreq);
		partitionCost[5] = wgreq.getResultDouble("severity");
		wgreq.addResult("severityDiffEdgeCount", wgreq.getResult("severity"));
		if(wgreq.getResult("result") != null) wgreq.addParameter("geom", wgreq.getResult("result"));
		
		(new Eval_GMFeat_Diff_EdgeCount()).run(wgreq);
		partitionCost[6] = wgreq.getResultDouble("severity");
		wgreq.addResult("severityDiffWidthLengthRatio", wgreq.getResult("severity"));
		if(wgreq.getResult("result") != null) wgreq.addParameter("geom", wgreq.getResult("result"));
		
		(new Eval_GMFeat_Diff_WidthLengthRatio()).run(wgreq);
		partitionCost[7] = wgreq.getResultDouble("severity");
		wgreq.addResult("severityDiffOrientation", wgreq.getResult("severity"));
		if(wgreq.getResult("result") != null) wgreq.addParameter("geom", wgreq.getResult("result"));
		
		wgreq.addResult("severities", partitionCost);
	}
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("Eval_GMFeat_All", "neun", "support",
				"",
				"Eval_GMFeat_All",
				"Evaluate All Group Multi-Constraints",
				"1.0");
		id.visible = true;
		
		//add input parameters
		id.addInputParameter(new ParameterDescription("geom", "FeatureCollection", null, true, "layer with geometries"));
		id.addInputParameter("minarea", "DOUBLE", 200.0, 0.0, Double.POSITIVE_INFINITY, "minimum size");
		id.addInputParameter("mindist", "DOUBLE", 10.0, 0.0, Double.POSITIVE_INFINITY, "minimum distance");
		id.addInputParameter("minlength", "DOUBLE", 10.0, 0.0, Double.POSITIVE_INFINITY, "minimum length");
		id.addInputParameter("historymsg", "STRING", "Eval_GMFeat_All", "history message");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}