package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.ems.payroll.psp.gateways.ers.ERSMockGateway;
import com.intuit.ems.payroll.psp.gateways.ers.EntitlementInfoDTO;
import com.intuit.ems.payroll.psp.gateways.ers.EntitlementUnitInfoDTO;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.CompanyAdapter;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.webservices.wsdto.EntitlementMessageResponseWSDTO;
import com.intuit.sbd.payroll.psp.webservices.wsdto.EntitlementUnitWSDTO;
import com.intuit.sbd.payroll.psp.webservices.wsdto.EntitlementWSDTO;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 21, 2010
 * Time: 12:55:18 PM
 */
@WebService()
public class EntitlementWS {
    public EntitlementWS() {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
    }

    @WebMethod
    public Collection<EntitlementWSDTO> getEntitlements(@WebParam(name = "LicenseNumber") String pLicenseNumber,
                                                        @WebParam(name = "EOC") String pEOC,
                                                        @WebParam(name = "State") String pState,
                                                        @WebParam(name = "ReturnLastValidationDate") Boolean pReturnLastValidationDate) throws Exception {

        try {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Entitlement> entitlements;
            if(pLicenseNumber == null && pEOC == null && pState == null) {
                entitlements = Application.find(Entitlement.class);
            } else {
                Criterion<Entitlement> where = null;

                if(pLicenseNumber != null) {
                    where =   Entitlement.LicenseNumber().equalTo(pLicenseNumber);
                }

                if(pEOC != null) {
                    if(where != null) {
                        where.And(Entitlement.EntitlementOfferingCode().equalTo(pEOC));
                    } else {
                        where = Entitlement.EntitlementOfferingCode().equalTo(pEOC);
                    }
                }

                if(pState != null) {
                    if(where != null) {
                        where.And(Entitlement.EntitlementState().equalTo(EntitlementStateCode.valueOf(pState)));
                    } else {
                        where = Entitlement.EntitlementState().equalTo(EntitlementStateCode.valueOf(pState));
                    }
                }

                entitlements = Application.find(Entitlement.class, new Query<Entitlement>().Where(where).OrderBy(Entitlement.LicenseNumber(), Entitlement.EntitlementOfferingCode()));
            }

            ArrayList<EntitlementWSDTO> entitlementWSDTOs = new ArrayList<EntitlementWSDTO>();
            for (Entitlement entitlement : entitlements) {
                EntitlementWSDTO entitlementWSDTO = new EntitlementWSDTO();
                entitlementWSDTO.setAsset(entitlement.getEntitlementCode().getAssetItemCd().toString());
                entitlementWSDTO.setBillingZipCode(entitlement.getBillingZipCode());
                entitlementWSDTO.setCancellationReason(entitlement.getCancellationReason());
                entitlementWSDTO.setContactEmail(entitlement.getContactEmail());
                entitlementWSDTO.setContactName(entitlement.getContactName());
                entitlementWSDTO.setCreditCardExpiration(entitlement.getCreditCardExpiration());
                entitlementWSDTO.setCreditCardNumber(entitlement.getCreditCardNumber());
                entitlementWSDTO.setCreditCardType(entitlement.getCreditCardType());
                entitlementWSDTO.setCustomerId(entitlement.getCustomerId());
                if(entitlement.getEntitlementCode().getEditionType() != null) {
                    entitlementWSDTO.setEdition(entitlement.getEntitlementCode().getEditionType().toString());
                }
                entitlementWSDTO.setEntitlementOfferingCode(entitlement.getEntitlementOfferingCode());
                entitlementWSDTO.setEntitlementState(entitlement.getEntitlementState().toString());
                entitlementWSDTO.setLicenseNumber(entitlement.getLicenseNumber());
                if(entitlement.getNextChargeDate() != null) {
                    entitlementWSDTO.setNextChargeDate(entitlement.getNextChargeDate().format("yyyy/MM/dd HH:mm"));
                }
                if(entitlement.getEntitlementCode().getNumberOfEmployeesType() != null) {
                    entitlementWSDTO.setNumberOfEmployees(entitlement.getEntitlementCode().getNumberOfEmployeesType().toString());
                }
                entitlementWSDTO.setOrderNumber(entitlement.getOrderNumber());
                if(entitlement.getPaymentMethodType() != null) {
                    entitlementWSDTO.setPaymentMethodType(entitlement.getPaymentMethodType().toString());
                }
                entitlementWSDTO.setSubscriptionNumber(entitlement.getSubscriptionNumber());
                DomainEntitySet<EntitlementUnit> entitlementUnits = entitlement.getEntitlementUnitCollection();
                for (EntitlementUnit entitlementUnit : entitlementUnits) {
                    EntitlementUnitWSDTO entitlementUnitWSDTO = new EntitlementUnitWSDTO();
                    entitlementUnitWSDTO.setEntitlementUnitStatus(entitlementUnit.getEntitlementUnitStatus().toString());
                    entitlementUnitWSDTO.setExtensionKey(entitlementUnit.getExtensionKey());
                    entitlementUnitWSDTO.setServiceKey(entitlementUnit.getServiceKey());
                    entitlementUnitWSDTO.setFEIN(entitlementUnit.getFedTaxId());

                    if (pReturnLastValidationDate != null && entitlementUnit.getLastValidationDate() != null &&
                            pReturnLastValidationDate) {
                        entitlementUnitWSDTO.setLastValidationDate(entitlementUnit.getLastValidationDate().format("yyyy/MM/dd HH:mm"));
                    }

                    entitlementWSDTO.getEntitlementUnits().add(entitlementUnitWSDTO);
                }
                entitlementWSDTOs.add(entitlementWSDTO);
            }

            return entitlementWSDTOs;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void addEntitlementInfoDtoToErsMockGateway(EntitlementWSDTO pEntitlementWSDTO){

        if (pEntitlementWSDTO != null) {
            EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();

            entitlementInfoDTO.setAssetItemNumber(pEntitlementWSDTO.getAssetItemNumber());
            entitlementInfoDTO.setCustomerId(pEntitlementWSDTO.getCustomerId());
            entitlementInfoDTO.setEditionType(EditionType.valueOf(pEntitlementWSDTO.getEdition()));
            entitlementInfoDTO.setEntitlementState(EntitlementStateCode.valueOf(pEntitlementWSDTO.getEntitlementState()));
            entitlementInfoDTO.setNumberOfEmployeesType(NumberOfEmployeesType.valueOf(pEntitlementWSDTO.getNumberOfEmployees()));

            for (EntitlementUnitWSDTO entitlementUnitWSDTO : pEntitlementWSDTO.getEntitlementUnits()) {
                EntitlementUnitInfoDTO entitlementUnitInfoDTO = new EntitlementUnitInfoDTO();
                entitlementUnitInfoDTO.setEntitlementUnitStatusCode(EntitlementUnitStatusCode.valueOf(entitlementUnitWSDTO.getEntitlementUnitStatus()));
                entitlementUnitInfoDTO.setFedTaxId(entitlementUnitWSDTO.getFEIN());
                entitlementInfoDTO.getEntitlementUnits().put(entitlementUnitInfoDTO.getFedTaxId(), entitlementUnitInfoDTO);
            }

            ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);
        }
    }

    @WebMethod
    public Collection<EntitlementMessageResponseWSDTO> getEntitlementMessages(@WebParam(name = "LicenseNumber") String pLicenseNumber,
                                                                              @WebParam(name = "EOC") String pEOC,
                                                                              @WebParam(name = "OrderNumber") String pOrderNumber,
                                                                              @WebParam(name = "Status") String pStatus) throws Exception {

        try {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<EntitlementMessage> entitlementMessages;
            if(pLicenseNumber == null && pEOC == null && pOrderNumber == null && pStatus == null) {
                entitlementMessages = Application.find(EntitlementMessage.class);
            } else {
                Criterion<EntitlementMessage> where = null;

                if(pLicenseNumber != null) {
                    where =   EntitlementMessage.LicenseNumber().equalTo(pLicenseNumber);
                }

                if(pEOC != null) {
                    if(where != null) {
                        where.And(EntitlementMessage.EntitlementOfferingCode().equalTo(pEOC));
                    } else {
                        where = EntitlementMessage.EntitlementOfferingCode().equalTo(pEOC);
                    }
                }

                if(pOrderNumber != null) {
                    if(where != null) {
                        where.And(EntitlementMessage.OrderNumber().equalTo(pOrderNumber));
                    } else {
                        where = EntitlementMessage.OrderNumber().equalTo(pOrderNumber);
                    }
                }

                if(pStatus != null) {
                    if(where != null) {
                        where.And(EntitlementMessage.Status().equalTo(EntitlementMessageStatusCode.valueOf(pStatus)));
                    } else {
                        where = EntitlementMessage.Status().equalTo(EntitlementMessageStatusCode.valueOf(pStatus));
                    }
                }

                entitlementMessages = Application.find(EntitlementMessage.class, new Query<EntitlementMessage>().Where(where).OrderBy(EntitlementMessage.OrderNumber(),EntitlementMessage.CreatedDate()));
            }

            entitlementMessages = entitlementMessages.sort(EntitlementMessage.LicenseNumber(), EntitlementMessage.Status());

            ArrayList<EntitlementMessageResponseWSDTO> entitlementMessageResponseWSDTOs = new ArrayList<EntitlementMessageResponseWSDTO>();
            for (EntitlementMessage entitlementMessage : entitlementMessages) {
                EntitlementMessageResponseWSDTO entitlementMessageResponseWSDTO = new EntitlementMessageResponseWSDTO();
                entitlementMessageResponseWSDTO.setEOC(entitlementMessage.getEntitlementOfferingCode());
                entitlementMessageResponseWSDTO.setLicenseNumber(entitlementMessage.getLicenseNumber());
                entitlementMessageResponseWSDTO.setMessage(entitlementMessage.getMessage());
                entitlementMessageResponseWSDTO.setOrderNumber(entitlementMessage.getOrderNumber());
                entitlementMessageResponseWSDTO.setStatus(entitlementMessage.getStatus().toString());
                entitlementMessageResponseWSDTOs.add(entitlementMessageResponseWSDTO);
            }

            return entitlementMessageResponseWSDTOs;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void updateEntitlement(@WebParam(name = "psid") String pSourceCompanyId,
                                          @WebParam(name = "licenseNumber") String pLicenseNumber,
                                          @WebParam(name = "EOC") String pEntitlementOfferingCode,
                                          @WebParam(name = "AssetItemNumber") String pAssetItemNumber,
                                          @WebParam(name = "NumberOfEmployees") NumberOfEmployeesType pNumberOfEmployeesType,
                                          @WebParam(name = "Edition") EditionType pEditionType,
                                          @WebParam(name = "NextChargeDate") Calendar pNextChargeDate) throws Exception {
        try {
            if (pSourceCompanyId == null || pSourceCompanyId.trim().length() == 0) {
                throw new RuntimeException("No Source Company Id specified");
            }
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.QBDT);

            //Deactivate current entitlement
            EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();
            EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
            entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);

            ProcessResult<EntitlementUnit> processResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(
                    company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
            if (!processResult.isSuccess()) {
                PayrollServices.rollbackUnitOfWork();
                throw new RuntimeException(processResult.getMessages().get(0).getMessageCode() + ": " +
                processResult.getMessages().get(0).getMessage());
            } else {
                PayrollServices.commitUnitOfWork();
            }

            DataLoadServices.AssetItemNumber assetItemNumber = null;
            try{
                assetItemNumber = DataLoadServices.AssetItemNumber.valueOf(pAssetItemNumber);
            } catch (Throwable t) {
                // ignore
            }

            PayrollServices.beginUnitOfWork();
            //Activate new entitlement
            entitlementUnitDTO = new EntitlementUnitDTO();
            entitlementUnitDTO.setLicenseNumber(pLicenseNumber);
            entitlementUnitDTO.setEntitlementOfferingCode(pEntitlementOfferingCode);
            entitlementUnitDTO.setEditionType(pEditionType);
            entitlementUnitDTO.setNumberOfEmployeesType(pNumberOfEmployeesType);
            entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingActivation);
            if(assetItemNumber == null) {
                entitlementUnitDTO.setAssetItemNumber(pAssetItemNumber);
            } else {
                entitlementUnitDTO.setAssetItemNumber(assetItemNumber.toString());
            }
            entitlementUnitDTO.setNextChargeDate(CalendarUtils.convertToSpcfCalendar(pNextChargeDate));
            entitlementUnitDTO.setFedTaxId(company.getFedTaxId());
            entitlementUnitDTO.setCustomerId("CustomerId");

            processResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(
                    company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
            if (!processResult.isSuccess()) {
                PayrollServices.rollbackUnitOfWork();
                throw new RuntimeException(processResult.getMessages().get(0).getMessageCode() + ": " +
                processResult.getMessages().get(0).getMessage());
            } else {
                PayrollServices.commitUnitOfWork();
            }
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            throw new RuntimeException(e);
        }  finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void moveEntitlementUnit(@WebParam(name = "FromLicenseNumber") String pFromLicenseNumber,
                                @WebParam(name = "FromEOC") String pFromEOC,
                                @WebParam(name = "FromEIN") String pFromEIN,
                                @WebParam(name = "ToLicenseNumber") String pToLicenseNumber,
                                @WebParam(name = "ToEOC") String pToEOC,
                                @WebParam(name = "ToAssetItemNumber") String pToAssetItemNumber) throws Exception {
        try {
            String entitlementUnitId;
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<EntitlementUnit> entitlementUnits = EntitlementUnit.findEntitlementUnits(pFromEIN, pFromLicenseNumber, pFromEOC);
            if(entitlementUnits.size() == 1) {
                EntitlementUnit entitlementUnit = entitlementUnits.get(0);
                entitlementUnitId = entitlementUnit.getId().toString();
            } else {
                throw new RuntimeException("Error finding entilement unit for LN: " + pFromLicenseNumber + " EOC: " + pFromEOC);
            }

            PayrollServices.rollbackUnitOfWork();

            CompanyAdapter companyAdapter = new CompanyAdapter();
            companyAdapter.moveEntitlementUnit(entitlementUnitId, pToLicenseNumber, pToEOC, pToAssetItemNumber);
        } catch (Throwable t) {
            throw new Exception(t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void expireEntitlement(@WebParam(name = "PSID") String pSourceCompanyId,
                                  @WebParam(name = "SourceSystemCode") String pSourceSystemCode,
                                  @WebParam(name = "LicenseNumber") String pLicenseNumber,
                                  @WebParam(name = "EOC") String pEOC) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        /*  Validate input args */
        if (pSourceSystemCode == null || pSourceSystemCode.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD specified");
        }
        if (pSourceCompanyId == null || pSourceCompanyId.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyId specified");
        }
        if (pLicenseNumber == null || pLicenseNumber.trim().length() == 0) {
            throw new RuntimeException("No License Number specified");
        }

        try {

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
            if (company == null) {
                throw new RuntimeException("Error finding company " + pSourceSystemCode + " : " + pSourceCompanyId + ".");
            }
            Entitlement entitlement = Entitlement.findEntitlement(pLicenseNumber, pEOC);
            if (entitlement == null) {
                throw new RuntimeException("Error finding entitlement unit for " + pSourceSystemCode + ":" + pSourceCompanyId + " License #: " + pLicenseNumber + ((pEOC != null && pEOC.trim().length() > 0) ? ", EOC #: " + pEOC : ""));
            }
            EntitlementDTO entitlementDTO = PayrollServices.dtoFactory.create(entitlement);
            entitlementDTO.setEntitlementState(EntitlementStateCode.Disabled);
            PayrollServices.entitlementManager.updateEntitlement(entitlementDTO);
            PayrollServices.commitUnitOfWork();

        } catch (Exception e) {
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void expireEntitlementCreditCard(@WebParam(name = "PSID") String pSourceCompanyId,
                                            @WebParam(name = "SourceSystemCode") String pSourceSystemCode,
                                            @WebParam(name = "LicenseNumber") String pLicenseNumber,
                                            @WebParam(name = "EOC") String pEOC) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        /*  Validate input args */
        if (pSourceSystemCode == null || pSourceSystemCode.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD specified");
        }
        if (pSourceCompanyId == null || pSourceCompanyId.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyId specified");
        }
        if (pLicenseNumber == null || pLicenseNumber.trim().length() == 0) {
            throw new RuntimeException("No License Number specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
            if (company == null) {
                throw new RuntimeException("Error finding company " + pSourceSystemCode + " : " + pSourceCompanyId + ".");
            }
            Entitlement entitlement = Entitlement.findEntitlement(pLicenseNumber, pEOC);
            if (entitlement == null) {
                throw new RuntimeException("Error finding entitlement unit for " + pSourceSystemCode + ":" + pSourceCompanyId + " License #: " + pLicenseNumber + ((pEOC != null && pEOC.trim().length() > 0) ? ", EOC #: " + pEOC : ""));
            }
            if (entitlement.getCreditCardNumber() == null) {
                throw new RuntimeException("There's no credit card for " + pSourceSystemCode + ":" + pSourceCompanyId + " License #: " + pLicenseNumber + ((pEOC != null && pEOC.trim().length() > 0) ? ", EOC #: " + pEOC : ""));
            }
            EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(company.getEntitlementUnit(entitlement, company.getFedTaxId()));
            SpcfCalendar pspDate = PSPDate.getPSPTime();
            pspDate.addMonths(-1);
            entitlementUnitDTO.setCreditCardExpiration(String.format("%1$02d/%2$04d", pspDate.getMonth(), pspDate.getYear()));
            PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(
                    company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void deleteEntitlement(@WebParam(name = "PSID") String pSourceCompanyId,
                                  @WebParam(name = "EIN") String pEin,
                                  @WebParam(name = "SubscriptionNo") String pSubscriptionNo) throws Exception {

        /*  Validate input args */
        boolean isPSIDProvided = false;
        boolean isEINAndSubscriptionNoProvided = false;

        PayrollServices.beginUnitOfWork();
        try {

            isPSIDProvided = pSourceCompanyId != null && pSourceCompanyId.trim().length() != 0;

            isEINAndSubscriptionNoProvided = (pEin != null && pEin.trim().length() != 0) &&
                    (pSubscriptionNo != null && pSubscriptionNo.trim().length() != 0)   ;

            if (!isPSIDProvided && !isEINAndSubscriptionNoProvided ) {
                throw new RuntimeException("No PSID or EIN and Subscription number provided");
            }

            Company company = null;
            DomainEntitySet<EntitlementUnit> entitlementUnits = null;
            if(isPSIDProvided){
                company = Company.findCompany(pSourceCompanyId, SourceSystemCode.QBDT);
                entitlementUnits =  company.getEntitlementUnitCollection();

            } else{
                Criterion<EntitlementUnit> criterion = null;
                if(pEin == null){
                    criterion = EntitlementUnit.Company().FedTaxIdEnc().isNull();
                }else{
                    List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName,pEin);
                    criterion = EntitlementUnit.Company().FedTaxIdEnc().in(fedTaxIdEncList);
                }
                criterion = criterion.And(EntitlementUnit.Company().SourceSystemCd().equalTo(SourceSystemCode.QBDT))
                        .And(EntitlementUnit.Entitlement().SubscriptionNumber().equalTo(pSubscriptionNo));
                Expression<EntitlementUnit> query =
                        new Query<EntitlementUnit>()
                                .Where(criterion)
                                .OrderBy(EntitlementUnit.Company().SignUpDate().Descending())
                                .LimitResults(0, 1);

                entitlementUnits = Application.find(EntitlementUnit.class, query);
            }

            if (entitlementUnits.isEmpty()) {
                throw new RuntimeException("EIN does not exist");
            }
            /*Delete all entitlement units for the company*/
            for (EntitlementUnit entitlementUnit : entitlementUnits){
                Application.delete(entitlementUnit);
            }

            PayrollServices.commitUnitOfWork();
        }catch (Exception e){
            throw e;
        }
        finally {
            PayrollServices.rollbackUnitOfWork();

        }

    }

    @WebMethod
    public void inactivateEntitlementUnit(@WebParam(name = "PSID") String pSourceCompanyId,
                                      @WebParam(name = "EIN") String pEin,
                                      @WebParam(name = "SubscriptionNo") String pSubscriptionNo) throws Exception {

        /*  Validate input args */
        boolean isPSIDProvided = false;
        boolean isEINAndSubscriptionNoProvided = false;

        try {

            isPSIDProvided = pSourceCompanyId != null && pSourceCompanyId.trim().length() != 0;

            isEINAndSubscriptionNoProvided = (pEin != null && pEin.trim().length() != 0) &&
                    (pSubscriptionNo != null && pSubscriptionNo.trim().length() != 0)   ;
            PayrollServices.beginUnitOfWork();
            if (!isPSIDProvided && !isEINAndSubscriptionNoProvided ) {
                throw new RuntimeException("No PSID or EIN and Subscription number provided");
            }
            Company company = null;
            DomainEntitySet<EntitlementUnit> entitlementUnits = null;
            if(isPSIDProvided){
                company = Company.findCompany(pSourceCompanyId, SourceSystemCode.QBDT);
                if (company != null) {
                    entitlementUnits =  company.getEntitlementUnitCollection();
                }
            } else{
                Criterion<EntitlementUnit> criterion = null;
               if(pEin == null){
                   criterion = EntitlementUnit.Company().FedTaxIdEnc().isNull();
               }else{
                   List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName,pEin);
                   criterion = EntitlementUnit.Company().FedTaxIdEnc().in(fedTaxIdEncList);
               }
                criterion = criterion.And(EntitlementUnit.Company().SourceSystemCd().equalTo(SourceSystemCode.QBDT))
                        .And(EntitlementUnit.Entitlement().SubscriptionNumber().equalTo(pSubscriptionNo));

                Expression<EntitlementUnit> query =
                        new Query<EntitlementUnit>()
                                .Where(criterion)
                                .OrderBy(EntitlementUnit.Company().SignUpDate().Descending())
                                .LimitResults(0, 1);

                entitlementUnits = Application.find(EntitlementUnit.class, query);
            }
            if (entitlementUnits.isEmpty()) {
                throw new RuntimeException("PSID/EIN does not exist");
            }

            //If PSID is passed in PSP will deactivate all EU's but if EIN and SubNum is passed in one that one EU will be deactivated.
            for (EntitlementUnit entitlementUnit : entitlementUnits){
                if(!entitlementUnit.isDeactivated()){
                    entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
                }
                Application.save(entitlementUnit);
            }

            PayrollServices.commitUnitOfWork();
        }catch (Exception e){
            throw e;
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void setSubscriptionNumberSequence(int pStartingValue) throws Exception {
        try {
            Application.beginUnitOfWork();

            Statement statement = Application.getConnection().createStatement();

            try {
                String environmentId = ConfigurationManager.getEnvironmentIdentifier();
                String ownerName = "pspadm";
                if("local".equals(environmentId)) {
                    ownerName = "psp_local";
                }

                statement.execute(String.format("DROP SEQUENCE %s.SEQ_SUBSCRIPTION_NUMBER", ownerName));
                statement.execute(String.format("create sequence %s.SEQ_SUBSCRIPTION_NUMBER INCREMENT BY 1 MINVALUE %d MAXVALUE %d CYCLE", ownerName, pStartingValue, pStartingValue + 100000000));

            } finally {
                statement.close();
            }

            Application.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void activateEntitlementUnit(@WebParam(name = "PSID") String pSourceCompanyId,
                                        @WebParam(name = "LicenseNumber") String pLicenseNumber) throws Exception {
        try {
            PayrollServices.beginUnitOfWork();

            if(pSourceCompanyId == null || pLicenseNumber == null) {
                throw new RuntimeException("All parameters are required");
            }

            Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.QBDT);
            for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().LicenseNumber().equalTo(pLicenseNumber))) {
                EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
                entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
                entitlementUnitDTO.setErrorCount(0);
                ProcessResult processResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
                if(processResult.isSuccess()) {
                    PayrollServices.commitUnitOfWork();
                } else {
                    throw new RuntimeException(processResult.toString());
                }
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }
}
