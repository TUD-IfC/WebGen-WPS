package ica.wps.server;

/**
 * Exception class holds an exception code.
 * @author	M. Wittensoeldner
 * @date	Created on 01.02.2007
 */
public class ServerException extends Exception {
	static final long			serialVersionUID = 0L;
	protected ExceptionCode		_exceptionCode;
	protected String			_sLocator = null;
	
	/**
	 * Constructor.
	 * @param exceptionCode						The exception code.
	 */
	public ServerException(ExceptionCode exceptionCode) {
		super();
		_exceptionCode = exceptionCode;
	}

	/**
	 * Constructor.
	 * @param exceptionCode						The exception code.
	 * @param sLocator							The exception locator. See definition of enum ExceptionCode. Can be null.
	 * @param sMessage							The exception message. Can be null.
	 */
	public ServerException(ExceptionCode exceptionCode, String sLocator, String sMessage) {
		super(sMessage);
		_exceptionCode = exceptionCode;
		_sLocator = sLocator;
	}
	
	/**
	 * Gets the exception code.
	 * @return ExceptionCode					The exception code.
	 */
	public ExceptionCode getExceptionCode() {
		return _exceptionCode;
	}

	/**
	 * Gets the locator.
	 * @return String							The locator. Can be null.
	 */
	public String getLocator() {
		return _sLocator;
	}
}

