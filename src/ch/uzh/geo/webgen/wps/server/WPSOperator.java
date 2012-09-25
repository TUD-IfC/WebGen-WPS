package ch.uzh.geo.webgen.wps.server;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Iterator;

import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

import ica.wps.common.IWPSStatusListener;
import ica.wps.data.WPSOperatorDescription;
import ica.wps.server.IWPSOperator;

/**
 * Interface defines a WPS operator.
 * @author	M. Wittensoeldner
 * @date	Created on 04.02.2007
 */
public class WPSOperator implements IWPSOperator {

	protected WPSOperatorDescription	_description;
	protected ClassLoader				_classLoader;
	
	/**
	 * Constructor.
	 * @param description						The operator description.
	 * @param classLoader						The class loader.
	 */
	WPSOperator(WPSOperatorDescription description, ClassLoader classLoader) {
		_description = description;
		_classLoader = classLoader;
	}
	/**
	 * Executes the operator.
	 * @return Map<String, Object>		The output parameters.
	 * @param mapParameterIn					The input parameters.
	 * @param statusListener					The status listener.
	 * @throws Exception						When an error occurs.
	 */
	public Map<String, Object> execute(Map<String, List<Object>> mapParameterIn, IWPSStatusListener statusListener) throws Exception {
		IWebGenAlgorithm algo = (IWebGenAlgorithm)_classLoader.loadClass(_description.getIdentifier()).newInstance();
		WebGenRequest wgreq = new WebGenRequest();
		Entry<String, List<Object>> entry;
		Iterator<Entry<String, List<Object>>> iterParam = mapParameterIn.entrySet().iterator();
		while (iterParam.hasNext()) {
			entry = iterParam.next();
			wgreq.addParameter(entry.getKey(), entry.getValue().iterator().next());
		}
		algo.run(wgreq);
		Map<String, Object> mapResult = new HashMap<String, Object>();
		Entry<String, Object> entry2;
		Iterator<Entry<String, Object>> iterResult = wgreq.getResults().entrySet().iterator();
		while (iterResult.hasNext()) {
			entry2 = iterResult.next();
			mapResult.put(entry2.getKey(), entry2.getValue());
		}
		return mapResult;
	}
	
	/**
	 * Gets the operator description.
	 * @return WPSOperatorDescription			The operator description.
	 */
	public WPSOperatorDescription getOperatorDescription() {
		return _description;
	}
}

