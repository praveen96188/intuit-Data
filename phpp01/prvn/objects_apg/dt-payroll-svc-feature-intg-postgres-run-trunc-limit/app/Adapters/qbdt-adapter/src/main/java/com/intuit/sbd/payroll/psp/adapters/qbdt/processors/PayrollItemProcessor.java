package com.intuit.sbd.payroll.psp.adapters.qbdt.processors;

import com.intuit.sbd.payroll.psp.adapters.qbdt.AssistedConnectionInformation;
import com.intuit.sbd.payroll.psp.adapters.qbdt.CredentialType;
import com.intuit.sbd.payroll.psp.adapters.qbdt.translators.PayrollItemTranslator;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollItem;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyLawDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyPayrollItemDTO;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPITEM;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyLaw;
import com.intuit.sbd.payroll.psp.domain.CompanyPayrollItem;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 11, 2010
 * Time: 1:47:51 PM
 */
public class PayrollItemProcessor {
    private Company mCompany;
    private AssistedConnectionInformation mConnectionInformation;
    private CredentialType mCredentialType;

    public PayrollItemProcessor(Company pCompany, AssistedConnectionInformation pAssistedConnectionInformation, CredentialType pCredentialType) {
        mCompany = pCompany;
        mConnectionInformation = pAssistedConnectionInformation;
        mCredentialType = pCredentialType;
        CompanyLaw.findCompanyLawBySourceId(mCompany, null, true);
    }

    public ProcessResult processPayrollItems(List<IPITEM> pPayrollItems) {
        ProcessResult processResult = new ProcessResult();

        if (mCredentialType != CredentialType.Pin) {
            return processResult;
        }

        if(pPayrollItems.size() > 0) {
            mConnectionInformation.setProcessedAddsOrUpdates(true);
        }

        List<PayrollItem> taxPayrollItems = new ArrayList<PayrollItem>();
        List<PayrollItem> nonTaxPayrollItems = new ArrayList<PayrollItem>();
        for (IPITEM iPayrollItem : pPayrollItems) {
            PayrollItem payrollItemWrapper = new PayrollItem(iPayrollItem);

            CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(mCompany, payrollItemWrapper.getSourceId());
            CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(mCompany, payrollItemWrapper.getSourceId());

            if(companyLaw != null && !payrollItemWrapper.isTaxItem()) {
                processResult.getMessages().GenericError(EntityName.PayrollItem, payrollItemWrapper.getSourceId(),
                                                         "Payroll item '" + payrollItemWrapper.getSourceId() + "' " + payrollItemWrapper.getSourceDescription() + " already exists as a tax item and cannot be changed to type " + payrollItemWrapper.getItemType());
                return processResult;
            } else if(companyPayrollItem != null && payrollItemWrapper.isTaxItem()) {
                processResult.getMessages().GenericError(EntityName.PayrollItem, payrollItemWrapper.getSourceId(),
                                                         "Payroll item '" + payrollItemWrapper.getSourceId() + "' " + payrollItemWrapper.getSourceDescription() + "already exists as a non-tax item and cannot be changed to type " + payrollItemWrapper.getItemType());
                return processResult;
            } else {
                if(payrollItemWrapper.isTaxItem()) {
                    taxPayrollItems.add(payrollItemWrapper);
                } else {
                    nonTaxPayrollItems.add(payrollItemWrapper);
                }
            }
        }

        // process tax items first b/c non-tax items can refer to the tax items
        for (PayrollItem taxPayrollItem : taxPayrollItems) {
            CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(mCompany, taxPayrollItem.getSourceId());
            if(companyLaw == null){
                processResult.merge(addCompanyLaw(taxPayrollItem));
            } else {
                processResult.merge(updateCompanyLaw(taxPayrollItem, companyLaw));
            }

            if(!processResult.isSuccess()) {
                return processResult;
            }
        }

        for (PayrollItem nonTaxPayrollItem : nonTaxPayrollItems) {
            CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(mCompany, nonTaxPayrollItem.getSourceId());

            if(companyPayrollItem == null){
                processResult.merge(addPayrollItem(nonTaxPayrollItem));
            } else {
                processResult.merge(updatePayrollItem(nonTaxPayrollItem, companyPayrollItem));
            }
            if(!processResult.isSuccess()) {
                return processResult;
            }
        }

        return processResult;
    }

    private ProcessResult addCompanyLaw(PayrollItem pPayrollItem) {
        CompanyLawDTO companyLawDTO = new CompanyLawDTO();
        PayrollItemTranslator.populateCompanyLawDTO(pPayrollItem, companyLawDTO);
        return PayrollServices.companyManager.addOrUpdateCompanyLaw(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), companyLawDTO);
    }

    private ProcessResult addPayrollItem(PayrollItem pPayrollItem) {
        CompanyPayrollItemDTO companyPayrollItemDTO = new CompanyPayrollItemDTO();
        PayrollItemTranslator.populatePayrollItemDTO(pPayrollItem, companyPayrollItemDTO);
        return PayrollServices.companyManager.addOrUpdateCompanyPayrollItem(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), companyPayrollItemDTO);
    }

    private ProcessResult updateCompanyLaw(PayrollItem pPayrollItem, CompanyLaw pCompanyLaw) {
        CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(pCompanyLaw);
        PayrollItemTranslator.populateCompanyLawDTO(pPayrollItem, companyLawDTO);
        return PayrollServices.companyManager.addOrUpdateCompanyLaw(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), companyLawDTO);
    }

    private ProcessResult updatePayrollItem(PayrollItem pPayrollItem, CompanyPayrollItem pCompanyPayrollItem) {
        CompanyPayrollItemDTO companyPayrollItemDTO = PayrollServices.dtoFactory.create(pCompanyPayrollItem);
        PayrollItemTranslator.populatePayrollItemDTO(pPayrollItem, companyPayrollItemDTO);
        return PayrollServices.companyManager.addOrUpdateCompanyPayrollItem(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), companyPayrollItemDTO);
    }

    public ProcessResult deletePayrollItems(List<String> pPayrollItemIds) {
        ProcessResult processResult = new ProcessResult();

        if (mCredentialType != CredentialType.Pin) {
            return processResult;
        }

        for (String payrollItemId : pPayrollItemIds) {
            CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(mCompany, payrollItemId);
            if(companyLaw != null) {
                CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(companyLaw);
                companyLawDTO.getQBDTPayrollItemInfoDTO().setIsDeleted(true);

                processResult.merge(PayrollServices.companyManager.addOrUpdateCompanyLaw(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), companyLawDTO));
                if(!processResult.isSuccess()) {
                    return processResult;
                }
            }

            CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(mCompany, payrollItemId);
            if(companyPayrollItem != null) {
                CompanyPayrollItemDTO companyPayrollItemDTO = PayrollServices.dtoFactory.create(companyPayrollItem);
                companyPayrollItemDTO.getQBDTPayrollItemInfoDTO().setIsDeleted(true);

                processResult.merge(PayrollServices.companyManager.addOrUpdateCompanyPayrollItem(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), companyPayrollItemDTO));
                if(!processResult.isSuccess()) {
                    return processResult;
                }
            }
        }

        return processResult;
    }

}
