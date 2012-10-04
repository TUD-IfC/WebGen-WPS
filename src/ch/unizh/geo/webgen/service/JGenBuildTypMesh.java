package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.registry.ParameterDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.axpand.jaxpand.genoperator.building.GenBuildTypMesh;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;

public class JGenBuildTypMesh extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		
		GenBuildTypMesh gbtm = new GenBuildTypMesh();
		
		String geometryTypeString = wgreq.getParameter("geometryType").toString();
		int geometryType;
		if(geometryTypeString.equals("POINT")) geometryType = GenBuildTypMesh.CLASS_POINT;
		else if(geometryTypeString.equals("LINE")) geometryType = GenBuildTypMesh.CLASS_LINE;
		else geometryType = GenBuildTypMesh.CLASS_AREA;
		
		double dMinimumShortestLength = wgreq.getParameterDouble("dMinimumShortestLength");
		int nTargetNofObjects = wgreq.getParameterInt("nTargetNofObjects");
		
		if((dMinimumShortestLength != 0.0) && (nTargetNofObjects != 0)) {
			this.addError("Both dMinimumShortestLength and nTargetNofObjects can't be 0.");
			return;
		}
		else if(dMinimumShortestLength == 0.0) gbtm.init(nTargetNofObjects, geometryType);
		else if(nTargetNofObjects == 0) gbtm.init(dMinimumShortestLength, geometryType);
		else gbtm.init(dMinimumShortestLength, nTargetNofObjects, geometryType);
		
		Feature f;
		for(Iterator iter = fc.iterator(); iter.hasNext();) {
			try {
				f = (Feature) iter.next();
				gbtm.addData(f.getGeometry(), f);
			}
			catch (ClassCastException e) {
				this.addError("only polygons can be aggregated");
				return;
			}
		}
		if(gbtm.execute()) {
			ConstrainedFeatureCollection fcr = new ConstrainedFeatureCollection(fc.getFeatureSchema());
			for(int i = 0; i < gbtm.getDataCount() ; i++) {
				f = (Feature)gbtm.getDataLinkedObject(i);
				f.setGeometry(gbtm.getDataGeometry(i));
				fcr.add(f);
			}
			wgreq.addResult("result", fcr);
		}
	}

	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("JGenBuildTypMesh", "neun", "operator",
				"",
				"JGenBuildTypMesh",
				"GenBuildTypMesh from jaxpand-genoperators",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Point", "LineString", "Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("dMinimumShortestLength", "DOUBLE", "10.0", "aggregation distance tolerance");
		id.addInputParameter("nTargetNofObjects", "INTEGER", "", "aggregation distance tolerance");
		
		ParameterDescription geometrytype = new ParameterDescription("geometryType", "STRING", "AREA", "type of geometries to typify");
		geometrytype.addSupportedValue("POINT");
		geometrytype.addSupportedValue("LINE");
		geometrytype.addSupportedValue("AREA");
		geometrytype.setChoiced();
		id.addInputParameter(geometrytype);
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
