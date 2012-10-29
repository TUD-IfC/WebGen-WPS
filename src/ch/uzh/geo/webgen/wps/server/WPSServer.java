package ch.uzh.geo.webgen.wps.server;
		
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;

import ica.wps.common.IWPSXmlObjectParser;
import ica.wps.data.WPSFeatureSchemaDescription;
import ica.wps.data.WPSOperatorDescription;
import ica.wps.data.WPSServerDescription;
import ica.wps.data.WPSFeatureSchemaDescription.DatatypeAttribute;
import ica.wps.data.WPSOperatorDescription.DatatypeComplex;
import ica.wps.data.WPSOperatorDescription.DatatypeLiteral;
import ica.wps.data.WPSOperatorDescription.Range;
import ica.wps.server.IWPSServer;
import ica.wps.server.IWPSOperator;
import ica.wps.server.WPSServlet;

import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.registry.ParameterDescription;
import ch.unizh.geo.webgen.registry.Interval;

/**
 * Implementation of the WPS server interface for a WebGen server.
 * @author	M. Wittensoeldner
 * @date	Created on 05.02.2007
 * Modified Version for open access to the source code
 * @author	R. Klammer
 * @date	Created on 24.09.2012
 */
public class WPSServer implements IWPSServer {
	
	
	/**
	 * The package where the WPSServer looks for Operators.
	 */
	protected String WPS_OPERATOR_PACKAGE = "ch/unizh/geo/webgen/service/";
	protected ClassLoader			_classLoader;
	protected IWPSXmlObjectParser	_parser = new WPSXmlObjectParser();
	
	/**
	 * Initializes the server.
	 * @return void
	 * @param classLoader						The class loader.
	 */
	public void init(ClassLoader classLoader) {
		_classLoader = classLoader;
	}

	/**
	 * Gets the server description
	 * @return WPSServerDescription				The server description.
	 */
	public WPSServerDescription getServerDescription() {
		try {
			return new WPSServerDescription("WebGen WPS Server", "WPS server provides executing Web Generalisation operators.",
				WPSServlet.getProperties().getProperty("admin.institution"),
				WPSServlet.getProperties().getProperty("admin.server"),
				WPSServlet.getProperties().getProperty("admin.conmail"),
				WPSServlet.getProperties().getProperty("admin.contact"),
				new String[] {"WPS", "WebGen"});
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to read 'wps-server.properties'");
			return null;
		}		
	}
	

	/**
	 * Gets the xml parser.
	 * @return IWPSXmlObjectParser				The xml parser.
	 */
	public IWPSXmlObjectParser getXmlParser() {
		return _parser;
	}

	/**
	 * Gets the WPS operators.
	 * @return List<IWPSOperator>				The WPS operators.
	 */
	public List<IWPSOperator> getOperators() {
		int k;
		int j;
		Vector<ParameterDescription> arrParams;
		ParameterDescription param;
		LinkedList<IWPSOperator> lstOperators = new LinkedList<IWPSOperator>();
		String sResourceBase = WPS_OPERATOR_PACKAGE;
		URL urlServices = _classLoader.getResource(sResourceBase);
		File dirServices = new File(urlServices.getFile());
		String[] arrFiles = dirServices.list();
		sResourceBase = sResourceBase.replace('/', '.');
		Arrays.sort(arrFiles);
		System.out.println("sha debug, WPSServer.getOperators(), Operators="+arrFiles.length);
		int i = 0;
		while (i < arrFiles.length) {
			if (arrFiles[i].lastIndexOf('.') > -1)
				arrFiles[i] = arrFiles[i].substring(0, arrFiles[i].lastIndexOf('.'));
//			System.out.println("sha debug, WPSServer.getOperators(), arrFiles["+i+"]="+arrFiles[i]);
			try {
				Object obj = _classLoader.loadClass(sResourceBase+arrFiles[i]).newInstance();
				if ((obj != null) && (obj instanceof IWebGenAlgorithm)) {
					IWebGenAlgorithm algo = (IWebGenAlgorithm)obj;
					InterfaceDescription desc = algo.getInterfaceDescription();
					// do only show capabilities of visible algorithms
					if (desc.visible) {
						WPSOperatorDescription opDescription = new WPSOperatorDescription(desc.version, sResourceBase+arrFiles[i], desc.name, desc.description, false, desc.lTimeoutSeconds);
						k = 0;
						while (k < desc.classification.size())
							opDescription.addClassification(desc.classification.get(k++));
						k = 0;
						while (k < 2) {
							if (k == 0)
								arrParams = desc.inputParameters;
							else
								arrParams = desc.outputParameters;
							j = 0;
							while (j < arrParams.size()) {
								param = arrParams.get(j);
								this.addParameter(param, opDescription, k == 0);
								j++;
							}
							k++;
						}
						lstOperators.add(new WPSOperator(opDescription, _classLoader)); 
					}
				}
			} catch (Exception ex) {
				if (arrFiles[i].equals("JTSDifference")) ex.printStackTrace();
			} catch (Error er) {
				if (arrFiles[i].equals("JTSDifference")) er.printStackTrace();
			}
			i++;
		}
		
		
		if (lstOperators.size() == 0)
			return null;
		System.out.println("sha debug, WPSServer.getOperators(), visible Operators="+lstOperators.size());
		return lstOperators;
	}

