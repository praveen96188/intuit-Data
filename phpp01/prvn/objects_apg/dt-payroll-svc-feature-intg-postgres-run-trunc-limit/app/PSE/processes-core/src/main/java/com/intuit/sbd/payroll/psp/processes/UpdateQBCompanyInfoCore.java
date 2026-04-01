/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/UpdateQBCompanyInfoCore.java#6 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.common.utils.RealmLogHelper;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.processes.common.CompanyRealmValidator;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Core process for updating a Quickbooks Information for a company.
 *
 * @author Marcela Villani
 */
public class UpdateQBCompanyInfoCore extends Process implements IProcess {

    private Company domainCompany;
    private CompanyDTO dtoCompany;
    private SourceSystemCode sourceSystemCd;
    private String sourceCompanyId;
    private QuickbooksInfoDTO dtoQuickBooksInfo;
    private CompanyRealmValidator companyRealmValidator;

    private static final String[] ACCOUNTANT_APPIDS = {"belacct", "accountant"};
    private static final String RELEASE_VER = "R";


    public Company getUpdatedCompany() {
        return domainCompany;
    }

    public UpdateQBCompanyInfoCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                   CompanyDTO pCompanyDTO) {
        sourceSystemCd = pSourceSystemCd;
        sourceCompanyId = pSourceCompanyId;
        dtoCompany = pCompanyDTO;
        companyRealmValidator = new CompanyRealmValidator();
        SystemParameter.findSystemParameter(SystemParameter.Code.PSP_DATE_OFFSET);
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        domainCompany.setNextEmployeeId(dtoCompany.getNextEmployeeId());
        domainCompany.setNextPaycheckId(dtoCompany.getNextPaycheckId());
        domainCompany.setNextPayrollItemId(dtoCompany.getNextPayrollItemId());
        domainCompany.setNextPayrollTransactionId(dtoCompany.getNextPayrollTransactionId());


        QuickbooksInfo qbInfo = domainCompany.getQuickbooksInfo();

        if (qbInfo == null) {
            qbInfo = new QuickbooksInfo();
        }
        if (dtoCompany.getQuickBooksInfo() != null) {
            if (canUpdateLicenseNumber(qbInfo, dtoCompany.getQuickBooksInfo())) {
                if (qbInfo.getApplicationVersion() != null && (!qbInfo.getApplicationVersion().equals(dtoCompany.getQuickBooksInfo().getApplicationVersion())) ||
                        qbInfo.getLicenseNumber() != null && (!qbInfo.getLicenseNumber().equals(dtoCompany.getQuickBooksInfo().getLicenseNumber()))) {

                    ArrayList<String> details = new ArrayList<String>();
                    ArrayList<String> oldValues = new ArrayList<String>();
                    ArrayList<String> newValues = new ArrayList<String>();
                    if (qbInfo.getApplicationVersion() != null) {
                        if (!qbInfo.getApplicationVersion().equals(dtoCompany.getQuickBooksInfo().getApplicationVersion())) {
                            details.add("Application Version");
                            oldValues.add(qbInfo.getApplicationVersion());
                            newValues.add(dtoCompany.getQuickBooksInfo().getApplicationVersion());
                        }
                    }

                    if (qbInfo.getLicenseNumber() != null) {
                        if (!qbInfo.getLicenseNumber().equals(dtoCompany.getQuickBooksInfo().getLicenseNumber())) {
                            details.add("License Number");
                            oldValues.add(qbInfo.getLicenseNumber());
                            newValues.add(dtoCompany.getQuickBooksInfo().getLicenseNumber());
                        }
                    }
                    CompanyEvent.createQuickBooksInfoChangedEvent(domainCompany, details, oldValues, newValues);
                }
                qbInfo.setApplicationId(dtoCompany.getQuickBooksInfo().getApplicationId());
                qbInfo.setApplicationVersion(dtoCompany.getQuickBooksInfo().getApplicationVersion());
                qbInfo.setLicenseNumber(dtoCompany.getQuickBooksInfo().getLicenseNumber());
                qbInfo.setQuickbooksSku(dtoCompany.getQuickBooksInfo().getQuickbooksSku());

            }

            qbInfo.setTaxTableId(dtoCompany.getQuickBooksInfo().getTaxTableId());
            qbInfo.setFileId(dtoCompany.getQuickBooksInfo().getFileId());

            qbInfo.setProcessTransmissions(dtoCompany.getQuickBooksInfo().isProcessTransmissions());
            qbInfo.setAllowTransmissions(dtoCompany.getQuickBooksInfo().isAllowTransmissions());

            // set SymphonyOnBoardVersion if needed
            if (qbInfo.getSymphonyOnBoardVersion() == null && domainCompany.onUsageBilling()) {
                qbInfo.setSymphonyOnBoardVersion(qbInfo.getApplicationVersion());
            }

            if (dtoCompany.getQuickBooksInfo().getIAMRealmId() != null) {
                if(qbInfo.getIAMRealmId()==null){
                    //here QBInfoRealm in request is not null, but previous qbRealm is null, so adding realmId in qbRealm
                    String log = RealmLogHelper.getRealmEventMessage(RealmLogHelper.QB_REALM_ADD, dtoCompany.getQuickBooksInfo().getIAMRealmId(), null, domainCompany, null);
                    Application.printStackTrace(log);
                }else{
                    //here QBInfoRealm in request is not null, and previous qbRealm is also not null, so updating realmId in qbRealm
                    String log = RealmLogHelper.getRealmEventMessage(RealmLogHelper.QB_REALM_UPDATE, dtoCompany.getQuickBooksInfo().getIAMRealmId(), qbInfo.getIAMRealmId(), domainCompany, null);
                    Application.printStackTrace(log);
                }
                qbInfo.setIAMRealmId(dtoCompany.getQuickBooksInfo().getIAMRealmId());
                if (domainCompany.getIAMRealmId() == null) {
                    //companyRealmId is null, so adding new qbinfo realm id in comapnyrealm
                    String log = RealmLogHelper.getRealmEventMessage(RealmLogHelper.COMPANY_REALM_ADD, dtoCompany.getQuickBooksInfo().getIAMRealmId(), null, domainCompany, "added from QBRealm");
                    Application.printStackTrace(log);
                    domainCompany.setIAMRealmId(qbInfo.getIAMRealmId());
                    CompanyEvent.createCompanyInfoChangeEvent(domainCompany, "NOT AVAILABLE",
                            qbInfo.getIAMRealmId(), EventTypeCode.RealmIdAdded);
                }
            }
            if (dtoCompany.getQuickBooksInfo().getWatermarkDate() != null) {
                qbInfo.setWatermarkDate(dtoCompany.getQuickBooksInfo().getWatermarkDate());
            }
        } else {
            qbInfo.setApplicationId(null);
            qbInfo.setApplicationVersion(null);
            qbInfo.setTaxTableId(null);
            qbInfo.setLicenseNumber(null);
        }

        domainCompany = Application.save(domainCompany);

        return processResult;
    }

    public static boolean canUpdateLicenseNumber(QuickbooksInfo pQbInfo, QuickbooksInfoDTO pQbInfoDTO) {

         // if it is a new install or the first update post ratable
        if (pQbInfo.getLicenseNumber() == null || pQbInfo.getQuickbooksSku() == null) {
            return true;
        }
        try {

            String releaseVersionDB = pQbInfo.getApplicationVersion().split("\\.")[2];
            // if upgrade to Alpha/Beta upgrade change license number irrespective of movement to higher/lower version.
            if (releaseVersionDB != null && !releaseVersionDB.equals(RELEASE_VER)) {
                return true;
            }

            int majorVersionDB = Integer.parseInt(pQbInfo.getApplicationVersion().split("\\.")[0]);
            int majorVersionIncoming = Integer.parseInt(pQbInfoDTO.getApplicationVersion().split("\\.")[0]);
            List<String> acctList = Arrays.asList(ACCOUNTANT_APPIDS);
            // if upgrade of major version
            if (majorVersionIncoming > majorVersionDB) {
                return true;
            } else if (pQbInfoDTO.getQuickbooksSku() == null) {
                // if input SKU is null then don`t update the license
                return false;
            } else if (majorVersionIncoming == majorVersionDB) {
                // if major version of input is equal to major version in the DB

                // upgrade license if input license equal to DB license

                if (pQbInfoDTO.getLicenseNumber().equals(pQbInfo.getLicenseNumber())) {
                    return true;
                } else if (acctList.contains(pQbInfo.getQuickbooksSku()) && !acctList.contains(pQbInfoDTO.getQuickbooksSku())) {
                    // Update the license if PSP DB has accountant Sku irrespective of incoming Sku because for us to
                    // reach here, incoming SKU != DB SKU and DB SKU is an accontant SKU, so incoming may or may not
                    // be accountant SKU, in either case we want to update.
                    return true;
                } else {
                    // to reach here:
                    // 1. Incoming is accountant
                    // 2. Incoming Major == DB Major
                    return false;
                }
            } else if (majorVersionIncoming < majorVersionDB) {
                //if major version of input is less than major version of DB
                //Update the license number only if PSP DB has accountant sku and the incoming request is that of a client
                if (acctList.contains(pQbInfo.getQuickbooksSku()) && !acctList.contains(pQbInfoDTO.getQuickbooksSku())) {
                    return true;
                }
            }
        } catch (Exception e){
            return false;
        }

        return false;

    }


    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (dtoCompany == null) {
            validationResult.getMessages().CompanyNotSpecified(EntityName.Company, null);
            return validationResult;
        }

        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCd, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Validate DTO
        validationResult.merge(dtoCompany.validateCompanyDTO());
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Validate company exists
        domainCompany = Company.findCompany(sourceCompanyId, sourceSystemCd);
        if (domainCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                                                               sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        validationResult.merge(companyRealmValidator.validate(CompanyRealmValidator.CompanyCoreEventType.QB_COMPANY_UPDATE, domainCompany, dtoCompany));

        return validationResult;
    }
}