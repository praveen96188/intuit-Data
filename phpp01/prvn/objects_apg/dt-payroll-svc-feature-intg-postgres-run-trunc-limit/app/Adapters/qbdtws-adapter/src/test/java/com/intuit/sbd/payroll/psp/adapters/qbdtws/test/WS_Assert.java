package com.intuit.sbd.payroll.psp.adapters.qbdtws.test;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.QBDTWSSubmitPayrollRequestProcess;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.*;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.QBProcessingMessage;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.QBProcessingMessageLevel;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.Assert;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * User: rnorian
 * Date: Mar 26, 2010
 * Time: 3:59:52 PM
 */
public class WS_Assert {
    public static void assertSuccess(String assertMessage, ProcessingResponse processResult) {
        if (processResult.getProcessingMessagesList().size() > 0) {
            printResponse(processResult);
        }
        assertEquals(assertMessage, 0, processResult.getProcessingMessagesList().size());
    }

    public static void assertCount(int expectedCount, ProcessingResponse processResult) {
        assertCount("ProcessingResponse message count", expectedCount, processResult);
    }

    public static void assertCount(String assertMessage, int expectedCount, ProcessingResponse processResult) {
        if (expectedCount != processResult.getProcessingMessagesList().size()) {
            printResponse(processResult);
        }
        assertEquals(assertMessage, expectedCount, processResult.getProcessingMessagesList().size());
    }

    public static void printResponse(ProcessingResponse processingResponse) {
        System.out.println("ProcessingResponse = ");
        for (QBProcessingMessage message : processingResponse.getProcessingMessagesList()) {
            System.out.println(message.getCode() + ":" + (message.getLevel() != null ? message.getLevel().name() + ":" : "") + message.getMessage());
        }
    }

    public static void assertCount(QBProcessingMessageLevel level, int expectedCount, ProcessingResponse processingResponse) {
        int foundCount = 0;
        for (QBProcessingMessage message : processingResponse.getProcessingMessagesList()) {
            if (message.getLevel() == level) {
                foundCount++;
            }
        }

        if (foundCount != expectedCount) {
            printResponse(processingResponse);
        }

        assertEquals(level.name() + " message count", expectedCount, foundCount);
    }

    public static void assertContains(QBProcessingMessageLevel messageLevel, int messageCode, ProcessingResponse processResult) {
        assertContains(null, messageLevel, messageCode, processResult);
    }

    public static void assertContains(String assertMessage, QBProcessingMessageLevel messageLevel, int messageCode, ProcessingResponse processResult) {
        boolean found = false;

        for (QBProcessingMessage message : processResult.getProcessingMessagesList()) {
            found =  (message.getLevel() == messageLevel && message.getCode() == messageCode);
            if (found) break;
        }

        if (!found) {
            for (QBProcessingMessage message : processResult.getProcessingMessagesList()) {
                System.out.println(message);
            }

            String msg = "expected to find processing message - code: " + messageCode + "   level:" + messageLevel.name();
            if (assertMessage != null) {
                msg = assertMessage + " - " + msg;
            }
            fail(msg);
        }
    }

    public static void assertPayrollItems(Company pCompany, QBPayrollItems pQbPayrollItems){
        List<QBPayrollItem> payrollItems = pQbPayrollItems.getPayrollItem();
        for (QBPayrollItem payrollItem : payrollItems) {
            if(payrollItem.getPayrollItemCategory().equals(QBPayrollItemCategory.TAX_ITEM)){
                CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(pCompany, payrollItem.getPspPayrollItemId());
                assertNotNull("Company Law not found", companyLaw);
            } else {
                CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(pCompany, payrollItem.getPspPayrollItemId());
                assertNotNull("Company Payroll item not found", companyPayrollItem);
                if(payrollItem.getPayrollItemCategory() == QBPayrollItemCategory.PRE_TAX_ITEM &&
                        payrollItem.getTaxTrackingTypeId().equals(QBDTWSSubmitPayrollRequestProcess.TAX_TRACKING_TYPE_401K) &&
                        payrollItem.getAgencyNumber().equalsIgnoreCase("401k")){
                    assertEquals("Payroll Item is not categorized.", PayrollItemCode.Tp401kEmployeeDeferral, companyPayrollItem.getPayrollItem().getPayrollItemCode());
                }
            }
        }
    }

