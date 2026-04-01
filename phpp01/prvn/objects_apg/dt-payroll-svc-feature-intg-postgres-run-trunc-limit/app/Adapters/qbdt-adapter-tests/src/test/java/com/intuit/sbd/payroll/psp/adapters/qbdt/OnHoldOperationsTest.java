package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyQB1DataLoader;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: Jun 5, 2008
 * Time: 10:20:31 PM
 */
public class OnHoldOperationsTest extends PayrollServicesTest {


    public static String COMPANY_PSID;
    @Before
    public void runBeforeEachTest() {
        COMPANY_PSID = CompanyQB1DataLoader.COMPANY_PSID;
        QBDTTestHelper.typicalRunBeforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        QBDTTestHelper.typicalRunAfterEachTest();
    }

    @Test
    public void testDDLimitSubmitAddEmployee() {
        testOnHoldStatusPayrollSubmitAddEmployee(ServiceSubStatusCode.DirectDepositLimit,ErrorMessages.PayrollRejectDDLimit());
    }

    @Test
    public void testDDLimitSubmitUpdateEmployee() {
        testOnHoldStatusPayrollSubmitUpdateEmployee(ServiceSubStatusCode.DirectDepositLimit,ErrorMessages.PayrollRejectDDLimit());
    }

    @Test
    public void testDDLimitSubmitNoEmpChanges() {
        testOnHoldStatusPayrollSubmitNoEmpChanges(ServiceSubStatusCode.DirectDepositLimit,ErrorMessages.PayrollRejectDDLimit());
    }

    @Test
    public void testDDLimitPaycheckMod() {
        testOnHoldStatusPaycheckMod(ServiceSubStatusCode.DirectDepositLimit);
    }

    @Test
    public void testDDLimitCompanyUpdate() {
        testOnHoldStatusCompanyUpdateAllowed(ServiceSubStatusCode.DirectDepositLimit);
    }

    @Test
    public void testDDLimitOnHoldSync() {
        testOnHoldSync(ServiceSubStatusCode.DirectDepositLimit);
    }

    @Test
    public void testR01ThroughR9SubstatusPayrollSubmitAddEmployee() {
        testOnHoldStatusPayrollSubmitAddEmployee(ServiceSubStatusCode.AchRejectR1R9,ErrorMessages.PayrollRejectACHReturnR01ThruR09());
    }

    @Test
    public void testR01ThroughR9SubstatusPayrollSubmitUpdateEmployee() {
        testOnHoldStatusPayrollSubmitUpdateEmployee(ServiceSubStatusCode.AchRejectR1R9,ErrorMessages.PayrollRejectACHReturnR01ThruR09());
    }

    @Test
    public void testR01ThroughR9SubstatusPayrollSubmitNoEmpChanges() {
        testOnHoldStatusPayrollSubmitNoEmpChanges(ServiceSubStatusCode.AchRejectR1R9,ErrorMessages.PayrollRejectACHReturnR01ThruR09());
    }

    @Test
    public void testR01ThroughR9SubstatusPaycheckMod() {
        testOnHoldStatusPaycheckMod(ServiceSubStatusCode.AchRejectR1R9);
    }

    @Test
    public void testR01ThroughR9SubstatusCompanyUpdate() {
        testOnHoldStatusCompanyUpdateAllowed(ServiceSubStatusCode.AchRejectR1R9);
    }

    @Test
    public void testR01ThroughR9SubstatusOnHoldSync() {
        testOnHoldSync(ServiceSubStatusCode.AchRejectR1R9);
    }

    @Test
    public void testNonR01ThroughR9SubstatusPayrollSubmitAddEmployee() {
        testOnHoldStatusPayrollSubmitAddEmployee(ServiceSubStatusCode.AchRejectOther,ErrorMessages.PayrollRejectACHReturnNonR01ThruR09());
    }

    @Test
    public void testNonR01ThroughR9SubstatusPayrollSubmitUpdateEmployee() {
        testOnHoldStatusPayrollSubmitUpdateEmployee(ServiceSubStatusCode.AchRejectOther,ErrorMessages.PayrollRejectACHReturnNonR01ThruR09());
    }

    @Test
    public void testNonR01ThroughR9SubstatusPayrollSubmitNoEmpChanges() {
        testOnHoldStatusPayrollSubmitNoEmpChanges(ServiceSubStatusCode.AchRejectOther,ErrorMessages.PayrollRejectACHReturnNonR01ThruR09());
    }

