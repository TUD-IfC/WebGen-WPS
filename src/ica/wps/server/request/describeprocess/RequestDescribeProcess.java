package ica.wps.server.request.describeprocess;

import java.math.BigInteger;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import ica.wps.data.WPSOperatorDescription;
import ica.wps.data.WPSOperatorDescriptionParameter;
import ica.wps.data.WPSOperatorDescriptionParameterComplex;
import ica.wps.data.WPSOperatorDescriptionParameterLiteral;
import ica.wps.data.WPSOperatorDescription.Range;
import ica.wps.server.ExceptionCode;
import ica.wps.server.ServerException;
import ica.wps.server.request.Request;
import ica.wps.server.request.describeschema.RequestDescribeSchema;
import ica.wps.server.WPSServlet;
import ica.wps.server.IWPSOperator;
import ica.wps.WPSConstants;

import net.opengis.wps.x100.ProcessDescriptionsDocument;
import net.opengis.wps.x100.ProcessDescriptionsDocument.ProcessDescriptions;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType.DataInputs;
import net.opengis.wps.x100.ProcessDescriptionType.ProcessOutputs;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.LiteralInputType;
import net.opengis.wps.x100.ComplexDataCombinationsType;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.SupportedComplexDataInputType;
import net.opengis.wps.x100.LiteralOutputType;
import net.opengis.wps.x100.SupportedUOMsType;
import net.opengis.wps.x100.SupportedComplexDataType;
import net.opengis.ows.x11.AllowedValuesDocument.AllowedValues;
import net.opengis.ows.x11.RangeType;

import org.apache.xmlbeans.XmlObject;



/**
 * Request class handles the GetCapabilities request.
 * @author	M. Wittensoeldner
 * @date	Created on 01.02.2007
 */
public abstract class RequestDescribeProcess extends Request {

	protected static final String		PARAM_IDENTIFIER = "Identifier";

	protected LinkedList<String>		_lstIdentifiers = null;
	
