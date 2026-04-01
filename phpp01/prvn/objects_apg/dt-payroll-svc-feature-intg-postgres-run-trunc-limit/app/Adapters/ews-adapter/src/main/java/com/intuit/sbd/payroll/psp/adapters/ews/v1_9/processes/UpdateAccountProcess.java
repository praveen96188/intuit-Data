package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.as400.PSIMessageWSDTO;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsUpdateAccount;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsUpdateAccountResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.As400Factory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.EwsFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.LoggingUtils;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.PSPTransmission;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.TransmissionsList;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.OfferingInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.TaxServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

/**
 * @author Jeff Jones
 */
public class UpdateAccountProcess extends BaseProcess {

    private EwsUpdateAccount mRequest;
    private static final SpcfLogger logger;

    private TransmissionsList mTransmissionsList;
    private PSPTransmission mPSPTransmission;

    static {
        logger = PayrollServices.getLogger(CreateAccountProcess.class);
    }

    public UpdateAccountProcess(EwsUpdateAccount pRequest) {
        mRequest = pRequest;
        this.mPSID = pRequest.getPsid();

        mTransmissionsList = new TransmissionsList();
        mPSPTransmission = new PSPTransmission();
        mTransmissionsList.add(mPSPTransmission);

        logger.info("Processing Update_Account Request / PSID: " + this.mPSID);
    }

    @Override
    public EwsUpdateAccountResponse execute() {
        EwsUpdateAccountResponse response = null;

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            mPSPTransmission.setInitializeDateTime(PSPDate.getPSPTime());
            mPSPTransmission.setTransmissionType(TransmissionType.UpdateAccount);
            mPSPTransmission.setRequest(mRequest);

            validate();
            response = process();

            PayrollServices.commitUnitOfWork();
        } catch (EwsException e) {
            response = new EwsUpdateAccountResponse();
            processEwsException(e, response);
        } catch (Throwable t){
            response = new EwsUpdateAccountResponse();
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
    protected EwsUpdateAccountResponse process() throws Exception {
        EwsUpdateAccountResponse response = new EwsUpdateAccountResponse();

        //Update Company
        CompanyDTO companyDTO = PspFactory.updateCompanyDTO(mRequest.getEwsCompany(), mPspCompany);
        ProcessResult<Company> companyPR = PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, mPSID, companyDTO);
        if (!companyPR.isSuccess()) {
            MessageList messageList = companyPR.getMessages();
            for (Message message : messageList) {
                logger.info("MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
            }
            if (!messageList.get(0).getMessageCode().equals("1040")) {
                throw new EwsException(EwsMessages.convertPSPMessage(messageList.get(0)));
            }
        }
        mPspCompany = companyPR.getResult();

        CompanyService companyService = mPspCompany.getCompanyService(ServiceCode.Tax);
        if (companyService != null && !companyService.isCancelTerm()) {
            TaxServiceInfoDTO taxServiceInfoDTO = (TaxServiceInfoDTO) PayrollServices.dtoFactory.create(companyService);
            PspFactory.updateTaxServiceInfo(mRequest.getEwsCompany(), taxServiceInfoDTO);

            ProcessResult<CompanyService> companyServicePR = PayrollServices.companyManager.updateService
                    (mPspCompany.getSourceSystemCd(), mPspCompany.getSourceCompanyId(), taxServiceInfoDTO);
            if (!companyServicePR.isSuccess()) {
                MessageList messageList = companyPR.getMessages();
                for (Message message : messageList) {
                    logger.info("MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
                }
                throw new EwsException(EwsMessages.convertPSPMessage(messageList.get(0)));
            }
        }

        response.setPsid(mPspCompany.getSourceCompanyId());
        response.setCompanyResponse(EwsFactory.createEwsCompanyResponse(mPspCompany));
        
        return response;
    }
}
