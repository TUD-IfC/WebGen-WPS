package ica.wps.server.request.getcapabilities;

import ica.wps.server.ServerException;
import ica.wps.server.request.Request;

import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Node;


/**
 * Request class handles the GetCapabilities GET request.
 * @author	M. Wittensoeldner
 * @date	Created on 01.02.2007
 */
public class RequestGetCapabilitiesGet extends RequestGetCapabilities {

	/**
	 * Parses an validates the servlet request.
	 * @return void
	 * @param document					The document containing the request input.
	 * @throws ServerException			When an error occurs.
	 */
	protected void parseRequest(XmlObject document) throws ServerException {
		Node node = document.getDomNode().getFirstChild();
		_sAcceptVersions = Request.getAttribute(node, PARAM_ACCEPTVERSIONS);
		_sLanguage = Request.getAttribute(node, PARAM_LANGUAGE);
	}
}
