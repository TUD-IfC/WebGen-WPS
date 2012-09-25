package ch.unizh.geo.webgen.xml;

import java.util.List;

import org.dom4j.Element;

import ch.unizh.geo.webgen.server.WebGenRequest;

public class ParamParserINTEGERARRAY implements IXMLParamParser {
	
	public String getParamType() {
		return "INTEGERARRAY";
	}
	
	public void parse(WebGenRequest wgreq, Element el) throws Exception {
		try {
			String name = el.attributeValue("name");
			List items = el.elements();
			Integer[] value = new Integer[items.size()];
			Element tel;
			for (int i=0; i<items.size(); i++) {
				tel = (Element)items.get(i);
				value[i] = Integer.parseInt(tel.getTextTrim());
			}
			wgreq.addParameter(name, value);
		}
		catch (Exception e) {
			throw new Exception("Error parsing parameter type INTEGERARRAY", e);
		}
	}
	
}
