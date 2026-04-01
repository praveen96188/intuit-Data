package com.intuit.sbd.payroll.psp.adapters.qbdt.processors;

import com.intuit.sbd.payroll.psp.adapters.qbdt.AssistedConnectionInformation;
import com.intuit.sbd.payroll.psp.adapters.qbdt.CredentialType;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.CompanyInfo;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ServiceInfoDTO;
import com.intuit.sbd.payroll.psp.common.ofx.request.ICOINFOMOD;
import com.intuit.sbd.payroll.psp.common.ofx.request.SONRQ;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ObjectUtils;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 5/13/12
 * Time: 7:07 PM
 */
@Slf4j
public class CompanyProcessor {
    private Company mCompany;
    private AssistedConnectionInformation mConnectionInformation;
    private CredentialType mCredentialType;

    public CompanyProcessor(Company pCompany, AssistedConnectionInformation pAssistedConnectionInformation, CredentialType pCredentialType) {
        mCompany = pCompany;
        mConnectionInformation = pAssistedConnectionInformation;
        mCredentialType = pCredentialType;
    }

    public ProcessResult processCompany(ICOINFOMOD pICOINFOMOD, SONRQ pSONRQ) {
        ProcessResult processResult = new ProcessResult();

        CompanyInfo companyInfo = new CompanyInfo(pICOINFOMOD, pSONRQ);

        if(companyInfo.hasCompanyMod() && mCredentialType == CredentialType.Pin) {
            boolean hasTaxService = mCompany.isCompanyOnService(ServiceCode.Tax);
            if(mConnectionInformation.isBalanceFile()) {
                if(companyInfo.getQuarterToStartDate() != null && hasTaxService) {
                    ServiceInfoDTO serviceInfoDTO = PayrollServices.dtoFactory.create(mCompany.getCompanyService(ServiceCode.Tax));
                    serviceInfoDTO.setServiceStartDate(companyInfo.getQuarterToStartDate());
                    mConnectionInformation.setProcessedAddsOrUpdates(true);
                    processResult.merge(PayrollServices.companyManager.updateService(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), serviceInfoDTO));
                    if(!processResult.isSuccess()) {
                        return processResult;
                    }
                }
            } else {
                CompanyBankAccount activeCompanyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(mCompany);
                if(activeCompanyBankAccount!= null && companyInfo.getSourceBankAccountName() != null && companyInfo.getSourceBankAccountName().trim().length() > 0 &&
                        !ObjectUtils.equals(companyInfo.getSourceBankAccountName(), activeCompanyBankAccount.getSourceBankAccountName())) {
                    CompanyBankAccountDTO coBankAcctDTO = PayrollServices.dtoFactory.create(activeCompanyBankAccount);
                    coBankAcctDTO.setSourceBankAccountName(companyInfo.getSourceBankAccountName());
                    mConnectionInformation.setProcessedAddsOrUpdates(true);
                    processResult.merge(PayrollServices.companyManager.updateCompanyBankAccount(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), coBankAcctDTO));
                    if(!processResult.isSuccess()) {
                        return processResult;
                    }
                }
                log.info("updating the realm in DTO hasTax="+hasTaxService+" companyRealm=" + mCompany.getIAMRealmId() + " companyInfoRealm=" + companyInfo.getIAMRealmId());

                if(!hasTaxService) {
                    CompanyDTO companyDTO = PayrollServices.dtoFactory.create(mCompany);
                    companyDTO.setLegalName(companyInfo.getLegalName());
                    companyDTO.setLegalAddress(companyInfo.getLegalAddressDTO());
                    if (FeatureFlags.get().booleanValue(FeatureFlags.Key.SMS_COMPANY_REALM_UPDATE, false)) {
                        companyDTO.setIAMRealmId(companyInfo.getIAMRealmId());
                    }
                    mConnectionInformation.setProcessedAddsOrUpdates(true);
                    processResult.merge(PayrollServices.companyManager.updateCompanyWithAccountService(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), companyDTO));
                    if(!processResult.isSuccess()) {
                        return processResult;
                    }
                }
            }
        }

        if(companyInfo.hasAppInfo()) {
            QuickbooksInfo quickbooksInfo = mCompany.getQuickbooksInfo();
            CompanyDTO companyDTO = null;
            if(quickbooksInfo != null) {
                if(!ObjectUtils.equals(companyInfo.getApplicationId(), quickbooksInfo.getApplicationId())) {
                    companyDTO = PayrollServices.dtoFactory.create(mCompany);
                    companyDTO.getQuickBooksInfo().setApplicationId(companyInfo.getApplicationId());
                }
                if(!ObjectUtils.equals(companyInfo.getQuickbooksSku(), quickbooksInfo.getQuickbooksSku())) {
                    companyDTO = PayrollServices.dtoFactory.create(mCompany);
                    companyDTO.getQuickBooksInfo().setQuickbooksSku(companyInfo.getQuickbooksSku());
                }

                if(!ObjectUtils.equals(companyInfo.getApplicationVersion(), quickbooksInfo.getApplicationVersion())) {
                    if(companyDTO == null) {
                        companyDTO = PayrollServices.dtoFactory.create(mCompany);
                    }
                    companyDTO.getQuickBooksInfo().setApplicationVersion(companyInfo.getApplicationVersion());
                }

                if (!"".equals(companyInfo.getIAMRealmId()) && !ObjectUtils.equals(companyInfo.getIAMRealmId(), quickbooksInfo.getIAMRealmId())) {
                    if(companyDTO == null) {
                        companyDTO = PayrollServices.dtoFactory.create(mCompany);
                    }
                    companyDTO.getQuickBooksInfo().setIAMRealmId(companyInfo.getIAMRealmId());
                }

                if(!ObjectUtils.equals(companyInfo.getTaxTable(), quickbooksInfo.getTaxTableId())) {
                    if(companyDTO == null) {
                        companyDTO = PayrollServices.dtoFactory.create(mCompany);
                    }
                    companyDTO.getQuickBooksInfo().setTaxTableId(companyInfo.getTaxTable());
                }

                if(companyDTO != null) {
                    processResult.merge(PayrollServices.companyManager.updateQBCompanyInfo(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), companyDTO));
                    if(!processResult.isSuccess()) {
                        return processResult;
                    }
                }
            }
        }

        return processResult;
    }
}
