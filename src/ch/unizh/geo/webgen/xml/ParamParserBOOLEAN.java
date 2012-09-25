package ch.unizh.geo.webgen.xml;

import org.dom4j.Element;

import ch.unizh.geo.webgen.server.WebGenRequest;

public class ParamParserBOOLEAN implements IXMLParamParser {
	
	public String getParamType() {
		return "BOOLEAN";
	}
	
	public void parse(WebGenRequest wgreq, Element el) throws Exception {
		try {
			String name = el.attributeValue("name");
			String vtext = el.getTextTrim();
			Boolean value = Boolean.valueOf(vtext);
			wgreq.addParameter(name, value);
		}
		catch (Exception e) {
			throw new Exception("Error parsing parameter type BOOLEAN", e);
		}
	}
	
}
