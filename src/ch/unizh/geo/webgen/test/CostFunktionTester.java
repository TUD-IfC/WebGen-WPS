package ch.unizh.geo.webgen.test;

public class CostFunktionTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		for(int i=0; i<20; i++) {
			double prob = Math.atan(i) / 3;
			System.out.println(i+"/1 probability: " + prob);
		}
		System.out.println("");
		for(int i=0; i<20; i++) {
			double prob = Math.atan(i/2) / 3;
			System.out.println(i+"/2 probability: " + prob);
		}
		System.out.println("");
		for(int i=0; i<20; i++) {
			double prob = Math.atan(i/5) / 3;
			System.out.println(i+"/5 probability: " + prob);
		}
	}

}
