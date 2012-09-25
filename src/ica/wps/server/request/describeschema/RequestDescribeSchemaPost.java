package ica.wps.server.request.describeschema;

import ica.wps.server.ExceptionCode;
import ica.wps.server.ServerException;

import org.apache.xmlbeans.XmlObject;



/**
 * Request class handles the DescribeSchema POST request.
 * @author	M. Wittensoeldner
 * @date	Created on 07.02.2007
 */
public class RequestDescribeSchemaPost extends RequestDescribeSchema {

	/**
	 * Parses an validates the servlet request.
	 * @return void
	 * @param document					The document containing the request input.
	 * @throws ServerException			When an error occurs.
	 */
	protected void parseRequest(XmlObject document) throws ServerException {
		throw new ServerException(ExceptionCode.NoApplicableCode, "", "POST request for the DescribeSchema command is not supported.");
	}
}
