package com.intuit.sbd.payroll.psp.common;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *Converts an OFX message into XML.  It does this by simply adding end tags
 *to all lines that have <whitespace><OFXTag><text><EOLN>.  It also adds an
 *XML header.
 */
public class OFXToXML
{
    private static Map<String, List<String>> orderMap = new HashMap<String, List<String>>(2);
    static {
        // response only tags
        // I.PAYROLLRS
        List<String> strings = new ArrayList<String>(3);
        strings.add("I.PAYCHKMOD");
        strings.add("I.PAYROLLTXMOD");
        strings.add("I.PAYROLLTX");
        orderMap.put("I.PAYROLLRS", strings);

        // I.PAYROLLUPDATEDATA
        strings = new ArrayList<String>(6);
        strings.add("I.COINFOMOD");
        strings.add("I.PITEMDELID");
        strings.add("I.PITEMMOD");
        strings.add("I.EMPDELID");
        strings.add("I.EMPMOD");
        strings.add("I.PAYCHKMOD");
        strings.add("I.PAYROLLTXDELID");
        strings.add("I.PAYCHKDELID");
        strings.add("I.SESSIONID");
        strings.add("I.PAYROLLTXMOD");
        orderMap.put("I.PAYROLLUPDATEDATA", strings);
        // end response only tags
    }

    public static final String NEW_LINE_INDICATOR = "NEW_LINE_HERE";
    private static final Pattern FILE_LINE_PATTERN = Pattern.compile("^(.*)$", Pattern.MULTILINE | Pattern.UNIX_LINES);
    private static final Pattern LINE_PATTERN = Pattern.compile("\\s*(<([^>]*)>(.*))");
    private static final Pattern NEW_LINE_PATTERN = Pattern.compile(NEW_LINE_INDICATOR);
    private static final Pattern EMPTY_STRING_PATTERN = Pattern.compile("[\\x0A\\x0D]|\\^@~\\*");
    private static final Pattern SPACE_PATTERN = Pattern.compile("[\\x7F-\\xFF]|[\\x01-\\x1F]");

