package ch.uzh.geo.webgen.wps.server;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.io.File;
import javax.swing.table.DefaultTableModel;

import ica.wps.data.WPSFeatureSchemaDescription;
import ica.wps.data.WPSOperatorDescription;
import ica.wps.data.WPSFeatureSchemaDescription.DatatypeAttribute;
import ica.wps.data.WPSFeatureSchemaDescription.DatatypeAttributeExt;
import ica.wps.data.WPSOperatorDescription.DatatypeComplex;
import ica.wps.data.WPSOperatorDescription.DatatypeLiteral;

import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.ShapefileWriter;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Operator class for the push displacement operator.
 * @author	M. Wittensoeldner
 * @date	Created on 04.02.2007
 */
public class WPSOperatorPushParameter extends WPSOperatorPush {

	protected File[]					_arrFileParameter = new File[2];
	protected File[]					_arrFile;
	protected boolean[]					_arrDoublePriority = new boolean[2];					
	
	/**
	 * Constructor.
	 * @param description						The operator description.
	 */
	WPSOperatorPushParameter() {
		_description = new WPSOperatorDescription("1.0", this.getClass().getName(), "PUSH Displacement Param", "PUSH displacement operator. The operator is limited to 250 features.", false, 3000L);
		_description.addClassification("ica.genops.cartogen.Displacement");
		try {
			int nMaxMegabyte =  50;
			WPSFeatureSchemaDescription[] arrSchemasIn = new WPSFeatureSchemaDescription[4];
			arrSchemasIn[0] = new WPSFeatureSchemaDescription();
			arrSchemasIn[0].addFeatureAttributeDescription("ID", DatatypeAttribute.String);
			arrSchemasIn[0].addFeatureAttributeDescription("Priority", DatatypeAttribute.Integer);
			arrSchemasIn[0].addFeatureAttributeDescription("Polygon", DatatypeAttribute.Polygon);

			arrSchemasIn[1] = new WPSFeatureSchemaDescription();
			arrSchemasIn[1].addFeatureAttributeDescription("ID", DatatypeAttribute.String);
			arrSchemasIn[1].addFeatureAttributeDescription("Priority", DatatypeAttribute.Double);
			arrSchemasIn[1].addFeatureAttributeDescription("Polygon", DatatypeAttribute.Polygon);
			
			arrSchemasIn[2] = new WPSFeatureSchemaDescription();
			arrSchemasIn[2].addFeatureAttributeDescription("ID", DatatypeAttribute.String);
			arrSchemasIn[2].addFeatureAttributeDescription("Priority", DatatypeAttribute.Integer);
			arrSchemasIn[2].addFeatureAttributeDescription("LineString", DatatypeAttribute.LineString);

			arrSchemasIn[3] = new WPSFeatureSchemaDescription();
			arrSchemasIn[3].addFeatureAttributeDescription("ID", DatatypeAttribute.String);
			arrSchemasIn[3].addFeatureAttributeDescription("Priority", DatatypeAttribute.Double);
			arrSchemasIn[3].addFeatureAttributeDescription("LineString", DatatypeAttribute.LineString);
			
			WPSFeatureSchemaDescription[] arrSchemaTable = new WPSFeatureSchemaDescription[2];
			int i = 0;
			while (i < 2) {
				arrSchemaTable[i] = new WPSFeatureSchemaDescription();
				if (i == 0)
					arrSchemaTable[i].addFeatureAttributeDescription("Priority", DatatypeAttribute.Integer);
				else
					arrSchemaTable[i].addFeatureAttributeDescription("Priority", DatatypeAttribute.Double);
				arrSchemaTable[i].addFeatureAttributeDescription("Aura", DatatypeAttribute.Double);
				arrSchemaTable[i].addFeatureAttributeDescription("Pushable", DatatypeAttribute.Double);
				arrSchemaTable[i].addFeatureAttributeDescription("Stiffness", DatatypeAttribute.Double);
				i++;
			}

			WPSFeatureSchemaDescription[] arrSchemasOut = new WPSFeatureSchemaDescription[4];
			i = 0;
			while (i < 4) {
				arrSchemasOut[i] = new WPSFeatureSchemaDescription();
				arrSchemasOut[i].addFeatureAttributeDescription("ID", DatatypeAttribute.String);
				if (i < 2)
					arrSchemasOut[i].addFeatureAttributeDescription("Priority", DatatypeAttribute.Integer);
				else
					arrSchemasOut[i].addFeatureAttributeDescription("Priority", DatatypeAttribute.Double);
				arrSchemasOut[i].addFeatureAttributeDescription("Aura", DatatypeAttribute.Double);
				arrSchemasOut[i].addFeatureAttributeDescription("Pushable", DatatypeAttribute.Double);
				arrSchemasOut[i].addFeatureAttributeDescription("Stiffness", DatatypeAttribute.Double);
				arrSchemasOut[i].addFeatureAttributeDescription("Enlarge", DatatypeAttribute.Double);
				arrSchemasOut[i].addFeatureAttributeDescription("Iteration", DatatypeAttribute.Integer);
				arrSchemasOut[i].addFeatureAttributeDescription("max_dedge", DatatypeAttribute.Double);
				arrSchemasOut[i].addFeatureAttributeDescription("max_dangle", DatatypeAttribute.Double);
				arrSchemasOut[i].addFeatureAttributeDescription("max_dorien", DatatypeAttribute.Double);
				arrSchemasOut[i].addFeatureAttributeDescription("max_dx", DatatypeAttribute.Double);
				arrSchemasOut[i].addFeatureAttributeDescription("max_dy", DatatypeAttribute.Double);
				i++;
			}
			arrSchemasOut[0].addFeatureAttributeDescription("Geometry", DatatypeAttribute.Polygon);
			arrSchemasOut[1].addFeatureAttributeDescription("Geometry", DatatypeAttribute.LineString);
			arrSchemasOut[2].addFeatureAttributeDescription("Geometry", DatatypeAttribute.Polygon);
			arrSchemasOut[3].addFeatureAttributeDescription("Geometry", DatatypeAttribute.LineString);

			_description.addDescriptionParameterIn("FeaturesToDisplace", "FeaturesToDisplace", "The features (polygon, linestring) to displace. The operator is limited to 250 features.", 1, Integer.MAX_VALUE, nMaxMegabyte, DatatypeComplex.FeatureCollection, arrSchemasIn);
			_description.addDescriptionParameterIn("ParameterPolygon", "ParameterPolygon", "Assignment of the Aura, Pushable and Stiffness parameter depending on the polygon feature Priority.<br>Recommendation:<br><table border='1'><tr><td>Priority</td><td>Aura</td><td>Pushable</td><td>Stiffness</td></tr><tr><td>1</td><td>1.5</td><td>0.01</td><td>0.1</td></tr><tr><td>2</td><td>1.5</td><td>0.01</td><td>0.1</td></tr><tr><td>3</td><td>1.5</td><td>0.01</td><td>0.1</td></tr><tr><td>4</td><td>1.5</td><td>0.01</td><td>0.1</td></tr></table>", 1, 1, nMaxMegabyte, DatatypeComplex.Table, arrSchemaTable);
			_description.addDescriptionParameterIn("ParameterLineString", "ParameterLineString", "Assignment of the Aura, Pushable and Stiffness parameter depending on the line string feature Priority.<br>Recommendation:<br><table border='1'><tr><td>Priority</td><td>Aura</td><td>Pushable</td><td>Stiffness</td></tr><tr><td>1</td><td>19.375</td><td>0.01</td><td>0.1</td></tr><tr><td>2</td><td>17.500</td><td>0.01</td><td>0.1</td></tr><tr><td>3</td><td>15.000</td><td>0.01</td><td>0.1</td></tr><tr><td>4</td><td>15.000</td><td>0.01</td><td>0.1</td></tr><tr><td>5</td><td>7.375</td><td>0.01</td><td>0.1</td></tr></table>", 1, 1, nMaxMegabyte, DatatypeComplex.Table, arrSchemaTable);
			_description.addDescriptionParameterIn("Iterations", "Iterations", "The number of iterations", null, 1, 1, DatatypeLiteral.Integer, new Integer(3), null);
			_description.addDescriptionParameterIn("MinDistance", "MinDistance", "This value specifies a general minimum distance that has to be enforced among the objects – in addition to the individual aura-values.", "meters", 1, 1, DatatypeLiteral.Double, new Double(1.0), null);
			_description.addDescriptionParameterIn("CriticalDistance", "CriticalDistance", "Instead of shifting objects apart, a certain threshold may specify that objects are attracted and move towards each other.", "meters", 1, 1, DatatypeLiteral.Double, new Double(-1.0), null);
			_description.addDescriptionParameterIn("DistanceOfSteinerPoints", "DistanceOfSteinerPoints", "The neighbourhood between the objects is determined based on a Constraint Delaunay Triangulation.", "meters", 1, 1, DatatypeLiteral.Double, new Double(30.0), null);
			
			_description.addDescriptionParameterOut("DisplacedPolygons", "DisplacedPolygons", "The displaced polygon features.", DatatypeComplex.List, DatatypeAttributeExt.FeatureCollection, new WPSFeatureSchemaDescription[] {arrSchemasOut[0], arrSchemasOut[2]});
			_description.addDescriptionParameterOut("DisplacedLineStrings", "DisplacedLineStrings", "The displaced line string features.", DatatypeComplex.List, DatatypeAttributeExt.FeatureCollection, new WPSFeatureSchemaDescription[] {arrSchemasOut[1], arrSchemasOut[3]});
			
		} catch (Exception ex) {
			ex.printStackTrace();
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
		boolean[] arrPolygon = new boolean[arrFiles.length];
		FeatureSchema[] arrSchema = new FeatureSchema[2];
		List<Object> lstFeatureCollections = mapParameterIn.get("FeaturesToDisplace");
		Iterator<Object> iterFc = lstFeatureCollections.iterator();
		int i = 0;
		int k;
		FeatureCollection fc;
		while (iterFc.hasNext()) {
			fc = (FeatureCollection)iterFc.next();
			if (fc.getFeatures().iterator().hasNext())
				arrPolygon[i] = (((Feature)fc.getFeatures().iterator().next()).getGeometry() instanceof Polygon);
			_arrDoublePriority[arrPolygon[i] ? 0 : 1] = (fc.getFeatureSchema().getAttributeType("Priority") == AttributeType.DOUBLE);
			i++;
		}

		GeometryFactory factory = new GeometryFactory();
		String sName = "ParameterPolygon";
		int nCount = 0;
		while (nCount < 2) {
			arrSchema[nCount] = new FeatureSchema();
			arrSchema[nCount].addAttribute("Priority", _arrDoublePriority[nCount] ? AttributeType.DOUBLE : AttributeType.INTEGER);
			arrSchema[nCount].addAttribute("Aura", AttributeType.DOUBLE);
			arrSchema[nCount].addAttribute("Pushable", AttributeType.DOUBLE);
			arrSchema[nCount].addAttribute("Stiffness", AttributeType.DOUBLE);
			arrSchema[nCount].addAttribute("Dummy", AttributeType.GEOMETRY);
			fc = new FeatureDataset(arrSchema[nCount]);
			DefaultTableModel table = (DefaultTableModel)((List<Object>)mapParameterIn.get(sName)).iterator().next();
			i = 0;
			while (i < table.getRowCount()) {
				Feature feature = new BasicFeature(arrSchema[nCount]);
				k = 0;
				while (k < table.getColumnCount()) {
					feature.setAttribute(table.getColumnName(k), table.getValueAt(i, k));
					k++;
				}
				feature.setGeometry(factory.createPoint(new Coordinate()));
				fc.add(feature);
				i++;
			}
			_arrFileParameter[nCount] = File.createTempFile("push", ".shp");
			lstFilesToDelete.add(_arrFileParameter[nCount]);
			lstFilesToDelete.add(new File(_arrFileParameter[nCount].getAbsolutePath().substring(0, _arrFileParameter[nCount].getAbsolutePath().length()-4)+".dbf"));
			lstFilesToDelete.add(new File(_arrFileParameter[nCount].getAbsolutePath().substring(0, _arrFileParameter[nCount].getAbsolutePath().length()-4)+".shx"));
			if (fc.getFeatures().size() > 0) {
				ShapefileWriter writer = new ShapefileWriter();
				writer.write(fc, new DriverProperties(_arrFileParameter[nCount].getAbsolutePath()));
			}
			sName = "ParameterLineString";
			nCount++;
		}
		
		List<String> lstCmd = new LinkedList<String>();
		_arrFile = new File[arrFiles.length];
		i = 0;
		while (i < arrFiles.length) {
			_arrFile[i] = arrFiles[i];
			lstCmd.clear();
			lstCmd.add("pushjoin");
			lstCmd.add(arrFiles[i].getAbsolutePath().substring(0, arrFiles[i].getAbsolutePath().length()-4));
			lstCmd.add("Priority");
			if (arrPolygon[i])
				lstCmd.add(_arrFileParameter[0].getAbsolutePath().substring(0, _arrFileParameter[0].getAbsolutePath().length()-4));
			else
				lstCmd.add(_arrFileParameter[1].getAbsolutePath().substring(0, _arrFileParameter[1].getAbsolutePath().length()-4));
			lstCmd.add("Priority");
			lstFilesToDelete.add(new File(arrFiles[i].getAbsolutePath().substring(0, arrFiles[i].getAbsolutePath().length()-4)+"_par.shp"));
			lstFilesToDelete.add(new File(arrFiles[i].getAbsolutePath().substring(0, arrFiles[i].getAbsolutePath().length()-4)+"_par.dbf"));
			lstFilesToDelete.add(new File(arrFiles[i].getAbsolutePath().substring(0, arrFiles[i].getAbsolutePath().length()-4)+"_par.shx"));
			this.runCommand(lstCmd, true);
			arrFiles[i] = new File(arrFiles[i].getAbsolutePath().substring(0, arrFiles[i].getAbsolutePath().length()-4)+"_par.shp");
			if (!arrFiles[i].exists())
				throw new Exception("Execution of PUSHJOIN failed.");
			i++;
		}
	}
	
	/**
	 * Adds extra schema attributes.
	 * @return void
	 * @param schema							The feature schema.
	 * @param bPolygon							The attribute to add is an attribute of a polygon.
	 */
	protected void addExtraAttribute(FeatureSchema schema, boolean bPolygon) {
		if (_arrDoublePriority[bPolygon ? 0 : 1])
			schema.addAttribute("Priority", AttributeType.DOUBLE);
		else
			schema.addAttribute("Priority", AttributeType.INTEGER);			
	}

}

