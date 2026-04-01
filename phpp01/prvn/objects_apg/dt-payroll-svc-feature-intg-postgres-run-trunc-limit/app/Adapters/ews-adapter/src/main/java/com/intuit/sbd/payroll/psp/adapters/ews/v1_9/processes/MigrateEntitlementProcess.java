package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsEntitlement;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsMigrateEntitlement;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsMigrateEntitlementResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.EwsFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.LoggingUtils;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.PSPTransmission;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.TransmissionsList;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

/**
 * @author Jeff Jones
 */
public class MigrateEntitlementProcess extends BaseProcess {

    private EwsMigrateEntitlement mRequest;
    private static final SpcfLogger logger;

    private TransmissionsList mTransmissionsList;
    private PSPTransmission mPSPTransmission;

    static {
        logger = PayrollServices.getLogger(MigrateAccountProcess.class);
    }

    public MigrateEntitlementProcess(EwsMigrateEntitlement pRequest) {
        mRequest = pRequest;

        this.mPSID = pRequest.getPsid();

        mTransmissionsList = new TransmissionsList();
        mPSPTransmission = new PSPTransmission();
        mTransmissionsList.add(mPSPTransmission);

        logger.info("Processing Migrate_Entitlement Request / PSID: " + this.mPSID);
    }

    @Override
    public EwsMigrateEntitlementResponse execute() {
        EwsMigrateEntitlementResponse response = null;

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            mPSPTransmission.setInitializeDateTime(PSPDate.getPSPTime());
            mPSPTransmission.setTransmissionType(TransmissionType.MigrateEntitlement);
            mPSPTransmission.setRequest(mRequest);

            validate();
            response = process();
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            mPspCompany = Application.refresh(mPspCompany);
            response.setEwsEntitlementUnitResponses(EwsFactory.createEwsEntitlementUnitResponses(mPspCompany.getPrimaryEntitlementUnits()));
            PayrollServices.rollbackUnitOfWork();
        } catch (EwsException e) {
            response = new EwsMigrateEntitlementResponse();
            processEwsException(e, response);
        } catch (Throwable t) {
            response = new EwsMigrateEntitlementResponse();
            processThrowable(t, response);
        } finally {
            PayrollServices.rollbackUnitOfWork();

            try {
                mPSPTransmission.setFinalizeDateTime(PSPDate.getPSPTime());
                mPSPTransmission.setResponse(response);

                LoggingUtils.logTransmissions(mTransmissionsList);
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }

        return response;
    }

    @Override
    protected void validate() throws Exception {
        mRequest.validate();

        mPspCompany = PspFactory.findCompany(mPSID);
        mTransmissionsList.setCompanySeq(mPspCompany.getId().toString());

        for (EwsEntitlement ewsEntitlement : mRequest.getEwsEntitlements()) {
            if (!isAssetItemNumberAssisted(ewsEntitlement.getAssetItemNumber())) {
                if (ewsEntitlement.getEdition() == null) {
                    throw new EwsException(EwsMessages.fieldCanNotBeNullOrEmpty("Edition", "Entitlement"));
                }
                if (ewsEntitlement.getTier() == null) {
                    throw new EwsException(EwsMessages.fieldCanNotBeNullOrEmpty("Tier", "Entitlement"));
                }
            }
        }
    }

    @Override
    protected EwsMigrateEntitlementResponse process() throws Exception {
        EwsMigrateEntitlementResponse response = new EwsMigrateEntitlementResponse();

        response.setPsid(mPspCompany.getSourceCompanyId());

        //Only Primary Entitlements are migrated via the EWS Adapter.
        EntitlementUnit oldEntitlementUnit = mPspCompany.getActivePrimaryEntitlementUnit();
        if (oldEntitlementUnit == null) {
            oldEntitlementUnit = mPspCompany.getPrimaryEntitlementUnits().sort(EntitlementUnit.<EntitlementUnit>ModifiedDate().Descending()).getFirst();
        }

        //Add Entitlements
        for (EwsEntitlement ewsEntitlement : mRequest.getEwsEntitlements()) {
            EntitlementUnitDTO entitlementUnitDTO = PspFactory.createEntitlementUnitDTO(mPspCompany, ewsEntitlement);

            if (oldEntitlementUnit == null || oldEntitlementUnit.isDeactivated()) {
                ProcessResult<EntitlementUnit> migratePR =
                        PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(mPspCompany.getSourceSystemCd(), mPspCompany.getSourceCompanyId(), entitlementUnitDTO);
                if (!migratePR.isSuccess()) {
                    MessageList messageList = migratePR.getMessages();
                    for (Message message : messageList) {
                        logger.info("MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
                    }
                    throw new EwsException(EwsMessages.convertPSPMessage(messageList.get(0)));
                }
            } else {
            //Skip this work if its already been done via a AMO message
            if (!areEntitlementsEqual(oldEntitlementUnit.getEntitlement(), entitlementUnitDTO)) {
                ProcessResult<Entitlement> migratePR =
                        PayrollServices.entitlementManager.migrateEntitlement(oldEntitlementUnit.getEntitlement(), entitlementUnitDTO);
                if (!migratePR.isSuccess()) {
                    MessageList messageList = migratePR.getMessages();
                    for (Message message : messageList) {
                        logger.info("MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
                    }
                    throw new EwsException(EwsMessages.convertPSPMessage(messageList.get(0)));
                }
            }
        }
        }

        return response;
    }

    private boolean isAssetItemNumberAssisted(String pAssetItemNumber) {
        Expression<EntitlementCode> query =
                new Query<EntitlementCode>()
                       .Where(EntitlementCode.AssetItemNumber().equalTo(pAssetItemNumber)
                       .And(EntitlementCode.AssetTypeCd().equalTo(AssetTypeCode.Payroll)));

        DomainEntitySet<EntitlementCode> entitlementCodes  =  Application.find(EntitlementCode.class, query);

        return !entitlementCodes.isEmpty() && entitlementCodes.get(0).isAssisted();
    }

    private boolean areEntitlementsEqual(Entitlement pEntitlement, EntitlementUnitDTO pEntitlementUnitDTO) {
        if (pEntitlement == null && pEntitlementUnitDTO == null) {
            return true;
        } else
        if (pEntitlement == null || pEntitlementUnitDTO == null) {
            return false;
        }

        return pEntitlement.getLicenseNumber().equals(pEntitlementUnitDTO.getLicenseNumber()) &&
                pEntitlement.getEntitlementOfferingCode().equals(pEntitlementUnitDTO.getEntitlementOfferingCode()) &&
                pEntitlement.getEntitlementCode().getAssetItemNumber().equals(pEntitlementUnitDTO.getAssetItemNumber()) &&
                pEntitlement.getEntitlementCode().getEditionType().equals(pEntitlementUnitDTO.getEditionType()) &&
                pEntitlement.getEntitlementCode().getNumberOfEmployeesType().equals(pEntitlementUnitDTO.getNumberOfEmployeesType());
    }
}
