package ch.unizh.geo.webgen.test;

import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.service.BufferFeatures;

public class printInterfaceDescriptions {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IWebGenAlgorithm sb = new BufferFeatures();
		System.out.println(sb.getInterfaceDescription().generateXMLDescription());
	}

}
