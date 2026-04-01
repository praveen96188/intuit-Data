package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.PSPTest;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.CharBuffer;

import static junit.framework.Assert.*;

public class QBDTPegServletTest extends PSPTest {

    private static final String QBDT_SIGN_ON = "<OFX><SIGNONMSGSRQV1><SONRQ><DTCLIENT>20080124185539<USERID>999031268<USERPASS>test1234<LANGUAGE>ENG<APPVER>50.00.R.6/20801#accountant<APPID>QBWPRO</SONRQ></SIGNONMSGSRQV1><I.PAYROLLMSGSRQV1><I.PAYROLLUPDATERQ><TOKEN>16<REJECTIFMISSING>N</I.PAYROLLUPDATERQ></I.PAYROLLMSGSRQV1></OFX>";

    @Test
    public void happyPath() {
        long start = System.currentTimeMillis();
        try {
            URL url = new URL("http://localhost:8080/QBDT/QBDTPegServlet");

            URLConnection urlConnection = url.openConnection();
            urlConnection.setDoOutput(true);

            OutputStreamWriter out = new OutputStreamWriter(
                                      urlConnection.getOutputStream());
            out.write(QBDT_SIGN_ON);
            out.close();

            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                    urlConnection.getInputStream()));

            StringBuilder response = new StringBuilder();
            CharBuffer charBuffer = CharBuffer.allocate(4096);
            while (in.read(charBuffer) != -1) {
                charBuffer.flip();
                response.append(charBuffer.toString());
                charBuffer.clear();
            }

