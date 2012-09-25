package ch.uzh.geo.webgen.wps.server;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.InputStream;

import ica.wps.common.IWPSStatusListener;
import ica.wps.data.WPSOperatorDescription;
import ica.wps.server.IWPSOperator;

import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.ShapefileWriter;
import com.vividsolutions.jump.io.ShapefileReader;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Operator class for the push displacement operator.
 * @author	M. Wittensoeldner
 * @date	Created on 04.02.2007
 */
public abstract class WPSOperatorPush implements IWPSOperator {

	protected WPSOperatorDescription	_description;
		
	/**
	 * Executes the operator.
	 * @return Map<String, Object>		The output parameters.
	 * @param mapParameterIn					The input parameters.
	 * @param statusListener					The status listener.
	 * @throws Exception						When an error occurs.
	 */
	public Map<String, Object> execute(Map<String, List<Object>> mapParameterIn, IWPSStatusListener statusListener) throws Exception {
		int nIterations = ((Integer)mapParameterIn.get("Iterations").iterator().next()).intValue();
		double dMinDistance = ((Double)mapParameterIn.get("MinDistance").iterator().next()).doubleValue();
		double dCriticalDistance = ((Double)mapParameterIn.get("CriticalDistance").iterator().next()).doubleValue();
		double dDistanceOfSteinerPoints = ((Double)mapParameterIn.get("DistanceOfSteinerPoints").iterator().next()).doubleValue();
		List<Object> lstFeatureCollections = mapParameterIn.get("FeaturesToDisplace");
		List<File> lstFilesToDelete = new LinkedList<File>();

		// add OTN attribute
		FeatureCollection fc;
		String sAttName = "OTN";
		int nCount = lstFeatureCollections.size();
		File[] arrFiles = new File[nCount];
		FeatureCollection[] arrFcResult = new FeatureCollection[nCount];
		Iterator<Object> iterFc = lstFeatureCollections.iterator();
		int nOtn;
		int k;
		int i = 0;
		try {
			while ((i < nCount) && iterFc.hasNext()) {
				fc = (FeatureCollection)iterFc.next();
				nOtn = 0;
				fc.getFeatureSchema().addAttribute(sAttName, AttributeType.INTEGER);
				Iterator<?> iterFeature = fc.getFeatures().iterator();
				Feature feature;
				while (iterFeature.hasNext()) {
					feature = (Feature)iterFeature.next();
					if (!feature.getSchema().hasAttribute(sAttName))
						feature.getSchema().addAttribute(sAttName, AttributeType.INTEGER);
					Object[] arrAttributes = new Object[feature.getSchema().getAttributeCount()];
					System.arraycopy(feature.getAttributes(), 0, arrAttributes, 0, feature.getAttributes().length);
					feature.setAttributes(arrAttributes);
					feature.setAttribute(sAttName, new Integer(nOtn));
					nOtn++;
				}
				// write the shape file
				arrFiles[i] = File.createTempFile("push", ".shp");
				lstFilesToDelete.add(arrFiles[i]);
				lstFilesToDelete.add(new File(arrFiles[i].getAbsolutePath().substring(0, arrFiles[i].getAbsolutePath().length()-4)+".shx"));
				lstFilesToDelete.add(new File(arrFiles[i].getAbsolutePath().substring(0, arrFiles[i].getAbsolutePath().length()-4)+".dbf"));
				ShapefileWriter writer = new ShapefileWriter();
				writer.write(fc, new DriverProperties(arrFiles[i].getAbsolutePath()));
				i++;
			}
			
			this.startPreprocess(mapParameterIn, arrFiles, lstFilesToDelete);
	
			// execute the push process
			List<String> lstCmd = new LinkedList<String>();
			lstCmd.add("push");
			lstCmd.add(""+nIterations);
			lstCmd.add(""+dMinDistance);
			lstCmd.add(""+dCriticalDistance);
			lstCmd.add(""+dDistanceOfSteinerPoints);
			lstCmd.add(" ");
			i = 0;
			while (i < nCount) {
				lstCmd.add(arrFiles[i].getAbsolutePath().substring(0, arrFiles[i].getAbsolutePath().length()-4));
				i++;
			}
			this.runCommand(lstCmd, true);
			
			i = 0;
			while (i < nCount) {
				lstFilesToDelete.add(new File(arrFiles[i].getAbsolutePath().substring(0, arrFiles[i].getAbsolutePath().length()-4)+"_p.shp"));
				lstFilesToDelete.add(new File(arrFiles[i].getAbsolutePath().substring(0, arrFiles[i].getAbsolutePath().length()-4)+"_p.shx"));
				lstFilesToDelete.add(new File(arrFiles[i].getAbsolutePath().substring(0, arrFiles[i].getAbsolutePath().length()-4)+"_p.dbf"));
				File file = new File(arrFiles[i].getAbsolutePath().substring(0, arrFiles[i].getAbsolutePath().length()-4)+"_p.shp");
				if (file.exists()) {
					lstCmd.clear();
					lstCmd.add("pushjoin");
					lstCmd.add(arrFiles[i].getAbsolutePath().substring(0, arrFiles[i].getAbsolutePath().length()-4)+"_p");
					lstCmd.add("otn");
					lstCmd.add(arrFiles[i].getAbsolutePath().substring(0, arrFiles[i].getAbsolutePath().length()-4));
					lstCmd.add("otn");
					lstFilesToDelete.add(new File(arrFiles[i].getAbsolutePath().substring(0, arrFiles[i].getAbsolutePath().length()-4)+"_p_par.shp"));
					lstFilesToDelete.add(new File(arrFiles[i].getAbsolutePath().substring(0, arrFiles[i].getAbsolutePath().length()-4)+"_p_par.shx"));
					lstFilesToDelete.add(new File(arrFiles[i].getAbsolutePath().substring(0, arrFiles[i].getAbsolutePath().length()-4)+"_p_par.dbf"));
					this.runCommand(lstCmd, true);
					file = new File(arrFiles[i].getAbsolutePath().substring(0, arrFiles[i].getAbsolutePath().length()-4)+"_p_par.shp");
					if (file.exists()) {
						if (true) {
							ShapefileReader reader = new ShapefileReader();
							arrFcResult[i] = reader.read(new DriverProperties(arrFiles[i].getAbsolutePath().substring(0, arrFiles[i].getAbsolutePath().length()-4)+"_p_par.shp"));
							reader = null;
							System.gc();
							boolean bPolygon = true;
							Iterator<?> iterFeature = arrFcResult[i].getFeatures().iterator();
							if (iterFeature.hasNext()) {
								bPolygon = (((Feature)iterFeature.next()).getGeometry() instanceof Polygon);
							}
							FeatureSchema schema = new FeatureSchema();
							schema.addAttribute("ID", AttributeType.STRING);
							this.addExtraAttribute(schema, bPolygon);
							schema.addAttribute("Aura", AttributeType.DOUBLE);
							schema.addAttribute("Pushable", AttributeType.DOUBLE);
							schema.addAttribute("Stiffness", AttributeType.DOUBLE);
							schema.addAttribute("Enlarge", AttributeType.DOUBLE);
							schema.addAttribute("Iteration", AttributeType.INTEGER);
							schema.addAttribute("max_dedge", AttributeType.DOUBLE);
							schema.addAttribute("max_dangle", AttributeType.DOUBLE);
							schema.addAttribute("max_dorien", AttributeType.DOUBLE);
							schema.addAttribute("max_dx", AttributeType.DOUBLE);
							schema.addAttribute("max_dy", AttributeType.DOUBLE);
							schema.addAttribute("Geometry", AttributeType.GEOMETRY);
							FeatureCollection fcNew = new FeatureDataset(schema);
							iterFeature = arrFcResult[i].getFeatures().iterator();
							Feature feature;
							while (iterFeature.hasNext()) {
								feature = (Feature)iterFeature.next();
								Object[] arrAttributes = new Object[schema.getAttributeCount()];
								k = 0;
								while (k < schema.getAttributeCount()-1) {
									arrAttributes[k] = feature.getAttribute(schema.getAttributeName(k).toLowerCase());
									k++;
								}
								arrAttributes[k] = feature.getGeometry();
								feature.setSchema(schema);
								feature.setAttributes(arrAttributes);
								fcNew.add(feature);
							}
							arrFcResult[i] = fcNew;
						}
					} else
						throw new Exception("Execution of PUSHJOIN failed.");
				} else
					throw new Exception("Execution of PUSH failed.");
				i++;
			}
		} catch (Exception ex) {
			// delete files
			Iterator<File> iterFile = lstFilesToDelete.iterator();
			while (iterFile.hasNext())
				this.deleteFile(iterFile.next());

			throw ex;
		}

		// delete files
		Iterator<File> iterFile = lstFilesToDelete.iterator();
		while (iterFile.hasNext())
			this.deleteFile(iterFile.next());

		Map<String, Object> mapResult = new HashMap<String, Object>();
		List<FeatureCollection> lstResultPoly = new LinkedList<FeatureCollection>();
		List<FeatureCollection> lstResultLine = new LinkedList<FeatureCollection>();
		i = 0;
		while (i < nCount) {
			Iterator<?> iterFeature = arrFcResult[i].getFeatures().iterator();
			if (iterFeature.hasNext()) {
				Feature f = (Feature)iterFeature.next();
				if (f.getGeometry() instanceof Polygon)
					lstResultPoly.add(arrFcResult[i]);
				else
					lstResultLine.add(arrFcResult[i]);
			}
			i++;
		}
		mapResult.put("DisplacedPolygons", lstResultPoly);
		mapResult.put("DisplacedLineStrings", lstResultLine);
		return mapResult;
	}
	
