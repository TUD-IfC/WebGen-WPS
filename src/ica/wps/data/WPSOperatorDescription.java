package ica.wps.data;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import ica.wps.data.WPSFeatureSchemaDescription.DatatypeAttribute;
import ica.wps.data.WPSFeatureSchemaDescription.DatatypeAttributeExt;

/**
 * Container class holds the operator description.
 * @author	M. Wittensoeldner
 * @date	Created on 04.02.2007
 */
public class WPSOperatorDescription extends WPSOperatorDescriptionSmall {

	public enum DatatypeLiteral	{
									String,
									Integer,
									Long,
									Double,
									Boolean,
									Date,
									// when add some new literal datatype, adjust the method WPSOperatorDescriptionParameterLiteral.isValid(Object obj)
								}
	
	public enum DatatypeComplex	{
									FeatureCollection,
									Feature,
									Point,
									LineString,
									Polygon,
									MultiPoint,
									MultiLineString,
									MultiPolygon,
									GeometryCollection,
									List,
									Table,
								}
	
	protected List<WPSOperatorDescriptionParameter>		_lstParameterIn;
	protected List<WPSOperatorDescriptionParameter>		_lstParameterOut;
	protected boolean									_bSupportStatus;
	protected boolean									_bSupportStore = true;
	protected long										_lTimeoutSeconds;
	
	public static Range createRange(Object minValue, Object maxValue)	{return new WPSOperatorDescription(null, null, null, null, false, 0L).new Range(minValue, maxValue);}

	/**
	 * Constructor.
	 * @param sVersion							The operator version.
	 * @param sIdentifier						The operator identifier.
	 * @param sTitle							The operator title (visible parameter name).
	 * @param sDescription						The operator description.
	 * @param bSupportStatus					The operator supports status/progress information.
	 * @param lTimeoutSeconds					The operator timeout in seconds.
	 */
	public WPSOperatorDescription(String sVersion, String sIdentifier, String sTitle, String sDescription, boolean bSupportStatus, long lTimeoutSeconds) {
		super(sVersion, sIdentifier, sTitle, sDescription);
		_lstParameterIn = new LinkedList<WPSOperatorDescriptionParameter>();
		_lstParameterOut = new LinkedList<WPSOperatorDescriptionParameter>();
		_bSupportStatus = bSupportStatus;
		_lTimeoutSeconds = lTimeoutSeconds;
	}
	
	/**
	 * Constructor.
	 * @param sVersion							The operator version.
	 * @param sIdentifier						The operator identifier.
	 * @param sTitle							The operator title (visible parameter name).
	 * @param sDescription						The operator description.
	 * @param bSupportStatus					The operator supports status/progress information.
	 * @param bSupportStore						The operator supports storing results on server.
	 */
	public WPSOperatorDescription(String sVersion, String sIdentifier, String sTitle, String sDescription, boolean bSupportStatus, boolean bSupportStore) {
		this(sVersion, sIdentifier, sTitle, sDescription, bSupportStatus, Long.MAX_VALUE);
		_bSupportStore = bSupportStore;
	}

	/**
	 * Adds an input description parameter.
	 * @param sIdentifier						The parameter identifier.
	 * @param sTitle							The parameter title (visible parameter name).
	 * @param sDescription						The parameter description.
	 * @param sUnitOfMeasure					The parameter unit.
	 * @param nMinOccurs						The minimal occurance.
	 * @param nMaxOccurs						The maximal occurance.
	 * @param eType								The data type.
	 * @param objDefaultValue					The default value. Can be null.
	 * @param arrAllowedRanges					The allowed value ranges for this parameter. Can be null.
	 * @throws Exception						When an error occurs.
	 */
	public void addDescriptionParameterIn(String sIdentifier,
											String sTitle,
											String sDescription,
											String sUnitOfMeasure,
											int nMinOccurs,
											int nMaxOccurs,
											DatatypeLiteral eType,
											Object objDefaultValue,
											Range[] arrAllowedRanges) throws Exception {
		_lstParameterIn.add(new WPSOperatorDescriptionParameterLiteral(sIdentifier, sTitle, sDescription, sUnitOfMeasure, nMinOccurs, nMaxOccurs, eType, objDefaultValue, arrAllowedRanges, null));
	}

