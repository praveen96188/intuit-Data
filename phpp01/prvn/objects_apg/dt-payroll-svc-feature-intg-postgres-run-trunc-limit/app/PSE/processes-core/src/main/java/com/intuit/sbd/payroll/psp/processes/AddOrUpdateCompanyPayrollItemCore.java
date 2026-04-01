/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/AddOrUpdateCompanyPayrollItemCore.java#3 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.dtos.CompanyPayrollItemDTO;
import com.intuit.sbd.payroll.psp.api.dtos.QBDTPayrollItemInfoDTO;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.Application;
// import com.intuit.sbd.payroll.psp.gateways.documentrepository.DocumentRepository;
// import com.intuit.sbd.payroll.psp.gateways.documentrepository.Document;
import com.intuit.sbd.payroll.psp.domain.*;

import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.api.PayrollServices;

/**
 * Core process for updating a payroll item for a company.
 *
 * @author Dawn Martens
 */

public class AddOrUpdateCompanyPayrollItemCore extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private Company mCompany;
    private CompanyPayrollItemDTO mCompanyPayrollItemDTO;
    private PayrollItem mPayrollItem;

    /**
     * The process parameters
     *
     * @param pSourceSystemCd - the source system
     * @param pSourceCompanyId - company id
     * @param pCompanyPayrollItemDTO - Company payroll item
     */
    public AddOrUpdateCompanyPayrollItemCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                             CompanyPayrollItemDTO pCompanyPayrollItemDTO) {
        this.mSourceSystemCd = pSourceSystemCd;
        this.mSourceCompanyId = pSourceCompanyId;
        this.mCompanyPayrollItemDTO = pCompanyPayrollItemDTO;
    }

    /**
     * process request
     * @return result - error messages and status if any
     */
    public ProcessResult<CompanyPayrollItem> process() {
        ProcessResult<CompanyPayrollItem> processResult = new ProcessResult<CompanyPayrollItem>();
        CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(mCompany, mCompanyPayrollItemDTO.getSourcePayrollItemId());

        if (companyPayrollItem == null) {
            companyPayrollItem = new CompanyPayrollItem();
            companyPayrollItem.setCompany(mCompany);

            if(mCompany.getSourceSystemCd() == SourceSystemCode.QBDT && mCompanyPayrollItemDTO.getSourcePayrollItemDescription() != null) {
                // QBDT only allows 1 item with a specific name
                CompanyPayrollItem originalCompanyPayrollItem = CompanyPayrollItem.findCompanyPayrollItemByDescription(mCompany, mCompanyPayrollItemDTO.getSourcePayrollItemDescription());
                if(originalCompanyPayrollItem != null && originalCompanyPayrollItem.getPayrollItem().getPayrollItemCode() == mPayrollItem.getPayrollItemCode()) {
                    CompanyEvent event = CompanyEvent.createCompanyEvent(mCompany, EventTypeCode.DuplicatePayrollItemReceived);
                    event.addCompanyEventDetail(EventDetailTypeCode.Description, originalCompanyPayrollItem.getSourceDescription());
                    event.addCompanyEventDetail(EventDetailTypeCode.NewStringValue, mCompanyPayrollItemDTO.getSourcePayrollItemId());
                    event.addCompanyEventDetail(EventDetailTypeCode.OldStringValue, originalCompanyPayrollItem.getSourcePayrollItemId());
                    Application.save(event);

                    if(originalCompanyPayrollItem.getQbdtPayrollItemInfo() != null) {
                        // hide the payroll item from the ui and data recovery
                        originalCompanyPayrollItem.getQbdtPayrollItemInfo().setToken(Company.EXCLUDE_TOKEN);
                    }
                    originalCompanyPayrollItem.setAdditionalPayrollItem(companyPayrollItem);
                    Application.save(originalCompanyPayrollItem);
                }
            }
        }

        companyPayrollItem.setPayrollItem(mPayrollItem);
        companyPayrollItem.setSourcePayrollItemId(mCompanyPayrollItemDTO.getSourcePayrollItemId());
        companyPayrollItem.setSourceDescription(mCompanyPayrollItemDTO.getSourcePayrollItemDescription());
        companyPayrollItem.setStatus(mCompanyPayrollItemDTO.getPayrollItemStatus());
        companyPayrollItem.setTaxFormLine(mCompanyPayrollItemDTO.getTaxFormLine());
        companyPayrollItem.setW2Code(mCompanyPayrollItemDTO.getW2Code() == null ? -1 : mCompanyPayrollItemDTO.getW2Code());
        companyPayrollItem.setIsArchived(mCompanyPayrollItemDTO.isArchived());
        //todo If this is an update, do we need to re-run payroll item-specific validations on existing paycheck line items?

        copyQbdtPayrollItemInfo(companyPayrollItem);

        companyPayrollItem = Application.save(companyPayrollItem);
        companyPayrollItem.setCompany(mCompany);
        mCompany.addCompanyPayrollItem(companyPayrollItem);

        Application.getSessionCache().addPrimaryKey(companyPayrollItem.getNaturalKey(), companyPayrollItem.getId());

        copyTaxableToLaws(companyPayrollItem);

        processResult.setResult(companyPayrollItem);
        return processResult;
    }

    /**
     * Validate process prarameters.
     *
     * @return ProcessResult - containing any validation errors
     */
    public ProcessResult validate() {
        ProcessResult validationResult = Validator.validCompanyParameters(mSourceSystemCd,
                                                                          mSourceCompanyId);

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if (mCompanyPayrollItemDTO == null) {
            validationResult.getMessages().InvalidValue(EntityName.PayrollItem, null, "Company Payroll Item is null");
        } else {
            validationResult.merge(mCompanyPayrollItemDTO.validate());
        }

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);
        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                                                               mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        mPayrollItem = PayrollServices.entityFinder.findById(PayrollItem.class, mCompanyPayrollItemDTO.getPayrollItemCode());
        if (mPayrollItem == null) {
            validationResult.getMessages().InvalidValue(EntityName.PayrollItem,
                                                        mCompanyPayrollItemDTO.getPayrollItemCode().toString(),
                                                        "Payroll item not found in static data");
        }

        for (String companyLawSourceId : mCompanyPayrollItemDTO.getTaxableToCompanyLawIds()) {
            CompanyLaw law = CompanyLaw.findCompanyLawBySourceId(mCompany, companyLawSourceId);

            if (law == null) {
                System.out.println("PSID: "+mCompany.getSourceCompanyId()+" Company law with source id: "+companyLawSourceId+ " applicable to "+mCompanyPayrollItemDTO.getSourcePayrollItemId() +" not found.");
            }
        }

        return validationResult;
    }

    private void copyQbdtPayrollItemInfo(CompanyPayrollItem pCompanyPayrollItem) {
        QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO = mCompanyPayrollItemDTO.getQBDTPayrollItemInfoDTO();
        if(qbdtPayrollItemInfoDTO != null) {
            QbdtPayrollItemInfo qbdtPayrollItemInfo = pCompanyPayrollItem.getQbdtPayrollItemInfo();
            if(qbdtPayrollItemInfo == null) {
                qbdtPayrollItemInfo = new QbdtPayrollItemInfo();
                pCompanyPayrollItem.setQbdtPayrollItemInfo(qbdtPayrollItemInfo);
                qbdtPayrollItemInfo.setCompany(pCompanyPayrollItem.getCompany());
                qbdtPayrollItemInfo.setCompanyPayrollItem(pCompanyPayrollItem);
                qbdtPayrollItemInfo.setRatePushToken(-1);
            }
            qbdtPayrollItemInfo.setListId(qbdtPayrollItemInfoDTO.getListId());
            qbdtPayrollItemInfo.setAdjustsGross(qbdtPayrollItemInfoDTO.adjustsGross());
            qbdtPayrollItemInfo.setAgencyId(qbdtPayrollItemInfoDTO.getAgencyId());
            qbdtPayrollItemInfo.setBasedOnQuantity(qbdtPayrollItemInfoDTO.isBasedOnQuantity());
            qbdtPayrollItemInfo.setDefaultLimit(qbdtPayrollItemInfoDTO.getDefaultLimit());
            qbdtPayrollItemInfo.setDefaultRate(qbdtPayrollItemInfoDTO.getDefaultRate());
            qbdtPayrollItemInfo.setDefaultRateType(qbdtPayrollItemInfoDTO.getDefaultRateType());
            qbdtPayrollItemInfo.setExpenseAccount(qbdtPayrollItemInfoDTO.getExpenseAccount());
            qbdtPayrollItemInfo.setExpenseByJob(qbdtPayrollItemInfoDTO.expenseByJob());
            qbdtPayrollItemInfo.setIsDeleted(qbdtPayrollItemInfoDTO.isDeleted());
            qbdtPayrollItemInfo.setIsEmployeePaid(qbdtPayrollItemInfoDTO.isEmployeePaid());
            qbdtPayrollItemInfo.setLiabilityAccount(qbdtPayrollItemInfoDTO.getLiabilityAccount());
            qbdtPayrollItemInfo.setLiabilityAgency(qbdtPayrollItemInfoDTO.getLiabilityAgency());
            qbdtPayrollItemInfo.setOnService(qbdtPayrollItemInfoDTO.isOnService());
            qbdtPayrollItemInfo.setPayType(qbdtPayrollItemInfoDTO.getPayType());
            qbdtPayrollItemInfo.setSpecialType(qbdtPayrollItemInfoDTO.getSpecialType());
            qbdtPayrollItemInfo.setEarningsTable(qbdtPayrollItemInfoDTO.isEarningsTable());
            qbdtPayrollItemInfo.setOvertimeMultiplier(qbdtPayrollItemInfoDTO.getOvertimeMultiplier());
            qbdtPayrollItemInfo.setDetailType(qbdtPayrollItemInfoDTO.getDetailType());
            Application.save(qbdtPayrollItemInfo);
        } else if(pCompanyPayrollItem.getQbdtPayrollItemInfo() != null) {
            pCompanyPayrollItem.setQbdtPayrollItemInfo(null);
        }
    }

    // delete and recreate tax associations
    private void copyTaxableToLaws(CompanyPayrollItem pCompanyPayrollItem) {
        for (PayrollItemTaxableTo payrollItemTaxableTo : pCompanyPayrollItem.getPayrollItemTaxableToCollection()) {
            Application.delete(payrollItemTaxableTo);
        }

        for (String companyLawSourceId : mCompanyPayrollItemDTO.getTaxableToCompanyLawIds()) {
            CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(mCompany, companyLawSourceId);
            
            if (companyLaw!=null) {
                PayrollItemTaxableTo payrollItemTaxableTo = new PayrollItemTaxableTo();
                payrollItemTaxableTo.setCompanyLaw(companyLaw);
                payrollItemTaxableTo.setCompanyPayrollItem(pCompanyPayrollItem);
                payrollItemTaxableTo = Application.save(payrollItemTaxableTo);
                pCompanyPayrollItem.getPayrollItemTaxableToCollection().add(payrollItemTaxableTo);
            }
        }
    }
}
