package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;

/**
 * @descrption:
 * 		filter duplex features
 *      --> returns FeatureCollection without those classes
 * 			
 * @author neun
 *
 * 
 */
public class FilterDuplexLineStrings extends AWebGenAlgorithm implements IWebGenAlgorithm  {

    public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		try {
			FeatureCollection fcnew = execute(fc);
			fcnew = execute(fcnew);
			wgreq.addResult("result", fcnew);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	
	public FeatureCollection execute(FeatureCollection fc) throws Exception {
		Quadtree qtree = new Quadtree();
		Feature f; LineString ls;
		for (Iterator i = fc.iterator(); i.hasNext();) {
            f = (Feature)i.next();
            try {
            	ls = (LineString)f.getGeometry();
            	int lsn = ls.getNumPoints();
            	if(lsn > 3) {
            		qtree.insert(ls.getPointN(1).getEnvelope().getEnvelopeInternal(),f);
                	qtree.insert(ls.getPointN(lsn-2).getEnvelope().getEnvelopeInternal(),f);
            	}
            	else if(lsn == 3) {
            		qtree.insert(ls.getPointN(1).getEnvelope().getEnvelopeInternal(),f);
            	}
            	else if(lsn == 2) {
            		qtree.insert(ls.getStartPoint().getEnvelope().getEnvelopeInternal(),f);
                	qtree.insert(ls.getEndPoint().getEnvelope().getEnvelopeInternal(),f);
            	}
            }
            catch(Exception e) {}
        }
		
		FeatureCollection fcnew = new FeatureDataset(fc.getFeatureSchema());
		List cf; Envelope tenv;
		for (Iterator i = fc.iterator(); i.hasNext();) {
			f = (Feature)i.next();
            ls = (LineString)f.getGeometry();
            tenv = ls.getPointN(1).getEnvelope().getEnvelopeInternal();
            cf = qtree.query(tenv);
			if(cf.size() == 0) System.out.println("attribs fehlen ganz und gar");
			if(cf.size() == 1) {
				fcnew.add((Feature)cf.get(0));
				qtree.remove(tenv,(Feature)cf.get(0));
			}
			else if(cf.size() > 1) {
				Feature tf; ArrayList tdubl = new ArrayList();
				for(Iterator ic = cf.iterator(); ic.hasNext();) {
					tf = (Feature)ic.next();
					if(tf.getGeometry().coveredBy(ls)) {
						tdubl.add(tf);
						qtree.remove(tenv,tf);
					}
				}
				if(tdubl.size() > 0) {
					fcnew.add((Feature)tdubl.get(0));
				}
				else System.out.println("attribs fehlen");
			}
		}
		return fcnew;
	}
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("FilterDuplexLineStrings", "neun", "operator",
				"",
				"FilterDuplexLineStrings",
				"Filter Duplex Roads",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowedS = {"LineString"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedS), "all streets");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
