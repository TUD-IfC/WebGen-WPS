package ch.unizh.geo.webgen.service.parallel;

import java.util.ArrayList;
import java.util.HashMap;

public class ProcessingOperator {
	
	public String name;
	public String shortName;
	public ArrayList<String> operationClasses;
	public String server;
	public HashMap<String, Object> additionalParameters;
	
	public ProcessingOperator(String name, String shortName, String[] opClasses, String server) {
		this.name = name;
		this.shortName = shortName;
		this.server = server;
		this.operationClasses = new ArrayList<String>();
		for(int i=0; i< opClasses.length; i++) this.operationClasses.add(opClasses[i]);
		this.additionalParameters = new HashMap<String, Object>();
	}
	
	public ProcessingOperator(String name, String shortName, String[] opClasses, String server, Object[][] additionalParams) {
		this(name, shortName, opClasses, server);
		for(int i=0; i< additionalParams.length; i++) {
			this.additionalParameters.put(additionalParams[i][0].toString(), additionalParams[i][1]);
		}
	}

}