    @Test
    public void testNonR01ThroughR9SubstatusCompanyUpdate() {
        testOnHoldStatusCompanyUpdateAllowed(ServiceSubStatusCode.AchRejectOther);
    }

    @Test
    public void testNonR01ThroughR9SubstatusOnHoldSync() {
        testOnHoldSync(ServiceSubStatusCode.AchRejectOther);
    }

    @Test
    public void testNonR01ThroughR9SubstatusPaycheckMod() {
        testOnHoldStatusPaycheckMod(ServiceSubStatusCode.AchRejectOther);
    }

    @Test
    public void testFraudSubstatusPayrollSubmitAddEmployee() {
        testOnHoldStatusPayrollSubmitAddEmployee(ServiceSubStatusCode.Fraud,ErrorMessages.PayrollRejectFraud());
    }

    @Test
    public void testFraudSubstatusPayrollSubmitUpdateEmployee() {
        testOnHoldStatusPayrollSubmitUpdateEmployee(ServiceSubStatusCode.Fraud,ErrorMessages.PayrollRejectFraud());
    }

    @Test
    public void testFraudSubstatusPayrollSubmitNoEmpChanges() {
        testOnHoldStatusPayrollSubmitNoEmpChanges(ServiceSubStatusCode.Fraud,ErrorMessages.PayrollRejectFraud());
    }

    @Test
    public void testFraudSubstatusPaycheckMod() {
        testOnHoldStatusPaycheckMod(ServiceSubStatusCode.Fraud);
    }

    @Test
    public void testFraudCompanyUpdate() {
        testOnHoldStatusCompanyUpdateNotAllowed(ServiceSubStatusCode.Fraud, ErrorMessages.PayrollRejectFraud(), 1);
    }

    @Test
    public void testFraudOnHoldSync() {
        testOnHoldSync(ServiceSubStatusCode.Fraud);
    }

    @Test
    public void testAMLHoldSubstatusPayrollSubmitAddEmployee() {
        testOnHoldStatusPayrollSubmitAddEmployee(ServiceSubStatusCode.AMLHold,ErrorMessages.PayrollRejectFraud());
    }

    @Test
    public void testAMLHoldSubstatusPayrollSubmitUpdateEmployee() {
        testOnHoldStatusPayrollSubmitUpdateEmployee(ServiceSubStatusCode.AMLHold,ErrorMessages.PayrollRejectFraud());
    }

    @Test
    public void testAMLHoldSubstatusPayrollSubmitNoEmpChanges() {
        testOnHoldStatusPayrollSubmitNoEmpChanges(ServiceSubStatusCode.AMLHold,ErrorMessages.PayrollRejectFraud());
    }

    @Test
    public void testAMLHoldSubstatusPaycheckMod() {
        testOnHoldStatusPaycheckMod(ServiceSubStatusCode.AMLHold);
    }

    @Test
    public void testAMLHoldCompanyUpdate() {
        testOnHoldStatusCompanyUpdateAllowed(ServiceSubStatusCode.AMLHold);
    }

    @Test
    public void testAMLHoldOnHoldSync() {
        testOnHoldSync(ServiceSubStatusCode.AMLHold);
    }

    @Test
    public void testFraudReviewSubstatusPayrollSubmitAddEmployee() {
        testOnHoldStatusPayrollSubmitAddEmployee(ServiceSubStatusCode.FraudReview,ErrorMessages.PayrollRejectFraudReview());
    }

    @Test
    public void testFraudReviewSubstatusPayrollSubmitUpdateEmployee() {
        testOnHoldStatusPayrollSubmitUpdateEmployee(ServiceSubStatusCode.FraudReview,ErrorMessages.PayrollRejectFraudReview());
    }

    @Test
    public void testFraudReviewSubstatusPayrollSubmitNoEmpChanges() {
        testOnHoldStatusPayrollSubmitNoEmpChanges(ServiceSubStatusCode.FraudReview,ErrorMessages.PayrollRejectFraudReview());
    }

    @Test
    public void testFraudReviewSubstatusPaycheckMod() {
        testOnHoldStatusPaycheckMod(ServiceSubStatusCode.FraudReview);
    }

