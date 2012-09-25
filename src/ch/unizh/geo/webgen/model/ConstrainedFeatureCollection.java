package ch.unizh.geo.webgen.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;

public class ConstrainedFeatureCollection extends FeatureDataset implements Serializable {
	
	static final long serialVersionUID = 12345;
	
	Envelope envelope = null;
	String FCname = "testcollection";
	int FCid = 1;
	public boolean hasConstraints = false;
	
	CollectionConstraint fcconstraint;
	
	
	public ConstrainedFeatureCollection(Collection newFeatures, FeatureSchema featureSchema)  {
		super(newFeatures, featureSchema);
	}
	
	public ConstrainedFeatureCollection(FeatureSchema featureSchema)  {
		super(featureSchema);
	}
	
	public ConstrainedFeatureCollection(FeatureCollection fc)  {
		super(fc.getFeatureSchema());
		ConstrainedFeature wgfeat;
		int i=0;
		for(Iterator iter = fc.iterator(); iter.hasNext();) {
			wgfeat = new ConstrainedFeature((Feature)iter.next());
			wgfeat.setUID(i);
			this.add(wgfeat);
			i++;
		}
		this.hasConstraints = true;
	}
	
	public ConstrainedFeatureCollection(FeatureCollection fc, boolean deepclone)  {
		super((FeatureSchema)fc.getFeatureSchema().clone());
		ConstrainedFeature wgfeat;
		int i=0;
		for(Iterator iter = fc.iterator(); iter.hasNext();) {
			wgfeat = new ConstrainedFeature((Feature)iter.next(), deepclone);
			wgfeat.setUID(i);
			this.add(wgfeat);
			i++;
		}
		this.hasConstraints = true;
	}
	
	public void initConstraints() {
		for(Iterator iter = this.iterator(); iter.hasNext();) {
			((ConstrainedFeature)iter.next()).initConstraint();
		}
		this.hasConstraints = true;
	}
	
	public void makeConstraintHistoryStep(String msg) {
		for(Iterator iter = this.iterator(); iter.hasNext();) {
			((ConstrainedFeature)iter.next()).getConstraint().updateHistory(msg);
		}
		this.fcconstraint.updateHistory(msg);
	}
	
	public Envelope getEnvelope() {
		List features = super.getFeatures();
        //if (envelope == null) {
            envelope = new Envelope();

            for (Iterator i = features.iterator(); i.hasNext();) {
                Feature feature = (Feature) i.next();
                envelope.expandToInclude(feature.getGeometry()
                                                .getEnvelopeInternal());
            }
        //}
        return envelope;
    }
	
	
	public void initCollectionConstraint(FeatureCollection cfc) {
		this.fcconstraint = new CollectionConstraint(this, cfc);
	}
	public void setCollectionConstraint(CollectionConstraint ncc) {
		this.fcconstraint = ncc;
	}
	public CollectionConstraint getCollectionConstraint() {
		return this.fcconstraint;
	}
	
	
	public ConstrainedFeatureCollection clone()  {
		ConstrainedFeatureCollection tcfc = new ConstrainedFeatureCollection(this.getFeatureSchema());
		for(Iterator iter = this.iterator(); iter.hasNext();) {
			tcfc.add(((ConstrainedFeature)iter.next()).clone());
		}
		if(this.fcconstraint != null) tcfc.setCollectionConstraint(this.fcconstraint.clone());
		tcfc.hasConstraints = true;
		return tcfc;
	}
	
}
