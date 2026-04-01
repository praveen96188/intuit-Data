/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/ServiceInfoDTO.java#3 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.OfferingCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.FundingModel;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

public class ServiceInfoDTO {
    private ServiceCode serviceCode;
    private FundingModel fundingModel;
    private SpcfCalendar serviceStartDate;
    private OfferingCode offeringCode;

    public ServiceCode getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(ServiceCode pServiceCode) {
        this.serviceCode = pServiceCode;
    }

    public FundingModel getFundingModel() {
        return fundingModel;
    }

    public void setFundingModel(FundingModel fundingModel) {
        this.fundingModel = fundingModel;
    }

    public SpcfCalendar getServiceStartDate() {
        return serviceStartDate;
    }

    public void setServiceStartDate(SpcfCalendar serviceStartDate) {
        this.serviceStartDate = serviceStartDate;
    }

    public OfferingCode getOfferingCode() {
        return offeringCode;
    }

    public void setOfferingCode(OfferingCode offeringCode) {
        this.offeringCode = offeringCode;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (serviceCode == null) {
            validationResult.getMessages().InvalidValue(EntityName.Service, null, "ServiceCode");
        }

        return validationResult;
    }
    
}
