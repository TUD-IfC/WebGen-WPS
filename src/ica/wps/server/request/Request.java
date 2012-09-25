package ica.wps.server.request;

import java.io.File;
import java.util.Iterator;

import ica.wps.server.IWPSOperator;
import ica.wps.server.ServerException;
import ica.wps.server.WPSServlet;
import ica.wps.WPSConstants;

import net.opengis.ows.x11.MetadataType;
import net.opengis.wps.x100.ProcessBriefType;

import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Node;


/**
 * Basic request class for WPS request handling.
 * @author	M. Wittensoeldner
 * @date	Created on 01.02.2007
 */
public abstract class Request {
	protected static final String		PARAM_ACCEPTVERSIONS = "AcceptVersions";
	protected static final String		PARAM_VERSION = "version";
	protected static final String		PARAM_LANGUAGE = "language";
	public static final String			PROP_DATADIR = "data.directory";
	protected static final String		WPS_LANGUAGE = "en-GB";
	public static final String			RAW_OUTPUT_NAME = "RawOu-tputLi3ter_alVa-lue";

	
	protected String					_sAcceptVersions = null;
	protected String					_sLanguage = null;
	
	/**
	 * Parses an validates the servlet request.
	 * @return void
	 * @param document					The document containing the request input.
	 * @throws ServerException			When an error occurs.
	 */
	protected abstract void parseRequest(XmlObject document) throws ServerException;

	/**
	 * Handles the request. This method is called after parseRequest().
	 * @return XmlObject				The resulted xml document.
	 * @throws ServerException			When an error occurs.
	 */
	protected abstract XmlObject handleRequest() throws ServerException;

	/**
	 * Handles the request.
	 * @return XmlObject				The resulted xml document.
	 * @param document					The document containing the request input.
	 * @throws ServerException			When an error occurs.
	 */
	public XmlObject handleRequest(XmlObject document) throws ServerException {
		this.parseRequest(document);
		return this.handleRequest();
	}

	/**
	 * Sets the process information.
	 * @return void
	 * @param process					The process information to set.
	 * @param operator					The operator.
	 */
	protected void setProcessInformation(ProcessBriefType process, IWPSOperator operator) {
		process.setProcessVersion(operator.getOperatorDescription().getVersion());
		process.addNewIdentifier().setStringValue(operator.getOperatorDescription().getIdentifier());
		process.addNewTitle().setStringValue(operator.getOperatorDescription().getTitle());
		process.addNewAbstract().setStringValue(operator.getOperatorDescription().getDescription());
		if ((operator.getOperatorDescription().getClassification() != null) && (operator.getOperatorDescription().getClassification().size() > 0)) {
			// add classification metadata
			Iterator<String> iterClass = operator.getOperatorDescription().getClassification().iterator();
			while (iterClass.hasNext()) {
				MetadataType meta = process.addNewMetadata();
				meta.setTitle(WPSConstants.WPS_CLASSIFICATION+":"+iterClass.next());
			}
		}
	}

	/**
	 * Helper class to find a case insensitive attribute in an xml node.
	 * @return String					The found attribute value. Null when not found.
	 * @param node						The node.
	 * @param sAttributeName			The attribute name.
	 */
	public static String getAttribute(Node node, String sAttributeName) {
		Node element;
		int i = 0;
		while ((node.getAttributes() != null) && (i < node.getAttributes().getLength())) {
			element = node.getAttributes().item(i);
			if (element.getLocalName().equalsIgnoreCase(sAttributeName))
				return element.getNodeValue();
			i++;
		}
		return null;
	}
	
	/**
	 * Creates a file on the server.
	 * @return File						The file.
	 * @param sExt						The file extension.
	 * @param arrUrl					Array to store the URL of the file.
	 * @throws Exception				When an error occurs.
	 */
	public static File createDataFile(String sExt, String[] arrUrl) throws Exception {
		File outFile = File.createTempFile("wps", sExt, new File(WPSServlet.getHomeDirectory()+WPSServlet.getProperties().getProperty(PROP_DATADIR)));
		String sServerUrl = WPSServlet.getCapabilities().getServerUrl();
		sServerUrl = sServerUrl.substring(0, sServerUrl.lastIndexOf('/'));
		sServerUrl += WPSServlet.getProperties().getProperty(PROP_DATADIR);
		if (!sServerUrl.endsWith("/"))
			sServerUrl += "/";
		sServerUrl += outFile.getName();
		if ((arrUrl != null) && (arrUrl.length > 0))
			arrUrl[0] = sServerUrl;
		return outFile;
	}

}