	/**
	 * Adds a WebGen parameter at the wps operator description.
	 * @return void
	 * @param param								The WebGen parameter.
	 * @param opDescription						The WPS operator description.
	 * @param bInput							True whether the parameter is an input parameter.
	 * @throws Exception						When the parameter cannot be matched.
	 */
	protected void addParameter(ParameterDescription param, WPSOperatorDescription opDescription, boolean bInput) throws Exception {
		int i;
		int k;
		String sIdentifier = param.name;
		String sDescription = param.description;
		Object objDefaultValue = param.defaultvalue;
		if ((param.defaultvalue != null) && (param.defaultvalue.trim().length() == 0))
			objDefaultValue = null;
		if (objDefaultValue != null)
			objDefaultValue = this.convertValue(param.type, param.defaultvalue);
		Range[] arrAllowedRanges = null;
		Object[] arrAllowedValues = null;
		
		
		// determine the parameter type
		DatatypeLiteral typeLiteral = null;
		DatatypeComplex typeComplex = null;
		if (param.type.equals("INTEGER")) {
			typeLiteral = DatatypeLiteral.Integer;
		} else if (param.type.equals("DOUBLE")) {
			typeLiteral = DatatypeLiteral.Double;
		} else if (param.type.equals("BOOLEAN")) {
			typeLiteral = DatatypeLiteral.Boolean;
		} else if (param.type.equals("STRING")) {
			typeLiteral = DatatypeLiteral.String;
		} else if (param.type.equals("FeatureCollection")) {
			typeComplex = DatatypeComplex.FeatureCollection;
		}
		if (typeLiteral != null) {
			if (param.supportedvalues.size() > 0) {
				ArrayList<Object> lstAllowedValues = new ArrayList<Object>();
				ArrayList<Range> lstAllowedRanges = new ArrayList<Range>();
				i = 0;
				while (i < param.supportedvalues.size()) {
					if (param.supportedvalues.get(i) instanceof Interval) {
						lstAllowedRanges.add(opDescription.new Range(this.convertValue(param.type, ((Interval)param.supportedvalues.get(i)).getMinString()), this.convertValue(param.type, ((Interval)param.supportedvalues.get(i)).getMaxString())));
					} else {
						lstAllowedValues.add(this.convertValue(param.type, param.supportedvalues.get(i).toString()));
					}
					i++;
				}
				if (lstAllowedRanges.size() > 0) {
					Object[] arrObj = lstAllowedRanges.toArray();
					arrAllowedRanges = new Range[arrObj.length];
					i = 0;
					while (i < arrObj.length)
						arrAllowedRanges[i] = (Range)arrObj[i++];
				}
				if (lstAllowedValues.size() > 0)
					arrAllowedValues = lstAllowedValues.toArray();
			}
			if (bInput)
				if (arrAllowedRanges != null)
					opDescription.addDescriptionParameterIn(sIdentifier, sIdentifier, sDescription, null, 1, 1, typeLiteral, objDefaultValue, arrAllowedRanges);
				else
					opDescription.addDescriptionParameterIn(sIdentifier, sIdentifier, sDescription, null, 1, 1, typeLiteral, objDefaultValue, arrAllowedValues);
			else
				if (arrAllowedRanges != null)
					opDescription.addDescriptionParameterOut(sIdentifier, sIdentifier, sDescription, null, typeLiteral);
				else
					opDescription.addDescriptionParameterOut(sIdentifier, sIdentifier, sDescription, null, typeLiteral);
		} else {
			LinkedList<WPSFeatureSchemaDescription> lstSchemaDescription = new LinkedList<WPSFeatureSchemaDescription>();
			LinkedList<WPSFeatureSchemaDescription> lstTemp = new LinkedList<WPSFeatureSchemaDescription>();
			if (param.attributes.size() > 0) {
				lstSchemaDescription.add(new WPSFeatureSchemaDescription());
				i = 0;
				while (i < param.attributes.size()) {
					DatatypeAttribute[] arrType = null;
					if (param.attributes.get(i).type.equals("GEOMETRY")) {
						arrType = new DatatypeAttribute[param.attributes.get(i).supportedvalues.size()];
						if (arrType.length > 0) {
							k = 0;
							while (k < param.attributes.get(i).supportedvalues.size()) {
								arrType[k] = this.convertAttributeType(param.attributes.get(i).supportedvalues.get(k).toString());
								k++;
							}
						} else {
							arrType = new DatatypeAttribute[] {DatatypeAttribute.Point, DatatypeAttribute.LineString, DatatypeAttribute.Polygon, DatatypeAttribute.MultiPoint, DatatypeAttribute.MultiLineString, DatatypeAttribute.MultiPolygon, DatatypeAttribute.MultiGeometry};
						}
						
					} else {
						arrType = new DatatypeAttribute[] {this.convertAttributeType(param.attributes.get(i).type)};
					}
					lstTemp.clear();
					Iterator<WPSFeatureSchemaDescription> iterDesc = lstSchemaDescription.iterator();
					WPSFeatureSchemaDescription desc;
					WPSFeatureSchemaDescription descNew;
					while (iterDesc.hasNext()) {
						desc = iterDesc.next();
						k = 0;
						while (k < arrType.length) {
							descNew = desc.clone();
							descNew.addFeatureAttributeDescription(param.attributes.get(i).name, arrType[k]);
							lstTemp.add(descNew);
							k++;
						}
					}
					lstSchemaDescription.clear();
					lstSchemaDescription.addAll(lstTemp);
					i++;
				}
			} else {
				// ?? TODO: test
				throw new Exception(opDescription.getIdentifier()+"."+param.name+": featurecollection without attributes is not supported.");
			}
			WPSFeatureSchemaDescription[] arrAllowedSchemaDescriptions = null;
			if (lstSchemaDescription.size() > 0) {
				Iterator<WPSFeatureSchemaDescription> iterSchema = lstSchemaDescription.iterator();
				arrAllowedSchemaDescriptions = new WPSFeatureSchemaDescription[lstSchemaDescription.size()];
				i = 0;
				while (iterSchema.hasNext()) {
					arrAllowedSchemaDescriptions[i] = iterSchema.next();
					i++;
				}
			}
			if (bInput)
				opDescription.addDescriptionParameterIn(sIdentifier, sIdentifier, sDescription, 1, 1, 50L, typeComplex, arrAllowedSchemaDescriptions);
			else
				opDescription.addDescriptionParameterOut(sIdentifier, sIdentifier, sDescription, typeComplex, arrAllowedSchemaDescriptions);
		}
	}

