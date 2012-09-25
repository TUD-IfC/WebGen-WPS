package ch.unizh.geo.webgen.model;

import java.util.Vector;

public class ClassHierarchy {

	private Vector<ClassHierarchyElement> elements;
	
	public ClassHierarchy() {
		elements = new Vector<ClassHierarchyElement>();
	}
	
	public void addElement(ClassHierarchyElement el) {
		this.elements.add(el);
	}
	
}
