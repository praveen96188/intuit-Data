package com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers;

import com.intuit.sbd.payroll.psp.common.ofx.request.IPITEM;
import com.intuit.sbd.payroll.psp.common.ofx.request.IRATECHANGE;
import com.intuit.sbd.payroll.psp.common.ofx.request.ITAXITEM;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.PayrollItemCode;
import com.intuit.sbd.payroll.psp.domain.PayrollItemStatus;
import com.intuit.sbd.payroll.psp.domain.PayrollItemType;
import com.intuit.sbd.payroll.psp.domain.QbdtNumericType;
import com.intuit.sbd.payroll.psp.domain.QbdtPayType;
import com.intuit.sbd.payroll.psp.domain.QbdtSpecialType;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 11, 2010
 * Time: 2:45:11 PM
 */
public class PayrollItem {
    private IPITEM mIPITEM;
    private QBOFX.OFXPayrollItemType mItemType;

    public PayrollItem(IPITEM pIPITEM) {
        mIPITEM = pIPITEM;
        if(mIPITEM.getIADDITEM() != null) {
            mItemType = QBOFX.OFXPayrollItemType.Addition;
        } else if(mIPITEM.getIBONUSITEM() != null) {
            mItemType = QBOFX.OFXPayrollItemType.Bonus;
        } else if(mIPITEM.getICOMMITEM() != null) {
            mItemType = QBOFX.OFXPayrollItemType.Commission;
        } else if(mIPITEM.getICONTRIBITEM() != null) {
            mItemType = QBOFX.OFXPayrollItemType.EmployerContribution;
        } else if(mIPITEM.getIDEDUCTITEM() != null) {
            mItemType = QBOFX.OFXPayrollItemType.Deduction;
        } else if(mIPITEM.getIDDITEM() != null) {
            mItemType = QBOFX.OFXPayrollItemType.DirectDeposit;
        } else if(mIPITEM.getIHRLYITEM() != null) {
            mItemType = QBOFX.OFXPayrollItemType.Hourly;
        } else if(mIPITEM.getISALARYITEM() != null) {
            mItemType = QBOFX.OFXPayrollItemType.Salary;
        } else if(mIPITEM.getITAXITEM() != null) {
            mItemType = QBOFX.OFXPayrollItemType.Tax;
        }
    }

    public QBOFX.OFXPayrollItemType getItemType() {
        return mItemType;
    }

    public boolean isTaxItem() {
        return getItemType() == QBOFX.OFXPayrollItemType.Tax;
    }

    public String getSourceId() {
        return mIPITEM.getIPITEMID();
    }

    public String getListId() {
        return mIPITEM.getIQBUNIQUEID();
    }

    public String getSourceDescription() {
        return QBOFX.truncateOFXString(mIPITEM.getIPITEMNAME(), 50);
    }

    public PayrollItemCode getPayrollItemCode() {
        return mapPayrollItemCode();
    }

    public PayrollItemType getPayrollItemType() {
        return QBOFX.mapPayrollItemType(mItemType);
    }

    public PayrollItemStatus getPayrollItemStatus() {
        return QBOFX.mapOFXStringToBoolean(mIPITEM.getIINACTIVE()) ? PayrollItemStatus.Inactive : PayrollItemStatus.Active;
    }

    public boolean getEarningsTable() {
        return mItemType == QBOFX.OFXPayrollItemType.Commission && QBOFX.mapOFXStringToBoolean(mIPITEM.getICOMMITEM().getIEARNINGSTABLE());
    }

    public String getTaxFormLine() {
        switch (mItemType) {
            case Addition:
                return QBOFX.getTaxFormLineFromValue(mIPITEM.getIADDITEM().getITAXFORMLINE());
            case Commission:
                return QBOFX.getTaxFormLineFromValue(mIPITEM.getICOMMITEM().getITAXFORMLINE());
            case Deduction:
                return QBOFX.getTaxFormLineFromValue(mIPITEM.getIDEDUCTITEM().getITAXFORMLINE());
            case EmployerContribution:
                return QBOFX.getTaxFormLineFromValue(mIPITEM.getICONTRIBITEM().getITAXFORMLINE());
            case Tax:
                return QBOFX.getTaxFormLineFromValue(mIPITEM.getITAXITEM().getITAXFORMLINE());
            default:
                return null;
        }
    }

    public Integer getW2Code() {
        switch (mItemType) {
            case Addition:
                return mIPITEM.getIADDITEM().getIW2CODE();
            case Commission:
                return mIPITEM.getICOMMITEM().getIW2CODE();
            case Deduction:
                return mIPITEM.getIDEDUCTITEM().getIW2CODE();
            case EmployerContribution:
                return mIPITEM.getICONTRIBITEM().getIW2CODE();
            case Tax:
                return mIPITEM.getITAXITEM().getIW2CODE();
            default:
                return null;
        }
    }

