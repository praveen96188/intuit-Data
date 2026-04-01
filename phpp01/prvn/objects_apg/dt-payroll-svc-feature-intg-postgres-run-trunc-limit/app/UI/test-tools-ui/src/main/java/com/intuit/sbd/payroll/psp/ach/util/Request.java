/**
* Request.java
* 
* Copyright (c) 1999-2000 PayCycle, Inc. All Rights Reserved.
* 
* This software is the confidential and proprietary information of
* PayCycle, Inc. ("Confidential Information").  You shall not
* disclose such Confidential Information and shall use it only in
* accordance with the terms of the license agreement you entered into
* with PayCycle.
* 
* PAYCYCLE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
* SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
* IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
* PURPOSE, OR NON-INFRINGEMENT. PAYCYCLE SHALL NOT BE LIABLE FOR ANY DAMAGES
* SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
* THIS SOFTWARE OR ITS DERIVATIVES.
* 
* CopyrightVersion 1.0
*/

package com.intuit.sbd.payroll.psp.ach.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.*;

import com.oreilly.servlet.MultipartRequest;

/**
 * Helper class to manage normal vs. multipart HTTP post request.
 */
public class Request implements HttpServletRequest
{
	final static int MAX_POST_SIZE_NUM_MEGS = 16;
	
	MultipartRequest m_multi;
	ServletRequest m_req;
	HttpServletRequest m_origHttpReq;
	Integer m_maxMultipartPostSizeInBytes;
    //Since we are not throwing IOExceptions, there is no way for the caller
    // to know that the request for multipart did not succeed, one of the 
    // common reason could be because of the size of files in the request is 
    // greater than the size permitted. Hence creating this flag, it will be
    // set to false incase of an exception which is most likely to be caused by
    // crossing the limit of the size of files allowed
    boolean multipartSuccess=true;
	
	public Request (ServletRequest request) {
		this(request, getDefaultMaxPostSizeInBytes());
	}
	public static int getDefaultMaxPostSizeInBytes() {
		return MAX_POST_SIZE_NUM_MEGS * 1024*1024;
	}
	public Request (ServletRequest request, int maxPostSizeInBytes) {
		if (isMultipart (request)) {
			try {
				// Upload the file in the multipart request
				m_multi = new MultipartRequest (request,  System.getProperty ("java.io.tmpdir"), maxPostSizeInBytes);
				m_maxMultipartPostSizeInBytes = maxPostSizeInBytes;
			} catch (Exception ex) {
				//AppMgr.getLogger().info("multipart failure: " + ex);
                multipartSuccess = false;
			}
		} else {
			m_req = request;
		}
		try {
			m_origHttpReq = null;
			if( request instanceof HttpServletRequest ) {
				m_origHttpReq = (HttpServletRequest) request;
			}
		} catch (Exception ex) {
			//AppMgr.getLogger().info("Request failure: " + ex);
		}
	}
	
	/**
	 * Return the parameter in the form of a String.
	 */
	public String getParameter (String name) {
		return (m_multi == null) ?
			m_req.getParameter (name) : m_multi.getParameter (name);
	}

	/**
	 * Return the parameter names in the request.
	 */
	public Enumeration getParameterNames() {
		return (m_multi == null) ?
			m_req.getParameterNames () : m_multi.getParameterNames ();
	}

	/**
	 * Return the parameter values in the request.
	 */
	public String[] getParameterValues(String name) {
		return (m_multi == null) ?
			m_req.getParameterValues(name) : m_multi.getParameterValues(name);
	}

	/**
	 * Return the paramter in the form of a File (system).
	 */
	public File getFile (String name) {
		return (m_multi == null) ?
			null : m_multi.getFile (name);
	}
    
    /**
     * Returns the content type of the file based on the filename sent as a 
     *  parameter
     */
    public String getContentType(String name){
        return(m_multi == null) ?
                null : m_multi.getContentType(name);
    }
	
	/**
     * This method can be used to check if the request object was constructed properly
     * @return true if there were no exceptions while creating the object
     *         false if there were exceptions most likely indicating that the 
     *               size exceeded the limit
     */
    public boolean isMultipartSuccess() {
        return multipartSuccess;
    }

	/**
	 * Return the files names in the request.
	 */
	public Enumeration getFileNames() {
		return (m_multi == null) ?
			null : m_multi.getFileNames ();
	}

	/** Determines if the request is a multipart request. */
	public static boolean isMultipart (ServletRequest request) {
		String type = request.getContentType();
		return type != null && type.toLowerCase().startsWith ("multipart/form-data");
	}
	
	public String getContextPath() {
		return (m_origHttpReq==null) ?
				null : m_origHttpReq.getContextPath();
	}
	
	public HttpSession getSession() {
		return (m_origHttpReq==null) ?
				null : m_origHttpReq.getSession();
	}

	public StringBuffer getRequestURL() {
		return (m_origHttpReq==null) ?
				null : m_origHttpReq.getRequestURL();
	}
	
	public Integer getMultipartMaxPostSize() {
		return (m_multi == null) ?
				null : m_maxMultipartPostSizeInBytes;
	}
	
