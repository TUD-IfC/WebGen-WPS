package ica.wps.server;


import ica.wps.common.IWPSXmlObjectParser;
import ica.wps.server.request.Request;
import ica.wps.server.request.describeprocess.RequestDescribeProcessGet;
import ica.wps.server.request.describeprocess.RequestDescribeProcessPost;
import ica.wps.server.request.getcapabilities.RequestGetCapabilitiesGet;
import ica.wps.server.request.getcapabilities.RequestGetCapabilitiesPost;
import ica.wps.server.request.execute.RequestExecuteGet;
import ica.wps.server.request.execute.RequestExecutePost;
import ica.wps.server.request.describeschema.RequestDescribeSchemaGet;
import ica.wps.server.request.describeschema.RequestDescribeSchemaPost;
import ica.wps.server.Capabilities;
import ica.wps.WPSConstants;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.HashMap;
import java.io.FileInputStream;
import java.io.File;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlCursor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import net.opengis.ows.x11.ExceptionReportDocument;
import net.opengis.ows.x11.ExceptionReportDocument.ExceptionReport;
import net.opengis.ows.x11.ExceptionType;



/**
 * WPS Servlet supports the official WPS requests.
 * @author	M. Wittensoeldner
 * @date	Created on 01.02.2007
 */
public class WPSServlet extends HttpServlet {
	static final long 						serialVersionUID = 0L;
	protected static final String			PARAM_SERVICE = "service";
	protected static final String			PARAM_REQUEST = "Request";
	protected static final Capabilities		_capabilities = new Capabilities();
	protected static String					_sHomeDirectory;
	protected static IWPSServer				_wpsServer;
	protected static boolean				_bInitialized = false;
	protected static XmlOptions				_xmlOptions;
	
	/**
	 * Constructor.
	 */
	public WPSServlet() {
		
	}

	/**
	 * Gets the capabilities.
	 * @return Capabilities				The capabilities.
	 */
	public static Capabilities getCapabilities() {
		return _capabilities;
	}
	
	/**
	 * Gets the xml object parser.
	 * @return IWPSXmlObjectParser		The xml object parser.
	 */
	public static IWPSXmlObjectParser getXmlObjectParser() {
		return _wpsServer.getXmlParser();
	}

	/**
	 * Gets the local home directory.
	 * @return String					The local home directory.
	 */
	public static String getHomeDirectory() {
		return _sHomeDirectory;
	}

	/**
	 * Gets the xml options.
	 * @return XmlOptions				The xml options.
	 */
	public static XmlOptions getXmlOptions() {
		return _xmlOptions;
	}

	/**
	 * Gets the server properties.
	 * @return Properties				The server properties.
	 * @throws Exception				When an error occurs.
	 */
	public static Properties getProperties() throws Exception {
		Properties props = new Properties();
		props.load(new FileInputStream(_sHomeDirectory+"config"+File.separator+"wps-server.properties"));
		return props;
	}

