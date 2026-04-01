package com.intuit.sbd.payroll.psp.processes;

import com.intuit.payroll.agency.api.IPaymentPeriod;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.MockSimpleSftpFile;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.api.dtos.factory.DTOFactory;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.processors.NightlyBatchJobsProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.common.utils.SftpFactory;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.PstubMsgType;
import com.intuit.sbd.payroll.psp.gateways.email.EmailGateway;
import com.intuit.sbd.payroll.psp.processes.dataloaders.GenerateData;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.workflows.WorkflowState;
import com.intuit.sbd.payroll.psp.workflows.Workflows;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.FlushMode;
import org.hibernate.SQLQuery;
import org.junit.Assert;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.*;

/**
 * User: rnorian
 * Date: Mar 2, 2010
 * Time: 12:37:40 PM
 */
public class DataLoadServices {

    private static int employeeCount = 1;
    private static int payrollCount = 1;
    private static boolean loadAdditionalSavingsAccount = false;

    public static void main(String[] args) {
        try {
            PayrollServicesTest.truncateTables();
            DataLoadServices.newCompany(SourceSystemCode.QBDT,
                    ServiceCode.DirectDeposit,
                    ServiceCode.ThirdParty401k);
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
        System.out.println("SUCCESS");

    }

    public static Integer employeeId = 0;
    private static Integer companyId = 0;
    private static Integer fedTaxId = 0;
    private static Integer bankAccountId = 0;
    private static Integer payrollBatchId = 0;
    private static Integer paycheckId = 0;
    private static Integer payrollItemId = 0;
    private static Integer billPaymentId = 0;
    private static Integer billPaymentSplitId = 0;

    public static final String PIN = "test1234!";
    public static final String LIC_PREFIX = "lic_";
    public static final String EOC_PREFIX = "eoc_";
    public static final String WAIVE_ALL_FEES = "Waive all major fees";

    /**
     * Creates a new company with specified services.  The new company includes:
     * <p/>
     * <p/>
     * The CloudService will be added implicitly even if it is not specified.
     *
     * @param pSourceSystemCode
     * @param pServiceCodes
     * @return
     */
    public static Company newCompany(SourceSystemCode pSourceSystemCode, ServiceCode... pServiceCodes) {
        return newCompany(pSourceSystemCode, true, pServiceCodes);
    }

    public static Company newCompany(SourceSystemCode pSourceSystemCode, boolean activateServices, ServiceCode... pServiceCodes) {
        String psid = "TEST_" + String.format("%1$04d", ++companyId);
        return newCompany(pSourceSystemCode, psid, activateServices, pServiceCodes);
    }

    public static String getNextPSID() {
        //does not increment
        return "TEST_" + String.format("%1$04d", companyId + 1);
    }

    public static Company newCompany(SourceSystemCode pSourceSystemCode, String pPSID, boolean activateServices, ServiceCode... pServiceCodes) {
        return newCompany(pSourceSystemCode, pPSID, null, activateServices, pServiceCodes);
    }

    public static Company newCompany(SourceSystemCode pSourceSystemCode, String pPSID, String pEin, boolean activateServices, ServiceCode... pServiceCodes) {

        Company company = DataLoadServices.newCompany(pSourceSystemCode, pPSID, pEin, null);

        boolean addBPService = false;
        List<ServiceCode> serviceCodeList = Arrays.asList(pServiceCodes);
        if (serviceCodeList.contains(ServiceCode.BillPayment)) {
            if (!serviceCodeList.contains(ServiceCode.DirectDeposit) || (serviceCodeList.contains(ServiceCode.DirectDeposit) && !activateServices)) {
                throw new RuntimeException("BillPayment service requires active DirectDeposit service.");
            }
            ArrayList<ServiceCode> serviceCodes = new ArrayList<ServiceCode>(serviceCodeList);
            serviceCodes.remove(ServiceCode.BillPayment);
            pServiceCodes = serviceCodes.toArray(new ServiceCode[]{});
            addBPService = true;
        }

        addServices(company, pServiceCodes);

        if (activateServices) {
            activateServices(company, pServiceCodes);
        }

        if (addBPService) {
            addServices(company, ServiceCode.BillPayment);
        }

        return company;
    }

    public static Company newCompany(SourceSystemCode sourceSystemCode, String psid) {
        return newCompany(sourceSystemCode, psid, null);
    }

    public static Company newCompany(SourceSystemCode sourceSystemCode, String psid, String pin) {
        return newCompany(sourceSystemCode, psid, null, pin);
    }

    public static Company newCompany(SourceSystemCode sourceSystemCode, String psid, String fein, String pin) {
        return newCompany(createCompany(sourceSystemCode, psid, fein), pin);
    }

    public static Company newCompany(CompanyDTO companyDTO, String pin) {
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> prAddCompany = PayrollServices.companyManager.addCompany(companyDTO);
        assertSuccess("addCompany", prAddCompany);
        PayrollServices.commitUnitOfWork();

        return prAddCompany.getResult();
    }

    public static CompanyLaw newCompanyLaw(Company company, String sourceId, String lawId) {
        CompanyLawDTO companyLawDTO = createCompanyLawDTO(sourceId, lawId);
        ProcessResult processResult = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO);
        assertSuccess("Add Law Failed.", processResult);
        return (CompanyLaw) processResult.getResult();
    }

    public static ProcessResult<Company> newCompany(SourceSystemCode sourceSystemCode, String psid, String pin, CompanyDTO companyDTO, List<EmployeeDTO> employeeDTOs) {
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(companyDTO);
        assertSuccess("addCompany", result);
        PayrollServices.commitUnitOfWork();

        result.merge(persistEmployees(sourceSystemCode, psid, employeeDTOs));

        return result;
    }

    public static Company refreshCompany(Company pCompany) {
        PayrollServices.beginUnitOfWork();
        pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        PayrollServices.rollbackUnitOfWork();
        return pCompany;
    }

    public static void updateCompanyPIN(Company pCompany, String pin) {
        PayrollServices.beginUnitOfWork();
        ProcessResult<HashMap<String, String>> prPIN =
                PayrollServices.subscriptionManager.updateCompanyPIN(pCompany.getSourceSystemCd(),
                        pCompany.getSourceCompanyId(),
                        pin);
        assertSuccess("setPIN", prPIN);
        PayrollServices.commitUnitOfWork();
    }

    public static void addCompanyPIN(Company pCompany, String pin) {
        PayrollServices.beginUnitOfWork();
        pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        if (pCompany.getCompanyPINCollection().size() == 0) {
            if (pin == null) {
                pin = PIN;
            }
            ProcessResult<HashMap<String, String>> prCreatePIN =
                    PayrollServices.subscriptionManager.createCompanyPIN(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), pin);
            assertSuccess("createPIN", prCreatePIN);
            PayrollServices.commitUnitOfWork();
        } else {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    public static CompanyBankAccount addCompanyBankAccount(Company pCompany) {
        return addCompanyBankAccount(pCompany, false);
    }

    public static CompanyBankAccount addCompanyBankAccount(Company pCompany, boolean randomDebits) {
        PayrollServices.beginUnitOfWork();
        // make sure company does not already have an active bank account
        pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(pCompany);
        if (companyBankAccount == null) {
            ProcessResult<CompanyBankAccount> prAddBank =
                    PayrollServices.companyManager.addCompanyBankAccount(pCompany.getSourceSystemCd(),
                            pCompany.getSourceCompanyId(),
                            createCompanyBankAccount(),
                            randomDebits, false);
            assertSuccess("addCompanyBankAccount", prAddBank);
            companyBankAccount = prAddBank.getResult();
            PayrollServices.commitUnitOfWork();
        } else {
            PayrollServices.rollbackUnitOfWork();
        }

        return companyBankAccount;
    }

    public static void verifyCompanyBankAccount(CompanyBankAccount pCompanyBankAccount) {
        PayrollServices.beginUnitOfWork();
        pCompanyBankAccount = Application.refresh(pCompanyBankAccount);
        Company company = pCompanyBankAccount.getCompany();
        SpcfMoney amount1 = pCompanyBankAccount.getVerificationTransactions().get(0).getFinancialTransactionAmount();
        SpcfMoney amount2 = pCompanyBankAccount.getVerificationTransactions().get(1).getFinancialTransactionAmount();

        assertSuccess(PayrollServices.companyManager.verifyCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(),
                                                                pCompanyBankAccount.getSourceBankAccountId(), amount1, amount2, false));
        PayrollServices.commitUnitOfWork();
    }

