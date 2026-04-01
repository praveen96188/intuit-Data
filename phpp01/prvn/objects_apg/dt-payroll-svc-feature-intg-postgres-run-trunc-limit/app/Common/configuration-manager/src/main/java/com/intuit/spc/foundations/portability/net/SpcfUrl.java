package com.intuit.spc.foundations.portability.net;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfFactory;

/**
 * Class URL represents a Uniform Resource Locator, a pointer to a "resource" on the World Wide Web. 
 * A resource can be something as simple as a file or a directory, or it can be a reference to a more 
 * complicated object, such as a query to a database or to a search engine. More information on the 
 * types of URLs and their formats can be found at: <p>
 *
 * http://archive.ncsa.uiuc.edu/SDG/Software/Mosaic/Demo/url-primer.html<p>
 * 
 * A URL can be broken down into follwoing parts: <p>
 * 
 * &lt;scheme&gt;:&lt;scheme-specific name&gt; <p>
 * 
 * where the &lt;scheme&gt; is the protocol or scheme being used (http, fttp, and so on). and the 
 * &lt;scheme-specific name&gt; varies in format based on the scheme. <p>
 * 
 * Many URL schemes use the follwoing format: <p>
 * 
 * &lt;protocol&gt;://&lt;user&gt;:&lt;password&gt;&#64;&lt;host&gt;:&lt;port&gt;/&lt;path&gt; <p>
 * 
 * The previous example of a URL indicates that the protocol to use is http (HyperText Transfer Protocol) 
 * and that the information resides on a host machine named archive.ncsa.uiuc.edu. The information on that 
 * host machine is named /SDG/Software/Mosaic/Demo/url-primer.html. The exact meaning of this name on the 
 * host machine is both protocol dependent and host dependent. The information normally resides in a file,
 * but it could be generated on the fly. This component of the URL is called the path component. <p>
 * 
 * A URL can optionally specify a "port", which is the port number to which the TCP connection is made on 
 * the remote host machine. If the port is not specified, the default port for the protocol is used instead. 
 * For example, the default port for http is 80. An alternative port could be specified as: <p>
 * 
 * http://archive.ncsa.uiuc.edu:80/SDG/Software/Mosaic/Demo/url-primer.html <p>
 *  
 * A URL may have appended to it a "fragment", also known as a "ref" or a "reference". The fragment is indicated
 * by the sharp sign character "#" followed by more characters. For example, <p>
 *
 * http://java.sun.com/index.html#chapter1 <p>
 * 
 * This fragment is not technically part of the URL. Rather, it indicates that after the specified resource 
 * is retrieved, the application is specifically interested in that part of the document that has the tag chapter1 
 * attached to it. The meaning of a tag is resource specific. <p>
 * 
 * An application can also specify a "relative URL", which contains only enough information to reach the resource 
 * relative to another URL. Relative URLs are frequently used within HTML pages. For example, if the contents 
 * of the URL: <p>
 * 
 * http://java.sun.com/index.html <p>
 * 
 * contained within it the relative URL: <p>
 * 
 * FAQ.html <p>
 * 
 * it would be a shorthand for: <p>
 * 
 * http://java.sun.com/FAQ.html <p>
 * 
 * The relative URL need not specify all the components of a URL. If the protocol, host name, or port number 
 * is missing, the value is inherited from the fully specified URL. The file component must be specified. 
 * The optional fragment is not inherited. <p>
 * 
 * The URL class does not itself encode or decode any URL components according to the escaping mechanism 
 * defined in RFC2396. It is the responsibility of the caller to encode any fields, which need to be escaped 
 * prior to calling URL, and also to decode any escaped fields, that are returned from URL. Furthermore, because 
 * URL has no knowledge of URL escaping, it does not recognise equivalence between the encoded or decoded form 
 * of the same URL. For example, the two URLs: <p>
 * 
 * http://foo.com/hello world/ and http://foo.com/hello%20world <p>
 * 
 * would be considered not equal to each other. <p>
 * 
 * The SpcfURLEncoder and SpcfURLDecoder classes can also be used, but only for HTML form encoding, which is 
 * not the same as the encoding scheme defined in RFC2396. <p>  
 * 
 * @author mgarg
 *
 */
abstract public class SpcfUrl
{
	
	/**
	 * Creates a URL object from the String representation.
	 * @param spec the String to parse as a URL.	 
	 * @return SpcfURL object
	 * @throws SpcfArgumentNullException If the argument is null	 
	 * @throws SpcfMalformedUrlException If an unknown protocol is specified
	 */
	public static SpcfUrl createInstance(String spec)
	{	
		return SpcfFactory.getInstance().createUrl(spec);
	}
	
