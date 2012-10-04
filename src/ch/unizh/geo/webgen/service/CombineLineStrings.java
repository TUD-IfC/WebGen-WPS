package ch.unizh.geo.webgen.service;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.operation.linemerge.LineMerger;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureUtil;

/**
 * @descrption:
 * 		filter features of one or multiple classes (,separated)
 *      --> returns FeatureCollection without those classes
 * 			
 * @author neun
 *
 * 
 */
public class CombineLineStrings extends AWebGenAlgorithm implements IWebGenAlgorithm  {

    public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		try {
			FeatureCollection fcnew = execute(fc);
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
            	if(lsn > 2) {
            		qtree.insert(ls.getPointN(1).getEnvelope().getEnvelopeInternal(),f);
                	qtree.insert(ls.getPointN(lsn-2).getEnvelope().getEnvelopeInternal(),f);
            	}
            	else {
            		qtree.insert(ls.getStartPoint().getEnvelope().getEnvelopeInternal(),f);
                	qtree.insert(ls.getEndPoint().getEnvelope().getEnvelopeInternal(),f);
            	}
            }
            catch(Exception e) {}
        }
		
		FeatureCollection fcnew = new FeatureDataset(fc.getFeatureSchema());
		LineMerger lm = new LineMerger();
		for (Iterator i = fc.iterator(); i.hasNext();) {
            f = (Feature)i.next();
            ls = (LineString)f.getGeometry();
            lm.add(ls);
        }
		
		List cf;
		Collection lines = lm.getMergedLineStrings();
		for (Iterator i = lines.iterator(); i.hasNext();) {
			f = new BasicFeature(fc.getFeatureSchema());
			ls = (LineString)i.next();
			cf = qtree.query(ls.getPointN(1).getEnvelope().getEnvelopeInternal());
			if(cf.size() == 1) FeatureUtil.copyAttributes((Feature)cf.get(0), f);
			else if(cf.size() > 1) {
				Feature tf;
				for(Iterator ic = cf.iterator(); ic.hasNext();) {
					tf = (Feature)ic.next();
					if(tf.getGeometry().coveredBy(ls)) {
						FeatureUtil.copyAttributes(tf, f);
					}
				}
			}
			else System.out.println("attribs fehlen");
			f.setGeometry(ls);
			fcnew.add(f);
		}
		return fcnew;
	}
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("CombineLineStrings", "neun", "operator",
				"",
				"CombineLineStrings",
				"Combine Roads",
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
