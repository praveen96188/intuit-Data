package com.intuit.sbd.payroll.psp.adapters.qbdt;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Nov 20, 2010
 * Time: 12:40:18 PM
 */
public class TestHttpServletResponse implements HttpServletResponse {
    private StringWriter mStringWriter;
    private PrintWriter mPrintWriter;

    public TestHttpServletResponse() {
        mStringWriter = new StringWriter();
        mPrintWriter = new PrintWriter(mStringWriter);
    }

    public void addCookie(Cookie pCookie) {
        
    }

    public boolean containsHeader(String s) {
        return false;  
    }

    public String encodeURL(String s) {
        return null;  
    }

    public String encodeRedirectURL(String s) {
        return null;  
    }

    public String encodeUrl(String s) {
        return null;  
    }

    public String encodeRedirectUrl(String s) {
        return null;  
    }

    public void sendError(int i, String s) throws IOException {
        
    }

    public void sendError(int i) throws IOException {
        
    }

    public void sendRedirect(String s) throws IOException {
        
    }

    public void setDateHeader(String s, long l) {
        
    }

    public void addDateHeader(String s, long l) {
        
    }

    public void setHeader(String s, String s1) {
        
    }

    public void addHeader(String s, String s1) {
        
    }

    public void setIntHeader(String s, int i) {
        
    }

    public void addIntHeader(String s, int i) {
        
    }

    public void setStatus(int i) {
        
    }

    public void setStatus(int i, String s) {
        
    }

    public String getCharacterEncoding() {
        return null;  
    }

    public String getContentType() {
        return null;  
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return null;  
    }

    public PrintWriter getWriter() throws IOException {
        return mPrintWriter;  
    }

    public void setCharacterEncoding(String s) {
        
    }

    public void setContentLength(int i) {
        
    }

    public void setContentType(String s) {
        
    }

    public void setBufferSize(int i) {
        
    }

    public int getBufferSize() {
        return 0;  
    }

    public void flushBuffer() throws IOException {
        
    }

    public void resetBuffer() {
        
    }

    public boolean isCommitted() {
        return false;  
    }

    public void reset() {
        
    }

    public void setLocale(Locale pLocale) {
        
    }

    public Locale getLocale() {
        return null;  
    }

    @Override
    public String toString() {
        return mStringWriter.toString();
    }
}
