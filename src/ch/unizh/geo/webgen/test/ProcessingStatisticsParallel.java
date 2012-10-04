package ch.unizh.geo.webgen.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import javax.imageio.ImageIO;

import ch.unizh.geo.webgen.model.ConstrainedFeature;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.model.Constraint;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.server.WebGenRequestExecuter;
import ch.unizh.geo.webgen.tools.ConsoleInput;
import ch.unizh.geo.webgen.tools.ProcessingTools;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.ShapefileReader;
import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.LayerPrinter;

public class ProcessingStatisticsParallel {
	public static void main(String[] args) {
		new ProcessingStatisticsParallel();
	}
	
	static final File partitionsDir = new File("T:\\neun\\partitions\\filtered\\");
	static final double minarea = 306.0; //25k=77; 50k=306
	static final double minlength = 10.0; //25k=6.25; 50k=10
	static final double mindist = 10.0; //25k=6.25; 50k=10
	static final double roaddist = 15.0; //25k=7.5; 50k=15
	static String search_method = "gradient std";
	static String[] search_methods = new String[]{
		"gradient std","simulated anealing","recursive genetic",
		"recursive fully","recursive gradient","recursive 2deep",
		"simulated anealing 1,3", "simulated anealing 3"
	};
	static String resultFileName;
	
	
	long startTime;
	int startPartition = -1;
	int endPartition = 1;
	
	public ProcessingStatisticsParallel() {
		startPartition = ConsoleInput.intEinlesen("enter start partition: ");
		endPartition = ConsoleInput.intEinlesen("enter end partition: ");
		System.out.println("Available search methods: ");
		for(int i=0; i<search_methods.length;i++) System.out.println(i+" = "+search_methods[i]);
		int smNbr = ConsoleInput.intEinlesen("choose a number: ");
		String fileSuffix = ConsoleInput.stringEinlesen("enter a output suffix: ");
		search_method = search_methods[smNbr];
		
		resultFileName = search_method.replaceAll(" ", "")+"_"+startPartition+"-"+endPartition +
			"_"+minarea+"-"+minlength+"-"+mindist+"-"+roaddist+"-"+fileSuffix;
		
		System.out.println("starting ...");
		startTime = System.currentTimeMillis();
		test();
		System.out.println("\nProcessing time: " + ((System.currentTimeMillis()- startTime)/1000) + "s");
	}
	
	int partitionCount = 0;
	static double costSum_Gradient = 0.0;
	static int iterSum_Gradient = 0;
	static double costSum_2deep = 0.0;
	static int iterSum_2deep = 0;
	static double costSum_initial = 0.0;
	static StringBuffer partitionStatistics = new StringBuffer();
	
	FeatureCollection t_geom;
	FeatureCollection t_congeom;
	
