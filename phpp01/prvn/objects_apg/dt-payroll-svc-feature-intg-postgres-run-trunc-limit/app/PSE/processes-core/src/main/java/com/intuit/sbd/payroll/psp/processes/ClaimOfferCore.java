package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyOffer;
import com.intuit.sbd.payroll.psp.domain.Offer;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: wnichols
 * Date: Apr 30, 2008
 * Time: 3:24:23 PM
 * Claims (or redeems) an offer for a company.
 */
public class ClaimOfferCore extends Process implements IProcess {
    private Company company;
    private String offerCd;
    private String promotionId;

    private Offer offer;

    public ClaimOfferCore(Company pCompany, String pOfferCd, String pPromotionId) {
        company = pCompany;
        offerCd = pOfferCd;
        promotionId = pPromotionId;
    }

    public ProcessResult validate() {
        ProcessResult result = new ProcessResult();

        if (company == null) {
            result.getMessages().CompanyNotSpecified(EntityName.Company, null);
        }

        if ((offerCd == null || offerCd.length() == 0) && (promotionId == null || promotionId.length() == 0)) {
            result.getMessages().RequiredInputMissingOrBlank(EntityName.Company, null, "OfferCd");
        }

        if (offerCd != null && offerCd.length() > 0) {
            offer = Offer.findOfferByOfferCode(offerCd);
            if (offer == null) {
                result.getMessages().NoEntityWithGivenId("Offer", offerCd);
            } else {
                // make sure the offer has not already expired
                SpcfCalendar today = PSPDate.getPSPTime();
                CalendarUtils.clearTime(today);

                if (offer.getEndDate() != null) {
                    SpcfCalendar offerEndDate = offer.getEndDate().toLocal();
                    CalendarUtils.clearTime(offerEndDate);

                    if (offerEndDate.before(today)) {
                        result.getMessages().GenericError(EntityName.Company, offerCd, "Offer has expired");
                    }
                }
                if (offer.getEffectiveDate() != null) {
                    SpcfCalendar offerEffectiveDate = offer.getEffectiveDate().toLocal();
                    CalendarUtils.clearTime(offerEffectiveDate);

                    if (offerEffectiveDate.after(today)) {
                        result.getMessages().GenericError(EntityName.Company, offerCd, "Offer has not started");
                    }
                }
            }
        } else {
            offer = Offer.findOfferByPromotionId(promotionId);
            if (offer == null) {
                result.getMessages().NoEntityWithGivenId("Offer", promotionId);
            } else {
                // make sure the offer has not already expired
                SpcfCalendar today = PSPDate.getPSPTime();
                CalendarUtils.clearTime(today);

                if (offer.getEndDate() != null) {
                    SpcfCalendar offerEndDate = offer.getEndDate().toLocal();
                    CalendarUtils.clearTime(offerEndDate);

                    if (offerEndDate.before(today)) {
                        result.getMessages().GenericError(EntityName.Company, promotionId, "Offer has expired");
                    }
                }
            }
        }

        return result;
    }

    public ProcessResult<CompanyOffer> process() {
        ProcessResult<CompanyOffer> result = new ProcessResult<CompanyOffer>();

        CompanyOffer co = company.claimOfferForCompany(offer);

        result.setResult(co);
        return result;
    }


}
