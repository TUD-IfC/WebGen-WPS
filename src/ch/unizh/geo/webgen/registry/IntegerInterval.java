package ch.unizh.geo.webgen.registry;

public class IntegerInterval implements Interval {

	public int min;
	public int max;
	
	public IntegerInterval(int min, int max) {
		this.min = min;
		this.max = max;
	}
	
	public String getMinString() {
		return ""+min;
	}
	
	public String getMaxString() {
		return ""+min;
	}
	
}
