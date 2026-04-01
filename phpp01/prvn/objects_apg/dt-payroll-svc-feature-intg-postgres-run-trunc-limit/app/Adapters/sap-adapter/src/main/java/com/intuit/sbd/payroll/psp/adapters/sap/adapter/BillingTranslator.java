/*
 * : $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPFeeDetail;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPOffer;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPOffering;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPOfferingServiceCharge;
import com.intuit.sbd.payroll.psp.domain.*;

import java.util.List;

/**
 * BillingTranslator - DESCRIPTION
 *
 * @author Joe Warmelink
 */
public class BillingTranslator {
    public static SAPOffer getOfferFromDomainEntity(Offer offer, boolean isOfferOpenToUser) {
        if (offer != null) {
            SAPOffer sapOffer = new SAPOffer();
            sapOffer.setDescription(offer.getDescription());
            sapOffer.setName(offer.getName());
            sapOffer.setOfferCd(offer.getOfferCd());
            sapOffer.setEffectiveDate(SAPTranslator.getDateFromSpcfCalendar(offer.getEffectiveDate()));
            sapOffer.setExpirationDate(SAPTranslator.getDateFromSpcfCalendar(offer.getEndDate()));
            sapOffer.setOfferEndEvent(offer.getEndEvent().toString());
            sapOffer.setOpenToUser(isOfferOpenToUser);

            return sapOffer;
        }
        return null;
    }

    public static SAPOffer getCompanyOfferFromDomainEntity(CompanyOffer offer) {
        if (offer != null) {
            SAPOffer sapOffer = new SAPOffer();
            sapOffer.setDescription(offer.getOffer().getDescription());
            sapOffer.setName(offer.getOffer().getName());
            sapOffer.setOfferCd(offer.getOffer().getOfferCd());
            sapOffer.setEffectiveDate(SAPTranslator.getDateFromSpcfCalendar(offer.getBeginDate()));
            sapOffer.setExpirationDate(SAPTranslator.getDateFromSpcfCalendar(offer.getEndDate()));
            return sapOffer;
        }
        return null;
    }

    public static SAPOffering getOfferingFromDomainEntity(Offering offering, List<OfferingServiceCharge> serviceCharges) {
        if (offering != null) {
            SAPOffering sapOffering = new SAPOffering();
            sapOffering.setDescription(offering.getDescription());
            sapOffering.setSKU(offering.getSKU());
            sapOffering.setName(offering.getName());
            for (OfferingServiceCharge serviceCharge : serviceCharges) {
                sapOffering.getServiceCharges().add(getOfferingServiceChargeFromDomainEntity(serviceCharge));
            }
            return sapOffering;
        }
        return null;
    }

    public static SAPFeeDetail getFeeDetailFromDomainEntities(FinancialTransaction financialTransaction,
                                                              BillingDetail billingDetail,
                                                              OfferingServiceCharge offeringServiceCharge,
                                                              OfferingServiceChargePrice currentPrice){
        SAPFeeDetail sapFeeDetail = new SAPFeeDetail();

        sapFeeDetail.setIsPayrollFee(SkuType.Payroll.equals(offeringServiceCharge.getSkuType()));
        sapFeeDetail.setTotalPrice(SAPTranslator.getDoubleFromSpcfMoney(financialTransaction.getFinancialTransactionAmount()));
        
        if(offeringServiceCharge.getOfferingServiceChargeGroup() != null){
            sapFeeDetail.setFeeName(offeringServiceCharge.getOfferingServiceChargeGroup().getName());
        }

        if(billingDetail != null){
            sapFeeDetail.setUnitPrice(SAPTranslator.getDoubleFromSpcfMoney(billingDetail.getUnitPrice()));
            sapFeeDetail.setUnits(billingDetail.getQuantity());
        }

        if(currentPrice != null){
// TODO BILLING: Do we need to take BasePrice into account here??
            sapFeeDetail.setCurrentUnitPrice(SAPTranslator.getDoubleFromSpcfMoney(currentPrice.getUnitPrice()));
        }
        else {
            sapFeeDetail.setCurrentUnitPrice(-1);
        }

        return sapFeeDetail;
    }

    public static SAPOfferingServiceCharge getOfferingServiceChargeFromDomainEntity(
            OfferingServiceCharge offeringServiceCharge) {
        SAPOfferingServiceCharge sapOfferingServiceCharge = new SAPOfferingServiceCharge();
        sapOfferingServiceCharge.setGroupAppliesTo(offeringServiceCharge.getOfferingServiceChargeGroup().getAppliesTo());
        sapOfferingServiceCharge.setGroupDescription(offeringServiceCharge.getOfferingServiceChargeGroup().getDescription());
        sapOfferingServiceCharge.setSKU(offeringServiceCharge.getSKU());
        sapOfferingServiceCharge.setSKUType(offeringServiceCharge.getSkuType());
        sapOfferingServiceCharge.setTier(offeringServiceCharge.getIsTier());
        sapOfferingServiceCharge.setTierNumber(offeringServiceCharge.getTierNumber());
        sapOfferingServiceCharge.setTierUnits(offeringServiceCharge.getTierUnits());

        OfferingServiceChargePrice price = offeringServiceCharge.getCurrentPrice();
        sapOfferingServiceCharge.setPrice(SAPTranslator.getDoubleFromSpcfMoney(price.getBasePrice()));
        sapOfferingServiceCharge.setUnitPrice(SAPTranslator.getDoubleFromSpcfMoney(price.getUnitPrice()));

        return sapOfferingServiceCharge;
	}
}