	/**
	 * Gets the operator description.
	 * @return WPSOperatorDescription			The operator description.
	 */
	public WPSOperatorDescription getOperatorDescription() {
		return _description;
	}

	/**
	 * Runs a command.
	 * @return void
	 * @param lstCmd							The command parameters.
	 * @param bWait								Waits until the process has been finished.
	 * @throws Exception						When an error occurs.
	 */
	protected void runCommand(List<String> lstCmd, boolean bWait) throws Exception {
		ProcessBuilder pb = new ProcessBuilder(lstCmd);
		Process proc = pb.start();
		if (bWait) {
			StreamGobbler streamIn = new StreamGobbler(proc.getInputStream());
			StreamGobbler streamErr = new StreamGobbler(proc.getErrorStream());
			streamIn.start();
			streamErr.start();
			while (streamIn.isAlive() || streamErr.isAlive()) {
				Thread.sleep(100);
			}
		}
	}

	/**
	 * Deletes a file.
	 * @return void
	 * @param file								The file to delete.
	 */
	protected void deleteFile(File file) {
		if ((file != null) && file.exists()) {
			file.delete();
		}
	}
	
	/**
	 * Starts the preprocessing.
	 * @return void
	 * @param mapParameterIn					The input parameters.
	 * @param arrFiles							The input shape files.
	 * @param lstFilesToDelete					The list contains files to be deleted after running the operator.
	 * @throws Exception						When an error occurs.
	 */
	protected void startPreprocess(Map<String, List<Object>> mapParameterIn, File[] arrFiles, List<File> lstFilesToDelete) throws Exception {
	}
	
	/**
	 * Adds extra schema attributes.
	 * @return void
	 * @param schema							The feature schema.
	 * @param bPolygon							The attribute to add is an attribute of a polygon.
	 */
	protected void addExtraAttribute(FeatureSchema schema, boolean bPolygon) {
	}

	public class StreamGobbler extends Thread
	{
		protected InputStream 		_streamIn;
		protected String			_sOutput;
    
		public StreamGobbler(InputStream streamIn) {
			_streamIn = streamIn;
		}
		
		public String getOutput() {
			return _sOutput;
		}
    
		public void run() {
			try {
				int nRead;
				while ((nRead = _streamIn.read()) != -1) {
					if (_sOutput == null) {
						_sOutput = ""+(char)nRead;
					} else {
						_sOutput += (char)nRead;
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				_sOutput = null;
			}
		}
	}
}

