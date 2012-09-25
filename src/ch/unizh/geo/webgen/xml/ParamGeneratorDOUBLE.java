package ch.unizh.geo.webgen.xml;

import org.dom4j.Element;
import org.dom4j.QName;

public class ParamGeneratorDOUBLE implements IXMLParamGenerator {
	
	/*public Class getParamType() {
		return Double.class;
	}*/
	
	public boolean instanceCheck(Object obj) {
		if(obj instanceof Double) return true;
		else return false;
	}
	
	public void generate(Object obj, String name, Element root) throws Exception {
		try {
			//QName paramname = new QName("Parameter", root.getNamespace());
			QName paramname = QName.get("Parameter", root.getNamespace());
			Element newel = root.addElement(paramname);
			//newel.setAttributeValue("name", name);
			//newel.setAttributeValue("type", "DOUBLE");
			newel.addAttribute("name", name);
			newel.addAttribute("type", "DOUBLE");
			newel.setText(obj.toString());
		}
		catch (Exception e) {
			throw new Exception("Error generating parameter type DOUBLE", e);
		}
	}
	
}
