/*
 * $Id: //psp/dev/Adapters/QBOE/src/com/intuit/sbd/payroll/psp/webservices/DDCompany.java#3 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import intuit.osp.common.wsf.base.WSException;
import intuit.osp.common.wsf.base.WSValidationException;
import intuit.osp.common.wsf.server.WS;
import intuit.osp.common.wsf.server.WSServerContext;
import intuit.osp.pse.dd.wsapi.xsd.companyex.CompanyEx;
import intuit.osp.pse.dd.wsapi.xsd.ddcompanyadd.DDCompanyAdd;
import intuit.osp.pse.dd.wsapi.xsd.ddcompanyaddrs.DDCompanyAddRs;
import intuit.osp.pse.dd.wsapi.xsd.ddcompanycancel.DDCompanyCancel;
import intuit.osp.pse.dd.wsapi.xsd.ddcompanycancelrs.DDCompanyCancelRs;
import intuit.osp.pse.dd.wsapi.xsd.ddcompanyexret.DDCompanyExRet;
import intuit.osp.pse.dd.wsapi.xsd.ddcompanyfundingmodelupdate.DDCompanyFundingModelUpdate;
import intuit.osp.pse.dd.wsapi.xsd.ddcompanyfundingmodelupdaters.DDCompanyFundingModelUpdateRs;
import intuit.osp.pse.dd.wsapi.xsd.ddcompanygetinfo.DDCompanyGetInfo;
import intuit.osp.pse.dd.wsapi.xsd.ddcompanygetinfors.DDCompanyGetInfoRs;
import intuit.osp.pse.dd.wsapi.xsd.ddcompanylimitsupdate.DDCompanyLimitsUpdate;
import intuit.osp.pse.dd.wsapi.xsd.ddcompanylimitsupdaters.DDCompanyLimitsUpdateRs;
import intuit.osp.pse.dd.wsapi.xsd.ddcompanyquery.DDCompanyQuery;
import intuit.osp.pse.dd.wsapi.xsd.ddcompanyqueryrs.DDCompanyQueryRs;
import intuit.osp.pse.dd.wsapi.xsd.ddcompanyret.DDCompanyRet;
import intuit.osp.pse.dd.wsapi.xsd.ddcompanystrikeadd.DDCompanyStrikeAdd;
import intuit.osp.pse.dd.wsapi.xsd.ddcompanystrikeaddrs.DDCompanyStrikeAddRs;
import intuit.osp.pse.dd.wsapi.xsd.ddcompanystrikecancel.DDCompanyStrikeCancel;
import intuit.osp.pse.dd.wsapi.xsd.ddcompanystrikecancelrs.DDCompanyStrikeCancelRs;
import intuit.osp.pse.dd.wsapi.xsd.ddcompanyupdate.DDCompanyUpdate;
import intuit.osp.pse.dd.wsapi.xsd.ddcompanyupdaters.DDCompanyUpdateRs;
import org.w3c.dom.Element;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Jeff Jones
 * @author Dawn Martens
 */
public class DDCompany extends WS {
    private static SpcfLogger logger = Application.getLogger(DDCompany.class);

    public static final String SERVICE_NAME = "DDCompany";
    private static final String DDCOMPANY_ALREADY_EXISTS = "1013";

    WSServerContext context = null;
    //    DDCompanyAddRs ddCompanyAddRs = null;
    DDCompanyUpdateRs ddCompanyUpdateRs = null;
    DDCompanyCancelRs ddCompanyCancelRs = null;
    DDCompanyGetInfoRs ddCompanyGetInfoRs = null;

