package ch.unizh.geo.webgen.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.unizh.geo.algorithms.polygons.BuildingOutlineSimplify;
import ch.unizh.geo.constraints.buildings.BuildingLocalWidth;
import ch.unizh.geo.constraints.buildings.BuildingShortestEdge;
import ch.unizh.geo.webgen.model.ConstrainedFeature;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;


public class BuildingSimplifyOutline extends AWebGenAlgorithm implements IWebGenAlgorithm {

    private final String newAttributString = "SimplifyOutline";
    private double flexibility = 0;
	
	public void run(WebGenRequest wgreq) {
		try {
			FeatureCollection fc = wgreq.getFeatureCollection("geom");
			double minLength = wgreq.getParameterDouble("minlength");
			int iterations;
			try {iterations = wgreq.getParameterInt("iterations");}
			catch(Exception e) {iterations = 20;}
			FeatureCollection fcnew = simplify(fc, minLength, iterations);
			wgreq.addResult("result", fcnew);
		}
		catch(Exception e) {}
	}

	private FeatureCollection simplify(FeatureCollection features, double minLength, 
	                          int iterations) throws Exception{
	    
	    System.gc(); //flush garbage collector
	    // --------------------------
	    boolean solveIterative=false;
	    if (iterations==1){
	        solveIterative=false;
	    }
	    else{
	        solveIterative=true;
	    }
	    // --------------------------
	    int count=0;
	    //int noItems = features.size();
	    //FeatureDataset problematicFeatures = null;
	    ConstrainedFeatureCollection resultFeatures = null;
	    ArrayList<LineString> problematicEdges = new ArrayList<LineString>();
	    //List resultList = new ArrayList();
	    FeatureSchema fs = new FeatureSchema();
	    //--get single object in selection to analyse
	    for (Iterator iter = features.iterator(); iter.hasNext();) {
	        count++;
	        Feature f = (Feature)iter.next();
	        //System.out.println("========== Item featureID: " + f.getID() + " ==========");      		
	        
	        //-- set schema by using the first selected item
	        //check if attribute name exists
	        boolean attributeExists = false;
	        if (count == 1){      			
	            //-- not sure to do that, since feature schemas of selected objects might be different 
	            fs = copyFeatureSchema(f.getSchema());
	            attributeExists = fs.hasAttribute(this.newAttributString);          			
	            if (attributeExists == false){	      		      			
	                fs.addAttribute(this.newAttributString, AttributeType.STRING);
	                //problematicFeatures = new FeatureDataset(fs);
	            }
	            resultFeatures = new ConstrainedFeatureCollection(fs);
	        }
	        //--create new Feature with one new attribute and copy attributvalues
	        ConstrainedFeature fnew = new ConstrainedFeature(fs);
	        try {fnew.setConstraint(((ConstrainedFeature)f).getConstraint());}
	        catch(Exception e) {}
	        Object[] attribs = f.getAttributes();
	        if (attributeExists == false){
	            Object[] attribsnew = new Object[attribs.length+1];
	            for (int i = 0; i < attribs.length; i++) {
	                attribsnew[i] = attribs[i];
	            }
	            attribsnew[attribs.length]= "init";
	            fnew.setAttributes(attribsnew);
	        }
	        else{
	            fnew.setAttributes(attribs);
	        }
	        //--
	        Geometry geom = f.getGeometry(); //= erste Geometrie
	        Polygon poly = null;       	
	        if ( geom instanceof Polygon){
	            poly = (Polygon) geom; //= erste Geometrie
	            // --------------------------
	            
	            List<Object> conflictListA = new ArrayList<Object>();
	            List<Object> conflictListB = new ArrayList<Object>();
	            List<Object> conflictListD = new ArrayList<Object>();
	            
	            //---- detect conflicts
	            BuildingLocalWidth plw = new BuildingLocalWidth(poly, 
	                    minLength,this.flexibility);
	            BuildingShortestEdge conflicts = new BuildingShortestEdge(poly, 
	                    minLength, this.flexibility);
	            //---
	            if(conflicts.measure.hasConflicts() == true){
	                //context.getWorkbenchFrame().setStatusMessage("conflicts detected! : " + conflicts.measure.getNrOfToShortEdges());           	
	                conflictListA.addAll((ArrayList<Object>)plw.measure.getDispVecPointEdgeLStringList());
	                conflictListB.addAll((ArrayList<Object>)plw.measure.getDispVecPointPointLStringList());
	                conflictListD.addAll((ArrayList<Object>)conflicts.measure.getLineStringList());
	                //--- solve conflicts ---
	                //-----------
	                // no iteration
	                //-----------
	                if (solveIterative == false){
	                    try{
	                        BuildingOutlineSimplify bosimplify = new BuildingOutlineSimplify(poly,
	                                conflicts.measure.getConflicList(), conflicts.getGoalValue());		           		
	                        //resultList.add(bosimplify.getOutPolygon());
	                        fnew.setGeometry(bosimplify.getOutPolygon());
	                        
	                        conflicts = new BuildingShortestEdge(bosimplify.getOutPolygon(), 
	                                minLength, this.flexibility);
	                        if (conflicts.measure.hasConflicts() == true){
	                            fnew.setAttribute(this.newAttributString, "not solved");
	                            problematicEdges.addAll((ArrayList<LineString>)conflicts.measure.getLineStringList());
	                        }
	                        else{
	                            fnew.setAttribute(this.newAttributString, "simplified");
	                        }
	                        /**
	                         transaction.setGeometry(count-1,bosimplify.getOutPolygon());
	                         **/
	                    }
	                    catch(Exception e){
	                        this.addMessage("problem with building " + count);
	                        fnew.setGeometry(poly);
	                        fnew.setAttribute(this.newAttributString, "error");
	                    }
	                }
	                else{
	                    //====================================   
	                    // if solution should be done iterative
	                    //====================================
	                    try{
	                        BuildingOutlineSimplify bosimplify = null;		           		
	                        int j = 0;
	                        boolean tosolve = conflicts.measure.hasConflicts();
	                        while(tosolve == true){
	                            bosimplify = new BuildingOutlineSimplify(poly,
	                                    conflicts.measure.getConflicList(), minLength);
	                            poly = bosimplify.getOutPolygon();
	                            boolean problems = bosimplify.isProblemsEncountered();
	                            conflicts = new BuildingShortestEdge(poly, 
	                                    minLength, this.flexibility); 
	                            tosolve = conflicts.measure.hasConflicts();
	                            //--notbremse:
	                            j = j + 1;
	                            //-- stop at max iterations 
	                            if(j == iterations){
	                                tosolve = false;
	                                //context.getWorkbenchFrame().warnUser("stopped at step: " + j);
	                            }
	                            //-- stop if only one not solveable conflicts appears 
	                            //   to avoid unnecessary loop till end   
	                            if(problems && (conflicts.measure.getConflicList().size() == 1 )){
	                                tosolve = false;
	                                //context.getWorkbenchFrame().warnUser("stopped at step: " + j);
	                            }
	                            if (tosolve == false){
	                                //--objects which still have problems
	                                if(conflicts.measure.hasConflicts()== true){
	                                    fnew.setAttribute(this.newAttributString, "not solved");
	                                    /*
	                                     Feature fnew = (Feature)f.clone();
	                                     fnew.setGeometry(bosimplify.getOutPolygon());
	                                     problematicFeatures.add(fnew);
	                                     */
	                                    problematicEdges.addAll((ArrayList<LineString>)conflicts.measure.getLineStringList());
	                                }//--objects with solved problems
	                                else{
	                                    fnew.setAttribute(this.newAttributString, "simplified");
	                                }
	                                //--store geometry
	                                fnew.setGeometry(bosimplify.getOutPolygon());
	                            }
	                            //-- visualisation
	                            /*
	                             List stepList = new ArrayList();
	                             stepList.add(0,bosimplify.getOutPolygon());
	                             FeatureCollection myCollD = FeatureDatasetFactory.createFromGeometry(stepList);
	                             if (myCollD.size() > 0){
	                             context.addLayer(StandardCategoryNames.WORKING, "stepList", myCollD);
	                             }	    	  
	                             */
	                        }//end while
	                        /**
	                         resultList.add(bosimplify.getOutPolygon());
	                         transaction.setGeometry(count-1,bosimplify.getOutPolygon());
	                         **/
	                    }
	                    catch (Exception e) {
							this.addMessage("problem with building " + count);
							fnew.setGeometry(poly);
							fnew.setAttribute(this.newAttributString, "error");
						}
	                    } // --end iterative solution
	                    // ===== visulisation =====
	                    /**
	                     FeatureCollection myCollA = FeatureDatasetFactory.createFromGeometry(conflictListA);
	                     if (myCollA.size() > 0){
	                     context.addLayer(StandardCategoryNames.WORKING, "point-edge conflicts", myCollA);
	                     }	           	
	                     FeatureCollection myCollC = FeatureDatasetFactory.createFromGeometry(conflictListB);
	                     if (myCollC.size() > 0){
	                     context.addLayer(StandardCategoryNames.WORKING, "point-point conflicts", myCollC);
	                     }
	                     
	                     FeatureCollection myCollD = FeatureDatasetFactory.createFromGeometry(conflictListD);
	                     if (myCollD.size() > 0){
	                     context.addLayer(StandardCategoryNames.WORKING, "shortest Edges", myCollD);
	                     } 
	                     
	                     FeatureCollection myCollB = FeatureDatasetFactory.createFromGeometry(resultList);
	                     if (myCollB.size() > 0){
	                     context.addLayer(StandardCategoryNames.WORKING, "result", myCollB);
	                     }
	                     **/
	            }// ======================== 
	            else{//has no conflict 
	                //context.getWorkbenchFrame().setStatusMessage("no conflict detected!");
	                fnew.setAttribute(this.newAttributString, "no conflict");
	            }
	        }
	        else{
	            //context.getWorkbenchFrame().warnUser("no polygon selected");
	            fnew.setAttribute(this.newAttributString, "no polygon");
	        }
	        resultFeatures.add(fnew);
	        //String mytext = "item: " + count + " / " + noItems + " : simplification finalized";
	        //monitor.report(mytext);	       	
	    }//  end loop for selection
	    /**
	     transaction.commit();
	     **/      	
	    /*if (problematicEdges.size() > 0){
	        FeatureCollection myCollE = FeatureDatasetFactory.createFromGeometry(problematicEdges);
	        //context.addLayer(StandardCategoryNames.WORKING, "problematic edges", myCollE);
	    }*/
	    //context.addLayer(StandardCategoryNames.WORKING, "simplified buildings", resultFeatures);	    
	    return resultFeatures;        
	}
    
	private FeatureSchema copyFeatureSchema(FeatureSchema oldSchema){
		FeatureSchema fs = new FeatureSchema();
		for (int i = 0; i < oldSchema.getAttributeCount(); i++) {
			AttributeType at = oldSchema.getAttributeType(i);
			String aname = oldSchema.getAttributeName(i);
			fs.addAttribute(aname,at);
			fs.setCoordinateSystem(oldSchema.getCoordinateSystem());
		}		
		return fs;
	}
  
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("BuildingSimplifyOutline", "sstein", "operator",
				"",
				"BuildingSimplifyOutline",
				"simplify building outlines",
				"1.0");
		id.visible = true;
		
		//add input parameters
		String[] allowed = {"Polygon"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowed), "layer with buildings");
		id.addInputParameter("minlength", "DOUBLE", "10.0", "segment minimum length");
		id.addInputParameter("iterations", "INTEGER", "5", "number of algorithm iterations");
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection");
		return id;
	}
	
}