    public boolean getIsEmployeePaid() {
        return QBOFX.mapOFXStringToBoolean(mIPITEM.getIISEMP());
    }

    public Double getRateMultiplier() {
        return (this.getIsEmployeePaid()) ? -1d : 1d;
    }

    public String getLiabilityAccount() {
        String liabilityAccount;
        switch (mItemType) {
            case Addition:
                liabilityAccount = mIPITEM.getIADDITEM().getILIABACCT();
                break;
            case Commission:
                liabilityAccount = mIPITEM.getICOMMITEM().getILIABACCT();
                break;
            case Deduction:
                liabilityAccount = mIPITEM.getIDEDUCTITEM().getILIABACCT();
                break;
            case DirectDeposit:
                liabilityAccount = mIPITEM.getIDDITEM().getILIABACCT();
                break;
            case EmployerContribution:
                liabilityAccount = mIPITEM.getICONTRIBITEM().getILIABACCT();
                break;
            case Tax:
                liabilityAccount = mIPITEM.getITAXITEM().getILIABACCT();
                break;
            default:
                liabilityAccount = null;
                break;
        }

        return QBOFX.truncateOFXString(liabilityAccount, 210);
    }

    public String getLiabilityAgency() {
        switch (mItemType) {
            case Addition:
                return QBOFX.truncateOFXString(mIPITEM.getIADDITEM().getILIABAGENCY(), 255);
            case Commission:
                return QBOFX.truncateOFXString(mIPITEM.getICOMMITEM().getILIABAGENCY(), 255);
            case Deduction:
                return QBOFX.truncateOFXString(mIPITEM.getIDEDUCTITEM().getILIABAGENCY(), 255);
            case DirectDeposit:
                return QBOFX.truncateOFXString(mIPITEM.getIDDITEM().getILIABAGENCY(), 255);
            case EmployerContribution:
                return QBOFX.truncateOFXString(mIPITEM.getICONTRIBITEM().getILIABAGENCY(), 255);
            case Tax:
                return QBOFX.truncateOFXString(mIPITEM.getITAXITEM().getILIABAGENCY(), 255);
            default:
                return null;
        }
    }

    public String getAgencyId() {
        switch (mItemType) {
            case Addition:
                return QBOFX.truncateOFXString(mIPITEM.getIADDITEM().getICOMPID(), 24);
            case Commission:
                return QBOFX.truncateOFXString(mIPITEM.getICOMMITEM().getICOMPID(), 24);
            case Deduction:
                return QBOFX.truncateOFXString(mIPITEM.getIDEDUCTITEM().getICOMPID(), 24);
            case DirectDeposit:
                return QBOFX.truncateOFXString(mIPITEM.getIDDITEM().getICOMPID(), 24);
            case EmployerContribution:
                return QBOFX.truncateOFXString(mIPITEM.getICONTRIBITEM().getICOMPID(), 24);
            case Tax:
                return QBOFX.truncateOFXString(mIPITEM.getITAXITEM().getICOMPID(), 24);
            default:
                return null;
        }
    }

    public boolean getAdjustsGross() {
        switch (mItemType) {
            case Addition:
                return QBOFX.mapOFXStringToBoolean(mIPITEM.getIADDITEM().getIADJGROSS());
            case Commission:
                return QBOFX.mapOFXStringToBoolean(mIPITEM.getICOMMITEM().getIADJGROSS());
            case Deduction:
                return QBOFX.mapOFXStringToBoolean(mIPITEM.getIDEDUCTITEM().getIADJGROSS());
            case EmployerContribution:
                return QBOFX.mapOFXStringToBoolean(mIPITEM.getICONTRIBITEM().getIADJGROSS());
            case Tax:
                return QBOFX.mapOFXStringToBoolean(mIPITEM.getITAXITEM().getIADJGROSS());
            default:
                return false;
        }
    }

    public boolean getBasedOnQuantity() {
        switch (mItemType) {
            case Addition:
                return QBOFX.mapOFXStringToBoolean(mIPITEM.getIADDITEM().getIBASEDONQTY());
            case Commission:
                return QBOFX.mapOFXStringToBoolean(mIPITEM.getICOMMITEM().getIBASEDONQTY());
            case Deduction:
                return QBOFX.mapOFXStringToBoolean(mIPITEM.getIDEDUCTITEM().getIBASEDONQTY());
            case EmployerContribution:
                return QBOFX.mapOFXStringToBoolean(mIPITEM.getICONTRIBITEM().getIBASEDONQTY());
            case Tax:
                return QBOFX.mapOFXStringToBoolean(mIPITEM.getITAXITEM().getIBASEDONQTY());
            default:
                return false;
        }
    }

