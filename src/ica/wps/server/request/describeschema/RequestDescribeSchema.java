package ica.wps.server.request.describeschema;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.net.URL;
import java.io.File;

import ica.wps.data.WPSFeatureAttribute;
import ica.wps.data.WPSFeatureCollectionAttribute;
import ica.wps.data.WPSFeatureSchemaDescription;
import ica.wps.data.WPSOperatorDescriptionParameter;
import ica.wps.data.WPSOperatorDescriptionParameterComplex;
import ica.wps.data.WPSSchemaAttribute;
import ica.wps.data.WPSFeatureSchemaDescription.DatatypeAttribute;
import ica.wps.data.WPSFeatureSchemaDescription.DatatypeAttributeExt;
import ica.wps.data.WPSOperatorDescription.DatatypeComplex;
import ica.wps.server.ExceptionCode;
import ica.wps.server.ServerException;
import ica.wps.server.WPSServlet;
import ica.wps.server.IWPSOperator;
import ica.wps.server.request.Request;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelElement;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelSimpleType;
import org.apache.xmlbeans.impl.xb.xsdschema.LocalElement;
import org.apache.xmlbeans.impl.xb.xsdschema.NoFixedFacet;
import org.apache.xmlbeans.impl.xb.xsdschema.ExplicitGroup;
import javax.xml.namespace.QName;

/**
 * Request class handles the DescribeSchema request.
 * @author	M. Wittensoeldner
 * @date	Created on 07.02.2007
 */
public abstract class RequestDescribeSchema extends Request {

	protected static final String		PARAM_OPERATOR = "operator";
	protected static final String		PARAM_INPUT = "input";
	protected static final String		PARAM_PARAMETER = "parameter";

	protected String					_sOperatorIdentifier;	
	protected String					_sParameterIdentifier;
	protected boolean					_bInput;

	/**
	 * Handles the request. This method is called after parseRequest().
	 * @return void
	 * @return XmlObject				The resulted xml document.
	 * @throws ServerException			When an error occurs.
	 */
	protected XmlObject handleRequest() throws ServerException {
		if (_sOperatorIdentifier == null)
			throw new ServerException(ExceptionCode.MissingParameterValue, PARAM_OPERATOR, "Parameter '"+PARAM_OPERATOR+"' is not set.");
		else if (_sParameterIdentifier == null)
			throw new ServerException(ExceptionCode.MissingParameterValue, PARAM_PARAMETER, "Parameter '"+PARAM_PARAMETER+"' is not set.");

		IWPSOperator op = WPSServlet.getCapabilities().getOperator(_sOperatorIdentifier);
		if (op != null) {
			int nIndex = 0;
			String sParam = _sParameterIdentifier;
			if (sParam.lastIndexOf('.') > 0) {
				sParam = sParam.substring(0, sParam.lastIndexOf('.'));
				try {
					nIndex = Integer.parseInt(_sParameterIdentifier.substring(_sParameterIdentifier.lastIndexOf('.')+1));
				} catch (Exception ex) {
					throw new ServerException(ExceptionCode.NoApplicableCode, "", "Parsing parameter index of '"+_sParameterIdentifier+"' failed.");
				}
			}
			Iterator<WPSOperatorDescriptionParameter> iterParams = op.getOperatorDescription().getParameters(_bInput).iterator();
			WPSOperatorDescriptionParameter param = null;
			while (iterParams.hasNext()) {
				param = iterParams.next();
				if (param.getIdentifier().equals(sParam)) {
					break;
				}
				param = null;
			}
			if (param != null) {
				if (param instanceof WPSOperatorDescriptionParameterComplex) {
					WPSOperatorDescriptionParameterComplex paramComplex = (WPSOperatorDescriptionParameterComplex)param;
					try {
						SchemaDocument doc = RequestDescribeSchema.getSchemaDocument(paramComplex, nIndex);
						return doc;
					} catch (Exception ex) {
						throw new ServerException(ExceptionCode.NoApplicableCode, "", "Generating schema file for '"+_sOperatorIdentifier+"' failed: " + ex.getMessage());
					}
				} else {
					throw new ServerException(ExceptionCode.InvalidParameterValue, _sParameterIdentifier, "Parameter '"+_sParameterIdentifier+"' is not a complex parameter.");
				}
			} else {
				throw new ServerException(ExceptionCode.InvalidParameterValue, _sParameterIdentifier, "Parameter '"+_sParameterIdentifier+"' not available.");
			}
		} else {
			throw new ServerException(ExceptionCode.OperationNotSupported, _sOperatorIdentifier, "Operator '"+_sOperatorIdentifier+"' not found.");
		}
	}
	
