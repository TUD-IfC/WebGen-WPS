package ica.wps.server.request.execute;

import ica.wps.server.ExceptionCode;
import ica.wps.server.ServerException;

import org.apache.xmlbeans.XmlObject;

import net.opengis.wps.x100.ExecuteDocument;



/**
 * Request class handles the GetCapabilities POST request.
 * @author	M. Wittensoeldner
 * @date	Created on 01.02.2007
 */
public class RequestExecutePost extends RequestExecute {

	/**
	 * Parses an validates the servlet request.
	 * @return void
	 * @param document					The document containing the request input.
	 * @throws ServerException			When an error occurs.
	 */
	protected void parseRequest(XmlObject document) throws ServerException {
		try {
			_document = (ExecuteDocument)document;
			_sAcceptVersions = _document.getExecute().getVersion();
			if (_document.getExecute().isSetLanguage())
				_sLanguage = _document.getExecute().getLanguage();
		} catch (Exception ex) {
			throw new ServerException(ExceptionCode.InvalidParameterValue, "xml", "The xml document is not a valid Execute document.");
		}
	}
}
