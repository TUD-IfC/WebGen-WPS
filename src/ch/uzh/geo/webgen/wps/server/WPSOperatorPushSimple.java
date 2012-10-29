package ch.uzh.geo.webgen.wps.server;

import ica.wps.data.WPSFeatureSchemaDescription;
import ica.wps.data.WPSOperatorDescription;
import ica.wps.data.WPSFeatureSchemaDescription.DatatypeAttribute;
import ica.wps.data.WPSFeatureSchemaDescription.DatatypeAttributeExt;
import ica.wps.data.WPSOperatorDescription.DatatypeComplex;
import ica.wps.data.WPSOperatorDescription.DatatypeLiteral;

/**
 * Operator class for the push displacement operator.
 * @author	M. Wittensoeldner
 * @date	Created on 04.02.2007
 */
public class WPSOperatorPushSimple extends WPSOperatorPush {
	
	/**
	 * Constructor.
	 * @param description						The operator description.
	 */
	WPSOperatorPushSimple() {
		_description = new WPSOperatorDescription("1.0", this.getClass().getName(), "PUSH Displacement", "PUSH displacement operator. The operator is limited to 250 features.", false, 3000L);
		_description.addClassification("ica.genops.cartogen.Displacement");
		try {
			int nMaxMegabyte =  50;
			WPSFeatureSchemaDescription[] arrSchemasIn = new WPSFeatureSchemaDescription[2];
			int i = 0;
			while (i < 2) {
				arrSchemasIn[i] = new WPSFeatureSchemaDescription();
				arrSchemasIn[i].addFeatureAttributeDescription("ID", DatatypeAttribute.String);
				arrSchemasIn[i].addFeatureAttributeDescription("Aura", DatatypeAttribute.Double);
				arrSchemasIn[i].addFeatureAttributeDescription("Pushable", DatatypeAttribute.Double);
				arrSchemasIn[i].addFeatureAttributeDescription("Stiffness", DatatypeAttribute.Double);
				i++;
			}
			arrSchemasIn[0].addFeatureAttributeDescription("Polygon", DatatypeAttribute.Polygon);
			arrSchemasIn[1].addFeatureAttributeDescription("LineString", DatatypeAttribute.LineString);			
			
			WPSFeatureSchemaDescription[] arrSchemasOut = new WPSFeatureSchemaDescription[2];
			i = 0;
			while (i < 2) {
				arrSchemasOut[i] = new WPSFeatureSchemaDescription();
				arrSchemasOut[i].addFeatureAttributeDescription("ID", DatatypeAttribute.String);
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

			_description.addDescriptionParameterIn("FeaturesToDisplace", "FeaturesToDisplace", "The features (polygon, linestring) to displace. The operator is limited to 250 features.", 1, Integer.MAX_VALUE, nMaxMegabyte, DatatypeComplex.FeatureCollection, arrSchemasIn);
			_description.addDescriptionParameterIn("Iterations", "Iterations", "The number of iterations", null, 1, 1, DatatypeLiteral.Integer, new Integer(3), null);
			_description.addDescriptionParameterIn("MinDistance", "MinDistance", "This value specifies a general minimum distance that has to be enforced among the objects – in addition to the individual aura-values.", "meters", 1, 1, DatatypeLiteral.Double, new Double(1.0), null);
			_description.addDescriptionParameterIn("CriticalDistance", "CriticalDistance", "Instead of shifting objects apart, a certain threshold may specify that objects are attracted and move towards each other.", "meters", 1, 1, DatatypeLiteral.Double, new Double(-1.0), null);
			_description.addDescriptionParameterIn("DistanceOfSteinerPoints", "DistanceOfSteinerPoints", "The neighbourhood between the objects is determined based on a Constraint Delaunay Triangulation.", "meters", 1, 1, DatatypeLiteral.Double, new Double(30.0), null);
			
			_description.addDescriptionParameterOut("DisplacedPolygons", "DisplacedPolygons", "The displaced polygon features.", DatatypeComplex.List, DatatypeAttributeExt.FeatureCollection, new WPSFeatureSchemaDescription[] {arrSchemasOut[0]});
			_description.addDescriptionParameterOut("DisplacedLineStrings", "DisplacedLineStrings", "The displaced line string features.", DatatypeComplex.List, DatatypeAttributeExt.FeatureCollection, new WPSFeatureSchemaDescription[] {arrSchemasOut[1]});
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}

