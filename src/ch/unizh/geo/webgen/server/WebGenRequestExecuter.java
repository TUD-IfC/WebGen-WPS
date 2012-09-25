package ch.unizh.geo.webgen.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.XMLWriter;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.xml.GenerateXMLFactory;
import ch.unizh.geo.webgen.xml.IXMLParamGenerator;
import ch.unizh.geo.webgen.xml.WebGenXMLParser;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;

public class WebGenRequestExecuter {
	
	private static Logger LOGGER = Logger.getLogger(WebGenRequestExecuter.class);

	public static WebGenRequest callService(HashMap<String,Object> params, String endpoint, String method) {
		//LOGGER.info("Endpoint: "+endpoint+"  - Method: "+method);
		try {
			if(endpoint.equals("local")) return makeLocalRequest(params, method);
			else if(endpoint.equals("localcloned")) return makeLocalClonedRequest(params, method);
			else return makeRequest(params, endpoint, method);
		}
		catch(Exception e) {
			System.out.println(e.getLocalizedMessage() + " while calling " + method);
			e.printStackTrace();
			return null;
		}
	}
	
	private static WebGenRequest makeRequest(HashMap<String,Object> params, String endpoint, String method) {
		GenerateXMLFactory generatexmlfactory = GenerateXMLFactory.getInstance();
		try {
			Document document = DocumentHelper.createDocument();
			QName requestname = QName.get("Request", "webgen", "http://www.webgen.org/webgen");
	        Element root = document.addElement(requestname);
	        root.addAttribute("algorithm", method);
	        
	        for(Iterator iter = params.keySet().iterator(); iter.hasNext();) {
	        	String pn = (String) iter.next();
	        	Object po = params.get(pn);
	        	IXMLParamGenerator fcgenerator = generatexmlfactory.getXMLParser(po);
	        	if(fcgenerator != null) fcgenerator.generate(po, pn, root);
	        	else LOGGER.warn("no XML generator for " + pn + "found!");
	        }
	        //System.out.println(document.asXML());
	        
	        URL url = new URL(endpoint+"?"+Math.random());
	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	        conn.setDoOutput(true);
	        conn.setRequestMethod("POST");
	        OutputStream os = conn.getOutputStream();
	        OutputStreamWriter wr = new OutputStreamWriter(os);
	        XMLWriter writer = new XMLWriter(wr);
	        writer.write(document);
	        wr.flush();
	        wr.close();
	        os.close();
	    
	        // Get the response
	        WebGenRequest wgreq = null;
	        try {
	        	InputStream is = conn.getInputStream();
				wgreq = WebGenXMLParser.parseXMLRequest(is);
	        }
			catch (IOException e) {
				wgreq = new WebGenRequest();
				wgreq.addMessage("error", e.getMessage()+" due to "+conn.getResponseMessage());
		    	BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
		        String line; String errorhtml = "";
		        while ((line = rd.readLine()) != null) {errorhtml += line;}
		        wgreq.addMessage("errorhtml", errorhtml);
			}
	        wr.close();
	        return wgreq;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static WebGenRequest makeLocalRequest(HashMap<String,Object> params, String classname) {
		WebGenRequest wgreq = new WebGenRequest();
		wgreq.addParameters(params);
        try {
        	IWebGenAlgorithm service = (IWebGenAlgorithm) Class.forName("ch.unizh.geo.webgen.service."+classname).newInstance();
        	service.run(wgreq);
		}
        catch (InstantiationException e) {e.printStackTrace();}
        catch (IllegalAccessException e) {e.printStackTrace();}
        catch (ClassNotFoundException e) {e.printStackTrace();}
        return wgreq;
	}
	
	
	private static WebGenRequest makeLocalClonedRequest(HashMap<String,Object> params, String classname) {
		WebGenRequest wgreq = new WebGenRequest();
		for(Iterator iter = params.keySet().iterator(); iter.hasNext();) {
        	String pn = (String) iter.next();
        	Object po = params.get(pn);
        	synchronized(po) {
        		if(po instanceof ConstrainedFeatureCollection){
            		po = ((ConstrainedFeatureCollection)po).clone();
            	}
            	else if(po instanceof FeatureDataset){
            		FeatureDataset pot = new FeatureDataset(((FeatureCollection)po).getFeatureSchema());
            		for(Iterator fi = ((FeatureCollection)po).iterator(); fi.hasNext();) pot.add(((Feature)fi.next()).clone(true));
            		po = pot;
            	}
        	}
        	wgreq.addParameter(pn, po);
        }
		
        try {
        	IWebGenAlgorithm service = (IWebGenAlgorithm) Class.forName("ch.unizh.geo.webgen.service."+classname).newInstance();
        	service.run(wgreq);
		}
        catch (InstantiationException e) {e.printStackTrace();}
        catch (IllegalAccessException e) {e.printStackTrace();}
        catch (ClassNotFoundException e) {e.printStackTrace();}
        catch (Exception e) {e.printStackTrace();}
        return wgreq;
	}
}
