package ica.wps.data;

import java.util.List;
import java.util.LinkedList;

/**
 * Container class holds a small operator description.
 * @author	M. Wittensoeldner
 * @date	Created on 04.02.2007
 */
public class WPSOperatorDescriptionSmall {
	protected String									_sVersion;
	protected String									_sIdentifier;
	protected String									_sTitle;
	protected String									_sDescription;
	protected List<String>								_lstClassification;

	/**
	 * Constructor.
	 * @param sVersion							The operator version.
	 * @param sIdentifier						The operator identifier.
	 * @param sTitle							The operator title (visible operator name).
	 * @param sDescription						The operator description.
	 */
	public WPSOperatorDescriptionSmall(String sVersion, String sIdentifier, String sTitle, String sDescription) {
		_sVersion = sVersion;
		_sIdentifier = sIdentifier;
		_sTitle = sTitle;
		_sDescription = sDescription;
		_lstClassification = new LinkedList<String>();
	}

	/**
	 * Adds a classification.
	 * @return void
	 * @param sClassification					The classification to add.
	 */
	public void addClassification(String sClassification) {
		if ((sClassification != null) && (sClassification.trim().length() > 0))
			_lstClassification.add(sClassification.trim());
	}
	
	/**
	 * Gets the version.
	 * @return String							The operator version.
	 */
	public String getVersion() {
		return _sVersion;
	}

	/**
	 * Gets the identifier.
	 * @return String							The operator identifier.
	 */
	public String getIdentifier() {
		return _sIdentifier;
	}

	/**
	 * Gets the title.
	 * @return String							The operator title.
	 */
	public String getTitle() {
		return _sTitle;
	}

	/**
	 * Gets the description.
	 * @return String							The operator description.
	 */
	public String getDescription() {
		return _sDescription;
	}

	/**
	 * Gets the classification.
	 * @return List<String>						The operator classification.
	 */
	public List<String> getClassification() {
		return _lstClassification;
	}
}

