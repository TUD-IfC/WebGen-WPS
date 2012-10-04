package ch.unizh.geo.webgen.service;

import ch.unizh.geo.webgen.registry.InterfaceDescription;
import ch.unizh.geo.webgen.registry.ParameterDescription;
import ch.unizh.geo.webgen.server.AWebGenAlgorithm;
import ch.unizh.geo.webgen.server.IWebGenAlgorithm;
import ch.unizh.geo.webgen.server.WebGenRequest;

public class ConcatStrings extends AWebGenAlgorithm implements IWebGenAlgorithm {

	public void run(WebGenRequest wgreq) {
		String string1 = wgreq.getParameter("string1").toString();
		String string2 = wgreq.getParameter("string2").toString();
		String order = wgreq.getParameter("order").toString();
		Integer interations = wgreq.getParameterInt("iterations");
		String result = "";
		for(int i=0; i<interations; i++) {
			if(order.equals("1-2")) result += string1 + string2;
			else if(order.equals("2-1")) result += string2 + string1;
		}
		wgreq.addResult("result", result);
	}
	
	public InterfaceDescription getInterfaceDescription() {
		InterfaceDescription id = new InterfaceDescription("ConcatStrings", "neun", "support",
				"",
				"ConcatStrings",
				"concat two strings",
				"1.0");
		
		//add input parameters
		id.addInputParameter("string1", "STRING", "abc", "String 1");
		id.addInputParameter("string2", "STRING", "def", "String 2");
		
		//adding STRINGCHOICE
		ParameterDescription pdesc = new ParameterDescription("order", "STRING", "1-2", "concat order");
		pdesc.addSupportedValue("1-2");
		pdesc.addSupportedValue("2-1");
		pdesc.setChoiced();
		/*pdesc.addChoice("1-2");
		pdesc.addChoice("2-1");*/
		id.addInputParameter(pdesc);
		//adding INTEGERCHOICE
		ParameterDescription pdescint = new ParameterDescription("iterations", "INTEGER", "3", "how often");
		pdescint.addSupportedValue(new Integer(1));
		pdescint.addSupportedValue(new Integer(3));
		pdescint.addSupportedValue(new Integer(5));
		pdescint.setChoiced();
		/*pdescint.addChoice(new Integer(1));
		pdescint.addChoice(new Integer(3));
		pdescint.addChoice(new Integer(5));*/
		id.addInputParameter(pdescint);
		
		
		//add output parameters
		id.addOutputParameter("result", "STRING");
		return id;
	}
}
