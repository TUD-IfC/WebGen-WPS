package ch.unizh.geo.webgen.client.jump;

import java.util.Vector;


public class ParameterType {
	private String name = "";
	private Class type = null;
	private boolean islayer = false;
	public Vector supportedvalues;
	
	/*public ParameterType(String name, Class type){
		this.name = name;
		this.type = type;
	}
	
	public ParameterType(String name, Class type, boolean islayer){
		this.name = name;
		this.type = type;
		this.islayer = islayer;
	}*/
	
	public ParameterType(String name, Class type, boolean islayer, Vector supportedvalues){
		this.name = name;
		this.type = type;
		this.islayer = islayer;
		this.supportedvalues = supportedvalues;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setType(Class type) {
		this.type = type;
	}
	
	public Class getType() {
		return this.type;
	}
	
	public void setIsLayer(boolean islayer) {
		this.islayer = islayer;
	}
	
	public boolean getIsLayer() {
		return this.islayer;
	}
}
