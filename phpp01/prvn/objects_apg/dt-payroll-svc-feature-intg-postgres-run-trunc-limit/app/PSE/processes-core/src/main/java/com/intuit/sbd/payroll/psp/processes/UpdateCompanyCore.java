/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/UpdateCompanyCore.java#7 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.cto.general.io.utils.http.IntuitCommonHeaders;
import com.intuit.payments.cdm.v2.client.enums.AddressTypeEnum;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.PSPStringUtils;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.common.utils.RealmLogHelper;
import com.intuit.sbd.payroll.psp.common.utils.log.MoneyMovementLogHelper;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.accountservice.RealmResetAccountServiceCore;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.processes.accountservice.UpdateAccountServiceInfo;
import com.intuit.sbd.payroll.psp.processes.accountservice.validation.AccountServiceValidationCore;
import com.intuit.sbd.payroll.psp.processes.accountservice.validation.AccountServiceValidationResultTransformer;
import com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.ValidationStep;
import com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.sms.validationsteps.factory.ValidationStepsFactory;
import com.intuit.sbd.payroll.psp.processes.common.CompanyRealmValidator;
import com.intuit.sbd.payroll.psp.processes.common.PSPToSMSMigrationHelper;
import com.intuit.sbd.payroll.psp.processes.common.ProcessesToDTO;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.wallet.WalletCloneCore;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.psp.validationservices.types.v1.SMSValidationResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Core process for updating a company.
 *
 * @author Dawn Martens
 */
public class UpdateCompanyCore extends Process implements IProcess {
    private Company mDomainCompany;
    private CompanyDTO mDtoCompany;
    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private UpdateEftpsEnrollmentCore mUpdateEftpsEnrollmentCore = null;
    private boolean mUpdateAccountService = false;
    private UpdateAccountServiceInfo mUpdateAccountServiceInfo = null;
    private CompanyRealmValidator companyRealmValidator;
    private static final String[] ACCOUNTANT_APPIDS = {"belacct", "accountant"};
    private static final String RELEASE_VER = "R";
    private static final SpcfLogger logger = SpcfLogManager.getLogger(UpdateCompanyCore.class);
    private boolean isPaymentsAccountValidationRequired = false;
    private RealmResetAccountServiceCore realmResetAccountServiceCore;


    public Company getUpdatedCompany() {
        return mDomainCompany;
    }

