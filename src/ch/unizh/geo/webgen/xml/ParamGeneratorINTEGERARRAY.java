package ch.unizh.geo.webgen.xml;

import org.dom4j.Element;
import org.dom4j.QName;

public class ParamGeneratorINTEGERARRAY implements IXMLParamGenerator {
		
	public boolean instanceCheck(Object obj) {
		if(obj instanceof Integer[]) return true;
		else return false;
	}
	
	public void generate(Object obj, String name, Element root) throws Exception {
		try {
			QName paramname = QName.get("Parameter", root.getNamespace());
			QName itemname = QName.get("item", root.getNamespace());
			Element newel = root.addElement(paramname);
			newel.addAttribute("name", name);
			newel.addAttribute("type", "INTEGERARRAY");
			
			Element tel;
			Integer[] doublearray = (Integer[]) obj;
			for(int i=0; i<doublearray.length; i++) {
				tel = newel.addElement(itemname);
				tel.setText(doublearray[i].toString());
			}
		}
		catch (Exception e) {
			throw new Exception("Error generating parameter type INTEGERARRAY", e);
		}
	}
	
}
