package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsBankResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsBillingDetails;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsUpdateBillingDetails;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.LoggingUtils;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.PSPTransmission;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.TransmissionsList;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementDTO;
import com.intuit.sbd.payroll.psp.domain.Entitlement;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

/**
 * @author Jeff Jones
 */
public class UpdateBillingDetailsProcess extends BaseProcess {

    EwsUpdateBillingDetails mRequest;
    private static final SpcfLogger logger;

    private TransmissionsList mTransmissionsList;
    private PSPTransmission mPSPTransmission;

    static {
        logger = PayrollServices.getLogger(UpdateBillingDetailsProcess.class);
    }

    public UpdateBillingDetailsProcess(EwsUpdateBillingDetails pRequest) {
        mRequest = pRequest;
        this.mPSID = pRequest.getPsid();

        mTransmissionsList = new TransmissionsList();
        mPSPTransmission = new PSPTransmission();
        mTransmissionsList.add(mPSPTransmission);

        logger.info("Processing Update_Billing_Details Request / PSID: " + this.mPSID);
    }

    @Override
    public EwsResponse execute() {
        EwsResponse response = null;

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            mPSPTransmission.setInitializeDateTime(PSPDate.getPSPTime());
            mPSPTransmission.setTransmissionType(TransmissionType.UpdateBillingDetails);
            mPSPTransmission.setRequest(mRequest);

            validate();
            response = process();

            PayrollServices.commitUnitOfWork();
        } catch (EwsException e) {
            response = new EwsBankResponse();
            processEwsException(e, response);
        } catch (Throwable t){
            response = new EwsBankResponse();
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
        mEIN = mPspCompany.getFedTaxId();
    }

    @Override
    protected EwsResponse process() throws Exception {
        EwsResponse response = new EwsResponse();

        Entitlement entitlement = Entitlement.findEntitlement(mRequest.getLicenseNumber(),
                mRequest.getEntitlementOfferingCode());
        EntitlementDTO entitlementDTO = PayrollServices.dtoFactory.create(entitlement);

        EwsBillingDetails ewsBillingDetails = mRequest.getEwsBillingDetails();
        PspFactory.updateEntitlementDTO(ewsBillingDetails, entitlementDTO);

        ProcessResult<Entitlement> entitlementPR = PayrollServices.entitlementManager.updateEntitlement(entitlementDTO);

        if (!entitlementPR.isSuccess()) {
            MessageList messageList = entitlementPR.getMessages();
            Message msg = messageList.get(0);
            throw new EwsException(EwsMessages.convertPSPMessage(msg));
        }

        return response;
    }
}