	/**
	 * Adds an input description parameter.
	 * @param sIdentifier						The parameter identifier.
	 * @param sTitle							The parameter title (visible parameter name).
	 * @param sDescription						The parameter description.
	 * @param sUnitOfMeasure					The parameter unit.
	 * @param nMinOccurs						The minimal occurance.
	 * @param nMaxOccurs						The maximal occurance.
	 * @param eType								The data type.
	 * @param objDefaultValue					The default value. Can be null.
	 * @param arrAllowedValues					The allowed values for this parameter. Can be null.
	 * @throws Exception						When an error occurs.
	 */
	public void addDescriptionParameterIn(String sIdentifier,
											String sTitle,
											String sDescription,
											String sUnitOfMeasure,
											int nMinOccurs,
											int nMaxOccurs,
											DatatypeLiteral eType,
											Object objDefaultValue,
											Object[] arrAllowedValues) throws Exception {
		_lstParameterIn.add(new WPSOperatorDescriptionParameterLiteral(sIdentifier, sTitle, sDescription, sUnitOfMeasure, nMinOccurs, nMaxOccurs, eType, objDefaultValue, null, arrAllowedValues));
	}

	/**
	 * Adds an input description parameter.
	 * @param sIdentifier						The parameter identifier.
	 * @param sTitle							The parameter title (visible parameter name).
	 * @param sDescription						The parameter description.
	 * @param nMinOccurs						The minimal occurance.
	 * @param nMaxOccurs						The maximal occurance.
	 * @param lMaxMegabyte						The maximal size of parameter data.
	 * @param eType								The data type.
	 */
	public void addDescriptionParameterIn(String sIdentifier,
											String sTitle,
											String sDescription,
											int nMinOccurs,
											int nMaxOccurs,
											long lMaxMegabyte,
											DatatypeComplex eType) {
		_lstParameterIn.add(new WPSOperatorDescriptionParameterComplex(sIdentifier, sTitle, sDescription, nMinOccurs, nMaxOccurs, lMaxMegabyte, eType));
	}

	/**
	 * Adds an input description parameter.
	 * @param sIdentifier						The parameter identifier.
	 * @param sTitle							The parameter title (visible parameter name).
	 * @param sDescription						The parameter description.
	 * @param nMinOccurs						The minimal occurance.
	 * @param nMaxOccurs						The maximal occurance.
	 * @param lMaxMegabyte						The maximal size of parameter data.
	 * @param eType								The data type.
	 * @param arrAllowedSchemaDescriptions		The allowed schema descriptions.
	 * @throws Exception						When an error occurs.
	 */
	public void addDescriptionParameterIn(String sIdentifier,
											String sTitle,
											String sDescription,
											int nMinOccurs,
											int nMaxOccurs,
											long lMaxMegabyte,
											DatatypeComplex eType,
											WPSFeatureSchemaDescription[] arrAllowedSchemaDescriptions) throws Exception {
		_lstParameterIn.add(new WPSOperatorDescriptionParameterComplex(sIdentifier, sTitle, sDescription, nMinOccurs, nMaxOccurs, lMaxMegabyte, eType, arrAllowedSchemaDescriptions));
	}
	
	/**
	 * Adds an input description parameter of type list.
	 * @param sIdentifier						The parameter identifier.
	 * @param sTitle							The parameter title (visible parameter name).
	 * @param sDescription						The parameter description.
	 * @param nMinOccurs						The minimal occurance.
	 * @param nMaxOccurs						The maximal occurance.
	 * @param lMaxMegabyte						The maximal size of parameter data.
	 * @param eType								The data type.
	 * @param arrAllowedListDatatype			The allowed list types.
	 * @throws Exception						When an error occurs.
	 */
	public void addDescriptionParameterIn(String sIdentifier,
											String sTitle,
											String sDescription,
											int nMinOccurs,
											int nMaxOccurs,
											long lMaxMegabyte,
											DatatypeComplex eType,
											DatatypeAttribute[] arrAllowedListDatatype) throws Exception {
		_lstParameterIn.add(new WPSOperatorDescriptionParameterComplex(sIdentifier, sTitle, sDescription, nMinOccurs, nMaxOccurs, lMaxMegabyte, eType, arrAllowedListDatatype));
	}

