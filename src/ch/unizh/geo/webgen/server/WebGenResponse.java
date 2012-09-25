package ch.unizh.geo.webgen.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.vividsolutions.jump.feature.FeatureCollection;

public class WebGenResponse implements IWebGenContainer {
	
	private HashMap<String,Object> messages = new HashMap<String,Object>();
	private HashMap<String,Object> results = new HashMap<String,Object>();
	
	public WebGenResponse() {}
	
	public FeatureCollection getFeatureCollection(String key) {
		Object obj = results.get(key);
		if(obj instanceof FeatureCollection) return (FeatureCollection) obj;
		else return null;
	}
	
	public HashMap<String,FeatureCollection> getFeatureCollections() {
		HashMap<String,FeatureCollection> rfcs = new HashMap<String,FeatureCollection>();
		for(Iterator iter = results.entrySet().iterator(); iter.hasNext();) {
			Map.Entry te = (Map.Entry)iter.next();
			if(te.getValue() instanceof FeatureCollection) rfcs.put((String)te.getKey(), (FeatureCollection)te.getValue());
		}
		return rfcs;
	}
	
	public void addParameter(String key, Object value)  {
		results.put(key, value);
	}
	
	public void addParameters(HashMap<String,Object> params)  {
		results.putAll(params);
	}
	
	public Object getParameter(String key)  {
		return results.get(key);
	}
	
	public int getParameterInt(String key)  {
		try {return ((Integer)getParameter(key)).intValue();}
		catch(Exception e) {return 0;}
	}
	
	public double getParameterDouble(String key)  {
		try {return ((Double)getParameter(key)).doubleValue();}
		catch(Exception e) {return 0.0;}
	}
	
	public boolean getParameterBoolean(String key)  {
		try {return ((Boolean)getParameter(key)).booleanValue();}
		catch(Exception e) {return false;}
	}
	
	public HashMap<String,Object> getParameters() {
		return results;
	}
	
	public void addMessage(String key, String value)  {
		messages.put(key, value);
	}
	
	public Object getMessage(String key)  {
		return messages.get(key);
	}
	
	public HashMap<String,Object> getMessages() {
		return messages;
	}
	
	public boolean hasErrors() {
		return messages.containsKey("error");
	}
	
	public String getErrorMessage() {
		return messages.get("error").toString();
	}
	
	
	public String toString() {
		return "WebGenResponse:" +
			   "\n results = " + results.toString() +
			   "\n messages = " + messages.toString();
	}
}
