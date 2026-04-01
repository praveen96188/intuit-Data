package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.DeliveryPreferenceCode;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * @author Jeff Jones
 */
public class TaxServiceInfoDTO extends ServiceInfoDTO {
    private int lastQuarterToFile;
    private int lastTaxYear;
    private DeliveryPreferenceCode w2DeliveryPreferenceCd;
    private DeliveryPreferenceCode clientPacketDeliveryPreferenceCd;
    private boolean fileAnnualReturns;
    private boolean finalAnnualReturns;
    private SpcfCalendar lastPayrollDate;
    private boolean inHouseW2;
    private boolean includeOnSsaFile;

    public TaxServiceInfoDTO() {
        this.setServiceCode(ServiceCode.Tax);
        this.setW2DeliveryPreferenceCd(DeliveryPreferenceCode.Mail);
        this.setClientPacketDeliveryPreferenceCd(DeliveryPreferenceCode.Electronic);
    }

    public int getLastQuarterToFile() {
        return lastQuarterToFile;
    }

    public void setLastQuarterToFile(int lastQuarterToFile) {
        this.lastQuarterToFile = lastQuarterToFile;
    }

    public int getLastTaxYear() {
        return lastTaxYear;
    }

    public void setLastTaxYear(int lastTaxYear) {
        this.lastTaxYear = lastTaxYear;
    }

    public DeliveryPreferenceCode getW2DeliveryPreferenceCd() {
        return w2DeliveryPreferenceCd;
    }

    public void setW2DeliveryPreferenceCd(DeliveryPreferenceCode w2DeliveryPreferenceCd) {
        this.w2DeliveryPreferenceCd = w2DeliveryPreferenceCd;
    }

    public DeliveryPreferenceCode getClientPacketDeliveryPreferenceCd() {
        return clientPacketDeliveryPreferenceCd;
    }

    public void setClientPacketDeliveryPreferenceCd(DeliveryPreferenceCode clientPacketDeliveryPreferenceCd) {
        this.clientPacketDeliveryPreferenceCd = clientPacketDeliveryPreferenceCd;
    }
    public boolean getFileAnnualReturns() {
        return fileAnnualReturns;
    }

    public void setFileAnnualReturns(boolean fileAnnualReturns) {
        this.fileAnnualReturns = fileAnnualReturns;
    }

    public boolean isFinalAnnualReturns() {
        return finalAnnualReturns;
    }

    public void setFinalAnnualReturns(boolean finalAnnualReturns) {
        this.finalAnnualReturns = finalAnnualReturns;
    }

    public SpcfCalendar getLastPayrollDate() {
        return lastPayrollDate;
    }

    public void setLastPayrollDate(SpcfCalendar lastPayrollDate) {
        this.lastPayrollDate = lastPayrollDate;
    }

    public boolean isInHouseW2() {
        return inHouseW2;
    }

    public void setInHouseW2(boolean inHouseW2) {
        this.inHouseW2 = inHouseW2;
    }

    public boolean isIncludeOnSsaFile() {
        return includeOnSsaFile;
    }

    public void setIncludeOnSsaFile(boolean includeOnSsaFile) {
        this.includeOnSsaFile = includeOnSsaFile;
    }
}
