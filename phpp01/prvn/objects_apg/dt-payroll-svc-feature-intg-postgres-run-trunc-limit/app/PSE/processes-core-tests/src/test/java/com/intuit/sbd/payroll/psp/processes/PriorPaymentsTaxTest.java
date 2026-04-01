package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PriorPaymentDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PriorPaymentSubmissionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.QBDTTransactionInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.TaxPaymentDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyLaw;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.domain.ManualPaymentStatus;
import com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction;
import com.intuit.sbd.payroll.psp.domain.PaymentMethod;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.PriorPaymentSubmission;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * User: mwaqarbaig
 * Date: Nov 1, 2010
 * Time: 3:03:51 PM
 */

public class PriorPaymentsTaxTest {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    private List<PriorPaymentSubmissionDTO> getPopulatedPriorPaymentDTOList(Company company, String paymentId) {
        String sourceId1 = "1";
        String sourceId2 = "2";
        CompanyLaw companyLaw1 = DataLoadServices.newCompanyLaw(company, sourceId1, "63");/*  sourceId here is the payroll Item ID*/
        CompanyLaw companyLaw2 = DataLoadServices.newCompanyLaw(company, sourceId2, "64");

        List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = new ArrayList<PriorPaymentSubmissionDTO>();
        PriorPaymentSubmissionDTO priorPaymentSubmissionDTO = new PriorPaymentSubmissionDTO();
        priorPaymentSubmissionDTO.setSourceId("1");
        priorPaymentSubmissionDTO.setQBDTTransactionInfoDTO(new QBDTTransactionInfoDTO());

        priorPaymentSubmissionDTO.setPayments(new HashMap<String, PriorPaymentDTO>());
        PriorPaymentDTO priorPaymentDTO = new PriorPaymentDTO();
        priorPaymentDTO.setSourceId(paymentId);   /*  sourceId here is the payment Id */
        QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
        qbdtTransactionInfoDTO.setAgencyName("Agency 1");
        priorPaymentDTO.setPaymentTemplateCd("IRS-941-PAYMENT");
        priorPaymentDTO.setIsRefund(false);
        priorPaymentDTO.setIsVoid(false);
        priorPaymentDTO.setPaymentDate(new DateDTO("2009-11-01"));
        priorPaymentDTO.setPeriodEndDate(new DateDTO("2010-12-31"));
        priorPaymentDTO.setTotalAmount(new SpcfMoney("11050.0"));

        TaxPaymentDTO taxPaymentDTO1 = new TaxPaymentDTO();
        taxPaymentDTO1.setLawId(companyLaw1.getLaw().getLawId());
        taxPaymentDTO1.setPayrollItemId(sourceId1);
        taxPaymentDTO1.setQBDTTransactionInfoDTO(new QBDTTransactionInfoDTO());
        taxPaymentDTO1.setDate(new DateDTO("2010-11-01"));
        taxPaymentDTO1.setAmount(new SpcfMoney("1250.0"));

        TaxPaymentDTO taxPaymentDTO2 = new TaxPaymentDTO();
        taxPaymentDTO2.setLawId(companyLaw2.getLaw().getLawId());
        taxPaymentDTO2.setPayrollItemId(sourceId2);
        taxPaymentDTO2.setQBDTTransactionInfoDTO(new QBDTTransactionInfoDTO());
        taxPaymentDTO2.setDate(new DateDTO("2009-11-01"));
        taxPaymentDTO2.setAmount(new SpcfMoney("9800.0"));

        List<TaxPaymentDTO> taxPaymentDTOs = new ArrayList<TaxPaymentDTO>();
        taxPaymentDTOs.add(taxPaymentDTO1);
        taxPaymentDTOs.add(taxPaymentDTO2);
        priorPaymentDTO.setTaxes(taxPaymentDTOs);

        priorPaymentSubmissionDTO.getPayments().put("IRS-941-PAYMENT", priorPaymentDTO);
        priorPaymentSubmissionDTOs.add(priorPaymentSubmissionDTO);

