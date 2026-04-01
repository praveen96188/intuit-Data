package com.intuit.sbd.payroll.psp.adapters.qbdt;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Nov 20, 2010
 * Time: 12:37:58 PM
 */
public class TestHttpServletRequest implements HttpServletRequest{

    private ServletInputStream mInputStream;
    private String mCompressionType;
    private Map<String, String> mParameters = new HashMap<String, String>();

    public TestHttpServletRequest(String pRequest) {
        try {
            mInputStream = new TestServletInputStream(new ByteArrayInputStream(pRequest.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public TestHttpServletRequest withParameters(Map<String, String> pParameterMap) {
        mParameters = pParameterMap;
        return this;
    }

    public String getAuthType() {
        return null;
    }

    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    public long getDateHeader(String s) {
        return 0;
    }

    public String getHeader(String name) {
        if("intuit-peg-compression".equals(name)) {
            return mCompressionType;
        }
        if("X-Forwarded-For".equals(name)) {
            return "172.17.214.120, 172.17.214.4";
        }
        return null;
    }

    public void setCompressionType(String pCompressionType) {
        mCompressionType = pCompressionType;
    }

    public Enumeration getHeaders(String s) {
        return null;
    }

    public Enumeration getHeaderNames() {
        return null;
    }

    public int getIntHeader(String s) {
        return 0;
    }

    public String getMethod() {
        return "POST";
    }

    public String getPathInfo() {
        return null;
    }

    public String getPathTranslated() {
        return null;
    }

    public String getContextPath() {
        return null;
    }

    public String getQueryString() {
        return null;
    }

    public String getRemoteUser() {
        return null;
    }

    public boolean isUserInRole(String s) {
        return false;
    }

    public Principal getUserPrincipal() {
        return null;
    }

    public String getRequestedSessionId() {
        return null;
    }

    public String getRequestURI() {
        return null;
    }

    public StringBuffer getRequestURL() {
        return null;
    }

    public String getServletPath() {
        return null;
    }

    public HttpSession getSession(boolean b) {
        return null;
    }

    public HttpSession getSession() {
        return null;
    }

    public boolean isRequestedSessionIdValid() {
        return false;
    }

    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    public Object getAttribute(String s) {
        return null;
    }

    public Enumeration getAttributeNames() {
        return null;
    }

    public String getCharacterEncoding() {
        return null;
    }

    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

    }

    public int getContentLength() {
        return 0;
    }

    public String getContentType() {
        return null;
    }

    public ServletInputStream getInputStream() throws IOException {
        return (ServletInputStream)mInputStream;
    }

    public String getParameter(String s) {
        return mParameters.get(s);
    }

    public Enumeration getParameterNames() {
        return new Vector<String>(mParameters.keySet()).elements();
    }

    public String[] getParameterValues(String s) {
        return new String[]{getParameter(s)};
    }

    public Map getParameterMap() {
        return mParameters;
    }

    public String getProtocol() {
        return null;
    }

    public String getScheme() {
        return null;
    }

    public String getServerName() {
        return null;
    }

    public int getServerPort() {
        return 0;
    }

    public BufferedReader getReader() throws IOException {
        return null;
    }

    public String getRemoteAddr() {
        return "Test IP";
    }

    public String getRemoteHost() {
        return null;
    }

    public void setAttribute(String s, Object o) {

    }

    public void removeAttribute(String s) {

    }

    public Locale getLocale() {
        return null;
    }

    public Enumeration getLocales() {
        return null;
    }

    public boolean isSecure() {
        return false;
    }

    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    public String getRealPath(String s) {
        return null;
    }

    public int getRemotePort() {
        return 0;
    }

    public String getLocalName() {
        return null;
    }

    public String getLocalAddr() {
        return null;
    }

    public int getLocalPort() {
        return 0;
    }
}
