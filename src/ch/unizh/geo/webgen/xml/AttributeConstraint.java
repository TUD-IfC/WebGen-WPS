package ch.unizh.geo.webgen.xml;

import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;

import ch.unizh.geo.webgen.model.Constraint;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;


public class AttributeConstraint {
	
	public static void encodeConstraint(Element propel, Constraint wgc) throws Exception {
		Element constraintEl = propel.addElement("Constraint");
		
		Element origposEl = constraintEl.addElement("origpos");
		Element origXposEl = origposEl.addElement("x");
		origXposEl.addText(""+wgc.getOrigPos().getX());
		Element origYposEl = origposEl.addElement("y");
		origYposEl.addText(""+wgc.getOrigPos().getY());
		
		Element origedgecountEl = constraintEl.addElement("origedgecount");
		origedgecountEl.addText(""+wgc.getOrigEdgeCount());
		Element origwlratioEl = constraintEl.addElement("origwlratio");
		origwlratioEl.addText(""+wgc.getOrigWLRatio());
		Element origorientationEl = constraintEl.addElement("origorientation");
		origorientationEl.addText(""+wgc.getOrigOrientation());
		
		for(int i=0; i<wgc.getHistorySize(); i++) {
			Element actstateEl = constraintEl.addElement("state");
			actstateEl.addAttribute("number", ""+i);
			actstateEl.addAttribute("message", wgc.getStateMessageFromHistory(i));
			double[] actstate = wgc.getStateFromHistory(i);
			for(int j=0; j<actstate.length; j++) {
				Element actvalueEl = actstateEl.addElement("value");
				actvalueEl.addText(""+actstate[j]);
			}
		}
	  }
	
	public static Constraint decodeConstraint(Element constrel) {
		//Vector actstates = new Vector();
		double origposX = 0.0;
		double origposY = 0.0;
		int origedgecount = 0;
		double origwlratio = 0.0;
		double origorientation = 0.0;
		
		try {
			Element origposel = constrel.element("origpos");
			origposX = Double.parseDouble(origposel.element("x").getTextTrim());
			origposY = Double.parseDouble(origposel.element("y").getTextTrim());
		}
		catch (Exception e) {}
		
		try {
			Element origedgecountel = constrel.element("origedgecount");
			origedgecount = Integer.parseInt(origedgecountel.getTextTrim());
		}
		catch (Exception e) {}
		
		try {
			Element origwlratioel = constrel.element("origwlratio");
			origwlratio = Double.parseDouble(origwlratioel.getTextTrim());
		}
		catch (Exception e) {}
		
		try {
			Element origorientationel = constrel.element("origorientation");
			origorientation = Double.parseDouble(origorientationel.getTextTrim());
		}
		catch (Exception e) {}
		
		GeometryFactory geometryFactory = new GeometryFactory();
		Constraint wgc = new Constraint(
				geometryFactory.createPoint(new Coordinate(origposX, origposY)),
				origedgecount, origwlratio, origorientation);
		
		try {
			Iterator statesiter = constrel.elementIterator("state");
			Element stateel;
			int statesize, ist;
			String message;
			double[] stateda;
			while(statesiter.hasNext()) {
				stateel = (Element) statesiter.next();
				message = stateel.attributeValue("message");
				List stateitems = stateel.elements();
				statesize = stateitems.size();
				stateda = new double[statesize];
				for(ist=0; ist<statesize; ist++) {
					stateda[ist] = Double.parseDouble(((Element)stateitems.get(ist)).getTextTrim());
				}
				wgc.addHistoryLast(stateda, message);
			}
		}
		catch (Exception e) {}
		
		/*for (Iterator viter = actstates.iterator(); viter.hasNext();) {
			wgc.addHistoryLast((double[])viter.next());
		}*/
		return wgc;
	}

}
