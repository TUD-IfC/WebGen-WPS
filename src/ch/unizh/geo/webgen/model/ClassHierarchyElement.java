package ch.unizh.geo.webgen.model;

import java.util.Vector;

public class ClassHierarchyElement {
	
	private String name = null;
	private ClassHierarchyElement parent = null;
	private Vector<ClassHierarchyElement> children;
	
	public ClassHierarchyElement(String name) {
		this.name = name;
		this.children = new Vector<ClassHierarchyElement>();
	}
	
	public ClassHierarchyElement(String name, ClassHierarchyElement parent) {
		this.name = name;
		this.parent = parent;
		this.children = new Vector<ClassHierarchyElement>();
	}
	
	public String getName() {
		return this.name;
	}
	
	public void addChild(ClassHierarchyElement child) {
		this.children.add(child);
	}
	
	public boolean hasChildren() {
		if(this.children.size() > 0) return true;
		else return false;
	}
	
	public boolean hasParent() {
		if(this.parent != null) return true;
		else return false;
	}
	
	public ClassHierarchyElement getParent() {
		return this.parent;
	}

}
