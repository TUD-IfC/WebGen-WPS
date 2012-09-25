package ica.wps.data;

import java.util.List;
import java.util.LinkedList;

import ica.wps.data.WPSOperatorDescription.DatatypeComplex;
import ica.wps.data.WPSFeatureSchemaDescription.DatatypeAttribute;
import ica.wps.data.WPSFeatureSchemaDescription.DatatypeAttributeExt;


/**
 * Class defines a single complex parameter.
 * @author	M. Wittensoeldner
 * @date	Created on 04.02.2007
 */
public class WPSOperatorDescriptionParameterComplex extends WPSOperatorDescriptionParameter {

	protected DatatypeComplex						_eType;
	protected List<WPSFeatureSchemaDescription>		_lstAllowedSchemaDescription = new LinkedList<WPSFeatureSchemaDescription>();
	protected List<DatatypeAttribute>				_lstAllowedListDatatype = new LinkedList<DatatypeAttribute>();
	protected DatatypeAttributeExt					_allowedListDatatypeExt = null;
	protected long									_lMaxMegabyte;
	
	/**
	 * Constructor.
	 * @param sIdentifier						The parameter identifier.
	 * @param sTitle							The parameter title (visible parameter name).
	 * @param sDescription						The parameter description.
	 * @param nMinOccurs						The minimal occurance.
	 * @param nMaxOccurs						The maximal occurance.
	 * @param lMaxMegabyte						The maximal size of parameter data.
	 * @param eType								The data type.
	 */
	public WPSOperatorDescriptionParameterComplex(String sIdentifier,
													String sTitle,
													String sDescription,
													int nMinOccurs,
													int nMaxOccurs,
													long lMaxMegabyte,
													DatatypeComplex eType) {
		super(sIdentifier, sTitle, sDescription, null, nMinOccurs, nMaxOccurs);
		_eType = eType;
		_lMaxMegabyte = lMaxMegabyte;
	}

	/**
	 * Constructor.
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
	public WPSOperatorDescriptionParameterComplex(String sIdentifier,
													String sTitle,
													String sDescription,
													int nMinOccurs,
													int nMaxOccurs,
													long lMaxMegabyte,
													DatatypeComplex eType,
													WPSFeatureSchemaDescription[] arrAllowedSchemaDescriptions) throws Exception {
		this(sIdentifier, sTitle, sDescription, nMinOccurs, nMaxOccurs, lMaxMegabyte, eType);
		if ((eType == DatatypeComplex.Feature) || (eType == DatatypeComplex.FeatureCollection) || (eType == DatatypeComplex.Table)) {
			if ((arrAllowedSchemaDescriptions != null) && (arrAllowedSchemaDescriptions.length > 0)) {
				int i = 0;
				while (i < arrAllowedSchemaDescriptions.length) {
					_lstAllowedSchemaDescription.add(arrAllowedSchemaDescriptions[i++]);
				}
			}
		} else {
			throw new Exception("Schema descriptions ar allowed for "+DatatypeComplex.Feature+" and "+DatatypeComplex.FeatureCollection.name()+" and "+DatatypeComplex.List.name()+" only.");
		}
	}

	/**
	 * Constructor for a list.
	 * @param sIdentifier						The parameter identifier.
	 * @param sTitle							The parameter title (visible parameter name).
	 * @param sDescription						The parameter description.
	 * @param nMinOccurs						The minimal occurance.
	 * @param nMaxOccurs						The maximal occurance.
	 * @param lMaxMegabyte						The maximal size of parameter data.
	 * @param eType								The data type.
	 * @param arrAllowedListDatatype			The allowed list datatype.
	 * @throws Exception						When an error occurs.
	 */
	public WPSOperatorDescriptionParameterComplex(String sIdentifier,
													String sTitle,
													String sDescription,
													int nMinOccurs,
													int nMaxOccurs,
													long lMaxMegabyte,
													DatatypeComplex eType,
													DatatypeAttribute[] arrAllowedListDatatype) throws Exception {
		this(sIdentifier, sTitle, sDescription, nMinOccurs, nMaxOccurs, lMaxMegabyte, eType);
		if (eType == DatatypeComplex.List) {
			if ((arrAllowedListDatatype != null) && (arrAllowedListDatatype.length > 0)) {
				int i = 0;
				while (i < arrAllowedListDatatype.length) {
					_lstAllowedListDatatype.add(arrAllowedListDatatype[i++]);
				}
			}
		} else {
			throw new Exception("List datatypes ar allowed for "+DatatypeComplex.List+" only.");
		}
	}

	/**
	 * Constructor for a list.
	 * @param sIdentifier						The parameter identifier.
	 * @param sTitle							The parameter title (visible parameter name).
	 * @param sDescription						The parameter description.
	 * @param nMinOccurs						The minimal occurance.
	 * @param nMaxOccurs						The maximal occurance.
	 * @param lMaxMegabyte						The maximal size of parameter data.
	 * @param eType								The data type.
	 * @param allowedListDatatype				The allowed list datatype.
	 * @param arrAllowedSchemaDescriptions		The allowed schema descriptions.
	 * @throws Exception						When an error occurs.
	 */
	public WPSOperatorDescriptionParameterComplex(String sIdentifier,
													String sTitle,
													String sDescription,
													int nMinOccurs,
													int nMaxOccurs,
													long lMaxMegabyte,
													DatatypeComplex eType,
													DatatypeAttributeExt allowedListDatatype,
													WPSFeatureSchemaDescription[] arrAllowedSchemaDescriptions) throws Exception {
		this(sIdentifier, sTitle, sDescription, nMinOccurs, nMaxOccurs, lMaxMegabyte, eType);
		if (eType == DatatypeComplex.List) {
			_allowedListDatatypeExt = allowedListDatatype;
			if ((arrAllowedSchemaDescriptions != null) && (arrAllowedSchemaDescriptions.length > 0)) {
				int i = 0;
				while (i < arrAllowedSchemaDescriptions.length) {
					_lstAllowedSchemaDescription.add(arrAllowedSchemaDescriptions[i++]);
				}
			}
		} else {
			throw new Exception("List datatype ar allowed for "+DatatypeComplex.List+" only.");
		}
	}

	/**
	 * Gets the datatype.
	 * @return DatatypeComplex					The parameter data type.
	 */
	public DatatypeComplex getType() {
		return _eType;
	}

	/**
	 * Gets max megabyte.
	 * @return long								The maximal size of parameter data.
	 */
	public long getMaxMegabyte() {
		return _lMaxMegabyte;
	}

	/**
	 * Gets the allowed feature schema descriptions.
	 * @return List<WPSFeatureSchemaDescription>The feature schema descriptions.
	 */
	public List<WPSFeatureSchemaDescription> getAllowedSchemaDescriptions() {
		return _lstAllowedSchemaDescription;
	}

	/**
	 * Gets the allowed list datatypes.
	 * @return List<DatatypeAttribute>			The list datatypes.
	 */
	public List<DatatypeAttribute> getAllowedListDatatypes() {
		return _lstAllowedListDatatype;
	}

	/**
	 * Gets the allowed list datatype.
	 * @return DatatypeAttributeExt				The list datatype.
	 */
	public DatatypeAttributeExt getAllowedListDatatypeExt() {
		return _allowedListDatatypeExt;
	}
}

