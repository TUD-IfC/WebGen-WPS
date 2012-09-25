package ch.unizh.geo.webgen.model;

import java.io.Serializable;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;

public class ConstrainedFeature extends BasicFeature implements Serializable {
	
	static final long serialVersionUID = 12345;
	
	Constraint constraint;
	int uid;
	
	public ConstrainedFeature(FeatureSchema fs) {
		super(fs);
	}
	
	public ConstrainedFeature(Feature feat) {
		super(feat.getSchema());
		this.setAttributes(feat.getAttributes());
		this.setGeometry(feat.getGeometry());
		constraint = new Constraint(this);
	}
	
	public ConstrainedFeature(Geometry geom) {
		super(makeGeometrySchema());
		this.setGeometry(geom);
		constraint = new Constraint(this);
	}
	
	public ConstrainedFeature(Feature feat, boolean deepclone) {
		super((FeatureSchema)feat.getSchema().clone());
		if(deepclone) {
			FeatureUtil.copyAttributes(feat.clone(true), this);
			constraint = new Constraint(this);
		}
		else {
			this.setAttributes(feat.getAttributes());
			this.setGeometry(feat.getGeometry());
			constraint = new Constraint(this);
		}
	}
	
	private static FeatureSchema makeGeometrySchema() {
		FeatureSchema fs = new FeatureSchema();
		fs.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
		return fs;
	}
	
	//constraint functionalities
	public void setConstraint(Constraint c) {
		this.constraint = c;
	}
	
	public void initConstraint() {
		constraint = new Constraint(this);
	}
	
	public Constraint getConstraint() {
		return this.constraint;
	}
	
	
	public void setUID(int uid) {
		this.uid = uid;
	}
	
	public int getUID() {
		return uid;
	}
	
	
	//for compatibility with old constraint attributes (must be removed soon)
	public Object getAttribute(String name) {
		if(name.equals("constraint")) return constraint;
		else return super.getAttribute(name);
	}
	public void setAttribute(String name, Object attrib) {
		if(name.equals("constraint") && (attrib instanceof Constraint)) 
			constraint = (Constraint)attrib;
		else super.setAttribute(name, attrib);
	}
	
	
	
	public ConstrainedFeature clone() {
		ConstrainedFeature tfeat = new ConstrainedFeature(((Feature)this).clone(true));
		try {
			tfeat.setUID(this.uid);
			tfeat.constraint = (Constraint)this.constraint.clone();
		}
		catch(Exception e) {
			tfeat.initConstraint();
		}
		return tfeat;
	}
	
}
