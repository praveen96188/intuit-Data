package com.intuit.ems.payroll.psp.clobPerformance.processor;

import com.intuit.ems.payroll.psp.clobPerformance.builder.OFXRequestBuilder;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.qbdt.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.ems.payroll.psp.clobPerformance.builder.EmployeeBuilder;

import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import org.apache.commons.io.FileUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.*;
import java.io.*;
import java.security.Principal;
import java.util.*;

import static junit.framework.Assert.assertNotNull;


public class ClobPerformanceProcessor {


    public static void main(String[] args) {

        try {
            Application.initialize();
            ApplicationSecondary.initialize();
            int pNumberOfEmployees = 1;
            String psid = "106911091";
            if (args != null && args.length == 1) {
                pNumberOfEmployees = Integer.parseInt(args[0]);
            }
            if (args != null && args.length > 1) {
                pNumberOfEmployees = Integer.parseInt(args[0]);
                psid = args[1];
            }
            System.out.println("number of employees=" + pNumberOfEmployees);
            System.out.println("Company psid=" + psid);
            Application.beginUnitOfWork();
            Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
            if (company == null) {
                throw new RuntimeException("Company not found");
            }
            Application.rollbackUnitOfWork();

            List<IEMP> iemps = EmployeeBuilder.generateNewEmployees(pNumberOfEmployees, new ArrayList<IPITEM>(), new ArrayList<IPITEM>(), new ArrayList<IPITEM>());
            for (IEMP iemp : iemps) {
                iemp.setIEMPID(iemp.getIEMPID() + 1);
                iemp.setIQBUNIQUEID("List" + iemp.getIEMPID());
                iemp.setIEMPTAX(null);
                iemp.setIEMPCOMPLIANCE(null);
                iemp.setIPAYROLL(null);
                iemp.setISICK(null);
                iemp.setIVAC(null);
            }
            System.out.println("***********************   Generated employees    ************************");
            System.out.println("Started inserting document");
            StopWatch timer = StopWatch.startTimer();
            OFX ofx = OFXRequestBuilder.generateOFX(company, iemps);
            submitQBDTRequest(ofx);
            System.out.println("Completed inserting document. Elapsed time: " + timer.stop().getElapsedTimeString());
            System.out.println("###############################################################################################################");

            PayrollServices.beginUnitOfWorkWithSecondary();
            Criterion<SourceSystemTransmission> query = SourceSystemTransmission.CompanyId().equalTo(company.getId().toString())
                    .And(SourceSystemTransmission.FromSourceSystem().equalTo(SourceSystemCode.valueOf(SourceSystemCode.QBDT.toString()))
                            .And(SourceSystemTransmission.Type().equalTo(TransmissionType.valueOf(TransmissionType.PayrollSubmission.toString()))));

            System.out.println("Started fetching document");
            timer = StopWatch.startTimer();
            DomainEntitySet<SourceSystemTransmission> sourceSystemTransmissions = ApplicationSecondary.find(SourceSystemTransmission.class, new Query<SourceSystemTransmission>().Where(query)
                    .OrderBy(SourceSystemTransmission.CreatedDate().Descending()));
            System.out.println("Completed fetching document. Elapsed time: " + timer.stop().getElapsedTimeString());
            System.out.println("###############################################################################################################");

            try {
                File file = new File("apps/batch/jss/logs/OFX_request.txt");
                if (sourceSystemTransmissions.size() != 0) {
                    FileUtils.writeStringToFile(file, sourceSystemTransmissions.get(0).getRequestDocument());
                    if (file.exists()) {
                        double bytes = file.length();
                        System.out.println("bytes:   " + bytes);
                        double kilobytes = (bytes / 1024);
                        System.out.println("kilobytes:   " + kilobytes);
                        double megabytes = (kilobytes / 1024);
                        System.out.println("megabytes:   " + megabytes);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        finally {
            PayrollServices.rollbackUnitOfWorkWithSecondary();
            Application.uninitialize();
            ApplicationSecondary.uninitialize();
        }


    }

    public static com.intuit.sbd.payroll.psp.common.ofx.response.OFX submitQBDTRequest(OFX pRequest) {
        return submitQBDTRequest(pRequest, true);
    }

    public static com.intuit.sbd.payroll.psp.common.ofx.response.OFX submitQBDTRequest(OFX pRequest,
                                                                                       boolean pShouldReturnSuccessful) {
        try {
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX response =
                    OFXManager.ofxResponseToJava(submitQBDTRequestStringResponse(pRequest, pShouldReturnSuccessful), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertNotNull("Response", response);
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String submitQBDTRequestStringResponse(OFX pRequest,
                                                         boolean pShouldReturnSuccessful) {
        String ofxString = OFXManager.javaRequestToOFX(pRequest, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        TestHttpServletResponseForClob testHttpServletResponse = new TestHttpServletResponseForClob();
        QBDTPegServlet qbdtPegServlet = new QBDTPegServlet();

        try {
            qbdtPegServlet.service(new TestHttpServletRequestForClob(ofxString), testHttpServletResponse);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        return testHttpServletResponse.toString();
    }

    static class TestHttpServletResponseForClob implements HttpServletResponse {
        private StringWriter mStringWriter;
        private PrintWriter mPrintWriter;

        public TestHttpServletResponseForClob() {
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

    static class TestHttpServletRequestForClob implements HttpServletRequest {

        private ServletInputStream mInputStream;
        private String mCompressionType;
        private Map<String, String> mParameters = new HashMap<String, String>();

        public TestHttpServletRequestForClob(String pRequest) {
            try {
                mInputStream = new TestServletInputStream(new ByteArrayInputStream(pRequest.getBytes("UTF-8")));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        public TestHttpServletRequestForClob withParameters(Map<String, String> pParameterMap) {
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
            if ("intuit-peg-compression".equals(name)) {
                return mCompressionType;
            }
            if ("X-Forwarded-For".equals(name)) {
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
            return (ServletInputStream) mInputStream;
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

    static class TestServletInputStream extends ServletInputStream {

        private InputStream mInputStream;

        public TestServletInputStream(InputStream pInputStream) {
            this.mInputStream = pInputStream;
        }

        @Override
        public int read() throws IOException {
            return mInputStream.read();
        }

        @Override
        public int available() throws IOException {
            return mInputStream.available();
        }

        @Override
        public void close() throws IOException {
            mInputStream.close();
        }

        @Override
        public synchronized void mark(int readlimit) {
            mInputStream.mark(readlimit);
        }

        @Override
        public boolean markSupported() {
            return mInputStream.markSupported();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return mInputStream.read(b, off, len);
        }

        @Override
        public int read(byte[] b) throws IOException {
            return mInputStream.read(b);
        }

        @Override
        public synchronized void reset() throws IOException {
            mInputStream.reset();
        }

        @Override
        public long skip(long n) throws IOException {
            return mInputStream.skip(n);
        }

        public InputStream getInputStream() {
            return mInputStream;
        }
    }
}


