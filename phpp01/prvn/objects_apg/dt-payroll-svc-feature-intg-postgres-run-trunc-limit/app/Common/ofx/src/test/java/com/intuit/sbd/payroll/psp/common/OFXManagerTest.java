package com.intuit.sbd.payroll.psp.common;

import junit.framework.TestCase;
import org.junit.*;

public class OFXManagerTest extends TestCase {

    public static String newline = System.getProperty("line.separator");

    @Before
    public void runBeforeEachTestCase() {

    }

    @After
    public void runAfterEachTestCase() {

    }

    @Test
    public void testHappyPath() {
        String userid = "USER_ID";
        String  origOfxStr = "<OFX>" + newline +
                "  <SIGNONMSGSRQV1>" + newline +
                "    <SONRQ>" + newline +
                "      <DTCLIENT>0" + newline +
                "      <USERID>"+userid+"" + newline +
                "      <USERPASS>^@~*" + newline +
                "      <LANGUAGE>ENG" + newline +
                "      <APPVER>50.00.R.3/20804#pro" + newline +
                "      <APPID>QBWPRO" + newline +
                "      <I.QBFILENAME>^@~*" + newline +
                "      <I.QBFILEID>^@~*" + newline +
                "      <I.IPADDRESS>^@~*" + newline +
                "      <I.QBUSERNAME>^@~*" + newline +
                "    </SONRQ>" + newline +
                "  </SIGNONMSGSRQV1>" + newline +
                "</OFX>" + newline;
        com.intuit.sbd.payroll.psp.common.ofx.request.OFX ofxObj = null;
        try {
            ofxObj = OFXManager.ofxRequestToJava(origOfxStr);
        } catch (Exception e) {
            TestCase.fail("Unexpected exception caught: " + e.toString());
        }
        String ofxObjUserId = ofxObj.getSIGNONMSGSRQV1().getSONRQ().getUSERID();
        assertEquals(ofxObjUserId,userid);
        com.intuit.sbd.payroll.psp.common.ofx.request.ObjectFactory requestObjFactory = new com.intuit.sbd.payroll.psp.common.ofx.request.ObjectFactory();
        String rtnOfxStr = OFXManager.javaToOFX(ofxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertEquals(rtnOfxStr.replaceAll("[\\x00-\\x0F]",""),origOfxStr.replaceAll("[\\x00-\\x0F]",""));
    }

    public void testBadOFX() {
        String userid = "USER_ID";
        String  origOfxStr = "<OFX>" + newline +
                "<SIGNONMSGSRQV1>" + newline +
                "<SONRQ>" + newline +
                "<DTCLIENT>0" + newline +
                "<USERID>"+userid+"" + newline +
                "</SONRQ>" + newline +
                "</SIGNONMSGSRQV1_XXXX>" + newline +
                "</OFX>" + newline;
        com.intuit.sbd.payroll.psp.common.ofx.request.OFX ofxObj = null;
        boolean caughtMalformedOFXException = false;
        try {
            ofxObj = OFXManager.ofxRequestToJava(origOfxStr);
        } catch (MalformedOFXException malformedOFX) {
            System.out.println("Error: " + malformedOFX.toString());
            caughtMalformedOFXException = true;
        } catch (Exception e) {
            TestCase.fail("Unexpected exception caught: " + e.toString());
        }
        if (!caughtMalformedOFXException) {
            TestCase.fail("MalformedOFX expected but not caught");
        }
    }

    public void testUnexpectedOFXElement() {
        String userid = "USER_ID";
        String  origOfxStr = "<OFX>" + newline +
                "<SIGNONMSGSRQV1>" + newline +
                "<SONRQ>" + newline +
                "<DTCLIENT>0" + newline +
                "<USERID>"+userid+"" + newline +
                "<FOOOOOO>LA LA LA" + newline +
                "</SONRQ>" + newline +
                "</SIGNONMSGSRQV1>" + newline +
                "</OFX>" + newline;
        com.intuit.sbd.payroll.psp.common.ofx.request.OFX ofxObj = null;
        boolean caughtOFXToJavaMappingErrorException = false;
        try {
            ofxObj = OFXManager.ofxRequestToJava(origOfxStr);
        } catch (OFXToJavaMappingError ofxToJavaMappingError) {
            System.out.println("Error: " + ofxToJavaMappingError.toString());
            caughtOFXToJavaMappingErrorException = true;
        } catch (Exception e) {
            TestCase.fail("Unexpected exception caught: " + e.toString());
        }
        if (!caughtOFXToJavaMappingErrorException) {
            TestCase.fail("OFXToJavaMappingError expected but not caught");
        }
    }

    public void testMissingOFXElement() {
        String userid = "USER_ID";
        String  origOfxStr = "<OFX>" + newline +
                "<SIGNONMSGSRQV1>" + newline +
                "<SONRQ>" + newline +
                "<DTCLIENT>0" + newline +
                "<USERID>"+userid+"" + newline +
                "<USERPASS>^@~*" + newline +
//                "<LANGUAGE>ENG" + newline +
                "<APPVER>50.00.R.3/20804#pro" + newline +
                "<APPID>QBWPRO" + newline +
                "<I.QBFILENAME>^@~*" + newline +
                "<I.QBFILEID>^@~*" + newline +
                "<I.IPADDRESS>^@~*" + newline +
                "<I.QBUSERNAME>^@~*" + newline +
                "</SONRQ>" + newline +
                "</SIGNONMSGSRQV1>" + newline +
                "</OFX>" + newline;
        com.intuit.sbd.payroll.psp.common.ofx.request.OFX ofxObj = null;
        boolean caughtOFXToJavaMappingErrorException = false;
        try {
            ofxObj = OFXManager.ofxRequestToJava(origOfxStr);
        } catch (OFXToJavaMappingError ofxToJavaMappingError) {
            System.out.println("Error: " + ofxToJavaMappingError.toString());
            caughtOFXToJavaMappingErrorException = true;
        } catch (Exception e) {
            TestCase.fail("Unexpected exception caught: " + e.toString());
        }
        if (!caughtOFXToJavaMappingErrorException) {
            TestCase.fail("OFXToJavaMappingError expected but not caught");
        }
    }

    @Test
    public void testEscapedXMLCharactersQBDT() {
        String escapedOFXUserid = "\\ &lt;joe&amp;&gt;'\" /";
        String actualUserid = "\\ <joe&>'\" /";
        String  origOfxStr = "<OFX>" + newline +
                "  <SIGNONMSGSRQV1>" + newline +
                "    <SONRQ>" + newline +
                "      <DTCLIENT>0" + newline +
                "      <USERID>"+escapedOFXUserid+"" + newline +
                "      <USERPASS>^@~*" + newline +
                "      <LANGUAGE>ENG" + newline +
                "      <APPVER>50.00.R.3/20804#pro" + newline +
                "      <APPID>QBWPRO" + newline +
                "      <I.QBFILENAME>^@~*" + newline +
                "      <I.QBFILEID>^@~*" + newline +
                "      <I.IPADDRESS>^@~*" + newline +
                "      <I.QBUSERNAME>^@~*" + newline +
                "    </SONRQ>" + newline +
                "  </SIGNONMSGSRQV1>" + newline +
                "</OFX>" + newline;
        com.intuit.sbd.payroll.psp.common.ofx.request.OFX ofxObj = null;
        try {
            ofxObj = OFXManager.ofxRequestToJava(origOfxStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail("Unexpected exception caught: " + e.toString());
        }
        String ofxObjUserId = ofxObj.getSIGNONMSGSRQV1().getSONRQ().getUSERID();
        assertEquals(actualUserid,ofxObjUserId);
        com.intuit.sbd.payroll.psp.common.ofx.request.ObjectFactory requestObjFactory = new com.intuit.sbd.payroll.psp.common.ofx.request.ObjectFactory();
        String rtnOfxStr = OFXManager.javaToOFX(ofxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertEquals(origOfxStr.replaceAll("[\\x00-\\x0F]",""),rtnOfxStr.replaceAll("[\\x00-\\x0F]",""));
    }

    @Test
    public void testUnprintableChars() {
        char tabChar = 0x09;
        char deleteChar = 0x7F;
        String userid = "b" + tabChar + "m1"+ deleteChar + "m2"+ deleteChar + "a";
        String expectedUserid = "b" + " " + "m1"+ " " + "m2"+ " " + "a";
        String origOfxStr = "<OFX>" + newline +
                "<SIGNONMSGSRQV1>" + newline +
                "<SONRQ>" + newline +
                "<DTCLIENT>0" + newline +
                "<USERID>"+userid+"" + newline +
                "<USERPASS>^@~*" + newline +
                "<LANGUAGE>ENG" + newline +
                "<APPVER>50.00.R.3/20804#pro" + newline +
                "<APPID>QBWPRO" + newline +
                "<I.QBFILENAME>^@~*" + newline +
                "<I.QBFILEID>^@~*" + newline +
                "<I.IPADDRESS>^@~*" + newline +
                "<I.QBUSERNAME>^@~*" + newline +
                "</SONRQ>" + newline +
                "</SIGNONMSGSRQV1>" + newline +
                "</OFX>" + newline;
        com.intuit.sbd.payroll.psp.common.ofx.request.OFX ofxObj = null;
        try {
            ofxObj = OFXManager.ofxRequestToJava(origOfxStr);
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail("Unexpected exception caught: " + e.toString());
        }
        String ofxObjUserId = ofxObj.getSIGNONMSGSRQV1().getSONRQ().getUSERID();
        assertEquals(expectedUserid,ofxObjUserId);
        com.intuit.sbd.payroll.psp.common.ofx.request.ObjectFactory requestObjFactory = new com.intuit.sbd.payroll.psp.common.ofx.request.ObjectFactory();
        String rtnOfxStr = OFXManager.javaToOFX(ofxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertNotSame(rtnOfxStr.replaceAll("[\\x00-\\x0F]",""),origOfxStr.replaceAll("[\\x00-\\x0F]",""));
    }
}
