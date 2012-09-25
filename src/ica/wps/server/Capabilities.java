package ica.wps.server;

import ica.wps.common.IWPSXmlObjectParser;
import ica.wps.data.WPSServerDescription;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Container class holds the server capabilities.
 * @author	M. Wittensoeldner
 * @date	Created on 06.02.2007
 */
public class Capabilities implements IWPSServer {
	protected WPSServerDescription			_serverDescription;
	protected String						_sServerUrl;
	protected List<IWPSOperator>			_lstOperators = null;
	protected HashMap<String, IWPSOperator>	_mapOperators = null;
	protected Date							_date = null;
	protected Calendar						_calendar = new GregorianCalendar();
	
	public Capabilities() {
	}
	
	/**
	 * Checks whether the capabilities needs to be refreshed.
	 * @return boolean							True when the capabilities needs a refresh.
	 */
	public boolean needsRefresh() {
		if (_date == null)
			return true;
		GregorianCalendar calendarOld = new GregorianCalendar();
		calendarOld.setTime(_date);
		GregorianCalendar calendarNew = new GregorianCalendar();
		Date dateNow = new Date();
		calendarNew.setTime(dateNow);
		if ((calendarOld.get(Calendar.DAY_OF_YEAR) != calendarNew.get(Calendar.DAY_OF_YEAR))
			|| (calendarOld.get(Calendar.YEAR) != calendarNew.get(Calendar.YEAR)))
			return true;
		return false;
	}
	
	/**
	 * Initializes the content.
	 * @return void
	 * @param server							The server.
	 * @param request							The servlet request.
	 */
	public void init(IWPSServer server, String sRequestUrl) {
		_date = new Date();
		_serverDescription = server.getServerDescription();
		List<IWPSOperator> lstOperators = server.getOperators();
		if (lstOperators != null) {
			_lstOperators = new LinkedList<IWPSOperator>(lstOperators);
			_mapOperators = new HashMap<String, IWPSOperator>();
			IWPSOperator op;
			Iterator<IWPSOperator> iterOps = _lstOperators.iterator();
			while (iterOps.hasNext()) {
				op = iterOps.next();
				_mapOperators.put(op.getOperatorDescription().getIdentifier(), op);
			}
		}

		_sServerUrl = sRequestUrl;
	}
	
	/**
	 * Initializes the server.
	 * @return void
	 * @param classLoader						The class loader.
	 */
	public void init(ClassLoader classLoader) {
		
	}

	/**
	 * Gets the server description
	 * @return WPSServerDescription				The server description.
	 */
	public WPSServerDescription getServerDescription() {
		return _serverDescription;
	}

	/**
	 * Gets the xml parser.
	 * @return IWPSXmlObjectParser				The xml parser.
	 */
	public IWPSXmlObjectParser getXmlParser() {
		return null;
	}
	
	/**
	 * Gets the server url.
	 * @return String							The server url.
	 */
	public String getServerUrl() {
		return _sServerUrl;
	}

	/**
	 * Gets the WPS operators.
	 * @return List<IWPSOperator>				The WPS operators.
	 */
	public List<IWPSOperator> getOperators() {
		return _lstOperators;
	}

	/**
	 * Gets the specified operator.
	 * @return IWPSOperator						The operator.
	 * @param sIdentifier						The operator identifier.
	 */
	public IWPSOperator getOperator(String sIdentifier) {
		return _mapOperators.get(sIdentifier);
	}
}