	/**
	 * Handles the request. This method is called after parseRequest().
	 * @return XmlObject				The resulted xml document.
	 * @throws ServerException			When an error occurs.
	 */
	protected XmlObject handleRequest() throws ServerException {
		if (_sAcceptVersions == null)
			throw new ServerException(ExceptionCode.MissingParameterValue, PARAM_VERSION, "Parameter '"+WPSConstants.WPS_VERSION+"' is not set.");
		else if (!_sAcceptVersions.equals(WPSConstants.WPS_VERSION))
			throw new ServerException(ExceptionCode.InvalidParameterValue, PARAM_VERSION, "Version must be '"+WPSConstants.WPS_VERSION+"', but it's '"+_sAcceptVersions+"'");
		else if (_lstIdentifiers == null)
			throw new ServerException(ExceptionCode.MissingParameterValue, PARAM_IDENTIFIER, "Parameter '"+PARAM_IDENTIFIER+"' is not set.");
		else if (_lstIdentifiers.size() <= 0)
			throw new ServerException(ExceptionCode.InvalidParameterValue, PARAM_IDENTIFIER, "Parameter value is empty.");
		
		ProcessDescriptionsDocument doc = ProcessDescriptionsDocument.Factory.newInstance();
		ProcessDescriptions descs = doc.addNewProcessDescriptions();
		descs.setService(WPSConstants.WPS_SERVICE);
		descs.setVersion(WPSConstants.WPS_VERSION);
		descs.setLang(WPS_LANGUAGE);
		Iterator<String> iterIdentifiers = _lstIdentifiers.iterator();
		while (iterIdentifiers.hasNext()) {
			String sIdentifier = iterIdentifiers.next();
			IWPSOperator op = WPSServlet.getCapabilities().getOperator(sIdentifier);
			
			if (op != null) {
				ProcessDescriptionType desc = descs.addNewProcessDescription();
				desc.setStoreSupported(op.getOperatorDescription().isStoreSupporded());
				desc.setStatusSupported(op.getOperatorDescription().isStatusSupporded());
				this.setProcessInformation(desc, op);
				if (op.getOperatorDescription().getParameters(true).size() > 0) {
					DataInputs inputs = desc.addNewDataInputs();
					WPSOperatorDescriptionParameter param;
					Iterator<WPSOperatorDescriptionParameter> iterParams = op.getOperatorDescription().getParameters(true).iterator();
					while (iterParams.hasNext()) {
						param = iterParams.next();
						InputDescriptionType input = inputs.addNewInput();
						input.addNewIdentifier().setStringValue(param.getIdentifier());
						input.addNewTitle().setStringValue(param.getTitle());
						input.addNewAbstract().setStringValue(param.getDescription());
						input.setMinOccurs(new BigInteger(""+param.getMinOccurs()));
						input.setMaxOccurs(new BigInteger(""+param.getMaxOccurs()));
						if (param instanceof WPSOperatorDescriptionParameterLiteral) {
							// literal data type
							WPSOperatorDescriptionParameterLiteral paramLiteral = (WPSOperatorDescriptionParameterLiteral)param;
							LiteralInputType literal = input.addNewLiteralData();
							literal.addNewDataType().setStringValue(paramLiteral.getType().name());
							if (paramLiteral.getUnitOfMeasure() != null) {
								SupportedUOMsType uoms = literal.addNewUOMs();
								uoms.addNewDefault().addNewUOM().setStringValue(paramLiteral.getUnitOfMeasure());
								uoms.addNewSupported().addNewUOM().setStringValue(paramLiteral.getUnitOfMeasure());
							}
							if ((paramLiteral.getAllowedRanges() != null) || (paramLiteral.getAllowedValues() != null)) {
								AllowedValues values = literal.addNewAllowedValues();
								if (paramLiteral.getAllowedRanges() != null) {
									Iterator<Range> iterRange = paramLiteral.getAllowedRanges().iterator();
									while (iterRange.hasNext()) {
										Range range = iterRange.next();
										RangeType newRange = values.addNewRange();
										newRange.addNewMinimumValue().setStringValue(WPSOperatorDescription.convertToString(range.getMinValue(), paramLiteral.getType(), true));
										newRange.addNewMaximumValue().setStringValue(WPSOperatorDescription.convertToString(range.getMaxValue(), paramLiteral.getType(), true));
									}
								}
								if (paramLiteral.getAllowedValues() != null) {
									Iterator<Object> iterValues = paramLiteral.getAllowedValues().iterator();
									while (iterValues.hasNext()) {
										values.addNewValue().setStringValue(WPSOperatorDescription.convertToString(iterValues.next(), paramLiteral.getType(), true));
									}
								}
							} else {
								literal.addNewAnyValue();
							}
							if (paramLiteral.getDefaultValue() != null) {
								literal.setDefaultValue(WPSOperatorDescription.convertToString(paramLiteral.getDefaultValue(), paramLiteral.getType(), true));
							}
						} else if (param instanceof WPSOperatorDescriptionParameterComplex) {
							// complex data type
							WPSOperatorDescriptionParameterComplex paramComplex = (WPSOperatorDescriptionParameterComplex)param;
							SupportedComplexDataInputType complex = input.addNewComplexData();
							complex.setMaximumMegabytes(BigInteger.valueOf(paramComplex.getMaxMegabyte()));
							try {
								List<String> lstSchemas = RequestDescribeSchema.generateSchemaUrl(op.getOperatorDescription().getIdentifier(), paramComplex, true);
								boolean bFirst = true;
								String sUrl;
								Iterator<String> iterSchema = lstSchemas.iterator();
								ComplexDataCombinationsType supported = null;
								ComplexDataDescriptionType format;
								while (iterSchema.hasNext()) {
									sUrl = iterSchema.next();
									if (bFirst) {
										format = complex.addNewDefault().addNewFormat();
										format.setMimeType(WPSConstants.WPS_MIME_XML);
										format.setEncoding(WPSConstants.WPS_ENCODING);
										format.setSchema(sUrl);
										supported = complex.addNewSupported();
										bFirst = false;
									}
									format = supported.addNewFormat();
									format.setMimeType(WPSConstants.WPS_MIME_XML);
									format.setEncoding(WPSConstants.WPS_ENCODING);
									format.setSchema(sUrl);
								}
							} catch (Exception ex) {
								throw new ServerException(ExceptionCode.NoApplicableCode, "", "Schema url generation of complex type failed: " + ex.getMessage());
							}
						}
					}
					
				}
				if (op.getOperatorDescription().getParameters(false).size() > 0) {
					ProcessOutputs outputs = desc.addNewProcessOutputs();
					WPSOperatorDescriptionParameter param;
					Iterator<WPSOperatorDescriptionParameter> iterParams = op.getOperatorDescription().getParameters(false).iterator();
					while (iterParams.hasNext()) {
						param = iterParams.next();
						OutputDescriptionType output = outputs.addNewOutput();
						output.addNewIdentifier().setStringValue(param.getIdentifier());
						output.addNewTitle().setStringValue(param.getTitle());
						output.addNewAbstract().setStringValue(param.getDescription());
						if (param instanceof WPSOperatorDescriptionParameterLiteral) {
							// literal data type
							WPSOperatorDescriptionParameterLiteral paramLiteral = (WPSOperatorDescriptionParameterLiteral)param;
							LiteralOutputType literal = output.addNewLiteralOutput();
							literal.addNewDataType().setStringValue(paramLiteral.getType().name());
							if (paramLiteral.getUnitOfMeasure() != null) {
								SupportedUOMsType uoms = literal.addNewUOMs();
								uoms.addNewDefault().addNewUOM().setStringValue(paramLiteral.getUnitOfMeasure());
								uoms.addNewSupported().addNewUOM().setStringValue(paramLiteral.getUnitOfMeasure());
							}
						} else if (param instanceof WPSOperatorDescriptionParameterComplex) {
							// complex data type
							WPSOperatorDescriptionParameterComplex paramComplex = (WPSOperatorDescriptionParameterComplex)param;
							SupportedComplexDataType complex = output.addNewComplexOutput();
							try {
								List<String> lstSchemas = RequestDescribeSchema.generateSchemaUrl(op.getOperatorDescription().getIdentifier(), paramComplex, false);
								boolean bFirst = true;
								String sUrl;
								Iterator<String> iterSchema = lstSchemas.iterator();
								ComplexDataCombinationsType supported = null;
								ComplexDataDescriptionType format;
								while (iterSchema.hasNext()) {
									sUrl = iterSchema.next();
									if (bFirst) {
										format = complex.addNewDefault().addNewFormat();
										format.setMimeType(WPSConstants.WPS_MIME_XML);
										format.setEncoding(WPSConstants.WPS_ENCODING);
										format.setSchema(sUrl);
										supported = complex.addNewSupported();
										bFirst = false;
									}
									format = supported.addNewFormat();
									format.setMimeType(WPSConstants.WPS_MIME_XML);
									format.setEncoding(WPSConstants.WPS_ENCODING);
									format.setSchema(sUrl);
								}
							} catch (Exception ex) {
								throw new ServerException(ExceptionCode.NoApplicableCode, "", "Schema url generation of complex type failed: " + ex.getMessage());
							}
						}
					}
				}
			} else {
				throw new ServerException(ExceptionCode.ResourceNotFound, sIdentifier, "Operator not found.");
			}
		}
		return doc;
	}

}
