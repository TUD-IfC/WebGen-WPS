package ch.unizh.geo.webgen.service;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import ch.unizh.geo.webgen.registry.AttributeDescription;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.service.GenLineDisplacementSnakeBaderTest.BeamObject;

import com.axpand.jaxpand.genoperator.common.SignatureWidth;
//import com.axpand.jaxpand.genoperator.line.GenLineDisplacementSnakeBaderTest;
//--> funktioniert derzeit nicht (k.A. warum!) ... ist aber nicht so schlimm, so ist es derzeit sowieso einfacher an beiden codes zu arbeiten
//--> muss später aber dann irgendwie funktionieren, dass der Code in der jaxpand-genoperators.jar liegt
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;

//this class has JGenLineDisplacement as base and is adapted to the special needs of the class GenLIneDisplacementSnakeBaderTest

public class JGenLineDisplacementTestSnakes extends AWebGenAlgorithm implements IWebGenAlgorithm {
	
	public void run(WebGenRequest wgreq) {
		FeatureCollection lines = wgreq.getFeatureCollection("geom");
		//FeatureCollection areas = wgreq.getFeatureCollection("congeom");
		
		double dWidth = wgreq.getParameterDouble("dWidth");
		double dAsymmetryDistance = wgreq.getParameterDouble("dAsymmetryDistance");					
		double dMinDist = wgreq.getParameterDouble("dMinDist");		
		int nMaxIterations = wgreq.getParameterInt("nMaxIterations");
		double dEnergyThreshold = wgreq.getParameterDouble("dEnergyThreshold");
		
		SignatureWidth sSigWidth = new SignatureWidth(dWidth, dAsymmetryDistance);
		GenLineDisplacementSnakeBaderTest gld = new GenLineDisplacementSnakeBaderTest(null);
		gld.init(nMaxIterations, dEnergyThreshold);
		
		Feature f; LineString l; //Polygon p;
		for(Iterator iter = lines.iterator(); iter.hasNext();) {
			try {
				f = (Feature) iter.next();				
				l = (LineString) f.getGeometry();
				gld.addDataLine(l, sSigWidth, dMinDist, true, 5, false);
				
			}
			catch (ClassCastException e) {
				this.addError("only lines can be displaced");
				return;
			}
		}
		/*if (areas != null){
			System.out.println("threre are any polygons? ");
		}
		else{
			System.out.println("threre are no polygons? ");
		}*/
		
		/*for(Iterator iter = areas.iterator(); iter.hasNext();) {
			try {
				f = (Feature) iter.next();
				p = (Polygon) f.getGeometry();
				gld.addDataPolygon(p, 0.0, dMinDist);
			}
			catch (ClassCastException e) {
				this.addError("only areas can cause displacement");
				return;
			}
		}*/
		/*if(gld.execute()) {
			for(int i = 0; i < lines.size(); i++) {
				l = (LineString) gld.getDataLine(i);
				lines.
			}
		}*/
		boolean ex = gld.execute();
		System.out.println("was processing successfull?    "+ex);
		
		gld.writeStiffMat();
		gld.writeForceMat();
		/*if(JOptionPane.showConfirmDialog(null, "Write StiffMat to XML?", "Write?", JOptionPane.YES_NO_OPTION )==JOptionPane.YES_OPTION){
			String root=null;
			if(JOptionPane.showConfirmDialog(null, "Set Filename and Directory manually?", "Directory", JOptionPane.YES_NO_OPTION )==JOptionPane.YES_OPTION){
				Boolean rootTest = false;			
				while (rootTest == false){
					root = JOptionPane.showInputDialog(null, "Please set the filename (inlcuding directory) for the XML!","filename.xml");
					if (root.contains(".xml")){
						rootTest = true;
						JOptionPane.showMessageDialog(null, root,"root", JOptionPane.OK_CANCEL_OPTION);						
					}
					else{
						JOptionPane.showMessageDialog(null, "Filename..."+ root+"...has to end on: *.xml","root", JOptionPane.OK_CANCEL_OPTION);
					}
				}
			}
			else{
				root="/media/Speicher/Programierung/WebGen/Verdraengung/Ausgaben/StiffMat.xml";				
			}
			gld.writeStiffMatXML(root);			
		}	*/
		String root = "/media/Speicher/Programierung/WebGen/Verdraengung/Ausgaben/Output.txt";
							
		LinkedList	linesList = gld._lstLines;		
		//LinkedList	shiftedLinesList = gld._lstLinesShifted;
		
		Iterator iterLines = linesList.iterator();
		//Iterator iterShift = shiftedLinesList.iterator();
		//Boolean bShifted;		
				
		FeatureCollection newLines = new FeatureDataset(lines.getFeatureSchema());
		FeatureSchema schema = newLines.getFeatureSchema();		
		newLines.clear();
		
		while (iterLines.hasNext()){// && iterShift.hasNext()){
			BeamObject line;
			line = (BeamObject)iterLines.next();
			//bShifted = (Boolean)iterShift.next();
						
			try {
				Geometry geom;				
				geom = line._geom;
				Feature feat = new BasicFeature(schema);
				feat.setGeometry(geom);				
				newLines.add(feat);
			}
			catch (ClassCastException e) {
				this.addError("failure");
				return;
			}			
		}			
		
		wgreq.addResult("result", newLines);
		
	}
		
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("LineDisplacementTestSnakes", "neun", "operator",
			"",
				"LineDisplacementTestSnakes",
				"LineDisplacementTestSnakes",
				"1.0",
				new String[] {"ica.genops.cartogen.Displacement"});
		id.visible = true;
		
		//add input parameters
		String[] allowedL = {"LineString"};
		id.addInputParameter("geom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedL), "lines to displace");
		//String[] allowedP = {"Polygon"};
		//id.addInputParameter("congeom", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedP), "areas causing displacements");
		
		id.addInputParameter("dWidth", "DOUBLE", "1.3", "The width of the lines.");
		id.addInputParameter("dAsymmetryDistance", "DOUBLE", "250", "The asymmetric distance of the lines.");
		id.addInputParameter("dMinDist", "DOUBLE", "0.5", "The minimum object distance for beeing legible.");		
		id.addInputParameter("nMaxIterations", "INTEGER", "15", "The maximal number of iterations.");		
		id.addInputParameter("dEnergyThreshold", "DOUBLE", "0.1", "The aspired energy difference between two iterations to break the iteration. -1 when nMaxIterations is used.");		
		
		//add output parameters
		id.addOutputParameter("result", "FeatureCollection", new AttributeDescription("GEOMETRY", "GEOMETRY", allowedL), "displaced lines");
		return id;
	}
}
