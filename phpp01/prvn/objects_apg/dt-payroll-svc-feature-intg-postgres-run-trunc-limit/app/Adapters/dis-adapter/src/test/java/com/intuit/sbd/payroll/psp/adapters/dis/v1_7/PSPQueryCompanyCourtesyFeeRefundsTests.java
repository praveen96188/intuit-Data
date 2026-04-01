package com.intuit.sbd.payroll.psp.adapters.dis.v1_7;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.LedgerTransactionDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.QueryCompanyCourtesyFeeRefundsRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.QueryCompanyCourtesyFeeRefundsResponseDISDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/test/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/PSPQueryCompanyEventsTests.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
public class PSPQueryCompanyCourtesyFeeRefundsTests {
    private String source_company_id = null;
    private Company company1 = null;
    private String courtesyFeeRefund1AmountStr = "5.00";
    private Double courtesyFeeRefund1Amount = new Double(courtesyFeeRefund1AmountStr);
    private SettlementTypeDTO courtesyFeeRefund1SettlementTypeDTO = SettlementTypeDTO.ACH;
    private String courtesyFeeRefund1Note = "NoteText - Testing";
    String courtesyFeeRefund2AmountStr = "11.00";
    Double courtesyFeeRefund2Amount = new Double(courtesyFeeRefund2AmountStr);
    SettlementTypeDTO courtesyFeeRefund2SettlementTypeDTO = SettlementTypeDTO.Wire;
    String courtesyFeeRefund2Note = "NoteText - Testing2";

    @Before
    public void loadDataHappyPath() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();

        PSPDate.setPSPTime("20070822000000");
        PayrollServices.commitUnitOfWork();

        company1 = DISCompanyDataloader.setupCompany();
//        company1 = DataLoadServices.newCompany(SourceSystemCode.QBDT, "12312332", false, ServiceCode.Tax);
        source_company_id = company1.getSourceCompanyId();

        DISCompanyDataloader.addCourtesyFeeRefund(company1, courtesyFeeRefund1AmountStr, courtesyFeeRefund1Note, courtesyFeeRefund1SettlementTypeDTO);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        DISCompanyDataloader.addCourtesyFeeRefund(company1, courtesyFeeRefund2AmountStr, courtesyFeeRefund2Note, courtesyFeeRefund2SettlementTypeDTO);
    }

    @Test
    public void testQueryCompanyCourtesyRefunds() {
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyCourtesyFeeRefundsRequestDISDTO request = new QueryCompanyCourtesyFeeRefundsRequestDISDTO();
            request.setSourceCompanyId(company1.getSourceCompanyId());
            request.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(company1.getSourceSystemCd()));

            QueryCompanyCourtesyFeeRefundsResponseDISDTO response = disAdapter.Query_CompanyCourtesyFeeRefunds(request);
            TestHelper.verifySuccess(response.getDisResponse());
            Assert.assertEquals(2, response.getLedgerTransactions().size());
            LedgerTransactionDISDTO tx1 = response.getLedgerTransactions().get(0);
            LedgerTransactionDISDTO tx2 = response.getLedgerTransactions().get(1);
            if (tx1.getTransactionType().equals(SettlementTypeDTO.ACH.toString())) {
                TestCase.assertEquals(courtesyFeeRefund1SettlementTypeDTO.toString(), tx1.getSettlementType());
                TestCase.assertEquals(courtesyFeeRefund2SettlementTypeDTO.toString(), tx2.getSettlementType());
                TestCase.assertEquals(courtesyFeeRefund1Amount, tx1.getAmount());
                TestCase.assertEquals(courtesyFeeRefund2Amount, tx2.getAmount());
            } else {
                TestCase.assertEquals(courtesyFeeRefund1SettlementTypeDTO.toString(), tx2.getSettlementType());
                TestCase.assertEquals(courtesyFeeRefund2SettlementTypeDTO.toString(), tx1.getSettlementType());
                TestCase.assertEquals(courtesyFeeRefund1Amount, tx2.getAmount());
                TestCase.assertEquals(courtesyFeeRefund2Amount, tx1.getAmount());
            }

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testQueryCompanyCourtesyRefundsSettlementDateSpecified() {
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyCourtesyFeeRefundsRequestDISDTO request = new QueryCompanyCourtesyFeeRefundsRequestDISDTO();
            request.setSourceCompanyId(company1.getSourceCompanyId());
            request.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(company1.getSourceSystemCd()));
            Calendar fromDate = Calendar.getInstance();
            fromDate.set(Calendar.YEAR, 2011);
            fromDate.set(Calendar.MONTH, 11);
            request.setFromDate(fromDate);

            QueryCompanyCourtesyFeeRefundsResponseDISDTO response = disAdapter.Query_CompanyCourtesyFeeRefunds(request);
            TestHelper.verifySuccess(response.getDisResponse());

            Assert.assertEquals(1, response.getLedgerTransactions().size());
            LedgerTransactionDISDTO tx = response.getLedgerTransactions().get(0);
            TestCase.assertEquals(courtesyFeeRefund2Amount, tx.getAmount());
            TestCase.assertEquals(courtesyFeeRefund2SettlementTypeDTO.toString(), tx.getSettlementType());

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testNoCompanyFound() {
        try {
            String sourceCoIdDNE = "companyDNE";
            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyCourtesyFeeRefundsRequestDISDTO request = new QueryCompanyCourtesyFeeRefundsRequestDISDTO();
            request.setSourceCompanyId(sourceCoIdDNE);
            request.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(company1.getSourceSystemCd()));

            QueryCompanyCourtesyFeeRefundsResponseDISDTO response = disAdapter.Query_CompanyCourtesyFeeRefunds(request);
            TestHelper.verifyFailure(response.getDisResponse());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

}