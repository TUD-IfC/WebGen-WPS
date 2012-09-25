package ch.unizh.geo.webgen.client.jump;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.server.WebGenRequestExecuter;
import ch.unizh.geo.webgen.xml.GenerateXMLFactory;
import ch.unizh.geo.webgen.xml.ParseXMLFactory;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.ErrorDialog;
import com.vividsolutions.jump.workbench.ui.HTMLFrame;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;

public class ExecuteServicePlugIn extends AbstractPlugIn implements	ThreadedPlugIn {
	
	/*String[] registeredGenerators = {
			"ch.unizh.geo.webgen.xml.ParamGeneratorINTEGER",
			"ch.unizh.geo.webgen.xml.ParamGeneratorDOUBLE",
			"ch.unizh.geo.webgen.xml.ParamGeneratorFeatureCollection"
			};;*/
	GenerateXMLFactory generatexmlfactory;
	/*String[] registeredParsers = {
			"ch.unizh.geo.webgen.xml.ParamParserINTEGER",
			"ch.unizh.geo.webgen.xml.ParamParserDOUBLE",
			"ch.unizh.geo.webgen.xml.ParamParserBOOLEAN",
			"ch.unizh.geo.webgen.xml.ParamParserSTRING",
			"ch.unizh.geo.webgen.xml.ParamParserFeatureCollection"
			};;*/
	
	private MultiInputDialog dialog;
	String errorhtml = null;
	String algorithm = null;
	String endpoint = null;
	//Vector allowedgeoms = null;
	Vector parametertypes = null;


	public ExecuteServicePlugIn() {
	}

	public void initialize(PlugInContext context) throws Exception {
		context.getFeatureInstaller().addMainMenuItem(this, "WebGen06",
				"WebGen Execute", null, null);
		initFactories();
	}
	
	public void initFactories() {
		GenerateXMLFactory.initialize();
        generatexmlfactory = GenerateXMLFactory.getInstance();
		ParseXMLFactory.initialize();
	}