    @Test
    public void testFraudReviewCompanyUpdate() {
        testOnHoldStatusCompanyUpdateAllowed(ServiceSubStatusCode.FraudReview);
    }

    @Test
    public void testFraudReviewOnHoldSync() {
        testOnHoldSync(ServiceSubStatusCode.FraudReview);
    }

    @Test
    public void testIntuitCollectionsSubstatusPayrollSubmitAddEmployee() {
        testOnHoldStatusPayrollSubmitAddEmployee(ServiceSubStatusCode.IntuitCollections,ErrorMessages.PayrollRejectIntuitCollections());
    }

    @Test
    public void testIntuitCollectionsSubstatusPayrollSubmitUpdateEmployee() {
        testOnHoldStatusPayrollSubmitUpdateEmployee(ServiceSubStatusCode.IntuitCollections,ErrorMessages.PayrollRejectIntuitCollections());
    }

    @Test
    public void testIntuitCollectionsSubstatusPayrollSubmitNoEmpChanges() {
        testOnHoldStatusPayrollSubmitNoEmpChanges(ServiceSubStatusCode.IntuitCollections,ErrorMessages.PayrollRejectIntuitCollections());
    }

    @Test
    public void testIntuitCollectionsSubstatusPaycheckMod() {
        testOnHoldStatusPaycheckMod(ServiceSubStatusCode.IntuitCollections);
    }

    @Test
    public void testIntuitCollectionsCompanyUpdate() {
        testOnHoldStatusCompanyUpdateAllowed(ServiceSubStatusCode.IntuitCollections);
    }

    @Test
    public void testIntuitCollectionsOnHoldSync() {
        testOnHoldSync(ServiceSubStatusCode.IntuitCollections);
    }

    @Test
    public void testRiskCollectionsSubstatusPayrollSubmitAddEmployee() {
        testOnHoldStatusPayrollSubmitAddEmployee(ServiceSubStatusCode.RiskCollections,ErrorMessages.PayrollRejectRiskCollections());
    }

    @Test
    public void testRiskCollectionsSubstatusPayrollSubmitUpdateEmployee() {
        testOnHoldStatusPayrollSubmitUpdateEmployee(ServiceSubStatusCode.RiskCollections,ErrorMessages.PayrollRejectRiskCollections());
    }

    @Test
    public void testRiskCollectionsSubstatusPayrollSubmitNoEmpChanges() {
        testOnHoldStatusPayrollSubmitNoEmpChanges(ServiceSubStatusCode.RiskCollections,ErrorMessages.PayrollRejectRiskCollections());
    }

    @Test
    public void testRiskCollectionsSubstatusCompanyUpdate() {
        testOnHoldStatusCompanyUpdateNotAllowed(ServiceSubStatusCode.RiskCollections,ErrorMessages.PayrollRejectRiskCollections(), 1);
    }
    
    @Test
    public void testRiskCollectionsOnHoldSync() {
        testOnHoldSync(ServiceSubStatusCode.RiskCollections);
    }

    @Test
    public void testRiskCollectionsSubstatusPaycheckMod() {
        testOnHoldStatusPaycheckMod(ServiceSubStatusCode.RiskCollections);
    }

    @Test
    public void testSuspendedDirectDepositSubstatusPayrollSubmitAddEmployee() {
        testOnHoldStatusPayrollSubmitAddEmployee(ServiceSubStatusCode.SuspendedDirectDeposit,ErrorMessages.PayrollRejectSuspendedDD());
    }

    @Test
    public void testSuspendedDirectDepositSubstatusPayrollSubmitUpdateEmployee() {
        testOnHoldStatusPayrollSubmitUpdateEmployee(ServiceSubStatusCode.SuspendedDirectDeposit,ErrorMessages.PayrollRejectSuspendedDD());
    }

    @Test
    public void testSuspendedDirectDepositSubstatusPayrollSubmitNoEmpChanges() {
        testOnHoldStatusPayrollSubmitNoEmpChanges(ServiceSubStatusCode.SuspendedDirectDeposit,ErrorMessages.PayrollRejectSuspendedDD());
    }

    @Test
    public void testSuspendedDirectDepositSubstatusPaycheckMod() {
        testOnHoldStatusPaycheckMod(ServiceSubStatusCode.SuspendedDirectDeposit);
    }