	/**
	 * String to Object conversion using the correct object type.
	 * @return Object							The converted object.
	 * @param sType								The object type.
	 * @param sValue							The value to convert.
	 * @throws Exception						When an error occurs.
	 */
	protected Object convertValue(String sType, String sValue) throws Exception {
		if (sType.equals("INTEGER")) {
			return new Integer(sValue);
		} else if (sType.equals("DOUBLE")) {
			return new Double(sValue);
		} else if (sType.equals("BOOLEAN")) {
			return new Boolean(sValue);
		} else if (sType.equals("STRING")) {
			return sValue;
		} else {
			throw new Exception("Type '"+sType+"' not found. Conversion failed.");
		}
	}

	/**
	 * String to DatatypeAttribute conversion using the correct data type.
	 * @return DatatypeAttribute				The attribute type.
	 * @param sType								The string type.
	 * @throws Exception						When an error occurs.
	 */
	protected DatatypeAttribute convertAttributeType(String sType) throws Exception {
		if (sType.equals("Point")) {
			return DatatypeAttribute.Point;
		} else if (sType.equals("LineString")) {
			return DatatypeAttribute.LineString;
		} else if (sType.equals("Polygon")) {
			return DatatypeAttribute.Polygon;
		} else if (sType.equals("MultiPoint")) {
			return DatatypeAttribute.MultiPoint;
		} else if (sType.equals("MultiLineString")) {
			return DatatypeAttribute.MultiLineString;
		} else if (sType.equals("MultiPolygon")) {
			return DatatypeAttribute.MultiPolygon;
		} else if (sType.equals("INTEGER")) {
			return DatatypeAttribute.Integer;
		} else if (sType.equals("DOUBLE")) {
			return DatatypeAttribute.Double;
		} else if (sType.equals("BOOLEAN")) {
			return DatatypeAttribute.Boolean;
		} else if (sType.equals("STRING")) {
			return DatatypeAttribute.String;
		} else {
			throw new Exception("Attribute type '"+sType+"' not found. Conversion failed.");
		}
	}
}