	/**
	 * Parses an attribute type
	 * @return String					The schema attribute name.
	 * @param doc						The schema document.
	 * @param type						The attribute type.
	 */
	protected static String parseAttriubteDatatype(SchemaDocument doc, DatatypeAttribute type) {
		TopLevelSimpleType[] arrSimpleType = doc.getSchema().getSimpleTypeArray();
		int i = 0;
		while (i < arrSimpleType.length) {
			if (arrSimpleType[i].getName().equals("AttTypeFeature")) {
				NoFixedFacet[] arrValues = arrSimpleType[i].getRestriction().getEnumerationArray();
				i = 0;
				while (i < arrValues.length) {
					if (arrValues[i].getValue().getStringValue().endsWith("Type"+type.name())) {
						return arrValues[i].getValue().getStringValue();
					}
					i++;
				}
				i = 0;
				while (i < arrValues.length) {
					if (arrValues[i].getValue().getStringValue().endsWith("Geometry"+type.name())) {
						return arrValues[i].getValue().getStringValue();
					}
					i++;
				}
				break;
			}
			i++;
		}
		return null;
	}
	
	/**
	 * Parses an attribute type
	 * @return String					The schema attribute name.
	 * @param doc						The schema document.
	 * @param type						The attribute type.
	 */
	protected static String parseAttriubteDatatypeExt(SchemaDocument doc, DatatypeAttributeExt type) {
		TopLevelSimpleType[] arrSimpleType = doc.getSchema().getSimpleTypeArray();
		int i = 0;
		while (i < arrSimpleType.length) {
			if (arrSimpleType[i].getName().equals("AttTypeList")) {
				NoFixedFacet[] arrValues = arrSimpleType[i].getRestriction().getEnumerationArray();
				i = 0;
				while (i < arrValues.length) {
					if (arrValues[i].getValue().getStringValue().endsWith("Type"+type.name())) {
						return arrValues[i].getValue().getStringValue();
					}
					i++;
				}
				break;
			}
			i++;
		}
		return null;
	}

	/**
	 * Gets the schema documents for a complex parameter.
	 * @return Map<String, SchemaDocument>	The schema documents.
	 * @param sOperatorIdentifier			The operator identifier
	 * @param param							The parameter.
	 * @param bInput						True whether its an input parameter.
	 * @throws Exception					When an error occurs.
	 */
	public static Map<String, SchemaDocument> getSchemaDocuments(String sOperatorIdentifier, WPSOperatorDescriptionParameterComplex param, boolean bInput) throws Exception {
		Map<String, SchemaDocument> mapResult = new HashMap<String, SchemaDocument>();
		Iterator<String> iterUrl = RequestDescribeSchema.generateSchemaUrl(sOperatorIdentifier, param, bInput).iterator();
		String sUrl;
		String sKey;
		String sParamName = "&parameter=";
		int nIndexStart;
		int nIndexEnd;
		while (iterUrl.hasNext()) {
			sUrl = iterUrl.next();
			sKey = sUrl;
			if (sUrl.indexOf('?') != -1) {
				// build schema dynamically
				nIndexStart = sUrl.indexOf(sParamName);
				if (nIndexStart < 0)
					throw new Exception("Invalid schema url.");
				nIndexStart += sParamName.length();
				nIndexEnd = sUrl.indexOf('&', nIndexStart);
				if (nIndexEnd < 0)
					nIndexEnd = sUrl.length();
				nIndexStart = sUrl.lastIndexOf('.', nIndexEnd)+1;
				nIndexStart = Integer.parseInt(sUrl.substring(nIndexStart, nIndexEnd));
				mapResult.put(sKey, RequestDescribeSchema.getSchemaDocument(param, nIndexStart));
			} else {
				// load schema file
				String sServerUrl = WPSServlet.getCapabilities().getServerUrl();
				sServerUrl = sServerUrl.substring(0, sServerUrl.lastIndexOf('/'));
				if (sUrl.startsWith(sServerUrl)) {
					sUrl = sUrl.substring(sServerUrl.length()+1);
					sUrl = WPSServlet.getHomeDirectory()+sUrl;
					File file = new File(sUrl);
					mapResult.put(sKey, SchemaDocument.Factory.parse(file));
				} else {
					throw new Exception("Invalid schema path '"+sUrl+"'.");
				}
			}
		}
		return mapResult;
	}

