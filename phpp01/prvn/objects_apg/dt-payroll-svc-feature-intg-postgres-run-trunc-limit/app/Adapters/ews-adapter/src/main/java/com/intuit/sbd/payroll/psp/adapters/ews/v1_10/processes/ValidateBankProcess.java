package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.EwsFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.LoggingUtils;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.PSPTransmission;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.TransmissionsList;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

/**
 * @author Jeff Jones
 */
public class ValidateBankProcess extends BaseProcess {

    private EwsValidateBank mRequest;
    private static final SpcfLogger logger;

    private TransmissionsList mTransmissionsList;
    private PSPTransmission mPSPTransmission;

    static {
        logger = PayrollServices.getLogger(CreateAccountProcess.class);
    }

    public ValidateBankProcess(EwsValidateBank pRequest) {
        mRequest = pRequest;
        this.mPSID = pRequest.getPsid();

        mTransmissionsList = new TransmissionsList();
        mPSPTransmission = new PSPTransmission();
        mTransmissionsList.add(mPSPTransmission);

        logger.info("Processing Validate_Bank Request / PSID: " + this.mPSID);
    }

    @Override
    public EwsBankResponse execute() {
        EwsBankResponse response = null;

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            mPSPTransmission.setInitializeDateTime(PSPDate.getPSPTime());
            mPSPTransmission.setTransmissionType(TransmissionType.ValidateBankAccount);
            mPSPTransmission.setRequest(mRequest);

            validate();
            response = process();

            PayrollServices.commitUnitOfWork();
        } catch (EwsException e) {
            response = new EwsBankResponse();
            processEwsException(e, response);

            if (response.getEwsResponseStatus().getCode() == 30104) {
                appendResponse(response);
            }
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

        if (PspFactory.isActiveOnService(mPspCompany, ServiceCode.Tax)) {
            if (mRequest.getEwsValidateBankServices().getEwsValidateBankAssistedService() == null) {
                throw new EwsException(EwsMessages.objectCanNotBeNull("Assisted"));
            }
        } else
        if (PspFactory.isActiveOnService(mPspCompany, ServiceCode.DirectDeposit)) {
            if (mRequest.getEwsValidateBankServices().getEwsValidateBankDirectDepositService() == null) {
                throw new EwsException(EwsMessages.objectCanNotBeNull("DirectDeposit"));
            }
        }
    }

    @Override
    protected EwsBankResponse process() throws Exception {

        EwsValidateBankAccount ewsValidateBankAccount;
        EwsValidateBankServices ewsValidateBankServices = mRequest.getEwsValidateBankServices();
        if (ewsValidateBankServices.getEwsValidateBankDirectDepositService() != null) {
            ewsValidateBankAccount = ewsValidateBankServices.getEwsValidateBankDirectDepositService().getEwsValidateBankAccount();
        } else {
            ewsValidateBankAccount = ewsValidateBankServices.getEwsValidateBankAssistedService().getEwsValidateBankAccount();
        }

        CompanyBankAccount companyBankAccount = PspFactory.findCompanyBankAccount(mPspCompany);

        SpcfMoney randomDollar1 = new SpcfMoney(ewsValidateBankAccount.getRandomDebit1());
        SpcfMoney randomDollar2 = new SpcfMoney(ewsValidateBankAccount.getRandomDebit2());
        ProcessResult<CompanyBankAccount> companyBankAccountPR = PayrollServices.companyManager.verifyCompanyBankAccount
                (SourceSystemCode.QBDT, mPspCompany.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(),
                        randomDollar1, randomDollar2, false);
        if (!companyBankAccountPR.isSuccess()) {
            //Commit psp validation logic
            PayrollServices.commitUnitOfWork();
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            MessageList messageList = companyBankAccountPR.getMessages();
            /*
             * as per security advisory we do not want bank id's in the logs
             * for (Message message : messageList) {
             *    logger.info("MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
             * }
             */
            throw new EwsException(EwsMessages.convertPSPMessage(messageList.get(0)));
        }

        return EwsFactory.createEwsBankResponse(companyBankAccountPR.getResult());
    }

    private void appendResponse(EwsBankResponse pEwsBankResponse) {
        try {
                CompanyBankAccount companyBankAccount = PspFactory.findCompanyBankAccount(mPspCompany);
            EwsBankServicesResponse ewsBankServicesResponse = EwsFactory.createEwsBankResponse(companyBankAccount).getEwsBankServicesResponse();

            pEwsBankResponse.setPsid(mPSID);
            pEwsBankResponse.setEwsBankServicesResponse(ewsBankServicesResponse);
        } catch (Throwable t){
            pEwsBankResponse = new EwsBankResponse();
            processThrowable(t, pEwsBankResponse);
        }
    }
}
