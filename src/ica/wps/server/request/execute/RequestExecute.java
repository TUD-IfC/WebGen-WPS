package ica.wps.server.request.execute;

import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.GregorianCalendar;
import org.w3c.dom.Node;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import javax.xml.transform.dom.DOMSource;

import ica.wps.common.IWPSStatusListener;
import ica.wps.common.ObjectXmlParser;
import ica.wps.common.XmlObjectParser;
import ica.wps.data.WPSOperatorDescription;
import ica.wps.data.WPSOperatorDescriptionParameter;
import ica.wps.data.WPSOperatorDescriptionParameterComplex;
import ica.wps.data.WPSOperatorDescriptionParameterLiteral;
import ica.wps.server.ExceptionCode;
import ica.wps.server.ServerException;
import ica.wps.server.WPSServlet;
import ica.wps.server.request.Request;
import ica.wps.server.IWPSOperator;
import ica.wps.server.request.describeschema.RequestDescribeSchema;
import ica.wps.WPSConstants;

import net.opengis.ows.x11.ExceptionType;
import net.opengis.wps.x100.DocumentOutputDefinitionType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.InputReferenceType;
import net.opengis.wps.x100.InputReferenceType.Header;
import net.opengis.wps.x100.ResponseDocumentType;
import net.opengis.wps.x100.OutputDefinitionType;
import net.opengis.wps.x100.OutputDefinitionsType;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.ExecuteResponseDocument.ExecuteResponse;
import net.opengis.wps.x100.ProcessBriefType;
import net.opengis.wps.x100.ExecuteResponseDocument.ExecuteResponse.ProcessOutputs;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.LiteralDataType;
import net.opengis.wps.x100.ComplexDataType;
import net.opengis.wps.x100.OutputReferenceType;
import net.opengis.wps.x100.StatusType;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;

/**
 * Request class handles the GetCapabilities request.
 * @author	M. Wittensoeldner
 * @date	Created on 01.02.2007
 */
public abstract class RequestExecute extends Request implements IWPSStatusListener {
	protected ExecuteDocument		_document = null;

