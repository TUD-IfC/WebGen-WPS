package ica.wps.server.request.describeschema;

import ica.wps.server.ServerException;
import ica.wps.server.request.Request;

import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Node;


/**
 * Request class handles the DescribeSchema GET request.
 * @author	M. Wittensoeldner
 * @date	Created on 07.02.2007
 */
public class RequestDescribeSchemaGet extends RequestDescribeSchema {

	/**
	 * Parses an validates the servlet request.
	 * @return void
	 * @param document					The document containing the request input.
	 * @throws ServerException			When an error occurs.
	 */
	protected void parseRequest(XmlObject document) throws ServerException {
		Node node = document.getDomNode().getFirstChild();
		_sAcceptVersions = Request.getAttribute(node, PARAM_VERSION);
		_sLanguage = Request.getAttribute(node, PARAM_LANGUAGE);
		_sOperatorIdentifier = Request.getAttribute(node, PARAM_OPERATOR);	
		_sParameterIdentifier = Request.getAttribute(node, PARAM_PARAMETER);
		_bInput = true;
		String sInput = Request.getAttribute(node, PARAM_INPUT);
		if (sInput != null);
			try {
				_bInput = Boolean.parseBoolean(sInput);
			} catch (Exception ex) {				
			}

	}
}
