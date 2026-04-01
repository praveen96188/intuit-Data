package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsBasePinResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsUpdatePin;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.EwsFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.LoggingUtils;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.PSPTransmission;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.TransmissionsList;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
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
public class UpdatePinProcess extends BaseProcess {

    private EwsUpdatePin mRequest;
    private static final SpcfLogger logger;

    private TransmissionsList mTransmissionsList;
    private PSPTransmission mPSPTransmission;

    static {
        logger = PayrollServices.getLogger(CreateAccountProcess.class);
    }

    public UpdatePinProcess(EwsUpdatePin pRequest) {
        mRequest = pRequest;
        this.mPSID = pRequest.getPsid();

        mTransmissionsList = new TransmissionsList();
        mPSPTransmission = new PSPTransmission();
        mTransmissionsList.add(mPSPTransmission);

        logger.info("Processing Update_Pin Request / PSID: " + this.mPSID);
    }

    @Override
    public EwsBasePinResponse execute() {
        EwsBasePinResponse response = null;

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            mPSPTransmission.setInitializeDateTime(PSPDate.getPSPTime());
            mPSPTransmission.setTransmissionType(TransmissionType.ChangePIN);
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

        mPspCompany = PspFactory.findCompany(mPSID);
        mTransmissionsList.setCompanySeq(mPspCompany.getId().toString());
    }

    @Override
    protected EwsBasePinResponse process() throws Exception {

        ProcessResult<Company> verifyPinPR = PayrollServices.subscriptionManager.verifyCompanyPIN
                (SourceSystemCode.QBDT, mPSID, mRequest.getOldPin());
        if (!verifyPinPR.isSuccess()) {
            //Commit psp validation logic
            PayrollServices.commitUnitOfWork();
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            MessageList messageList = verifyPinPR.getMessages();
            for (Message message : messageList) {
                logger.info("MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
            }
            throw new EwsException(EwsMessages.convertPSPMessage(messageList.get(0)));
        }
        mPspCompany = verifyPinPR.getResult();

        ProcessResult<HashMap<String,String>> updatePinPR = PayrollServices.subscriptionManager.updateCompanyPIN
                (SourceSystemCode.QBDT, mPSID, mRequest.getPin());
        if (!updatePinPR.isSuccess()) {
            //Commit psp validation logic
            PayrollServices.commitUnitOfWork();
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            MessageList messageList = updatePinPR.getMessages();
            for (Message message : messageList) {
                logger.info("MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
            }
            throw new EwsException(EwsMessages.convertPSPMessage(messageList.get(0)));
        }

        return EwsFactory.createEwsBasePinResponse(mPspCompany, updatePinPR.getResult());
    }
}
