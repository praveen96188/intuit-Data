package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsBasePin;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsBasePinResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.EwsFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.LoggingUtils;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.PSPTransmission;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.TransmissionsList;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.Validation;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.log.MoneyMovementLogHelper;
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
                removeLoggerContext();
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
        setLoggerContext();
        mTransmissionsList.setCompanySeq(mPspCompany.getId().toString());

        if (mPspCompany.isPINCreated()) {
            throw new EwsException(EwsMessages.pinAlreadyExists());
        }
        moneyMovementTrackingLog(logger, MoneyMovementLogHelper.EventType.CreatePin,
                "Completed Validation");
    }

    @Override
    protected EwsBasePinResponse process() throws Exception {
        moneyMovementTrackingLog(logger, MoneyMovementLogHelper.EventType.CreatePin, "Started process");
        ProcessResult<HashMap<String,String>> createPinPR = PayrollServices.subscriptionManager.createCompanyPIN
                (SourceSystemCode.QBDT, mPSID, mRequest.getPin());
        if (!createPinPR.isSuccess()) {
            MessageList messageList = createPinPR.getMessages();
            for (Message message : messageList) {
                logger.info("MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
            }
            moneyMovementTrackingLog(logger, MoneyMovementLogHelper.EventType.CreatePin, "Error in process",messageList.get(0).getMessage());
            throw new EwsException(EwsMessages.convertPSPMessage(messageList.get(0)));
        }
        moneyMovementTrackingLog(logger, MoneyMovementLogHelper.EventType.CreatePin,
                "Completed process");
        return EwsFactory.createEwsBasePinResponse(mPspCompany, createPinPR.getResult());
    }
}
