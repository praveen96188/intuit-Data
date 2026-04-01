package com.intuit.sbd.payroll.psp.processes.guideline401k.employer;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domainsecondary.*;
import com.intuit.sbd.payroll.psp.domainsecondary.util.constants.Guideline401kConstants;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Objects;

public class Add401kCompanyQBDTPItemCoreTests {

    @Before
    public void runBeforeEachTest(){
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    public Hcm401kCompanyPolicy setupDataForAddPItemCoreSuccess(){
        ApplicationSecondary.beginUnitOfWork();
        Hcm401kCompanyPolicy hcm401kCompanyPolicy = new Hcm401kCompanyPolicy();
        Hcm401kPolicy hcm401kPolicy = Hcm401kPolicy.getHcm401kPolicyByDeductionItemAndProvider(DeductionItemPolicy.TppoCus401K, DeductionItemProvider.Guideline);

        if (Objects.isNull(hcm401kPolicy)) {
            hcm401kPolicy = new Hcm401kPolicy();
            hcm401kPolicy.setDeductionItemProvider(DeductionItemProvider.Guideline);
            hcm401kPolicy.setDeductionItemPolicy(DeductionItemPolicy.TppoCus401K);
            hcm401kPolicy.setDescription(Guideline401kConstants.GUIDELINE_TRADITIONAL_401K);
            ApplicationSecondary.save(hcm401kPolicy);
        }
        hcm401kCompanyPolicy.setHcm401kPolicy(hcm401kPolicy);
        hcm401kCompanyPolicy.setActive(true);

//        String psid = "1234567";
////        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ServiceCode.Tax);
        Company company = new Company();
        hcm401kCompanyPolicy.setCompanyId(company.getId().getStandardFormatString());
        ApplicationSecondary.save(hcm401kCompanyPolicy);
        ApplicationSecondary.commitUnitOfWork();

        return hcm401kCompanyPolicy;
    }

    @Test
    public void testAdd401kCompanyQBDTPItemCoreSuccess(){
        Hcm401kCompanyPolicy hcm401kCompanyPolicy = setupDataForAddPItemCoreSuccess();

        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<Hcm401kCompanyQbdtPitem> result = PayrollServices.companyManager.addOrUpdate401kCompanyQBDTPItem(hcm401kCompanyPolicy, "er_item_id", "ee_item_id");
        PayrollServices.commitUnitOfWorkWithSecondary();

        Assert.assertNotNull(result.getResult());
    }
}
