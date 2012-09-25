package ica.wps.server;

import ica.wps.data.WPSServerDescription;
import ica.wps.common.IWPSXmlObjectParser;

import java.util.List;

/**
 * Interface defines a WPS server. The interface supports methods to get metadata about the WPS server.
 * @author	M. Wittensoeldner
 * @date	Created on 05.02.2007
 */
public interface IWPSServer {

	/**
	 * Initializes the server.
	 * @return void
	 * @param classLoader						The class loader.
	 */
	public void init(ClassLoader classLoader);

	/**
	 * Gets the server description
	 * @return WPSServerDescription				The server description.
	 */
	public WPSServerDescription getServerDescription();

	/**
	 * Gets the WPS operators.
	 * @return List<IWPSOperator>				The WPS operators.
	 */
	public List<IWPSOperator> getOperators();

	/**
	 * Gets the xml parser.
	 * @return IWPSXmlObjectParser				The xml parser.
	 */
	public IWPSXmlObjectParser getXmlParser();
}

