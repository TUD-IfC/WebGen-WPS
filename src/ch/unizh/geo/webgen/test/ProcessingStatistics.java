package ch.unizh.geo.webgen.test;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;

import javax.imageio.ImageIO;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.server.WebGenRequestExecuter;
import ch.unizh.geo.webgen.tools.ProcessingTools;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.ShapefileReader;
import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.LayerPrinter;

public class ProcessingStatistics {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ProcessingStatistics();
	}
	
	public ProcessingStatistics() {
		long startTime = System.currentTimeMillis();
		test();
		System.out.println("\nProcessing time: " + ((System.currentTimeMillis()- startTime)/1000) + "s");
	}
	
	int partitionCount = 0;
	double costSum_Gradient = 0.0;
	int iterSum_Gradient = 0;
	double costSum_2deep = 0.0;
	int iterSum_2deep = 0;
	double costSum_initial = 0.0;
	StringBuffer partitionStatistics = new StringBuffer();

	
	private void test() {
		File partitionsDir = new File("C:\\java\\processing\\filtered\\");
		String[] partitionFiles =  partitionsDir.list();
		
		for(String partitionFile : partitionFiles) {
			String[] partitionFileS = partitionFile.split("\\.");
			if(partitionFileS[0].startsWith("partition") && partitionFileS[1].equals("shp")) {
				DriverProperties wproperties = new DriverProperties();
				ShapefileReader reader = new ShapefileReader();
				try {
					if(partitionCount > 2 && partitionCount < 20) {
						wproperties.putAll(Collections.singletonMap(DataSource.FILE_KEY, partitionsDir+"\\"+partitionFile));
						FeatureCollection geom = reader.read(wproperties);
						wproperties.putAll(Collections.singletonMap(DataSource.FILE_KEY, partitionsDir+"\\"+partitionFile.replaceFirst("partition", "streets")));
						FeatureCollection congeom = reader.read(wproperties);
						
						doProcessing(geom, congeom);
						//doProcessingVaryingOperators(geom, congeom);
						System.out.println("Partition "+partitionCount+" done!");
					}
					partitionCount++;
				} catch (Exception e) {e.printStackTrace();}
				System.gc();
				//if(partitionCount > 199) break;
			}
		}
		
		DecimalFormat df = new DecimalFormat("#0.00000000");
		System.out.println("\n\nTest statistics:\n"+partitionStatistics+"\n");
		System.out.println("Total Initial Cost:   "+df.format(costSum_initial/partitionCount));
		System.out.println("Average StdGradient:  "+df.format(costSum_Gradient/partitionCount));
		System.out.println("Iterations StdGradient:  "+(iterSum_Gradient/partitionCount));
		
//		System.out.println("\n\nTest statistics:\n"+partitionStatistics+"\n");
//		for(int i=1; i<=operationsArray.length; i++) {
//			System.out.println(i+" Operators - Total Initial Cost:   "+df.format(costSumsInitial[i-1]/partitionCount));
//			System.out.println(i+"Operators - Average StdGradient:  "+df.format(costSumsFinal[i-1]/partitionCount));
//			System.out.println(i+"Operators - Iterations StdGradient:  "+(interationSum[i-1]/partitionCount));
//		}
		
		
		int maxiterations = 20; int lineSum;
		System.out.println("\nOperator Sequences: ");
		System.out.print("op name");
		for(int i=1; i<=maxiterations; i++) {
			System.out.print(";"+i);
		}
		System.out.print(";sum\n");
		for(int i=0; i<operatorPositions.length; i++) {
			System.out.print(ProcessingTools.operationNameArray[i]);
			lineSum = 0;
			for(int j=0; j<maxiterations; j++) {
				System.out.print(";"+operatorPositions[i][j]);
				lineSum += operatorPositions[i][j];
			}
			System.out.print(";"+lineSum+"\n");
		}
	}
	
	private void writeImage(String filename, FeatureCollection streets, FeatureCollection before, FeatureCollection after) {
		try {
			LayerManager lmgr = new LayerManager();
			lmgr.addCategory("mylayers");
			lmgr.addLayer("mylayers", "streets", streets);
			lmgr.addLayer("mylayers", "before", before);
			lmgr.addLayer("mylayers", "after", after);
			Envelope lmgrenv = lmgr.getEnvelopeOfAllLayers();
			
			LayerPrinter lpr = new LayerPrinter();
			BufferedImage image = lpr.print(lmgr.getVisibleLayers(true), lmgrenv, 600);
			
			FileOutputStream os = new FileOutputStream("C:\\java\\processing\\images\\"+filename+".png");
			ImageIO.write((RenderedImage)image, "PNG", os);
			os.flush();
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	private void doProcessing(FeatureCollection geom, FeatureCollection congeom) {
		geom = new ConstrainedFeatureCollection(geom);
		congeom = new ConstrainedFeatureCollection(congeom);
		
		HashMap<String,Object> tparams = new HashMap<String,Object>();
		tparams.put("geom", geom);
		tparams.put("congeom", congeom);
		tparams.put("webgenserver", "localcloned");
		tparams.put("minarea", 306.0);
		tparams.put("minlength", 10.0);
		tparams.put("mindist", 10.0);
		tparams.put("roaddist", 15.0);
		tparams.put("parallel", true);
		tparams.put("partitionNr", partitionCount);
		for(String opn : operationsArray) {
			tparams.put("use " + opn, true);
			
		}
		/*tparams.put("use AreaScalingRelative", true);
		tparams.put("use BuildingSimplifyGN", true);
		tparams.put("use EnlargeToRectangle", true);
		tparams.put("use DisplaceConstrainedNewFast", true);
		tparams.put("use BuildingTypification (10% Reduktion)", true);
		//tparams.put("use BuildingTypification (30% Reduktion)", true);
		//tparams.put("use AreaFeatureRemoval", true);
		tparams.put("use AggregateBuiltUpArea", true);
		//tparams.put("use ShrinkPartition (10%)", true);
		tparams.put("use ShrinkAllPolygons", true);
		tparams.put("use CompressPartitionConstrained", true);*/
		
		tparams.put("search method", "gradient std");
		WebGenRequest twgreq = WebGenRequestExecuter.callService(tparams, "localcloned", "BuildingProcessing_SinglePartition");
		this.costSum_initial += twgreq.getResultDouble("initial cost");
		this.costSum_Gradient += twgreq.getResultDouble("final cost");
		this.iterSum_Gradient += twgreq.getResultInteger("iterations");
		partitionStatistics.append("p"+partitionCount+": initial = "+twgreq.getResultDouble("initial cost")+"; final = "+
				twgreq.getResultDouble("final cost") + "; iterations = " + twgreq.getResultInteger("iterations") + "\n");
		ConstrainedFeatureCollection result = (ConstrainedFeatureCollection)twgreq.getParameter("result");
		if(result == null ) result = (ConstrainedFeatureCollection)twgreq.getResult("result");
		writeImage("result_"+partitionCount+"_gradient", congeom, geom, result);
		
		/*tparams.put("search method", "recursive 2deep");
		twgreq = WebGenRequestExecuter.callService(tparams, "localcloned", "BuildingProcessing_SinglePartition");
		this.costSum_Gradient += twgreq.getResultDouble("final cost");
		this.iterSum_Gradient += twgreq.getResultInteger("iterations");
		result = (ConstrainedFeatureCollection)twgreq.getParameter("result");
		if(result == null ) result = (ConstrainedFeatureCollection)twgreq.getResult("result");
		writeImage("result_"+partitionCount+"_2deep", congeom, geom, result);*/
	}
	
	
	public static final String[] operationsArray = {"AreaScalingRelative", "BuildingSimplifyGN", 
		"EnlargeToRectangle", "DisplaceConstrainedNewFast",
		"BuildingTypification (10% Reduktion)", /*"BuildingTypification (30% Reduktion)",*/
		/*"AreaFeatureRemoval",*/ "AggregateBuiltUpArea", /*"ShrinkPartition (10%)",*/ "ShrinkAllPolygons",
		"CompressPartitionConstrained"};
	
	
	double[] costSumsInitial = new double[operationsArray.length];
	double[] costSumsFinal = new double[operationsArray.length];
	int[] interationSum = new int[operationsArray.length];
	
	private void doProcessingVaryingOperators(FeatureCollection geom, FeatureCollection congeom) {
		geom = new ConstrainedFeatureCollection(geom);
		congeom = new ConstrainedFeatureCollection(congeom);
		
		HashMap<String,Object> tparams = new HashMap<String,Object>();
		tparams.put("geom", geom);
		tparams.put("congeom", congeom);
		tparams.put("webgenserver", "localcloned");
		tparams.put("minarea", 306.0);
		tparams.put("minlength", 10.0);
		tparams.put("mindist", 10.0);
		tparams.put("roaddist", 15.0);
		tparams.put("parallel", true);
		for(String opn : operationsArray) {
			tparams.put("use " + opn, false);
		}
		int ops = 1;
		for(String opn : operationsArray) {
			System.out.println("processing with "+ops+" operators");
			tparams.put("use " + opn, true);
			tparams.put("search method", "gradient std");
			WebGenRequest twgreq = WebGenRequestExecuter.callService(tparams, "localcloned", "BuildingProcessing_SinglePartition");
			costSumsInitial[ops-1] += twgreq.getResultDouble("initial cost");
			costSumsFinal[ops-1] += twgreq.getResultDouble("final cost");
			interationSum[ops-1] += twgreq.getResultInteger("iterations");
			partitionStatistics.append("p"+partitionCount+": ops = "+ops+"; initial = "+twgreq.getResultDouble("initial cost")+"; final = "+
					twgreq.getResultDouble("final cost") + "; iterations = " + twgreq.getResultInteger("iterations") + "\n");
			ConstrainedFeatureCollection result = (ConstrainedFeatureCollection)twgreq.getParameter("result");
			if(result == null ) result = (ConstrainedFeatureCollection)twgreq.getResult("result");
			writeImage("result_"+partitionCount+"_"+ops+"operators", congeom, geom, result);
			ops++;
		}
	}
	
	
	static int[][] operatorPositions = new int[ProcessingTools.operationNameArray.length][20];
	public static void addOpteratorPosition(int opID, int iteration) {
		try {
			if(operatorPositions[opID][iteration] <= 0) operatorPositions[opID][iteration] = 1;
			else operatorPositions[opID][iteration]++;
		}
		catch (Exception e) {}
	}
	

	/*private static void test1() {
		StringBuffer endOut = new StringBuffer();
		
		HashMap<String,Object> tparams = new HashMap<String,Object>();
		
		String path = "T:\\neun\\daten\\";
		DriverProperties wproperties = new DriverProperties();
		ShapefileReader reader = new ShapefileReader();
		try {
			wproperties.putAll(Collections.singletonMap(DataSource.FILE_KEY, path+"t25-buildings-kl3.shp"));
			FeatureCollection geom = reader.read(wproperties);
			tparams.put("geom", geom);
			wproperties.putAll(Collections.singletonMap(DataSource.FILE_KEY, path+"t25-streets-kl3.shp"));
			FeatureCollection congeom = reader.read(wproperties);
			tparams.put("congeom", congeom);
		} catch (Exception e) {e.printStackTrace(); return;}
		
		tparams.put("webgenserver", "localcloned");
		tparams.put("minarea", 306.0);
		tparams.put("minlength", 10.0);
		tparams.put("mindist", 10.0);
		tparams.put("roaddist", 15.0);
		tparams.put("parallel", true);
		
		int testNbr = 10;
		double initialCost = 0.0;
		double sumStdGradient = 0.0;
		double sumSimAnealing = 0.0;
		double sumRandGradient = 0.0;
		int sumIterationsStdGradient = 0;
		int sumIterationsSimAnealing = 0;
		int sumIterationsRandGradient = 0;
		
		tparams.put("search method", "gradient std");
		for(int i=0; i<1; i++) {
			WebGenRequest twgreq = WebGenRequestExecuter.callService(tparams, "localcloned", "BuildingProcessing");
			writeResult(twgreq);
			endOut.append("gradient std;"+twgreq.getResultDouble("initial cost")+";"+twgreq.getResultDouble("final cost")+";"+twgreq.getResultInteger("iterations")+"\n");
			sumStdGradient += twgreq.getResultDouble("final cost");
			sumIterationsStdGradient += twgreq.getResultInteger("iterations");
			if(i==0) initialCost = twgreq.getResultDouble("initial cost");
		}
		
		tparams.put("search method", "simulated anealing");
		for(int i=0; i<testNbr; i++) {
			WebGenRequest twgreq = WebGenRequestExecuter.callService(tparams, "localcloned", "BuildingProcessing");
			writeResult(twgreq);
			endOut.append("simulated anealing;"+twgreq.getResultDouble("initial cost")+";"+twgreq.getResultDouble("final cost")+";"+twgreq.getResultInteger("iterations")+"\n");
			sumIterationsSimAnealing += twgreq.getResultInteger("iterations");
			sumSimAnealing += twgreq.getResultDouble("final cost");
		}
		
		tparams.put("search method", "gradient random");
		for(int i=0; i<testNbr; i++) {
			WebGenRequest twgreq = WebGenRequestExecuter.callService(tparams, "localcloned", "BuildingProcessing");
			writeResult(twgreq);
			endOut.append("gradient random;"+twgreq.getResultDouble("initial cost")+";"+twgreq.getResultDouble("final cost")+";"+twgreq.getResultInteger("iterations")+"\n");
			sumIterationsRandGradient += twgreq.getResultInteger("iterations");
			sumRandGradient += twgreq.getResultDouble("final cost");
		}
		
		DecimalFormat df = new DecimalFormat("#0.00000000");
		System.out.println("\n\nTest statistics:\n" + endOut);
		System.out.println("Total Initial Cost:   "+df.format(initialCost));
		System.out.println("Average StdGradient:  "+df.format(sumStdGradient));
		System.out.println("Average SimAnealing:  "+df.format(sumSimAnealing/testNbr));
		System.out.println("Average RandGradient: "+df.format(sumRandGradient/testNbr));
		System.out.println("Iterations StdGradient:  "+(sumIterationsStdGradient));
		System.out.println("Iterations SimAnealing:  "+(sumIterationsSimAnealing/testNbr));
		System.out.println("Iterations RandGradient: "+(sumIterationsRandGradient/testNbr));
	}
	
	private static void writeResult(WebGenRequest twgreq) {
		//
	}*/
}
