/*
 * : $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.processes.dataloaders.coretests;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.PayrollStatus;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company123272727DataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * ERFeeAddCoreDataLoader - DESCRIPTION
 *
 * @author Joe Warmelink
 */
public class ERFeeAddCoreDataLoader {
    private static SpcfUniqueId payrollRunId;

    public static void loadDataForAddFeeForACHSettlement() {
        PayrollRunDTO payrollRunDTO;

        PayrollServices.beginUnitOfWork();
        Company123272727DataLoader dataloader = new Company123272727DataLoader();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        payrollRunDTO = psdl.loadDataForPayrollSubmit();
        dataloader.savePayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                payrollRunDTO.getPayrollTXBatchId());

        payrollRun.setPayrollRunStatus(PayrollStatus.Complete);
        Application.save(payrollRun);

        PayrollServices.commitUnitOfWork();
        payrollRunId = payrollRun.getId();
    }

    public static void beforeEachTestForERFeeAdd() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    public static void loadDataForAddFeeForACHSettlementWithSetup() {
        beforeEachTestForERFeeAdd();
        loadDataForAddFeeForACHSettlement();
    }

    public static PayrollRun getPayrollRun() {
        return PayrollServices.entityFinder.findById(PayrollRun.class, payrollRunId);
    }

    public static Company getCompany() {
        return getPayrollRun().getCompany();
    }
}
