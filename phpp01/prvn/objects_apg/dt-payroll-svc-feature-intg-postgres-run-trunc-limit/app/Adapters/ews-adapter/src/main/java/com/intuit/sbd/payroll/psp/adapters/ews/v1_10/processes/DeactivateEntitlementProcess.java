package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsDeactivateEntitlement;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.LoggingUtils;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.PSPTransmission;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.TransmissionsList;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.AssetItemCode;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnitStatusCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;
import java.util.List;

/**
 * User: praveenkumarh635
 */
public class DeactivateEntitlementProcess extends BaseProcess {

    private EwsDeactivateEntitlement mRequest;
    private static final SpcfLogger logger;

    private TransmissionsList mTransmissionsList;
    private PSPTransmission mPSPTransmission;
    private List<String> mEins;
    private DomainEntitySet<EntitlementUnit> entitlementUnitsToBeDeactivated;

    static {
        logger = PayrollServices.getLogger(DeactivateEntitlementProcess.class);
    }

    public DeactivateEntitlementProcess(EwsDeactivateEntitlement pRequest) {
        mRequest = pRequest;

        this.mEins = pRequest.getEins();
        mTransmissionsList = new TransmissionsList();
        mPSPTransmission = new PSPTransmission();
        mTransmissionsList.add(mPSPTransmission);
        mSubscriptionNumber = pRequest.getSubscriptionNumber();
        entitlementUnitsToBeDeactivated = new DomainEntitySet<EntitlementUnit>();
        logger.info("Processing Deactivate_Entitlement Request having Subscription Number : " + pRequest.getSubscriptionNumber());
    }

    @Override
    public EwsResponse execute() {

        EwsResponse response = null;
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            mPSPTransmission.setInitializeDateTime(PSPDate.getPSPTime());
            mPSPTransmission.setTransmissionType(TransmissionType.EntitlementUnitDeactivation);
            mPSPTransmission.setRequest(mRequest);

            validate();
            response = process();

        } catch (EwsException e) {
            response = new EwsResponse();
            processEwsException(e, response);
        } catch (Throwable t) {
            response = new EwsResponse();
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

        DomainEntitySet<EntitlementUnit> entitlementUnits = PspFactory.findEntitlementUnitsBySubscriptionNumber(mSubscriptionNumber);
        if(entitlementUnits == null || entitlementUnits.isEmpty()){
            throw new EwsException(EwsMessages.fieldCanNotBeNullOrEmpty(entitlementUnits));
        }

        for(String ein : mEins){
            boolean validEin = false;
        for(EntitlementUnit entitlementUnit : entitlementUnits){
                if(entitlementUnit.getFedTaxId().equals(ein)){
                    if(entitlementUnit.isDeactivated()){
                        throw new EwsException(EwsMessages.einIsAlreadyDeactivated(entitlementUnit.getFedTaxId()));
                    }
                    if (entitlementUnit.getEntitlement().getEntitlementCode().getAssetItemCd().equals(AssetItemCode.Assisted) ||
                            entitlementUnit.getEntitlement().getEntitlementCode().getAssetItemCd().equals(AssetItemCode.AssistedAdvantage)) {
                        throw new EwsException(EwsMessages.assistedOrAssistedAdvantageEntitlementCannotDeactivated(entitlementUnit.getFedTaxId()));
                    }
                    validEin = true;
                    entitlementUnitsToBeDeactivated.add(entitlementUnit);
                    break;
                }
            }
            if(!validEin){
                throw new EwsException(EwsMessages.einDoesNotBelongToTheGivenSubscriptionNumber(ein, mSubscriptionNumber));
            }
        }


        DomainEntitySet<Company> companiesByEin = PspFactory.findCompaniesByEin(mEins.get(0));
        mPspCompany = companiesByEin.getFirst();
        if(mPspCompany != null){
            mTransmissionsList.setCompanySeq(mPspCompany.getId().toString());
        }
    }

    @Override
    protected EwsResponse process() throws Exception {
        EwsResponse response = new EwsResponse();

        try {
            for(EntitlementUnit entitlementUnit : entitlementUnitsToBeDeactivated){
                EntitlementUnitDTO dto = PayrollServices.dtoFactory.create(entitlementUnit);
                dto.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingDeactivation);
                ProcessResult pr = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(entitlementUnit.getCompany().getSourceSystemCd(), entitlementUnit.getCompany().getSourceCompanyId(), dto);

                if (!pr.isSuccess()) {
                    MessageList messageList = pr.getMessages();
                    for (Message message : messageList) {
                        logger.info("MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
                    }
                    throw new EwsException(EwsMessages.convertPSPMessage(messageList.get(0)));
                }
            }
            PayrollServices.commitUnitOfWork();

        } catch (Throwable t) {
            response = new EwsResponse();
            processThrowable(t, response);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return response;
    }
}
