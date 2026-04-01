package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.FormType;

/**
 * Created by IntelliJ IDEA.
 * User: mamin
 * Date: Mar 13, 2009
 * Time: 6:56:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class DocumentDTO {
    private String mimeType;
    private FormType formId;
    private byte[] document;

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public FormType getFormId() {
        return formId;
    }

    public void setFormId(FormType formId) {
        this.formId = formId;
    }
    public byte[] getDocument() {
        return document;
    }

    public void setDocument(byte[] pDocument) {
        this.document = pDocument;
    }
}
