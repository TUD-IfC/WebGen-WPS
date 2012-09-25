package ch.unizh.geo.webgen.xml;

import org.dom4j.Element;

import ch.unizh.geo.webgen.server.WebGenRequest;

public class ParamParserMESSAGE implements IXMLParamParser {
	
	public String getParamType() {
		return "MESSAGE";
	}
	
	public void parse(WebGenRequest wgreq, Element el) throws Exception {
		try {
			String name = el.attributeValue("name");
			String vtext = el.getTextTrim();
			wgreq.addMessage(name, vtext);
		}
		catch (Exception e) {
			throw new Exception("Error parsing parameter type MESSAGE", e);
		}
	}
	
}
