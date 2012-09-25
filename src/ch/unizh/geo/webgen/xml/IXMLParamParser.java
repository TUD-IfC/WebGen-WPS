package ch.unizh.geo.webgen.xml;

import org.dom4j.Element;

import ch.unizh.geo.webgen.server.WebGenRequest;

public interface IXMLParamParser {

	public String getParamType();
	
	public void parse(WebGenRequest wgreq, Element el) throws Exception;
	
}
