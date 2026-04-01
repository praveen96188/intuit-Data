package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;

/**
 * User: dweinberg
 * Date: 11/28/11
 * Time: 10:58 AM
 * This class is for methods that are much coarser than DataLoadServices.
 * These methods are often shared across multiple unit tests that are doing similar things,
 * but the basic guideline is that they have behavior that uses magic values instead of generic ones (DLS)
 */
public class DataLoadPalette {


    /*
    Setup a typical tax company that is ready to send payrolls
     */
    public static Company setupTaxCompany() {
        return setupTaxCompany(Boolean.TRUE);
    }

    /*
    Setup a typical tax company that is in pendingsetup state or active state depending on the flag activateTaxService values passed
     */
    public static Company setupTaxCompany(Boolean activateTaxService) {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        DataLoadServices.addAssistedEntitlementUnit(company, "lic1", "eoc1", true);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);


        DataLoadServices.addFederalTaxCompanyLaws(company);
        //Add laws for all supported templates
        Set<String> states = new HashSet<String>();
        for (PaymentTemplate paymentTemplate : Application.find(PaymentTemplate.class,
                PaymentTemplate.SupportStartDate().isNotNull()
                        .And(PaymentTemplate.Agency().AgencyId().notEqualTo("IRS")))) {
            states.add(paymentTemplate.getPaymentTemplateCd().substring(0, 2));
        }
        for (String state : states) {
            DataLoadServices.addCompanyLawsWithAgencyId(null, company, state);
        }
        DataLoadServices.addAdditionalFilingAmounts(company);

        DataLoadServices.addEEs(company, 2, false, true);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.enrollEFTPS(company);
        DataLoadServices.updateACHAgentEnabledFlags(company, null, true);
        DataLoadServices.updateRequiredIDs(company, null, true);
        if(activateTaxService)
            DataLoadServices.activateTaxService(company);
        return company;
    }

    public static Company setupTaxCompany(String pEin){
        return setupTaxCompany(pEin, "0001", 2);
    }

    public static Company setupTaxCompany(String pEin, String pSuffix, int numEmployees){
        String psid = "TEST_" + pSuffix;

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, pEin, false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        DataLoadServices.addAssistedEntitlementUnit(company, "lic1", "eoc1", true);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);


        DataLoadServices.addFederalTaxCompanyLaws(company);
        //Add laws for all supported templates
        Set<String> states = new HashSet<String>();
        for (PaymentTemplate paymentTemplate : Application.find(PaymentTemplate.class,
                PaymentTemplate.SupportStartDate().isNotNull()
                        .And(PaymentTemplate.Agency().AgencyId().notEqualTo("IRS")))) {
            states.add(paymentTemplate.getPaymentTemplateCd().substring(0, 2));
        }
        for (String state : states) {
            DataLoadServices.addCompanyLawsWithAgencyId(null, company, state);
        }
        DataLoadServices.addAdditionalFilingAmounts(company);

        DataLoadServices.addEEs(company, numEmployees, false, true);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.enrollEFTPS(company);
        DataLoadServices.updateACHAgentEnabledFlags(company, null, true);
        DataLoadServices.updateRequiredIDs(company, null, true);
        DataLoadServices.activateTaxService(company);

        return company;
    }

    /*
    Runs a payroll that includes all the company laws
     */
    public static PayrollRun runSimpleTaxPayroll(Company company, DateDTO checkDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        List<String> lawIds = DataLoadServices.getCompanyLawsIds(company);

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);

        for (CompanyAgency companyAgency : Application.refresh(company).getCompanyAgencyCollection()) {
            for (CompanyLaw companyLaw : companyAgency.getCompanyLawCollection()) {
                //don't add dead laws
                if (!companyLaw.getLaw().shouldExcludeFromUI()) {
                    lawIds.add(companyLaw.getLaw().getLawId());
                }
            }

        }
        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, checkDate, new ArrayList<Employee>(company.getCloudEmployees()), lawIds.toArray(new String[lawIds.size()]), lawIds.toArray(new String[lawIds.size()]));
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);
        return processResult.getResult();
    }

}