	/**
	 * Creates a URL from the specified protocol name, host name, and file name. The default port for the specified 
	 * protocol is used. <p>
	 * 
	 * This method is equivalent to calling the four-argument constructor with the arguments being protocol, 
     * host, -1, and file. No validation of the inputs is performed by this method. 
	 * @param protocol the name of the protocol to use.
	 * @param host the name of the host.
	 * @param file  the file on the host	  
	 * @return SpcfURL object
	 * @throws SpcfMalformedUrlException if an unknown protocol is specified.
	 */
	public static SpcfUrl createInstance(String protocol, String host, String file)
	{
		return SpcfFactory.getInstance().createUrl(protocol, host, file);
	}
	
	/**
	 * Creates a URL object from the specified protocol, host, port number, and file. <p>
	 * 
	 * Host can be expressed as a host name or a literal IP address. <p> 
	 * 
	 * Specifying a port number of -1 indicates that the URL should use the default port for the protocol. <p>	 
	 * 
	 * No validation of the inputs is performed by this method.
	 *   
	 * @param protocol the name of the protocol to use.
	 * @param host the name of the host.
	 * @param port the port number on the host.
	 * @param file the file on the host	  
	 * @return SpcfURL object
	 * @throws SpcfMalformedUrlException if an unknown protocol is specified.
	 */
	public static SpcfUrl createInstance(String protocol, String host, int port, String file)
	{
		return SpcfFactory.getInstance().createUrl(protocol, host, port, file);
	}
	
	/**
	 * Gets any query information included in the specified URL. <p>
	 * 
	 * The Query property contains any query information included in the URL. Query information is separated from 
	 * the path information by a question mark (?) and continues to the end of the URI. The query information returned
	 * does not include the leading question mark. <p>
	 * 
	 * The query information is escaped according to RFC 2396.
	 * @return the query part of this URL, or null if one does not exist
	 */
	public abstract String getQuery();
	
	/**
	 * Gets the absolute path of the URL. <p> 
	 * 
	 * The property contains the path information that the server uses to resolve requests for information. 
     * Typically this is the path to the desired information on the server's file system, although it also 
     * can indicate the application or script the server must run to provide the information. <p>
	 * 
	 * The path information does not include the scheme, host name, or query portion of the URL.
	 *  
	 * @return A String containing the absolute path to the resource or an empty string if one does not exist.
	 */
	public abstract String getPath();
	
	/**
	 * Gets the user name, password, or other user-specific information associated with the specified URL.
	 * The value returned by this property is usually in the format "userName:password". 
	 * @return A String containing the user information associated with the URL, or null if one does not 
     * exist. The returned value does not include the &#64; character reserved for delimiting the user 
     * information part of the URL. 
	 */
	public abstract String getUserInfo();
	
	/**
	 * Gets the Domain Name System (DNS) host name or IP address and the port number for a server. <p>
	 * 
	 * The Authority property is typically a server DNS host name or IP address. This property might include 
	 * the service port number if it differs from the default port for the URL. If the Authority component 
     * contains reserved characters, these are escaped in the string value returned by this property.
	 * @return the authority part of this URL
	 */
	public abstract String getAuthority();
	
	/**
	 * Gets the port number of this URL. 
	 * @return the port number, or the default port number if not set
	 */
	public abstract int getPort();
	
	/**
	 * Gets the default port number of the protocol associated with this URL. If the URL scheme do not define a 
	 * default port number, then -1 is returned. 
	 * @return the port number
	 */
	public abstract int getDefaultPort();
	
	/**
	 * Gets the protocol name of this URL. 
	 * @return the protocol of this URL.
	 */
	public abstract String getProtocol();
	
	/**
	 * Gets the host name of this URL, if applicable. The format of the host conforms to RFC 2732, i.e. for a literal 
	 * IPv6 address, this method will return the IPv6 address enclosed in square brackets ('[' and ']'). 
	 * @return the host name of this URL.
	 */
	public abstract String getHost();
	
	/**
	 * Gets the file name of this URL. The returned file portion will be the same as getPath(), plus the 
     * concatenation of the value of getQuery(), if any. If there is no query portion, this method and getPath() 
     * will return identical results. 
	 * @return the file name of this URL, or an empty string if one does not exist
	 */
	public abstract String getFile();
	
	/**
	 * Gets the anchor (also known as the "reference") of this URL. 
	 * @return the anchor (also known as the "reference") of this URL, or null if one does not exist
	 */
	public abstract String getRef();


	/**
	 * Get the string representation of this URL object.
	 * @return the string representation of this URL object.
	 */
	@Override
    public abstract String toString();

}
