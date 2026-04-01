package com.intuit.sbd.payroll.psp.adapters.sap;

import com.intuit.ems.payroll.psp.gateways.ers.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTRequestProcessor;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.QBEmployee;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.QBPaycheck;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.QBPaychecks;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.SubmitPayrollRequest;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.test.*;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.webservices.QBPayrollWebServices;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.CompanyAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.CompanyTranslator;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SourceSystemTransmissionDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Entitlement;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnit;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.gateways.amo.AMOWSGateway;
import com.intuit.sbd.payroll.psp.gateways.amo.AMOWSGatewayFactory;
import com.intuit.sbd.payroll.psp.gateways.amo.AMOWSMockGateway;
import com.intuit.sbd.payroll.psp.gateways.amo.GetCustomerAssetResponseTypeDTO;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.LoadFraudEvents;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;

import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.apache.commons.io.FileUtils;
import org.hibernate.Hibernate;
import org.junit.*;

import java.io.File;
import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: Oct 21, 2009
 * Time: 4:17:53 PM
 */
public class CompanyAdapterTests {


    private CompanyAdapter mCompanyAdapter;
    private DataLoader mDataloader;
    private final String psid = "123456789";

    public CompanyAdapterTests() {
        this.mCompanyAdapter = new CompanyAdapter();
        this.mDataloader = new DataLoader();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        FlexUnitDataLoaderService.AddUsers();
        DataLoadServices.reinitialize();

        AMOWSMockGateway.clear();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        ERSGatewayFactory.setInstanceClass(ERSGateway.class);
        AMOWSGatewayFactory.setInstanceClass(AMOWSGateway.class);
    }

