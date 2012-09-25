package ica.wps.server.request.getcapabilities;

import ica.wps.server.ExceptionCode;
import ica.wps.server.ServerException;

import org.apache.xmlbeans.XmlObject;
import net.opengis.wps.x100.GetCapabilitiesDocument;



/**
 * Request class handles the GetCapabilities POST request.
 * @author	M. Wittensoeldner
 * @date	Created on 01.02.2007
 */
public class RequestGetCapabilitiesPost extends RequestGetCapabilities {

	/**
	 * Parses an validates the servlet request.
	 * @return void
	 * @param document					The document containing the request input.
	 * @throws ServerException			When an error occurs.
	 */
	protected void parseRequest(XmlObject document) throws ServerException {
		try {
			GetCapabilitiesDocument doc = (GetCapabilitiesDocument)document;
			if (doc.getGetCapabilities().isSetAcceptVersions())
				_sAcceptVersions = doc.getGetCapabilities().getAcceptVersions().getVersionList().get(0);
			if (doc.getGetCapabilities().isSetLanguage())
				_sLanguage = doc.getGetCapabilities().getLanguage();
		} catch (Exception ex) {
			throw new ServerException(ExceptionCode.InvalidParameterValue, "xml", "The xml document is not a valid GetCapabilities document.");
		}
	}
}
