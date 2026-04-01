package com.intuit.sbd.payroll.psp.common;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: May 21, 2008
 * Time: 10:32:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class OFXItem {
    String ofxTag;
    String ofxData;

    public OFXItem(String ofxTag,String ofxData) {
        this.ofxTag=ofxTag;
        this.ofxData=ofxData;
    }

    public String getOfxTag() {
        return ofxTag;
    }

    public String getOfxData() {
        return ofxData;
    }

    public void setOfxData(String pOfxData) {
        ofxData = pOfxData;
    }
}
