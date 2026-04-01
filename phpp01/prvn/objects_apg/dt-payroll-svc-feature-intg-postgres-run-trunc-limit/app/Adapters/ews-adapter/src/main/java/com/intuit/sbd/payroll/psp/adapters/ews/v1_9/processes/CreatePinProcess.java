package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsBasePin;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsBasePinResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.EwsFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.LoggingUtils;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.PSPTransmission;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.TransmissionsList;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.Validation;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.util.HashMap;

/**
 * @author Jeff Jones
 */
public class CreatePinProcess extends BaseProcess {

    private EwsBasePin mRequest;
    private static final SpcfLogger logger;

    private TransmissionsList mTransmissionsList;
    private PSPTransmission mPSPTransmission;

    static {
        logger = PayrollServices.getLogger(CreateAccountProcess.class);
    }

    public CreatePinProcess(EwsBasePin pRequest) {
        mRequest = pRequest;
        this.mPSID = pRequest.getPsid();

        mTransmissionsList = new TransmissionsList();
        mPSPTransmission = new PSPTransmission();
        mTransmissionsList.add(mPSPTransmission);

        logger.info("Processing Create_Pin Request / PSID: " + this.mPSID);
    }

    @Override
    public EwsBasePinResponse execute() {
        EwsBasePinResponse response = null;

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            mPSPTransmission.setInitializeDateTime(PSPDate.getPSPTime());
            mPSPTransmission.setTransmissionType(TransmissionType.CreatePIN);
            mPSPTransmission.setRequest(mRequest);

            validate();
            response = process();

            PayrollServices.commitUnitOfWork();
        } catch (EwsException e) {
            response = new EwsBasePinResponse();
            processEwsException(e, response);
        } catch (Throwable t){
            response = new EwsBasePinResponse();
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

        if (!Validation.validateValue(mRequest.getPin(), false, "^(\\P{M}\\p{M}*){8,12}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("Pin", "BasePin"));
        }

        mPspCompany = PspFactory.findCompany(mPSID);
        mTransmissionsList.setCompanySeq(mPspCompany.getId().toString());

        if (mPspCompany.isPINCreated()) {
            throw new EwsException(EwsMessages.pinAlreadyExists());
        }
    }

    @Override
    protected EwsBasePinResponse process() throws Exception {

        ProcessResult<HashMap<String,String>> createPinPR = PayrollServices.subscriptionManager.createCompanyPIN
                (SourceSystemCode.QBDT, mPSID, mRequest.getPin());
        if (!createPinPR.isSuccess()) {
            MessageList messageList = createPinPR.getMessages();
            for (Message message : messageList) {
                logger.info("MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
            }
            throw new EwsException(EwsMessages.convertPSPMessage(messageList.get(0)));
        }

        return EwsFactory.createEwsBasePinResponse(mPspCompany, createPinPR.getResult());
    }
}
