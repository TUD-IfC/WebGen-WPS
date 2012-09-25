package ch.unizh.geo.webgen.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import ch.unizh.geo.webgen.tools.HTMLEncoder;
import ch.unizh.geo.webgen.xml.GenerateXMLFactory;
import ch.unizh.geo.webgen.xml.ParseXMLFactory;
import ch.unizh.geo.webgen.xml.WebGenXMLParser;

public class WebGenService extends HttpServlet {
	
	static final long serialVersionUID = 12345;
	
	HashMap<String,IWebGenAlgorithm> persistentServices;
	
	private static Logger LOGGER = Logger.getLogger(WebGenService.class);

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		LOGGER.info("WebProcessingService initializing...");
		ParseXMLFactory.initialize();
		LOGGER.info("XML Parsers initialized successfully");
		GenerateXMLFactory.initialize();
		LOGGER.info("XML Generators initialized successfully");
		
		persistentServices = new HashMap<String,IWebGenAlgorithm>();
		/*try {
			persistentServices.put("ch.unizh.geo.webgen.service.ConstraintSpaceKnowledgeBase", new ConstraintSpaceKnowledgeBase());
			LOGGER.info("Persistent Services initialized successfully");
		}
		catch (Exception e) {}*/
		
		LOGGER.info("WebGen up and running!");
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
			try {
				new WebGenRequestHandler(req.getParameterMap(), res);
			}
			catch(Exception e) {
				handleException(e, res);
			}
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try {
			InputStream is = req.getInputStream();
			if(is != null) {
				//new WebGenRequestHandler(is, res);
				handleRequest(is, res);
				res.setStatus(HttpServletResponse.SC_OK);
			}
			else{
				handleException(new Exception("POST Message is null"), res);
			}
		} 
		catch(Exception e) {
			handleException(e, res);
		}
	}
	
	
	public void handleRequest(InputStream is, HttpServletResponse res) throws Exception {
		WebGenRequest wgreq = WebGenXMLParser.parseXMLRequest(is);
		
		String algorithmPath = wgreq.getAlgorithmPath();
		if(persistentServices.containsKey(algorithmPath)) {
			IWebGenAlgorithm algoclass = (IWebGenAlgorithm) persistentServices.get(algorithmPath);
			algoclass.run(wgreq);
		}
		else {
			Class toRun = Class.forName(algorithmPath);
			IWebGenAlgorithm algoclass = (IWebGenAlgorithm) toRun.newInstance();
			algoclass.run(wgreq);
		}
		
		WebGenRequestHandler.generateXMLResponse(wgreq, res);
	}
	
	
	private void handleException(Exception exception, HttpServletResponse res) {
		res.setContentType("text/xml");
		try {
			String exstr = exception.getClass().getSimpleName() + " in " + exception.getLocalizedMessage()+"\n";
			for(int erri=0; erri < exception.getStackTrace().length; erri++) {
				exstr = exstr + "\n" + exception.getStackTrace()[erri];
			}
			exstr = "<webgen:Error xmlns:webgen=\"http://www.webgen.org/webgen\">\n" +
			        HTMLEncoder.encode(exstr) +
			        "\n</webgen:Error>";
			PrintWriter reswriter = res.getWriter();
			reswriter.print(exstr);
			reswriter.close();
			LOGGER.debug(exception.toString());
			res.setStatus(HttpServletResponse.SC_OK);
		}
		catch(IOException e){
			LOGGER.warn("exception occured while writing ExceptionReport to stream");
			try {
				res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error occured, while writing OWS Exception output");
				}
			catch(IOException ex) {
				LOGGER.error("error while writing error code to client!");
			}
		
		}
	}
}