	/**
	 * Initializes the servlet.
	 * @return void
	 * @param config					The servlet configuration.
	 * @throws ServletException			When an error occurs.
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		_sHomeDirectory = config.getServletContext().getRealPath("")+File.separator;
		Properties props = null;
		try {
			props = WPSServlet.getProperties();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ServletException("Loading properties file failed. ("+ex.getMessage()+")");
		}
		try {
			ClassLoader classLoader = WPSServlet.class.getClassLoader();
			String sClassIWPSServer = (String)props.get(IWPSServer.class.getSimpleName());
			_wpsServer = (IWPSServer)classLoader.loadClass(sClassIWPSServer).newInstance();
			_wpsServer.init(classLoader);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ServletException("Loading IWPSServer interface failed. ("+ex.getMessage()+")");
		}
		_xmlOptions = new XmlOptions();
		HashMap<String, String> mapPrefixes = new HashMap<String, String>();
		mapPrefixes.put("http://www.opengis.net/wps/1.0.0", "wps");
		mapPrefixes.put("http://www.opengis.net/ows/1.1", "ows");
		mapPrefixes.put("http://www.w3.org/1999/xlink", "xlink");
		mapPrefixes.put(WPSConstants.WPS_DEFAULT_NAMESPACE, "ica");
		_xmlOptions.setSaveSuggestedPrefixes(mapPrefixes);
		_xmlOptions.setSaveAggressiveNamespaces();
		_xmlOptions.setSavePrettyPrint();
		_xmlOptions.setCharacterEncoding(WPSConstants.WPS_ENCODING);
		_bInitialized = true;
	}

	/**
	 * Checks the servlet state and initializes capabilities when not yet done.
	 * @return void
	 * @param request					The servlet request.
	 * @param response					The servlet response.
	 * @throws ServerException			When an error occurs.
	 */
	public void initialCheck(HttpServletRequest request, HttpServletResponse response) throws ServerException {
		// check whether initialization was successful
		if (!_bInitialized)
			throw new ServerException(ExceptionCode.NoApplicableCode, "", "Server initialization failed.");
		// check whether capabilities needs to be refreshed
		if (_capabilities.needsRefresh()) {
			String sRequestUrl = request.getRequestURL().toString();
			try {
				sRequestUrl = WPSServlet.getProperties().getProperty("request.url", request.getRequestURL().toString());
			} catch (Exception ex) {
			}
			_capabilities.init(_wpsServer, sRequestUrl);
			// delete data files older than 12h.
			try {
				File outFile = new File(WPSServlet.getHomeDirectory()+WPSServlet.getProperties().getProperty(Request.PROP_DATADIR));
				long lRemoveAgeHour = 12;
				try {
					lRemoveAgeHour = new Long(WPSServlet.getProperties().getProperty("data.remove.age.hour")).longValue();
				} catch (Exception ex) {}
				DataDeletionThread threadDeletion = new DataDeletionThread(outFile, lRemoveAgeHour);
				threadDeletion.start();				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		response.setHeader("Expires", "0");
		if (request.getProtocol().equals("HTTP/1.1"))
			response.setHeader("Cache-Control","no-cache");
		else if (request.getProtocol().equals("HTTP/1.0"))
			response.setHeader("Pragma","no-cache");
		response.setContentType(WPSConstants.WPS_MIME_XML);
	}

	/**
	 * This method is called when a GET request has been arrived.
	 * @return void
	 * @param request					The servlet request.
	 * @param response					The servlet response.
	 * @throws ServletException			When an error occurs.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			this.initialCheck(request, response);			
		} catch (ServerException ex) {
			this.handleException(response, ex);
			return;
		}
		String sService = this.getParameter(request, PARAM_SERVICE);
		String sRequest = this.getParameter(request, PARAM_REQUEST);
		Document doc = XmlObject.Factory.newDomImplementation().createDocument("", "GETRequest", null);
		Element root = doc.getDocumentElement();
		String sAttributeName;
		Enumeration<?> eParams = (Enumeration<?>)request.getParameterNames();
		while (eParams.hasMoreElements()) {
			sAttributeName = (String)eParams.nextElement();
			root.setAttribute(sAttributeName, request.getParameter(sAttributeName));
		}
		XmlObject document = null;
		try {
			document = XmlObject.Factory.parse(doc.getFirstChild());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		this.handleRequest(document, response, sService, sRequest, false);
	}
	
	/**
	 * This method is called when a POST request has been arrived.
	 * @return void
	 * @param request					The servlet request.
	 * @param response					The servlet response.
	 * @throws ServletException			When an error occurs.
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			this.initialCheck(request, response);			
		} catch (ServerException ex) {
			this.handleException(response, ex);
			return;
		}
		if (request.getInputStream() != null) {
			XmlObject doc = null;
			
			try {
				// read posted xml data
				try {
					File filePost = Request.createDataFile("req", null);
					java.io.FileOutputStream streamOut = new java.io.FileOutputStream(filePost);
					int nRead;
					while ((nRead = request.getInputStream().read()) != -1)
						streamOut.write(nRead);
					streamOut.close();
					request.getInputStream().close();
					
					doc = XmlObject.Factory.parse(filePost);
					filePost.delete();
					Node node;
					int i = 0;
					while (i < doc.getDomNode().getChildNodes().getLength()) {
						node = doc.getDomNode().getChildNodes().item(i);
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							String sService = Request.getAttribute(node, PARAM_SERVICE);
							String sRequest = node.getLocalName();
							this.handleRequest(doc, response, sService, sRequest, true);
							break;
						}
						i++;
					}
				} catch (Exception e) {
					if (e instanceof ServerException)
						throw new ServerException(((ServerException)e).getExceptionCode(), ((ServerException)e).getLocator(), e.getMessage());
					else
						throw new ServerException(ExceptionCode.InvalidParameterValue, "POST request", e.toString()+":"+e.getMessage());
				}				
			} catch (ServerException ex) {
				this.handleException(response, ex);
			}
		}
	}
	
	/**
	 * Initializes the correct request handler and handles the request.
	 * @return void
	 * @param document					The document holds the request input.
	 * @param response					The servlet response.
	 * @param sService					The service parameter.
	 * @param sRequest					The request parameter.
	 * @param bPost						True whether it is a POST request.
	 */
	protected void handleRequest(XmlObject document, HttpServletResponse response, String sService, String sRequest, boolean bPost) {
		try {
			Request requestHandler = null;
			if (sService != null && sService.equalsIgnoreCase("WPS")) {
				if (sRequest != null) {
					if (sRequest.equalsIgnoreCase(WPSConstants.WPS_SERVICE_GETCAPABILITIES)) {
						if (bPost)
							requestHandler = new RequestGetCapabilitiesPost();
						else
							requestHandler = new RequestGetCapabilitiesGet();
					} else if (sRequest.equalsIgnoreCase(WPSConstants.WPS_SERVICE_DESCRIBEPROCESS)) {
						if (bPost)
							requestHandler = new RequestDescribeProcessPost();
						else
							requestHandler = new RequestDescribeProcessGet();
					} else if (sRequest.equalsIgnoreCase(WPSConstants.WPS_SERVICE_EXECUTEPROCESS)) {
						if (bPost)
							requestHandler = new RequestExecutePost();
						else
							requestHandler = new RequestExecuteGet();
					} else if (sRequest.equalsIgnoreCase(WPSConstants.WPS_SERVICE_DESCRIBESCHEMA)) {
						if (bPost)
							requestHandler = new RequestDescribeSchemaPost();
						else
							requestHandler = new RequestDescribeSchemaGet();
					} else {
						// Request parameter is not valid
						throw new ServerException(ExceptionCode.InvalidParameterValue, PARAM_REQUEST, "The parameter '"+PARAM_REQUEST+"' msut be '"+WPSConstants.WPS_SERVICE_GETCAPABILITIES+"', '"+WPSConstants.WPS_SERVICE_DESCRIBEPROCESS+"' ,'"+WPSConstants.WPS_SERVICE_EXECUTEPROCESS+"' or '"+WPSConstants.WPS_SERVICE_DESCRIBESCHEMA+"'.");
					}
				} else {
					// Request parameter is not set
					throw new ServerException(ExceptionCode.MissingParameterValue, PARAM_REQUEST, "The parameter '"+PARAM_REQUEST+"' is not set.");
				}
			} else {
				// service must be WPS
				throw new ServerException(ExceptionCode.MissingParameterValue, PARAM_SERVICE, "The '"+PARAM_SERVICE+"' is not set.");
			}
			// handle the request
			XmlObject doc = requestHandler.handleRequest(document);
			try {
				if (doc.getDomNode().getFirstChild().getNodeName().equals(Request.RAW_OUTPUT_NAME)) {
					// handle direct raw output
					response.setContentType(WPSConstants.WPS_MIME_PLAIN);
					response.getOutputStream().write(doc.getDomNode().getFirstChild().getFirstChild().getNodeValue().getBytes());
				} else {
					doc.save(response.getOutputStream(), WPSServlet.getXmlOptions());
				}
				response.getOutputStream().flush();
			} catch(Exception ex) {
				throw new ServerException(ExceptionCode.NoApplicableCode, "", "Writing xml response failed.");
			}
			
		} catch (ServerException ex) {
			this.handleException(response, ex);
		}
	}
	
	/**
	 * Handles an exception.
	 * @return void
	 * @param res						The response to write the exception to.
	 * @param ex						The server exception.
	 */
	protected void handleException(HttpServletResponse res, ServerException ex) {
		ExceptionReportDocument document = ExceptionReportDocument.Factory.newInstance();
		ExceptionReport report = document.addNewExceptionReport();
		report.setVersion("1.0.0");
		XmlCursor cursor = report.newCursor();
		cursor.setAttributeText(QName.valueOf("language"), "en-GB");
		ExceptionType exception = report.addNewException();
		exception.setExceptionCode(ex.getExceptionCode().name());
        if (ex.getLocator() != null)
        	exception.setLocator(ex.getLocator());
        if (ex.getMessage() != null) {
            exception.addExceptionText(ex.getMessage());
        }
        try {
        	document.save(res.getOutputStream(), WPSServlet.getXmlOptions());
        	res.getOutputStream().close();
    		res.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception ex2) {
        	ex2.printStackTrace();
    		res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);        	
        }
	}

	/**
	 * Helper class to find a case insensitive parameter in get request.
	 * @return String					The found paramter value. Null when not found.
	 * @param request					The servlet request
	 * @param sParameterName			The parameter name.
	 */
	protected String getParameter(HttpServletRequest request, String sParameterName) {
		Enumeration<?> eNames = (Enumeration<?>)request.getParameterNames();
		String sElement;
		while (eNames.hasMoreElements()) {
			sElement = (String)eNames.nextElement();
			if (sElement.equalsIgnoreCase(sParameterName))
				return request.getParameter(sElement);
		}
		return null;
	}
}