    @Test
    public void testSuspendedDirectDepositCompanyUpdate() {
        testOnHoldStatusCompanyUpdateAllowed(ServiceSubStatusCode.SuspendedDirectDeposit);
    }

    @Test
    public void tesSuspendedDirectDepositOnHoldSync() {
        testOnHoldSync(ServiceSubStatusCode.SuspendedDirectDeposit);
    }

    @Test
    public void testPendingTerminationSubstatusPayrollSubmitAddEmployee() {
        testOnHoldStatusPayrollSubmitAddEmployee(ServiceSubStatusCode.PendingTermination,ErrorMessages.PayrollRejectPendingTermination());
    }

    @Test
    public void testPendingTerminationSubstatusPayrollSubmitUpdateEmployee() {
        testOnHoldStatusPayrollSubmitUpdateEmployee(ServiceSubStatusCode.PendingTermination,ErrorMessages.PayrollRejectPendingTermination());
    }

    @Test
    public void testPendingTerminationSubstatusPayrollSubmitNoEmpChanges() {
        testOnHoldStatusPayrollSubmitNoEmpChanges(ServiceSubStatusCode.PendingTermination,ErrorMessages.PayrollRejectPendingTermination());
    }

    @Test
    public void testPendingTerminationSubstatusPaycheckMod() {
        testOnHoldStatusPaycheckMod(ServiceSubStatusCode.PendingTermination);
    }

    @Test
    public void testPendingTerminationSubstatusCompanyUpdate() {
        testOnHoldStatusCompanyUpdateNotAllowed(ServiceSubStatusCode.PendingTermination,ErrorMessages.PayrollRejectPendingTermination(), 1);
    }

    @Test
    public void testPendingTerminationOnHoldSync() {
        testOnHoldSync(ServiceSubStatusCode.PendingTermination);
    }

    private void testOnHoldStatusCompanyUpdateNotAllowed(ServiceSubStatusCode companySubstatus, ErrorMessage errorMessage, int pEventCount) {
        List<ServiceSubStatusCode> statusList = new ArrayList<ServiceSubStatusCode>();
        statusList.add(companySubstatus);
        testOnHoldStatusCompanyUpdateNotAllowed(statusList,errorMessage, pEventCount);
    }