        return priorPaymentSubmissionDTOs;
    }

    @Test
    public void testChangeQBDTInfoOnPriorPaymentHappyPath() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = getPopulatedPriorPaymentDTOList(company, "1");
        ProcessResult processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Prior payment not added", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        QBDTTransactionInfoDTO qbdtTransactionInfoDTOUpd = new QBDTTransactionInfoDTO();
        qbdtTransactionInfoDTOUpd.setAgencyName("Agency 2");
        priorPaymentSubmissionDTOs.get(0).setQBDTTransactionInfoDTO(qbdtTransactionInfoDTOUpd);
        processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Prior payment not added", processResult);
        PayrollServices.commitUnitOfWork();

        /*  See if the QBDT Info persisted  */
        PayrollServices.beginUnitOfWork();
        PriorPaymentSubmission paymentSubmission = assertOne(Application.<PriorPaymentSubmission>find(PriorPaymentSubmission.class, PriorPaymentSubmission.SourceId().equalTo("1")));
        MoneyMovementTransaction mmt = paymentSubmission.getQbdtTransactionInfoCollection().get(0).getMoneyMovementTransaction();
        /*MoneyMovementTransaction mmt = assertOne(Application.<MoneyMovementTransaction>find(MoneyMovementTransaction.class, MoneyMovementTransaction.AgencyTaxpayerId().equalTo("1")));*/
        assertEquals("QBDT Info change not persisted", priorPaymentSubmissionDTOs.get(0).getQBDTTransactionInfoDTO().getAgencyName(), mmt.getQbdtTransactionInfo().getAgencyName());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testVoidPriorPaymentHappyPath() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = getPopulatedPriorPaymentDTOList(company, "1");
        ProcessResult processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Prior payment not added", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").setIsVoid(true);
        processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Prior payment not added", processResult);
        PayrollServices.commitUnitOfWork();
        /* See if the payment was voided and individual tax lines were voided as well   */
        PayrollServices.beginUnitOfWork();
        PriorPaymentSubmission paymentSubmission = assertOne(Application.<PriorPaymentSubmission>find(PriorPaymentSubmission.class, PriorPaymentSubmission.SourceId().equalTo("1")));
        MoneyMovementTransaction mmt = paymentSubmission.getQbdtTransactionInfoCollection().get(0).getMoneyMovementTransaction();
        DomainEntitySet<FinancialTransaction> financialTransactions = mmt.getFinancialTransactionCollection();
        assertEquals("Not all tax lines were saved", 2, financialTransactions.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testCreateNewPriorPaymentHappyPath() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = getPopulatedPriorPaymentDTOList(company, "1");
        ProcessResult processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Prior payment not added", processResult);
        PayrollServices.commitUnitOfWork();
        /*  See if the data persisted  */
        PayrollServices.beginUnitOfWork();
        PriorPaymentSubmission paymentSubmission = assertOne(Application.<PriorPaymentSubmission>find(PriorPaymentSubmission.class, PriorPaymentSubmission.SourceId().equalTo("1")));
        MoneyMovementTransaction mmt = paymentSubmission.getQbdtTransactionInfoCollection().get(0).getMoneyMovementTransaction();
        /*MoneyMovementTransaction mmt = assertOne(Application.<MoneyMovementTransaction>find(MoneyMovementTransaction.class, MoneyMovementTransaction.AgencyTaxpayerId().equalTo("1")));*/
        assertEquals("Transaction amount not persisted", priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").getTotalAmount(), mmt.getMoneyMovementTransactionAmount());
        assertEquals("Transaction Date did not persist.", priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").getPaymentDate().toSpcfCalendar(), mmt.getInitiationDate().toLocal());
        assertEquals("Transaction End Date did not persist.", priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").getPeriodEndDate().toSpcfCalendar(), mmt.getPaymentPeriodEnd().toLocal());
        assertEquals("Transaction Status did not persist.", priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").isIsVoid(), ManualPaymentStatus.Voided == mmt.getManualPaymentStatus());
        DomainEntitySet<FinancialTransaction> financialTransactions = mmt.getFinancialTransactionCollection();
        assertEquals("Not all tax lines were saved", 2, financialTransactions.size());
        DomainEntitySet<FinancialTransaction> sortedFinancialTransactions = financialTransactions.sort(FinancialTransaction.FinancialTransactionAmount());
        ArrayList<TaxPaymentDTO> taxPaymentDTOs = (ArrayList<TaxPaymentDTO>) priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").getTaxes();
        Collections.sort(taxPaymentDTOs, new TaxPaymentDTOSorter());
        int i = 0;
        for (FinancialTransaction financialTransaction : sortedFinancialTransactions) {
            assertEquals("Tax line Amount did not persist.", taxPaymentDTOs.get(i).getAmount(), financialTransaction.getFinancialTransactionAmount());
            assertEquals("Tax line Law did not persist.", taxPaymentDTOs.get(i).getLawId(), financialTransaction.getLaw().getLawId());
            i++;
        }
        PayrollServices.rollbackUnitOfWork();
    }

    private class TaxPaymentDTOSorter implements Comparator<TaxPaymentDTO> {
        public int compare(TaxPaymentDTO o1, TaxPaymentDTO o2) {
            return o1.getAmount().compareTo(o2.getAmount());
        }
    }

    @Test
    public void testTotalAmountIsZero() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = getPopulatedPriorPaymentDTOList(company, "1");
        /*  Observation: Gets error of individual sums not matching with the set total of 0.0. We should set individual amounts to zero as well if asserting for 5001    */
        TaxPaymentDTO taxPaymentDTO[]=new TaxPaymentDTO[2];
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").getTaxes().toArray(taxPaymentDTO)[0].setAmount(SpcfMoney.ZERO);
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").getTaxes().toArray(taxPaymentDTO)[1].setAmount(SpcfMoney.ZERO);
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").setTotalAmount(new SpcfMoney("0.0"));
        ProcessResult processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess(processResult);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testTaxLineAmountIsZero() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = getPopulatedPriorPaymentDTOList(company, "1");
        TaxPaymentDTO[] tpDTOs = new TaxPaymentDTO[2];
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").getTaxes().toArray(tpDTOs)[1].setAmount(new SpcfMoney("0.0"));
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").setTotalAmount(new SpcfMoney("1250"));
        ProcessResult processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Could not submit prior payments tax", processResult);
        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void testTotalDoesNotEqualSum() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = getPopulatedPriorPaymentDTOList(company, "1");
        TaxPaymentDTO[] tpDTOs = new TaxPaymentDTO[2];
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").getTaxes().toArray(tpDTOs)[1].setAmount(new SpcfMoney("100.0"));
        ProcessResult processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertTrue("", processResult.getMessages().size() > 0);
        assertEquals("Total payment does not equal sum amount for tax lines.", "10106", processResult.getMessages().get(0).getMessageCode());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testInvalidCompany() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), "1111", null);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Invalid Company ID", "169", errorMessage.getMessageCode());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testLawDoesNotExist() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        String sourceId1 = "1";
        List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = new ArrayList<PriorPaymentSubmissionDTO>();
        PriorPaymentSubmissionDTO priorPaymentSubmissionDTO = new PriorPaymentSubmissionDTO();
        priorPaymentSubmissionDTO.setSourceId("1");
        priorPaymentSubmissionDTO.setQBDTTransactionInfoDTO(new QBDTTransactionInfoDTO());

        priorPaymentSubmissionDTO.setPayments(new HashMap<String, PriorPaymentDTO>());
        PriorPaymentDTO priorPaymentDTO = new PriorPaymentDTO();
        priorPaymentDTO.setSourceId("1");   /*  sourceId here is the payment Id */
        priorPaymentDTO.setPaymentTemplateCd("IRS-941-PAYMENT");
        priorPaymentDTO.setIsRefund(false);
        priorPaymentDTO.setIsVoid(false);
        priorPaymentDTO.setPaymentDate(new DateDTO("2010-11-15"));
        priorPaymentDTO.setPeriodEndDate(new DateDTO("2010-12-31"));
        priorPaymentDTO.setTotalAmount(new SpcfMoney("12241.0"));

        TaxPaymentDTO taxPaymentDTO1 = new TaxPaymentDTO();
        taxPaymentDTO1.setLawId("63");
        taxPaymentDTO1.setPayrollItemId(sourceId1);
        taxPaymentDTO1.setQBDTTransactionInfoDTO(new QBDTTransactionInfoDTO());
        taxPaymentDTO1.setDate(new DateDTO("2010-11-01"));
        taxPaymentDTO1.setAmount(new SpcfMoney("2510.0"));

        List<TaxPaymentDTO> taxPaymentDTOs = new ArrayList<TaxPaymentDTO>();
        taxPaymentDTOs.add(taxPaymentDTO1);
        priorPaymentDTO.setTaxes(taxPaymentDTOs);

        priorPaymentSubmissionDTO.getPayments().put("IRS-941-PAYMENT", priorPaymentDTO);
        priorPaymentSubmissionDTOs.add(priorPaymentSubmissionDTO);

        ProcessResult processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Law does not exist", "1500", errorMessage.getMessageCode());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testLawsDontMatch() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        String sourceId1 = "1";
        String sourceId2 = "2";
        CompanyLaw companyLaw1 = DataLoadServices.newCompanyLaw(company, sourceId1, "63");  /*  sourceId here is the payroll Item ID*/
        DataLoadServices.newCompanyLaw(company, sourceId2, "64");
        List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = new ArrayList<PriorPaymentSubmissionDTO>();
        PriorPaymentSubmissionDTO priorPaymentSubmissionDTO = new PriorPaymentSubmissionDTO();
        priorPaymentSubmissionDTO.setSourceId("1");
        priorPaymentSubmissionDTO.setQBDTTransactionInfoDTO(new QBDTTransactionInfoDTO());

        priorPaymentSubmissionDTO.setPayments(new HashMap<String, PriorPaymentDTO>());

        PriorPaymentDTO priorPaymentDTO = new PriorPaymentDTO();
        priorPaymentDTO.setSourceId("1");   /*  sourceId here is the payment Id */
        priorPaymentDTO.setPaymentTemplateCd("IRS-941-PAYMENT");
        priorPaymentDTO.setIsRefund(false);
        priorPaymentDTO.setIsVoid(false);
        priorPaymentDTO.setPaymentDate(new DateDTO("2010-11-15"));
        priorPaymentDTO.setPeriodEndDate(new DateDTO("2010-12-31"));
        priorPaymentDTO.setTotalAmount(new SpcfMoney("12241.0"));

        TaxPaymentDTO taxPaymentDTO1 = new TaxPaymentDTO();
        taxPaymentDTO1.setLawId(companyLaw1.getLaw().getLawId());
        taxPaymentDTO1.setPayrollItemId(sourceId2);
        taxPaymentDTO1.setQBDTTransactionInfoDTO(new QBDTTransactionInfoDTO());
        taxPaymentDTO1.setDate(new DateDTO("2010-11-01"));
        taxPaymentDTO1.setAmount(new SpcfMoney("2510.0"));

        List<TaxPaymentDTO> taxPaymentDTOs = new ArrayList<TaxPaymentDTO>();
        taxPaymentDTOs.add(taxPaymentDTO1);
        priorPaymentDTO.setTaxes(taxPaymentDTOs);

        priorPaymentSubmissionDTO.getPayments().put("IRS-941-PAYMENT", priorPaymentDTO);
        priorPaymentSubmissionDTOs.add(priorPaymentSubmissionDTO);

        ProcessResult processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Law don't match", "10102", errorMessage.getMessageCode());    /*    10102 for mismatching laws  */
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAlterTaxLineAmount() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = getPopulatedPriorPaymentDTOList(company, "1");
        ProcessResult processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Prior payment not added", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        /*  Observation: Alteration in one of the tax amounts doesn't reflect in the total and hence a mismatch error is generated causing the process to fail   */
        TaxPaymentDTO[] tpDTOs = new TaxPaymentDTO[2];
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").getTaxes().toArray(tpDTOs)[0].setAmount(new SpcfMoney("35.0"));
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").setTotalAmount(new SpcfMoney("9835.0"));
        processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Prior payment not added after changing Tax line amount", processResult);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAlterTotalAmount() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = getPopulatedPriorPaymentDTOList(company, "1");
        ProcessResult processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Prior payment not added", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        /*  Observation: Alteration in one of the tax amounts doesn't reflect in the total and hence a mismatch error is generated causing the process to fail   */
        TaxPaymentDTO[] tpDTOs = new TaxPaymentDTO[2];
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").getTaxes().toArray(tpDTOs)[0].setAmount(new SpcfMoney("150.0"));
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").getTaxes().toArray(tpDTOs)[1].setAmount(new SpcfMoney("50.0"));
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").setTotalAmount(new SpcfMoney("200.0"));
        processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Prior payment not added after changing Total amount", processResult);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAlterPaymentDate() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = getPopulatedPriorPaymentDTOList(company, "1");
        ProcessResult processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Prior payment not added", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").setPaymentDate(new DateDTO("2009-10-01"));
        processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Prior payment not added after changing Payment Date", processResult);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAlterPeriodEndDate() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = getPopulatedPriorPaymentDTOList(company, "1");
        ProcessResult processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Prior payment not added", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").setPeriodEndDate(new DateDTO("2011-12-25"));
        processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Prior payment not added after changing Period End Date", processResult);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testUnvoiding() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = getPopulatedPriorPaymentDTOList(company, "1");
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").setIsVoid(true);
        ProcessResult processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Prior payment not added", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").setIsVoid(false);
        processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Prior payment not added after voiding", processResult);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAlterPaymentMethod() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = getPopulatedPriorPaymentDTOList(company, "1");
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").setIsRefund(true);
        ProcessResult processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Prior payment not added", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").setIsRefund(false);
        processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Prior payment not added for refund", processResult);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAlterTaxLineDates() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = getPopulatedPriorPaymentDTOList(company, "1");
        ProcessResult processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Prior payment not added", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        TaxPaymentDTO[] tpDTOs = new TaxPaymentDTO[2];
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").getTaxes().toArray(tpDTOs)[0].setDate(new DateDTO("2010-11-02"));
        processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Prior payment not added after changing Tax line settlement Date", processResult);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testRemoveTaxLines() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = getPopulatedPriorPaymentDTOList(company, "1");
        ProcessResult processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Prior payment not added", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        /*  Observation: Alteration in one of the tax amounts doesn't reflect in the total and hence a mismatch error is generated causing the process to fail  */
        TaxPaymentDTO[] tpDTOs = new TaxPaymentDTO[2];
        TaxPaymentDTO tpDTOToRemove = priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").getTaxes().toArray(tpDTOs)[0];
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").getTaxes().remove(tpDTOToRemove);
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").setTotalAmount(new SpcfMoney("9800"));    /*  removed 1250, should be 9800    */
        processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Failed to remove tax lines", processResult);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAddTaxLines() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = getPopulatedPriorPaymentDTOList(company, "1");
        ProcessResult processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Prior payment not added", processResult);
        String sourceId3 = "3";
        CompanyLaw companyLaw3 = DataLoadServices.newCompanyLaw(company, sourceId3, "64");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        TaxPaymentDTO taxPaymentDTO3 = new TaxPaymentDTO();
        taxPaymentDTO3.setLawId(companyLaw3.getLaw().getLawId());
        taxPaymentDTO3.setPayrollItemId(sourceId3);
        taxPaymentDTO3.setQBDTTransactionInfoDTO(new QBDTTransactionInfoDTO());
        taxPaymentDTO3.setDate(new DateDTO("2011-09-20"));
        taxPaymentDTO3.setAmount(new SpcfMoney("8000.0"));

        PriorPaymentDTO priorPaymentDTO = new PriorPaymentDTO();
        priorPaymentDTO.setSourceId("1");   /*  sourceId here is the payment Id */
        QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
        qbdtTransactionInfoDTO.setAgencyName("Agency 1");
        priorPaymentDTO.setPaymentTemplateCd("IRS-941-PAYMENT");
        priorPaymentDTO.setIsRefund(false);
        priorPaymentDTO.setIsVoid(false);
        priorPaymentDTO.setPaymentDate(new DateDTO("2010-11-15"));
        priorPaymentDTO.setPeriodEndDate(new DateDTO("2010-12-31"));
        priorPaymentDTO.setTotalAmount(new SpcfMoney("8000.0"));

        List<TaxPaymentDTO> taxPaymentDTOs = new ArrayList<TaxPaymentDTO>();
        taxPaymentDTOs.add(taxPaymentDTO3);
        priorPaymentDTO.setTaxes(taxPaymentDTOs);
        priorPaymentSubmissionDTOs.get(0).getPayments().put("IRS-941-PAYMENT",priorPaymentDTO);

        processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Tax lines could not be added.", processResult);
        PriorPaymentSubmission paymentSubmission = assertOne(Application.<PriorPaymentSubmission>find(PriorPaymentSubmission.class, PriorPaymentSubmission.SourceId().equalTo("1")));
        MoneyMovementTransaction mmt = paymentSubmission.getQbdtTransactionInfoCollection().get(0).getMoneyMovementTransaction();
        DomainEntitySet<FinancialTransaction> financialTransactions = mmt.getFinancialTransactionCollection();
        assertEquals("Addition of tax line is not allowed", 2, financialTransactions.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testAddTaxLinesWithIncorrectTemplate() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = getPopulatedPriorPaymentDTOList(company, "1");
        ProcessResult processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertSuccess("Prior payment not added", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        String sourceId3 = "3";
        CompanyLaw companyLaw3 = DataLoadServices.newCompanyLaw(company, sourceId3, "65");
        TaxPaymentDTO taxPaymentDTO3 = new TaxPaymentDTO();
        taxPaymentDTO3.setLawId(companyLaw3.getLaw().getLawId());
        taxPaymentDTO3.setPayrollItemId(sourceId3);
        taxPaymentDTO3.setQBDTTransactionInfoDTO(new QBDTTransactionInfoDTO());
        taxPaymentDTO3.setDate(new DateDTO("2010-09-20"));
        taxPaymentDTO3.setAmount(new SpcfMoney("8000.0"));
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").getTaxes().add(taxPaymentDTO3);
        processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs);
        assertTrue("Tax lines can not be added or deleted.", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Tax lines with mismatching payment templates can not be added or deleted.", "20107", errorMessage.getMessageCode());
        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * TT: PSRV002416
     * After BALF Transmission, payment or refunds should not impact the ledger
     *
     * @throws Throwable
     */
    @Test
    public void test_HPDE_BALF_PaymentsToNotSucceed() throws Exception {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 25, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        PayrollServices.beginUnitOfWork();
        List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = getPopulatedPriorPaymentDTOList(company, "1");
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").setIsRefund(false);
        ProcessResult processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs, true);
        assertSuccess("Prior payment not added", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Assert.assertEquals("Number of PayrollRuns", 2, PayrollRun.findPayrollRuns(company).size());
        DomainEntitySet<FinancialTransaction> agencyHPDETaxPaymentFinancialTransactions = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyHPDETaxPayment);
        Assert.assertEquals("Number of Agency HPDE Tax Payment Financial Transactions", 2, agencyHPDETaxPaymentFinancialTransactions.size());
        DomainEntitySet<MoneyMovementTransaction> hpdeMMTs = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.HPDE));
        Assert.assertEquals("Number of HPDE MMTs", 1, hpdeMMTs.size());
        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * TT: PSRV002416
     * After BALF Transmission, payment or refunds should not impact the ledger
     *
     * @throws Throwable
     */
    @Test
    public void test_Post_BALF_PaymentsToNotSucceed() throws Exception {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 25, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        PayrollServices.beginUnitOfWork();
        List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = getPopulatedPriorPaymentDTOList(company, "1");
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").setIsRefund(false);
        ProcessResult processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs, false);
        assertSuccess("Prior payment not added", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Assert.assertEquals("Number of PayrollRuns", 2, PayrollRun.findPayrollRuns(company).size());
        DomainEntitySet<FinancialTransaction> agencyPostBALFHPDETaxPaymentFinancialTransactions = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyPostBALFHPDETaxPayment);
        Assert.assertEquals("Number of Agency Post BALF HPDE Tax Payment Financial Transactions", 2, agencyPostBALFHPDETaxPaymentFinancialTransactions.size());
        DomainEntitySet<MoneyMovementTransaction> hpdeMMTs = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.PostBalfHPDE));
        Assert.assertEquals("Number of HPDE MMTs", 1, hpdeMMTs.size());
        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * TT: PSRV002416
     * After BALF Transmission, payment or refunds should not impact the ledger
     *
     * @throws Throwable
     */
    @Test
    public void test_HPDE_BALF_RefundToNotSucceed() throws Exception {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 25, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        PayrollServices.beginUnitOfWork();
        List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = getPopulatedPriorPaymentDTOList(company, "1");
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").setIsRefund(true);
        ProcessResult processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs, true);
        assertSuccess("Prior payment not added", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Assert.assertEquals("Number of PayrollRuns", 2, PayrollRun.findPayrollRuns(company).size());
        DomainEntitySet<FinancialTransaction> agencyHPDETaxRefundFinancialTransactions = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyHPDETaxRefund);
        Assert.assertEquals("Number of Agency HPDE Tax Refund Financial Transactions", 2, agencyHPDETaxRefundFinancialTransactions.size());
        DomainEntitySet<MoneyMovementTransaction> hpdeMMTs = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.HPDERefund));
        Assert.assertEquals("Number of HPDE Refund MMTs", 1, hpdeMMTs.size());
        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * TT: PSRV002416
     * After BALF Transmission, payment or refunds should not impact the ledger
     *
     * @throws Throwable
     */
    @Test
    public void test_HPDE_Post_BALF_RefundToNotSucceed() throws Exception {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 25, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        PayrollServices.beginUnitOfWork();
        List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = getPopulatedPriorPaymentDTOList(company, "1");
        priorPaymentSubmissionDTOs.get(0).getPayments().get("IRS-941-PAYMENT").setIsRefund(true);
        ProcessResult processResult = PayrollServices.paymentManager.submitPriorPaymentsTax(company.getSourceSystemCd(), company.getSourceCompanyId(), priorPaymentSubmissionDTOs, false);
        assertSuccess("Prior payment not added", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Assert.assertEquals("Number of PayrollRuns", 2, PayrollRun.findPayrollRuns(company).size());
        DomainEntitySet<FinancialTransaction> agencyPostBALFHPDETaxRefundFinancialTransactions = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyPostBALFHPDETaxRefund);
        Assert.assertEquals("Number of Agency Post BALF HPDE Tax Refund Financial Transactions", 2, agencyPostBALFHPDETaxRefundFinancialTransactions.size());
        DomainEntitySet<MoneyMovementTransaction> hpdeMMTs = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.PostBalfHPDERefund));
        Assert.assertEquals("Number of HPDE Refund MMTs", 1, hpdeMMTs.size());
        PayrollServices.rollbackUnitOfWork();
    }
}
