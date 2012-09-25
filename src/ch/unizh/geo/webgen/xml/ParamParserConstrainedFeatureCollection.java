package ch.unizh.geo.webgen.xml;

import java.util.Iterator;

import org.dom4j.Element;

import ch.unizh.geo.webgen.model.ConstrainedFeature;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;

public class ParamParserConstrainedFeatureCollection implements IXMLParamParser {

	public String getParamType() {
		return "ConstrainedFeatureCollection";
	}

	public void parse(WebGenRequest wgreq, Element el) throws Exception {
		try {
			String name = el.attributeValue("name");
			ConstrainedFeatureCollection value = decodeFeatureSet(el);
			wgreq.addFeatureCollection(name, value);
		} catch (Exception e) {
			throw new Exception("Error parsing parameter type FeatureCollection", e);
		}
	}
	
	public static ConstrainedFeatureCollection decodeFeatureSet(Element layerbody) throws Exception {
		//FeatureSchema fsnew = new FeatureSchema();
		ConstrainedFeatureCollection fcnew = null;
		try {
			FeatureSchema fs = decodeAttributeSchema(layerbody);
			fcnew = decodeFeatures(layerbody, fs);
		}
		catch (Exception e) {
			e.printStackTrace();
			}
		return fcnew;
	}

	public static FeatureSchema decodeAttributeSchema(Element layerbody)
			throws Exception {
		FeatureSchema fsnew = new FeatureSchema();
		try {
			Element schema = layerbody.element("FeatureSchema");
			for (Iterator siter = schema.elementIterator(); siter.hasNext();) {
				Element tatt = (Element)siter.next();
				decodeAttribute(tatt, fsnew);
			}
		} catch (Exception e) {}
		return fsnew;
	}

	public static void decodeAttribute(Element el, FeatureSchema fsnew) throws Exception {
		String name = el.element("name").getTextTrim();
		String type = el.element("type").getTextTrim();
		if (type.equals("GEOMETRY"))
			fsnew.addAttribute(name, AttributeType.GEOMETRY);
		else if (type.equals("DOUBLE"))
			fsnew.addAttribute(name, AttributeType.DOUBLE);
		else if (type.equals("INTEGER"))
			fsnew.addAttribute(name, AttributeType.INTEGER);
		else if (type.equals("OBJECT"))
			fsnew.addAttribute(name, AttributeType.OBJECT);
		else
			fsnew.addAttribute(name, AttributeType.STRING);
	}

	public static ConstrainedFeatureCollection decodeFeatures(Element parameterEl, FeatureSchema fsnew) throws Exception {
		ConstrainedFeatureCollection fcnew = new ConstrainedFeatureCollection(fsnew);
		Element featureset = parameterEl.element("FeatureSet");
		for (Iterator fiter = featureset.elementIterator(); fiter.hasNext();) {
			Element tn = (Element)fiter.next();
			Feature tf = decodeFeature(tn, fsnew);
			fcnew.add(tf);
		}
		return fcnew;
	}

	public static ConstrainedFeature decodeFeature(Element el, FeatureSchema fsnew)
			throws Exception {
		ConstrainedFeature tmpf = new ConstrainedFeature(fsnew);
		for (Iterator faiter = el.elementIterator("Property"); faiter.hasNext();) {
			Element propel = (Element)faiter.next(); //Property element
			String propname = propel.attributeValue("name");
			AttributeType proptype = fsnew.getAttributeType(propname);
			String propvalue = "";
			try {propvalue = propel.getTextTrim();} catch (Exception e) {}
			if (proptype == AttributeType.GEOMETRY) {
				Element gnode = (Element)propel.elements().get(0);
				tmpf.setAttribute(propname, (new GMLGeometryParser()).read(gnode));
			}
			else if (proptype == AttributeType.INTEGER)
				tmpf.setAttribute(propname, new Integer(Integer.parseInt(propvalue)));
			else if (proptype == AttributeType.DOUBLE)
				tmpf.setAttribute(propname, new Double(Double.parseDouble(propvalue)));
			else if (proptype == AttributeType.OBJECT) {
				Element onode = (Element)propel.elements().get(0);
				decodeObject(tmpf, propname, onode);
			}
			else
				tmpf.setAttribute(propname, propvalue);
		}
		Element constrel = el.element("Constraint");
		tmpf.setConstraint(AttributeConstraint.decodeConstraint(constrel));
		return tmpf;
	}
	
	public static void decodeObject(Feature tmpf, String propname, Element objnode) {
		tmpf.setAttribute(propname, null);
	}
}
