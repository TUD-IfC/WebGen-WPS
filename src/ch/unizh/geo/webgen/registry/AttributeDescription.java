package ch.unizh.geo.webgen.registry;

import java.util.Vector;

public class AttributeDescription {
	
	public String name;
	public String type;
	public Vector<Object> supportedvalues = new Vector<Object>();
	
	public AttributeDescription(String name, String type) {
		this.name = name;
		this.type = type;
	}
	
	public AttributeDescription(String name, String type, Object[] supportedvalues) {
		this.name = name;
		this.type = type;
		for(int i=0; i< supportedvalues.length; i++) {
			this.supportedvalues.add(supportedvalues[i]);
		}
	}
	
	public void addSupportedValue(String value) {
		this.supportedvalues.add(value);
	}
	
	public void addSupportedIntegerInterval(int min, int max) {
		this.supportedvalues.add(new IntegerInterval(min, max));
	}
	
	public void addSupportedDoubleInterval(double min, double max) {
		this.supportedvalues.add(new DoubleInterval(min, max));
	}

}
