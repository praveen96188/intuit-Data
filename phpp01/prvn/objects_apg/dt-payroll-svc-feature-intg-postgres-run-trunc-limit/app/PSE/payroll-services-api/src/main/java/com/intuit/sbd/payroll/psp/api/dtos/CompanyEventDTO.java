/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/CompanyEventDTO.java#1 $
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
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.Collection;

public class CompanyEventDTO {
    public EventTypeCode getEventTypeCode() {
        return eventTypeCode;
    }

    public void setEventTypeCode(EventTypeCode eventTypeCode) {
        this.eventTypeCode = eventTypeCode;
    }

    public Collection<CompanyEventDetailDTO> getEventDetails() {
        return eventDetails;
    }

    public void setEventDetails(Collection<CompanyEventDetailDTO> eventDetails) {
        this.eventDetails = eventDetails;
    }

    private EventTypeCode eventTypeCode;
    private Collection<CompanyEventDetailDTO> eventDetails;


    /**
     * Validates a CompanyEvent DTO
     *
     * @return
     */
    public ProcessResult validateCompanyEventDTO() {
        ProcessResult validationResult = new ProcessResult();


        return validationResult;
    }



}