            System.out.println(response.toString().trim());
            in.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            long end = System.currentTimeMillis();
            double resolutionTime = (end - start) / 1000.0d;
            System.out.println("resolutionTime = " + resolutionTime );
        }
    }

    @Test
    public void exerciseConvertRequestStreamToStringFails() throws Exception {
        StringBuffer sb = new StringBuffer("This is not compressed data");
        ByteArrayInputStream bais= new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
        QBDTPegServlet qbdtPegServlet = new QBDTPegServlet();
        QBDTProcessResult<String> ofxResponsePR = qbdtPegServlet.convertRequestStreamToString(QBDTPegServlet.ZLIB_COMPRESSION,bais);
        assertFalse(ofxResponsePR.isSuccess());
        assertEquals(ErrorMessages.ErrorUncompressingHTTPRequest().getUniqueErrorIdentifier(),ofxResponsePR.getMessage().getUniqueErrorIdentifier());
    }

    @Test
    public void testBlastCompressionDiscontinued() throws Exception {
        String ofxString = "This is a compressed request";
        TestHttpServletRequest testHttpServletRequest = new TestHttpServletRequest(ofxString);
        testHttpServletRequest.setCompressionType(QBDTPegServlet.BLAST_COMPRESSION);
        TestHttpServletResponse testHttpServletResponse = new TestHttpServletResponse();
        QBDTPegServlet qbdtPegServlet = new QBDTPegServlet();
        qbdtPegServlet.service(testHttpServletRequest, testHttpServletResponse);
        assertTrue("response does not contain errors " + testHttpServletResponse.toString(), QBOFX.ofxStringContainsErrorSeverity(testHttpServletResponse.toString()));
        assertTrue("Error message not expected", testHttpServletResponse.toString().contains(ProcessingErrorHandler.getUnsupportedCompressionProcessingErrorString()));
    }

    @Test
    public void exerciseConvertRequestStreamToStringSuccessZLib() {
        try {
            URL xsdURL = QBDTPegServletTest.class.getResource("/resources/zlib_compression.dat");
            InputStream fileStream = xsdURL.openStream();

//            FileInputStream fis = new FileInputStream("successful_compress.dat");
            QBDTPegServlet qbdtPegServlet = new QBDTPegServlet();
            QBDTProcessResult<String> ofxResponsePR = qbdtPegServlet.convertRequestStreamToString("2",fileStream);
            assertTrue(ofxResponsePR.isSuccess());
            String ofxResponse = ofxResponsePR.getResult();
            String expectedTestOFX = "<OFX>\r\n" +
                    "<SIGNONMSGSRQV1>\r\n" +
                    "<SONRQ>\r\n" +
                    "<DTCLIENT>20081016161344\r\n" +
                    "<USERID>100001413\r\n" +
                    "<USERPASS>*****\r\n" +
                    "<LANGUAGE>ENG\r\n" +
                    "<APPVER>18.00.R.5/20814#accountant\r\n" +
                    "<APPID>QBWPRO\r\n" +
                    "<I.QBFILENAME>C:\\Documents and Settings\\sreynolds\\Desktop\\UAT_Gail's Group.QBW\r\n" +
                    "<I.QBFILEID>490627f835d249ce8d723ff3942b1870\r\n" +
                    "<I.IPADDRESS>FileInfo:QB_RNOL04L3YZF66_18:172.17.210.148#10180 ServerInfo:QB_RNOL04L3YZF66_18:172.17.210.148#10180\r\n" +
                    "<I.QBUSERNAME>Admin\r\n" +
                    "</SONRQ>\r\n" +
                    "</SIGNONMSGSRQV1>\r\n" +
                    "<I.PAYROLLMSGSRQV1>\r\n" +
                    "<I.PAYROLLUPDATERQ>\r\n" +
                    "<TOKEN>4\r\n" +
                    "<REJECTIFMISSING>Y\r\n" +
                    "<I.PAYROLLTRNRQ>\r\n" +
                    "<TRNUID>068433F0-7A36-1000-B771-D0B4870B0026\r\n" +
                    "<I.PAYROLLRQ>\r\n" +
                    "<I.PAYROLLRUN>\r\n" +
                    "<I.DTPAYCHKS>20081018\r\n" +
                    "<I.PAYCHK>\r\n" +
                    "<I.PAYCHKID>2\r\n" +
                    "<I.EMPID>0\r\n" +
                    "<I.DTTX>20081018\r\n" +
                    "<I.PAYCHKTYPE>PAYCHK\r\n" +
                    "<I.EMPNAME>Gail Padin\r\n" +
                    "<I.CLASS>^@~*\r\n" +
                    "<I.ACCTNAME>Bank Account\r\n" +
                    "<I.AMT>$0.00\r\n" +
                    "<I.PAYCHKINFO>\r\n" +
                    "<I.SICKACCRUED>^@~*\r\n" +
                    "<I.VACACCRUED>^@~*\r\n" +
                    "<I.PRORATE>N\r\n" +
                    "<I.CHKNUM>TOPRINT\r\n" +
                    "</I.PAYCHKINFO>\r\n" +
                    "<I.VOID>N\r\n" +
                    "<I.ONSERVICE>Y\r\n" +
                    "<I.DTPAYPDBEGIN>20081015\r\n" +
                    "<I.DTPAYPDEND>20081028\r\n" +
                    "<I.MEMO>Direct Deposit\r\n" +
                    "<I.CLEARED>2\r\n" +
                    "<I.DDLINE>\r\n" +
                    "<I.DDACCT>\r\n" +
                    "<I.ACCTNAME>Sane Bank\r\n" +
                    "<I.AMT>^@~*\r\n" +
                    "<BANKACCTTO>\r\n" +
                    "<BANKID>011302357\r\n" +
                    "<ACCTID>5645634544\r\n" +
                    "<ACCTTYPE>CHECKING\r\n" +
                    "</BANKACCTTO>\r\n" +
                    "</I.DDACCT>\r\n" +
                    "<I.PITEMID>0\r\n" +
                    "<I.AMT>$-257.05\r\n" +
                    "</I.DDLINE>\r\n" +
                    "</I.PAYCHK>\r\n" +
                    "<I.DDADVICE>\r\n" +
                    "<I.DDAMT>$-257.05\r\n" +
                    "<I.DD>\r\n" +
                    "<BANKACCTTO>\r\n" +
                    "<BANKID>011302357\r\n" +
                    "<ACCTID>5645634544\r\n" +
                    "<ACCTTYPE>CHECKING\r\n" +
                    "</BANKACCTTO>\r\n" +
                    "<I.EMPID>0\r\n" +
                    "<I.AMT>$-257.05\r\n" +
                    "<I.EMPNAME>Gail Padin\r\n" +
                    "<I.SSN>^@~*\r\n" +
                    "</I.DD>\r\n" +
                    "</I.DDADVICE>\r\n" +
                    "</I.PAYROLLRUN>\r\n" +
                    "</I.PAYROLLRQ>\r\n" +
                    "</I.PAYROLLTRNRQ>\r\n" +
                    "</I.PAYROLLUPDATERQ>\r\n" +
                    "</I.PAYROLLMSGSRQV1>\r\n" +
                    "</OFX>";
            assertEquals(expectedTestOFX,ofxResponse);
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail();
        }
    }

    @Test
    public void exerciseConvertRequestStreamToStringNull() {
        try {
            URL xsdURL = QBDTPegServletTest.class.getResource("/resources/zlib_compression.dat");
            InputStream fileStream = xsdURL.openStream();

//            FileInputStream fis = new FileInputStream("successful_compress.dat");
            QBDTPegServlet qbdtPegServlet = new QBDTPegServlet();
            QBDTProcessResult<String> ofxResponsePR = qbdtPegServlet.convertRequestStreamToString(null ,fileStream);
            assertTrue(ofxResponsePR.isSuccess());
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail();
        }
    }

    @Test
    public void testInvalidOFX() {
        String psid = "504000069";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        String request = "<OFX>\n" +
                "<SIGNONMSGSRQV1>\n" +
                "<SONRQ>\n" +
                "<DTCLIENT>20101119204621\n" +
                "<USERID>504000069\n" +
                "<USERPASS>*****\n" +
                "<LANGUAGE>ENG\n" +
                "<APPVER>19.00.R.12/21016#pro\n" +
                "<APPID>QBWPRO\n" +
                "<I.QBFILENAME>C:\\Mallard.qbw\n" +
                "<I.QBFILEID>1764dec3bd084821bddcdf5a7fc6486b\n" +
                "<I.IPADDRESS>FileInfo:QB_data_engine_19:192.168.1.104#0\n" +
                "<I.QBUSERNAME>Elaine\n" +
                "</SONRQ>\n" +
                "</SIGNONMSGSRQV1>\n" +
                "<I.PAYROLLMSGSRQV1>\n" +
                "<I.PAYROLLUPDATERQ>\n" +
                "<TOKEN>271\n" +
                "<REJECTIFMISSING>Y\n" +
                "<I.PAYROLLTRNRQ>\n" +
                "<TRNUID>C107E8E0-7ACF-1000-B771-DCF049D60026\n" +
                "<I.PAYROLLRQ>\n" +
                "<I.COINFOMOD>\n" +
                "<I.BANKNAME>^@~*\n" +
                "<I.ACCTNAME>[~ Wells Fargo Bank\n" +
                "<I.REGNUM>0881-2579-3942-416\n" +
                "<I.LEGALNAME>MALLARD CABINETRY, INC.\n" +
                "<I.FEIN>86-1020525\n" +
                "<I.ADDR1>PO BOX 313\n" +
                "<I.ADDR2>47853 W. HIGHWAY 288\n" +
                "<I.CITY>YOUNG\n" +
                "<I.STATE>AZ\n" +
                "<I.POSTALCODE>85554\n" +
                "<I.Q3TAXLIABAMT>$0.00\n" +
                "<I.Q4TAXLIABAMT>$0.00\n" +
                "<I.PRINCFIRSTNAME>^@~*\n" +
                "<I.PRINCLASTNAME>^@~*\n" +
                "<I.PRINCMIDINI>^@~*\n" +
                "<I.PRINCTITLE>^@~*\n" +
                "<I.CONTACT>\n" +
                "<I.CONFIRSTNAME>f &amp;�D�G G �{ 7 \n" +
                " �\n" +
                "<I.CONLASTNAME>�I\n" +
                "<I.CONMIDINI>� � � � \n" +
                "<I.CONTITLE>^@~*\n" +
                "<I.PHONE>� \n" +
                "<I.FAX>u � \n" +
                "<I.EMAIL>�I� �k� * ��(\n" +
                "</I.CONTACT>\n" +
                "<BANKACCTFROM>\n" +
                "<BANKID>^@~*\n" +
                "<ACCTID>@~*\n" +
                "<ACCTTYPE>CHECKING\n" +
                "</BANKACCTFROM>\n" +
                "<I.MAILADDR1>^@~*\n" +
                "<I.MAILADDR2>^@~*\n" +
                "<I.MAILCITY>^@~*\n" +
                "<I.MAILSTATE>^@~*\n" +
                "<I.MAILPCODE>^@~*\n" +
                "<I.STRTADDR1>^@~*\n" +
                "<I.STRTADDR2>^@~*\n" +
                "<I.STRTCITY>^@~*\n" +
                "<I.STRTSTATE>^@~*\n" +
                "<I.STRTPCODE>^@~*\n" +
                "</I.COINFOMOD>\n" +
                "<I.PAYROLLRUN>\n" +
                "<I.DTPAYCHKS>20101124\n" +
                "<I.PAYCHK>\n" +
                "<I.PAYCHKID>1279\n" +
                "<I.EMPID>0\n" +
                "<I.DTTX>20101124\n" +
                "<I.PAYCHKTYPE>PAYCHK\n" +
                "<I.EMPNAME>Mailliard, M. Elaine\n" +
                "<I.CLASS>^@~*\n" +
                "<I.ACCTNAME>[~ Wells Fargo Bank\n" +
                "<I.AMT>$0.00\n" +
                "<I.PAYCHKINFO>\n" +
                "<I.SICKACCRUED>^@~*\n" +
                "<I.VACACCRUED>^@~*\n" +
                "<I.PRORATE>N\n" +
                "<I.CHKNUM>Auto\n" +
                "</I.PAYCHKINFO>\n" +
                "<I.VOID>N\n" +
                "<I.ONSERVICE>Y\n" +
                "<I.DTPAYPDBEGIN>20101111\n" +
                "<I.DTPAYPDEND>20101124\n" +
                "<I.MEMO>Direct Deposit\n" +
                "<I.CLEARED>2\n" +
                "<I.DDLINE>\n" +
                "<I.DDACCT>\n" +
                "<I.ACCTNAME>First Credit Union\n" +
                "<I.AMT>^@~*\n" +
                "<BANKACCTTO>\n" +
                "<BANKID>322172742\n" +
                "<ACCTID>*****1533\n" +
                "<ACCTTYPE>CHECKING\n" +
                "</BANKACCTTO>\n" +
                "</I.DDACCT>\n" +
                "<I.PITEMID>0\n" +
                "<I.AMT>$-1478.35\n" +
                "</I.DDLINE>\n" +
                "</I.PAYCHK>\n" +
                "<I.DDADVICE>\n" +
                "<I.DDAMT>$-1478.35\n" +
                "<I.DD>\n" +
                "<BANKACCTTO>\n" +
                "<BANKID>322172742\n" +
                "<ACCTID>*****1533\n" +
                "<ACCTTYPE>CHECKING\n" +
                "</BANKACCTTO>\n" +
                "<I.EMPID>0\n" +
                "<I.AMT>$-1478.35\n" +
                "<I.EMPNAME>Mailliard, M. Elaine\n" +
                "<I.SSN>999-99-9999\n" +
                "</I.DD>\n" +
                "</I.DDADVICE>\n" +
                "</I.PAYROLLRUN>\n" +
                "</I.PAYROLLRQ>\n" +
                "</I.PAYROLLTRNRQ>\n" +
                "</I.PAYROLLUPDATERQ>\n" +
                "</I.PAYROLLMSGSRQV";
        TestHttpServletResponse httpServletResponse = new TestHttpServletResponse();
        QBDTPegServlet qbdtPegServlet = new QBDTPegServlet();
        qbdtPegServlet.processesRequest(new TestHttpServletRequest(request), httpServletResponse);
        assertTrue("responses is not an 18500 error " + httpServletResponse, httpServletResponse.toString().contains("<CODE>18500"));

        PayrollServices.beginUnitOfWork();
        String parameterCode = "UNCOMPRESSED_OFX_" + psid;
        SystemParameter systemParameter = SystemParameter.findSystemParameter(parameterCode);
        assertNotNull("system parameter", systemParameter);
        PayrollServices.rollbackUnitOfWork();
    }

}
