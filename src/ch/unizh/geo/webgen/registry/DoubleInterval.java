package ch.unizh.geo.webgen.registry;

public class DoubleInterval implements Interval {

	public double min;
	public double max;
	
	public DoubleInterval(double min, double max) {
		this.min = min;
		this.max = max;
	}
	
	public String getMinString() {
		return ""+min;
	}
	
	public String getMaxString() {
		return ""+max;
	}
	
}
