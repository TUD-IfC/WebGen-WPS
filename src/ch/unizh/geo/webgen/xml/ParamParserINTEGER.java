package ch.unizh.geo.webgen.xml;

import org.dom4j.Element;

import ch.unizh.geo.webgen.server.WebGenRequest;

public class ParamParserINTEGER implements IXMLParamParser {
	
	public String getParamType() {
		return "INTEGER";
	}
	
	public void parse(WebGenRequest wgreq, Element el) throws Exception {
		try {
			String name = el.attributeValue("name");
			String vtext = el.getTextTrim();
			Integer value = Integer.parseInt(vtext);
			wgreq.addParameter(name, value);
		}
		catch (Exception e) {
			throw new Exception("Error parsing parameter type INTEGER", e);
		}
	}
	
}
