package ch.unizh.geo.webgen.xml;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class ParseXMLFactory {

	private static ParseXMLFactory factory;
	private static Logger LOGGER = Logger.getLogger(ParseXMLFactory.class);
	
	private List<IXMLParamParser> registeredParsers;

	public static void initialize() {
		if (factory == null) {
			factory = new ParseXMLFactory(ParserGeneratorRegistry.getRegisteredParsers());
		}
	}
	
	private ParseXMLFactory(String[] parserClasses) {
		registeredParsers = new ArrayList<IXMLParamParser>();
		for(String parserClass : parserClasses) {
			IXMLParamParser parser = null;
			try {
				parser = (IXMLParamParser) Class.forName(parserClass).newInstance();
				//parser = (IXMLParamParser) this.getClass().getClassLoader().loadClass(parserClass).newInstance();
			}
			catch (ClassNotFoundException e) {
				LOGGER.error("One of the parsers could not be loaded: " + parserClass, e);
			}
			catch(IllegalAccessException e) {
				LOGGER.error("One of the parsers could not be loaded: " + parserClass, e);
			}
			catch(InstantiationException e) {
				LOGGER.error("One of the parsers could not be loaded: " + parserClass, e);
			}
			registeredParsers.add(parser);
		}
	}

	public static ParseXMLFactory getInstance() {
		return factory;
	}
	
	public IXMLParamParser getXMLParser(String type) {
		for(IXMLParamParser parser : registeredParsers) {
			if(parser.getParamType().equals(type))
				return parser;
		}
		return null;
	}
}
