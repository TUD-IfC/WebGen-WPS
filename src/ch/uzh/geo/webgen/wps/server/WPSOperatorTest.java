package ch.uzh.geo.webgen.wps.server;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.table.DefaultTableModel;

import ica.wps.common.IWPSStatusListener;
import ica.wps.data.WPSFeatureSchemaDescription;
import ica.wps.data.WPSOperatorDescription;
import ica.wps.data.WPSFeatureSchemaDescription.DatatypeAttribute;
import ica.wps.data.WPSFeatureSchemaDescription.DatatypeAttributeExt;
import ica.wps.data.WPSOperatorDescription.DatatypeComplex;
import ica.wps.data.WPSOperatorDescription.DatatypeLiteral;
import ica.wps.data.WPSOperatorDescription.Range;
import ica.wps.server.IWPSOperator;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.AttributeType;

/**
 * Interface defines a WPS operator.
 * @author	M. Wittensoeldner
 * @date	Created on 04.02.2007
 */
public class WPSOperatorTest implements IWPSOperator {

	protected WPSOperatorDescription	_description;
	
	/**
	 * Constructor.
	 * @param description						The operator description.
	 */
	WPSOperatorTest() {
		_description = new WPSOperatorDescription("1.1", this.getClass().getName(), "Test Operator", "Operator supports all datatypes.", true, 120L);
		try {
			int nMaxMegabyte =  5;
			_description.addDescriptionParameterIn("ParamString", "TheString", "String Parameter", null, 0, 3, DatatypeLiteral.String, "Default", null);
			Object[] arrAllowedValues = {new Integer(23), new Integer(24), new Integer(27), new Integer(28)};
			_description.addDescriptionParameterIn("ParamInteger", "TheInteger", "Integer Parameter", null, 1, 2, DatatypeLiteral.Integer, new Integer(23), arrAllowedValues);
			_description.addDescriptionParameterIn("ParamLong", "TheLong", "Long Parameter", null, 1, 1, DatatypeLiteral.Long, new Long(123456789), null);
			Range[] arrAllowedRanges = {WPSOperatorDescription.createRange(new Double(1.2), new Double(2.4)), WPSOperatorDescription.createRange(new Double(5.3), new Double(6.78))};
			_description.addDescriptionParameterIn("ParamDouble", "TheDouble", "Double Parameter", "meters", 1, 1, DatatypeLiteral.Double, null, arrAllowedRanges);
			_description.addDescriptionParameterIn("ParamBoolean", "TheBoolean", "Boolean Parameter", null, 1, 4, DatatypeLiteral.Boolean, null, null);
			_description.addDescriptionParameterIn("ParamDate", "TheDate", "Date Parameter", null, 1, 1, DatatypeLiteral.Date, new java.util.Date(), null);
			_description.addDescriptionParameterIn("ParamPoint", "ThePoint", "Point Parameter", 0, 1, nMaxMegabyte, DatatypeComplex.Point);
			_description.addDescriptionParameterIn("ParamLineString", "TheLineString", "LineString Parameter", 0, 1, nMaxMegabyte, DatatypeComplex.LineString);
			_description.addDescriptionParameterIn("ParamPolygon", "ThePolygon", "Polygon Parameter", 0, 1, nMaxMegabyte, DatatypeComplex.Polygon);
			_description.addDescriptionParameterIn("ParamMultiPoint", "TheMultiPoint", "MultiPoint Parameter", 0, 1, nMaxMegabyte, DatatypeComplex.MultiPoint);
			_description.addDescriptionParameterIn("ParamMultiLineString", "TheMultiLineString", "MultiLineString Parameter", 0, 1, nMaxMegabyte, DatatypeComplex.MultiLineString);
			_description.addDescriptionParameterIn("ParamMultiPolygon", "TheMultiPolygon", "MultiPolygon Parameter", 0, 1, nMaxMegabyte, DatatypeComplex.MultiPolygon);
			_description.addDescriptionParameterIn("ParamGeometryCollection", "TheGeometryCollection", "GeometryCollection Parameter", 0, 1, nMaxMegabyte, DatatypeComplex.GeometryCollection);
			_description.addDescriptionParameterIn("ListMix", "ListMix", "List Parameter", 0, 1, nMaxMegabyte, DatatypeComplex.List, new DatatypeAttribute[] {DatatypeAttribute.Integer, DatatypeAttribute.Double, DatatypeAttribute.Polygon});
			_description.addDescriptionParameterIn("ListLiteral", "ListLiteral", "List Parameter", 0, 1, nMaxMegabyte, DatatypeComplex.List, new DatatypeAttribute[] {DatatypeAttribute.Integer, DatatypeAttribute.Double, DatatypeAttribute.String});
			WPSFeatureSchemaDescription[] arrSchemas = new WPSFeatureSchemaDescription[1];
			arrSchemas[0] = new WPSFeatureSchemaDescription();
			arrSchemas[0].addFeatureAttributeDescription("AttString", DatatypeAttribute.String);
			arrSchemas[0].addFeatureAttributeDescription("AttInteger", DatatypeAttribute.Integer);
			arrSchemas[0].addFeatureAttributeDescription("AttLong", DatatypeAttribute.Long);
			arrSchemas[0].addFeatureAttributeDescription("AttDouble", DatatypeAttribute.Double);
			arrSchemas[0].addFeatureAttributeDescription("AttBoolean", DatatypeAttribute.Boolean);
			arrSchemas[0].addFeatureAttributeDescription("AttDate", DatatypeAttribute.Date);
//			arrSchemas[0].addAttributeDescription("AttComplex", DatatypeAttribute.Complex);
//			arrSchemas[0].addAttributeDescription("AttPoint", DatatypeAttribute.Point);
//			arrSchemas[0].addFeatureAttributeDescription("AttLineString", DatatypeAttribute.LineString);
			arrSchemas[0].addFeatureAttributeDescription("AttPolygon", DatatypeAttribute.Polygon);
//			arrSchemas[0].addFeatureAttributeDescription("AttMultiPoint", DatatypeAttribute.MultiPoint);
//			arrSchemas[0].addFeatureAttributeDescription("AttMultiLineString", DatatypeAttribute.MultiLineString);
//			arrSchemas[0].addFeatureAttributeDescription("AttMultiPolygon", DatatypeAttribute.MultiPolygon);
//			arrSchemas[0].addFeatureAttributeDescription("AttMultiGeometry", DatatypeAttribute.MultiGeometry);
//			arrSchemas[0].addFeatureAttributeDescription("AttGeometry", DatatypeAttribute.Geometry);

//			arrSchemas[0].addFeatureCollectionAttributeDescription("AttString", DatatypeAttribute.String);
//			arrSchemas[0].addFeatureCollectionAttributeDescription("AttInteger", DatatypeAttribute.Integer);
			
			WPSFeatureSchemaDescription[] arrSchemasF = new WPSFeatureSchemaDescription[1];
			arrSchemasF[0] = new WPSFeatureSchemaDescription();
			arrSchemasF[0].addFeatureAttributeDescription("AttPolygon", DatatypeAttribute.Polygon);
			
			
			_description.addDescriptionParameterIn("ParamFeature", "TheFeature", "Feature Parameter", 0, 5, nMaxMegabyte, DatatypeComplex.Feature, arrSchemasF);
			_description.addDescriptionParameterIn("ParamFeatureCollection", "TheFeatureCollection", "Feature ParameterCollection", 0, 1, nMaxMegabyte, DatatypeComplex.FeatureCollection, arrSchemas);


			WPSFeatureSchemaDescription[] arrSchemaTable = new WPSFeatureSchemaDescription[2];
			int i = 0;
			while (i < 2) {
				arrSchemaTable[i] = new WPSFeatureSchemaDescription();
				arrSchemaTable[i].addFeatureAttributeDescription("AttString", DatatypeAttribute.String);
				arrSchemaTable[i].addFeatureAttributeDescription("AttInteger", DatatypeAttribute.Integer);
				arrSchemaTable[i].addFeatureAttributeDescription("AttLong", DatatypeAttribute.Long);
				arrSchemaTable[i].addFeatureAttributeDescription("AttDouble", DatatypeAttribute.Double);
				i++;
			}
			arrSchemaTable[0].addFeatureAttributeDescription("AttBoolean", DatatypeAttribute.Boolean);
			arrSchemaTable[0].addFeatureAttributeDescription("AttLineString", DatatypeAttribute.LineString);
			arrSchemaTable[1].addFeatureAttributeDescription("AttDate", DatatypeAttribute.Date);

			_description.addDescriptionParameterIn("Table", "Table", "Table Parameter", 0, 1, nMaxMegabyte, DatatypeComplex.Table, arrSchemaTable);

			WPSFeatureSchemaDescription[] arrSchemaTableOut = new WPSFeatureSchemaDescription[2];
			i = 0;
			while (i < 2) {
				arrSchemaTableOut[i] = new WPSFeatureSchemaDescription();
				arrSchemaTableOut[i].getAttributes().addAll(arrSchemaTable[i].getAttributes());
				i++;
			}
			arrSchemaTableOut[0].addFeatureAttributeDescription("AttPolygon", DatatypeAttribute.Polygon);
			arrSchemaTableOut[1].addFeatureAttributeDescription("AttPolygon", DatatypeAttribute.Polygon);
			
			
			
			_description.addDescriptionParameterIn("ListFeature", "ListFeature", "List Parameter", 0, 1, nMaxMegabyte, DatatypeComplex.List, DatatypeAttributeExt.Feature, arrSchemasF);
			_description.addDescriptionParameterIn("ListFeatureCollection", "ListFeatureCollection", "List Parameter", 0, 1, nMaxMegabyte, DatatypeComplex.List, DatatypeAttributeExt.FeatureCollection, arrSchemasF);
			
			
			_description.addDescriptionParameterOut("ParamString", "TheString", "String Parameter", null, DatatypeLiteral.String);
			_description.addDescriptionParameterOut("ParamInteger", "TheInteger", "Integer Parameter", null, DatatypeLiteral.Integer);
			_description.addDescriptionParameterOut("ParamLong", "TheLong", "Long Parameter", null, DatatypeLiteral.Long);
			_description.addDescriptionParameterOut("ParamDouble", "TheDouble", "Double Parameter", "meters", DatatypeLiteral.Double);
			_description.addDescriptionParameterOut("ParamBoolean", "TheBoolean", "Boolean Parameter", null, DatatypeLiteral.Boolean);
			_description.addDescriptionParameterOut("ParamDate", "TheDate", "Date Parameter", null, DatatypeLiteral.Date);
			_description.addDescriptionParameterOut("ParamPoint", "ThePoint", "Point Parameter", DatatypeComplex.Point);
			_description.addDescriptionParameterOut("ParamLineString", "TheLineString", "LineString Parameter", DatatypeComplex.LineString);
			_description.addDescriptionParameterOut("ParamPolygon", "ThePolygon", "Polygon Parameter", DatatypeComplex.Polygon);
			_description.addDescriptionParameterOut("ParamMultiPoint", "TheMultiPoint", "MultiPoint Parameter", DatatypeComplex.MultiPoint);
			_description.addDescriptionParameterOut("ParamMultiLineString", "TheMultiLineString", "MultiLineString Parameter", DatatypeComplex.MultiLineString);
			_description.addDescriptionParameterOut("ParamMultiPolygon", "TheMultiPolygon", "MultiPolygon Parameter", DatatypeComplex.MultiPolygon);
			_description.addDescriptionParameterOut("ParamGeometryCollection", "TheGeometryCollection", "GeometryCollection Parameter", DatatypeComplex.GeometryCollection);
			_description.addDescriptionParameterOut("ParamFeature", "TheFeature", "Feature Parameter", DatatypeComplex.Feature, arrSchemas);
			_description.addDescriptionParameterOut("ParamFeatureCollection", "TheFeatureCollection", "Feature ParameterCollection", DatatypeComplex.FeatureCollection, arrSchemas);
			_description.addDescriptionParameterOut("ListMix", "ListMix", "List Parameter", DatatypeComplex.List, new DatatypeAttribute[] {DatatypeAttribute.Integer, DatatypeAttribute.Double, DatatypeAttribute.Polygon});
			_description.addDescriptionParameterOut("ListLiteral", "ListLiteral", "List Parameter", DatatypeComplex.List, new DatatypeAttribute[] {DatatypeAttribute.Integer, DatatypeAttribute.Double, DatatypeAttribute.String});
			_description.addDescriptionParameterOut("ListFeature", "ListFeature", "List Parameter", DatatypeComplex.List, DatatypeAttributeExt.Feature, arrSchemasF);
			_description.addDescriptionParameterOut("ListFeatureCollection", "ListFeatureCollection", "List Parameter", DatatypeComplex.List, DatatypeAttributeExt.FeatureCollection, arrSchemasF);
			_description.addDescriptionParameterOut("Table", "Table", "Table Parameter", DatatypeComplex.Table, arrSchemaTableOut);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Executes the operator.
	 * @return Map<String, Object>		The output parameters.
	 * @param mapParameterIn					The input parameters.
	 * @param statusListener					The status listener.
	 * @throws Exception						When an error occurs.
	 */
	public Map<String, Object> execute(Map<String, List<Object>> mapParameterIn, IWPSStatusListener statusListener) throws Exception {
		GeometryFactory factory = new GeometryFactory();
				
		Map<String, Object> mapResult = new HashMap<String, Object>();

		mapResult.put("ParamString", "Test2");

		mapResult.put("ParamInteger", new Integer(34));

		mapResult.put("ParamLong", new Long(34567));

		mapResult.put("ParamDouble", new Double(111.22));

		mapResult.put("ParamBoolean", new Boolean(true));

		mapResult.put("ParamDate", new java.util.Date());

		mapResult.put("ParamPoint", factory.createPoint(new Coordinate(12, 32)));

		mapResult.put("ParamLineString", factory.createLineString(new Coordinate[] {new Coordinate(1, 2), new Coordinate(2, 3)}));

		mapResult.put("ParamPolygon", factory.createPolygon(factory.createLinearRing(new Coordinate[] {new Coordinate(1, 2), new Coordinate(2, 3), new Coordinate(22, 33), new Coordinate(1, 2)}), null));

		mapResult.put("ParamMultiPoint", factory.createMultiPoint(new Coordinate[] {new Coordinate(1, 2), new Coordinate(2, 3)}));

		mapResult.put("ParamMultiLineString", factory.createMultiLineString(new LineString[] {factory.createLineString(new Coordinate[] {new Coordinate(1, 2), new Coordinate(2, 3)}), factory.createLineString(new Coordinate[] {new Coordinate(4, 2), new Coordinate(5, 3)})}));

		mapResult.put("ParamMultiPolygon", factory.createMultiPolygon(new Polygon[] {factory.createPolygon(factory.createLinearRing(new Coordinate[] {new Coordinate(1, 2), new Coordinate(2, 3), new Coordinate(22, 33), new Coordinate(1, 2)}), null), factory.createPolygon(factory.createLinearRing(new Coordinate[] {new Coordinate(32, 4), new Coordinate(43, 2), new Coordinate(322, 34), new Coordinate(32, 4)}), null)}));

		mapResult.put("ParamGeometryCollection", factory.createGeometryCollection(new Geometry[] {factory.createPoint(new Coordinate(12, 32)), factory.createPolygon(factory.createLinearRing(new Coordinate[] {new Coordinate(1, 2), new Coordinate(2, 3), new Coordinate(22, 33), new Coordinate(1, 2)}), null), factory.createPolygon(factory.createLinearRing(new Coordinate[] {new Coordinate(32, 4), new Coordinate(43, 2), new Coordinate(322, 34), new Coordinate(32, 4)}), null)}));
		
		mapResult.put("ListMix", mapParameterIn.get("ListMix").iterator().next());

		mapResult.put("ListLiteral", mapParameterIn.get("ListLiteral").iterator().next());

		mapResult.put("ListFeature", mapParameterIn.get("ListFeature").iterator().next());

		mapResult.put("ListFeatureCollection", mapParameterIn.get("ListFeatureCollection").iterator().next());
		
		DefaultTableModel tableModel = (DefaultTableModel)mapParameterIn.get("Table").iterator().next();
		Object[] arrPolygon = new Object[tableModel.getRowCount()];
		int i = 0;
		while (i < tableModel.getRowCount()) {
			arrPolygon[i] = factory.createPolygon(factory.createLinearRing(new Coordinate[] {new Coordinate(1+i*10, 2+i*10), new Coordinate(2+i*10, 3+i*10), new Coordinate(22+i*10, 33+i*10), new Coordinate(1+i*10, 2+i*10)}), null);
			i++;
		}
		tableModel.addColumn("AttPolygon", arrPolygon);
		mapResult.put("Table", tableModel);

		FeatureSchema schema = new FeatureSchema();
		schema.addAttribute("AttString", AttributeType.STRING);
		schema.addAttribute("AttInteger", AttributeType.INTEGER);
		schema.addAttribute("AttLong", AttributeType.OBJECT);
		schema.addAttribute("AttDouble", AttributeType.DOUBLE);
		schema.addAttribute("AttBoolean", AttributeType.OBJECT);
		schema.addAttribute("AttDate", AttributeType.DATE);
//		schema.addAttribute("AttComplex", AttributeType.OBJECT);
//		schema.addAttribute("AttPoint", AttributeType.GEOMETRY);
//		schema.addAttribute("AttLineString", AttributeType.GEOMETRY);
		schema.addAttribute("AttPolygon", AttributeType.GEOMETRY);
//		schema.addAttribute("AttMultiPoint", AttributeType.GEOMETRY);
//		schema.addAttribute("AttMultiLineString", AttributeType.GEOMETRY);
//		schema.addAttribute("AttMultiPolygon", AttributeType.GEOMETRY);
//		schema.addAttribute("AttMultiGeometry", AttributeType.GEOMETRY);
//		schema.addAttribute("AttGeometry", AttributeType.GEOMETRY);

		Feature feature = new BasicFeature(schema);
		feature.setAttribute("AttString", "Attribute String");
		feature.setAttribute("AttInteger", new Integer(332));
		feature.setAttribute("AttLong", new Long(6666));
		feature.setAttribute("AttDouble", new Double(347.32));
		feature.setAttribute("AttBoolean", new Boolean(true));
		feature.setAttribute("AttDate", new java.util.Date());
//		feature.setAttribute("AttComplex", new Point2D.Double(12.23, 123.445));
		feature.setAttribute("AttPolygon", factory.createPolygon(factory.createLinearRing(new Coordinate[] {new Coordinate(1, 2), new Coordinate(2, 3), new Coordinate(22, 33), new Coordinate(1, 2)}), null));
		mapResult.put("ParamFeature", feature);

		FeatureDataset fc = new FeatureDataset(schema);
		fc.add(feature);
		fc.add(feature.clone(true));
		mapResult.put("ParamFeatureCollection", fc);
		
		i = 0;
		while (i < 100) {
			statusListener.setStatus((double)i/100.0, "Running...");
			Thread.sleep(1000);
			i += 10;
		}
		
		return mapResult;
	}
	
	/**
	 * Gets the operator description.
	 * @return WPSOperatorDescription			The operator description.
	 */
	public WPSOperatorDescription getOperatorDescription() {
		return _description;
	}
}

