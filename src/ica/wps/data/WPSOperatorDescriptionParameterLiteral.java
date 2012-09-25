package ica.wps.data;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Date;

import ica.wps.data.WPSOperatorDescription.DatatypeLiteral;
import ica.wps.data.WPSOperatorDescription.Range;

/**
 * Class defines a single literal parameter.
 * @author	M. Wittensoeldner
 * @date	Created on 04.02.2007
 */
public class WPSOperatorDescriptionParameterLiteral extends WPSOperatorDescriptionParameter {

	protected DatatypeLiteral		_eType;
	protected Object				_objDefaultValue = null;
	protected List<Range>			_lstAllowedRanges = null;
	protected List<Object>			_lstAllowedValues = null;
	
	/**
	 * Constructor.
	 * @param sIdentifier						The parameter identifier.
	 * @param sTitle							The parameter title (visible parameter name).
	 * @param sDescription						The parameter description.
	 * @param sUnitOfMeasure					The parameter unit.
	 * @param nMinOccurs						The minimal occurance.
	 * @param nMaxOccurs						The maximal occurance.
	 * @param eType								The data type.
	 * @param objDefaultValue					The default value. Can be null.
	 * @param arrAllowedRanges					The allowed value ranges for this parameter. Can be null.
	 * @param arrAllowedValues					The allowed values for this parameter. Can be null.
	 * @throws Exception						When an error occurs.
	 */
	public WPSOperatorDescriptionParameterLiteral(String sIdentifier,
													String sTitle,
													String sDescription,
													String sUnitOfMeasure,
													int nMinOccurs,
													int nMaxOccurs,
													DatatypeLiteral eType,
													Object objDefaultValue,
													Range[] arrAllowedRanges,
													Object[] arrAllowedValues) throws Exception {
		super(sIdentifier, sTitle, sDescription, sUnitOfMeasure, nMinOccurs, nMaxOccurs);
		int i;
		_eType = eType;
		if (objDefaultValue != null) {
			if (this.isValidType(objDefaultValue))
				_objDefaultValue = objDefaultValue;
			else
				throw new Exception("Default value is invalid.");
		}
		if ((arrAllowedRanges != null) && (arrAllowedRanges.length > 0)) {
			_lstAllowedRanges = new LinkedList<Range>();
			i = 0;
			while (i < arrAllowedRanges.length) {
				if (this.isValidType(arrAllowedRanges[i].getMinValue()) && this.isValidType(arrAllowedRanges[i].getMaxValue()))
					_lstAllowedRanges.add(arrAllowedRanges[i]);
				else
					throw new Exception("Range values are invalid.");
				i++;
			}
		}
		if (arrAllowedValues != null) {
			_lstAllowedValues = new LinkedList<Object>();
			i = 0;
			while (i < arrAllowedValues.length) {
				if (arrAllowedValues[i] != null) {
					if (this.isValidType(arrAllowedValues[i])) {
						_lstAllowedValues.add(arrAllowedValues[i]);
					} else {
						_lstAllowedValues.clear();
						_lstAllowedValues = null;
						throw new Exception("Allowed values are invalid.");
					}
				}
				i++;
			}
		}
	}

	/**
	 * Gets the datatype.
	 * @return DatatypeLiteral					The parameter data type.
	 */
	public DatatypeLiteral getType() {
		return _eType;
	}

	/**
	 * Gets the default value.
	 * @return Object							The default parameter value.
	 */
	public Object getDefaultValue() {
		return _objDefaultValue;
	}

	/**
	 * Gets the allowed value range.
	 * @return List<Range>						The allowed value ranges.
	 */
	public List<Range> getAllowedRanges() {
		return _lstAllowedRanges;
	}

	/**
	 * Gets the allowed parameter values.
	 * @return List<Object>						The allowed parameter values.
	 */
	public List<Object> getAllowedValues() {
		return _lstAllowedValues;
	}

	/**
	 * Checks whether an object has the correct literal type.
	 * @return boolean							True when valid.
	 * @param obj								The object to check.
	 */
	protected boolean isValidType(Object obj) {
		if (obj != null) {
			if (_eType == DatatypeLiteral.String) {
				return obj instanceof String;
			} else if (_eType == DatatypeLiteral.Integer) {
				return obj instanceof Integer;
			} else if (_eType == DatatypeLiteral.Long) {
				return obj instanceof Long;
			} else if (_eType == DatatypeLiteral.Double) {
				return obj instanceof Double;
			} else if (_eType == DatatypeLiteral.Boolean) {
				return obj instanceof Boolean;
			} else if (_eType == DatatypeLiteral.Date) {
				return obj instanceof java.util.Date;
			}
		}
		return false;
	}

	/**
	 * Checks whether an object has a valid value.
	 * @return boolean							True when valid.
	 * @param obj								The object to check.
	 */
	public boolean isValid(Object obj) {
		boolean bResult = false;
		if (obj != null) {
			bResult = this.isValidType(obj);
			if (bResult) {
				if (_lstAllowedRanges != null) {
					bResult = false;
					Iterator<Range> iterRange = _lstAllowedRanges.iterator();
					Range range;
					while (iterRange.hasNext()) {
						range = iterRange.next();
						bResult |= (this.compareValue(range.getMinValue(), obj) <= 0) && (this.compareValue(range.getMaxValue(), obj) >= 0);
					}
				}
			}
			if (bResult) {
				if (_lstAllowedValues != null) {
					bResult = false;
					Iterator<Object> iterObj = _lstAllowedValues.iterator();
					while (iterObj.hasNext()) {
						bResult |= this.compareValue(iterObj.next(), obj) == 0;
					}
				}
			}
		}
		return bResult;
	}

	/**
	 * Compares the values of two objects
	 * @return int								The value 0 if the value of obj1 is equal to the value of obj2;
	 * 											A value less than 0 if the value of obj1 is less than the the value of obj2;
	 * 											And a value greater than 0 if the value of obj1 is greater than the the value of obj2.
	 * @param obj1								The first object.
	 * @param obj2								The second object.
	 */
	private int compareValue(Object obj1, Object obj2) {
		if (_eType == DatatypeLiteral.String) {
			return ((String)obj1).compareTo((String)obj2);
		} else if (_eType == DatatypeLiteral.Integer) {
			return ((Integer)obj1).compareTo((Integer)obj2);
		} else if (_eType == DatatypeLiteral.Long) {
			return ((Long)obj1).compareTo((Long)obj2);
		} else if (_eType == DatatypeLiteral.Double) {
			return ((Double)obj1).compareTo((Double)obj2);
		} else if (_eType == DatatypeLiteral.Boolean) {
			return ((Boolean)obj1).compareTo((Boolean)obj2);
		} else if (_eType == DatatypeLiteral.Date) {
			return ((Date)obj1).compareTo((Date)obj2);
		}
		return -13;
	}
}

