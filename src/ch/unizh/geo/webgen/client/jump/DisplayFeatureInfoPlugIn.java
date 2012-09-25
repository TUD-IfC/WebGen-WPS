package ch.unizh.geo.webgen.client.jump;

import java.util.Iterator;

import ch.unizh.geo.webgen.model.ConstrainedFeature;
import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.model.ObservableFeatureCollection;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.HTMLFrame;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;

public class DisplayFeatureInfoPlugIn extends AbstractPlugIn implements ThreadedPlugIn {

	private MultiInputDialog dialog;
	
	public DisplayFeatureInfoPlugIn() {
    }

    public void initialize(PlugInContext context) throws Exception {
    	context.getFeatureInstaller().addMainMenuItem(
                this, "WebGen06", "Display FeatureInfo",
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
        dialog = new MultiInputDialog(context.getWorkbenchFrame(), "Display ConstrainedFeature", true);
        dialog.setSideBarDescription("Display ConstrainedFeature");
        dialog.addLayerComboBox("layer", context.getCandidateLayer(0), null, context.getLayerManager());
        GUIUtil.centreOnWindow(dialog);
    }

    public void run(TaskMonitor monitor, PlugInContext context)
        throws Exception {
    	ObservableFeatureCollection ofc = (ObservableFeatureCollection)dialog.getLayer("layer").getFeatureCollectionWrapper();
    	Object fc = ofc.getUltimateWrappee();
    	
    	HTMLFrame htmlframe = context.getWorkbenchFrame().getOutputFrame();
    	htmlframe.createNewDocument();

    	if(!(fc instanceof ConstrainedFeatureCollection)) {
    		htmlframe.append("<html><body><h1>Features in Layer are not WebGenFeatures!</h1></body></html>");
    		//htmlframe.surface();
    		htmlframe.setVisible(true);
    		return;
    	}
    	
    	htmlframe.append("<html><body><h1>WebGenFeatures in Layer: " +dialog.getLayer("layer").getName()+ "</h1><ul>");
    	ConstrainedFeatureCollection wgfc = (ConstrainedFeatureCollection)fc;
    	for(Iterator iter = wgfc.iterator(); iter.hasNext();) {
    		ConstrainedFeature wgfeat = (ConstrainedFeature) iter.next();
    		htmlframe.append("<li>Feature "+ wgfeat.getID() + 
    				"; Attributes: " + wgfeat.getAttributes().toString() + 
    				"; Constraint: " + wgfeat.getConstraint().toString() + 
    				"</li>");
    	}
    	htmlframe.append("</ul></body></html>");
    	//htmlframe.surface();
    	htmlframe.setVisible(true);
    }
}
