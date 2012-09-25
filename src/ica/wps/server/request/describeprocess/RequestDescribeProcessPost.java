package ica.wps.server.request.describeprocess;

import ica.wps.server.ExceptionCode;
import ica.wps.server.ServerException;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import org.apache.xmlbeans.XmlObject;
import net.opengis.wps.x100.DescribeProcessDocument;
import net.opengis.ows.x11.CodeType;



/**
 * Request class handles the GetCapabilities POST request.
 * @author	M. Wittensoeldner
 * @date	Created on 01.02.2007
 */
public class RequestDescribeProcessPost extends RequestDescribeProcess {

	/**
	 * Parses an validates the servlet request.
	 * @return void
	 * @param document					The document containing the request input.
	 * @throws ServerException			When an error occurs.
	 */
	protected void parseRequest(XmlObject document) throws ServerException {
		try {
			DescribeProcessDocument doc = (DescribeProcessDocument)document;
			_sAcceptVersions = doc.getDescribeProcess().getVersion();
			if (doc.getDescribeProcess().isSetLanguage())
				_sLanguage = doc.getDescribeProcess().getLanguage();
			_lstIdentifiers = new LinkedList<String>();
			List<CodeType> lstId = doc.getDescribeProcess().getIdentifierList();
			Iterator<CodeType> iterId = lstId.iterator();
			while (iterId.hasNext()) {
				_lstIdentifiers.add(iterId.next().getStringValue().trim());
				if (_lstIdentifiers.getLast().length() == 0)
					_lstIdentifiers.removeLast();
			}
		} catch (Exception ex) {
			throw new ServerException(ExceptionCode.InvalidParameterValue, "xml", "The xml document is not a valid GetCapabilities document.");
		}
	}
}
