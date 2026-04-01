package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.PstubMsgType;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 2/20/13
 * Time: 4:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class PstubMsgDTO {
    private String mText;
    private PstubMsgType mType;

    public String getText() {
        return mText;
    }

    public void setText(String pText) {
        mText = pText;
    }

    public PstubMsgType getType() {
        return mType;
    }

    public void setType(PstubMsgType pType) {
        mType = pType;
    }
}