	/**
	 * Adds an input description parameter of type list.
	 * @param sIdentifier						The parameter identifier.
	 * @param sTitle							The parameter title (visible parameter name).
	 * @param sDescription						The parameter description.
	 * @param nMinOccurs						The minimal occurance.
	 * @param nMaxOccurs						The maximal occurance.
	 * @param lMaxMegabyte						The maximal size of parameter data.
	 * @param eType								The data type.
	 * @param allowedListDatatype				The allowed list type.
	 * @param arrAllowedSchemaDescriptions		The allowed schema descriptions.
	 * @throws Exception						When an error occurs.
	 */
	public void addDescriptionParameterIn(String sIdentifier,
											String sTitle,
											String sDescription,
											int nMinOccurs,
											int nMaxOccurs,
											long lMaxMegabyte,
											DatatypeComplex eType,
											DatatypeAttributeExt allowedListDatatype,
											WPSFeatureSchemaDescription[] arrAllowedSchemaDescriptions) throws Exception {
		_lstParameterIn.add(new WPSOperatorDescriptionParameterComplex(sIdentifier, sTitle, sDescription, nMinOccurs, nMaxOccurs, lMaxMegabyte, eType, allowedListDatatype, arrAllowedSchemaDescriptions));
	}

	/**
	 * Adds an output description parameter.
	 * @param sIdentifier						The parameter identifier.
	 * @param sTitle							The parameter title (visible parameter name).
	 * @param sDescription						The parameter description.
	 * @param sUnitOfMeasure					The parameter unit.
	 * @param eType								The data type.
	 * @throws Exception						When an error occurs.
	 */
	public void addDescriptionParameterOut(String sIdentifier,
											String sTitle,
											String sDescription,
											String sUnitOfMeasure,
											DatatypeLiteral eType) throws Exception {
		_lstParameterOut.add(new WPSOperatorDescriptionParameterLiteral(sIdentifier, sTitle, sDescription, sUnitOfMeasure, 1, 1, eType, null, null, null));
	}

	/**
	 * Adds an output description parameter.
	 * @param sIdentifier						The parameter identifier.
	 * @param sTitle							The parameter title (visible parameter name).
	 * @param sDescription						The parameter description.
	 * @param eType								The data type.
	 */
	public void addDescriptionParameterOut(String sIdentifier,
											String sTitle,
											String sDescription,
											DatatypeComplex eType) {
		_lstParameterOut.add(new WPSOperatorDescriptionParameterComplex(sIdentifier, sTitle, sDescription, 1, 1, Long.MAX_VALUE, eType));
	}

	/**
	 * Adds an output description parameter.
	 * @param sIdentifier						The parameter identifier.
	 * @param sTitle							The parameter title (visible parameter name).
	 * @param sDescription						The parameter description.
	 * @param eType								The data type.
	 * @param arrAllowedSchemaDescriptions		The allowed schema descriptions.
	 * @throws Exception						When an error occurs.
	 */
	public void addDescriptionParameterOut(String sIdentifier,
											String sTitle,
											String sDescription,
											DatatypeComplex eType,
											WPSFeatureSchemaDescription[] arrAllowedSchemaDescriptions) throws Exception {
		_lstParameterOut.add(new WPSOperatorDescriptionParameterComplex(sIdentifier, sTitle, sDescription, 1, 1, Long.MAX_VALUE, eType, arrAllowedSchemaDescriptions));
	}

	/**
	 * Adds an output description parameter of type list.
	 * @param sIdentifier						The parameter identifier.
	 * @param sTitle							The parameter title (visible parameter name).
	 * @param sDescription						The parameter description.
	 * @param eType								The data type.
	 * @param arrAllowedListDatatype			The allowed list datatype.
	 * @throws Exception						When an error occurs.
	 */
	public void addDescriptionParameterOut(String sIdentifier,
											String sTitle,
											String sDescription,
											DatatypeComplex eType,
											DatatypeAttribute[] arrAllowedListDatatype) throws Exception {
		_lstParameterOut.add(new WPSOperatorDescriptionParameterComplex(sIdentifier, sTitle, sDescription, 1, 1, Long.MAX_VALUE, eType, arrAllowedListDatatype));
	}

	/**
	 * Adds an output description parameter of type list.
	 * @param sIdentifier						The parameter identifier.
	 * @param sTitle							The parameter title (visible parameter name).
	 * @param sDescription						The parameter description.
	 * @param eType								The data type.
	 * @param allowedListDatatype				The allowed list type.
	 * @param arrAllowedSchemaDescriptions		The allowed schema descriptions.
	 * @throws Exception						When an error occurs.
	 */
	public void addDescriptionParameterOut(String sIdentifier,
											String sTitle,
											String sDescription,
											DatatypeComplex eType,
											DatatypeAttributeExt allowedListDatatype,
											WPSFeatureSchemaDescription[] arrAllowedSchemaDescriptions) throws Exception {
		_lstParameterOut.add(new WPSOperatorDescriptionParameterComplex(sIdentifier, sTitle, sDescription, 1, 1, Long.MAX_VALUE, eType, allowedListDatatype, arrAllowedSchemaDescriptions));
	}

