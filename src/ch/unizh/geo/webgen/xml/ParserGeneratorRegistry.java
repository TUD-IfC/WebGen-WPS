package ch.unizh.geo.webgen.xml;

public class ParserGeneratorRegistry {

	static String[] registeredGenerators = {
			"ch.unizh.geo.webgen.xml.ParamGeneratorINTEGER",
			"ch.unizh.geo.webgen.xml.ParamGeneratorINTEGERARRAY",
			"ch.unizh.geo.webgen.xml.ParamGeneratorDOUBLE",
			"ch.unizh.geo.webgen.xml.ParamGeneratorDOUBLEARRAY",
			"ch.unizh.geo.webgen.xml.ParamGeneratorBOOLEAN",
			"ch.unizh.geo.webgen.xml.ParamGeneratorSTRING",
			"ch.unizh.geo.webgen.xml.ParamGeneratorFeatureCollection",
			"ch.unizh.geo.webgen.xml.ParamGeneratorConstrainedFeatureCollection",
			"ch.unizh.geo.webgen.xml.ParamGeneratorMESSAGE"
			};;

	static String[] registeredParsers = {
			"ch.unizh.geo.webgen.xml.ParamParserINTEGER",
			"ch.unizh.geo.webgen.xml.ParamParserINTEGERARRAY",
			"ch.unizh.geo.webgen.xml.ParamParserDOUBLE",
			"ch.unizh.geo.webgen.xml.ParamParserDOUBLEARRAY",
			"ch.unizh.geo.webgen.xml.ParamParserBOOLEAN",
			"ch.unizh.geo.webgen.xml.ParamParserSTRING",
			"ch.unizh.geo.webgen.xml.ParamParserFeatureCollection",
			"ch.unizh.geo.webgen.xml.ParamParserConstrainedFeatureCollection",
			"ch.unizh.geo.webgen.xml.ParamParserMESSAGE"
			};;
	
	public static String[] getRegisteredGenerators() {
		return registeredGenerators;
	}
	
	public static String[] getRegisteredParsers() {
		return registeredParsers;
	}
}
