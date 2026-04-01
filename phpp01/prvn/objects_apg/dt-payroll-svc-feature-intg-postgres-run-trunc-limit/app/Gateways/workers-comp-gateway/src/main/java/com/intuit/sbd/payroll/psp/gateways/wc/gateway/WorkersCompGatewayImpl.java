package com.intuit.sbd.payroll.psp.gateways.wc.gateway;

import com.intuit.bp.wc.common.schema.WorkersCompPayrollUploadResponse;
import com.intuit.bp.wc.common.schema.WorkersCompSubscriptions;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.wc.dto.PayrollDTO;
import com.intuit.sbd.payroll.psp.gateways.wc.dto.WCChangeEventDTO;
import com.intuit.sbd.payroll.psp.gateways.wc.dto.WCChangeEventResponseDTO;
import com.intuit.sbd.payroll.psp.gateways.wc.manager.WorkersCompGatewayManager;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.config.SpcfConfigEntryMissingException;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gateway for Workers comp service, to upload payroll
 * Author: Sriram Nutakki
 * Date created: 10/24/12
 */
public class WorkersCompGatewayImpl implements WorkersCompGateway {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(WorkersCompGatewayImpl.class);

    public void getSubscriptionChangesFromWC() {

        // Get subscription changes from Workers comp service.
        WorkersCompSubscriptions subscriptions = WorkersCompServiceDelegate.getSubscriptionChanges();

        // Store changes
        WorkersCompGatewayManager manager = new WorkersCompGatewayManager();
        WorkersCompSubscriptions savedSubscriptions = manager.saveSubscriptionChanges(subscriptions);

        // Send confirmation
        if (savedSubscriptions != null &&
                savedSubscriptions.getWorkersCompSubscription() != null &&
                savedSubscriptions.getWorkersCompSubscription().size() > 0) {
            logger.info("Added/Updated/Deactivated/Reactivated WorkersCompSubscription , number of such subscriptions are : "+savedSubscriptions.getWorkersCompSubscription().size());
            WorkersCompServiceDelegate.postSubscriptionConfirmation(savedSubscriptions);
        } else {
            if(savedSubscriptions == null) {
                logger.debug("Unable to post subscription confirmation, savedSubscriptions is null");
            } else if(savedSubscriptions.getWorkersCompSubscription() == null) {
                logger.debug("Unable to post subscription confirmation, savedSubscriptions.getWorkersCompSubscription() is null");
            } else {
                logger.debug("Unable to post subscription confirmation, number of subscriptions to save is 0 or less");
            }
        }
    }

    public void pushPayrollDataToWC() {

        // Get companies
        WorkersCompGatewayManager manager = new WorkersCompGatewayManager();
        List<Set<SpcfUniqueId>> batches = manager.getCompaniesWithPendingPaychecks();

        // Process one batch at a time
        if (batches != null && batches.size() > 0) {
            for (Set<SpcfUniqueId> set : batches) {
                logger.info("pushPayrollDataToWC() batch: ");
                // Get payroll
                PayrollDTO dto = manager.getPendingPaychecks(new HashSet<SpcfUniqueId>(set));
                // Push payroll to WC service
                if (dto != null
                        && dto.getPayroll() != null
                        && dto.getPayroll().getBusinesses() != null
                        && dto.getPayroll().getBusinesses().getItem() != null
                        && dto.getPayroll().getBusinesses().getItem().size() > 0)
                {
                    WorkersCompPayrollUploadResponse response = WorkersCompServiceDelegate.uploadPayroll(dto.getPayroll());
                    if (response != null && response.getProcessedCompanyIds() != null) {
                        for (String companyId : response.getProcessedCompanyIds()) {
                            List<WorkersCompPaycheck> paychecks = dto.getIncludedPaychecks(companyId);
                            if (paychecks != null && paychecks.size() > 0) {
                                manager.markAsSent(paychecks);
                            }
                        }
                    }
                }
            }
        }
    }

    public String getDisplayDataForHelpDesk(String sourceSystemCd, String sourceCompanyId) {
        return WorkersCompServiceDelegate.getDisplayDataForHelpDesk(sourceSystemCd, sourceCompanyId);
    }

    public void pushCompanyChanges(){
        try {
            WorkersCompGatewayManager manager = new WorkersCompGatewayManager();
            // get start time
            long startTime = manager.getToken();

            // get EINChanged and PSIDChanged events
            long endTime = PSPDate.getPSPTime().getTimeInMilliseconds();
            List<WCChangeEventDTO> companyChangeEvents = manager.getCompanyChangeEvents(startTime, endTime);
            if (companyChangeEvents != null) {
                logger.info("pushCompanyChanges(): company events to be sent to WC: " + companyChangeEvents.size());
            }

            // inform WC service about the changes
            WCChangeEventResponseDTO responseDTO = WorkersCompServiceDelegate.postChangeEvents(companyChangeEvents);

            if (responseDTO != null) {
                logger.info("pushCompanyChanges(): response from WC: " + responseDTO.toString());
            }

            // go through response and mark unprocessed ones for resending, for now doing very basic checking - TODO
            if (responseDTO == null || responseDTO.getProcessedEvents() == null || responseDTO.getProcessedEvents().isEmpty()){
                logger.info("pushCompanyChanges(): none of the events were processed by WC hence not updating token");
                return;
            }

            // store the token
            manager.updateToken(endTime);
        } catch (Exception ex) {
            logger.error("Error pushing change events to WC...will try sending them when batch job runs again", ex);
        }
    }


}
