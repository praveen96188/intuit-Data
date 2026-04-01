package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.ServiceChargePrices;
import com.intuit.sbd.payroll.psp.api.dtos.AddressDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ERFeeAddDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.TransactionCancelEEDTO;
import com.intuit.sbd.payroll.psp.api.dtos.factory.DTOFactory;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.utils.ACHOffloadRunner;
import com.intuit.sbd.payroll.psp.domain.BankAccount;
import com.intuit.sbd.payroll.psp.domain.BankAccountOwnerType;
import com.intuit.sbd.payroll.psp.domain.BillingDetail;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.CompanyEventDetail;
import com.intuit.sbd.payroll.psp.domain.CompanyOffer;
import com.intuit.sbd.payroll.psp.domain.CompanyOffering;
import com.intuit.sbd.payroll.psp.domain.CompanyService;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount;
import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.domain.FraudBankAccount;
import com.intuit.sbd.payroll.psp.domain.LimitRule;
import com.intuit.sbd.payroll.psp.domain.LimitValueType;
import com.intuit.sbd.payroll.psp.domain.Offer;
import com.intuit.sbd.payroll.psp.domain.OfferingServiceChargeType;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.domain.SourcePayrollParameter;
import com.intuit.sbd.payroll.psp.domain.SourcePayrollParameterCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TaxExemptStatusCode;
import com.intuit.sbd.payroll.psp.domain.TransactionStateCode;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company2Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.GenerateData;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import edu.emory.mathcs.backport.java.util.Collections;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: rsakhamuri
 * Date: Dec 9, 2008
 * Time: 9:40:29 AM
 */
public class NamedQueryTests {

    private DataLoader dataloader = new DataLoader();
    private Company1Dataloader company1DataLoader = new Company1Dataloader();
    private Company2Dataloader company2DataLoader = new Company2Dataloader();
    private Company3Dataloader company3DataLoader = new Company3Dataloader();
    private PayrollSubmitDataLoader payrollDataLoader = new PayrollSubmitDataLoader();
    private static SpcfLogger logger = SpcfLogManager.getLogger(NamedQueryTests.class);


    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /*
     *******************************************
     * CheckDDLimits related Named query tests
     *******************************************
     */

    /**
     * Test for query SumAmountGroupByPayCheckDate in FinancialTransaction.query.hbm.xml
     */
    @Test
    public void testSumAmountGroupByPayCheckDate() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        TreeMap<SpcfCalendar, SpcfMoney> amountMap = getAmountsPerPayCheckDate(company, SpcfCalendar.createInstance(2007, 10, 5, SpcfTimeZone.getLocalTimeZone()));
        assertEquals("Number of paycheck dates", 2, amountMap.size());
        assertEquals("Amount1", new SpcfMoney("180.00"), amountMap.get(SpcfCalendar.createInstance(2007, 10, 2, SpcfTimeZone.getLocalTimeZone())));
        assertEquals("Amount2", new SpcfMoney("250.00"), amountMap.get(SpcfCalendar.createInstance(2007, 10, 9, SpcfTimeZone.getLocalTimeZone())));
        PayrollServices.commitUnitOfWork();

        //Set Current Principal as agent
        PspPrincipal principal = Application.getCurrentPrincipal();
        PayrollServices.beginUnitOfWork();
        DataLoader.setPrincipalIsAgent();
        PayrollServices.commitUnitOfWork();

        // todo change state of one of the two ee txns to cancelled
        PayrollServices.beginUnitOfWork();
        company = Application.findById(Company.class, company.getId());
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        DomainEntitySet<FinancialTransaction> payroll1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        FinancialTransaction finTxn = payroll1FinTxns.get(0);
        for (FinancialTransaction finTx:payroll1FinTxns) {
            if (finTx.getFinancialTransactionAmount().equals(new SpcfMoney("150.00"))) {
                finTxn = finTx;
            }
        }

        List<String> sourcePaycheckIds = new Vector<String>(4);
        TransactionCancelEEDTO dto = new TransactionCancelEEDTO();
        dto.setSourcePayrollRunId("BatchTest05");
        sourcePaycheckIds.add(finTxn.getPaycheckSplit().getPaycheck().getSourcePaycheckId());
        dto.setSourcePaycheckIdList(sourcePaycheckIds);
        ProcessResult result = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, company.getSourceCompanyId(), dto);
        // Remove Agent from Principal
        PayrollServices.userManager.deleteUser("UnitTestAgent");
        PayrollServices.setCurrentPrincipal(principal);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        amountMap = getAmountsPerPayCheckDate(company, SpcfCalendar.createInstance(2007, 10, 5, SpcfTimeZone.getLocalTimeZone()));
        assertEquals("Number of paycheck dates", 2, amountMap.size());
        assertEquals("Amount1", new SpcfMoney("30.00"), amountMap.get(SpcfCalendar.createInstance(2007, 10, 2, SpcfTimeZone.getLocalTimeZone())));
        assertEquals("Amount2", new SpcfMoney("250.00"), amountMap.get(SpcfCalendar.createInstance(2007, 10, 9, SpcfTimeZone.getLocalTimeZone())));
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test for query SumAmountGroupByPayCheckDateAndEmployee in FinancialTransaction.query.hbm.xml
     */
    @Test
    public void testSumAmountGroupByPayCheckDateAndEmployee() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        HashMap<SpcfUniqueId, TreeMap<SpcfCalendar, SpcfMoney>>  amountMap = getAmountsPerEmployeePerPayCheckDate(company, SpcfCalendar.createInstance(2007, 10, 5, SpcfTimeZone.getLocalTimeZone()));
        assertEquals("Number of paycheck dates", 2, amountMap.size());
        Employee ee1 = Employee.findEmployee(company, "EE1");
        Employee ee2 = Employee.findEmployee(company, "EE2");
        assertEquals("Amount1 of EE1 ", new SpcfMoney("30.00"), amountMap.get(ee1.getId()).get(SpcfCalendar.createInstance(2007, 10, 2, SpcfTimeZone.getLocalTimeZone())));
        assertEquals("Amount2 of EE1", new SpcfMoney("100.00"), amountMap.get(ee1.getId()).get(SpcfCalendar.createInstance(2007, 10, 9, SpcfTimeZone.getLocalTimeZone())));
        assertEquals("Amount1 of EE2 ", new SpcfMoney("150.00"), amountMap.get(ee2.getId()).get(SpcfCalendar.createInstance(2007, 10, 2, SpcfTimeZone.getLocalTimeZone())));
        assertEquals("Amount2 of EE2", new SpcfMoney("150.00"), amountMap.get(ee2.getId()).get(SpcfCalendar.createInstance(2007, 10, 9, SpcfTimeZone.getLocalTimeZone())));
        PayrollServices.commitUnitOfWork();

        //Set Current Principal as agent
        PspPrincipal principal = Application.getCurrentPrincipal();
        PayrollServices.beginUnitOfWork();
        DataLoader.setPrincipalIsAgent();
        PayrollServices.commitUnitOfWork();

        // todo change state of one of the two ee txns to cancelled
        PayrollServices.beginUnitOfWork();
        company = Application.findById(Company.class, company.getId());
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest002");

        DomainEntitySet<FinancialTransaction> payroll1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        FinancialTransaction finTxn = payroll1FinTxns.get(0);
        for (FinancialTransaction finTx:payroll1FinTxns) {
            if (finTx.getFinancialTransactionAmount().equals(new SpcfMoney("100.00"))) {
                finTxn = finTx;
            }
        }

        List<String> sourcePaycheckIds = new Vector<String>(4);
        TransactionCancelEEDTO dto = new TransactionCancelEEDTO();
        dto.setSourcePayrollRunId("BatchTest002");
        sourcePaycheckIds.add(finTxn.getPaycheckSplit().getPaycheck().getSourcePaycheckId());
        dto.setSourcePaycheckIdList(sourcePaycheckIds);
        ProcessResult result = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, company.getSourceCompanyId(), dto);
        // Remove Agent from Principal
        PayrollServices.userManager.deleteUser("UnitTestAgent");
        PayrollServices.setCurrentPrincipal(principal);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        amountMap = getAmountsPerEmployeePerPayCheckDate(company, SpcfCalendar.createInstance(2007, 10, 5, SpcfTimeZone.getLocalTimeZone()));
        assertEquals("Number of paycheck dates", 2, amountMap.size());
        ee1 = Employee.findEmployee(company, "EE1");
        ee2 = Employee.findEmployee(company, "EE2");
        assertEquals("Size of EE1 Amount Map", 1, amountMap.get(ee1.getId()).size());
        assertEquals("Size of EE2 Amount Map", 2, amountMap.get(ee2.getId()).size());
        assertEquals("Amount1 of EE1 ", new SpcfMoney("30.00"), amountMap.get(ee1.getId()).get(SpcfCalendar.createInstance(2007, 10, 2, SpcfTimeZone.getLocalTimeZone())));
