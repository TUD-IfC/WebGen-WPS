package ch.unizh.geo.webgen.test.parallel;

import java.util.HashMap;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollectionSorted;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.service.Eval_GMFeat_All;

public class OperatorThread implements Runnable {
	
	Thread runner;
	final int GO = 1;
	final int SUSPEND = 2;
	final int TERMINATE = 0;
	public int status = SUSPEND;
	
	//private Logger LOGGER;
	String operationName;
	IWebGenAlgorithm service;
	HashMap<String,Object> globalParameters;
	HashMap<String,Object> specialParameters = new HashMap<String,Object>();
	WebGenRequest otreq;
	//Vector<ConstrainedFeatureCollectionSorted> sortedFeatureCollectionVector;
	ConstrainedFeatureCollectionSorted sortedFeatureCollection;

	public OperatorThread(int partitionNr, String servicename, String operationName, HashMap<String,Object> globalParameters) {
		this.operationName = operationName;
		this.globalParameters = globalParameters;
		this.otreq = new WebGenRequest();
		this.otreq.addParameters(globalParameters);
		try {
			service = (IWebGenAlgorithm) Class.forName(servicename).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		//LOGGER = Logger.getLogger("PartitionThread "+partitionNr+": OperatorThread " + operationName);
		//LOGGER.info("initializing complete");
	}
	
	synchronized public void start() {
		// Called when the applet is being started or restarted.
		// Create a new thread or restart the existing thread.
		status = GO;
		if (runner == null || ! runner.isAlive()) {  // Thread doens't yet exist or has died for some reason.
			runner = new Thread(this);
			runner.start();
		}
		else {
			notify();
		}
		executeOperation();
		//LOGGER.info("... algorithm finished");
	}
	
	public void run() {
		status = SUSPEND;
		while (status != TERMINATE) {
            synchronized(this) {
               while (status == SUSPEND) waitDelay();
            }
            if (status == GO) {
            	executeOperation();
            	status = SUSPEND;
            }
            //if (status == GO) waitDelay(250);
         }
    }
	
	public void reLoad(ConstrainedFeatureCollection geom, ConstrainedFeatureCollection congeom/*,
		      Vector<ConstrainedFeatureCollectionSorted> sfcv*/) {
		this.otreq = new WebGenRequest();
		this.otreq.addParameters(globalParameters);
		this.otreq.addParameters(specialParameters);
		this.otreq.addParameter("geom", geom);
		this.otreq.addParameter("congeom", congeom);
		sortedFeatureCollection = null;
	}
	
	synchronized public void stop() {
		// Called when the applet is about to be stopped.
		// Suspend the thread.
		status = SUSPEND;
		notify();
	}
	
	
	synchronized public void destroy() {
		// Called when the applet is about to be permanently destroyed;
		// Stop the thread.
		status = TERMINATE;
		notify();
	} 
	
	synchronized void waitDelay() {
		// Pause until the notify() method is called
		// by some other thread.
		try {
			wait();
		}
		catch (InterruptedException e) {
		}
	}
	
	public void addParameter(String key, Object value) {
		otreq.addParameter(key, value);
		this.specialParameters.put(key, value);
	}

    public void executeOperation() {
    	//LOGGER.info("executing " + operationName + " - " + otreq.toString());
        try {
        	service.run(otreq);
        	ConstrainedFeatureCollection result = (ConstrainedFeatureCollection)otreq.getResult("result");
        	if(result == null) result = (ConstrainedFeatureCollection)otreq.getParameter("geom");
        	
        	WebGenRequest otres = new WebGenRequest();
        	otres.addParameters(globalParameters);
        	otres.addParameter("geom", result);
        	(new Eval_GMFeat_All()).run(otres);
        	Double[] costVec = (Double[])otres.getResult("severities");
	    	for(int i=4; i<8; i++) {
	    		try {costVec[i] *= 0.25;}
	    		catch (Exception e) {
	    			e.printStackTrace();
	    		}
	    	}
	    	
	    	double costAllCurrent = getCostFromCostVector(costVec);
	    	sortedFeatureCollection = new ConstrainedFeatureCollectionSorted(costAllCurrent, result, operationName);
			//ConstrainedFeatureCollectionSorted sortedFeatureCollection = new ConstrainedFeatureCollectionSorted(costAllCurrent, result, operationName);
			//sortedFeatureCollectionVector.add(sortedFeatureCollection);
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
    }
    
    public ConstrainedFeatureCollectionSorted getResult() {
    	return this.sortedFeatureCollection;
    }
    
    
    public double getCostFromCostVector(Double[] costVec) {
    	double costAll = 0.0;
    	for(int i=0; i<costVec.length; i++) {
    		costAll += costVec[i].doubleValue();
    	}
    	return costAll;    
    }

}
