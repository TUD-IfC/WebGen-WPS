package ch.unizh.geo.webgen.client.jump;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;
import ch.unizh.geo.webgen.server.WebGenRequest;
import ch.unizh.geo.webgen.server.WebGenRequestExecuter;
import ch.unizh.geo.webgen.xml.GenerateXMLFactory;

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

public class LocalExecuteServicePlugIn extends AbstractPlugIn implements	ThreadedPlugIn {
	
	GenerateXMLFactory generatexmlfactory;
	private MultiInputDialog dialog;
	boolean newlocallayer = true;
	String errorhtml = null;
	String algorithm = null;
	String endpoint = null;
	Vector parametertypes = null;


	public LocalExecuteServicePlugIn() {
	}

	public void initialize(PlugInContext context) throws Exception {
		context.getFeatureInstaller().addMainMenuItem(this, "WebGen06",
				"Local Execute", null, null);
		//initFactories();
	}
	
	/*public void initFactories() {
		GenerateXMLFactory.initialize(ParserGeneratorRegistry.getRegisteredGenerators());
        generatexmlfactory = GenerateXMLFactory.getInstance();
		ParseXMLFactory.initialize(ParserGeneratorRegistry.getRegisteredParsers());
	}*/

	public boolean execute(PlugInContext context) throws Exception {
		initDialog(context);
		dialog.setVisible(true);
		if (!dialog.wasOKPressed()) return false;
		return true;
	}

	private void initDialog(PlugInContext context) {
		Blackboard bb = (Blackboard) context.getWorkbenchContext().getBlackboard().getProperties().get("com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn - BLACKBOARD");
		String ls = bb.get("LAST WEBGEN SERVICE LOCAL").toString();
		System.out.println("selected method name: "+ls);
		
		ExecuteServiceInterface esi = new ExecuteServiceInterface(context, ls);
		dialog = esi.dialog;
		algorithm = esi.algorithm;
		endpoint = esi.endpoint;
		parametertypes = esi.parametertypes;
		
		System.out.println("selected method name: "+algorithm);
	}

	public void run(TaskMonitor monitor, PlugInContext context)	throws Exception {
		String newLayerName = "";
		HashMap<String,Object> params = new HashMap<String,Object>();
		for (Iterator i = parametertypes.iterator(); i.hasNext();) {
	        ParameterType tmpt = (ParameterType) i.next();
	        if(tmpt.getIsLayer()) {
	        	try {
	        		//params.put(tmpt.getName(), dialog.getLayer(tmpt.getName()).getFeatureCollectionWrapper().getUltimateWrappee());
	        		ConstrainedFeatureCollection wgfc;
	        		FeatureCollection fc = dialog.getLayer(tmpt.getName()).getFeatureCollectionWrapper().getUltimateWrappee();
	        		if(fc instanceof ConstrainedFeatureCollection) {
	        			wgfc = ((ConstrainedFeatureCollection)fc).clone();
	        			newlocallayer = true;
	        		}
	        		else {
	        			wgfc = new ConstrainedFeatureCollection(fc, true);
	        			newlocallayer = true;
	        		}
	        		params.put(tmpt.getName(), wgfc);
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
		Thread current = Thread.currentThread();
		ClassLoader oldLoader = current.getContextClassLoader();
		try {
			current.setContextClassLoader(getClass().getClassLoader());
			
			//call service
			WebGenRequest wgreq = WebGenRequestExecuter.callService(params, "local", algorithm);
	        monitor.report("... finished!");
			
			
			HashMap result = wgreq.getResults();
			for(int ir=0; ir < result.size(); ir++) {
				try {
					Object key = result.keySet().toArray()[ir];
					Object value = result.get(key);
					if(value instanceof String) {
						context.getWorkbenchFrame().warnUser(value.toString());
					}
					else if(value instanceof FeatureDataset) {
						FeatureCollection tmpcol = (FeatureCollection) value;
						if(newlocallayer) context.addLayer(StandardCategoryNames.WORKING, newLayerName+" "+algorithm+" "+key.toString(), tmpcol);
					}
				}
				catch(Exception e) {System.out.println("result layer empty!");}
			}
			// !!!!!!!!!!!!!!!!!!!!!!!!!!! TODO Messages and Errors !!!!!!!!!!!!!!!!!!!!!!
			/*for(int im=0; im < wgreq.getMessages().; im++) {
				context.getWorkbenchFrame().warnUser(wgmessage.messages.get(im).toString());
			}
			context.getWorkbenchFrame().getOutputFrame().createNewDocument();
			context.getWorkbenchFrame().getOutputFrame().addText("Message Key-Value Pairs:");
			for(int ikvp=0; ikvp < wgmessage.kvp.size(); ikvp++) {
				Object key = wgmessage.kvp.keySet().toArray()[ikvp];
				Object value = wgmessage.kvp.get(key);
				context.getWorkbenchFrame().getOutputFrame().addText(key + " = " + value + "");
			}*/
		}
		catch(Exception e) {
			e.printStackTrace();
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
}
