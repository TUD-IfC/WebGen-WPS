package ch.unizh.geo.webgen.xml;

import org.dom4j.Element;
import org.dom4j.QName;

public class ParamGeneratorSTRING implements IXMLParamGenerator {
	
	/*public Class getParamType() {
		return Integer.class;
	}*/
	
	public boolean instanceCheck(Object obj) {
		if(obj instanceof String) return true;
		else return false;
	}
	
	public void generate(Object obj, String name, Element root) throws Exception {
		try {
			//QName paramname = new QName("Parameter", root.getNamespace());
			QName paramname = QName.get("Parameter", root.getNamespace());
			Element newel = root.addElement(paramname);
			newel.addAttribute("name", name);
			newel.addAttribute("type", "STRING");
			newel.setText(obj.toString());
		}
		catch (Exception e) {
			throw new Exception("Error generating parameter type STRING", e);
		}
	}
	
}
