package ica.wps.server;

import ica.wps.common.IWPSStatusListener;
import ica.wps.data.WPSOperatorDescription;

import java.util.Map;
import java.util.List;

/**
 * Interface defines a WPS operator.
 * @author	M. Wittensoeldner
 * @date	Created on 04.02.2007
 */
public interface IWPSOperator {

	/**
	 * Executes the operator.
	 * @return Map<String, Object>				The output parameters.
	 * @param mapParameterIn					The input parameters.
	 * @param statusListener					The status listener.
	 * @throws Exception						When an error occurs.
	 */
	public Map<String, Object> execute(Map<String, List<Object>> mapParameterIn, IWPSStatusListener statusListener) throws Exception;
	
	/**
	 * Gets the operator description.
	 * @return WPSOperatorDescription			The operator description.
	 */
	public WPSOperatorDescription getOperatorDescription();
}

