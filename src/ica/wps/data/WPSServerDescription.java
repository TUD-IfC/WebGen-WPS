package ica.wps.data;

import java.util.List;
import java.util.LinkedList;

/**
 * Container class holds the server description.
 * @author	M. Wittensoeldner
 * @date	Created on 15.02.2007
 */
public class WPSServerDescription {
	protected String		_sTitle;
	protected String		_sDescription;
	protected List<String>	_lstKeywords = new LinkedList<String>();
	protected String		_sProviderName;
	protected String		_sProviderUrl;
	protected String		_sProviderEmail;
	protected String		_sProviderContactName;

	/**
	 * Constructor.
	 * @param sTitle							The server title.
	 * @param sDescription						The server description.
	 * @param sProviderName						The server provider name. e.g. 'Agriculture and Agri-Food Canada'
	 * @param sProviderUrl						The server provider url. 'http://gis.agr.gc.ca/'
	 * @param sProviderEmail					The server provider email. e.g. 'schutp@agr.gc.ca'
	 * @param sProviderContactName				The server provider contact name. e.g. 'Peter Schut'
	 * @param arrKeywords						The server keywords.
	 */
	public WPSServerDescription(String sTitle, String sDescription, String sProviderName, String sProviderUrl, String sProviderEmail, String sProviderContactName, String[] arrKeywords) {
		_sTitle = sTitle;
		_sDescription = sDescription;
		_sProviderName = sProviderName;
		_sProviderUrl = sProviderUrl;
		_sProviderEmail = sProviderEmail;
		_sProviderContactName = sProviderContactName;
		int i = 0;
		while ((arrKeywords != null) && (i < arrKeywords.length))
			_lstKeywords.add(arrKeywords[i++]);
		
		
	}

	/**
	 * Gets the server title.
	 * @return String							The server title.
	 */
	public String getTitle() {
		return _sTitle;
	}
	
	/**
	 * Gets the server description.
	 * @return String							The server description.
	 */
	public String getDescription() {
		return _sDescription;
	}

	/**
	 * Gets the server keywords.
	 * @return List<String>						The server keywords. Can be null.
	 */
	public List<String> getKeywords() {
		return _lstKeywords;
	}

	/**
	 * Gets the service provider name. e.g. 'Agriculture and Agri-Food Canada'
	 * @return String							The service provider name.
	 */
	public String getServiceProviderName() {
		return _sProviderName;
	}

	/**
	 * Gets the URL of the service provider. e.g. 'http://gis.agr.gc.ca/'
	 * @return String							The service provider URL.
	 */
	public String getServiceProviderURL() {
		return _sProviderUrl;
	}

	/**
	 * Gets the email address of the service provider. e.g. 'schutp@agr.gc.ca'
	 * @return String							The service provider email address.
	 */
	public String getServiceProviderEmail() {
		return _sProviderEmail;
	}

	/**
	 * Gets the name of the contact person. e.g. 'Peter Schut'
	 * @return String							The name of the contact person.
	 */
	public String getServiceProviderContactName() {
		return _sProviderContactName;
	}
}

