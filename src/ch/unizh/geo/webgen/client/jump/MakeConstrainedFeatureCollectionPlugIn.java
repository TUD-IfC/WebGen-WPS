package ch.unizh.geo.webgen.client.jump;

import ch.unizh.geo.webgen.model.ConstrainedFeatureCollection;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.ObservableFeatureCollection;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;

public class MakeConstrainedFeatureCollectionPlugIn extends AbstractPlugIn implements ThreadedPlugIn {

	private MultiInputDialog dialog;
	
	public MakeConstrainedFeatureCollectionPlugIn() {
    }

    public void initialize(PlugInContext context) throws Exception {
    	context.getFeatureInstaller().addMainMenuItem(
                this, "WebGen06", "Make Constrained Features",
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
        dialog = new MultiInputDialog(context.getWorkbenchFrame(), "Make Constrained Features", true);
        dialog.setSideBarDescription("Make Constrained Features");
        dialog.addLayerComboBox("layer", context.getCandidateLayer(0), null, context.getLayerManager());
        GUIUtil.centreOnWindow(dialog);
    }

    public void run(TaskMonitor monitor, PlugInContext context)
        throws Exception {
    	Layer layer = dialog.getLayer("layer");
    	ObservableFeatureCollection ofc = (ObservableFeatureCollection)layer.getFeatureCollectionWrapper();
    	FeatureCollection fc = ofc.getUltimateWrappee();
    	ConstrainedFeatureCollection cfc = new ConstrainedFeatureCollection(fc, true);
    	context.addLayer(StandardCategoryNames.WORKING, layer.getName()+" constrained", cfc);
    }
}
