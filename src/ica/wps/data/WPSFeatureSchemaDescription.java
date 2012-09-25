package ica.wps.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;


/**
 * Container class holds the feature schema description.
 * @author	M. Wittensoeldner
 * @date	Created on 04.02.2007
 */
public class WPSFeatureSchemaDescription {

	public enum DatatypeAttribute	{
									String,
									Integer,
									Long,
									Double,
									Boolean,
									Date,
									Complex,
									Point,
									LineString,
									Polygon,
									MultiPoint,
									MultiLineString,
									MultiPolygon,
									MultiGeometry,
									Geometry,
									}

	public enum DatatypeAttributeExt	{	// additional datatypes for list items
										Feature,
										FeatureCollection,
										}
	
	protected List<WPSSchemaAttribute>		_lstAttributes = null;
	
	/**
	 * Constructor.
	 */
	public WPSFeatureSchemaDescription() {
		_lstAttributes = new LinkedList<WPSSchemaAttribute>();
	}

	/**
	 * Adds a feature collection attribute description.
	 * @return void
	 * @param sIdentifier						The attribute identifier.
	 * @param eType								The attribute type.
	 */
	public void addFeatureCollectionAttributeDescription(String sIdentifier, DatatypeAttribute eType) {
		_lstAttributes.add(new WPSFeatureCollectionAttribute(sIdentifier, eType));
	}

	/**
	 * Adds a feature attribute description.
	 * @return void
	 * @param sIdentifier						The attribute identifier.
	 * @param eType								The attribute type.
	 */
	public void addFeatureAttributeDescription(String sIdentifier, DatatypeAttribute eType) {
		_lstAttributes.add(new WPSFeatureAttribute(sIdentifier, eType));
	}

	/**
	 * Gets the attributes.
	 * @return List<WPSSchemaAttribute>			The attributes.
	 */
	public List<WPSSchemaAttribute> getAttributes() {
		return _lstAttributes;
	}
	
	/**
	 * Clones the object.
	 * @return WPSFeatureSchemaDescription		The cloned object.
	 */
	public WPSFeatureSchemaDescription clone() {
		WPSFeatureSchemaDescription objClone = new WPSFeatureSchemaDescription();
		Iterator<WPSSchemaAttribute> iterAtt = this.getAttributes().iterator();
		WPSSchemaAttribute attribute;
		while(iterAtt.hasNext()) {
			attribute = iterAtt.next();
			if (attribute instanceof WPSFeatureAttribute)
				objClone.addFeatureAttributeDescription(attribute.getIdentifier(), attribute.getType());
			else
				objClone.addFeatureCollectionAttributeDescription(attribute.getIdentifier(), attribute.getType());
		}
		return objClone;
	}

	/**
	 * Gets a string representation of the attribute datatypes.
	 * @return String							The string representation.
	 */
	public String toString() {
		Iterator<WPSSchemaAttribute> iterAtt;
		String sItem = "[";
		iterAtt = this.getAttributes().iterator();
		while (iterAtt.hasNext()) {
			sItem += iterAtt.next().getType().name();
			if (iterAtt.hasNext())
				sItem += ", ";
		}
		sItem += "]";
		return sItem;
	}
	/**
	 * Checks whether an attribute type is a literal type.
	 * @return boolean							True whether the type is a literal type.
	 * @param type								The type to check.
	 */
	public static boolean isLiteralType(DatatypeAttribute type) {
		return ((type == DatatypeAttribute.String)
			|| (type == DatatypeAttribute.Integer)
			|| (type == DatatypeAttribute.Long)
			|| (type == DatatypeAttribute.Double)
			|| (type == DatatypeAttribute.Boolean)
			|| (type == DatatypeAttribute.Date));
	}

	/**
	 * Converts a string value (literal type) to an object.
	 * @return Object									The converted object.
	 * @param sValue									The value to convert.
	 * @param eType										The data type.
	 * @throws Exception								When an error occurs.
	 */
	public static Object convertToObject(String sValue, DatatypeAttribute eType) throws Exception {
		Object objValue;
		if (eType == DatatypeAttribute.Integer) {
			objValue = new Integer(sValue);
		} else if (eType == DatatypeAttribute.Long) {
			objValue = new Long(sValue);
		} else if (eType == DatatypeAttribute.Double) {
			objValue = new Double(sValue);				
		} else if (eType == DatatypeAttribute.Boolean) {
			objValue = new Boolean(sValue);				
		} else if (eType == DatatypeAttribute.Date) {
			try {
				objValue = new Date(Long.parseLong(sValue));				
			} catch (Exception ex) {
				objValue = new SimpleDateFormat().parse(sValue);				
			}
		} else if (eType == DatatypeAttribute.String) {
			objValue = sValue;				
		} else {
			throw new Exception("Unsupported literal type '"+eType.name()+"'.");
		}
		return objValue;
	}

	/**
	 * Converts an object value (literal type) to a string.
	 * @return String									The converted object.
	 * @param objValue									The value to convert.
	 * @throws Exception								When the objValue is not a literal type.
	 */
	public static String convertToString(Object objValue) throws Exception {
		if (objValue instanceof Integer) {
			return ((Integer)objValue).toString();
		} else if (objValue instanceof Long) {
			return ((Long)objValue).toString();
		} else if (objValue instanceof Double) {
			return ((Double)objValue).toString();
		} else if (objValue instanceof Boolean) {
			return ((Boolean)objValue).toString();
		} else if (objValue instanceof Date) {
			return new SimpleDateFormat().format((Date)objValue);		
		} else if (objValue instanceof String) {
			return (String)objValue;				
		} else {
			throw new Exception("Unsupported literal type '"+objValue.getClass().getSimpleName()+"'.");
		}
	}
}

