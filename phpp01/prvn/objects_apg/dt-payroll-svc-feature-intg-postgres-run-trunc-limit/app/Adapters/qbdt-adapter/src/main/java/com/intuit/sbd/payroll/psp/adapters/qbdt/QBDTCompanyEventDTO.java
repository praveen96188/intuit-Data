package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: Sep 2, 2008
 * Time: 1:41:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class QBDTCompanyEventDTO {
    private Company company;
    private EventTypeCode eventTypeCode;
    private String transmissionId;
    private String errMsg;

    /**
     *
     * @return
     */
    public Company getCompany() {
        return company;
    }

    /**
     *
     * @param company
     */
    public void setCompany(Company company) {
        this.company = company;
    }

    /**
     *
     * @return
     */
    public EventTypeCode getEventTypeCode() {
        return eventTypeCode;
    }

    /**
     *
     * @param eventTypeCode
     */
    public void setEventTypeCode(EventTypeCode eventTypeCode) {
        this.eventTypeCode = eventTypeCode;
    }

    /**
     *
     * @return
     */
    public String getTransmissionId() {
        return transmissionId;
    }

    /**
     *
     * @param transmissionId
     */
    public void setTransmissionId(String transmissionId) {
        this.transmissionId = transmissionId;
    }

    /**
     *
     * @return
     */
    public String getErrMsg() {
        return errMsg;
    }

    /**
     *
     * @param errMsg
     */
    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

}
