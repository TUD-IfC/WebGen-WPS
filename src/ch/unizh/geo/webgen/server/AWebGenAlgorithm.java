/*
 * Created on 17.08.2006
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

package ch.unizh.geo.webgen.server;

import org.apache.log4j.Logger;

import ch.unizh.geo.webgen.registry.InterfaceDescription;

public abstract class AWebGenAlgorithm implements IWebGenAlgorithm {

	public static Logger LOGGER = Logger.getLogger(AWebGenAlgorithm.class);
	
	public boolean haserrors = false;
	private String errors = "";
	private String messages = "";
	
	public String getErrors() {
		return errors;
	}
	
	public void addErrorStack(String lm , StackTraceElement[] ste) {
		haserrors = true;
		errors += "Error in WebGen Algorithm:\n\n";
		errors += lm + "\n\n";
		for(int i = 0; i < ste.length; i++) {
			errors += ste[i] + "\n\n";
		}
		errors += "\n\n";
	}
	
	public void addError(String message) {
		haserrors = true;
		errors = errors + message + "\n\n";
	}
	
	public void addMessage(String message) {
		messages = messages+ message + "\n\n";
	}

	//public abstract HashMap<String,Object> run(HashMap layers, HashMap parameters);

	public abstract InterfaceDescription getInterfaceDescription();
}