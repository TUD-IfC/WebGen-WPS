package ch.unizh.geo.webgen.registry;

import java.util.Iterator;
import java.util.Vector;

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;


public class ParameterDescription {

	public String name = null;
	public String type = null;
	public String defaultvalue = null;
	public boolean haschoices = false;
	public Vector<Object> supportedvalues = new Vector<Object>();
	public String description = null;
	public Vector<AttributeDescription> attributes = new Vector<AttributeDescription>();
	
	public ParameterDescription(String name, String type, String defaultvalue, String description) {
		this.name = name;
		this.type = type;
		this.defaultvalue = defaultvalue;
		this.description = description;
	}
		
	public ParameterDescription(String name, String type, String defaultvalue, Object[] supportedvalues, String description) {
		this.name = name;
		this.type = type;
		this.defaultvalue = defaultvalue;
		this.description = description;
		for(int i=0; i< supportedvalues.length; i++) {
			this.supportedvalues.add(supportedvalues[i]);
		}
	}
	
	public ParameterDescription(String name, String type, double defaultvalue, double min, double max, String description) {
		this.name = name;
		this.type = type;
		this.defaultvalue = ""+defaultvalue;
		this.description = description;
		this.addSupportedDoubleInterval(min, max);
	}
	
	public ParameterDescription(String name, String type, int defaultvalue, int min, int max, String description) {
		this.name = name;
		this.type = type;
		this.defaultvalue = ""+defaultvalue;
		this.description = description;
		this.addSupportedIntegerInterval(min, max);
	}
	
	public ParameterDescription(String name, String type, Vector<AttributeDescription> attributes, boolean makegeom, String description) {
		this.name = name;
		this.type = type;
		this.description = description;
		if((attributes == null) || makegeom) {
			this.attributes.add(new AttributeDescription("GEOMETRY", "GEOMETRY"));
		}
		else {
			this.attributes.addAll(attributes);
		}
	}
	
	public ParameterDescription(String name, String type, AttributeDescription attribute, String description) {
		this.name = name;
		this.type = type;
		this.description = description;
		if(attribute == null) {
			this.attributes.add(new AttributeDescription("GEOMETRY", "GEOMETRY"));
		}
		else {
			this.attributes.add(attribute);
		}
	}
	
	public ParameterDescription(String name, String type, String description) {
		this.name = name;
		this.type = type;
		this.description = description;
	}
	
	public void addAttribute(AttributeDescription attribute) {
		this.attributes.add(attribute);
	}
	
	public void addSupportedValue(Object value) {
		this.supportedvalues.add(value);
	}
	
	public void addSupportedIntegerInterval(int min, int max) {
		this.supportedvalues.add(new IntegerInterval(min, max));
	}
	
	public void addSupportedDoubleInterval(double min, double max) {
		this.supportedvalues.add(new DoubleInterval(min, max));
	}
	
	public void setChoiced() {
		this.haschoices = true;
	}
	
	public boolean hasChoices() {
		return haschoices;
	}
	
	public Vector<Object> getChoices() {
		Vector<Object> choices = new Vector<Object>();
		for(Object o : supportedvalues) {
			if(!(o instanceof Interval)) choices.add(o);
		}
		return choices;
	}
	
