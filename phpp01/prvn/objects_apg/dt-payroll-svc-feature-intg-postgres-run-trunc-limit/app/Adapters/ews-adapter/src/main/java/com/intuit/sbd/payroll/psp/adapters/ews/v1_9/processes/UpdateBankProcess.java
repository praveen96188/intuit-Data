package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.as400.PSIMessageWSDTO;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsBankAccount;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsBankResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsUpdateBank;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsBankAccountType;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.As400Factory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.EwsFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.LoggingUtils;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.PSPTransmission;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.TransmissionsList;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

/**
 * @author Jeff Jones
 */
public class UpdateBankProcess extends BaseProcess {

    private EwsUpdateBank mRequest;
    private static final SpcfLogger logger;

    private TransmissionsList mTransmissionsList;
    private PSPTransmission mPSPTransmission;

    static {
        logger = PayrollServices.getLogger(CreateAccountProcess.class);
    }

    public UpdateBankProcess(EwsUpdateBank pRequest) {
        mRequest = pRequest;
        this.mPSID = pRequest.getPsid();

        mTransmissionsList = new TransmissionsList();
        mPSPTransmission = new PSPTransmission();
        mTransmissionsList.add(mPSPTransmission);

        logger.info("Processing Update_Bank Request / PSID: " + this.mPSID);
    }

    @Override
    public EwsBankResponse execute() {
        EwsBankResponse response = null;

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            mPSPTransmission.setInitializeDateTime(PSPDate.getPSPTime());
            mPSPTransmission.setTransmissionType(TransmissionType.UpdateBankAccount);
            mPSPTransmission.setRequest(mRequest);

            validate();
            response = process();

//            if (PspFactory.isActiveOnService(mPspCompany, ServiceCode.Tax)) {
//                PSIMessageWSDTO psiMessageRequest = As400Factory.createUpdateBank(mRequest, mPspCompany);
//                mTransmissionsList.add(initializeAS400Transmission(TransmissionType.UpdateBankAccount, psiMessageRequest));
//
//                PSIMessageWSDTO  psiMessageResponse;
//                AS400Process as400Process = new AS400Process(psiMessageRequest);
//                try {
//                    as400Process.execute();
//                } finally {
//                    psiMessageResponse = as400Process.getResponse();
//                    finalizeAS400Transmission(psiMessageResponse);
//                }
//            }

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
    }

    @Override
    protected EwsBankResponse process() throws Exception {

        EwsBankAccount ewsBankAccount;
        if (mRequest.getEwsUpdateBankServices().getEwsUpdateBankDirectDepositService() != null) {
            ewsBankAccount = mRequest.getEwsUpdateBankServices().getEwsUpdateBankDirectDepositService().getEwsBankAccount();
        } else {
            ewsBankAccount = mRequest.getEwsUpdateBankServices().getEwsUpdateBankAssistedService().getEwsBankAccount();
        }

        CompanyBankAccount companyBankAccount =  PspFactory.findCompanyBankAccount(mPspCompany);
        CompanyBankAccountDTO companyBankAccountDTO = PspFactory.createCompanyBankAccountDTO(ewsBankAccount, companyBankAccount);

        boolean changeBankAccount = isBankAccountDifferent(ewsBankAccount, companyBankAccount);

        ProcessResult<CompanyBankAccount> companyBankAccountPR;
        if (changeBankAccount) {
            companyBankAccountPR = PayrollServices.companyManager.changeCompanyBankAccount
                    (SourceSystemCode.QBDT, mPSID, companyBankAccountDTO, true, false, false);
        } else {
            companyBankAccountPR = PayrollServices.companyManager.updateCompanyBankAccount
                    (SourceSystemCode.QBDT, mPSID, companyBankAccountDTO);
        }

        if (!companyBankAccountPR.isSuccess()) {
            MessageList messageList = companyBankAccountPR.getMessages();
            Message msg = messageList.get(0);
            if (msg.getMessageCode().equals("255")) {
                throw new EwsException(EwsMessages.fieldDataNotValid("RoutingNumber", "BankAccount"));
            }
            /*
             * per security advisory we do not want bank id's in the log
             * for (Message message : messageList) {
             *   logger.info("MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
             *}
             */
            throw new EwsException(EwsMessages.convertPSPMessage(msg));
        }

        companyBankAccount = companyBankAccountPR.getResult();
        if (changeBankAccount) {
            if (mRequest.getForceRandomDollar()) {
                OverrideRandomDebits(companyBankAccount);
            }
        }

        return EwsFactory.createEwsBankResponse(companyBankAccount);
    }

    private boolean isBankAccountDifferent(EwsBankAccount pEwsBankAccount, CompanyBankAccount pCompanyBankAccount) {
        BankAccount bankAccount = pCompanyBankAccount.getBankAccount();

        return !((bankAccount.getAccountNumber().equals(pEwsBankAccount.getAccountNumber())) &&
               (bankAccount.getRoutingNumber().equals(pEwsBankAccount.getRoutingNumber())) &&
               ((bankAccount.getAccountTypeCd().equals(BankAccountType.Checking)) &&
               (pEwsBankAccount.getAccountType().equals(EwsBankAccountType.Checking)) ||
               (bankAccount.getAccountTypeCd().equals(BankAccountType.Savings)) &&
               (pEwsBankAccount.getAccountType().equals(EwsBankAccountType.Savings))));
    }
}
