package ica.wps.server.request.describeprocess;

import ica.wps.server.ServerException;
import ica.wps.server.request.Request;

import java.util.LinkedList;

import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Node;


/**
 * Request class handles the GetCapabilities GET request.
 * @author	M. Wittensoeldner
 * @date	Created on 01.02.2007
 */
public class RequestDescribeProcessGet extends RequestDescribeProcess {

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
		String sIdentifier = Request.getAttribute(node, PARAM_IDENTIFIER);
		if (sIdentifier != null) {
			_lstIdentifiers = new LinkedList<String>();
			String[] arrIds = sIdentifier.split(",");
			int i = 0;
			while (i < arrIds.length) {
				if (arrIds[i].trim().length() > 0)
					_lstIdentifiers.add(arrIds[i].trim());
				i++;
			}
		}
	}
}