	/**
	 * Gets the schema document of a specified feature schema description.
	 * @return SchemaDocument			The schema document.
	 * @param param						The parameter.
	 * @param nIndex					The schema index.
	 * @throws Exception				When an error occurs.
	 */
	public static SchemaDocument getSchemaDocument(WPSOperatorDescriptionParameterComplex param, int nIndex) throws Exception {
		String sUrl = WPSServlet.getCapabilities().getServerUrl();
		sUrl = sUrl.substring(0, sUrl.lastIndexOf('/'));
		sUrl += (String)WPSServlet.getProperties().getProperty("schema.default");
		SchemaDocument doc = SchemaDocument.Factory.parse(new URL(sUrl));
		int i = 0;
		TopLevelElement elem = null;
		TopLevelElement[] arrElement = doc.getSchema().getElementArray();
		while (i < arrElement.length) {
			if (arrElement[i].getName().equalsIgnoreCase(param.getType().name())) {
				elem = (TopLevelElement)arrElement[i].copy();
				break;
			}
			i++;
		}
		i = 0;
		while (i < arrElement.length) {
			doc.getSchema().removeElement(0);
			i++;
		}
		doc.getSchema().addNewElement().set(elem);
		if (param.getAllowedSchemaDescriptions().size() > 0) {
			WPSFeatureSchemaDescription desc = param.getAllowedSchemaDescriptions().get(nIndex);
			TopLevelComplexType[] arrComplex = doc.getSchema().getComplexTypeArray();
			i = 0;
			while (i < arrComplex.length) {
				if (arrComplex[i].getName().equals("FeatureType") && ((param.getType() == DatatypeComplex.Feature) || (param.getType() == DatatypeComplex.FeatureCollection) || (param.getType() == DatatypeComplex.List))) {
					RequestDescribeSchema.addAttributes(desc, arrComplex[i].getComplexContent().getExtension().getSequence(), doc, WPSFeatureAttribute.class);
				} else if (arrComplex[i].getName().equals("FeatureCollectionType") && (param.getType() == DatatypeComplex.FeatureCollection)) {
					RequestDescribeSchema.addAttributes(desc, arrComplex[i].getComplexContent().getExtension().getSequence(), doc, WPSFeatureCollectionAttribute.class);
				} else if (arrComplex[i].getName().equals("TableType") && (param.getType() == DatatypeComplex.Table)) {
					RequestDescribeSchema.addAttributes(desc, arrComplex[i].getSequence().getElementArray()[0].getComplexType().getSequence(), doc, WPSFeatureAttribute.class);
				}
				i++;
			}
		}
		if (param.getAllowedListDatatypes().size() > 0) {
			DatatypeAttribute typeList = param.getAllowedListDatatypes().get(nIndex);
			TopLevelComplexType[] arrComplex = doc.getSchema().getComplexTypeArray();
			i = 0;
			while (i < arrComplex.length) {
				if (arrComplex[i].getName().equals("ListType") && (param.getType() == DatatypeComplex.List)) {
					LocalElement element = arrComplex[i].getSequence().addNewElement();
					element.setName("ListItem");
					element.setType(new QName(doc.getSchema().getTargetNamespace(), RequestDescribeSchema.parseAttriubteDatatype(doc, typeList)));
					break;
				}
				i++;
			}
		} else if (param.getAllowedListDatatypeExt() != null) {
			TopLevelComplexType[] arrComplex = doc.getSchema().getComplexTypeArray();
			i = 0;
			while (i < arrComplex.length) {
				if (arrComplex[i].getName().equals("ListType") && (param.getType() == DatatypeComplex.List)) {
					LocalElement element = arrComplex[i].getSequence().addNewElement();
					element.setName("ListItem");
					element.setType(new QName(doc.getSchema().getTargetNamespace(), RequestDescribeSchema.parseAttriubteDatatypeExt(doc, param.getAllowedListDatatypeExt())));
					break;
				}
				i++;
			}
		}
		return doc;
	}

	/**
	 * Adds the description schema attributes to schema document.
	 * @return void
	 * @param desc						The feature schema description.
	 * @param seqence					The seqence to add the attribute.
	 * @param doc						The schema document.
	 * @param clsType					The attribute class type.
	 */
	private static void addAttributes(WPSFeatureSchemaDescription desc, ExplicitGroup seqence, SchemaDocument doc, Class<?> clsType) {
		Iterator<WPSSchemaAttribute> iterAtt = desc.getAttributes().iterator();
		while (iterAtt.hasNext()) {
			WPSSchemaAttribute attribute = iterAtt.next();
			if (clsType.isAssignableFrom(attribute.getClass())) {
				LocalElement element = seqence.addNewElement();
				element.setName("Attribute"+attribute.getIdentifier());
				element.setType(new QName(doc.getSchema().getTargetNamespace(), RequestDescribeSchema.parseAttriubteDatatype(doc, attribute.getType())));
			}
		}
	}

