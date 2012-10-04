package ch.unizh.geo.webgen.test;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.XMLWriter;

import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.xml.GenerateXMLFactory;
import ch.unizh.geo.webgen.xml.IXMLParamGenerator;
import ch.unizh.geo.webgen.xml.ParseXMLFactory;
import ch.unizh.geo.webgen.xml.ParserGeneratorRegistry;
import ch.unizh.geo.webgen.xml.WebGenXMLParser;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;

public class JUMPDom4jTestClient extends AbstractPlugIn implements ThreadedPlugIn {
	
	private MultiInputDialog dialog;
	long timeAverage;
	
	public JUMPDom4jTestClient() {
    }

    public void initialize(PlugInContext context) throws Exception {
    	context.getFeatureInstaller().addMainMenuItem(
                this, "Moritz", "Dom4j Client",
    			null, null);
    }

    public boolean execute(PlugInContext context) throws Exception {
    	try {
    		initDialog(context);
        	dialog.setVisible(true);
        	if (!dialog.wasOKPressed()) {return false;}
        	return true;
    	}
    	catch (java.lang.IndexOutOfBoundsException e) {return false;}
    }

    private void initDialog(PlugInContext context) {
        dialog = new MultiInputDialog(context.getWorkbenchFrame(), "Dom4j Buffer", true);
        dialog.setSideBarDescription("Dom4j Buffer");
        dialog.addLayerComboBox("layer", context.getCandidateLayer(0), null, context.getLayerManager());
        dialog.addDoubleField("width", 10.0, 5, "width");
        GUIUtil.centreOnWindow(dialog);
    }

    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
    	sendPOST(context);
    }
    
    private void sendPOST(PlugInContext context) throws Exception {
    	Document document = DocumentHelper.createDocument();
		QName requestname = QName.get("Request", "webgen", "http://www.webgen.org/webgen");
        Element root = document.addElement(requestname);
        root.addAttribute("algorithm", "BufferFeatures");
        
        //generate parameters
        /*String[] registeredGenerators = {
				"ch.unizh.geo.webgen.xml.ParamGeneratorINTEGER",
				"ch.unizh.geo.webgen.xml.ParamGeneratorDOUBLE",
				"ch.unizh.geo.webgen.xml.ParamGeneratorFeatureCollection"
				};*/
		GenerateXMLFactory.initialize();
        GenerateXMLFactory generatexmlfactory = GenerateXMLFactory.getInstance();
        
        FeatureCollection fc = dialog.getLayer("layer").getFeatureCollectionWrapper();
        IXMLParamGenerator fcgenerator = generatexmlfactory.getXMLParser(fc);
    	if(fcgenerator != null) fcgenerator.generate(fc, "bufflayer", root);
        
    	Double width = (Double)dialog.getDouble("width");
        IXMLParamGenerator widthgenerator = generatexmlfactory.getXMLParser(width);
    	if(widthgenerator != null) widthgenerator.generate(width, "width", root);
        
    	//Construct data
        //String data = URLEncoder.encode("key1", "UTF-8") + "=" + URLEncoder.encode("value1", "UTF-8");
        //data += "&" + URLEncoder.encode("key2", "UTF-8") + "=" + URLEncoder.encode("value2", "UTF-8");
    
        // Send data
        //URL url = new URL("http://192.168.58.146:8080/webgen/servlet/ch.unizh.geo.webgen.server.WebGenService");
    	//URL url = new URL("http://localhost:8080/webgen/servlet/ch.unizh.geo.webgen.server.WebGenService");
    	URL url = new URL("http://localhost:8080/webgen/execute");
    	//URL url = new URL("http://localhost:8080/webgen/servlet/ch.unizh.geo.webgen.test.WebGenForward");
    	//URL url = new URL("http://www.geo.unizh.ch:8080/neun/servlet/webgen.server.WebGenForward");
    	URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        XMLWriter writer = new XMLWriter(wr);
        writer.write(document);
        //writer.close();
        wr.flush();
    
        // Get the response
        parse(context, conn.getInputStream());
        /*BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            // Process line...
        	System.out.println(line);
        }*/
        wr.close();
        //rd.close();
    }
    
    private void parse(PlugInContext context, InputStream is) throws Exception {
		ParseXMLFactory.initialize();
		WebGenRequest wgreq = WebGenXMLParser.parseXMLRequest(is);
		context.addLayer(StandardCategoryNames.WORKING, "dom4j", wgreq.getFeatureCollection("result"));
    }
    
}