    public String getExpenseAccount() {
        switch (mItemType) {
            case Addition:
                return QBOFX.truncateOFXString(mIPITEM.getIADDITEM().getIEXPACCT(), 255);
            case Bonus:
                return QBOFX.truncateOFXString(mIPITEM.getIBONUSITEM().getIEXPACCT(), 255);
            case Commission:
                return QBOFX.truncateOFXString(mIPITEM.getICOMMITEM().getIEXPACCT(), 255);
            case EmployerContribution:
                return QBOFX.truncateOFXString(mIPITEM.getICONTRIBITEM().getIEXPACCT(), 255);
            case Hourly:
                return QBOFX.truncateOFXString(mIPITEM.getIHRLYITEM().getIEXPACCT(), 255);
            case Salary:
                return QBOFX.truncateOFXString(mIPITEM.getISALARYITEM().getIEXPACCT(), 255);
            case Tax:
                return QBOFX.truncateOFXString(mIPITEM.getITAXITEM().getIEXPACCT(), 255);
            default:
                return null;
        }
    }

    public double getOvertimeMultiplier() {
        if (mItemType ==  QBOFX.OFXPayrollItemType.Hourly && mIPITEM.getIHRLYITEM().getIOTMULTIPLIER() != null) {
            return mIPITEM.getIHRLYITEM().getIOTMULTIPLIER();
        }

        return 1.0;
    }

    public long getDetailType() {
        if (mIPITEM.getIDETAILTYPE() != null) {
            return mIPITEM.getIDETAILTYPE();
        }

        return -1;
    }

    public double getDefaultRate() {
        switch (mItemType) {
            case Addition:
                return QBOFX.mapOFXStringToDouble(mIPITEM.getIADDITEM().getIDEFRATE());
            case Commission:
                return QBOFX.mapOFXStringToDouble(mIPITEM.getICOMMITEM().getIDEFRATE());
            case Deduction:
                return QBOFX.mapOFXStringToDouble(mIPITEM.getIDEDUCTITEM().getIDEFRATE());
            case EmployerContribution:
                return QBOFX.mapOFXStringToDouble(mIPITEM.getICONTRIBITEM().getIDEFRATE());
            default:
                return 0;
        }
    }

    public QbdtNumericType getDefaultRateType() {
        switch (mItemType) {
            case Addition:
                return QBOFX.mapOFXStringNumericType(mIPITEM.getIADDITEM().getIDEFRATE());
            case Commission:
                return QBOFX.mapOFXStringNumericType(mIPITEM.getICOMMITEM().getIDEFRATE());
            case Deduction:
                return QBOFX.mapOFXStringNumericType(mIPITEM.getIDEDUCTITEM().getIDEFRATE());
            case EmployerContribution:
                return QBOFX.mapOFXStringNumericType(mIPITEM.getICONTRIBITEM().getIDEFRATE());
            default:
                return null;
        }
    }


    public SpcfMoney getDefaultLimit() {
        switch (mItemType) {
            case Addition:
                return QBOFX.mapOFXStringToMoney(mIPITEM.getIADDITEM().getIDEFLIMIT());
            case Commission:
                return QBOFX.mapOFXStringToMoney(mIPITEM.getICOMMITEM().getIDEFLIMIT());
            case Deduction:
                return QBOFX.mapOFXStringToMoney(mIPITEM.getIDEDUCTITEM().getIDEFLIMIT());
            case EmployerContribution:
                return QBOFX.mapOFXStringToMoney(mIPITEM.getICONTRIBITEM().getIDEFLIMIT());
            case Tax:
                return QBOFX.mapOFXStringToMoney(mIPITEM.getITAXITEM().getIDEFLIMIT());
            default:
                return null;
        }
    }

    public boolean getExpenseByJob() {
        switch (mItemType) {
            case Addition:
                return QBOFX.mapOFXStringToBoolean(mIPITEM.getIADDITEM().getIEXPBYJOB());
            case Commission:
                return QBOFX.mapOFXStringToBoolean(mIPITEM.getICOMMITEM().getIEXPBYJOB());
            case Deduction:
                return QBOFX.mapOFXStringToBoolean(mIPITEM.getIDEDUCTITEM().getIDEFLIMIT());
            case EmployerContribution:
                return QBOFX.mapOFXStringToBoolean(mIPITEM.getICONTRIBITEM().getIEXPBYJOB());
            default:
                return false;
        }
    }

