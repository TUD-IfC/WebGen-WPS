package ch.unizh.geo.webgen.service;

import java.util.Iterator;

import ch.unizh.geo.algorithms.polygons.BuildingEnlargeToRectangle;
import ch.unizh.geo.constraints.polygons.PolygonMinimalArea;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;


public class EnlargeToRectangle extends AWebGenAlgorithm implements IWebGenAlgorithm  {

    private double flexibility = 10.0; //percent
    private boolean solveIter = true;
	
	public void run(WebGenRequest wgreq) {
		try {
			FeatureCollection fc = wgreq.getFeatureCollection("geom");
			double minSize = wgreq.getParameterDouble("minarea");
			changeToRectangle(fc, minSize, this.solveIter);
			wgreq.addResult("result", fc);
		}
		catch (Exception e) {}
	}
	
	private void changeToRectangle(FeatureCollection features, double minArea, boolean solveIterative) throws Exception {
        int count = 0;
        
        //--get single object in selection to analyse
        for (Iterator iter = features.iterator(); iter.hasNext();) {
            count++;
            Feature f = (Feature) iter.next();
            //Feature newF = (Feature)f.clone();
            Geometry geom = f.getGeometry(); //= erste Geometrie
            Polygon poly = null;
            if (geom instanceof Polygon) {
                poly = (Polygon) geom; //= erste Geometrie

                PolygonMinimalArea pma = new PolygonMinimalArea(poly, minArea, this.flexibility);
                if (pma.isfullfilled() == false) {
                    //--- solve conflicts ---
                    if (solveIterative == false) {
                        BuildingEnlargeToRectangle enlarge = new BuildingEnlargeToRectangle(poly, minArea);
                        f.setGeometry(enlarge.getOutPolygon());
                    } else {
                        //====================================
                        // if solution should be done iterative
                        //====================================
                    	BuildingEnlargeToRectangle enlarge = null;
                        int j = 0;
                        boolean solved = false;
                        while (solved == false) {
                            enlarge = new BuildingEnlargeToRectangle(poly,minArea);
                            poly = enlarge.getOutPolygon();
                            //-- detect conflicts
                            pma = new PolygonMinimalArea(poly, minArea, this.flexibility);
                            if (pma.isfullfilled() == true) {
                                solved = true;
                            }

                            //--notbremse:
                            j = j + 1;
                            if (j == 50) {
                                solved = true;
                            }
                        }
                        f.setGeometry(enlarge.getOutPolygon());
                    }
                }// ========================       		
            }
        }// end loop over item selection
    }

	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("EnlargeToRectangle", "sstein", "operator",
				"",
				"EnlargeToRectangle",
				"Enlarges polygons (buildings) to rectangles",
				"1.0",
				new String[] {"ica.genops.cartogen.Enhancement"});
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "Simplification and rectified enlargement of buildings smaller than the minimum building size");
		id.addInputParameter("minarea", "DOUBLE", "200.0", "minimum size");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "rectified buildings");
		return id;
	}
	
}
