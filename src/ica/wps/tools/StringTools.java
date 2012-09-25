package ica.wps.tools;

import java.io.File;

public class StringTools {
	
	/**
	 * Removes consecutive File separator within a path.<p>
	 * E.g. "/usr/home/bla//test" will be returned as "/usr/home/bla/test"
	 * 
	 * @param path the path
	 * @return the path guaranteed to have no consecutive file separators
	 */
	public static String removeConsecutiveFileSeparator(String path) {
		
		return path.replace(File.separator + File.separator, File.separator);
	}

}
