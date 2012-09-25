package ch.unizh.geo.webgen.xml;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


public class GenerateXMLFactory {
	
	private static GenerateXMLFactory factory;
	private static Logger LOGGER = Logger.getLogger(ParseXMLFactory.class);
	
	private List<IXMLParamGenerator> registeredGenerators;

	public static void initialize() {
		if (factory == null) {
			factory = new GenerateXMLFactory(ParserGeneratorRegistry.getRegisteredGenerators());
		}
	}
	
	private GenerateXMLFactory(String[] generatorClasses) {
		registeredGenerators = new ArrayList<IXMLParamGenerator>();
		for(String generatorClass : generatorClasses) {
			IXMLParamGenerator generator = null;
			try {
				generator = (IXMLParamGenerator) Class.forName(generatorClass).newInstance();
				//generator = (IXMLParamGenerator) this.getClass().getClassLoader().loadClass(generatorClass).newInstance();
			}
			catch (ClassNotFoundException e) {
				LOGGER.error("One of the parsers could not be loaded: " + generatorClass, e);
			}
			catch(IllegalAccessException e) {
				LOGGER.error("One of the parsers could not be loaded: " + generatorClass, e);
			}
			catch(InstantiationException e) {
				LOGGER.error("One of the parsers could not be loaded: " + generatorClass, e);
			}
			registeredGenerators.add(generator);
		}
	}

	public static GenerateXMLFactory getInstance() {
		if (factory == null) initialize();
		return factory;
	}
	
	public IXMLParamGenerator getXMLParser(Object objtype) {
		for(IXMLParamGenerator generator : registeredGenerators) {
			/*if(objtype.getClass() == generator.getParamType())
				return generator;*/
			if(generator.instanceCheck(objtype))
				return generator;
		}
		return null;
	}
}
