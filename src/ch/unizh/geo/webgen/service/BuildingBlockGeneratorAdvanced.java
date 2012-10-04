package ch.unizh.geo.webgen.service;

import java.util.Iterator;
import java.util.List;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureUtil;

public class BuildingBlockGeneratorAdvanced extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection streets = wgreq.getFeatureCollection("streets");
		FeatureCollection partition = wgreq.getFeatureCollection("partition");
		if(streets.getFeatureSchema().hasAttribute("SIGWIDTH")) {
			FeatureCollection fcnew = runFilling(partition, streets);
			wgreq.addResult("result", fcnew);
		}
	}

	private FeatureCollection runFilling(FeatureCollection partition, FeatureCollection streets) {
		FeatureCollection fcp = new FeatureDataset(partition.getFeatureSchema());
		
		//generate street qtree
		Quadtree qtree = new Quadtree();
		
		for (Iterator is = streets.iterator(); is.hasNext();) {
			Feature fs = (Feature) is.next();
			Geometry gs = fs.getGeometry();
			qtree.insert(gs.getEnvelope().getEnvelopeInternal(),fs);
		}
		
		int mindist;
		Geometry actl, buff;
		//get partitions and process (usually only one partition)
		for (Iterator ip = partition.iterator(); ip.hasNext();) {
			Feature fp = (Feature) ip.next();
			Polygon gp = (Polygon)fp.getGeometry();
			Feature fpn = new BasicFeature(fp.getSchema());
			FeatureUtil.copyAttributes(fp, fpn);
			Geometry gpn = gp;
			Envelope genv = gp.getEnvelopeInternal();
			List instreet = qtree.query(genv);
			for(Iterator iin = instreet.iterator(); iin.hasNext();) {
				Feature fiin = (Feature) iin.next();
				mindist = ((Integer)fiin.getAttribute("SIGWIDTH")).intValue();
				actl = fiin.getGeometry();
				boolean inters = actl.intersects(gpn);
				buff = actl.buffer(mindist/2);
				if(inters){
					//System.out.print("aha");
					try {
						gpn = gpn.difference(buff);
					}
					catch(Exception e){
						buff = buff.buffer(Math.random()*0.0001);
						gpn = gpn.buffer(Math.random()*0.0001);
						gpn = gpn.difference(buff);
					}
				}
			}
			fp.setGeometry(gpn);
			fcp.add(fpn);
		}
		return fcp;
	}
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("BuildingBlockGeneratorAdvanced",  "neun", "support",
				"",
				"BuildingBlockGeneratorAdvanced",
				"Creates a built up area polygon from partitions and dead end roads, needs attribute SIGWIDTH for every street",
				"1.0");
		
		//add input parameters
		String[] allowedP = {"Polygon"};
		id.addInputParameter("partition", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedP), "partition polygons");
		String[] allowedS = {"LineString"};
		id.addInputParameter("streets", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedS), "all streets (with dead ends)");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}