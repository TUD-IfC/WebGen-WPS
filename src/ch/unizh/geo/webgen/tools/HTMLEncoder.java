package ch.unizh.geo.webgen.tools;

/**
 * Converts Strings so that they can be used within HTML-Code.
 */
public abstract class HTMLEncoder
{
	/**
	 * Variant of {@link #encode} where encodeNewline is false and encodeNbsp is true.
	 */
	public static String encode (String string)
	{
		return encode(string, false, true);
	}

	/**
	 * Variant of {@link #encode} where encodeNbsp is true.
	 */
	public static String encode (String string, boolean encodeNewline)
	{
		return encode(string, encodeNewline, true);
	}

	/**
	 * Encodes the given string, so that it can be used within a html page.
	 * @param string the string to convert
	 * @param encodeNewline if true newline characters are converted to &lt;br&gt;'s
	 * @param encodeSubsequentBlanksToNbsp if true subsequent blanks are converted to &amp;nbsp;'s
	 */
	public static String encode (String string,
								 boolean encodeNewline,
								 boolean encodeSubsequentBlanksToNbsp)
	{
		if (string == null)
		{
			return "";
		}

		StringBuffer sb = null;	//create later on demand
		String app;
		char c;
		for (int i = 0; i < string.length (); ++i)
		{
			app = null;
			c = string.charAt(i);
			switch (c)
			{
                case '"': app = "&quot;"; break;    //"
                case '&': app = "&amp;"; break;     //&
                case '<': app = "&lt;"; break;      //<
                case '>': app = "&gt;"; break;      //>
                case ' ':
                    if (encodeSubsequentBlanksToNbsp &&
                        (i == 0 || (i - 1 >= 0 && string.charAt(i - 1) == ' ')))
                    {
                        //Space at beginning or after another space
                        app = "&nbsp;";
                    }
                    break;
                case '\n':
                    if (encodeNewline)
                    {
                        app = "<br/>";
                    }
                    break;

                //german umlauts
			    case '\u00E4' : app = "&auml;";  break;
			    case '\u00C4' : app = "&Auml;";  break;
			    case '\u00F6' : app = "&ouml;";  break;
			    case '\u00D6' : app = "&Ouml;";  break;
			    case '\u00FC' : app = "&uuml;";  break;
			    case '\u00DC' : app = "&Uuml;";  break;
			    case '\u00DF' : app = "&szlig;"; break;

                //misc
                //case 0x80: app = "&euro;"; break;  sometimes euro symbol is ascii128, should we suport it?
                case '\u20AC': app = "&euro;";  break;
                case '\u00AB': app = "&laquo;"; break;
                case '\u00BB': app = "&raquo;"; break;
                case '\u00A0': app = "&nbsp;"; break;

                default:
                    if (((int)c) >= 0x80)
                    {
                        //encode all non basic latin characters
                        app = "&#" + ((int)c) + ";";
                    }
                    break;
			}
			if (app != null)
			{
				if (sb == null)
				{
					sb = new StringBuffer(string.substring(0, i));
				}
				sb.append(app);
			} else {
				if (sb != null)
				{
					sb.append(c);
				}
			}
		}

		if (sb == null)
		{
			return string;
		}
		else
		{
			return sb.toString();
		}
	}


}