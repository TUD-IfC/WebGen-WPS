package ch.unizh.geo.webgen.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.ShapefileReader;
import com.vividsolutions.jump.io.ShapefileWriter;
import com.vividsolutions.jump.io.datasource.DataSource;

public class ArcGISSimplifyLines extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		double maxdist = wgreq.getParameterDouble("maxdist");
		FeatureCollection fcnew = simplifyLines(fc, maxdist);
		wgreq.addResult("result", fcnew);
	}

	private FeatureCollection simplifyLines(FeatureCollection fc, double maxdist) {
		try {
			String inshp = "C:\\java\\eclipse\\workspace\\WebGen2006\\work\\tinput.shp";
			String resshp = "C:\\java\\eclipse\\workspace\\WebGen2006\\work\\tresult.shp";
			
			DriverProperties wproperties = new DriverProperties();
			wproperties.putAll(Collections.singletonMap(DataSource.FILE_KEY, inshp));
			ShapefileWriter writer = new ShapefileWriter();
			//WebGenShapefileWriter writer = new WebGenShapefileWriter();
			writer.write(fc, wproperties);
			
			Process ls = Runtime.getRuntime().exec("C:\\Python24\\python X:\\neun\\eclipse\\workspace\\WebGen2006\\arcscript.py");
			InputStream stdin = ls.getInputStream();
            InputStreamReader isr = new InputStreamReader(stdin);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            StringBuffer output = new StringBuffer();
            while ( (line = br.readLine()) != null)
                output.append(line);
			this.addMessage(output.toString());
			
			DriverProperties rproperties = new DriverProperties();
			rproperties.putAll(Collections.singletonMap(DataSource.FILE_KEY, resshp));
			ShapefileReader reader = new ShapefileReader();
			FeatureCollection fcnew = reader.read(rproperties);
			return fcnew;
		}
		catch (Exception e) {
			addErrorStack(e.getLocalizedMessage(), e.getStackTrace());
			return null;
		}
	}
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("ArcGISSimplifyLines", "neun", "support",
				"",
				"ArcGISSimplifyLines",
				"ArcGIS SimplifyLines",
				"1.0");
		
		//add input parameters
		String[] allowed = {"LineString"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with geometries");
		id.addInputParameter("maxdist", "DOUBLE", "30.0", "maxdist");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
}