	/**
	 * Generates the schema URLs for a specific parameter.
	 * @return List<String>				The generated schema URLs for that parameter.
	 * @param sOperatorIdentifier		The operator identifier.
	 * @param param						The parameter.
	 * @param bInput					True whether its an input parameter.
	 * @throws Exception				When an error occurs.
	 */
	public static List<String> generateSchemaUrl(String sOperatorIdentifier, WPSOperatorDescriptionParameterComplex param, boolean bInput) throws Exception {
		List<String> lstURLs = new LinkedList<String>();
		String sHomeUrl = WPSServlet.getCapabilities().getServerUrl();
		String sHomeUrlBase = sHomeUrl.substring(0, sHomeUrl.lastIndexOf('/'));
		Properties props = WPSServlet.getProperties();
		Object objProps;
		if ((param.getType() == DatatypeComplex.Point)
			|| (param.getType() == DatatypeComplex.LineString)
			|| (param.getType() == DatatypeComplex.Polygon)
			|| (param.getType() == DatatypeComplex.MultiPoint)
			|| (param.getType() == DatatypeComplex.MultiLineString)
			|| (param.getType() == DatatypeComplex.MultiPolygon)
			|| (param.getType() == DatatypeComplex.GeometryCollection)) {
			// geometry parameter
			// 2. define generic url
			String sUrl = sHomeUrl;
			sUrl += "?service=WPS&Request=DescribeSchema&operator=";
			sUrl += sOperatorIdentifier;
			sUrl += "&input=";
			sUrl += bInput;
			sUrl += "&parameter=";
			sUrl += param.getIdentifier() + "." + 0;
			lstURLs.add(sUrl);
		} else if ((param.getType() == DatatypeComplex.Feature)
					|| (param.getType() == DatatypeComplex.FeatureCollection)
					|| (param.getType() == DatatypeComplex.Table)) {
			Iterator<WPSFeatureSchemaDescription> iterSchema = param.getAllowedSchemaDescriptions().iterator();
			WPSFeatureSchemaDescription schema;
			int nSchemaCount = 0;
			boolean bGo;
			while (iterSchema.hasNext()) {
				schema = iterSchema.next();
				bGo = true;
				// 1. check for standard schema
				if (schema.getAttributes().size() == 1 && ((param.getType() == DatatypeComplex.Feature) || (param.getType() == DatatypeComplex.FeatureCollection))) {
					String sName = schema.getAttributes().get(0).getIdentifier();
					sName = sName + "_" + schema.getAttributes().get(0).getType().name();
					objProps = props.getProperty("schema.default."+param.getType().name().toLowerCase()+"."+sName.toLowerCase());
					if (objProps != null) {
						lstURLs.add(sHomeUrlBase + (String)objProps);
						bGo = false;
					}
				}
				if (bGo) {
					// 2. define generic url
					lstURLs.add(RequestDescribeSchema.generateSchemaUrl(sHomeUrl, nSchemaCount, sOperatorIdentifier, param, bInput));
				}
				nSchemaCount++;
			}
		} else if (param.getType() == DatatypeComplex.List) {
			Iterator<?> iterType;
			if (param.getAllowedListDatatypeExt() != null)
				iterType = param.getAllowedSchemaDescriptions().iterator();
			else
				iterType = param.getAllowedListDatatypes().iterator();
			int nSchemaCount = 0;
			while (iterType.hasNext()) {
				iterType.next();
				// 2. define generic url
				lstURLs.add(RequestDescribeSchema.generateSchemaUrl(sHomeUrl, nSchemaCount, sOperatorIdentifier, param, bInput));
				nSchemaCount++;
			}
		} else {
			throw new Exception("Parameter type '"+param.getType().name()+"' is not supported.");
		}
		return lstURLs;
	}

	/**
	 * Generates the schema URL for a specific parameter.
	 * @return String					The generated schema URL.
	 * @param sHomeUrl					The home URL.
	 * @param nSchemaIndex				The schema index.
	 * @param sOperatorIdentifier		The operator identifier.
	 * @param param						The parameter.
	 * @param bInput					True whether its an input parameter.
	 */
	private static String generateSchemaUrl(String sHomeUrl, int nSchemaIndex, String sOperatorIdentifier, WPSOperatorDescriptionParameterComplex param, boolean bInput) {
		String sUrl = sHomeUrl;
		sUrl += "?service=WPS&Request=DescribeSchema&operator=";
		sUrl += sOperatorIdentifier;
		sUrl += "&input=";
		sUrl += bInput;
		sUrl += "&parameter=";
		sUrl += param.getIdentifier() + "." + nSchemaIndex;
		return sUrl;
	}
}