	/**
	 * Checks whether status information is supported.
	 * @return boolean							True whether status information is supported.
	 */
	public boolean isStatusSupporded() {
		return _bSupportStatus;
	}
	
	/**
	 * Checks whether storing results on server is supported.
	 * @return boolean							True whether storing results on server is supported.
	 */
	public boolean isStoreSupporded() {
		return _bSupportStore;
	}

	/**
	 * Gets the timeout in milliseconds.
	 * @return long								True timeout in milliseconds.
	 */
	public long getTimeoutMillis() {
		if (_lTimeoutSeconds != Long.MAX_VALUE)
			return _lTimeoutSeconds*1000L;
		else
			return _lTimeoutSeconds;
	}

	/**
	 * Gets the parameters.
	 * @return List<WPSOperatorDescriptionParameter>	The parameters.
	 * @param bInput									True when the input parameters are requested, false for the output parameters.
	 */
	public List<WPSOperatorDescriptionParameter> getParameters(boolean bInput) {
		if (bInput)
			return _lstParameterIn;
		else
			return _lstParameterOut;
	}
	
	/**
	 * Gets a parameter by name.
	 * @return WPSOperatorDescriptionParameter			The found parameter.
	 * @param sIdentifier								The parameter identifier.
	 * @param bInput									True when the parameter to look for is an input parameter.
	 * @throws Exception								When the parameter doesn't exists.
	 */
	public WPSOperatorDescriptionParameter getParameter(String sIdentifier, boolean bInput) throws Exception {
		List<WPSOperatorDescriptionParameter> lstParam = _lstParameterOut;
		if (bInput)
			lstParam = _lstParameterIn;
		Iterator<WPSOperatorDescriptionParameter> iterParam = lstParam.iterator();
		WPSOperatorDescriptionParameter param;
		while (iterParam.hasNext()) {
			param = iterParam.next();
			if (param.getIdentifier().equals(sIdentifier))
				return param;
		}
		throw new Exception(this.getClass().getSimpleName()+".getParameter(): Parameter '"+sIdentifier+"' doesn't exists.");
	}

	/**
	 * Converts a string value to an object.
	 * @return Object									The converted object.
	 * @param sValue									The value to convert.
	 * @param eType										The data type.
	 * @throws Exception								When an error occurs.
	 */
	public static Object convertToObject(String sValue, DatatypeLiteral eType) throws Exception {
		Object objValue;
		if (eType == DatatypeLiteral.Integer) {
			objValue = new Integer(sValue);
		} else if (eType == DatatypeLiteral.Long) {
			objValue = new Long(sValue);
		} else if (eType == DatatypeLiteral.Double) {
			objValue = new Double(sValue);				
		} else if (eType == DatatypeLiteral.Boolean) {
			objValue = new Boolean(sValue);				
		} else if (eType == DatatypeLiteral.Date) {
			try {
				objValue = new Date(Long.parseLong(sValue));				
			} catch (Exception ex) {
				objValue = new SimpleDateFormat().parse(sValue);				
			}
		} else if (eType == DatatypeLiteral.String) {
			objValue = sValue;				
		} else {
			throw new Exception("Unsupported literal type '"+eType.name()+"'.");
		}
		return objValue;
	}

	/**
	 * Converts an object value to a string.
	 * @return String									The converted object.
	 * @param objValue									The value to convert.
	 * @param eType										The data type.
	 * @param bDateAsLong								Converts the date value to a long value.
	 */
	public static String convertToString(Object objValue, DatatypeLiteral eType, boolean bDateAsLong) {
		if (eType == DatatypeLiteral.Date) {
			if (bDateAsLong)
				return ""+((Date)objValue).getTime();		
			else
				return new SimpleDateFormat().format((Date)objValue);		
		} else {
			return objValue.toString();
		}
	}

	public class Range {
		protected Object	_minValue;
		protected Object	_maxValue;
		
		public Range(Object minValue, Object maxValue) {
			_minValue = minValue;
			_maxValue = maxValue;
			if (_minValue.equals(Double.NEGATIVE_INFINITY))
				_minValue = Double.MIN_VALUE;
			if (_maxValue.equals(Double.POSITIVE_INFINITY))
				_maxValue = Double.MAX_VALUE;
		}
		
		public Object getMinValue() {
			return _minValue;
		}

		public Object getMaxValue() {
			return _maxValue;
		}
		
	}
}

