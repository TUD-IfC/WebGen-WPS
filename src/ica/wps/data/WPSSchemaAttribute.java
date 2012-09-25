package ica.wps.data;

import ica.wps.data.WPSFeatureSchemaDescription.DatatypeAttribute;

/**
 * Class represents the definition of a single schema attribute.
 * @author	M. Wittensoeldner
 * @date	Created on 15.02.2007
 */
public abstract class WPSSchemaAttribute {

	protected String					_sIdentifier;
	protected DatatypeAttribute			_eType = null;
	
	
	/**
	 * Constructor.
	 * @param sIdentifier						The attribute identifier.
	 * @param eType								The attribute type.
	 */
	protected WPSSchemaAttribute(String sIdentifier, DatatypeAttribute eType) {
		_sIdentifier = sIdentifier;
		_eType = eType;
	}

	/**
	 * Gets the identifier.
	 * @return String							The attribute identifier.
	 */
	public String getIdentifier() {
		return _sIdentifier;
	}

	/**
	 * Gets the datatype.
	 * @return DatatypeAttribute				The attribute type.
	 */
	public DatatypeAttribute getType() {
		return _eType;
	}
}