    public static void enrollEFTPS(Company pCompany) {
        PayrollServices.beginUnitOfWork();
        pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        switch (pCompany.getCurrentEnrollment().getStatusCd()) {
            case PendingEnrollment:
                PayrollServices.companyManager.updateEftpsEnrollment(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), EftpsEnrollmentStatus.PendingAcceptance);
            case PendingAcceptance:
                PayrollServices.companyManager.updateEftpsEnrollment(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), EftpsEnrollmentStatus.Enrolled);
        }
        PayrollServices.commitUnitOfWork();
    }

    //precondition: enrollment created
    //should happen automatically if active and AID is correct
    public static void enrollACH(Company pCompany) {
        PayrollServices.beginUnitOfWork();
        pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        if (pCompany.getCurrentACHEnrollment() != null) {
            switch (pCompany.getCurrentACHEnrollmentStatus()) {
                case PendingEnrollment:
                    assertSuccess(PayrollServices.companyManager.updateACHEnrollmentStatus(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), PaymentTemplate.FL_SUI, ACHEnrollmentStatus.PendingEnrollmentResponse));
                case PendingEnrollmentResponse:
                    assertSuccess(PayrollServices.companyManager.updateACHEnrollmentStatus(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), PaymentTemplate.FL_SUI, ACHEnrollmentStatus.Enrolled));
            }
        }
        PayrollServices.commitUnitOfWork();
    }

    public static void updateACHAgentEnabledFlags(Company company, String paymentTemplateCd, boolean agentEnabled) {
        PayrollServices.beginUnitOfWork();
        for (CompanyAgency companyAgency : Application.refresh(company).getCompanyAgencyCollection()) {
            for (CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate : companyAgency.getCompanyAgencyPaymentTemplateCollection()) {
                if (paymentTemplateCd == null || companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd().equals(paymentTemplateCd)) {
                    for (CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod :
                            companyAgencyPaymentTemplate.getCompanyPaymentTemplatePaymentMethodCollection()
                                    .find(CompanyPaymentTemplatePaymentMethod.PaymentMethod().in(PaymentMethod.ACHCredit, PaymentMethod.EDI))) {
                        assertSuccess(PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(),
                                company.getSourceCompanyId(),
                                companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd(),
                                companyPaymentTemplatePaymentMethod.getPaymentMethod(),
                                agentEnabled));
                    }
                }
            }
        }
        PayrollServices.commitUnitOfWork();
    }

    /*
    Updates CA IDs
     */

    public static void updateRequiredIDs(Company company, String paymentTemplateCd, boolean meetRequirements) {
        PayrollServices.beginUnitOfWork();
        for (CompanyAgency companyAgency : Application.refresh(company).getCompanyAgencyCollection()) {
            for (CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate : companyAgency.getCompanyAgencyPaymentTemplateCollection()) {
                if (paymentTemplateCd == null || companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd().equals(paymentTemplateCd)) {
                    for (CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod :
                            companyAgencyPaymentTemplate.getCompanyPaymentTemplatePaymentMethodCollection()
                                    .find(CompanyPaymentTemplatePaymentMethod.PaymentMethod().in(PaymentMethod.ACHCredit, PaymentMethod.EDI))) {
                        for (PaymentMethodRequirement paymentMethodRequirement : companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplatePaymentMethod(companyPaymentTemplatePaymentMethod.getPaymentMethod()).getPaymentMethodRequirementCollection()) {
                            if (paymentMethodRequirement instanceof AgencyIdRequirement) {
                                AgencyIdRequirement aidRequirement = (AgencyIdRequirement) paymentMethodRequirement;
                                String idToUse;
                                if (!meetRequirements) {
                                    idToUse = null;
                                } else if (StringUtils.isEmpty(aidRequirement.getExample())) {
                                    idToUse = "122456" ;
                                } else {

                                    if (companyAgencyPaymentTemplate.getPaymentTemplate() != null && "MN-DEED1-PAYMENT".equals(companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd())) {
                                        idToUse = aidRequirement.getExample().split("or")[0].trim();
                                    } else {
                                        idToUse = aidRequirement.getExample();
                                    }
                                }
                                if (aidRequirement.getPaymentTemplateAgencyId() == null) {
                                    CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgency);
                                    companyAgencyDTO.getCompanyAgencyPaymentTemplate(companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd()).setAgencyTaxpayerId(idToUse);
                                    assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), companyAgency.getAgency().getAgencyId(), companyAgencyDTO));
                                } else {
                                    assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(
                                            company.getSourceSystemCd(),
                                            company.getSourceCompanyId(),
                                            new AgencyIdDTO(companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd(),
                                                    aidRequirement.getPaymentTemplateAgencyId().getName(),
                                                    idToUse)));
                                }
                            }
                        }

                    }
                }
            }
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        for (CompanyAgency companyAgency : Application.refresh(company).getCompanyAgencyCollection()) {
            for (CompanyLaw companyLaw : companyAgency.getCompanyLawCollection()) {
                CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(companyLaw);
                PaymentTemplate paymentTemplate = companyLaw.getLaw().getPaymentTemplate();
                CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate =
                        assertOne(companyAgency.getCompanyAgencyPaymentTemplateCollection()
                                               .find(CompanyAgencyPaymentTemplate.PaymentTemplate().equalTo(paymentTemplate)));
                companyLawDTO.getQBDTPayrollItemInfoDTO().setAgencyId(companyAgencyPaymentTemplate.getAgencyTaxpayerId());
                PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO);
            }
        }
        PayrollServices.commitUnitOfWork();
    }

    public static void updateAgencyTaxpayerId(Company company, String paymentTemplateCd, String agencyTaxpayerId) {
        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, paymentTemplate.getAgency().getAgencyId());
        CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgency);
        companyAgencyDTO.getCompanyAgencyPaymentTemplate(paymentTemplateCd).setAgencyTaxpayerId(agencyTaxpayerId);
        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), companyAgency.getAgency().getAgencyId(), companyAgencyDTO));
        PayrollServices.commitUnitOfWork();
    }

    public static void addAdditionalFilingAmounts(Company company) {
        addAdditionalFilingAmounts(company, null, 12.34, null, false);
    }

    public static void updateAdditionalFilingAmount(Company company, double amount, String name, int year, int quarter) {
        PayrollServices.beginUnitOfWork();

        CompanyFilingAmountDTO companyFilingAmountDTO = new CompanyFilingAmountDTO();
        companyFilingAmountDTO.setAmount(amount);
        companyFilingAmountDTO.setEffectiveDate(new DateDTO(CalendarUtils.getFirstDayOfQuarter(year, quarter)));
        companyFilingAmountDTO.setName(name);

        assertSuccess(PayrollServices.companyManager.addOrUpdateCompanyFilingAmount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyFilingAmountDTO));

        PayrollServices.commitUnitOfWork();
    }

    public static void addAdditionalFilingAmounts(Company company, String pAgencyId, double pRate, SpcfCalendar pEffectivateDate, boolean inValidateIfFound) {
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        DomainEntitySet<CompanyAgency> companyAgencyCollection = company.getCompanyAgencyCollection();
        if(pAgencyId != null) {
            companyAgencyCollection = companyAgencyCollection.find(CompanyAgency.Agency().AgencyId().equalTo(pAgencyId));
        }
        for (CompanyAgency companyAgency : companyAgencyCollection) {
            CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgency);
            for (CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate : companyAgency.getCompanyAgencyPaymentTemplateCollection()) {
                for (AdditionalFilingAmount additionalFilingAmount : companyAgencyPaymentTemplate.getPaymentTemplate().getAdditionalFilingAmountCollection()) {
                    CompanyFilingAmountDTO filingAmountDTO = new CompanyFilingAmountDTO();
                    if(inValidateIfFound) {
                        CompanyFilingAmount companyFilingAmount = Application.find(CompanyFilingAmount.class, CompanyFilingAmount.CompanyAgencyPaymentTemplate().equalTo(companyAgencyPaymentTemplate)
                                                                                                                               .And(CompanyFilingAmount.Name().equalTo(additionalFilingAmount.getName()))
                                                                                                                               .And(CompanyFilingAmount.InvalidDate().isNull())).getFirst();
                        if(companyFilingAmount != null) {
                            filingAmountDTO.setId(companyFilingAmount.getId());
                        }
                    }

                    filingAmountDTO.setName(additionalFilingAmount.getName());
                    filingAmountDTO.setAmount(pRate);
                    if(pEffectivateDate == null) {
                        filingAmountDTO.setEffectiveDate(new DateDTO(CalendarUtils.getFirstDayOfQuarter(PSPDate.getPSPTime())));
                    } else {
                        filingAmountDTO.setEffectiveDate(new DateDTO(pEffectivateDate));
                    }

                    companyAgencyDTO.getCompanyAgencyPaymentTemplate(companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd()).getCompanyFilingAmountDTOs().add(filingAmountDTO);
                }
            }
            assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), companyAgency.getAgency().getAgencyId(), companyAgencyDTO));
        }

        PayrollServices.commitUnitOfWork();

    }

    public static void createSecondStandardOffload() {
        PayrollServices.beginUnitOfWork();
        SpcfCalendar today = PSPDate.getPSPTime();
        today.setValues(today.getYear(), today.getMonth(), today.getDay(), 19, 0, 0, 0);
        OffloadGroup group = OffloadGroup.findStandardOffloadGroup();
        group.createSecondOffload(today);
        PayrollServices.commitUnitOfWork();
    }

    public static void runOffload(Company pCompany, int pYear, int pMonth, int pDay) {
        runOffload(pCompany, SpcfCalendar.createInstance(pYear, pMonth, pDay, SpcfTimeZone.getLocalTimeZone()));
    }

    public static void runOffload(Company pCompany, SpcfCalendar pOffloadDate) {
        PayrollServices.beginUnitOfWork();
        pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        pOffloadDate = pCompany.getOffloadGroup().getCalendarForCutoffTime(pOffloadDate);
        PayrollServices.rollbackUnitOfWork();
        runOffload(pOffloadDate);
    }

    public static void runEmailGateway() {
        EmailGateway emailGateway = new EmailGateway();
        emailGateway.processCompanyEventsForEmail();
    }

    public static void runOffload() {
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
    }

    public static void runOffload(SpcfCalendar pOffloadDate) {
        setPSPDate(pOffloadDate);
        runOffload();
    }

    public static void runOffloadTaxPayments(SpcfCalendar pOffloadDate) {
        setPSPDate(pOffloadDate);
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, null, ACHFileType.Tax);
    }

    public static void runACHTransactionProcessor() {
        runACHTransactionProcessor(null);
    }

    public static void runACHTransactionProcessor(Integer pNumberOfDaysToMoveForward) {

        if(pNumberOfDaysToMoveForward == null) {
            System.out.println("runACHTransactionProcessor will add 7 days to the psp date");
            pNumberOfDaysToMoveForward = 7;
        }

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions post = new ProcessACHTransactions();
        SpcfCalendar postDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(postDate, pNumberOfDaysToMoveForward);
        PSPDate.setPSPTime(postDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        post.process(PSPDate.getPSPTime());
        PayrollServices.commitUnitOfWork();
    }

    public static void setPSPDate(SpcfCalendar pDate) {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(pDate);
        PayrollServices.commitUnitOfWork();
    }

    public static void setPSPDate(int pYear, int pMonth, int pDay) {
        setPSPDate(SpcfCalendar.createInstance(pYear, pMonth, pDay, SpcfTimeZone.getLocalTimeZone()));
    }

    public static ProcessResult<List<Employee>> persistEmployees(SourceSystemCode pSourceSystem, String psid, List<EmployeeDTO> employees) {

        if (employees == null)
            return new ProcessResult<List<Employee>>();

        return persistEmployees(pSourceSystem, psid, employees.toArray(new EmployeeDTO[]{}));
    }

    private static ProcessResult<List<Employee>> persistEmployees(SourceSystemCode pSourceSystem, String psid, EmployeeDTO... employees) {
        ProcessResult<List<Employee>> pr = new ProcessResult<List<Employee>>();

        PayrollServices.beginUnitOfWork();
        List<Employee> addedEEs = new ArrayList<Employee>();

        for (EmployeeDTO employee : employees) {
            ProcessResult<Employee> addEmployeePR = PayrollServices.employeeManager.addEmployee(pSourceSystem, psid, employee);
            addedEEs.add(addEmployeePR.getResult());
            pr.merge(addEmployeePR);
        }

        PayrollServices.commitUnitOfWork();

        pr.setResult(addedEEs);
        return pr;
    }


    public static List<Employee> addEEs(Company pCompany, int count) {
        return addEEs(pCompany, count, false, false);
    }

    public static List<Employee> addEEs(Company pCompany, int count, boolean addBankAccounts, boolean cloudEmployees) {
        return addEEs(pCompany, count, addBankAccounts, cloudEmployees, cloudEmployees);
    }

    public static List<Employee> addEEs(Company pCompany, int count, boolean addBankAccounts, boolean cloudEmployees, boolean useNumericId) {
        ProcessResult<List<Employee>> pr = persistEmployees(pCompany.getSourceSystemCd(),
                pCompany.getSourceCompanyId(),
                createEEs(count, cloudEmployees, useNumericId));
        assertSuccess("addEEs", pr);

        if (addBankAccounts) {
            for (Employee employee : pr.getResult()) {
                addEEBankAccount(pCompany, employee, BankAccountType.Checking);
                if (loadAdditionalSavingsAccount) {
                    addEEBankAccount(pCompany, employee, BankAccountType.Savings);
                }
            }
        }

        return pr.getResult();
    }

    public static Employee addEE(Company pCompany, EmployeeDTO employeeDTO) {
        return addEE(pCompany, employeeDTO, true);
    }

    public static Employee addEE(Company pCompany, EmployeeDTO employeeDTO, boolean addBankAccount) {
        List<EmployeeDTO> employeesToAdd = new ArrayList<EmployeeDTO>();
        employeesToAdd.add(employeeDTO);

        ProcessResult<List<Employee>> pr = persistEmployees(pCompany.getSourceSystemCd(),
                pCompany.getSourceCompanyId(),
                employeesToAdd);
        assertSuccess("addEE", pr);

        if (addBankAccount) {
            for (Employee employee : pr.getResult()) {
                addEEBankAccount(pCompany, employee, BankAccountType.Checking);
            }
        }

        return pr.getResult().get(0);
    }

    public static EmployeeBankAccount addEEBankAccount(Company pCompany, Employee pEmployee, BankAccountType accountType) {
        EmployeeBankAccountDTO eeBankAccountDTO = createEmployeeBankAccount(accountType);
        PayrollServices.beginUnitOfWork();
        ProcessResult<EmployeeBankAccount> procResult =
                PayrollServices.employeeManager.addEmployeeBankAccount(pCompany.getSourceSystemCd(),
                        pCompany.getSourceCompanyId(),
                        pEmployee.getSourceEmployeeId(),
                        eeBankAccountDTO);

        assertSuccess("addEmployeeBankAccount", procResult);
        PayrollServices.commitUnitOfWork();

        return procResult.getResult();
    }
    
	public static Employee addEEWithBankAccount(Company pCompany) {
		PayrollServices.beginUnitOfWork();
		EmployeeDTO employeeDTO = createEEWithBankAccount();
		ProcessResult<Employee> addEmployeePR = PayrollServices.employeeManager
				.addEmployee(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), employeeDTO);
		Employee employee = addEmployeePR.getResult();
		PayrollServices.commitUnitOfWork();
		return employee;
	}

    public static void addEEBankAccounts(Company pCompany) {
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = new DomainEntitySet<Employee>();
        for (Employee employee : Employee.findEmployees(pCompany)) {
            if (employee.getEmployeeBankAccountCollection().isEmpty()) {
                employees.add(employee);
            }
        }
        PayrollServices.rollbackUnitOfWork();

        for (Employee employee : employees) {
            addEEBankAccount(pCompany, employee, BankAccountType.Checking);
        }
    }

    public static Payee createPayeeWithBankAccount(Company pCompany) {
        PayrollServices.beginUnitOfWork();
        PayeeDTO payeeDTO = createPayee();
        ProcessResult<Payee> addPayeePR = PayrollServices
                .billPaymentManager.addOrUpdatePayee(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), payeeDTO);
        Payee payee = addPayeePR.getResult();
        assertTrue("Test Result:", addPayeePR.isSuccess());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayeeBankAccountDTO payeeBankAccountDTO = GenerateData.getPayeeBankAccountDTO("PBATest" + employeeCount);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayeeBankAccount> processResult = PayrollServices.billPaymentManager.addOrUpdatePayeeBankAccount
                (pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), payee.getSourcePayeeId(), payeeBankAccountDTO);

        // Verify that no  validation errors have been returned
        assertSuccess("addOrUpdatePayeeBankAccount", processResult);

        PayrollServices.commitUnitOfWork();
        return payee;
    }

    public static List<Payee> addPayees(Company pCompany, int count) {
        ProcessResult<List<Payee>> pr = persistPayees(pCompany.getSourceSystemCd(),
                pCompany.getSourceCompanyId(),
                createPayees(count));
        assertSuccess("addPayees", pr);

        for (Payee payee : pr.getResult()) {
            addPayeeBankAccount(pCompany, payee, BankAccountType.Checking);
        }

        return pr.getResult();
    }

    private static ProcessResult<List<Payee>> persistPayees(SourceSystemCode pSourceSystem, String psid, List<PayeeDTO> pPayees) {

        if (pPayees == null)
            return new ProcessResult<List<Payee>>();

        return persistPayees(pSourceSystem, psid, pPayees.toArray(new PayeeDTO[]{}));
    }

    private static ProcessResult<List<Payee>> persistPayees(SourceSystemCode pSourceSystem, String psid, PayeeDTO... payees) {
        ProcessResult<List<Payee>> pr = new ProcessResult<List<Payee>>();

        PayrollServices.beginUnitOfWork();

        List<Payee> addedPayees = new ArrayList<Payee>();
        for (PayeeDTO payee : payees) {
            ProcessResult<Payee> addPayeePR = PayrollServices.billPaymentManager.addOrUpdatePayee(pSourceSystem, psid, payee);
            pr.merge(addPayeePR);
            addedPayees.add(addPayeePR.getResult());
        }

        PayrollServices.commitUnitOfWork();

        pr.setResult(addedPayees);
        return pr;
    }

    public static PayeeBankAccount addPayeeBankAccount(Company pCompany, Payee pPayee, BankAccountType accountType) {
        PayeeBankAccountDTO payeeBankAccountDTO = createPayeeBankAccount(pPayee, accountType);

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayeeBankAccount> pr =
                PayrollServices.billPaymentManager.addOrUpdatePayeeBankAccount(pCompany.getSourceSystemCd(),
                        pCompany.getSourceCompanyId(),
                        pPayee.getSourcePayeeId(),
                        payeeBankAccountDTO);

        assertSuccess("addEmployeeBankAccount", pr);
        PayrollServices.commitUnitOfWork();

        return pr.getResult();
    }

    public static CompanyService addDDService(Company company) {
        if(company.isCompanyOnService(ServiceCode.DirectDeposit)){
            return company.getCompanyService(ServiceCode.DirectDeposit);
        }
        return addDDService(company, 1000.00, 2000.00);
    }

    public static void cancelDDService(Company company) {
        if(company.isCompanyOnService(ServiceCode.DirectDeposit)){
            PayrollServices.companyManager.deactivateService(SourceSystemCode.QBDT, company.getSourceCompanyId(), ServiceCode.DirectDeposit);
        }
    }

    public static void terminateDDService(Company company){
        if(company.isCompanyOnService(ServiceCode.DirectDeposit)){
            PayrollServices.companyManager.terminateService(SourceSystemCode.QBDT, company.getSourceCompanyId(), ServiceCode.DirectDeposit);
        }
    }

    public static CompanyService addDDService(Company pCompany, Double avgPayrollAmt, Double highPayrollAmt) {
        String modifyLic = "";
        PayrollServices.beginUnitOfWork();
        pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        if(!pCompany.isCompanyOnService(ServiceCode.Tax)) {
            boolean hasDIYEntitlementUnit = false;
            for (EntitlementUnit entitlementUnit : pCompany.getEntitlementUnitCollection()) {
                if(entitlementUnit.getEntitlement().getEntitlementCode().isAssisted()) {
                    entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
                    Application.save(entitlementUnit);
                    modifyLic = "Y";
                } else {
                    hasDIYEntitlementUnit = true;
                    if(entitlementUnit.isDeactivated()) {
                        entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
                    }
                }
            }
            PayrollServices.commitUnitOfWork();

            if(!hasDIYEntitlementUnit) {
                addEntitlementUnit(pCompany, LIC_PREFIX + pCompany.getSourceCompanyId() + modifyLic, EOC_PREFIX + pCompany.getSourceCompanyId());
            }
        } else {
            PayrollServices.rollbackUnitOfWork();
        }

        // add the service
        DDServiceInfoDTO ddCompanyService = new DDServiceInfoDTO();

        ddCompanyService.setAveragePayrollAmount(new BigDecimal(avgPayrollAmt));
        ddCompanyService.setHighAnnualPayrollAmount(new BigDecimal(highPayrollAmt));

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> addServicePR = null;
        CompanyService ddService = pCompany.getCompanyService(ServiceCode.DirectDeposit);
        if (ddService != null && ddService.isCancelTerm()) {
            addServicePR = PayrollServices.companyManager.reactivateService(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), ServiceCode.DirectDeposit);
            assertSuccess("addDDService", addServicePR);
            addServicePR = PayrollServices.companyManager.updateService(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), ddCompanyService);
        } else {
            addServicePR = PayrollServices.companyManager.addService(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), ddCompanyService);
        }

        assertSuccess("addDDService", addServicePR);
        PayrollServices.commitUnitOfWork();

        return addServicePR.getResult();
    }

    public static CompanyService addBillPaymentService(Company pCompany) {
        ServiceInfoDTO billPaymentService = new ServiceInfoDTO();

        billPaymentService.setServiceCode(ServiceCode.BillPayment);
        FundingModel fundingModel = Application.findById(FundingModel.class, FundingModel.Codes.TWO_DAY);
        billPaymentService.setFundingModel(fundingModel);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> pr = PayrollServices.companyManager.addService(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), billPaymentService);
        assertSuccess("addBillPaymentService", pr);
        PayrollServices.commitUnitOfWork();

        return pr.getResult();

    }

    public static CompanyService addWorkersCompService(Company pCompany) {
        ServiceInfoDTO workersCompService = new ServiceInfoDTO();
        workersCompService.setServiceCode(ServiceCode.WorkersComp);
        workersCompService.setServiceStartDate(PSPDate.getPSPTime());
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> processResult = PayrollServices.companyManager.addService(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), workersCompService);
        PayrollServices.commitUnitOfWork();

        return processResult.getResult();
    }

    public static CompanyService addViewMyPaycheckService(Company pCompany) {
        ServiceInfoDTO viewMyPaycheckService = new ServiceInfoDTO();
        viewMyPaycheckService.setServiceCode(ServiceCode.ViewMyPaycheck);
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> processResult = PayrollServices.companyManager.addService(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), viewMyPaycheckService);
        PayrollServices.commitUnitOfWork();
        return processResult.getResult();
    }

    public static CompanyService add401kService(Company pCompany) {
        return add401kService(pCompany, PSPDate.getPSPTime());
    }

    public static CompanyService add401kService(Company pCompany, SpcfCalendar pServiceStartDate) {

        String custodialId = pCompany.getSourceCompanyId();
        if (custodialId.length() > 9)
            custodialId = custodialId.substring(0, 9);
        else if (custodialId.length() < 9)
            custodialId = custodialId + "000000000".substring(0, 9 - custodialId.length());

        ThirdParty401kServiceInfoDTO tp401kCompanyService = createThirdParty401kServiceInfo(custodialId,
                false,
                pServiceStartDate.copy());

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> pr = PayrollServices.companyManager.addService(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), tp401kCompanyService);

        assertSuccess("add401KService", pr);
        PayrollServices.commitUnitOfWork();

        return pr.getResult();
    }

    public static CompanyService addCloudService(Company pCompany) {

        ServiceInfoDTO serviceInfoDTO = createCloudServiceInfo();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> pr = PayrollServices.companyManager.addService(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), serviceInfoDTO);

        assertSuccess("addCloudService", pr);
        PayrollServices.commitUnitOfWork();

        return pr.getResult();
    }

    public static CompanyService addCheckDistributionService(Company pCompany) {
        PayrollServices.beginUnitOfWork();

        ServiceInfoDTO serviceInfoDTO = createCheckDistributionServiceInfoDTO(5L);

        ProcessResult<CompanyService> pr = PayrollServices.companyManager.addService(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), serviceInfoDTO);

        assertSuccess("addCheckDistributionService", pr);
        PayrollServices.commitUnitOfWork();

        return pr.getResult();
    }

    public static void cancelService(Company pCompany, ServiceCode pServiceCode) {
        //Remove company On Hold reasons if any, to cancel the service
        removeCompanyOnHoldReasons(pCompany);

        PayrollServices.beginUnitOfWork();
        Application.refresh(pCompany);
        assertSuccess(PayrollServices.companyManager.updateServiceStatus(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), pServiceCode, ServiceSubStatusCode.Cancelled));
        PayrollServices.commitUnitOfWork();
    }

    public static void removeCompanyOnHoldReasons(Company pCompany) {
        PayrollServices.beginUnitOfWork();
        Application.refresh(pCompany);
        for (OnHoldReason onHoldReason : pCompany.getOnHoldReasonCollection().find(OnHoldReason.ExpirationDate().isNull())) {
            assertSuccess(PayrollServices.companyManager.removeOnHoldReason(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), onHoldReason.getOnHoldReasonCd()));
        }
        PayrollServices.commitUnitOfWork();
    }
    
    public static void addCompanyOnHoldReason(Company pCompany, ServiceSubStatusCode pServiceSubStatusCode) {
        PayrollServices.beginUnitOfWork();
        Application.refresh(pCompany);
        assertSuccess(PayrollServices.companyManager.addOnHoldReason(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), pServiceSubStatusCode));
        PayrollServices.commitUnitOfWork();
        
    }

    public static void activateDDService(Company pCompany) {
        activateDDService(pCompany, false);
    }

    public static void activateDDService(Company pCompany, boolean pAlsoAddingTax) {
        CompanyBankAccount companyBankAccount = addCompanyBankAccount(pCompany);

        addCompanyPIN(pCompany, null);

        if(!pAlsoAddingTax) {
            List<Employee> employees = addEEs(pCompany, employeeCount, true, pAlsoAddingTax);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
            companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company, companyBankAccount.getSourceBankAccountId());

            for (int i = 0; i < payrollCount; i++) {
                ProcessResult<PayrollRun> submitPayrollPR =
                        PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(),
                                                                     createDDPayrollRun(company, companyBankAccount, new DateDTO(PSPDate.getPSPTime()), employees));
                assertSuccess("addFirstPayroll", submitPayrollPR);
            }

            PayrollServices.commitUnitOfWork();
        }
    }

    public static void reactivateDDService(Company pCompany) {
        Collection<Employee> employees = pCompany.getDirectDepositEmployees();
        PayrollServices.beginUnitOfWork();
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findCompanyBankAccount(pCompany, BankAccountStatus.Active);
        assertSuccess(PayrollServices.payrollManager.submitPayroll(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(),
                createDDPayrollRun(pCompany, companyBankAccount, new DateDTO(PSPDate.getPSPTime()), employees)));
        PayrollServices.commitUnitOfWork();
    }

    public static void addServices(Company company, ServiceCode... pServiceCodes) {
        if (pServiceCodes != null) {
            boolean addedDD = false;
            for (ServiceCode serviceCode : pServiceCodes) {
                switch (serviceCode) {
                    case DirectDeposit:
                        if(!addedDD) {
                            addDDService(company);
                            addedDD = true;
                        }
                        break;
                    case CheckDistribution:
                        addCheckDistributionService(company);
                        break;
                    case ThirdParty401k:
                        add401kService(company);
                        break;
                    case Tax:
                        addTaxService(company);
                        if(!addedDD) {
                            if(!company.hasService(ServiceCode.DirectDeposit)) {
                                addDDService(company);
                            } else if(!company.isCompanyOnService(ServiceCode.DirectDeposit)) {
                                company.getCompanyService(ServiceCode.DirectDeposit).setStatusCd(ServiceSubStatusCode.PendingFirstPayroll);
                            }
                            addedDD = true;
                        }
                        break;
                    case Cloud:
                        addCloudService(company);
                        break;
                    case BillPayment:
                        addBillPaymentService(company);
                        break;
                    case WorkersComp:
                        addWorkersCompService(company);
                        break;
                    case ViewMyPaycheck:
                        addViewMyPaycheckService(company);
                        break;
                }
            }

            // cloud should always be added
            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            boolean companyDoesNotHaveCloudService = company.getService(ServiceCode.Cloud) == null;
            PayrollServices.rollbackUnitOfWork();
            if (companyDoesNotHaveCloudService) {
                addCloudService(company);
            }
        }
    }

    public static void activateServices(Company company, ServiceCode... pServiceCodes) {
        if (pServiceCodes != null) {
            boolean addingTaxService = false;
            for (ServiceCode serviceCode : pServiceCodes) {
                if(serviceCode == ServiceCode.Tax) {
                    addingTaxService = true;
                    break;
                }
            }

            for (ServiceCode serviceCode : pServiceCodes) {
                switch (serviceCode) {
                    case DirectDeposit:
                        if(!addingTaxService) {
                            activateDDService(company, addingTaxService);
                        }
                        break;
                    case CheckDistribution:
                        break;
                    case ThirdParty401k:
                        activateThirdParty401kService(company);
                        break;
                    case Tax:
                        activateTaxService(company);
                        break;
                    case Cloud:
                        activateCloudService(company);
                        break;
                }
            }

            // cloud should always be activated
            activateCloudService(company);
        }
    }

    public static void activateTaxServiceExceptBalanceFile(Company pCompany) {
        addCompanyPIN(pCompany, null);
        addCompanyBankAccount(pCompany);

        updateCompanyService(pCompany, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
    }

    public static void updateCompanyService(Company pCompany, ServiceCode pServiceCode, ServiceSubStatusCode pServiceSubStatusCode) {
        PayrollServices.beginUnitOfWork();
        // this step is done manually by a rep
        assertSuccess(PayrollServices.companyManager.updateSubStatuses(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), pServiceCode,
                                                                 PayrollServices.entityFinder.find(ServiceSubStatus.class, ServiceSubStatus.ServiceSubStatusCd().equalTo(pServiceSubStatusCode))));
        PayrollServices.commitUnitOfWork();
    }

    public static void activateTaxService(Company pCompany) {
        activateTaxServiceExceptBalanceFile(pCompany);

        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        CompanyService taxService = CompanyService.findCompanyService(company, ServiceCode.Tax);

        // create company event to indicate balance file received
        CompanyEvent.createBalanceFileReceivedEvent(company, taxService);

        // this is normally done by the qbdt adapter
        taxService.updateCompanyServiceStatus(ServiceSubStatusCode.ActiveCurrent);

        PayrollServices.commitUnitOfWork();

        activateDDService(pCompany, true);
    }

    public static void activateCloudService(Company pCompany) {
        addCompanyPIN(pCompany, null);

        List<Employee> employees = addEEs(pCompany, 1, false, true);

        List<CompanyPayrollItem> companyPayrollItems = addPayrollItems(pCompany, PayrollItemCode.Compensation, PayrollItemCode.Tp401kEmployeeDeferral, PayrollItemCode.Tp401kEmployerMatch);

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> submitPayrollPR =
                PayrollServices.payrollManager.submitPayroll(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(),
                        create401kPayrollRun(employees, companyPayrollItems));
        assertSuccess("addFirstPayroll", submitPayrollPR);
        PayrollServices.commitUnitOfWork();
    }

    public static void activateThirdParty401kService(Company pCompany) {
        addCompanyPIN(pCompany, null);

        List<Employee> employees = addEEs(pCompany, 1, false, true);

        List<CompanyPayrollItem> companyPayrollItems = addPayrollItems(pCompany, PayrollItemCode.Compensation, PayrollItemCode.Tp401kEmployeeDeferral, PayrollItemCode.Tp401kEmployerMatch);

        PayrollServices.beginUnitOfWork();
        // simulate calling through QBDTWS adapter
        PspPrincipal pspPrincipal = Application.getCurrentPrincipal();
        PayrollServices.setCurrentPrincipal(SystemPrincipal.QBDTWSAdapter);
        ProcessResult<PayrollRun> submitPayrollPR =
                PayrollServices.payrollManager.submitPayroll(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(),
                        create401kPayrollRun(employees, companyPayrollItems));
        PayrollServices.setCurrentPrincipal(pspPrincipal);
        assertSuccess("addFirstPayroll", submitPayrollPR);
        PayrollServices.commitUnitOfWork();
    }

    public static List<CompanyPayrollItem> addPayrollItems(Company pCompany, PayrollItemCode... pPayrollItemCodes) {
        ArrayList<CompanyPayrollItemDTO> companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
        for (PayrollItemCode payrollItemCode : pPayrollItemCodes) {
            companyPayrollItemDTOs.add(createCompanyPayrollItem("PayrollItem_" + ++payrollItemId, payrollItemCode));
        }

        ProcessResult<List<CompanyPayrollItem>> pr = persistPayrollItems(pCompany.getSourceSystemCd(),
                pCompany.getSourceCompanyId(),
                companyPayrollItemDTOs);
        assertSuccess("addPayrollItems", pr);
        return pr.getResult();
    }

    public static ProcessResult<List<CompanyPayrollItem>> persistPayrollItems(SourceSystemCode pSourceSystem, String psid, List<CompanyPayrollItemDTO> pCompanyPayrollItems) {

        if (pCompanyPayrollItems == null)
            return new ProcessResult<List<CompanyPayrollItem>>();

        return persistPayrollItems(pSourceSystem, psid, pCompanyPayrollItems.toArray(new CompanyPayrollItemDTO[]{}));
    }

    public static CompanyPayrollItem persistPayrollItem(SourceSystemCode pSourceSystem, String psid,
                                                        String pDescription, String pTaxFormLine, PayrollItemCode pPayrollItemCode) {
        CompanyPayrollItemDTO covidItem = new CompanyPayrollItemDTO();
        covidItem.setSourcePayrollItemId(DataLoadServices.nextPayrollItemId() + "");
        covidItem.setSourcePayrollItemDescription(pDescription);
        covidItem.setTaxFormLine(pTaxFormLine);
        covidItem.setPayrollItemCode(pPayrollItemCode);
        return DataLoadServices.persistPayrollItems(pSourceSystem, psid, covidItem).getResult().get(0);
    }

    public static ProcessResult<List<CompanyPayrollItem>> persistPayrollItems(SourceSystemCode pSourceSystem, String psid, CompanyPayrollItemDTO... pCompanyPayrollItems) {
        ProcessResult<List<CompanyPayrollItem>> pr = new ProcessResult<List<CompanyPayrollItem>>();

        PayrollServices.beginUnitOfWork();
        List<CompanyPayrollItem> addedPayrollItems = new ArrayList<CompanyPayrollItem>();
        for (CompanyPayrollItemDTO companyPayrollItem : pCompanyPayrollItems) {
            ProcessResult<CompanyPayrollItem> companyPayrollItemProcessResult = PayrollServices.companyManager.addOrUpdateCompanyPayrollItem(pSourceSystem, psid, companyPayrollItem);
            assertSuccess(companyPayrollItemProcessResult);
            addedPayrollItems.add(companyPayrollItemProcessResult.getResult());
            pr.merge(companyPayrollItemProcessResult);
        }
        PayrollServices.commitUnitOfWork();

        pr.setResult(addedPayrollItems);
        return pr;
    }

    public static List<CompanyLaw> addFederalTaxCompanyLaws(Company pCompany) {
        return addCompanyLaws(pCompany, "66", "61", "62", "63", "64", "65", "143", "1");
    }

    public static List<CompanyLaw> addFederalAndPAStateTaxCompanyLaws(Company pCompany) {
        return addCompanyLaws(pCompany, "40", "61", "62", "63", "64", "65", "143", "1");
    }
    public static List<CompanyLaw> addFederalAndMNStateTaxCompanyLaws(Company pCompany) {
        return addCompanyLaws(pCompany,"106","158", "25", "61", "62", "63", "64", "65", "143", "1");
    }

    public static List<CompanyLaw> addFederalAndMEStateTaxCompanyLaws(Company pCompany) {
        return addCompanyLaws(pCompany, "23", "104", "61", "62", "63", "64", "65", "1");
    }

    public static List<CompanyLaw> addFederalILAndMEStateTaxCompanyLaws(Company pCompany) {
        return addCompanyLaws(pCompany, "23", "104", "61", "62", "63", "64", "65", "1","97");
    }

    public static List<CompanyLaw> addFederalAndCAStateTaxCompanyLaws(Company pCompany) {
        return addCompanyLaws(pCompany, "6", "67", "87", "142", "61", "62", "63", "64", "65", "66", "143", "1");
    }
    public static List<CompanyLaw> addFederalAndNYStateTaxCompanyLaws(Company pCompany) {
        return addCompanyLaws(pCompany, "117", "152", "36", "54","56","197", "61", "62", "63", "64", "65", "66", "143", "1");
    }

    public static PaymentTemplate getStatePaymentTemplate(String state, PaymentTemplateCategory pCategory ) {
        return getStatePaymentTemplate(state,pCategory,null);
    }

    public static PaymentTemplate getStatePaymentTemplate(String state, PaymentTemplateCategory pCategory, String paymentTemplateCd) {
        PaymentTemplate paymentTemplate = null;
        PayrollServices.beginUnitOfWork();
        if ("NY".equals(state) && pCategory == PaymentTemplateCategory.Withholding) {
            state = state + "-1MN"; // To make it we find and return NY-1MN-PAYMENT template, to have same unit test case set up as before adding 'NY-MTA305-PAYMENT' template as Withholding
        } else if ("NM".equals(state) && pCategory == PaymentTemplateCategory.SUI) {
            state = state + "-ES903A"; // To make it we find and return NM-ES903A-PAYMENT template, as this is the SUI payment template
        } else if ("MA".equals(state) && pCategory == PaymentTemplateCategory.Withholding) {
            state = state + "-M941"; // To make it we find and return NM-ES903A-PAYMENT template, as this is the SUI payment template
        } else if ("CT".equals(state) && pCategory == PaymentTemplateCategory.SUI) {
            state = state + "-2MAG"; // CT-2MAG-PAYMENT alone has ACHDebit payment method among CT paymentemplates
        }

        DomainEntitySet<PaymentTemplate> paymentTemplates = Application.find(PaymentTemplate.class, PaymentTemplate.Category().equalTo(pCategory).And(PaymentTemplate.PaymentTemplateCd().like(state + "-%")));

        //existing logic - if specific template is not requested, return the first payment template which meets the conditions
        if(Objects.isNull(paymentTemplateCd)) {
            paymentTemplate = paymentTemplates.size() > 0 ? paymentTemplates.get(0) : null;
        } else { //when specific payment template is requested look for the template in the list
            for (PaymentTemplate paymentTemplateIter : paymentTemplates) {
                if (paymentTemplateIter.getPaymentTemplateCd().equals(paymentTemplateCd)) {
                    paymentTemplate = paymentTemplateIter;
                    break;
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();
        return paymentTemplate;
    }

    public static DomainEntitySet<PaymentTemplate> getAllStatePaymentTemplates(String state, PaymentTemplateCategory pCategory) {
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<PaymentTemplate> paymentTemplates = Application.find(PaymentTemplate.class, PaymentTemplate.Category().equalTo(pCategory).And(PaymentTemplate.PaymentTemplateCd().like(state + "-%")));
        PayrollServices.rollbackUnitOfWork();
        return paymentTemplates;
    }

    public static ArrayList<String> getAllStateLawIds(String state) {
        ArrayList<String> lawIds = new ArrayList<String>();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<PaymentTemplate> paymentTemplates = Application.find(PaymentTemplate.class, PaymentTemplate.PaymentTemplateCd().like(state + "-%"));
        for (PaymentTemplate paymentTemplate : paymentTemplates) {
            for (Law law : paymentTemplate.getLawCollection()) {
                lawIds.add(law.getLawId());
            }
        }
        PayrollServices.rollbackUnitOfWork();
        return lawIds;
    }

    public static ArrayList<String> getAllStateLawIdsWithNoCalcuations(String state) {
        ArrayList<String> lawIds = new ArrayList<String>();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<PaymentTemplate> paymentTemplates = Application.find(PaymentTemplate.class, PaymentTemplate.PaymentTemplateCd().like(state + "-%"));
        for (PaymentTemplate paymentTemplate : paymentTemplates) {
            for (Law law : paymentTemplate.getLawCollection()) {
                if(!law.getPaymentTemplate().getNoCalculation()) {
                    lawIds.add(law.getLawId());
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();
        return lawIds;
    }

    public static List<CompanyLaw> addCompanyLawsWithAgencyId(String agencyId, Company pCompany, String state) {
        ArrayList<String> lawIds = getAllStateLawIds(state);
        if (agencyId != null) {
            return addCompanyLawsWithAgencyId(agencyId, pCompany, lawIds.toArray(new String[]{}));
        } else {
            return addCompanyLaws(pCompany, lawIds.toArray(new String[]{}));
        }

    }

    public static List<CompanyLaw> addCompanyLawsWithAgencyId(String agencyId, Company pCompany, String state,ArrayList<String> lawIds) {
        if (agencyId != null) {
            return addCompanyLawsWithAgencyId(agencyId, pCompany, lawIds.toArray(new String[]{}));
        } else {
            return addCompanyLaws(pCompany, lawIds.toArray(new String[]{}));
        }

    }

    public static List<CompanyLaw> addCOBRACompanyLaw(Company pCompany) {
        return addCompanyLaws(pCompany, "196");
    }


    public static List<CompanyLaw> addCompanyLawsWithAgencyId(String agencyId, Company pCompany, String... pLawIds) {
        ArrayList<CompanyLawDTO> companyLawDTOs = new ArrayList<CompanyLawDTO>();
        for (String pLawId : pLawIds) {
            CompanyLawDTO companyLawDTO = createCompanyLawDTO(++payrollItemId + "", pLawId);
            companyLawDTO.getQBDTPayrollItemInfoDTO().setAgencyId(agencyId);
            companyLawDTOs.add(companyLawDTO);
        }

        ProcessResult<List<CompanyLaw>> pr = persistCompanyLaws(pCompany.getSourceSystemCd(),
                pCompany.getSourceCompanyId(),
                companyLawDTOs.toArray(new CompanyLawDTO[]{}));
        assertSuccess("add Company Laws", pr);
        return pr.getResult();
    }

    public static List<CompanyLaw> addCompanyLaws(Company pCompany, String... pLawIds) {
        ArrayList<CompanyLawDTO> companyLawDTOs = new ArrayList<CompanyLawDTO>();
        for (String pLawId : pLawIds) {
            CompanyLawDTO companyLawDTO = createCompanyLawDTO(++payrollItemId + "", pLawId);
            companyLawDTO.getQBDTPayrollItemInfoDTO().setAgencyId(pCompany.getSourceCompanyId() + pCompany.getFedTaxId());
            companyLawDTOs.add(companyLawDTO);
        }

        ProcessResult<List<CompanyLaw>> pr = persistCompanyLaws(pCompany.getSourceSystemCd(),
                pCompany.getSourceCompanyId(),
                companyLawDTOs.toArray(new CompanyLawDTO[]{}));
        assertSuccess("add Company Laws", pr);
        return pr.getResult();
    }

    /**
     * This method changes the filingStatus of the CompanyLaw with the LawId pLawId to Inactive.
     * @param pCompany
     * @param pLawId
     */
    public static void updateCompanyLawFilingFlag(Company pCompany, String pLawId){
        PayrollServices.beginUnitOfWork();
        CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(CompanyLaw.findCompanyLaw(pCompany, pLawId));
        companyLawDTO.setFilingStatus(PayrollItemStatus.Inactive);
        PayrollServices.commitUnitOfWork();
        ProcessResult<List<CompanyLaw>> pr = persistCompanyLaws(pCompany.getSourceSystemCd(),
                                                                pCompany.getSourceCompanyId(),
                                                                companyLawDTO);
        assertSuccess("modified Company Laws", pr);
    }

    public static List<CompanyLaw> addCompanyLaws_177(Company pCompany, String... pSourceLawIds) {
        ArrayList<CompanyLawDTO> companyLawDTOs = new ArrayList<CompanyLawDTO>();
        for (String pSourceLawId : pSourceLawIds) {
            CompanyLawDTO companyLawDTO = createCompanyLawDTO(pSourceLawId, "177", "Source Id "+pSourceLawId);
            companyLawDTO.getQBDTPayrollItemInfoDTO().setAgencyId(pCompany.getSourceCompanyId() + pCompany.getFedTaxId());
            companyLawDTOs.add(companyLawDTO);
        }

        ProcessResult<List<CompanyLaw>> pr = persistCompanyLaws(pCompany.getSourceSystemCd(),
                                                                pCompany.getSourceCompanyId(),
                                                                companyLawDTOs.toArray(new CompanyLawDTO[]{}));
        assertSuccess("add Company Laws - 177", pr);
        return pr.getResult();
    }

    public static void changeCompanyLawRates(Company pCompany,String lawId,double rateInDecimal) {
        if(pCompany ==null || lawId ==null || lawId.length()==0){
            return ;
        }
        PayrollServices.beginUnitOfWork();
        Application.refresh(pCompany);
        CompanyLaw companyLaw=   CompanyLaw.findCompanyLaw(pCompany,lawId);

                CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(companyLaw);
                CompanyLawRateDTO companyLawRateDTO = new CompanyLawRateDTO();
                companyLawRateDTO.setEffectiveDate(new DateDTO(CalendarUtils.getFirstDayOfQuarter(PSPDate.getPSPTime())));
                companyLawRateDTO.setRate(rateInDecimal);
                companyLawDTO.getRateDTOs().add(companyLawRateDTO);
                assertSuccess(PayrollServices.companyManager.addOrUpdateCompanyLaw(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), companyLawDTO));
        PayrollServices.commitUnitOfWork();
    }
    public static void addCompanyLawRates(Company pCompany) {
        PayrollServices.beginUnitOfWork();
        Application.refresh(pCompany);
        for (CompanyAgency companyAgency : pCompany.getCompanyAgencyCollection()) {
            for (CompanyLaw companyLaw : companyAgency.getCompanyLawCollection()) {
                CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(companyLaw);
                CompanyLawRateDTO companyLawRateDTO = new CompanyLawRateDTO();
                companyLawRateDTO.setEffectiveDate(new DateDTO(CalendarUtils.getFirstDayOfQuarter(PSPDate.getPSPTime())));
                companyLawRateDTO.setRate(0.03);
                companyLawDTO.getRateDTOs().add(companyLawRateDTO);
                assertSuccess(PayrollServices.companyManager.addOrUpdateCompanyLaw(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), companyLawDTO));
            }
        }
        PayrollServices.commitUnitOfWork();
    }
    public static void addCompanyLawRates(Company pCompany, EffectiveRate... rates) {
        addCompanyLawRates(pCompany, null, rates);
    }

    public static void addCompanyLawRates(Company pCompany, String lawId, EffectiveRate... rates) {
        PayrollServices.beginUnitOfWork();
        Application.refresh(pCompany);
        for (CompanyAgency companyAgency : pCompany.getCompanyAgencyCollection()) {
            for (CompanyLaw companyLaw : companyAgency.getCompanyLawCollection()) {
                if (lawId == null || companyLaw.getLaw().getLawId().equals(lawId)) {
                    CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(companyLaw);
                    companyLawDTO.getRateDTOs().clear();
                    for (int i = 0; i < rates.length; i++) {
                        EffectiveRate rate = rates[i];

                        CompanyLawRateDTO companyLawRateDTO = new CompanyLawRateDTO();
                        companyLawRateDTO.setEffectiveDate(new DateDTO(rate.effectiveDate));
                        companyLawRateDTO.setRate(rate.rate);
                        companyLawDTO.getRateDTOs().add(companyLawRateDTO);
                    }
                    assertSuccess(PayrollServices.companyManager.addOrUpdateCompanyLaw(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), companyLawDTO));
                }
            }
        }
        PayrollServices.commitUnitOfWork();
    }

    public static void setNextPaycheckIdToHigherValue(int pPaycheckId) {
        paycheckId = pPaycheckId;
    }

    public static class EffectiveRate {
        public SpcfCalendar effectiveDate;
        public double rate;
        public EffectiveRate(SpcfCalendar pEffectiveDate, double pRate) {
            effectiveDate = pEffectiveDate;
            rate = pRate;
        }
    }


    private static ProcessResult<List<CompanyLaw>> persistCompanyLaws(SourceSystemCode pSourceSystem, String psid, CompanyLawDTO... pCompanyLawDTOs) {
        ProcessResult<List<CompanyLaw>> pr = new ProcessResult<List<CompanyLaw>>();

        PayrollServices.beginUnitOfWork();
        List<CompanyLaw> addedCompanyLaws = new ArrayList<CompanyLaw>();
        for (CompanyLawDTO companyLawDTO : pCompanyLawDTOs) {
            ProcessResult<CompanyLaw> lawProcessResult = PayrollServices.companyManager.addOrUpdateCompanyLaw(pSourceSystem, psid.trim(), companyLawDTO);
            addedCompanyLaws.add(lawProcessResult.getResult());
            pr.merge(lawProcessResult);
        }
        PayrollServices.commitUnitOfWork();

        pr.setResult(addedCompanyLaws);
        return pr;
    }

    ////////////////
    // DTO creation
    ////////////////

    public static ThirdParty401kServiceInfoDTO createThirdParty401kServiceInfo() {
        return createThirdParty401kServiceInfo("100000000", false, PSPDate.getPSPTime());
    }

    public static ThirdParty401kServiceInfoDTO createThirdParty401kServiceInfo(String custodialId, boolean isSafeHarbor, SpcfCalendar startDate) {
        ThirdParty401kServiceInfoDTO tp401kCompanyService = new ThirdParty401kServiceInfoDTO();
        tp401kCompanyService.setCustodialId(custodialId);
        tp401kCompanyService.setHasSafeHarbor(isSafeHarbor);
        CalendarUtils.clearTime(startDate);
        tp401kCompanyService.setServiceStartDate(startDate);
        return tp401kCompanyService;
    }

    public static CheckDistributionServiceInfoDTO createCheckDistributionServiceInfoDTO(Long pLastPaycheckId) {
        CheckDistributionServiceInfoDTO checkDistributionServiceInfoDTO = new CheckDistributionServiceInfoDTO();
        checkDistributionServiceInfoDTO.setLastPaycheckId(pLastPaycheckId);
        return checkDistributionServiceInfoDTO;
    }


    public static ServiceInfoDTO createCloudServiceInfo() {
        ServiceInfoDTO serviceInfoDTO = new ServiceInfoDTO();
        serviceInfoDTO.setServiceCode(ServiceCode.Cloud);
        return serviceInfoDTO;
    }

    public static DDServiceInfoDTO createDDServiceInfo() {
        DDServiceInfoDTO ddServiceInfoDTO = new DDServiceInfoDTO();
        ddServiceInfoDTO.setAveragePayrollAmount(new BigDecimal(10000));
        ddServiceInfoDTO.setHighAnnualPayrollAmount(new BigDecimal(10000));
        ddServiceInfoDTO.setServiceCode(ServiceCode.DirectDeposit);
        return ddServiceInfoDTO;
    }

    public static CompanyDTO createCompany(SourceSystemCode pSourceSystem, String pPSID) {
        return createCompany(pSourceSystem, pPSID, null);
    }

    public static CompanyDTO createCompany(SourceSystemCode pSourceSystem, String pPSID, String pFEIN) {
        CompanyDTO companyDTO = new CompanyDTO();

        ++fedTaxId;

        companyDTO.setCompanyId(pPSID);
        companyDTO.setSourceSystemCd(pSourceSystem);
        if (pFEIN == null) {
            companyDTO.setFein(String.format("%1$09d", fedTaxId));
        } else {
            companyDTO.setFein(pFEIN);
        }
        companyDTO.setLegalName("TEST_COMPANY_" + fedTaxId.toString());
        companyDTO.setNameControl(createNameControl(companyDTO.getLegalName()));
        companyDTO.setDBA(companyDTO.getLegalName());
        companyDTO.setNotificationEmail("TEST_" + pFEIN + "@COMPANY.COM");
        companyDTO.setNextEmployeeId("1");
        companyDTO.setNextPaycheckId("1");
        companyDTO.setNextPayrollItemId("1");
        companyDTO.setNextPayrollTransactionId("1");
        companyDTO.setCurrentToken(1L);

        companyDTO.setMailingAddress(createAddress("COMAIL"));
        companyDTO.setLegalAddress(createAddress("COLEGAL"));
        companyDTO.setContacts(createContacts());
        companyDTO.setPriceType("Standard");

        QuickbooksInfoDTO qbInfoDTO = new QuickbooksInfoDTO();
        qbInfoDTO.setCoaFeeAccountName(QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME);
        qbInfoDTO.setCoaSalesTaxAccountName(QBOFX.ACCOUNTS.DEFAULT_SALES_TAX_ACCOUNT_NAME);
        companyDTO.setQuickBooksInfo(qbInfoDTO);

        return companyDTO;
    }

    private static String createNameControl(String companyLegalName){

        companyLegalName = companyLegalName.toUpperCase();
        companyLegalName = companyLegalName.replaceAll("[^A-Z,^a-z,^0-9,^/&,^/s,^/-]","");
        String nameControl = null;
        if(companyLegalName.length() > 4){
            nameControl = companyLegalName.substring(0,4);
        } else {
            nameControl = companyLegalName;
        }
        return nameControl;
    }

    public static Collection<ContactDTO> createContacts() {
        Collection<ContactDTO> contacts = new ArrayList<ContactDTO>();

        // set up common contact address
        AddressDTO contactAddr = createAddress("CONTACT");

        // set up primary principal contact
        ContactDTO contact = new ContactDTO();
        contact.setFirstName("Johnny");
        contact.setMiddleName("Q");
        contact.setLastName("PrimaryPrincipal");
        contact.setPhoneNumber("(775) 111-1111");
        contact.setContactRoleCd(ContactRole.PrimaryPrincipal);
        contact.setAccountSignatory(Boolean.TRUE);
        contact.setEmail("PrimaryPrincipal@aol.com");
        contact.setTitle("Mr.");
        contact.setTitleSuffix("Jr.");
        contact.setJobTitle("Payroll Accountant 1");
        contact.setFaxNumber("(775) 101-1001");
        contact.setSecondPhoneNumber("(775) 010-0110");
        contact.setAddress(contactAddr);
        contact.setContactId(DataLoader.generateContactKey(contact));

        contacts.add(contact);

        // set up secondary principal contact
        contact = new ContactDTO();
        contact.setFirstName("Johnny");
        contact.setMiddleName("Q");
        contact.setLastName("SecondaryPrincipal");
        contact.setPhoneNumber("(775) 222-2222");
        contact.setContactRoleCd(ContactRole.SecondaryPrincipal);
        contact.setAccountSignatory(Boolean.TRUE);
        contact.setEmail("SecondaryPrincipal@aol.com");
        contact.setTitle("Mr.");
        contact.setTitleSuffix("Jr.");
        contact.setJobTitle("Payroll Accountant 2");
        contact.setFaxNumber("(775) 202-2002");
        contact.setSecondPhoneNumber("(775) 020-0220");
        contact.setAddress(contactAddr);
        contact.setContactId(DataLoader.generateContactKey(contact));

        contacts.add(contact);

        // set up payroll admin contact
        contact = new ContactDTO();
        contact.setFirstName("Johnny");
        contact.setMiddleName("Q");
        contact.setLastName("PayrollAdmin");
        contact.setPhoneNumber("(775) 333-3333");
        contact.setContactRoleCd(ContactRole.PayrollAdmin);
        contact.setAccountSignatory(Boolean.FALSE);
        contact.setEmail("PayrollAdmin@aol.com");
        contact.setTitle("Mr.");
        contact.setTitleSuffix("Jr.");
        contact.setJobTitle("Payroll Accountant 3");
        contact.setFaxNumber("(775) 303-3003");
        contact.setSecondPhoneNumber("(775) 030-0330");
        contact.setAddress(contactAddr);
        contact.setContactId(DataLoader.generateContactKey(contact));

        contacts.add(contact);

        // set up 'other' contact
        contact = new ContactDTO();
        contact.setFirstName("Johnny");
        contact.setMiddleName("Q");
        contact.setLastName("Other");
        contact.setPhoneNumber("(775) 444-4444");
        contact.setContactRoleCd(ContactRole.Other);
        contact.setAccountSignatory(Boolean.FALSE);
        contact.setEmail("Other@aol.com");
        contact.setTitle("Mr.");
        contact.setTitleSuffix("Jr.");
        contact.setJobTitle("Payroll Accountant 4");
        contact.setFaxNumber("(775) 404-4004");
        contact.setSecondPhoneNumber("(775) 040-0440");
        contact.setAddress(contactAddr);
        contact.setContactId(DataLoader.generateContactKey(contact));

        contacts.add(contact);

        return contacts;
    }

    public static CompanyBankAccountDTO createCompanyBankAccount() {
        return createCompanyBankAccount(createBankAccount(BankAccountType.Checking));
    }

    public static CompanyBankAccountDTO createCompanyBankAccount(BankAccountDTO bankAccountDTO) {
        CompanyBankAccountDTO retBA = new CompanyBankAccountDTO();
        retBA.setCompanyBankAccountID("AccountID_" + Integer.toString(++bankAccountId));
        retBA.setSourceBankAccountName("AccountName_" + Integer.toString(++bankAccountId));
        retBA.setBankAccountDTO(bankAccountDTO);
        return retBA;

    }

    public static CompanyBankAccountDTO createCompanyBankAccount(CompanyBankAccount pCompanyBankAccount) {
        CompanyBankAccountDTO companyBankAccountDTO = new CompanyBankAccountDTO();
        companyBankAccountDTO.setCompanyBankAccountID(pCompanyBankAccount.getSourceBankAccountId());
        companyBankAccountDTO.setBankAccountDTO(createBankAccountDTO(pCompanyBankAccount.getBankAccount()));
        return companyBankAccountDTO;
    }

    public static OfferingInfoDTO createDDOffering() {
        return createOffering(PayrollSubtypeCode.Standard);
    }

    public static OfferingInfoDTO createOffering(PayrollSubtypeCode pPayrollSubtype) {
        OfferingInfoDTO offeringInfoDTO;
        offeringInfoDTO = new OfferingInfoDTO();
        offeringInfoDTO.setPayrollSubTypeCd(pPayrollSubtype);
        return offeringInfoDTO;
    }

    public static List<EmployeeDTO> createEEs(int count, boolean pIsAssisted) {
        return createEEs(count, pIsAssisted, pIsAssisted);
    }

    public static List<EmployeeDTO> createEEs(int count, boolean pIsAssisted, boolean useNumericIds) {
        List<EmployeeDTO> eeDTOs = new ArrayList<EmployeeDTO>(count);
        for (int i = 0; i < count; i++) {
            eeDTOs.add(createEE(useNumericIds ? ++employeeId : null, pIsAssisted));
        }
        return eeDTOs;
    }

    public static EmployeeDTO createEE() {
        return createEE(null, false);
    }

    public static EmployeeDTO createEE(Integer pEmployeeId, boolean pIsAssisted) {
        return   createEE( pEmployeeId,  pIsAssisted,true,true);
    }
    public static EmployeeDTO createEE(Integer pEmployeeId, boolean pIsAssisted,boolean isEEHaveFirstName,boolean isEEHaveLastName) {
        EmployeeDTO eeDTO = new EmployeeDTO();
        Integer eeID = pEmployeeId != null ? pEmployeeId : ++employeeId;
        if(isEEHaveFirstName){
            eeDTO.setFirstName("First_" + eeID.toString());
        }
        eeDTO.setMiddleName("M_" + eeID.toString());
        if(isEEHaveLastName){
            eeDTO.setLastName("Last_" + eeID.toString());
        }

        if (pIsAssisted) {
            eeDTO.setEmployeeId(eeID.toString());
            eeDTO.setQBDTEmployeeInfoDTO(new QBDTEmployeeInfoDTO());
            eeDTO.getQBDTEmployeeInfoDTO().setIsAssisted(true);
        } else {
            eeDTO.setEmployeeId(eeDTO.getFirstName() + " " + eeDTO.getMiddleName() + " " + eeDTO.getLastName());
            if(pEmployeeId != null) {
                // newer qbdt employees for DIY will have a numeric id and qbdt employee info
                eeDTO.setEmployeeId(eeID.toString());
                eeDTO.setQBDTEmployeeInfoDTO(new QBDTEmployeeInfoDTO());
            }
        }
        if(eeDTO.getQBDTEmployeeInfoDTO() != null) {
            eeDTO.getQBDTEmployeeInfoDTO().setListId(eeDTO.getEmployeeId());
        }

        eeDTO.setWorkState("NJ");
        eeDTO.setFedAllowances(0);
        eeDTO.setHasRetirementPlan(false);

        WagePlanDTO wagePlanDTO = new WagePlanDTO();
        wagePlanDTO.setDomainCode(WagePlanDomainCode.WorkOrLiveState);
        wagePlanDTO.setName(WagePlanNameCode.WPC);
        wagePlanDTO.setState("NJ");
        wagePlanDTO.setWagePlanValue("??");
        eeDTO.getWagePlanDTOs().add(wagePlanDTO);

        eeDTO.setGender(Gender.Female);
        DateDTO hireDate = new DateDTO(PSPDate.getPSPTime());
        eeDTO.setHireDate(hireDate);
        DateDTO birthDate = new DateDTO("1975-08-08");
        eeDTO.setBirthDate(birthDate);
        eeDTO.setLiveAddress(createAddress("EE" + eeID.toString()));
        eeDTO.setEmail("EE" + eeID.toString() + "@intuit.com");

        String ssn = eeID.toString();
        if (ssn.length() > 4)
            ssn = ssn.substring(0, 4);
        else
            ssn = String.format("%1$04d", eeID);

        eeDTO.setSocialSecurityNumber("99909" + ssn);


        return eeDTO;
    }

    public static EmployeeDTO createEEWithBankAccount() {
        EmployeeDTO employeeDTO = createEE(1, true);
        EmployeeBankAccountDTO bankAccountDTO = createEmployeeBankAccount(BankAccountType.Savings);
		employeeDTO.setEmployeeBankAccountDTOs(Arrays.asList(bankAccountDTO));
        return employeeDTO;
    }

    public static Employee getLastCreatedEmployee(Company pCompany) {
        Employee cloudEmployee = Employee.findEmployee(pCompany, employeeId.toString());
        if (cloudEmployee != null) {
            return cloudEmployee;
        }
        return Employee.findEmployees(pCompany).findEntity(Employee.FirstName().equalTo("First_" + employeeId));
    }

    public static List<PayeeDTO> createPayees(int count) {
        List<PayeeDTO> payeeDTOs = new ArrayList<PayeeDTO>(count);
        for (int i = 0; i < count; i++) {
            payeeDTOs.add(createPayee());
        }
        return payeeDTOs;
    }

    public static PayeeDTO createPayee() {
        return createPayee(-1);
    }

    public static PayeeDTO createPayee(Integer pPayeeId) {
        PayeeDTO payeeDTO = new PayeeDTO();

        Integer id = pPayeeId > -1 ? pPayeeId : ++employeeId;
        payeeDTO.setSourcePayeeId(id.toString());
        payeeDTO.setName("Name_" + id.toString());
        payeeDTO.setEmail("Email_" + id.toString() + "@intuit.com");
        payeeDTO.setMailingAddress(createAddress("Payee" + id.toString()));

        return payeeDTO;
    }

    public static PayeeDTO createPayee(Payee pPayee) {
        DTOFactory dtoFactory = new DTOFactory();

        PayeeDTO payeeDTO = new PayeeDTO();

        payeeDTO.setSourcePayeeId(pPayee.getSourcePayeeId());
        payeeDTO.setName(pPayee.getName());
        payeeDTO.setEmail(pPayee.getEmail());
        payeeDTO.setMailingAddress(dtoFactory.create(pPayee.getMailingAddress()));
        payeeDTO.setIs1099(pPayee.getIs1099());
        payeeDTO.setPhone(pPayee.getPhone());
        payeeDTO.setTaxId(pPayee.getTaxId());

        return payeeDTO;
    }

    public static AddressDTO createAddress() {
        return createAddress("");
    }

    public static AddressDTO createAddress(String linePrefix) {
        if (linePrefix != null && linePrefix.length() > 0)
            linePrefix += "_";

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressLine1(linePrefix + "AddressLine1");
        addressDTO.setAddressLine2(linePrefix + "AddressLine2");
        addressDTO.setAddressLine3(linePrefix + "AddressLine3");
        addressDTO.setCity("Ridgewood");
        addressDTO.setState("NJ");
        addressDTO.setCountry("USA");
        addressDTO.setZipCode("07450");
        addressDTO.setZipCodeExtension("4444");

        return addressDTO;
    }

    public static BankAccountDTO createBankAccount() {
        return createBankAccount(BankAccountType.Checking);
    }

    public static BankAccountDTO createBankAccount(BankAccountType accountType) {
        return createBankAccount(accountType, "111000025", "ACCNT_" + Integer.toString(++bankAccountId));
    }

    public static BankAccountDTO createBankAccount(BankAccountType accountType, String routingNumber, String accountNumber) {
        return createBankAccount(accountType, routingNumber, accountNumber, "TestBank_" + Integer.toString(++bankAccountId));
    }

    public static BankAccountDTO createBankAccount(BankAccountType accountType, String routingNumber, String accountNumber, String bankName) {
        BankAccountDTO ba = new BankAccountDTO();
        ba.setAccountNumber(accountNumber);
        ba.setRoutingNumber(routingNumber);
        ba.setBankName(bankName);
        ba.setAccountType(accountType);
        return ba;
    }

    public static EmployeeBankAccountDTO createEmployeeBankAccount(BankAccountType accountType) {
        BankAccountDTO ba = createBankAccount(accountType);

        EmployeeBankAccountDTO eeba = new EmployeeBankAccountDTO();
        eeba.setBankAccount(ba);

        return eeba;
    }

    public static EmployeeBankAccountDTO createEmployeeBankAccount(Employee pEmployee, BankAccountDTO ba) {
        EmployeeBankAccountDTO eeba = new EmployeeBankAccountDTO();
        eeba.setEmployeeBankAccountId(pEmployee.getSourceEmployeeId());
        eeba.setBankAccount(ba);
        return eeba;
    }

    public static EmployeeBankAccountDTO createEmployeeBankAccount(EmployeeBankAccount pEmployeeBankAccount) {
        EmployeeBankAccountDTO employeeBankAccountDTO = new EmployeeBankAccountDTO();
        employeeBankAccountDTO.setBankAccount(createBankAccountDTO(pEmployeeBankAccount.getBankAccount()));
        employeeBankAccountDTO.setEmployeeBankAccountId(pEmployeeBankAccount.getSourceBankAccountId());
        return employeeBankAccountDTO;
    }

    public static PayeeBankAccountDTO createPayeeBankAccount(Payee pPayee, BankAccountType accountType) {
        BankAccountDTO ba = createBankAccount(accountType);

        PayeeBankAccountDTO payeeBankAccountDTO = new PayeeBankAccountDTO();
        payeeBankAccountDTO.setPayeeBankAccountId(pPayee.getSourcePayeeId());
        payeeBankAccountDTO.setBankAccount(ba);

        return payeeBankAccountDTO;
    }

    public static PayeeBankAccountDTO createPayeeBankAccount(PayeeBankAccount pPayeeBankAccount) {
        DTOFactory dtoFactory = new DTOFactory();
        BankAccountDTO bankAccountDTO = dtoFactory.create(pPayeeBankAccount.getBankAccount());

        PayeeBankAccountDTO payeeBankAccountDTO = new PayeeBankAccountDTO();
        payeeBankAccountDTO.setPayeeBankAccountId(pPayeeBankAccount.getPayee().getSourcePayeeId());
        payeeBankAccountDTO.setBankAccount(bankAccountDTO);

        return payeeBankAccountDTO;
    }

    public static PayrollRunDTO createDDPayrollRun(Company pCompany, DateDTO payrollDate) {
        DomainEntitySet<Employee> employees = Application.find(Employee.class, Employee.Company().equalTo(pCompany));
        return createDDPayrollRun(pCompany, CompanyBankAccount.findActiveCompanyBankAccount(pCompany), payrollDate, employees);
    }

    public static PayrollRunDTO createDDPayrollRun(Company pCompany, DateDTO payrollDate, Collection<Employee> pEmployees) {
        return createDDPayrollRun(pCompany, CompanyBankAccount.findActiveCompanyBankAccount(pCompany), payrollDate, pEmployees);
    }

    public static PayrollRunDTO createDDPayrollRun(Company pCompany, CompanyBankAccount pCompanyBankAccount,
                                                   DateDTO payrollDate, Collection<Employee> pEmployees) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        if (pCompanyBankAccount != null) {
            CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccount(pCompanyBankAccount);
            companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
            payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        }

        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
        payrollRunDTO.setPayrollTXBatchId("Batch_" + ++payrollBatchId);

        Collection<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();
        for (Employee employee : pEmployees) {
            employee = Employee.findEmployee(pCompany, employee.getSourceEmployeeId());
            // Create Paycheck splits
            Collection<DDTransactionDTO> ddTransactions = new ArrayList<DDTransactionDTO>();
            DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = employee.getEmployeeBankAccountCollection();
            for (int i = 0; i < employeeBankAccounts.size(); i++) {
                if (employeeBankAccounts.get(i).getStatusCd().equals(BankAccountStatus.Active)) {
                    ddTransactions.add(createDDTransactionDTO(createEmployeeBankAccount(employeeBankAccounts.get(i)), new BigDecimal(i + 1)));
                }
            }

            // Create Paycheck
            paychecks.add(createPaycheckDTO(ddTransactions, employee.getSourceEmployeeId(), SpcfUniqueId.generateRandomUniqueIdString()));
        }

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public static BankAccountDTO createBankAccountDTO(BankAccount pBankAccount) {
        BankAccountDTO bankAccountDTO = new BankAccountDTO();
        bankAccountDTO.setAccountNumber(pBankAccount.getAccountNumber());
        bankAccountDTO.setRoutingNumber(pBankAccount.getRoutingNumber());
        bankAccountDTO.setAccountType(BankAccountType.valueOf(pBankAccount.getAccountTypeCd().toString()));
        bankAccountDTO.setBankName(pBankAccount.getBankName());
        return bankAccountDTO;
    }

    public static ServiceBankAccountDTO createServiceBankAccountDTO(CompanyBankAccountDTO pCompanyBankAccountDTO, ServiceCode pServiceCode) {
        ServiceBankAccountDTO serviceBankAccountDTO = new ServiceBankAccountDTO();
        serviceBankAccountDTO.setCompanyBankAccount(pCompanyBankAccountDTO);
        serviceBankAccountDTO.setServiceCode(pServiceCode);
        return serviceBankAccountDTO;
    }

    public static DDTransactionDTO createDDTransactionDTO(EmployeeBankAccountDTO pEmployeeBankAccountDTO, BigDecimal pTransactionAmount) {
        DDTransactionDTO ddTransactionDTO = new DDTransactionDTO();
        ddTransactionDTO.setDDTransactionAmount(pTransactionAmount);
        ddTransactionDTO.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
        ddTransactionDTO.setEmployeeBankAccount(pEmployeeBankAccountDTO);
        return ddTransactionDTO;
    }

    public static PaycheckDTO createPaycheckDTO(Collection<DDTransactionDTO> pDDTransactions, String pEmployeeId, String pPaycheckId) {
        PaycheckDTO paycheckDTO = new PaycheckDTO();
        paycheckDTO.setDdTransactions((List<DDTransactionDTO>) pDDTransactions);
        paycheckDTO.setEmployeeId(pEmployeeId);
        paycheckDTO.setPaycheckId(pPaycheckId);
        SpcfMoney totalPaycheckNetAmount = new SpcfMoney();
        for (DDTransactionDTO currDDTxn : pDDTransactions) {
            SpcfMoney currAmount = new SpcfMoney(currDDTxn.getDDTransactionAmount().toString());
            totalPaycheckNetAmount = (SpcfMoney) totalPaycheckNetAmount.add(currAmount);
        }

        if (pDDTransactions == null || pDDTransactions.size() == 0) {
            totalPaycheckNetAmount = new SpcfMoney("300.00");
        }

        paycheckDTO.setPaycheckNetAmount(totalPaycheckNetAmount);
        return paycheckDTO;
    }

    public static CompensationTransactionDTO createCompensationTransaction(String payrollItemId) {
        return createCompensationTransaction(payrollItemId, SpcfDecimal.createInstance(1), new SpcfMoney("1"));
    }

    public static CompensationTransactionDTO createCompensationTransaction(String payrollItemId, SpcfDecimal hoursWorked, SpcfMoney compensationAmount) {
        CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
        compensationTransactionDTO.setSourcePayrollItemId(payrollItemId);
        compensationTransactionDTO.setHoursWorked(hoursWorked);
        compensationTransactionDTO.setCompensationAmount(compensationAmount);
        return compensationTransactionDTO;
    }

    public static DeductionTransactionDTO createDeductionTransaction(String payrollItemId) {
        DeductionTransactionDTO deductionTransactionDTO = new DeductionTransactionDTO();
        deductionTransactionDTO.setSourcePayrollItemId(payrollItemId);
        deductionTransactionDTO.setDeductionAmount(new BigDecimal(1));
        deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal(1));
        return deductionTransactionDTO;
    }

    public static EmployerContributionTransactionDTO createEmployerContributionTransaction(String payrollItemId) {
        return createEmployerContributionTransaction(payrollItemId, new BigDecimal(1), new BigDecimal(1), new BigDecimal(1), new BigDecimal(1));
    }

    public static EmployerContributionTransactionDTO createEmployerContributionTransaction(String payrollItemId,
                                                                                           BigDecimal pContributionAmount, BigDecimal pContributionAmountYTD,
                                                                                           BigDecimal pTaxableWagesAmount, BigDecimal pTotalWagesAmount) {
        EmployerContributionTransactionDTO employerContributionTransactionDTO = new EmployerContributionTransactionDTO();
        employerContributionTransactionDTO.setSourcePayrollItemId(payrollItemId);
        employerContributionTransactionDTO.setContributionAmount(pContributionAmount);
        employerContributionTransactionDTO.setContributionYTDAmount(pContributionAmountYTD);
        employerContributionTransactionDTO.setTaxableWagesAmount(pTaxableWagesAmount);
        employerContributionTransactionDTO.setTotalWagesAmount(pTotalWagesAmount);
        return employerContributionTransactionDTO;
    }

    public static PaycheckDTO createPaycheck(Employee pEmployee, List<CompanyPayrollItem> pCompanyPayrollItems) {
        PaycheckDTO paycheckDTO = new PaycheckDTO();
        paycheckDTO.setPaycheckId(Integer.toString(++paycheckId));

        ArrayList<CompensationTransactionDTO> compensationTransactions = new ArrayList<CompensationTransactionDTO>();
        ArrayList<DeductionTransactionDTO> deductionTransactions = new ArrayList<DeductionTransactionDTO>();
        ArrayList<EmployerContributionTransactionDTO> employerContributionTransactions = new ArrayList<EmployerContributionTransactionDTO>();

        for (CompanyPayrollItem companyPayrollItem : pCompanyPayrollItems) {
            switch (companyPayrollItem.getPayrollItem().getPayrollItemCode()) {
                case Compensation:
                    compensationTransactions.add(createCompensationTransaction(companyPayrollItem.getSourcePayrollItemId()));
                    break;
                case Tp401kEmployeeDeferral:
                    deductionTransactions.add(createDeductionTransaction(companyPayrollItem.getSourcePayrollItemId()));
                    break;
                case Tp401kEmployerMatch:
                    employerContributionTransactions.add(createEmployerContributionTransaction(companyPayrollItem.getSourcePayrollItemId()));
                    break;
            }
        }

        paycheckDTO.setCompensationTransactions(compensationTransactions);
        paycheckDTO.setDeductionTransactions(deductionTransactions);
        paycheckDTO.setEmployerContributionTransactions(employerContributionTransactions);

        paycheckDTO.setEmployeeId(pEmployee.getSourceEmployeeId());
        SpcfDecimal compensationAmount = new SpcfMoney("0.00");
        for (CompensationTransactionDTO compensationTransaction : compensationTransactions) {
            compensationAmount = compensationAmount.add(compensationTransaction.getCompensationAmount());
        }
        paycheckDTO.setPaycheckGrossAmount(new SpcfMoney(compensationAmount));

        SpcfDecimal deductionAmount = new SpcfMoney("0.00");
        for (DeductionTransactionDTO deductionTransaction : deductionTransactions) {
            deductionAmount = deductionAmount.add(SpcfUtils.convertToSpcfMoney(deductionTransaction.getDeductionAmount()));
        }

        paycheckDTO.setPaycheckNetAmount(new SpcfMoney(compensationAmount.subtract(deductionAmount)));

        SpcfCalendar periodBeginDate = PSPDate.getPSPTime();
        periodBeginDate.addDays(-7);
        paycheckDTO.setPayPeriodBeginDate(new DateDTO(periodBeginDate));

        SpcfCalendar periodEndDate = PSPDate.getPSPTime();
        periodEndDate.addDays(-2);
        paycheckDTO.setPayPeriodEndDate(new DateDTO(periodEndDate));

        return paycheckDTO;
    }

    public static PayrollRunDTO create401kPayrollRun(List<Employee> pEmployees, List<CompanyPayrollItem> pCompanyPayrollItems) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        SpcfCalendar payrollDate = PSPDate.getPSPTime();
        payrollDate.addDays(2);
        payrollRunDTO.setTargetPayrollTXDate(new DateDTO(payrollDate));
        payrollRunDTO.setPayrollTXBatchId("Batch_" + ++payrollBatchId);

        Collection<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();
        for (Employee employee : pEmployees) {
            paychecks.add(createPaycheck(employee, pCompanyPayrollItems));
        }
        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
    }

    public static Collection<BillPaymentDTO> createBPPayrollRun(Company pCompany, List<Payee> pPayees) {
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>(pPayees.size());

        SpcfCalendar payrollDate = PSPDate.getPSPTime();
        payrollDate.addDays(2);
        for (Payee payee : pPayees) {
            BillPaymentDTO billPaymentDTO = new BillPaymentDTO();
            billPaymentDTO.setBillPaymentId("Payment" + ++billPaymentId);
            billPaymentDTO.setDepositDate(new DateDTO(payrollDate));
            billPaymentDTO.setPayeeDTO(createPayee(payee));
            DomainEntitySet<PayeeBankAccount> payeeBankAccounts = PayeeBankAccount.findPayeeBankAccounts(pCompany, payee.getSourcePayeeId());
            Collection<BillPaymentSplitDTO> billPaymentSplitDTOs = new ArrayList<BillPaymentSplitDTO>(payeeBankAccounts.size());
            for (PayeeBankAccount payeeBankAccount : payeeBankAccounts) {
                BillPaymentSplitDTO billPaymentSplitDTO = new BillPaymentSplitDTO();
                billPaymentSplitDTO.setAmount(new BigDecimal(1));
                billPaymentSplitDTO.setBillPaymentSplitId("Split" + ++billPaymentSplitId);
                billPaymentSplitDTO.setPayeeBankAccount(createPayeeBankAccount(payeeBankAccount));
                billPaymentSplitDTOs.add(billPaymentSplitDTO);
            }
            billPaymentDTO.setPaymentTransactions(billPaymentSplitDTOs);
            billPaymentDTO.setAmount(new SpcfMoney(Integer.toString(billPaymentSplitDTOs.size())));
            billPaymentDTOs.add(billPaymentDTO);
        }

        return billPaymentDTOs;
    }

    public static CompanyPayrollItemDTO createCompanyPayrollItem(String pId, PayrollItemCode pPayrollItemCode) {
        CompanyPayrollItemDTO companyPayrollItemDTO = new CompanyPayrollItemDTO();
        companyPayrollItemDTO.setPayrollItemCode(pPayrollItemCode);
        companyPayrollItemDTO.setSourcePayrollItemDescription("My description!");
        companyPayrollItemDTO.setSourcePayrollItemId(pId);
        return companyPayrollItemDTO;
    }

    public static CompanyLawDTO createCompanyLawDTO(String pId, String pLawId) {
        CompanyLawDTO companyLawDTO = new CompanyLawDTO();
        companyLawDTO.setLawId(pLawId);
        companyLawDTO.setSourceDescription("A law");
        companyLawDTO.setSourceId(pId);
        companyLawDTO.setStatus(PayrollItemStatus.Active);
        QBDTPayrollItemInfoDTO QBDTPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
        QBDTPayrollItemInfoDTO.setExpenseAccount("Expense Account " + pLawId);
        companyLawDTO.setQBDTPayrollItemInfoDTO(QBDTPayrollItemInfoDTO);
        return companyLawDTO;
    }

    public static CompanyLawDTO createCompanyLawDTO(String pSourceId, String pLawId, String pSourceDescription) {
        CompanyLawDTO companyLawDTO = new CompanyLawDTO();
        companyLawDTO.setLawId(pLawId);
        companyLawDTO.setSourceDescription(pSourceDescription);
        companyLawDTO.setSourceId(pSourceId);
        companyLawDTO.setStatus(PayrollItemStatus.Active);
        QBDTPayrollItemInfoDTO QBDTPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
        QBDTPayrollItemInfoDTO.setExpenseAccount("Expense Account " + pLawId);
        companyLawDTO.setQBDTPayrollItemInfoDTO(QBDTPayrollItemInfoDTO);
        return companyLawDTO;
    }

    public static LiabilityAdjustmentDTO createLiabilityAdjustmentDTO(String pLawId, String pPayrollItemId, DateDTO pEffectiveDate) {
        return createLiabilityAdjustmentDTO(pLawId, pPayrollItemId, null, pEffectiveDate);
    }

    public static LiabilityAdjustmentDTO createLiabilityAdjustmentDTO(String pLawId, String pPayrollItemId, String pEmployeeId, DateDTO pEffectiveDate) {

        return createLiabilityAdjustmentDTO(pLawId, pPayrollItemId, pEmployeeId, pEffectiveDate,
                new SpcfMoney("27.00"), new SpcfMoney("1200.00"), new SpcfMoney("1500.00"), false);
    }


    public static LiabilityAdjustmentDTO createLiabilityAdjustmentDTO(String pLawId, String pPayrollItemId, String pEmployeeId, DateDTO pEffectiveDate,
                                                                      SpcfMoney pAmount, SpcfMoney pTaxableWages, SpcfMoney pTotalWages, boolean pIsReconcilingAdjustment) {
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = new LiabilityAdjustmentDTO();
        liabilityAdjustmentDTO.setAmount(pAmount);
        liabilityAdjustmentDTO.setTaxableWages(pTaxableWages);
        liabilityAdjustmentDTO.setTotalWages(pTotalWages);
        liabilityAdjustmentDTO.setLawId(pLawId);
        liabilityAdjustmentDTO.setPayrollItemId(pPayrollItemId);
        liabilityAdjustmentDTO.setSourceEmployeeId(pEmployeeId);
        liabilityAdjustmentDTO.setEffectiveDate(pEffectiveDate);
        liabilityAdjustmentDTO.setReconcilingAdjustment(pIsReconcilingAdjustment);
        return liabilityAdjustmentDTO;
    }

    public static QBDTPayrollTransactionLineDTO createQBDTPayrollTransactionLineDTO(SpcfMoney pAmount, SpcfMoney pTaxableWageAmount, SpcfMoney pWageBaseAmount, String pPayrollItemId) {
    	QBDTPayrollTransactionLineDTO qbdtPayrollTransactionLineDTO = new QBDTPayrollTransactionLineDTO();
    	qbdtPayrollTransactionLineDTO.setAmount(pAmount);
		qbdtPayrollTransactionLineDTO.setTaxableWageAmount(pTaxableWageAmount);
		qbdtPayrollTransactionLineDTO.setWageBaseAmount(pWageBaseAmount);
		qbdtPayrollTransactionLineDTO.setPayrollItemId(pPayrollItemId);
        return qbdtPayrollTransactionLineDTO;
    }

    public static QBDTPayrollTransactionDTO createQBDTPayrollTransactionDTO(String pEmployeeSourceId, SpcfCalendar pPeriodEndDate) {
    	QBDTPayrollTransactionDTO qbdtPayrollTransactionDTO = new QBDTPayrollTransactionDTO();
		qbdtPayrollTransactionDTO.setEmployeeSourceId(pEmployeeSourceId);
		qbdtPayrollTransactionDTO.setPeriodEndDate(pPeriodEndDate);
        return qbdtPayrollTransactionDTO;
    }

    public static CompanyAdjustmentSubmissionDTO createCompanyAdjustmentSubmissionDTO(String pSourceId, DateDTO pSubmissionDate) {
        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = new CompanyAdjustmentSubmissionDTO();
        companyAdjustmentSubmissionDTO.setSourceId(pSourceId);
        companyAdjustmentSubmissionDTO.setSubmissionDate(pSubmissionDate);
        return companyAdjustmentSubmissionDTO;
    }

    public static QBDTTransactionInfoDTO createQBDTTransactionInfoDTO(String pId) {
        QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
        qbdtTransactionInfoDTO.setAccountName("AcctName" + pId);
        qbdtTransactionInfoDTO.setAgencyName("AgencyName" + pId);
        qbdtTransactionInfoDTO.setCleared("1");
        qbdtTransactionInfoDTO.setMemo("Memo" + pId);
        qbdtTransactionInfoDTO.setOnService(true);
        qbdtTransactionInfoDTO.setReferenceNumber("Ref" + pId);
        qbdtTransactionInfoDTO.setTrackingClass("TC" + pId);

        return qbdtTransactionInfoDTO;
    }

    public static void updateSourcePayrollParameter(SourceSystemCode sourceSystem, SourcePayrollParameterCode code, String value) {
        PayrollServices.beginUnitOfWork();
        List<SourcePayrollParameterDTO> updatedParamList = new ArrayList<SourcePayrollParameterDTO>();
        updatedParamList.add(new SourcePayrollParameterDTO(sourceSystem, code, value));
        assertSuccess(PayrollServices.payrollManager.updateSourcePayrollParameter(sourceSystem, updatedParamList));
        PayrollServices.commitUnitOfWork();
    }

    public static PayrollRunDTO createPayrollRunWithQBInfo(Company pCompany, DateDTO payrollDate, List<Employee> pEmployees) {
        return createPayrollRunWithQBInfo(pCompany, CompanyBankAccount.findActiveCompanyBankAccount(pCompany), payrollDate, pEmployees);
    }

    public static PayrollRunDTO createPayrollRunWithQBInfo(Company pCompany, CompanyBankAccount pCompanyBankAccount,
                                                           DateDTO payrollDate, List<Employee> pEmployees) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        if (pCompanyBankAccount != null) {
            CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccount(pCompanyBankAccount);
            companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.Tax));
            companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
            payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        }

        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
        payrollRunDTO.setPayrollTXBatchId("Batch_" + ++payrollBatchId);

        Collection<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();
        for (Employee employee : pEmployees) {
            employee = Employee.findEmployee(pCompany, employee.getSourceEmployeeId());
            // Create Paycheck splits
            Collection<DDTransactionDTO> ddTransactions = new ArrayList<DDTransactionDTO>();
            DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = employee.getEmployeeBankAccountCollection();
            for (int i = 0; i < employeeBankAccounts.size(); i++) {
                if (employeeBankAccounts.get(i).getStatusCd().equals(BankAccountStatus.Active)) {
                    ddTransactions.add(createDDTransactionDTO(createEmployeeBankAccount(employeeBankAccounts.get(i)), new BigDecimal(i + 1)));
                }
            }

            // Create Paycheck
            PaycheckDTO paycheckDTO = createPaycheckDTO(ddTransactions, employee.getSourceEmployeeId(), SpcfUniqueId.generateRandomUniqueIdString());
            QBDTPaycheckInfoDTO qbdtPaycheckInfoDTO = new QBDTPaycheckInfoDTO();
            qbdtPaycheckInfoDTO.setAccountName("AcctName" + paycheckDTO.getEmployeeId());
            qbdtPaycheckInfoDTO.setCheckNumber("ChkNumber");
            qbdtPaycheckInfoDTO.setCleared("1");
            qbdtPaycheckInfoDTO.setMemo("Memo" + paycheckDTO.getEmployeeId());
            qbdtPaycheckInfoDTO.setOnService(true);
            qbdtPaycheckInfoDTO.setProrate(true);
            qbdtPaycheckInfoDTO.setTrackingClass("TC");

            paycheckDTO.setQBDTPaycheckInfoDTO(qbdtPaycheckInfoDTO);
            paychecks.add(paycheckDTO);
        }

        payrollRunDTO.setPaychecks(paychecks);

        return payrollRunDTO;
    }

    public static CompanyService addTaxService(Company pCompany) {
        String modifyLic = "";
        PayrollServices.beginUnitOfWork();
        pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        boolean hasAssistedEntitlementUnit = false;
        for (EntitlementUnit entitlementUnit : pCompany.getEntitlementUnitCollection()) {
            if(!entitlementUnit.getEntitlement().getEntitlementCode().isAssisted()) {
                entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
                Application.save(entitlementUnit);
                modifyLic = "Y";
            } else {
                hasAssistedEntitlementUnit = true;
                if(entitlementUnit.isDeactivated()) {
                    entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
                }
            }
        }
        PayrollServices.commitUnitOfWork();

        if(!hasAssistedEntitlementUnit) {
            addAssistedEntitlementUnit(pCompany, LIC_PREFIX + pCompany.getSourceCompanyId() + modifyLic, EOC_PREFIX + pCompany.getSourceCompanyId(), true);
        }

        TaxServiceInfoDTO taxServiceInfoDTO = new TaxServiceInfoDTO();
        taxServiceInfoDTO.setServiceStartDate(PSPDate.getPSPTime());
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> pr = PayrollServices.companyManager.addService(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), taxServiceInfoDTO);
        assertSuccess("addTaxService", pr);
        PayrollServices.commitUnitOfWork();

        return pr.getResult();
    }

    public static void addAssistedBankAccounts(Company pCompany, PayrollRunDTO pPayrollRunDTO) {

        CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(pCompany);

        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        if (companyBankAccount != null) {
            CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccount(companyBankAccount);
            pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
            for (CompanyService companyService : pCompany.getCompanyServiceCollection().find(CompanyService.Service().ServiceCd().in(ServiceCode.Tax, ServiceCode.DirectDeposit))) {
                companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, companyService.getService().getServiceCd()));
            }
            pPayrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        }
    }

    public static List<String> getCompanyLawsIds(Company pCompany) {
        Application.beginUnitOfWork();
        List<String> lawIds = new ArrayList<String>();
        for (CompanyAgency companyAgency : Application.refresh(pCompany).getCompanyAgencyCollection()) {
            for (CompanyLaw companyLaw : companyAgency.getCompanyLawCollection()) {
                //don't add dead laws
                if (!companyLaw.getLaw().shouldExcludeFromUI()) {
                    lawIds.add(companyLaw.getLaw().getLawId());
                }
            }

        }
        Application.rollbackUnitOfWork();
        return lawIds;
    }

    public static PayrollRunDTO createPayrollRunWithLawsAndAmounts(PayrollRunDTO pPayrollRunDTO, Company pCompany, DateDTO payrollDate, List<Employee> pEmployees, String[] pLawIds, String[] pAmounts) {

        PayrollSubmitDataLoader payrollSubmitDataLoader = new PayrollSubmitDataLoader();
        pPayrollRunDTO.setTargetPayrollTXDate(payrollDate);
        pPayrollRunDTO.setPayrollTXBatchId("Batch_" + ++payrollBatchId);

        Collection<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();
        for (Employee employee : pEmployees) {
            employee = Employee.findEmployee(pCompany, employee.getSourceEmployeeId());
            // Create Paycheck splits
            Collection<DDTransactionDTO> ddTransactions = new ArrayList<DDTransactionDTO>();
            DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = employee.getEmployeeBankAccountCollection();
            for (int i = 0; i < employeeBankAccounts.size(); i++) {
                if (employeeBankAccounts.get(i).getStatusCd().equals(BankAccountStatus.Active)) {
                    ddTransactions.add(createDDTransactionDTO(createEmployeeBankAccount(employeeBankAccounts.get(i)), new BigDecimal(i + 1)));
                }
            }

            // Create Paycheck
            PaycheckDTO paycheckDTO = createPaycheckDTO(ddTransactions, employee.getSourceEmployeeId(), SpcfUniqueId.generateRandomUniqueIdString());
            QBDTPaycheckInfoDTO qbdtPaycheckInfoDTO = new QBDTPaycheckInfoDTO();
            qbdtPaycheckInfoDTO.setAccountName("AcctName" + paycheckDTO.getEmployeeId());
            qbdtPaycheckInfoDTO.setCheckNumber("ChkNumber");
            qbdtPaycheckInfoDTO.setCleared("1");
            qbdtPaycheckInfoDTO.setMemo("Memo" + paycheckDTO.getEmployeeId());
            qbdtPaycheckInfoDTO.setOnService(true);
            qbdtPaycheckInfoDTO.setProrate(true);
            qbdtPaycheckInfoDTO.setTrackingClass("TC");

            paycheckDTO.setQBDTPaycheckInfoDTO(qbdtPaycheckInfoDTO);
            paychecks.add(paycheckDTO);
            Collection<LiabilityTransactionDTO> liabilityTransactions = new ArrayList<LiabilityTransactionDTO>();

            for (int i = 0; i < pLawIds.length; i++) {
                LiabilityTransactionDTO liabilityTxDTO = payrollSubmitDataLoader.createLiabilityTransactionDTO(paycheckDTO);
                liabilityTxDTO.setLiabilityAmount(new BigDecimal(pAmounts[i]));
                liabilityTxDTO.setLiabilityTaxableWages(new BigDecimal(pAmounts[i]).multiply(new BigDecimal("10"))); //simulate this tax is 10% of taxable wages
                liabilityTxDTO.setLawId(pLawIds[i]);
                liabilityTransactions.add(liabilityTxDTO);
                liabilityTxDTO.setPayrollItemId(CompanyLaw.findCompanyLaw(pCompany, pLawIds[i]).getSourceId());
            }
            paycheckDTO.setLiabilityTransactions(liabilityTransactions);
        }
        pPayrollRunDTO.setPaychecks(paychecks);
        return pPayrollRunDTO;
    }


    public static PayrollRunDTO createPayrollRunWith941AndILStateTaxes(PayrollRunDTO pPayrollRunDTO, Company pCompany, DateDTO payrollDate, List<Employee> pEmployees) {

        HashMap<String, String> lawAmounts = new HashMap();
        lawAmounts.put("97", "27");
        lawAmounts.put("16", "28");
        lawAmounts.put("908", "5");
        lawAmounts.put("62", "12");
        lawAmounts.put("63", "5.5");
        lawAmounts.put("64", "45");
        lawAmounts.put("1", "25");
        return createPayrollRun(pPayrollRunDTO, pCompany, payrollDate, pEmployees, lawAmounts);
    }

    public static PayrollRunDTO createPayrollRunWith941AndMEStateTaxes(PayrollRunDTO pPayrollRunDTO, Company pCompany, DateDTO payrollDate, List<Employee> pEmployees) {

        HashMap<String, String> lawAmounts = new HashMap();
        lawAmounts.put("23", "27");
        lawAmounts.put("104", "28");
        lawAmounts.put("61", "5");
        lawAmounts.put("62", "12");
        lawAmounts.put("63", "5.5");
        lawAmounts.put("64", "45");
        lawAmounts.put("1", "25");
        return createPayrollRun(pPayrollRunDTO, pCompany, payrollDate, pEmployees, lawAmounts);
    }

    public static PayrollRunDTO createPayrollRunWith941AndPAStateTaxes(PayrollRunDTO pPayrollRunDTO, Company pCompany, DateDTO payrollDate, List<Employee> pEmployees) {

        HashMap<String, String> lawAmounts = new HashMap();
        lawAmounts.put("40", "25");
        lawAmounts.put("61", "5");
        lawAmounts.put("62", "12");
        lawAmounts.put("63", "5.5");
        lawAmounts.put("64", "45");
        lawAmounts.put("143", "2");
        lawAmounts.put("1", "25");
        return createPayrollRun(pPayrollRunDTO, pCompany, payrollDate, pEmployees, lawAmounts);
    }
    public static PayrollRunDTO createPayrollRunWith941AndNVStateTaxes(PayrollRunDTO pPayrollRunDTO, Company pCompany, DateDTO payrollDate, List<Employee> pEmployees) {
        HashMap<String, String> lawAmounts = new HashMap();
        lawAmounts.put("116", "25");
        lawAmounts.put("61", "5");
        lawAmounts.put("62", "12");
        lawAmounts.put("63", "5.5");
        lawAmounts.put("64", "45");
        lawAmounts.put("159", "2");
        lawAmounts.put("1", "25");
        return createPayrollRun(pPayrollRunDTO, pCompany, payrollDate, pEmployees, lawAmounts);
    }
    public static PayrollRunDTO createPayrollRunWith941AndMOStateTaxes(PayrollRunDTO pPayrollRunDTO, Company pCompany, DateDTO payrollDate, List<Employee> pEmployees) {
        HashMap<String, String> lawAmounts = new HashMap();
        //MO
        lawAmounts.put("26", "25");
        lawAmounts.put("107", "20");
        //IRS
        lawAmounts.put("61", "5");
        lawAmounts.put("62", "12");
        lawAmounts.put("63", "5.5");
        lawAmounts.put("64", "45");

        lawAmounts.put("1", "25");
        return createPayrollRun(pPayrollRunDTO, pCompany, payrollDate, pEmployees, lawAmounts);
    }
    public static PayrollRunDTO createPayrollRunWith941AndNVPAMAStateTaxes(PayrollRunDTO pPayrollRunDTO, Company pCompany, DateDTO payrollDate, List<Employee> pEmployees) {

        HashMap<String, String> lawAmounts = new HashMap();
        //PA
        lawAmounts.put("40", "25");
        lawAmounts.put("121", "2");
         //MA
        lawAmounts.put("150", "30");
        lawAmounts.put("102", "50");
        //NV
        lawAmounts.put("116", "25");
        lawAmounts.put("159", "2");
        //IRS
        lawAmounts.put("61", "5");
        lawAmounts.put("62", "12");
        lawAmounts.put("63", "5.5");
        lawAmounts.put("64", "45");

        lawAmounts.put("1", "25");
        return createPayrollRun(pPayrollRunDTO, pCompany, payrollDate, pEmployees, lawAmounts);
    }


    public static PayrollRunDTO createPayrollRunWith941AndMNStateTaxes(PayrollRunDTO pPayrollRunDTO, Company pCompany, DateDTO payrollDate, List<Employee> pEmployees) {

        HashMap<String, String> lawAmounts = new HashMap();
        lawAmounts.put("25", "100");
        lawAmounts.put("106", "75");
        lawAmounts.put("158", "50");
        lawAmounts.put("61", "5");
        lawAmounts.put("62", "12");
        lawAmounts.put("63", "5.5");
        lawAmounts.put("64", "45");
        lawAmounts.put("143", "2");
        lawAmounts.put("1", "25");
        return createPayrollRun(pPayrollRunDTO, pCompany, payrollDate, pEmployees, lawAmounts);
    }

    public static PayrollRunDTO createPayrollRun(PayrollRunDTO pPayrollRunDTO, Company pCompany, DateDTO payrollDate, List<Employee> pEmployees, HashMap<String, String> lawAmounts) {
        pPayrollRunDTO.setTargetPayrollTXDate(payrollDate);
        pPayrollRunDTO.setPayrollTXBatchId("Batch_" + ++payrollBatchId);
        Company company = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        if (company.isCompanyOnService(ServiceCode.Tax)) {
            pPayrollRunDTO.setIsAssisted(true);
        }

        Collection<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();
        for (Employee employee : pEmployees) {
            employee = Employee.findEmployee(pCompany, employee.getSourceEmployeeId());
            // Create Paycheck splits
            Collection<DDTransactionDTO> ddTransactions = new ArrayList<DDTransactionDTO>();
            DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = employee.getEmployeeBankAccountCollection();
            for (int i = 0; i < employeeBankAccounts.size(); i++) {
                if (employeeBankAccounts.get(i).getStatusCd().equals(BankAccountStatus.Active)) {
                    ddTransactions.add(createDDTransactionDTO(createEmployeeBankAccount(employeeBankAccounts.get(i)), new BigDecimal(i + 1)));
                }
            }

            // Create Paycheck
            PaycheckDTO paycheckDTO = createPaycheckDTO(ddTransactions, employee.getSourceEmployeeId(), SpcfUniqueId.generateRandomUniqueIdString());
            QBDTPaycheckInfoDTO qbdtPaycheckInfoDTO = new QBDTPaycheckInfoDTO();
            qbdtPaycheckInfoDTO.setAccountName("AcctName" + paycheckDTO.getEmployeeId());
            qbdtPaycheckInfoDTO.setCheckNumber("ChkNumber");
            qbdtPaycheckInfoDTO.setCleared("1");
            qbdtPaycheckInfoDTO.setMemo("Memo" + paycheckDTO.getEmployeeId());
            qbdtPaycheckInfoDTO.setOnService(true);
            qbdtPaycheckInfoDTO.setProrate(true);
            qbdtPaycheckInfoDTO.setTrackingClass("TC");

            paycheckDTO.setQBDTPaycheckInfoDTO(qbdtPaycheckInfoDTO);
            paychecks.add(paycheckDTO);
            Collection<LiabilityTransactionDTO> liabilityTransactions = new ArrayList<LiabilityTransactionDTO>();

            for (String lawId : lawAmounts.keySet()) {
                BigDecimal amount = new BigDecimal(lawAmounts.get(lawId));
                LiabilityTransactionDTO liabilityTransactionDTO = new LiabilityTransactionDTO();
                liabilityTransactionDTO.setLiabilityTaxableWages(amount.multiply(new BigDecimal("10")));
                liabilityTransactionDTO.setLiabilityTotalWages(amount.multiply(new BigDecimal("10")));
                liabilityTransactionDTO.setLiabilityTipsTaxableWages(amount.multiply(new BigDecimal("10")));
                liabilityTransactionDTO.setLiabilityAmount(new BigDecimal(lawAmounts.get(lawId)));
                liabilityTransactionDTO.setLawId(lawId);
                liabilityTransactionDTO.setPayrollItemId(CompanyLaw.findCompanyLaw(pCompany, lawId).getSourceId());
                liabilityTransactions.add(liabilityTransactionDTO);
            }
            paycheckDTO.setLiabilityTransactions(liabilityTransactions);
        }

        pPayrollRunDTO.setPaychecks(paychecks);

        return pPayrollRunDTO;
    }

    public static PayrollRunDTO setupCompanyGetPayrollRunDTO(String psid) {
        List<Employee> emps = setupCompany(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWith941AndPAStateTaxes(payrollRunDTO, company, new DateDTO("2011-01-07"), emps);
        PayrollServices.commitUnitOfWork();
        return payrollDTO;
    }

    public static List<Employee> setupCompany(String psid) {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 12, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxService(company);

        DataLoadServices.addFederalAndPAStateTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        updateEffectiveDepositFreqEffDateToQuarterly(psid, "PA-501-PAYMENT");

        return emps;
    }
    public static List<Employee> setupCompany(String psid,boolean addEEBankAccounts,boolean cloudEmployees) {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 12, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxService(company);

        DataLoadServices.addFederalAndPAStateTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 2,addEEBankAccounts,cloudEmployees);

        updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        updateEffectiveDepositFreqEffDateToQuarterly(psid, "PA-501-PAYMENT");

        return emps;
    }
    public static Company  getCompanyNoEagerLoad(String psId) {
        com.intuit.sbd.payroll.psp.domain.Company company =null;
        if (psId != null ) {
            company = com.intuit.sbd.payroll.psp.domain.Company.findCompanyNoEagerLoad(psId, SourceSystemCode.QBDT);
        }
        return company;
    }
    public static List<Employee> setupCACompany(String psid) {


        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxService(company);

        DataLoadServices.addFederalAndCAStateTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        updateEffectiveDepositFreqEffDateToQuarterly(psid,"CA-PITSDI-PAYMENT");
        updateEffectiveDepositFreqEffDateToQuarterly(psid, "CA-UIETT-PAYMENT");

        return emps;
    }

    public static List<Company> setupCompany(long pStartPsid, int pNumberOfCompanies, String[] pStatesList, PaymentTemplateCategory pCategory) {
        return setupCompany(pStartPsid, pNumberOfCompanies, pStatesList, pCategory, PaymentMethod.ACHCredit);
    }
    public static List<Company> setupCompany(long pStartPsid, int pNumberOfCompanies, String[] pStatesList, PaymentTemplateCategory pCategory, PaymentMethod pPaymentMethod) {
        for (int i = 0; i < pNumberOfCompanies; i++) {
            String psid = Long.toString(pStartPsid++);
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
            DataLoadServices.activateTaxService(company);
            DataLoadServices.addFederalTaxCompanyLaws(company);
            DataLoadServices.addEEs(company, 2);
            updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        }

        DomainEntitySet<Company> companies = setupCompanyAgency(pStatesList, pCategory, pPaymentMethod);

        return Arrays.asList(companies.toArray(new Company[]{}));
    }
    public static List<Company> setupCompanyWithRandomPsid(int pRangePsid, int pNumberOfCompanies, String[] pStatesList, PaymentTemplateCategory pCategory, PaymentMethod pPaymentMethod) {
        Random random= new   Random();
        for (int i = 0; i < pNumberOfCompanies; i++) {

            String psid = String.format("%1$06d",  random.nextInt(pRangePsid));
            String fein =  String.format("%1$09d",  random.nextInt(899999999));
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid,fein, false, ServiceCode.Tax);
            DataLoadServices.activateTaxService(company);
            DataLoadServices.addFederalTaxCompanyLaws(company);
            DataLoadServices.addEEs(company, 2);
            updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        }

        DomainEntitySet<Company> companies = setupCompanyAgency(pStatesList, pCategory, pPaymentMethod);

        return Arrays.asList(companies.toArray(new Company[]{}));
    }

    public static DomainEntitySet<Company> setupCompanyAgency(String[] pStatesList, PaymentTemplateCategory pCategory, PaymentMethod pPaymentMethod) {
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().notEqualTo(SourceSystemCode.PSP));
       ArrayList<String> paymentTemplateList = new ArrayList<String>();
        paymentTemplateList.add("VT-WH433-PAYMENT");
        paymentTemplateList.add("MN-DEED1-PAYMENT");
        paymentTemplateList.add("MO-MODES-PAYMENT");
        paymentTemplateList.add("IA-44105-PAYMENT");
        paymentTemplateList.add("VA-VA15-PAYMENT");
        paymentTemplateList.add("PA-501-PAYMENT");
        PayrollServices.rollbackUnitOfWork();
        for (Company company : companies) {
            for (String stateName : pStatesList) {
                PaymentTemplate paymentTemplate = getStatePaymentTemplate(stateName, pCategory);
                // Pull example state agency id
                PayrollServices.beginUnitOfWork();
                PaymentTemplatePaymentMethod paymentTemplatePaymentMethod = Application.find(PaymentTemplatePaymentMethod.class,
                        PaymentTemplatePaymentMethod.PaymentTemplate().equalTo(paymentTemplate)
                                .And(PaymentTemplatePaymentMethod.PaymentMethod().equalTo(pPaymentMethod))).getFirst();

                DomainEntitySet<PaymentMethodRequirement> paymentMethodRequirements = paymentTemplatePaymentMethod
                        .getPaymentMethodRequirementCollection();

                AgencyIdRequirement agencyIdRequirement = null;

                for (PaymentMethodRequirement paymentMethodRequirement : paymentMethodRequirements) {
                    if (paymentMethodRequirement instanceof AgencyIdRequirement) {
                        if (((AgencyIdRequirement) paymentMethodRequirement).getPaymentTemplateAgencyId() == null) {
                            agencyIdRequirement = (AgencyIdRequirement) paymentMethodRequirement;
                            break;
                        }
                    }
                }
                PayrollServices.rollbackUnitOfWork();

                // Figure out what format the state tax id should be
                String exampleAgencyId = null;

                if (agencyIdRequirement == null) {
                    exampleAgencyId = null;
                } else if (agencyIdRequirement.getCustomRequirement().equals(AgencyIdCustomRequirement.None) ||
                        agencyIdRequirement.getCustomRequirement().equals(AgencyIdCustomRequirement.MustNotFollowFedTaxIdSubstitueIf8Digits) ||
                        agencyIdRequirement.getCustomRequirement().equals(AgencyIdCustomRequirement.MustNotInExemptedIdList) ||
                        agencyIdRequirement.getCustomRequirement().equals(AgencyIdCustomRequirement.IFNotPatternMustFollowFedTaxId)) {
                    exampleAgencyId = agencyIdRequirement.getExample();
                } else if (agencyIdRequirement.getCustomRequirement().equals(AgencyIdCustomRequirement.Digits2Through10FollowFedTaxId)) {
                    exampleAgencyId = agencyIdRequirement.getExample();
                    exampleAgencyId = exampleAgencyId.substring(0, 1) + company.getFedTaxId() + exampleAgencyId.substring(10);
                } else if (agencyIdRequirement.getCustomRequirement().equals(AgencyIdCustomRequirement.Digits4Through12FollowFedTaxId)) {
                    exampleAgencyId = agencyIdRequirement.getExample();
                    exampleAgencyId = exampleAgencyId.substring(0, 3) + company.getFedTaxId() + exampleAgencyId.substring(12);
                } else if (agencyIdRequirement.getCustomRequirement().equals(AgencyIdCustomRequirement.MustStartWithFedTaxId)) {
                    exampleAgencyId = agencyIdRequirement.getExample();
                    if (paymentTemplate.getPaymentTemplateCd().equals("NJ-NJ927PUI-PAYMENT") || paymentTemplate.getPaymentTemplateCd().equals("NJ-NJ927PWH-PAYMENT")) {
                        exampleAgencyId = exampleAgencyId.split("or")[0].trim();
                        exampleAgencyId = company.getFedTaxId() + "/" + exampleAgencyId.substring(10);
                    } else
                        exampleAgencyId = company.getFedTaxId() + exampleAgencyId.substring(9);
                } else if ( agencyIdRequirement.getCustomRequirement().equals(AgencyIdCustomRequirement.MustFollowFedTaxId)){
                    exampleAgencyId = company.getFedTaxId();

                    if(exampleAgencyId != null && exampleAgencyId.charAt(2) != '-' && !(paymentTemplate.getPaymentTemplateCd().equals("AZ-A1-PAYMENT"))) {
                        exampleAgencyId = new StringBuffer(exampleAgencyId).insert(2, "-").toString();
                    }

                }else if(agencyIdRequirement.getCustomRequirement().equals(AgencyIdCustomRequirement.MustNotFollowFedTaxId)){
                    if(company.getFedTaxId()!= null && !company.getFedTaxId().equals(agencyIdRequirement.getExample())){
                        exampleAgencyId = agencyIdRequirement.getExample();
                    }
                }else if(agencyIdRequirement.getCustomRequirement().equals(AgencyIdCustomRequirement.Digits3Through11FollowFedTaxId)){
                    exampleAgencyId = agencyIdRequirement.getExample();
                    if (paymentTemplate.getPaymentTemplateCd().equals("VA-VA15-PAYMENT"))
                        exampleAgencyId = exampleAgencyId.substring(0, 2) + "-" + company.getFedTaxId() + exampleAgencyId.substring(12);
                    else
                        exampleAgencyId = exampleAgencyId.substring(0, 2) + company.getFedTaxId() + exampleAgencyId.substring(11);
                }
                if (exampleAgencyId != null && paymentTemplate != null && (paymentTemplateList.contains(paymentTemplate.getPaymentTemplateCd()))) {
                    exampleAgencyId = exampleAgencyId.split("or")[0].trim();
                }

                DataLoadServices.addCompanyLawsWithAgencyId(exampleAgencyId, company, stateName);
                //Enable ACH Credit payment
                PayrollServices.beginUnitOfWork();
                assertSuccess(PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), paymentTemplate.getPaymentTemplateCd(),pPaymentMethod, true));
                PayrollServices.commitUnitOfWork();

                PayrollServices.beginUnitOfWork();
                Application.refresh(paymentTemplatePaymentMethod);
                for (PaymentMethodRequirement paymentMethodRequirement : paymentTemplatePaymentMethod.getPaymentMethodRequirementCollection()) {
                    if (paymentMethodRequirement instanceof AgencyIdRequirement) {
                        if (((AgencyIdRequirement) paymentMethodRequirement).getPaymentTemplateAgencyId() != null) {
                            AgencyIdRequirement additionalIdRequirement = (AgencyIdRequirement) paymentMethodRequirement;
                            AgencyIdDTO agencyIdDTO = new AgencyIdDTO(additionalIdRequirement.getPaymentTemplateAgencyId().getPaymentTemplate().getPaymentTemplateCd(), additionalIdRequirement.getPaymentTemplateAgencyId().getName(), "12245678");
                            assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));
                        }
                    }
                }
                PayrollServices.commitUnitOfWork();


            }
        }
        return companies;
    }

    public static void runPayrollRun(Company pCompany, String[] pStatesList) {
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        runPayrollRun(pCompany, pStatesList, supportedDate, payrollDate, true);
    }

    public static void runPayrollRun(Company pCompany, String[] pStatesList, HashMap<String, String> pLawAmounts, DateDTO pPayrollDate) {
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());

        runPayrollRun(pCompany, pStatesList, supportedDate, pPayrollDate, false, pLawAmounts, PaymentTemplateCategory.Withholding);
    }

    public static void runPayrollRunNYMetro(Company pCompany, String[] pStatesList, HashMap<String, String> pLawAmounts, DateDTO pPayrollDate) {
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());

        runPayrollRunNYMetro(pCompany, pStatesList, supportedDate, pPayrollDate, false, pLawAmounts);
    }

    public static void runPayrollRun(Company pCompany, String[] pStatesList, SpcfCalendar supportedDate, DateDTO payrollDate,
                                     boolean checkIRS) {
        runPayrollRun(pCompany, pStatesList, supportedDate, payrollDate, checkIRS, new HashMap<String, String>(), PaymentTemplateCategory.Withholding);

    }

    public static void runPayrollRun(Company pCompany, String[] pStatesList, SpcfCalendar supportedDate, DateDTO payrollDate,
                                     boolean checkIRS, HashMap<String, String> pLawAmounts, PaymentTemplateCategory pCategory) {
        runPayrollRun(pCompany,pStatesList,supportedDate,payrollDate,checkIRS,pLawAmounts,pCategory,null);
    }
    public static void runPayrollRun(Company pCompany, String[] pStatesList, SpcfCalendar supportedDate, DateDTO payrollDate,
                                     boolean checkIRS, HashMap<String, String> pLawAmounts, PaymentTemplateCategory pCategory, String paymentTemplateCd) {
        HashMap<String, String> lawAmounts = new HashMap();
        if (pLawAmounts.isEmpty()) {
            lawAmounts.put("61", "6.1");
            lawAmounts.put("62", "6.2");
            lawAmounts.put("63", "6.3");
            lawAmounts.put("64", "6.4");
            lawAmounts.put("1", "25");
            lawAmounts.put("65", "6.5");
        } else {
            for (String lawId : pLawAmounts.keySet()) {
                lawAmounts.put(lawId, pLawAmounts.get(lawId));
            }
        }
        for (String state : pStatesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state,pCategory, paymentTemplateCd);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
            PayrollServices.beginUnitOfWork();
            Application.getHibernateSession().refresh(paymentTemplate);
            CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(pCompany, paymentTemplate.getAgency().getAgencyId());
            SpcfDecimal withHoldingsAmount = SpcfMoney.ZERO;
            DomainEntitySet<CompanyLaw> companyLaws = companyAgency.getCompanyLawCollection();
            for (CompanyLaw companyLaw : companyLaws) {
                if(paymentTemplate.getLawCollection().contains(companyLaw.getLaw())) { // To include only laws from individual payment template, companyAgency will have Law for the agency
                    if (pLawAmounts.containsKey(companyLaw.getLaw().getLawId())) {
                        lawAmounts.put(companyLaw.getLaw().getLawId(), pLawAmounts.get(companyLaw.getLaw().getLawId()));
                        withHoldingsAmount = withHoldingsAmount.add(SpcfDecimal.createInstance(pLawAmounts.get(companyLaw.getLaw().getLawId())));
                    } else {
                        lawAmounts.put(companyLaw.getLaw().getLawId(), companyLaw.getLaw().getLawId());
                        withHoldingsAmount = withHoldingsAmount.add(SpcfDecimal.createInstance(companyLaw.getLaw().getLawId()));
                    }
                }
            }
            PayrollServices.rollbackUnitOfWork();
        }
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(pCompany);
        DataLoadServices.addAssistedBankAccounts(pCompany, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, pCompany, payrollDate, Arrays.asList(employees.toArray(new Employee[]{})), lawAmounts);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, pCompany.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        for (String state : pStatesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state, pCategory, paymentTemplateCd);
            PayrollServices.beginUnitOfWork();
            Application.getHibernateSession().refresh(paymentTemplate);
            CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(pCompany, paymentTemplate.getAgency().getAgencyId());
            SpcfDecimal withHoldingsAmount = SpcfMoney.ZERO;
            DomainEntitySet<CompanyLaw> companyLaws = companyAgency.getCompanyLawCollection();
            for (CompanyLaw companyLaw : companyLaws) {
                if(paymentTemplate.getLawCollection().contains(companyLaw.getLaw())) { // To include only laws from individual payment template, companyAgency will have Law for the agency
                    if (pLawAmounts.containsKey(companyLaw.getLaw().getLawId())) {
                        lawAmounts.put(companyLaw.getLaw().getLawId(), pLawAmounts.get(companyLaw.getLaw().getLawId()));
                        withHoldingsAmount = withHoldingsAmount.add(SpcfDecimal.createInstance(pLawAmounts.get(companyLaw.getLaw().getLawId())));
                    } else {
                        lawAmounts.put(companyLaw.getLaw().getLawId(), companyLaw.getLaw().getLawId());
                        withHoldingsAmount = withHoldingsAmount.add(SpcfDecimal.createInstance(companyLaw.getLaw().getLawId()));
                    }
                }
            }

            // Multiply by number of employees
            withHoldingsAmount = withHoldingsAmount.multiply(SpcfDecimal.createInstance(String.valueOf(employees.size())));
            DomainEntitySet<MoneyMovementTransaction> statePayments = getReadyToSendTaxPayments(pCompany, paymentTemplate.getPaymentTemplateCd());
            if (statePayments.size() == 0)   {
                statePayments = getATFFinalizedTaxPayments(pCompany, paymentTemplate.getPaymentTemplateCd());
            }

            if(statePayments.size() == 0 && paymentTemplate.getAgency().getAgencyId().equals(Agency.FL_AGENT_ID)) {
                statePayments = getOnHoldTaxPayments(pCompany, paymentTemplate.getPaymentTemplateCd());
            }

            assertTrue("Number of State payments for " + state, statePayments.size() > 0);
            if (statePayments.getFirst().getMoneyMovementPaymentMethod() == PaymentMethod.ACHCredit) {
                DomainEntitySet<EntryDetailRecord> entryDetailRecords = Application.find(EntryDetailRecord.class, EntryDetailRecord.NACHAFileType().equalTo(NACHAFileType.CCDPlus).And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)).And(EntryDetailRecord.MoneyMovementTransaction().equalTo(statePayments.getFirst())));
                assertEquals("State ACH credit payment entry detail for " + state, 1 + (paymentTemplate.getPaymentTemplateCd().equals("OR-OTCUI-PAYMENT") ? 1 : 0), entryDetailRecords.size());
            }
            PayrollServices.rollbackUnitOfWork();
        }

        if (checkIRS) {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<MoneyMovementTransaction> irs941Payments = getOnHoldTaxPayments(pCompany, "IRS-941-PAYMENT");
            assertEquals("IRS 941 payments", 1, irs941Payments.size());
            assertEquals("IRS 941 payment Amount", new SpcfMoney("100"), irs941Payments.get(0).getMoneyMovementTransactionAmount());
            DomainEntitySet<MoneyMovementTransaction> irs940Payments = getOnHoldTaxPayments(pCompany, "IRS-940-PAYMENT");
            assertEquals("IRS 941 payments", 1, irs940Payments.size());
            assertEquals("IRS 941 payment Amount", new SpcfMoney("13"), irs940Payments.get(0).getMoneyMovementTransactionAmount());
            PayrollServices.rollbackUnitOfWork();
        }

    }

    public static void runPayrollRunNYMetro(Company pCompany, String[] pStatesList, SpcfCalendar supportedDate, DateDTO payrollDate,
                                            boolean checkIRS, HashMap<String, String> pLawAmounts) {
        HashMap<String, String> lawAmounts = new HashMap();
        if (pLawAmounts.isEmpty()) {
            lawAmounts.put("61", "6.1");
            lawAmounts.put("62", "6.2");
            lawAmounts.put("63", "6.3");
            lawAmounts.put("64", "6.4");
            lawAmounts.put("1", "25");
            lawAmounts.put("65", "6.5");
        } else {
            for (String lawId : pLawAmounts.keySet()) {
                lawAmounts.put(lawId, pLawAmounts.get(lawId));
            }
        }
        for (String state : pStatesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state, PaymentTemplateCategory.Withholding);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
            PayrollServices.beginUnitOfWork();
            CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(pCompany, paymentTemplate.getAgency().getAgencyId());
            SpcfDecimal withHoldingsAmount = SpcfMoney.ZERO;
            DomainEntitySet<CompanyLaw> companyLaws = companyAgency.getCompanyLawCollection();
            for (CompanyLaw companyLaw : companyLaws) {
                if (pLawAmounts.containsKey(companyLaw.getLaw().getLawId())) {
                    lawAmounts.put(companyLaw.getLaw().getLawId(), pLawAmounts.get(companyLaw.getLaw().getLawId()));
                    withHoldingsAmount = withHoldingsAmount.add(SpcfDecimal.createInstance(pLawAmounts.get(companyLaw.getLaw().getLawId())));
                } else {
                    lawAmounts.put(companyLaw.getLaw().getLawId(), companyLaw.getLaw().getLawId());
                    withHoldingsAmount = withHoldingsAmount.add(SpcfDecimal.createInstance(companyLaw.getLaw().getLawId()));
                }
            }
            if (state.equals("NY")) {
                 PayrollServices.rollbackUnitOfWork();
                paymentTemplate = PaymentTemplate.findPaymentTemplate("NY-MTA305-PAYMENT");
                DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
                PayrollServices.beginUnitOfWork();
                companyAgency = CompanyAgency.findCompanyAgency(pCompany, paymentTemplate.getAgency().getAgencyId());
                withHoldingsAmount = SpcfMoney.ZERO;
                companyLaws = companyAgency.getCompanyLawCollection();
                for (CompanyLaw companyLaw : companyLaws) {
                    if (pLawAmounts.containsKey(companyLaw.getLaw().getLawId())) {
                        lawAmounts.put(companyLaw.getLaw().getLawId(), pLawAmounts.get(companyLaw.getLaw().getLawId()));
                        withHoldingsAmount = withHoldingsAmount.add(SpcfDecimal.createInstance(pLawAmounts.get(companyLaw.getLaw().getLawId())));
                    } else {
                        lawAmounts.put(companyLaw.getLaw().getLawId(), companyLaw.getLaw().getLawId());
                        withHoldingsAmount = withHoldingsAmount.add(SpcfDecimal.createInstance(companyLaw.getLaw().getLawId()));
                    }
                }
            }
            PayrollServices.rollbackUnitOfWork();
        }
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(pCompany);
        DataLoadServices.addAssistedBankAccounts(pCompany, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, pCompany, payrollDate, Arrays.asList(employees.toArray(new Employee[]{})), lawAmounts);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, pCompany.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        for (String state : pStatesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state, PaymentTemplateCategory.Withholding);
            PayrollServices.beginUnitOfWork();
            CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(pCompany, paymentTemplate.getAgency().getAgencyId());
            SpcfDecimal withHoldingsAmount = SpcfMoney.ZERO;
            DomainEntitySet<CompanyLaw> companyLaws = companyAgency.getCompanyLawCollection();
            for (CompanyLaw companyLaw : companyLaws) {
                if (pLawAmounts.containsKey(companyLaw.getLaw().getLawId())) {
                    lawAmounts.put(companyLaw.getLaw().getLawId(), pLawAmounts.get(companyLaw.getLaw().getLawId()));
                    withHoldingsAmount = withHoldingsAmount.add(SpcfDecimal.createInstance(pLawAmounts.get(companyLaw.getLaw().getLawId())));
                } else {
                    lawAmounts.put(companyLaw.getLaw().getLawId(), companyLaw.getLaw().getLawId());
                    withHoldingsAmount = withHoldingsAmount.add(SpcfDecimal.createInstance(companyLaw.getLaw().getLawId()));
                }
            }

            // Multiply by number of employees
            withHoldingsAmount = withHoldingsAmount.multiply(SpcfDecimal.createInstance(String.valueOf(employees.size())));
            DomainEntitySet<MoneyMovementTransaction> statePayments = getReadyToSendTaxPayments(pCompany, paymentTemplate.getPaymentTemplateCd());
            assertTrue("Number of State payments for " + state, statePayments.size() > 0);
            assertTrue("State payment Amount for " + state, statePayments.getFirst().getMoneyMovementTransactionAmount().isGreaterThanEqualTo(withHoldingsAmount));
            if (statePayments.getFirst().getMoneyMovementPaymentMethod() == PaymentMethod.ACHCredit) {
                DomainEntitySet<EntryDetailRecord> entryDetailRecords = Application.find(EntryDetailRecord.class, EntryDetailRecord.NACHAFileType().equalTo(NACHAFileType.CCDPlus).And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)).And(EntryDetailRecord.MoneyMovementTransaction().equalTo(statePayments.getFirst())));
                assertEquals("State ACH credit payment entry detail for " + state, 1, entryDetailRecords.size());
            }
            PayrollServices.rollbackUnitOfWork();
        }

        if (checkIRS) {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<MoneyMovementTransaction> irs941Payments = getOnHoldTaxPayments(pCompany, "IRS-941-PAYMENT");
            assertEquals("IRS 941 payments", 1, irs941Payments.size());
            assertEquals("IRS 941 payment Amount", new SpcfMoney("100"), irs941Payments.get(0).getMoneyMovementTransactionAmount());
            DomainEntitySet<MoneyMovementTransaction> irs940Payments = getOnHoldTaxPayments(pCompany, "IRS-940-PAYMENT");
            assertEquals("IRS 941 payments", 1, irs940Payments.size());
            assertEquals("IRS 941 payment Amount", new SpcfMoney("13"), irs940Payments.get(0).getMoneyMovementTransactionAmount());
            PayrollServices.rollbackUnitOfWork();
        }

    }

    public static PayrollRunDTO setupCompanyAndRunPayrollForCA(String psid, DateDTO pspDate, DateDTO payrollDate) {
        return setupCompanyAndRunPayrollForCA(psid, pspDate, payrollDate, true);
    }


    public static PayrollRunDTO setupCompanyAndRunPayrollForCA(String psid, DateDTO pspDate, DateDTO payrollDate, boolean pSupportCATemplate) {
        Company assistedCompany = setupAssistedCompanyForCA(psid, 2, pSupportCATemplate);
        return runPayrollForCa(psid, new ArrayList<Employee>(assistedCompany.getEmployees()), pspDate, payrollDate);
    }


    public static Company setupCompanyAndSubmit100KPayrollWithOneState(String psid, String state, SpcfCalendar beginDate, DateDTO payrollDateDTO) {
        DataLoadServices.setPSPDate(beginDate);
        PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate("AZ", PaymentTemplateCategory.Withholding);
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), beginDate);

        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", "12200");
        lawAmounts.put("62", "12400");
        lawAmounts.put("63", "12600");
        lawAmounts.put("64", "12800");
        lawAmounts.put("1", "2500");
        lawAmounts.put("65", "6500");

        String[] statesList = new String[]{state};
        Company company = assertOne(DataLoadServices.setupCompany(Long.parseLong(psid), 1, statesList, PaymentTemplateCategory.Withholding));

        SpcfCalendar payrollDate = DateDTO.convertToSpcfCalendar(payrollDateDTO);
        payrollDate.addDays(-1);
        setPSPDate(payrollDate);

        List<Employee> employees = Arrays.asList(Employee.findEmployees(company).toArray(new Employee[]{}));
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, payrollDateDTO, employees, lawAmounts);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();
        return company;
    }

    public static Company setupAssistedCompanyForCA(String psid, int numberOfEmployees, boolean pSupportCATemplate) {
        SpcfCalendar supportStartDate = SpcfCalendar.createInstance(2010, 1, 1, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(supportStartDate);
        PayrollServices.commitUnitOfWork();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxService(company);

        DataLoadServices.addFederalAndCAStateTaxCompanyLaws(company);
        DataLoadServices.addEEs(company, numberOfEmployees);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());
        if (pSupportCATemplate) {
            DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "CA-PITSDI-PAYMENT");
            DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "CA-UIETT-PAYMENT");
            updateCAEDDPaymentTemplateSupportDate(PSPDate.getPSPTime());
        }
        DataLoadServices.claimOffer(company, DataLoadServices.WAIVE_ALL_FEES);

        return company;
    }

    public static PayrollRunDTO runPayrollForCa(String psid, List<Employee> employees, DateDTO payrollRunDateDto, DateDTO payrollDate) {
        Company assistedCompany = Company.findCompany(psid, SourceSystemCode.QBDT);
        SpcfCalendar payrollRunDate = DateDTO.convertToSpcfCalendar(payrollRunDateDto);
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(payrollRunDate);
        PayrollServices.commitUnitOfWork();

        HashMap<String, String> lawAmounts = new HashMap();
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("66", "6.6");
        lawAmounts.put("143", "14.3");
        lawAmounts.put("1", "10");
        lawAmounts.put("6", "6");
        lawAmounts.put("67", "6.7");
        lawAmounts.put("87", "8.7");
        lawAmounts.put("142", "14.2");

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(assistedCompany, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRun(payrollRunDTO, assistedCompany, payrollDate, employees, lawAmounts);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(assistedCompany, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        PayrollServices.rollbackUnitOfWork();

        return payrollDTO;
    }

    public static PayrollRunDTO runPayrollForNY(String psid, List<Employee> employees, DateDTO payrollRunDateDto, DateDTO payrollDate, HashMap<String, String> lawAmounts, boolean isBalanceFile) {
        Company assistedCompany = Company.findCompany(psid, SourceSystemCode.QBDT);
        SpcfCalendar payrollRunDate = DateDTO.convertToSpcfCalendar(payrollRunDateDto);
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        if (lawAmounts == null || lawAmounts.size() == 0) {
            lawAmounts = new HashMap();
            lawAmounts.put("61", "6.1");
            lawAmounts.put("62", "6.2");
            lawAmounts.put("63", "6.3");
            lawAmounts.put("64", "6.4");
            lawAmounts.put("66", "6.6");
            lawAmounts.put("143", "14.3");
            lawAmounts.put("1", "10");
            lawAmounts.put("36", "50");
            lawAmounts.put("54", "25");
        }
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(assistedCompany, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRun(payrollRunDTO, assistedCompany, payrollDate, employees, lawAmounts);
        if (isBalanceFile) {
            payrollDTO.setBalanceFilePayroll(true);
        }
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);
        return payrollDTO;
    }

    private static EffectiveDepositFrequencyDTO getDto() {
        return getDto(PSPDate.getPSPTime(), "IRS-941-PAYMENT");
    }

    private static EffectiveDepositFrequencyDTO getDto(SpcfCalendar pEffectiveDate, String pPaymentTemplate) {
        return getDto(pEffectiveDate, pPaymentTemplate, DepositFrequencyCode.QUARTERLY);
    }

    private static EffectiveDepositFrequencyDTO getDto(SpcfCalendar pEffectiveDate, String pPaymentTemplate, DepositFrequencyCode pDepositFrequencyCode) {
        EffectiveDepositFrequencyDTO dto = new EffectiveDepositFrequencyDTO();
        PaymentTemplate paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, pPaymentTemplate);
        dto.setAgencyId(paymentTemplate.getAgency().getAgencyId());

        dto.setEffectiveDate(pEffectiveDate);
        dto.setPaymentTemplateCd(pPaymentTemplate);
        dto.setPaymentFrequencyId(pDepositFrequencyCode);
        return dto;
    }

    public static void finalizePayment(MoneyMovementTransaction payment) {
        PayrollServices.beginUnitOfWork();
        Application.refresh(payment);
        assertSuccess(PayrollServices.paymentManager.finalizeSUIPayments(Arrays.asList(payment), payment.getPaymentTemplate(), payment.getPaymentPeriodEnd().getYear(), CalendarUtils.getQuarterAsInt(payment.getPaymentPeriodEnd())));
        PayrollServices.commitUnitOfWork();
    }

    public static void unfinalizePayment(MoneyMovementTransaction payment) {
        PayrollServices.beginUnitOfWork();
        Application.refresh(payment);
        assertSuccess(PayrollServices.paymentManager.unfinalizeSUIPayments(Arrays.asList(payment), payment.getPaymentTemplate(), payment.getPaymentPeriodEnd().getYear(), CalendarUtils.getQuarterAsInt(payment.getPaymentPeriodEnd())));
        PayrollServices.commitUnitOfWork();
    }

    public static void createCheckPrintSignature() throws Exception {
        PayrollServices.beginUnitOfWork();
        SourceSystemPrintedCheckInfo sourceSystemPrintedCheckInfo = assertOne(Application.find(SourceSystemPrintedCheckInfo.class));
        if (sourceSystemPrintedCheckInfo.getCheckPrintSignature() == null) {
            CheckPrintSignature checkPrintSignature = new CheckPrintSignature();
            checkPrintSignature.setSourceSystemPrintedCheckInfo(sourceSystemPrintedCheckInfo);
            Application.save(checkPrintSignature);
            checkPrintSignature.setSignatureAsImage(getBytesFromFile(new File(Application.findFileOnClassPath("resources/signature.png"))));
        }
        PayrollServices.commitUnitOfWork();

    }

    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead;
        while (offset < bytes.length
                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    ////////////////
    // Assertions
    ////////////////

    public static void assertMmt(PaymentMethod pPaymentMethod, SpcfMoney pTransAmount, SpcfCalendar pInitDate, SpcfCalendar pDueDate, SpcfCalendar pBegindate, SpcfCalendar pEndDate, int count) {
        Criterion<MoneyMovementTransaction> mmtCriteria = MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(pTransAmount);
        if (pPaymentMethod != null) {
            mmtCriteria = mmtCriteria.And(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(pPaymentMethod));
        }
        if (pInitDate != null) {
            mmtCriteria = mmtCriteria.And(MoneyMovementTransaction.InitiationDate().equalTo(pInitDate));
        }
        if (pDueDate != null) {
            mmtCriteria = mmtCriteria.And(MoneyMovementTransaction.DueDate().equalTo(pDueDate));
        }
        if (pBegindate != null) {
            mmtCriteria = mmtCriteria.And(MoneyMovementTransaction.PaymentPeriodBegin().equalTo(pBegindate));
        }
        if (pEndDate != null) {
            mmtCriteria = mmtCriteria.And(MoneyMovementTransaction.PaymentPeriodEnd().equalTo(pEndDate));
        }
        DomainEntitySet<MoneyMovementTransaction> moneyMovementACHTransactions = Application.find(MoneyMovementTransaction.class, mmtCriteria);
        assertEquals("Money movement Transactions:", count, moneyMovementACHTransactions.size());
    }

    public static void assertFinancialTransaction(SettlementType pSettlementType, SpcfMoney pFinancialTransAmount, TransactionTypeCode pTransactionTypeCode, TransactionStateCode pTransactionStateCode, int count) {
        TransactionState lookUpState;
        if (pTransactionStateCode == null) {
            lookUpState = Application.findById(TransactionState.class, TransactionStateCode.Created);
        } else {
            lookUpState = Application.findById(TransactionState.class, pTransactionStateCode);
        }
        Criterion<FinancialTransaction> ftCriteria = FinancialTransaction.SettlementTypeCd().equalTo(pSettlementType).And(FinancialTransaction.FinancialTransactionAmount().equalTo(pFinancialTransAmount))
                .And(FinancialTransaction.CurrentTransactionState().equalTo(lookUpState));
        ;
        if (pTransactionTypeCode != null) {
            ftCriteria = ftCriteria.And(FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(pTransactionTypeCode)));
        }
        DomainEntitySet<FinancialTransaction> financialTransactions = Application.find(FinancialTransaction.class, ftCriteria);
        assertEquals("Financial Transactions:", count, financialTransactions.size());
    }

    public static void assertTax(Paycheck pPaycheck, String pLawId, SpcfMoney pLiabilityAmount) {
        Criterion<Tax> taxCriterion = Tax.Paycheck().equalTo(pPaycheck).And(Tax.Law().LawId().equalTo(pLawId)).And(Tax.TaxLiabilityAmount().equalTo(pLiabilityAmount));
        DomainEntitySet<Tax> taxes = Application.find(Tax.class, taxCriterion);
        assertEquals("Number of Tax liabilities:", 1, taxes.size());
    }

    public static void assertEffectiveDepositFreq(SpcfCalendar pEffectiveDate) {
        DomainEntitySet<EffectiveDepositFrequency> effectiveDepositFrequencies = Application.find(EffectiveDepositFrequency.class, EffectiveDepositFrequency.InvalidDate().isNull()).sort(EffectiveDepositFrequency.<EffectiveDepositFrequency>ModifiedDate().Descending());
        if (effectiveDepositFrequencies.size() > 0) {
            EffectiveDepositFrequency effectiveDepositFrequency = (EffectiveDepositFrequency) effectiveDepositFrequencies.get(0);
            CalendarUtils.clearTime(effectiveDepositFrequency.getEffectiveDate());
            assertEquals("Latest Effective date:", effectiveDepositFrequency.getEffectiveDate().toString(), pEffectiveDate.toString());
        } else {
            assertFalse("Effective Deposit Frequency is not found", true);
        }

    }

    public static void assertEFTPSPayrolls(PayrollRunDTO pPayrollRunDTO, PayrollRun pPayrollRun, String pPaymentTemplateId, SpcfCalendar pMmtDuedate) {

        List<String> federalLaws = new ArrayList<String>();
        PaymentTemplate paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, pPaymentTemplateId);
        Criterion<Law> lCriteria = Law.PaymentTemplate().equalTo(paymentTemplate);
        DomainEntitySet<Law> laws = Application.find(Law.class, lCriteria);
        for (Law law : laws) {
            federalLaws.add(law.getLawId());
        }

        BigDecimal totalMmtAmount = new BigDecimal(0);
        Set<String> lawIds = new TreeSet<String>();
        String unSupportedLawId = "40";
        Collection<PaycheckDTO> paycheckDTOs = pPayrollRunDTO.getPaychecks();
        for (PaycheckDTO paycheckDTO : paycheckDTOs) {
            Collection<LiabilityTransactionDTO> liabilityTransactionDTOs = paycheckDTO.getLiabilityTransactions();
            for (LiabilityTransactionDTO liabilityTransactionDTO : liabilityTransactionDTOs) {
                if (federalLaws.contains(liabilityTransactionDTO.getLawId()) && liabilityTransactionDTO.getLawId() != Law.COBRA) {
                    lawIds.add(liabilityTransactionDTO.getLawId());
                    totalMmtAmount = new BigDecimal(totalMmtAmount.add(liabilityTransactionDTO.getLiabilityAmount()).doubleValue());
                }
            }
        }
        DomainEntitySet<FinancialTransaction> employerTaxDebits = pPayrollRun.getFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.EmployerTaxDebit}, null);
        assertEquals(1, employerTaxDebits.size());

        DomainEntitySet<FinancialTransaction> agencyTaxCredits = pPayrollRun.getFinancialTransactions(paymentTemplate, TransactionStateCode.Created, TransactionTypeCode.AgencyTaxCredit);
        //Verify number of transactions
        assertEquals(lawIds.size(), agencyTaxCredits.size());

        Criterion<MoneyMovementTransaction> mmtCriteria = MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(agencyTaxCredits.get(0).findPaymentMethod())
                .And(MoneyMovementTransaction.PaymentTemplate().equalTo(paymentTemplate));
        DomainEntitySet<MoneyMovementTransaction> moneyMovementEFTPSTransactions = Application.find(MoneyMovementTransaction.class, mmtCriteria);
        //Verify only one MMT with payment method EFTPS is created.
        assertEquals(1, moneyMovementEFTPSTransactions.size());
        MoneyMovementTransaction moneyMovementEFTPSTransaction = moneyMovementEFTPSTransactions.get(0);
        CalendarUtils.clearTime(moneyMovementEFTPSTransaction.getDueDate());
        assertEquals(pMmtDuedate.toString(), moneyMovementEFTPSTransaction.getDueDate().toString());

        SpcfMoney totalTransactionAmount = new SpcfMoney();
        for (FinancialTransaction financialTransaction : agencyTaxCredits) {
            assertEquals(financialTransaction.getSettlementTypeCd(), SettlementType.EFTPS);
            //Verify no financial transaction is created for unsupported law id.
            assertFalse(unSupportedLawId.equals(financialTransaction.getLaw().getLawId()));

            totalTransactionAmount = new SpcfMoney(totalTransactionAmount.add(financialTransaction.getFinancialTransactionAmount()));

            //Verify Due date is correct
            DepositFrequencyCode depositFrequencyCode = moneyMovementEFTPSTransaction.getPaymentTemplate().getEffectiveDepositFrequency(financialTransaction.getPayrollRun());
            IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(paymentTemplate.getPaymentTemplateCd(), depositFrequencyCode.toString(), CalendarUtils.convertToRulesCalendar(financialTransaction.getPayrollRun().getPaycheckDate()));
            assertEquals("MMT Due Date", paymentPeriod.getDueDate().toString(), moneyMovementEFTPSTransaction.getDueDate().toString());

            //Verify all financial transaction have the same money movement transaction
            assertEquals(financialTransaction.getMoneyMovementTransaction(), moneyMovementEFTPSTransaction);
            assertTaxSettlementDate(financialTransaction);
        }

        //Verify total financial transaction amount
        assertEquals(SpcfUtils.convertToSpcfMoney(totalMmtAmount), totalTransactionAmount);

        mmtCriteria = MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.ACHDirectDeposit);
        DomainEntitySet<MoneyMovementTransaction> moneyMovementACHTransactions = Application.find(MoneyMovementTransaction.class, mmtCriteria);

        DomainEntitySet<FinancialTransaction> financialTransactions = Application.find(FinancialTransaction.class);
        DomainEntitySet<FinancialTransaction> allEFTPSFinancialTransactions = financialTransactions.find(FinancialTransaction.SettlementTypeCd().equalTo(SettlementType.EFTPS)
                .And(FinancialTransaction.MoneyMovementTransaction().PaymentTemplate().equalTo(paymentTemplate)));
        DomainEntitySet<FinancialTransaction> allACHFinancialTransactions = financialTransactions.find(FinancialTransaction.SettlementTypeCd().equalTo(SettlementType.ACH));
        //  assertEquals(allACHFinancialTransactions.size(), moneyMovementACHTransactions.size());
