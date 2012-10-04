package ch.unizh.geo.webgen.test;

import java.io.FileInputStream;
import java.io.InputStream;

import ch.unizh.geo.webgen.server.WebGenRequestHandler;
import ch.unizh.geo.webgen.xml.GenerateXMLFactory;
import ch.unizh.geo.webgen.xml.ParseXMLFactory;

public class WebGenParserTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			InputStream is = new FileInputStream("reqex.xml");
			/*String[] registeredParsers = {
					"ch.unizh.geo.webgen.xml.ParamParserINTEGER",
					"ch.unizh.geo.webgen.xml.ParamParserDOUBLE",
					"ch.unizh.geo.webgen.xml.ParamParserBOOLEAN",
					"ch.unizh.geo.webgen.xml.ParamParserSTRING",
					"ch.unizh.geo.webgen.xml.ParamParserFeatureCollection"
					};*/
			ParseXMLFactory.initialize();
			
			/*String[] registeredGenerators = {
					"ch.unizh.geo.webgen.xml.ParamGeneratorINTEGER",
					"ch.unizh.geo.webgen.xml.ParamGeneratorDOUBLE",
					"ch.unizh.geo.webgen.xml.ParamGeneratorFeatureCollection"
					};*/
			GenerateXMLFactory.initialize();
			
			new WebGenRequestHandler(is, null);
		}
		catch (Exception e) {e.printStackTrace();}
	}

}
