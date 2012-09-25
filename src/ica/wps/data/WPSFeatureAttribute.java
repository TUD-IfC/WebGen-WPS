package ica.wps.data;

import ica.wps.data.WPSFeatureSchemaDescription.DatatypeAttribute;

/**
 * Class represents the definition of a single feature attribute.
 * @author	M. Wittensoeldner
 * @date	Created on 04.02.2007
 */
public class WPSFeatureAttribute extends WPSSchemaAttribute {

	/**
	 * Constructor.
	 * @param sIdentifier						The attribute identifier.
	 * @param eType								The attribute type.
	 */
	public WPSFeatureAttribute(String sIdentifier, DatatypeAttribute eType) {
		super(sIdentifier, eType);
	}
}

