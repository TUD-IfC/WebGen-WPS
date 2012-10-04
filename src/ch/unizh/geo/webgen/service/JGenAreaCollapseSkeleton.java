package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.axpand.jaxpand.genoperator.area.GenAreaCollapseSkeleton;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;

public class JGenAreaCollapseSkeleton extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		double dMinEndSegmentLength = wgreq.getParameterDouble("dMinEndSegmentLength");
		GenAreaCollapseSkeleton gacs = new GenAreaCollapseSkeleton();
		gacs.init(dMinEndSegmentLength);
		Polygon p; Feature f;
		for(Iterator iter = fc.iterator(); iter.hasNext();) {
			try {
				f = (Feature) iter.next();
				p = (Polygon) f.getGeometry();
				gacs.addDataPolygon(p);
			}
			catch (ClassCastException e) {
				this.addError("only polygons can be skeletonized");
				return;
			}
		}
		if(gacs.execute()) {
			ConstrainedFeatureCollection fcr = new ConstrainedFeatureCollection(fc.getFeatureSchema());
			f = (Feature)fc.getFeatures().get(0);
			f.setGeometry(gacs.getDataLines());
			fcr.add(f);
			wgreq.addResult("result", fcr);
		}
	}

	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("JGenAreaCollapseSkeleton", "neun", "operator",
				"",
				"JGenAreaCollapseSkeleton",
				"GenAreaCollapseSkeleton from jaxpand-genoperators",
				"1.0",
				new String[] {"ica.genops.cartogen.Enhancement"});
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("dMinEndSegmentLength", "DOUBLE", "10.0", "dMinEndSegmentLength");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", new String[] {"MultiLineString"}), "simplified geometries");
		return id;
	}
}
