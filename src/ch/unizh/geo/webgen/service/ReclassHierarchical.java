/*
 * Created on 19.08.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author neun
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */

package ch.unizh.geo.webgen.service;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;

public class ReclassHierarchical extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		try {
			FeatureCollection fc = wgreq.getFeatureCollection("geom");
			String classfield = wgreq.getParameter("classfield").toString();
			//String hierarchy = wgreq.getParameter("hierarchy").toString();
			FeatureCollection fcnew = reclass(fc, classfield);
			wgreq.addResult("result", fcnew);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private FeatureCollection reclass(FeatureCollection fc, String classfield) throws Exception {
		HashMap<String, Element> hierarchy = new HashMap<String, Element>();
		String filename = "C://Dokumente und Einstellungen//neun//Desktop//documents//2006_12_IJGIS_resubmit//vec25-25-abbildungsregeln.xml";
		Document document = (new SAXReader()).read(new FileInputStream(filename));
		Element root = document.getRootElement();
		Element el, cel;
		for(Object elo : root.elements("HierarchyElement")) {
			el = (Element) elo;
			for(Object celo : el.elements("HierarchyElement")) {
				cel = (Element) celo;
				hierarchy.put(cel.attributeValue("value"), cel);

			}
			/*if(el.getParent().getName().equals("HierarchyElement")) {
				hierarchy.put(el.attributeValue("value"), el);
			}*/
		}
        FeatureCollection fcnew = new FeatureDataset(fc.getFeatureSchema());
        for (Iterator i = fc.iterator(); i.hasNext();) {
            Feature f = (Feature)((Feature)i.next()).clone();
            String oldclass = f.getAttribute(classfield).toString().trim();
            String newclass = hierarchy.get(oldclass).getParent().attributeValue("value");
        	f.setAttribute(classfield, newclass);
        	fcnew.add(f);
        }
        return fcnew;
    }
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("ReclassHierarchical", "neun", "support",
				"",
				"ReclassHierarchical",
				"Reclass Features",
				"1.0");
		
		//add input parameters
		String[] allowed = {"Point","LineString","Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries and classes");
		id.addInputParameter("classfield", "STRING", "OBJECTVAL", "classfield");
		id.addInputParameter("hierarchy", "STRING", "", "hierarchy");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}