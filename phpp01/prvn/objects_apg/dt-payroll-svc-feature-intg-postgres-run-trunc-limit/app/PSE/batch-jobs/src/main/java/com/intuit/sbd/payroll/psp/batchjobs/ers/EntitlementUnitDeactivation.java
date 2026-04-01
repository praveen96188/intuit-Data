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
 * Date: Jul 22, 2010
 * Time: 1:30:05 PM
 */
public class EntitlementUnitDeactivation {
    private static SpcfLogger logger = Application.getLogger(EntitlementUnitDeactivation.class);

    private int batchSize;
    private int maxRetries;
    private PSPRequestContextManager pspRequestContextManager;

    public EntitlementUnitDeactivation() {
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

        // only process one batch at a time
        boolean moreRecords = true;
        while (moreRecords) {
            StopWatch stopWatch = new StopWatch().start();

            DomainEntitySet<EntitlementUnit> entitlementUnitsToDeactivate;
            int entitlementUnitsDeactivationSkipped = 0;

            try {
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

                Criterion criterion = EntitlementUnit.EntitlementUnitStatus().equalTo(EntitlementUnitStatusCode.PendingDeactivation)
                        .And(EntitlementUnit.Entitlement().LicenseNumber().isNotNull())
                        .And(EntitlementUnit.Entitlement().EntitlementOfferingCode().isNotNull());

                // Exclude companies marked as DGDeleted
                if (Company.isDGDeleteFeatureEnabled()) {
                    criterion = criterion.And(EntitlementUnit.Company().IsDgDisassociated().equalTo(Boolean.FALSE));
                }

                Expression<EntitlementUnit> query =
                        new Query<EntitlementUnit>()
                                .Where(criterion)
                                .OrderBy(EntitlementUnit.CreatedDate())
                                .LimitResults(0, batchSize);

                entitlementUnitsToDeactivate = Application.find(EntitlementUnit.class, query);
                moreRecords = entitlementUnitsToDeactivate.size() == batchSize;
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }

            SourceSystemCode sourceSystemCode;
            String companyId;
            for (EntitlementUnit entitlementUnit : entitlementUnitsToDeactivate) {
                sourceSystemCode = null;
                companyId = null;
                try {
                    PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                    entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
                    Company company = entitlementUnit.getCompany();
                    pspRequestContextManager.setRequestContextCompany(company);
                    sourceSystemCode = company.getSourceSystemCd();
                    companyId = company.getSourceCompanyId();

                    if (entitlementUnit.getEntitlement().hasPendingOrRecentMessages()) {
                        ++entitlementUnitsDeactivationSkipped;
                        continue;
                    }

                    EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);

                    EntitlementInfoDTO entitlementInfoDTO = ersGateway.getEntitlementInfo(entitlementUnit.getEntitlement().getLicenseNumber(),
                                                                                          entitlementUnit.getEntitlement().getEntitlementOfferingCode(),
                                                                                          true,
                                                                                          new ERSListener(company, TransmissionType.EntitlementUnitActivation));
                    if (EntitlementStateCode.Enabled.equals(entitlementInfoDTO.getEntitlementState())) {
                        EntitlementUnitInfoDTO entitlementUnitInfoDTO = entitlementInfoDTO.getEntitlementUnits().get(entitlementUnit.getFedTaxId());
                        if (entitlementUnitInfoDTO != null && EntitlementUnitStatusCode.Activated.equals(entitlementUnitInfoDTO.getEntitlementUnitStatusCode())) {
                            ersGateway.deactivateEntitlementUnit(entitlementUnit.getEntitlement().getLicenseNumber(),
                                                                 entitlementUnit.getEntitlement().getEntitlementOfferingCode(),
                                                                 entitlementUnit.getFedTaxId(),
                                                                 new ERSListener(company, TransmissionType.EntitlementUnitDeactivation));
                        }
                        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
                        entitlementUnitDTO.setEntitlementState(EntitlementStateCode.Enabled);
                        entitlementUnitDTO.setErrorCount(0);
                    } else if (EntitlementStateCode.Disabled.equals(entitlementInfoDTO.getEntitlementState())) {
                        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorDeactivating);
                        entitlementUnitDTO.setEntitlementState(EntitlementStateCode.Disabled);
                        entitlementUnitDTO.setErrorCount(0);
                    } else {
                        logger.warn(String.format("ERS returned an unknown EntitlementStateCode. EntitlementStateCode:%s LicenseNumber:%s EntitlementOfferingCode:%s",
                                entitlementInfoDTO.getEntitlementState(), entitlementUnit.getEntitlement().getLicenseNumber(),
                                entitlementUnit.getEntitlement().getEntitlementOfferingCode()) );
                        entitlementUnitDTO.setErrorCount(entitlementUnitDTO.getErrorCount() + 1);
                        if(entitlementUnitDTO.getErrorCount() >= maxRetries) {
                            entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorDeactivating);
                        }
                    }

                    ProcessResult processResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);

                    if(processResult.isSuccess()) {
                        PayrollServices.commitUnitOfWork();
                    } else {
                        throw new RuntimeException(processResult.toString());
                    }

                } catch(ERSConnectionException e) {
                    logger.error("Error connection to ERS", e);
                    // stop processing if we cannot connect to the ERS
                    break;
                } catch (Throwable t) {
                    PayrollServices.rollbackUnitOfWork();

                    logger.error("Error deactivating entitlement for company " + sourceSystemCode + ":" + companyId, t);

                    PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                    entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
                    Company company = entitlementUnit.getCompany();
                    EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
                    entitlementUnitDTO.setErrorCount(entitlementUnitDTO.getErrorCount() + 1);
                    if(entitlementUnitDTO.getErrorCount() >= maxRetries) {
                        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorDeactivating);
                    }
                    ProcessResult processResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
                    if(processResult.isSuccess()) {
                        CompanyEvent.createEntitlementErrorEvent(company, EventTypeCode.EntitlementCommunication,
                                                                 "Error deactivating entitlement unit for entitlement " +
                                                                         entitlementUnit.getEntitlement() + ": " + t.getMessage(),
                                                                 entitlementUnit.getEntitlement(),
                                                                 entitlementUnit);
                        PayrollServices.commitUnitOfWork();
                    } else {
                        PayrollServices.rollbackUnitOfWork();
                    }
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                    pspRequestContextManager.clearRequestContextCompany();
                }
            }

            logger.info("Entitlement raised for deactivation = "+entitlementUnitsToDeactivate.size()+". Completed submitting deactivated entitlement = " +( entitlementUnitsToDeactivate.size()-entitlementUnitsDeactivationSkipped ) + " entitlement units in " + stopWatch.getElapsedTimeString());
        }
    }
}