    public static void assertPaychecks(SubmitPayrollRequest pRequest){
        try{
            PayrollServices.beginUnitOfWork();
            List<QBPaycheck> qbPaychecks = pRequest.getPaycheckList().getPaycheck();
            UpdateCompanyRequest companyRequest = pRequest.getUpdateCompanyRequest();
            Company company = null;
            if(companyRequest != null){
                QBCompany qbCompany = companyRequest.getCompany();
                if(qbCompany != null){
                    company = Company.findCompany(qbCompany.getSourceCompanyId(), SourceSystemCode.QBDT);
                }
            }
            if(company == null){
                fail("Company is expected, but not present in request.");
            }
            assertPayrollItems(company, pRequest.getPayrollItemList());
            for (QBPaycheck qbPaycheck : qbPaychecks) {
                Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getOfxPaycheckID());
                if(qbPaycheck.getOperation().equals(QBPaycheckOperationEnum.ADD)){
                    assertNotNull("Paycheck with OFX paycheck id " + qbPaycheck.getOfxPaycheckID() + "does not exist", paycheck);
                    Assert.assertEquals(qbPaycheck.getEmployeeID(), paycheck.getSourceEmployee().getSourceEmployeeId());
                    Assert.assertEquals(qbPaycheck.getPeriodStartDate(), QBDTWSRequestCreator.createQBDate(paycheck.getPayPeriodBeginDate()));
                    Assert.assertEquals(qbPaycheck.getPeriodEndDate(), QBDTWSRequestCreator.createQBDate(paycheck.getPayPeriodEndDate()));
                    Assert.assertEquals(qbPaycheck.getPayDate(), QBDTWSRequestCreator.createQBDate(paycheck.getPayrollRun().getPaycheckDate()));
                    Assert.assertEquals(qbPaycheck.getNetPay(), SpcfUtils.convertToBigDecimal((SpcfMoney)paycheck.getNetAmount()));
                    DomainEntitySet<Tax> taxes = paycheck.getTaxCollection();
                    for (QBPaycheckLineTaxItem qbPaycheckLineTaxItem : qbPaycheck.getTaxItems()) {
                        SpcfMoney taxAmount = (SpcfMoney)SpcfUtils.convertToSpcfMoney(qbPaycheckLineTaxItem.getCurrent()).negate();
                        SpcfMoney ytdTaxAmount = (SpcfMoney)SpcfUtils.convertToSpcfMoney(qbPaycheckLineTaxItem.getYTD()).negate();
                        CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(company, qbPaycheckLineTaxItem.getPayrollItemId());
                        DomainEntitySet<Tax> taxItems = taxes.find(Tax.CompanyLaw().equalTo(companyLaw).And(Tax.Law().equalTo(companyLaw.getLaw())).And(Tax.TaxLiabilityAmount().equalTo(taxAmount)));
                        Assert.assertEquals("Tax Item", 1, taxItems.size());
                        Assert.assertEquals("Paycheck Line item YTD amount", ytdTaxAmount, taxItems.get(0).getTaxLiabilityYTDAmount());
                        Assert.assertEquals("Paycheck Line item Wage Base", SpcfUtils.convertToSpcfMoney(qbPaycheckLineTaxItem.getWageBase()), taxItems.get(0).getTaxableWagesAmount());
                        Assert.assertEquals("Paycheck Line item Wage Base", SpcfUtils.convertToSpcfMoney(qbPaycheckLineTaxItem.getIncomeSubjectToTax()), taxItems.get(0).getTotalWagesAmount());
                    }

                    DomainEntitySet<Deduction> paycheckDeductions = paycheck.getDeductionCollection();
                    for (QBPaycheckLineItem qbPaycheckLineItem : qbPaycheck.getPreTaxItems()) {
                        CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(company, qbPaycheckLineItem.getPayrollItemId());
                        assertNotNull("Company Payroll item not found", companyPayrollItem);
                        DomainEntitySet<Deduction> deductions = paycheckDeductions.find(Deduction.CompanyPayrollItem().in(companyPayrollItem));
                        Assert.assertEquals("Deduction", 1, deductions.size());
                        Assert.assertEquals("Deduction YTD amount", SpcfUtils.convertToSpcfMoney(qbPaycheckLineItem.getYTD()), deductions.get(0).getDeductionYTDAmount().negate());
                        Assert.assertEquals("Deduction amount", SpcfUtils.convertToSpcfMoney(qbPaycheckLineItem.getCurrent()), deductions.get(0).getDeductionAmount().negate());
                    }

                    DomainEntitySet<Compensation> paycheckCompensations = paycheck.getCompensationCollection();
                    for (QBEarningItem qbEarningItem : qbPaycheck.getEarningItems()) {
                        CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(company, qbEarningItem.getPayrollItemId());
                        assertNotNull("Company Payroll item not found", companyPayrollItem);
                        DomainEntitySet<Compensation> compensations = paycheckCompensations.find(Compensation.CompanyPayrollItem().in(companyPayrollItem));
                        Assert.assertEquals("Compensation", 1, compensations.size());
                        Assert.assertEquals("Compensation YTD amount", SpcfUtils.convertToSpcfMoney(qbEarningItem.getYTD()), compensations.get(0).getCompensationYTDAmount());
                        Assert.assertEquals("Compensation amount", SpcfUtils.convertToSpcfMoney(qbEarningItem.getCurrent()), compensations.get(0).getCompensationAmount());
                        Assert.assertEquals("Compensation Hours worked", qbEarningItem.getQty(), compensations.get(0).getHoursWorked());
                    }
                    for (QBPaycheckLineTaxCompanyItem qbPaycheckLineTaxCompanyItem : qbPaycheck.getTaxCompanyItems()) {
                        CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(company, qbPaycheckLineTaxCompanyItem.getPayrollItemId());
                        assertNotNull("Company Payroll item not found", companyPayrollItem);
                    }
                } else if(qbPaycheck.getOperation().equals(QBPaycheckOperationEnum.VOID)){
                    assertEquals("Paycheck Status", PaycheckStatusCode.Inactive, paycheck.getStatus());
                    int eventCount = 0;
                    if(paycheck.hasBeenOffloadedTOTOK()){
                        eventCount = 1;
                        assertEquals("Payroll Run Status", PayrollStatus.OffloadedAll,  paycheck.getPayrollRun().getPayrollRunStatus());
                    } else {
                        assertEquals("Payroll Run Status", PayrollStatus.Complete,  paycheck.getPayrollRun().getPayrollRunStatus());
                    }
                    assertEquals("PaycheckInvalidReason company event detail", eventCount, CompanyEventDetail.findCompanyEventDetails(company).find(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.PaycheckInvalidReason)).size());
                    assertEquals("VoidedPaycheckAlreadyOffloadedToTOK company event", eventCount, CompanyEvent.findCompanyEvents(company, EventTypeCode.VoidedPaycheckAlreadyOffloadedToTOK).size());
                }
            }
            PayrollServices.rollbackUnitOfWork();
        }finally{
            PayrollServices.rollbackUnitOfWork();
        }
    }

}
