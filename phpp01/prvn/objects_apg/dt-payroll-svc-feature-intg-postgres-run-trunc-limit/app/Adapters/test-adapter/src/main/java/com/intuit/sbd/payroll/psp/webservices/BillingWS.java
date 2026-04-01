package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.CompanyAdapter;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.webservices.wsdto.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.lang.StringUtils;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: 7/17/12
 * Time: 3:23 AM
 * To change this template use File | Settings | File Templates.
 */
@WebService()
public class BillingWS {
    public static SpcfCalendar convert(Calendar pDate) {
        return (pDate == null) ? null : CalendarUtils.convertToSpcfCalendar(pDate);
    }

    public static Date convert(SpcfCalendar pDate) {
        if(pDate == null) {
            return null;
        }
        CalendarUtils.clearTime(pDate.toLocal());
        return CalendarUtils.convertToDate(pDate);
    }

    @WebMethod
    public List<OfferingWSDTO> getOfferingListForService(@WebParam(name = "ServiceCode") String pServiceCode) {
        if ((pServiceCode == null) || (pServiceCode.length() == 0)) {
            throw new RuntimeException("ServiceCode cannot be null or empty.");
        }

        List<OfferingWSDTO> list = new ArrayList<OfferingWSDTO>();

        try {
            ServiceCode serviceCode = ServiceCode.valueOf(pServiceCode);

            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

            PayrollServices.beginUnitOfWork();

            DomainEntitySet<Offering> offeringList = Application.find(Offering.class, Offering.ServiceCode().equalTo(serviceCode));

            if (offeringList.isEmpty()) {
                throw new RuntimeException(String.format("Unable to find Offering(s) for Service Code %s", pServiceCode));
            }

            for (Offering offering : offeringList) {
                OfferingWSDTO dto = new OfferingWSDTO();

                dto.OfferingCode = offering.getOfferingCode().toString();
                dto.Name = offering.getName();
                dto.Description = offering.getDescription();
                dto.ServiceCode = offering.getServiceCode().toString();
                dto.Sku = offering.getSKU();
                dto.IsApproved = offering.getIsApproved();

                list.add(dto);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return list;
    }

    @WebMethod
    @SuppressWarnings({"unchecked"})
    public List<OfferingServiceChargeWSDTO> getOfferingPriceList(@WebParam(name = "OfferingCode") String pOfferingCode) {
        if ((pOfferingCode == null) || (pOfferingCode.length() == 0)) {
            throw new RuntimeException("OfferingCode cannot be null or empty.");
        }

        List<OfferingServiceChargeWSDTO> list = new ArrayList<OfferingServiceChargeWSDTO>();

        try {
            OfferingCode offeringCode = OfferingCode.valueOf(pOfferingCode);

            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

            PayrollServices.beginUnitOfWork();

            String hql = "   select prc " +
                         "     from com.intuit.sbd.payroll.psp.domain.OfferingServiceChargePrice as prc " +
                         "     join fetch prc.OfferingServiceCharge as chg " +
                         "     join fetch chg.OfferingServiceChargeGroup as grp " +
                         "     join fetch grp.Offering as ofr " +
                         "    where ofr.OfferingCode = :OfferingCode " +
                         " order by grp.AppliesTo, chg.TierNumber, prc.EffectiveDate ";


            org.hibernate.Query hibernateQuery = Application.createHibernateQuery(hql);

            hibernateQuery.setParameter("OfferingCode", offeringCode);

            DomainEntitySet<OfferingServiceChargePrice> priceList = Application.<OfferingServiceChargePrice>getUniqueActualObjects(hibernateQuery.list());
            
            if (priceList.isEmpty()) {
                throw new RuntimeException(String.format("Unable to find Price List for Offering Code %s", offeringCode));
            }
            
            for (OfferingServiceChargePrice price : priceList) {
                OfferingServiceCharge charge = price.getOfferingServiceCharge();
                OfferingServiceChargeGroup group = charge.getOfferingServiceChargeGroup();
                Offering offering = group.getOffering();

                OfferingServiceChargeWSDTO dto = new OfferingServiceChargeWSDTO();

                dto.OfferingCode = offering.getOfferingCode().toString();
                dto.AppliesTo = group.getAppliesTo().toString();
                dto.IsTier = charge.getIsTier();
                dto.TierNumber = charge.getTierNumber();
                dto.TierUnits = charge.getTierUnits();
                dto.Sku = charge.getSKU();
                dto.SkuType = charge.getSkuType().toString();
                dto.BasePrice = price.getBasePrice().toString();
                dto.UnitPrice = price.getUnitPrice().toString();
                dto.EffectiveDate = convert(price.getEffectiveDate());

                list.add(dto);
            }
            
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return list;
    }

    @WebMethod
    public OfferingWSDTO getCompanyOfferingForService(@WebParam(name = "sourceCompanyId") String pSourceCompanyId,
                                                      @WebParam(name = "ServiceCode") String pServiceCode) {
        if ((pSourceCompanyId == null) || (pSourceCompanyId.length() == 0)) {
            throw new RuntimeException("SourceCompanyId cannot be null or empty.");
        }

        if ((pServiceCode == null) || (pServiceCode.length() == 0)) {
            throw new RuntimeException("ServiceCode cannot be null or empty.");
        }

        OfferingWSDTO dto = null;

        try {
            ServiceCode serviceCode = ServiceCode.valueOf(pServiceCode);

            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

            PayrollServices.beginUnitOfWork();

            DomainEntitySet<Company> companySet = Application.find(Company.class, Company.SourceCompanyId().equalTo(pSourceCompanyId));

            if (companySet.isEmpty()) {
                throw new RuntimeException(String.format("Unable to find Company for Source Company Id %s", pSourceCompanyId));
            }

            CompanyOffering companyOffering = companySet.get(0).getOffering(serviceCode);
            
            if (companyOffering != null) {
                Offering offering = companyOffering.getOffering();
                
                dto = new OfferingWSDTO();

                dto.OfferingCode = offering.getOfferingCode().toString();
                dto.Name = offering.getName();
                dto.Description = offering.getDescription();
                dto.ServiceCode = offering.getServiceCode().toString();
                dto.Sku = offering.getSKU();
                dto.IsApproved = offering.getIsApproved();
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return dto;
    }

    @WebMethod
    public CompanyOfferWSDTO getActiveCompanyOffersForOffering(@WebParam(name = "sourceCompanyId") String pSourceCompanyId,
                                                               @WebParam(name = "OfferingCode") String pOfferingCode) {
        if ((pSourceCompanyId == null) || (pSourceCompanyId.length() == 0)) {
            throw new RuntimeException("SourceCompanyId cannot be null or empty.");
        }

        if ((pOfferingCode == null) || (pOfferingCode.length() == 0)) {
            throw new RuntimeException("OfferingCode cannot be null or empty.");
        }

        CompanyOfferWSDTO dto = null;

        try {
            OfferingCode offeringCode = OfferingCode.valueOf(pOfferingCode);

            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

            PayrollServices.beginUnitOfWork();

            DomainEntitySet<Company> companySet = Application.find(Company.class, Company.SourceCompanyId().equalTo(pSourceCompanyId));

            if (companySet.isEmpty()) {
                throw new RuntimeException(String.format("Unable to find Company for Source Company Id %s", pSourceCompanyId));
            }

            DomainEntitySet<CompanyOffer> offerSet = companySet.get(0).getActiveCompanyOffersForOffering(offeringCode);

            for (CompanyOffer companyOffer : offerSet) {
                Offer offer = companyOffer.getOffer();

                dto = new CompanyOfferWSDTO();

                dto.BeginDate = convert(companyOffer.getBeginDate());
                dto.EndDate = convert(companyOffer.getEndDate());
                dto.UsagesRemaining = companyOffer.getUsagesRemaining();
                
                dto.BeginEvent = offer.getBeginEvent().toString();
                dto.Description = offer.getDescription();
                dto.DiscountAmount = offer.getDiscountAmount().toString();
                dto.DiscountPercent = Double.toString(offer.getDiscountPercent());
                dto.DiscountType = offer.getDiscountType().toString();
                dto.DurationDays = offer.getDurationDays();
                dto.EffectiveDate = convert(offer.getEffectiveDate());
                dto.EndDate = convert(offer.getEndDate());
                dto.EndEvent = offer.getEndEvent().toString();
                dto.IsApproved = offer.getIsApproved();
                dto.Name = offer.getName();
                dto.OfferCd = offer.getOfferCd();
                dto.PromotionId = offer.getPromotionId();
                dto.UsagesAllowed = offer.getUsagesAllowed();

                DomainEntitySet<OfferPrice> priceSet = Application.find(OfferPrice.class, OfferPrice.Offer().equalTo(offer));

                for (OfferPrice price : priceSet) {
                    OfferPriceWSDTO priceDto = new OfferPriceWSDTO();

                    priceDto.AltBasePrice = price.getAltBasePrice().toString();
                    priceDto.AltUnitPrice = price.getAltUnitPrice().toString();
                    priceDto.FeeType = price.getFeeType().toString();

                    dto.OfferPriceList.add(priceDto);
                }
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return dto;
    }

    @WebMethod
    public List<BillingDetailWSDTO> getBillingDetailsForPayrollRun(@WebParam(name = "SourcePayrollRunId") String pSourcePayrollRunId,
                                                                   @WebParam(name = "OfferingServiceChargeType") String pOfferingServiceChargeType) {
        if ((pSourcePayrollRunId == null) || (pSourcePayrollRunId.length() == 0)) {
            throw new RuntimeException("PayrollRunId cannot be null or empty.");
        }

        List<BillingDetailWSDTO> list = new ArrayList<BillingDetailWSDTO>();

        try {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

            PayrollServices.beginUnitOfWork();

            DomainEntitySet<PayrollRun> payrollRunSet = Application.find(PayrollRun.class, PayrollRun.SourcePayRunId().equalTo(pSourcePayrollRunId));

            if (payrollRunSet.isEmpty()) {
                throw new RuntimeException(String.format("Unable to find Payroll Run for Payroll Run Id %s", pSourcePayrollRunId));
            }

            DomainEntitySet<BillingDetail> detailSet;

            if ((pOfferingServiceChargeType == null) || (pOfferingServiceChargeType.length() == 0)) {
                detailSet = BillingDetail.findBillingDetails(payrollRunSet.get(0));
            } else {
                OfferingServiceChargeType oscType = OfferingServiceChargeType.valueOf(pOfferingServiceChargeType);
                detailSet = BillingDetail.findBillingDetails(payrollRunSet.get(0), oscType);
            }

            for (BillingDetail detail : detailSet) {
                BillingDetailWSDTO dto = new BillingDetailWSDTO();

                dto.basePrice = detail.getBasePrice().toString();
                dto.billingPeriod = convert(detail.getBillingPeriod());
                dto.discountAmount = detail.getDiscountAmount().toString();
                dto.itemName = detail.getItemName();
                dto.itemSKU = detail.getItemSku();
                dto.itemTotal = detail.getItemTotal().toString();
                dto.memo = detail.getMemo();
                dto.offerCode = detail.getOfferCd();
                dto.offeringServiceChargeType = detail.getOfferingServiceChargeType().toString();
                dto.offerName = detail.getOfferName();
                dto.offloadDate = convert(detail.getOffloadDate());
                dto.quantity = detail.getQuantity();
                dto.serviceCode = detail.getServiceCd();
                dto.serviceDate = convert(detail.getServiceDate());
                dto.taxAmount = detail.getTaxAmount().toString();
                dto.taxWhenOffloaded = detail.getTaxAmountWhenOffloaded().toString();
                dto.taxComputedDate = convert(detail.getTaxComputedDate());
                dto.taxExceptionInd = detail.getTaxExceptionInd();
                dto.taxJurisdiction = detail.getTaxJurisdiction();
                dto.unitPrice = detail.getUnitPrice().toString();
                
                list.add(dto);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return list;
    }
    
    @WebMethod
    @SuppressWarnings({"unchecked", "JpaQlInspection"})
    public List<BillingDetailWSDTO> getBillingDetailsForCompany(@WebParam(name = "sourceCompanyId") String pSourceCompanyId,
                                                                @WebParam(name = "BillingPeriod") Calendar pBillingPeriod,
                                                                @WebParam(name = "OfferingServiceChargeType") String pOfferingServiceChargeType) {
        if ((pSourceCompanyId == null) || (pSourceCompanyId.length() == 0)) {
            throw new RuntimeException("SourceCompanyId cannot be null or empty.");
        }

        List<BillingDetailWSDTO> list = new ArrayList<BillingDetailWSDTO>();

        try {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

            PayrollServices.beginUnitOfWork();

            DomainEntitySet<Company> companySet = Application.find(Company.class, Company.SourceCompanyId().equalTo(pSourceCompanyId));

            if (companySet.isEmpty()) {
                throw new RuntimeException(String.format("Unable to find Company for Source Company Id %s", pSourceCompanyId));
            }

            DomainEntitySet<BillingDetail> detailSet;
            OfferingServiceChargeType oscType = null;

            if (pOfferingServiceChargeType != null) {
                oscType = OfferingServiceChargeType.valueOf(pOfferingServiceChargeType);
            }

            if (pBillingPeriod != null) {
                SpcfCalendar billingPeriod = convert(pBillingPeriod);

                if (oscType == null) {
                    detailSet = BillingDetail.findBillingDetailsInBillingPeriod(companySet.get(0), billingPeriod);
                } else {
                    detailSet = BillingDetail.findBillingDetailsInBillingPeriod(companySet.get(0), billingPeriod, oscType);
                }
            } else {
                String hql;

                if (oscType != null) {
                    hql = "   select detail " +
                          "     from com.intuit.sbd.payroll.psp.domain.BillingDetail as detail " +
                          "     join detail.PayrollRun as payrollRun " +
                          "    where payrollRun.Company = :company " +
                          "      and detail.OfferingServiceChargeType = :oscType " +
                          " order by detail.BillingPeriod, detail.OfferingServiceChargeType ";
                } else {
                    hql = "   select detail " +
                          "     from com.intuit.sbd.payroll.psp.domain.BillingDetail as detail " +
                          "     join detail.PayrollRun as payrollRun " +
                          "    where payrollRun.Company = :company " +
                          " order by detail.BillingPeriod, detail.OfferingServiceChargeType ";
                }


                org.hibernate.Query hibernateQuery = Application.createHibernateQuery(hql);

                if (oscType != null) {
                    hibernateQuery.setParameter("oscType", oscType);
                }

                hibernateQuery.setParameter("company", companySet.get(0));

                detailSet = Application.<BillingDetail>getUniqueActualObjects(hibernateQuery.list());
            }

            for (BillingDetail detail : detailSet) {
                BillingDetailWSDTO dto = new BillingDetailWSDTO();

                dto.basePrice = detail.getBasePrice().toString();
                dto.billingPeriod = convert(detail.getBillingPeriod());
                dto.discountAmount = detail.getDiscountAmount().toString();
                dto.itemName = detail.getItemName();
                dto.itemSKU = detail.getItemSku();
                dto.itemTotal = detail.getItemTotal().toString();
                dto.memo = detail.getMemo();
                dto.offerCode = detail.getOfferCd();
                dto.offeringServiceChargeType = detail.getOfferingServiceChargeType().toString();
                dto.offerName = detail.getOfferName();
                dto.offloadDate = convert(detail.getOffloadDate());
                dto.quantity = detail.getQuantity();
                dto.serviceCode = detail.getServiceCd();
                dto.serviceDate = convert(detail.getServiceDate());
                dto.taxAmount = detail.getTaxAmount().toString();
                dto.taxWhenOffloaded = detail.getTaxAmountWhenOffloaded().toString();
                dto.taxComputedDate = convert(detail.getTaxComputedDate());
                dto.taxExceptionInd = detail.getTaxExceptionInd();
                dto.taxJurisdiction = detail.getTaxJurisdiction();
                dto.unitPrice = detail.getUnitPrice().toString();

                list.add(dto);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return list;
    }

    @WebMethod
    public void setPriceType(@WebParam(name = "sourceCompanyId") String pSourceCompanyId,
                             @WebParam(name = "priceType") String pPriceType) throws Exception {
        if (pSourceCompanyId == null || StringUtils.isEmpty(pSourceCompanyId)) {
            throw new RuntimeException("SourceCompanyId cannot be null or empty.");
        }

        if (pPriceType == null || StringUtils.isEmpty(pPriceType)) {
            throw new RuntimeException("pPriceType cannot be null or empty.");
        }
        try {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

            PayrollServices.beginUnitOfWork();

            DomainEntitySet<Company> companySet = Application.find(Company.class, Company.SourceCompanyId().equalTo(pSourceCompanyId));

            if (companySet.isEmpty()) {
                throw new RuntimeException(String.format("Unable to find Company for Source Company Id %s", pSourceCompanyId));
            }

            Company company = companySet.getFirst();
            
            String offerCode = null;
            if(company.getCompanyOffers().size() > 0) {
                offerCode = company.getCompanyOffers().getFirst().getOffer().getOfferCd();
            }
            
            PayrollServices.rollbackUnitOfWork();
            
            try {
                new CompanyAdapter().setAssistedPriceTypeAndOffer(SourceSystemCode.QBDT.toString(), pSourceCompanyId, pPriceType, offerCode);
            } catch (Throwable t) {
                throw new Exception(t);
            }

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public AnnualBillingBatchWSDTO getAnnualBillingBatch(@WebParam(name = "FormType") String pFormType,
                                      @WebParam(name = "FormYear") Integer pFormYear) throws Exception {
        AnnualBillingBatchWSDTO response = null;
        try {
            PayrollServices.beginUnitOfWork();

            FormTypeCode formTypeCode = FormTypeCode.valueOf(pFormType);

            AnnualBillingBatch annualBillingBatch = AnnualBillingBatch.findAnnualBillingBatch(formTypeCode, pFormYear);

            if (annualBillingBatch != null) {
                response = new AnnualBillingBatchWSDTO();

                response.formType = annualBillingBatch.getFormTypeCd().toString();
                response.formYear = annualBillingBatch.getFormYear();
                response.status = annualBillingBatch.getAnnualBillingBatchStatusCd().toString();

                for (AnnualBillingItem annualBillingItem : annualBillingBatch.getAnnualBillingItemCollection()) {
                    if (response.annualBillingItems == null) {
                        response.annualBillingItems = new ArrayList<AnnualBillingItemWSDTO>();
                    }
                    AnnualBillingItemWSDTO item = new AnnualBillingItemWSDTO();

                    item.psid = annualBillingItem.getCompany().getSourceCompanyId();
                    item.formCount = annualBillingItem.getFormCount();
                    item.status = annualBillingItem.getAnnualBillingItemStatusCd().toString();
                    item.errorMessage = annualBillingItem.getErrorMessage();

                    response.annualBillingItems.add(item);
                }
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return response;
    }
}
