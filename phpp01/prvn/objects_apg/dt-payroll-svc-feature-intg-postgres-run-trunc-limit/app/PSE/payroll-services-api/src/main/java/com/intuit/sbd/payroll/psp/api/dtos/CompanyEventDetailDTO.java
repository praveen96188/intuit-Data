/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/CompanyEventDetailDTO.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.Collection;

public class CompanyEventDetailDTO {


    private EventDetailTypeCode eventDetailTypeCode;
    private String eventDetailValue;

    public EventDetailTypeCode getEventDetailTympeCode() {
        return eventDetailTypeCode;
    }

    public void setEventDetailTypeCode(EventDetailTypeCode eventDetailTypeCode) {
        this.eventDetailTypeCode = eventDetailTypeCode;
    }

    public String getEventDetailValue() {
        return eventDetailValue;
    }

    public void setEventDetailValue(String eventDetailValue) {
        this.eventDetailValue = eventDetailValue;
    }

    /**
     * Validates a CompanyEventDetail DTO
     *
     * @return
     */
    public ProcessResult validateCompanyEventDetailDTO() {
        ProcessResult validationResult = new ProcessResult();


        return validationResult;
    }


}