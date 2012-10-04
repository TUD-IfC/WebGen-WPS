package ch.unizh.geo.webgen.test.parallel;

import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

public class EvalConstraintThread extends Thread {
	
	IWebGenAlgorithm service;
	WebGenRequest wgreq;
	Double[] severities;
	int sevindex;

	public EvalConstraintThread(String constraintservice, WebGenRequest wgreq, Double[] severities, int sevindex) {
		this.wgreq = wgreq;
		this.severities = severities;
		this.sevindex = sevindex;
		try {
			service = (IWebGenAlgorithm) Class.forName(constraintservice).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    }

    public void run() {
        try {
        	synchronized(wgreq) {
        		service.run(wgreq);
            	severities[sevindex] = new Double(((Double)wgreq.getResult("severity")).doubleValue());
        	}
        }
        catch (Exception e) {
        	severities[sevindex] = new Double(0.0);
        	e.printStackTrace();
        }
    }

}
