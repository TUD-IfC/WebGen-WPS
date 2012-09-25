package ch.unizh.geo.webgen.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import ch.unizh.geo.webgen.registry.AlgorithmRegistry;
import ch.unizh.geo.webgen.tools.HTMLEncoder;

public class WebGenRegistry extends HttpServlet {
	
	static final long serialVersionUID = 12345;
	
	//public static String PROPERTY_NAME_HOST_NAME = "hostname";
	//public static String PROPERTY_NAME_HOST_PORT = "hostport";
	public String[] algorithms;
	AlgorithmRegistry registry;
	
	String hostname = null;
	String hostport = null;
	
	private static Logger LOGGER = Logger.getLogger(WebGenService.class);

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		LOGGER.info("WebGenRegistry initializing...");
		
		try {
			ClassLoader classLoader = WebGenRegistry.class.getClassLoader();
			InputStream propIs = classLoader.getResourceAsStream("webgen.properties");
			Properties webgenProperties = new Properties();
			webgenProperties.load(propIs);
			hostname = webgenProperties.getProperty("hostname");
			if(hostname.equals("")) hostname = null;
			hostport = webgenProperties.getProperty("hostport");
			if(hostport.equals("")) hostport = null;
			//String algorithmsPropVal = webgenProperties.getProperty("algorithms");
			//algorithms = algorithmsPropVal.split(",");
			//int algocount = Integer.parseInt(webgenProperties.getProperty("algorithm.count"));
			//algorithms = new String[algocount];
			//for(int i=0; i<algocount; i++) algorithms[i] = webgenProperties.getProperty("algorithm."+i);
		}
		catch(Exception e) {
			algorithms = new String[0];
		}
		
		try {
			ClassLoader classLoader = ClassLoader.getSystemClassLoader();
			//ClassLoader classLoader = this.getClass().getClassLoader();
			URL servicesurl = classLoader.getResource("ch/unizh/geo/webgen/service/");
			File servicesdir = new File(servicesurl.getFile());
			algorithms = servicesdir.list();
			List<String> alglist = Arrays.asList(algorithms);
			Collections.sort(alglist);
			algorithms = new String[alglist.size()];
			for(int i=0; i<alglist.size(); i++) algorithms[i] = alglist.get(i).replaceFirst(".class","");
			//for(int i=0; i<algorithms.length; i++) algorithms[i] = algorithms[i].replaceFirst(".class","");
		}
		catch(Exception e) {
			algorithms = new String[0];
		}
		
		registry = new AlgorithmRegistry(algorithms, false);
		
		LOGGER.info("WebGen Registry sucessfully initialized!");
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try {
			if(hostname == null) hostname = ""+req.getServerName();
			if(hostport == null) hostport = ""+req.getServerPort();
			String registryurl = "http://"+hostname+":"+hostport+req.getContextPath()+"/describe";
			PrintWriter reswriter = res.getWriter();
			reswriter.print(registry.generateXMLList(registryurl));
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
