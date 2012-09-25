package ch.unizh.geo.webgen.model;

public class ConstrainedFeatureCollectionSorted extends Object {
    private double cost = 0; 
    private ConstrainedFeatureCollection fc;
    String  operation;
    Double[] costVec;
    int operationID;
    int steps = 1;;

	/** Creates a new instance of SortedArray 
	 */
	public ConstrainedFeatureCollectionSorted(double _cost){
	  this.cost	= _cost;
	}
    
	/** Creates a new instance of SortedArray
	 */
	public ConstrainedFeatureCollectionSorted(double _cost, ConstrainedFeatureCollection _fc){
	  this.cost 	= _cost;
	  this.fc		= _fc;
	}

	/** Creates a new instance of SortedArray
	 */
	public ConstrainedFeatureCollectionSorted(double _cost, ConstrainedFeatureCollection _fc, String _operation, int _operationID, Double[] _costVec){
	  this.cost 	 = _cost;
	  this.fc		 = _fc;
	  this.operation = _operation;
	  this.costVec   = _costVec;
	  this.operationID = _operationID;
	}
	
	/** Creates a new instance of SortedArray
	 */
	public ConstrainedFeatureCollectionSorted(double _cost, ConstrainedFeatureCollection _fc, String _operation){
	  this.cost 	 = _cost;
	  this.fc		 = _fc;
	  this.operation = _operation;
	}
	
	/** accessor method: get value 
	 */
	public double getCost() {
	  return cost;
	}
    

	/** accessor method: set value 
	 */
	public void setCost(double _cost) {
	  this.cost = _cost;
	}

	/** accessor method: get id 
	 */
	public ConstrainedFeatureCollection getFeatureCollection() {
	  return fc;
	}

	/** accessor method: get operation 
	 */
	public String getOperation() {
	  return operation;
	}
	
	/** accessor method: get operation id
	 */
	public int getOperationID() {
	  return operationID;
	}
	
	/** accessor method: get cost vector 
	 */
	public Double[] getCostVec() {
	  return costVec;
	}
	
	
	/** accessor method: get processing steps 
	 */
	public int getProcessingSteps() {
	  return steps;
	}
    

	/** accessor method: set rocessing steps  
	 */
	public void setProcessingSteps(int _steps) {
	  this.steps = _steps;
	}
	
	/** modifyer method: update rocessing steps  
	 */
	public void updateProcessingSteps(int _depth) {
	  this.steps *= _depth;
	}
	
	/** Comparator 
	 */
	public void setFeatureCollection(ConstrainedFeatureCollection _fc) {
	  this.fc = _fc;
	}
	
	public int compareTo(ConstrainedFeatureCollectionSorted obj) {
		if (obj.getCost() > cost)
			return(-1);
		if (obj.getCost() < cost)
			return(1);
		return(0);
	}
	
	public String toString() {
		return operation + "=" + cost;
	}

}
