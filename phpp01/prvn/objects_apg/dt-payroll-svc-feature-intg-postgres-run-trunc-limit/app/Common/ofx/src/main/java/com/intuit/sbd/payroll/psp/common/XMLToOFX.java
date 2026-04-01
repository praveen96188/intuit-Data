package com.intuit.sbd.payroll.psp.common;

import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: Dec 14, 2007
 * Time: 2:31:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class XMLToOFX {
    
    public static final String TAX_ITEM_NAME_AMPERSAND = "TAX_ITEM_AMP";

    // this is a list of complex type tags that should not be converted to ^@~*
    // not an ideal solution, but we can revisit it later
    private static List<String> OFX_COMPLEX_TAGS;

    static {
        OFX_COMPLEX_TAGS = new ArrayList<String>(Arrays.asList("STATUS",
                                                                        "SONRS",
                                                                        "SIGNONMSGSRSV1",
                                                                        "OFX",
                                                                        "I.PAYSVCMSGSRSV1",
                                                                        "I.PAYSVCSYNCRS",
                                                                        "I.DSSCUINFOMODTRNRS",
                                                                        "I.TXLINE",
                                                                        "I.TAXSERVSTATUS",
                                                                        "I.PAYROLLUPDATERS",
                                                                        "I.PAYROLLUPDATEDATA",
                                                                        "I.PAYROLLTX",
                                                                        "I.PAYROLLTRNRS",
                                                                        "I.PAYROLLRS",
                                                                        "I.PAYROLLMSGSRSV1",
                                                                        "I.EMPMOD",
                                                                        "I.PITEMMOD",
                                                                        "I.ADDRINFO",
                                                                        "I.PAYROLLTXMOD",
                                                                        "I.PAYCHKMOD",
                                                                        "I.DDLINE",
                                                                        "I.PAYCHKINFO",
                                                                        "I.DDSTATUS",
                                                                        "I.COINFOMOD",
                                                                        "I.CUEVENTTRNRS",
                                                                        "I.CUEVENTRS",
                                                                        "I.PAYROLLEVENT",
                                                                        "I.EVENTREASON",
                                                                        "I.CUINFOMODTRNRS",
                                                                        "I.CUINFOMODRS",
                                                                        "I.STATELIST",
                                                                        "I.PRINCIPALLIST",
                                                                        "I.PRINCIPAL",
                                                                        "I.CONTACT",
                                                                        "I.ADDRESSLIST",
                                                                        "I.ADDRESS",
                                                                        "I.EMPTAX",
                                                                        "I.PAYROLL",
                                                                        "I.WAGE",
                                                                        "I.SICK",
                                                                        "I.VAC",
                                                                        "I.EMPDD",
                                                                        "I.EMPCOMPLIANCE",
                                                                        "I.SETTING",
                                                                        "I.DDACCT",
                                                                        "BANKACCTTO",
                                                                        "BANKACCTFROM",
                                                                        "I.EMPFIT",
                                                                        "I.EMPSIT",
                                                                        "I.EMPSDI",
                                                                        "I.EMPOTHERTAX",
                                                                        "I.EMPSUI",
                                                                        "I.CUSTOMFLD",
                                                                        "I.ADJ",
                                                                        "I.BONUSITEM",
                                                                        "I.COMMITEM",
                                                                        "I.DDITEM",
                                                                        "I.TAXITEM",
                                                                        "I.ADDITEM",
                                                                        "I.DEDUCTITEM",
                                                                        "I.SALARYITEM",
                                                                        "I.CONTRIBITEM",
                                                                        "I.RATECHANGE",
                                                                        "I.HRLYITEM",
                                                                        "I.STATETAXDESC",
                                                                        "I.HRLYWAGELINE",
                                                                        "I.SALARYLINE",
                                                                        "I.ADJLINE",
                                                                        "I.TAXLINE",
                                                                        "SONRQ",
                                                                        "SIGNONMSGSRQV1",
                                                                        "I.PAYSVCSYNCRQ",
                                                                        "I.DSSCUINFOMODTRNRQ",
                                                                        "I.PAYSVCMSGSRQV1",
                                                                        "I.PAYROLLUPDATERQ",
                                                                        "I.PAYROLLTRNRQ",
                                                                        "I.PAYROLLRUN",
                                                                        "I.DISBURSEADVICE",
                                                                        "I.TAXLIAB",
                                                                        "I.PAYROLLRQ",
                                                                        "I.PITEM",
                                                                        "I.PAYROLLMSGSRQV1",
                                                                        "I.EMP",
                                                                        "I.PAYCHK",
                                                                        "I.DDADVICE",
                                                                        "I.DD",
                                                                        "I.DSSCUINFOMODRQ"));
    }

    private static final String OFX_TAB = "  ";

    public static String convert(Document xmlDoc,OfxConversionOptions ofxConversionOption) throws Exception {
        Element docRootNode = xmlDoc.getDocumentElement();
        StringBuffer strBuf = new StringBuffer(1000);
        handleNode(strBuf,docRootNode,ofxConversionOption, "");
        return strBuf.toString();
    }

    public static void handleNode(StringBuffer strBuf,Node xmlNode,OfxConversionOptions ofxConversionOption, String pTabs) throws Exception {
        String tagName = xmlNode.getNodeName();
        strBuf.append(pTabs).append("<").append(tagName).append(">");
        NodeList nodeList = xmlNode.getChildNodes();
        // Empty XML elements have no child element.
        if (nodeList.getLength() == 0) {
            if(OFX_COMPLEX_TAGS.contains(tagName)) {
                strBuf.append("\n").append(pTabs).append("</").append(tagName).append(">\n");
            } else {
                strBuf.append(QBOFX.EMPTY_STR).append("\n");
            }
        } else if ((nodeList.getLength()==1) && (nodeList.item(0).getNodeType()==3)) {
            // If the only child is a text node, then we are at the lowest level
            String nodeValue = nodeList.item(0).getNodeValue();
            if (ofxConversionOption==OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES) {
                if (nodeValue != null) {
                    nodeValue = nodeValue.replace("&","&amp;");
                    nodeValue = nodeValue.replace("<","&lt;");
                    nodeValue = nodeValue.replace(">","&gt;");
                    nodeValue = nodeValue.replace(TAX_ITEM_NAME_AMPERSAND, "&");
                }
            }
            strBuf.append(nodeValue).append("\n");
        } else {
            strBuf.append("\n");
            for (int i=0;i<nodeList.getLength();i++) {
                Node thisNode = nodeList.item(i);
                handleNode(strBuf,thisNode,ofxConversionOption, pTabs + OFX_TAB);
            }
            strBuf.append(pTabs).append("</").append(xmlNode.getNodeName()).append(">\n");
        }
    }


}
