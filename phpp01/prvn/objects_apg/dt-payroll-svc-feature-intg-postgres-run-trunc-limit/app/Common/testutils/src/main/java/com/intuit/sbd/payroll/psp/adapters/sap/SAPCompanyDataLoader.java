/*
 * : $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.AssertionFailedError;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

/**
 * SAPCompanyDataLoader - DESCRIPTION
 *
 * @author Joe Warmelink
 */
public class SAPCompanyDataLoader {

    public static void createCompanyData() throws Exception {
        // Create 1 PSP data loader company with a payroll run
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        PayrollServices.commitUnitOfWork();

        SpcfCalendar payRunDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(payRunDate, 10);

        PayrollServices.beginUnitOfWork();
        try {
            Company1Dataloader.persistPayrollRun(c1dl.getCompany1PR_ExceedsLimits(new DateDTO(payRunDate)));
        } catch (AssertionFailedError ex) {
            // expected to throw
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company1Dataloader.persistPayrollRun(c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO(payRunDate)));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company1Dataloader.persistPayrollRun(c1dl.getCompany1PR2_DoesNotExceedLimits(new DateDTO(payRunDate)));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company1Dataloader.persistPayrollRun(create500paychecks(new DateDTO(payRunDate)));
        PayrollServices.commitUnitOfWork();

        // Modify the PSP data loader company and change the notification email and funding model to generate history
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        company.setNotificationEmail("joe_warmelink@intuit.com");
        DomainEntitySet<FundingModel> fundingModelList =
                PayrollServices.entityFinder.findObjects(FundingModel.class);
        for (FundingModel fundingModel : fundingModelList) {
            if (!company.getFundingModel().getFundingModelCd().equals(fundingModel.getFundingModelCd())) {
                company.setFundingModel(fundingModel);
                break;
            }
        }
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        // Modify the PSP data loader company and change the DD limits to generate history
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateDDLimits(SourceSystemCode.QBOE,
                                                      "1234567",
                                                      new SpcfMoney("2500000.0"),
                                                      new SpcfMoney("12500.0"));
        PayrollServices.commitUnitOfWork();


        //Increase the limits
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateDDLimits(SourceSystemCode.QBOE,
                                                      "1234567",
                                                      new SpcfMoney("2700000.0"),
                                                      new SpcfMoney("17500.0"));
        PayrollServices.commitUnitOfWork();

        // Add a note to the company
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.addCompanyNote(SourceSystemCode.QBOE, "1234567", null, "10000112518", "My notes are awesome.", false);
        PayrollServices.commitUnitOfWork();

        // Add a manual strike to the company
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.addStrikeEvent(SourceSystemCode.QBOE, "1234567", "Strike for testing",
                PSPDate.getPSPTime());
         PayrollServices.commitUnitOfWork();

        //Deactivate and reactivate service
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.deactivateService(SourceSystemCode.QBOE, "1234567", ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.reactivateService(SourceSystemCode.QBOE, "1234567", ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();
    }

    private static PayrollRunDTO create500paychecks(DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        Company1Dataloader c1dl = new Company1Dataloader();

        // Set Service Bank Accounts
        Company company = Company.findCompany(c1dl.getCompany1().getCompanyId(), c1dl.getCompany1().getSourceSystemCd());
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().get(0);
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        CompanyBankAccountDTO companyBankAccountDTO = c1dl.createCompanyBankAccountDTO(companyBankAccount);
        companyBankAccounts.add(c1dl.createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId("BatchTest99");

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();
        Collection<DDTransactionDTO> ee1Txns;
        DDTransactionDTO ee1PaycheckSplit1;
        for(int i = 1; i <= 500; i++) {
            ee1Txns = new ArrayList<DDTransactionDTO>();
            ee1PaycheckSplit1 = new DDTransactionDTO();
            ee1PaycheckSplit1.setEmployeeBankAccount(c1dl.getEmployee1BankAccount());
            ee1PaycheckSplit1.setDDTransactionId("EEBA1PT" + i);
            BigDecimal amount = new BigDecimal(i);
            amount = amount.divide(new BigDecimal(10), 2, BigDecimal.ROUND_HALF_EVEN);
            ee1PaycheckSplit1.setDDTransactionAmount(amount);
            ee1Txns.add(ee1PaycheckSplit1);
            PaycheckDTO paycheckDTO = c1dl.createPaycheckDTO(ee1Txns, c1dl.getEmployee1(null).getSourceEmployeeId(),
                    SpcfUniqueId.createInstance(true).toString());
            paycheckDTO.setPaycheckNetAmount(SpcfUtils.convertToSpcfMoney(amount));
            paychecks.add(paycheckDTO);
        }

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public static void createEventTestCompany() throws Exception {
        createCompanyData();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PayrollRun pr = PayrollRun.findPayrollRun(company, "BatchTest05");


        CompanyEvent.createACHReturnStatusChangeEvent(company, "12355", PayrollStatus.Canceled, PayrollStatus.AutoRedebitOffloaded);
        CompanyEvent.createBackdatedPayrollEvent(pr, 5);

        makeEvent(company, pr, EventTypeCode.EmployeesPaidToSameBankAccount, makeNote(4000));
        makeEvent(company, pr, EventTypeCode.EmployeesPaidToSameBank, makeNote(300));
        makeEvent(company, pr, EventTypeCode.EmployeeInTermedCompany, makeNote(100));

        CompanyEvent.createManualRedebitCreatedEvent(company, pr.getFinancialTransactionCollection().get(0));

        CompanyEvent.createPINUpdatedEvent(company);

        PayrollServices.commitUnitOfWork();
    }

    private static String makeNote(int end) {
        String s = "More than 3 unique employees paid into a single bank account on the same day \n" +
                        "\n" +
                        "Bank Name: abc bank\n" +
                        "Bank Routing: 111000025\n" +
                        "Account Type: Checking\n" +
                        "Bank Account: 12345\n" +
                        "\n" +
                        "Source Payroll Id: BatchTestExceedsBALimits\n" +
                        "Payroll Date: October 02, 2007 12:00 AM\n" +
                        "Payroll Amount: $21,000.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE3 TestLastName3\n" +
                        "Paycheck Amount: $2,000.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE1 TestLastName\n" +
                        "Paycheck Amount: $8,000.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE2 TestLastName2\n" +
                        "Paycheck Amount: $9,000.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE4 TestLastName4\n" +
                        "Paycheck Amount: $2,000.00\n" + "\n";
        return end < s.length() ? s.substring(0, end) : s;
    }

    private static void makeEvent(Company company, PayrollRun pr, EventTypeCode code, String note) {
        Application.save(CompanyEvent.createFraudPayrollEvent(company, code, pr, note));
    }

    /*public static void create1000Companies() throws Exception {
        PayrollServicesTest.truncateTables();
        MockCompanyTestSetup.create1000MockCompanies();

        PayrollServices.beginUnitOfWork();

        for (int i=0;i<1000;i++){
            Company c = Company.findCompany("100000000" + i, SourceSystemCode.QBOE);
            if (i % 3 == 0){
                c.addOnHoldReason(ServiceSubStatusCode.Fraud);
            }
            if (i % 5 == 0){
                c.addOnHoldReason(ServiceSubStatusCode.IntuitCollections);
            }
            if (i % 7 == 0){
                c.addOnHoldReason(ServiceSubStatusCode.RiskAssessment);
            }
            if (i % 11 == 0){
                c.addOnHoldReason(ServiceSubStatusCode.MissingPaperwork);
            }
        }

        PayrollServices.commitUnitOfWork();

    }


    public static void main(String[] args) {
        PayrollServices.beginUnitOfWork();

        Company1Dataloader c1dl = new Company1Dataloader();
        try{
            c1dl.persistCompany1();
        } catch (Throwable e){
            // ignore
        }
        PayrollServices.rollbackUnitOfWork();

        SpcfCalendar payRunDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(payRunDate, 10);

        PayrollServices.beginUnitOfWork();
        c1dl.persistPayrollRun(c1dl.getCompany1PR_MultiplePaychecksSameEE(new DateDTO(payRunDate)));
        PayrollServices.commitUnitOfWork();
    }
    */
}