//        for (MoneyMovementTransaction moneyMovementACHTransaction : moneyMovementACHTransactions) {
//            DomainEntitySet<FinancialTransaction> financialTransaction = allACHFinancialTransactions.find(FinancialTransaction.MoneyMovementTransaction().equalTo(moneyMovementACHTransaction));
//            assertEquals(1, financialTransaction.size());
//            assertEquals(financialTransaction.get(0).getFinancialTransactionAmount(), moneyMovementACHTransaction.getMoneyMovementTransactionAmount());
//            //Verify ACH financial transaction settlement date is same ACH MMT transaction due date.
//            assertEquals(financialTransaction.get(0).getSettlementDate(), moneyMovementACHTransaction.getDueDate());
//        }

        SpcfMoney totalMMTEFTPSTransactionAmount = new SpcfMoney();
        for (FinancialTransaction eFTPSFinancialTransaction : allEFTPSFinancialTransactions) {
            //Verify no financial transaction is created for unsupported law id.
            assertFalse(unSupportedLawId.equals(eFTPSFinancialTransaction.getLaw().getLawId()));
            totalMMTEFTPSTransactionAmount = new SpcfMoney(totalMMTEFTPSTransactionAmount.add(eFTPSFinancialTransaction.getFinancialTransactionAmount()));
            //Verify all financial transaction have the same money movement transaction
            assertEquals(eFTPSFinancialTransaction.getMoneyMovementTransaction(), moneyMovementEFTPSTransaction);
            //Verify Due date is correct
            DepositFrequencyCode depositFrequencyCode = moneyMovementEFTPSTransaction.getPaymentTemplate().getEffectiveDepositFrequency(eFTPSFinancialTransaction.getPayrollRun());
            IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(paymentTemplate.getPaymentTemplateCd(), depositFrequencyCode.toString(), CalendarUtils.convertToRulesCalendar(eFTPSFinancialTransaction.getPayrollRun().getPaycheckDate()));
            assertEquals("MMT Due Date", paymentPeriod.getDueDate().toString(), moneyMovementEFTPSTransaction.getDueDate().toString());

            assertTaxSettlementDate(eFTPSFinancialTransaction);
        }


    }

    public static void assertPayrollsEqual(PayrollRunDTO pPayrollRunDTO, PayrollRun pPayrollRun) {
        assertPayrollsEqual(pPayrollRunDTO, PayrollStatus.Pending, pPayrollRun);
    }

    public static void assertPayrollsEqual(PayrollRunDTO pPayrollRunDTO, PayrollStatus pPayrollStatus, PayrollRun pPayrollRun) {
        assertEquals("Payroll Run ID", pPayrollRunDTO.getPayrollTXBatchId(), pPayrollRun.getSourcePayRunId());
        for (PaycheckDTO paycheckDTO : pPayrollRunDTO.getPaychecks()) {
            if(paycheckDTO.getDdTransactions() != null && paycheckDTO.getDdTransactions().size() > 0) {
                assertEquals("Payroll net amount", pPayrollRunDTO.getPayrollDirectDepositAmount(), pPayrollRun.getPayrollDirectDepositAmount());
                break;
            }
        }
        assertEquals("Paycheck date", DateDTO.convertToSpcfCalendar(pPayrollRunDTO.getTargetPayrollTXDate()), pPayrollRun.getPaycheckDate().toLocal());
        assertEquals("Payroll Run Status", pPayrollStatus, pPayrollRun.getPayrollRunStatus());
        Collection<PaycheckDTO> dtoPaychecks = pPayrollRunDTO.getPaychecks();
        DomainEntitySet<Paycheck> domainPaychecks = pPayrollRun.getPaycheckCollection();
        assertEquals("number of paychecks", dtoPaychecks.size(), domainPaychecks.size());

        if (pPayrollRun.getCompany().isCompanyOnService(ServiceCode.Tax)) {
           boolean  mustProcessTax = pPayrollRun.getCompany().isCompanyOnService(ServiceCode.Tax) && PayrollSubmitHelper.getInstance().anyTaxTransactionsInPayroll(pPayrollRunDTO)
                    && !ServiceSubStatusCode.PendingSetup.equals(pPayrollRun.getCompany().getService(ServiceCode.Tax).getStatusCd());
            boolean mustProcessDD = pPayrollRun.getCompany().isCompanyOnService(ServiceCode.DirectDeposit) && PayrollSubmitHelper.anyDDTransactionsInPayroll(pPayrollRunDTO);
            PayrollType expectedPayrollType= (mustProcessDD && !pPayrollRunDTO.getBalanceFilePayroll()) ? PayrollType.Regular : PayrollType.CloudOnly;
            assertEquals("Not a cloud payroll", expectedPayrollType, pPayrollRun.getPayrollRunType());
            if(PayrollSubmitHelper.getInstance().anyTaxTransactionsInPayroll(pPayrollRunDTO)) {
                // check confirmation event and email
                CompanyEvent payrollConfirmation =
                        assertOne(Application.find(CompanyEventDetail.class,
                                                   CompanyEventDetail.Value().equalTo(pPayrollRun.getId().toString())
                                                                     .And(CompanyEventDetail.CompanyEvent().EventTypeCd().equalTo(EventTypeCode.AssistedPayrollConfirmation)))).getCompanyEvent();
                assertOne(Application.find(CompanyEventEmail.class, CompanyEventEmail.CompanyEvent().equalTo(payrollConfirmation)));
            }
        } else if(pPayrollRun.getCompany().isCompanyOnService(ServiceCode.DirectDeposit) && pPayrollRun.getFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit}, null).isNotEmpty()) {
            assertEquals("Not a regular payroll", PayrollType.Regular, pPayrollRun.getPayrollRunType());
        } else {
            assertEquals("Not a cloud payroll", PayrollType.CloudOnly, pPayrollRun.getPayrollRunType());
        }

        for (PaycheckDTO currentDTO : dtoPaychecks) {
            Criterion<Paycheck> where = Paycheck.SourcePaycheckId().equalTo(currentDTO.getPaycheckId());
            where = where.And(Paycheck.GrossAmount().equalTo(currentDTO.getPaycheckGrossAmount()));
            where = where.And(Paycheck.NetAmount().equalTo(currentDTO.getPaycheckNetAmount()));
            where = where.And(Paycheck.YTDGrossAmount().equalTo(currentDTO.getPaycheckYTDGrossAmount()));
            where = where.And(Paycheck.YTDNetAmount().equalTo(currentDTO.getPaycheckYTDNetAmount()));
            where = where.And(Paycheck.PayPeriodBeginDate().equalTo(DateDTO.convertToSpcfCalendar(currentDTO.getPayPeriodBeginDate())));
            where = where.And(Paycheck.PayPeriodEndDate().equalTo(DateDTO.convertToSpcfCalendar(currentDTO.getPayPeriodEndDate())));
            DomainEntitySet<Paycheck> foundDomainPaychecks = domainPaychecks.find(where);
            if (foundDomainPaychecks.size() != 1) {
                TestCase.fail("Did not find domain paycheck for paycheck id " + currentDTO.getPaycheckId() + " in the request");
            }
            Paycheck domainPaycheck = foundDomainPaychecks.get(0);
            assertEquals("NetAmount", domainPaycheck.getNetAmount(), currentDTO.getPaycheckNetAmount());
            assertEquals("NetYTDAmount", domainPaycheck.getYTDNetAmount(), currentDTO.getPaycheckYTDNetAmount());
            assertEquals("GrossAmount", domainPaycheck.getGrossAmount(), currentDTO.getPaycheckGrossAmount());
            assertEquals("GrossYTDAmount", domainPaycheck.getYTDGrossAmount(), currentDTO.getPaycheckYTDGrossAmount());

            Collection<CompensationTransactionDTO> dtoCompensation = currentDTO.getCompensationTransactions();
            DomainEntitySet<Compensation> compensationCollection = domainPaycheck.getCompensationCollection();
            DomainEntitySet<Compensation> newCompensationCollection = new DomainEntitySet<Compensation>();
            newCompensationCollection.addAll(compensationCollection);

            if (dtoCompensation != null && newCompensationCollection != null) {
                assertEquals(dtoCompensation.size(), newCompensationCollection.size());
            } else if (dtoCompensation != null && newCompensationCollection == null) {
                TestCase.fail("Found compensations in the DTO but not in the database");
            } else if (newCompensationCollection != null && newCompensationCollection.size() > 0 && dtoCompensation == null) {
                TestCase.fail("Found compensations in the db but not in the dto");
            }

            if (currentDTO.getCompensationTransactions() != null) {
                for (CompensationTransactionDTO currentCompensationDTO : currentDTO.getCompensationTransactions()) {
                    CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(pPayrollRun.getCompany(), currentCompensationDTO.getSourcePayrollItemId());
                    if (companyPayrollItem == null) {
                        TestCase.fail("Found compensation with a company payroll item that does not exist");
                    }
                    Criterion<Compensation> compensationWhere = Compensation.CompensationAmount().equalTo(currentCompensationDTO.getCompensationAmount());
                    compensationWhere = compensationWhere.And(Compensation.CompanyPayrollItem().equalTo(companyPayrollItem));
                    if (currentCompensationDTO.getCompensationYTDAmount() != null) {
                        compensationWhere = compensationWhere.And(Compensation.CompensationYTDAmount().equalTo(currentCompensationDTO.getCompensationYTDAmount()));
                    } else {
                        compensationWhere = compensationWhere.And(Compensation.CompensationYTDAmount().equalTo(new SpcfMoney("0.00")));
                    }
                    compensationWhere = compensationWhere.And(Compensation.HoursWorked().equalTo(Double.parseDouble(currentCompensationDTO.getHoursWorked().toString())));
                    DomainEntitySet<Compensation> foundCompensations = newCompensationCollection.find(compensationWhere);
                    boolean hasAtLeastOneCompensation = foundCompensations.size() > 0;
                    assertTrue("Found at least one matching compensation", hasAtLeastOneCompensation);
                    Compensation currentCompensation = foundCompensations.get(0);
                    newCompensationCollection.remove(currentCompensation);
                }
            }

            Collection<DeductionTransactionDTO> dtoDeduction = currentDTO.getDeductionTransactions();
            DomainEntitySet<Deduction> deductionCollection = domainPaycheck.getDeductionCollection();
            DomainEntitySet<Deduction> newDeductionCollection = new DomainEntitySet<Deduction>();
            newDeductionCollection.addAll(deductionCollection);

            if (dtoDeduction != null && newDeductionCollection != null) {
                assertEquals(dtoDeduction.size(), newDeductionCollection.size());
            } else if (dtoDeduction != null && newDeductionCollection == null) {
                TestCase.fail("Found deductions in the DTO but not in the database");
            } else if (newDeductionCollection != null && newDeductionCollection.size() > 0 && dtoDeduction == null) {
                TestCase.fail("Found deductions in the db but not in the dto");
            }

            if (dtoDeduction != null) {
                for (DeductionTransactionDTO currentDeductionDTO : currentDTO.getDeductionTransactions()) {
                    CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(pPayrollRun.getCompany(), currentDeductionDTO.getSourcePayrollItemId());
                    if (companyPayrollItem == null) {
                        TestCase.fail("Found currentDeductionDTO with a company payroll item that does not exist");
                    }
                    Criterion<Deduction> deductionWhere = Deduction.DeductionAmount().equalTo(SpcfUtils.convertToSpcfMoney(currentDeductionDTO.getDeductionAmount()));
                    deductionWhere = deductionWhere.And(Deduction.CompanyPayrollItem().equalTo(companyPayrollItem));
                    if (currentDeductionDTO.getDeductionYTDAmount() != null) {
                        deductionWhere = deductionWhere.And(Deduction.DeductionYTDAmount().equalTo(SpcfUtils.convertToSpcfMoney(currentDeductionDTO.getDeductionYTDAmount())));
                    } else {
                        deductionWhere = deductionWhere.And(Deduction.DeductionYTDAmount().equalTo(new SpcfMoney("0.00")));
                    }
                    DomainEntitySet<Deduction> foundDeductions = newDeductionCollection.find(deductionWhere);
                    boolean hasAtLeastOneDeduction = newDeductionCollection.size() > 0;
                    assertTrue("Found at least one matching deduction", hasAtLeastOneDeduction);
                    Deduction currentDeduction = foundDeductions.get(0);
                    newDeductionCollection.remove(currentDeduction);
                }
            }

            Collection<EmployerContributionTransactionDTO> employerContributionDTO = currentDTO.getEmployerContributionTransactions();
            DomainEntitySet<EmployerContribution> erContributionCollection = domainPaycheck.getEmployerContributionCollection();
            DomainEntitySet<EmployerContribution> newContributionCollection = new DomainEntitySet<EmployerContribution>();
            newContributionCollection.addAll(erContributionCollection);

            if (employerContributionDTO != null && newContributionCollection != null) {
                assertEquals(employerContributionDTO.size(), newContributionCollection.size());
            } else if (employerContributionDTO != null && newContributionCollection == null) {
                TestCase.fail("Found employer contributions in the DTO but not in the database");
            } else if (newContributionCollection != null && newContributionCollection.size() > 0 && employerContributionDTO == null) {
                TestCase.fail("Found employer contributions in the db but not in the dto");
            }

            if (employerContributionDTO != null) {
                for (EmployerContributionTransactionDTO currentContributionDTO : currentDTO.getEmployerContributionTransactions()) {
                    CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(pPayrollRun.getCompany(), currentContributionDTO.getSourcePayrollItemId());
                    if (companyPayrollItem == null) {
                        TestCase.fail("Found currentContributionDTO with a company payroll item that does not exist");
                    }

                    Criterion<EmployerContribution> contributionWhere = EmployerContribution.ContributionAmount().equalTo(SpcfUtils.convertToSpcfMoney(currentContributionDTO.getContributionAmount()));
                    contributionWhere = contributionWhere.And(EmployerContribution.CompanyPayrollItem().equalTo(companyPayrollItem));
                    if (currentContributionDTO.getContributionYTDAmount() != null) {
                        contributionWhere = contributionWhere.And(EmployerContribution.ContributionYTDAmount().equalTo(SpcfUtils.convertToSpcfMoney(currentContributionDTO.getContributionYTDAmount())));
                    } else {
                        contributionWhere = contributionWhere.And(EmployerContribution.ContributionYTDAmount().equalTo(new SpcfMoney("0.00")));
                    }
                    DomainEntitySet<EmployerContribution> foundContributions = newContributionCollection.find(contributionWhere);
                    boolean hasAtLeastOneContribution = newContributionCollection.size() > 0;
                    assertTrue("Found at least one matching contribution", hasAtLeastOneContribution);
                    EmployerContribution currentContribution = foundContributions.get(0);
                    newContributionCollection.remove(currentContribution);
                }
            }
        }
    }

    public static void assertBillPaymentsEqual(Company pCompany, Collection<BillPaymentDTO> pBillPaymentDTOs) {
        for (BillPaymentDTO billPaymentDTO : pBillPaymentDTOs) {
            BillPayment billPayment = BillPayment.findBillPaymentBySourceId(pCompany, billPaymentDTO.getBillPaymentId());

            assertEquals("amount", billPaymentDTO.getAmount().toString(), billPayment.getAmount().toString());
            PayrollRun payrollRun = billPayment.getPayrollRun();
            assertEquals("date", DateDTO.convertToSpcfCalendar(billPaymentDTO.getDepositDate()), payrollRun.getPaycheckDate().toLocal());
            assertEquals("Payroll Run Status", PayrollStatus.Pending, payrollRun.getPayrollRunStatus());
            assertEquals("Payroll Type", PayrollType.BillPayment, payrollRun.getPayrollRunType());
            assertEquals("Payee", billPaymentDTO.getPayeeDTO().getSourcePayeeId(), billPayment.getPayee().getSourcePayeeId());

            assertEquals(billPaymentDTO.getPaymentTransactions().size(), billPayment.getBillPaymentSplitCollection().size());
            for (BillPaymentSplitDTO billPaymentSplitDTO : billPaymentDTO.getPaymentTransactions()) {
                DomainEntitySet<BillPaymentSplit> billPaymentSplits = billPayment.getBillPaymentSplitCollection().find(BillPaymentSplit.SourceId().equalTo(billPaymentSplitDTO.getBillPaymentSplitId()));
                assertEquals("number of splits with id", 1, billPaymentSplits.size());
                BillPaymentSplit billPaymentSplit = billPaymentSplits.get(0);

                assertEquals("Split amount", SpcfUtils.convertToSpcfMoney(billPaymentSplitDTO.getAmount()).toString(), billPaymentSplit.getAmount().toString());
                assertEquals("split bank account", billPaymentSplitDTO.getPayeeBankAccount().getPayeeBankAccountId(), billPaymentSplit.getPayeeBankAccount().getSourceBankAccountId());
                assertEquals("fin txn type", TransactionTypeCode.EmployeeDdCredit, billPaymentSplit.getFinancialTransaction().getTransactionType().getTransactionTypeCd());
            }
        }
    }

    public static void assertEmployeesEqual(EmployeeDTO eeDTO, Employee domainEmployee) {
        assertEmployeesEqual(eeDTO, domainEmployee, null);
    }

    public static void assertEmployeesEqual(EmployeeDTO eeDTO, Employee domainEmployee, EmployeeStatus expectedStatus) {
        Assert.assertEquals("Employee First Name:", eeDTO.getFirstName(), domainEmployee.getFirstName());
        Assert.assertEquals("Employee Last Name:", eeDTO.getLastName(), domainEmployee.getLastName());
        Assert.assertEquals("Employee Middle Name:", eeDTO.getMiddleName(), domainEmployee.getMiddleName());
        Assert.assertEquals("Employee Source Id:", eeDTO.getEmployeeId(), domainEmployee.getSourceEmployeeId());
        Assert.assertEquals("Employee SSN:", eeDTO.getSocialSecurityNumber(), domainEmployee.getTaxId());
        Assert.assertEquals("Employee Status:", expectedStatus == null ? EmployeeStatus.Active : expectedStatus, domainEmployee.getStatusCd());
        Assert.assertEquals("Employee WorkState:", eeDTO.getWorkState(), domainEmployee.getWorkState());
        if (eeDTO.getHireDate() != null) {
            Assert.assertEquals("Employee Hire Date: ", DateDTO.convertToSpcfCalendar(eeDTO.getHireDate()), domainEmployee.getHireDate().toLocal());
        } else {
            assertNull(domainEmployee.getHireDate());
        }
        if (eeDTO.getEmployee401kInfo() == null) {
            Assert.assertEquals("Employee Terminated Date: ", null, domainEmployee.getTerminationDate());
        }
        Assert.assertEquals("Employee ReHire Date: ", null, domainEmployee.getReHireDate());
        Assert.assertEquals("Employee Fed Filing Status:", eeDTO.getFedFilingStatus(), domainEmployee.getFedFilingStatus());
        Assert.assertEquals("Employee Fed Allowances:", eeDTO.getFedAllowances(), domainEmployee.getFedAllowances());

        // todo fix these
        /*Assert.assertEquals("Employee State Filing Status:", eeDTO.getStateFilingStatus(), domainEmployee.getStateFilingStatus());
        Assert.assertEquals("Employee State Allowances:", eeDTO.getStateAllowances(), domainEmployee.getStateAllowances());*/

        Assert.assertEquals("Employee Has Retirement Plan", eeDTO.getHasRetirementPlan(), domainEmployee.getHasRetirementPlan());
        Assert.assertEquals("Employee Has ThirdParty Sick Pay", eeDTO.getHasThirdPartySickPay(), domainEmployee.getHasThirdPartySickPay());
        Assert.assertEquals("Employee Is Statutory", eeDTO.getIsStatutory(), domainEmployee.getIsStatutory());

        if (eeDTO.getLiveAddress() != null) {
            Assert.assertEquals("Employee Live Address1", eeDTO.getLiveAddress().getAddressLine1(), domainEmployee.getMailingAddress().getAddressLine1());
            Assert.assertEquals("Employee Live Address2", eeDTO.getLiveAddress().getAddressLine2(), domainEmployee.getMailingAddress().getAddressLine2());
            Assert.assertEquals("Employee Live Address3", eeDTO.getLiveAddress().getAddressLine3(), domainEmployee.getMailingAddress().getAddressLine3());
            Assert.assertEquals("Employee Live city", eeDTO.getLiveAddress().getCity(), domainEmployee.getMailingAddress().getCity());
            Assert.assertEquals("Employee Live State", eeDTO.getLiveAddress().getState(), domainEmployee.getMailingAddress().getState());
            Assert.assertEquals("Employee Live Country", eeDTO.getLiveAddress().getCountry(), domainEmployee.getMailingAddress().getCountry());
        }
        if (eeDTO.getWagePlanDTOs() != null) {
            Assert.assertEquals("Employee Wage Plans", eeDTO.getWagePlanDTOs().size(), domainEmployee.getEmployeeWagePlanCollection().size());
        }

        if (eeDTO.getBirthDate() != null) {
            Assert.assertEquals("Birthdate", DateDTO.convertToSpcfCalendar(eeDTO.getBirthDate()), domainEmployee.getBirthDate().toLocal());
        } else {
            assertNull(domainEmployee.getBirthDate());
        }
        if (eeDTO.getTerminationDate() != null) {
            Assert.assertEquals("Termination date", DateDTO.convertToSpcfCalendar(eeDTO.getTerminationDate()), domainEmployee.getTerminationDate().toLocal());
        } else {
            assertNull(domainEmployee.getTerminationDate());
        }


        if (eeDTO.getEmployee401kInfo() != null) {
            ThirdParty401kEmployeeInfoDTO ee401kInfoDTO = eeDTO.getEmployee401kInfo();
            Assert.assertEquals("Email address", ee401kInfoDTO.getEmail(), domainEmployee.getEmail());
            Assert.assertEquals("Phone", ee401kInfoDTO.getPhoneNumber(), domainEmployee.getPhone());
            if (ee401kInfoDTO.getOwnershipPercent() != null) {
                Assert.assertEquals("Ownership Percent", ee401kInfoDTO.getOwnershipPercent().doubleValue(), domainEmployee.getThirdParty401kInfo().getOwnershipPercentage());
            } else {
                Assert.assertEquals("Ownership percent", new Double("0"), new Double(domainEmployee.getThirdParty401kInfo().getOwnershipPercentage()));
            }
            if (ee401kInfoDTO.isFamilyMember() != null) {
                Assert.assertEquals("Family Member", ee401kInfoDTO.isFamilyMember(), domainEmployee.getThirdParty401kInfo().getIsFamilyMember());
            } else {
                assertFalse(domainEmployee.getThirdParty401kInfo().getIsFamilyMember());
            }
            if (ee401kInfoDTO.isHighlyCompensatedEmployee() != null) {
                Assert.assertEquals("HCE", ee401kInfoDTO.isHighlyCompensatedEmployee(), domainEmployee.getThirdParty401kInfo().getIsHighlyCompensated());
            } else {
                assertFalse(domainEmployee.getThirdParty401kInfo().getIsHighlyCompensated());
            }
        }
    }

    public static void activateEntitlementUnit(EntitlementUnit entitlementUnit) {
        updateEntitlementUnitStatus(entitlementUnit, EntitlementUnitStatusCode.Activated);
    }

    public static void deactivateEntitlementUnit(EntitlementUnit entitlementUnit) {
        updateEntitlementUnitStatus(entitlementUnit, EntitlementUnitStatusCode.Deactivated);
    }
    public static void makeHistoricEntitlementUnit(EntitlementUnit entitlementUnit) {
        updateEntitlementUnitStatus(entitlementUnit, EntitlementUnitStatusCode.Historic);
    }

    private static void updateEntitlementUnitStatus(EntitlementUnit entitlementUnit, EntitlementUnitStatusCode status) {
        PayrollServices.beginUnitOfWork();
        Application.refresh(entitlementUnit);
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
        entitlementUnitDTO.setEntitlementUnitStatus(status);
        assertSuccess(PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(entitlementUnit.getCompany().getSourceSystemCd(),
                                                                                    entitlementUnit.getCompany().getSourceCompanyId(),
                                                                                    entitlementUnitDTO));
        PayrollServices.commitUnitOfWork();
    }

	public static EntitlementUnit addEntitlementUnit(Company pCompany, String pLicenseNumber, String pEntitlementOfferingCode) {
        return addDIYEntitlementUnit(pCompany, pLicenseNumber, pEntitlementOfferingCode, EditionType.Basic, NumberOfEmployeesType.UPTO3);
    }

    public static EntitlementUnit addDiskDeliveryEntitlementUnit(Company pCompany, String pLicenseNumber, String pEntitlementOfferingCode) {
        return addEntitlementUnit(pCompany, pLicenseNumber, pEntitlementOfferingCode, EditionType.Standard, NumberOfEmployeesType.UNLIMITED, AssetItemNumber.DIY_DISK_DELIVERY, SpcfCalendar.createInstance());
    }

    public static EntitlementUnit addDIYEntitlementUnit(Company pCompany, String pLicenseNumber, String pEntitlementOfferingCode, EditionType pEditionType, NumberOfEmployeesType pNumberOfEmployeesType) {
        return addEntitlementUnit(pCompany, pLicenseNumber, pEntitlementOfferingCode, pEditionType, pNumberOfEmployeesType, AssetItemNumber.DIY_YEARLY, null);
    }

    public static EntitlementUnit addAssistedAdvantageEntitlementUnit(Company pCompany, String pLicenseNumber, String pEntitlementOfferingCode) {
        return addEntitlementUnit(pCompany, pLicenseNumber, pEntitlementOfferingCode, null, null, AssetItemNumber.ASSISTED_ADVANTAGE, SpcfCalendar.createInstance());
    }

    public static EntitlementUnit addAssistedBundleEntitlementUnit(Company pCompany, String pLicenseNumber, String pEntitlementOfferingCode) {
        return addEntitlementUnit(pCompany, pLicenseNumber, pEntitlementOfferingCode, null, null, AssetItemNumber.ASSISTED_BUNDLE, SpcfCalendar.createInstance());
    }

    public static EntitlementUnit addEntitlementUnit(Company pCompany,
                                                     String pLicenseNumber,
                                                     String pEntitlementOfferingCode,
                                                     EditionType pEditionType,
                                                     NumberOfEmployeesType pNumberOfEmployeesType,
                                                     AssetItemNumber pAssetItemNumber,
                                                     SpcfCalendar pNextChargeDate) {
        return addEntitlementUnit(pCompany, pLicenseNumber, pEntitlementOfferingCode, pEditionType, pNumberOfEmployeesType, pAssetItemNumber, pNextChargeDate, null, null, null, null, "Contact Name", "contact@email.com", null);
    }

    public static EntitlementUnit addEntitlementUnit(Company pCompany,
                                                     String pLicenseNumber,
                                                     String pEntitlementOfferingCode,
                                                     EditionType pEditionType,
                                                     NumberOfEmployeesType pNumberOfEmployeesType,
                                                     AssetItemNumber pAssetItemNumber,
                                                     SpcfCalendar pNextChargeDate,
                                                     String pCCNumber,
                                                     String pCCType,
                                                     String pCCExpiration,
                                                     String pBillingZip,
                                                     String pContactName,
                                                     String pContactEmail,
                                                     SpcfCalendar pSubEndDate) {
        EntitlementUnitDTO entitlementUnitDTO = new EntitlementUnitDTO();
        entitlementUnitDTO.setLicenseNumber(pLicenseNumber);
        entitlementUnitDTO.setEntitlementOfferingCode(pEntitlementOfferingCode);
        entitlementUnitDTO.setEditionType(pEditionType);
        entitlementUnitDTO.setNumberOfEmployeesType(pNumberOfEmployeesType);
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingActivation);
        entitlementUnitDTO.setAssetItemNumber(pAssetItemNumber.toString());
        entitlementUnitDTO.setNextChargeDate(pNextChargeDate);
        entitlementUnitDTO.setFedTaxId(pCompany.getFedTaxId());
        entitlementUnitDTO.setCustomerId("CustomerId");
        entitlementUnitDTO.setCreditCardNumber(pCCNumber);
        entitlementUnitDTO.setCreditCardType(pCCType);
        entitlementUnitDTO.setCreditCardExpiration(pCCExpiration);
        entitlementUnitDTO.setBillingZipCode(pBillingZip);
        entitlementUnitDTO.setContactName(pContactName);
        entitlementUnitDTO.setContactEmail(pContactEmail);
        if(pCCNumber != null) {
            entitlementUnitDTO.setPaymentMethodType(EntitlementPaymentMethodType.CC);
        }
        entitlementUnitDTO.setSubscriptionEndDate(pSubEndDate);


        PayrollServices.beginUnitOfWork();
        pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        for (EntitlementUnit entitlementUnit : pCompany.getEntitlementUnitCollection()) {
            if(entitlementUnit.getEntitlement().getEntitlementCode().getAssetItemNumber().equals(pAssetItemNumber.toString()) &&
                    entitlementUnit.isActivated() &&
                    entitlementUnit.getEntitlement().getLicenseNumber().equals(pLicenseNumber) &&
                    entitlementUnit.getEntitlement().getEntitlementCode().equals(pEntitlementOfferingCode)) {
                PayrollServices.rollbackUnitOfWork();
                return entitlementUnit;
            } else {
                entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
            }
        }

        ProcessResult<EntitlementUnit> processResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(pCompany.getSourceSystemCd(),
                pCompany.getSourceCompanyId(),
                entitlementUnitDTO);
        assertSuccess("add entitlement ProcessResult", processResult);
        PayrollServices.commitUnitOfWork();
        return processResult.getResult();
    }

    public static EntitlementUnit addAssistedEntitlementUnit(Company pCompany, String pLicenseNumber, String pEntitlementOfferingCode, boolean activated) {
        EntitlementUnitDTO entitlementUnitDTO = new EntitlementUnitDTO();
        entitlementUnitDTO.setLicenseNumber(pLicenseNumber);
        entitlementUnitDTO.setEntitlementOfferingCode(pEntitlementOfferingCode);
        entitlementUnitDTO.setEditionType(null);
        entitlementUnitDTO.setNumberOfEmployeesType(null);
        entitlementUnitDTO.setCustomerId(pCompany.getSourceCompanyId());
        entitlementUnitDTO.setEntitlementUnitStatus(activated ? EntitlementUnitStatusCode.Activated : EntitlementUnitStatusCode.PendingActivation);
        entitlementUnitDTO.setAssetItemNumber(AssetItemNumber.ASSISTED.toString());
        entitlementUnitDTO.setFedTaxId(pCompany.getFedTaxId());

        PayrollServices.beginUnitOfWork();
        pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        for (EntitlementUnit entitlementUnit : pCompany.getEntitlementUnitCollection()) {
            if(entitlementUnit.getEntitlement().getEntitlementCode().isAssisted() && entitlementUnit.isActivated()) {
                PayrollServices.rollbackUnitOfWork();
                return entitlementUnit;
            }
        }

        ProcessResult<EntitlementUnit> processResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(pCompany.getSourceSystemCd(),
                pCompany.getSourceCompanyId(),
                entitlementUnitDTO);
        assertSuccess("add entitlement ProcessResult", processResult);
        PayrollServices.commitUnitOfWork();
        return processResult.getResult();
    }

    public static Entitlement disableEntitlement(Entitlement entitlement) {
        PayrollServices.beginUnitOfWork();
        EntitlementDTO assistedEntitlementDTO = PayrollServices.dtoFactory.create(entitlement);
        assistedEntitlementDTO.setEntitlementState(EntitlementStateCode.Disabled);
        PayrollServices.rollbackUnitOfWork();
        return DataLoadServices.updateEntitlement(assistedEntitlementDTO);
    }

    public static Entitlement updateEntitlement(EntitlementDTO pEntitlementDTO) {
        PayrollServices.beginUnitOfWork();
        ProcessResult<Entitlement> processResult = PayrollServices.entitlementManager.updateEntitlement(pEntitlementDTO);
        assertSuccess("update entitlement ProcessResult", processResult);
        PayrollServices.commitUnitOfWork();
        return processResult.getResult();

    }
    public static void updateIRSPaymentTemplateSupportDate(SpcfCalendar pSupportedDate) {
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", pSupportedDate);
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", pSupportedDate);
    }

    public static void updateCAEDDPaymentTemplateSupportDate(SpcfCalendar pSupportedDate) {
        DataLoadServices.updatePaymentTemplateSupportedDate("CA-UIETT-PAYMENT", pSupportedDate);
        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", pSupportedDate);
    }
    public static void updateMAPaymentTemplateSupportDate(SpcfCalendar pSupportedDate) {
        DataLoadServices.updatePaymentTemplateSupportedDate("MA-1700HI-PAYMENT", pSupportedDate);
        DataLoadServices.updatePaymentTemplateSupportedDate("MA-M941-PAYMENT", pSupportedDate);
    }

    public static PaymentTemplate updatePaymentTemplateSupportedDate(String pPaymentTemplateCd, SpcfCalendar pSupportedDate) {
        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, pPaymentTemplateCd);
        paymentTemplate.setSupportStartDate(pSupportedDate);
        PayrollServices.commitUnitOfWork();
        return paymentTemplate;
    }

    public static void updateEffectiveDepositFreqEffDateToQuarterly(String pCompId, String pPaymentTemplate) {
        updateEffectiveDepositFreqEffDate(pCompId, pPaymentTemplate, DepositFrequencyCode.QUARTERLY);
    }

    public static void updateEffectiveDepositFreqEffDate(String pCompId, String pPaymentTemplate, DepositFrequencyCode pDepositFrequencyCode) {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(pCompId, SourceSystemCode.QBDT);
        SpcfCalendar firstDayOfServiceStartYear = CalendarUtils.getFirstDayOfTheYearLocal(company.getService(ServiceCode.Tax).getServiceStartDate());
        PayrollServices.rollbackUnitOfWork();

        updateEffectiveDepositFreqEffDate(pCompId, pPaymentTemplate, pDepositFrequencyCode, firstDayOfServiceStartYear);
    }

    public static void updateEffectiveDepositFreqEffDate(String pPSID, String pPaymentTemplate, DepositFrequencyCode pDepositFrequencyCode, SpcfCalendar pEffectiveDate) {
        if (!pEffectiveDate.isLocal()) {
            pEffectiveDate = SpcfCalendar.createInstance(pEffectiveDate.getYear(), pEffectiveDate.getMonth(), pEffectiveDate.getDay(), SpcfTimeZone.getLocalTimeZone());
        }
        PayrollServices.beginUnitOfWork();

        EffectiveDepositFrequencyDTO dto = new EffectiveDepositFrequencyDTO();
        PaymentTemplate paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, pPaymentTemplate);

        dto.setAgencyId(paymentTemplate.getAgency().getAgencyId());
        dto.setPaymentTemplateCd(pPaymentTemplate);
        dto.setPaymentFrequencyId(pDepositFrequencyCode);
        dto.setEffectiveDate(pEffectiveDate);

        ProcessResult processResultUf = PayrollServices.paymentManager.updateDepositFrequency(SourceSystemCode.QBDT, pPSID, dto);

        assertSuccess(processResultUf);

        PayrollServices.commitUnitOfWork();
    }

    public static void invalidateDepositFrequencies(Company company, String paymentTemplate) {
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EffectiveDepositFrequency> validEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, PaymentTemplate.findPaymentTemplate(paymentTemplate), null, true);
        for (EffectiveDepositFrequency validEffectiveDepositFrequency : validEffectiveDepositFrequencies) {
            EffectiveDepositFrequencyDTO effectiveDepositFrequencyDTO = PayrollServices.dtoFactory.create(validEffectiveDepositFrequency);
            assertSuccess(PayrollServices.paymentManager.invalidateDepositFrequency(SourceSystemCode.QBDT, company.getSourceCompanyId(), effectiveDepositFrequencyDTO));
        }
        PayrollServices.commitUnitOfWork();
    }

    public static void setPrincipalToQBDT() {
        PayrollServices.beginUnitOfWork();
        PayrollServices.setCurrentPrincipal(SystemPrincipal.QBDTAdapter);
        PayrollServices.commitUnitOfWork();
    }

    public static void setPrincipalToDataSync() {
        PayrollServices.beginUnitOfWork();
        PayrollServices.setCurrentPrincipal(SystemPrincipal.AS400DataSyncBatchJob);
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Sets the current principal to a user.  The user will be created if needed.
     * The user will be deleted when running the next test
     *
     * @param operationIds optional any operations on the user
     */
    public static void setPrincipalToAgent(OperationId... operationIds) {
        PayrollServices.beginUnitOfWork();
        AuthRole role = AuthRole.findRole("UnitTestRole");
        if (role == null) {
            UserRoleDTO roleDTO = new UserRoleDTO();
            roleDTO.setRoleId("UnitTestRole");
            roleDTO.setName("UnitTestRole");
            roleDTO.setDomainId("DDUI");
            roleDTO.setDescription("Unit test role");
            roleDTO.setOperationIds(new ArrayList<OperationId>());
            roleDTO.getOperationIds().add(OperationId.AccessApplication);
            role = assertSuccessResult(PayrollServices.userManager.addRole(roleDTO));
        }

        role.getAuthOperationCollection().clear();
        for (OperationId operationId : operationIds) {
            role.getAuthOperationCollection().add(Application.<AuthOperation>findById(AuthOperation.class, operationId));
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        AuthUser user = AuthUser.findUser("UnitTestAgent");
        if (user == null) {
            ProcessResult<AuthUser> processResult =
                    PayrollServices.userManager.addUser("UnitTestAgent", Arrays.asList(role.getRoleId()), "First", "Last");
            assertSuccess("Add User ProcessResult ", processResult);
            user = processResult.getResult();
        }

        PayrollServices.setCurrentPrincipal(new PspPrincipal(user.getCorpId(), user.getFirstName()));

        PayrollServices.commitUnitOfWork();
    }

    public static void offloadAgencyTaxCredits(PaymentTemplate pPaymentTemplate) {
        offloadAgencyTaxCredits(pPaymentTemplate, null);
    }

    public static void offloadAgencyTaxCredits(PaymentTemplate pPaymentTemplate, Company company) {
        PayrollServices.beginUnitOfWork();
        Criterion<MoneyMovementTransaction> exp = MoneyMovementTransaction.PaymentTemplate().equalTo(pPaymentTemplate).And(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend, TaxPaymentStatus.ATFFinalized));
        if (company != null) {
            exp = exp.And(MoneyMovementTransaction.Company().equalTo(company));
        }
        for (MoneyMovementTransaction mmt : Application.find(MoneyMovementTransaction.class, exp)) {
            SpcfCalendar newInitiationDate = PSPDate.getPSPTime();
            CalendarUtils.clearTime(newInitiationDate);
            mmt.updateTaxInitiationDate(newInitiationDate);
        }
        PayrollServices.commitUnitOfWork();

        if (pPaymentTemplate.isIRS941() || pPaymentTemplate.getPaymentTemplateCd().equals("IRS-940-PAYMENT")) {
            BatchJobManager.runJob(BatchJobType.EftpsPayment);
        } else {
            SftpFactory.setInstanceClass(MockSimpleSftpFile.class);
            runOffloadTaxPayments(PSPDate.getPSPTime());
            BatchJobManager.runJob(BatchJobType.PrintedCheckBatch);
        }

    }

    public static void returnAgencyTaxCredits(PaymentTemplate paymentTemplate) {
        /*todo make this work for IRS.  Currently would require circular dependencies:
        EftpsDataLoader.overridePendingToCompletedStatus();

        EftpsDataLoader.callRejectSimulator();

        //Process simulator generated files.
        EdiManager.processWaitingResponseFiles();*/

        PayrollServices.beginUnitOfWork();
        for (MoneyMovementTransaction moneyMovementTransaction : MoneyMovementTransaction.findTaxPayments().setExecutedOrSuccessful().setPaymentTemplate(paymentTemplate).find()) {
            assertSuccess(PayrollServices.paymentManager.rejectPayment(moneyMovementTransaction.getId().toString(), "Big Problem Exception"));
        }
        PayrollServices.commitUnitOfWork();

    }

    public static void returnTxns(PayrollRun payrollRun, TransactionTypeCode... transactionTypeCodes) {
        Application.beginUnitOfWork();
        Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> c1FinTxns = payrollRun.getFinancialTransactionCollection()
                .find(FinancialTransaction.TransactionType().TransactionTypeCd().in(transactionTypeCodes)
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Executed)));

        assertTrue(c1FinTxns.size() > 0);

        Application.commitUnitOfWork();

        returnTxns(c1FinTxns);
    }

    public static void createNocForTxns(PayrollRun payrollRun, TransactionTypeCode... transactionTypeCodes) {
        Application.beginUnitOfWork();
        Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> c1FinTxns = payrollRun.getFinancialTransactionCollection()
                .find(FinancialTransaction.TransactionType().TransactionTypeCd().in(transactionTypeCodes)
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Executed)));

        assertTrue(c1FinTxns.size() > 0);

        Application.commitUnitOfWork();

        nocReturnTxns(c1FinTxns);
    }

    public static PayrollRunDTO createPayrollRunWithLawsAmountsAndFICATips(PayrollRunDTO pPayrollRunDTO, Company pCompany, DateDTO payrollDate, List<Employee> pEmployees, String[] pLawIds, String[] pAmounts) {

        PayrollSubmitDataLoader payrollSubmitDataLoader = new PayrollSubmitDataLoader();
        pPayrollRunDTO.setTargetPayrollTXDate(payrollDate);
        pPayrollRunDTO.setPayrollTXBatchId("Batch_" + ++payrollBatchId);

        Collection<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();
        for (Employee employee : pEmployees) {
            employee = Employee.findEmployee(pCompany, employee.getSourceEmployeeId());
            // Create Paycheck splits
            Collection<DDTransactionDTO> ddTransactions = new ArrayList<DDTransactionDTO>();
            DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = employee.getEmployeeBankAccountCollection();
            for (int i = 0; i < employeeBankAccounts.size(); i++) {
                if (employeeBankAccounts.get(i).getStatusCd().equals(BankAccountStatus.Active)) {
                    ddTransactions.add(createDDTransactionDTO(createEmployeeBankAccount(employeeBankAccounts.get(i)), new BigDecimal(i + 1)));
                }
            }

            // Create Paycheck
            PaycheckDTO paycheckDTO = createPaycheckDTO(ddTransactions, employee.getSourceEmployeeId(), SpcfUniqueId.generateRandomUniqueIdString());
            QBDTPaycheckInfoDTO qbdtPaycheckInfoDTO = new QBDTPaycheckInfoDTO();
            qbdtPaycheckInfoDTO.setAccountName("AcctName" + paycheckDTO.getEmployeeId());
            qbdtPaycheckInfoDTO.setCheckNumber("ChkNumber");
            qbdtPaycheckInfoDTO.setCleared("1");
            qbdtPaycheckInfoDTO.setMemo("Memo" + paycheckDTO.getEmployeeId());
            qbdtPaycheckInfoDTO.setOnService(true);
            qbdtPaycheckInfoDTO.setProrate(true);
            qbdtPaycheckInfoDTO.setTrackingClass("TC");

            paycheckDTO.setQBDTPaycheckInfoDTO(qbdtPaycheckInfoDTO);
            paychecks.add(paycheckDTO);

            Collection<LiabilityTransactionDTO> liabilityTransactions = new ArrayList<LiabilityTransactionDTO>();

            for (int i = 0; i < pLawIds.length; i++) {
                LiabilityTransactionDTO liabilityTxDTO = payrollSubmitDataLoader.createLiabilityTransactionDTO(paycheckDTO);
                liabilityTxDTO.setLiabilityAmount(new BigDecimal(pAmounts[i]));
                liabilityTxDTO.setLiabilityTaxableWages(new BigDecimal(pAmounts[i]).multiply(new BigDecimal("10"))); //simulate this tax is 10% of taxable wages
                if (pLawIds[i].equals("61") || pLawIds[i].equals("62")) {
                    /*  simulate this tip is 5% of taxable wages (only for FICA EE & FICA ER)   */
                    liabilityTxDTO.setLiabilityTipsTaxableWages(new BigDecimal(pAmounts[i]).multiply(new BigDecimal("0.5")));
                }
                liabilityTxDTO.setLawId(pLawIds[i]);
                liabilityTransactions.add(liabilityTxDTO);
                liabilityTxDTO.setPayrollItemId(CompanyLaw.findCompanyLaw(pCompany, pLawIds[i]).getSourceId());
            }
            paycheckDTO.setLiabilityTransactions(liabilityTransactions);
        }

        pPayrollRunDTO.setPaychecks(paychecks);

        return pPayrollRunDTO;
    }

    public static void returnTxns(DomainEntitySet<FinancialTransaction> financialTransactions) {
        returnTxns(financialTransactions, "R01", "NSF return");
    }

    public static void returnTxns(DomainEntitySet<FinancialTransaction> financialTransactions, String pReturnCode, String pReturnDisc) {
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = new DomainEntitySet<MoneyMovementTransaction>();
        Application.beginUnitOfWork();
        for (FinancialTransaction ft : financialTransactions) {
            Application.refresh(ft);
            if(ft.getMoneyMovementTransaction() != null) {
                moneyMovementTransactions.add(ft.getMoneyMovementTransaction());
            }
        }

        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.persistTransactionReturns(financialTransactions, pReturnCode, pReturnDisc);
        Application.commitUnitOfWork();

        assertEquals("Transaction return count", moneyMovementTransactions.size(), returnList.size());

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            Application.beginUnitOfWork();
            currRet = Application.findById(TransactionReturn.class, currRet.getId());
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
            Application.commitUnitOfWork();
        }
    }

    public static void nocReturnTxns(DomainEntitySet<FinancialTransaction> financialTransactions) {
        returnTxns(financialTransactions, "C02", "111000025");
    }

    public static void assertTaxSettlementDate(FinancialTransaction financialTransaction) {
        SpcfCalendar initDate = financialTransaction.getMoneyMovementTransaction().getInitiationDate().copy();
        CalendarUtils.addBusinessDays(initDate, 1);
        assertEquals(initDate, financialTransaction.getSettlementDate());
    }

    public static void assertLedgerBalances(Company company, LB... balances) {
        assertLedgerBalances(company, null, balances);
    }

    public static void assertLedgerBalances(Company company, PayrollRun payrollRun, LB... balances) {
        Set<LedgerAccountCode> allAccounts = new HashSet<LedgerAccountCode>(Arrays.asList(LedgerAccountCode.values()));
        for (LB balance : balances) {
            assertLedgerBalance(company, payrollRun, balance);
            allAccounts.remove(balance.lac);
        }
        for (LedgerAccountCode zeroAccount : allAccounts) {
            SpcfDecimal actual;
            if (payrollRun != null) {
                actual = LedgerAccount.getLedgerAccountBalanceByPayroll(zeroAccount, payrollRun.getSourcePayRunId(), company);
            } else {
                actual = LedgerAccount.getLedgerAccountBalance(company, zeroAccount);
            }
            assertEquals(zeroAccount.toString(), SpcfMoney.ZERO, actual);
        }
    }

    public static void assertLedgerBalance(Company company, LB balance) {
        assertLedgerBalance(company, null, balance);
    }

    public static void assertLedgerBalance(Company company, PayrollRun payrollRun, LB balance) {
        SpcfDecimal actual;
        if (payrollRun != null) {
            actual = LedgerAccount.getLedgerAccountBalanceByPayroll(balance.lac, payrollRun.getSourcePayRunId(), company);
        } else {
            actual = LedgerAccount.getLedgerAccountBalance(company, balance.lac);
        }
        assertEquals(balance.lac.toString(), balance.balance, actual);
    }

    /**
     * @deprecated use {@link com.intuit.sbd.payroll.psp.util.SystemParameterTestUtils#updateAndSavePrevious(SystemParameter.Code, String)} instead
     *
     *     @After
     *     public void cleanup() {
     *         SystemParameterTestUtils.restoreChangedSystemParameters();
     *     }
     *
     *     @Test
     *     public void someTest() {
     *         SystemParameterTestUtils.updateAndSavePrevious(SystemParameter.Code.SOME_CODE, "aValue");
     *     }
     */
    @Deprecated
    public static void updateSystemParameter(SystemParameter.Code pCode, String pValue) {
        PayrollServices.beginUnitOfWork();
        SystemParameter.update(pCode, pValue);
        PayrollServices.commitUnitOfWork();
    }

    public static Company setupCompanyWithNegativeLiability_IRS_NM() {
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("NM-WC1-PAYMENT");
        updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("191", "-10");
        lawAmounts.put("192", "5");
        lawAmounts.put("61", "12");
        lawAmounts.put("62", "11");
        lawAmounts.put("63", "13");
        lawAmounts.put("64", "14");
        lawAmounts.put("1", "1");
        lawAmounts.put("65", "15");

        List<String> federalLawIds = Arrays.asList("61", "62", "63", "64", "65", "1");

        SpcfCalendar beginDate = SpcfCalendar.createInstance(2011, 1, 1);
        setPSPDate(beginDate);

        String[] statesList = new String[]{"NM"};

        List<Company> companies = setupCompany(12345678L, 1, statesList, PaymentTemplateCategory.Withholding);

        setPSPDate(SpcfCalendar.createInstance(2011, 8, 10));
        Company company = companies.get(0);
        DataLoadServices.claimOffer(company, DataLoadServices.WAIVE_ALL_FEES);

        DateDTO payrollRunDate = new DateDTO("2011-08-12");

        List<Employee> employees = Arrays.asList(Employee.findEmployees(company).toArray(new Employee[]{}));
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = createPayrollRun(payrollRunDTO, company, payrollRunDate, employees, lawAmounts);
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            for (LiabilityTransactionDTO liabilityTransactionDTO : paycheckDTO.getLiabilityTransactions()) {
                if(federalLawIds.contains(liabilityTransactionDTO.getLawId())) {
                    liabilityTransactionDTO.setLiabilityAmount(liabilityTransactionDTO.getLiabilityAmount().multiply(BigDecimal.valueOf(-1)).subtract(BigDecimal.valueOf(1)));
                }
            }
            break;
        }
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();
        return company;

    }

    public static class LB {
        public LedgerAccountCode lac;
        public SpcfMoney balance;

        public LB(LedgerAccountCode lac, double balance) {
            this.lac = lac;
            this.balance = new SpcfMoney(Double.toString(balance));
        }
    }

    public static enum IntuitBankAccountType {
        Fee, EE_Return, ER_Return, DD, Tax
    }

    public static void assertIntuitBankAccounts(FinancialTransaction ft, IntuitBankAccountType debitBankAccount, IntuitBankAccountType creditBankAccount) {
        assertNotNull(ft);
        assertNotNull(ft.getCreditBankAccount());
        assertNotNull(ft.getDebitBankAccount());
        IntuitBankAccount creditIBA = IntuitBankAccount.findIntuitBankAccount(ft.getCreditBankAccount());
        IntuitBankAccount debitIBA = IntuitBankAccount.findIntuitBankAccount(ft.getDebitBankAccount());
        assertNotNull(creditIBA);
        assertNotNull(debitIBA);
        String debitBankAccountString = "INTUIT " + debitBankAccount.name().replace('_', ' ').toUpperCase();
        String creditBankAccountString = "INTUIT " + creditBankAccount.name().replace('_', ' ').toUpperCase();
        assertEquals("Debit bank account", debitBankAccountString, debitIBA.getDescription());
        assertEquals("Credit bank account", creditBankAccountString, creditIBA.getDescription());
    }

    public static AgencyIdRequirement getAIDRequirement(String paymentTemplateCd) {
        PayrollServices.beginUnitOfWork();
        AgencyIdRequirement aidRequirement = null;
        for (PaymentMethodRequirement paymentMethodRequirement : assertOne(Application.find(PaymentTemplatePaymentMethod.class, PaymentTemplatePaymentMethod.PaymentMethod().equalTo(PaymentMethod.ACHCredit)
                .And(PaymentTemplatePaymentMethod.PaymentTemplate().PaymentTemplateCd().equalTo(paymentTemplateCd)))).getPaymentMethodRequirementCollection()) {
            if (paymentMethodRequirement instanceof AgencyIdRequirement) {
                if (((AgencyIdRequirement) paymentMethodRequirement).getPaymentTemplateAgencyId() == null) {
                    aidRequirement = (AgencyIdRequirement) paymentMethodRequirement;
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();
        return aidRequirement;

    }

    public static String readFile(String pFileName) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            FileReader fileReader = new FileReader(new File(pFileName));
            BufferedReader input = new BufferedReader(fileReader);
            try {
                String line;
                while ((line = input.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }

            return stringBuilder.toString();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static void reinitialize() {
        employeeId = 0;
        companyId = 0;
        fedTaxId = 0;
        bankAccountId = 0;
        payrollBatchId = 0;
        paycheckId = 0;
        payrollItemId = 0;
        billPaymentId = 0;
        billPaymentSplitId = 0;
    }

    public static String nextCompanyId() {
        return Integer.toString(++companyId);
    }

    public static void submitPayment(SpcfCalendar pInitiationDate) {
        if(pInitiationDate != null) {
            DataLoadServices.setPSPDate(pInitiationDate);
        }
        BatchJobManager.runJob(BatchJobType.EftpsPayment);
    }

    /*
     *
     *  probably bad and redundant payment finders, todo refactor
     *
     */

    public static DomainEntitySet<MoneyMovementTransaction> getReadyToSendNonDirectPayments(Company pCompany) {
        Expression<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>()
                .Where(MoneyMovementTransaction.Company().equalTo(pCompany)
                        .And(BaseMoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend))
                        .And(BaseMoneyMovementTransaction.Status().in(PaymentStatus.Created, PaymentStatus.OnHold))
                        .And(BaseMoneyMovementTransaction.MoneyMovementPaymentMethod().in(getNonDirectTaxPaymentMethods())));
        return Application.find(MoneyMovementTransaction.class, query);
    }

    public static PaymentMethod[] getNonDirectTaxPaymentMethods() {
        return new PaymentMethod[]{PaymentMethod.EFTPS, PaymentMethod.ACHCredit, PaymentMethod.CheckPayment, PaymentMethod.SuperCheck};
    }


    public static DomainEntitySet<MoneyMovementTransaction> getReadyToSendTaxPayments(Company pCompany, PaymentMethod pPaymentMethod) {
        Expression<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>()
                .Where(MoneyMovementTransaction.Company().equalTo(pCompany)
                        .And(BaseMoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend))
                        .And(BaseMoneyMovementTransaction.Status().in(PaymentStatus.Created, PaymentStatus.OnHold))
                        .And(BaseMoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(pPaymentMethod)));
        return Application.find(MoneyMovementTransaction.class, query);
    }

    public static DomainEntitySet<MoneyMovementTransaction> getOnHoldTaxPayments(Company pCompany, String pPaymentTemplateCd) {
        return getOnHoldTaxPayments(pCompany, pPaymentTemplateCd, null, null);
    }

    public static DomainEntitySet<MoneyMovementTransaction> getOnHoldTaxPayments(Company pCompany, String pPaymentTemplateCd, SpcfCalendar pPeriodBeginDate, SpcfCalendar pPeriodEndDate) {
        return MoneyMovementTransaction.findTaxPayments()
                .setCompany(pCompany)
                .setPaymentTemplateCd(pPaymentTemplateCd)
                .setPeriodBeginDate(pPeriodBeginDate)
                .setPeriodEndDate(pPeriodEndDate)
                .setOnHold()
                .setNonDirect()
                .find()
                .sort(MoneyMovementTransaction.InitiationDate());
    }

    public static DomainEntitySet<MoneyMovementTransaction> getReadyToSendTaxPayments(Company pCompany, String pPaymentTemplateCd) {
        return getReadyToSendTaxPayments(pCompany, pPaymentTemplateCd, null, null);
    }

    public static DomainEntitySet<MoneyMovementTransaction> getATFFinalizedTaxPayments(Company pCompany, String pPaymentTemplateCd) {
        return getATFFinalizedTaxPayments(pCompany, pPaymentTemplateCd, null, null);
    }


    public static DomainEntitySet<MoneyMovementTransaction> getReadyToSendTaxPayments(Company pCompany, String pPaymentTemplateCd, SpcfCalendar pPeriodBeginDate, SpcfCalendar pPeriodEndDate) {
        return MoneyMovementTransaction.findTaxPayments()
                .setCompany(pCompany)
                .setPaymentTemplateCd(pPaymentTemplateCd)
                .setPeriodBeginDate(pPeriodBeginDate)
                .setPeriodEndDate(pPeriodEndDate)
                .setReadyToSend()
                .setNonDirect()
                .find()
                .sort(MoneyMovementTransaction.InitiationDate());
    }

    public static DomainEntitySet<MoneyMovementTransaction> getATFFinalizedTaxPayments(Company pCompany, String pPaymentTemplateCd, SpcfCalendar pPeriodBeginDate, SpcfCalendar pPeriodEndDate) {
        return MoneyMovementTransaction.findTaxPayments()
                .setCompany(pCompany)
                .setPaymentTemplateCd(pPaymentTemplateCd)
                .setPeriodBeginDate(pPeriodBeginDate)
                .setPeriodEndDate(pPeriodEndDate)
                .setATFFinalized()
                .setNonDirect()
                .find()
                .sort(MoneyMovementTransaction.InitiationDate());
    }

    public static void resetAllPaymentTemplateSupportDates() {
        resetAllPaymentTemplateSupportDates(false);
    }

    public static void resetAllPaymentTemplateSupportDates(boolean alwaysReset) {
        //don't actually do do anything if the users have been set (i.e. being run for some UI testing)
        if (!alwaysReset && System.getProperty("insertTestUsers") != null) {
            return;
        }

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
        DomainEntitySet<PaymentTemplate> paymentTemplates = Application.find(PaymentTemplate.class, PaymentTemplate.SupportStartDate().isNotNull());
        for (PaymentTemplate paymentTemplate : paymentTemplates) {
            paymentTemplate.setSupportStartDate(null);
        }
        PayrollServices.commitUnitOfWork();

        resetAllPaymentTemplateProcessDates();
    }

    public static void setAllPaymentTemplateProcessDates() {
        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
        DomainEntitySet<PaymentTemplate> paymentTemplates = Application.find(PaymentTemplate.class, PaymentTemplate.SupportStartDate().isNotNull());
        for (PaymentTemplate paymentTemplate : paymentTemplates) {
            paymentTemplate.setProcessingStartDate(paymentTemplate.getSupportStartDate());
        }
        PayrollServices.commitUnitOfWork();
    }

    public static void resetAllPaymentTemplateProcessDates() {
        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
        DomainEntitySet<PaymentTemplate> paymentTemplates = Application.find(PaymentTemplate.class, PaymentTemplate.SupportStartDate().isNotNull());
        for (PaymentTemplate paymentTemplate : paymentTemplates.find(PaymentTemplate.PaymentTemplateCd().in("IRS-940-PAYMENT", "IRS-941-PAYMENT"))) {
            paymentTemplate.setProcessingStartDate(SpcfCalendar.createInstance(2011, 4, 1));
        }
        for (PaymentTemplate paymentTemplate : paymentTemplates.find(PaymentTemplate.PaymentTemplateCd().in("AR-941M-PAYMENT", "AZ-A1-PAYMENT", "CA-PITSDI-PAYMENT", "CO-DR1094-PAYMENT", "MN-MW1-PAYMENT", "MT-MW1-PAYMENT", "NC-NC5P-PAYMENT", "OH-IT501-PAYMENT", "OR-OTCWH-PAYMENT", "SC-WH1601-PAYMENT"))) {
            paymentTemplate.setProcessingStartDate(SpcfCalendar.createInstance(2011, 10, 1));
        }
        for (PaymentTemplate paymentTemplate : paymentTemplates.find(PaymentTemplate.PaymentTemplateCd().in("AK-AKNS-PAYMENT", "AL-CR4UI-PAYMENT", "AL-CR4WH-PAYMENT", "AR-209B-PAYMENT", "AZ-UC018-PAYMENT", "CA-UIETT-PAYMENT", "CO-UITR1-PAYMENT", "CT-2MAG-PAYMENT", "CT-CTWH-PAYMENT", "DC-FR900-PAYMENT", "DC-UC30-PAYMENT", "DE-DES-PAYMENT", "DE-UC8-PAYMENT", "FL-UCT6-PAYMENT", "GA-DOL4-PAYMENT", "GA-GAV-PAYMENT", "HI-UCB6-PAYMENT", "HI-VP1-PAYMENT", "IA-44105-PAYMENT", "IA-600103-PAYMENT", "ID-020-PAYMENT", "ID-910-PAYMENT", "IL-501-PAYMENT", "IL-UI340-PAYMENT", "KS-KCNS100-PAYMENT", "KS-KW5-PAYMENT", "KY-K1-PAYMENT", "KY-UI3-PAYMENT", "LA-ES61-PAYMENT", "LA-L1-PAYMENT", "MA-1700HI-PAYMENT", "MA-M941-PAYMENT", "MD-DLLR-PAYMENT", "MD-MW506-PAYMENT", "ME-900ME-PAYMENT", "ME-941C1ME-PAYMENT", "MI-MW106-PAYMENT", "MI-UIA1020-PAYMENT", "MN-DEED1-PAYMENT", "MO-941-PAYMENT", "MO-MODES-PAYMENT", "MS-M89-PAYMENT", "MS-UI23-PAYMENT", "MT-UI5-PAYMENT", "NC-101-PAYMENT", "ND-306-PAYMENT", "ND-SFN41263-PAYMENT", "NE-941N-PAYMENT", "NE-UI11T-PAYMENT", "NH-DES200-PAYMENT", "NJ-NJ927PUI-PAYMENT", "NJ-NJ927PWH-PAYMENT", "NM-CRS1-PAYMENT", "NM-ES903A-PAYMENT", "NM-WC1-PAYMENT", "NV-NUCS4072-PAYMENT", "NY-1MN-PAYMENT", "NY-45MN-PAYMENT", "NY-MTA305-PAYMENT", "OH-JFS20127-PAYMENT", "OK-OES3-PAYMENT", "OK-OW9A-PAYMENT", "OR-OTCUI-PAYMENT", "PA-501-PAYMENT", "PA-UC2-PAYMENT", "RI-941-PAYMENT", "RI-TX17-PAYMENT", "SC-UCE120-PAYMENT", "SD-UID21-PAYMENT", "TN-LB0456-PAYMENT", "TX-C3V-PAYMENT", "UT-F3-PAYMENT", "UT-TC96-PAYMENT", "VA-FC20-PAYMENT", "VA-VA15-PAYMENT", "VT-C101-PAYMENT", "VT-WH433-PAYMENT", "WA-F5208-PAYMENT", "WI-UCT101-PAYMENT", "WI-WT6-PAYMENT", "WV-A154-PAYMENT", "WV-IT101-PAYMENT", "WY-WYO056-PAYMENT"))) {
            paymentTemplate.setProcessingStartDate(SpcfCalendar.createInstance(2012, 1, 1));
        }

        PayrollServices.commitUnitOfWork();
    }

    public enum AssetItemNumber {
        DIY_YEARLY ("1099581"),
        DIY_MONTHLY ("1099580"),
        DIY_DISK_DELIVERY ("1099574"),
        DIY_USAGE_BILLING_YEARLY ("1100521"),
        DIY_USAGE_BILLING_MONTHLY ("1100520"),
        DIY_USAGE_BILLING_LOWBASE ("1101313"),
        ASSISTED ("1099750"),
        ASSISTED_SYMPHONY ("1100860"),
        ASSISTED_ADVANTAGE ("1099753"),
        EMPLOYEE_ORGANIZER ("1099598"),
        EMPLOYMENT_REGULATION ("1099597"),
        ASSISTED_SYMPHONY_USAGE ("1101575"),
        DIY_DD_STD ("1099736"),
        DIY_DD_YEAREND ("1101754"),
        ASSISTED_BUNDLE ("1400076");

        private String value;
        private AssetItemNumber(String pValue) {
            value = pValue;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static int getEmployeeCount() {
        return employeeCount;
    }

    public static void setEmployeeCount(int employeeCount) {
        DataLoadServices.employeeCount = employeeCount;
    }

    public static int getPayrollCount() {
        return payrollCount;
    }

    public static void setPayrollCount(int payrollCount) {
        DataLoadServices.payrollCount = payrollCount;
    }

    public static boolean isLoadAdditionalSavingsAccount() {
        return loadAdditionalSavingsAccount;
    }

    public static void setLoadAdditionalSavingsAccount(boolean loadAdditionalSavingsAccount) {
        DataLoadServices.loadAdditionalSavingsAccount = loadAdditionalSavingsAccount;
    }

    public static void claimOffer(Company pCompany, String pOfferCd) {
        PayrollServices.beginUnitOfWork();
        pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        assertSuccess(PayrollServices.companyManager.claimOfferForCompany(pOfferCd, null, pCompany));
        PayrollServices.commitUnitOfWork();
    }

    public static void claimNoFeesOffer(Company pCompany) {
        DataLoadServices.claimOffer(pCompany, WAIVE_ALL_FEES);
    }

    public static void updateOffering(Company pCompany, OfferingCode pOfferingCode, String pOfferingSKU) {
        PayrollServices.beginUnitOfWork();
        OfferingInfoDTO offeringInfoDTO = new OfferingInfoDTO();
        offeringInfoDTO.setSKU(pOfferingSKU);
        offeringInfoDTO.setOfferingCode(pOfferingCode);
        assertSuccess(PayrollServices.companyManager.updateCompanyOffering(pCompany.getSourceSystemCd(),
                                                                   pCompany.getSourceCompanyId(),
                                                                   offeringInfoDTO));
        PayrollServices.commitUnitOfWork();
    }

    public static void updateProcessRequestFlag(Company pCompany, boolean pShouldProcessRequests) {
        PayrollServices.beginUnitOfWork();
        pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(pCompany);
        companyDTO.getQuickBooksInfo().setProcessTransmissions(pShouldProcessRequests);
        assertSuccess(PayrollServices.companyManager.updateQBCompanyInfo(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), companyDTO));
        PayrollServices.commitUnitOfWork();
    }

    public static void resetQbdtSourceCompanyIdSeq() {
        PayrollServices.beginUnitOfWork();
        Application.executeSqlCommand("DROP SEQUENCE SEQ_QBDT_SOURCE_COMPANY_ID", true);
        Application.executeSqlCommand("CREATE SEQUENCE SEQ_QBDT_SOURCE_COMPANY_ID INCREMENT BY 1 MINVALUE 100000000 MAXVALUE 308999999", true);
        PayrollServices.commitUnitOfWork();
    }

    public static void updateWageLimits(String pYear, String pQuarter) {
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<WageLimit> wageLimits = Application.find(WageLimit.class, WageLimit.EffectiveYearQuarter().like(pYear+"%"));
        if (wageLimits.size() == 0) {
        	int quarter = Integer.parseInt(pQuarter);
			for (int currentQuarter = quarter; currentQuarter <= 4; currentQuarter++) {
				String currentYearQuarter = pYear + currentQuarter;
				SQLQuery query = Application.getHibernateSession().createSQLQuery(
						"insert into psp_wage_limit (wage_limit_id, version, effective_year_quarter, amount, law_fk) ("
								+ " select lpad(law_fk, 4, '0')||'" + currentYearQuarter + "', 1, '" + currentYearQuarter
								+ "', amount, law_fk from psp_wage_limit where effective_year_quarter = (select max(effective_year_quarter) from psp_wage_limit))");
				query.executeUpdate();
			}         
        }
        PayrollServices.commitUnitOfWork();
    }

    public static void deleteAllACHEnrollmentDirFiles() {
        cleanDirectory(BatchUtils.getTaxAgencyConfigString("psp_achenrollment_work_dir"));
        cleanDirectory(BatchUtils.getTaxAgencyConfigString("psp_achenrollment_send_dir"));
        cleanDirectory(BatchUtils.getTaxAgencyConfigString("psp_achenrollment_recv_dir"));
        cleanDirectory(BatchUtils.getTaxAgencyConfigString("psp_achenrollment_arcv_dir"));
        cleanDirectory(BatchUtils.getTaxAgencyConfigString("psp_achenrollment_err_dir"));
    }

    public static void cleanDirectory(String pDir) {
        if (StringUtils.isNotEmpty(pDir)) {
            try {
                FileUtils.cleanDirectory(new File(pDir));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static PaystubDTO createPaystubDto(Employee employee, SpcfCalendar beginDate, SpcfCalendar endDate, SpcfCalendar payDate, String paystubNetPay) throws Exception {
        PaystubDTO paystub = new PaystubDTO();
        paystub.setModTS(BigInteger.valueOf(0));
        paystub.setChkNum("100");
        paystub.setAdjNetPay("1000.00");
        paystub.setGrossPay("1000.00");
        paystub.setNetPay(paystubNetPay);
        paystub.setPreTaxDeducts("1000.00");
        paystub.setTax("-200.00");
        paystub.setPayBeginDate(SpcfUtils.convertSpcfCalendarToXmlGregorianCalendar(beginDate));
        paystub.setPayDate(SpcfUtils.convertSpcfCalendarToXmlGregorianCalendar(payDate));
        paystub.setPayEndDate(SpcfUtils.convertSpcfCalendarToXmlGregorianCalendar(endDate));
        paystub.setYTDAdjNetPay("1000.00");
        paystub.setYTDGrossPay("1000.00");
        paystub.setYTDNetPay("1000.00");
        paystub.setYTDPreTaxDeducts("1000.00");
        paystub.setYTDTax("-650.00");

        ArrayList<PstubMsgDTO> pstubMsgList = new ArrayList<>();
        PstubMsgDTO msgDto = new PstubMsgDTO();
        msgDto.setText("sample DTO");
        msgDto.setType(PstubMsgType.Company);
        pstubMsgList.add(msgDto);
        paystub.setMsgDTOs(pstubMsgList);

        paystub.setEmployeeInfoDTO(createPstubEmployeeInfoDto(employee));
        paystub.setEmployerInfoDTO(createPstubEmployerInfoDto(employee));

        List<PstubMsgDTO> pstubMsgDTOS = new ArrayList<>();
        PstubMsgDTO pstubMsgDTO1 = new PstubMsgDTO();
        pstubMsgDTO1.setText("Dummy user message");
        pstubMsgDTO1.setType(PstubMsgType.User);
        pstubMsgDTOS.add(pstubMsgDTO1);

        PstubMsgDTO pstubMsgDTO2 = new PstubMsgDTO();
        pstubMsgDTO2.setText("Dummy company message");
        pstubMsgDTO2.setType(PstubMsgType.Company);
        pstubMsgDTOS.add(pstubMsgDTO2);

        paystub.setMsgDTOs(pstubMsgDTOS);


        ArrayList<PstubDDItemDTO> pstubDDItemDTOS = new ArrayList<>();
        PstubDDItemDTO pstubDDItemDTO = new PstubDDItemDTO();
        pstubDDItemDTO.setAcctName("AccnName");
        pstubDDItemDTO.setAcctType("AcctType");
        pstubDDItemDTO.setName("Name");
        pstubDDItemDTOS.add(pstubDDItemDTO);
        paystub.setDDItemDTOs(pstubDDItemDTOS);

        ArrayList<PstubPaidTimeoffItemDTO> pstubPaidTimeoffItemDTOS = new ArrayList<>();
        PstubPaidTimeoffItemDTO pstubPaidTimeoffItemDTO = new PstubPaidTimeoffItemDTO();
        pstubPaidTimeoffItemDTO.setAcctName("AccnName");
        pstubPaidTimeoffItemDTO.setName("Name");
        pstubPaidTimeoffItemDTO.setAvailable("Available");
        pstubPaidTimeoffItemDTOS.add(pstubPaidTimeoffItemDTO);
        paystub.setPaidTimeoffItemDTOs(pstubPaidTimeoffItemDTOS);


        ArrayList<PstubPayItemDTO> payItems = new ArrayList<PstubPayItemDTO>();
        payItems.add(createHourlyEarningItem("38.5"));
        payItems.add(createHourlyEarningItem("11.5"));
        payItems.add(createHourlyEarningItem("10.0"));

        payItems.add(createTaxItem("Federal Withholding", true));
        payItems.add(createTaxItem("CA Withholding", true));
        payItems.add(createTaxItem("disability Employer", false));
        paystub.setPayItemDTOs(payItems);

        return  paystub;
    }

    public static PstubPayItemDTO createTaxItem(String name, boolean employeePaid) {
        PstubPayItemDTO taxItem = new PstubPayItemDTO(PstubItemType.Tax);
        taxItem.setCurAmt("-100.00");
        taxItem.setYTD("-325.0");
        taxItem.setName(name);
        taxItem.setEmployeePaid(String.valueOf(employeePaid));
        return taxItem;
    }

    public static PstubPayItemDTO createHourlyEarningItem(String qtyTime) {
        PstubPayItemDTO pstubPayItemDTO = new PstubPayItemDTO(PstubItemType.Earnings);
        pstubPayItemDTO.setQtyTime(qtyTime);
        pstubPayItemDTO.setRate("100.0");
        pstubPayItemDTO.setName("Hourly Pay");
        pstubPayItemDTO.setEmployeePaid(String.valueOf(true));
        return pstubPayItemDTO;
    }

    public static PstubEmployeeInfoDTO createPstubEmployeeInfoDto(Employee employee) {
        PstubEmployeeInfoDTO employeeInfo = new PstubEmployeeInfoDTO();
        employeeInfo.setModTS(BigInteger.valueOf(0));
        employeeInfo.setFedAllowances(0);
        employeeInfo.setFedExtra("100.00");
        employeeInfo.setFedTaxFilingStatus("Single");
        employeeInfo.setFedTaxFilingStatusCode(0);
        employeeInfo.setFirstName(employee.getFirstName());
        employeeInfo.setLastName(employee.getLastName());
        employeeInfo.setSSN(employee.getTaxId());
        employeeInfo.setAddressDTO(createPstubAddressDto());
        employeeInfo.setStateAllowances(0);
        employeeInfo.setStateExtra("100.00");
        employeeInfo.setStateTaxFilingStatus("Single");
        employeeInfo.setTaxFilingState("CA");
        employeeInfo.setStateTaxFilingStatusCode(0);
        return employeeInfo;
    }

    public static PstubEmployerInfoDTO createPstubEmployerInfoDto(Employee employee) {
        PstubEmployerInfoDTO employerInfo = new PstubEmployerInfoDTO();
        employerInfo.setObjHash(employee.getSourceEmployeeId());
        employerInfo.setName("Employer");
        employerInfo.setAddressDTO(createPstubAddressDto());
        employerInfo.setStateTaxDTO(createPstubStateTaxInfoDto());
        return employerInfo;
    }

    public static PstubAddressDTO createPstubAddressDto() {
        PstubAddressDTO address = new PstubAddressDTO();
        address.setLine1("Line 1");
        address.setLine2("Line 2");
        address.setLine3("Line 3");
        return address;
    }

    private static Collection<PstubStateTaxInfoDTO> createPstubStateTaxInfoDto() {
        ArrayList<PstubStateTaxInfoDTO> pstubStateTaxInfoDTOs = new ArrayList<PstubStateTaxInfoDTO>();
        PstubStateTaxInfoDTO pstubStateTaxInfoDTO = new PstubStateTaxInfoDTO();
        pstubStateTaxInfoDTO.setAgencyId("123456");
        pstubStateTaxInfoDTO.setAgencyName("Test-Agency");
        pstubStateTaxInfoDTOs.add(pstubStateTaxInfoDTO);
        return pstubStateTaxInfoDTOs;
    }

    public static  void updateFilerType(Company company,String fileType){
        {
            try {
                if(fileType == null){
                    fileType="941";
                }
                PayrollServices.beginUnitOfWork();
                CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, Agency.IRS);
                CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgency);

                //remove 941/944 that are on or after the specified.
                Iterator<FormTemplateDTO> formTemplateIterator = companyAgencyDTO.getFormTemplateDtoList().iterator();
                while (formTemplateIterator.hasNext()) {
                    FormTemplateDTO formTemplateDTO = formTemplateIterator.next();
                    if (formTemplateDTO.is941944() ) {
                        formTemplateIterator.remove();
                    }
                }

                //then add the specified one in
                companyAgencyDTO.getFormTemplateDtoList().add(get941FormTemplateDTOFromSAPFilerType(fileType));

                ProcessResult pr = PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), companyAgency.getAgency().getAgencyId(), companyAgencyDTO);

                if (pr.isSuccess()) {
                    PayrollServices.commitUnitOfWork();
                } else {
                    // aeFactory.throwGenericException("Error updating Filer Type", pr);
                }
            } catch (Throwable t) {
                //
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public static void updateFilerType(String pSourceSystemCd, String pSourceSystemId, String filerType,SpcfCalendar date) {

        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));

            CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, Agency.IRS);
            CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgency);
            SpcfCalendar firstDayOfQuarter= getStartOfQuarter(date);
            //remove 941/944 that are on or after the specified.
            Iterator<FormTemplateDTO> formTemplateIterator = companyAgencyDTO.getFormTemplateDtoList().iterator();
            while (formTemplateIterator.hasNext()) {
                FormTemplateDTO formTemplateDTO = formTemplateIterator.next();
                if (formTemplateDTO.is941944() && !firstDayOfQuarter.after(formTemplateDTO.getEffectiveDate().toLocal())) {
                    formTemplateIterator.remove();
                }
            }

            //then add the specified one in
            companyAgencyDTO.getFormTemplateDtoList().add(get941FormTemplateDTOFromSAPFilerType(filerType,firstDayOfQuarter));

            ProcessResult pr = PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), companyAgency.getAgency().getAgencyId(), companyAgencyDTO);

            if (pr.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }
    public static FormTemplateDTO get941FormTemplateDTOFromSAPFilerType(String pFilerType) {
        FormTemplateDTO dto = new FormTemplateDTO();

        Calendar calnder=Calendar.getInstance();
        calnder.setTimeInMillis(PSPDate.getPSPTime().getTimeInMilliseconds());
        int quarter = (calnder.get(Calendar.MONTH) / 3) + 1;
        calnder.set(Calendar.MONTH, quarter <4 ?Calendar.JANUARY:(quarter <6 ? Calendar.APRIL:(quarter<9?Calendar.JULY:Calendar.OCTOBER)) );
        calnder.set(Calendar.DAY_OF_MONTH,1);


        dto.setEffectiveDate( SpcfCalendar.createInstance(calnder.getTimeInMillis(), SpcfTimeZone.getLocalTimeZone()));
        dto.setFilerType(pFilerType.equals("944") ? FormTemplate.IRS_944 : FormTemplate.IRS_941);
        return dto;
    }
    public static FormTemplateDTO get941FormTemplateDTOFromSAPFilerType(String pFilerType,SpcfCalendar firstDayOfQuarter) {
        FormTemplateDTO dto = new FormTemplateDTO();
        dto.setEffectiveDate( getStartOfQuarter(firstDayOfQuarter));
        dto.setFilerType(pFilerType.equals("944") ? FormTemplate.IRS_944 : FormTemplate.IRS_941);
        return dto;
    }
    public static  SpcfCalendar getStartOfQuarter(SpcfCalendar date){
        Calendar calnder=Calendar.getInstance();
        calnder.setTimeInMillis(date.getTimeInMilliseconds());
        int quarter = (calnder.get(Calendar.MONTH) / 3) + 1;
        int month=quarter <2 ?Calendar.JANUARY:(quarter <3 ? Calendar.APRIL:(quarter<4?Calendar.JULY:Calendar.OCTOBER));
        calnder.set(Calendar.MONTH, month);
        calnder.set(Calendar.DAY_OF_MONTH,1);
        return SpcfCalendar.createInstance(calnder.getTimeInMillis(), SpcfTimeZone.getLocalTimeZone());
    }

    public static void voidAPaycheck(PayrollRun payrollRun) {
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        List<String> voidPaychecks = new ArrayList<String>();
        voidPaychecks.add(payrollRun.getPaycheckCollection().sort(Paycheck.SourceEmployee().SourceEmployeeId()).get(0).getSourcePaycheckId());
        voidPayrollDTO.setPaycheckIdList(voidPaychecks);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, payrollRun.getCompany().getSourceCompanyId(), voidPayrollDTO));
        PayrollServices.commitUnitOfWork();
    }

    public static int nextPayrollItemId() {
        return ++payrollItemId;
    }

    public static boolean xmlSchemaValidator(File xsdFile, Reader xmlReader) {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new StreamSource(xsdFile));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xmlReader));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }
    public static void runJobs(int numberOfDays){
        int jDays = 1;
        for(int i= 0;i < numberOfDays;i++){
            Application.beginUnitOfWork();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            String yyyyMMdd= format.format(new Date(PSPDate.getPSPTime().getTimeInMilliseconds()));
            //  String yyyyMMdd = PSPDate.getPSPTime().getYear()+""+PSPDate.getPSPTime().getMonth()+""+PSPDate.getPSPTime().getDay();
            Application.commitUnitOfWork();
            Application.beginUnitOfWork();
            PSPDate.setPSPTime(yyyyMMdd+"010000");
            Application.commitUnitOfWork();
            BatchJobManager.runJob(BatchJobType.LedgerBalance);
            Application.beginUnitOfWork();
            PSPDate.setPSPTime(yyyyMMdd+"023500");
            Application.commitUnitOfWork();
            BatchJobManager.runJob(BatchJobType.EftpsPayment);
            Application.beginUnitOfWork();
            PSPDate.setPSPTime(yyyyMMdd+"053000");
            Application.commitUnitOfWork();
            BatchJobManager.runJob(BatchJobType.NightlyBatchJobs, NightlyBatchJobsProcessor.Commands.achtrans.name(),yyyyMMdd);
            Application.beginUnitOfWork();
            PSPDate.setPSPTime(yyyyMMdd+"133000");
            Application.commitUnitOfWork();
            // BatchJobManager.runJob(BatchJobType.AchTaxPaymentOffload);
            DataLoadServices.runOffloadTaxPayments(PSPDate.getPSPTime());
            Application.beginUnitOfWork();
            PSPDate.setPSPTime(yyyyMMdd+"140000");
            Application.commitUnitOfWork();
            BatchJobManager.runJob(BatchJobType.AchDebitOffload);
            Application.beginUnitOfWork();
            PSPDate.setPSPTime(yyyyMMdd+"153500");
            Application.commitUnitOfWork();
            BatchJobManager.runJob(BatchJobType.EftpsPayment);
            Application.beginUnitOfWork();
            PSPDate.setPSPTime(yyyyMMdd+"171500");
            Application.commitUnitOfWork();
            DataLoadServices.runOffload(PSPDate.getPSPTime());
            // BatchJobManager.runJob(BatchJobType.PrimaryDailyBatchJobs);
            Application.beginUnitOfWork();
            int month= PSPDate.getPSPTime().getMonth();
            int day= PSPDate.getPSPTime().getDay();
            int year =   PSPDate.getPSPTime().getYear();
            day  = day + jDays;
            int numberOfDaysInMonth= PSPDate.getPSPTime().getDaysInMonth();
            if(numberOfDaysInMonth < day){
                day = 1;
                jDays = 0;
                month = month + 1 ;
            }
            if(month > 12){
                month = 1;
                year = year+1;
            }

            Application.commitUnitOfWork();
            DataLoadServices.setPSPDate(year, month, day);
        }


    }
    public static void runJobsBetween(SpcfCalendar startTime,SpcfCalendar endTime) {
        Application.beginUnitOfWork();
        SpcfCalendar curentTime = PSPDate.getPSPTime();
        if (startTime == null) {
            startTime = curentTime;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        String yyyyMMdd = format.format(new Date(PSPDate.getPSPTime().getTimeInMilliseconds()));
        //  String yyyyMMdd = PSPDate.getPSPTime().getYear()+""+PSPDate.getPSPTime().getMonth()+""+PSPDate.getPSPTime().getDay();
        Application.commitUnitOfWork();
        if (!isCurrentTimeBetween(startTime, endTime)) {
            return;
        }
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(yyyyMMdd + "010000");
        startTime = PSPDate.getPSPTime();
        Application.commitUnitOfWork();
        if (isCurrentTimeBetween(startTime, endTime)) {
            BatchJobManager.runJob(BatchJobType.LedgerBalance);
        }

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(yyyyMMdd + "023500");
        startTime = PSPDate.getPSPTime();
        Application.commitUnitOfWork();
        if (isCurrentTimeBetween(startTime, endTime)) {
            BatchJobManager.runJob(BatchJobType.EftpsPayment);
        }

        Application.beginUnitOfWork();
        startTime = PSPDate.getPSPTime();
        PSPDate.setPSPTime(yyyyMMdd + "053000");
        Application.commitUnitOfWork();
        if (isCurrentTimeBetween(startTime, endTime)) {
            BatchJobManager.runJob(BatchJobType.NightlyBatchJobs, NightlyBatchJobsProcessor.Commands.achtrans.name(), yyyyMMdd);
        }

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(yyyyMMdd + "133000");
        startTime = PSPDate.getPSPTime();
        Application.commitUnitOfWork();
        if (isCurrentTimeBetween(startTime, endTime)) {
            // BatchJobManager.runJob(BatchJobType.AchTaxPaymentOffload);
            DataLoadServices.runOffloadTaxPayments(PSPDate.getPSPTime());
        }

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(yyyyMMdd + "140000");
        startTime = PSPDate.getPSPTime();
        Application.commitUnitOfWork();
        if (isCurrentTimeBetween(startTime, endTime)) {
            BatchJobManager.runJob(BatchJobType.AchDebitOffload);
        }

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(yyyyMMdd + "153500");
        startTime = PSPDate.getPSPTime();
        Application.commitUnitOfWork();
        if (isCurrentTimeBetween(startTime, endTime)) {
            BatchJobManager.runJob(BatchJobType.EftpsPayment);
        }

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(yyyyMMdd + "171500");
        startTime = PSPDate.getPSPTime();
        Application.commitUnitOfWork();
        if (isCurrentTimeBetween(startTime, endTime)) {
            //BatchJobManager.runJob(BatchJobType.PrimaryDailyBatchJobs);
            DataLoadServices.runOffload(PSPDate.getPSPTime());
        }

    }
    public static boolean isCurrentTimeBetween(SpcfCalendar startTime,SpcfCalendar endTime ){
        Application.beginUnitOfWork();
        SpcfCalendar curentTime = PSPDate.getPSPTime();
        if(curentTime.compareTo(startTime) >=0  && curentTime.compareTo(endTime) < 0)  {
            Application.rollbackUnitOfWork();
            return true;
        }
        Application.rollbackUnitOfWork();
        return false;
    }

    public static void runMMTJobs(int numberOfDays){
        int jDays = 1;
        for(int i= 0;i < numberOfDays;i++){
            Application.beginUnitOfWork();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            String yyyyMMdd= format.format(new Date(PSPDate.getPSPTime().getTimeInMilliseconds()));
            Application.commitUnitOfWork();
            Application.beginUnitOfWork();
            PSPDate.setPSPTime(yyyyMMdd+"023500");
            Application.commitUnitOfWork();
            BatchJobManager.runJob(BatchJobType.EftpsPayment);
            Application.beginUnitOfWork();
            PSPDate.setPSPTime(yyyyMMdd+"053000");
            Application.commitUnitOfWork();
            BatchJobManager.runJob(BatchJobType.NightlyBatchJobs, NightlyBatchJobsProcessor.Commands.achtrans.name(),yyyyMMdd);
            Application.beginUnitOfWork();
            PSPDate.setPSPTime(yyyyMMdd+"133000");
            Application.commitUnitOfWork();
            DataLoadServices.runOffloadTaxPayments(PSPDate.getPSPTime());

            Application.beginUnitOfWork();
            PSPDate.setPSPTime(yyyyMMdd+"153500");
            Application.commitUnitOfWork();
            BatchJobManager.runJob(BatchJobType.EftpsPayment);
            Application.beginUnitOfWork();
            PSPDate.setPSPTime(yyyyMMdd+"171500");
            Application.commitUnitOfWork();
            DataLoadServices.runOffload(PSPDate.getPSPTime());
            Application.beginUnitOfWork();
            int month= PSPDate.getPSPTime().getMonth();
            int day= PSPDate.getPSPTime().getDay();
            int year =   PSPDate.getPSPTime().getYear();
            day  = day + jDays;
            int numberOfDaysInMonth= PSPDate.getPSPTime().getDaysInMonth();
            if(numberOfDaysInMonth < day){
                day = 1;
                jDays = 0;
                month = month + 1 ;
            }
            if(month > 12){
                month = 1;
                year = year+1;
            }

            Application.commitUnitOfWork();
            DataLoadServices.setPSPDate(year, month, day);
        }


    }

    public static EntitlementUnitDTO createAssistedSymponyCompanyEntitlementDTO(String pEIN) {
        EntitlementUnitDTO entitlementUnitDTO = new EntitlementUnitDTO();

        entitlementUnitDTO.setFedTaxId(pEIN);
        entitlementUnitDTO.setCustomerId("1");
        entitlementUnitDTO.setContactEmail("test4@intuit.com");
        entitlementUnitDTO.setLicenseNumber("2");
        entitlementUnitDTO.setEntitlementOfferingCode("1234567891");

        entitlementUnitDTO.setAssetItemNumber(DataLoadServices.AssetItemNumber.ASSISTED_SYMPHONY.toString());
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingActivation);


        PspPrincipal principal = Application.getCurrentPrincipal();
        if (principal.isAgent()) {
            entitlementUnitDTO.setOrderSourceCd(OrderSourceCode.Siebel);
        } else {
            entitlementUnitDTO.setOrderSourceCd(OrderSourceCode.EStore);
        }

        return entitlementUnitDTO;
    }

    public static CompanyLaw deactivateFilingStatusOfCompanyLaw(Company pCompany, String lawId){
        CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(pCompany, lawId);
        CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(companyLaw);
        companyLawDTO.setFilingStatus(PayrollItemStatus.Inactive);
        PayrollServices.companyManager.addOrUpdateCompanyLaw(SourceSystemCode.QBDT, pCompany.getSourceCompanyId(), companyLawDTO);
        return companyLaw;
    }
    public static Company createAssistedCompany(String state, String psid, String ein, String aid) {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ein, true, ServiceCode.Tax);
        DataLoadServices.enrollEFTPS(company);
        ArrayList<String> lawIds = DataLoadServices.getAllStateLawIds(state);
        lawIds.addAll(DataLoadServices.getAllStateLawIds("IRS"));
        DataLoadServices.addCompanyLawsWithAgencyId(aid, company, state, lawIds);
        DataLoadServices.addCompanyLawRates(company);
        return company;
    }
    public static List<Company> setupGenericCompany(long pStartPsid, int pNumberOfCompanies, String[] pStatesList, PaymentTemplateCategory pCategory, PaymentMethod pPaymentMethod) {
        for (int i = 0; i < pNumberOfCompanies; i++) {
            String psid = Long.toString(pStartPsid++);
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
            DataLoadServices.activateTaxService(company);
            DataLoadServices.addFederalTaxCompanyLaws(company);
            DataLoadServices.addEEs(company, 2);
            updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        }

        DomainEntitySet<Company> companies = setupGenericCompanyAgency(pStatesList, pCategory, pPaymentMethod);

        return Arrays.asList(companies.toArray(new Company[]{}));
    }
    public static DomainEntitySet<Company> setupGenericCompanyAgency(String[] pStatesList, PaymentTemplateCategory pCategory, PaymentMethod pPaymentMethod) {
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().notEqualTo(SourceSystemCode.PSP));
        ArrayList<String> paymentTemplateList = new ArrayList<String>();
        paymentTemplateList.add("VT-WH433-PAYMENT");
        paymentTemplateList.add("MN-DEED1-PAYMENT");
        paymentTemplateList.add("MO-MODES-PAYMENT");
        PayrollServices.rollbackUnitOfWork();
        for (Company company : companies) {
            for (String stateName : pStatesList) {
                PaymentTemplate paymentTemplate = getStateGenericPaymentTemplate(stateName, pCategory);
                // Pull example state agency id
                PayrollServices.beginUnitOfWork();
                PaymentTemplatePaymentMethod paymentTemplatePaymentMethod = Application.find(PaymentTemplatePaymentMethod.class,
                        PaymentTemplatePaymentMethod.PaymentTemplate().equalTo(paymentTemplate)
                                .And(PaymentTemplatePaymentMethod.PaymentMethod().equalTo(pPaymentMethod))).getFirst();

                DomainEntitySet<PaymentMethodRequirement> paymentMethodRequirements = paymentTemplatePaymentMethod
                        .getPaymentMethodRequirementCollection();

                AgencyIdRequirement agencyIdRequirement = null;

                for (PaymentMethodRequirement paymentMethodRequirement : paymentMethodRequirements) {
                    if (paymentMethodRequirement instanceof AgencyIdRequirement) {
                        if (((AgencyIdRequirement) paymentMethodRequirement).getPaymentTemplateAgencyId() == null) {
                            agencyIdRequirement = (AgencyIdRequirement) paymentMethodRequirement;
                            break;
                        }
                    }
                }
                PayrollServices.rollbackUnitOfWork();

                // Figure out what format the state tax id should be
                String exampleAgencyId = null;

                if (agencyIdRequirement == null) {
                    exampleAgencyId = null;
                } else if (agencyIdRequirement.getCustomRequirement().equals(AgencyIdCustomRequirement.None) ||
                        agencyIdRequirement.getCustomRequirement().equals(AgencyIdCustomRequirement.MustNotFollowFedTaxIdSubstitueIf8Digits) ||
                        agencyIdRequirement.getCustomRequirement().equals(AgencyIdCustomRequirement.MustNotInExemptedIdList) ||
                        agencyIdRequirement.getCustomRequirement().equals(AgencyIdCustomRequirement.IFNotPatternMustFollowFedTaxId)) {
                    exampleAgencyId = agencyIdRequirement.getExample();
                } else if (agencyIdRequirement.getCustomRequirement().equals(AgencyIdCustomRequirement.Digits2Through10FollowFedTaxId)) {
                    exampleAgencyId = agencyIdRequirement.getExample();
                    exampleAgencyId = exampleAgencyId.substring(0, 1) + company.getFedTaxId() + exampleAgencyId.substring(10);
                } else if (agencyIdRequirement.getCustomRequirement().equals(AgencyIdCustomRequirement.Digits4Through12FollowFedTaxId)) {
                    exampleAgencyId = agencyIdRequirement.getExample();
                    exampleAgencyId = exampleAgencyId.substring(0, 3) + company.getFedTaxId() + exampleAgencyId.substring(12);
                } else if (agencyIdRequirement.getCustomRequirement().equals(AgencyIdCustomRequirement.MustStartWithFedTaxId)) {
                    exampleAgencyId = agencyIdRequirement.getExample();
                    exampleAgencyId = company.getFedTaxId() + exampleAgencyId.substring(9);
                } else if ( agencyIdRequirement.getCustomRequirement().equals(AgencyIdCustomRequirement.MustFollowFedTaxId)){
                    exampleAgencyId = company.getFedTaxId();

                    if(exampleAgencyId != null && exampleAgencyId.charAt(2) != '-') {
                        exampleAgencyId = new StringBuffer(exampleAgencyId).insert(2, "-").toString();
                    }

                }
                if (exampleAgencyId != null && paymentTemplate != null && paymentTemplateList.contains(paymentTemplate.getPaymentTemplateCd())) {
                    exampleAgencyId = exampleAgencyId.split("or")[0].trim();
                }

                DataLoadServices.addCompanyLawsWithAgencyId(exampleAgencyId, company, stateName);
                //Enable ACH Credit payment
                PayrollServices.beginUnitOfWork();
                assertSuccess(PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), paymentTemplate.getPaymentTemplateCd(),pPaymentMethod, true));
                PayrollServices.commitUnitOfWork();

                PayrollServices.beginUnitOfWork();
                Application.refresh(paymentTemplatePaymentMethod);
                for (PaymentMethodRequirement paymentMethodRequirement : paymentTemplatePaymentMethod.getPaymentMethodRequirementCollection()) {
                    if (paymentMethodRequirement instanceof AgencyIdRequirement) {
                        if (((AgencyIdRequirement) paymentMethodRequirement).getPaymentTemplateAgencyId() != null) {
                            AgencyIdRequirement additionalIdRequirement = (AgencyIdRequirement) paymentMethodRequirement;
                            AgencyIdDTO agencyIdDTO = new AgencyIdDTO(additionalIdRequirement.getPaymentTemplateAgencyId().getPaymentTemplate().getPaymentTemplateCd(), additionalIdRequirement.getPaymentTemplateAgencyId().getName(), "12245678");
                            assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));
                        }
                    }
                }
                PayrollServices.commitUnitOfWork();


            }
        }
        return companies;
    }
    public static PaymentTemplate getStateGenericPaymentTemplate(String state, PaymentTemplateCategory pCategory) {
        PaymentTemplate paymentTemplate = null;
        PayrollServices.beginUnitOfWork();
        if("NY".equals(state) && pCategory == PaymentTemplateCategory.Withholding){
            state = state + "-1MN"; // To make it we find and return NY-1MN-PAYMENT template, to have same unit test case set up as before adding 'NY-MTA305-PAYMENT' template as Withholding
        } else if ("NM".equals(state) && pCategory == PaymentTemplateCategory.SUI ) {
            state = state + "-ES903A"; // To make it we find and return NM-ES903A-PAYMENT template, as this is the SUI payment template
        }else if ("MA".equals(state) && pCategory == PaymentTemplateCategory.Withholding ) {
            state = state + "-PFML"; // To make it we find and return NM-ES903A-PAYMENT template, as this is the SUI payment template
        }
        DomainEntitySet<PaymentTemplate> paymentTemplates = Application.find(PaymentTemplate.class, PaymentTemplate.Category().equalTo(pCategory).And(PaymentTemplate.PaymentTemplateCd().like(state + "-%")));
        if (paymentTemplates.size() > 0) {
            paymentTemplate = paymentTemplates.get(0);
        }
        PayrollServices.rollbackUnitOfWork();
        return paymentTemplate;
    }
    public static void runGenericPayrollRun(Company pCompany, String[] pStatesList, SpcfCalendar supportedDate, DateDTO payrollDate,
                                     boolean checkIRS, HashMap<String, String> pLawAmounts, PaymentTemplateCategory pCategory) {
        HashMap<String, String> lawAmounts = new HashMap();
        if (pLawAmounts.isEmpty()) {
            lawAmounts.put("61", "6.1");
            lawAmounts.put("62", "6.2");
            lawAmounts.put("63", "6.3");
            lawAmounts.put("64", "6.4");
            lawAmounts.put("1", "25");
            lawAmounts.put("65", "6.5");
        } else {
            for (String lawId : pLawAmounts.keySet()) {
                lawAmounts.put(lawId, pLawAmounts.get(lawId));
            }
        }
        for (String state : pStatesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStateGenericPaymentTemplate(state,pCategory);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
            PayrollServices.beginUnitOfWork();
            Application.getHibernateSession().refresh(paymentTemplate);
            CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(pCompany, paymentTemplate.getAgency().getAgencyId());
            SpcfDecimal withHoldingsAmount = SpcfMoney.ZERO;
            DomainEntitySet<CompanyLaw> companyLaws = companyAgency.getCompanyLawCollection();
            for (CompanyLaw companyLaw : companyLaws) {
                if(paymentTemplate.getLawCollection().contains(companyLaw.getLaw())) { // To include only laws from individual payment template, companyAgency will have Law for the agency
                    if (pLawAmounts.containsKey(companyLaw.getLaw().getLawId())) {
                        lawAmounts.put(companyLaw.getLaw().getLawId(), pLawAmounts.get(companyLaw.getLaw().getLawId()));
                        withHoldingsAmount = withHoldingsAmount.add(SpcfDecimal.createInstance(pLawAmounts.get(companyLaw.getLaw().getLawId())));
                    } else {
                        lawAmounts.put(companyLaw.getLaw().getLawId(), companyLaw.getLaw().getLawId());
                        withHoldingsAmount = withHoldingsAmount.add(SpcfDecimal.createInstance(companyLaw.getLaw().getLawId()));
                    }
                }
            }
            PayrollServices.rollbackUnitOfWork();
        }
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(pCompany);
        DataLoadServices.addAssistedBankAccounts(pCompany, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, pCompany, payrollDate, Arrays.asList(employees.toArray(new Employee[]{})), lawAmounts);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, pCompany.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        for (String state : pStatesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStateGenericPaymentTemplate(state, pCategory);
            PayrollServices.beginUnitOfWork();
            Application.getHibernateSession().refresh(paymentTemplate);
            CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(pCompany, paymentTemplate.getAgency().getAgencyId());
            SpcfDecimal withHoldingsAmount = SpcfMoney.ZERO;
            DomainEntitySet<CompanyLaw> companyLaws = companyAgency.getCompanyLawCollection();
            for (CompanyLaw companyLaw : companyLaws) {
                if(paymentTemplate.getLawCollection().contains(companyLaw.getLaw())) { // To include only laws from individual payment template, companyAgency will have Law for the agency
                    if (pLawAmounts.containsKey(companyLaw.getLaw().getLawId())) {
                        lawAmounts.put(companyLaw.getLaw().getLawId(), pLawAmounts.get(companyLaw.getLaw().getLawId()));
                        withHoldingsAmount = withHoldingsAmount.add(SpcfDecimal.createInstance(pLawAmounts.get(companyLaw.getLaw().getLawId())));
                    } else {
                        lawAmounts.put(companyLaw.getLaw().getLawId(), companyLaw.getLaw().getLawId());
                        withHoldingsAmount = withHoldingsAmount.add(SpcfDecimal.createInstance(companyLaw.getLaw().getLawId()));
                    }
                }
            }

            // Multiply by number of employees
            withHoldingsAmount = withHoldingsAmount.multiply(SpcfDecimal.createInstance(String.valueOf(employees.size())));
            DomainEntitySet<MoneyMovementTransaction> statePayments = getReadyToSendTaxPayments(pCompany, paymentTemplate.getPaymentTemplateCd());
            if (statePayments.size() == 0)   {
                statePayments = getATFFinalizedTaxPayments(pCompany, paymentTemplate.getPaymentTemplateCd());
            }

            if(statePayments.size() == 0 && paymentTemplate.getAgency().getAgencyId().equals(Agency.FL_AGENT_ID)) {
                statePayments = getOnHoldTaxPayments(pCompany, paymentTemplate.getPaymentTemplateCd());
            }

            assertTrue("Number of State payments for " + state, statePayments.size() > 0);
            if (statePayments.getFirst().getMoneyMovementPaymentMethod() == PaymentMethod.ACHCredit) {
                DomainEntitySet<EntryDetailRecord> entryDetailRecords = Application.find(EntryDetailRecord.class, EntryDetailRecord.NACHAFileType().equalTo(NACHAFileType.CCDPlus).And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)).And(EntryDetailRecord.MoneyMovementTransaction().equalTo(statePayments.getFirst())));
                assertEquals("State ACH credit payment entry detail for " + state, 1 + (paymentTemplate.getPaymentTemplateCd().equals("OR-OTCUI-PAYMENT") ? 1 : 0), entryDetailRecords.size());
            }
            PayrollServices.rollbackUnitOfWork();
        }

        if (checkIRS) {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<MoneyMovementTransaction> irs941Payments = getOnHoldTaxPayments(pCompany, "IRS-941-PAYMENT");
            assertEquals("IRS 941 payments", 1, irs941Payments.size());
            assertEquals("IRS 941 payment Amount", new SpcfMoney("100"), irs941Payments.get(0).getMoneyMovementTransactionAmount());
            DomainEntitySet<MoneyMovementTransaction> irs940Payments = getOnHoldTaxPayments(pCompany, "IRS-940-PAYMENT");
            assertEquals("IRS 941 payments", 1, irs940Payments.size());
            assertEquals("IRS 941 payment Amount", new SpcfMoney("13"), irs940Payments.get(0).getMoneyMovementTransactionAmount());
            PayrollServices.rollbackUnitOfWork();
        }

    }

    public static void enableOIIFlagsForDirectDeposit(String sourceCompanyId, SourceSystemCode sourceSystemCode) {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, sourceSystemCode);
        assertNotNull("Company not found for"+ sourceCompanyId, company);
        company.setWorkflowState(Workflows.OII, WorkflowState.ENABLED);
        company.setWorkflowState(Workflows.ACTIVATE_DIRECT_DEPOSIT, WorkflowState.ENABLED);
        company.setWorkflowState(Workflows.MONEY_MOVEMENT_ONBOARDING, WorkflowState.ENABLED);
        PayrollServices.commitUnitOfWork();
    }

    public static Company updateCompany(SourceSystemCode sourceSystemCode, String sourceCompanyId, CompanyDTO companyDTO) {
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> updateCompanyProcessResult = PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, "123456789", companyDTO);
        assertSuccess("updateCompany", updateCompanyProcessResult);
        PayrollServices.commitUnitOfWork();

        return updateCompanyProcessResult.getResult();
    }

}

