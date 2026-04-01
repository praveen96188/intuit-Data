package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.EwsFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.LoggingUtils;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.PSPTransmission;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.TransmissionsList;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.Validation;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

/**
 * @author Marcela Villani
 */
public class MigrateAccountProcess extends BaseProcess {
    private EwsMigrateAccount mRequest;
    private DomainEntitySet<EntitlementUnit> mEntitlementUnits;
    private static final SpcfLogger logger;

    private TransmissionsList mTransmissionsList;
    private PSPTransmission mPSPTransmission;

    static {
        logger = PayrollServices.getLogger(MigrateAccountProcess.class);
    }

    public MigrateAccountProcess(EwsMigrateAccount pRequest) {
        mRequest = pRequest;

        this.mPSID = pRequest.getPsid();
        this.mEIN = pRequest.getEwsCompany().getEin();

        mTransmissionsList = new TransmissionsList();
        mPSPTransmission = new PSPTransmission();
        mTransmissionsList.add(mPSPTransmission);

        mEntitlementUnits = new DomainEntitySet<EntitlementUnit>();

        logger.info("Processing Migrate_Account Request / PSID: " + this.mPSID);
    }

    @Override
    public EwsMigrateAccountResponse execute() {
        EwsMigrateAccountResponse response = null;

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            mPSPTransmission.setInitializeDateTime(PSPDate.getPSPTime());
            mPSPTransmission.setTransmissionType(TransmissionType.MigrateAccount);
            mPSPTransmission.setRequest(mRequest);

            validate();
            response = process();

            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            mPspCompany = PspFactory.findCompany(mPSID);
            response.setEwsEntitlementUnitResponses(EwsFactory.createEwsEntitlementUnitResponses(mPspCompany.getPrimaryEntitlementUnits()));
            PayrollServices.rollbackUnitOfWork();

        } catch (EwsException e) {
            response = new EwsMigrateAccountResponse();
            processEwsException(e, response);
        } catch (Throwable t) {
            response = new EwsMigrateAccountResponse();
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

        //Service Specific Validation
        EwsCompany ewsCompany = mRequest.getEwsCompany();
        EwsServices ewsServices = mRequest.getEwsServices();
        if (ewsServices.getAssistedService() != null) {
            //Assisted Customer

            if (mRequest.getEwsCompany().getLegalInfo() == null) {
                throw new EwsException(EwsMessages.objectCanNotBeNull("LegalInfo"));
            }
        } else if (ewsServices.getDirectDepositService() != null) {
            //DD Customer

            if (mRequest.getEwsCompany().getLegalInfo() == null) {
                throw new EwsException(EwsMessages.objectCanNotBeNull("LegalInfo"));
            }

            if (!Validation.validateValue(ewsCompany.getDba(), false, "^(\\P{M}\\p{M}*){1,100}$")) {
                throw new EwsException(EwsMessages.fieldDataNotValid("DBA", "Company"));
            }

            if (mRequest.getEwsCompany().getMailingAddress() == null) {
                throw new EwsException(EwsMessages.objectCanNotBeNull("MailingAddress"));
            }

            for (EwsEntitlement ewsEntitlement : mRequest.getEwsEntitlements()) {
                if (ewsEntitlement.getEdition() == null) {
                    throw new EwsException(EwsMessages.fieldCanNotBeNullOrEmpty("Edition", "Entitlement"));
                }
                if (ewsEntitlement.getTier() == null) {
                    throw new EwsException(EwsMessages.fieldCanNotBeNullOrEmpty("Tier", "Entitlement"));
                }
            }
        } else {
            //DIY Only
            if (!Validation.validateValue(ewsCompany.getDba(), false, "^(\\P{M}\\p{M}*){1,100}$")) {
                throw new EwsException(EwsMessages.fieldDataNotValid("DBA", "Company"));
            }

            ewsCompany.getMailingAddress().validate();

            for (EwsEntitlement ewsEntitlement : mRequest.getEwsEntitlements()) {
                if (ewsEntitlement.getEdition() == null) {
                    throw new EwsException(EwsMessages.fieldCanNotBeNullOrEmpty("Edition", "Entitlement"));
                }
                if (ewsEntitlement.getTier() == null) {
                    throw new EwsException(EwsMessages.fieldCanNotBeNullOrEmpty("Tier", "Entitlement"));
                }

                if (ewsEntitlement.getEwsBillingDetails() != null) {
                    ewsEntitlement.getEwsBillingDetails().validate();
                }
            }
        }

        //Agent Exclusions
        if (mRequest.getEwsCompany().getQuickBooks() != null) {
            mRequest.getEwsCompany().getQuickBooks().validate();
        } else {
            PspPrincipal principal = Application.getCurrentPrincipal();
            if (!principal.isAgent()) {
                throw new EwsException(EwsMessages.objectCanNotBeNull("QuickBooks"));
            }
        }

    }

    @Override
    protected EwsMigrateAccountResponse process() throws Exception {
        EwsMigrateAccountResponse response = new EwsMigrateAccountResponse();

        Company pspCompany = Company.findCompany(mRequest.getPsid(), SourceSystemCode.QBDT);

        //Update Company
        CompanyDTO companyDTO = PspFactory.updateCompanyDTO(mRequest.getEwsCompany(), pspCompany);
        ProcessResult<Company> companyPR = PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, companyDTO.getCompanyId(), companyDTO);
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
        mPSID = mPspCompany.getSourceCompanyId();

        response.setPsid(mPspCompany.getSourceCompanyId());
        response.setCompanyResponse(EwsFactory.createEwsCompanyResponse(mPspCompany));

        //Only Primary Entitlements are migrated via the EWS Adapter.
        EntitlementUnit oldEntitlementUnit = mPspCompany.getActivePrimaryEntitlementUnit();

        //Add Entitlements
        for (EwsEntitlement ewsEntitlement : mRequest.getEwsEntitlements()) {
            EntitlementUnitDTO entitlementUnitDTO = PspFactory.createEntitlementUnitDTO(mPspCompany, ewsEntitlement);
            ProcessResult<Entitlement> migratePR = PayrollServices.entitlementManager.migrateEntitlement(oldEntitlementUnit.getEntitlement(),                                                                                                                   entitlementUnitDTO);
            if (!migratePR.isSuccess()) {
                MessageList messageList = migratePR.getMessages();
                for (Message message : messageList) {
                    logger.info("MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
                }
                throw new EwsException(EwsMessages.convertPSPMessage(messageList.get(0)));
            }
            mEntitlementUnits.addAll(migratePR.getResult().getEntitlementUnitCollection());
        }

        //Services
        EwsServices ewsServices = mRequest.getEwsServices();
        EwsServicesResponse ewsServicesResponse = new EwsServicesResponse();
        response.setEwsServicesResponse(ewsServicesResponse);
        if (ewsServices.getAssistedService() != null) {
            addAssistedService(mRequest.getEwsCompany(), ewsServices, mEntitlementUnits, mRequest.getForceRandomDollar());
        }

        return response;
    }
}