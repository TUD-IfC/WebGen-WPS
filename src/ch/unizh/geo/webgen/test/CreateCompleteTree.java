package ch.unizh.geo.webgen.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollectionSorted;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.service.BuildingProcessing_SinglePartition;
import ch.unizh.geo.webgen.tools.ProcessingTools;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.ShapefileReader;
import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.LayerPrinter;

public class CreateCompleteTree {

	public static void main(String[] args) {
		new CreateCompleteTree();
	}
		
	static final File partitionsDir = new File("T:\\neun\\partitions\\filtered\\");
	static final double minarea = 306.0; //25k=77; 50k=306
	static final double minlength = 10.0; //25k=6.25; 50k=10
	static final double mindist = 10.0; //25k=6.25; 50k=10
	static final double roaddist = 15.0; //25k=7.5; 50k=15
	HashMap<String,Object> globalParams = new HashMap<String,Object>();
	double initialCost;
	
	ConstrainedFeatureCollection geom;
	ConstrainedFeatureCollection congeom;
	
	public CreateCompleteTree() {
		globalParams.put("minarea", minarea);
		globalParams.put("minlength", minlength);
		globalParams.put("mindist", mindist);
		globalParams.put("roaddist", roaddist);
		try {
			DriverProperties wproperties = new DriverProperties();
			ShapefileReader reader = new ShapefileReader();
			wproperties.putAll(Collections.singletonMap(DataSource.FILE_KEY, ProcessingStatisticsParallel.partitionsDir+"\\partition139.shp"));
			geom = new ConstrainedFeatureCollection(reader.read(wproperties));
			wproperties.putAll(Collections.singletonMap(DataSource.FILE_KEY, ProcessingStatisticsParallel.partitionsDir+"\\streets139.shp"));
			congeom = new ConstrainedFeatureCollection(reader.read(wproperties));
			geom.initCollectionConstraint(congeom);
			
			WebGenRequest ewgreq = new WebGenRequest();
 			ewgreq.addParameters(globalParams);
 			ewgreq.addFeatureCollection("geom", geom);
 			ewgreq.addFeatureCollection("congeom", congeom);
			Double[] costVec = BuildingProcessing_SinglePartition.evalPartitions(ewgreq);
			double costAllCurrent = BuildingProcessing_SinglePartition.getCostFromCostVector(costVec);
			initialCost = costAllCurrent;
			geom.makeConstraintHistoryStep("initial#"+costAllCurrent);
			ConstrainedFeatureCollectionSorted newGeom = new ConstrainedFeatureCollectionSorted(costAllCurrent, geom, "initial", -1, costVec);
			//start recursion
			doStepRecursive(newGeom, 3, 0, "init", new ArrayList<String>()); //displace
			doStepRecursive(newGeom, 2, 0, "init", new ArrayList<String>()); //enlarge
			doStepRecursive(newGeom, 4, 0, "init", new ArrayList<String>()); //typify
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	
	
	void doStepRecursive(ConstrainedFeatureCollectionSorted formerGeom, int operationId, int depth, String chain, ArrayList<String> costList) {
		if(depth >= 3) return;
		depth++;
		
		HashMap<String,Object> tparams = new HashMap<String,Object>();
		tparams.putAll(globalParams);
		tparams.put("geom", formerGeom.getFeatureCollection());
		tparams.put("congeom", this.congeom);
		ConstrainedFeatureCollection fcNew = ProcessingTools.executeOperation(operationId, tparams, "localcloned"); //displace
 		if(fcNew != null ) {
 			WebGenRequest ewgreq = new WebGenRequest();
 			ewgreq.addParameters(tparams);
 			ewgreq.addFeatureCollection("geom", fcNew);
			Double[] costVec = BuildingProcessing_SinglePartition.evalPartitions(ewgreq);
			double costAllCurrent = BuildingProcessing_SinglePartition.getCostFromCostVector(costVec);
			String operationName = ProcessingTools.lookupOperation(operationId);
			fcNew.makeConstraintHistoryStep(operationName+"#"+costAllCurrent);
			ConstrainedFeatureCollectionSorted newGeom = new ConstrainedFeatureCollectionSorted(costAllCurrent, fcNew, operationName, operationId, costVec);
			
			costList.add(ProcessingTools.lookupOperationShort(operationId)+" cost:   "+costAllCurrent);
			chain += "-" + ProcessingTools.lookupOperationShort(operationId);
			writeImage(chain, congeom, geom, fcNew, initialCost, costAllCurrent, costList);
			
			//recursion
			doStepRecursive(newGeom, 3, depth, chain, new ArrayList<String>(costList)); //displace
			doStepRecursive(newGeom, 2, depth, chain, new ArrayList<String>(costList)); //enlarge
			doStepRecursive(newGeom, 4, depth, chain, new ArrayList<String>(costList)); //typify
    	}
	}
	
	
	public static void writeImage(String filename, FeatureCollection streets, FeatureCollection before, FeatureCollection after, double initCost, double finalCost, List<String> costList) {
		try {
			LayerManager lmgr = new LayerManager();
			lmgr.addCategory("mylayers");
			lmgr.addLayer("mylayers", "streets", streets);
			lmgr.addLayer("mylayers", "before", before);
			lmgr.addLayer("mylayers", "after", after);
			Envelope lmgrenv = lmgr.getEnvelopeOfAllLayers();
			
			LayerPrinter lpr = new LayerPrinter();
			BufferedImage image = lpr.print(lmgr.getVisibleLayers(true), lmgrenv, 600);
			
			
			int envDrittel = (int) Math.floor(lmgrenv.getWidth()/3);
			Graphics2D g = image.createGraphics();
			g.setColor(Color.BLACK);
			g.drawLine(10,10,210,10);
			g.drawLine(10,9,10,11);
			g.drawLine(210,9,210,11);
			g.drawString(envDrittel+" m", 100, 22);
			
			int upperY = 560-(20*costList.size());
			g.drawString("initial cost: "+initCost, 10, upperY);
			upperY += 20;
			for(String cl : costList) {
				g.drawString(cl, 10, upperY);
				upperY += 20;
			}
			g.drawString("final cost:   "+finalCost, 10, upperY);
			
			FileOutputStream os = new FileOutputStream("T:\\neun\\imagetree\\"+filename+".png");
			ImageIO.write((RenderedImage)image, "PNG", os);
			os.flush();
		}
		catch (Exception e) {e.printStackTrace();}
	}

}
