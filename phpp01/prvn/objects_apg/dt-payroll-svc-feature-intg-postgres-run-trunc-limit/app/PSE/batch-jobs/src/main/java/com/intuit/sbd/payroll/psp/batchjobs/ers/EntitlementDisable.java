package com.intuit.sbd.payroll.psp.batchjobs.ers;

import com.intuit.ems.payroll.psp.gateways.ers.ERSConnectionException;
import com.intuit.ems.payroll.psp.gateways.ers.ERSGatewayFactory;
import com.intuit.ems.payroll.psp.gateways.ers.EntitlementInfoDTO;
import com.intuit.ems.payroll.psp.gateways.ers.IERSGateway;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 21, 2010
 * Time: 1:30:28 PM
 */
public class EntitlementDisable {
    private static SpcfLogger logger = Application.getLogger(EntitlementDisable.class);

    private int maxRetries;
    private int assistedWaitDays;
    private int successfulCount = 0;

    public EntitlementDisable() {
        assistedWaitDays = SystemParameter.findIntValue(SystemParameter.Code.ERS_ASSISTED_WAIT_DAYS, 120);
        maxRetries = SystemParameter.findIntValue(SystemParameter.Code.ERS_MAX_REQUEST_RETRIES, 5);
    }

    public void execute() {
        StopWatch stopWatch = new StopWatch().start();

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.ERSBatchJob));

        int companiesToDisableCount;
        List<EntitlementUnit> entitlementUnitsToDisable = new ArrayList<EntitlementUnit>();
        boolean manageTransaction = !Application.hasActiveTransaction();
        int  entitlementUnitsDisableSkipped = 0;

        try {
            if (manageTransaction){
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            }

            // select assisted companies that are still pending, do not have any payrolls, and were created > 120 days ago
            String query = "Select distinct company" +
            " from com.intuit.sbd.payroll.psp.domain.Company company" +
            "   inner join company.EntitlementUnitSet entitlementUnit" +
            " where entitlementUnit.EntitlementUnitStatus = :entitlementStatus" +
            "   and entitlementUnit.CreatedDate < :assistedWaitDate" +
            " order by company.SourceCompanyId";

            org.hibernate.Query hibernateQuery = Application.createHibernateQuery(query);
            hibernateQuery.setParameter("entitlementStatus", EntitlementUnitStatusCode.ActivationHold);
            SpcfCalendar assistedWaitPeriod = PSPDate.getPSPTime().copy();
            assistedWaitPeriod.addDays(-assistedWaitDays);
            hibernateQuery.setParameter("assistedWaitDate", assistedWaitPeriod);
            List<Company> companiesToDisable = hibernateQuery.list();
            companiesToDisableCount = companiesToDisable.size();

            for (Company company : companiesToDisable) {
                for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection()) {
                    Entitlement entitlement = entitlementUnit.getEntitlement();

                    if (entitlement.hasPendingOrRecentMessages()) {
                        ++entitlementUnitsDisableSkipped;
                        continue;
                    }

                    if((entitlement.getEntitlementCode().isAssisted() && entitlementUnit.getEntitlementUnitStatus().equals(EntitlementUnitStatusCode.ActivationHold)) ||
                            (entitlement.getEntitlementCode().isEOorER() && (entitlementUnit.getEntitlementUnitStatus().in(EntitlementUnitStatusCode.PendingActivation, EntitlementUnitStatusCode.Activated)))) {
                        entitlementUnitsToDisable.add(entitlementUnit);
                    }
                }
            }
        } finally {
            if (manageTransaction){
                PayrollServices.rollbackUnitOfWork();
            }
        }

        disable(entitlementUnitsToDisable);

        logger.info("Completed submitting disabled entitlements =" +(companiesToDisableCount - entitlementUnitsDisableSkipped) + " entitlements in " + stopWatch.getElapsedTimeString());
    }

    public void disable(List<EntitlementUnit> entitlementUnitsToDisable) {

        IERSGateway ersGateway = ERSGatewayFactory.createInstance();

        if(ersGateway == null) {
            logger.error("Could not connect to ERS.");
            return;
        }

        for (EntitlementUnit entitlementUnit : entitlementUnitsToDisable) {
            EntitlementUnit eu;
            Company company;
            boolean manageTransaction = !Application.hasActiveTransaction();
            try {
                if (manageTransaction){
                    PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                }

                eu = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
                company = eu.getCompany();
                Entitlement entitlement = eu.getEntitlement();

                EntitlementInfoDTO entitlementInfoDTO = ersGateway.getEntitlementInfo(entitlement.getLicenseNumber(),
                                                                                      entitlement.getEntitlementOfferingCode(),
                                                                                      true,
                                                                                      new ERSListener(company, TransmissionType.EntitlementCancel));

                if (EntitlementStateCode.Enabled.equals(entitlementInfoDTO.getEntitlementState())) {
                    ersGateway.disableEntitlement(entitlement.getLicenseNumber(),
                                                  entitlement.getEntitlementOfferingCode(),
                                                  new ERSListener(company, TransmissionType.EntitlementCancel));
                }

                // update company entitlement & entitlement unit status
                EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(eu);
                entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
                entitlementUnitDTO.setEntitlementState(EntitlementStateCode.Disabled);
                ProcessResult processResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);

                if(processResult.isSuccess()) {
                    if (manageTransaction){
                        PayrollServices.commitUnitOfWork();
                    }
                } else {
                    throw new RuntimeException(processResult.toString());
                }
            } catch(ERSConnectionException e) {
                logger.error("Error connecting to ERS.", e);
                // stop processing if we cannot connect to the ERS
                break;
            } catch (Throwable t) {
                if (manageTransaction){
                    PayrollServices.rollbackUnitOfWork();
                    PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                }

                eu = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
                company = eu.getCompany();

                logger.error("Error disabling entitlement for company " + company.getSourceSystemCd().toString() + ":" + company.getSourceCompanyId(), t);

                CompanyEvent.createEntitlementErrorEvent(company,
                                                         EventTypeCode.EntitlementCommunication,
                                                         "Error disabling entitlement for company " +
                                                                 company.getSourceSystemCompanyId() + ". " + t.getMessage(),
                                                         null,
                                                         null);

                EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(eu);
                entitlementUnitDTO.setErrorCount(entitlementUnitDTO.getErrorCount() + 1);
                if(entitlementUnitDTO.getErrorCount() >= maxRetries) {
                    entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorDeactivating);
                }
                ProcessResult processResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
                if(processResult.isSuccess()) {
                    CompanyEvent.createEntitlementErrorEvent(company,
                                                             EventTypeCode.EntitlementCommunication,
                                                             "Error activating entitlement unit for entitlement " +
                                                                     eu.getEntitlement() + ": " + t.getMessage(),
                                                             eu.getEntitlement(),
                                                             eu);
                    if (manageTransaction){
                        PayrollServices.commitUnitOfWork();
                    }
                } else {
                    logger.error("Error updating entitlement unit " + eu.getId() + "to " + EntitlementUnitStatusCode.ErrorActivating + "\n" + processResult.toString());
                    if (manageTransaction){
                        PayrollServices.rollbackUnitOfWork();
                    }
                }
            } finally {
                if (manageTransaction){
                    PayrollServices.rollbackUnitOfWork();
                }
            }

        }
    }

    /**
     * @param entitlementUnitsToDisable
     * @return
     */

    public String disableAuto(List<EntitlementUnit> entitlementUnitsToDisable){

        IERSGateway ersGateway = ERSGatewayFactory.createInstance();
        String jobResult = null;

        if(ersGateway == null) {
            logger.error("Could not connect to ERS.");
            return null;
        }

        for (EntitlementUnit entitlementUnit : entitlementUnitsToDisable) {
            EntitlementUnit eu;
            Company company = null;
            boolean manageTransaction = !Application.hasActiveTransaction();
            try {
                if (manageTransaction){
                    PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                }

                eu = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
                company = eu.getCompany();
                Entitlement entitlement = eu.getEntitlement();

                EntitlementInfoDTO entitlementInfoDTO = ersGateway.getEntitlementInfo(entitlement.getLicenseNumber(),
                        entitlement.getEntitlementOfferingCode(),
                        true,
                        new ERSListener(company, TransmissionType.EntitlementCancel));

                if (EntitlementStateCode.Enabled.equals(entitlementInfoDTO.getEntitlementState())) {
                    ersGateway.disableEntitlement(entitlement.getLicenseNumber(),
                            entitlement.getEntitlementOfferingCode(),
                            new ERSListener(company, TransmissionType.EntitlementCancel));
                }

                // update company entitlement & entitlement unit status
                EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(eu);
                entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
                entitlementUnitDTO.setEntitlementState(EntitlementStateCode.Disabled);
                ProcessResult processResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);

                if(processResult.isSuccess()) {
                    if (manageTransaction){
                        PayrollServices.commitUnitOfWork();
                    }
                    successfulCount++;
                    logger.info("Successfully deactivated source company with PSID (AutoMass Cancellation)=" + company.getSourceCompanyId());

                } else {
                    throw new RuntimeException(processResult.toString());
                }
            } catch(ERSConnectionException e) {
                logger.error("Error connecting to ERS.", e);
                // stop processing if we cannot connect to the ERS
                break;
            } catch (Throwable t) {
                if (manageTransaction){
                    PayrollServices.rollbackUnitOfWork();
                    PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                }

                eu = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
                company = eu.getCompany();

                logger.error("Error disabling entitlement for company " + company.getSourceSystemCd().toString() + ":" + company.getSourceCompanyId(), t);

                CompanyEvent.createEntitlementErrorEvent(company,
                        EventTypeCode.EntitlementCommunication,
                        "Error disabling entitlement for company " +
                                company.getSourceSystemCompanyId() + ". " + t.getMessage(),
                        null,
                        null);

                EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(eu);
                entitlementUnitDTO.setErrorCount(entitlementUnitDTO.getErrorCount() + 1);
                if(entitlementUnitDTO.getErrorCount() >= maxRetries) {
                    entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorDeactivating);
                }
                ProcessResult processResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
                if(processResult.isSuccess()) {
                    CompanyEvent.createEntitlementErrorEvent(company,
                            EventTypeCode.EntitlementCommunication,
                            "Error activating entitlement unit for entitlement " +
                                    eu.getEntitlement() + ": " + t.getMessage(),
                            eu.getEntitlement(),
                            eu);
                    if (manageTransaction){
                        PayrollServices.commitUnitOfWork();
                    }
                } else {
                    logger.error("Error updating entitlement unit " + eu.getId() + "to " + EntitlementUnitStatusCode.ErrorActivating + "\n" + processResult.toString());
                    if (manageTransaction){
                        PayrollServices.rollbackUnitOfWork();
                    }
                }
            } finally {
                if (manageTransaction){
                    PayrollServices.rollbackUnitOfWork();
                }
            }
        }
        jobResult= String.valueOf(successfulCount);
        return jobResult;
    }
}