    public Element add(Element requestDoc) throws WSException {
        Element returnDoc;
        String[] expectedErrorCodes = {"141","137","138","5001","120","127","125","169","177","207","1044","117",
                "1010","1012","1038","1039","1013", "1063", "1101","1040","1011","1045","161","5002"};

        try {
            //Get the incoming companyDTO
            PayrollServices.beginUnitOfWork();

            context = new WSServerContext("DDCompany", "add");
            DDCompanyAdd ddCompanyAdd = (DDCompanyAdd) context.translateInputElement(requestDoc);

            //Build the response to give back to the client
            DDCompanyAddRs ddCompanyAddRs = (DDCompanyAddRs) context.getOutputDTO();
            DDCompanyRet ddCompanyRet = ddCompanyAddRs.getDDCompanyRet();

            //Create a new company DO
            Company domainCompany = null;
            CompanyDTO dtoCompany = new CompanyDTO();
            DDServiceInfoDTO dtoDDService = new DDServiceInfoDTO();

            ProcessResult<Company> result = new ProcessResult<Company>();

            //Get all data from the request into the domainCompany
            populateCompanyDTOFromLegacy(ddCompanyAdd, dtoCompany);

            //To maintain backwards-compatibility, if the company exists, we update the company and the service and reactivate the service
            //Otherwise, we add the company and the service
            Company duplicateIdCompany = Company.findCompany(ddCompanyAdd.getCompany().getCompanyID(),
                    SourceSystemCode.valueOf(ddCompanyAdd.getCompany().getSourceSystemCd()));

            if (duplicateIdCompany != null) {
                //Reactivate service before updating company because company update is not allowed for cancelled service
                ProcessResult<CompanyService> ddServiceAddProcessResult = PayrollServices.companyManager.reactivateService(
                        SourceSystemCode.valueOf(dtoCompany.getSourceSystemCd().toString()),
                        dtoCompany.getCompanyId(), ServiceCode.DirectDeposit);
                result.merge(ddServiceAddProcessResult);
                //Update service
                if (result.isSuccess()) {
                    populateServiceCodeFromLegacy(ddCompanyAdd, dtoDDService);
                    ProcessResult<CompanyService> updateServiceProcessResult = PayrollServices.companyManager.updateService(
                            SourceSystemCode.valueOf(dtoCompany.getSourceSystemCd().toString()), dtoCompany.getCompanyId(), dtoDDService);
                    result.merge(updateServiceProcessResult);
                    if (! updateServiceProcessResult.isSuccess()) {
                        CompanyService companyService = CompanyService.findCompanyService(duplicateIdCompany, ServiceCode.DirectDeposit);
                        String qboeStatus = DDCodeToPSP.getQBOEServiceStatus(companyService);
                        if ("TERMD".equals(qboeStatus)) {
                            DDCommon.replacePSPError(result, "1101", "177", duplicateIdCompany);
                        }
                        if (! "ACTV".equals(qboeStatus)) {
                            DDCommon.replacePSPError(result, "1101", "177", duplicateIdCompany);
                        }
                    }
                    else {
                        //Update company
                        result = PayrollServices.companyManager.updateCompany(
                                duplicateIdCompany.getSourceSystemCd(),
                                duplicateIdCompany.getSourceCompanyId(),
                                dtoCompany);
                        domainCompany = result.getResult();
                        if (! result.isSuccess()) {
                            DDCommon.replacePSPError(result, "1101", "177", domainCompany);
                        }
                    }
                }
                else {
                    CompanyService ddService = CompanyService.findCompanyService(duplicateIdCompany, ServiceCode.DirectDeposit);
                    String qboeStatus = DDCodeToPSP.getQBOEServiceStatus(ddService);
                    if ("TERMD".equals(qboeStatus)) {
                        DDCommon.replacePSPError(result, "1101", "1012", duplicateIdCompany);
                    }

                    // Check for message 1013 - DDComany already exists and change the level from ERROR to WARNING
                    if (result.getMessages().size() == 1 && result.getMessages().get(0).getMessageCode().equals(DDCOMPANY_ALREADY_EXISTS)) {
                        result.getMessages().get(0).setLevel(MessageInfo.MessageLevel.WARNING);
                        build_DDCompanyRet(duplicateIdCompany, ddCompanyRet);
                        ddCompanyAddRs.setDDCompanyRet(ddCompanyRet);
                        PayrollServices.rollbackUnitOfWork();
                        ddCompanyAddRs.setResponseStatus(DDCommon.build_ResponseStatus(result, expectedErrorCodes));
                        returnDoc = context.translateOutputDTO();

                        return returnDoc;
                    }
                }
            } else {
                //Execute the prcoess flow for adding a new company, and gather the result and newly created company afterwards
                result = PayrollServices.companyManager.addCompany(dtoCompany);

                //Need to get the company from the process flow because SPCF follows JSR-220, which states that the original Entity is not modified
                domainCompany = result.getResult();

                //If adding the company was successful, add the service
                if (result.isSuccess()) {
                    populateServiceCodeFromLegacy(ddCompanyAdd, dtoDDService);
                    ProcessResult<CompanyService> ddServiceAddProcessResult = PayrollServices.companyManager.addService(
                            SourceSystemCode.valueOf(dtoCompany.getSourceSystemCd().toString()), dtoCompany.getCompanyId(), dtoDDService);
                    result.merge(ddServiceAddProcessResult);
                    if (! result.isSuccess()) {
                        DDCommon.replacePSPError(result, "1101", "177", domainCompany);
                    }
                }
            }

            if (result.isSuccess()) {
                build_DDCompanyRet(domainCompany, ddCompanyRet);
                ddCompanyAddRs.setDDCompanyRet(ddCompanyRet);
                PayrollServices.commitUnitOfWork();
            } else {
                ddCompanyAddRs.setDDCompanyRet(null);
                PayrollServices.rollbackUnitOfWork();
            }

            ddCompanyAddRs.setResponseStatus(DDCommon.build_ResponseStatus(result, expectedErrorCodes));
            returnDoc = context.translateOutputDTO();
        }
        catch (WSValidationException e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e);
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        
        return returnDoc;
    }

