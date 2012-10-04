package ch.unizh.geo.webgen.test;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.relate.RelateOp;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.ShapefileReader;
import com.vividsolutions.jump.io.ShapefileWriter;
import com.vividsolutions.jump.io.datasource.DataSource;

public class FilterTopologyProblems {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File partitionsDir = new File("T:\\neun\\partitions\\");
		ShapefileReader reader = new ShapefileReader();
		ShapefileWriter writer = new ShapefileWriter();
		DriverProperties wproperties = new DriverProperties();
		String[] partitionFiles =  partitionsDir.list();
		int pCount = 0;
		for(String partitionFile : partitionFiles) {
			String[] partitionFileS = partitionFile.split("\\.");
			if(partitionFileS[0].startsWith("partition") && partitionFileS[1].equals("shp")) {
				try {
					wproperties.putAll(Collections.singletonMap(DataSource.FILE_KEY, partitionsDir+"\\"+partitionFile));
					FeatureCollection geom = reader.read(wproperties);
					wproperties.putAll(Collections.singletonMap(DataSource.FILE_KEY, partitionsDir+"\\"+partitionFile.replaceFirst("partition", "partpoly")));
					FeatureCollection congeom = reader.read(wproperties);
					
					/*if(pCount == 25) {
						System.gc();
					}*/
					
					if(topologyOk(geom)) {
						wproperties.putAll(Collections.singletonMap(DataSource.FILE_KEY, partitionsDir+"\\filtered\\"+"partition"+pCount+".shp"));
	            		writer.write(geom, wproperties);
	            		wproperties.putAll(Collections.singletonMap(DataSource.FILE_KEY, partitionsDir+"\\filtered\\"+"streets"+pCount+".shp"));
	            		writer.write(congeom, wproperties);
	            		pCount++;
					}
					else System.out.println("file: "+partitionFile+" defectuous!");
				} catch (Exception e) {e.printStackTrace();}
				System.gc();
				//if(partitionCount > 4) break;
			}
		}
	}
	
	private static boolean topologyOk(FeatureCollection fc)  {
		try {
			List fcl = fc.getFeatures();
			Polygon a, b, p;
			GeometryFactory geomfact = new GeometryFactory();
			for(int i=0; i<fcl.size(); i++)  {
				p = (Polygon)((Feature)fcl.get(i)).getGeometry();
				for(int r=0; r<p.getNumInteriorRing(); r++)  {
					a = geomfact.createPolygon(geomfact.createLinearRing(p.getInteriorRingN(r).getCoordinateSequence()), null);
					for(int j=0; j<fcl.size(); j++)  {
						if(i != j) {
							b = (Polygon)((Feature)fcl.get(j)).getGeometry();
							if(a.intersects(b)) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}

}
