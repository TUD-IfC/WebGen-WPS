package ch.unizh.geo.webgen.test;

import java.util.Collections;
import java.util.Iterator;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.ShapefileWriter;
import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;

public class MakeShpFromFeatures extends AbstractPlugIn implements ThreadedPlugIn {
	
	private MultiInputDialog dialog;
	long timeAverage;
	
	public MakeShpFromFeatures() {
    }

    public void initialize(PlugInContext context) throws Exception {
    	context.getFeatureInstaller().addMainMenuItem(
                this, "Moritz", "Make Shapefiles from Features",
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
        dialog = new MultiInputDialog(context.getWorkbenchFrame(), "MakeShpFromFeatures", true);
        dialog.setSideBarDescription("MakeShpFromFeatures");
        dialog.addLayerComboBox("layer", context.getCandidateLayer(0), null, context.getLayerManager());
        GUIUtil.centreOnWindow(dialog);
    }

    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
    	FeatureCollection fc = dialog.getLayer("layer").getFeatureCollectionWrapper();
    	
    	DriverProperties wproperties = new DriverProperties();
    	FeatureCollection fct;
    	FeatureSchema fcs = fc.getFeatureSchema();
    	Feature f;
    	for(Iterator iter = fc.iterator(); iter.hasNext();) {
    		f = (Feature)iter.next();
    		wproperties.putAll(Collections.singletonMap(DataSource.FILE_KEY, "c:\\java\\mittelland\\mittelland-"+f.getAttribute("Id")+"-"+f.getAttribute("Nr_Frage")+".shp"));
    		fct = new FeatureDataset(fcs);
    		fct.add(f);
    		ShapefileWriter writer = new ShapefileWriter();
    		writer.write(fct, wproperties);
    	}
    }
    
}