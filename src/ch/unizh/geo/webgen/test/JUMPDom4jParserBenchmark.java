package ch.unizh.geo.webgen.test;

import java.io.FileInputStream;
import java.io.InputStream;

import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.xml.ParseXMLFactory;
import ch.unizh.geo.webgen.xml.ParserGeneratorRegistry;
import ch.unizh.geo.webgen.xml.WebGenXMLParser;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;

public class JUMPDom4jParserBenchmark extends AbstractPlugIn implements ThreadedPlugIn {
	
	//private MultiInputDialog dialog;
	long timeAverage;
	
	public JUMPDom4jParserBenchmark() {
    }

    public void initialize(PlugInContext context) throws Exception {
    	context.getFeatureInstaller().addMainMenuItem(
                this, "Moritz", "Parser Benchmark Dom4j",
    			null, null);
    }

    public boolean execute(PlugInContext context) throws Exception {
    	try {
    		/*initDialog(context);
        	dialog.setVisible(true);
        	if (!dialog.wasOKPressed()) {return false;}*/
        	return true;
    	}
    	catch (java.lang.IndexOutOfBoundsException e) {return false;}
    }

    /*private void initDialog(PlugInContext context) {
        dialog = new MultiInputDialog(context.getWorkbenchFrame(), "JUMPDome4jParserBenchmark", true);
        dialog.setSideBarDescription("JUMPDome4jParserBenchmark");
        dialog.addLayerComboBox("layer", context.getCandidateLayer(0), null, context.getLayerManager());
        GUIUtil.centreOnWindow(dialog);
    }*/

    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
    	timeAverage = 0;
    	int i=0;
    	for(;i<10;i++) {
    		//generate();
        	parse(context);
    	}
    	System.out.println("Dom4j benchmark, Average time " + timeAverage/i + "ms");
    }
    
    private void parse(PlugInContext context) throws Exception {
		ParseXMLFactory.initialize();
		
		long startTime = System.currentTimeMillis();
		
		InputStream is = new FileInputStream("c:\\java\\dom4j_output.xml");
		//WebGenRequestHandler wgreqh = new WebGenRequestHandler();
		//WebGenRequest wgreq = wgreqh.parseXMLRequest(is);
		WebGenRequest wgreq = WebGenXMLParser.parseXMLRequest(is);
		timeAverage += (System.currentTimeMillis() - startTime);
		System.out.println("Finished parsing dom4j in " + (System.currentTimeMillis() - startTime) + "ms");
		System.out.println("Result contains " + wgreq.getResults().size() + "items");
		//context.addLayer(StandardCategoryNames.WORKING, "dom4j", wgreq.getFeatureCollection("result"));
    }
    
    /*private void generate() throws Exception {
		GenerateXMLFactory.initialize(ParserGeneratorRegistry.getRegisteredGenerators());
		
		long startTime = System.currentTimeMillis();
    	
		FeatureCollection fc = dialog.getLayer("layer").getFeatureCollectionWrapper();
    	WebGenRequest wgreq = new WebGenRequest();
		wgreq.addResult("result", fc);
		WebGenRequestHandler wgreqh = new WebGenRequestHandler();
		//wgreqh.generateXMLResponse(wgreq, null);
		timeAverage += (System.currentTimeMillis() - startTime);
		System.out.println("Finished generating dom4j in " + (System.currentTimeMillis() - startTime) + "ms");
    }*/
    
}