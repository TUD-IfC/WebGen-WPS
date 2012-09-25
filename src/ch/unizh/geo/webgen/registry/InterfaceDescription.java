package ch.unizh.geo.webgen.registry;

import java.util.Iterator;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

public class InterfaceDescription {
	
	public static final String protocolVersion = "1.0";

	public String name;
	public String author;
	public String category;
	public String endpoint;
	public String algorithm;
	public String description;
	public String version;
	public Vector<String> classification = new Vector<String>();
	public long lTimeoutSeconds = Long.MAX_VALUE;
	public boolean visible;
	public Object genontology;
	public Vector<ParameterDescription> inputParameters = new Vector<ParameterDescription>();
	public Vector<ParameterDescription> outputParameters = new Vector<ParameterDescription>();
	
	public InterfaceDescription(String name, String author, String category, 
								String endpoint, String algorithm, 
								String description, String version) {
		this.name = name;
		this.author = author;
		this.category = category;
		this.endpoint = endpoint;
		this.algorithm = algorithm;
		this.description = description;
		this.version = version;
		this.visible = false;
	}
	
	public InterfaceDescription(String name, String author, String category, 
			String endpoint, String algorithm, 
			String description, String version,
			String[] classification) {
		this(name, author, category, endpoint, algorithm, description, version);
		int i = 0;
		while (i < classification.length)
			this.classification.add(classification[i++]);
	}

	public InterfaceDescription(String name, String author, String category, 
			String endpoint, String algorithm, 
			String description, String version,
			String[] classification,
			long lTimeoutSeconds) {
		this(name, author, category, endpoint, algorithm, description, version, classification);
		this.lTimeoutSeconds = lTimeoutSeconds;
	}

	public void addInputParameter(String name, String type, String defaultvalue, String description) {
		inputParameters.add(new ParameterDescription(name, type, defaultvalue, description));
	}
	
	public void addInputParameter(String name, String type, double defaultvalue, double min, double max, String description) {
		inputParameters.add(new ParameterDescription(name, type, defaultvalue, min, max, description));
	}
	
	public void addInputParameter(String name, String type, int defaultvalue, int min, int max, String description) {
		inputParameters.add(new ParameterDescription(name, type, defaultvalue, min, max, description));
	}
	
	public void addInputParameter(String name, String type, AttributeDescription attribute, String description) {
		inputParameters.add(new ParameterDescription(name, type, attribute, description));
	}
	
	public void addInputParameter(String name, String type, Vector<AttributeDescription> attributes, String description) {
		inputParameters.add(new ParameterDescription(name, type, attributes, false, description));
	}
	
	public void addInputParameter(ParameterDescription pdesc) {
		inputParameters.add(pdesc);
	}
	
	public void addOutputParameter(String name, String type) {
		outputParameters.add(new ParameterDescription(name, type, ""));
	}
	
	public void addOutputParameter(ParameterDescription pdesc) {
		outputParameters.add(pdesc);
	}
	
	public void addOutputParameter(String name, String type, AttributeDescription attribute, String description) {
		outputParameters.add(new ParameterDescription(name, type, attribute, description));
	}

	public String generateXMLDescription() {
		return generateXMLDescriptionDocument(this.endpoint).asXML();
	}
	
	public String generateXMLDescription(String endpoint) {
		return generateXMLDescriptionDocument(endpoint).asXML();
	}
	
	public Document generateXMLDescriptionDocument(String endpoint) {
		Document document = DocumentHelper.createDocument();
		QName requestname = QName.get("Interface", "webgen", "http://www.webgen.org/webgen");
        Element root = document.addElement(requestname);
        Namespace webgenns = root.getNamespace();
        root.addAttribute("protocolVersion", protocolVersion);
        
		Element nameel = root.addElement(QName.get("name", webgenns));
		nameel.setText(this.name);
		Element authorel = root.addElement(QName.get("author", webgenns));
		authorel.setText(this.author);
		Element categoryel = root.addElement(QName.get("category", webgenns));
		categoryel.setText(this.category);
		Element endpointel = root.addElement(QName.get("endpoint", webgenns));
		endpointel.setText(endpoint);
		Element algorithmel = root.addElement(QName.get("algorithm", webgenns));
		algorithmel.setText(this.algorithm);
		Element descriptionel = root.addElement(QName.get("description", webgenns));
		descriptionel.setText(this.description);
		Element versionel = root.addElement(QName.get("version", webgenns));
		versionel.setText(this.version);
		
		Element inputParametersel = root.addElement(QName.get("InputParameters", webgenns));
		for(Iterator iter = inputParameters.iterator(); iter.hasNext();) {
			Element inputel = inputParametersel.addElement(QName.get("ParameterDescription", webgenns));
			ParameterDescription inputpd = (ParameterDescription) iter.next();
			inputpd.makeXMLDescription(inputel);
		}
		
		Element outputParametersel = root.addElement(QName.get("OutputParameters", webgenns));
		for(Iterator iter = outputParameters.iterator(); iter.hasNext();) {
			Element outputel = outputParametersel.addElement(QName.get("ParameterDescription", webgenns));
			ParameterDescription outputpd = (ParameterDescription) iter.next();
			outputpd.makeXMLDescription(outputel);
		}
        
        return document;
	}
	
	
	public InterfaceDescription(Document document) throws Exception {
		Element root = document.getRootElement();
        if(!root.getNamespacePrefix().equals("webgen"))
        	throw new Exception("Only webgen namespace is supported!");
        if(!root.getName().equals("Interface"))
        	throw new Exception("Only Interface elements are supported!");
        if(!root.attribute("protocolVersion").getValue().equals(protocolVersion))
        	throw new Exception("Only protocolVersion "+ protocolVersion + " supported!");
        
        this.name = root.elementText("name");
        this.author = root.elementText("author");
        this.category = root.elementText("category");
        this.endpoint = root.elementText("endpoint");
        this.algorithm = root.elementText("algorithm");
        this.description = root.elementText("description");
		this.version = root.elementText("version");
        
        Element inputparamel = root.element("InputParameters");
        for (Iterator il = inputparamel.elementIterator("ParameterDescription"); il.hasNext();) {
        	Element tmpp = (Element) il.next();
        	inputParameters.add(new ParameterDescription(tmpp));
        }
        
        Element outputparamel = root.element("OutputParameters");
        for (Iterator il = outputparamel.elementIterator("ParameterDescription"); il.hasNext();) {
        	Element tmpp = (Element) il.next();
        	outputParameters.add(new ParameterDescription(tmpp));
        }
	}
}