    private void testOnHoldStatusCompanyUpdateNotAllowed(List<ServiceSubStatusCode> companySubstatuses, ErrorMessage errorMessage, int pEventCount) {
        try {
            for (ServiceSubStatusCode companySubstatus : companySubstatuses) {
                addCompanyOnHoldReason(companySubstatus);
            }
            OFXDataloader ofxDataloader = new OFXDataloader();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX coInfoOFX = ofxDataloader.loadCompany3WithCOINFOMODChangeOnlyLegalNameAndAddress();
            verifyPayrollRejected(coInfoOFX,errorMessage,pEventCount);
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    private void testOnHoldStatusCompanyUpdateAllowed(ServiceSubStatusCode companySubstatus) {
        try {
            addCompanyOnHoldReason(companySubstatus);
            OFXDataloader ofxDataloader = new OFXDataloader();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX coInfoOFX = ofxDataloader.loadCompany3WithCOINFOMODChangeOnlyLegalNameAndAddress();
            QBDTTestHelper.processOFXRequestSuccess(coInfoOFX);
            assertEquals(0,PayrollServices.entityFinder.find(PayrollRun.class).size());
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    private void testOnHoldStatusPayrollSubmitAddEmployee(ServiceSubStatusCode companySubstatus, ErrorMessage errorMessage) {
        List<ServiceSubStatusCode> statusList = new ArrayList<ServiceSubStatusCode>();
        statusList.add(companySubstatus);
        testOnHoldStatusPayrollSubmitAddEmployee(statusList,errorMessage);
    }


    private void testOnHoldStatusPayrollSubmitNoEmpChanges(ServiceSubStatusCode companySubstatus, ErrorMessage errorMessage) {
        List<ServiceSubStatusCode> statusList = new ArrayList<ServiceSubStatusCode>();
        statusList.add(companySubstatus);
        testOnHoldStatusPayrollSubmitNoEmpChanges(statusList,errorMessage);
    }

    private void testOnHoldStatusPayrollSubmitUpdateEmployee(ServiceSubStatusCode companySubstatus, ErrorMessage errorMessage) {
        List<ServiceSubStatusCode> statusList = new ArrayList<ServiceSubStatusCode>();
        statusList.add(companySubstatus);
        testOnHoldStatusPayrollSubmitUpdateEmployee(statusList,errorMessage);
    }

    private void testOnHoldStatusPayrollSubmitAddEmployee(List<ServiceSubStatusCode> companySubstatuses, ErrorMessage errorMessage) {
        try {
            for (ServiceSubStatusCode companySubstatus : companySubstatuses) {
                addCompanyOnHoldReason(companySubstatus);
            }
            OFXDataloader ofxDataloader = new OFXDataloader();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOFX = ofxDataloader.loadHappyPathOFX();
            verifyPayrollRejected(happyPathOFX,errorMessage,2);
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    private void testOnHoldStatusPayrollSubmitNoEmpChanges(List<ServiceSubStatusCode> companySubstatuses, ErrorMessage errorMessage) {
        try {
            OFXDataloader ofxDataloader = new OFXDataloader();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX payroll1OFX = ofxDataloader.loadHappyPathOFXPayroll1();
            QBDTTestHelper.processOFXRequestSuccess(payroll1OFX);
            for (ServiceSubStatusCode companySubstatus : companySubstatuses) {
                addCompanyOnHoldReason(companySubstatus);
            }
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX payroll2OFX = ofxDataloader.loadHappyPathOFXPayroll2();
            verifyPayrollRejected(payroll2OFX,errorMessage,2);
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    private void testOnHoldStatusPayrollSubmitUpdateEmployee(List<ServiceSubStatusCode> companySubstatuses, ErrorMessage errorMessage) {
        try {
            OFXDataloader ofxDataloader = new OFXDataloader();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX payroll1OFX = ofxDataloader.loadHappyPathOFXPayroll1();
            QBDTTestHelper.processOFXRequestSuccess(payroll1OFX);
            for (ServiceSubStatusCode companySubstatus : companySubstatuses) {
                addCompanyOnHoldReason(companySubstatus);
            }
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX payroll2OFX = ofxDataloader.loadHappyPathOFXPayroll2();
            payroll2OFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setACCTID("5555555");
            verifyPayrollRejected(payroll2OFX,errorMessage,2);
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    private void testOnHoldStatusPaycheckMod(List<ServiceSubStatusCode> companySubstatuses) {
        try {
            QBDTTestHelper.processOFXPayrollRequestHappyPath();

            for (ServiceSubStatusCode companySubstatus : companySubstatuses) {
                addCompanyOnHoldReason(companySubstatus);
            }
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();

            List<String> paycheckIdVoidList = new LinkedList<String>();
            paycheckIdVoidList.add("1");
            OFX voidOfxObj= ofxDataloader.loadVoidCompany3Payroll(paycheckIdVoidList,happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDTPAYCHKS());
            QBDTTestHelper.processOFXRequestSuccess(voidOfxObj);
            assertEquals(2,PayrollServices.entityFinder.find(PayrollRun.class).size());
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }

    }

    private void testOnHoldStatusPaycheckMod(ServiceSubStatusCode companySubstatus) {
        List<ServiceSubStatusCode> statusList = new ArrayList<ServiceSubStatusCode>();
        statusList.add(companySubstatus);
        testOnHoldStatusPaycheckMod(statusList);
    }

    private void verifyPayrollRejected(com.intuit.sbd.payroll.psp.common.ofx.request.OFX ofx,ErrorMessage errorMessage, int eventCount) throws Exception {
        // Need session because we are using SPCFCal
        String ofxStr = OFXManager.javaToOFX(ofx,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        QBDTTestHelper.processRequestPayrollError(ofxStr,errorMessage);
        QBDTTestHelper.verifyEventExists(EventTypeCode.PayrollRejected,eventCount);
    }

    public static void addCompanyOnHoldReason(ServiceSubStatusCode serviveOnHoldReason) {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
        assertSuccess("OnHoldReason Added", PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), serviveOnHoldReason));
        PayrollServices.commitUnitOfWork();
    }


    private void testOnHoldSync(ServiceSubStatusCode serviveOnHoldReason) {
        try {
            addCompanyOnHoldReason(serviveOnHoldReason);
            QBDTTestHelper.processOFXSyncRequestHappyPath();
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

}
