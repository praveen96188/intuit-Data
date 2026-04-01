package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.DiscountType;
import com.intuit.sbd.payroll.psp.domain.OfferBeginEvent;
import com.intuit.sbd.payroll.psp.domain.OfferEndEvent;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wnichols
 * Date: Feb 21, 2008
 * Time: 10:44:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class OfferDTO {
    private String id;             // SPCF unique ID of the entity
    private String offerCd;        // Offer code as defined by marketing
    private String name;           // Short name of the offer
    private String description;    // Long description of the offer
    private Calendar effectiveDate; // The earliest date when this offer should be applied to any charge
    private Boolean isApproved;    // Whether the Offer has been approved for use
    private DiscountType discountType;   // Whether the discount is quoted in dollars or percentage
    private BigDecimal discountAmount;     // When DiscountType=AmountOff, this is the dollar amount of the discount, as a positive quantity
    private BigDecimal discountPercent;    // When DiscountType=PercentOff, this is the magnitude of the discount, in percent
    private OfferBeginEvent beginEvent;     // The condition that enables or activates the offer for a given company
    private OfferEndEvent endEvent;       // The condition that disables or deactivates the event for a given company
    private Calendar endDate;      // When EndEvent=DateEvent, this is the date when the offer ends (expires)
    private Integer durationDays;  // When EndEvent=DurationEvent, this the number of days after the BeginEvent when the offer is valid
    private Integer usagesAllowed; // When EndEvent=PayrollUsageEvent, this is the number of payroll submissions (after the begin event has occurred) subject to the offer
    private List<String> serviceChargeIds = new ArrayList(); // List of 0 or more SPCF unique IDs of OfferingServiceCharge entities subject to this offer

    public String getId() {
        return id;
    }

    public void setId(String pId) {
        id = pId;
    }

    public String getOfferCd() {
        return offerCd;
    }

    public void setOfferCd(String pOfferCd) {
        offerCd = pOfferCd;
    }

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String pDescription) {
        description = pDescription;
    }

    public Calendar getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Calendar pEffectiveDate) {
        effectiveDate = pEffectiveDate;
    }

    public Boolean getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(Boolean pIsApproved) {
        isApproved = pIsApproved;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType pDiscountType) {
        discountType = pDiscountType;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal pDiscountAmount) {
        discountAmount = pDiscountAmount;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal pDiscountPercent) {
        discountPercent = pDiscountPercent;
    }

    public OfferBeginEvent getBeginEvent() {
        return beginEvent;
    }

    public void setBeginEvent(OfferBeginEvent pBeginEvent) {
        beginEvent = pBeginEvent;
    }

    public OfferEndEvent getEndEvent() {
        return endEvent;
    }

    public void setEndEvent(OfferEndEvent pEndEvent) {
        endEvent = pEndEvent;
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public void setEndDate(Calendar pEndDate) {
        endDate = pEndDate;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer pDurationDays) {
        durationDays = pDurationDays;
    }

    public Integer getUsagesAllowed() {
        return usagesAllowed;
    }

    public void setUsagesAllowed(Integer pUsagesAllowed) {
        usagesAllowed = pUsagesAllowed;
    }

    public List<String> getServiceChargeIds() {
        return serviceChargeIds;
    }

    /**
     * Performs validations common to more than one operation.
     * @return a ProcessResult
     */
    public ProcessResult validateCommon()
    {
        ProcessResult result = new ProcessResult();

        // Name non-null and non-empty	5002 Required 'Name' input is missing or blank
        if (getName()==null || getName().length()==0) {
            result.getMessages().RequiredInputMissingOrBlank(null, null, "Name");
        }

        // EffectiveDate non-null	5002 Required 'EffectiveDate' input is missing or blank
        if (getEffectiveDate() == null) {
            result.getMessages().RequiredInputMissingOrBlank(null, null, "EffectiveDate");
        }

        // DiscountType non-null and non-empty	5002 Required 'DiscountType' input is missing or blank
        if (getDiscountType() == null) {
            result.getMessages().RequiredInputMissingOrBlank(null, null, "DiscountType");
        } else {
            if (getDiscountType() == DiscountType.AmountOff) {
                // DiscountAmount non-null	5002 Required 'DiscountAmount' input is missing or blank
                if (getDiscountAmount() == null) {
                    result.getMessages().RequiredInputMissingOrBlank(null, null, "DiscountAmount");
                } else {
                    // DiscountAmount in range	5001 DiscountAmount has invalid value
                    if (getDiscountAmount().doubleValue() < 0.0) {
                        result.getMessages().InvalidValue(null, null, "DiscountAmount");
                    }
                }
            } else if (getDiscountType() == DiscountType.PercentOff) {
                // DiscountPercent non-null	5002 Required 'DiscountPercent' input is missing or blank
                if (getDiscountPercent() == null) {
                    result.getMessages().RequiredInputMissingOrBlank(null, null, "DiscountPercent");
                } else {
                    // DiscountPercent in range	5001 DiscountPercent has invalid value
                    if (getDiscountPercent().doubleValue() < 0.0 || getDiscountPercent().doubleValue() > 100.0) {
                        result.getMessages().InvalidValue(null, null, "DiscountPercent");
                    }
                }
            }
        }

        // BeginEvent non-null and non-empty	5002 Required 'BeginEvent' input is missing or blank
        if (getBeginEvent() == null) {
            result.getMessages().RequiredInputMissingOrBlank(null, null, "BeginEvent");
        }

        // EndEvent non-null and non-empty	5002 Required 'EndEvent' input is missing or blank
        if (getEndEvent() == null) {
            result.getMessages().RequiredInputMissingOrBlank(null, null, "EndEvent");
        } else {
            if (getEndEvent() == OfferEndEvent.DateEvent) {
                // EndDate non-null *	5002 Required 'EndDate' input is missing or blank
                if (getEndDate() == null) {
                    result.getMessages().RequiredInputMissingOrBlank(null, null, "EndDate");
                }
            } else if (getEndEvent() == OfferEndEvent.DurationEvent) {
                // DurationDays non-null
                if (getDurationDays() == null) {
                    result.getMessages().RequiredInputMissingOrBlank(null, null, "DurationDays");
                } else {
                    // DurationDays in range *	5001 DurationDays has invalid value
                    if (getDurationDays() < 1) {
                        result.getMessages().InvalidValue(null, null, "DurationDays");
                    }
                }
            } else if (getEndEvent() == OfferEndEvent.PayrollUsageEvent) {
                // UsagesAllowed non-null
                if (getUsagesAllowed() == null) {
                    result.getMessages().RequiredInputMissingOrBlank(null, null, "UsagesAllowed");
                } else {
                    // UsagesAllowed in range *	5001 UsagesAllowed has invalid value
                    if (getUsagesAllowed() < 1) {
                        result.getMessages().InvalidValue(null, null, "UsagesAllowed");
                    }
                }
            }
        }

        return result;
    }
}
