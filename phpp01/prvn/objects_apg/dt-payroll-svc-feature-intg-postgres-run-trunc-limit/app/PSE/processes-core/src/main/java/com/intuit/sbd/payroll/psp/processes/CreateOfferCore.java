package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.OfferDTO;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.domain.DiscountType;
import com.intuit.sbd.payroll.psp.domain.Offer;
import com.intuit.sbd.payroll.psp.domain.OfferEndEvent;
import com.intuit.sbd.payroll.psp.domain.OfferingServiceCharge;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wnichols
 * Date: Feb 21, 2008
 * Time: 10:43:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class CreateOfferCore extends Process implements IProcess {
    private OfferDTO dtoInput;
    private List<OfferingServiceCharge> serviceCharges = new ArrayList<OfferingServiceCharge>();

    public CreateOfferCore(OfferDTO pDTO) {
        dtoInput = pDTO;
    }

    public ProcessResult validate() {
        // perform common validations first
        ProcessResult result = dtoInput.validateCommon();

        // OfferCd non-null and non-empty	5002 Required 'OfferCd' input is missing or blank
        if (dtoInput.getOfferCd()==null || dtoInput.getOfferCd().length()==0) {
            result.getMessages().RequiredInputMissingOrBlank(null, null, "OfferCd");
        } else {
            // OfferCd is unique	400 OfferCd '{0}' must be unique
            if (Offer.findOfferByOfferCode(dtoInput.getOfferCd()) != null) {
                result.getMessages().PropertyValueNotUnique("OfferCd", dtoInput.getOfferCd());
            }
        }

        // ServiceCharges (GUIDs) refer to entities that exist	5003 OfferingServiceCharge '{1}' does not exist
        for (String chargeId : dtoInput.getServiceChargeIds()) {
            OfferingServiceCharge charge = Application.findById(OfferingServiceCharge.class, SpcfUniqueId.createInstance(chargeId));
            if (charge == null) {
                result.getMessages().NoEntityWithGivenId("OfferingServiceCharge", chargeId);
            } else if (! serviceCharges.contains(charge)) {
                serviceCharges.add(charge);
            }
        }

        return result;
    }

    public ProcessResult<Offer> process() {
        ProcessResult<Offer> result = new ProcessResult<Offer>();

        // create and save the entity
        Offer offer = new Offer();
        offer.setOfferCd( dtoInput.getOfferCd() );
        offer.setName( dtoInput.getName() );
        offer.setDescription( dtoInput.getDescription() );
        offer.setEffectiveDate( CalendarUtils.convertToSpcfCalendar(dtoInput.getEffectiveDate()) );
        offer.setIsApproved( false );
        offer.setDiscountType(dtoInput.getDiscountType());
        offer.setBeginEvent(dtoInput.getBeginEvent());
        offer.setEndEvent(dtoInput.getEndEvent());

        if (dtoInput.getDiscountType() == DiscountType.AmountOff) {
            offer.setDiscountAmount( SpcfUtils.convertToSpcfMoney(dtoInput.getDiscountAmount()) );
        }
        else if (dtoInput.getDiscountType() == DiscountType.PercentOff) {
            offer.setDiscountPercent( dtoInput.getDiscountPercent().doubleValue() );
        }

        if (dtoInput.getEndEvent() == OfferEndEvent.DateEvent) {
            offer.setEndDate( CalendarUtils.convertToSpcfCalendar(dtoInput.getEndDate()) );
        }
        else if (dtoInput.getEndEvent() == OfferEndEvent.DurationEvent) {
            offer.setDurationDays( dtoInput.getDurationDays() );
        }
        else if (dtoInput.getEndEvent() == OfferEndEvent.PayrollUsageEvent) {
            offer.setUsagesAllowed( dtoInput.getUsagesAllowed() );
        }

        for (OfferingServiceCharge charge : serviceCharges) {
            offer.addOfferingServiceCharge(charge);
        }

        offer = Application.save(offer);

        result.setResult(offer);
        return result;
    }

}
