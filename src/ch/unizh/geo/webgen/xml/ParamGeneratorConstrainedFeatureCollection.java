package ch.unizh.geo.webgen.xml;

import java.util.Iterator;

import org.dom4j.Element;
import org.dom4j.QName;

import ch.unizh.geo.webgen.model.ConstrainedFeature;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;

public class ParamGeneratorConstrainedFeatureCollection implements IXMLParamGenerator {
	
	/*public Class getParamType() {
		//return FeatureCollection.class;
		return FeatureDataset.class;
	}*/
	
	public boolean instanceCheck(Object obj) {
		if(obj instanceof ConstrainedFeatureCollection) return true;
		else return false;
	}
	
	public void generate(Object obj, String name, Element root) throws Exception {
		try {
			//add namespace for gml geometries
			root.addNamespace("gml", "http://www.opengis.net/gml");
			//create parameter tag
			QName paramname = QName.get("Parameter", root.getNamespace());
			Element fcel = root.addElement(paramname);
			fcel.addAttribute("name", name);
			fcel.addAttribute("type", "ConstrainedFeatureCollection");
			encodeConstrainedFeatureCollection(fcel, (ConstrainedFeatureCollection) obj);
			}
		catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error generating parameter type ConstrainedFeatureCollection", e);
		}
	}
	
	private void encodeConstrainedFeatureCollection(Element paramel, ConstrainedFeatureCollection fc) throws Exception {
		//add feature schema
		Element fsel = paramel.addElement("FeatureSchema");
		FeatureSchema fs = fc.getFeatureSchema();
        int fslen = fs.getAttributeCount();
        String[] fschemanames = new String[fslen];
        for(int i=0;i<fslen;i++) {
        	Element attel = fsel.addElement("Attribute");
        	String namestring = fs.getAttributeName(i).toString();
        	fschemanames[i] = namestring;
			Element attn = attel.addElement("name");
			attn.setText(namestring);
			Element attt = attel.addElement("type");
			attt.setText(fs.getAttributeType(i).toString());
		}
        
        //add feature set
        Element fsetel = paramel.addElement("FeatureSet");
        GMLGeometryGenerator gmlgen = new GMLGeometryGenerator();
		//gmlgen.setGID("-1");
		for (Iterator i = fc.iterator(); i.hasNext();) {
			Feature f = (Feature)i.next();
			Element fel = fsetel.addElement("Feature");
			if(f.getSchema().hasAttribute("wgid")) {
				fel.addAttribute("wgid", ""+f.getAttribute("wgid"));
			}
			else {
				fel.addAttribute("wgid", ""+f.getID());
			}
			//write attributes (encode Feature-Attributes)
			for(int ifa=0;ifa<fslen;ifa++) {
				Element fpel = fel.addElement("Property");
				fpel.addAttribute("name", fschemanames[ifa]);
				Object actatt = f.getAttribute(ifa);
				if(actatt instanceof Geometry) {
					gmlgen.write(fpel, f.getGeometry());
				}
				else {
					fpel.addText(actatt.toString());
				}
			}
			if(f instanceof ConstrainedFeature)
				AttributeConstraint.encodeConstraint(fel, ((ConstrainedFeature)f).getConstraint());
		}
		//System.out.println("sent");
	}
	
}
