package ch.unizh.geo.webgen.client.jump;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.registry.ParameterDescription;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;

public class ExecuteServiceInterface {
	
	PlugInContext context;
	public MultiInputDialog dialog;
	//Document document;
	
	public String algorithm;
	public String endpoint;
	public Vector<ParameterType> parametertypes;
	
	//for normal remote xml document
	public ExecuteServiceInterface(PlugInContext context, URL url) {
		this.context = context;
		try {
			Reader is = new InputStreamReader(url.openStream());
			SAXReader reader = new SAXReader();
			Document document = reader.read(is);
			InterfaceDescription idesc = new InterfaceDescription(document);
			initInterface(idesc);
		} catch (IOException e) {
			System.out.println("IOException: " + e);
		} catch (DocumentException e) {
			System.out.println("DocumentException: " + e);
		} catch (Exception e) {
			System.out.println("Exception: " + e);
			e.printStackTrace();
		}
	}
	
	//for local execution of algorithm
	public ExecuteServiceInterface(PlugInContext context, String algoclass) {
		this.context = context;
		try {
			String algorithmpath = "ch.unizh.geo.webgen.service." + algoclass;
			IWebGenAlgorithm algoobj = (IWebGenAlgorithm)Class.forName(algorithmpath).newInstance();
			initInterface(algoobj.getInterfaceDescription());
		} catch (InstantiationException e) {
			System.out.println("InstantiationException: " + e);
		} catch (ClassNotFoundException e) {
			System.out.println("ClassNotFoundException: " + e);
		} catch (IllegalAccessException e) {
			System.out.println("IllegalAccessException: " + e);
		}
	}
	