	public void makeXMLDescription(Element paramel) {
		Namespace webgenns = paramel.getNamespace();
		
		if(name != null) {
			Element nameel = paramel.addElement(QName.get("name", webgenns));
			nameel.setText(name);
		}
		if(type != null) {
			Element typeel = paramel.addElement(QName.get("type", webgenns));
			typeel.setText(type);
		}
		if(defaultvalue != null) {
			Element defaultel = paramel.addElement(QName.get("default", webgenns));
			defaultel.setText(defaultvalue);
		}
		
		if(haschoices) {
			Element choiceel = paramel.addElement(QName.get("choices", webgenns));
			choiceel.setText("true");
		}
		
		if(supportedvalues.size() > 0) {
			addSupportedValues(paramel, supportedvalues);
		}
		
		for(Iterator iter = attributes.iterator(); iter.hasNext();) {
			AttributeDescription tattrib = (AttributeDescription)iter.next();
			Element attributeel = paramel.addElement(QName.get("Attribute", webgenns));
			Element nameel = attributeel.addElement(QName.get("name", webgenns));
			nameel.setText(tattrib.name);
			Element typeel = attributeel.addElement(QName.get("type", webgenns));
			typeel.setText(tattrib.type);
			if(tattrib.supportedvalues.size() > 0) {
				addSupportedValues(attributeel, tattrib.supportedvalues);
			}
		}
		
		if(description != null) {
			Element descriptionel = paramel.addElement(QName.get("description", webgenns));
			descriptionel.setText(description);
		}
	}
	
	private void addSupportedValues(Element el, Vector values) {
		Namespace webgenns = el.getNamespace();
		Element supportedvaluesel = el.addElement(QName.get("SupportedValues", webgenns));
		for(Iterator iter = values.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if((obj instanceof IntegerInterval) || (obj instanceof DoubleInterval)) {
				Element valueintervalel = supportedvaluesel.addElement(QName.get("ValueInterval", webgenns));
				Element valueminel = valueintervalel.addElement(QName.get("valuemin", webgenns));
				valueminel.setText(((Interval)obj).getMinString());
				Element valuemaxel = valueintervalel.addElement(QName.get("valuemax", webgenns));
				valuemaxel.setText(((Interval)obj).getMaxString());
			}
			else {
				Element valueel = supportedvaluesel.addElement(QName.get("value", webgenns));
				valueel.setText(obj.toString());
			}
		}
	}
	
	
	public ParameterDescription(Element paramel) {
		this.name = paramel.elementText("name");
		this.type = paramel.elementText("type");
		this.defaultvalue = paramel.elementText("default");
		this.description = paramel.elementText("description");
		try {
			this.haschoices = Boolean.parseBoolean(paramel.elementText("choices"));
		} catch (Exception e) {}
		
		parseSupportedValues(paramel.element("SupportedValues"), this.type, this.supportedvalues);
		
		for (Iterator il = paramel.elementIterator("Attribute"); il.hasNext();) {
        	Element attribel = (Element)il.next();
        	String attribname = attribel.elementText("name");
        	String attribtype = attribel.elementText("type");
        	AttributeDescription attribdesc = new AttributeDescription(attribname, attribtype);
        	parseSupportedValues(attribel.element("supportedvalues"), attribtype, attribdesc.supportedvalues);
		}
	}
	
	private void parseSupportedValues(Element supportedvaluesel, String type, Vector<Object> svalues) {
		if(supportedvaluesel == null) return;
		for (Iterator il = supportedvaluesel.elementIterator("value"); il.hasNext();) {
			String eltext = ((Element)il.next()).getTextTrim();
			Object suppobj;
			try {
				suppobj = new Integer(Integer.parseInt(eltext));
			} catch (Exception e1) {
				try {
					suppobj = new Double(Double.parseDouble(eltext));
				} catch (Exception e2) {
					suppobj = eltext;
				}
			}
			svalues.add(suppobj);
		}
		for (Iterator il = supportedvaluesel.elementIterator("ValueInterval"); il.hasNext();) {
			Element tvi = (Element)il.next();
			try {
				if(this.type.equals("INTEGER")) svalues.add(new IntegerInterval(Integer.parseInt(tvi.elementText("min")), Integer.parseInt(tvi.elementText("max"))));
				else if(this.type.equals("DOUBLE")) svalues.add(new DoubleInterval(Double.parseDouble(tvi.elementText("min")), Double.parseDouble(tvi.elementText("max"))));
			}
			catch (Exception e) {}
		}
	}
}
