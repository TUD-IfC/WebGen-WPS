package ch.unizh.geo.webgen.server;

import ch.unizh.geo.webgen.registry.InterfaceDescription;

public interface IWebGenAlgorithm {
	//public HashMap<String,Object> run(HashMap<String,Object> layers, HashMap<String,Object> parameters);
	public void run(WebGenRequest wgreq);
	public String getErrors();
	public InterfaceDescription getInterfaceDescription();
}