	/**
	 * Handles the request. This method is called after parseRequest().
	 * @return void
	 * @return XmlObject				The resulted xml document.
	 * @throws ServerException			When an error occurs.
	 */
	protected XmlObject handleRequest() throws ServerException {
		if (_sAcceptVersions == null)
			throw new ServerException(ExceptionCode.MissingParameterValue, PARAM_VERSION, "Parameter '"+WPSConstants.WPS_VERSION+"' is not set.");
		else if (!_sAcceptVersions.equals(WPSConstants.WPS_VERSION))
			throw new ServerException(ExceptionCode.InvalidParameterValue, PARAM_VERSION, "Version must be '"+WPSConstants.WPS_VERSION+"', but it's '"+_sAcceptVersions+"'");
		else if (_document == null)
			throw new ServerException(ExceptionCode.NoApplicableCode, "", "XML file not valid.");

		String sIdentifier = _document.getExecute().getIdentifier().getStringValue();
		IWPSOperator op = WPSServlet.getCapabilities().getOperator(sIdentifier);
		Map<String, Object[]> mapParameterOutFormat = new HashMap<String, Object[]>();
		if (op != null) {
			try {
				WPSOperatorDescriptionParameter param;
				// first check the response form
				boolean bDirectResponse = true;
				boolean bIncludeInput = false;
				boolean bUpdateStatus = false;
				String sRawOutputName = null;
				if (_document.getExecute().isSetResponseForm()) {
					if (_document.getExecute().getResponseForm().isSetResponseDocument()) {
						ResponseDocumentType responseType = _document.getExecute().getResponseForm().getResponseDocument();
						if (responseType.isSetStoreExecuteResponse())
							bDirectResponse = !responseType.getStoreExecuteResponse();
						if (responseType.isSetStatus())
							bUpdateStatus = responseType.getStatus();
						if (bDirectResponse && bUpdateStatus)
							throw new ServerException(ExceptionCode.InvalidParameterValue, "status", "Output status is not supported by a direct response.");
						if (responseType.isSetLineage())
							bIncludeInput = responseType.getLineage();
						Iterator<DocumentOutputDefinitionType> iterOut = responseType.getOutputList().iterator();
						DocumentOutputDefinitionType output;
						while (iterOut.hasNext()) {
							output = iterOut.next();
							String sOutName = output.getIdentifier().getStringValue();
							param = this.findParameter(op, sOutName, false);
							Object[] arrOutDef = {new Boolean(false), WPSConstants.WPS_ENCODING, param.getTitle(), param.getDescription()}; // {reference, encoding, title, abstract}
							if (output.isSetMimeType()) {
								if (!output.getMimeType().equalsIgnoreCase(WPSConstants.WPS_MIME_XML))
									throw new ServerException(ExceptionCode.InvalidParameterValue, sOutName, "Only output mime type '"+WPSConstants.WPS_MIME_XML+"' is supported.");
							}
							if (output.isSetEncoding())
								arrOutDef[1] = output.getEncoding();
							if (output.isSetSchema()) {
								throw new ServerException(ExceptionCode.InvalidParameterValue, sOutName, "Choosing the schema for output data is not yet supported.");
							}
							if (output.isSetUom()) {
								if ((param.getUnitOfMeasure() == null) || (output.getUom().equalsIgnoreCase(param.getUnitOfMeasure())))
									throw new ServerException(ExceptionCode.InvalidParameterValue, sOutName, "Output unit of measure '"+output.getUom()+"' is not supported.");
							}
							if (output.isSetAsReference())
								arrOutDef[0] = new Boolean(output.getAsReference());
							if (output.isSetTitle())
								arrOutDef[2] = output.getTitle().getStringValue();
							if (output.isSetAbstract())
								arrOutDef[3] = output.getAbstract().getStringValue();
							mapParameterOutFormat.put(sOutName, arrOutDef);
						}
					} else {
						// raw output
						OutputDefinitionType rawType = _document.getExecute().getResponseForm().getRawDataOutput();
						sRawOutputName = rawType.getIdentifier().getStringValue();
						param = this.findParameter(op, sRawOutputName, false);
						Object[] arrOutDef = {new Boolean(false), WPSConstants.WPS_ENCODING, param.getTitle(), param.getDescription()}; // {reference, encoding, title, abstract}
						if (rawType.isSetMimeType()) {
							if (!rawType.getMimeType().equalsIgnoreCase(WPSConstants.WPS_MIME_XML))
								throw new ServerException(ExceptionCode.InvalidParameterValue, rawType.getIdentifier().getStringValue(), "Only output mime type '"+WPSConstants.WPS_MIME_XML+"' is supported.");
						}
						if (rawType.isSetEncoding())
							arrOutDef[1] = rawType.getEncoding();
						if (rawType.isSetSchema()) {
							throw new ServerException(ExceptionCode.InvalidParameterValue, rawType.getIdentifier().getStringValue(), "Choosing the schema for output data is not yet supported.");
						}
						if (rawType.isSetUom()) {
							if ((param.getUnitOfMeasure() == null) || (rawType.getUom().equalsIgnoreCase(param.getUnitOfMeasure())))
								throw new ServerException(ExceptionCode.InvalidParameterValue, rawType.getIdentifier().getStringValue(), "Output unit of measure '"+rawType.getUom()+"' is not supported.");
						}
						mapParameterOutFormat.put(sRawOutputName, arrOutDef);
					}
				}
				
				if (bDirectResponse) {
					// run operator directly and return the response
					ThreadExecuteDirect threadDirect = new ThreadExecuteDirect(this, op, mapParameterOutFormat, bIncludeInput);
					ThreadTimeout threadTimeout = new ThreadTimeout(threadDirect, op.getOperatorDescription().getTimeoutMillis());
					threadTimeout.start();
					while (threadTimeout.isAlive()) {
						Thread.sleep(1000);
					}
					if (threadTimeout.isTimeout())
						return this.getTimeoutResponse(op, bIncludeInput);
					else
						return threadDirect.getResponseDocument();
				} else {
					// store response document
					String[] sServerUrl = {null};
					File outFile = Request.createDataFile(".xml", sServerUrl);
					ExecuteResponseDocument doc = this.initResponseDocument(op, false);
					StatusType status = doc.getExecuteResponse().addNewStatus();
					status.setCreationTime(new GregorianCalendar());
					status.setProcessAccepted("Request is in progress...");
					doc.getExecuteResponse().setStatusLocation(sServerUrl[0]);
					doc.save(outFile, WPSServlet.getXmlOptions());
					// start execution thread
					ThreadExecuteStatus thread = new ThreadExecuteStatus(outFile, sServerUrl[0], this, op, mapParameterOutFormat, bIncludeInput, bUpdateStatus);
					ThreadTimeout threadTimeout = new ThreadTimeout(thread, op.getOperatorDescription().getTimeoutMillis());
					threadTimeout.start();
					// return response document
					return doc;
				}
				
				// produce output
			} catch (Exception ex) {
				if (ex instanceof ServerException)
					throw new ServerException(((ServerException)ex).getExceptionCode(), ((ServerException)ex).getLocator(), ex.getMessage());
				else {
					throw new ServerException(ExceptionCode.NoApplicableCode, "", "Execution of operator '"+sIdentifier+"' failed.");
				}
			}
		} else {
			throw new ServerException(ExceptionCode.OperationNotSupported, sIdentifier, "Operator '"+sIdentifier+"' not available.");
		}
	}
	
