package ica.wps.server.request.execute;

import java.io.File;
import java.util.GregorianCalendar;
import java.util.Map;

import ica.wps.common.IWPSStatusListener;
import ica.wps.server.ExceptionCode;
import ica.wps.server.ServerException;
import ica.wps.server.IWPSOperator;
import ica.wps.server.WPSServlet;
import ica.wps.server.request.Request;

import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.ProcessStartedType;
import net.opengis.wps.x100.StatusType;
import net.opengis.ows.x11.ExceptionType;

import org.apache.xmlbeans.XmlObject;

/**
 * Thread runs the operator.
 * @author	M. Wittensoeldner
 * @date	Created on 10.02.2007
 */
public class ThreadExecuteStatus extends Thread implements IWPSStatusListener {
	protected File					_file;
	protected String				_sUrl;
	protected RequestExecute		_request;
	protected IWPSOperator			_operator;
	protected Map<String, Object[]>	_mapParameterOutFormat;
	protected boolean				_bIncludeInput;
	protected boolean				_bUpdateStatus;

	/**
	 * Constructor.
	 * @param file						The file where the result has to be store.
	 * @param sUrl						The url of the file.
	 * @param request					The execute request.
	 * @param op						The operator to run.
	 * @param mapParameterOutFormat		The requested output format for output values.
	 * @param bIncludeInput				True when the input is included in the response document.
	 * @param bUpdateStatus				True when the progress status has to be updated.
	 */
	public ThreadExecuteStatus(File file,
								String sUrl,
								RequestExecute request,
								IWPSOperator operator,
								Map<String, Object[]> mapParameterOutFormat,
								boolean bIncludeInput,
								boolean bUpdateStatus) {
		_file = file;
		_sUrl = sUrl;
		_request = request;
		_operator = operator;
		_mapParameterOutFormat = mapParameterOutFormat;
		_bIncludeInput = bIncludeInput;
		_bUpdateStatus = bUpdateStatus;
	}

	/**
	 * Runs the thread internal.
	 * @return void
	 */
    public void run() {
    	try {
    		ExecuteResponseDocument doc = (ExecuteResponseDocument)_request.runOperator(_operator, _mapParameterOutFormat, _bIncludeInput, null, this);
    		doc.getExecuteResponse().setStatusLocation(_sUrl);
    		this.updateDocument(doc);
    	} catch (ServerException ex) {
    		ExecuteResponseDocument doc = _request.initResponseDocument(_operator, false);
    		StatusType status = doc.getExecuteResponse().addNewStatus();
			status.setCreationTime(new GregorianCalendar());
    		ExceptionType exception = status.addNewProcessFailed().addNewExceptionReport().addNewException();    		
    		exception.setExceptionCode(ex.getExceptionCode().name());
    		exception.setLocator(ex.getLocator());
    		exception.addExceptionText(ex.getMessage());
    		// try 5 times to save the document
    		int i = 0;
    		while (i < 5) {
    			try {
    				this.updateDocument(doc);
    			} catch (ServerException e) {
    				System.out.println(e.getMessage());
    			}
    			i++;
    		}
    	}
	}

	/**
	 * Updates the document on the server.
	 * @return void
	 * @throws ServerException			When an error occurs.
	 */
    public void updateDocument(XmlObject doc) throws ServerException {
    	try {
    		File file = Request.createDataFile(".xml", null);
			doc.save(file, WPSServlet.getXmlOptions());
			if (!_file.delete())
				throw new Exception("Deleting file failed.");
			if (!file.renameTo(_file))
				throw new Exception("Renaming file failed.");
    	} catch (Exception ex) {
    		throw new ServerException(ExceptionCode.NotEnoughStorage, "", "Saving document failed: "+ex.getMessage());
    	}
	}

	/**
	 * Handles a timeout error.
	 * @return void
	 */
    public void handleTimeout() {
		// try 5 times to save the document
		int i = 0;
		while (i < 5) {
			try {
	    		this.updateDocument(_request.getTimeoutResponse(_operator, _bIncludeInput));
			} catch (ServerException e) {
				System.out.println(e.getMessage());
			}
			i++;
		}
	}

    /**
	 * Sets the progress status.
	 * @return void
	 * @param dStatus							The progress status. Its a value between 0.0 and 1.0.
	 * @param sStatusText						The progress status text. Can be null.
	 */
	public void setStatus(double dStatus, String sStatusText) {
		try {
			if (_bUpdateStatus) {
				if (sStatusText == null)
					sStatusText = "Processing...";
				ExecuteResponseDocument doc = _request.initResponseDocument(_operator, false);
				doc.getExecuteResponse().setStatusLocation(_sUrl);
				StatusType status = doc.getExecuteResponse().addNewStatus();
				status.setCreationTime(new GregorianCalendar());
				ProcessStartedType started = status.addNewProcessStarted();
				started.setStringValue(sStatusText);
				int nPercent = (int)(100.0*dStatus);
				nPercent = Math.min(nPercent, 99);
				nPercent = Math.max(nPercent, 0);
				started.setPercentCompleted(nPercent);
				this.updateDocument(doc);
			}
		} catch (ServerException ex) {
			System.out.println(ex.getMessage());
		}
	}
}
