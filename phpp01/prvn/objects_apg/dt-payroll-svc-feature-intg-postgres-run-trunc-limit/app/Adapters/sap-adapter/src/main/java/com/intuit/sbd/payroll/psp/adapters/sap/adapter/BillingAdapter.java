/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/adapter/BillingAdapter.java#9 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexMethod;
import com.intuit.sbd.payroll.psp.adapters.sap.Operation;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPFeeDetail;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPOffer;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPOffering;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.OfferingInfoDTO;
import com.intuit.sbd.payroll.psp.context.aspect.CompanyIdentifierType;
import com.intuit.sbd.payroll.psp.context.aspect.TenantId;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * BankReturnAdapter - SAP Adapter for Bank Returns
 *
 * @author Joe Warmelink
 */
public class BillingAdapter {

    private static final SpcfLogger logger = PayrollServices.getLogger(BillingAdapter.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);

    public BillingAdapter() {
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.AddOffer, OperationId.AddAssistedOfferPreBALF, OperationId.AddAssistedOfferPostBALF})
    public ArrayList<SAPOffer> findOffers(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pServiceCode) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));

            CompanyOffering companyOffering = company.getOffering(ServiceCode.valueOf(pServiceCode));

            DomainEntitySet<Offer> offerList = companyOffering.getOffering().getApplicableOffers();
            ArrayList<SAPOffer> sapOfferList = new ArrayList<SAPOffer>(offerList.size());
            for (Offer offer : offerList) {
                // filter out expired offers
                if (offer.getEndDate().toLocal().after(PSPDate.getPSPTime())) {
                    sapOfferList.add(BillingTranslator.getOfferFromDomainEntity(offer, isOfferOpenToUser(offer, AuthUser.findUser(Application.getCurrentPrincipal().getId()))));
                }
            }
            return sapOfferList;
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding offers.", pSourceSystemCd, pCompanyId, "ServiceCode", pServiceCode, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    private boolean isOfferOpenToUser(Offer offer, AuthUser user) {
        switch (offer.getOfferRestriction()) {
            case Open:
                return true;
            case Restricted:
                return user.hasOperation(OperationId.AddRestrictedOffer);
            case SalesOps:
                return user.hasRole("MarketingOps");
            default:
                throw new RuntimeException(offer.getOfferRestriction().toString() + " not defined");
        }
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.AddOffer, OperationId.AddAssistedOfferPreBALF, OperationId.AddAssistedOfferPostBALF})
    public void claimOfferWithExpirationForCompany(String offerCd, @TenantId(IdType = CompanyIdentifierType.PSID) String companyId, String sourceSystemCd, Date expirationDate) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            Offer offerToAdd = PayrollServices.entityFinder.find(Offer.class, Offer.OfferCd().equalTo(offerCd)).getFirst();

            Company company = Company.findCompany(companyId, SourceSystemCode.valueOf(sourceSystemCd));

            DomainEntitySet<CompanyOffer> companyOffers = company.getCompanyOffers();
            if ((companyOffers != null) && (companyOffers.size() > 0)) {
                // remove the current offering(s)
                for (CompanyOffer companyOffer : companyOffers) {
                    Offer offer = companyOffer.getOffer();
                    company.cancelOfferForCompany(offer);
                }
            }
            SpcfCalendar calendar = null;
            // In case the date field is not applicable for the company
            if (expirationDate != null ) {
                calendar = SAPTranslator.getSpcfCalendarFromDate(expirationDate);
            }
            if (!isOfferOpenToUser(offerToAdd, AuthUser.findUser(Application.getCurrentPrincipal().getId()))) {
                aeFactory.throwGenericException("User does not have access to claim an offer with a restriction of " + offerToAdd.getOfferRestriction().toString());
            }
            company.claimOfferForCompany(offerToAdd, calendar);

            PayrollServices.commitUnitOfWork();
        }
        catch (Throwable t) {
            aeFactory.throwGenericException("Error claiming offer.", sourceSystemCd, companyId, "Offer", offerCd, t);
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.AddOffer, OperationId.AddAssistedOfferPreBALF, OperationId.AddAssistedOfferPostBALF})
    public void cancelOfferForCompany(String offerCd,@TenantId(IdType = CompanyIdentifierType.PSID) String companyId, String sourceSystemCd) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            DomainEntitySet<Offer> offerList = PayrollServices.entityFinder.find(Offer.class, Offer.OfferCd().equalTo(offerCd));
            if (offerList.size() > 1) {
                aeFactory.throwGenericException("More than one offer returned.");
            } else if (offerList.size() == 0) {
                aeFactory.throwGenericException("No offer found");
            }

            Company company = Company.findCompany(companyId, SourceSystemCode.valueOf(sourceSystemCd));

            // remove the offer
            Offer offer = offerList.get(0);
            company.cancelOfferForCompany(offer);

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error canceling offer.", sourceSystemCd, companyId, "Offer", offerCd, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public ArrayList<SAPOffering> findOfferings(String pServiceCode,@TenantId(IdType = CompanyIdentifierType.PSID) String companyId, String sourceSystemCd) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(companyId, SourceSystemCode.valueOf(sourceSystemCd));
            if (company.getActivePrimaryEntitlementUnit() == null) {
                aeFactory.throwGenericException("Cannot find offerings for company because there were no active primary entitlement units");
            }

            DomainEntitySet<Offering> offeringList = Offering.findAvailableOfferings(ServiceCode.valueOf(pServiceCode), company.getActivePrimaryEntitlementUnit().getEntitlement().getEntitlementCode());

            ArrayList<SAPOffering> sapOfferingList = new ArrayList<SAPOffering>(offeringList.size());
            for (Offering offering : offeringList) {
                List<OfferingServiceCharge> serviceCharges = offering.getPayrollCharges();
                sapOfferingList.add(BillingTranslator.getOfferingFromDomainEntity(offering, serviceCharges));
            }
            return sapOfferingList;
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding offerings.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    public SAPOffering getCurrentOffering(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pServiceCode) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            CompanyOffering companyOffering = company.getOffering(ServiceCode.valueOf(pServiceCode));
            if(companyOffering != null){
                Offering offering = company.getOffering(ServiceCode.valueOf(pServiceCode)).getOffering();
                List<OfferingServiceCharge> serviceCharges = offering.getPayrollCharges();
                return BillingTranslator.getOfferingFromDomainEntity(offering, serviceCharges);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding offerings.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.AddOffering, OperationId.AddAssistedOfferingPreBALF, OperationId.AddAssistedOfferingPostBALF})
    public void addOfferingToCompany(String offeringSKU,@TenantId(IdType = CompanyIdentifierType.PSID)  String companyId, String sourceSystemCd) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            Offering offering = Offering.findBySKU(offeringSKU);

            OfferingInfoDTO offeringInfoDTO = PayrollServices.dtoFactory.create(offering);

            ProcessResult processResult = PayrollServices.companyManager.updateCompanyOffering(SourceSystemCode.valueOf(sourceSystemCd), companyId, offeringInfoDTO);

            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error adding offering.", processResult);
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adding offering.", sourceSystemCd, companyId, "Offering", offeringSKU, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = {
            OperationId.AgentInitiatesRefundRebill
    })
    public SAPFeeDetail findFeeDetail(String transactionId, @TenantId(IdType = CompanyIdentifierType.PSID) String companyId) throws Throwable {
        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
        SAPFeeDetail sapFeeDetail = new SAPFeeDetail();
        try {
            FinancialTransaction financialTransaction = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(transactionId));
            BillingDetail billingDetail = financialTransaction.getBillingDetail();
            OfferingServiceCharge offeringServiceCharge = OfferingServiceCharge.findBySKU(financialTransaction.getSku());
            OfferingServiceChargePrice currentPrice = offeringServiceCharge.getCurrentPrice();

            sapFeeDetail = BillingTranslator.getFeeDetailFromDomainEntities(financialTransaction, billingDetail, offeringServiceCharge, currentPrice);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding fee transaction.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapFeeDetail;
    }

}
