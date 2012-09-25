package ch.unizh.geo.webgen.httpd;

import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.util.InetAddrPort;

public class WebGenJetty {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new WebGenJetty();
	}
	
	public WebGenJetty() {
		try {
			HttpServer server = new HttpServer();
			SocketListener listener = new SocketListener(new InetAddrPort("localhost", 8383));
			server.addListener(listener);
			//SocketListener listener2 = new SocketListener(new InetAddrPort("130.60.176.154", 8383));
			//server.addListener(listener2);
			HttpContext context = server.getContext("/");
			ServletHandler handler= new ServletHandler();
			handler.addServlet("WebGenRegistry","/registry", "ch.unizh.geo.webgen.server.WebGenRegistry");
			handler.addServlet("WebGenInterface","/describe", "ch.unizh.geo.webgen.server.WebGenInterface");
			handler.addServlet("WebGenService","/execute", "ch.unizh.geo.webgen.server.WebGenService");
			context.addHandler(handler);
			server.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