    public UpdateCompanyCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                             CompanyDTO pCompanyToCopyFrom) {
        this(pSourceSystemCd,pSourceCompanyId,pCompanyToCopyFrom,false);
    }

    public UpdateCompanyCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                             CompanyDTO pCompanyToCopyFrom, boolean pUpdateAccountService) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mDtoCompany = pCompanyToCopyFrom;
        mUpdateAccountService = pUpdateAccountService;
        companyRealmValidator = new CompanyRealmValidator();
        SystemParameter.findSystemParameter(SystemParameter.Code.PSP_DATE_OFFSET);
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        MoneyMovementLogHelper.logMoneyMovementEventMessage(logger,
                MoneyMovementLogHelper.EventType.UpdateAccount,
                mSourceCompanyId,
                mDomainCompany.getIAMRealmId(),
                mDomainCompany.hasService(ServiceCode.Tax)?"Assisted":"DIY",
                Objects.nonNull(mDomainCompany.getQuickbooksInfo())?mDomainCompany.getQuickbooksInfo().getApplicationVersion():"",
                "Started process",
                null);
        // Create events for Company Info Changes

        // EIN Change
        if (!mDomainCompany.getFedTaxId().equals(mDtoCompany.getFein())) {
            isPaymentsAccountValidationRequired=true;
            //New Entity Change History record is created only when company is on Tax Service and is activated
            //Otherwise just fire an ein change event
            if (mDomainCompany.isCompanyOnService(ServiceCode.Tax)) {
                CompanyService taxService = mDomainCompany.getService(ServiceCode.Tax);
                ServiceStatusCode serviceStatusCode = CompanyService.getServiceStatus(taxService.getStatusCd());
                if (serviceStatusCode != ServiceStatusCode.PendingActivation) {
                    processResult.merge(addEntityChangeRecord());
                }
            } else {
                CompanyEvent.createCompanyInfoChangeEvent(mDomainCompany, mDomainCompany.getFedTaxId(), mDtoCompany.getFein(), EventTypeCode.EINChanged);
            }

            if (mDomainCompany.isCompanyOnService(ServiceCode.Tax)) {
                // update FEIN before calling core process to update company agency (will verify FEIN == tax payer id)
                mDomainCompany.setFedTaxId(mDtoCompany.getFein());

                // update IRS company agency tax payer id
                CompanyAgency agencyIRS = CompanyAgency.findCompanyAgency(mDomainCompany, Agency.IRS);
                CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(agencyIRS);
                for (CompanyAgencyPaymentTemplateDTO companyAgencyPaymentTemplateDTO : companyAgencyDTO.getCompanyAgencyPaymentTemplateDTOList()) {
                    companyAgencyPaymentTemplateDTO.setAgencyTaxpayerId(mDtoCompany.getFein());
                }
                processResult.merge(PayrollServices.companyManager.updateCompanyAgency(mSourceSystemCd, mSourceCompanyId,
                                                                                       Agency.IRS, companyAgencyDTO));

                reenrollRAF();
                //Enroll in to FL ACH enrollment with new FEIN if all conditions are met
                processResult.merge(PayrollServices.companyManager.addACHEnrollment(mSourceSystemCd, mSourceCompanyId, PaymentTemplate.FL_SUI, true));
            }

            // if the ein changes we need to deactivate the current entitlement unit and activate the new ein
            DomainEntitySet<EntitlementUnit> activeEntitlementUnits = mDomainCompany.getActiveEntitlementUnits();

            for (EntitlementUnit entitlementUnit : activeEntitlementUnits) {
                EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);

                // deactivate the current entitlement unit
                entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingDeactivation);
                ProcessResult deactivatePR = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(mDomainCompany.getSourceSystemCd(), mDomainCompany.getSourceCompanyId(), entitlementUnitDTO);
                if (!deactivatePR.isSuccess()) {
                    return deactivatePR;
                }

                // activate new ein
                entitlementUnitDTO.setServiceKey(null);
                entitlementUnitDTO.setExtensionKey(null);
                entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingActivation);
                entitlementUnitDTO.setFedTaxId(mDtoCompany.getFein());
                ProcessResult activatePR = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(mDomainCompany.getSourceSystemCd(), mDomainCompany.getSourceCompanyId(), entitlementUnitDTO);
                if (!activatePR.isSuccess()) {
                    return activatePR;
                }

            }
        }

        // Legal Name Change
        if (!StringUtils.equalsIgnoreCase(mDomainCompany.getLegalName(), mDtoCompany.getLegalName())) {
            isPaymentsAccountValidationRequired=true;

            // Create LegalNameChanged event
            CompanyEvent.createCompanyInfoChangeEvent(mDomainCompany, mDomainCompany.getLegalName(), mDtoCompany.getLegalName(), EventTypeCode.LegalNameChanged);
        }

        // DBA Name Change
        if (mDtoCompany.getDBA() != null && mDomainCompany.getDbaName() != null &&
                !StringUtils.equalsIgnoreCase(mDomainCompany.getDbaName(), mDtoCompany.getDBA())) {
            // Create DBANameChanged event
            CompanyEvent.createCompanyInfoChangeEvent(mDomainCompany, mDomainCompany.getDbaName(), mDtoCompany.getDBA(), EventTypeCode.DBANameChanged);
        }

        // Legal Address Change
        if (addressChanged(mDtoCompany.getLegalAddress(), mDomainCompany.getLegalAddress())) {
           isPaymentsAccountValidationRequired=true;
            // Build a string with the old address
            String oldAddressString = getAddressString(mDomainCompany.getLegalAddress());

            // Build a string with the new address
            Address newAddress = new Address();
            copyAddress(mDtoCompany.getLegalAddress(), newAddress);
            String newAddressString = getAddressString(newAddress);

            //update the Compliance address if the new updated Legal address is valid
            validateAddressAndUpdateComplianceAddress();

            // Create LegalAddressChanged event
            CompanyEvent.createCompanyInfoChangeEvent(mDomainCompany, oldAddressString, newAddressString, EventTypeCode.LegalAddressChanged);
        }

        // if Compliance address is updated then create an event.
        if (Objects.nonNull(mDtoCompany.getComplianceAddress()) && addressChanged(mDtoCompany.getComplianceAddress(), mDomainCompany.getComplianceAddress())) {
            createAddressChangedEvent(mDomainCompany.getComplianceAddress(), mDtoCompany.getComplianceAddress(), EventTypeCode.ComplianceAddressChanged);
        }

        PspPrincipal principal = (PspPrincipal) Application.getCurrentPrincipal();
        boolean agentFlag = false;
        if (principal.isAgent()) {
            agentFlag = true;
        }

        // If COA Fee Account Name Change
        if (coaFeeAccountChanged(mDomainCompany.getQuickbooksInfo(), mDtoCompany.getQuickBooksInfo())) {
            // Create CoaFeeAccountChangeEvent
            CompanyEvent.createCoaAccountChangeEvent(mDomainCompany, EventTypeCode.CoaFeeAccountChange,
                                                     mDomainCompany.getQuickbooksInfo() == null ? null : mDomainCompany.getQuickbooksInfo().getCoaFeeAccountName(), // old value
                                                     mDtoCompany.getQuickBooksInfo() == null ? null : mDtoCompany.getQuickBooksInfo().getCoaFeeAccountName(), // new value
                                                     agentFlag);
        }

        // If COA Sales Tax Account Name Change
        if (coaSalesTaxeAccountChanged(mDomainCompany.getQuickbooksInfo(), mDtoCompany.getQuickBooksInfo())) {
            // Create CoaSalesTaxAccountChangeEvent
            CompanyEvent.createCoaAccountChangeEvent(mDomainCompany, EventTypeCode.CoaSalesTaxAccountChange,
                                                     mDomainCompany.getQuickbooksInfo() == null ? null : mDomainCompany.getQuickbooksInfo().getCoaSalesTaxAccountName(), // old value
                                                     mDtoCompany.getQuickBooksInfo() == null ? null : mDtoCompany.getQuickBooksInfo().getCoaSalesTaxAccountName(), // new value
                                                     agentFlag);
        }

        if(isSMSSapValidationEnabled()) {
            Contact mDomainPrimaryPrincipalContact = mDomainCompany.getContactByRoleCode(ContactRole.PrimaryPrincipal);

            for (ContactDTO currContactDTO : mDtoCompany.getContacts()) {

                if (currContactDTO.getContactRoleCd().equals(ContactRole.PrimaryPrincipal) && !Objects.isNull(currContactDTO.getAddress())
                        && addressChanged(currContactDTO.getAddress(), mDomainPrimaryPrincipalContact.getMailingAddress())) {
                    isPaymentsAccountValidationRequired = true;
                    ProcessResult<SMSValidationResult> ppoAddressValidationResult = getSmsValidationResult(processResult, currContactDTO);

                    processResult.merge(ppoAddressValidationResult);
                    break;
                }
            }
        }

        copyDTOCompanyToDomainCompany();

        //
        // Update the company's EFTPS enrollment (if appropriate)
        //
        if (mUpdateEftpsEnrollmentCore != null) {
            processResult.merge(mUpdateEftpsEnrollmentCore.process());
        }

        // Update company data in account service
        if (mUpdateAccountServiceInfo != null){

            processResult.merge(mUpdateAccountServiceInfo.process());
        }

        doPaymentsAccountValidation(processResult);


        mDomainCompany = Application.save(mDomainCompany);


        // Add updated info to fraud tables for accounts that were once termed for a service that PSP moves money for.
        // To prevent companies from updating company info to allow them to resign up.
        boolean termedAccount = false;
        for (CompanyService companyService : mDomainCompany.getCompanyServiceCollection()) {
            if (companyService.getStatusCd().equals(ServiceSubStatusCode.Terminated) &&
                    companyService.getService().doesPSPMoveMoneyForService()) {
                termedAccount = true;
                break;
            }
        }

        if (termedAccount) {
            if (FraudCompany.findFraudCompany(mDomainCompany) == null) {
                FraudCompany fraudCompany = new FraudCompany(mDomainCompany);
                Application.save(fraudCompany);
            }

            if (FraudAddress.findFraudAddress(mDomainCompany, mDomainCompany.getLegalAddress()) == null) {
                FraudAddress fraudAddress = new FraudAddress(mDomainCompany, mDomainCompany.getLegalAddress());
                Application.save(fraudAddress);
            }

            if (FraudAddress.findFraudAddress(mDomainCompany, mDomainCompany.getMailingAddress()) == null) {
                FraudAddress fraudAddress = new FraudAddress(mDomainCompany, mDomainCompany.getMailingAddress());
                Application.save(fraudAddress);
            }

            for (Contact contact : mDomainCompany.getContactCollection()) {
                if (FraudContact.findFraudContact(mDomainCompany, contact) == null) {
                    FraudContact fraudContact = new FraudContact(mDomainCompany, contact);
                    Application.save(fraudContact);
                }
            }
        }
        MoneyMovementLogHelper.logMoneyMovementEventMessage(logger,
                MoneyMovementLogHelper.EventType.UpdateAccount,
                mSourceCompanyId,
                mDomainCompany.getIAMRealmId(),
                mDomainCompany.hasService(ServiceCode.Tax)?"Assisted":"DIY",
                Objects.nonNull(mDomainCompany.getQuickbooksInfo())?mDomainCompany.getQuickbooksInfo().getApplicationVersion():"",
                "Completed process",
                null);
        return processResult;
    }

    private ProcessResult<SMSValidationResult> getSmsValidationResult(ProcessResult processResult, ContactDTO currContactDTO) {
        logger.info("PPO ADDRESS validation PSID="+mDomainCompany.getSourceCompanyId()+" ,SYSTEMPRINCIPAL="+ Application.getCurrentPrincipal().getSystemPrincipal()+" ,EVENT=STARTED");
        AccountServiceValidationCore accountServiceValidationCore = new AccountServiceValidationCore(currContactDTO.getAddress(), AddressTypeEnum.RESIDENCE, mDomainCompany,false);
        ProcessResult<SMSValidationResult> ppoAddressValidationResult = accountServiceValidationCore.process();

        SMSValidationResult smsValidationResult = ppoAddressValidationResult.getResult();
        if (!smsValidationResult.isValidationPassed()) {
            logger.info("PPO ADDRESS validation PSID="+mDomainCompany.getSourceCompanyId()+" ,SYSTEMPRINCIPAL="+Application.getCurrentPrincipal().getSystemPrincipal()+" ,EVENT=FAILED");
            processResult.getMessages().ASValidationException(mDomainCompany.getSourceCompanyId(), "Validation Failure: "
                    + AccountServiceValidationResultTransformer.getFormattedValidationResultForSAP(smsValidationResult));
        }else{
            logger.info("PPO ADDRESS validation PSID="+mDomainCompany.getSourceCompanyId()+" ,SYSTEMPRINCIPAL="+Application.getCurrentPrincipal().getSystemPrincipal()+" ,EVENT=SUCCESS");

        }
        return ppoAddressValidationResult;
    }

    private void doPaymentsAccountValidation(ProcessResult processResult) {

        if(isPaymentsValidationEnabled()){
            logger.info("payments account validation PSID="+mDomainCompany.getSourceCompanyId()+" ,SYSTEMPRINCIPAL="+ Application.getCurrentPrincipal().getSystemPrincipal()+" ,EVENT=STARTED");

            List<ValidationStep<SMSValidationResult>> paymentsAccountOnly = ValidationStepsFactory.getPaymentsAccountValidationStep(mDtoCompany,mDomainCompany,mDomainCompany.getSourceCompanyId());
            AccountServiceValidationCore accountServiceValidationCore = new AccountServiceValidationCore(mDtoCompany,mDomainCompany,false,paymentsAccountOnly);
            ProcessResult<SMSValidationResult> paValidationResult = accountServiceValidationCore.process();

            if(!paValidationResult.getResult().isValidationPassed() ){
                logger.info("payments account validation PSID="+mDomainCompany.getSourceCompanyId()+" ,SYSTEMPRINCIPAL="+Application.getCurrentPrincipal().getSystemPrincipal()+" ,EVENT=FAILED");
                processResult.getMessages().ASValidationException(mDomainCompany.getSourceCompanyId(), "Validation Failure: "
                            + AccountServiceValidationResultTransformer.getFormattedValidationResultForSAP(paValidationResult.getResult()));
                processResult.merge(paValidationResult);

            }else{
                logger.info("payments account validation PSID="+mDomainCompany.getSourceCompanyId()+" ,SYSTEMPRINCIPAL="+Application.getCurrentPrincipal().getSystemPrincipal()+" ,EVENT=SUCCESS");

            }


        }
    }

    private boolean isPaymentsValidationEnabled() {
        if(!isPaymentsAccountValidationRequired)
        {
            return false;
        }

        if(!Application.getCurrentPrincipal().isAgent()){
            return false;
        }



        if(!mDomainCompany.isCompanyOnService(ServiceCode.DirectDeposit) ){
            return false;
        }

        if(mDomainCompany.isMoneyMovementOnboardingEnabled())
        {
            return false;
        }

        if(Objects.isNull(mDomainCompany.getComplianceAddress())){
            return false;
        }

        return FeatureFlags.get().booleanValue(FeatureFlags.Key.SMS_PAYMENTS_ACCOUNT_VALIDATION_ENABLED, false);


    }

    private void validateAddressAndUpdateComplianceAddress() {

        if(Application.getCurrentPrincipal().isAgent() && !isSMSSapValidationEnabled()  ){
            return ;
        }

        if(Application.getCurrentPrincipal().isCustomer() && !isSMSQBDTValidationEnabled()  ){
            return ;
        }


        logger.info("LEGAL ADDRESS validation PSID="+mDomainCompany.getSourceCompanyId()+" ,SYSTEMPRINCIPAL="+Application.getCurrentPrincipal().getSystemPrincipal()+" ,EVENT=STARTED");
        AccountServiceValidationCore accountServiceValidationCore = new AccountServiceValidationCore(mDtoCompany.getLegalAddress(), AddressTypeEnum.COMPANY, mDomainCompany, false);
        ProcessResult<SMSValidationResult> addressValidationResult = accountServiceValidationCore.process();
        SMSValidationResult smsValidationResult = addressValidationResult.getResult();
        if(smsValidationResult.isValidationPassed()){
            logger.info("LEGAL ADDRESS validation PSID="+mDomainCompany.getSourceCompanyId()+" ,SYSTEMPRINCIPAL="+Application.getCurrentPrincipal().getSystemPrincipal()+" ,EVENT=SUCCESS");
            updateComplianceAddressWithLegalAddress();
        }else{
            logger.info("LEGAL ADDRESS validation PSID="+mDomainCompany.getSourceCompanyId()+" ,SYSTEMPRINCIPAL="+Application.getCurrentPrincipal().getSystemPrincipal()+" ,EVENT=FAILED");
        }

    }

    private void createAddressChangedEvent(Address oldAddress, AddressDTO newAddressDTO, EventTypeCode eventTypeCode) {
        // Build a string with the old address
        String oldAddressString = getAddressString(oldAddress);

        // Build a string with the new address
        Address newAddress = new Address();
        copyAddress(newAddressDTO, newAddress);
        String newAddressString = getAddressString(newAddress);
        CompanyEvent.createCompanyInfoChangeEvent(mDomainCompany, oldAddressString, newAddressString, eventTypeCode);
    }



    private void updateComplianceAddressWithLegalAddress() {

        logger.info("Action=updateComplianceAddressWithLegalAddress, status=start, LegalAddressEmpty=" + Objects.isNull(mDtoCompany.getLegalAddress()) +
                ", ComplianceAddressEmpty=" + Objects.isNull(mDtoCompany.getComplianceAddress()) + ", realmId=" + mDtoCompany.getIAMRealmId());


        if (Objects.isNull(mDtoCompany.getLegalAddress())) {
            return;
        }

        AddressDTO complianceAddress = copyAddress(mDtoCompany.getLegalAddress(), mDtoCompany.getComplianceAddress());
        mDtoCompany.setComplianceAddress(complianceAddress);

        logger.info("Action=updateComplianceAddressWithLegalAddress, status=complete, LegalAddressEmpty=" + Objects.isNull(mDtoCompany.getLegalAddress()) +
                ", ComplianceAddressEmpty=" + Objects.isNull(mDtoCompany.getComplianceAddress()) + ", realmId=" + mDtoCompany.getIAMRealmId());

    }

    private AddressDTO copyAddress(AddressDTO fromDtoObject, AddressDTO toDtoObject) {

        if (Objects.isNull(toDtoObject)) {
            toDtoObject = new AddressDTO();
        }

        toDtoObject.setAddressLine1(fromDtoObject.getAddressLine1());
        toDtoObject.setAddressLine2(fromDtoObject.getAddressLine2());
        toDtoObject.setAddressLine3(fromDtoObject.getAddressLine3());

        toDtoObject.setCity(fromDtoObject.getCity());
        toDtoObject.setState(fromDtoObject.getState());
        toDtoObject.setCountry(fromDtoObject.getCountry());
        toDtoObject.setZipCode(fromDtoObject.getZipCode());
        toDtoObject.setZipCodeExtension(fromDtoObject.getZipCodeExtension());
        return toDtoObject;
    }


    private boolean isSMSSapValidationEnabled() {
        if(!Application.getCurrentPrincipal().isAgent()){
            return false;
        }

        if(!mDomainCompany.isCompanyOnService(ServiceCode.DirectDeposit) ){
            return false;
        }

        if(mDomainCompany.isMoneyMovementOnboardingEnabled()){
            return false;
        }

        return FeatureFlags.get().booleanValue(FeatureFlags.Key.SMS_SAP_VALIDATION_ENABLED, false);
    }

    private boolean isSMSQBDTValidationEnabled() {
        if(!Application.getCurrentPrincipal().isCustomer()){
            return false;
        }

        if(!mDomainCompany.isCompanyOnService(ServiceCode.DirectDeposit) ){
            return false;
        }

        if(mDomainCompany.isMoneyMovementOnboardingEnabled()){
            return false;
        }

        return FeatureFlags.get().booleanValue(FeatureFlags.Key.SMS_QBDT_VALIDATION_ENABLED, false);
    }





    private void copyDTOCompanyToDomainCompany() {
        //company fields
        mDomainCompany.setFedTaxId(mDtoCompany.getFein());
        mDomainCompany.setLegalName(mDtoCompany.getLegalName());
        mDomainCompany.setNameControl(mDtoCompany.getNameControl());
        mDomainCompany.setDbaName(mDtoCompany.getDBA());

        PayrollFrequency domainPayrollFrequency = ProcessesToDTO
                .getDomainPayrollFrequency(mDtoCompany.getPayrollFrequencyCd());

        mDomainCompany.setPayrollFrequency(domainPayrollFrequency);

        copyAddress(mDtoCompany.getMailingAddress(), mDomainCompany.getMailingAddress());
        copyAddress(mDtoCompany.getLegalAddress(), mDomainCompany.getLegalAddress());

        if(mDtoCompany.getComplianceAddress() != null) {
            if(Objects.isNull(mDomainCompany.getComplianceAddress())) {
                mDomainCompany.setComplianceAddress(createDomainAddressFromDTO(mDtoCompany.getComplianceAddress()));
                logger.info("Compliance Address changed PSID="+mDomainCompany.getSourceCompanyId()+" ,SYSTEMPRINCIPAL="+Application.getCurrentPrincipal().getSystemPrincipal()+" ,EVENT=ADDED");
            } else if(addressChanged(mDtoCompany.getComplianceAddress(), mDomainCompany.getComplianceAddress())){
                copyAddress(mDtoCompany.getComplianceAddress(), mDomainCompany.getComplianceAddress());
                logger.info("Compliance Address changed PSID="+mDomainCompany.getSourceCompanyId()+" ,SYSTEMPRINCIPAL="+Application.getCurrentPrincipal().getSystemPrincipal()+" ,EVENT=UPDATED");
            }
        }

        if (!StringUtils.equalsIgnoreCase(mDomainCompany.getNotificationEmail(), mDtoCompany.getNotificationEmail())) {
            // Create Company Notification Email changed event
            CompanyEvent.createCompanyInfoChangeEvent(mDomainCompany, mDomainCompany.getNotificationEmail(), mDtoCompany.getNotificationEmail(), EventTypeCode.EmailAddressChanged);
            mDomainCompany.setNotificationEmail(mDtoCompany.getNotificationEmail());
        }

        //copy CompanyAdditionalInfo
        if (mDtoCompany.getCompanyAdditionalInfo() != null) {

            if (mDomainCompany.getCompanyAdditionalInfo() != null) {
                copyCompanyAdditionalInfo(mDtoCompany.getCompanyAdditionalInfo(), mDomainCompany.getCompanyAdditionalInfo());
            } else {
                CompanyAdditionalInfo companyAdditionalInfo = new CompanyAdditionalInfo();
                copyCompanyAdditionalInfo(mDtoCompany.getCompanyAdditionalInfo(), companyAdditionalInfo);
                companyAdditionalInfo.setCompany(mDomainCompany);
                companyAdditionalInfo = Application.save(companyAdditionalInfo);
                mDomainCompany.setCompanyAdditionalInfo(companyAdditionalInfo);
            }
        }

        mDomainCompany = Application.save(mDomainCompany);

        //Iterate DTO contacts
        for (ContactDTO currContactDTO : mDtoCompany.getContacts()) {
            //Find the contact associated with this DTO source ID
            Contact currDomainContact = mDomainCompany.getContact(currContactDTO.getContactId());
            //Update contact if it already exists; otherwise, add it
            if (currDomainContact != null) {
                copyContact(currContactDTO, currDomainContact);
            } else {
                Contact newDomainContact = new Contact();
                newDomainContact.setContactRoleCd(null); //so we don't generate a PA changed event
                copyContact(currContactDTO, newDomainContact);
                newDomainContact.setCompany(mDomainCompany);
                mDomainCompany.addContact(newDomainContact);
                CompanyEvent.createCompanyContactAddedEvent(mDomainCompany, getContactAddressString(newDomainContact));
            }
            mDomainCompany = Application.save(mDomainCompany);
        }

        //Since the list of contacts returned by SPCF is unmodifiable, we must collect the contacts first in
        // another list and then remove them one by one
        ArrayList<Contact> modifiableExistingContacts = new ArrayList<Contact>();

        for (Contact contact : mDomainCompany.getContactCollection()) {
            modifiableExistingContacts.add(contact);
        }

        //Iterate all of company's contacts.  See if any are missing from the incoming list of contacts
        //If any are missing, remove them        
        for (Contact currContact : modifiableExistingContacts) {
            String currContactKey = currContact.getSourceContactId();
            boolean foundInIncomingCollection = false;
            for (ContactDTO currContactDTO : mDtoCompany.getContacts()) {
                if (currContactDTO.getContactId().equals(currContactKey)) {
                    foundInIncomingCollection = true;
                }
            }

            if (!foundInIncomingCollection) {
                CompanyEvent.createCompanyContactDeletedEvent(mDomainCompany, getContactAddressString(currContact));
                mDomainCompany.removeContact(currContact);
            }
        }

        mDomainCompany.setNextEmployeeId(mDtoCompany.getNextEmployeeId());
        mDomainCompany.setNextPaycheckId(mDtoCompany.getNextPaycheckId());
        mDomainCompany.setNextPayrollItemId(mDtoCompany.getNextPayrollItemId());
        mDomainCompany.setNextPayrollTransactionId(mDtoCompany.getNextPayrollTransactionId());
        if ((mDomainCompany.getTaxExemptExpirationDate() != null && !mDomainCompany.getTaxExemptExpirationDate().equals(DateDTO.convertToSpcfCalendar(mDtoCompany.getTaxExemptExpirationDate()))) ||
                (mDtoCompany.getTaxExemptExpirationDate() != null && !DateDTO.convertToSpcfCalendar(mDtoCompany.getTaxExemptExpirationDate()).equals(mDomainCompany.getTaxExemptExpirationDate())) ||
                (mDomainCompany.getTaxExemptStatus() != mDtoCompany.getTaxExemptStatus())) {
            mDomainCompany.setTaxExemptExpirationDate(DateDTO.convertToSpcfCalendar(mDtoCompany.getTaxExemptExpirationDate()));
            mDomainCompany.setTaxExemptStatus(mDtoCompany.getTaxExemptStatus());
            CompanyEvent.createCompanyEvent(mDomainCompany, EventTypeCode.TaxExemptStatusChanged);
        }

        if (mDtoCompany.getCloudCurrentToken() != null)
            mDomainCompany.setCloudCurrentToken(mDtoCompany.getCloudCurrentToken());

        QuickbooksInfo qbInfo = mDomainCompany.getQuickbooksInfo();
        if (qbInfo == null) {
            qbInfo = new QuickbooksInfo();
        }

        if (mDtoCompany.getQuickBooksInfo() != null) {

            ArrayList<String> details = new ArrayList<String>();
            ArrayList<String> oldValues = new ArrayList<String>();
            ArrayList<String> newValues = new ArrayList<String>();

            if (UpdateQBCompanyInfoCore.canUpdateLicenseNumber(qbInfo, mDtoCompany.getQuickBooksInfo())) {
                if (!StringUtils.equals(StringUtils.trim(qbInfo.getApplicationVersion()), StringUtils.trim(mDtoCompany.getQuickBooksInfo().getApplicationVersion()))) {
                    details.add("Application Version");
                    oldValues.add(qbInfo.getApplicationVersion());
                    newValues.add(mDtoCompany.getQuickBooksInfo().getApplicationVersion());
                }

                if (!StringUtils.equals(qbInfo.getLicenseNumber(), mDtoCompany.getQuickBooksInfo().getLicenseNumber())) {
                    details.add("License Number");
                    oldValues.add(qbInfo.getLicenseNumber());
                    newValues.add(mDtoCompany.getQuickBooksInfo().getLicenseNumber());
                }


                qbInfo.setLicenseNumber(mDtoCompany.getQuickBooksInfo().getLicenseNumber());
                qbInfo.setApplicationId(mDtoCompany.getQuickBooksInfo().getApplicationId());
                qbInfo.setApplicationVersion(mDtoCompany.getQuickBooksInfo().getApplicationVersion());
                qbInfo.setQuickbooksSku(mDtoCompany.getQuickBooksInfo().getQuickbooksSku());
            }

            if (details.size() > 0) {
                CompanyEvent.createQuickBooksInfoChangedEvent(mDomainCompany, details, oldValues, newValues);
            }

            if (!StringUtils.equals(qbInfo.getFileId(), mDtoCompany.getQuickBooksInfo().getFileId())) {
                CompanyEvent.createQuickBooksFileIDChangedEvent(mDomainCompany, qbInfo.getFileId(), mDtoCompany.getQuickBooksInfo().getFileId());
            }


            qbInfo.setCoaFeeAccountName(mDtoCompany.getQuickBooksInfo().getCoaFeeAccountName());
            qbInfo.setCoaSalesTaxAccountName(mDtoCompany.getQuickBooksInfo().getCoaSalesTaxAccountName());
            qbInfo.setTaxTableId(mDtoCompany.getQuickBooksInfo().getTaxTableId());
            qbInfo.setFileId(mDtoCompany.getQuickBooksInfo().getFileId());
            qbInfo.setProcessTransmissions(mDtoCompany.getQuickBooksInfo().isProcessTransmissions());
            qbInfo.setAllowTransmissions(mDtoCompany.getQuickBooksInfo().isAllowTransmissions());

            if (mDtoCompany.getQuickBooksInfo().getIAMRealmId() != null) {
                if(qbInfo.getIAMRealmId()==null){
                    //here QBInfoRealm in request is not null, but previous qbRealm is null, so adding realmId in qbRealm
                    String log = RealmLogHelper.getRealmEventMessage(RealmLogHelper.QB_REALM_ADD, mDtoCompany.getQuickBooksInfo().getIAMRealmId(), null, mDomainCompany, null);
                    Application.printStackTrace(log);
                }else{
                    //here QBInfoRealm in request is not null, and previous qbRealm is also not null, so updating realmId in qbRealm
                    String log = RealmLogHelper.getRealmEventMessage(RealmLogHelper.QB_REALM_UPDATE, mDtoCompany.getQuickBooksInfo().getIAMRealmId(), qbInfo.getIAMRealmId(), mDomainCompany, null);
                    Application.printStackTrace(log);
                }
                qbInfo.setIAMRealmId(mDtoCompany.getQuickBooksInfo().getIAMRealmId());
                if (mDomainCompany.getIAMRealmId() == null) {
                    //companyRealmId is null, so adding new qbinfo realm id in comapnyrealm
                    String log = RealmLogHelper.getRealmEventMessage(RealmLogHelper.COMPANY_REALM_ADD, mDtoCompany.getQuickBooksInfo().getIAMRealmId(), null, mDomainCompany, "added from QBRealm");
                    Application.printStackTrace(log);
                    mDomainCompany.setIAMRealmId(qbInfo.getIAMRealmId());
                    CompanyEvent.createCompanyInfoChangeEvent(mDomainCompany, "NOT AVAILABLE",
                            qbInfo.getIAMRealmId(), EventTypeCode.RealmIdAdded);
                }
            }
        } else {
            qbInfo.setApplicationId(null);
            qbInfo.setApplicationVersion(null);
            qbInfo.setLicenseNumber(null);
            qbInfo.setCoaFeeAccountName(null);
            qbInfo.setCoaSalesTaxAccountName(null);
            qbInfo.setTaxTableId(null);
        }

        qbInfo.setCompany(mDomainCompany);
        mDomainCompany.setQuickbooksInfo(qbInfo);

        mDomainCompany.setDebugLogging(mDtoCompany.isDebugLogging());

        if (!StringUtils.equals(mDomainCompany.getPriceType(), mDtoCompany.getPriceType())) {
            CompanyEvent.createPriceTypeChangedEvent(mDomainCompany, mDomainCompany.getPriceType(), mDtoCompany.getPriceType());
        }
        mDomainCompany.setPriceType(mDtoCompany.getPriceType());
        //it is delete scenario so not updating realmId in companyRealm
        if((mDtoCompany.getIAMRealmId()==null || mDtoCompany.getIAMRealmId().isEmpty()) && mDomainCompany.getIAMRealmId()!= null){
            String log = RealmLogHelper.getRealmEventMessage(RealmLogHelper.COMPANY_REALM_DELETE, null, mDomainCompany.getIAMRealmId(), mDomainCompany, null);
            Application.printStackTrace(log);

        }else if(mDtoCompany.getIAMRealmId()!=null && mDomainCompany.getIAMRealmId()== null) {
            //company didn't have any realmid so adding realmID
            String log = RealmLogHelper.getRealmEventMessage(RealmLogHelper.COMPANY_REALM_ADD, mDtoCompany.getIAMRealmId(), null, mDomainCompany, "added from companyRealm");
            Application.printStackTrace(log);

            mDomainCompany.setIAMRealmId(mDtoCompany.getIAMRealmId());
            CompanyEvent.createCompanyInfoChangeEvent(mDomainCompany, "NOT AVAILABLE",
                    mDtoCompany.getIAMRealmId(), EventTypeCode.RealmIdAdded);
        }/* TODO it should be done only when AMs is sucees and wallet is success*/
        else if(mDtoCompany.getIAMRealmId()!=null && mDomainCompany.getIAMRealmId()!= null && !(mDtoCompany.getIAMRealmId().equals(mDomainCompany.getIAMRealmId()))){
            //company has a realmId and request has new realmId so updating realmID
            logger.info(String.format("Updating realm oldRealmId=%s newRealmId=%s", mDomainCompany.getIAMRealmId(), mDtoCompany.getIAMRealmId()));
            if(handleRealmReset()) {

                String log = RealmLogHelper.getRealmEventMessage(RealmLogHelper.COMPANY_REALM_UPDATE, mDtoCompany.getIAMRealmId(), mDomainCompany.getIAMRealmId(), mDomainCompany, "updated from companyRealm");
                Application.printStackTrace(log);

                CompanyEvent.createCompanyInfoChangeEvent(mDomainCompany, mDomainCompany.getIAMRealmId(),
                        mDtoCompany.getIAMRealmId(), EventTypeCode.RealmIdUpdated);
                mDomainCompany.setIAMRealmId(mDtoCompany.getIAMRealmId());
            }
        }

        mDomainCompany = Application.save(mDomainCompany);
        Application.save(mDomainCompany.getQuickbooksInfo());
    }

    private boolean handleRealmReset() {
        ProcessResult realmResetAccountServiceResult = null;
        // Needed updated compnay data for realm reset in AMS
        if (Objects.nonNull(realmResetAccountServiceCore)
                && mDomainCompany.isMoneyMovementOnboardingEnabled()
                && FeatureFlags.get().booleanValue(FeatureFlags.Key.SMS_COMPANY_REALM_UPDATE, false)) {
            String oldRealmId= mDomainCompany.getIAMRealmId();
            try {
                //this value assignment is required because internal code is written with domain company having newRealmId,
                mDomainCompany.setIAMRealmId(mDtoCompany.getIAMRealmId());
                realmResetAccountServiceResult = realmResetAccountServiceCore.execute();
            } catch (Exception e) {
                logger.error(String.format("Realm Reset Core Exception OldRealm=%s NewRealm=%s PSID=%s",
                        mDomainCompany.getIAMRealmId(), mDtoCompany.getIAMRealmId(), mDomainCompany.getSourceCompanyId()), e);
                return false;
            } finally {
                // once RealmRest is done oldRealmId is set back
                mDomainCompany.setIAMRealmId(oldRealmId);
            }
        }
        if(realmResetAccountServiceResult!=null && realmResetAccountServiceResult.isSuccess()){
            handleWalletClone();
            return true;
        }
        return false;
    }

    private void handleWalletClone() {
        ProcessResult cloneWalletProcessResult = null;
        //RealmId Change should clone all WalletIds for Employees as well as vendors
        if (FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_WALLET_CLONE, true)) {
            try {
                cloneWalletProcessResult = new WalletCloneCore(mDomainCompany, mDtoCompany.getIAMRealmId()).execute();
            } catch (Exception e) {
                logger.error(String.format("Wallet Clone Exception OldRealm=%s NewRealm=%s PSID=%s",
                        mDomainCompany.getIAMRealmId(), mDtoCompany.getIAMRealmId(), mDomainCompany.getSourceCompanyId()), e);
            }
        } else {
            logger.info(String.format("Wallet Clone Feature Flag is disabled OldRealm=%s NewRealm=%s PSID=%s",
                    mDomainCompany.getIAMRealmId(), mDtoCompany.getIAMRealmId(), mDomainCompany.getSourceCompanyId()));
        }

        ProcessResult updateEmployeeOnRealmResetCore = new UpdateEmployeeOnRealmResetCore(mDomainCompany, mDtoCompany).execute();
    }

    public void copyCompanyAdditionalInfo(CompanyAdditionalInfoDTO mDTOCompanyAdditionalInfo, CompanyAdditionalInfo mDomainCompanyAdditionalInfo) {
        //IndustryType
        if (mDTOCompanyAdditionalInfo.getIndustry() != null) {
            if (mDomainCompany.isCompanyOnService(ServiceCode.DirectDeposit)) {
                if (mDomainCompanyAdditionalInfo.getIndustryType() == null) {
                    isPaymentsAccountValidationRequired = true;
                    CompanyEvent.createCompanyInfoChangeEvent(mDomainCompany, "", mDTOCompanyAdditionalInfo.getIndustry(), EventTypeCode.CompanyIndustryTypeChanged);
                } else if (!StringUtils.equalsIgnoreCase(mDTOCompanyAdditionalInfo.getIndustry(), mDomainCompanyAdditionalInfo.getIndustryType().getIndustry())) {
                    isPaymentsAccountValidationRequired = true;
                    CompanyEvent.createCompanyInfoChangeEvent(mDomainCompany, mDomainCompanyAdditionalInfo.getIndustryType().getIndustry(), mDTOCompanyAdditionalInfo.getIndustry(), EventTypeCode.CompanyIndustryTypeChanged);
                }
            }
            mDomainCompanyAdditionalInfo.setIndustryType(IndustryType.findIndustryType(mDTOCompanyAdditionalInfo.getIndustry()));
        }
        // OwnershipType
        if (mDTOCompanyAdditionalInfo.getOwnership() != null) {
            logger.info("OwnershipType updated to: " + mDTOCompanyAdditionalInfo.getOwnership() + " for company with PSID: " + mDtoCompany.getCompanyId());
            mDomainCompanyAdditionalInfo.setOwnershipType(OwnershipType.findOwnershipType(mDTOCompanyAdditionalInfo.getOwnership()));
        }
    }

    public void copyAddress(AddressDTO pAddressToCopyFrom, Address pAddressToCopyTo) {
        pAddressToCopyTo.setAddressLine1(pAddressToCopyFrom.getAddressLine1());
        pAddressToCopyTo.setAddressLine2(pAddressToCopyFrom.getAddressLine2());
        pAddressToCopyTo.setAddressLine3(pAddressToCopyFrom.getAddressLine3());

        pAddressToCopyTo.setCity(pAddressToCopyFrom.getCity());
        pAddressToCopyTo.setState(pAddressToCopyFrom.getState());
        pAddressToCopyTo.setCountry(pAddressToCopyFrom.getCountry());
        pAddressToCopyTo.setZipCode(pAddressToCopyFrom.getZipCode());
        pAddressToCopyTo.setZipCodeExtension(pAddressToCopyFrom.getZipCodeExtension());
    }

    private ProcessResult addEntityChangeRecord() {
        boolean isSuccessor = false;
        boolean isError = false;
        boolean hasNewDataFile = false;
        DateDTO effectiveDate = new DateDTO(PSPDate.getPSPTime());

        if (mDtoCompany.getEntityChange() != null) {
            hasNewDataFile = mDtoCompany.getEntityChange().getHasNewDataFile();
            isSuccessor = mDtoCompany.getEntityChange().getIsSuccessor();
            isError = mDtoCompany.getEntityChange().getIsError();
            effectiveDate = mDtoCompany.getEntityChange().getEffectiveDate();
        }

        EntityChangeDTO entityChangeDTO = new EntityChangeDTO();
        entityChangeDTO.setNewEIN(mDtoCompany.getFein());
        entityChangeDTO.setOldEIN(mDomainCompany.getFedTaxId());
        PspPrincipal currentPrincipal = Application.getCurrentPrincipal();
        String currentId = null;
        if (currentPrincipal != null) {
            currentId = currentPrincipal.getId();
        }
        entityChangeDTO.setUserId(currentId);
        entityChangeDTO.setHasNewDataFile(hasNewDataFile);
        entityChangeDTO.setIsSuccessor(isSuccessor);
        entityChangeDTO.setIsError(isError);
        entityChangeDTO.setEffectiveDate(effectiveDate);
        return PayrollServices.companyManager.addOrUpdateEntityChange(mDomainCompany.getSourceSystemCd(), mDomainCompany.getSourceCompanyId(), entityChangeDTO);
    }

    private boolean addressChanged(AddressDTO pNewAddress, Address pExistingAddress) {

        if (pExistingAddress == null) {
            return !pNewAddress.equals(new AddressDTO());
        }

        if (pNewAddress.equals(new AddressDTO())) {
            return true;
        }
        return !equalsWithDefault(pNewAddress.getAddressLine1(), pExistingAddress.getAddressLine1())
                || !equalsWithDefault(pNewAddress.getAddressLine2(), pExistingAddress.getAddressLine2())
                || !equalsWithDefault(pNewAddress.getAddressLine3(), pExistingAddress.getAddressLine3())
                || !equalsWithDefault(pNewAddress.getCity(), pExistingAddress.getCity())
                || !equalsWithDefault(pNewAddress.getState(), pExistingAddress.getState())
                || !equalsWithDefault(pNewAddress.getCountry(), pExistingAddress.getCountry())
                || !equalsWithDefault(pNewAddress.getZipCode(), pExistingAddress.getZipCode())
                || !equalsWithDefault(pNewAddress.getZipCodeExtension(), pExistingAddress.getZipCodeExtension());
    }

    private static boolean equalsWithDefault(String s1, String s2) {
        return StringUtils.equals(StringUtils.defaultIfEmpty(s1, ""), StringUtils.defaultIfEmpty(s2, ""));
    }

    /**
     * Build a string with the address properties to store as an event detail
     *
     * @param pAddress
     * @return
     */
    private String getAddressString(Address pAddress) {
        if (pAddress == null) {
            return "[No Address]";
        }
        StringBuilder address = new StringBuilder();
        // Line 1
        address.append(pAddress.getAddressLine1());
        //Line 2, if not null
        if (pAddress.getAddressLine2() != null) {
            address.append(" ");
            address.append(pAddress.getAddressLine2());
        }
        //Line 3, if not null
        if (pAddress.getAddressLine3() != null) {
            address.append(" ");
            address.append(pAddress.getAddressLine3());
        }
        // City
        address.append(", ");
        address.append(pAddress.getCity());

        // State
        address.append(",");
        address.append(pAddress.getState());

        //Zip
        address.append(", ");
        address.append(pAddress.getZipCode());

        //Zip Extension, if not null
        if (pAddress.getZipCodeExtension() != null) {
            address.append("-");
            address.append(pAddress.getZipCodeExtension());
        }

        // Country, if not null
        if (pAddress.getCountry() != null) {
            address.append(", ");
            address.append(pAddress.getCountry());
        }
        return address.toString();
    }

    private String getContactAddressString(Contact pContact) {

        if (pContact == null) {
            return "[No Contact]";
        }
        StringBuilder contact = new StringBuilder();

        // Title
        if (pContact.getTitle() != null) {
            contact.append(pContact.getTitle());
            contact.append(" ");
        }
        // First Name
        contact.append(pContact.getFirstName());
        contact.append(" ");

        // Middle Name
        if (pContact.getMiddleName() != null) {
            contact.append(pContact.getMiddleName());
            contact.append(" ");
        }

        // Last Name
        contact.append(pContact.getLastName());
        contact.append(" ");

        //Job Title
        if (pContact.getJobTitle() != null) {
            contact.append(pContact.getJobTitle());
            contact.append(" ");
        }

        //Suffix
        if (pContact.getSuffix() != null) {
            contact.append(pContact.getSuffix());
            contact.append(" ");
        }

        // Phone
        contact.append(pContact.getPhone());
        contact.append(" ");

        //Fax
        if (pContact.getFax() != null) {
            contact.append(pContact.getFax());
            contact.append(" ");
        }
        //Email
        if (pContact.getEmail() != null) {
            contact.append(pContact.getEmail());
            contact.append(" ");
        }
        //Address
        if (pContact.getMailingAddress() != null) {
            contact.append(getAddressString(pContact.getMailingAddress()));
            contact.append(" ");
        }

        return contact.toString();
    }

    /**
     * @param pDTOContact:    Contact from AMS
     * @param pDomainContact: Contact in PSP
     */
    private void copyContact(ContactDTO pDTOContact, Contact pDomainContact) {
        ContactRole domainContactRole = ProcessesToDTO.getDomainContactRole(pDTOContact.getContactRoleCd());

// todo: several of these will not clear the field if the dto value is null - is this correct?
// todo: (if so, how does the client clear an old field - i.e. setting their email address to nothing?)

        // Contact Name change Event
        if (((pDomainContact.getLastName() != null && !pDomainContact.getLastName().equals(pDTOContact.getLastName()) ||
                (pDomainContact.getFirstName() != null && !pDomainContact.getFirstName().equals(pDTOContact.getFirstName()) ||
                        !StringUtils.isEmpty(pDTOContact.getMiddleName()) &&
                                !StringUtils.equalsIgnoreCase(pDomainContact.getMiddleName(), pDTOContact.getMiddleName()))))) {

            // Create a PayrollAdmin changed event if the payroll admin contact name changes
            if ((pDomainContact.getContactRoleCd() != null && ContactRole.PayrollAdmin.equals(pDomainContact.getContactRoleCd()))) {
                CompanyEvent.createCompanyInfoChangeEvent(mDomainCompany, pDomainContact.getFullName(), pDTOContact.getFullName(), EventTypeCode.PayrollAdminChanged);
            } else if (pDomainContact.getFirstName() != null) {
                CompanyEvent.createCompanyInfoChangeEvent(mDomainCompany, pDomainContact.getFullName(), pDTOContact.getFullName(), EventTypeCode.CompanyContactNameChanged);
            }

            if (ContactRole.PrimaryPrincipal.equals(pDomainContact.getContactRoleCd()) && mDomainCompany.isCompanyOnService(ServiceCode.DirectDeposit)) {
                isPaymentsAccountValidationRequired = true;
                CompanyEvent.createCompanyInfoChangeEvent(mDomainCompany, pDomainContact.getFullName(), pDTOContact.getFullName(), EventTypeCode.PrimaryPrincipalNameChanged);
            }
        }

        pDomainContact.setLastName(pDTOContact.getLastName());
        pDomainContact.setFirstName(pDTOContact.getFirstName());

        if (pDTOContact.getMiddleName() != null) {
            pDomainContact.setMiddleName(pDTOContact.getMiddleName());
        }

        AddressDTO contactAddressDTO = pDTOContact.getAddress();
        if (contactAddressDTO != null) {

            Address oldContactAddress = pDomainContact.getMailingAddress();

            if (pDomainContact.getMailingAddress() != null) {
                Application.delete(pDomainContact.getMailingAddress());
            }

            Address domainAddress = createDomainAddressFromDTO(contactAddressDTO);
            pDomainContact.setMailingAddress(domainAddress);

            // Check if address changed and create event
            if (addressChanged(contactAddressDTO, oldContactAddress)) {
                CompanyEvent.createCompanyInfoChangeEvent(mDomainCompany, getAddressString(oldContactAddress), getAddressString(domainAddress), EventTypeCode.CompanyContactAddressChanged);
            }
        }

        // Create ContactRoleChanged event
        if (pDomainContact.getContactRoleCd() != null && !pDomainContact.getContactRoleCd().equals(pDTOContact.getContactRoleCd())) {
            CompanyEvent.createCompanyInfoChangeEvent(mDomainCompany, pDomainContact.getContactRoleCd().toString(), pDTOContact.getContactRoleCd().toString(), EventTypeCode.CompanyContactRoleChanged);
            if (ContactRole.PayrollAdmin.equals(pDomainContact.getContactRoleCd()) || ContactRole.PayrollAdmin.equals(pDTOContact.getContactRoleCd())) {
                CompanyEvent.createCompanyInfoChangeEvent(mDomainCompany, pDomainContact.getContactRoleCd().toString(), pDTOContact.getContactRoleCd().toString(), EventTypeCode.PayrollAdminChanged);
            }
        }
        pDomainContact.setContactRoleCd(domainContactRole);
        if (Objects.nonNull(pDomainContact.getPhone()) || Objects.nonNull(pDTOContact.getPhoneNumber())) {
            if (!PSPStringUtils.equalsIgnoreNonDigits(pDomainContact.getPhone(), pDTOContact.getPhoneNumber())) {
                if (Objects.isNull(pDTOContact.getPhoneNumber()) && Objects.nonNull(pDomainContact.getPhone())) {
                    logger.error("Action=syncPhoneNumber, msg=no_phoneNumber_exists_in_DTO");
                }
                isPaymentsAccountValidationRequired = true;
                CompanyEvent.createCompanyInfoChangeEvent(mDomainCompany, pDomainContact.getPhone(), pDTOContact.getPhoneNumber(), EventTypeCode.CompanyContactPhoneChanged);
                pDomainContact.setPhone(pDTOContact.getPhoneNumber());
            }
        }

        if (pDTOContact.getEmail() != null) {
            if ((pDomainContact.getEmail() != null) && !StringUtils.equalsIgnoreCase(pDTOContact.getEmail(), pDomainContact.getEmail())) {
                isPaymentsAccountValidationRequired = true;
                CompanyEvent.createContactEmailChangedEvent(mDomainCompany, pDomainContact, pDomainContact.getEmail(), pDTOContact.getEmail());
            }
            pDomainContact.setEmail(pDTOContact.getEmail());
        }

        if (pDTOContact.getCommunicationTypeCd() != null) {
            if (CommunicationType.Phone.equals(pDTOContact.getCommunicationTypeCd())) {
                pDomainContact
                        .setCommunicationTypePreference(CommunicationType.Phone);
            } else {
                pDomainContact
                        .setCommunicationTypePreference(CommunicationType.Email);
            }
        }

        if (pDTOContact.getJobTitle() != null) {
            if ((pDomainContact.getJobTitle() != null) && !pDTOContact.getJobTitle().equals(pDomainContact.getJobTitle())) {
                CompanyEvent.createCompanyInfoChangeEvent(mDomainCompany, pDomainContact.getJobTitle(), pDTOContact.getJobTitle(), EventTypeCode.CompanyContactJobTitleChanged);
            }
            pDomainContact.setJobTitle(pDTOContact.getJobTitle());
        }

        if (pDTOContact.getFaxNumber() != null) {
            if ((pDomainContact.getFax() != null) && !pDTOContact.getFaxNumber().equals(pDomainContact.getFax())) {
                CompanyEvent.createCompanyInfoChangeEvent(mDomainCompany, pDomainContact.getFax(), pDTOContact.getFaxNumber(), EventTypeCode.CompanyContactFaxChanged);
            }
            pDomainContact.setFax(pDTOContact.getFaxNumber());
        }

        pDomainContact.setAuthSignerYnInd(pDTOContact.getAccountSignatory());
        pDomainContact.setTitle(pDTOContact.getTitle());
        pDomainContact.setSuffix(pDTOContact.getTitleSuffix());
        pDomainContact.setSecondPhone(pDTOContact.getSecondPhoneNumber());
        pDomainContact.setIAMAuthenticationId(pDTOContact.getIAMAuthenticationId());

        //create primary principal DOB changed event and Primary principal SSN changed event
        if (pDTOContact.getContactRoleCd() == ContactRole.PrimaryPrincipal
                && mDomainCompany.isCompanyOnService(ServiceCode.DirectDeposit)) {
            if (!StringUtils.equalsIgnoreCase(pDomainContact.getSocialSecurityNumberPlainText(), pDTOContact.getSocialSecurityNumber())) {
                isPaymentsAccountValidationRequired = true;
                CompanyEvent.createCompanyInfoChangeEvent(mDomainCompany, pDomainContact.getSocialSecurityNumberPlainText(), pDTOContact.getSocialSecurityNumber(), EventTypeCode.PrimaryPrincipalSSNChanged);
            }
            if (!CalendarUtils.compareSpcfCalendarDate(pDomainContact.getDateOfBirth(), DateDTO.convertToSpcfCalendar(pDTOContact.getDateOfBirth()))) {
                String from = pDomainContact.getDateOfBirth() != null ? pDomainContact.getDateOfBirth().toString() : "";
                String to = pDTOContact.getDateOfBirth() != null ? pDTOContact.getDateOfBirth().toString() : "";
                isPaymentsAccountValidationRequired = true;
                CompanyEvent.createCompanyInfoChangeEvent(mDomainCompany, from, to, EventTypeCode.PrimaryPrincipalDOBChanged);
            }
        }

        if (pDTOContact.getDateOfBirth() != null) {
            SpcfCalendar dateOfBirth = DateDTO.convertToSpcfCalendar(pDTOContact.getDateOfBirth());
            CalendarUtils.clearTime(dateOfBirth);
            pDomainContact.setDateOfBirth(dateOfBirth);
        }

        pDomainContact.setSocialSecurityNumberPlainText(pDTOContact.getSocialSecurityNumber());
        pDomainContact.setSourceContactId(pDTOContact.getContactId());
    }

    private boolean coaFeeAccountChanged(QuickbooksInfo pExistingQBInfo, QuickbooksInfoDTO pNewQBInfo) {
        if (null == pExistingQBInfo && null == pNewQBInfo) {
            return false;
        } else if ((null == pExistingQBInfo || null == pExistingQBInfo.getCoaFeeAccountName()) &&
                (null != pNewQBInfo && null != pNewQBInfo.getCoaFeeAccountName())) {
            return true;
        } else if ((null != pExistingQBInfo && null != pExistingQBInfo.getCoaFeeAccountName()) &&
                (null == pNewQBInfo || null == pNewQBInfo.getCoaFeeAccountName())) {
            return true;
        } else if (null != pExistingQBInfo && null != pExistingQBInfo.getCoaFeeAccountName() &&
                null != pNewQBInfo.getCoaFeeAccountName()) {
            return (!pExistingQBInfo.getCoaFeeAccountName().equals(pNewQBInfo.getCoaFeeAccountName()));
        }

        return false;
    }

    private boolean coaSalesTaxeAccountChanged(QuickbooksInfo pExistingQBInfo, QuickbooksInfoDTO pNewQBInfo) {
        if (null == pExistingQBInfo && null == pNewQBInfo) {
            return false;
        } else if ((null == pExistingQBInfo || null == pExistingQBInfo.getCoaSalesTaxAccountName()) &&
                (null != pNewQBInfo && null != pNewQBInfo.getCoaSalesTaxAccountName())) {
            return true;
        } else if ((null != pExistingQBInfo && null != pExistingQBInfo.getCoaSalesTaxAccountName()) &&
                (null == pNewQBInfo || null == pNewQBInfo.getCoaSalesTaxAccountName())) {
            return true;
        } else if (null != pExistingQBInfo && null != pExistingQBInfo.getCoaSalesTaxAccountName() &&
                null != pNewQBInfo.getCoaSalesTaxAccountName()) {
            return (!pExistingQBInfo.getCoaSalesTaxAccountName().equals(pNewQBInfo.getCoaSalesTaxAccountName()));
        }

        return false;
    }

    private Address createDomainAddressFromDTO(AddressDTO pAddressDTO) {
        if (pAddressDTO.equals(new AddressDTO())) {
            return null;
        }
        Address domainAddress = new Address();
        domainAddress.setAddressLine1(pAddressDTO.getAddressLine1());
        domainAddress.setAddressLine2(pAddressDTO.getAddressLine2());
        domainAddress.setAddressLine3(pAddressDTO.getAddressLine3());
        domainAddress.setCity(pAddressDTO.getCity());
        domainAddress.setCountry(pAddressDTO.getCountry());
        domainAddress.setState(pAddressDTO.getState());
        domainAddress.setZipCode(pAddressDTO.getZipCode());
        domainAddress.setZipCodeExtension(pAddressDTO.getZipCodeExtension());
        return Application.save(domainAddress);
    }


    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (mDtoCompany == null) {
            validationResult.getMessages().CompanyNotSpecified(EntityName.Company, null);
            return validationResult;
        }

        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Validate DTO
        validationResult.merge(mDtoCompany.validateCompanyDTO());
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Validate company exists
        mDomainCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);
        if (mDomainCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                                                               mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        if (!mDomainCompany.isAllowedCapability(SystemCapabilityCode.ChangeCompanyInfo)) {
            if (mDomainCompany.isCompanyOnService(ServiceCode.ThirdParty401k)) {
                validationResult.getMessages().CompanyOperationNotAllowed401k(EntityName.Company,
                                                                              mDomainCompany.getSourceSystemCd().toString(),
                                                                              mDomainCompany.getSourceCompanyId(), SystemCapabilityCode.ChangeCompanyInfo.toString());
            } else {
                validationResult.getMessages().CompanyOperationNotAllowed(
                        mDomainCompany.getSourceSystemCd().toString(),
                        mDomainCompany.getSourceCompanyId(), SystemCapabilityCode.ChangeCompanyInfo.toString());
            }
            return validationResult;
        }

        if (!mDomainCompany.passesAdditionalCancelTermValidation(false, true, true, true)) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                    mDomainCompany.getSourceSystemCd().toString(),
                    mDomainCompany.getSourceCompanyId(), SystemCapabilityCode.ChangeCompanyInfo.toString());
        }

        // validate the new ein is not already in use on the entitlement
        if (!mDomainCompany.getFedTaxId().equals(mDtoCompany.getFein())) {
            for (EntitlementUnit entitlementUnit : mDomainCompany.getEntitlementUnitCollection()) {
                DomainEntitySet<EntitlementUnit> activeEntitlementUnits = EntitlementUnit.getActiveEntitlementUnits(mDtoCompany.getFein(),
                                                                                                                    entitlementUnit.getEntitlement().getLicenseNumber(),
                                                                                                                    entitlementUnit.getEntitlement().getEntitlementOfferingCode());

                if (activeEntitlementUnits.size() > 0) {
                    Company company = activeEntitlementUnits.get(0).getCompany();
                    validationResult.getMessages().EINAlreadyInUse(EntityName.EntitlementUnit,
                                                                   activeEntitlementUnits.get(0).getId().toString(),
                                                                   mDtoCompany.getFein(),
                                                                   company.getSourceSystemCd().toString(),
                                                                   company.getSourceCompanyId());
                    return validationResult;
                }
            }
        }

        // Validate account signatory
        if (!mDtoCompany.hasAccountSignatoryContact()) {
            validationResult.getMessages().NoAccountSignatory(EntityName.Company,
                                                              mDtoCompany.getCompanyId(), mDtoCompany.getSourceSystemCd().toString(),
                                                              mDtoCompany.getCompanyId());
        }

        String compFedTaxId = mDtoCompany.getFein();
        SourceSystemCode compSrcSysCd = mDtoCompany.getSourceSystemCd();

        //IOP allows the same EIN on multiple accounts.
        if (!mDtoCompany.getSourceSystemCd().equals(SourceSystemCode.IOP)) {
            if (!mDomainCompany.getFedTaxId().equals(compFedTaxId)) {
                //Any risk?
                if (mDomainCompany.doesPSPMoveMoneyFor()) {
                    // Validate that no terminated companies are using the EIN
                    DomainEntitySet<CompanyService> termed = CompanyService
                            .findTerminatedCompanyServicesByFeinExcludingSourceSystemIdAndServiceCode(mDtoCompany.getSourceSystemCd(),
                                                                                                      mDtoCompany.getCompanyId(), compFedTaxId, ServiceCode.Cloud);
                    if (!termed.isEmpty()) {
                        validationResult.getMessages().EINInUseByTerminatedCompanyUpdate(EntityName.Company,
                                                                                         mDtoCompany.getCompanyId(),
                                                                                         compSrcSysCd.toString(),
                                                                                         mDtoCompany.getCompanyId(), compFedTaxId);
                    }
                }

                // validate new EIN not in use by other companies
                DomainEntitySet<CompanyService> companyServices = CompanyService.findActiveCompanyServicesByFeinExcludingSourceSystemIdAndServiceCode(
                        mDtoCompany.getSourceSystemCd(), mDtoCompany.getCompanyId(), mDtoCompany.getFein(), ServiceCode.Cloud);
                if (!companyServices.isEmpty()) {
                    validationResult.getMessages().EinInUseUpdate(EntityName.Company, mDtoCompany.getCompanyId(),
                                                                  compSrcSysCd.toString(), mDtoCompany.getCompanyId(),
                                                                  compFedTaxId);
                }
            }
        }

        //
        // Check to see if we need to update their EFTPS enrollment
        //
        validationResult.merge(checkEftpsEnrollmentStatus());

        // Validate NameControl
        // if NameControl is not null then
        // 1. Should have whitespaces only at the end
        // 2. Should not be of length greater than 4
        // 3. Should not have any special characters other than - and &

        String nameControl = mDtoCompany.getNameControl();
        if (nameControl != null && !nameControl.isEmpty()) {
            if (!nameControl.matches("[A-Z,a-z,0-9,/&,/-]{1,4}")) {
                validationResult.getMessages().InvalidNameControlValue(EntityName.Company, mDtoCompany.getCompanyId(),
                                                                       mDtoCompany.getSourceSystemCd().toString(), mDtoCompany.getCompanyId(), nameControl);
            }
        }

        validationResult.merge(companyRealmValidator.validate(CompanyRealmValidator.CompanyCoreEventType.COMPANY_UPDATE, mDomainCompany, mDtoCompany));

        // Validate AccountService Update
        if(StringUtils.isNotEmpty(mDomainCompany.getIAMRealmId())
                && StringUtils.isNotEmpty(mDtoCompany.getIAMRealmId())
                && !StringUtils.equals(mDomainCompany.getIAMRealmId(), mDtoCompany.getIAMRealmId())
                && mDomainCompany.isCompanyOnActiveService(ServiceCode.DirectDeposit)){
            realmResetAccountServiceCore = new RealmResetAccountServiceCore(mDomainCompany,mDomainCompany.getIAMRealmId(),getTid());
        } else if(StringUtils.isNotEmpty(mDtoCompany.getIAMRealmId())
                && mDomainCompany.isMoneyMovementOnboardingEnabled()
                && mUpdateAccountService){
            // Validate AccountService Update
            mUpdateAccountServiceInfo = new UpdateAccountServiceInfo(mDtoCompany,mDomainCompany);

            ProcessResult result= mUpdateAccountServiceInfo.validate();

            if(!result.isSuccess() && !mUpdateAccountServiceInfo.propagateError()){
                mUpdateAccountServiceInfo = null;
                logger.error("Error while doing sync with Account Service on payroll run for PSID="+mSourceCompanyId+ " error reason "+result.getMessages());
            }else{
                validationResult.merge(result);
            }
        }



        MoneyMovementLogHelper.logMoneyMovementEventMessage(logger,
                MoneyMovementLogHelper.EventType.UpdateAccount,
                mSourceCompanyId,
                mDomainCompany.getIAMRealmId(),
                mDomainCompany.hasService(ServiceCode.Tax)?"Assisted":"DIY",
                Objects.nonNull(mDomainCompany.getQuickbooksInfo())?mDomainCompany.getQuickbooksInfo().getApplicationVersion():"",
                "Completed Validation",
                null);
        return validationResult;
    }

    public String getTid() {
        String tid = MDC.get(IntuitCommonHeaders.INTUIT_HEADER_TID);
        if (StringUtils.isEmpty(tid)){
            tid = UUID.randomUUID().toString();
            MDC.put(IntuitCommonHeaders.INTUIT_HEADER_TID,tid);
        }
        return tid;
    }

    private ProcessResult reenrollRAF() {
        ProcessResult validationResult = new ProcessResult();

        //
        // If the company is on the Tax service, check to see if we need to adjust the raf enrollment.
        //

        if (mDomainCompany.isEligibleForRAF() && (mDomainCompany.getCurrentRAFEnrollmentStatus() != null)) {
            RAFEnrollment rafEnrollment = mDomainCompany.getCurrentRAFEnrollment();
            switch (rafEnrollment.getStatus()) {
                case Enrolled:
                    rafEnrollment.updateEnrollmentStatus(RAFEnrollmentStatus.Cancelled);
                    RAFEnrollment.createNewEnrollment(rafEnrollment.getCompanyAgency());
            }
        }

        return validationResult;
    }

    private ProcessResult checkEftpsEnrollmentStatus() {
        ProcessResult validationResult = new ProcessResult();

        //
        // If the company is on the Assisted or Tax service, check to see if we need to adjust the eftps enrollment.
        //

        if (mDomainCompany.isEligibleForEftps() && (mDomainCompany.getCurrentEnrollmentStatus() != null)) {
            EftpsEnrollment eftpsEnrollment = mDomainCompany.getCurrentEnrollment();
            EftpsEnrollmentStatus newEftpsEnrollmentStatus = null;

            //
            // Check to see if the company EIN has changed (reenroll if appropriate)
            //
            if (!mDtoCompany.getFein().equals(mDomainCompany.getFedTaxId())) {
                switch (eftpsEnrollment.getStatusCd()) {
                    case PendingAcceptance:
                    case Enrolled:
                    case Rejected:
                    case AgedOut:
                        newEftpsEnrollmentStatus = EftpsEnrollmentStatus.PendingEnrollment;
                        break;
                }
            }

            //
            // Check to see if the company legal name has changed (reenroll if appropriate)
            //
            if (!mDtoCompany.getLegalName().equals(mDomainCompany.getLegalName())) {
                switch (eftpsEnrollment.getStatusCd()) {
                    case PendingAcceptance:
                    case Rejected:
                    case AgedOut:
                        newEftpsEnrollmentStatus = EftpsEnrollmentStatus.PendingEnrollment;
                        break;
                }
            }

            //
            // Check to see if the company legal address (zip code) has changed (reenroll if appropriate)
            //
            if (!mDtoCompany.getLegalAddress().getZipCode().equals(mDomainCompany.getLegalAddress().getZipCode())) {
                switch (eftpsEnrollment.getStatusCd()) {
                    case PendingAcceptance:
                    case Rejected:
                    case AgedOut:
                        newEftpsEnrollmentStatus = EftpsEnrollmentStatus.PendingEnrollment;
                        break;
                }
            }

            if (newEftpsEnrollmentStatus != null) {
                mUpdateEftpsEnrollmentCore = new UpdateEftpsEnrollmentCore(mDomainCompany.getSourceSystemCd(),
                                                                           mDomainCompany.getSourceCompanyId(),
                                                                           newEftpsEnrollmentStatus);
                validationResult.merge(mUpdateEftpsEnrollmentCore.validate());
            }
        }

        return validationResult;
    }

}
