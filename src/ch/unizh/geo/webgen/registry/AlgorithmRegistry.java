package ch.unizh.geo.webgen.registry;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

import ch.unizh.geo.webgen.server.IWebGenAlgorithm;

public class AlgorithmRegistry {
	
	public static final String protocolVersion = "1.0";
	
	String[] algorithms;
	Element[] services;
	Document document;
	boolean showAll;
	
	public AlgorithmRegistry(String[] algorithms, boolean showAll) {
		this.algorithms = algorithms;
		this.showAll = showAll;
		services = new Element[algorithms.length];
		initXMLList();
	}
	
	private void initXMLList() {
		document = DocumentHelper.createDocument();
		Element root = document.addElement(QName.get("Registry", "webgen", "http://www.webgen.org/webgen"));
        Namespace webgenns = root.getNamespace();
        root.addAttribute("protocolVersion", protocolVersion);
        
        String algorithmpath;
        IWebGenAlgorithm algorithm;
        InterfaceDescription ainterface;
        Element serviceel, idel, nameel, ownerel, categoryel, descriptionel, versionel, visibleel, urlel;
        for(int i=0; i<algorithms.length; i++) {
        	try {
        		algorithmpath = "ch.unizh.geo.webgen.service." + algorithms[i];
            	algorithm = (IWebGenAlgorithm)Class.forName(algorithmpath).newInstance();
            	ainterface = algorithm.getInterfaceDescription();
            	
            	if(ainterface.visible || showAll) {
            		serviceel = root.addElement(QName.get("Service", webgenns));
            		idel = serviceel.addElement(QName.get("id", webgenns));
                	idel.setText(ainterface.algorithm+ainterface.version);
            		nameel = serviceel.addElement(QName.get("name", webgenns));
            		nameel.setText(ainterface.algorithm);
            		ownerel = serviceel.addElement(QName.get("owner", webgenns));
            		ownerel.setText(ainterface.author);
            		categoryel = serviceel.addElement(QName.get("category", webgenns));
            		categoryel.setText(ainterface.category);
            		descriptionel = serviceel.addElement(QName.get("description", webgenns));
            		descriptionel.setText(ainterface.description);
            		versionel = serviceel.addElement(QName.get("version", webgenns));
            		versionel.setText(ainterface.version);
            		//visibleel = serviceel.addElement(QName.get("visible", webgenns));
            		//visibleel.setText(""+ainterface.visible);
                	
            		urlel = serviceel.addElement(QName.get("url", webgenns));        		
                	services[i] = urlel;
            	}
        	}
        	catch (Exception e) {}
        }
	}
	
	public Document getDocument() {
		return document;
	}
	
	public String generateXMLList(String interfaceURL) {
		for(int i=0; i<services.length; i++) {
			try {
				services[i].setText(interfaceURL + "?algorithm=" + algorithms[i]);
			}
			catch (NullPointerException e) {}
		}
        return document.asXML();
	}
}
