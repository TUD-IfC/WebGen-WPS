package ica.wps.server.request.getcapabilities;

import java.util.Iterator;

import ica.wps.server.ExceptionCode;
import ica.wps.server.ServerException;
import ica.wps.server.WPSServlet;
import ica.wps.server.Capabilities;
import ica.wps.server.IWPSOperator;
import ica.wps.server.request.Request;
import ica.wps.WPSConstants;

import net.opengis.wps.x100.CapabilitiesDocument;
import net.opengis.wps.x100.WPSCapabilitiesType;
import net.opengis.wps.x100.ProcessOfferingsDocument.ProcessOfferings;
import net.opengis.wps.x100.ProcessBriefType;
import net.opengis.wps.x100.LanguagesDocument.Languages;
import net.opengis.ows.x11.ServiceIdentificationDocument.ServiceIdentification;
import net.opengis.ows.x11.ServiceProviderDocument.ServiceProvider;
import net.opengis.ows.x11.OperationsMetadataDocument.OperationsMetadata;
import net.opengis.ows.x11.KeywordsType;
import net.opengis.ows.x11.OperationDocument.Operation;
import net.opengis.ows.x11.HTTPDocument.HTTP;
import org.apache.xmlbeans.XmlObject;

/**
 * Request class handles the GetCapabilities request.
 * @author	M. Wittensoeldner
 * @date	Created on 01.02.2007
 */
public abstract class RequestGetCapabilities extends Request {

	/**
	 * Handles the request. This method is called after parseRequest().
	 * @return void
	 * @return XmlObject				The resulted xml document.
	 * @throws ServerException			When an error occurs.
	 */
	protected XmlObject handleRequest() throws ServerException {
		if (_sAcceptVersions != null && !_sAcceptVersions.equals(WPSConstants.WPS_VERSION)) {
			throw new ServerException(ExceptionCode.InvalidParameterValue, PARAM_ACCEPTVERSIONS, "Version must be '"+WPSConstants.WPS_VERSION+"', but it's '"+_sAcceptVersions+"'");
		}
		
		Capabilities capabilities = WPSServlet.getCapabilities();
		
		CapabilitiesDocument doc = CapabilitiesDocument.Factory.newInstance();
		WPSCapabilitiesType caps = doc.addNewCapabilities();
		caps.addNewService().setStringValue(WPSConstants.WPS_SERVICE);
		caps.setVersion(WPSConstants.WPS_VERSION);
		caps.setUpdateSequence("1");
		caps.setLang(WPS_LANGUAGE);
		ServiceIdentification service = caps.addNewServiceIdentification();
		service.addNewTitle().setStringValue(capabilities.getServerDescription().getTitle());
		service.addNewAbstract().setStringValue(capabilities.getServerDescription().getDescription());
		if ((capabilities.getServerDescription().getKeywords() != null) && (capabilities.getServerDescription().getKeywords().size() > 0)) {
			KeywordsType keywords = service.addNewKeywords();
			Iterator<String> iterKeywords = capabilities.getServerDescription().getKeywords().iterator();
			while (iterKeywords.hasNext()) {
				keywords.addNewKeyword().setStringValue(iterKeywords.next());
			}
		}
		service.addNewServiceType().setStringValue(WPSConstants.WPS_SERVICE);
		service.addNewServiceTypeVersion().setStringValue(WPSConstants.WPS_VERSION);
		service.setFees("NONE");
		service.addNewAccessConstraints().setStringValue("NONE");
		
		ServiceProvider provider = caps.addNewServiceProvider();
		provider.setProviderName(capabilities.getServerDescription().getServiceProviderName());
		provider.addNewProviderSite().setHref(capabilities.getServerDescription().getServiceProviderURL());
		provider.addNewServiceContact().addNewContactInfo().addNewAddress().addElectronicMailAddress(capabilities.getServerDescription().getServiceProviderEmail());
		provider.getServiceContact().setIndividualName(capabilities.getServerDescription().getServiceProviderContactName());
		
		OperationsMetadata metadata = caps.addNewOperationsMetadata();
		Operation op = metadata.addNewOperation();
		op.setName(WPSConstants.WPS_SERVICE_GETCAPABILITIES);
		HTTP http = op.addNewDCP().addNewHTTP();
		http.addNewGet().setHref(capabilities.getServerUrl()+"?");
		http.addNewPost().setHref(capabilities.getServerUrl());
		op = metadata.addNewOperation();
		op.setName(WPSConstants.WPS_SERVICE_DESCRIBEPROCESS);
		http = op.addNewDCP().addNewHTTP();
		http.addNewGet().setHref(capabilities.getServerUrl()+"?");
		http.addNewPost().setHref(capabilities.getServerUrl());
		op = metadata.addNewOperation();
		op.setName(WPSConstants.WPS_SERVICE_EXECUTEPROCESS);
		http = op.addNewDCP().addNewHTTP();
		http.addNewPost().setHref(capabilities.getServerUrl());
		
		if ((capabilities.getOperators() != null) && (capabilities.getOperators().size() > 0)) {
			ProcessOfferings offerings = caps.addNewProcessOfferings();
			IWPSOperator operator;
			Iterator<IWPSOperator> iterOps = capabilities.getOperators().iterator();
			while (iterOps.hasNext()) {
				operator = iterOps.next();
				ProcessBriefType process = offerings.addNewProcess();
				this.setProcessInformation(process, operator);
			}
		}
		
		Languages langs = caps.addNewLanguages();
		langs.addNewDefault().setLanguage(WPS_LANGUAGE);
		langs.addNewSupported().addLanguage(WPS_LANGUAGE);
		
		return doc;
	}

}
