package com.intuit.sbd.payroll.psp.agency.eftps;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Dec 14, 2010
 * Time: 3:50:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class RejectionInfo {
    private String id;
    private String code;

    public RejectionInfo(String id, String code) {
        this.id = id;
        this.code = code;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }
}
