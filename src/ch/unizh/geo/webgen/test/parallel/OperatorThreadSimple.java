package ch.unizh.geo.webgen.test.parallel;

import java.util.Vector;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollectionSorted;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.service.Eval_GMFeat_All;
import ch.unizh.geo.webgen.tools.ProcessingTools;

public class OperatorThreadSimple extends Thread {

	//private Logger LOGGER;
	String operationName;
	IWebGenAlgorithm service;
	//HashMap<String,Object> globalParameters;
	//HashMap<String,Object> specialParameters = new HashMap<String,Object>();
	WebGenRequest otreq;
	ConstrainedFeatureCollectionSorted sortedFeatureCollection;
	Vector<ConstrainedFeatureCollectionSorted> sortedFeatureCollectionVector;
	
	public OperatorThreadSimple(int partitionNr, String servicename, String operationName, 
			WebGenRequest otreq, Vector<ConstrainedFeatureCollectionSorted> sortedFeatureCollectionVector) {
		this.operationName = operationName;
		this.otreq = otreq;
		this.sortedFeatureCollectionVector = sortedFeatureCollectionVector;
		try {
			service = (IWebGenAlgorithm) Class.forName(servicename).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
    	//LOGGER.info("executing " + operationName + " - " + otreq.toString());
        try {
        	service.run(otreq);
        	ConstrainedFeatureCollection result = (ConstrainedFeatureCollection)otreq.getResult("result");
        	if(result == null) result = (ConstrainedFeatureCollection)otreq.getParameter("geom");
        	
        	WebGenRequest otres = new WebGenRequest();
        	otres.addParameter("minarea", otreq.getParameter("minarea"));
        	otres.addParameter("minlength", otreq.getParameter("minlength"));
        	otres.addParameter("mindist", otreq.getParameter("mindist"));
        	otres.addParameter("roaddist", otreq.getParameter("roaddist"));
        	//otres.addParameters(globalParameters);
        	otres.addParameter("geom", result);
        	(new Eval_GMFeat_All()).run(otres);
        	Double[] costVec = (Double[])otres.getResult("severities");
	    	for(int i=4; i<8; i++) {
	    		try {costVec[i] *= 0.25;}
	    		catch (Exception e) {
	    			e.printStackTrace();
	    		}
	    	}
	    	
	    	double costAllCurrent = ProcessingTools.getCostFromCostVector(costVec);
	    	sortedFeatureCollection = new ConstrainedFeatureCollectionSorted(costAllCurrent, result, operationName);
	    	sortedFeatureCollectionVector.add(sortedFeatureCollection);
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
    }
}