    public Element update(Element requestDoc) throws WSException {
        String[] expectedErrorCodes = {"141","137","138","5001","120","127","125","169","177","207","1044","117",
                "1011","1045"};
        Element returnDoc;
        try {
            PayrollServices.beginUnitOfWork();
            context = new WSServerContext("DDCompany", "update");
            DDCompanyUpdate ddCompanyUpdate = (DDCompanyUpdate) context.translateInputElement(requestDoc);

            CompanyDTO dtoCompany = new CompanyDTO();
            DDServiceInfoDTO dtoDDService = new DDServiceInfoDTO();

            //Get all data from the request into the domainCompany
            populateCompanyUpdateDTOFromLegacy(ddCompanyUpdate, dtoCompany);
            ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(
                    SourceSystemCode.valueOf(dtoCompany.getSourceSystemCd().toString()),
                    dtoCompany.getCompanyId(), dtoCompany);
            Company domainCompany = result.getResult();

            //update service
            if (result.isSuccess()) {
                build_DDCompanyServiceInfoFromUpdate(dtoDDService, ddCompanyUpdate);

                ProcessResult<CompanyService> updateServiceProcessResult = PayrollServices.companyManager.updateService(
                        SourceSystemCode.valueOf(dtoCompany.getSourceSystemCd().toString()),
                        dtoCompany.getCompanyId(), dtoDDService);
                result.merge(updateServiceProcessResult);
            }
            else {
                domainCompany = Company.findCompany(
                        dtoCompany.getCompanyId(), SourceSystemCode.valueOf(dtoCompany.getSourceSystemCd().toString()));
                DDCommon.replacePSPError(result, "1101", "177", domainCompany);
            }

            ddCompanyUpdateRs = (DDCompanyUpdateRs) context.getOutputDTO();
            DDCompanyRet ddCompanyRet = ddCompanyUpdateRs.getDDCompanyRet();
            if (result.isSuccess()) {
                build_DDCompanyRet(domainCompany, ddCompanyRet);
                ddCompanyUpdateRs.setDDCompanyRet(ddCompanyRet);
                PayrollServices.commitUnitOfWork();
            } else {
                ddCompanyUpdateRs.setDDCompanyRet(null);
                PayrollServices.rollbackUnitOfWork();
            }

            ddCompanyUpdateRs.setResponseStatus(DDCommon.build_ResponseStatus(result, expectedErrorCodes));
            returnDoc = context.translateOutputDTO();

        } catch (WSValidationException e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e.getCause());
            throw e;
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e.getCause());
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return returnDoc;
    }

    public Element cancel(Element requestDoc) throws WSException {
        Element returnDoc;
        String[] expectedErrorCodes = {"137","138","125","169","117","1010","1022","1023","1025","1024","175","182",
                "227","145","168","215","217","170","218","1026","1027"};

        try {
            PayrollServices.beginUnitOfWork();
            context = new WSServerContext("DDCompany", "cancel");
            DDCompanyCancel ddCompanyCancel = (DDCompanyCancel) context.translateInputElement(requestDoc);

            ProcessResult<Company> result = new ProcessResult<Company>();

            ProcessResult<CompanyService> cancelServiceProcesResult = PayrollServices.companyManager.deactivateService(
                    SourceSystemCode.valueOf(ddCompanyCancel.getSourceSystemCd()),
                    ddCompanyCancel.getCompanyID(), ServiceCode.DirectDeposit);
            result.merge(cancelServiceProcesResult);

            ddCompanyCancelRs = (DDCompanyCancelRs) context.getOutputDTO();
            DDCompanyRet ddCompanyRet = ddCompanyCancelRs.getDDCompanyRet();
            if (result.isSuccess()) {
                Company canceledCompany = cancelServiceProcesResult.getResult().getCompany();
                build_DDCompanyRet(canceledCompany, ddCompanyRet);
                ddCompanyCancelRs.setDDCompanyRet(ddCompanyRet);
                PayrollServices.commitUnitOfWork();
            } else {
                com.intuit.sbd.payroll.psp.domain.Company domainCompany = Company.findCompany(
                            ddCompanyCancel.getCompanyID(),
                            SourceSystemCode.valueOf(ddCompanyCancel.getSourceSystemCd()));
                if (domainCompany != null) {
                    CompanyService domainCompanyService = CompanyService.findCompanyService(
                            domainCompany, ServiceCode.DirectDeposit);
                    String qboeStatus = DDCodeToPSP.getQBOEServiceStatus(domainCompanyService);
                    if ("CNCLD".equals(qboeStatus)) {
                        DDCommon.replacePSPError(result, "1101", "1022", domainCompany);
                    } else if ("TERMD".equals(qboeStatus)) {
                        DDCommon.replacePSPError(result, "1101", "1023", domainCompany);
                    } else if ("SSPND".equals(qboeStatus)) {
                        DDCommon.replacePSPError(result, "1101", "1025", domainCompany);
                    } else if ("HOLD".equals(qboeStatus)) {
                        DDCommon.replacePSPError(result, "1101", "1024", domainCompany);
                    } else if ("PNDTERMN".equals(qboeStatus)) {
                        DDCommon.replacePSPError(result, "1101", "1026", domainCompany);
                    } else if ("PNDACTVN".equals(qboeStatus)) {
                        DDCommon.replacePSPError(result, "1101", "1027", domainCompany);
                    }                    
                }
                ddCompanyCancelRs.setDDCompanyRet(null);
                PayrollServices.rollbackUnitOfWork();
            }

            ddCompanyCancelRs.setResponseStatus(DDCommon.build_ResponseStatus(result, expectedErrorCodes));

            returnDoc = context.translateOutputDTO();
        } catch (WSValidationException e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e.getCause());
            throw e;
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e.getCause());
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return returnDoc;
    }

    public Element getInfo(Element requestDoc) throws WSException {
        String[] expectedErrorCodes = {"137","138","125","10003","117","10004"};
        Element returnDoc;

        try {
            PayrollServices.beginUnitOfWork();
            context = new WSServerContext("DDCompany", "getInfo");
            DDCompanyGetInfo ddCompanyGetInfo = (DDCompanyGetInfo) context.translateInputElement(requestDoc);

            String sourceSystemCd = ddCompanyGetInfo.getSourceSystemCd();
            String sourceCompanyId = ddCompanyGetInfo.getCompanyID();

            ProcessResult<CompanyService> result = validateRequest(SourceSystemCode.valueOf(sourceSystemCd), sourceCompanyId, ServiceCode.DirectDeposit);

            //Build the response to give back to the client
            ddCompanyGetInfoRs = (DDCompanyGetInfoRs) context.getOutputDTO();
            DDCompanyRet ddCompanyRet = ddCompanyGetInfoRs.getDDCompanyRet();

            if (result.isSuccess()) {
                build_DDCompanyRet(result.getResult().getCompany(), ddCompanyRet);
                ddCompanyGetInfoRs.setDDCompanyRet(ddCompanyRet);
            } else {
                ddCompanyGetInfoRs.setDDCompanyRet(null);
            }

            ddCompanyGetInfoRs.setResponseStatus(DDCommon.build_ResponseStatus(result, expectedErrorCodes));

            returnDoc = context.translateOutputDTO();
            PayrollServices.commitUnitOfWork();
        } catch (WSValidationException e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e.getCause());
            throw e;
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e.getCause());
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return returnDoc;
    }

    private ProcessResult<CompanyService> validateRequest(SourceSystemCode pSourceSystem, String pSourceCompanyId, ServiceCode serviceCd) {
        ProcessResult<CompanyService> validationResult = new ProcessResult<CompanyService>();

        //Validate foundCompany parameters
        validationResult.merge(Validator.validCompanyParameters(pSourceSystem, pSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //See if company exists
        Company foundCompany = Company.findCompany(pSourceCompanyId, pSourceSystem);
        if (foundCompany == null) {
            validationResult.getMessages().CompanyDoesNotExistSourceSystemCdSourceSystemId(EntityName.Company,
                    pSourceCompanyId, pSourceSystem.toString(), pSourceCompanyId);
            return validationResult;
        }

        //Ensure service code is not null
        if (serviceCd == null) {
            validationResult.getMessages().CompanyServiceNotSpecified(EntityName.Service, null);
            return validationResult;
        }

        //Ensure company exists on the specified service
        CompanyService companyService = CompanyService
                .findCompanyService(foundCompany, ServiceCode.valueOf(serviceCd.toString()));
        if (companyService == null) {
            Service service = Application.findById(Service.class, ServiceCode.valueOf(serviceCd.toString()));

            String serviceName = service.getName().toLowerCase();
            validationResult.getMessages().CompanyDoesNotExistOnService(EntityName.Company, pSourceCompanyId,
                    pSourceSystem.toString(), pSourceCompanyId, serviceName);
            return validationResult;
        }

        validationResult.setResult(companyService);

        return validationResult;

    }

    private void populateCompanyDTOFromLegacy(DDCompanyAdd pCompanyAddDTO, CompanyDTO pDTOCompany) {
        intuit.osp.pse.dd.wsapi.xsd.company.Company company = pCompanyAddDTO.getCompany();
        build_CompanyDTO(company, pDTOCompany, pCompanyAddDTO.getPayrollFrequencyCd());
    }

    private void populateCompanyUpdateDTOFromLegacy(DDCompanyUpdate pCompanyUpdateDTO, CompanyDTO pDTOCompany) {
        intuit.osp.pse.dd.wsapi.xsd.company.Company company = pCompanyUpdateDTO.getCompany();
        build_CompanyDTO(company, pDTOCompany, pCompanyUpdateDTO.getPayrollFrequencyCd());
    }

    private void populateServiceCodeFromLegacy(DDCompanyAdd pCompanyAddDTO, DDServiceInfoDTO pDTOServiceInfo) {
        if (pCompanyAddDTO.getAveragePayrollAmt() != null) {
            pDTOServiceInfo.setAveragePayrollAmount(pCompanyAddDTO.getAveragePayrollAmt().setScale(2));
        }

        if (pCompanyAddDTO.getHighAnnualPayAmt() != null) {
            pDTOServiceInfo.setHighAnnualPayrollAmount(pCompanyAddDTO.getHighAnnualPayAmt().setScale(2));
        }
    }

    private void build_CompanyDTO(intuit.osp.pse.dd.wsapi.xsd.company.Company company, CompanyDTO companyDTO,
                                  String pPayrollFrequency) {
        companyDTO.setNotificationEmail(company.getNotificationEmail());

        companyDTO.setSourceSystemCd(SourceSystemCode.valueOf(company.getSourceSystemCd()));

        companyDTO.setCompanyId(company.getCompanyID());
        companyDTO.setFein(company.getFEIN());
        companyDTO.setLegalName(company.getLegalName());
        if (company.getDBA() != null) {
            companyDTO.setDBA(company.getDBA());
        }

        if (pPayrollFrequency != null) {
            PayrollFrequencyDTO freqValDTO = DDCodeToPSP.getPayrollFrequencyDTO(pPayrollFrequency);
            companyDTO.setPayrollFrequencyCd(freqValDTO);
        }

        companyDTO.setLegalAddress(build_AddressDTO(company.getLegalAddress()));
        companyDTO.setMailingAddress(build_AddressDTO(company.getMailingAddress()));

        build_ContactDTO(companyDTO, company.getContact());
    }


    private void build_DDCompanyServiceInfoFromUpdate(DDServiceInfoDTO pServiceInfoDTO,
                                                      DDCompanyUpdate pDDCompanyUpdate)
            throws Exception {
        if (pDDCompanyUpdate.getAveragePayrollAmt() != null) {
            pServiceInfoDTO.setAveragePayrollAmount(pDDCompanyUpdate.getAveragePayrollAmt().setScale(2));
        }

        if (pDDCompanyUpdate.getHighAnnualPayAmt() != null) {
            pServiceInfoDTO.setHighAnnualPayrollAmount(pDDCompanyUpdate.getHighAnnualPayAmt().setScale(2));
        }
    }

    private AddressDTO build_AddressDTO(intuit.osp.pse.dd.wsapi.xsd.address.Address pAddressDTO) {
        AddressDTO addressDTO = new AddressDTO();

        if (pAddressDTO != null) {
            addressDTO.setAddressLine1(pAddressDTO.getAddressLine1());
            if (pAddressDTO.getAddressLine2() != null) {
                addressDTO.setAddressLine2(pAddressDTO.getAddressLine2());
            }
            if (pAddressDTO.getAddressLine3() != null) {
                addressDTO.setAddressLine3(pAddressDTO.getAddressLine3());
            }
            addressDTO.setCity(pAddressDTO.getCity());
            addressDTO.setState(pAddressDTO.getState());
            addressDTO.setZipCode(pAddressDTO.getZipCode());
            if (pAddressDTO.getZipCodeExtension() != null) {
                addressDTO.setZipCodeExtension(pAddressDTO.getZipCodeExtension());
            }
            if (pAddressDTO.getCountry() != null) {
                addressDTO.setCountry(pAddressDTO.getCountry());
            }
        } else {
            String args[] = {"Address", "build_AddressBO"};
            throw new NullPointerException(DDCommon.getErrorMessage(DDCommon.npe_Error, args));
        }
        return addressDTO;
    }


    private void build_ContactDTO(CompanyDTO pCompanyDTO,
                                  List<intuit.osp.pse.dd.wsapi.xsd.contact.impl.ContactImpl> pContactList) {
        if (pContactList != null && pContactList.size() > 0) {
            Collection<ContactDTO> contactDTOs = new ArrayList();
            for (intuit.osp.pse.dd.wsapi.xsd.contact.impl.ContactImpl aPContactList : pContactList) {
                ContactDTO contactDTO = new ContactDTO();

                intuit.osp.pse.dd.wsapi.xsd.contact.Contact contact = aPContactList;

                contactDTO.setLastName(contact.getLastName());
                contactDTO.setFirstName(contact.getFirstName());

                if (contact.getMiddleName() != null) {
                    contactDTO.setMiddleName(contact.getMiddleName());
                }

                intuit.osp.pse.dd.wsapi.xsd.address.Address contactAddressDTO = contact.getAddress();
                if (contactAddressDTO != null) {
                    AddressDTO addressDO = build_AddressDTO(contactAddressDTO);
                    if (addressDO != null) {
                        contactDTO.setAddress(addressDO);
                    }
                }

                if (contact.getPhoneNumber() != null) {
                    contactDTO.setPhoneNumber(contact.getPhoneNumber());
                }

                if (contact.getEmail() != null) {
                    contactDTO.setEmail(contact.getEmail());
                }

                if (contact.getCommunicationPref() != null) {
                    CommunicationType domainCommType = DDCodeToPSP.getDomainCommunicationType(contact.getCommunicationPref());
                    contactDTO.setCommunicationTypeCd(domainCommType);
                }

                contactDTO.setAccountSignatory(contact.isAccountSignatory());
                ContactRole contactRole = ContactRole.valueOf(DDCodeToPSP.getContactRole(contact.getContactRoleCd()).toString());
                contactDTO.setContactRoleCd(contactRole);
                String sourceContactId = DDCommon.generateContactKey(contactDTO);
                contactDTO.setContactId(sourceContactId);

                contactDTOs.add(contactDTO);
            }

            pCompanyDTO.setContacts(contactDTOs);
        } else {
            String args[] = {"List", "build_ContactBO"};
            throw new NullPointerException(DDCommon.getErrorMessage(DDCommon.npe_Error, args));
        }
    }

    protected void build_DDCompanyRet(Company domainCompany, DDCompanyRet pDDCompanyRet) throws Exception {

        if (domainCompany != null) {
            pDDCompanyRet.setCompany(build_Company(domainCompany));
            if (domainCompany.getPayrollFrequency() != null) {
                pDDCompanyRet.setPayrollFrequency(domainCompany.getPayrollFrequency().getPayrollFreqCd());
            }

            DDCompanyServiceInfo domainDDCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                    .findCompanyService(domainCompany, ServiceCode.DirectDeposit);

            if (domainDDCompanyServiceInfo != null) {
                //todo:v2 take out all these checks for NULLs when SPCF has a common strategy
                if (domainDDCompanyServiceInfo.getAveragePayRunAmount() != null) {
                    SpcfMoney moneyAvgPayRunAmount = domainDDCompanyServiceInfo.getAveragePayRunAmount();
                    if (moneyAvgPayRunAmount != null) {
                        BigDecimal bdAvgPayRunAmount = new BigDecimal(
                                moneyAvgPayRunAmount.toString());
                        bdAvgPayRunAmount.setScale(2);
                        pDDCompanyRet.setAveragePayrollAmt(bdAvgPayRunAmount);
                    }
                }
                if (domainDDCompanyServiceInfo.getHighAnnualPayAmount() != null) {
                    SpcfMoney moneyHighPayRunAmount = domainDDCompanyServiceInfo.getHighAnnualPayAmount();
                    if (moneyHighPayRunAmount != null) {
                        BigDecimal bdHighPayRunAmount = new BigDecimal(
                                moneyHighPayRunAmount.toString());
                        bdHighPayRunAmount.setScale(2);
                        pDDCompanyRet.setHighAnnualPayAmt(bdHighPayRunAmount);
                    }
                }

                String strDDServiceStatus = DDCodeToPSP.getQBOEServiceStatus(domainDDCompanyServiceInfo);
                pDDCompanyRet.setCompanyDDStatusCd(strDDServiceStatus);
            }

            if (domainCompany.getFundingModel() != null) {
                pDDCompanyRet.setFundingModelCd(domainCompany.getFundingModel().getFundingModelCd());
                pDDCompanyRet.setFundingModelDesc(domainCompany.getFundingModel().getName());
            }
        } else {
            String args[] = {"CompanyBO", "build_CompanyRet"};
            throw new NullPointerException(DDCommon.getErrorMessage(DDCommon.npe_Error, args));
        }
    }

    private intuit.osp.pse.dd.wsapi.xsd.company.Company build_Company(Company companyDO) throws Exception {
        intuit.osp.pse.dd.wsapi.xsd.company.ObjectFactory companyObjectFactory =
                new intuit.osp.pse.dd.wsapi.xsd.company.ObjectFactory();
        intuit.osp.pse.dd.wsapi.xsd.address.ObjectFactory addressObjectFactory =
                new intuit.osp.pse.dd.wsapi.xsd.address.ObjectFactory();

        intuit.osp.pse.dd.wsapi.xsd.company.Company companyDTO = companyObjectFactory.createCompany();
        if (companyDO != null) {
            companyDTO.setSourceSystemCd(companyDO.getSourceSystemCd().toString());
            companyDTO.setCompanyID(companyDO.getSourceCompanyId());
            companyDTO.setFEIN(companyDO.getFedTaxId());
            companyDTO.setLegalName(companyDO.getLegalName());
            if (companyDO.getDbaName() != null) {
                companyDTO.setDBA(companyDO.getDbaName());
            }

            companyDTO.setNotificationEmail(companyDO.getNotificationEmail());

            Address addressDO = companyDO.getLegalAddress();
            companyDTO.setLegalAddress(addressObjectFactory.createAddress());
            build_Address(addressDO, companyDTO.getLegalAddress());

            addressDO = companyDO.getMailingAddress();
            companyDTO.setMailingAddress(addressObjectFactory.createAddress());
            build_Address(addressDO, companyDTO.getMailingAddress());

            DomainEntitySet<Contact> contactMap = companyDO.getContactCollection();

            if (contactMap != null && contactMap.size() > 0) {
                for (Contact contactDO : contactMap) {
                    intuit.osp.pse.dd.wsapi.xsd.contact.Contact contactDTO = build_Contact(contactDO);
                    companyDTO.getContact().add(contactDTO);
                }
            }
        } else {
            String args[] = {"CompanyBO", "build_Company"};
            throw new NullPointerException(DDCommon.getErrorMessage(DDCommon.npe_Error, args));
        }

        return companyDTO;
    }

    private void build_Address(Address pAddressDO, intuit.osp.pse.dd.wsapi.xsd.address.Address pAddressDTO)
            throws Exception {
        if (pAddressDTO != null) {
            if (pAddressDO != null) {
                pAddressDTO.setAddressLine1(pAddressDO.getAddressLine1());
                if (pAddressDO.getAddressLine2() != null) {
                    pAddressDTO.setAddressLine2(pAddressDO.getAddressLine2());
                }
                if (pAddressDO.getAddressLine3() != null) {
                    pAddressDTO.setAddressLine3(pAddressDO.getAddressLine3());
                }
                pAddressDTO.setCity(pAddressDO.getCity());
                pAddressDTO.setState(pAddressDO.getState());
                pAddressDTO.setZipCode(pAddressDO.getZipCode());
                if (pAddressDO.getZipCodeExtension() != null) {
                    pAddressDTO.setZipCodeExtension(pAddressDO.getZipCodeExtension());
                }
                if (pAddressDO.getCountry() != null) {
                    pAddressDTO.setCountry(pAddressDO.getCountry());
                }
            } else {
                String args[] = {"AddressBO", "build_Address"};
                throw new NullPointerException(DDCommon.getErrorMessage(DDCommon.npe_Error, args));
            }
        } else {
            String args[] = {"Address", "build_Address"};
            throw new NullPointerException(DDCommon.getErrorMessage(DDCommon.npe_Error, args));
        }
    }

    private intuit.osp.pse.dd.wsapi.xsd.contact.Contact build_Contact(Contact contactDO) throws Exception {
        intuit.osp.pse.dd.wsapi.xsd.contact.ObjectFactory contactObjectFactory =
                new intuit.osp.pse.dd.wsapi.xsd.contact.ObjectFactory();
        intuit.osp.pse.dd.wsapi.xsd.address.ObjectFactory addressObjectFactory =
                new intuit.osp.pse.dd.wsapi.xsd.address.ObjectFactory();
        intuit.osp.pse.dd.wsapi.xsd.contact.Contact contact = contactObjectFactory.createContact();
        if (contactDO != null) {
            String apiRoleCdValue = DDCodeToPSP.getQBOEContactRole(contactDO.getContactRoleCd());
            contact.setContactRoleCd(apiRoleCdValue);
            contact.setAccountSignatory(contactDO.getAuthSignerYnInd());
            contact.setLastName(contactDO.getLastName());
            contact.setFirstName(contactDO.getFirstName());

            if (contactDO.getMiddleName() != null) {
                contact.setMiddleName(contactDO.getMiddleName());
            }

            if (contactDO.getMailingAddress() != null) {
                Address mailingAddressDO = contactDO.getMailingAddress();
                if (mailingAddressDO != null) {
                    contact.setAddress(addressObjectFactory.createAddress());
                    build_Address(mailingAddressDO, contact.getAddress());
                }
            }

            contact.setPhoneNumber(contactDO.getPhone());
            contact.setEmail(contactDO.getEmail());

            if (contactDO.getCommunicationTypePreference() != null) {
                CommunicationType ddCode = contactDO.getCommunicationTypePreference();
                contact.setCommunicationPref(DDCodeToPSP.getQBOECommunicationTypePreference(ddCode));
            }
        } else {
            String args[] = {"ContactBO", "build_Contact"};
            throw new NullPointerException(DDCommon.getErrorMessage(DDCommon.npe_Error, args));
        }
        return contact;
    }


    /**
     * This method is used to query for Companies with DD Service.
     *
     * @param requestDoc
     * @return
     * @throws WSException
     */
    public Element query(Element requestDoc) throws WSException {
        String[] expectedErrorCodes = {"125"};
        WSServerContext wsServerContext = new WSServerContext(DDCompany.SERVICE_NAME,
                DDCompany.Operations.QUERY);

        try {
            WSServerContext context = new WSServerContext("DDCompany", "query");
            DDCompanyQuery queryRequest = (DDCompanyQuery) context.translateInputElement(requestDoc);
            DDCompanyQueryRs queryResponse = (DDCompanyQueryRs) context.getOutputDTO();


            executeDDQuery(queryRequest, queryResponse, expectedErrorCodes);

            Element responseDoc = context.translateOutputDTO();
            return responseDoc;
        } catch (WSValidationException e) {
            logger.error(e.getMessage(), e.getCause());
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }


    }

    private void executeDDQuery(
            DDCompanyQuery pDDCompanyQueryRequest,
            DDCompanyQueryRs pDDCompanyQueryResponse,
            String[] pExpectedErrorCodes) throws Exception {

        PayrollServices.beginUnitOfWork();
        try {

            ProcessResult validationResult = new ProcessResult();

            // Instantiate the SourceSystem Object
            SourceSystemCode sourceSystemCd = null;
            try {
                sourceSystemCd = SourceSystemCode.valueOf(pDDCompanyQueryRequest.getSourceSystemCd());
            } catch (Exception ex) {
            }

            if (sourceSystemCd == null) {
                validationResult.getMessages().InvalidSourceSystemCdSpecified(EntityName.Company, "", pDDCompanyQueryRequest.getSourceSystemCd());
                logger.warn(validationResult.getMessages().get(0).getMessage());

            } else {

                String searchCriteria = "";
                DomainEntitySet<CompanyService> companyServices = null;

                if (pDDCompanyQueryRequest.getCompanyID() != null) {

                    searchCriteria = " and SourceCompanyId=" + pDDCompanyQueryRequest.getCompanyID();
                    companyServices = CompanyService.findCompanyServicesBySourceCompanyId(
                            sourceSystemCd, ServiceCode.DirectDeposit, pDDCompanyQueryRequest.getCompanyID());

                } else if (pDDCompanyQueryRequest.getFEIN() != null) {

                    searchCriteria = " and FedTaxId=" + pDDCompanyQueryRequest.getFEIN();
                    companyServices = CompanyService.findCompanyServicesByFedTaxId(
                            sourceSystemCd, ServiceCode.DirectDeposit, pDDCompanyQueryRequest.getFEIN());

                } else if (pDDCompanyQueryRequest.getLegalName() != null) {

                    searchCriteria = " and LegalName=" + pDDCompanyQueryRequest.getLegalName();
                    companyServices = CompanyService.findCompanyServices(
                            sourceSystemCd, ServiceCode.DirectDeposit, pDDCompanyQueryRequest.getLegalName());

                } else {

                    searchCriteria = "";
                    companyServices = CompanyService.findCompanyServices(
                            sourceSystemCd, ServiceCode.DirectDeposit);

                }

                int n = 0;
                if (companyServices != null) {
                    for (n = 0; n < 100 && n < companyServices.size(); n++) {
                        CompanyService cs = companyServices.get(n);
                        DDCompanyExRet c = buildDDCompanyExRet(cs);
                        pDDCompanyQueryResponse.getDDCompanyExRet().add(c);
                    }
                }
                logger.info("search by SourceSystemCd=" + sourceSystemCd + searchCriteria + " returns " + n + " results");

            }

            pDDCompanyQueryResponse.setResponseStatus(DDCommon.build_ResponseStatus(validationResult, pExpectedErrorCodes));
        }
        catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e);
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.commitUnitOfWork();
        }
    }

    /**
     * This method creates a DDCompanyExRet from DDCompanyServiceInfo domain Object
     *
     * @param pCompanyService
     * @param pCompanyService
     * @return
     * @throws Exception
     */
    private DDCompanyExRet buildDDCompanyExRet(CompanyService pCompanyService) throws Exception {
        intuit.osp.pse.dd.wsapi.xsd.ddcompanyexret.ObjectFactory ddCompanyExRetObjectFactory =
                new intuit.osp.pse.dd.wsapi.xsd.ddcompanyexret.ObjectFactory();

        DDCompanyExRet ddCompanyExRet = ddCompanyExRetObjectFactory.createDDCompanyExRet();

        Company company = pCompanyService.getCompany();
        ddCompanyExRet.setCompanyEx(this.buildCompanyEx(pCompanyService));

        String code = null;

        if (company.getPayrollFrequency() != null) {
            code = company.getPayrollFrequency().getPayrollFreqCd();
        }

        if (code != null) {
            ddCompanyExRet.setPayrollFrequency(code);
        }

        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) pCompanyService;
        SpcfMoney amount = ddCompanyServiceInfo.getAveragePayRunAmount();
        if (amount != null) {
            ddCompanyExRet.setAveragePayrollAmt(SpcfUtils.convertToBigDecimal(amount).setScale(2));
        }

        if ((amount = ddCompanyServiceInfo.getHighAnnualPayAmount()) != null) {
            ddCompanyExRet.setHighAnnualPayAmt(SpcfUtils.convertToBigDecimal(amount).setScale(2));
        }

        FundingModel fundingModel = company.getFundingModel();
        ddCompanyExRet.setFundingModelCd(fundingModel.getFundingModelCd());
        ddCompanyExRet.setFundingModelDesc(fundingModel.getName());
        ddCompanyExRet.setCompanyDDStatusCd(DDCodeToPSP.getQBOEServiceStatus(ddCompanyServiceInfo));
        if ((amount = ddCompanyServiceInfo.getOverrideCompanyLimitAmount()) != null) {
            ddCompanyExRet.setCompanyLimitAmt(SpcfUtils.convertToBigDecimal(amount).setScale(2));
        }

        if ((amount = ddCompanyServiceInfo.getOverrideEmployeeLimitAmount()) != null) {
            ddCompanyExRet.setEmployeeLimitAmt(SpcfUtils.convertToBigDecimal(amount).setScale(2));
        }

        String sourceCompanyId = company.getSourceCompanyId();
        ddCompanyExRet.setPayrollRunTotalCount(com.intuit.sbd.payroll.psp.domain.PayrollRun.findPayrollRuns(company).size());

        ddCompanyExRet.setBankReturnTotalCount(TransactionReturn.findTransactionReturnCount(company));
        ddCompanyExRet.setLimitViolationTotalCount(com.intuit.sbd.payroll.psp.domain.CompanyEvent.getEventCountByType(company, EventTypeCode.LimitViolation));
        ddCompanyExRet.setLimitViolationConsecutiveCount(Long.valueOf(ddCompanyServiceInfo.getConsecutiveLimitViolationCount()).intValue());
        SpcfCalendar fromDate = PSPDate.getPSPTime();
        fromDate.addMonths(-12);
        ddCompanyExRet.setStrike12MonthCount(com.intuit.sbd.payroll.psp.domain.CompanyEvent.getCompanyStrikeCount(company, fromDate, null));
        ddCompanyExRet.setLastTransactionResponseToken(
                new Long(TransactionResponse.getLastTransactionTokenNumber(company)).intValue());

        return ddCompanyExRet;
    }

    /**
     * This method creates CompanyEx objects from CompanyBO objects.
     *
     * @param pCompanyService
     * @return
     * @throws Exception
     */
    private CompanyEx buildCompanyEx(CompanyService pCompanyService) throws Exception {
        intuit.osp.pse.dd.wsapi.xsd.companyex.ObjectFactory companyExObjectFactory =
                new intuit.osp.pse.dd.wsapi.xsd.companyex.ObjectFactory();

        CompanyEx companyEx = companyExObjectFactory.createCompanyEx();
        companyEx.setSourceSystemCd(pCompanyService.getCompany().getSourceSystemCd().toString());
        companyEx.setCompanyID(pCompanyService.getCompany().getSourceCompanyId());
        companyEx.setFEIN(pCompanyService.getCompany().getFedTaxId());
        companyEx.setLegalName(pCompanyService.getCompany().getLegalName());
        if (pCompanyService.getCompany().getDbaName() != null) {
            companyEx.setDBA(pCompanyService.getCompany().getDbaName());
        }

        companyEx.setNotificationEmail(pCompanyService.getCompany().getNotificationEmail());

        companyEx.setLegalAddress(DDCommon.addressToXML(pCompanyService.getCompany().getLegalAddress()));
        companyEx.setMailingAddress(DDCommon.addressToXML(pCompanyService.getCompany().getMailingAddress()));

        for (Contact contact : pCompanyService.getCompany().getContactCollection()) {
            companyEx.getContact().add(DDCommon.contactToXML(contact));
        }

        String ddCode = DDCodeToPSP.getQBOECompanyStatus(pCompanyService);
        companyEx.setCompanyStatusCd(ddCode);
        return companyEx;
    }

    /**
     * @param requestDoc
     * @return
     * @throws WSException
     */
    public Element updateFundingModel(Element requestDoc) throws WSException {
        String[] expectedErrorCodes = {"137","138","125","291","169"};

        WSServerContext wsServerContext = new WSServerContext(DDCompany.SERVICE_NAME,
                DDCompany.Operations.UPDATE_FUNDING_MODEL);

        DDCompanyFundingModelUpdateRs ddCompanyFundingModelUpdateRs =
                (DDCompanyFundingModelUpdateRs) wsServerContext.getOutputDTO();

        ProcessResult processResult = new ProcessResult();
        try {
            PayrollServices.beginUnitOfWork();
            DDCompanyFundingModelUpdate ddCompanyFundingModelUpdate =
                    (DDCompanyFundingModelUpdate) wsServerContext.translateInputElement(requestDoc);
            FundingModel newFundingModel = PayrollServices.entityFinder.findById(FundingModel.class, ddCompanyFundingModelUpdate.getFundingModelCd());
            processResult = PayrollServices.companyManager.updateCompanyFundingModel(
                    SourceSystemCode.valueOf(ddCompanyFundingModelUpdate.getSourceSystemCd()),
                    ddCompanyFundingModelUpdate.getCompanyID(),
                    newFundingModel);


            PayrollServices.commitUnitOfWork();
            ddCompanyFundingModelUpdateRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));

            return wsServerContext.translateOutputDTO();

        } catch (WSValidationException wsValidationException) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(wsValidationException.getMessage(), wsValidationException.getCause());
            throw wsValidationException;
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(ex.getMessage(), ex.getCause());
            throw new WSException(DDCommon.pse_Error, ex);

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }


    }

    public Element updateLimits(Element requestDoc) throws WSException {
        String[] expectedErrorCodes = {"137","138","125","169","1010"};

        WSServerContext wsServerContext = new WSServerContext(DDCompany.SERVICE_NAME,
			DDCompany.Operations.UPDATE_LIMITS);

		DDCompanyLimitsUpdateRs ddCompanyLimitsUpdateRs =
			(DDCompanyLimitsUpdateRs) wsServerContext.getOutputDTO();

      ProcessResult processResult = new ProcessResult();
        try {
            PayrollServices.beginUnitOfWork();
            DDCompanyLimitsUpdate ddCompanyLimitsUpdate =
                    (DDCompanyLimitsUpdate) wsServerContext.translateInputElement(requestDoc);
            SpcfMoney companyLimitAmt = null;
            SpcfMoney employeeLimitAmount = null;
            if (ddCompanyLimitsUpdate.getCompanyLimitAmt() != null) {
               companyLimitAmt = SpcfUtils.convertToSpcfMoney(ddCompanyLimitsUpdate.getCompanyLimitAmt());
            }
            if (ddCompanyLimitsUpdate.getEmployeeLimitAmt() != null) {
               employeeLimitAmount = SpcfUtils.convertToSpcfMoney(ddCompanyLimitsUpdate.getEmployeeLimitAmt());
            }
            processResult = PayrollServices.companyManager.updateDDLimits(
				SourceSystemCode.valueOf(ddCompanyLimitsUpdate.getSourceSystemCd()),
				ddCompanyLimitsUpdate.getCompanyID(),
				companyLimitAmt,
				employeeLimitAmount);
            PayrollServices.commitUnitOfWork();
            ddCompanyLimitsUpdateRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));

            return wsServerContext.translateOutputDTO();

        } catch (WSValidationException wsValidationException) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(wsValidationException.getMessage(), wsValidationException.getCause());
            throw wsValidationException;
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(ex.getMessage(), ex.getCause());
            throw new WSException(DDCommon.pse_Error, ex);

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

    }

    public Element strikeAdd(Element requestDoc) throws WSException {
        String[] expectedErrorCodes = {"137","138","125","169","5001","11"};
        WSServerContext wsServerContext = new WSServerContext("DDCompany", "strikeAdd");

        DDCompanyStrikeAddRs ddCompanyStrikeAddRs = null;
        ProcessResult processResult = new ProcessResult();

        try {
            PayrollServices.beginUnitOfWork();

            DDCompanyStrikeAdd ddCompanyStrikeAdd =
                    (DDCompanyStrikeAdd) wsServerContext.translateInputElement(requestDoc);

            ddCompanyStrikeAddRs = (DDCompanyStrikeAddRs) wsServerContext.getOutputDTO();

            processResult = PayrollServices.companyManager.addStrikeEvent(
                    SourceSystemCode.valueOf(ddCompanyStrikeAdd.getSourceSystemCd()),
                    ddCompanyStrikeAdd.getCompanyID(), ddCompanyStrikeAdd.getStrikeReason(),
                    CalendarUtils.convertToSpcfCalendar(ddCompanyStrikeAdd.getStrikeDate()));

            PayrollServices.commitUnitOfWork();

            ddCompanyStrikeAddRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));

            return wsServerContext.translateOutputDTO();

        } catch (WSValidationException wsValidationException) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(wsValidationException.getMessage(), wsValidationException.getCause());
            throw wsValidationException;
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(ex.getMessage(), ex.getCause());
            throw new WSException(DDCommon.pse_Error, ex);

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    public Element strikeCancel(Element requestDoc) throws WSException {
        String[] expectedErrorCodes = {"137","138","125","169","257"};
        WSServerContext wsServerContext = new WSServerContext(DDCompany.SERVICE_NAME, "strikeCancel");
        DDCompanyStrikeCancelRs ddCompanyStrikeCancelRs = null;
        ProcessResult processResult = new ProcessResult();

        try {
            PayrollServices.beginUnitOfWork();

            DDCompanyStrikeCancel ddCompanyStrikeCancel =
                    (DDCompanyStrikeCancel) wsServerContext.translateInputElement(requestDoc);

            ddCompanyStrikeCancelRs =
                    (DDCompanyStrikeCancelRs) wsServerContext.getOutputDTO();

            processResult = PayrollServices.companyManager.cancelStrikeEvent(
                    SourceSystemCode.valueOf(ddCompanyStrikeCancel.getSourceSystemCd()),
                    ddCompanyStrikeCancel.getCompanyID(),
                    SpcfUniqueId.createInstance(ddCompanyStrikeCancel.getPSEStrikeID()));

            PayrollServices.commitUnitOfWork();

            ddCompanyStrikeCancelRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));

            return wsServerContext.translateOutputDTO();
        } catch (WSValidationException wsValidationException) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(wsValidationException.getMessage(), wsValidationException.getCause());
            throw wsValidationException;
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(ex.getMessage(), ex.getCause());
            throw new WSException(DDCommon.pse_Error, ex);

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    /**
     * Interface to store names of DD Company operations.
     */
    public interface Operations {

        public static final String ADD = "add";
        public static final String CANCEL = "cancel";
        public static final String GET_INFO = "getInfo";
        public static final String QUERY = "query";
        public static final String UPDATE = "update";
        public static final String UPDATE_FUNDING_MODEL = "updateFundingModel";
        public static final String UPDATE_LIMITS = "updateLimits";
        public static final String UPDATE_DD_STATUS = "updateDDStatus";
        public static final String STRIKE_ADD = "strikeAdd";
        public static final String STRIKE_CANCEL = "strikeCancel";
    }
}