	private void initInterface(InterfaceDescription idesc) {
		try {
	        this.parametertypes = new Vector<ParameterType>();
	        this.algorithm = idesc.algorithm;
	        this.endpoint = idesc.endpoint;

	        dialog = new MultiInputDialog(context.getWorkbenchFrame(), "WebGen Dynamic - "+idesc.name, true);
	        dialog.setSideBarDescription(idesc.description);
	        
	        for(ParameterDescription pdesc : idesc.inputParameters) {
	        	if(pdesc.type.equals("FeatureCollection")) {
	        		dialog.addLayerComboBox(pdesc.name, context.getSelectedLayer(0), null, context.getLayerManager());
		        	parametertypes.add(new ParameterType(pdesc.name, FeatureCollection.class, true, pdesc.supportedvalues));
		        	/*for(AttributeDescription adesc : pdesc.attributes) {
		        		//noch auszufuellen ...
		        	}*/
	        	}
	        	else if(pdesc.type.equals("ConstrainedFeatureCollection")) {
	        		dialog.addLayerComboBox(pdesc.name, context.getSelectedLayer(0), null, context.getLayerManager());
		        	parametertypes.add(new ParameterType(pdesc.name, FeatureCollection.class, true, pdesc.supportedvalues));
		        	/*for(AttributeDescription adesc : pdesc.attributes) {
		        		//noch auszufuellen ...
		        	}*/
	        	}
	        	else if(pdesc.type.equals("GEOMETRY")) {
	        		dialog.addLayerComboBox(pdesc.name, null, null, context.getLayerManager());
	        		parametertypes.add(new ParameterType(pdesc.name, Geometry.class, false, pdesc.supportedvalues));
	        	}
	        	else if(pdesc.type.equals("INTEGER")) {
	        		int di = 0;
	        		try {di = Integer.parseInt(pdesc.defaultvalue);} catch (Exception e) {}
	        		if(pdesc.hasChoices()) {
	        			dialog.addComboBox(pdesc.name, di, pdesc.getChoices(), pdesc.description);
	        		}
	        		else {
	        			dialog.addIntegerField(pdesc.name, di, 5, pdesc.description);
	        		}
	        		parametertypes.add(new ParameterType(pdesc.name, Integer.class, false, pdesc.supportedvalues));
	        	}
	        	else if(pdesc.type.equals("DOUBLE")) {
	        		double dd = 0.0;
	        		if(pdesc.defaultvalue!=null) dd = Double.parseDouble(pdesc.defaultvalue);
	        		if(pdesc.hasChoices()) {
	        			dialog.addComboBox(pdesc.name, dd, pdesc.getChoices(), pdesc.description);
	        		}
	        		else {
	        			dialog.addDoubleField(pdesc.name, dd, 5, pdesc.description);
	        		}
	        		parametertypes.add(new ParameterType(pdesc.name, Double.class, false, pdesc.supportedvalues));
	        	}
	        	else if(pdesc.type.equals("DOUBLEARRAY")) {
	        		if(pdesc.defaultvalue==null) pdesc.defaultvalue = "";
	        		if(pdesc.hasChoices()) {
	        			dialog.addComboBox(pdesc.name, pdesc.defaultvalue, pdesc.getChoices(), pdesc.description);
	        		}
	        		else {
	        			dialog.addTextField(pdesc.name, pdesc.defaultvalue, 30, null, pdesc.description + " (separate by ,)");
	        		}
	        		parametertypes.add(new ParameterType(pdesc.name, Double[].class, false, pdesc.supportedvalues));
	        	}
	        	else if(pdesc.type.equals("BOOLEAN")) {
	        		boolean db = false;
	        		if(pdesc.defaultvalue!=null) db = Boolean.parseBoolean(pdesc.defaultvalue);
	        		dialog.addCheckBox(pdesc.name, db, pdesc.description);
	        		parametertypes.add(new ParameterType(pdesc.name, Boolean.class, false, pdesc.supportedvalues));
	        	}
	        	else {
	        		if(pdesc.defaultvalue==null) pdesc.defaultvalue = "";
	        		if(pdesc.hasChoices()) {
	        			dialog.addComboBox(pdesc.name, pdesc.defaultvalue, pdesc.getChoices(), pdesc.description);
	        		}
	        		else {
	        			dialog.addTextField(pdesc.name, pdesc.defaultvalue, 30, null, pdesc.description);
	        		}
	        		parametertypes.add(new ParameterType(pdesc.name, String.class, false, pdesc.supportedvalues));
	        	}
            }
	        	        
	        GUIUtil.centreOnWindow(dialog);
		} catch (IndexOutOfBoundsException e) {
			System.out.println("IndexOutOfBoundsException: " + e);
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
	}
	
	/*private void initInterface() {
		try {
	        Element root = document.getRootElement();
	        
	        //check root element
	        if(!root.getNamespacePrefix().equals("webgen"))
	        	throw new Exception("Only webgen namespace is supported!");
	        if(!root.getName().equals("Interface"))
	        	throw new Exception("Only Interface elements are supported!");
	        
	        parametertypes = new Vector<ParameterType>();
	        
	        String name = root.elementText("name");
	        endpoint = root.elementText("endpoint");
	        algorithm = root.elementText("algorithm");
	        String description = root.elementText("description");

	        dialog = new MultiInputDialog(context.getWorkbenchFrame(), "WebGen Dynamic - "+name, true);
	        dialog.setSideBarDescription(description);
	        
	        String pname, ptype, pdefault, pdesc;
	        Element tmpp;
	        Element inputParameters = root.element("InputParameters");
	        for (Iterator il = inputParameters.elementIterator("ParameterDescription"); il.hasNext();) {
	        	tmpp = (Element) il.next();
	        	pname = tmpp.elementText("name");
	        	ptype = tmpp.elementText("type");
	        	pdefault = tmpp.elementText("default");
	        	pdesc = tmpp.elementText("description");
	        	
	        	if(ptype.equals("FeatureCollection")) {
	        		dialog.addLayerComboBox(pname, context.getSelectedLayer(0), null, context.getLayerManager());
	        		ParameterType pt = new ParameterType(pname, FeatureCollection.class, true);
	        		parseSupportedValues(tmpp.element("supportedvalues"), pt);
		        	parametertypes.add(pt);
		        	for(Iterator iter = tmpp.elementIterator("attribute"); iter.hasNext();) {
		        		Element tatt = (Element) iter.next();
		        		String tattname = tatt.elementText("name");
		        		String tatttype = tatt.elementText("type");
		        	}
		        	/*for(Iterator iter = tmpp.elementIterator("allowed"); iter.hasNext();) {
		        		Element tall = (Element) iter.next();
		        		String allowed = tall.getText();
		        		if(allowed.equals("gml:Point")) allowedgeoms.add(Point.class);
		        		else if(allowed.equals("gml:MultiPoint")) allowedgeoms.add(MultiPoint.class);
		        		else if(allowed.equals("gml:LineString")) allowedgeoms.add(LineString.class);
		        		else if(allowed.equals("gml:MultiLineString")) allowedgeoms.add(MultiLineString.class);
		        		else if(allowed.equals("gml:Polygon")) allowedgeoms.add(Polygon.class);
		        		else if(allowed.equals("gml:MultiPolygon")) allowedgeoms.add(MultiPolygon.class);
		        	}*/
	        	/*}
	        	else if(ptype.equals("GEOMETRY")) {
	        		dialog.addLayerComboBox(pname, null, null, context.getLayerManager());
	        		parametertypes.add(new ParameterType(pname, Geometry.class));
	        	}
	        	else if(ptype.equals("INTEGER")) {
	        		int di = 0;
	        		if(pdefault!=null) di = Integer.parseInt(pdefault);
	        		dialog.addIntegerField(pname, di, 5, pdesc);
	        		parametertypes.add(new ParameterType(pname, Integer.class));
	        	}
	        	else if(ptype.equals("DOUBLE")) {
	        		double dd = 0.0;
	        		if(pdefault!=null) dd = Double.parseDouble(pdefault);
	        		dialog.addDoubleField(pname, dd, 5, pdesc);
	        		parametertypes.add(new ParameterType(pname, Double.class));
	        	}
	        	else if(ptype.equals("BOOLEAN")) {
	        		boolean db = false;
	        		if(pdefault!=null) db = Boolean.getBoolean(pdefault);
	        		dialog.addCheckBox(pname, db, pdesc);
	        		parametertypes.add(new ParameterType(pname, Boolean.class));
	        	}
	        	else if(ptype.equals("STRINGCHOICE")) {
	        		List<String> options = new ArrayList<String>();
	        		for(Iterator iter = tmpp.elementIterator("item"); iter.hasNext();) {
	        			options.add(((Element)iter.next()).getText());
		        	}
	        		dialog.addComboBox(pname, pdefault, options, pdesc);
	        		parametertypes.add(new ParameterType(pname, String.class));
	        	}
	        	else {
	        		if(pdefault==null) pdefault = "";
	        		dialog.addTextField(pname, pdefault, 30, null, pdesc);
	        		parametertypes.add(new ParameterType(pname, String.class));
	        	}
            }
	        	        
	        GUIUtil.centreOnWindow(dialog);
		} catch (MalformedURLException e) {
			System.out.println("MalformedURLException: " + e);
		} catch (IndexOutOfBoundsException e) {
			System.out.println("IndexOutOfBoundsException: " + e);
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
	}*/
	
}
