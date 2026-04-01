package com.intuit.sbd.payroll.psp.adapters.sap;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.RoleSubStatus;
import com.intuit.sbd.payroll.psp.domain.SubStatusChangeType;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.query.Expression;
import static org.junit.Assert.*;

import java.util.*;

/**
 * User: dweinberg
 * Date: Aug 3, 2009
 * Time: 10:18:28 AM
 * Tests that substatus transitions match the requirements
 */
@Ignore("The code gen needs a lot of work to make this work and it's probably not worth it.")
public class RoleStatusTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testRoleStatuses() {
        PayrollServices.beginUnitOfWork();

        StringBuilder fails = new StringBuilder();

        Collection<String> roles = getRoles();
        Map<String, List<String>> substatuses = getSubstatuses();

        for (String role : roles) {
            Expression<RoleSubStatus> fromSSQuery = new Query<RoleSubStatus>()
                    .Where(RoleSubStatus.AuthRole().RoleId().equalTo(role)
                    .And(RoleSubStatus.AllowedChangeType().equalTo(SubStatusChangeType.CanMoveFromSubStatus)));
            DomainEntitySet<RoleSubStatus> fromSS = PayrollServices.entityFinder.find(RoleSubStatus.class, fromSSQuery);

            Expression<RoleSubStatus> toSSQuery = new Query<RoleSubStatus>()
                    .Where(RoleSubStatus.AuthRole().RoleId().equalTo(role)
                    .And(RoleSubStatus.AllowedChangeType().equalTo(SubStatusChangeType.CanMoveToSubStatus)));
            DomainEntitySet<RoleSubStatus> toSS = PayrollServices.entityFinder.find(RoleSubStatus.class, toSSQuery);

            List<String> fromOps = substatuses.get(role + ",from");
            List<String> toOps = substatuses.get(role + ",to");

            for (RoleSubStatus rss : fromSS ) {
                if (! fromOps.contains(rss.getServiceSubStatus().getServiceSubStatusCd().toString())) {
                    fails.append("(").append(role).append(", from, ").append(rss.getServiceSubStatus().getServiceSubStatusCd().toString()).append(") is in db but not requirements").append("\n");
                }
            }

            for (RoleSubStatus rss : toSS ) {
                String rssString = rss.getServiceSubStatus().getServiceSubStatusCd().toString();
                if (! toOps.contains(rssString)) {
                    if (! rssString.equals("ActiveCurrent") && !rssString.equals("FraudReview")) {
                        fails.append("(").append(role).append(", to, ").append(rss.getServiceSubStatus().getServiceSubStatusCd().toString()).append(") is in db but not requirements").append("\n");
                    }                    
                }
            }

            //other direction
            for (String op : fromOps) {
                boolean found = false;
                for (RoleSubStatus rss : fromSS) {
                    if (rss.getServiceSubStatus().getServiceSubStatusCd().toString().equals(op)) {
                        found=true;
                        break;
                    }
                }
                if (!found){
                    fails.append("(").append(role).append(", from, ").append(op).append(") is in requirements but not db").append("\n");
                }
            }

            for (String op : toOps) {
                if (! op.equals("ActiveCurrent") && !op.equals("FraudReview")) {
                    boolean found = false;
                    for (RoleSubStatus rss : toSS) {
                        if (rss.getServiceSubStatus().getServiceSubStatusCd().toString().equals(op)) {
                            found=true;
                            break;
                        }
                    }
                    if (!found){
                        fails.append("(").append(role).append(", to, ").append(op).append(") is in requirements but not db").append("\n");
                    }
                }
            }


        }

        if (fails.length() != 0) {
            fail(fails.toString());
        }