    public boolean getOnService() {
        switch (mItemType) {
            case Addition:
                return QBOFX.mapOFXStringToBoolean(mIPITEM.getIADDITEM().getIONSERVICE());
            case Commission:
                return QBOFX.mapOFXStringToBoolean(mIPITEM.getICOMMITEM().getIONSERVICE());
            case Deduction:
                return QBOFX.mapOFXStringToBoolean(mIPITEM.getIDEDUCTITEM().getIONSERVICE());
            case EmployerContribution:
                return QBOFX.mapOFXStringToBoolean(mIPITEM.getICONTRIBITEM().getIONSERVICE());
            case Tax:
                return QBOFX.mapOFXStringToBoolean(mIPITEM.getITAXITEM().getIONSERVICE());
            default:
                return false;
        }
    }

    public QbdtPayType getPayType() {
        switch (mItemType) {
            case Hourly:
                return QbdtPayType.valueOf(mIPITEM.getIHRLYITEM().getIPAYTYPE());
            case Salary:
                return QbdtPayType.valueOf(mIPITEM.getISALARYITEM().getIPAYTYPE());
            default:
                return null;
        }
    }

    public QbdtSpecialType getSpecialType() {
        return QBOFX.getSpecialTypeFromOFXValue(mIPITEM.getISPECIALTYPE());
    }

    public String getSourceLawId() {
        if(mItemType == QBOFX.OFXPayrollItemType.Tax) {
            ITAXITEM taxItem = mIPITEM.getITAXITEM();
            if(taxItem.getIFEDTAX() != null) {
                return taxItem.getIFEDTAX();
            } else if(taxItem.getISTATETAXDESC() != null) {
                return taxItem.getISTATETAXDESC().getISTATE() + " " + taxItem.getISTATETAXDESC().getISTATETAX();
            } else if(taxItem.getIOTHERTAX() != null) { 
                if("9".equals(taxItem.getIOTHERTAX())) {
                    return "177";
                } else {
                    return taxItem.getIOTHERTAX();
                }
            }
        }
        return null;
    }
    
    public Rate getFutureRate() {
        if(mItemType == QBOFX.OFXPayrollItemType.Tax && QBOFX.nullStringCheck(mIPITEM.getITAXITEM().getIRATE()) != null) {
            Double rate =  getRateMultiplier() * QBOFX.mapOFXStringToDouble(mIPITEM.getITAXITEM().getIRATE());
            QbdtNumericType rateType = Rate.getRateType(mIPITEM.getITAXITEM().getIRATE());
            //if no rate type is found, it is assumed to be a percentage
            // if rate type is percentage then divide by 100 else leave as it is
            if(rateType == null || rateType.equals(QbdtNumericType.Percentage)){
                rate = rate / 100;
            }

            return new Rate(rate,rateType);
        }

        return null;
    }

    public Map<Date, Rate> getRateChanges() {

        if(mItemType == QBOFX.OFXPayrollItemType.Tax && mIPITEM.getITAXITEM().getIRATECHANGE() != null) {
            Map<Date, Rate> rateMap = new HashMap<Date, Rate>();
            for (IRATECHANGE rateChange : mIPITEM.getITAXITEM().getIRATECHANGE()) {
                Double rate = getRateMultiplier() * QBOFX.mapOFXStringToDouble(rateChange.getIRATE());
                QbdtNumericType rateType = Rate.getRateType(rateChange.getIRATE());
                //if no rate type is found, it is assumed to be a percentage
                // if rate type is percentage then divide by 100 else leave as it is
                if (rateType == null || rateType.equals(QbdtNumericType.Percentage)) {
                    rate = rate / 100;
                }
                rateMap.put(QBOFX.mapOFXStringToDate(rateChange.getIDTSUNSET()), new Rate(rate,rateType));
            }
            return rateMap;
        }

        return new HashMap<Date, Rate>();
    }

    public List<String> getTaxableToPayrollItemIds() {
        switch (mItemType) {
            case Addition:
                return mIPITEM.getIADDITEM().getITAXAFFECTED() != null ? mIPITEM.getIADDITEM().getITAXAFFECTED() : new ArrayList<String>();
            case Deduction:
                return mIPITEM.getIDEDUCTITEM().getITAXAFFECTED() != null ? mIPITEM.getIDEDUCTITEM().getITAXAFFECTED() : new ArrayList<String>();
            case EmployerContribution:
                return mIPITEM.getICONTRIBITEM().getITAXAFFECTED() != null ? mIPITEM.getICONTRIBITEM().getITAXAFFECTED() : new ArrayList<String>();
            default:
                return new ArrayList<String>();
        }
    }

    private PayrollItemCode mapPayrollItemCode() {
        return QBOFX.mapPayrollItemCode(mItemType, getTaxableToPayrollItemIds().size());
    }
}