	/**
	 * Loads the reference.
	 * @return XmlObject				The loaded reference data. Only xml supported.
	 * @param ref						The reference to load.
	 * @param sIdentifier				The parameter identifier.
	 * @param lMaxMegabyte				The maximum file size.
	 * @throws Exception				When an error occurs.
	 */
	protected XmlObject loadReference(InputReferenceType ref, String sIdentifier, long lMaxMegabyte) throws ServerException {
        XmlObject doc = null;
		InputStream streamIn;
		try {
	        URL url = new URL(ref.getHref());
	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	        if (ref.isSetMethod())
	            conn.setRequestMethod(ref.getMethod().toString().toUpperCase());
	        else
	            conn.setRequestMethod("GET");
	        Iterator<Header> iterHeader = ref.getHeaderList().iterator();
	        Header header;
	        while (iterHeader.hasNext()) {
	        	header = iterHeader.next();
	        	conn.addRequestProperty(header.getKey(), header.getValue());
	        }
	        conn.setDoOutput(conn.getRequestMethod().equals("POST"));
	        conn.connect();
	        if (ref.isSetBodyReference()) {
	            HttpURLConnection connRef = (HttpURLConnection)new URL(ref.getBodyReference().getHref()).openConnection();
	            connRef.connect();
	            streamIn = connRef.getInputStream();
	            if (connRef.getResponseCode() == HttpURLConnection.HTTP_OK) {
	                byte[] arrData = new byte[2048];
	                int nRead;
	                while ((nRead = streamIn.read(arrData)) > 0) {
	        	        conn.getOutputStream().write(arrData, 0, nRead);
	                }
	            	conn.getOutputStream().flush();
	                connRef.disconnect();
	            } else {
	                connRef.disconnect();
	                conn.disconnect();
	            	throw new Exception("Loading reference data of reference data failed. HTTP Code: "+ conn.getResponseCode());
	            }
	        } else if (ref.isSetBody()) {
	        	ref.getBody().save(conn.getOutputStream());
	        	conn.getOutputStream().flush();
	        }
	        streamIn = conn.getInputStream();
	        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
		        long lDataMegabyte = streamIn.available()/1000000;
		        if (lDataMegabyte > lMaxMegabyte) {
		        	conn.disconnect();
					throw new ServerException(ExceptionCode.FileSizeExceeded, sIdentifier, "Maximum filesize "+lMaxMegabyte+"MB exceeded ("+lDataMegabyte+"MB).");
		        }
	        	doc = XmlObject.Factory.parse(streamIn);
	        } else {
	        	conn.disconnect();
	        	throw new Exception("Loading reference data failed. HTTP Code: "+ conn.getResponseCode());
	        }
	    	conn.disconnect();
	        return doc;

		} catch (ServerException e) {
			throw e;
		} catch (Exception ex) {
			throw new ServerException(ExceptionCode.NoApplicableCode, "", ex.toString());
		}
	}

	/**
	 * Validates the input value and converts it into the correct object.
	 * @return Object					The validated object.
	 * @param sOperatorIdentifier		The operator identifier.
	 * @param obj						The object to validate.
	 * @param param						The paramteter.
	 * @throws Exception				When validation fails.
	 */
	protected Object validateInput(String sOperatorIdentifier, Object obj, WPSOperatorDescriptionParameter param) throws Exception {
		if (param instanceof WPSOperatorDescriptionParameterLiteral) {
			WPSOperatorDescriptionParameterLiteral paramLiteral = (WPSOperatorDescriptionParameterLiteral)param;
			Object objValue = WPSOperatorDescription.convertToObject(obj.toString(), paramLiteral.getType());
			if (!paramLiteral.isValid(objValue)) {
				throw new ServerException(ExceptionCode.InvalidParameterValue, param.getIdentifier(), "Value is not in valid range or in allowed values.");
			}
			return objValue;
		} else {
			this.validateSchema(sOperatorIdentifier, (XmlObject)obj, (WPSOperatorDescriptionParameterComplex)param, true);
        	XmlObjectParser parser = new XmlObjectParser(WPSServlet.getXmlObjectParser());
       		return parser.parseXmlObject((XmlObject)obj);
		}
	}
	
	/**
	 * Validates the xml object against the schema.
	 * @return String					The path to the correct validated schema.
	 * @param sOperatorIdentifier		The operator identifier.
	 * @param objXml					The object to validate.
	 * @param param						The paramteter.
	 * @param bInput					True when the parameter is an input parameter.
	 * @throws Exception				When validation fails.
	 */
	protected String validateSchema(String sOperatorIdentifier, XmlObject objXml, WPSOperatorDescriptionParameterComplex param, boolean bInput) throws Exception {
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		Validator validator;
		Iterator<Entry<String, SchemaDocument>> iterSchema = RequestDescribeSchema.getSchemaDocuments(sOperatorIdentifier, param, bInput).entrySet().iterator();
		Entry<String, SchemaDocument> entry;
		Schema schema;
		while (iterSchema.hasNext()) {
			entry = iterSchema.next();
			schema = factory.newSchema(new DOMSource(entry.getValue().getDomNode()));
	        validator = schema.newValidator();
	        try {
	        	validator.validate(new DOMSource(objXml.getDomNode()));
	        	return entry.getKey();
	        } catch (Exception ex) {
	        }
		}
//		System.out.println(objXml.toString());
		throw new Exception(this.getClass().getSimpleName()+".validateSchema(): Schema validation failed.");
	}

	/**
	 * Checks the occurance of parameters.
	 * @return void
	 * @param lstParameter				The parameter list.
	 * @param mapValues					The effective values.
	 * @throws Exception				When occurance is invalid.
	 */
	protected void checkOccurance(List<WPSOperatorDescriptionParameter> lstParameter, Map<String, List<Object>> mapValuesList, Map<String, Object> mapValuesObjects) throws Exception {
		Iterator<WPSOperatorDescriptionParameter> iterParams = lstParameter.iterator();
		WPSOperatorDescriptionParameter param = null;
		int nSize;
		while (iterParams.hasNext()) {
			param = iterParams.next();
			nSize = -1;
			if (mapValuesList != null) {
				if (mapValuesList.get(param.getIdentifier()) != null)
					nSize = mapValuesList.get(param.getIdentifier()).size();
			} else if (mapValuesObjects != null) {
				if (mapValuesObjects.get(param.getIdentifier()) != null)
					nSize = 1;
			}
			if (nSize == -1) {
				if (param.getMinOccurs() > 0) {
					throw new ServerException(ExceptionCode.MissingParameterValue, param.getIdentifier(), "The parameter occurance is 0, should be "+param.getMinOccurs()+" in minimum.");
				}
			} else {
				if ((nSize < param.getMinOccurs()) || (nSize > param.getMaxOccurs()))
					throw new ServerException(ExceptionCode.InvalidParameterValue, param.getIdentifier(), "The parameter occurance is "+nSize+", but should be between "+param.getMinOccurs()+" and "+param.getMaxOccurs()+".");
			}
		}
	}
	
	/**
	 * Finds a operator parameter by name.
	 * @return WPSOperatorDescriptionParameter	The found parameter
	 * @param op						The operatoer.
	 * @param sName						The parameter name.
	 * @param bInput					True when an input parameter.
	 * @throws Exception				When the parameter doesnt exist.
	 */
	protected WPSOperatorDescriptionParameter findParameter(IWPSOperator op, String sName, boolean bInput) throws Exception {
		Iterator<WPSOperatorDescriptionParameter> iterParams = op.getOperatorDescription().getParameters(bInput).iterator();
		WPSOperatorDescriptionParameter param;
		while (iterParams.hasNext()) {
			param = iterParams.next();
			if (param.getIdentifier().equals(sName)) 
				return param;
		}
		String sInput = "output";
		if (bInput)
			sInput = "input";
		throw new ServerException(ExceptionCode.InvalidParameterValue, sName, "Operator "+sInput+" parameter '"+sName+"' doesnt exist.");
	}
	
	/**
	 * Runs the operator.
	 * @return XmlObject				The result document.
	 * @param op						The operator to run.
	 * @param mapParameterOutFormat		The requested output format for output values.
	 * @param bIncludeInput				True when the input is included in the response document.
	 * @param sRawOutputName			The name of the raw output parameter. Null when no raw output. 
	 * @param statusListener			The status listener.
	 * @throws ServerException			When an error occurs.
	 */
	XmlObject runOperator(IWPSOperator op, Map<String, Object[]> mapParameterOutFormat, boolean bIncludeInput, String sRawOutputName, IWPSStatusListener statusListener) throws ServerException {
		boolean bReturnRawData = sRawOutputName != null;
		WPSOperatorDescriptionParameter param;
		Map<String, List<Object>> mapParameter = new HashMap<String, List<Object>>();
		// check the input parameter
		try {
			if (_document.getExecute().isSetDataInputs() && (_document.getExecute().getDataInputs().getInputList() != null)) {
				InputType input;
				Iterator<InputType> iterInput = _document.getExecute().getDataInputs().getInputList().iterator();
				while (iterInput.hasNext()) {
					input = iterInput.next();
					param = this.findParameter(op, input.getIdentifier().getStringValue(), true);
					long lMaxMegabyte = Long.MAX_VALUE;
					if (param instanceof WPSOperatorDescriptionParameterComplex)
						lMaxMegabyte = ((WPSOperatorDescriptionParameterComplex)param).getMaxMegabyte();
					Object objValue = null;
					if (input.isSetReference()) {
						// load reference
						objValue = this.loadReference(input.getReference(), param.getIdentifier(), lMaxMegabyte);
					} else if (input.isSetData()) {
						if (input.getData().isSetLiteralData()) {
							if ((input.getData().getLiteralData().getUom() != null) && !input.getData().getLiteralData().getUom().equalsIgnoreCase(param.getUnitOfMeasure()))
								throw new ServerException(ExceptionCode.InvalidParameterValue, input.getIdentifier().getStringValue(), "UOM '"+input.getData().getLiteralData().getUom()+"' is not valid.");
							objValue = input.getData().getLiteralData().getStringValue();
						} else if (input.getData().isSetComplexData()) {
							Node node = input.getData().getComplexData().getDomNode();
							int i = 0;
							while (i < node.getChildNodes().getLength()) {
								if (node.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
									objValue = XmlObject.Factory.parse(node.getChildNodes().item(i));
									if (lMaxMegabyte < Long.MAX_VALUE) {
										File file = Request.createDataFile(".dmp", null);
										((XmlObject)objValue).save(file);
										long lDataMegabyte = file.length();
										file.delete();
										lDataMegabyte /= 1000000L;
										if (lDataMegabyte > lMaxMegabyte)
											throw new ServerException(ExceptionCode.FileSizeExceeded, param.getIdentifier(), "Maximum filesize "+lMaxMegabyte+"MB exceeded ("+lDataMegabyte+"MB).");
									}
									break;
								}
								i++;
							}
						} else if (input.getData().isSetBoundingBoxData()) {
							throw new Exception("BoundingBox InputData is not supported. ('"+input.getIdentifier().getStringValue()+"')");
						}
					}
					if (objValue != null) {
						// assign the value
						try {
							objValue = this.validateInput(op.getOperatorDescription().getIdentifier(), objValue, param);
							List<Object> lstObject = mapParameter.get(param.getIdentifier());
							if (lstObject == null) {
								lstObject = new LinkedList<Object>();
								mapParameter.put(param.getIdentifier(), lstObject);
							}
							lstObject.add(objValue);
						} catch (Exception ex) {
							throw new ServerException(ExceptionCode.InvalidParameterValue, input.getIdentifier().getStringValue(), "Assigning the value failed. Wrong data type? ("+ex.toString()+":"+ex.getMessage()+")");
						}
						
					} else {
						throw new ServerException(ExceptionCode.InvalidParameterValue, input.getIdentifier().getStringValue(), "Reading value failed.");
					}
				}
			}
			// check the occurance of the input parameters
			this.checkOccurance(op.getOperatorDescription().getParameters(true), mapParameter, null);
			// execute the operator
			statusListener.setStatus(0.0, "Execution started...");
			Map<String, Object> mapResult = op.execute(mapParameter, statusListener);
			// validate output
			// check occurance
			this.checkOccurance(op.getOperatorDescription().getParameters(false), null, mapResult);
			// create output document and validate result values
			ExecuteResponseDocument doc = this.initResponseDocument(op, bIncludeInput);
			ProcessOutputs processOutputs = doc.getExecuteResponse().addNewProcessOutputs();
			// validate output values
			Iterator<Entry<String, Object>> iterResult = mapResult.entrySet().iterator();
			Object objOut;
			Object[] arrOutFormat;
			boolean bReference;
			Entry<String, Object> entry;
			while (iterResult.hasNext()) {
				entry = iterResult.next();
				param = this.findParameter(op, entry.getKey(), false);
				if (entry.getValue() != null) {
					OutputDataType outType = processOutputs.addNewOutput();
					bReference = false;
					arrOutFormat = mapParameterOutFormat.get(param.getIdentifier());
					outType.addNewIdentifier().setStringValue(param.getIdentifier());
					if (arrOutFormat != null) {
						bReference = ((Boolean)arrOutFormat[0]).booleanValue();
						outType.addNewTitle().setStringValue((String)arrOutFormat[2]);
						outType.addNewAbstract().setStringValue((String)arrOutFormat[3]);
					} else {
						outType.addNewTitle().setStringValue(param.getTitle());
						outType.addNewAbstract().setStringValue(param.getDescription());
					}						
					objOut = entry.getValue();
					if (param instanceof WPSOperatorDescriptionParameterLiteral) {
						if (((WPSOperatorDescriptionParameterLiteral)param).isValid(objOut)) {
							if (bReturnRawData && sRawOutputName.equals(param.getIdentifier())) {
								return XmlObject.Factory.parse("<" + RAW_OUTPUT_NAME +">"+WPSOperatorDescription.convertToString(objOut, ((WPSOperatorDescriptionParameterLiteral)param).getType(), true)+"</"+RAW_OUTPUT_NAME+">");
							} else {
								if (bReference) {
									// save data on server
									String[] sServerUrl = {null};
									File outFile = Request.createDataFile(".txt", sServerUrl);
									OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outFile), (String)arrOutFormat[1]);
									writer.write(WPSOperatorDescription.convertToString(objOut, ((WPSOperatorDescriptionParameterLiteral)param).getType(), true));
									writer.close();
									OutputReferenceType refType = outType.addNewReference();
									refType.setEncoding((String)arrOutFormat[1]);
									refType.setMimeType(WPSConstants.WPS_MIME_PLAIN);
									refType.setHref(sServerUrl[0]);
								} else {
									LiteralDataType literal = outType.addNewData().addNewLiteralData();
									if (param.getUnitOfMeasure() != null)
										literal.setUom(param.getUnitOfMeasure());
									literal.setDataType(((WPSOperatorDescriptionParameterLiteral)param).getType().name());
									literal.setStringValue(WPSOperatorDescription.convertToString(objOut, ((WPSOperatorDescriptionParameterLiteral)param).getType(), true));
								}
							}
						} else {
							throw new ServerException(ExceptionCode.InvalidParameterValue, param.getIdentifier(), "Output parameter value is not valid.");
						}
					} else {
						XmlObject xmlObject = null;
						ObjectXmlParser parser = new ObjectXmlParser(WPSServlet.getXmlObjectParser());
						xmlObject = parser.parseObjectAsDocument(objOut, WPSServlet.getXmlObjectParser());
						// validate schema
						String sSchemaPath = this.validateSchema(op.getOperatorDescription().getIdentifier(), xmlObject, (WPSOperatorDescriptionParameterComplex)param, false);
						// create output
						if (bReturnRawData && sRawOutputName.equals(param.getIdentifier())) {
							return xmlObject;
						} else {
							if (bReference) {
								// save data on server
								String[] sServerUrl = {null};
								File outFile = Request.createDataFile(".xml", sServerUrl);
								XmlOptions options = new XmlOptions(WPSServlet.getXmlOptions());
								options.setCharacterEncoding((String)arrOutFormat[1]);
								xmlObject.save(outFile, options);
								OutputReferenceType refType = outType.addNewReference();
								refType.setEncoding((String)arrOutFormat[1]);
								refType.setMimeType(WPSConstants.WPS_MIME_XML);
								refType.setSchema(sSchemaPath);
								refType.setHref(sServerUrl[0]);
							} else {
								ComplexDataType complex = outType.addNewData().addNewComplexData();
								complex.setMimeType(WPSConstants.WPS_MIME_XML);
								complex.setEncoding(WPSConstants.WPS_ENCODING);
								complex.setSchema(sSchemaPath);
								complex.set(xmlObject);
							}
						}
					}
				} else {
					// no result value for this parameter
				}
			}
			if (bReturnRawData)
				return XmlObject.Factory.parse("<" + RAW_OUTPUT_NAME +"></"+RAW_OUTPUT_NAME+">");
			StatusType status = doc.getExecuteResponse().addNewStatus();
			status.setCreationTime(new GregorianCalendar());
			status.setProcessSucceeded("Process finished successfully.");
			return doc;
		} catch (Exception ex) {
			if (!(ex instanceof ServerException)) {
				ex = new ServerException(ExceptionCode.NoApplicableCode, "", "Execution of operator '"+op.getOperatorDescription().getIdentifier()+"' failed. ("+ex.toString()+":"+ex.getMessage()+")");
			}
			ExecuteResponseDocument doc = this.initResponseDocument(op, bIncludeInput);
    		StatusType status = doc.getExecuteResponse().addNewStatus();
			status.setCreationTime(new GregorianCalendar());
    		ExceptionType exception = status.addNewProcessFailed().addNewExceptionReport().addNewException();    		
    		exception.setExceptionCode(((ServerException)ex).getExceptionCode().name());
    		exception.setLocator(((ServerException)ex).getLocator());
    		exception.addExceptionText(ex.getMessage());
			return doc;
		} catch (Error er) {
			System.gc();
			er.printStackTrace();
			ExecuteResponseDocument doc = this.initResponseDocument(op, bIncludeInput);
    		StatusType status = doc.getExecuteResponse().addNewStatus();
			status.setCreationTime(new GregorianCalendar());
    		ExceptionType exception = status.addNewProcessFailed().addNewExceptionReport().addNewException();
    		if (er instanceof OutOfMemoryError)
    			exception.setExceptionCode(ExceptionCode.NotEnoughStorage.name());
    		else
    			exception.setExceptionCode(ExceptionCode.NoApplicableCode.name());
    		exception.setLocator("");
    		exception.addExceptionText(er.toString());
			return doc;
		}
	}

	/**
	 * Initializes response document.
	 * @return ExecuteResponseDocument	The result document.
	 * @param operator					The operator.
	 * @param bIncludeInput				True whether the request parameters should be included.
	 */
	ExecuteResponseDocument initResponseDocument(IWPSOperator operator, boolean bIncludeInput) {
		ExecuteResponseDocument doc = ExecuteResponseDocument.Factory.newInstance();
		ExecuteResponse response = doc.addNewExecuteResponse();
		response.setService(WPSConstants.WPS_SERVICE);
		response.setVersion(WPSConstants.WPS_VERSION);
		response.setLang(WPS_LANGUAGE);
		response.setServiceInstance(WPSServlet.getCapabilities().getServerUrl());
		ProcessBriefType process = response.addNewProcess();
		this.setProcessInformation(process, operator);
		if (bIncludeInput) {
			if (_document.getExecute().isSetDataInputs())
				response.addNewDataInputs().set(_document.getExecute().getDataInputs().copy());
			if (_document.getExecute().isSetResponseForm() && _document.getExecute().getResponseForm().isSetResponseDocument()) {
				Iterator<DocumentOutputDefinitionType> iterOutput = _document.getExecute().getResponseForm().getResponseDocument().getOutputList().iterator();
				OutputDefinitionsType outType = response.addNewOutputDefinitions();
				while (iterOutput.hasNext()) {
					outType.addNewOutput().set(iterOutput.next().copy());
				}
			}
		}
		return doc;
	}

	/**
	 * Gets the response document contains a timeout error.
	 * @return ExecuteResponseDocument	The response document.
	 * @param operator					The operator.
	 * @param bIncludeInput				True when the input is included in the response document.
	 */
    protected ExecuteResponseDocument getTimeoutResponse(IWPSOperator operator, boolean bIncludeInput) {
    	ExecuteResponseDocument doc = this.initResponseDocument(operator, bIncludeInput);
		StatusType status = doc.getExecuteResponse().addNewStatus();
		status.setCreationTime(new GregorianCalendar());
		ExceptionType exception = status.addNewProcessFailed().addNewExceptionReport().addNewException();    		
		exception.setExceptionCode(ExceptionCode.NoApplicableCode.name());
		exception.setLocator("");
		exception.addExceptionText("Timeout has been reached.");
		return doc;
	}

    /**
	 * Sets the progress status.
	 * @return void
	 * @param dStatus							The progress status. Its a value between 0.0 and 1.0.
	 * @param sStatusText						The progress status text. Can be null.
	 */
	public void setStatus(double dStatus, String sStatusText) {
		// do nothing
	}
}
