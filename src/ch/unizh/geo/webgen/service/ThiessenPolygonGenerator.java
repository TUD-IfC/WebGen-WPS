package ch.unizh.geo.webgen.service;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureDatasetFactory;
import com.vividsolutions.jump.feature.FeatureSchema;

import edu.cornel.cs.chew.voronoi.DTriangulationForJTS;


public class ThiessenPolygonGenerator extends AWebGenAlgorithm implements IWebGenAlgorithm  {
	
	public void run(WebGenRequest wgreq) {
		FeatureCollection fc = wgreq.getFeatureCollection("geom");
		try {
			FeatureCollection fcnew = createThiessenPolys(fc);
			wgreq.addResult("result", fcnew);
		}
		catch(Exception e) {addError(e.getMessage());}
		
	}
		
	public FeatureCollection runAlgo(FeatureCollection fc, HashMap parameters) {
		try {
			FeatureCollection fcnew = createThiessenPolys(fc);
			return fcnew;
		}
		catch(Exception e) {
		    this.addError(e.getMessage());
		    return fc;
		    }
	}
	

	private FeatureCollection createThiessenPolys(FeatureCollection features) throws Exception{

	    System.gc(); //flush garbage collector
	    //-- create empty FC for output 
	    FeatureSchema fs = new FeatureSchema();
	    fs.addAttribute("Geometry", AttributeType.GEOMETRY);
	    FeatureCollection newFeatures = new FeatureDataset(fs);
	    //---
	    ArrayList points = new ArrayList();
	    for (Iterator iter = features.iterator(); iter.hasNext();) {
            Feature f = (Feature) iter.next();
            Geometry g = f.getGeometry();
            if(g instanceof Point){
                points.add(f.getGeometry());
            }
            else{
                //context.getWorkbenchFrame().warnUser("no point geometry");
            }
        }
	    if(points.size() > 0){
		    //monitor.report("create triangulation");
		    DTriangulationForJTS tri = new DTriangulationForJTS(points);
		    
		    //ArrayList nodes = tri.drawAllSites();	    
		    //FeatureCollection myCollA = FeatureDatasetFactory.createFromGeometry(nodes);	    
			//context.addLayer(StandardCategoryNames.WORKING, "sites", myCollA);
			
			//ArrayList nodes2 = tri.getInitialSimmplexAsJTSPoints();
		    //FeatureCollection myCollD = FeatureDatasetFactory.createFromGeometry(nodes2);	    
			//context.addLayer(StandardCategoryNames.WORKING, "cornerpoints", myCollD);
			
		    //ArrayList edges = tri.drawAllVoronoi(); 
		    //FeatureCollection myCollB = FeatureDatasetFactory.createFromGeometry(edges);	    
			//context.addLayer(StandardCategoryNames.WORKING, "voronoi edges", myCollB);
		    
			//ArrayList bbox = new ArrayList(); 
			//bbox.add(tri.getThiessenBoundingBox());
		    //FeatureCollection myCollE = FeatureDatasetFactory.createFromGeometry(bbox);	    
			//context.addLayer(StandardCategoryNames.WORKING, "bbox", myCollE);
			
		    //monitor.report("create polygons from voronoi edges");
		    Collection polys = tri.getThiessenPolys();
		    newFeatures = FeatureDatasetFactory.createFromGeometry(polys);	    
			
	    }
	    else{
	        this.addMessage("Create Thiessen Polygons: No Point geometries found");
	        //context.getWorkbenchFrame().warnUser("no point data");	        
	    }
		return  newFeatures;       		
	}
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("ThiessenPolygonGenerator", "sstein", "support",
				"",
				"ThiessenPolygonGenerator",
				"Thiessen-Polygon Generator",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Point"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "point features");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}

}