    @Test
    public void testGetCompanyStatus() {
        PayrollServices.beginUnitOfWork();
        Company company1 = mDataloader.persistTestIntuitCompany();
        mDataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        try {
            String sourceSystemCd = company1.getSourceSystemCd().toString();
            String companyId = company1.getSourceCompanyId();

            SAPCompanyStatus sapCompanyStatus = mCompanyAdapter.getCompanyStatus(sourceSystemCd, companyId, true, false);

            assertEquals("SourceSystemCd", sourceSystemCd, sapCompanyStatus.getSourceSystemCd());
            assertEquals("CompanyId", companyId, sapCompanyStatus.getCompanyId());
            assertFalse("isFlaggedForFraud", sapCompanyStatus.isFlaggedForFraud());

            ArrayList<SAPCompanyServiceStatus> serviceStatusList = sapCompanyStatus.getServiceStatusCollection();

            for (SAPCompanyServiceStatus serviceStatus : serviceStatusList) {
                assertEquals("ServiceCd", "DirectDeposit", serviceStatus.getServiceCd());
                assertTrue("CanUpdateStatus", serviceStatus.getCanUpdateStatus());

                SAPServiceStatus status = serviceStatus.getStatus();
                assertEquals("ServiceStatusCd", "PendingActivation", status.getServiceStatusCd());
                assertEquals("ServiceStatusName", "Pending Activation", status.getServiceStatusName());
                assertEquals("ServiceStatusDescription", "Customer is pending activation on the service", status.getServiceStatusDescription());


                ArrayList<SAPServiceSubStatus> subStatusList = status.getServiceSubStatusList();
                assertEquals("SubStatus Collection Size", 1, subStatusList.size());

                SAPServiceSubStatus subStatus = subStatusList.iterator().next();
                assertEquals("SubStatusCd", ServiceSubStatusCode.PendingBankVerification, subStatus.getSubStatusCd());
                assertEquals("SubStatusName", "Pending Bank Verification", subStatus.getSubStatusName());
                assertEquals("SubStatusType", "Pending Activation", subStatus.getSubStatusType());
                assertEquals("SubStatusDescription", "Pre-reqs done for set up and now awaiting the 2 small bank debits.", subStatus.getSubStatusDescription());
                assertFalse("isManuallyUpdatable", subStatus.isManuallyUpdatable());

                ArrayList<SAPServiceStatus> allowedTransitions = serviceStatus.getAllowedTransitions();
                assertEquals("AllowedTransitions Collection Size", 5, allowedTransitions.size());

                for (SAPServiceStatus allowedTransition : allowedTransitions) {
                    if ("Active".equals(allowedTransition.getServiceStatusCd())) {
                        assertEquals("ServiceStatusCd", "Active", allowedTransition.getServiceStatusCd());
                        assertEquals("ServiceStatusName", "Active", allowedTransition.getServiceStatusName());
                        assertEquals("ServiceStatusDescription", "Customer is active on the service", allowedTransition.getServiceStatusDescription());


                        ArrayList<SAPServiceSubStatus> serviceSubStatusList = allowedTransition.getServiceSubStatusList();
                        assertEquals("AllowedTransitions SubStatus Collection Size", 1, serviceSubStatusList.size());

                        SAPServiceSubStatus serviceSubStatus = serviceSubStatusList.iterator().next();
                        assertEquals("SubStatusCd", ServiceSubStatusCode.ActiveCurrent, serviceSubStatus.getSubStatusCd());
                        assertEquals("SubStatusName", "Active Current", serviceSubStatus.getSubStatusName());
                        assertEquals("SubStatusDescription", "Customer is active on the service with no restrictions", serviceSubStatus.getSubStatusDescription());
                        assertEquals("SubStatusType", "Active", serviceSubStatus.getSubStatusType());
                        assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                    } else if ("Cancelled".equals(allowedTransition.getServiceStatusCd())) {
                        assertEquals("ServiceStatusCd", "Cancelled", allowedTransition.getServiceStatusCd());
                        assertEquals("ServiceStatusName", "Cancelled", allowedTransition.getServiceStatusName());
                        assertEquals("ServiceStatusDescription", "Customer cancelled the service", allowedTransition.getServiceStatusDescription());


                        ArrayList<SAPServiceSubStatus> serviceSubStatusList = allowedTransition.getServiceSubStatusList();
                        assertEquals("AllowedTransitions SubStatus Collection Size", 1, serviceSubStatusList.size());

                        SAPServiceSubStatus serviceSubStatus = serviceSubStatusList.iterator().next();
                        assertEquals("SubStatusCd", ServiceSubStatusCode.Cancelled, serviceSubStatus.getSubStatusCd());
                        assertEquals("SubStatusName", "Cancelled", serviceSubStatus.getSubStatusName());
                        assertEquals("SubStatusDescription", "Customer has voluntarily cancelled tax service and all services can be 'turned off'.", serviceSubStatus.getSubStatusDescription());
                        assertEquals("SubStatusType", "Cancelled", serviceSubStatus.getSubStatusType());
                        assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                    } else if ("OnHold".equals(allowedTransition.getServiceStatusCd())) {
                        assertEquals("ServiceStatusCd", "OnHold", allowedTransition.getServiceStatusCd());
                        assertEquals("ServiceStatusName", "On Hold", allowedTransition.getServiceStatusName());
                        assertEquals("ServiceStatusDescription", "Customer is on hold", allowedTransition.getServiceStatusDescription());


                        ArrayList<SAPServiceSubStatus> serviceSubStatusList = allowedTransition.getServiceSubStatusList();
                        assertEquals("AllowedTransitions SubStatus Collection Size", 13, serviceSubStatusList.size());

                        for (SAPServiceSubStatus serviceSubStatus : serviceSubStatusList) {
                            if (ServiceSubStatusCode.IntuitCollections.equals(serviceSubStatus.getSubStatusCd())) {
                                assertEquals("SubStatusCd", ServiceSubStatusCode.IntuitCollections, serviceSubStatus.getSubStatusCd());
                                assertEquals("SubStatusName", "Intuit Collections", serviceSubStatus.getSubStatusName());
                                assertEquals("SubStatusDescription", "Client is placed on hold manually for internal collections which includes the following reasons - Assisted Collectable, Historical Collectable, Repayment Agreement, Notes Receivable etc.", serviceSubStatus.getSubStatusDescription());
                                assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                                assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                            } else if (ServiceSubStatusCode.PendingPrefundingWire.equals(serviceSubStatus.getSubStatusCd())) {
                                assertEquals("SubStatusCd", ServiceSubStatusCode.PendingPrefundingWire, serviceSubStatus.getSubStatusCd());
                                assertEquals("SubStatusName", "Pending Prefunding Wire", serviceSubStatus.getSubStatusName());
                                assertEquals("SubStatusDescription", "Agent places a customer in this status when they are waiting for a customer to submit a payroll that is greater than their dd limits, that will be funded by a wire transaction", serviceSubStatus.getSubStatusDescription());
                                assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                                assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                            } else if (ServiceSubStatusCode.RiskAssessment.equals(serviceSubStatus.getSubStatusCd())) {
                                assertEquals("SubStatusCd", ServiceSubStatusCode.RiskAssessment, serviceSubStatus.getSubStatusCd());
                                assertEquals("SubStatusName", "Risk Assessment", serviceSubStatus.getSubStatusName());
                                assertEquals("SubStatusDescription", "Possible Fraud or Risk to Intuit.  Client that has 3 or more NSFs on file within 12 month period.  Client will also be placed on Risk Assessment hold, when identified as possible fraudulent activity.", serviceSubStatus.getSubStatusDescription());
                                assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                                assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                            } else if (ServiceSubStatusCode.DirectDepositLimit.equals(serviceSubStatus.getSubStatusCd())) {
                                assertEquals("SubStatusCd", ServiceSubStatusCode.DirectDepositLimit, serviceSubStatus.getSubStatusCd());
                                assertEquals("SubStatusName", "Direct Deposit Limit", serviceSubStatus.getSubStatusName());
                                assertEquals("SubStatusDescription", "Client sent payroll over DD Limit, Exposure Limit four times.  Account is automatically placed on hold.", serviceSubStatus.getSubStatusDescription());
                                assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                                assertFalse("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                            } else if (ServiceSubStatusCode.AchRejectOther.equals(serviceSubStatus.getSubStatusCd())) {
                                assertEquals("SubStatusCd", ServiceSubStatusCode.AchRejectOther, serviceSubStatus.getSubStatusCd());
                                assertEquals("SubStatusName", "ACH Reject Other", serviceSubStatus.getSubStatusName());
                                assertEquals("SubStatusDescription", "Payroll debit rejects, Intuit receives bank return file and file is imported into PSP.  In the file, returns are coded, the system needs to automatically place this hold to all returns other than R01 or R09. Client is placed on hold until Intuit receives the funds from the customer.", serviceSubStatus.getSubStatusDescription());
                                assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                                assertFalse("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                            } else if (ServiceSubStatusCode.RiskCollections.equals(serviceSubStatus.getSubStatusCd())) {
                                assertEquals("SubStatusCd", ServiceSubStatusCode.RiskCollections, serviceSubStatus.getSubStatusCd());
                                assertEquals("SubStatusName", "Risk Collections", serviceSubStatus.getSubStatusName());
                                assertEquals("SubStatusDescription", "Hold code changes when ACH Reject is not paid in full, or required information is not received by Intuit from client. ", serviceSubStatus.getSubStatusDescription());
                                assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                                assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                            } else if (ServiceSubStatusCode.FraudReview.equals(serviceSubStatus.getSubStatusCd())) {
                                assertEquals("SubStatusCd", ServiceSubStatusCode.FraudReview, serviceSubStatus.getSubStatusCd());
                                assertEquals("SubStatusName", "Fraud Review", serviceSubStatus.getSubStatusName());
                                assertEquals("SubStatusDescription", "PSP has sytematically identified potential fraud based on criteria.  Risk Assessmenet team would need to remove these holds, but customers can continue to update their accounts, just not process payroll until hold is removed.", serviceSubStatus.getSubStatusDescription());
                                assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                                assertFalse("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                            } else if (ServiceSubStatusCode.SuspendedDirectDeposit.equals(serviceSubStatus.getSubStatusCd())) {
                                assertEquals("SubStatusCd", ServiceSubStatusCode.SuspendedDirectDeposit, serviceSubStatus.getSubStatusCd());
                                assertEquals("SubStatusName", "Suspended Direct Deposit", serviceSubStatus.getSubStatusName());
                                assertEquals("SubStatusDescription", "Financial resolutions has made arrangements with a customer to bar them from using the direct deposit service for a period of time (most likely in increments of 3,6,9, or 12 months?)", serviceSubStatus.getSubStatusDescription());
                                assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                                assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                            } else if (ServiceSubStatusCode.PendingTermination.equals(serviceSubStatus.getSubStatusCd())) {
                                assertEquals("SubStatusCd", ServiceSubStatusCode.PendingTermination, serviceSubStatus.getSubStatusCd());
                                assertEquals("SubStatusName", "Pending Termination", serviceSubStatus.getSubStatusName());
                                assertEquals("SubStatusDescription", "Customer is under review and is pending termination from all services", serviceSubStatus.getSubStatusDescription());
                                assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                                assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                            } else if (ServiceSubStatusCode.Fraud.equals(serviceSubStatus.getSubStatusCd())) {
                                assertEquals("SubStatusCd", ServiceSubStatusCode.Fraud, serviceSubStatus.getSubStatusCd());
                                assertEquals("SubStatusName", "Fraud", serviceSubStatus.getSubStatusName());
                                assertEquals("SubStatusDescription", "SBD Risk Management has identified client as processing fraudulent payrolls.", serviceSubStatus.getSubStatusDescription());
                                assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                                assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                            } else if (ServiceSubStatusCode.AchRejectR1R9.equals(serviceSubStatus.getSubStatusCd())) {
                                assertEquals("SubStatusCd", ServiceSubStatusCode.AchRejectR1R9, serviceSubStatus.getSubStatusCd());
                                assertEquals("SubStatusName", "ACH Reject R01-R09", serviceSubStatus.getSubStatusName());
                                assertEquals("SubStatusDescription", "Anything that is an ER payroll debit rejects and coded R01-R09 receives this hold code. Intuit receives bank return file and file is imported into PSP.  In the file, returns are coded, the system needs to automatically place this hold code for R01 or R09", serviceSubStatus.getSubStatusDescription());
                                assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                                assertFalse("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                            } else if (ServiceSubStatusCode.AMLHold.equals(serviceSubStatus.getSubStatusCd())) {
                                assertEquals("SubStatusCd", ServiceSubStatusCode.AMLHold, serviceSubStatus.getSubStatusCd());
                                assertEquals("SubStatusName", "AML Hold", serviceSubStatus.getSubStatusName());
                                assertEquals("SubStatusDescription", "SBD Risk Management has identified client has failed to clear AML Fraud Check.", serviceSubStatus.getSubStatusDescription());
                                assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                                assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                            } else if (ServiceSubStatusCode.MTLHold.equals(serviceSubStatus.getSubStatusCd())) {
                                assertEquals("SubStatusCd", ServiceSubStatusCode.MTLHold, serviceSubStatus.getSubStatusCd());
                                assertEquals("SubStatusName", "MTL Compliance Hold", serviceSubStatus.getSubStatusName());
                                assertEquals("SubStatusDescription", "Non-compliance to MTL workflow", serviceSubStatus.getSubStatusDescription());
                                assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                                assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                            } else {
                                Assert.fail("Unexpected ServiceSubStatus Type: " + serviceSubStatus.getSubStatusCd());
                            }
                        }
                    } else if ("PendingActivation".equals(allowedTransition.getServiceStatusCd())) {
                        assertEquals("ServiceStatusCd", "PendingActivation", allowedTransition.getServiceStatusCd());
                        assertEquals("ServiceStatusName", "Pending Activation", allowedTransition.getServiceStatusName());
                        assertEquals("ServiceStatusDescription", "Customer is pending activation on the service", allowedTransition.getServiceStatusDescription());


                        ArrayList<SAPServiceSubStatus> serviceSubStatusList = allowedTransition.getServiceSubStatusList();
                        assertEquals("AllowedTransitions SubStatus Collection Size", 3, serviceSubStatusList.size());

                        for (SAPServiceSubStatus serviceSubStatus : serviceSubStatusList) {
                            if (ServiceSubStatusCode.PendingPinCreation.equals(serviceSubStatus.getSubStatusCd())) {
                                assertEquals("SubStatusCd", ServiceSubStatusCode.PendingPinCreation, serviceSubStatus.getSubStatusCd());
                                assertEquals("SubStatusName", "Pending PIN Creation", serviceSubStatus.getSubStatusName());
                                assertEquals("SubStatusDescription", "Status after the customer signup request has been received, and bank verifcation is complete, but prior to the customer creating their PIN", serviceSubStatus.getSubStatusDescription());
                                assertEquals("SubStatusType", "Pending Activation", serviceSubStatus.getSubStatusType());
                                assertFalse("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                            } else if (ServiceSubStatusCode.PendingBankVerification.equals(serviceSubStatus.getSubStatusCd())) {
                                assertEquals("SubStatusCd", ServiceSubStatusCode.PendingBankVerification, serviceSubStatus.getSubStatusCd());
                                assertEquals("SubStatusName", "Pending Bank Verification", serviceSubStatus.getSubStatusName());
                                assertEquals("SubStatusDescription", "Pre-reqs done for set up and now awaiting the 2 small bank debits.", serviceSubStatus.getSubStatusDescription());
                                assertEquals("SubStatusType", "Pending Activation", serviceSubStatus.getSubStatusType());
                                assertFalse("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                            } else if (ServiceSubStatusCode.PendingFirstPayroll.equals(serviceSubStatus.getSubStatusCd())) {
                                assertEquals("SubStatusCd", ServiceSubStatusCode.PendingFirstPayroll, serviceSubStatus.getSubStatusCd());
                                assertEquals("SubStatusName", "Pending First Payroll", serviceSubStatus.getSubStatusName());
                                assertEquals("SubStatusDescription", "Status after the customer Balance file has been received and approved, but before the first payroll has been received ready for that payroll.  Last step in Pending Activation service subStatusCd.", serviceSubStatus.getSubStatusDescription());
                                assertEquals("SubStatusType", "Pending Activation", serviceSubStatus.getSubStatusType());
                                assertFalse("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                            }
                        }
                    } else if ("Terminated".equals(allowedTransition.getServiceStatusCd())) {
                        assertEquals("ServiceStatusCd", "Terminated", allowedTransition.getServiceStatusCd());
                        assertEquals("ServiceStatusName", "Terminated", allowedTransition.getServiceStatusName());
                        assertEquals("ServiceStatusDescription", "Customer was terminated by Intuit", allowedTransition.getServiceStatusDescription());


                        ArrayList<SAPServiceSubStatus> serviceSubStatusList = allowedTransition.getServiceSubStatusList();
                        assertEquals("AllowedTransitions SubStatus Collection Size", 1, serviceSubStatusList.size());

                        SAPServiceSubStatus serviceSubStatus = serviceSubStatusList.iterator().next();
                        assertEquals("SubStatusCd", ServiceSubStatusCode.Terminated, serviceSubStatus.getSubStatusCd());
                        assertEquals("SubStatusName", "Terminated", serviceSubStatus.getSubStatusName());
                        assertEquals("SubStatusDescription", "Intuit has determined the customer in question poses a financial risk and has been terminated from all services", serviceSubStatus.getSubStatusDescription());
                        assertEquals("SubStatusType", "Terminated", serviceSubStatus.getSubStatusType());
                        assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                    } else {
                        Assert.fail("Unexpected ServiceStatusCd: " + allowedTransition.getServiceStatusCd());
                    }

                }
            }

        } catch (Throwable t) {
            fail(t.getMessage());
        }
    }

    @Test
    public void testGetCompanyStatus_OnHold() {
        ACHReturnsDataLoader.loadQBDTPayrollReturned("R01", "NSF description");

        try {
            SAPCompanyStatus sapCompanyStatus = mCompanyAdapter.getCompanyStatus("QBDT", "8574536", true, false);

            assertEquals("SourceSystemCd", "QBDT", sapCompanyStatus.getSourceSystemCd());
            assertEquals("CompanyId", "8574536", sapCompanyStatus.getCompanyId());
            assertFalse("isFlaggedForFraud", sapCompanyStatus.isFlaggedForFraud());

            ArrayList<SAPCompanyServiceStatus> serviceStatusList = sapCompanyStatus.getServiceStatusCollection();
            assertEquals("ServiceStatus Collection Size", 1, serviceStatusList.size());

            SAPCompanyServiceStatus serviceStatus = serviceStatusList.iterator().next();

            assertEquals("ServiceCd", "DirectDeposit", serviceStatus.getServiceCd());
            assertTrue("CanUpdateStatus", serviceStatus.getCanUpdateStatus());

            SAPServiceStatus status = serviceStatus.getStatus();
            assertEquals("ServiceStatusCd", "OnHold", status.getServiceStatusCd());
            assertEquals("ServiceStatusName", "On Hold", status.getServiceStatusName());
            assertEquals("ServiceStatusDescription", "Customer is on hold", status.getServiceStatusDescription());

            ArrayList<SAPServiceSubStatus> subStatusList = status.getServiceSubStatusList();
            assertEquals("SubStatus Collection Size", 1, subStatusList.size());

            for (SAPServiceSubStatus subStatus : subStatusList) {
                if (ServiceSubStatusCode.AchRejectR1R9.equals(subStatus.getSubStatusCd())) {
                    assertEquals("SubStatusName", "ACH Reject R01-R09", subStatus.getSubStatusName());
                    assertEquals("SubStatusType", "On Hold", subStatus.getSubStatusType());
                    assertEquals("SubStatusDescription", "Anything that is an ER payroll debit rejects and coded R01-R09 receives this hold code. Intuit receives bank return file and file is imported into PSP.  In the file, returns are coded, the system needs to automatically place this hold code for R01 or R09", subStatus.getSubStatusDescription());
                    assertTrue("isManuallyUpdatable", subStatus.isManuallyUpdatable());
                } else {
                    Assert.fail("Unexpected SubStatusCd: " + subStatus.getSubStatusCd());
                }
            }

            ArrayList<SAPServiceStatus> allowedTransitions = serviceStatus.getAllowedTransitions();
            assertEquals("AllowedTransitions Collection Size", 5, allowedTransitions.size());

            for (SAPServiceStatus allowedTransition : allowedTransitions) {
                if ("Active".equals(allowedTransition.getServiceStatusCd())) {
                    assertEquals("ServiceStatusCd", "Active", allowedTransition.getServiceStatusCd());
                    assertEquals("ServiceStatusName", "Active", allowedTransition.getServiceStatusName());
                    assertEquals("ServiceStatusDescription", "Customer is active on the service", allowedTransition.getServiceStatusDescription());

                    ArrayList<SAPServiceSubStatus> serviceSubStatusList = allowedTransition.getServiceSubStatusList();
                    assertEquals("AllowedTransitions SubStatus Collection Size", 1, serviceSubStatusList.size());

                    SAPServiceSubStatus serviceSubStatus = serviceSubStatusList.iterator().next();
                    assertEquals("SubStatusCd", ServiceSubStatusCode.ActiveCurrent, serviceSubStatus.getSubStatusCd());
                    assertEquals("SubStatusName", "Active Current", serviceSubStatus.getSubStatusName());
                    assertEquals("SubStatusDescription", "Customer is active on the service with no restrictions", serviceSubStatus.getSubStatusDescription());
                    assertEquals("SubStatusType", "Active", serviceSubStatus.getSubStatusType());
                    assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                } else if ("Cancelled".equals(allowedTransition.getServiceStatusCd())) {
                    assertEquals("ServiceStatusCd", "Cancelled", allowedTransition.getServiceStatusCd());
                    assertEquals("ServiceStatusName", "Cancelled", allowedTransition.getServiceStatusName());
                    assertEquals("ServiceStatusDescription", "Customer cancelled the service", allowedTransition.getServiceStatusDescription());

                    ArrayList<SAPServiceSubStatus> serviceSubStatusList = allowedTransition.getServiceSubStatusList();
                    assertEquals("AllowedTransitions SubStatus Collection Size", 1, serviceSubStatusList.size());

                    SAPServiceSubStatus serviceSubStatus = serviceSubStatusList.iterator().next();
                    assertEquals("SubStatusCd", ServiceSubStatusCode.Cancelled, serviceSubStatus.getSubStatusCd());
                    assertEquals("SubStatusName", "Cancelled", serviceSubStatus.getSubStatusName());
                    assertEquals("SubStatusDescription", "Customer has voluntarily cancelled tax service and all services can be 'turned off'.", serviceSubStatus.getSubStatusDescription());
                    assertEquals("SubStatusType", "Cancelled", serviceSubStatus.getSubStatusType());
                    assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                } else if ("OnHold".equals(allowedTransition.getServiceStatusCd())) {
                    assertEquals("ServiceStatusCd", "OnHold", allowedTransition.getServiceStatusCd());
                    assertEquals("ServiceStatusName", "On Hold", allowedTransition.getServiceStatusName());
                    assertEquals("ServiceStatusDescription", "Customer is on hold", allowedTransition.getServiceStatusDescription());

                    ArrayList<SAPServiceSubStatus> serviceSubStatusList = allowedTransition.getServiceSubStatusList();
                    assertEquals("AllowedTransitions SubStatus Collection Size", 13, serviceSubStatusList.size());

                    for (SAPServiceSubStatus serviceSubStatus : serviceSubStatusList) {
                        if (ServiceSubStatusCode.IntuitCollections.equals(serviceSubStatus.getSubStatusCd())) {
                            assertEquals("SubStatusCd", ServiceSubStatusCode.IntuitCollections, serviceSubStatus.getSubStatusCd());
                            assertEquals("SubStatusName", "Intuit Collections", serviceSubStatus.getSubStatusName());
                            assertEquals("SubStatusDescription", "Client is placed on hold manually for internal collections which includes the following reasons - Assisted Collectable, Historical Collectable, Repayment Agreement, Notes Receivable etc.", serviceSubStatus.getSubStatusDescription());
                            assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                            assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                        } else if (ServiceSubStatusCode.PendingPrefundingWire.equals(serviceSubStatus.getSubStatusCd())) {
                            assertEquals("SubStatusCd", ServiceSubStatusCode.PendingPrefundingWire, serviceSubStatus.getSubStatusCd());
                            assertEquals("SubStatusName", "Pending Prefunding Wire", serviceSubStatus.getSubStatusName());
                            assertEquals("SubStatusDescription", "Agent places a customer in this status when they are waiting for a customer to submit a payroll that is greater than their dd limits, that will be funded by a wire transaction", serviceSubStatus.getSubStatusDescription());
                            assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                            assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                        } else if (ServiceSubStatusCode.RiskAssessment.equals(serviceSubStatus.getSubStatusCd())) {
                            assertEquals("SubStatusCd", ServiceSubStatusCode.RiskAssessment, serviceSubStatus.getSubStatusCd());
                            assertEquals("SubStatusName", "Risk Assessment", serviceSubStatus.getSubStatusName());
                            assertEquals("SubStatusDescription", "Possible Fraud or Risk to Intuit.  Client that has 3 or more NSFs on file within 12 month period.  Client will also be placed on Risk Assessment hold, when identified as possible fraudulent activity.", serviceSubStatus.getSubStatusDescription());
                            assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                            assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                        } else if (ServiceSubStatusCode.DirectDepositLimit.equals(serviceSubStatus.getSubStatusCd())) {
                            assertEquals("SubStatusCd", ServiceSubStatusCode.DirectDepositLimit, serviceSubStatus.getSubStatusCd());
                            assertEquals("SubStatusName", "Direct Deposit Limit", serviceSubStatus.getSubStatusName());
                            assertEquals("SubStatusDescription", "Client sent payroll over DD Limit, Exposure Limit four times.  Account is automatically placed on hold.", serviceSubStatus.getSubStatusDescription());
                            assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                            assertFalse("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                        } else if (ServiceSubStatusCode.AchRejectOther.equals(serviceSubStatus.getSubStatusCd())) {
                            assertEquals("SubStatusCd", ServiceSubStatusCode.AchRejectOther, serviceSubStatus.getSubStatusCd());
                            assertEquals("SubStatusName", "ACH Reject Other", serviceSubStatus.getSubStatusName());
                            assertEquals("SubStatusDescription", "Payroll debit rejects, Intuit receives bank return file and file is imported into PSP.  In the file, returns are coded, the system needs to automatically place this hold to all returns other than R01 or R09. Client is placed on hold until Intuit receives the funds from the customer.", serviceSubStatus.getSubStatusDescription());
                            assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                            assertFalse("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                        } else if (ServiceSubStatusCode.RiskCollections.equals(serviceSubStatus.getSubStatusCd())) {
                            assertEquals("SubStatusCd", ServiceSubStatusCode.RiskCollections, serviceSubStatus.getSubStatusCd());
                            assertEquals("SubStatusName", "Risk Collections", serviceSubStatus.getSubStatusName());
                            assertEquals("SubStatusDescription", "Hold code changes when ACH Reject is not paid in full, or required information is not received by Intuit from client. ", serviceSubStatus.getSubStatusDescription());
                            assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                            assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                        } else if (ServiceSubStatusCode.FraudReview.equals(serviceSubStatus.getSubStatusCd())) {
                            assertEquals("SubStatusCd", ServiceSubStatusCode.FraudReview, serviceSubStatus.getSubStatusCd());
                            assertEquals("SubStatusName", "Fraud Review", serviceSubStatus.getSubStatusName());
                            assertEquals("SubStatusDescription", "PSP has sytematically identified potential fraud based on criteria.  Risk Assessmenet team would need to remove these holds, but customers can continue to update their accounts, just not process payroll until hold is removed.", serviceSubStatus.getSubStatusDescription());
                            assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                            assertFalse("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                        } else if (ServiceSubStatusCode.SuspendedDirectDeposit.equals(serviceSubStatus.getSubStatusCd())) {
                            assertEquals("SubStatusCd", ServiceSubStatusCode.SuspendedDirectDeposit, serviceSubStatus.getSubStatusCd());
                            assertEquals("SubStatusName", "Suspended Direct Deposit", serviceSubStatus.getSubStatusName());
                            assertEquals("SubStatusDescription", "Financial resolutions has made arrangements with a customer to bar them from using the direct deposit service for a period of time (most likely in increments of 3,6,9, or 12 months?)", serviceSubStatus.getSubStatusDescription());
                            assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                            assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                        } else if (ServiceSubStatusCode.PendingTermination.equals(serviceSubStatus.getSubStatusCd())) {
                            assertEquals("SubStatusCd", ServiceSubStatusCode.PendingTermination, serviceSubStatus.getSubStatusCd());
                            assertEquals("SubStatusName", "Pending Termination", serviceSubStatus.getSubStatusName());
                            assertEquals("SubStatusDescription", "Customer is under review and is pending termination from all services", serviceSubStatus.getSubStatusDescription());
                            assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                            assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                        } else if (ServiceSubStatusCode.Fraud.equals(serviceSubStatus.getSubStatusCd())) {
                            assertEquals("SubStatusCd", ServiceSubStatusCode.Fraud, serviceSubStatus.getSubStatusCd());
                            assertEquals("SubStatusName", "Fraud", serviceSubStatus.getSubStatusName());
                            assertEquals("SubStatusDescription", "SBD Risk Management has identified client as processing fraudulent payrolls.", serviceSubStatus.getSubStatusDescription());
                            assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                            assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                        } else if (ServiceSubStatusCode.AchRejectR1R9.equals(serviceSubStatus.getSubStatusCd())) {
                            assertEquals("SubStatusCd", ServiceSubStatusCode.AchRejectR1R9, serviceSubStatus.getSubStatusCd());
                            assertEquals("SubStatusName", "ACH Reject R01-R09", serviceSubStatus.getSubStatusName());
                            assertEquals("SubStatusDescription", "Anything that is an ER payroll debit rejects and coded R01-R09 receives this hold code. Intuit receives bank return file and file is imported into PSP.  In the file, returns are coded, the system needs to automatically place this hold code for R01 or R09", serviceSubStatus.getSubStatusDescription());
                            assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                            assertFalse("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                        } else if (ServiceSubStatusCode.AMLHold.equals(serviceSubStatus.getSubStatusCd())) {
                            assertEquals("SubStatusCd", ServiceSubStatusCode.AMLHold, serviceSubStatus.getSubStatusCd());
                            assertEquals("SubStatusName", "AML Hold", serviceSubStatus.getSubStatusName());
                            assertEquals("SubStatusDescription", "SBD Risk Management has identified client has failed to clear AML Fraud Check.", serviceSubStatus.getSubStatusDescription());
                            assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                            assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                        } else if (ServiceSubStatusCode.MTLHold.equals(serviceSubStatus.getSubStatusCd())) {
                            assertEquals("SubStatusCd", ServiceSubStatusCode.MTLHold, serviceSubStatus.getSubStatusCd());
                            assertEquals("SubStatusName", "MTL Compliance Hold", serviceSubStatus.getSubStatusName());
                            assertEquals("SubStatusDescription", "Non-compliance to MTL workflow", serviceSubStatus.getSubStatusDescription());
                            assertEquals("SubStatusType", "On Hold", serviceSubStatus.getSubStatusType());
                            assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                        }else {
                            Assert.fail("Unexpected ServiceSubStatus Type: " + serviceSubStatus.getSubStatusCd());
                        }
                    }
                } else if ("PendingActivation".equals(allowedTransition.getServiceStatusCd())) {
                    assertEquals("ServiceStatusCd", "PendingActivation", allowedTransition.getServiceStatusCd());
                    assertEquals("ServiceStatusName", "Pending Activation", allowedTransition.getServiceStatusName());
                    assertEquals("ServiceStatusDescription", "Customer is pending activation on the service", allowedTransition.getServiceStatusDescription());

                    ArrayList<SAPServiceSubStatus> serviceSubStatusList = allowedTransition.getServiceSubStatusList();
                    assertEquals("AllowedTransitions SubStatus Collection Size", 3, serviceSubStatusList.size());

                    for (SAPServiceSubStatus serviceSubStatus : serviceSubStatusList) {
                        if (ServiceSubStatusCode.PendingPinCreation.equals(serviceSubStatus.getSubStatusCd())) {
                            assertEquals("SubStatusCd", ServiceSubStatusCode.PendingPinCreation, serviceSubStatus.getSubStatusCd());
                            assertEquals("SubStatusName", "Pending PIN Creation", serviceSubStatus.getSubStatusName());
                            assertEquals("SubStatusDescription", "Status after the customer signup request has been received, and bank verifcation is complete, but prior to the customer creating their PIN", serviceSubStatus.getSubStatusDescription());
                            assertEquals("SubStatusType", "Pending Activation", serviceSubStatus.getSubStatusType());
                            assertFalse("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                        } else if (ServiceSubStatusCode.PendingBankVerification.equals(serviceSubStatus.getSubStatusCd())) {
                            assertEquals("SubStatusCd", ServiceSubStatusCode.PendingBankVerification, serviceSubStatus.getSubStatusCd());
                            assertEquals("SubStatusName", "Pending Bank Verification", serviceSubStatus.getSubStatusName());
                            assertEquals("SubStatusDescription", "Pre-reqs done for set up and now awaiting the 2 small bank debits.", serviceSubStatus.getSubStatusDescription());
                            assertEquals("SubStatusType", "Pending Activation", serviceSubStatus.getSubStatusType());
                            assertFalse("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                        } else if (ServiceSubStatusCode.PendingFirstPayroll.equals(serviceSubStatus.getSubStatusCd())) {
                            assertEquals("SubStatusCd", ServiceSubStatusCode.PendingFirstPayroll, serviceSubStatus.getSubStatusCd());
                            assertEquals("SubStatusName", "Pending First Payroll", serviceSubStatus.getSubStatusName());
                            assertEquals("SubStatusDescription", "Status after the customer Balance file has been received and approved, but before the first payroll has been received ready for that payroll.  Last step in Pending Activation service subStatusCd.", serviceSubStatus.getSubStatusDescription());
                            assertEquals("SubStatusType", "Pending Activation", serviceSubStatus.getSubStatusType());
                            assertFalse("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                        }
                    }
                } else if ("Terminated".equals(allowedTransition.getServiceStatusCd())) {
                    assertEquals("ServiceStatusCd", "Terminated", allowedTransition.getServiceStatusCd());
                    assertEquals("ServiceStatusName", "Terminated", allowedTransition.getServiceStatusName());
                    assertEquals("ServiceStatusDescription", "Customer was terminated by Intuit", allowedTransition.getServiceStatusDescription());

                    ArrayList<SAPServiceSubStatus> serviceSubStatusList = allowedTransition.getServiceSubStatusList();
                    assertEquals("AllowedTransitions SubStatus Collection Size", 1, serviceSubStatusList.size());

                    SAPServiceSubStatus serviceSubStatus = serviceSubStatusList.iterator().next();
                    assertEquals("SubStatusCd", ServiceSubStatusCode.Terminated, serviceSubStatus.getSubStatusCd());
                    assertEquals("SubStatusName", "Terminated", serviceSubStatus.getSubStatusName());
                    assertEquals("SubStatusDescription", "Intuit has determined the customer in question poses a financial risk and has been terminated from all services", serviceSubStatus.getSubStatusDescription());
                    assertEquals("SubStatusType", "Terminated", serviceSubStatus.getSubStatusType());
                    assertTrue("isManuallyUpdatable", serviceSubStatus.isManuallyUpdatable());
                } else {
                    Assert.fail("Unexpected ServiceStatusCd: " + allowedTransition.getServiceStatusCd());
                }

            }
        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.getMessage());
        }
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testGetCompanyServiceStatus() throws Throwable {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader.loadDataHappyPath();
        PayrollServices.commitUnitOfWork();


        Date fromDate = CalendarUtils.convertToDate(SpcfCalendar.createInstance(2007, 8, 1, SpcfTimeZone.getLocalTimeZone()));
        Date toDate = CalendarUtils.convertToDate(SpcfCalendar.createInstance(2007, 9, 30, SpcfTimeZone.getLocalTimeZone()));

        SAPCompanyEventQueryReturn sapCompanyEventQueryReturn = null;
        try {
            sapCompanyEventQueryReturn = mCompanyAdapter.findCompanyEvents
                    ("QBOE", "1234567", fromDate, toDate, "AchOffloadBatchJob", null, false);
        } catch (Throwable t) {
            fail(t.getMessage());
        }

        ArrayList<SAPCompanyEvent> sapCompanyEvents = sapCompanyEventQueryReturn.getEvents();
        assertEquals("Events List Count", 9, sapCompanyEvents.size());

        for (SAPCompanyEvent sapCompanyEvent : sapCompanyEvents) {
            if (EventTypeCode.CompanyBankAccountStatusChange.equals(sapCompanyEvent.getEventTypeCd())) {
                assertEquals("EventTypeName", "Company Bank Account Status Change", sapCompanyEvent.getEventTypeName());
                assertEquals("EventTypeDescription", "<a href=\"event:goBanks\">Bank account</a> status changed from {OldBAStatus} to {NewBAStatus}", sapCompanyEvent.getEventTypeDescription());
                assertEquals("StatusCd", CompanyEventStatus.Active, sapCompanyEvent.getStatusCd());
                assertEquals("CreatorId", "AchOffloadBatchJob", sapCompanyEvent.getCreatorId());
                assertEquals("EventGroupCode", EventGroup.CompanyInfo, sapCompanyEvent.getEventGroupCode());
                assertNull("LastNoteDate", sapCompanyEvent.getLastNoteDate());

                ArrayList<SAPCompanyEventDetail> sapCompanyEventDetails = sapCompanyEvent.getCompanyEventDetails();
                assertEquals("EventDetails List Count", 3, sapCompanyEventDetails.size());

                for (SAPCompanyEventDetail sapCompanyEventDetail : sapCompanyEventDetails) {
                    if (EventDetailTypeCode.NewBAStatus.equals(sapCompanyEventDetail.getEventDetailTypeCd())) {
                        assertEquals("Name", "New Bank Account Status", sapCompanyEventDetail.getName());
                        assertEquals("Value", "Active", sapCompanyEventDetail.getValue());
                        assertEquals("ValueClassName", "com.intuit.sbd.payroll.psp.domain.BankAccountStatus", sapCompanyEventDetail.getValueClassName());
                    } else if (EventDetailTypeCode.CompanyBankAccountId.equals(sapCompanyEventDetail.getEventDetailTypeCd())) {
                        assertEquals("Name", "Company Bank Account Id", sapCompanyEventDetail.getName());
                        //assertEquals("Value", "8af75a24-23c9-427a-9629-0ee2bd5a4952", sapCompanyEventDetail.getValue());
                        assertEquals("ValueClassName", "com.intuit.spc.foundations.portability.SpcfUniqueId", sapCompanyEventDetail.getValueClassName());
                    } else if (EventDetailTypeCode.OldBAStatus.equals(sapCompanyEventDetail.getEventDetailTypeCd())) {
                        assertEquals("Name", "Old Bank Account Status", sapCompanyEventDetail.getName());
                        assertEquals("Value", "Pending Verification", sapCompanyEventDetail.getValue());
                        assertEquals("ValueClassName", "com.intuit.sbd.payroll.psp.domain.BankAccountStatus", sapCompanyEventDetail.getValueClassName());
                    } else {
                        Assert.fail("Unexpected EventDetailTypeCd: " + sapCompanyEventDetail.getEventDetailTypeCd());
                    }
                }

                ArrayList<SAPCompanyEventEmail> sapCompanyEventEmail = sapCompanyEvent.getCompanyEventEmails();
                assertEquals("CompanyEventEmails List Count", 0, sapCompanyEventEmail.size());
            } else if (EventTypeCode.ServiceStatusChange.equals(sapCompanyEvent.getEventTypeCd())) {
                assertEquals("EventTypeName", "Service Status Change", sapCompanyEvent.getEventTypeName());
                assertEquals("EventTypeDescription", "Service status changed from {OldServiceStatus} to {NewServiceStatus}", sapCompanyEvent.getEventTypeDescription());
                assertEquals("StatusCd", CompanyEventStatus.Active, sapCompanyEvent.getStatusCd());
                assertEquals("CreatorId", "AchOffloadBatchJob", sapCompanyEvent.getCreatorId());
                assertEquals("EventGroupCode", EventGroup.CompanyInfo, sapCompanyEvent.getEventGroupCode());
                assertNull("LastNoteDate", sapCompanyEvent.getLastNoteDate());

                ArrayList<SAPCompanyEventDetail> sapCompanyEventDetails = sapCompanyEvent.getCompanyEventDetails();
                assertEquals("EventDetails List Count", 4, sapCompanyEventDetails.size());

                for (SAPCompanyEventDetail sapCompanyEventDetail : sapCompanyEventDetails) {
                    if (EventDetailTypeCode.ServiceCode.equals(sapCompanyEventDetail.getEventDetailTypeCd())) {
                        assertEquals("Name", "Service Code", sapCompanyEventDetail.getName());
                        assertEquals("Value", "Direct Deposit Service", sapCompanyEventDetail.getValue());
                        assertEquals("ValueClassName", "com.intuit.sbd.payroll.psp.domain.ServiceCode", sapCompanyEventDetail.getValueClassName());
                    } else if (EventDetailTypeCode.OldServiceStatus.equals(sapCompanyEventDetail.getEventDetailTypeCd())) {
                        assertEquals("Name", "Old Service Status", sapCompanyEventDetail.getName());
                        //assertEquals("Value", "Pending Bank Verification", sapCompanyEventDetail.getValue());
                        assertEquals("ValueClassName", "com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode", sapCompanyEventDetail.getValueClassName());
                    } else if (EventDetailTypeCode.NewServiceStatus.equals(sapCompanyEventDetail.getEventDetailTypeCd())) {
                        assertEquals("Name", "New Service Status", sapCompanyEventDetail.getName());
                        //assertEquals("Value", "Pending First Payroll", sapCompanyEventDetail.getValue());
                        assertEquals("ValueClassName", "com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode", sapCompanyEventDetail.getValueClassName());
                    } else if (EventDetailTypeCode.CompanyServiceId.equals(sapCompanyEventDetail.getEventDetailTypeCd())) {
                        assertEquals("Name", "Company Service Id", sapCompanyEventDetail.getName());
                    } else {
                        Assert.fail("Unexpected EventDetailTypeCd: " + sapCompanyEventDetail.getEventDetailTypeCd());
                    }
                }

                ArrayList<SAPCompanyEventEmail> sapCompanyEventEmail = sapCompanyEvent.getCompanyEventEmails();
                assertEquals("CompanyEventEmails List Count", 0, sapCompanyEventEmail.size());
            } else if (EventTypeCode.BankAccountVerified.equals(sapCompanyEvent.getEventTypeCd())) {
                assertEquals("EventTypeName", "Bank Account Verified", sapCompanyEvent.getEventTypeName());
                assertEquals("EventTypeDescription", "<a href=\"event:goBanks\">Bank account</a> successfully verified", sapCompanyEvent.getEventTypeDescription());
                assertEquals("StatusCd", CompanyEventStatus.Active, sapCompanyEvent.getStatusCd());
                assertEquals("CreatorId", "AchOffloadBatchJob", sapCompanyEvent.getCreatorId());
                assertEquals("EventGroupCode", EventGroup.Bank, sapCompanyEvent.getEventGroupCode());
                assertNull("LastNoteDate", sapCompanyEvent.getLastNoteDate());

                ArrayList<SAPCompanyEventDetail> sapCompanyEventDetails = sapCompanyEvent.getCompanyEventDetails();
                assertEquals("EventDetails List Count", 2, sapCompanyEventDetails.size());

                for (SAPCompanyEventDetail sapCompanyEventDetail : sapCompanyEventDetails) {
                    if (EventDetailTypeCode.CompanyBankAccountId.equals(sapCompanyEventDetail.getEventDetailTypeCd())) {
                        assertEquals("Name", "Company Bank Account Id", sapCompanyEventDetail.getName());
                        //assertEquals("Value", "572de546-36a7-4d74-b852-a8b1a31cdab7", sapCompanyEventDetail.getValue());
                        assertEquals("ValueClassName", "com.intuit.spc.foundations.portability.SpcfUniqueId", sapCompanyEventDetail.getValueClassName());
                    } else if (EventDetailTypeCode.CompanyServiceId.equals(sapCompanyEventDetail.getEventDetailTypeCd())) {
                        assertEquals("Name", "Company Service Id", sapCompanyEventDetail.getName());
                        assertEquals("Value", ACHReturnsDataLoader.c1dl.getCompany().getCompanyService(ServiceCode.DirectDeposit).getId().toString(), sapCompanyEventDetail.getValue());
                    } else {
                        Assert.fail("Unexpected EventDetailTypeCd: " + sapCompanyEventDetail.getEventDetailTypeCd());
                    }
                }

                ArrayList<SAPCompanyEventEmail> sapCompanyEventEmail = sapCompanyEvent.getCompanyEventEmails();
                assertEquals("CompanyEventEmails List Count", 0, sapCompanyEventEmail.size());
            } else if (EventTypeCode.BackdatedPayrollReceived.equals(sapCompanyEvent.getEventTypeCd())) {
                assertEquals("EventTypeName", "Backdated Payroll Received", sapCompanyEvent.getEventTypeName());
                assertEquals("EventTypeDescription", "Backdated {SourcePayrollRunId} received", sapCompanyEvent.getEventTypeDescription());
                assertEquals("StatusCd", CompanyEventStatus.Active, sapCompanyEvent.getStatusCd());
                assertEquals("CreatorId", "AchOffloadBatchJob", sapCompanyEvent.getCreatorId());
                assertEquals("EventGroupCode", EventGroup.FinancialOps, sapCompanyEvent.getEventGroupCode());
                assertNull("LastNoteDate", sapCompanyEvent.getLastNoteDate());

                ArrayList<SAPCompanyEventDetail> sapCompanyEventDetails = sapCompanyEvent.getCompanyEventDetails();
                assertEquals("EventDetails List Count", 2, sapCompanyEventDetails.size());

                for (SAPCompanyEventDetail sapCompanyEventDetail : sapCompanyEventDetails) {
                    if (EventDetailTypeCode.SourcePayrollRunId.equals(sapCompanyEventDetail.getEventDetailTypeCd())) {
                        assertEquals("Name", "Source System Payroll Run Id", sapCompanyEventDetail.getName());
                        assertEquals("Value", "BatchTest05", sapCompanyEventDetail.getValue());
                        assertEquals("ValueClassName", "java.lang.String", sapCompanyEventDetail.getValueClassName());
                    } else if (EventDetailTypeCode.PayrollRunId.equals(sapCompanyEventDetail.getEventDetailTypeCd())) {
                        assertEquals("Name", "Payroll Run Id", sapCompanyEventDetail.getName());
                        //assertEquals("Value", "91fae08e-d303-40f7-afd1-5c9a11aae163", sapCompanyEventDetail.getValue());
                        assertEquals("ValueClassName", "com.intuit.spc.foundations.portability.SpcfUniqueId", sapCompanyEventDetail.getValueClassName());
                    } else {
                        Assert.fail("Unexpected EventDetailTypeCd: " + sapCompanyEventDetail.getEventDetailTypeCd());
                    }
                }

                ArrayList<SAPCompanyEventEmail> sapCompanyEventEmail = sapCompanyEvent.getCompanyEventEmails();
                assertEquals("CompanyEventEmails List Count", 0, sapCompanyEventEmail.size());
            } else if (EventTypeCode.FirstPayrollReceived.equals(sapCompanyEvent.getEventTypeCd())) {
                assertEquals("EventTypeName", "First Payroll Received", sapCompanyEvent.getEventTypeName());
                assertEquals("EventTypeDescription", "First {SourcePayrollRunId} received", sapCompanyEvent.getEventTypeDescription());
                assertEquals("StatusCd", CompanyEventStatus.Active, sapCompanyEvent.getStatusCd());
                assertEquals("CreatorId", "AchOffloadBatchJob", sapCompanyEvent.getCreatorId());
                assertEquals("EventGroupCode", EventGroup.PayrollStatus, sapCompanyEvent.getEventGroupCode());
                assertNull("LastNoteDate", sapCompanyEvent.getLastNoteDate());

                ArrayList<SAPCompanyEventDetail> sapCompanyEventDetails = sapCompanyEvent.getCompanyEventDetails();
                assertEquals("EventDetails List Count", 3, sapCompanyEventDetails.size());

                for (SAPCompanyEventDetail sapCompanyEventDetail : sapCompanyEventDetails) {
                    if (EventDetailTypeCode.SourcePayrollRunId.equals(sapCompanyEventDetail.getEventDetailTypeCd())) {
                        assertEquals("Name", "Source System Payroll Run Id", sapCompanyEventDetail.getName());
                        assertEquals("Value", "BatchTest05", sapCompanyEventDetail.getValue());
                        assertEquals("ValueClassName", "java.lang.String", sapCompanyEventDetail.getValueClassName());
                    } else if (EventDetailTypeCode.PayrollRunId.equals(sapCompanyEventDetail.getEventDetailTypeCd())) {
                        assertEquals("Name", "Payroll Run Id", sapCompanyEventDetail.getName());
                        //assertEquals("Value", "91fae08e-d303-40f7-afd1-5c9a11aae163", sapCompanyEventDetail.getValue());
                        assertEquals("ValueClassName", "com.intuit.spc.foundations.portability.SpcfUniqueId", sapCompanyEventDetail.getValueClassName());
                    } else if (EventDetailTypeCode.ServiceCode.equals(sapCompanyEventDetail.getEventDetailTypeCd())) {
                        assertEquals("Name", "Service Code", sapCompanyEventDetail.getName());
                        assertEquals("ValueClassName", "com.intuit.sbd.payroll.psp.domain.ServiceCode", sapCompanyEventDetail.getValueClassName());
                    } else {
                        Assert.fail("Unexpected EventDetailTypeCd: " + sapCompanyEventDetail.getEventDetailTypeCd());
                    }
                }

                ArrayList<SAPCompanyEventEmail> sapCompanyEventEmail = sapCompanyEvent.getCompanyEventEmails();
                assertEquals("CompanyEventEmails List Count", 0, sapCompanyEventEmail.size());
            } else if (EventTypeCode.PayrollReceived.equals(sapCompanyEvent.getEventTypeCd())) {
                assertEquals("EventTypeName", "Payroll Received", sapCompanyEvent.getEventTypeName());
                assertEquals("EventTypeDescription", "A {SourcePayrollRunId} was received", sapCompanyEvent.getEventTypeDescription());
                assertEquals("StatusCd", CompanyEventStatus.Active, sapCompanyEvent.getStatusCd());
                assertEquals("CreatorId", "AchOffloadBatchJob", sapCompanyEvent.getCreatorId());
                assertEquals("EventGroupCode", EventGroup.PayrollStatus, sapCompanyEvent.getEventGroupCode());
                assertNull("LastNoteDate", sapCompanyEvent.getLastNoteDate());

                ArrayList<SAPCompanyEventDetail> sapCompanyEventDetails = sapCompanyEvent.getCompanyEventDetails();
                assertEquals("EventDetails List Count", 2, sapCompanyEventDetails.size());

                for (SAPCompanyEventDetail sapCompanyEventDetail : sapCompanyEventDetails) {
                    if (EventDetailTypeCode.SourcePayrollRunId.equals(sapCompanyEventDetail.getEventDetailTypeCd())) {
                        assertEquals("Name", "Source System Payroll Run Id", sapCompanyEventDetail.getName());
                        assertEquals("Value", "BatchTest05", sapCompanyEventDetail.getValue());
                        assertEquals("ValueClassName", "java.lang.String", sapCompanyEventDetail.getValueClassName());
                    } else if (EventDetailTypeCode.PayrollRunId.equals(sapCompanyEventDetail.getEventDetailTypeCd())) {
                        assertEquals("Name", "Payroll Run Id", sapCompanyEventDetail.getName());
                        //assertEquals("Value", "91fae08e-d303-40f7-afd1-5c9a11aae163", sapCompanyEventDetail.getValue());
                        assertEquals("ValueClassName", "com.intuit.spc.foundations.portability.SpcfUniqueId", sapCompanyEventDetail.getValueClassName());
                    } else {
                        Assert.fail("Unexpected EventDetailTypeCd: " + sapCompanyEventDetail.getEventDetailTypeCd());
                    }
                }

                ArrayList<SAPCompanyEventEmail> sapCompanyEventEmail = sapCompanyEvent.getCompanyEventEmails();
                assertEquals("CompanyEventEmails List Count", 0, sapCompanyEventEmail.size());
            } else if (EventTypeCode.EmployeeBankAccountChange.equals(sapCompanyEvent.getEventTypeCd())) {
                assertEquals("EventTypeName", "Employee Bank Account Change", sapCompanyEvent.getEventTypeName());
                assertEquals("EventTypeDescription", "Employee bank account changed", sapCompanyEvent.getEventTypeDescription());
                assertEquals("StatusCd", CompanyEventStatus.Active, sapCompanyEvent.getStatusCd());
                assertEquals("CreatorId", "AchOffloadBatchJob", sapCompanyEvent.getCreatorId());
                assertEquals("EventGroupCode", EventGroup.CompanyInfo, sapCompanyEvent.getEventGroupCode());
                assertNull("LastNoteDate", sapCompanyEvent.getLastNoteDate());

                ArrayList<SAPCompanyEventDetail> sapCompanyEventDetails = sapCompanyEvent.getCompanyEventDetails();
                assertEquals("EventDetails List Count", 3, sapCompanyEventDetails.size());

                for (SAPCompanyEventDetail sapCompanyEventDetail : sapCompanyEventDetails) {
                    if (EventDetailTypeCode.NewEmployeeBankAccountId.equals(sapCompanyEventDetail.getEventDetailTypeCd())) {
                        assertEquals("Name", "New Employee Bank Account Id", sapCompanyEventDetail.getName());
                        assertNotNull(sapCompanyEventDetail.getValue().toString());
                        assertEquals("ValueClassName", "java.lang.String", sapCompanyEventDetail.getValueClassName());
                    } else if (EventDetailTypeCode.OldEmployeeBankAccountId.equals(sapCompanyEventDetail.getEventDetailTypeCd())) {
                        assertEquals("Name", "Old Employee Bank Account Id", sapCompanyEventDetail.getName());
                        assertNull(sapCompanyEventDetail.getValue());
                        assertEquals("ValueClassName", "java.lang.String", sapCompanyEventDetail.getValueClassName());
                    } else if (EventDetailTypeCode.CompanyServiceId.equals(sapCompanyEventDetail.getEventDetailTypeCd())) {
                        assertEquals("Name", "Company Service Id", sapCompanyEventDetail.getName());
                        assertNotNull(sapCompanyEventDetail.getValue().toString());
                        assertEquals("ValueClassName", "com.intuit.spc.foundations.portability.SpcfUniqueId", sapCompanyEventDetail.getValueClassName());
                    }  else {
                        Assert.fail("Unexpected EventDetailTypeCd: " + sapCompanyEventDetail.getEventDetailTypeCd());
                    }
                }

                ArrayList<SAPCompanyEventEmail> sapCompanyEventEmail = sapCompanyEvent.getCompanyEventEmails();
                assertEquals("CompanyEventEmails List Count", 0, sapCompanyEventEmail.size());
            } else if (!EventTypeCode.EmployeeAdded.equals(sapCompanyEvent.getEventTypeCd())){
                Assert.fail("Unexpected EventTypeCd: " + sapCompanyEvent.getEventTypeCd());
            }
        }

        List<SAPCompanyEventGroup> sapCompanyEventGroups = mCompanyAdapter.findCompanyEventGroups("QBOE", "1234567");
        assertEquals("Groups List Count", 4, sapCompanyEventGroups.size());

        for (SAPCompanyEventGroup sapCompanyEventGroup : sapCompanyEventGroups) {
            if ("CompanyInfo".equals(sapCompanyEventGroup.getEventGroupCode())) {
                assertEquals("Name", "CompanyInfo", sapCompanyEventGroup.getName());

                ArrayList<SAPCompanyEventGroupItem> companyEventGroupItems = sapCompanyEventGroup.getChildren();
                for (SAPCompanyEventGroupItem companyEventGroupItem : companyEventGroupItems) {
                    if ("CustomerSignedUp".equals(companyEventGroupItem.getEventTypeCd())) {
                        assertEquals("EventTypeName", "Customer Signed Up", companyEventGroupItem.getEventTypeName());
                    } else if ("CompanyBankAccountStatusChange".equals(companyEventGroupItem.getEventTypeCd())) {
                        assertEquals("EventTypeName", "Company Bank Account Status Change", companyEventGroupItem.getEventTypeName());
                    } else if ("ServiceStatusChange".equals(companyEventGroupItem.getEventTypeCd())) {
                        assertEquals("EventTypeName", "Service Status Change", companyEventGroupItem.getEventTypeName());
                    } else if ("CompanyBankAccountChange".equals(companyEventGroupItem.getEventTypeCd())) {
                        assertEquals("EventTypeName", "Company Bank Account Changed", companyEventGroupItem.getEventTypeName());
                    } else if ("EmployeeBankAccountChange".equals(companyEventGroupItem.getEventTypeCd())) {
                        assertEquals("EventTypeName", "Employee Bank Account Change", companyEventGroupItem.getEventTypeName());
                    } else if (!"EmployeeAdded".equals(companyEventGroupItem.getEventTypeCd())){
                        Assert.fail("Unexpected CompanyEventGroupItem: " + companyEventGroupItem.getEventTypeCd());
                    }
                }
                assertEquals("EventGroupItem List Count", 6, companyEventGroupItems.size());
            } else if ("Bank".equals(sapCompanyEventGroup.getEventGroupCode())) {
                assertEquals("Name", "Bank", sapCompanyEventGroup.getName());

                ArrayList<SAPCompanyEventGroupItem> companyEventGroupItems = sapCompanyEventGroup.getChildren();
                assertEquals("EventGroupItem List Count", 1, companyEventGroupItems.size());

                for (SAPCompanyEventGroupItem companyEventGroupItem : companyEventGroupItems) {
                    if ("BankAccountVerified".equals(companyEventGroupItem.getEventTypeCd())) {
                        assertEquals("EventTypeName", "Bank Account Verified", companyEventGroupItem.getEventTypeName());
                    } else {
                        Assert.fail("Unexpected CompanyEventGroupItem: " + companyEventGroupItem.getEventTypeCd());
                    }
                }
            } else if ("PayrollStatus".equals(sapCompanyEventGroup.getEventGroupCode())) {
                assertEquals("Name", "PayrollStatus", sapCompanyEventGroup.getName());

                ArrayList<SAPCompanyEventGroupItem> companyEventGroupItems = sapCompanyEventGroup.getChildren();
                assertEquals("EventGroupItem List Count", 2, companyEventGroupItems.size());

                for (SAPCompanyEventGroupItem companyEventGroupItem : companyEventGroupItems) {
                    if ("PayrollReceived".equals(companyEventGroupItem.getEventTypeCd())) {
                        assertEquals("EventTypeName", "Payroll Received", companyEventGroupItem.getEventTypeName());
                    } else if ("FirstPayrollReceived".equals(companyEventGroupItem.getEventTypeCd())) {
                        assertEquals("EventTypeName", "First Payroll Received", companyEventGroupItem.getEventTypeName());
                    } else {
                        Assert.fail("Unexpected CompanyEventGroupItem: " + companyEventGroupItem.getEventTypeCd());
                    }
                }
            } else if ("FinancialOps".equals(sapCompanyEventGroup.getEventGroupCode())) {
                assertEquals("Name", "FinancialOps", sapCompanyEventGroup.getName());

                ArrayList<SAPCompanyEventGroupItem> companyEventGroupItems = sapCompanyEventGroup.getChildren();
                assertEquals("EventGroupItem List Count", 1, companyEventGroupItems.size());

                for (SAPCompanyEventGroupItem companyEventGroupItem : companyEventGroupItems) {
                    if ("BackdatedPayrollReceived".equals(companyEventGroupItem.getEventTypeCd())) {
                        assertEquals("EventTypeName", "Backdated Payroll Received", companyEventGroupItem.getEventTypeName());
                    } else {
                        Assert.fail("Unexpected CompanyEventGroupItem: " + companyEventGroupItem.getEventTypeCd());
                    }
                }
            } else {
                Assert.fail("Unexpected EventGroupCode: " + sapCompanyEventGroup.getEventGroupCode());
            }
        }

        List<SAPUser> creators = mCompanyAdapter.findCompanyEventCreators("QBOE", "1234567");
        assertEquals("Creators List Count", 3, creators.size());
        List<String> expectedCorpIDs = new ArrayList<String>(Arrays.asList("", "UnitTest", "AchOffloadBatchJob"));
        for (SAPUser creator : creators) {
            assertNotNull(expectedCorpIDs.remove(creator.getCorpId()));
        }
    }
    // doesn't make sense after modification to the fraud rule which takes into account test debit verification date instead of sign up date
    @Ignore @Test
    public void testFindCompanyFraudEvents() throws Throwable {
        LoadFraudEvents.load6PayrolLEventsFrom4Companies();


        Date fromDate = CalendarUtils.convertToDate(SpcfCalendar.createInstance(2007, 8, 1, SpcfTimeZone.getLocalTimeZone()));
        Date toDate = CalendarUtils.convertToDate(SpcfCalendar.createInstance(2007, 9, 30, SpcfTimeZone.getLocalTimeZone()));

        ArrayList<SAPFraudEvent> sapFraudEvents;

        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents("48484848488", null, 0.00, fromDate, toDate, null);

        assertEquals("FraudEvents List Count", 1, sapFraudEvents.size());

        SAPFraudEvent sapFraudEvent = sapFraudEvents.iterator().next();
        assertEquals("CompanyEin", "847656466", sapFraudEvent.getCompanyEin());
        assertEquals("CompanyId", "48484848488", sapFraudEvent.getCompanyId());
        assertEquals("CompanyName", "Intuit", sapFraudEvent.getCompanyName());
        assertNull("EmployeeName", sapFraudEvent.getEmployeeName());
        assertEquals("Details", "This company was not activated because the company bank account matches the company bank account of company Intuit (Source System=QBOE Source ID=1234567) with status of On Hold (Fraud).", sapFraudEvent.getDetails());
        assertFalse("isFraudFlagSet", sapFraudEvent.isFraudFlagSet());
        assertNull("SourcePayRunId", sapFraudEvent.getSourcePayRunId());
        assertNull("PayrollRunDate", sapFraudEvent.getPayrollRunDate());
        assertNull("PayrollCheckDate", sapFraudEvent.getPayrollCheckDate());
        assertNull("PayrollRunStatus", sapFraudEvent.getPayrollRunStatus());
        assertEquals("FraudIndicator", "SignUp", sapFraudEvent.getFraudIndicator());
        assertEquals("SourceSystemCd", "QBOE", sapFraudEvent.getSourceSystemCd());
        assertEquals("PayrollAmount", -1.0, sapFraudEvent.getPayrollAmount());


        //test various scenarios
        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents(null, null, -1.0, null, null, null);
        assertEventsEqual(sapFraudEvents, 0, 1, 2, 3);
        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents(null, null, -1.0, null, null, getEventCodes(0));
        assertEventsEqual(sapFraudEvents, 0);
        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents("999", null, -1.0, null, null, null);
        assertEventsEqual(sapFraudEvents, 1, 2);
        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents("999", null, -1.0, null, null, getEventCodes(1, 2));
        assertEventsEqual(sapFraudEvents, 1, 2);
        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents(null, FraudEventCategory.Payroll.toString(), -1.0, null, null, null);
        assertEventsEqual(sapFraudEvents, 0, 1, 2, 3);
        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents(null, FraudEventCategory.Payroll.toString(), -1.0, null, null, getEventCodes(0, 3));
        assertEventsEqual(sapFraudEvents, 0, 3);
        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents(null, FraudEventCategory.SignUp.toString(), -1.0, null, null, null);
        assertEventsEqual(sapFraudEvents, 4, 5);
        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents("877", FraudEventCategory.SignUp.toString(), -1.0, null, null, null);
        assertEventsEqual(sapFraudEvents, 5);
        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents(null, null, 1600, null, null, null);
        assertEventsEqual(sapFraudEvents, 1, 2, 3, 4, 5);
        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents(null, FraudEventCategory.Payroll.toString(), 1600, null, null, null);
        assertEventsEqual(sapFraudEvents, 1, 2, 3);
        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents(null, FraudEventCategory.Payroll.toString(), 1600, null, null, getEventCodes(6));
        assertEventsEqual(sapFraudEvents);


        Calendar c1 = Calendar.getInstance();
        c1.set(2007, 8, 2);
        Calendar c2 = Calendar.getInstance();
        c2.set(2007, 8, 4);
        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents(null, null, -1.0, c1.getTime(), c2.getTime(), null);
        assertEventsEqual(sapFraudEvents, 0, 4);
        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents(null, null, -1.0, c1.getTime(), c2.getTime(), getEventCodes(4));
        assertEventsEqual(sapFraudEvents, 4);


        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents("1234567", FraudEventCategory.Payroll.toString(), 180, c1.getTime(), c2.getTime(), null);
        assertEventsEqual(sapFraudEvents, 0);

        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents("52834234324", FraudEventCategory.Payroll.toString(), 180, c1.getTime(), c2.getTime(), null);
        assertEventsEqual(sapFraudEvents);
        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents("52834234324", null, -1, null, null, null);
        assertEventsEqual(sapFraudEvents);

        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents(null, FraudEventCategory.Payroll.toString(), 8601.11, null, null, null);
        assertEventsEqual(sapFraudEvents, 3);


        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents(null, FraudEventCategory.Payroll.toString(), 22323.34, null, null, null);
        assertEventsEqual(sapFraudEvents);

        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents(null, null, 22323.34, null, null, null);
        assertEventsEqual(sapFraudEvents, 4, 5);

        Calendar c3 = Calendar.getInstance();
        c1.set(2008, 8, 2);
        Calendar c4 = Calendar.getInstance();
        c2.set(2008, 8, 4);
        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents(null, null, -1, c3.getTime(), c4.getTime(), null);
        assertEventsEqual(sapFraudEvents);

        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents("8574536", null, -1, null, null, null);
        assertEventsEqual(sapFraudEvents, 3);

        //remove a fraud flag
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.removeFraudFlag(SourceSystemCode.QBOE, "2222222");
        PayrollServices.commitUnitOfWork();

        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents(null, null, -1, null, null, null);
        assertEventsEqual(sapFraudEvents, 0, 3, 4, 5);
        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents("2222222", null, -1, null, null, null);
        assertEventsEqual(sapFraudEvents);

        //remove fraud review
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.removeOnHoldReason(SourceSystemCode.QBDT, "8774536", ServiceSubStatusCode.FraudReview);
        PayrollServices.commitUnitOfWork();

        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents(null, null, -1, null, null, null);
        assertEventsEqual(sapFraudEvents, 0, 3, 4);
        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents("8774536", null, -1, null, null, null);
        assertEventsEqual(sapFraudEvents);

        //remove the rest
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.removeFraudFlag(SourceSystemCode.QBOE, "1234567");
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.removeFraudFlag(SourceSystemCode.QBDT, "8574536");
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.removeOnHoldReason(SourceSystemCode.QBOE, "48484848488", ServiceSubStatusCode.FraudReview);
        PayrollServices.commitUnitOfWork();

        sapFraudEvents = mCompanyAdapter.findCompanyFraudEvents(null, null, -1, null, null, null);
        assertEventsEqual(sapFraudEvents);

    }

    private void assertEventsEqual(List<SAPFraudEvent> events, int... eventNum) {
        assertEquals(eventNum.length, events.size());
        Collections.sort(events, new Comparator<SAPFraudEvent>() {
            public int compare(SAPFraudEvent o1, SAPFraudEvent o2) {
                return o1.getDetails().compareTo(o2.getDetails());
            }
        });
        for (int i = 0; i < eventNum.length; i++) {
            assertEventEquals(eventNum[i], events.get(i));
        }
    }

    private static SAPFraudEvent[] fraudEvents = new SAPFraudEvent[]{
            new SAPFraudEvent("Payroll", "Intuit", "1234567", "QBOE", "123456789", null, 180, new Date(), "Company ran 1 payrolls of amount greater than 10000.00 with in 3 days of test verification debit date  of 10/30/2009", true, "BatchTest05", new Date(), new Date(), "Pending"), //payroll processed too soon
            new SAPFraudEvent("Payroll", "Dawn Company 2", "2222222", "QBOE", "999999999", "SecondCompEELast, SecondCompEEFirst TMI", 1600, new Date(), "EE SecondCompEEFirst:SecondCompEELast paid 600.00, an even dollar amount, for payroll with check date 10/09/2007", true, "BatchTest002", new Date(), new Date(), "Pending"),  //Employee Paid Even Dollar Amount
            new SAPFraudEvent("Payroll", "Dawn Company 2", "2222222", "QBOE", "999999999", "SecondCompEELastTwo, SecondCompEEFirstTwo TMI2", 1600, new Date(), "EE SecondCompEEFirstTwo:SecondCompEELastTwo paid 1000.00, an even dollar amount, for payroll with check date 10/09/2007", true, "BatchTest002", new Date(), new Date(), "Pending"), //Employee Paid Even Dollar Amount
            new SAPFraudEvent("Payroll", "QB Desktop 3", "8574536", "QBDT", "242335465", "ThirdCompEELastTwo, ThirdCompEEFirstTwo TMI2", 8601.11, new Date(), "EE ThirdCompEEFirstTwo:ThirdCompEELastTwo paid 8388.88 in a single paycheck for payroll with check date 10/02/2007", true, "BatchTest87", new Date(), new Date(), "Pending"), //employee paid greater than max
            new SAPFraudEvent("SignUp", "Intuit", "48484848488", "QBOE", "847656466", null, -1.0, new Date(), "This company was not activated because the company bank account matches the company bank account of company Intuit (Source System=QBOE Source ID=1234567) with status of On Hold (Fraud).", false, null, null, null, null), //Company matches fraudulent company
            new SAPFraudEvent("SignUp", "QB Desktop 2", "8774536", "QBDT", "243335465", null, -1.0, new Date(), "This company was not activated because the company bank account matches the company bank account of company Intuit (Source System=QBOE Source ID=1234567) with status of On Hold (Fraud).This company wa...", false, null, null, null, null) //company matches fradulent company
    };

    private static ArrayList<String> getEventCodes(int... indices) {
        ArrayList<String> codes = new ArrayList<String>();
        for (int index : indices) {
            EventTypeCode foundCode = null;
            switch (index) {
                case 0:
                    foundCode = EventTypeCode.PayrollProcessedTooSoon;
                    break;
                case 1:
                case 2:
                    foundCode = EventTypeCode.EmployeePaidEvenDollarAmount;
                    break;
                case 3:
                    foundCode = EventTypeCode.EmployeePaidGreaterThanMax;
                    break;
                case 4:
                case 5:
                    foundCode = EventTypeCode.CompanyMatchesFraudulentCompany;
                    break;
                default:
                    foundCode = EventTypeCode.EmployeePaidTooManyTimes;
                    break;
            }
            if (!codes.contains(foundCode.toString())) {
                codes.add(foundCode.toString());
            }
        }
        return codes;
    }

    private void assertEventEquals(int eventNum, SAPFraudEvent event) {
        assertEquals("Comparing event index " + eventNum, CompanyAdapterTests.fraudEvents[eventNum], event);
    }


    @Test
    public void testSaveCompanyStatus() throws Throwable {
        ACHReturnsDataLoader.loadAndOffloadQBOE();

        PayrollServices.beginUnitOfWork();
        PspPrincipal principal = AuthUser.findUser("AL_admin").createPrincipal();
        PayrollServices.setCurrentPrincipal(principal);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ArrayList<SAPServiceSubStatus> sapServiceSubStatusList = new ArrayList<SAPServiceSubStatus>();
        ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, ServiceSubStatusCode.Fraud);
        SAPServiceSubStatus sapServiceSubStatus = CompanyTranslator.getSAPServiceSubStatusFromDomainEntity(serviceSubStatus);
        sapServiceSubStatusList.add(sapServiceSubStatus);
        PayrollServices.rollbackUnitOfWork();

        mCompanyAdapter.saveCompanyService("QBOE", "1234567", ServiceCode.DirectDeposit.toString(), sapServiceSubStatusList, null, null);

    }

    @Test
    public void testEntitlementEINSearch() throws Throwable {
        String licenseNumber = "lic1";
        String eoc = "eoc1";
        String licenseNumber2 = "lic2";
        String eoc2 = "eoc2";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoc), company.getFedTaxId()));

        // update entitlement unit status
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);

        PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(
                company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        PayrollServices.commitUnitOfWork();

        Company company2 = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company2, licenseNumber, eoc);

        Company company3 = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company3, licenseNumber2, eoc2);

        ArrayList<SAPEntitlementSearchResult> searchResults = new CompanyAdapter().findCurrentEINs(licenseNumber, eoc);
        SAPEntitlementSearchResult result1 = searchResults.get(0);
        assertEquals("000000001", result1.getFein());
        assertEquals("Activated", result1.getEntitlementUnitStatus());
        SAPEntitlementSearchResult result2 = searchResults.get(1);
        assertEquals("000000002", result2.getFein());
        assertEquals("PendingActivation", result2.getEntitlementUnitStatus());
    }

    @Test
    public void testFindLicenseByOrderNumber() throws Throwable {
        String licenseNumber = "lic1";
        String eoc = "eoc1";
        String licenseNumber2 = "lic2";
        String eoc2 = "eoc2";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoc), company.getFedTaxId()));
        entitlementUnitDTO.setCustomerId("customerId");

        // update entitlement unit status
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);

        PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(
                company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        PayrollServices.commitUnitOfWork();

        Company company2 = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        EntitlementUnit eu1 = DataLoadServices.addEntitlementUnit(company2, licenseNumber, eoc);
        PayrollServices.beginUnitOfWork();
        eu1 = Application.findById(EntitlementUnit.class, eu1.getId());
        Entitlement ent1 = eu1.getEntitlement();
        ent1.setOrderNumber("ord1");
        Application.save(ent1);
        PayrollServices.commitUnitOfWork();


        Company company3 = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        EntitlementUnit eu = DataLoadServices.addEntitlementUnit(company3, licenseNumber2, eoc2);
        PayrollServices.beginUnitOfWork();
        eu = Application.findById(EntitlementUnit.class, eu.getId());
        Entitlement ent = eu.getEntitlement();
        ent.setOrderNumber("ord2");
        Application.save(ent);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        EntitlementMessage em = new EntitlementMessage();
        em.setEntitlementOfferingCode("eoc3");
        em.setLicenseNumber("lic3");
        em.setOrderNumber("ord3");

        em.setStatus(EntitlementMessageStatusCode.New);
        Application.save(em);
        em.setMessage(FileUtils.readFileToString(new File(Application.findFileOnClassPath("resources/EntitlementCreation.xml"))));
        PayrollServices.commitUnitOfWork();

        Company company4 = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company4, "lic5", "eoc5");

        PayrollServices.beginUnitOfWork();
        company4 = Company.findCompany(company4.getSourceCompanyId(), company4.getSourceSystemCd());
        EntitlementUnitDTO entitlementUnitDTO4 = PayrollServices.dtoFactory.create(company4.getEntitlementUnit(Entitlement.findEntitlement("lic5", "eoc5"), company4.getFedTaxId()));

        // update entitlement unit status
        entitlementUnitDTO4.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);

        PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(
                company4.getSourceSystemCd(), company4.getSourceCompanyId(), entitlementUnitDTO4);
        PayrollServices.commitUnitOfWork();


        Company company5 = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        PayrollServices.beginUnitOfWork();
        company5 = Company.findCompany(company5.getSourceCompanyId(), company5.getSourceSystemCd());
        company5.setFedTaxId(company4.getFedTaxId());
        Application.save(company5);
        PayrollServices.commitUnitOfWork();
        DataLoadServices.addEntitlementUnit(company5, "lic5", "eoc5");

        SAPEntitlementInfo lic1 = new CompanyAdapter().getLicenseFromOrderNumber("ord1");
        assertEquals("lic1", lic1.getLicenseNumber());
        assertEquals("eoc1", lic1.getEoc());

        SAPEntitlementInfo lic2 = new CompanyAdapter().getLicenseFromOrderNumber("ord2");
        assertEquals("lic2", lic2.getLicenseNumber());
        assertEquals("eoc2", lic2.getEoc());

        SAPEntitlementInfo lic3 = new CompanyAdapter().getLicenseFromOrderNumber("ord3");
        assertEquals("lic3", lic3.getLicenseNumber());
        assertEquals("eoc3", lic3.getEoc());

        try {
            new CompanyAdapter().getLicenseFromOrderNumber("cheese");
            fail("Expected exception");
        } catch (SAPException e) {

        }
    }

    @Test
    public void testAddDIYCompany() throws Throwable {
        testAddCompany(DataLoadServices.AssetItemNumber.DIY_YEARLY.toString());
    }

    @Test
    public void testAddDiskDeliveryCompany() throws Throwable {
        testAddCompany(DataLoadServices.AssetItemNumber.DIY_DISK_DELIVERY.toString());
    }

    @Test
    public void testAddAssistedCompany() throws Throwable {
        testAddCompany(DataLoadServices.AssetItemNumber.ASSISTED.toString());
        Company c = Application.<Company>find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).get(0);
        DataLoadServices.addTaxService(c);

    }

    @Test
    public void testAddMultipleCompaniesWithSameEntitlement() throws Exception {

        String[] statesList = new String[]{"PA"};
        DataLoadServices.setupCompany(1L, 10, statesList, PaymentTemplateCategory.Withholding);

        String licenseNumber = "12345678901234567890";
        String entitlementOfferingCode = "09876543210987654321";

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT));
        PayrollServices.commitUnitOfWork();

        for (Company company1 : companies) {
            DataLoadServices.addEntitlementUnit(company1, licenseNumber, entitlementOfferingCode);
        }

    }

    @Test
    @Ignore ("For UI Pagination tests")
    public void testSetupMultipleCompanies() throws Exception {

        String[] statesList = new String[]{"PA"};
        List<Company> companiesList = DataLoadServices.setupCompany(1L, 60, statesList, PaymentTemplateCategory.Withholding);

//        for (Company company : companiesList) {
//            DataLoadServices.addCompanyOnHoldReason(company, ServiceSubStatusCode.RiskAssessment);
//            DataLoadServices.addCompanyOnHoldReason(company, ServiceSubStatusCode.RiskCollections);
//        }

        PayrollServices.beginUnitOfWork();
        for (Company company : companiesList) {
            Application.refresh(company);
            CompanyService companyService = company.getCompanyService(ServiceCode.Tax);
            companyService.setStatusCd(ServiceSubStatusCode.PendingBalanceFile);
            Application.save(companyService);
            companyService = company.getCompanyService(ServiceCode.DirectDeposit);
            companyService.setStatusCd(ServiceSubStatusCode.PendingBankVerification);
            Application.save(companyService);
        }
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testAvailablePriceTypes() throws Throwable {

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        Application.beginUnitOfWork();
        company = Application.refresh(company);
        EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();
        String itemNumber = entitlementUnit.getEntitlement().getEntitlementCode().getAssetItemNumber();
        Application.rollbackUnitOfWork();

        DataLoadServices.deactivateEntitlementUnit(entitlementUnit);

        ArrayList<String> priceTypes = new CompanyAdapter().getAvailablePriceTypes(company.getSourceSystemCd().toString(), company.getSourceCompanyId());
        assertEquals(1, priceTypes.size());

        priceTypes = new CompanyAdapter().getAvailablePriceTypes(itemNumber);
        assertEquals(1, priceTypes.size());

        DataLoadServices.addAssistedEntitlementUnit(company, "123", "789", true);

        Application.beginUnitOfWork();
        company = Application.refresh(company);
        entitlementUnit = company.getActivePrimaryEntitlementUnit();
        itemNumber = entitlementUnit.getEntitlement().getEntitlementCode().getAssetItemNumber();
        Application.rollbackUnitOfWork();

        priceTypes = new CompanyAdapter().getAvailablePriceTypes(company.getSourceSystemCd().toString(), company.getSourceCompanyId());
        assertEquals(5, priceTypes.size());

        priceTypes = new CompanyAdapter().getAvailablePriceTypes(itemNumber);
        assertEquals(5, priceTypes.size());
    }

    @Test
    public void testAMOWSEntitlementSync() throws Throwable {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 25, SpcfTimeZone.getLocalTimeZone()));

        ERSGatewayFactory.setInstanceClass(ERSMockGateway.class);
        AMOWSGatewayFactory.setInstanceClass(AMOWSMockGateway.class);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();
        Entitlement entitlement = entitlementUnit.getEntitlement();

        entitlement.setNextChargeDate(PSPDate.getPSPTime());
        Application.save(entitlement);

        GetCustomerAssetResponseTypeDTO getCustomerAssetResponseTypeDTO = new GetCustomerAssetResponseTypeDTO();
        getCustomerAssetResponseTypeDTO.setContactName("Updated Name");
        getCustomerAssetResponseTypeDTO.setPaymentMethodType(EntitlementPaymentMethodType.CC);
        getCustomerAssetResponseTypeDTO.setSubscriptionEndDate(PSPDate.getPSPTime());
        getCustomerAssetResponseTypeDTO.setNextChargeDate(SpcfCalendar.createInstance(2013, 1, 25, SpcfTimeZone.getLocalTimeZone()));
        getCustomerAssetResponseTypeDTO.setBillingDayOfMonth(28);
        getCustomerAssetResponseTypeDTO.setBillingProfileId("123456789");
        getCustomerAssetResponseTypeDTO.setBillingZipCode("89511");
        getCustomerAssetResponseTypeDTO.setContactEmail("updated@email.com");
        getCustomerAssetResponseTypeDTO.setCreditCardExpiration("01/12");
        getCustomerAssetResponseTypeDTO.setCreditCardNumber("1234");
        getCustomerAssetResponseTypeDTO.setCreditCardType("Visa");
        getCustomerAssetResponseTypeDTO.setEntitlementOfferingCode(entitlement.getEntitlementOfferingCode());
        getCustomerAssetResponseTypeDTO.setCustomerId(entitlement.getCustomerId());
        getCustomerAssetResponseTypeDTO.setEntitlementState(EntitlementStateCode.Enabled);
        getCustomerAssetResponseTypeDTO.setLicenseNumber(entitlement.getLicenseNumber());
        AMOWSMockGateway.setGetCustomerAssetResponseTypeDTO(getCustomerAssetResponseTypeDTO);

        PayrollServices.commitUnitOfWork();

        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        entitlement = entitlementUnit.getEntitlement();

        assertEquals(getCustomerAssetResponseTypeDTO.getContactName(), entitlement.getContactName());
        //assertEquals(getCustomerAssetResponseTypeDTO.getPaymentMethodType(), entitlement.getPaymentMethodType());
        assertNull(entitlement.getSubscriptionEndDate());
        assertEquals(getCustomerAssetResponseTypeDTO.getNextChargeDate(), entitlement.getNextChargeDate().toLocal());
        assertEquals(getCustomerAssetResponseTypeDTO.getBillingDayOfMonth(), Integer.valueOf(entitlement.getBillingDayOfMonth()));
        assertEquals(getCustomerAssetResponseTypeDTO.getBillingProfileId(), entitlement.getBillingProfileId());
        assertEquals(getCustomerAssetResponseTypeDTO.getBillingZipCode(), entitlement.getBillingZipCode());
        assertEquals(getCustomerAssetResponseTypeDTO.getContactEmail(), entitlement.getContactEmail());
        //assertEquals(getCustomerAssetResponseTypeDTO.getCreditCardExpiration(), entitlement.getCreditCardExpiration());
        //assertEquals(getCustomerAssetResponseTypeDTO.getCreditCardNumber(), entitlement.getCreditCardNumber());
        //assertEquals(getCustomerAssetResponseTypeDTO.getCreditCardType(), entitlement.getCreditCardType());
        assertEquals(getCustomerAssetResponseTypeDTO.getEntitlementOfferingCode(), entitlement.getEntitlementOfferingCode());
        assertEquals(getCustomerAssetResponseTypeDTO.getCustomerId(), entitlement.getCustomerId());
        assertEquals(getCustomerAssetResponseTypeDTO.getEntitlementState(), entitlement.getEntitlementState());
        assertEquals(getCustomerAssetResponseTypeDTO.getLicenseNumber(), entitlement.getLicenseNumber());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testERSEntitlementSync() throws Throwable {
        ERSGatewayFactory.setInstanceClass(ERSMockGateway.class);
        AMOWSGatewayFactory.setInstanceClass(AMOWSMockGateway.class);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();

        //Test with default options / the entitlement code should NOT be updated
        String syncOptions = SystemParameter.findStringValue(SystemParameter.Code.ERS_DATA_SYNC_OPTIONS, null);
        if (syncOptions == null) {
            SystemParameter systemParameter = new SystemParameter();
            systemParameter.setSystemParameterCd(SystemParameter.Code.ERS_DATA_SYNC_OPTIONS.toString());
            systemParameter.setSystemParameterDescription("");
            systemParameter.setSystemParameterOrg("PSP");
            systemParameter.setSystemParameterValue("EntitlementState,EntitlementUnitStatus");
            Application.save(systemParameter);
        } else {
            SystemParameter.update(SystemParameter.Code.ERS_DATA_SYNC_OPTIONS, "EntitlementState,EntitlementUnitStatus");
        }

        EntitlementInfoDTO entitlementInfoDTO = copyEntitlementUnitToEntitlementInfoDTO(entitlementUnit);
        entitlementInfoDTO.setCustomerId("123456789");
        entitlementInfoDTO.setAssetItemNumber("1099574");
        entitlementInfoDTO.setEditionType(EditionType.Enhanced);
        entitlementInfoDTO.setNumberOfEmployeesType(NumberOfEmployeesType.UNLIMITED);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);

        GetCustomerAssetResponseTypeDTO getCustomerAssetResponseTypeDTO = copyEntitlementUnitToGetCustomerAssetResponseTypeDTO(entitlementUnit);
        AMOWSMockGateway.setGetCustomerAssetResponseTypeDTO(getCustomerAssetResponseTypeDTO);

        PayrollServices.commitUnitOfWork();

        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        EntitlementUnit newEntitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());

        assertEquals(entitlementUnit.getEntitlement().getEntitlementCode().getAssetItemNumber(), newEntitlementUnit.getEntitlement().getEntitlementCode().getAssetItemNumber());
        assertEquals(entitlementUnit.getEntitlement().getCustomerId(), newEntitlementUnit.getEntitlement().getCustomerId());
        assertEquals(entitlementUnit.getEntitlement().getEntitlementCode().getEditionType(), newEntitlementUnit.getEntitlement().getEntitlementCode().getEditionType());
        assertEquals(entitlementUnit.getEntitlement().getEntitlementState(), newEntitlementUnit.getEntitlement().getEntitlementState());
        assertEquals(entitlementUnit.getEntitlement().getEntitlementCode().getNumberOfEmployeesType(), newEntitlementUnit.getEntitlement().getEntitlementCode().getNumberOfEmployeesType());
        assertEquals(entitlementUnit.getEntitlementUnitStatus(), newEntitlementUnit.getEntitlementUnitStatus());
        PayrollServices.rollbackUnitOfWork();

        //Test with all options on / the entitlement code SHOULD be updated
        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.ERS_DATA_SYNC_OPTIONS, "EntitlementState,EntitlementUnitStatus,CustomerId,NumberOfEmployeesType,EditionType,AssetItemNumber");
        PayrollServices.commitUnitOfWork();

        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.PendingActivation);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testERSEntitlementSyncEnabledEntitlement() throws Throwable {
        ERSGatewayFactory.setInstanceClass(ERSMockGateway.class);
        AMOWSGatewayFactory.setInstanceClass(AMOWSMockGateway.class);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();

        String syncOptions = SystemParameter.findStringValue(SystemParameter.Code.ERS_DATA_SYNC_OPTIONS, null);
        if (syncOptions == null) {
            SystemParameter systemParameter = new SystemParameter();
            systemParameter.setSystemParameterCd(SystemParameter.Code.ERS_DATA_SYNC_OPTIONS.toString());
            systemParameter.setSystemParameterDescription("");
            systemParameter.setSystemParameterOrg("PSP");
            systemParameter.setSystemParameterValue("EntitlementState,EntitlementUnitStatus");
            Application.save(systemParameter);
        } else {
            SystemParameter.update(SystemParameter.Code.ERS_DATA_SYNC_OPTIONS, "EntitlementState,EntitlementUnitStatus");
        }

        EntitlementInfoDTO entitlementInfoDTO = copyEntitlementUnitToEntitlementInfoDTO(entitlementUnit);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);

        GetCustomerAssetResponseTypeDTO getCustomerAssetResponseTypeDTO = copyEntitlementUnitToGetCustomerAssetResponseTypeDTO(entitlementUnit);
        AMOWSMockGateway.setGetCustomerAssetResponseTypeDTO(getCustomerAssetResponseTypeDTO);

        PayrollServices.commitUnitOfWork();

        //Test with default options and no data change
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.PendingActivation);
        PayrollServices.rollbackUnitOfWork();

        //Test with default options and updated Entitlement Unit Status
        entitlementInfoDTO.getEntitlementUnits().get(company.getFedTaxId()).setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Activated);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.PendingActivation);
        PayrollServices.rollbackUnitOfWork();


        //EU:ActivationHold / ERS:Activated = ActivationHold - Update skipped
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.ActivationHold);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.ActivationHold, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().get(company.getFedTaxId()).setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Activated);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.ActivationHold);
        PayrollServices.rollbackUnitOfWork();


        //EU:PendingReactivation / ERS:Activated = PendingReactivation - Update skipped
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingReactivation);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.PendingReactivation, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().get(company.getFedTaxId()).setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Activated);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.PendingReactivation);
        PayrollServices.rollbackUnitOfWork();


        //EU:PendingDeactivation / ERS:Activated = PendingDeactivation - Update skipped
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingDeactivation);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.PendingDeactivation, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().get(company.getFedTaxId()).setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Activated);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.PendingDeactivation);
        PayrollServices.rollbackUnitOfWork();


        //EU:Activated / ERS:Deactivated = PendingReactivation
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.Activated, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().get(company.getFedTaxId()).setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Deactivated);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.PendingReactivation);
        PayrollServices.rollbackUnitOfWork();


        //EU:ErrorActivating / ERS:Deactivated = PendingReactivation
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorActivating);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.ErrorActivating, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().get(company.getFedTaxId()).setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Deactivated);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.PendingReactivation);
        PayrollServices.rollbackUnitOfWork();


        //EU:ErrorActivating / ERS:Activated = Activated
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorActivating);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.ErrorActivating, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().get(company.getFedTaxId()).setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Activated);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.Activated);
        PayrollServices.rollbackUnitOfWork();


        //EU:ErrorDeactivating / ERS:Activated = PendingDeactivation
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorDeactivating);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.ErrorDeactivating, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().get(company.getFedTaxId()).setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Activated);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.PendingDeactivation);
        PayrollServices.rollbackUnitOfWork();


        //EU:DeactivationHold / ERS:Deactivated = Deactivated
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.DeactivationHold);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.DeactivationHold, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().get(company.getFedTaxId()).setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Deactivated);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.Deactivated);
        PayrollServices.rollbackUnitOfWork();


        //EU:DeactivationHold / ERS:Deactivated = Deactivated
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorDeactivating);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.ErrorDeactivating, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().get(company.getFedTaxId()).setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Deactivated);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.Deactivated);
        PayrollServices.rollbackUnitOfWork();


        //EU:DeactivationHold / ERS:Deactivated = Deactivated
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.DeactivationHold);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.DeactivationHold, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().get(company.getFedTaxId()).setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Activated);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.PendingDeactivation);
        PayrollServices.rollbackUnitOfWork();


        //EU:DeactivationHold / ERS:Deactivated = Deactivated
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorDeactivating);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.ErrorDeactivating, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().get(company.getFedTaxId()).setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Activated);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.PendingDeactivation);
        PayrollServices.rollbackUnitOfWork();


        //EU:DeactivationHold / ERS:Null = Deactivated
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.DeactivationHold);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.DeactivationHold, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().remove(company.getFedTaxId());
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.Deactivated);
        PayrollServices.rollbackUnitOfWork();


        //EU:ErrorDeactivating / ERS:Null = Deactivated
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorDeactivating);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.ErrorDeactivating, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().remove(company.getFedTaxId());
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.Deactivated);
        PayrollServices.rollbackUnitOfWork();


        //EU:Activated / ERS:Null = PendingActivation
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.Activated, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().remove(company.getFedTaxId());
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.PendingActivation);
        PayrollServices.rollbackUnitOfWork();


        //EU:ActivationHold / ERS:Null = ActivationHold - Update skipped
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.ActivationHold);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.ActivationHold, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.ActivationHold);
        PayrollServices.rollbackUnitOfWork();


        //EU:ErrorActivating / ERS:Null = PendingActivation
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorActivating);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.ErrorActivating, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.PendingActivation);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testERSEntitlementSyncDisabledEntitlement() throws Throwable {
        ERSGatewayFactory.setInstanceClass(ERSMockGateway.class);
        AMOWSGatewayFactory.setInstanceClass(AMOWSMockGateway.class);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();

        String syncOptions = SystemParameter.findStringValue(SystemParameter.Code.ERS_DATA_SYNC_OPTIONS, null);
        if (syncOptions == null) {
            SystemParameter systemParameter = new SystemParameter();
            systemParameter.setSystemParameterCd(SystemParameter.Code.ERS_DATA_SYNC_OPTIONS.toString());
            systemParameter.setSystemParameterDescription("");
            systemParameter.setSystemParameterOrg("PSP");
            systemParameter.setSystemParameterValue("EntitlementState,EntitlementUnitStatus");
            Application.save(systemParameter);
        } else {
            SystemParameter.update(SystemParameter.Code.ERS_DATA_SYNC_OPTIONS, "EntitlementState,EntitlementUnitStatus");
        }

        GetCustomerAssetResponseTypeDTO getCustomerAssetResponseTypeDTO = copyEntitlementUnitToGetCustomerAssetResponseTypeDTO(entitlementUnit);
        AMOWSMockGateway.setGetCustomerAssetResponseTypeDTO(getCustomerAssetResponseTypeDTO);

        EntitlementInfoDTO entitlementInfoDTO = copyEntitlementUnitToEntitlementInfoDTO(entitlementUnit);
        entitlementInfoDTO.setEntitlementState(EntitlementStateCode.Disabled);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);

        entitlementUnit.getEntitlement().setEntitlementState(EntitlementStateCode.Disabled);
        PayrollServices.commitUnitOfWork();


        //EU:Activated / ERS:Deactivated = ErrorActivating
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.Activated, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().get(company.getFedTaxId()).setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Deactivated);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.ErrorActivating);
        PayrollServices.rollbackUnitOfWork();


        //EU:Deactivated / ERS:Activated = DeactivationHold
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.Deactivated, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().get(company.getFedTaxId()).setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Activated);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.DeactivationHold);
        PayrollServices.rollbackUnitOfWork();


        //EU:ErrorActivating / ERS:Activated = Activated
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorActivating);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.ErrorActivating, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().get(company.getFedTaxId()).setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Activated);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.Activated);
        PayrollServices.rollbackUnitOfWork();


        //EU:DeactivationHold / ERS:Deactivated = Deactivated
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.DeactivationHold);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.DeactivationHold, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().get(company.getFedTaxId()).setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Deactivated);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.Deactivated);
        PayrollServices.rollbackUnitOfWork();


        //EU:DeactivationHold / ERS:Activated = DeactivationHold
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.DeactivationHold);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.DeactivationHold, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().get(company.getFedTaxId()).setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Activated);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.DeactivationHold);
        PayrollServices.rollbackUnitOfWork();


        //EU:ErrorDeactivating / ERS:Deactivated = Deactivated
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorDeactivating);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.ErrorDeactivating, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().get(company.getFedTaxId()).setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Deactivated);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.Deactivated);
        PayrollServices.rollbackUnitOfWork();


        //EU:ErrorDeactivating / ERS:Activated = DeactivationHold
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorDeactivating);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.ErrorDeactivating, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().get(company.getFedTaxId()).setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Activated);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.DeactivationHold);
        PayrollServices.rollbackUnitOfWork();


        //EU:DeactivationHold / ERS:Null = Deactivated
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.DeactivationHold);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.DeactivationHold, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().remove(company.getFedTaxId());
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.Deactivated);
        PayrollServices.rollbackUnitOfWork();


        //EU:ErrorActivating / ERS:Null = ErrorActivating
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorActivating);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.ErrorActivating, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().remove(company.getFedTaxId());
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.ErrorActivating);
        PayrollServices.rollbackUnitOfWork();


        //EU:ErrorDeactivating / ERS:Null = Deactivated
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorDeactivating);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.ErrorDeactivating, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().remove(company.getFedTaxId());
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.Deactivated);
        PayrollServices.rollbackUnitOfWork();


        //EU:Activated / ERS:Null = ErrorActivating
        PayrollServices.beginUnitOfWork();
        Application.findById(EntitlementUnit.class, entitlementUnit.getId()).setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorActivating);
        PayrollServices.commitUnitOfWork();
        assertEquals(EntitlementUnitStatusCode.ErrorActivating, Application.findById(EntitlementUnit.class, entitlementUnit.getId()).getEntitlementUnitStatus());

        entitlementInfoDTO.getEntitlementUnits().remove(company.getFedTaxId());
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        new CompanyAdapter().syncEntitlementUnitFromSourceSystems(entitlementUnit.getId().toString());

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        assertERSSync(entitlementInfoDTO, entitlementUnit, EntitlementUnitStatusCode.ErrorActivating);
        PayrollServices.rollbackUnitOfWork();
    }

    private EntitlementInfoDTO copyEntitlementUnitToEntitlementInfoDTO(EntitlementUnit pEntitlementUnit) {
        Entitlement entitlement = pEntitlementUnit.getEntitlement();
        EntitlementCode entitlementCode = entitlement.getEntitlementCode();

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        entitlementInfoDTO.setAssetItemNumber(entitlementCode.getAssetItemNumber());
        entitlementInfoDTO.setCustomerId(entitlement.getCustomerId());
        entitlementInfoDTO.setEditionType(entitlementCode.getEditionType());
        entitlementInfoDTO.setEntitlementState(entitlement.getEntitlementState());
        entitlementInfoDTO.setNumberOfEmployeesType(entitlementCode.getNumberOfEmployeesType());

        for (EntitlementUnit entitlementUnit : entitlement.getEntitlementUnitCollection()) {
            EntitlementUnitInfoDTO entitlementUnitInfoDTO = new EntitlementUnitInfoDTO();
            entitlementUnitInfoDTO.setEntitlementUnitStatusCode(entitlementUnit.getEntitlementUnitStatus());
            entitlementUnitInfoDTO.setFedTaxId(entitlementUnit.getFedTaxId());

            entitlementInfoDTO.getEntitlementUnits().put(entitlementUnit.getFedTaxId(), entitlementUnitInfoDTO);
        }

        return entitlementInfoDTO;
    }

    private GetCustomerAssetResponseTypeDTO copyEntitlementUnitToGetCustomerAssetResponseTypeDTO(EntitlementUnit pEntitlementUnit) {
        GetCustomerAssetResponseTypeDTO getCustomerAssetResponseTypeDTO = new GetCustomerAssetResponseTypeDTO();

        Entitlement entitlement = pEntitlementUnit.getEntitlement();

        getCustomerAssetResponseTypeDTO.setBillingDayOfMonth(entitlement.getBillingDayOfMonth());
        getCustomerAssetResponseTypeDTO.setBillingProfileId(entitlement.getBillingProfileId());
        getCustomerAssetResponseTypeDTO.setBillingZipCode(entitlement.getBillingZipCode());
        getCustomerAssetResponseTypeDTO.setContactEmail(entitlement.getContactEmail());
        getCustomerAssetResponseTypeDTO.setContactName(entitlement.getContactName());
        getCustomerAssetResponseTypeDTO.setCreditCardExpiration(entitlement.getCreditCardExpiration());
        getCustomerAssetResponseTypeDTO.setCreditCardNumber(entitlement.getCreditCardNumber());
        getCustomerAssetResponseTypeDTO.setCreditCardType(entitlement.getCreditCardType());
        getCustomerAssetResponseTypeDTO.setCustomerId(entitlement.getCustomerId());
        getCustomerAssetResponseTypeDTO.setEntitlementOfferingCode(entitlement.getEntitlementOfferingCode());
        getCustomerAssetResponseTypeDTO.setEntitlementState(entitlement.getEntitlementState());
        getCustomerAssetResponseTypeDTO.setLicenseNumber(entitlement.getLicenseNumber());
        getCustomerAssetResponseTypeDTO.setNextChargeDate(entitlement.getNextChargeDate());
        getCustomerAssetResponseTypeDTO.setPaymentMethodType(entitlement.getPaymentMethodType());
        getCustomerAssetResponseTypeDTO.setSubscriptionEndDate(entitlement.getSubscriptionEndDate());

        return getCustomerAssetResponseTypeDTO;
    }


    private void assertERSSync(EntitlementInfoDTO pEntitlementInfoDTO, EntitlementUnit pEntitlementUnit, EntitlementUnitStatusCode pEntitlementUnitStatusCode) {
        assertEquals(pEntitlementInfoDTO.getAssetItemNumber(), pEntitlementUnit.getEntitlement().getEntitlementCode().getAssetItemNumber());
        assertEquals(pEntitlementInfoDTO.getCustomerId(), pEntitlementUnit.getEntitlement().getCustomerId());
        assertEquals(pEntitlementInfoDTO.getEditionType(), pEntitlementUnit.getEntitlement().getEntitlementCode().getEditionType());
        assertEquals(pEntitlementInfoDTO.getEntitlementState(), pEntitlementUnit.getEntitlement().getEntitlementState());
        assertEquals(pEntitlementInfoDTO.getNumberOfEmployeesType(), pEntitlementUnit.getEntitlement().getEntitlementCode().getNumberOfEmployeesType());
        assertEquals(pEntitlementUnitStatusCode, pEntitlementUnit.getEntitlementUnitStatus());
    }

    @Test
    public void testInvalidAssetItemNumber() throws Throwable {
        CompanyAdapter companyAdapter = new CompanyAdapter();

        SAPAssetInfo sapAssetInfo = companyAdapter.getAssetInfo(DataLoadServices.AssetItemNumber.DIY_MONTHLY.toString());
        assertNotNull("Asset not found", sapAssetInfo);

        try {
            companyAdapter.getAssetInfo("1099601");
            fail("Method should throw an exception.");
        } catch (Throwable pThrowable) {
            assertEquals("Item number '1099601' does not exist. Please make sure you have the correct row selected in Siebel before you click the launch product ui button.", pThrowable.getMessage());
        }

        try {
            companyAdapter.getAssetInfo("blah");
            fail("Method should throw an exception.");
        } catch (Throwable pThrowable) {
            assertEquals("Error finding asset info\nDetails: Item code blah does not exist", pThrowable.getMessage());
        }
    }

    private void testAddCompany(String itemNumber) throws Throwable {
        PayrollServices.beginUnitOfWork();
        PspPrincipal principal = AuthUser.findUser("AL_admin").createPrincipal();
        PayrollServices.setCurrentPrincipal(principal);
        PayrollServices.commitUnitOfWork();

        SAPAddCompany addCompany = new SAPAddCompany();
        addCompany.setLicenseNumber("lic10");
        addCompany.setEoc("eoc10");
        addCompany.setItemNumber(itemNumber);
        addCompany.setServiceAccountId("43252");

        SAPCompanyLegalInfo legalInfo = new SAPCompanyLegalInfo();
        legalInfo.setEin("123456789");
        legalInfo.setLegalName("Dave's Widget Company");
        SAPAddress legalAddress = new SAPAddress();
        legalAddress.setAddressLine1("10 Main St");
        legalAddress.setCity("Reno");
        legalAddress.setState("NV");
        legalAddress.setZipCode("89511");
        legalInfo.setAddress(legalAddress);
        addCompany.setLegalInfo(legalInfo);

        addCompany.setContacts(new ArrayList<SAPContact>());
        SAPContact pp = new SAPContact();
        pp.setContactRoleCd(ContactRole.PrimaryPrincipal);
        pp.setAddress(new SAPAddress());
        pp.setFirstName("David");
        pp.setLastName("Weinberg");
        pp.setPhoneNumber("(123) 555-5555");
        pp.setEmail("david@aol.com");
        addCompany.getContacts().add(pp);

        SAPContact pa = new SAPContact();
        pa.setContactRoleCd(ContactRole.PayrollAdmin);
        pa.setAddress(new SAPAddress());
        pa.setFirstName("David");
        pa.setLastName("Weinberg");
        pa.setPhoneNumber("(123) 555-5555");
        pa.setEmail("david@aol.com");
        addCompany.getContacts().add(pa);

        SAPContact other = new SAPContact();
        other.setContactRoleCd(ContactRole.Other);
        other.setAddress(new SAPAddress());
        other.setFirstName("Joe");
        other.setLastName("Plumber");
        other.setPhoneNumber("(123) 555-5555");
        other.setEmail("david@aol.com");
        addCompany.getContacts().add(other);

        new CompanyAdapter().addCompany(addCompany);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Company.findCompanies(SourceSystemCode.QBDT, "123456789");
        assertEquals(1, companies.size());
        Company c = companies.get(0);
        assertEquals("Dave's Widget Company", c.getLegalName());

        assertEquals(3, c.getContactCollection().size());


        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testFindEntitlementInfoForNoNumberOfEmployees() throws Throwable {
        PayrollServices.beginUnitOfWork();
        EntitlementMessage em = new EntitlementMessage();
        em.setEntitlementOfferingCode("eoc3");
        em.setLicenseNumber("lic3");
        em.setOrderNumber("ord3");
        em.setStatus(EntitlementMessageStatusCode.New);
        Application.save(em);
        em.setMessage(FileUtils.readFileToString(new File(Application.findFileOnClassPath("resources/EntitlementCreationNoNumberEmployees.xml"))));
        PayrollServices.commitUnitOfWork();

        SAPEntitlementInfo entitlementInfo = new CompanyAdapter().getEntitlementInfo("lic3", "eoc3");
        assertNull(entitlementInfo.getEntitlementCodeInfo().getNumberOfEmployees());
        assertEquals("Enhanced", entitlementInfo.getEntitlementCodeInfo().getEdition());

    }


    @Test
    public void testSwitchAllowTransmissionsForDD(){
        processSwitchAllowTransmissions(ServiceCode.DirectDeposit);
    }

    @Test
    public void testSwitchAllowTransmissionsForDIY(){
        processSwitchAllowTransmissions(ServiceCode.Cloud);
    }
    @Test
    public void testSwitchAllowTransmissionsForTax(){
        processSwitchAllowTransmissions(ServiceCode.Tax);
    }


    @Test
    public void testSwitchProcessTransmissionsForDD(){
        processSwitchProcessTransmissions(ServiceCode.DirectDeposit);
    }

    @Test
    public void testSwitchProcessTransmissionsForDIY(){
        processSwitchProcessTransmissions(ServiceCode.Cloud);
    }
    @Test
    public void testSwitchProcessTransmissionsForTax(){
        processSwitchProcessTransmissions(ServiceCode.Tax);
    }

    @Test(expected = Throwable.class)
    public void testErrorInSwitchAllowTransmissions() throws Throwable {
        Company company = DataLoadServices.newCompany(SourceSystemCode.IOP, psid);

        Application.beginUnitOfWork();
        Application.refresh(company);
        boolean isAllowTransmissions = company.getQuickbooksInfo().getAllowTransmissions();
        boolean isAllowTransAllowed = isAllowTransmissions ? false : true;
        Application.rollbackUnitOfWork();

        new CompanyAdapter().switchAllowTransmissions(SourceSystemCode.IOP.toString(), psid, isAllowTransAllowed);

    }

    @Test(expected = Throwable.class)
    public void testErrorInSwitchProcessTransmissions() throws Throwable {
        Company company = DataLoadServices.newCompany(SourceSystemCode.IOP, psid);

        Application.beginUnitOfWork();
        Application.refresh(company);
        boolean isProcessTransmissions = company.getQuickbooksInfo().getProcessTransmissions();
        boolean isProcessTransAllowed = isProcessTransmissions ? false : true;
        Application.rollbackUnitOfWork();

        new CompanyAdapter().switchProcessTransmissions(SourceSystemCode.IOP.toString(), psid, isProcessTransAllowed);

    }



    public void processSwitchProcessTransmissions (ServiceCode pServiceCode){
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);
        DataLoadServices.addServices(company, pServiceCode);

        Application.beginUnitOfWork();
        Application.refresh(company);
        boolean isProcessTransmissions = company.getQuickbooksInfo().getProcessTransmissions();
        boolean isProcessAllowed = isProcessTransmissions ? false : true;
        Application.rollbackUnitOfWork();

        try {
            new CompanyAdapter().switchProcessTransmissions(SourceSystemCode.QBDT.toString(), psid, isProcessAllowed);
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
        }


        Application.beginUnitOfWork();
        Application.refresh(company);
        isProcessTransmissions = company.getQuickbooksInfo().getProcessTransmissions();
        Application.rollbackUnitOfWork();

        Assert.assertTrue("Process Transmissions update is failed for " + pServiceCode, isProcessTransmissions == isProcessAllowed );

        isProcessAllowed = isProcessTransmissions ? false : true;



        try {
            new CompanyAdapter().switchProcessTransmissions(SourceSystemCode.QBDT.toString(), psid, isProcessAllowed );
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
        }

        Application.beginUnitOfWork();
        Application.refresh(company);
        isProcessTransmissions = company.getQuickbooksInfo().getProcessTransmissions();
        Application.rollbackUnitOfWork();
        Assert.assertTrue("Process Transmissions update is failed for " + pServiceCode, isProcessTransmissions == isProcessAllowed);

    }



    public void processSwitchAllowTransmissions (ServiceCode pServiceCode){
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);
        DataLoadServices.addServices(company, pServiceCode);

        Application.beginUnitOfWork();
        Application.refresh(company);
        boolean isAllowTransmissions = company.getQuickbooksInfo().getAllowTransmissions();
        boolean isAllowTransAllowed = isAllowTransmissions ? false : true;
        Application.rollbackUnitOfWork();

        try {
            new CompanyAdapter().switchAllowTransmissions(SourceSystemCode.QBDT.toString(), psid, isAllowTransAllowed);
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
        }


        Application.beginUnitOfWork();
        Application.refresh(company);
        isAllowTransmissions = company.getQuickbooksInfo().getAllowTransmissions();
        Application.rollbackUnitOfWork();

        Assert.assertTrue("Allowed Transmissions update is failed for " + pServiceCode, isAllowTransmissions == isAllowTransAllowed );

        isAllowTransAllowed = isAllowTransmissions ? false : true;



        try {
            new CompanyAdapter().switchAllowTransmissions(SourceSystemCode.QBDT.toString(), psid, isAllowTransAllowed);
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
        }

        Application.beginUnitOfWork();
        Application.refresh(company);
        isAllowTransmissions = company.getQuickbooksInfo().getAllowTransmissions();
        Application.rollbackUnitOfWork();
        Assert.assertTrue("Allowed Transmissions update is failed for " + pServiceCode, isAllowTransmissions == isAllowTransAllowed);

    }

    @Test
    public void testFindTransmissions(){

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.ThirdParty401k);

        PayrollServices.beginUnitOfWork();

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSPayrollItemRepository payrollItemRepository = new QBDTWSPayrollItemRepository(true);
        WSPaycheckGenerator paycheckGenerator = new WSPaycheckGenerator(payrollItemRepository);
        QBPaychecks qbPaychecks = new QBPaychecks();

        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            QBPaycheck paycheck = paycheckGenerator.newPaycheck(qbEmployee, "01/12/2011", 102)
                    .addEarningLine(12, 120.50, 10200.48)
                    .add401kEmployeeDeferralLine(-12, -100)
                    .getPaycheck();

            qbPaychecks.getPaycheck().add(paycheck);
        }

        request.setPaycheckList(qbPaychecks);
        request.setPayrollItemList(payrollItemRepository.getAllPayrollItems());

        Application.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", new QBPayrollWebServices().SubmitPayroll(request));
        ArrayList<SAPTransmission> returnList = new ArrayList<>();
        try {
            returnList = mCompanyAdapter.findTransmissions("QBDT", psid , new Date(107, 10, 14), new Date(111, 10, 19), SourceSystemCode.QBDT.toString());
        }
        catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
        }
        assertEquals(1, returnList.size());
        assertEquals(returnList.get(0).getCompanyKey().getCompanyId(), "123456789");
        assertEquals(returnList.get(0).getCompanyKey().getSourceSystemCd(), "QBDT");
        assertEquals(returnList.get(0).getCompanyName(), "TEST_COMPANY_1");
        assertEquals(returnList.get(0).getDescription(), "WS401KSubmitPayroll");

    }

    @Test
    public void testTransmissionsByIPAndDate_validateSAPTransmissionContent() {

        String companyPSID = "999061349";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        PayrollServices.commitUnitOfWork();
        String OFX = com.intuit.sbd.payroll.psp.util.FileUtils.readClasspathFileContent("ofx/HPDE999061606.txt");

        QBDTRequestProcessor qbdtRequestProcessor = new QBDTRequestProcessor();
        qbdtRequestProcessor.processRequest(OFX, companyPSID,"192.168.123.22");
        QBDTTestHelper.assertNoErrorRequests(company);
        SAPSearchResults<SAPTransmission> transmissionList = new SAPSearchResults<>();
        try {
            transmissionList = mCompanyAdapter.findTransmissionByIPAndDate("192.168.123.22", new Date(107, 10, 14), new Date(111, 10, 19));
        }
        catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
        }
        Assert.assertEquals("Transmissions count ", 1, transmissionList.getReturnsList().size());

    }

    @Test
    public void testTransmissionsByIPAndDate() {
        // Load Company
        DataLoadServices.newCompany(SourceSystemCode.QBDT,"123");

        // Create a Source System Transmission
        SourceSystemTransmissionDTO sourceSystemTransmissionDTO;
        Long initialToken = 0L;
        Random rnd = new Random();
        String ipAddress1="192.168.123.22";
        String ipAddress2="192.162.122.3";
        String ipAddress="";
        for (int i = 1; i <= 10; i++) {
            PayrollServices.beginUnitOfWork();
            String transmissionId = SpcfUniqueId.createInstance(true).toString();
            sourceSystemTransmissionDTO = new SourceSystemTransmissionDTO();
            sourceSystemTransmissionDTO.setRequestToken(initialToken + i);
            SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 11, 15 + (i/61), rnd.nextInt(23),rnd.nextInt(59),rnd.nextInt(59),rnd.nextInt(999),SpcfTimeZone.getLocalTimeZone());
            PSPDate.setPSPTime(testTime);
            PayrollServices.commitUnitOfWork();

            sourceSystemTransmissionDTO.setRequestDocument(OFX_REQUEST_DOC);
            sourceSystemTransmissionDTO.setTransmissionType(TransmissionType.Sync);
            sourceSystemTransmissionDTO.setFromSourceSystem(SourceSystemCode.QBDT);
            if(i<5) {
                ipAddress = ipAddress1;
            }
            else{
                ipAddress = ipAddress2;
            }
            sourceSystemTransmissionDTO.setIPAddress(ipAddress);

                ProcessResult<SourceSystemTransmission> processResult = PayrollServices.transmissionManagerSecondary.initializeTransmission(SourceSystemCode.QBDT,
                        "123", transmissionId, sourceSystemTransmissionDTO);

                // Check that transmission was successfully created
                assertSuccess("initializeSourceSystemTransmission", processResult);

                // Finalize the Source System Transmission
                sourceSystemTransmissionDTO.setResponseToken(initialToken + i + 1);
                sourceSystemTransmissionDTO.setResponseDocument(OFX_RESPONSE_DOC);
                processResult = PayrollServices.transmissionManagerSecondary.finalizeTransmission(SourceSystemCode.QBDT,
                        "123", transmissionId, sourceSystemTransmissionDTO);

                // Check that transmission was successfully finalized
                assertSuccess("finalizeSourceSystemTransmission", processResult);

        }
        SAPSearchResults<SAPTransmission> transmissionList = null;
        try {
            /*  Reminder: Date(deprecated) uses 0 based months and year is {year minus 1900}   */
            transmissionList = mCompanyAdapter.findTransmissionByIPAndDate("192.168.123.22", new Date(107, 10, 14), new Date(107, 10, 19));
        }
        catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
        }
        Assert.assertEquals("Transmissions count ", 1, transmissionList.getReturnsList().size());
    }
    private static String OFX_REQUEST_DOC = "<OFX>\n"+
            "<SIGNONMSGSRQV1>\n"+
            "<SONRQ>\n"+
            "<DTCLIENT>20080718001754\n"+
            "<USERID>8574536\n"+
            "<USERPASS>test1234\n"+
            "<LANGUAGE>ENG\n"+
            "<APPVER>50.00.R.3/20804#pro\n"+
            "<APPID>QBWPRO\n"+
            "<I.QBFILENAME>C:\\Documents and Settings\\All Users\\Documents\\Intuit\\QuickBooks\\Company Files\\Joes Cool Co.QBW\n"+
            "<I.QBFILEID>c8e251053a984b3b9e107e8daa9bb640\n"+
            "<I.IPADDRESS>FileInfo:QB_data_engine_18:172.17.214.180#10180\n"+
            "<I.QBUSERNAME>Admin\n"+
            "</SONRQ>\n"+
            "</SIGNONMSGSRQV1>\n"+
            "<I.PAYROLLMSGSRQV1>\n"+
            "<I.PAYROLLUPDATERQ>\n"+
            "<TOKEN>1\n"+
            "<REJECTIFMISSING>Y\n"+
            "<I.PAYROLLTRNRQ>\n"+
            "<TRNUID>87536D20-79F5-1000-BB15-CB9C31AB0026\n"+
            "<I.PAYROLLRQ>\n"+
            "<I.PAYROLLRUN>\n"+
            "<I.DTPAYCHKS>20070810\n"+
            "<I.PAYCHK>\n"+
            "<I.PAYCHKID>1\n"+
            "<I.EMPID>0\n"+
            "<I.DTTX>20070810\n"+
            "<I.PAYCHKTYPE>PAYCHK\n"+
            "<I.EMPNAME>Donovan McNabb\n"+
            "<I.CLASS>^@~*\n"+
            "<I.ACCTNAME>BofA\n"+
            "<I.AMT>$0.00\n"+
            "<I.PAYCHKINFO>\n"+
            "<I.SICKACCRUED>^@~*\n"+
            "<I.VACACCRUED>^@~*\n"+
            "<I.PRORATE>N\n"+
            "<I.CHKNUM>TOPRINT\n"+
            "</I.PAYCHKINFO>\n"+
            "<I.VOID>N\n"+
            "<I.ONSERVICE>Y\n"+
            "<I.DTPAYPDBEGIN>20071117\n"+
            "<I.DTPAYPDEND>20071130\n"+
            "<I.MEMO>Direct Deposit\n"+
            "<I.CLEARED>2\n"+
            "<I.DDLINE>\n"+
            "<I.DDACCT>\n"+
            "<I.ACCTNAME>Bank of Money\n"+
            "<I.AMT>^@~*\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>0011992288\n"+
            "<ACCTTYPE>SAVINGS\n"+
            "</BANKACCTTO>\n"+
            "</I.DDACCT>\n"+
            "<I.PITEMID>0\n"+
            "<I.AMT>$-927.69\n"+
            "</I.DDLINE>\n"+
            "</I.PAYCHK>\n"+
            "<I.PAYCHK>\n"+
            "<I.PAYCHKID>2\n"+
            "<I.EMPID>0\n"+
            "<I.DTTX>20070810\n"+
            "<I.PAYCHKTYPE>PAYCHK\n"+
            "<I.EMPNAME>Abe Lincoln\n"+
            "<I.CLASS>^@~*\n"+
            "<I.ACCTNAME>Abe's Acct\n"+
            "<I.AMT>$0.00\n"+
            "<I.PAYCHKINFO>\n"+
            "<I.SICKACCRUED>^@~*\n"+
            "<I.VACACCRUED>^@~*\n"+
            "<I.PRORATE>N\n"+
            "<I.CHKNUM>TOPRINT\n"+
            "</I.PAYCHKINFO>\n"+
            "<I.VOID>N\n"+
            "<I.ONSERVICE>Y\n"+
            "<I.DTPAYPDBEGIN>20071117\n"+
            "<I.DTPAYPDEND>20071130\n"+
            "<I.MEMO>Direct Deposit\n"+
            "<I.CLEARED>0\n"+
            "<I.DDLINE>\n"+
            "<I.DDACCT>\n"+
            "<I.ACCTNAME>Abe's Bank\n"+
            "<I.AMT>$40.00\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>11122221111\n"+
            "<ACCTTYPE>SAVINGS\n"+
            "</BANKACCTTO>\n"+
            "</I.DDACCT>\n"+
            "<I.PITEMID>0\n"+
            "<I.AMT>$-40.00\n"+
            "</I.DDLINE>\n"+
            "<I.DDLINE>\n"+
            "<I.DDACCT>\n"+
            "<I.ACCTNAME>Abe's Bank\n"+
            "<I.AMT>^@~*\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>333322222233\n"+
            "<ACCTTYPE>CHECKING\n"+
            "</BANKACCTTO>\n"+
            "</I.DDACCT>\n"+
            "<I.PITEMID>0\n"+
            "<I.AMT>$-153.11\n"+
            "</I.DDLINE>\n"+
            "</I.PAYCHK>\n"+
            "<I.DDADVICE>\n"+
            "<I.DDAMT>$-0.00\n"+
            "<I.DD>\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>0011992288\n"+
            "<ACCTTYPE>SAVINGS\n"+
            "</BANKACCTTO>\n"+
            "<I.EMPID>0\n"+
            "<I.AMT>$-927.69\n"+
            "<I.EMPNAME>Donovan McNabb\n"+
            "<I.SSN>567-12-3456\n"+
            "</I.DD>\n"+
            "<I.DD>\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>0011992288\n"+
            "<ACCTTYPE>SAVINGS\n"+
            "</BANKACCTTO>\n"+
            "<I.EMPID>0\n"+
            "<I.AMT>$-40.00\n"+
            "<I.EMPNAME>Abe Lincoln\n"+
            "<I.SSN>567-12-3456\n"+
            "</I.DD>\n"+
            "<I.DD>\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>0011992288\n"+
            "<ACCTTYPE>SAVINGS\n"+
            "</BANKACCTTO>\n"+
            "<I.EMPID>0\n"+
            "<I.AMT>$-153.11\n"+
            "<I.EMPNAME>Abe Lincoln\n"+
            "<I.SSN>567-12-3456\n"+
            "</I.DD>\n"+
            "</I.DDADVICE>\n"+
            "</I.PAYROLLRUN>\n"+
            "<I.PAYROLLRUN>\n"+
            "<I.DTPAYCHKS>20070816\n"+
            "<I.PAYCHK>\n"+
            "<I.PAYCHKID>3\n"+
            "<I.EMPID>0\n"+
            "<I.DTTX>20070816\n"+
            "<I.PAYCHKTYPE>PAYCHK\n"+
            "<I.EMPNAME>Donovan McNabb\n"+
            "<I.CLASS>^@~*\n"+
            "<I.ACCTNAME>BofA\n"+
            "<I.AMT>$0.00\n"+
            "<I.PAYCHKINFO>\n"+
            "<I.SICKACCRUED>^@~*\n"+
            "<I.VACACCRUED>^@~*\n"+
            "<I.PRORATE>N\n"+
            "<I.CHKNUM>TOPRINT\n"+
            "</I.PAYCHKINFO>\n"+
            "<I.VOID>N\n"+
            "<I.ONSERVICE>Y\n"+
            "<I.DTPAYPDBEGIN>20071117\n"+
            "<I.DTPAYPDEND>20071130\n"+
            "<I.MEMO>Direct Deposit\n"+
            "<I.CLEARED>2\n"+
            "<I.DDLINE>\n"+
            "<I.DDACCT>\n"+
            "<I.ACCTNAME>Bank of Money\n"+
            "<I.AMT>^@~*\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>0011992288\n"+
            "<ACCTTYPE>SAVINGS\n"+
            "</BANKACCTTO>\n"+
            "</I.DDACCT>\n"+
            "<I.PITEMID>0\n"+
            "<I.AMT>$-8091.11\n"+
            "</I.DDLINE>\n"+
            "</I.PAYCHK>\n"+
            "<I.PAYCHK>\n"+
            "<I.PAYCHKID>4\n"+
            "<I.EMPID>0\n"+
            "<I.DTTX>20070816\n"+
            "<I.PAYCHKTYPE>PAYCHK\n"+
            "<I.EMPNAME>Abe Lincoln\n"+
            "<I.CLASS>^@~*\n"+
            "<I.ACCTNAME>Abe's Acct\n"+
            "<I.AMT>$0.00\n"+
            "<I.PAYCHKINFO>\n"+
            "<I.SICKACCRUED>^@~*\n"+
            "<I.VACACCRUED>^@~*\n"+
            "<I.PRORATE>N\n"+
            "<I.CHKNUM>TOPRINT\n"+
            "</I.PAYCHKINFO>\n"+
            "<I.VOID>N\n"+
            "<I.ONSERVICE>Y\n"+
            "<I.DTPAYPDBEGIN>20071117\n"+
            "<I.DTPAYPDEND>20071130\n"+
            "<I.MEMO>Direct Deposit\n"+
            "<I.CLEARED>0\n"+
            "<I.DDLINE>\n"+
            "<I.DDACCT>\n"+
            "<I.ACCTNAME>Abe's Bank\n"+
            "<I.AMT>$100.00\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>11122221111\n"+
            "<ACCTTYPE>SAVINGS\n"+
            "</BANKACCTTO>\n"+
            "</I.DDACCT>\n"+
            "<I.PITEMID>0\n"+
            "<I.AMT>$-100.00\n"+
            "</I.DDLINE>\n"+
            "<I.DDLINE>\n"+
            "<I.DDACCT>\n"+
            "<I.ACCTNAME>Abe's Bank\n"+
            "<I.AMT>^@~*\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>333322222233\n"+
            "<ACCTTYPE>CHECKING\n"+
            "</BANKACCTTO>\n"+
            "</I.DDACCT>\n"+
            "<I.PITEMID>0\n"+
            "<I.AMT>$-2012.44\n"+
            "</I.DDLINE>\n"+
            "</I.PAYCHK>\n"+
            "<I.DDADVICE>\n"+
            "<I.DDAMT>$-0.00\n"+
            "<I.DD>\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>0011992288\n"+
            "<ACCTTYPE>SAVINGS\n"+
            "</BANKACCTTO>\n"+
            "<I.EMPID>0\n"+
            "<I.AMT>$-8091.11\n"+
            "<I.EMPNAME>Donovan McNabb\n"+
            "<I.SSN>567-12-3456\n"+
            "</I.DD>\n"+
            "<I.DD>\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>0011992288\n"+
            "<ACCTTYPE>SAVINGS\n"+
            "</BANKACCTTO>\n"+
            "<I.EMPID>0\n"+
            "<I.AMT>$-100.00\n"+
            "<I.EMPNAME>Abe Lincoln\n"+
            "<I.SSN>567-12-3456\n"+
            "</I.DD>\n"+
            "<I.DD>\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>0011992288\n"+
            "<ACCTTYPE>SAVINGS\n"+
            "</BANKACCTTO>\n"+
            "<I.EMPID>0\n"+
            "<I.AMT>$-2012.44\n"+
            "<I.EMPNAME>Abe Lincoln\n"+
            "<I.SSN>567-12-3456\n"+
            "</I.DD>\n"+
            "</I.DDADVICE>\n"+
            "</I.PAYROLLRUN>\n"+
            "</I.PAYROLLRQ>\n"+
            "</I.PAYROLLTRNRQ>\n"+
            "</I.PAYROLLUPDATERQ>\n"+
            "</I.PAYROLLMSGSRQV1>\n"+
            "</OFX>";

    private static String OFX_RESPONSE_DOC = "<OFX>\n"+
            "<SIGNONMSGSRSV1>\n"+
            "<SONRS>\n"+
            "<STATUS>\n"+
            "<CODE>0\n"+
            "<SEVERITY>INFO\n"+
            "</STATUS>\n"+
            "<DTSERVER>20080718001917\n"+
            "<LANGUAGE>ENG\n"+
            "</SONRS>\n"+
            "</SIGNONMSGSRSV1>\n"+
            "<I.PAYROLLMSGSRSV1>\n"+
            "<I.PAYROLLUPDATERS>\n"+
            "<TOKEN>2\n"+
            "<I.PAYROLLTXNEXTID>3\n"+
            "<I.PAYCHKNEXTID>5\n"+
            "<I.EMPNEXTID>1\n"+
            "<I.PITEMNEXTID>1\n"+
            "<I.PAYROLLTRNRS>\n"+
            "<TRNUID>87536D20-79F5-1000-BB15-CB9C31AB0026\n"+
            "<STATUS>\n"+
            "<CODE>0\n"+
            "<SEVERITY>INFO\n"+
            "</STATUS>\n"+
            "<I.PAYROLLRS>\n"+
            "<I.PAYROLLTX>\n"+
            "<I.PAYROLLTXID>1\n"+
            "<I.NAME>QuickBooks Payroll Service\n"+
            "<I.ACCTNAME>BofA\n"+
            "<I.AMT>$-1126.29\n"+
            "<I.MEMO>Created by Payroll Services on 08/02/2007\n"+
            "<I.CLEARED>0\n"+
            "<I.DTTX>20070810\n"+
            "<I.REFNUM>^@~*\n"+
            "<I.PAYROLLTXTYPE>LIABCHK\n"+
            "<I.DTPAYPDEND>20070810\n"+
            "<I.VOID>N\n"+
            "<I.ONSERVICE>Y\n"+
            "<I.TXLINE>\n"+
            "<I.ACCTNAME>Payroll Expenses\n"+
            "<I.AMT>$2.10\n"+
            "<I.MEMO>Fee for 2 direct deposit(s) at $1.05 each\n"+
            "</I.TXLINE>\n"+
            "<I.TXLINE>\n"+
            "<I.ACCTNAME>Payroll Expenses\n"+
            "<I.AMT>$3.00\n"+
            "<I.MEMO>Direct Deposit Transmission Fee\n"+
            "</I.TXLINE>\n"+
            "<I.TXLINE>\n"+
            "<I.ACCTNAME>Payroll Expenses\n"+
            "<I.AMT>0.51\n"+
            "<I.MEMO>Sales Tax for null\n"+
            "</I.TXLINE>\n"+
            "<I.TXLINE>\n"+
            "<I.AMT>$1120.80\n"+
            "<I.ISDD>Y\n"+
            "</I.TXLINE>\n"+
            "</I.PAYROLLTX>\n"+
            "</I.PAYROLLRS>\n"+
            "<I.PAYROLLRS>\n"+
            "<I.PAYROLLTX>\n"+
            "<I.PAYROLLTXID>2\n"+
            "<I.NAME>QuickBooks Payroll Service\n"+
            "<I.ACCTNAME>BofA\n"+
            "<I.AMT>$-10209.04\n"+
            "<I.MEMO>Created by Payroll Services on 08/02/2007\n"+
            "<I.CLEARED>0\n"+
            "<I.DTTX>20070816\n"+
            "<I.REFNUM>^@~*\n"+
            "<I.PAYROLLTXTYPE>LIABCHK\n"+
            "<I.DTPAYPDEND>20070816\n"+
            "<I.VOID>N\n"+
            "<I.ONSERVICE>Y\n"+
            "<I.TXLINE>\n"+
            "<I.ACCTNAME>Payroll Expenses\n"+
            "<I.AMT>$2.10\n"+
            "<I.MEMO>Fee for 2 direct deposit(s) at $1.05 each\n"+
            "</I.TXLINE>\n"+
            "<I.TXLINE>\n"+
            "<I.ACCTNAME>Payroll Expenses\n"+
            "<I.AMT>$3.00\n"+
            "<I.MEMO>Direct Deposit Transmission Fee\n"+
            "</I.TXLINE>\n"+
            "<I.TXLINE>\n"+
            "<I.ACCTNAME>Payroll Expenses\n"+
            "<I.AMT>0.51\n"+
            "<I.MEMO>Sales Tax for null\n"+
            "</I.TXLINE>\n"+
            "<I.TXLINE>\n"+
            "<I.AMT>$10203.55\n"+
            "<I.ISDD>Y\n"+
            "</I.TXLINE>\n"+
            "</I.PAYROLLTX>\n"+
            "</I.PAYROLLRS>\n"+
            "</I.PAYROLLTRNRS>\n"+
            "</I.PAYROLLUPDATERS>\n"+
            "</I.PAYROLLMSGSRSV1>\n"+
            "</OFX>";

}



