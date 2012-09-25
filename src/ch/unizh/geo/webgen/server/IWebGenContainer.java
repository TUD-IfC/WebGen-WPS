package ch.unizh.geo.webgen.server;

import java.util.HashMap;

public interface IWebGenContainer {
	
	public void addParameter(String key, Object value);
	public void addParameters(HashMap<String,Object> params);
	public Object getParameter(String key);

}
