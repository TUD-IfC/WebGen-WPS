package ch.unizh.geo.webgen.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import ch.unizh.geo.webgen.tools.HTMLEncoder;

public class WebGenInterface extends HttpServlet {
	
	static final long serialVersionUID = 12345;
	
	//public static String PROPERTY_NAME_HOST_NAME = "hostname";
	//public static String PROPERTY_NAME_HOST_PORT = "hostport";
	
	String hostname = null;
	String hostport = null;
	
	private static Logger LOGGER = Logger.getLogger(WebGenService.class);

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		LOGGER.info("WebProcessingInterface initializing...");
		try {
			ClassLoader classLoader = WebGenRegistry.class.getClassLoader();
			InputStream propIs = classLoader.getResourceAsStream("webgen.properties");
			Properties webgenProperties = new Properties();
			webgenProperties.load(propIs);
			hostname = webgenProperties.getProperty("hostname");
			if(hostname.equals("")) hostname = null;
			hostport = webgenProperties.getProperty("hostport");
			if(hostport.equals("")) hostport = null;
		}
		catch(Exception e) {}
		LOGGER.info("WebGen Interface Descriptions sucessfully initialized!");
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try {
			if(hostname == null) hostname = ""+req.getServerName();
			if(hostport == null) hostport = ""+req.getServerPort();
			String executeurl = "http://"+hostname+":"+hostport+req.getContextPath()+"/execute";
			String algorithmname = req.getParameter("algorithm");	
			String algorithmpath = "ch.unizh.geo.webgen.service." + algorithmname;
			
			IWebGenAlgorithm algorithm = (IWebGenAlgorithm)Class.forName(algorithmpath).newInstance();
			
			PrintWriter reswriter = res.getWriter();
			reswriter.print(algorithm.getInterfaceDescription().generateXMLDescription(executeurl));
			reswriter.close();
			res.setStatus(HttpServletResponse.SC_OK);
		}
		catch (Exception e) {
			handleException(e, res);
			//handleException(new Exception("Interface Description for could not be created ..."), res);
		}
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		handleException(new Exception("POST not allowed yet ..."), res);
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
