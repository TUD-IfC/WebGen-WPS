package ica.wps.server.request.execute;

import java.util.GregorianCalendar;
import java.util.Map;

import ica.wps.server.ServerException;
import ica.wps.server.IWPSOperator;

import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.StatusType;
import net.opengis.ows.x11.ExceptionType;


/**
 * Thread runs the operator.
 * @author	M. Wittensoeldner
 * @date	Created on 11.03.2007
 */
public class ThreadExecuteDirect extends Thread {
	protected RequestExecute		_request;
	protected IWPSOperator			_operator;
	protected Map<String, Object[]>	_mapParameterOutFormat;
	protected boolean				_bIncludeInput;
	ExecuteResponseDocument			_doc;

	/**
	 * Constructor.
	 * @param request					The execute request.
	 * @param operator					The operator to run.
	 * @param mapParameterOutFormat		The requested output format for output values.
	 * @param bIncludeInput				True when the input is included in the response document.
	 */
	public ThreadExecuteDirect(RequestExecute request,
								IWPSOperator operator,
								Map<String, Object[]> mapParameterOutFormat,
								boolean bIncludeInput) {
		_request = request;
		_operator = operator;
		_mapParameterOutFormat = mapParameterOutFormat;
		_bIncludeInput = bIncludeInput;
	}

	/**
	 * Runs the thread internal.
	 * @return void
	 */
    public void run() {
    	try {
    		_doc = (ExecuteResponseDocument)_request.runOperator(_operator, _mapParameterOutFormat, _bIncludeInput, null, _request);
    	} catch (ServerException ex) {
    		_doc = _request.initResponseDocument(_operator, false);
    		StatusType status = _doc.getExecuteResponse().addNewStatus();
			status.setCreationTime(new GregorianCalendar());
    		ExceptionType exception = status.addNewProcessFailed().addNewExceptionReport().addNewException();    		
    		exception.setExceptionCode(ex.getExceptionCode().name());
    		exception.setLocator(ex.getLocator());
    		exception.addExceptionText(ex.getMessage());
    	}
	}

	/**
	 * Gets the response document.
	 * @return ExecuteResponseDocument			The response document.
	 */
    public ExecuteResponseDocument getResponseDocument() {
    	return _doc;
	}
}
