package ch.unizh.geo.webgen.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class WebGenForward extends HttpServlet {

	static final long serialVersionUID = 12345;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		doPost(req, res);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		try {
			URL url = new URL("http://141.30.137.195:8080/webgen_core/execute");
	    	URLConnection conn = url.openConnection();
	        conn.setDoOutput(true);
	        String outstring = "";
	        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	        BufferedReader ird = new BufferedReader(new InputStreamReader(req.getInputStream()));
	        String iline;
	        while ((iline = ird.readLine()) != null) {
	        	//wr.write(iline);
	        	outstring += iline;
	        }
	        wr.write(outstring);
	        wr.flush();
	        wr.close();

	        PrintWriter out = res.getWriter();
	        BufferedReader ord = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        String oline;
	        while ((oline = ord.readLine()) != null) {
	        	out.println(oline);
	        }
	        //out.println("<testit/>");
	        out.close();
	        wr.close();
	        res.setStatus(HttpServletResponse.SC_OK);
		}
		catch(Exception exception) {
			String exstr = exception.getClass().getSimpleName() + " in " + exception.getLocalizedMessage()+"\n";
			for(int erri=0; erri < exception.getStackTrace().length; erri++) {
				exstr = exstr + "\n" + exception.getStackTrace()[erri];
			}
			exstr = "<webgen:Error xmlns:webgen=\"http://www.webgen.org/webgen\">\n" +
			        exstr +
			        "\n</webgen:Error>";
			PrintWriter reswriter = res.getWriter();
			reswriter.print(exstr);
			reswriter.close();
			res.setStatus(HttpServletResponse.SC_OK);
		}
	}
}
