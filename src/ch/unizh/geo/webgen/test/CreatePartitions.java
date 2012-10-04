package ch.unizh.geo.webgen.test;

import java.util.Collections;
import java.util.HashMap;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.service.AreaPartitioningFlexible;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.ShapefileReader;
import com.vividsolutions.jump.io.ShapefileWriter;
import com.vividsolutions.jump.io.datasource.DataSource;

public class CreatePartitions {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String inpath = "T:\\neun\\daten\\";
		String outpath = "T:\\neun\\partitions\\";
		DriverProperties wproperties = new DriverProperties();
		ShapefileReader reader = new ShapefileReader();
		ShapefileWriter writer = new ShapefileWriter();
		try {
			wproperties.putAll(Collections.singletonMap(DataSource.FILE_KEY, inpath+"testdaten\\nyontest-geb.shp"));
			FeatureCollection geom = reader.read(wproperties);
			wproperties.putAll(Collections.singletonMap(DataSource.FILE_KEY, inpath+"testdaten\\nyontest-str.shp"));
			FeatureCollection congeom = reader.read(wproperties);
			
			//partioning support service
	    	WebGenRequest preq = new WebGenRequest();
	    	preq.addFeatureCollection("geom", geom);
	    	preq.addFeatureCollection("congeom", congeom);
	    	(new AreaPartitioningFlexible()).run(preq);
	    	HashMap fc_partitions = preq.getResults();
	    	
	    	int partition_cnt = (int)fc_partitions.size()/2;
	    	for(int i=0; i<partition_cnt; i++) {
        		ConstrainedFeatureCollection partgeom = (ConstrainedFeatureCollection)fc_partitions.get("partition" + (i+1));
        		ConstrainedFeatureCollection partcongeom = (ConstrainedFeatureCollection)fc_partitions.get("partpoly" + (i+1));
        		if(partgeom.size() > 0 && partcongeom.size() > 0) {
        			String outfilenbr = ""+System.currentTimeMillis()+i;
            		wproperties.putAll(Collections.singletonMap(DataSource.FILE_KEY, outpath+"partition"+outfilenbr+".shp"));
            		writer.write(partgeom, wproperties);
            		wproperties.putAll(Collections.singletonMap(DataSource.FILE_KEY, outpath+"partpoly"+outfilenbr+".shp"));
            		writer.write(partcongeom, wproperties);
        		}
        	}
		} catch (Exception e) {e.printStackTrace(); return;}
	}

}
