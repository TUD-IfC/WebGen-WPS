package ch.unizh.geo.webgen.client.jump;

import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public class WebGen4JumpExtension extends Extension {
	public void configure(PlugInContext context) throws Exception {
		System.out.println("Loading MakeConstrainedFeatureCollectionPlugIn ...");
		new MakeConstrainedFeatureCollectionPlugIn().initialize(context);
		System.out.println("Loading ListServicesPlugIn ...");
		new ListServicesPlugIn().initialize(context);
		System.out.println("Loading ExecuteServicePlugIn ...");
		new ExecuteServicePlugIn().initialize(context);
		System.out.println("Loading DisplayFeatureInfoPlugIn ...");
		new DisplayFeatureInfoPlugIn().initialize(context);
		System.out.println("Loading ConstraintHistoryPlugIn ...");
		new ConstraintHistoryPlugIn().initialize(context);
		System.out.println("Loading WebGen completed!");
	}
}