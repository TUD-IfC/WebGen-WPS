package ch.unizh.geo.webgen.model;

import java.util.Comparator;

public class ConstraintSpaceArrayItemComparator implements Comparator<ConstraintSpaceArrayItem> {
 	/* old java 1.4
 	 * public int compare(Object o1, Object o2) {
 		ConstraintSpaceArrayItem a = (ConstraintSpaceArrayItem)o1;
 		ConstraintSpaceArrayItem b = (ConstraintSpaceArrayItem)o2;
 		return (a.compareTo(b));
 	}*/
	public int compare(ConstraintSpaceArrayItem o1, ConstraintSpaceArrayItem o2) {
 		return (o1.compareTo(o2));
 	}
}
