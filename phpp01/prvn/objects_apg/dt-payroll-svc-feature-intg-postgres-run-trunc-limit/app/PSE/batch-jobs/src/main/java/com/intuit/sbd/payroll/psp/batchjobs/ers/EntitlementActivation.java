package com.intuit.sbd.payroll.psp.batchjobs.ers;

import com.intuit.ems.payroll.psp.gateways.ers.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnit;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 21, 2010
 * Time: 12:47:21 PM
 */
public class EntitlementActivation {
    private static SpcfLogger logger = Application.getLogger(EntitlementActivation.class);

    private int batchSize;
    private int maxRetries;
    private PSPRequestContextManager pspRequestContextManager;

    public EntitlementActivation() {
        batchSize = SystemParameter.findIntValue(SystemParameter.Code.ERS_BATCH_SIZE, 500);
        maxRetries = SystemParameter.findIntValue(SystemParameter.Code.ERS_MAX_REQUEST_RETRIES, 5);
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    public void execute() {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.ERSBatchJob));
        IERSGateway ersGateway = ERSGatewayFactory.createInstance();

        if(ersGateway == null) {
            logger.error("Could not connect to ERS.");
            return;
        }

        StopWatch stopWatch = new StopWatch().start();

        DomainEntitySet<EntitlementUnit> entitlementUnitsToActivate;
        int entitlementUnitsActivationSkipped = 0;
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            Criterion criterion = EntitlementUnit.Entitlement().LicenseNumber().isNotNull()
                    .And(EntitlementUnit.Entitlement().EntitlementOfferingCode().isNotNull())
                    .And(EntitlementUnit.EntitlementUnitStatus().equalTo(EntitlementUnitStatusCode.PendingActivation)
                            .Or(EntitlementUnit.EntitlementUnitStatus().equalTo(EntitlementUnitStatusCode.PendingReactivation)));

            // Exclude companies marked as DGDeleted
            if (Company.isDGDeleteFeatureEnabled()) {
                criterion = criterion.And(EntitlementUnit.Company().IsDgDisassociated().equalTo(Boolean.FALSE));
            }

            Expression<EntitlementUnit> query =
                    new Query<EntitlementUnit>()
                            .Where(criterion)
                            .OrderBy(EntitlementUnit.CreatedDate())
                            .LimitResults(0, batchSize);