        PayrollServices.rollbackUnitOfWork();
    }

    private Collection<String> getRoles() {
        ArrayList<String> roles = new ArrayList<String>();
        roles.add("QBOE-IOPRep");
        roles.add("QBOE-IOPSeniorRep");
        roles.add("QBOE-IOPTeamLead");
        roles.add("QBOE-IOPProductSpecialist");
        roles.add("FRGRep");
        roles.add("FRGSupervisor");
        roles.add("FRGManager");
        roles.add("RMRep");
        roles.add("RMSupervisor");
        roles.add("RMManager");
        roles.add("Accounting");
        roles.add("AccountingManager");
        roles.add("ReadOnly");
        roles.add("DesktopCareAgent");
        roles.add("DesktopCareManager");
        roles.add("DirectDepositTierIII");
        roles.add("DirectDepositSME");
        roles.add("Activations");
        roles.add("DesktopCareLead");
        roles.add("ActivationsTierIII");
        roles.add("PSOAgent");
        roles.add("POA");
        roles.add("TaxRep");
        roles.add("Operator");
        roles.add("HelpDesk");
        roles.add("DataCustodian");
        roles.add("CheckDistributionAgent");
        roles.add("PrintQueueAgent");
        roles.add("EngineeringReadOnly");
        roles.add("TaxCreditsRep");
        roles.add("CS-RMRep");
        return roles;
    }

    private Map<String, List<String>> getSubstatuses() {
        Map<String, List<String>> sss = new HashMap<String, List<String>>();
        sss.put("QBOE-IOPRep,from", new ArrayList<String>());
        sss.put("QBOE-IOPRep,to", new ArrayList<String>());
        sss.put("QBOE-IOPSeniorRep,from", new ArrayList<String>());
        sss.put("QBOE-IOPSeniorRep,to", new ArrayList<String>());
        sss.put("QBOE-IOPTeamLead,from", new ArrayList<String>());
        sss.put("QBOE-IOPTeamLead,to", new ArrayList<String>());
        sss.get("QBOE-IOPTeamLead,from").add("ActiveCurrent");
        sss.get("QBOE-IOPTeamLead,to").add("Cancelled");
        sss.get("QBOE-IOPTeamLead,from").add("PendingBankVerification");
        sss.get("QBOE-IOPTeamLead,from").add("PendingFirstPayroll");
        sss.get("QBOE-IOPTeamLead,from").add("PendingPinCreation");
        sss.put("QBOE-IOPProductSpecialist,from", new ArrayList<String>());
        sss.put("QBOE-IOPProductSpecialist,to", new ArrayList<String>());
        sss.put("FRGRep,from", new ArrayList<String>());
        sss.put("FRGRep,to", new ArrayList<String>());
        sss.get("FRGRep,from").add("ActiveCurrent");
        sss.get("FRGRep,from").add("DirectDepositLimit");
        sss.get("FRGRep,to").add("IntuitCollections");
        sss.get("FRGRep,to").add("RiskCollections");
        sss.get("FRGRep,from").add("RiskAssessment");
        sss.get("FRGRep,from").add("PendingPrefundingWire");
        sss.get("FRGRep,to").add("PendingPrefundingWire");
        sss.get("FRGRep,from").add("AchRejectOther");
        sss.get("FRGRep,from").add("AchRejectR1R9");
        sss.get("FRGRep,to").add("RiskAssessment");
        sss.put("FRGSupervisor,from", new ArrayList<String>());
        sss.put("FRGSupervisor,to", new ArrayList<String>());
        sss.get("FRGSupervisor,from").add("ActiveCurrent");
        sss.get("FRGSupervisor,to").add("Cancelled");
        sss.get("FRGSupervisor,from").add("PendingBankVerification");
        sss.get("FRGSupervisor,from").add("PendingFirstPayroll");
        sss.get("FRGSupervisor,from").add("PendingPinCreation");
        sss.get("FRGSupervisor,from").add("DirectDepositLimit");
        sss.get("FRGSupervisor,to").add("IntuitCollections");
        sss.get("FRGSupervisor,to").add("RiskCollections");
        sss.get("FRGSupervisor,from").add("RiskAssessment");
        sss.get("FRGSupervisor,from").add("PendingPrefundingWire");
        sss.get("FRGSupervisor,to").add("PendingPrefundingWire");
        sss.get("FRGSupervisor,from").add("AchRejectOther");
        sss.get("FRGSupervisor,from").add("AchRejectR1R9");
        sss.get("FRGSupervisor,to").add("RiskAssessment");
        sss.put("FRGManager,from", new ArrayList<String>());
        sss.put("FRGManager,to", new ArrayList<String>());
        sss.get("FRGManager,from").add("ActiveCurrent");
        sss.get("FRGManager,to").add("Cancelled");
        sss.get("FRGManager,from").add("PendingBankVerification");
        sss.get("FRGManager,from").add("PendingFirstPayroll");
        sss.get("FRGManager,from").add("PendingPinCreation");
        sss.get("FRGManager,from").add("DirectDepositLimit");
        sss.get("FRGManager,to").add("IntuitCollections");
        sss.get("FRGManager,to").add("RiskCollections");
        sss.get("FRGManager,from").add("RiskAssessment");
        sss.get("FRGManager,from").add("PendingPrefundingWire");
        sss.get("FRGManager,to").add("PendingPrefundingWire");
        sss.get("FRGManager,from").add("AchRejectOther");
        sss.get("FRGManager,from").add("AchRejectR1R9");
        sss.get("FRGManager,to").add("RiskAssessment");
        sss.put("RMRep,from", new ArrayList<String>());
        sss.put("RMRep,to", new ArrayList<String>());
        sss.get("RMRep,from").add("ActiveCurrent");
        sss.get("RMRep,to").add("Cancelled");
        sss.get("RMRep,from").add("DirectDepositLimit");
        sss.get("RMRep,to").add("IntuitCollections");
        sss.get("RMRep,to").add("RiskCollections");
        sss.get("RMRep,to").add("Terminated");
        sss.get("RMRep,from").add("RiskAssessment");
        sss.get("RMRep,from").add("IntuitCollections");
        sss.get("RMRep,from").add("RiskCollections");
        sss.get("RMRep,to").add("Fraud");
        sss.get("RMRep,from").add("Fraud");
        sss.get("RMRep,from").add("FraudReview");
        sss.get("RMRep,to").add("SuspendedDirectDeposit");
        sss.get("RMRep,from").add("SuspendedDirectDeposit");
        sss.get("RMRep,to").add("PendingTermination");
        sss.get("RMRep,from").add("PendingTermination");
        sss.get("RMRep,from").add("PendingPrefundingWire");
        sss.get("RMRep,to").add("PendingPrefundingWire");
        sss.get("RMRep,from").add("AchRejectOther");
        sss.get("RMRep,from").add("AchRejectR1R9");
        sss.get("RMRep,to").add("RiskAssessment");
        sss.get("RMRep,from").add("Terminated");
        sss.put("RMSupervisor,from", new ArrayList<String>());
        sss.put("RMSupervisor,to", new ArrayList<String>());
        sss.get("RMSupervisor,from").add("ActiveCurrent");
        sss.get("RMSupervisor,to").add("Cancelled");
        sss.get("RMSupervisor,from").add("PendingBankVerification");
        sss.get("RMSupervisor,from").add("PendingFirstPayroll");
        sss.get("RMSupervisor,from").add("PendingPinCreation");
        sss.get("RMSupervisor,from").add("DirectDepositLimit");
        sss.get("RMSupervisor,to").add("IntuitCollections");
        sss.get("RMSupervisor,to").add("RiskCollections");
        sss.get("RMSupervisor,to").add("Terminated");
        sss.get("RMSupervisor,from").add("RiskAssessment");
        sss.get("RMSupervisor,from").add("IntuitCollections");
        sss.get("RMSupervisor,from").add("RiskCollections");
        sss.get("RMSupervisor,to").add("Fraud");
        sss.get("RMSupervisor,from").add("Fraud");
        sss.get("RMSupervisor,from").add("FraudReview");
        sss.get("RMSupervisor,to").add("SuspendedDirectDeposit");
        sss.get("RMSupervisor,from").add("SuspendedDirectDeposit");
        sss.get("RMSupervisor,to").add("PendingTermination");
        sss.get("RMSupervisor,from").add("PendingTermination");
        sss.get("RMSupervisor,from").add("PendingPrefundingWire");
        sss.get("RMSupervisor,to").add("PendingPrefundingWire");
        sss.get("RMSupervisor,from").add("AchRejectOther");
        sss.get("RMSupervisor,from").add("AchRejectR1R9");
        sss.get("RMSupervisor,to").add("RiskAssessment");
        sss.get("RMSupervisor,from").add("Terminated");
        sss.put("RMManager,from", new ArrayList<String>());
        sss.put("RMManager,to", new ArrayList<String>());
        sss.get("RMManager,from").add("ActiveCurrent");
        sss.get("RMManager,to").add("Cancelled");
        sss.get("RMManager,from").add("PendingBankVerification");
        sss.get("RMManager,from").add("PendingFirstPayroll");
        sss.get("RMManager,from").add("PendingPinCreation");
        sss.get("RMManager,from").add("DirectDepositLimit");
        sss.get("RMManager,to").add("IntuitCollections");
        sss.get("RMManager,to").add("RiskCollections");
        sss.get("RMManager,to").add("Terminated");
        sss.get("RMManager,from").add("RiskAssessment");
        sss.get("RMManager,from").add("IntuitCollections");
        sss.get("RMManager,from").add("RiskCollections");
        sss.get("RMManager,to").add("Fraud");
        sss.get("RMManager,from").add("Fraud");
        sss.get("RMManager,from").add("FraudReview");
        sss.get("RMManager,to").add("SuspendedDirectDeposit");
        sss.get("RMManager,from").add("SuspendedDirectDeposit");
        sss.get("RMManager,to").add("PendingTermination");
        sss.get("RMManager,from").add("PendingTermination");
        sss.get("RMManager,from").add("PendingPrefundingWire");
        sss.get("RMManager,to").add("PendingPrefundingWire");
        sss.get("RMManager,from").add("AchRejectOther");
        sss.get("RMManager,from").add("AchRejectR1R9");
        sss.get("RMManager,to").add("RiskAssessment");
        sss.get("RMManager,from").add("Terminated");
        sss.put("Accounting,from", new ArrayList<String>());
        sss.put("Accounting,to", new ArrayList<String>());
        sss.put("AccountingManager,from", new ArrayList<String>());
        sss.put("AccountingManager,to", new ArrayList<String>());
        sss.put("ReadOnly,from", new ArrayList<String>());
        sss.put("ReadOnly,to", new ArrayList<String>());
        sss.put("DesktopCareAgent,from", new ArrayList<String>());
        sss.put("DesktopCareAgent,to", new ArrayList<String>());
        sss.put("DesktopCareManager,from", new ArrayList<String>());
        sss.put("DesktopCareManager,to", new ArrayList<String>());
        sss.get("DesktopCareManager,from").add("ActiveCurrent");
        sss.get("DesktopCareManager,to").add("Cancelled");
        sss.get("DesktopCareManager,from").add("PendingBankVerification");
        sss.get("DesktopCareManager,from").add("PendingFirstPayroll");
        sss.get("DesktopCareManager,from").add("PendingPinCreation");
        sss.put("DirectDepositTierIII,from", new ArrayList<String>());
        sss.put("DirectDepositTierIII,to", new ArrayList<String>());
        sss.get("DirectDepositTierIII,to").add("Cancelled");
        sss.get("DirectDepositTierIII,from").add("ActiveCurrent");
        sss.get("DirectDepositTierIII,from").add("PendingBankVerification");
        sss.get("DirectDepositTierIII,from").add("PendingFirstPayroll");
        sss.get("DirectDepositTierIII,from").add("PendingPinCreation");
        sss.put("DirectDepositSME,from", new ArrayList<String>());
        sss.put("DirectDepositSME,to", new ArrayList<String>());
        sss.get("DirectDepositSME,from").add("ActiveCurrent");
        sss.get("DirectDepositSME,to").add("Cancelled");
        sss.get("DirectDepositSME,from").add("PendingBankVerification");
        sss.get("DirectDepositSME,from").add("PendingFirstPayroll");
        sss.get("DirectDepositSME,from").add("PendingPinCreation");
        sss.put("Activations,from", new ArrayList<String>());
        sss.put("Activations,to", new ArrayList<String>());
        sss.put("DesktopCareLead,from", new ArrayList<String>());
        sss.put("DesktopCareLead,to", new ArrayList<String>());
        sss.put("ActivationsTierIII,from", new ArrayList<String>());
        sss.put("ActivationsTierIII,to", new ArrayList<String>());
        sss.put("PSOAgent,from", new ArrayList<String>());
        sss.put("PSOAgent,to", new ArrayList<String>());
        sss.put("POA,from", new ArrayList<String>());
        sss.put("POA,to", new ArrayList<String>());
        sss.put("TaxRep,from", new ArrayList<String>());
        sss.put("TaxRep,to", new ArrayList<String>());
        sss.put("Operator,from", new ArrayList<String>());
        sss.put("Operator,to", new ArrayList<String>());
        sss.put("HelpDesk,from", new ArrayList<String>());
        sss.put("HelpDesk,to", new ArrayList<String>());
        sss.put("DataCustodian,from", new ArrayList<String>());
        sss.put("DataCustodian,to", new ArrayList<String>());
        sss.put("CheckDistributionAgent,from", new ArrayList<String>());
        sss.put("CheckDistributionAgent,to", new ArrayList<String>());
        sss.get("CheckDistributionAgent,from").add("ActiveCurrent");
        sss.get("CheckDistributionAgent,to").add("Cancelled");
        sss.put("PrintQueueAgent,from", new ArrayList<String>());
        sss.put("PrintQueueAgent,to", new ArrayList<String>());
        sss.put("EngineeringReadOnly,from", new ArrayList<String>());
        sss.put("EngineeringReadOnly,to", new ArrayList<String>());
        sss.put("TaxCreditsRep,from", new ArrayList<String>());
        sss.put("TaxCreditsRep,to", new ArrayList<String>());
        sss.put("CS-RMRep,from", new ArrayList<String>());
        sss.put("CS-RMRep,to", new ArrayList<String>());
        sss.get("CS-RMRep,from").add("ActiveCurrent");
        sss.get("CS-RMRep,to").add("Cancelled");
        sss.get("CS-RMRep,from").add("PendingBankVerification");
        sss.get("CS-RMRep,from").add("PendingFirstPayroll");
        sss.get("CS-RMRep,from").add("PendingPinCreation");
        sss.get("CS-RMRep,from").add("DirectDepositLimit");
        return sss;
    }

}
