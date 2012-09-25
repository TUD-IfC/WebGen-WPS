package ica.wps.server;

/**
 * Exception code enumeration containing the official exception codes.
 * @author	M. Wittensoeldner
 * @date	Created on 01.02.2007
 */
public enum ExceptionCode {
	// exceptionCode			// "locator" value
	
	OperationNotSupported,		// Name of operation not supported
	MissingParameterValue,		// Name of missing parameter
	InvalidParameterValue,		// Name of parameter with invalid value
	ResourceNotFound,			// A short description what went wrong (i.e. the name of the faulty or required class)
	NoApplicableCode,			// None, omit "locator" parameter
	NotEnoughStorage,			// None, omit "locator" parameter
	ServerBusy,					// None, omit "locator" parameter
	FileSizeExceeded,			// Identifier of the parameter which exceeded the maximum file size
	StorageNotSupported,		// None, omit "locator" parameter
	VersionNegotiationFailed,	// Identifier of the Input which could not be accessed
}

