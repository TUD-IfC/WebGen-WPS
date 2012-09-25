package ch.unizh.geo.webgen.xml;

import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import ch.unizh.geo.webgen.server.WebGenRequest;

public class WebGenXMLParser {
	
	public static WebGenRequest parseXMLRequest(Reader in) throws Exception {
		return parseXMLRequest((new SAXReader()).read(in));
	}
	
	public static WebGenRequest parseXMLRequest(InputStream is) throws Exception {
		return parseXMLRequest((new SAXReader()).read(is));
	}
	
	public static WebGenRequest parseXMLRequest(Document document) throws Exception {
        Element root = document.getRootElement();
        
        //check root element
        if(!root.getNamespacePrefix().equals("webgen"))
        	throw new Exception("Only webgen namespace is supported!");
        if(root.getName().equals("Error")) {
        	System.out.println(root.getText());
        	throw new Exception("Error:");
        }
        if(!root.getName().equals("Request"))
        	throw new Exception("Only WebGenRequest elements are supported!");
        
        //parse XML
        WebGenRequest wgreq = new WebGenRequest();
        
        //get algorithm name
        String algorithm = root.attributeValue("algorithm");
        if(algorithm != null) {
        	if(algorithm.contains("."))
            	throw new Exception("Invalid algorithm name!");
            wgreq.setAlgorithmName(algorithm);
        }
        
        //parse parameters
        ParseXMLFactory parsexmlfactory = ParseXMLFactory.getInstance();
        Iterator paramiter = root.elementIterator("Parameter");
        while(paramiter.hasNext()) {
        	Element telem = (Element)paramiter.next();
        	String telemtype = telem.attributeValue("type");
        	IXMLParamParser parser = parsexmlfactory.getXMLParser(telemtype);
        	if(parser != null) parser.parse(wgreq, telem);
        }

        return wgreq;
	}

}
