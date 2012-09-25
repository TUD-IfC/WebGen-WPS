package ch.unizh.geo.webgen.model;

import java.util.Comparator;

public class ConstrainedFeatureCollectionSortedComparator implements Comparator {
	public int compare(Object o1, Object o2) {
 		ConstrainedFeatureCollectionSorted a = (ConstrainedFeatureCollectionSorted)o1;
 		ConstrainedFeatureCollectionSorted b = (ConstrainedFeatureCollectionSorted)o2;
 		return (a.compareTo(b));
 	}
}