	public boolean execute(PlugInContext context) throws Exception {
		initDialog(context);
		dialog.setVisible(true);
		if (!dialog.wasOKPressed()) return false;
		return true;
	}

	
	private void initDialog(PlugInContext context) {
		Blackboard bb = (Blackboard) context.getWorkbenchContext().getBlackboard().getProperties().get("com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn - BLACKBOARD");
		String ls = bb.get("LAST WEBGEN SERVICE").toString();
		System.out.println("selected method url: "+ls);
		
		ExecuteServiceInterface esi;
		try {
			esi = new ExecuteServiceInterface(context, new URL(ls));
			dialog = esi.dialog;
			algorithm = esi.algorithm;
			endpoint = esi.endpoint;
			parametertypes = esi.parametertypes;
		}
		catch (MalformedURLException e) {e.printStackTrace();}
		
		System.out.println("selected method name: "+algorithm);
	}

	
	public void run(TaskMonitor monitor, PlugInContext context)	throws Exception {
		String newLayerName = "";
		HashMap<String,Object> params = new HashMap<String,Object>();
		for (Iterator i = parametertypes.iterator(); i.hasNext();) {
	        ParameterType tmpt = (ParameterType) i.next();
	        if(tmpt.getIsLayer()) {
	        	try {
	        		params.put(tmpt.getName(), dialog.getLayer(tmpt.getName()).getFeatureCollectionWrapper().getUltimateWrappee());
	        		newLayerName = dialog.getLayer(tmpt.getName()).getName();
	        	} catch (Exception e) {
	        		//ErrorDialog.showMessageDialog(dialog, "Please select a Layer for: " + tmpt.getName());
	        		//return;
	        	}
	        }
	        else if(tmpt.getType() == Integer.class) {
	        	try {params.put(tmpt.getName(), new Integer(dialog.getInteger(tmpt.getName())));} catch (Exception e) {}
	        }
	        else if(tmpt.getType() == Double.class) {
	        	try {params.put(tmpt.getName(), new Double(dialog.getDouble(tmpt.getName())));} catch (Exception e) {}
	        }
	        else if(tmpt.getType() == Double[].class) {
	        	try {
	        		String[] values = dialog.getText(tmpt.getName()).split(",");
	        		Double[] items = new Double[values.length];
	        		for(int iv=0; iv<values.length; iv++) items[iv] = Double.parseDouble(values[iv]);
	        		params.put(tmpt.getName(), items);
	        	} catch (Exception e) {e.printStackTrace();}
	        }
	        else if(tmpt.getType() == Boolean.class) {
	        	try {params.put(tmpt.getName(), new Boolean(dialog.getBoolean(tmpt.getName())));} catch (Exception e) {}
	        }
	        else if(tmpt.getType() == String.class) {
	        	try {params.put(tmpt.getName(), dialog.getText(tmpt.getName()));} catch (Exception e) {}
	        }
		}
		
		//disable the JUMP default class loader while the WebGen libraries are called
		boolean noresults = true;
		Thread current = Thread.currentThread();
		ClassLoader oldLoader = current.getContextClassLoader();
		try {
			current.setContextClassLoader(getClass().getClassLoader());
				
			WebGenRequest wgreq = webgen(monitor, context, params, algorithm);
			HashMap result = wgreq.getFeatureCollections();
			for(int ir=0; ir < result.size(); ir++) {
				try {
					Object key = result.keySet().toArray()[ir];
					Object value = result.get(key);
					if(value instanceof String) {
						context.getWorkbenchFrame().warnUser(value.toString());
					}
					else if((value instanceof FeatureDataset) || (value instanceof ConstrainedFeatureCollection)) {
						FeatureCollection tmpcol = (FeatureCollection) value;
						context.addLayer(StandardCategoryNames.WORKING, newLayerName+" "+algorithm+" "+key.toString(), tmpcol);
					}
					noresults = false;
				}
				catch(Exception e) {System.out.println("result layer empty!");}
			}
			// !!!!!!!!!!!!!!!!!!!!!!!!!!! TODO Messages and Errors !!!!!!!!!!!!!!!!!!!!!!
			HashMap messages = wgreq.getMessages();
			for(int ir=0; ir < messages.size(); ir++) {
				Object key = messages.keySet().toArray()[ir];
				Object value = messages.get(key);
				context.getWorkbenchFrame().warnUser(key.toString() + ": " + value.toString());
				System.out.println(key.toString() + ": " + value.toString());
				noresults = false;
			}
			try {
				String error = wgreq.getMessage("error");
				noresults = false;
				ErrorDialog.show(context.getWorkbenchFrame(), "error", error, "error in service");
				this.errorhtml = messages.get("errorhtml").toString();
			}
			catch (Exception e) {}
			
			if(noresults) ErrorDialog.show(context.getWorkbenchFrame(), "error", "no results returned", "");;
			/*context.getWorkbenchFrame().getOutputFrame().createNewDocument();
			context.getWorkbenchFrame().getOutputFrame().addText("Message Key-Value Pairs:");
			for(int ikvp=0; ikvp < wgmessage.kvp.size(); ikvp++) {
				Object key = wgmessage.kvp.keySet().toArray()[ikvp];
				Object value = wgmessage.kvp.get(key);
				context.getWorkbenchFrame().getOutputFrame().addText(key + " = " + value + "");
			}*/
		}
		catch(Exception e) {
			System.out.println("results empty!");
			}
		finally {
			if(this.errorhtml != null) {
				HTMLFrame htmlframe = context.getWorkbenchFrame().getOutputFrame();
		    	htmlframe.createNewDocument();
		        htmlframe.append(this.errorhtml);
		        htmlframe.doLayout();
		    	htmlframe.surface();
		    	System.out.println(this.errorhtml);
			}
			current.setContextClassLoader(oldLoader);
		}
	}

	private WebGenRequest webgen(TaskMonitor monitor, PlugInContext context, HashMap<String,Object> params, String method) {
		monitor.allowCancellationRequests();
		System.out.println("Endpoint: "+endpoint+"\n");
		monitor.report("Executing Call ...");
		WebGenRequest wgreq = WebGenRequestExecuter.callService(params, endpoint, method);
		monitor.report("Response received!");
		return wgreq;
	}

}
