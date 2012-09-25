package ica.wps.data;

/**
 * Class defines a single parameter.
 * @author	M. Wittensoeldner
 * @date	Created on 04.02.2007
 */
public abstract class WPSOperatorDescriptionParameter {

	protected String				_sIdentifier;
	protected String				_sTitle;
	protected String				_sDescription;
	protected String				_sUnitOfMeasure;
	protected int					_nMinOccurs;
	protected int					_nMaxOccurs;
		
	/**
	 * Constructor.
	 * @param sIdentifier						The parameter identifier.
	 * @param sTitle							The parameter title (visible parameter name).
	 * @param sDescription						The parameter description.
	 * @param sUnitOfMeasure					The parameter unit. Can be null.
	 * @param nMinOccurs						The minimal occurance.
	 * @param nMaxOccurs						The maximal occurance.
	 */
	protected WPSOperatorDescriptionParameter(String sIdentifier, String sTitle, String sDescription, String sUnitOfMeasure, int nMinOccurs, int nMaxOccurs) {
		_sIdentifier = sIdentifier;
		_sTitle = sTitle;
		_sDescription = sDescription;
		_sUnitOfMeasure = sUnitOfMeasure;
		_nMinOccurs = nMinOccurs;
		_nMaxOccurs = nMaxOccurs;
	}
	
	/**
	 * Gets the identifier.
	 * @return String							The parameter identifier.
	 */
	public String getIdentifier() {
		return _sIdentifier;
	}

	/**
	 * Gets the title.
	 * @return String							The parameter title.
	 */
	public String getTitle() {
		return _sTitle;
	}

	/**
	 * Gets the description.
	 * @return String							The parameter description.
	 */
	public String getDescription() {
		return _sDescription;
	}

	/**
	 * Gets the unit of measure. Can be null.
	 * @return String							The unit of measure.
	 */
	public String getUnitOfMeasure() {
		return _sUnitOfMeasure;
	}

	/**
	 * Gets the minimal occurance.
	 * @return int								The minimal occurance.
	 */
	public int getMinOccurs() {
		return _nMinOccurs;
	}

	/**
	 * Gets the maximal occurance.
	 * @return int								The maximal occurance.
	 */
	public int getMaxOccurs() {
		return _nMaxOccurs;
	}
}

