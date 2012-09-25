package ch.unizh.geo.webgen.server;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.XMLWriter;

import ch.unizh.geo.webgen.xml.GenerateXMLFactory;
import ch.unizh.geo.webgen.xml.IXMLParamGenerator;
import ch.unizh.geo.webgen.xml.ParamGeneratorMESSAGE;
import ch.unizh.geo.webgen.xml.WebGenXMLParser;

public class WebGenRequestHandler {

	/**
	 * default constructor
	 */
	public WebGenRequestHandler() {
	}
	
	
	/**
	 * handles GET requests (do nothing, currently)
	 * @param params
	 */
	public WebGenRequestHandler(Map params, HttpServletResponse res) throws Exception {
		this();
		throw new Exception("HTTP GET not supported yet!");
	}
	
	
	/**
	 * handles POST requests (parse XML message and process request)
	 * @param is, res
	 */
	public WebGenRequestHandler(InputStream is, HttpServletResponse res) throws Exception {
		this();
		//WebGenRequest wgreq = parseXMLRequest(is);
		WebGenRequest wgreq = WebGenXMLParser.parseXMLRequest(is);
		executeRequest(wgreq);
		generateXMLResponse(wgreq, res);
	}
	
	
	public void executeRequest(WebGenRequest wgreq) throws Exception {
			Class toRun = Class.forName(wgreq.getAlgorithmPath());
			IWebGenAlgorithm algoclass = (IWebGenAlgorithm) toRun.newInstance();
			algoclass.run(wgreq);
	}
	
	public static void generateXMLResponse(WebGenRequest wgreq, HttpServletResponse res) throws Exception {
		generateXMLResponse(wgreq, res.getWriter());
	}
	
	public static void generateXMLResponse(WebGenRequest wgreq, PrintWriter pw) throws Exception {
		//encode result
		Document document = DocumentHelper.createDocument();
		QName requestname = QName.get("Request", "webgen", "http://www.webgen.org/webgen");
        Element root = document.addElement(requestname);
        
        //generate parameters
        GenerateXMLFactory generatexmlfactory = GenerateXMLFactory.getInstance();
        
        HashMap results = wgreq.getResults();
        for(Iterator riter = results.entrySet().iterator(); riter.hasNext();) {
        	Map.Entry tr = (Map.Entry)riter.next();
        	String tn = (String)tr.getKey();
        	Object tv = tr.getValue();
        	IXMLParamGenerator generator = generatexmlfactory.getXMLParser(tv);
        	if(generator != null) generator.generate(tv, tn, root);
        }
        
        HashMap messages = wgreq.getMessages();
        for(Iterator riter = messages.entrySet().iterator(); riter.hasNext();) {
        	Map.Entry tr = (Map.Entry)riter.next();
        	String tn = (String)tr.getKey();
        	Object tv = tr.getValue();
        	IXMLParamGenerator generator = new ParamGeneratorMESSAGE();
        	generator.generate(tv, tn, root);
        }
        
        /*Pretty print the document to System.out
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter owriter = new XMLWriter( System.out, format );
        owriter.write(document);*/
        /*XMLWriter writer = new XMLWriter(
                new FileWriter( "c:\\java\\dom4j_output2.xml" )
            );
            writer.write( document );
            writer.close();*/
        //write to the response stream
        XMLWriter writer = new XMLWriter(pw);
        writer.write(document);
        writer.close();
	}
}
