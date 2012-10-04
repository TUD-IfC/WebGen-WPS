package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.index.quadtree.Quadtree;
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
public class NodeLineStrings extends AWebGenAlgorithm implements IWebGenAlgorithm  {

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
		GeometryFactory geomfact = new GeometryFactory();
		FeatureCollection fcnew = new FeatureDataset(fc.getFeatureSchema());
		ArrayList<LineString> linesegments = new ArrayList<LineString>();
		LOGGER.info("NodeLineStrings: initalizing qtree");
		Quadtree qtree = new Quadtree();
		Feature f; LineString ls;
		for (Iterator i = fc.iterator(); i.hasNext();) {
            f = (Feature)i.next();
            try {
            	ls = (LineString)f.getGeometry();
            	Coordinate[] lsc = ls.getCoordinates();
            	for(int il = 0; il < lsc.length-1; il++) {
            		LineString lsg = geomfact.createLineString(new Coordinate[]{lsc[il],lsc[il+1]});
            		linesegments.add(lsg);
            		qtree.insert(lsg.getEnvelope().getEnvelopeInternal(),f);
            	}
            }
            catch(Exception e) {}
            fcnew.add(f);
        }
		
		LOGGER.info("NodeLineStrings: processing 0/" + linesegments.size() + " linesegments");
		ArrayList oldf = new ArrayList();
		int infoc = 0; int infocx = 100;
		for (LineString lsg : linesegments) {
            List tfeats = qtree.query(lsg.getEnvelope().getEnvelopeInternal());
            for(Iterator tfi = tfeats.iterator(); tfi.hasNext();) {
            	Feature tf = (Feature)tfi.next();
            	LineString tls = (LineString)tf.getGeometry();
            	if(tls.intersects(lsg) && !tls.covers(lsg)) {
            		Geometry mls = tls.difference(lsg);
                	if(mls instanceof MultiLineString) {
                		//if(!mls.getGeometryN(0).touches(mls.getGeometryN(1))) break;
                		//tf.setGeometry(mls.getGeometryN(0));
                		for(int mlsi=0; mlsi<mls.getNumGeometries(); mlsi++) {
                			Feature tf2 = tf.clone(true);
                    		tf2.setGeometry(mls.getGeometryN(mlsi));
                    		fcnew.add(tf2);
                    		qtree.insert(mls.getGeometryN(mlsi).getEnvelope().getEnvelopeInternal(),tf2);
                		}
                		qtree.remove(lsg.getEnvelope().getEnvelopeInternal(), tf);
                		oldf.add(tf);
                	}
            	}
            }
            if(infoc == 100) {
            	LOGGER.info("NodeLineStrings: processing " + infocx + "/" + linesegments.size() + " linesegments");
            	infoc = 0;
            	infocx += 100;
            }
            infoc++;
        }
		fcnew.removeAll(oldf);
		return fcnew;
	}
	
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("NodeLineStrings", "neun", "operator",
				"",
				"NodeLineStrings",
				"Node LineStrings",
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