//        assertEquals("Amount2 of EE1", new SpcfMoney("100.00"), amountMap.get(ee1.getId()).get(SpcfCalendar.createInstance(2007, 10, 9, SpcfTimeZone.getLocalTimeZone())));
        assertEquals("Amount1 of EE2 ", new SpcfMoney("150.00"), amountMap.get(ee2.getId()).get(SpcfCalendar.createInstance(2007, 10, 2, SpcfTimeZone.getLocalTimeZone())));
        assertEquals("Amount2 of EE2", new SpcfMoney("150.00"), amountMap.get(ee2.getId()).get(SpcfCalendar.createInstance(2007, 10, 9, SpcfTimeZone.getLocalTimeZone())));
        PayrollServices.commitUnitOfWork();
    }

     /**
     * Test for query SumAmountGroupByPayCheckDateAndBankAccountENC in FinancialTransaction.query.hbm.xml
     */
    @Test
    public void testSumAmountGroupByPayCheckDateAndBankAccount() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        HashMap<String, TreeMap<SpcfCalendar, SpcfMoney>>  amountMap = getAmountsPerBankAccountPerPayCheckDate(company, SpcfCalendar.createInstance(2007, 10, 5, SpcfTimeZone.getLocalTimeZone()));
        assertEquals("Number of paycheck dates", 2, amountMap.size());
        assertEquals("Amount1 of EE1 ", new SpcfMoney("30.00"), amountMap.get("111000025:12345").get(SpcfCalendar.createInstance(2007, 10, 2, SpcfTimeZone.getLocalTimeZone())));
        assertEquals("Amount2 of EE1", new SpcfMoney("100.00"), amountMap.get("111000025:12345").get(SpcfCalendar.createInstance(2007, 10, 9, SpcfTimeZone.getLocalTimeZone())));
        assertEquals("Amount1 of EE2 ", new SpcfMoney("150.00"), amountMap.get("111000025:22345").get(SpcfCalendar.createInstance(2007, 10, 2, SpcfTimeZone.getLocalTimeZone())));
        assertEquals("Amount2 of EE2", new SpcfMoney("150.00"), amountMap.get("111000025:22345").get(SpcfCalendar.createInstance(2007, 10, 9, SpcfTimeZone.getLocalTimeZone())));
        PayrollServices.commitUnitOfWork();

    }

    /**
     * Test for query SumAmountGroupByPayCheckDateAndBankAccountENC in FinancialTransaction.query.hbm.xml
     * Multiple employees same bank account
     */
    @Test
    public void testSumAmountGroupByPayCheckDateAndBankAccount2() {
        // make two employees of company1 and one employee of company2 have the same bankaccount
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistCompany(company1DataLoader.getCompany1());

        dataloader.persistCompanyService(company1, company1DataLoader.getCompany1Service());

        CompanyBankAccount cba1 = dataloader.persistCompanyBankAccount(company1, dataloader.getTestCompanyBankAccount());

        company1DataLoader.persistEmployee(company1DataLoader.getEmployee1(company1));
        company1DataLoader.persistEmployee(company1DataLoader.getEmployee2(company1));
        company1 = Company.findCompany("1234567", SourceSystemCode.QBOE);

        Employee employee2 = Employee.findEmployee(company1, "EE2");
        EmployeeBankAccountDTO ebaDTO = company1DataLoader.getEmployee1BankAccount();

        ebaDTO.setEmployeeBankAccountId("EEBA2");

        EmployeeBankAccount eba2 = company1DataLoader.persistEEBA(company1, employee2, ebaDTO);

        Employee employee1 = Employee.findEmployee(company1, "EE1");

        EmployeeBankAccount eba1 = company1DataLoader.persistEEBA(company1, employee1, company1DataLoader.getEmployee1BankAccount());

        PSPDate.setPSPTime("20070905000000");
        company2DataLoader.persistCompany2();
        PSPDate.setPSPTime("20070906000000");
        Company company3 = company3DataLoader.persistCompany3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Company  company2 = Company.findCompany("2222222", SourceSystemCode.QBOE);
        Employee ee1c1 = Employee.findEmployee(company, "EE1");
        Employee ee1c2 = Employee.findEmployee(company2, "EE1_1");
        BankAccount ba1c1 = EmployeeBankAccount.findEmployeeBankAccount(ee1c1, "EEBA1").getBankAccount();
        EmployeeBankAccount eba1c2 = EmployeeBankAccount.findEmployeeBankAccount(ee1c2, "EEBA1");
        eba1c2.getBankAccount().setAccountNumber(ba1c1.getAccountNumber());
        eba1c2.getBankAccount().setAccountTypeCd(ba1c1.getAccountTypeCd());
        eba1c2.getBankAccount().setBankName(ba1c1.getBankName());
        eba1c2.getBankAccount().setRoutingNumber(ba1c1.getRoutingNumber());
        Application.save(eba1c2);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Collection<PayrollRunDTO> payrollDTOs = new ArrayList<PayrollRunDTO>();
        cba1 = Application.findById(CompanyBankAccount.class, cba1.getId());
        company1DataLoader.setBankAccount1(cba1);
        eba1 = Application.findById(EmployeeBankAccount.class, eba1.getId());
        eba2 = Application.findById(EmployeeBankAccount.class, eba2.getId());
        employee1 = Application.findById(Employee.class, employee1.getId());
        employee2 = Application.findById(Employee.class, employee2.getId());
        company1DataLoader.setEmployee1(employee1);
        company1DataLoader.setEmployee2(employee2);
        company1DataLoader.setEmployeeBankAccount1(eba1);
        company1DataLoader.setEmployee2BankAccount1(eba2);
        payrollDTOs.add(company1DataLoader.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02")));
        payrollDTOs.add(company1DataLoader.getCompany1PR2_DoesNotExceedLimits(new DateDTO("2007-10-09")));
        company1 = Application.findById(Company.class, company1.getId());
        submitPayroll(company1, payrollDTOs);
        payrollDTOs.clear();
        payrollDTOs.add(company2DataLoader.getCompany2PR_DoesNotExceedLimits(new DateDTO("2007-10-02")));
        payrollDTOs.add(company2DataLoader.getCompany2PR2_DoesNotExceedLimits(new DateDTO("2007-10-09")));
        company2 = Application.findById(Company.class, company2.getId());
        submitPayroll(company2, payrollDTOs);
        payrollDTOs.clear();
        payrollDTOs.add(company3DataLoader.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-10-02")));
        payrollDTOs.add(company3DataLoader.getCompany3PR2_DoesNotExceedLimits(new DateDTO("2007-10-09")));
        company3 = Application.findById(Company.class, company3.getId());
        submitPayroll(company3,payrollDTOs);
        payrollDTOs.clear();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        HashMap<String, TreeMap<SpcfCalendar, SpcfMoney>>  amountMap = getAmountsPerBankAccountPerPayCheckDate(company, SpcfCalendar.createInstance(2007, 10, 5, SpcfTimeZone.getLocalTimeZone()));
        assertEquals("Number of paycheck dates", 1, amountMap.size());
        assertEquals("Amount1 of EE1 & EE2 Bank Account", new SpcfMoney("180.00"), amountMap.get("111000025:12345").get(SpcfCalendar.createInstance(2007, 10, 2, SpcfTimeZone.getLocalTimeZone())));
        assertEquals("Amount2 of EE1 & EE2 Bank Account", new SpcfMoney("250.00"), amountMap.get("111000025:12345").get(SpcfCalendar.createInstance(2007, 10, 9, SpcfTimeZone.getLocalTimeZone())));
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test for query findPayrollRunByLimitIncreaseCriteria in PayrollRun.query.hbm.xml
     */
    @Test
    public void testFindPayrollRunByLimitIncreaseCriteria() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();
        ACHOffloadRunner.runAchOffload("20071002", 5);
        ACHOffloadRunner.runAchOffload("20071009", 5);

        // submit another four payrolls for company1
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20080404000000");
        Company company1 = Company
                .findCompany("1234567", SourceSystemCode.QBOE);
        Company company2 = Company
                .findCompany("2222222", SourceSystemCode.QBOE);
        Company company3 = Company
                .findCompany("8574536", SourceSystemCode.QBDT);
        CompanyBankAccount cba1 = CompanyBankAccount.findActiveCompanyBankAccount(company1);
        Collection<PayrollRunDTO> payrollDTOs = new ArrayList<PayrollRunDTO>();
        PayrollRunDTO payrollDTO = null;
        int numOfPayrolls = Integer.parseInt(LimitRule.findLimitRule(company1, ServiceCode.DirectDeposit)
                                                              .findLimitValueByName(LimitValueType.MinPayrollRunsForLimitAutoIncrease).getValue());
        for (int i = 0; i < 4; i++) {
            payrollDTO = payrollDataLoader.createPayrollRunDTO(company1, cba1, "Batch" + (i + 3));
            payrollDTOs.add(payrollDTO);
        }
        submitPayroll(company1, payrollDTOs);
        PayrollServices.commitUnitOfWork();
        ACHOffloadRunner.runAchOffload("20071012", 10);
        ACHOffloadRunner.runAchOffload("20080404", 5);
        ACHOffloadRunner.runAchOffload("20080409", 5);

        PayrollServices.beginUnitOfWork();
        String[] paramNames = new String[3];
        Object[] paramValues = new Object[3];
        paramNames[0] = "companyId";
        paramValues[0] = company1.getId().toString();

        paramNames[1] = "eventTypeCd";
        paramValues[1] = EventTypeCode.FirstPayrollReceived;

        SourcePayrollParameter minEarliestPayrollRunDate =
                SourcePayrollParameter.findSourcePayrollParameter(
                        company1.getSourceSystemCd(), SourcePayrollParameterCode.MinimumEarliestPayrollRunDays);
        Integer minDaysInPastForPayRunDate = -1 * Integer.valueOf(minEarliestPayrollRunDate.getParameterValue());

        paramNames[2] = "earliestPayrollRunDateMin";
        SpcfCalendar earliestPayrollRunDateMin = PSPDate.getPSPTime();
        earliestPayrollRunDateMin.addDays(minDaysInPastForPayRunDate);
        paramValues[2] = earliestPayrollRunDateMin;

        DomainEntitySet<PayrollRun> retList = Application
                .findByNamedQueryUsingCache(PayrollRun.class, "findPayrollRunByLimitIncreaseCriteria", paramNames, paramValues);

        assertEquals("Number of payrolls", 6, retList.size());

        paramValues[0] = company2.getId().toString();
        retList = Application
                .findByNamedQueryUsingCache(PayrollRun.class, "findPayrollRunByLimitIncreaseCriteria", paramNames, paramValues);

        assertEquals("Number of payrolls", 2, retList.size());

        paramValues[0] = company3.getId().toString();
        retList = Application
                .findByNamedQueryUsingCache(PayrollRun.class, "findPayrollRunByLimitIncreaseCriteria", paramNames, paramValues);

        assertEquals("Number of payrolls", 2, retList.size());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test for query findPayrollRunByLimitIncreaseCriteria in PayrollRun.query.hbm.xml
     *
     * There are six payrolls, but none is older than 3 months for company1
     * There are only five payrolls and first payroll is older than three months
     */
    @Test
    public void testFindPayrollRunByLimitIncreaseCriteria2() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();
        //ACHOffloadRunner.runAchOffload("20071002", 5);
        ACHOffloadRunner.runAchOffload("20071009", 10);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071010000000");
        PayrollServices.commitUnitOfWork();

        // submit another four payrolls for company1
        PayrollServices.beginUnitOfWork();
        Company company1 = Company
                .findCompany("1234567", SourceSystemCode.QBOE);
        CompanyBankAccount cba1 = CompanyBankAccount.findActiveCompanyBankAccount(company1);
        Collection<PayrollRunDTO> payrollDTOs = new ArrayList<PayrollRunDTO>();
        PayrollRunDTO payrollDTO = null;
        for (int i = 0; i < 4; i++) {
            payrollDTO = payrollDataLoader.createPayrollRunDTO(company1, cba1, "Batch" + (i + 3));
            payrollDTOs.add(payrollDTO);
        }
        submitPayroll(company1, payrollDTOs);
        PayrollServices.commitUnitOfWork();

        ACHOffloadRunner.runAchOffload("20071012", 1);

        PayrollServices.beginUnitOfWork();
        String[] paramNames = new String[3];
        Object[] paramValues = new Object[3];
        paramNames[0] = "companyId";
        paramValues[0] = company1.getId().toString();

        paramNames[1] = "eventTypeCd";
        paramValues[1] = EventTypeCode.FirstPayrollReceived;

        SourcePayrollParameter minEarliestPayrollRunDate =
                SourcePayrollParameter.findSourcePayrollParameter(
                        company1.getSourceSystemCd(), SourcePayrollParameterCode.MinimumEarliestPayrollRunDays);
        Integer minDaysInPastForPayRunDate = -1 * Integer.valueOf(minEarliestPayrollRunDate.getParameterValue());

        paramNames[2] = "earliestPayrollRunDateMin";
        SpcfCalendar earliestPayrollRunDateMin = PSPDate.getPSPTime();
        earliestPayrollRunDateMin.addDays(minDaysInPastForPayRunDate);
        paramValues[2] = earliestPayrollRunDateMin;

        DomainEntitySet<PayrollRun> retList = Application
                .findByNamedQueryUsingCache(PayrollRun.class, "findPayrollRunByLimitIncreaseCriteria", paramNames, paramValues);

        assertEquals("Number of payrolls", 0, retList.size());
        PayrollServices.commitUnitOfWork();
        //todo query for company2 which dont have a payroll older than 3 months
        // submit another three payrolls for company2
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        Company company2 = Company
                .findCompany("2222222", SourceSystemCode.QBOE);
        CompanyBankAccount cba2 = CompanyBankAccount.findActiveCompanyBankAccount(company2);
        payrollDTOs.clear();
        for (int i = 0; i < 3; i++) {
            payrollDTO = payrollDataLoader.createPayrollRunDTO(company2, cba2, "Batch" + (i + 3));
            payrollDTOs.add(payrollDTO);
        }
        submitPayroll(company2, payrollDTOs);
        PayrollServices.commitUnitOfWork();
        ACHOffloadRunner.runAchOffload(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        paramValues[0] = company1.getId().toString();

        earliestPayrollRunDateMin = PSPDate.getPSPTime();
        earliestPayrollRunDateMin.addDays(minDaysInPastForPayRunDate);
        paramValues[2] = SpcfCalendar.createInstance(2007, 9, 15, SpcfTimeZone.getLocalTimeZone());

        retList = Application
                .findByNamedQueryUsingCache(PayrollRun.class, "findPayrollRunByLimitIncreaseCriteria", paramNames, paramValues);

        assertEquals("Number of payrolls", 6, retList.size());

        paramValues[0] = company2.getId().toString();
        retList = Application
                .findByNamedQueryUsingCache(PayrollRun.class, "findPayrollRunByLimitIncreaseCriteria", paramNames, paramValues);

        assertEquals("Number of payrolls", 0, retList.size());

        paramValues[0] = company2.getId().toString();
        paramValues[2] = SpcfCalendar.createInstance(2007, 9, 16, SpcfTimeZone.getLocalTimeZone());
        retList = Application
                .findByNamedQueryUsingCache(PayrollRun.class, "findPayrollRunByLimitIncreaseCriteria", paramNames, paramValues);

        assertEquals("Number of payrolls", 5, retList.size());
        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void testFindCompanyEventByLimitIncreaseCriteria() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();

        // add a strike
        PayrollServices.beginUnitOfWork();
        Company company1 = Company
                .findCompany("1234567", SourceSystemCode.QBOE);
        Company company2 = Company
                .findCompany("2222222", SourceSystemCode.QBOE);
        Company company3 = Company
                .findCompany("8574536", SourceSystemCode.QBDT);
        ProcessResult<CompanyEvent> result = PayrollServices.companyManager.addStrikeEvent(company1.getSourceSystemCd(),
                company1.getSourceCompanyId(),
                "Strike reason", PSPDate.getPSPTime());
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        assertFalse(CompanyEvent.hasActiveStrikeEventWithinLastYear(company2));

        assertFalse(CompanyEvent.hasActiveStrikeEventWithinLastYear(company3));
        PayrollServices.rollbackUnitOfWork();

        // check after a year
        PayrollServices.beginUnitOfWork();
        SpcfCalendar date = PSPDate.getPSPTime();
        date.addMonths(13);
        PSPDate.setPSPTime(date);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertFalse(CompanyEvent.hasActiveStrikeEventWithinLastYear(company1));
    }

    /**
     * Strike cancelled
     */
    @Test
    public void testFindCompanyEventByLimitIncreaseCriteria2() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();

        // add a strike
        PayrollServices.beginUnitOfWork();
        Company company1 = Company
                .findCompany("1234567", SourceSystemCode.QBOE);
        ProcessResult<CompanyEvent> result = PayrollServices.companyManager.addStrikeEvent(company1.getSourceSystemCd(),
                company1.getSourceCompanyId(),
                "Strike reason", PSPDate.getPSPTime());
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        // cancel the active strike
        PayrollServices.beginUnitOfWork();
        company1 = Company
                .findCompany("1234567", SourceSystemCode.QBOE);
        result = PayrollServices.companyManager.cancelStrikeEvent(company1.getSourceSystemCd(),
                company1.getSourceCompanyId(), result.getResult().getId());
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertFalse(CompanyEvent.hasActiveStrikeEventWithinLastYear(company1));
        PayrollServices.rollbackUnitOfWork();
    }

    /*
     *******************************************
     * BillingDetails related Named query tests
     *******************************************
     */

    /**
     * Test for findOffloadedBillingDetails query in BillingDetails.query.hbm.xml
     */
    @Test
    public void testFindOffloadedBillingDetails() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company3 = Company.findCompany("8574536", SourceSystemCode.QBDT);
        ProcessResult<PayrollRun> result = PayrollServices.payrollManager.submitPayroll(company3.getSourceSystemCd(),
                company3.getSourceCompanyId(),
                company3DataLoader.get2ndCompany2PR_DoesNotExceedLimits());
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        SpcfCalendar currentDate = SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar vertexUpdateDate = SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.clearTime(currentDate);
        String[] paramNames = new String[3];

        paramNames[0] = "currentDate";
        paramNames[1] = "vertexUpdateDate";
        paramNames[2] = "txnStateCd";

        Object[] paramValues = new Object[3];
        paramValues[0] = currentDate;
        paramValues[1] = vertexUpdateDate;
        paramValues[2] = TransactionStateCode.Executed;
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<BillingDetail> billingDetails =
                Application.findByNamedQueryUsingCache(BillingDetail.class, "findOffloadedBillingDetails", paramNames, paramValues);
        // size zero because no transactions associated with the billing details were offloaded
        assertEquals("Number of Billing Details", 0, billingDetails.size());
        PayrollServices.commitUnitOfWork();

        // offload
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071004000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071005000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Date txDate = CalendarUtils.convertToDate(PSPDate.getPSPTime());

        ERFeeAddDTO feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBDT, "8574536", "BatchTest09",
                                                 SettlementTypeDTO.Cash, txDate, new SpcfMoney("100.00"),
                                                 OfferingServiceChargeType.ReversalFee, null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        assertSuccess(processResult);
        String c1FT1=processResult.getResult().getFirst().getId().toString();
        PayrollServices.commitUnitOfWork();

        currentDate = SpcfCalendar.createInstance(2007, 9, 27, SpcfTimeZone.getLocalTimeZone());
        paramValues[0] = currentDate;
        PayrollServices.beginUnitOfWork();
        billingDetails =
                Application.findByNamedQueryUsingCache(BillingDetail.class, "findOffloadedBillingDetails", paramNames, paramValues);
        // size zero because no money movement transactions associated with the billing details has inititation date
        // equal to current date
        assertEquals("Number of Billing Details", 0, billingDetails.size());
        PayrollServices.commitUnitOfWork();

        currentDate = SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone());
        vertexUpdateDate = SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone());
        paramValues[0] = currentDate;
        paramValues[1] = vertexUpdateDate;
        PayrollServices.beginUnitOfWork();
        billingDetails =
                Application.findByNamedQueryUsingCache(BillingDetail.class, "findOffloadedBillingDetails", paramNames, paramValues);
        // size zero because no billing detail's tax computed date is less than vertex update date
        assertEquals("Number of Billing Details", 0, billingDetails.size());
        PayrollServices.commitUnitOfWork();

        currentDate = SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone());
        vertexUpdateDate = SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone());
        paramValues[0] = currentDate;
        paramValues[1] = vertexUpdateDate;
        PayrollServices.beginUnitOfWork();
        billingDetails =
                Application.findByNamedQueryUsingCache(BillingDetail.class, "findOffloadedBillingDetails", paramNames, paramValues);

        assertEquals("Number of Billing Details", 1, billingDetails.size());
        BillingDetail billingDetail = billingDetails.get(0);
        assertEquals("Payroll Run ID","BatchTest87", billingDetail.getPayrollRun().getSourcePayRunId());
        assertEquals("Company Id", "8574536", billingDetail.getPayrollRun().getCompany().getSourceCompanyId());
        assertEquals("Billing Detail Amount", ServiceChargePrices.getNormalPerPayrollServiceChargeWithSalesTaxFY16(2), billingDetail.getItemTotal());
        assertEquals("Billing Detail Item Name", "Direct Deposit Fee", billingDetail.getItemName());
        assertEquals("Billing Detail SKU", "408181", billingDetail.getItemSku());
        assertEquals("Billing Detail Quantity", 2, billingDetail.getQuantity());
        PayrollServices.commitUnitOfWork();

        currentDate = SpcfCalendar.createInstance(2007, 10, 4, SpcfTimeZone.getLocalTimeZone());
        vertexUpdateDate = SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone());
        paramValues[0] = currentDate;
        paramValues[1] = vertexUpdateDate;
        PayrollServices.beginUnitOfWork();
        billingDetails =
                Application.findByNamedQueryUsingCache(BillingDetail.class, "findOffloadedBillingDetails", paramNames, paramValues);

        assertEquals("Number of Billing Details", 1, billingDetails.size());
        billingDetail = billingDetails.get(0);
        assertEquals("Payroll Run ID","BatchTest002", billingDetail.getPayrollRun().getSourcePayRunId());
        assertEquals("Company Id", "8574536", billingDetail.getPayrollRun().getCompany().getSourceCompanyId());
        assertEquals("Billing Detail Amount", ServiceChargePrices.getNormalPerPayrollServiceChargeWithSalesTaxFY16(2), billingDetail.getItemTotal());
        assertEquals("Billing Detail Item Name", "Direct Deposit Fee", billingDetail.getItemName());
        assertEquals("Billing Detail SKU", "408181", billingDetail.getItemSku());
        assertEquals("Billing Detail Quantity", 2, billingDetail.getQuantity());
        PayrollServices.commitUnitOfWork();

        currentDate = SpcfCalendar.createInstance(2007, 10, 5, SpcfTimeZone.getLocalTimeZone());
        vertexUpdateDate = SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone());
        paramValues[0] = currentDate;
        paramValues[1] = vertexUpdateDate;
        PayrollServices.beginUnitOfWork();
        billingDetails =
                Application.findByNamedQueryUsingCache(BillingDetail.class, "findOffloadedBillingDetails", paramNames, paramValues);

        assertEquals("Number of Billing Details", 1, billingDetails.size());
        billingDetail = billingDetails.get(0);
        assertEquals("Payroll Run ID","BatchTest09", billingDetail.getPayrollRun().getSourcePayRunId());
        assertEquals("Company Id", "8574536", billingDetail.getPayrollRun().getCompany().getSourceCompanyId());
        assertEquals("Billing Detail Amount", ServiceChargePrices.getNormalPerPayrollServiceChargeWithSalesTaxFY16(2), billingDetail.getItemTotal());
        assertEquals("Billing Detail Item Name", "Direct Deposit Fee", billingDetail.getItemName());
        assertEquals("Billing Detail SKU", "408181", billingDetail.getItemSku());
        assertEquals("Billing Detail Quantity", 2, billingDetail.getQuantity());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test for query findLastNonZeroSalesTaxRate in BillingDetail.query.hbm.xml
     */
    @Test
    public void testFindLastNonZeroSalesTaxRate() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        createQBDTTaxCompaniesAndSubmitPayroll();

        // add a PerTransmission charge (usually subject to sales tax, if the address is right)
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        CompanyBankAccount companyBankAccount = payrollRun.getCompanyBankAccountForService(
                ServiceCode.DirectDeposit);
        SpcfCalendar settlementDate = payrollRun.getBillingDetailCollection().get(0).getFeeTransaction().getSettlementDate();
        CompanyOffering companyOffering = company.getOffering(ServiceCode.DirectDeposit);
        BillingDetail.createBillingDetail(payrollRun, companyBankAccount, OfferingServiceChargeType.PerTransmission, 5, settlementDate.toLocal(), companyOffering.getOffering().getOfferingCode());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company2 = Company.findCompany("1234567", SourceSystemCode.QBDT);
        PayrollRun payrollRun2 = PayrollRun.findPayrollRun(company2, "BatchId01");
        CompanyBankAccount companyBankAccount2 = payrollRun2.getCompanyBankAccountForService(
                ServiceCode.DirectDeposit);
        settlementDate = payrollRun2.getBillingDetailCollection().get(0).getFeeTransaction().getSettlementDate();
        BillingDetail.createBillingDetail(payrollRun2, companyBankAccount2, OfferingServiceChargeType.PerTransmission, 5, settlementDate.toLocal(), companyOffering.getOffering().getOfferingCode());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Application.findById(Company.class, company.getId());
        String[] names = new String[2];
        names[0] = "company";
        names[1] = "sku";

        Object[] values = new Object[2];
        values[0] = company;
        values[1] = "408181";
        DomainEntitySet<BillingDetail> found = Application.findByNamedQuery("findLastNonZeroSalesTaxRate", names, values, 0, -1);
        assertEquals("Number of billing details found", 1, found.size());
        assertEquals("Payroll Run ID","BatchId01", found.get(0).getPayrollRun().getSourcePayRunId());
        assertEquals("CompanyId ID","123272727", found.get(0).getPayrollRun().getCompany().getSourceCompanyId());
        assertEquals("Tax amount",new SpcfMoney("0.08"), found.get(0).getTaxAmount());
        PayrollServices.commitUnitOfWork();
    }

    /*
     *******************************************
     * Company related Named query tests
     *******************************************
     */

    @Test
    public void testSearchCompanies() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Company company3 = Company.findCompany("8574536", SourceSystemCode.QBDT);
        // todo call update company process
        DTOFactory fac = new DTOFactory();
        CompanyDTO dtoUpdate = fac.create(company1);
        dtoUpdate.setLegalName("Legal Name1");
        ProcessResult<Company>  result =
                PayrollServices.companyManager.updateCompany(SourceSystemCode.QBOE, "1234567", dtoUpdate);
        assertSuccess(result);
        dtoUpdate = fac.create(company3);
        dtoUpdate.setLegalName("Legal Name3");
        result =
                PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, "8574536", dtoUpdate);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> retList = Company.searchCompaniesByLegalName("Legal Name");
        assertEquals("Number of Companies found", 2, retList.size());
        DomainEntitySet<Company> companyList = retList.sort(Company.SourceCompanyId());
        assertEquals("Company1", company1.getId(), companyList.get(0).getId());
        assertEquals("Company2", company3.getId(), companyList.get(1).getId());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testSearchCompaniesByLegalNameAsAdmin() {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Company company3 = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("Admin"),"mock","name");
        company1.setIsDgDisassociated(true);
        company3.setIsDgDisassociated(true);
        Application.save(company1);
        Application.save(company3);
        // todo call update company process
        DTOFactory fac = new DTOFactory();
        CompanyDTO dtoUpdate = fac.create(company1);
        dtoUpdate.setLegalName("Legal Name1");
        ProcessResult<Company>  result =
                PayrollServices.companyManager.updateCompany(SourceSystemCode.QBOE, "1234567", dtoUpdate);
        assertSuccess(result);
        dtoUpdate = fac.create(company3);
        dtoUpdate.setLegalName("Legal Name3");
        result =
                PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, "8574536", dtoUpdate);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> retList = Company.searchCompaniesByLegalName("Legal Name");
        assertEquals("Number of Companies found", 2, retList.size());
        DomainEntitySet<Company> companyList = retList.sort(Company.SourceCompanyId());
        assertEquals("Company1", company1.getId(), companyList.get(0).getId());
        assertEquals("Company2", company3.getId(), companyList.get(1).getId());
        assertEquals(company1.getIsDgDisassociated(), true);
        assertEquals(company3.getIsDgDisassociated(), true);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testSearchCompaniesByLegalNameAsNonAdmin() {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Company company3 = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("RTBAdmin"),"mock","name");
        company1.setIsDgDisassociated(true);
        company3.setIsDgDisassociated(true);
        Application.save(company1);
        Application.save(company3);
        // todo call update company process
        DTOFactory fac = new DTOFactory();
        CompanyDTO dtoUpdate = fac.create(company1);
        dtoUpdate.setLegalName("Legal Name1");
        ProcessResult<Company>  result =
                PayrollServices.companyManager.updateCompany(SourceSystemCode.QBOE, "1234567", dtoUpdate);
        assertSuccess(result);
        dtoUpdate = fac.create(company3);
        dtoUpdate.setLegalName("Legal Name3");
        result =
                PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, "8574536", dtoUpdate);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        DomainEntitySet<Company> retList = Company.searchCompaniesByLegalName("Legal Name");
        assertEquals("Number of Companies found", 0, retList.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testSearchCompaniesByLegalNameForNonSAPFlow() {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Company company3 = Company.findCompany("8574536", SourceSystemCode.QBDT);
        company1.setIsDgDisassociated(true);
        company3.setIsDgDisassociated(true);
        Application.save(company1);
        Application.save(company3);
        // todo call update company process
        DTOFactory fac = new DTOFactory();
        CompanyDTO dtoUpdate = fac.create(company1);
        dtoUpdate.setLegalName("Legal Name1");
        ProcessResult<Company>  result =
                PayrollServices.companyManager.updateCompany(SourceSystemCode.QBOE, "1234567", dtoUpdate);
        assertSuccess(result);
        dtoUpdate = fac.create(company3);
        dtoUpdate.setLegalName("Legal Name3");
        result =
                PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, "8574536", dtoUpdate);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> retList = Company.searchCompaniesByLegalName("Legal Name");
        assertEquals("Number of Companies found", 0, retList.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testSearchCompaniesByAnythingUsingLegalNameAsAdmin() {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Company company3 = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("Admin"),"mock","name");
        company1.setIsDgDisassociated(true);
        company3.setIsDgDisassociated(true);
        Application.save(company1);
        Application.save(company3);
        // todo call update company process
        DTOFactory fac = new DTOFactory();
        CompanyDTO dtoUpdate = fac.create(company1);
        dtoUpdate.setLegalName("Legal Name1");
        ProcessResult<Company>  result =
                PayrollServices.companyManager.updateCompany(SourceSystemCode.QBOE, "1234567", dtoUpdate);
        assertSuccess(result);
        dtoUpdate = fac.create(company3);
        dtoUpdate.setLegalName("Legal Name3");
        result =
                PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, "8574536", dtoUpdate);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        Application.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> retList = Company.searchCompaniesByAnything("Legal Name");
        assertEquals("Number of Companies found", 2, retList.size());
        DomainEntitySet<Company> companyList = retList.sort(Company.SourceCompanyId());
        assertEquals("Company1", company1.getId(), companyList.get(0).getId());
        assertEquals("Company2", company3.getId(), companyList.get(1).getId());
        assertEquals(company1.getIsDgDisassociated(), true);
        assertEquals(company3.getIsDgDisassociated(), true);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testSearchCompaniesByAnythingUsingLegalNameAsNonAdmin() {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Company company3 = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("RTBAdmin"),"mock","name");
        company1.setIsDgDisassociated(true);
        company3.setIsDgDisassociated(true);
        Application.save(company1);
        Application.save(company3);
        // todo call update company process
        DTOFactory fac = new DTOFactory();
        CompanyDTO dtoUpdate = fac.create(company1);
        dtoUpdate.setLegalName("Legal Name1");
        ProcessResult<Company>  result =
                PayrollServices.companyManager.updateCompany(SourceSystemCode.QBOE, "1234567", dtoUpdate);
        assertSuccess(result);
        dtoUpdate = fac.create(company3);
        dtoUpdate.setLegalName("Legal Name3");
        result =
                PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, "8574536", dtoUpdate);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        DomainEntitySet<Company> retList = Company.searchCompaniesByAnything("Legal Name");
        assertEquals("Number of Companies found", 0, retList.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testSearchCompaniesByAnythingUsingLegalNameForNonSAPFlow() {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Company company3 = Company.findCompany("8574536", SourceSystemCode.QBDT);
        company1.setIsDgDisassociated(true);
        company3.setIsDgDisassociated(true);
        Application.save(company1);
        Application.save(company3);
        // todo call update company process
        DTOFactory fac = new DTOFactory();
        CompanyDTO dtoUpdate = fac.create(company1);
        dtoUpdate.setLegalName("Legal Name1");
        ProcessResult<Company>  result =
                PayrollServices.companyManager.updateCompany(SourceSystemCode.QBOE, "1234567", dtoUpdate);
        assertSuccess(result);
        dtoUpdate = fac.create(company3);
        dtoUpdate.setLegalName("Legal Name3");
        result =
                PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, "8574536", dtoUpdate);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> retList = Company.searchCompaniesByAnything("Legal Name");
        assertEquals("Number of Companies found", 0, retList.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testFindCompaniesTermedForEIN() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company1DataLoader.persistCompany1();
        company2DataLoader.persistCompany2();
        company3DataLoader.persistCompany3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company3 = Company.findCompany("8574536", SourceSystemCode.QBDT);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService>  result = PayrollServices.companyManager.terminateService(SourceSystemCode.QBDT, "8574536", ServiceCode.DirectDeposit);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        String[] paramNames = new String[1];
        paramNames[0] = "companyFedTaxIdEncList";

        Object[] paramValues = new Object[1];
        paramValues[0] = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName,"242335465");

        DomainEntitySet<Company> retList = Application.findByNamedQueryUsingCache(Company.class, "findCompaniesTermedForEINENC", paramNames, paramValues);
        // one match
        assertEquals("Companies Found", 1, retList.size());
        assertEquals("Company Id", company3.getId(), retList.get(0).getId());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        paramValues[0] = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName,"999999999");
        retList = Application.findByNamedQueryUsingCache(Company.class, "findCompaniesTermedForEINENC", paramNames, paramValues);

        // zero since company2 is not terminated
        assertEquals("Companies Found", 0, retList.size());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testFindCompanySystemEvents() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        String[] paramNames = new String[3];
        paramNames[0] = "company";
        paramNames[1] = "fromDate";
        paramNames[2] = "toDate";

        Object[] paramValues = new Object[3];
        paramValues[0] = company;
        paramValues[1] = SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone());
        paramValues[2] = PSPDate.getPSPTime();

        DomainEntitySet<CompanyEvent> retList =
                Application.findByNamedQuery("findCompanySystemEvents", paramNames, paramValues);

        assertEquals("Number of Company1 Events", 3, retList.size());

        DomainEntitySet<CompanyEvent> events = retList.sort(CompanyEvent.EventTypeCd());
        assertEquals("Event1 Type", EventTypeCode.CompanyBankAccountStatusChange, events.get(0).getEventTypeCd());
        assertEquals("Event2 Type", EventTypeCode.ServiceStatusChange, events.get(1).getEventTypeCd());
        assertEquals("Event3 Type", EventTypeCode.ServiceStatusChange, events.get(2).getEventTypeCd());

        assertEquals("Event 1 Company", company.getId(), events.get(0).getCompany().getId());
        assertEquals("Event 2 Company", company.getId(), events.get(1).getCompany().getId());
        assertEquals("Event 3 Company", company.getId(), events.get(2).getCompany().getId());

        paramValues[1] = SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone());
        paramValues[2] = SpcfCalendar.createInstance(2007, 9, 16, 0, 1, 0, 0, SpcfTimeZone.getLocalTimeZone());

        retList =
                Application.findByNamedQuery("findCompanySystemEvents", paramNames, paramValues);

        assertEquals("Number of Company1 Events", 3, retList.size());

        events = retList.sort(CompanyEvent.EventTypeCd());
        assertEquals("Event1 Type", EventTypeCode.CompanyBankAccountStatusChange, events.get(0).getEventTypeCd());
        assertEquals("Event2 Type", EventTypeCode.ServiceStatusChange, events.get(1).getEventTypeCd());
        assertEquals("Event3 Type", EventTypeCode.ServiceStatusChange, events.get(2).getEventTypeCd());

        assertEquals("Event 1 Company", company.getId(), events.get(0).getCompany().getId());
        assertEquals("Event 2 Company", company.getId(), events.get(1).getCompany().getId());
        assertEquals("Event 3 Company", company.getId(), events.get(2).getCompany().getId());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testFindSystemEventsByCompany() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        String[] paramNames = new String[1];
        paramNames[0] = "company";

        Object[] paramValues = new Object[1];
        paramValues[0] = company;

        DomainEntitySet<CompanyEvent> retList =
                Application.findByNamedQuery("findSystemEventsByCompany", paramNames, paramValues);
        assertEquals("Number of Company1 Events", 3, retList.size());

        DomainEntitySet<CompanyEvent> events = retList.sort(CompanyEvent.EventTypeCd());
        assertEquals("Event1 Type", EventTypeCode.CompanyBankAccountStatusChange, events.get(0).getEventTypeCd());
        assertEquals("Event2 Type", EventTypeCode.ServiceStatusChange, events.get(1).getEventTypeCd());
        assertEquals("Event3 Type", EventTypeCode.ServiceStatusChange, events.get(2).getEventTypeCd());

        assertEquals("Event 1 Company", company.getId(), events.get(0).getCompany().getId());
        assertEquals("Event 2 Company", company.getId(), events.get(1).getCompany().getId());
        assertEquals("Event 3 Company", company.getId(), events.get(2).getCompany().getId());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testFindSourceSystemEvents() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Company company2 = Company.findCompany("2222222", SourceSystemCode.QBOE);
        Company company3 = Company.findCompany("8574536", SourceSystemCode.QBDT);
        String[] paramNames = new String[3];
        paramNames[0] = "sourceSystemCd";
        paramNames[1] = "fromDate";
        paramNames[2] = "toDate";

        Object[] paramValues = new Object[3];
        paramValues[0] = SourceSystemCode.QBOE;
        paramValues[1] = SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone());
        paramValues[2] = CalendarUtils.getPSPDateFromDB();

        DomainEntitySet<CompanyEvent> retList =
                Application.findByNamedQuery("findSourceSystemEvents", paramNames, paramValues);

        assertEquals("Number of System Events", 10, retList.size());

        DomainEntitySet<CompanyEvent> events = retList.sort(CompanyEvent.Company().SourceCompanyId(), CompanyEvent.EventTypeCd());
        assertEquals("Event1 Type", EventTypeCode.CompanyBankAccountStatusChange, events.get(0).getEventTypeCd());
        assertEquals("Event2 Type", EventTypeCode.ServiceStatusChange, events.get(1).getEventTypeCd());
        assertEquals("Event3 Type", EventTypeCode.ServiceStatusChange, events.get(2).getEventTypeCd());
        assertEquals("Event4 Type", EventTypeCode.CompanyBankAccountStatusChange, events.get(3).getEventTypeCd());
        assertEquals("Event5 Type", EventTypeCode.ServiceStatusChange, events.get(4).getEventTypeCd());
        assertEquals("Event6 Type", EventTypeCode.ServiceStatusChange, events.get(5).getEventTypeCd());
        assertEquals("Event7 Type", EventTypeCode.CompanyBankAccountStatusChange, events.get(6).getEventTypeCd());
        assertEquals("Event8 Type", EventTypeCode.ServiceStatusChange, events.get(7).getEventTypeCd());
        assertEquals("Event9 Type", EventTypeCode.ServiceStatusChange, events.get(8).getEventTypeCd());
        assertEquals("Event10 Type", EventTypeCode.ServiceStatusChange, events.get(9).getEventTypeCd());

        assertEquals("Event 1 Company", company.getId(), events.get(0).getCompany().getId());
        assertEquals("Event 2 Company", company.getId(), events.get(1).getCompany().getId());
        assertEquals("Event 3 Company", company.getId(), events.get(2).getCompany().getId());
        assertEquals("Event 4 Company", company2.getId(), events.get(3).getCompany().getId());
        assertEquals("Event 5 Company", company2.getId(), events.get(4).getCompany().getId());
        assertEquals("Event 6 Company", company2.getId(), events.get(5).getCompany().getId());
        assertEquals("Event 7 Company", company3.getId(), events.get(6).getCompany().getId());
        assertEquals("Event 8 Company", company3.getId(), events.get(7).getCompany().getId());
        assertEquals("Event 9 Company", company3.getId(), events.get(8).getCompany().getId());
        assertEquals("Event 10 Company", company3.getId(), events.get(9).getCompany().getId());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testFindCompanyEventsByToken() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        String[] paramNames = new String[2];
        paramNames[0] = "company";
        paramNames[1] = "fromToken";

        Object[] paramValues = new Object[2];
        paramValues[0] = company;
        paramValues[1] = 0L;

        DomainEntitySet<CompanyEvent> retList =
                Application.findByNamedQuery("findCompanyEventsByToken", paramNames, paramValues);
        assertEquals("Number of Company1 Events", 3, retList.size());

        DomainEntitySet<CompanyEvent> events = retList.sort(CompanyEvent.EventToken());

        assertEquals("Event1 Type", EventTypeCode.CompanyBankAccountStatusChange, events.get(0).getEventTypeCd());
        assertEquals("Event2 Type", EventTypeCode.ServiceStatusChange, events.get(1).getEventTypeCd());
        assertEquals("Event3 Type", EventTypeCode.ServiceStatusChange, events.get(2).getEventTypeCd());

        assertEquals("Event1 Type", 1L, events.get(0).getEventToken());
        assertEquals("Event2 Type", 2L, events.get(1).getEventToken());
        assertEquals("Event3 Type", 3L, events.get(2).getEventToken());

        assertEquals("Event 1 Company", company.getId(), events.get(0).getCompany().getId());
        assertEquals("Event 2 Company", company.getId(), events.get(1).getCompany().getId());
        assertEquals("Event 3 Company", company.getId(), events.get(2).getCompany().getId());

        // again try with from token number value as 4L
        paramValues[1] = 4L;

        retList =
                Application.findByNamedQuery("findCompanyEventsByToken", paramNames, paramValues);
        assertEquals("Number of Company1 Events with token number greater than 3", 0L, retList.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testFindCompaniesByBAFraudCriteria() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company1DataLoader.persistCompany1();
        company2DataLoader.persistCompany2();
        company3DataLoader.persistCompany3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Company company2 = Company.findCompany("2222222", SourceSystemCode.QBOE);
        Company company3 = Company.findCompany("8574536", SourceSystemCode.QBDT);

        String[] paramNames = new String[3];
        paramNames[0] = "routingNumber";
        paramNames[1] = "accountNumber";
        paramNames[2] = "pCompany";

        Object[] paramValues = new Object[3];
        paramValues[0] = "263182914";
        paramValues[1] = EncryptionUtils.deterministicEncryptWithAllKeys(FraudBankAccount.AccountNumberKeyName, "123098");
        paramNames[1]="accountNumberEncList";

        paramValues[2] = company1;
        DomainEntitySet<FraudBankAccount> retList = null;

        retList = Application.findByNamedQueryUsingCache(FraudBankAccount.class, "findCompaniesByBAFraudCriteriaENC", paramNames, paramValues);

        // No match
        assertEquals("Companies Found", 0, retList.size());
        paramValues[2] = company2;
        retList = Application.findByNamedQueryUsingCache(FraudBankAccount.class, "findCompaniesByBAFraudCriteriaENC", paramNames, paramValues);

        // zero because company1 is neither terminated nor onhold
        assertEquals("Companies Found", 0, retList.size());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> result1 = PayrollServices.companyManager.terminateService(SourceSystemCode.QBOE, "1234567", ServiceCode.DirectDeposit);
        ProcessResult<CompanyService> result2 = PayrollServices.companyManager.terminateService(SourceSystemCode.QBDT, "8574536", ServiceCode.DirectDeposit);
        assertSuccess(result1);
        assertSuccess(result2);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        paramValues[2] = company1;
        retList = Application.findByNamedQueryUsingCache(FraudBankAccount.class, "findCompaniesByBAFraudCriteriaENC", paramNames, paramValues);
        // zero since no other company meets for the criteria
        assertEquals("Companies Found", 0, retList.size());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        paramValues[2] = company2;

        retList = Application.findByNamedQueryUsingCache(FraudBankAccount.class, "findCompaniesByBAFraudCriteriaENC", paramNames, paramValues);

        assertEquals("Companies Found", 1, retList.size());
        assertEquals("Company Id", company1.getId(), retList.get(0).getCompany().getId());

        PayrollServices.commitUnitOfWork();
        // add an onhold to company2
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE, "2222222", ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();
        assertSuccess(result);

        PayrollServices.beginUnitOfWork();
        paramValues[0] = "111000025";
        paramValues[1] = EncryptionUtils.deterministicEncryptWithAllKeys(FraudBankAccount.AccountNumberKeyName, "4747474747");

        paramValues[2] = company3;
        retList = Application.findByNamedQueryUsingCache(FraudBankAccount.class, "findCompaniesByBAFraudCriteriaENC", paramNames, paramValues);
        assertEquals("Companies Found", 1, retList.size());
        assertEquals("Company Id", company2.getId(), retList.get(0).getCompany().getId());

        PayrollServices.commitUnitOfWork();
        // todo expire company2 onhold with same above criteria
        // expire onhold of company2
        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.removeOnHoldReason(SourceSystemCode.QBOE, "2222222", ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();
        assertSuccess(result);

        PayrollServices.beginUnitOfWork();
        paramValues[0] = "111000025";
        paramValues[1] = EncryptionUtils.deterministicEncryptWithAllKeys(FraudBankAccount.AccountNumberKeyName, "4747474747");
        paramValues[2] = company3;

        retList = Application.findByNamedQueryUsingCache(FraudBankAccount.class, "findCompaniesByBAFraudCriteriaENC", paramNames, paramValues);

        assertEquals("Companies Found", 0, retList.size());

        PayrollServices.commitUnitOfWork();
    }


    /*
     *******************************************
     * CompanyEvent related Named query tests
     *******************************************
     */

    @Test
    public void testFindCompanyEventDetailsByTypeAndValue() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Company company3 = Company.findCompany("8574536", SourceSystemCode.QBDT);

        DomainEntitySet<CompanyEventDetail> retList = CompanyEvent.findCompanyEventDetails(company1, EventTypeCode.ServiceStatusChange, EventDetailTypeCode.NewServiceStatus,  "Pending First Payroll");

        assertEquals("Number of event details found", 1, retList.size());
        assertEquals("Event Type", EventTypeCode.ServiceStatusChange, retList.get(0).getCompanyEvent().getEventTypeCd());
        assertEquals("Event Detail Type", EventDetailTypeCode.NewServiceStatus, retList.get(0).getEventDetailTypeCd());
        assertEquals("Event Detail Value", "Pending First Payroll", retList.get(0).getValue());
        assertEquals("Company", company1.getId(), retList.get(0).getCompanyEvent().getCompany().getId());
        // specify QBDT company event code for ServiceStatusChange event details
        retList = CompanyEvent.findCompanyEventDetails(company1, EventTypeCode.PINCreated, EventDetailTypeCode.NewServiceStatus,  "Pending First Payroll");

        assertEquals("Number of event details found", 0, retList.size());

        // specify QBDT company and PINCreated event code for ServiceStatusChange event details
        retList = CompanyEvent.findCompanyEventDetails(company3, EventTypeCode.PINCreated, EventDetailTypeCode.NewServiceStatus,  "Pending First Payroll");

        assertEquals("Number of event details found", 0, retList.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testFindCompanyEventsByTypeAndValue() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Company company3 = Company.findCompany("8574536", SourceSystemCode.QBDT);

        DomainEntitySet<CompanyEvent> retList = CompanyEvent.findCompanyEvents(company1, EventDetailTypeCode.NewServiceStatus, "Pending First Payroll");

        assertEquals("Number of events found", 1, retList.size());
        assertEquals("Event Type", EventTypeCode.ServiceStatusChange, retList.get(0).getEventTypeCd());

        assertEquals("Event Detail Value", "Pending First Payroll",
                retList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.NewServiceStatus));
        assertEquals("Company", company1.getId(), retList.get(0).getCompany().getId());

        // QBDT event detail value for QBOE company
        retList = CompanyEvent.findCompanyEvents(company1, EventDetailTypeCode.NewServiceStatus, "Pending PIN Creation");
        assertEquals("Number of events found", 0, retList.size());

        // QBDT event detail value for QBDT company
        retList = CompanyEvent.findCompanyEvents(company3, EventDetailTypeCode.NewServiceStatus, "Pending PIN Creation");
        assertEquals("Number of events found", 1, retList.size());
        assertEquals("Event Type", EventTypeCode.ServiceStatusChange, retList.get(0).getEventTypeCd());

        assertEquals("Event Detail Value", "Pending PIN Creation",
                retList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.NewServiceStatus));
        assertEquals("Company", company3.getId(), retList.get(0).getCompany().getId());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testFindCompanyEventDetailsByGUIDValueNewEventType() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("1234567", SourceSystemCode.QBOE);
        CompanyBankAccount cba1 = CompanyBankAccount.findActiveCompanyBankAccount(company1);

        DomainEntitySet<CompanyEventDetail> retList = CompanyEvent.findCompanyEventDetailForEventDetailValue(company1, EventDetailTypeCode.NewBAStatus, EventDetailTypeCode.OldBAStatus, "Pending Verification");
        assertEquals("Number of event details found", 1, retList.size());

        retList =  CompanyEvent.findCompanyEventDetailForEventDetailValue(company1, EventDetailTypeCode.NewBAStatus, EventDetailTypeCode.CompanyBankAccountId, cba1.getId().toString());
        assertEquals("Number of event details found", 1, retList.size());
        assertEquals("Event Type", EventTypeCode.CompanyBankAccountStatusChange, retList.get(0).getCompanyEvent().getEventTypeCd());

        assertEquals("Event Detail Value", "Active",
                retList.get(0).getCompanyEvent().getCompanyEventDetailValue(EventDetailTypeCode.NewBAStatus));
        assertEquals("Company", company1.getId(), retList.get(0).getCompanyEvent().getCompany().getId());

        PayrollServices.commitUnitOfWork();
    }

    private void createQBDTTaxCompaniesAndSubmitPayroll() {

        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        List<PayrollRunDTO> payrollRunDTOs = loadCompaniesAndPayrollsWithValidAddress(SourceSystemCode.QBDT);
        setOfferCodeForCompany("123272727", payrollRunDTOs.get(0), DataLoader.TAXABLE_ADDRESS, null,  null);
        setOfferCodeForCompany("1234567", payrollRunDTOs.get(1), DataLoader.TAXABLE_ADDRESS2, null,  null);

    }

    private void setOfferCodeForCompany(String sourceCompanyId, PayrollRunDTO pPayrollRunDTO,
                                   AddressDTO pLegalAddress, SpcfCalendar pTaxExemptExpirationDate, String pOfferCd) {

        PayrollServices.beginUnitOfWork();
        // set the company's tax-exempt expiration date based on the input param
        Company company1 = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        DTOFactory fac = new DTOFactory();
        CompanyDTO dtoUpdate = fac.create(company1);
        dtoUpdate.setTaxExemptExpirationDate(pTaxExemptExpirationDate==null ? null : new DateDTO(pTaxExemptExpirationDate));
        if (pTaxExemptExpirationDate != null) {
            /*  Tax Exemption is intended   */
            dtoUpdate.setTaxExemptStatus(TaxExemptStatusCode.Exempt);
        }

        if (pLegalAddress != null) {
            dtoUpdate.setLegalAddress(pLegalAddress);
        }
        ProcessResult<Company> prUpdate = PayrollServices.companyManager.updateCompany(company1.getSourceSystemCd(), company1.getSourceCompanyId(), dtoUpdate);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("Updating company for tax-exempt-expiration and legal address", prUpdate);
        company1 = prUpdate.getResult();

        CompanyOffer companyOffer = null;
        if (pOfferCd != null) {
            PayrollServices.beginUnitOfWork();
            // claim offer for Company
            Offer offer = Offer.findOfferByOfferCode(pOfferCd); // P57553 is 50% off PerPaycheck charges
            company1 = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
            companyOffer = company1.claimOfferForCompany(offer);
            PayrollServices.commitUnitOfWork();

            assertTrue("offer claimed", companyOffer != null);
            assertTrue("Company Offer", companyOffer != null);
            assertTrue("Company Offer is Active", companyOffer.companyOfferIsActive());
        }

        PayrollServices.beginUnitOfWork();
        // submit the payroll -- this will add a PerPaycheck fee with some quantity
        ProcessResult<PayrollRun> processResult;
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, sourceCompanyId, pPayrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue("Process Result", processResult.isSuccess());
    }

     public List<PayrollRunDTO> loadCompaniesAndPayrollsWithValidAddress(SourceSystemCode pSourceSystemCode) {
        PayrollServices.beginUnitOfWork();
        DataLoader dataLoader = new DataLoader();
        dataLoader.setSrcSystemCodeForNewCompany(pSourceSystemCode);

        // Create Company and CompanyBankAccount
        Company company = dataLoader.persistTestActiveCompanyWithValidAddress();
         // Create another Company and CompanyBankAccount
        Company company2 = dataLoader.persistTestActiveCompany2WithValidAddress();

        // Create Company Service - Direct Deposit
        dataLoader.persistTestCompanyService(company);
        dataLoader.persistTestCompanyService(company2);

        CompanyBankAccount companyBankAccount = dataLoader.persistCompanyBankAccount(company, dataLoader.getTestCompanyBankAccount());
        CompanyBankAccount companyBankAccount2 = dataLoader.persistCompanyBankAccount(company2, dataLoader.getTestCompanyBankAccount2());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //Create company PIN
        company = Application.findById(Company.class, company.getId());
        company2 = Application.findById(Company.class, company2.getId());
        payrollDataLoader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        payrollDataLoader.persistCompanyPIN(company.getSourceCompanyId());
        payrollDataLoader.persistCompanyPIN(company2.getSourceCompanyId());
        PayrollServices.commitUnitOfWork();
        // Create Employees and Employee Bank Accounts
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", pSourceSystemCode);
        GenerateData.generateEmployees(company, 2);
        GenerateData.generateEmployeeBankAccounts(GenerateData.getEmployeeCollection(company.getDirectDepositEmployees()), 1, "Active");
//        company = PayrollServicesTest.save(company);

        company = Company.findCompany("123272727", pSourceSystemCode);
         companyBankAccount = Application.findById(CompanyBankAccount.class, companyBankAccount.getId());
        PayrollRunDTO payrollRunDTO = payrollDataLoader.createPayrollRunDTO(company, companyBankAccount, "BatchId01");
        List<PayrollRunDTO> payrollDTOs = new ArrayList<PayrollRunDTO>();
        payrollDTOs.add(payrollRunDTO);
        company2 = Company.findCompany("1234567", pSourceSystemCode);
        GenerateData.generateEmployees(company2, 2);
        GenerateData.generateEmployeeBankAccounts(GenerateData.getEmployeeCollection(company2.getDirectDepositEmployees()), 1, "Active");
//        company = PayrollServicesTest.save(company2);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", pSourceSystemCode);
         companyBankAccount2 = Application.findById(CompanyBankAccount.class, companyBankAccount2.getId());
        payrollRunDTO = payrollDataLoader.createPayrollRunDTO(company2, companyBankAccount2, "BatchId01");
        PayrollServices.commitUnitOfWork();
        payrollDTOs.add(payrollRunDTO);
        return payrollDTOs;
    }

    private void loadMultipleCompaniesAndPayrolls() {
        Company company1 = company1DataLoader.persistCompany1();
        Collection<PayrollRunDTO> payrollDTOs = new ArrayList<PayrollRunDTO>();
        payrollDTOs.add(company1DataLoader.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02")));
        payrollDTOs.add(company1DataLoader.getCompany1PR2_DoesNotExceedLimits(new DateDTO("2007-10-09")));
        submitPayroll(company1, payrollDTOs);
        payrollDTOs.clear();
        PSPDate.addDaysToPSPTime(-9);
        Company company2 = company2DataLoader.persistCompany2();
        payrollDTOs.add(company2DataLoader.getCompany2PR_DoesNotExceedLimits(new DateDTO("2007-10-02")));
        payrollDTOs.add(company2DataLoader.getCompany2PR2_DoesNotExceedLimits(new DateDTO("2007-10-09")));
        submitPayroll(company2, payrollDTOs);
        payrollDTOs.clear();
        PSPDate.addDaysToPSPTime(-8);
        Company company3 = company3DataLoader.persistCompany3();
        payrollDTOs.add(company3DataLoader.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-10-02")));
        payrollDTOs.add(company3DataLoader.getCompany3PR2_DoesNotExceedLimits(new DateDTO("2007-10-09")));
        submitPayroll(company3,payrollDTOs);
        payrollDTOs.clear();
    }

   private void submitPayroll(Company pCompany, Collection<PayrollRunDTO> payrollRunDTOs) {
        for (PayrollRunDTO payrollRunDTO: payrollRunDTOs) {
            ProcessResult<PayrollRun> result = PayrollServices.payrollManager.submitPayroll(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), payrollRunDTO);
            assertSuccess(result);
        }
    }

    private TreeMap<SpcfCalendar, SpcfMoney> getAmountsPerPayCheckDate(Company pCompany, SpcfCalendar pBaseCheckDate) {
        int intHalfEeLimitDuration = Integer.parseInt(LimitRule.findLimitRule(pCompany, ServiceCode.DirectDeposit)
                                                              .findLimitValueByName(LimitValueType.CompanyLimitDuration).getValue());

        String[] paramNames = {"companyId", "fromDate", "toDate", "txnType", "txnState"};
        Object[] paramValues = new Object[5];

        paramValues[0] = pCompany.getId();

        //from date
        SpcfCalendar fromCalendar = pBaseCheckDate.copy();
        fromCalendar.addDays(-intHalfEeLimitDuration);
        paramValues[1] = fromCalendar;

        //to date
        SpcfCalendar toCalendar = pBaseCheckDate.copy();
        toCalendar.addDays(intHalfEeLimitDuration);
        paramValues[2] = toCalendar;

        paramValues[3] = TransactionTypeCode.EmployeeDdCredit;
        paramValues[4] = TransactionStateCode.Cancelled;

        TreeMap<SpcfCalendar, SpcfMoney> amountsPerPayCheckDate = new TreeMap<SpcfCalendar, SpcfMoney>(new SpcfCalendarComparator());
        List<Object> retList = Application.executeNamedQuery("SumAmountGroupByPayCheckDate", paramNames, paramValues);

        for (Object row : retList) {
            SpcfCalendar checkDate = (SpcfCalendar) ((Object[]) row)[0];
            checkDate = checkDate.toLocal();
            SpcfMoney checkAmount = (SpcfMoney) ((Object[]) row)[1];

            amountsPerPayCheckDate.put(checkDate, checkAmount);
        }

        return amountsPerPayCheckDate;
    }

     private HashMap<SpcfUniqueId, TreeMap<SpcfCalendar, SpcfMoney>> getAmountsPerEmployeePerPayCheckDate(Company pCompany, SpcfCalendar pBaseCheckDate) {
        //
        // First add all payrolls that are in the database
        //
         int intHalfEeLimitDuration = Integer.parseInt(LimitRule.findLimitRule(pCompany, ServiceCode.DirectDeposit)
                                                                .findLimitValueByName(LimitValueType.EmployeeLimitDuration).getValue());

        String[] paramNames = {"companyId", "fromDate", "toDate", "txnType", "txnState"};
        Object[] paramValues = new Object[5];

        paramValues[0] = pCompany.getId();

        //from date
        SpcfCalendar fromCalendar = pBaseCheckDate.copy();
        fromCalendar.addDays(-intHalfEeLimitDuration);
        paramValues[1] = fromCalendar;

        //to date
        SpcfCalendar toCalendar = pBaseCheckDate.copy();
        toCalendar.addDays(intHalfEeLimitDuration);
        paramValues[2] = toCalendar;

        paramValues[3] = TransactionTypeCode.EmployeeDdCredit;
        paramValues[4] = TransactionStateCode.Cancelled;

        HashMap<SpcfUniqueId, TreeMap<SpcfCalendar, SpcfMoney>> amountsPerEmployeePerPayCheckDate = new HashMap<SpcfUniqueId, TreeMap<SpcfCalendar, SpcfMoney>>();
        List<Object> retList = Application.executeNamedQuery("SumAmountGroupByPayCheckDateAndEmployee", paramNames, paramValues);

        for (Object row : retList) {
            SpcfUniqueId employeeId = (SpcfUniqueId) ((Object[]) row)[0];
            SpcfCalendar checkDate = (SpcfCalendar) ((Object[]) row)[1];
            checkDate = checkDate.toLocal();
            SpcfMoney checkAmount = (SpcfMoney) ((Object[]) row)[2];

            TreeMap<SpcfCalendar, SpcfMoney> checkDateAmountMap = amountsPerEmployeePerPayCheckDate.get(employeeId);
            if (checkDateAmountMap == null) {
                checkDateAmountMap = new TreeMap<SpcfCalendar, SpcfMoney>(new SpcfCalendarComparator());
                amountsPerEmployeePerPayCheckDate.put(employeeId, checkDateAmountMap);
            }
            checkDateAmountMap.put(checkDate, checkAmount);
        }

        return amountsPerEmployeePerPayCheckDate;
    }

    private HashMap<String, TreeMap<SpcfCalendar, SpcfMoney>> getAmountsPerBankAccountPerPayCheckDate(Company pCompany, SpcfCalendar pBaseCheckDate) {
        int intHalfEeLimitDuration = Integer.parseInt(LimitRule.findLimitRule(pCompany, ServiceCode.DirectDeposit)
                .findLimitValueByName(LimitValueType.EmployeeLimitDuration).getValue());
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        String[] paramNames = {"companyId", "creditBankAccountType", "fromDate", "toDate", "txnType", "txnState"};
        Object[] paramValues = new Object[6];

        paramValues[0] = pCompany.getId();
        paramValues[1] = BankAccountOwnerType.Employee;

        //from date
        SpcfCalendar fromCalendar = pBaseCheckDate.copy();
        fromCalendar.addDays(-intHalfEeLimitDuration);
        paramValues[2] = fromCalendar;

        //to date
        SpcfCalendar toCalendar = pBaseCheckDate.copy();
        toCalendar.addDays(intHalfEeLimitDuration);
        paramValues[3] = toCalendar;

        paramValues[4] = TransactionTypeCode.EmployeeDdCredit;
        paramValues[5] = TransactionStateCode.Cancelled;

        HashMap<String, TreeMap<SpcfCalendar, SpcfMoney>> amountsPerBankAccountPerPayCheckDate = new HashMap<String, TreeMap<SpcfCalendar, SpcfMoney>>();
        String namedQuery = "SumAmountGroupByPayCheckDateAndBankAccountENC";
        List<Object> retList = Application.executeNamedQuery(namedQuery, paramNames, paramValues);

        String bankAccount = null;
        for (Object row : retList) {
            bankAccount = ((Object[]) row)[1].toString();
            bankAccount = EncryptionUtils.deterministicDecrypt(BankAccount.AccountNumberKeyName, bankAccount);
            String routingNumberAndBankAccountNumber = ((Object[]) row)[0].toString() + ":" + bankAccount;
            SpcfCalendar checkDate = (SpcfCalendar) ((Object[]) row)[2];
            checkDate = checkDate.toLocal();
            SpcfMoney checkAmount = (SpcfMoney) ((Object[]) row)[3];

            TreeMap<SpcfCalendar, SpcfMoney> checkDateAmountMap = amountsPerBankAccountPerPayCheckDate.get(routingNumberAndBankAccountNumber);
            if (checkDateAmountMap == null) {
                checkDateAmountMap = new TreeMap<SpcfCalendar, SpcfMoney>(new SpcfCalendarComparator());
                amountsPerBankAccountPerPayCheckDate.put(routingNumberAndBankAccountNumber, checkDateAmountMap);
            }
            checkDateAmountMap.put(checkDate, checkAmount);
        }
        return amountsPerBankAccountPerPayCheckDate;
    }



    private class SpcfCalendarComparator implements Comparator<SpcfCalendar> {
       public int compare(SpcfCalendar o1, SpcfCalendar o2) {
           if (o1.before(o2)) return -1;
           if (o2.before(o1)) return 1;
           return 0;
       }
   }

}
