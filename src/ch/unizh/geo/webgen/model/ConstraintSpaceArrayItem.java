package ch.unizh.geo.webgen.model;

public class ConstraintSpaceArrayItem extends Object {
    private double value = 0; 
    private int	   id	 = 0;

	/** Creates a new instance of SortedArray 
	 */
	public ConstraintSpaceArrayItem(double _value){
	  this.value 	= _value;
	}
    
	/** Creates a new instance of SortedArray
	 */
	public ConstraintSpaceArrayItem(double _value, int _id){
	  this.value 	= _value;
	  this.id		= _id;
	}
    
	/** accessor method: get value 
	 */
	public double getValue() {
	  return value;
	}
    

	/** accessor method: set value 
	 */
	public void setValue(double _value) {
	  this.value=_value;
	}

	/** accessor method: get id 
	 */
	public int getId() {
	  return id;
	}

	/** Comparator 
	 */
	public void setId(int _id) {
	  this.id=_id;
	}
	
	public int compareTo(ConstraintSpaceArrayItem obj) {
		if (obj.getValue() > value)
			return(1);
		if (obj.getValue() < value)
			return(-1);
		return(0);
	}
	
	public String toString() {
		return id+"("+value+")";
	}
}