	private void test() {
		String[] partitionFiles =  partitionsDir.list();
		Vector<String> partitionShpFileNames = new Vector<String>();
		int shpCount = 0;
		for(String partitionFile : partitionFiles) {
			String[] partitionFileS = partitionFile.split("\\.");
			if(partitionFileS[0].startsWith("partition") && partitionFileS[1].equals("shp")) {
					if(shpCount >= startPartition && shpCount < endPartition) {
						partitionShpFileNames.add(partitionFile);
						partitionCount++;
						}
					shpCount++;
			}
		}
		
		
		int nextPartition = 0;
		int maxThreads = 15;
		if(maxThreads > partitionCount) maxThreads = partitionCount;
		PartitionStatisticThread[] threads = new PartitionStatisticThread[maxThreads];
		
		for(int i=0; i<maxThreads; i++) {
			threads[i] = new PartitionStatisticThread(nextPartition, partitionShpFileNames.get(nextPartition));
	 		threads[i].start();
	 		nextPartition++;
    	}
		
		while(nextPartition < partitionCount) {
	 		for(int i=0; i<maxThreads; i++) {
	 			if(nextPartition == partitionCount) break;
        		if(!threads[i].isAlive()) {
        			try {
        				threads[i].join();
        				System.gc();
        				threads[i] = new PartitionStatisticThread(nextPartition, partitionShpFileNames.get(nextPartition));
        		 		threads[i].start();
        		 		nextPartition++;
        			} catch (InterruptedException e) {}
        		}
        	}
		}
		
		System.out.println("\n\nlast threads running ... waiting for join() \n\n");
		
		for(int i=0; i<maxThreads; i++) {
    		try {
				threads[i].join();
				System.out.println("\nthread " +i+ "done\n");
			}
    		catch (InterruptedException e) {}
			catch (Exception e) {}
    	}
		
		DecimalFormat df = new DecimalFormat("#0.00000000");
		System.out.println("\n\nTest statistics:\n"+partitionStatistics+"\n");
		System.out.println("Total Initial Cost:   "+df.format(costSum_initial/partitionCount));
		System.out.println("Average StdGradient:  "+df.format(costSum_Gradient/partitionCount));
		System.out.println("Iterations StdGradient:  "+(iterSum_Gradient/partitionCount));
		
		writeText(resultFileName+".txt", "total: initial = "+df.format(costSum_initial/partitionCount)+
				"; final = "+df.format(costSum_Gradient/partitionCount)+"; iterations = "+
				(iterSum_Gradient/partitionCount)+
				"\r\n\r\nProcessing time: " + ((System.currentTimeMillis()- startTime)/1000) + "s" +
				"\r\n\r\n" + partitionStatistics.toString());
		
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
	
	public static void writeImage(String filename, FeatureCollection streets, FeatureCollection before, FeatureCollection after) {
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
			
			FileOutputStream os = new FileOutputStream("T:\\neun\\images\\"+filename+".png");
			ImageIO.write((RenderedImage)image, "PNG", os);
			os.flush();
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	public static void writeText(String filename, String data) {
		try {
			FileWriter fw = new FileWriter("T:\\neun\\logs\\"+filename); 
			//C:\Dokumente und Einstellungen\burg\Eigene Dateien\withSave\Experimente\Steuerung\test.txt
			BufferedWriter bfw = new BufferedWriter(fw);
			bfw.write(data);
			bfw.close();
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	
	
	public static final String[] operationsArray = {"AreaScalingRelative", "BuildingSimplifyGN", 
		"EnlargeToRectangle", "DisplaceConstrainedNewFast",
		"BuildingTypification (10% Reduktion)", /*"BuildingTypification (30% Reduktion)",*/
		/*"AreaFeatureRemoval",*/ "AggregateBuiltUpArea", /*"ShrinkPartition (10%)",*/ "ShrinkAllPolygons",
		"CompressPartitionConstrained"};
	
	
	
	static int[][] operatorPositions = new int[ProcessingTools.operationNameArray.length][20];
	public static void addOpteratorPosition(int opID, int iteration) {
		try {
			if(operatorPositions[opID][iteration] <= 0) operatorPositions[opID][iteration] = 1;
			else operatorPositions[opID][iteration]++;
		}
		catch (Exception e) {}
	}
}


class PartitionStatisticThread extends Thread {
	
	int pCount;
	String pShpName;
	
	public PartitionStatisticThread(int _pCount, String _pShpName) {
		this.pCount = _pCount;
		this.pShpName = _pShpName;
	}
	
	public void run() {
 		try {
 			DriverProperties wproperties = new DriverProperties();
			ShapefileReader reader = new ShapefileReader();
			wproperties.putAll(Collections.singletonMap(DataSource.FILE_KEY, ProcessingStatisticsParallel.partitionsDir+"\\"+pShpName));
			FeatureCollection geom = reader.read(wproperties);
			wproperties.putAll(Collections.singletonMap(DataSource.FILE_KEY, ProcessingStatisticsParallel.partitionsDir+"\\"+pShpName.replaceFirst("partition", "streets")));
			FeatureCollection congeom = reader.read(wproperties);
 			geom = new ConstrainedFeatureCollection(geom);
 			congeom = new ConstrainedFeatureCollection(congeom);
 			
 			HashMap<String,Object> tparams = new HashMap<String,Object>();
 			tparams.put("geom", geom);
 			tparams.put("congeom", congeom);
 			tparams.put("webgenserver", "localcloned");
 			tparams.put("minarea", ProcessingStatisticsParallel.minarea);
 			tparams.put("minlength", ProcessingStatisticsParallel.minlength);
 			tparams.put("mindist", ProcessingStatisticsParallel.mindist);
 			tparams.put("roaddist", ProcessingStatisticsParallel.roaddist);
 			tparams.put("parallel", true);
 			tparams.put("partitionNr", pCount);
 			for(String opn : ProcessingStatisticsParallel.operationsArray) {
 				tparams.put("use " + opn, true);
 				
 			}
 			
 			tparams.put("search method", ProcessingStatisticsParallel.search_method);
 			WebGenRequest twgreq = WebGenRequestExecuter.callService(tparams, "localcloned", "BuildingProcessing_SinglePartition");
 			ProcessingStatisticsParallel.costSum_initial += twgreq.getResultDouble("initial cost");
 			ProcessingStatisticsParallel.costSum_Gradient += twgreq.getResultDouble("final cost");
 			ProcessingStatisticsParallel.iterSum_Gradient += twgreq.getResultInteger("iterations");
 			
 			ConstrainedFeatureCollection result = (ConstrainedFeatureCollection)twgreq.getParameter("result");
 			if(result == null ) result = (ConstrainedFeatureCollection)twgreq.getResult("result");
 			
 			ProcessingStatisticsParallel.partitionStatistics.append("p"+pCount+": initial = "+twgreq.getResultDouble("initial cost")+"; final = "+
 					twgreq.getResultDouble("final cost") + "; iterations = " + twgreq.getResultInteger("iterations"));
 			try {
 				ProcessingStatisticsParallel.partitionStatistics.append("; maxCost = "+twgreq.getResultDouble("returnstates_max") +
 						"; minCost = "+twgreq.getResultDouble("returnstates_min") + "; returnStates = "+twgreq.getResultInteger("returnstates_size"));
 			}
 			catch (Exception e) {}
 			ProcessingStatisticsParallel.partitionStatistics.append("; sequence = ");
 			try {
 				Constraint con = ((ConstrainedFeature)result.getFeature(0)).getConstraint();
 				for(int i=0; i < con.getHistorySize(); i++) {
 					ProcessingStatisticsParallel.partitionStatistics.append("|"+con.getStateMessageFromHistory(i));
 				}
 			}
 			catch (Exception e) {}
 			ProcessingStatisticsParallel.partitionStatistics.append("\r\n");
 			
 			ProcessingStatisticsParallel.writeImage(ProcessingStatisticsParallel.resultFileName+"_result_"+pCount, congeom, geom, result);
		}
		catch(Exception e) {}
		System.out.println("Partition "+pCount+" done!");
	}
}