	// If you ever need one of these functions, feel free to implement it...
	//GLOBALYZER_START_IGNORE
	public boolean isSecure() {throw new RuntimeException("isSecure not implemented");}
	public java.io.BufferedReader getReader() {throw new RuntimeException("getReader not implemented");}
	public ServletInputStream getInputStream()  {throw new RuntimeException("getInputStream not implemented");}
	public Object getAttribute(String s) {throw new RuntimeException("getAttribute not implemented");}
	public String getCharacterEncoding() {throw new RuntimeException("getCharacterEncoding not implemented");}
	public int getContentLength() {throw new RuntimeException("getContentLength not implemented");}
	public String getContentType() {throw new RuntimeException("getContentType not implemented");}
	public String getProtocol() {throw new RuntimeException("getProtocol not implemented");}
	public String getScheme() {throw new RuntimeException("getScheme not implemented");}
	public String getServerName() {throw new RuntimeException("getServerName not implemented");}
	public int getServerPort() {throw new RuntimeException("getServerPort not implemented");}
	public String getRemoteAddr() {throw new RuntimeException("getRemoteAddr not implemented");}
	public String getRemoteHost() {throw new RuntimeException("getRemoteHost not implemented");}
	@Deprecated public String getRealPath(String s) {throw new RuntimeException("getRealPath not implemented");}
	public void setAttribute(String name, Object o) {throw new RuntimeException("getRealPath not implemented");}
	public Enumeration getAttributeNames() {throw new RuntimeException("getAttributeNames not implemented");}
	public void setCharacterEncoding(String s) {throw new RuntimeException("setCharacterEncoding not implemented");}
	public java.util.Map getParameterMap() {throw new RuntimeException("getParameterMap not implemented");}
	public void removeAttribute(String s) {throw new RuntimeException("removeAttribute not implemented");}
	public java.util.Locale getLocale() {throw new RuntimeException("getLocale not implemented");}
	public Enumeration getLocales() {throw new RuntimeException("getLocales not implemented");}
	public javax.servlet.RequestDispatcher getRequestDispatcher(String s) {throw new RuntimeException("getRequestDispatcher not implemented");}
	public int getRemotePort() {throw new RuntimeException("getRemotePort not implemented");}
	public String getLocalName() {throw new RuntimeException("getLocalName not implemented");}
	public String getLocalAddr() {throw new RuntimeException("getLocalAddr not implemented");}
	public String getAuthType() {throw new RuntimeException("getAuthType not implemented");}
	public javax.servlet.http.Cookie[] getCookies() {throw new RuntimeException("getCookies not implemented");}
	public long getDateHeader(String name) {throw new RuntimeException("getDateHeader not implemented");}
	public String getHeader(String name) {throw new RuntimeException("getHeader not implemented");}
	public Enumeration getHeaders(String name) {throw new RuntimeException("getHeaders not implemented");}
	public Enumeration getHeaderNames() {throw new RuntimeException("getHeaderNames not implemented");}
	public int getIntHeader(String name) {throw new RuntimeException("getIntHeader not implemented");}
	public String getMethod() {throw new RuntimeException("getMethod not implemented");}
	public String getPathInfo() {throw new RuntimeException("getPathInfo not implemented");}
	public String getPathTranslated() {throw new RuntimeException("getPathTranslated not implemented");}
	//public java.lang.String getContextPath() {throw new RuntimeException("getContextPath not implemented");}
	public String getQueryString() {throw new RuntimeException("getQueryString not implemented");}
	public String getRemoteUser() {throw new RuntimeException("getRemoteUser not implemented");}
	public boolean isUserInRole(String role) {throw new RuntimeException("isUserInRole not implemented");}
	public java.security.Principal getUserPrincipal() {throw new RuntimeException("getUserPrincipal not implemented");}
	public String getRequestedSessionId() {throw new RuntimeException("getRequestedSessionId not implemented");}
	public String getRequestURI() {throw new RuntimeException("getRequestURI not implemented");}
	//public java.lang.StringBuffer getRequestURL() {throw new RuntimeException("getRequestURL not implemented");}
	public String getServletPath() {throw new RuntimeException("getServletPath not implemented");}
	public HttpSession getSession(boolean create) {throw new RuntimeException("getSession not implemented");}
	//public HttpSession getSession() {throw new RuntimeException("getSession not implemented");}
	public boolean isRequestedSessionIdValid() {throw new RuntimeException("isRequestedSessionIdValid not implemented");}
	public boolean isRequestedSessionIdFromCookie() {throw new RuntimeException("isRequestedSessionIdFromCookie not implemented");}
	@Deprecated public boolean isRequestedSessionIdFromURL() {throw new RuntimeException("isRequestedSessionIdFromURL not implemented");}
	@Deprecated public boolean isRequestedSessionIdFromUrl() {throw new RuntimeException("isRequestedSessionIdFromUrl not implemented");}
    public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {throw new RuntimeException("isRequestedSessionIdFromCookie not implemented");}
    public void login(String s, String s1) throws ServletException {throw new RuntimeException("isRequestedSessionIdFromCookie not implemented");}
    public void logout() throws ServletException {throw new RuntimeException("isRequestedSessionIdFromCookie not implemented");}
    public Collection<Part> getParts() throws IOException, ServletException {throw new RuntimeException("isRequestedSessionIdFromCookie not implemented");}
    public Part getPart(String s) throws IOException, ServletException {throw new RuntimeException("isRequestedSessionIdFromCookie not implemented");}
	public int getLocalPort() {throw new RuntimeException("getLocalPort not implemented");}
    public ServletContext getServletContext() {throw new RuntimeException("isRequestedSessionIdFromCookie not implemented");}
    public AsyncContext startAsync() throws IllegalStateException {throw new RuntimeException("isRequestedSessionIdFromCookie not implemented");}
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {throw new RuntimeException("isRequestedSessionIdFromCookie not implemented");}
    public boolean isAsyncStarted() {throw new RuntimeException("isRequestedSessionIdFromCookie not implemented");}
    public boolean isAsyncSupported() {throw new RuntimeException("isRequestedSessionIdFromCookie not implemented");}
    public AsyncContext getAsyncContext() {throw new RuntimeException("isRequestedSessionIdFromCookie not implemented");}
    public DispatcherType getDispatcherType() {throw new RuntimeException("isRequestedSessionIdFromCookie not implemented");}
    //GLOBALYZER_END_IGNORE
}
