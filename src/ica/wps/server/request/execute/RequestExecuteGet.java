package ica.wps.server.request.execute;

import ica.wps.server.ExceptionCode;
import ica.wps.server.ServerException;

import org.apache.xmlbeans.XmlObject;


/**
 * Request class handles the GetCapabilities GET request.
 * @author	M. Wittensoeldner
 * @date	Created on 01.02.2007
 */
public class RequestExecuteGet extends RequestExecute {

	/**
	 * Parses an validates the servlet request.
	 * @return void
	 * @param document					The document containing the request input.
	 * @throws ServerException			When an error occurs.
	 */
	protected void parseRequest(XmlObject document) throws ServerException {
		throw new ServerException(ExceptionCode.NoApplicableCode, "", "GET request for the Execute command is not supported.");
	}
}
