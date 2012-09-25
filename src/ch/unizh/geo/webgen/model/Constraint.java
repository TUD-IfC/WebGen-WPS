package ch.unizh.geo.webgen.model;

import java.util.ArrayList;

import ch.unizh.geo.measures.OrientationMBR;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;

public class Constraint {
	
	ArrayList<double[]> statehistory = new ArrayList<double[]>();
	ArrayList<String> statehistorymessages = new ArrayList<String>();
	
	public Constraint() {
		//standard constructor
	}
	
	public Constraint(Point origpos, int origedgecount, double origwlratio, double origorientation) {
		//init constructor
		this.origpos = origpos;
		this.origedgecount = origedgecount;
		this.origwlratio = origwlratio;
		this.origorientation = origorientation;
	}
	
	public Constraint(Feature feat) {
		Geometry geom = feat.getGeometry();
		this.origpos = geom.getCentroid();
		this.origedgecount = geom.getNumPoints() - 1 ;
		try {
			OrientationMBR myMbrcalc = new OrientationMBR(geom); 
			this.origwlratio = myMbrcalc.getMbrWidth()/myMbrcalc.getMbrLength();
			this.origorientation = myMbrcalc.getStatOrientation();
		}
		catch (Exception e) {
			//
		}
	}
	
	//init original constraint settings
	public void initConstraint(Point origpos, int origedgecount, double origwlratio, double origorientation) {
		//init constructor
		this.origpos = origpos;
		this.origedgecount = origedgecount;
		this.origwlratio = origwlratio;
		this.origorientation = origorientation;
	}
	
	public void initConstraint(Feature feat) {
		Geometry geom = feat.getGeometry();
		this.origpos = geom.getCentroid();
		this.origedgecount = geom.getNumPoints() - 1 ;
		OrientationMBR myMbrcalc = new OrientationMBR(geom); 
		this.origwlratio = myMbrcalc.getMbrWidth()/myMbrcalc.getMbrLength();
		this.origorientation = myMbrcalc.getStatOrientation();
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
		//mindists = new ArrayList();
		resetMinDistAvg();
	}
	
	public void addHistoryLast(double[] newvalues, String message) {
		statehistory.add(newvalues);
		statehistorymessages.add(message);
		//mindists = new ArrayList();
		resetMinDistAvg();
		this.minsizesev = newvalues[0];
		this.minlengthsev = newvalues[1];
		this.mindistsev = newvalues[2];
		this.localwidthsev = newvalues[3];
		this.diffpos = newvalues[4];
		this.diffedgecount = newvalues[5];
		this.diffwlratio = newvalues[6];
		this.difforientation = newvalues[7];
	}
	
	
	//minimum distance constraint
	double mindistsum = 0;
	int mindistcount;
	double mindistsev;
	public void addMinDist(double newmindist){
		mindistsum += newmindist;
		mindistcount++;
	}
	public double getMinDistAvg() {
		return mindistsum/mindistcount;
	}
	public void resetMinDistAvg() {
		mindistsum = 0;
		mindistcount = 0;
	}
	public void setSeverityMinDist(double md) {
		this.mindistsev = md;
	}
	public double getSeverityMinDist() {
		return this.mindistsev;
	}
	//new mindist (quotient von weight/mindist oder weight/roaddist)
	double mindistquotsum = 0;
	double mindistquotmax = 0;
	int mindistquotcount = 0;
	public void addMinDistQuot(double newmindistquot){
			mindistquotsum += newmindistquot;
			mindistquotcount++;
			if(newmindistquot > mindistquotmax) mindistquotmax = newmindistquot;
	}
	public double getMinDistAvgQuot() {
		return mindistquotsum/mindistquotcount;
	}
	public double getMinDistMaxQuot() {
		return mindistquotmax;
	}
	public void resetMinDistAvgQuot() {
		mindistquotsum = 0;
		mindistquotcount = 0;
	}

	
	//minimum length constraint
	double minlengthsev;
	public void setSeverityMinLength(double mls) {
		this.minlengthsev = mls;
	}
	public double getSeverityMinLength() {
		return this.minlengthsev;
	}
	
	//minimum size constraint
	double minsizesev;
	public void setSeverityMinSize(double ms) {
		this.minsizesev = ms;
	}
	public double getSeverityMinSize() {
		return this.minsizesev;
	}
	
	
	//local width constraint
	double localwidthsev;
	public void setSeverityLocalWidth(double lw) {
		this.localwidthsev = lw;
	}
	public double getSeverityLocalWidth() {
		return this.localwidthsev;
	}
	
	
	//position difference constraint
	Point origpos;
	Point newpos;
	double diffpos = 0.0;
	public void setNewPos(Point np) {
		this.newpos =  np;
	}
	public Point getOrigPos(){
		return origpos;
	}
	public Point getNewPos(){
		return newpos;
	}
	public void setSeverityDiffPos(double dp) {
		this.diffpos = dp;
	}
	public double getSeverityDiffPos() {
		return diffpos;
	}
	
	
	
