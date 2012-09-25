package ica.wps.data;

import ica.wps.data.WPSFeatureSchemaDescription.DatatypeAttribute;

/**
 * Class represents the definition of a single feature attribute.
 * @author	M. Wittensoeldner
 * @date	Created on 15.02.2007
 */
public class WPSFeatureCollectionAttribute extends WPSSchemaAttribute {

	/**
	 * Constructor.
	 * @param sIdentifier						The attribute identifier.
	 * @param eType								The attribute type.
	 */
	public WPSFeatureCollectionAttribute(String sIdentifier, DatatypeAttribute eType) {
		super(sIdentifier, eType);
	}
}

