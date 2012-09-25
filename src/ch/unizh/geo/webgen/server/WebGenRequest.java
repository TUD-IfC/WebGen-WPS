package ch.unizh.geo.webgen.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;

import com.vividsolutions.jump.feature.FeatureCollection;

public class WebGenRequest implements IWebGenContainer {
	
	private String algorithm;
	//private HashMap<String,Object> featurecollections = new HashMap<String,Object>();
	//private HashMap<String,Object> parameters = new HashMap<String,Object>();
	private HashMap<String,Object> messages = new HashMap<String,Object>();
	private HashMap<String,Object> results = new HashMap<String,Object>();
	
	private HashMap<String,Object> allparameters = new HashMap<String,Object>();
	
	public WebGenRequest() {}
	
	/*private WebGenRequest(HashMap<String,Object> fc, HashMap<String,Object> pa, HashMap<String,Object> mg, HashMap<String,Object> rs) {
		this.featurecollections = fc;
		this.parameters = pa;
		this.messages = mg;
		this.results = rs;
	}*/
	private WebGenRequest(HashMap<String,Object> apa, HashMap<String,Object> mg) {
		this.allparameters = apa;
		this.messages = mg;
	}
	
	public void setAlgorithmName(String algoname) {
		this.algorithm = "ch.unizh.geo.webgen.service."+algoname;
	}
	
	public void setAlgorithmPath(String algoname) {
		this.algorithm = algoname;
	}
	
	public String getAlgorithmPath() {
		return this.algorithm;
	}
	
	public void addFeatureCollection(String key, FeatureCollection fc)  {
		//featurecollections.put(key, fc);
		allparameters.put(key, fc);
	}
	
	public FeatureCollection getFeatureCollection(String key) {
		Object obj = allparameters.get(key);
		if(obj instanceof FeatureCollection) return (FeatureCollection) obj;
		else return null;
		//return (FeatureCollection)featurecollections.get(key);
	}
	
	public HashMap<String,FeatureCollection> getFeatureCollections() {
		//return featurecollections;
		HashMap<String,FeatureCollection> rfcs = new HashMap<String,FeatureCollection>();
		for(Iterator iter = allparameters.entrySet().iterator(); iter.hasNext();) {
			Map.Entry te = (Map.Entry)iter.next();
			if(te.getValue() instanceof FeatureCollection) rfcs.put((String)te.getKey(), (FeatureCollection)te.getValue());
		}
		return rfcs;
	}
	
	public void addParameter(String key, Object value)  {
		allparameters.put(key, value);
		//parameters.put(key, value);
	}
	
	public void addParameters(HashMap<String,Object> params)  {
		allparameters.putAll(params);
		//parameters.putAll(params);
	}
	
	public Object getParameter(String key)  {
		/*Object obj;
		try {obj = allparameters.get(key);}
		catch(Exception e) {obj =  results.get(key);}
		return obj;*/
		return allparameters.get(key);
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
	
	public String getParameterString(String key)  {
		try {return getParameter(key).toString();}
		catch(Exception e) {return "";}
	}
	
	public HashMap<String,Object> getParameters() {
		return allparameters;
	}
	
	public void addMessage(String key, String value)  {
		messages.put(key, value);
	}
	
	public String getMessage(String key)  {
		return messages.get(key).toString();
	}
	
	public HashMap<String,Object> getMessages() {
		return messages;
	}
	
	
	
	public void addResult(String key, Object value)  {
		results.put(key, value);
		//allparameters.put(key, value);
	}
	
	public void addResults(HashMap<String, Object> map)  {
		results.putAll(map);
	}
	
	public Object getResult(String key)  {
		return results.get(key);
		//return allparameters.get(key);
	}
	
	public ConstrainedFeatureCollection getResultFeatureCollection(String key)  {
		ConstrainedFeatureCollection fc = (ConstrainedFeatureCollection)results.get(key);;
		if(fc == null ) fc = (ConstrainedFeatureCollection)getParameter(key);;
		return fc;
	}
	
	public int getResultInteger(String key)  {
		try {return ((Integer)results.get(key)).intValue();}
		catch(Exception e) {return 0;}
	}
	
	public double getResultDouble(String key)  {
		try {return ((Double)results.get(key)).doubleValue();}
		catch(Exception e) {return 0.0;}
	}
	
	public HashMap<String,Object> getResults() {
		return results;
	}
	
	
	public String toString() {
		return "WebGenRequest:" +
			   "\n allparameters = " + allparameters.toString() +
			   "\n results = " + results.toString() +
			   "\n messages = " + messages.toString();
	}
	
	
	/*public WebGenRequest clone() {
		//return new WebGenRequest(this.featurecollections, this.parameters, this.messages, this.results);
		return new WebGenRequest(this.allparameters, this.messages);
	}*/
	
	public WebGenRequest cloneWithoutResults() {
		//return new WebGenRequest(this.featurecollections, this.parameters, new HashMap<String,Object>(), new HashMap<String,Object>());
		return new WebGenRequest(this.allparameters, new HashMap<String,Object>());
	}
}
