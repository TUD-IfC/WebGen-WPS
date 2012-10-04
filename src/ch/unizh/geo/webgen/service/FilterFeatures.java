package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;

/**
 * @descrption:
 * 		filter features of one or multiple classes (,separated)
 *      --> returns FeatureCollection without those classes
 * 			
 * @author neun
 *
 * 
 */
public class FilterFeatures extends AWebGenAlgorithm implements IWebGenAlgorithm  {

	String[] filters;
	
    public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("features");
		String classname = wgreq.getParameter("classname").toString();
		String filterstring = wgreq.getParameter("filters").toString();
		try {
			String[] filterarray = filterstring.split(",");
			FeatureCollection fcnew = filter(fc, classname, filterarray);
			wgreq.addResult("result", fcnew);
		}
		catch(Exception e) {}
	}

	
	public FeatureCollection filter(FeatureCollection fc, String classname, String[] filterarray) throws Exception {
		filters = filterarray;
		if(!fc.getFeatureSchema().hasAttribute(classname)) return fc;
		FeatureCollection fcnew = new FeatureDataset(fc.getFeatureSchema());
		Feature f; String actclass;
		for (Iterator i = fc.iterator(); i.hasNext();) {
            f = (Feature)i.next();
            actclass = f.getAttribute(classname).toString();
            if(filterNot(actclass)) {
            	fcnew.add(f);
            }
        }
		return fcnew;
	}
	
	private boolean filterNot(String actclass) {
		String tclass = actclass.trim();
		for(int i=0; i<filters.length;i++) {
			if(filters[i].equals(tclass)) return false;
		}
		return true;
	}
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("FilterFeatures", "neun", "operator",
				"",
				"FilterFeatures",
				"Filter Features",
				"1.0");
		id.visible = true;
		
		//add input parameters
		id.addInputParameter("features", "FeatureCollection", "", "layer to filter");
		id.addInputParameter("classname", "STRING", "OBJECTVAL", "classname");
		id.addInputParameter("filters", "STRING", "Q_Klass,Parkweg,5_Klass,6_Klass", "filters (commaseparated)");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
