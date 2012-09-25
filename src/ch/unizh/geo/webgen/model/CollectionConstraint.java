package ch.unizh.geo.webgen.model;

import java.util.ArrayList;

import ch.unizh.geo.webgen.tools.CollectionHelper;

import com.vividsolutions.jump.feature.FeatureCollection;

public class CollectionConstraint {
	
	ArrayList<double[]> statehistory = new ArrayList<double[]>();
	ArrayList<String> statehistorymessages = new ArrayList<String>();

	public CollectionConstraint() {
		//standard constructor
	}
	
	public CollectionConstraint(double origBWRatio) {
		this.origBWRatio = origBWRatio;
	}
	
	public CollectionConstraint(ConstrainedFeatureCollection fc, FeatureCollection cfc) {
		this.origBWRatio = CollectionHelper.calculateBWRatio(fc, cfc);
	}
	
	//init original constraint settings
	public void initConstraint(double origBWRatio) {
		this.origBWRatio = origBWRatio;
	}
	
	public void initConstraint(ConstrainedFeatureCollection fc, FeatureCollection cfc) {
		this.origBWRatio = CollectionHelper.calculateBWRatio(fc, cfc);
	}
	
	
	//constraint history functionalities
	public int getHistorySize() {
		return statehistory.size();
	}
	
	public double[] getHistoryFirst() {
		if(statehistory.size() > 0)
			return (double[])statehistory.get(0);
		return null;
	}

	public double[] getHistorySecondLast() {
		if(statehistory.size() > 1)
			return (double[])statehistory.get(statehistory.size()-2);
		return null;
	}
	
	public double[] getHistoryLast() {
		if(statehistory.size() > 0)
			return (double[])statehistory.get(statehistory.size()-1);
		return null;
	}
	
	public double[] getStateFromHistory(int i) {
		//System.out.println(i + " " + statehistory.size());
		try {
			return (double[])statehistory.get(i);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}
	public String getStateMessageFromHistory(int i) {
		try {
			return statehistorymessages.get(i).toString();
		}
		catch (IndexOutOfBoundsException e) {
			return "no message";
		}
	}
	
	public void updateHistory(String message) {
		statehistory.add(this.getCostValues());
		statehistorymessages.add(message);
	}
	
	public void addHistoryLast(double[] newvalues, String message) {
		statehistory.add(newvalues);
		statehistorymessages.add(message);
		this.bwRatioSev = newvalues[0];
	}
	
	

	//edge count difference
	double origBWRatio;
	double actBWRatio;
	double bwRatioSev;
	public double getOrigBWRatio() {
		return origBWRatio;
	}
	public void setActBWRatio(double nbwr) {
		this.actBWRatio = nbwr;
	}
	public double getActBWRatio() {
		return actBWRatio;
	}
	public void setSeverityDiffBWRatio(double dbwr) {
		this.bwRatioSev = dbwr;
	}
	public double getSeverityDiffBWRatio() {
		return bwRatioSev;
	}
	
	


	//other functionalities
	public String toString() {
		return "severities: bwRatio="+getSeverityDiffBWRatio();
	}
	
	
	@SuppressWarnings("unchecked")
	public CollectionConstraint clone() {
		//return this.clone();
		CollectionConstraint tcconst = new CollectionConstraint(this.origBWRatio);
		tcconst.statehistory = (ArrayList)this.statehistory.clone();
		tcconst.statehistorymessages = (ArrayList<String>)this.statehistorymessages.clone();
		return tcconst;
	}
	
	
	int nbrConstraints = 1;
	public int getNbrConstraints() {
		return this.nbrConstraints;
	}
	public double[] getCostValues() {
		double[] cost = new double[nbrConstraints];
		cost[0] = this.getSeverityDiffBWRatio();
		return cost;
	}
}
