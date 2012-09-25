package ch.unizh.geo.webgen.xml;

import java.util.Iterator;

import org.dom4j.Element;

import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;

public class ParamParserFeatureCollection implements IXMLParamParser {

	public String getParamType() {
		return "FeatureCollection";
	}

	public void parse(WebGenRequest wgreq, Element el) throws Exception {
		try {
			String name = el.attributeValue("name");
			FeatureCollection value = decodeFeatureSet(el);
			wgreq.addFeatureCollection(name, value);
		} catch (Exception e) {
			throw new Exception("Error parsing parameter type FeatureCollection", e);
		}
	}
	
	public static FeatureCollection decodeFeatureSet(Element layerbody) throws Exception {
		//FeatureSchema fsnew = new FeatureSchema();
		FeatureCollection fcnew = null;
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

	public static FeatureCollection decodeFeatures(Element parameterEl, FeatureSchema fsnew) throws Exception {
		FeatureCollection fcnew = new FeatureDataset(fsnew);
		Element featureset = parameterEl.element("FeatureSet");
		for (Iterator fiter = featureset.elementIterator(); fiter.hasNext();) {
			Element tn = (Element)fiter.next();
			Feature tf = decodeFeature(tn, fsnew);
			fcnew.add(tf);
		}
		return fcnew;
	}

	public static Feature decodeFeature(Element el, FeatureSchema fsnew)
			throws Exception {
		//NodeList flist = node.getChildNodes();
		Feature tmpf = new BasicFeature(fsnew);
		//Element fn = el.element("Feature");
		for (Iterator faiter = el.elementIterator(); faiter.hasNext();) {
			Element propel = (Element)faiter.next(); //Property element
			String propname = propel.attributeValue("name");
			AttributeType proptype = fsnew.getAttributeType(propname);
			//Node propnode = (Node)propel.node(0);
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
		return tmpf;
	}
	
	public static void decodeObject(Feature tmpf, String propname, Element objnode) {
		if(propname.equals("constraint") && objnode.getName().equals("constraint")) {
			/*Vector actstates = new Vector();
			double origposX = 0.0;
			double origposY = 0.0;
			int origedgecount = 0;
			double origwlratio = 0.0;
			double origorientation = 0.0;
			NodeList clist = objnode.getChildNodes();
			for(int i=0; i<clist.getLength(); i++) {
				Node actnode = clist.item(i);
				String actname = actnode.getNodeName();
				String actvalue = "";
				try {actvalue = actnode.getFirstChild().getNodeValue();} catch (Exception e) {}
				if(actname.equals("state")) {
					NodeList vlist = actnode.getChildNodes();
					double[] tvalues = new double[vlist.getLength()];
					for(int j=0; j<vlist.getLength(); j++) {
						Node tvalue = vlist.item(j);
						String ttext = tvalue.getFirstChild().getNodeValue();
						tvalues[j] = Double.parseDouble(ttext);
					}
					actstates.add(tvalues);
				}
				else if(actname.equals("origpos")) {
					Node posX = actnode.getFirstChild();
					Node posY = actnode.getLastChild();
					String textX = posX.getFirstChild().getNodeValue();
					String textY = posY.getFirstChild().getNodeValue();
					origposX = Double.parseDouble(textX);
					origposY = Double.parseDouble(textY);
				}
				else if(actname.equals("origedgecount")) {
					origedgecount = Integer.parseInt(actvalue);
				}
				else if(actname.equals("origwlratio")) {
					origwlratio = Double.parseDouble(actvalue);
				}
				else if(actname.equals("origorientation")) {
					origorientation = Double.parseDouble(actvalue);
				}
			}
			GeometryFactory geometryFactory = new GeometryFactory();
			Constraint wgc = new Constraint(
					geometryFactory.createPoint(new Coordinate(origposX, origposY)),
					origedgecount, origwlratio, origorientation);
			for (Iterator viter = actstates.iterator(); viter.hasNext();) {
				wgc.addHistoryLast((double[])viter.next());
			}
			tmpf.setAttribute(propname, wgc);*/
			tmpf.setAttribute(propname, null);
		}
		else {
			tmpf.setAttribute(propname, null);
		}
	}
}
