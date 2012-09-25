package ch.unizh.geo.webgen.xml;

import org.dom4j.Element;

public interface IXMLParamGenerator {

	//public Class getParamType();
	public boolean instanceCheck(Object obj);
	
	public void generate(Object obj, String name, Element root) throws Exception;
	
}