            entitlementUnitsToActivate = Application.find(EntitlementUnit.class, query);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        SourceSystemCode sourceSystemCode;
        String companyId;
        for (EntitlementUnit entitlementUnit : entitlementUnitsToActivate) {
            sourceSystemCode = null;
            companyId = null;
            try {
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

                entitlementUnit = EntitlementUnit.findEntitlementUnit(entitlementUnit.getId());
                Company company = entitlementUnit.getCompany();
                pspRequestContextManager.setRequestContextCompany(company);
                sourceSystemCode = company.getSourceSystemCd();
                companyId = company.getSourceCompanyId();

                if (entitlementUnit.getEntitlement().hasPendingOrRecentMessages()) {
                    ++entitlementUnitsActivationSkipped;
                    continue;
                }

                EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);

                EntitlementInfoDTO entitlementInfoDTO = ersGateway.getEntitlementInfo(entitlementUnit.getEntitlement().getLicenseNumber(),
                                                                                      entitlementUnit.getEntitlement().getEntitlementOfferingCode(),
                                                                                      true,
                                                                                      new ERSListener(company, TransmissionType.EntitlementUnitActivation));

                if (EntitlementStateCode.Enabled.equals(entitlementInfoDTO.getEntitlementState())) {
                    EntitlementUnitInfoDTO entitlementUnitInfoDTO = entitlementInfoDTO.getEntitlementUnits().get(entitlementUnit.getFedTaxId());
                    if (entitlementUnitInfoDTO == null) {
                        ersGateway.activateEntitlement(entitlementUnit.getEntitlement().getLicenseNumber(),
                                                       entitlementUnit.getEntitlement().getEntitlementOfferingCode(),
                                                       entitlementUnit.getFedTaxId(),
                                                       false,
                                                       new ERSListener(company, TransmissionType.EntitlementUnitActivation));
                    } else {
                        if (EntitlementUnitStatusCode.Deactivated.equals(entitlementUnitInfoDTO.getEntitlementUnitStatusCode())) {
                            ersGateway.activateEntitlement(entitlementUnit.getEntitlement().getLicenseNumber(),
                                                           entitlementUnit.getEntitlement().getEntitlementOfferingCode(),
                                                           entitlementUnit.getFedTaxId(),
                                                           true,
                                                           new ERSListener(company, TransmissionType.EntitlementUnitActivation));
                        }
                    }

                    // update company entitlement status
                    entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
                    entitlementUnitDTO.setEntitlementState(EntitlementStateCode.Enabled);
                    entitlementUnitDTO.setErrorCount(0);
                } else if (EntitlementStateCode.Disabled.equals(entitlementInfoDTO.getEntitlementState())) {
                    // update company entitlement status
                    entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorActivating);
                    entitlementUnitDTO.setEntitlementState(EntitlementStateCode.Disabled);
                    entitlementUnitDTO.setErrorCount(0);
                } else {
                    logger.warn(String.format("ERS returned an unknown EntitlementStateCode. EntitlementStateCode:%s LicenseNumber:%s EntitlementOfferingCode:%s",
                            entitlementInfoDTO.getEntitlementState(), entitlementUnit.getEntitlement().getLicenseNumber(),
                            entitlementUnit.getEntitlement().getEntitlementOfferingCode()) );
                    entitlementUnitDTO.setErrorCount(entitlementUnitDTO.getErrorCount() + 1);
                    if(entitlementUnitDTO.getErrorCount() >= maxRetries) {
                        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorActivating);
                    }
                }

                ProcessResult processResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
                if(!processResult.isSuccess()) {
                    PayrollServices.rollbackUnitOfWork();
                    throw new RuntimeException(processResult.toString());
                }

                PayrollServices.commitUnitOfWork();
            } catch(ERSConnectionException e) {
                logger.error("Error connection to ERS.", e);
                // stop processing if we cannot connect to the ERS
                break;
            } catch (Throwable t) {
                PayrollServices.rollbackUnitOfWork();

                logger.error("Error enabling entitlement for company " + sourceSystemCode + ":" + companyId, t);
                
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                entitlementUnit = EntitlementUnit.findEntitlementUnit(entitlementUnit.getId());
                Company company = entitlementUnit.getCompany();
                EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
                entitlementUnitDTO.setErrorCount(entitlementUnitDTO.getErrorCount() + 1);
                if(entitlementUnitDTO.getErrorCount() >= maxRetries) {
                    entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorActivating);
                }
                ProcessResult processResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
                if(processResult.isSuccess()) {
                    CompanyEvent.createEntitlementErrorEvent(company,
                                                             EventTypeCode.EntitlementCommunication,
                                                             "Error activating entitlement unit for entitlement " +
                                                                     entitlementUnit.getEntitlement() + ": " + t.getMessage(),
                                                             entitlementUnit.getEntitlement(),
                                                             entitlementUnit);
                    PayrollServices.commitUnitOfWork();
                } else {
                    logger.error("Error updating entitlement unit " + entitlementUnit.getId() + "to " + EntitlementUnitStatusCode.ErrorActivating + "\n" + processResult.toString());
                    PayrollServices.rollbackUnitOfWork();
                }
            } finally {
                PayrollServices.rollbackUnitOfWork();
                pspRequestContextManager.clearRequestContextCompany();
            }
        }
        logger.info("Entitlement raised for activation = "+entitlementUnitsToActivate.size()+". Completed submitting activated entitlements = " +( entitlementUnitsToActivate.size()-entitlementUnitsActivationSkipped ) + " entitlement units in " + stopWatch.getElapsedTimeString());
    }
}
