package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;

import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: wnichols
 * Date: Feb 19, 2008
 * Time: 1:30:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class OfferingServiceChargePriceDTO {
    private String id;                 // SPCF unique ID of the price entity
    private String chargeId;           // SPCF unique ID of the parent OfferingServiceCharge entity
    private BigDecimal basePrice;      // the price in dollars
    private BigDecimal unitPrice;      // the price in dollars
    private Calendar effectiveDate;    // the date when this price will go into effect

    public String getId() {
        return id;
    }

    public void setId(String pId) {
        id = pId;
    }

    public String getChargeId() {
        return chargeId;
    }

    public void setChargeId(String pChargeId) {
        chargeId = pChargeId;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal pPrice) {
        basePrice = pPrice;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal pPrice) {
        unitPrice = pPrice;
    }

    public Calendar getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Calendar pEffectiveDate) {
        effectiveDate = pEffectiveDate;
    }

    /**
     * Perform validations common to more than one operation.
     * @return a ProcessResult
     */
    public ProcessResult validateCommon() {
        ProcessResult result = new ProcessResult();

        // BasePrice non-null	5002 Required 'BasePrice' is missing or blank
        if (getBasePrice() == null) {
            result.getMessages().RequiredInputMissingOrBlank(null, null, "BasePrice");
        } else {
            // BasePrice >= 0.00	5001 BasePrice has invalid value
            if (getBasePrice().compareTo(BigDecimal.ZERO) < 0) {
                result.getMessages().InvalidValue(null, null, "BasePrice");
            }
        }

        // UnitPrice non-null	5002 Required 'UnitPrice' is missing or blank
        if (getUnitPrice() == null) {
            result.getMessages().RequiredInputMissingOrBlank(null, null, "UnitPrice");
        } else {
            // UnitPrice >= 0.00	5001 UnitPrice has invalid value
            if (getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                result.getMessages().InvalidValue(null, null, "UnitPrice");
            }
        }

        return result;
    }
}