	//edge count difference
	int origedgecount;
	int newedgecount;
	double diffedgecount = 0.0;
	public void setNewEdgeCount(int nec) {
		this.newedgecount = nec;
	}
	public void addOrigEdgeCount(int nec) {
		this.origedgecount += nec;
	}
	public int getOrigEdgeCount() {
		return origedgecount;
	}
	public int getNewEdgeCount() {
		return newedgecount;
	}
	public void setSeverityDiffEdgeCount(double dec) {
		this.diffedgecount = dec;
	}
	public double getSeverityDiffEdgeCount() {
		return diffedgecount;
	}
	
	
	
	//width-length ratio difference
	double origwlratio;
	double newwlratio;
	double diffwlratio = 0.0;
	public void setNewWLRatio(double nwlr) {
		this.newwlratio = nwlr;
	}
	public double getOrigWLRatio() {
		return origwlratio;
	}
	public double getNewWLRatio() {
		return newwlratio;
	}
	public void setSeverityDiffWLRatio(double dwlr) {
		this.diffwlratio = dwlr;
	}
	public double getSeverityDiffWLRatio() {
		return diffwlratio;
	}
	
	
	//width-length ratio difference
	double origorientation;
	double neworientation;
	double difforientation = 0.0;
	public void setNewOrientation(double nwo) {
		this.neworientation = nwo;
	}
	public double getOrigOrientation() {
		return origorientation;
	}
	public double getNewOrientation() {
		return neworientation;
	}
	public void setSeverityDiffOrientation(double dwo) {
		this.difforientation = dwo;
	}
	public double getSeverityDiffOrientation() {
		return difforientation;
	}
	
	

	//other functionalities
	public String toString() {
		return "severities: minsize="+getSeverityMinSize() +
			   "; minlen="+getSeverityMinLength() +
			   "; mindist="+getSeverityMinDist() +
			   "; localwidth="+getSeverityLocalWidth() +
			   "; dpos="+getSeverityDiffPos() +
			   "; dedgec="+getSeverityDiffEdgeCount() +
			   "; dwlratio="+getSeverityDiffWLRatio() +
			   "; dorient="+getSeverityDiffOrientation() +
			   "; histcount="+statehistory.size() +
			   "; opos=" + this.origpos.toString() + 
			   "; oedgc=" + this.origedgecount +
			   "; oorient=" + this.origorientation + 
			   "; owlr=" + this.origwlratio;
	}
	
	/*public static FeatureCollection addConstraintAttribute(FeatureCollection fc) {
		//ignore if ConstrainedFeature (remove soon)
		if(fc instanceof ConstrainedFeatureCollection) return fc;
		//else
		FeatureSchema fsold = (FeatureSchema) fc.getFeatureSchema();
		FeatureSchema fs = new FeatureSchema();
		for(int i=0; i<fsold.getAttributeCount(); i++) {
			fs.addAttribute(fsold.getAttributeName(i), fsold.getAttributeType(i));
		}
		if(!fs.hasAttribute("constraint")) {
			fs.addAttribute("constraint", AttributeType.OBJECT);
		}
		FeatureCollection fcnew = new FeatureDataset(fs);
		Iterator iter = fc.iterator();
		while(iter.hasNext()) {
			Feature tf = (Feature)iter.next();
			Feature nf = new BasicFeature(fs);
			FeatureUtil.copyAttributes(tf, nf);
			nf.setAttribute("constraint", new Constraint());
			fcnew.add(nf);
		}
		return fcnew;
	}*/
	
	@SuppressWarnings("unchecked")
	public Object clone() {
		//return this.clone();
		Constraint tconst = new Constraint((Point)this.origpos.clone(), this.origedgecount, this.origwlratio, this.origorientation);
		tconst.statehistory = (ArrayList)this.statehistory.clone();
		tconst.statehistorymessages = (ArrayList<String>)this.statehistorymessages.clone();
		return tconst;
	}
	
	
	int nbrConstraint = 8;
	public int getNbrConstraint() {
		return this.nbrConstraint;
	}
	public double[] getCostValues() {
		double[] cost = new double[nbrConstraint];
		cost[0] = this.getSeverityMinSize();
		cost[1] = this.getSeverityMinLength();
		cost[2] = this.getSeverityMinDist();
		cost[3] = this.getSeverityLocalWidth();
		cost[4] = this.getSeverityDiffPos();
		cost[5] = this.getSeverityDiffEdgeCount();
		cost[6] = this.getSeverityDiffWLRatio();
		cost[7] = this.getSeverityDiffOrientation();
		//beim neuanlegen immer nbrConstraint anpassen !!!
		
		return cost;
	}
}