    /**
     *
     * @param ofx
     * @return
     * @throws MalformedOFXException
     */
    public static String convert(String ofx) throws MalformedOFXException {
        return convert(ofx,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
    }

    /**
     *Converts the OFX string passed in into XML.  It does this by simply
     *adding end tags to all lines that have <whitespace><OFXTag><text><EOLN>.
     *All other lines are left as-is.  It also adds an XML header.
     *
     *@param ofx The OFX to convert.
     *
     *@return The XML version of the OFX.
     *
     *@throws Exception - If the OFX could not be
     *                                         converted.
     */
    public static String convert(String ofx,OfxConversionOptions ofxConversionOption) throws MalformedOFXException {
        Pattern fileLinePattern = FILE_LINE_PATTERN;
        Matcher fileLineMatcher = fileLinePattern.matcher(ofx);
        Stack<OFXItem> ofxElementQueue = new Stack<OFXItem>();

        Pattern linePattern = LINE_PATTERN;
        OFXItem ofxItem;
        while (fileLineMatcher.find()){
            Matcher lineMatcher = linePattern.matcher(fileLineMatcher.group(0));

            if (lineMatcher.find()) {
                String ofxTag = lineMatcher.group(2);
                String ofxData = lineMatcher.group(3);

                ofxData = StringEscapeUtils.unescapeXml(ofxData);
                ofxData = StringEscapeUtils.escapeXml(ofxData);

                ofxElementQueue.push(new OFXItem(ofxTag,ofxData));
            } else {
                // handel tags with multiple lines
                ofxItem = ofxElementQueue.peek();
                String ofxData = fileLineMatcher.group(0);

                ofxData = StringEscapeUtils.unescapeXml(ofxData);
                ofxData = StringEscapeUtils.escapeXml(ofxData);

                ofxItem.setOfxData(ofxItem.getOfxData() + NEW_LINE_INDICATOR + ofxData);
            }
        }

        if (ofxElementQueue.size() == 0) {
            throw new MalformedOFXException("Error handling root OFX tag.");
        }

        StringBuilder stringBuilder = new StringBuilder((int)(ofx.length()*1.5));
        buildOFX_XML(stringBuilder, ofxElementQueue, null);
        String ofxXML = stringBuilder.toString();

        // Replace CR and NL and LF with ""
        ofxXML = replaceAll(EMPTY_STRING_PATTERN, ofxXML, "");
        ofxXML = replaceAll(SPACE_PATTERN, ofxXML, " ");
        ofxXML = replaceAll(NEW_LINE_PATTERN, ofxXML, "\r\n");
        return ofxXML;
    }

    private static void buildOFX_XML(StringBuilder pReturnStringBuilder, Stack<OFXItem> pOFXItemStack, String pTagToMatch) throws MalformedOFXException {
        if(pOFXItemStack.size() == 0) {
            throw new MalformedOFXException("Error handling OFX tag '" + pTagToMatch + "'.");
        }

        if(pTagToMatch == null) {
            // this is the first call for recursion
            OFXItem ofxItem = pOFXItemStack.pop();

            // starts with ending tag
            if(!ofxItem.getOfxTag().startsWith("/")) {
                throw new MalformedOFXException("Error handling OFX tag '" + ofxItem.getOfxTag() + "'.");
            }

            String tag = ofxItem.getOfxTag().substring(1);
            pReturnStringBuilder.append("<");
            pReturnStringBuilder.append(tag);
            pReturnStringBuilder.append(">");
            buildOFX_XML(pReturnStringBuilder, pOFXItemStack, tag);
            pReturnStringBuilder.append("</");
            pReturnStringBuilder.append(tag);
            pReturnStringBuilder.append(">");

            // missing matching tags
            if(pOFXItemStack.size() != 0) {
                throw new MalformedOFXException("Error handling OFX tag '" + ofxItem.getOfxTag() + "'.");
            }

        } else {
            OFXItem ofxItem = null;
            Map<String, List> map = new LinkedHashMap<String, List>();
            while (ofxItem == null || !ofxItem.getOfxTag().equals(pTagToMatch)) {
                if(pOFXItemStack.size() == 0 && ofxItem != null) {
                    throw new MalformedOFXException("Error handling OFX tag '" + ofxItem.getOfxTag() + "'.");
                }
                ofxItem = pOFXItemStack.pop();
                String tag = ofxItem.getOfxTag();                

                if(ofxItem.getOfxTag().startsWith("/")) {
                    tag = tag.substring(1);
                        StringBuilder subTree = new StringBuilder();
                        subTree.append("<");
                        subTree.append(tag);
                        subTree.append(">");
                        buildOFX_XML(subTree, pOFXItemStack, tag);
                        subTree.append("</");
                        subTree.append(tag);
                        subTree.append(">");
                        if(map.get(tag) == null) {
                            map.put(tag, new ArrayList());
                        }
                        map.get(tag).add(subTree);
                } else if(!tag.equals(pTagToMatch)) {
                    if(map.get(tag) == null) {
                        map.put(tag, new ArrayList());
                    }
                    map.get(tag).add(ofxItem);
                }
            }

            List<String> keys = new ArrayList<String>(map.keySet());
            if(orderMap.get(pTagToMatch) != null) {
                Collections.sort(keys, new OrderComparator(orderMap.get(pTagToMatch)));
            }

            // reverse the order of the keys added to the map for recursion
            // we are reading the tags in the OFX from bottom to top, but we want to
            // print them from top to bottom
            Collections.reverse(keys);

            for (String key : keys) {                
                buildXML(pReturnStringBuilder, map.get(key));
            }
        }
    }

    private static void buildXML(StringBuilder pReturnStringBuilder, Object pO) {
        if(pO instanceof StringBuilder) {
            pReturnStringBuilder.append(pO.toString());
        } else if(pO instanceof OFXItem) {
            pReturnStringBuilder.append("<");
            pReturnStringBuilder.append(((OFXItem) pO).getOfxTag());
            pReturnStringBuilder.append(">");
            String data = ((OFXItem) pO).getOfxData();
            pReturnStringBuilder.append(data);
            pReturnStringBuilder.append("</");
            pReturnStringBuilder.append(((OFXItem) pO).getOfxTag());
            pReturnStringBuilder.append(">");
        } else {
            ArrayList arrayList = (ArrayList)pO;
            Collections.reverse(arrayList);
            for (Object o : arrayList) {                
                buildXML(pReturnStringBuilder, o);
            }
        }
    }

    private static String replaceAll(Pattern pPattern, String inputString, String pReplacement) {
        Matcher matcher = pPattern.matcher(inputString);        
        return matcher.replaceAll(pReplacement);
    }

    private static class OrderComparator implements Comparator<String> {
        private List<String> mOrderList;

        private OrderComparator(List<String> pOrderList) {
            mOrderList = pOrderList;
        }

        public int compare(String a, String b) {
            if(mOrderList.contains(a) && mOrderList.contains(b)) {
                return mOrderList.indexOf(a) > mOrderList.indexOf(b) ? -1 : 1;
            }

            return 0;
        }
    }
}
