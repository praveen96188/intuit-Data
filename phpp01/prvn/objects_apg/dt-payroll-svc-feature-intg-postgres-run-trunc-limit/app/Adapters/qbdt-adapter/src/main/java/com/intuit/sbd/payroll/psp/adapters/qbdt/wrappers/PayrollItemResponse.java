package com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers;

import com.intuit.sbd.payroll.psp.common.XMLToOFX;
import com.intuit.sbd.payroll.psp.common.ofx.response.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;

import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 13, 2011
 * Time: 1:56:49 PM
 */
public class PayrollItemResponse {
    private static final Pattern DIGIT_PATTERN = Pattern.compile ("^\\d.*");
    private IPITEM mIPITEM;
    private QbdtPayrollItemInfo ratePushItem;

    public PayrollItemResponse(QbdtPayrollItemInfo pQbdtPayrollItemInfo, CompanyLaw pCompanyLaw) {
        mIPITEM = new IPITEM();
        mIPITEM.setIINACTIVE(QBOFX.Y_N(pCompanyLaw.getStatus() == PayrollItemStatus.Inactive));
        mIPITEM.setIPITEMID(pCompanyLaw.getSourceId());

        // qb cannot handle &amp; for tax items. This is a hack to get the XML to OFX parser to not escape it
        String payrollItemName = pCompanyLaw.getSourceDescription();
        if(payrollItemName != null) {
            payrollItemName = payrollItemName.replace("&", XMLToOFX.TAX_ITEM_NAME_AMPERSAND);
        }

        mIPITEM.setIPITEMNAME(payrollItemName);
        mIPITEM.setITAXITEM(buildITAXITEM(pQbdtPayrollItemInfo, pCompanyLaw));
    }

    public PayrollItemResponse(QbdtPayrollItemInfo pQbdtPayrollItemInfo, CompanyPayrollItem pCompanyPayrollItem) {
        mIPITEM = new IPITEM();
        mIPITEM.setIINACTIVE(QBOFX.Y_N(pCompanyPayrollItem.getStatus() == PayrollItemStatus.Inactive));
        mIPITEM.setIPITEMID(pCompanyPayrollItem.getSourcePayrollItemId());
        mIPITEM.setIPITEMNAME(pCompanyPayrollItem.getSourceDescription());

        QBOFX.OFXPayrollItemType ofxPayrollItemType = QBOFX.mapOFXPayrollItemType(pCompanyPayrollItem.getPayrollItem().getPayrollItemCode());
        switch (ofxPayrollItemType) {
            case Addition:
                mIPITEM.setIADDITEM(buildIADDITEM(pQbdtPayrollItemInfo, pCompanyPayrollItem));
                break;
            case Bonus:
                mIPITEM.setIBONUSITEM(buildIBONUSITEM(pQbdtPayrollItemInfo));
                break;
            case Commission:
                mIPITEM.setICOMMITEM(buildICOMMITEM(pQbdtPayrollItemInfo, pCompanyPayrollItem));
                break;
            case Deduction:
                mIPITEM.setIDEDUCTITEM(buildIDEDUCTITEM(pQbdtPayrollItemInfo, pCompanyPayrollItem));
                break;
            case DirectDeposit:
                mIPITEM.setIDDITEM(buildIDDITEM(pQbdtPayrollItemInfo));
                break;
            case EmployerContribution:
                mIPITEM.setICONTRIBITEM(buildICONTRIBITEM(pQbdtPayrollItemInfo, pCompanyPayrollItem));
                break;
            case Hourly:
                mIPITEM.setIHRLYITEM(buildIHRLYITEM(pQbdtPayrollItemInfo));
                break;
            case Salary:
                mIPITEM.setISALARYITEM(buildISALARYITEM(pQbdtPayrollItemInfo));
                break;
        }
    }

    public IPITEM getIPITEM() {
        return mIPITEM;
    }

    public QbdtPayrollItemInfo getRatePushItem() {
        return ratePushItem;
    }

    private ITAXITEM buildITAXITEM(QbdtPayrollItemInfo pQbdtPayrollItemInfo, CompanyLaw pCompanyLaw) {
        ITAXITEM itaxitem = new ITAXITEM();
        itaxitem.setITAXFORMLINE(QBOFX.getTaxFormLineFromOFXValue(pCompanyLaw.getTaxFormLine()));

        if(pQbdtPayrollItemInfo != null) {
            itaxitem.setICOMPID(QBOFX.nullStringCheck(pQbdtPayrollItemInfo.getAgencyId()));
            mIPITEM.setIISEMP(QBOFX.Y_N(pQbdtPayrollItemInfo.getIsEmployeePaid()));
            if(pQbdtPayrollItemInfo.getSpecialType() != null) {
                mIPITEM.setISPECIALTYPE(QBOFX.getOFXSpecialType(pQbdtPayrollItemInfo.getSpecialType()));
            }
            itaxitem.setIADJGROSS(QBOFX.Y_N(pQbdtPayrollItemInfo.getAdjustsGross()));
            itaxitem.setIBASEDONQTY(QBOFX.Y_N(pQbdtPayrollItemInfo.getBasedOnQuantity()));
            itaxitem.setIDEFLIMIT(QBOFX.convertSpcfMoneyToOFXString(pQbdtPayrollItemInfo.getDefaultLimit()));
            itaxitem.setIEXPACCT(pQbdtPayrollItemInfo.getExpenseAccount());
            itaxitem.setILIABACCT(QBOFX.convertNullToOFXString(pQbdtPayrollItemInfo.getLiabilityAccount()));
            itaxitem.setILIABAGENCY(QBOFX.convertNullToOFXString(pQbdtPayrollItemInfo.getLiabilityAgency()));
            itaxitem.setIONSERVICE(QBOFX.Y_N(pQbdtPayrollItemInfo.getOnService()));

            if(pQbdtPayrollItemInfo.getToken() >= pQbdtPayrollItemInfo.getRatePushToken() && pQbdtPayrollItemInfo.getRatePushToken() > -1) {
                itaxitem.setIRATEPUSH(QBOFX.Y_N(true));
                ratePushItem = pQbdtPayrollItemInfo;
            }
        }

        String sourceLawCode = SourceSystemLawAssoc.findSourceIdBySourceSystemAndLaw(SourceSystemCode.QBDT, pCompanyLaw.getLaw());
        if(sourceLawCode != null) {
            if(sourceLawCode.contains(" ")) {
                String[] stateCode = sourceLawCode.split(" ");
                ISTATETAXDESC istatetaxdesc = new ISTATETAXDESC();
                istatetaxdesc.setISTATE(stateCode[0]);
                istatetaxdesc.setISTATETAX(stateCode[1]);
                itaxitem.setISTATETAXDESC(istatetaxdesc);
            } else if(DIGIT_PATTERN.matcher(sourceLawCode).matches()) {
                itaxitem.setIOTHERTAX(sourceLawCode);
            } else  {
                itaxitem.setIFEDTAX(sourceLawCode);
            }
        }

        return itaxitem;
    }

    private IADDITEM buildIADDITEM(QbdtPayrollItemInfo pQbdtPayrollItemInfo, CompanyPayrollItem pCompanyPayrollItem) {
        IADDITEM iadditem = new IADDITEM();
        iadditem.setITAXFORMLINE(QBOFX.getTaxFormLineFromOFXValue(pCompanyPayrollItem.getTaxFormLine()));

        if(pQbdtPayrollItemInfo != null) {
            mIPITEM.setIISEMP(QBOFX.Y_N(pQbdtPayrollItemInfo.getIsEmployeePaid()));
            if(pQbdtPayrollItemInfo.getSpecialType() != null) {
                mIPITEM.setISPECIALTYPE(QBOFX.getOFXSpecialType(pQbdtPayrollItemInfo.getSpecialType()));
            }
            iadditem.setIADJGROSS(QBOFX.Y_N(pQbdtPayrollItemInfo.getAdjustsGross()));
            iadditem.setIBASEDONQTY(QBOFX.Y_N(pQbdtPayrollItemInfo.getBasedOnQuantity()));
            iadditem.setICOMPID(QBOFX.nullStringCheck(pQbdtPayrollItemInfo.getAgencyId()));
            iadditem.setIDEFRATE(QBOFX.mapNumericTypeToString(pQbdtPayrollItemInfo.getDefaultRateType(), pQbdtPayrollItemInfo.getDefaultRate()));
            iadditem.setIDEFLIMIT(QBOFX.convertSpcfMoneyToOFXString(pQbdtPayrollItemInfo.getDefaultLimit()));
            iadditem.setIEXPACCT(pQbdtPayrollItemInfo.getExpenseAccount());
            iadditem.setIEXPBYJOB(QBOFX.Y_N(pQbdtPayrollItemInfo.getExpenseByJob()));
            iadditem.setILIABACCT(QBOFX.convertNullToOFXString(pQbdtPayrollItemInfo.getLiabilityAccount()));
            iadditem.setILIABAGENCY(QBOFX.convertNullToOFXString(pQbdtPayrollItemInfo.getLiabilityAgency()));
        }

        return iadditem;
    }

    private IBONUSITEM buildIBONUSITEM(QbdtPayrollItemInfo pQbdtPayrollItemInfo) {
        IBONUSITEM ibonusitem = new IBONUSITEM();
        if(pQbdtPayrollItemInfo != null) {
            mIPITEM.setIISEMP(QBOFX.Y_N(pQbdtPayrollItemInfo.getIsEmployeePaid()));
            if(pQbdtPayrollItemInfo.getSpecialType() != null) {
                mIPITEM.setISPECIALTYPE(QBOFX.getOFXSpecialType(pQbdtPayrollItemInfo.getSpecialType()));
            }
            ibonusitem.setIEXPACCT(QBOFX.convertNullToOFXString(pQbdtPayrollItemInfo.getExpenseAccount()));
        }
        return ibonusitem;
    }

    private ICOMMITEM buildICOMMITEM(QbdtPayrollItemInfo pQbdtPayrollItemInfo, CompanyPayrollItem pCompanyPayrollItem) {
        ICOMMITEM icommitem = new ICOMMITEM();
        icommitem.setITAXFORMLINE(QBOFX.getTaxFormLineFromOFXValue(pCompanyPayrollItem.getTaxFormLine()));

        if(pQbdtPayrollItemInfo != null) {
            mIPITEM.setIISEMP(QBOFX.Y_N(pQbdtPayrollItemInfo.getIsEmployeePaid()));
            if(pQbdtPayrollItemInfo.getSpecialType() != null) {
                mIPITEM.setISPECIALTYPE(QBOFX.getOFXSpecialType(pQbdtPayrollItemInfo.getSpecialType()));
            }
            icommitem.setIADJGROSS(QBOFX.Y_N(pQbdtPayrollItemInfo.getAdjustsGross()));
            icommitem.setIBASEDONQTY(QBOFX.Y_N(pQbdtPayrollItemInfo.getBasedOnQuantity()));
            icommitem.setICOMPID(QBOFX.nullStringCheck(pQbdtPayrollItemInfo.getAgencyId()));
            icommitem.setIDEFRATE(QBOFX.mapNumericTypeToString(pQbdtPayrollItemInfo.getDefaultRateType(), pQbdtPayrollItemInfo.getDefaultRate()));
            icommitem.setIDEFLIMIT(QBOFX.convertSpcfMoneyToOFXString(pQbdtPayrollItemInfo.getDefaultLimit()));
            icommitem.setIEARNINGSTABLE(QBOFX.Y_N(pQbdtPayrollItemInfo.getEarningsTable()));
            icommitem.setIEXPACCT(pQbdtPayrollItemInfo.getExpenseAccount());
            icommitem.setIEXPBYJOB(QBOFX.Y_N(pQbdtPayrollItemInfo.getExpenseByJob()));
            icommitem.setILIABACCT(QBOFX.convertNullToOFXString(pQbdtPayrollItemInfo.getLiabilityAccount()));
            icommitem.setILIABAGENCY(QBOFX.convertNullToOFXString(pQbdtPayrollItemInfo.getLiabilityAgency()));
            icommitem.setIONSERVICE(QBOFX.Y_N(pQbdtPayrollItemInfo.getOnService()));
        }
        return icommitem;
    }

    private ICONTRIBITEM buildICONTRIBITEM(QbdtPayrollItemInfo pQbdtPayrollItemInfo, CompanyPayrollItem pCompanyPayrollItem) {
        ICONTRIBITEM icontribitem = new ICONTRIBITEM();
        icontribitem.setITAXFORMLINE(QBOFX.getTaxFormLineFromOFXValue(pCompanyPayrollItem.getTaxFormLine()));

        if(pQbdtPayrollItemInfo != null) {
            mIPITEM.setIISEMP(QBOFX.Y_N(pQbdtPayrollItemInfo.getIsEmployeePaid()));
            if(pQbdtPayrollItemInfo.getSpecialType() != null) {
                mIPITEM.setISPECIALTYPE(QBOFX.getOFXSpecialType(pQbdtPayrollItemInfo.getSpecialType()));
            }
            icontribitem.setIADJGROSS(QBOFX.Y_N(pQbdtPayrollItemInfo.getAdjustsGross()));
            icontribitem.setIBASEDONQTY(QBOFX.Y_N(pQbdtPayrollItemInfo.getBasedOnQuantity()));
            icontribitem.setICOMPID(QBOFX.nullStringCheck(pQbdtPayrollItemInfo.getAgencyId()));
            icontribitem.setIDEFRATE(QBOFX.mapNumericTypeToString(pQbdtPayrollItemInfo.getDefaultRateType(), pQbdtPayrollItemInfo.getDefaultRate()));
            icontribitem.setIDEFLIMIT(QBOFX.convertSpcfMoneyToOFXString(pQbdtPayrollItemInfo.getDefaultLimit()));
            icontribitem.setIEXPACCT(pQbdtPayrollItemInfo.getExpenseAccount());
            icontribitem.setIEXPBYJOB(QBOFX.Y_N(pQbdtPayrollItemInfo.getExpenseByJob()));
            icontribitem.setILIABACCT(QBOFX.convertNullToOFXString(pQbdtPayrollItemInfo.getLiabilityAccount()));
            icontribitem.setILIABAGENCY(QBOFX.convertNullToOFXString(pQbdtPayrollItemInfo.getLiabilityAgency()));
            icontribitem.setIONSERVICE(QBOFX.Y_N(pQbdtPayrollItemInfo.getOnService()));
        }

        return icontribitem;
    }

    private IDEDUCTITEM buildIDEDUCTITEM(QbdtPayrollItemInfo pQbdtPayrollItemInfo, CompanyPayrollItem pCompanyPayrollItem) {
        IDEDUCTITEM ideductitem = new IDEDUCTITEM();
        ideductitem.setITAXFORMLINE(QBOFX.getTaxFormLineFromOFXValue(QBOFX.convertNullToOFXString(pCompanyPayrollItem.getTaxFormLine())));

        if(pQbdtPayrollItemInfo != null) {
            mIPITEM.setIISEMP(QBOFX.Y_N(pQbdtPayrollItemInfo.getIsEmployeePaid()));
            if(pQbdtPayrollItemInfo.getSpecialType() != null) {
                mIPITEM.setISPECIALTYPE(QBOFX.getOFXSpecialType(pQbdtPayrollItemInfo.getSpecialType()));
            }
            ideductitem.setIADJGROSS(QBOFX.Y_N(pQbdtPayrollItemInfo.getAdjustsGross()));
            ideductitem.setIBASEDONQTY(QBOFX.Y_N(pQbdtPayrollItemInfo.getBasedOnQuantity()));
            ideductitem.setICOMPID(QBOFX.nullStringCheck(pQbdtPayrollItemInfo.getAgencyId()));
            ideductitem.setIDEFRATE(QBOFX.mapNumericTypeToString(pQbdtPayrollItemInfo.getDefaultRateType(), pQbdtPayrollItemInfo.getDefaultRate()));
            ideductitem.setIDEFLIMIT(QBOFX.convertSpcfMoneyToOFXString(pQbdtPayrollItemInfo.getDefaultLimit()));
            ideductitem.setILIABACCT(QBOFX.convertNullToOFXString(pQbdtPayrollItemInfo.getLiabilityAccount()));
            ideductitem.setILIABAGENCY(QBOFX.convertNullToOFXString(pQbdtPayrollItemInfo.getLiabilityAgency()));
            ideductitem.setIONSERVICE(QBOFX.Y_N(pQbdtPayrollItemInfo.getOnService()));
        }

        return ideductitem;
    }

    private IDDITEM buildIDDITEM(QbdtPayrollItemInfo pQbdtPayrollItemInfo) {
        IDDITEM idditem = new IDDITEM();
        if(pQbdtPayrollItemInfo != null) {
            mIPITEM.setIISEMP(QBOFX.Y_N(pQbdtPayrollItemInfo.getIsEmployeePaid()));
            if(pQbdtPayrollItemInfo.getSpecialType() != null) {
                mIPITEM.setISPECIALTYPE(QBOFX.getOFXSpecialType(pQbdtPayrollItemInfo.getSpecialType()));
            }
            idditem.setICOMPID(QBOFX.nullStringCheck(pQbdtPayrollItemInfo.getAgencyId()));
            idditem.setILIABACCT(QBOFX.convertNullToOFXString(pQbdtPayrollItemInfo.getLiabilityAccount()));
            idditem.setILIABAGENCY(QBOFX.convertNullToOFXString(pQbdtPayrollItemInfo.getLiabilityAgency()));
        }
        return idditem;
    }

    private IHRLYITEM buildIHRLYITEM(QbdtPayrollItemInfo pQbdtPayrollItemInfo) {
        IHRLYITEM ihrlyitem = new IHRLYITEM();
        if(pQbdtPayrollItemInfo != null) {
            mIPITEM.setIISEMP(QBOFX.Y_N(pQbdtPayrollItemInfo.getIsEmployeePaid()));
            if(pQbdtPayrollItemInfo.getSpecialType() != null) {
                mIPITEM.setISPECIALTYPE(QBOFX.getOFXSpecialType(pQbdtPayrollItemInfo.getSpecialType()));
            }
            ihrlyitem.setIEXPACCT(QBOFX.convertNullToOFXString(pQbdtPayrollItemInfo.getExpenseAccount()));
            ihrlyitem.setIPAYTYPE(QBOFX.convertNullToOFXString(pQbdtPayrollItemInfo.getPayType().toString()));
        }
        return ihrlyitem;
    }

    private ISALARYITEM buildISALARYITEM(QbdtPayrollItemInfo pQbdtPayrollItemInfo) {
        ISALARYITEM isalaryitem = new ISALARYITEM();
        if(pQbdtPayrollItemInfo != null) {
            mIPITEM.setIISEMP(QBOFX.Y_N(pQbdtPayrollItemInfo.getIsEmployeePaid()));
            if(pQbdtPayrollItemInfo.getSpecialType() != null) {
                mIPITEM.setISPECIALTYPE(QBOFX.getOFXSpecialType(pQbdtPayrollItemInfo.getSpecialType()));
            }
            isalaryitem.setIEXPACCT(pQbdtPayrollItemInfo.getExpenseAccount());
            isalaryitem.setIPAYTYPE(pQbdtPayrollItemInfo.getPayType().toString());
        }
        return isalaryitem;
    }

    public void addTaxableTo(String pSourceId) {
        if(mIPITEM.getIADDITEM() != null) {
            mIPITEM.getIADDITEM().getITAXAFFECTED().add(pSourceId);
        } else if(mIPITEM.getIDEDUCTITEM() != null) {
            mIPITEM.getIDEDUCTITEM().getITAXAFFECTED().add(pSourceId);
        } else if(mIPITEM.getICONTRIBITEM() != null) {
            mIPITEM.getICONTRIBITEM().getITAXAFFECTED().add(pSourceId);
        }
    }

    public void addRate(CompanyLawRate lawRate, SpcfCalendar expirationDate) {
        double rate = lawRate.getRate();
        QbdtNumericType rateType = lawRate.getRateType();
        ITAXITEM itaxitem = mIPITEM.getITAXITEM();
        if(itaxitem == null) {
            return;
        }

        SpcfDecimal rateMultiplier = null;
        //If its an employee than value is negative -1 else +1 * if its a percentage then value is 100 else 1 for fixed
        // values
        if (QBOFX.mapOFXStringToBoolean(mIPITEM.getIISEMP())) {
            rateMultiplier = SpcfDecimal.createInstance(-1);
        } else {
            rateMultiplier = SpcfDecimal.createInstance(1);
        }
        if (rateType == null || rateType.equals(QbdtNumericType.Percentage)) {
            rateMultiplier = rateMultiplier.multiply(SpcfDecimal.createInstance(100));
        }

        if(expirationDate == null) {
            String rateString = QBOFX.truncZeros(SpcfDecimal.createInstance(rate).multiply(rateMultiplier).setScale(6, SpcfDecimal.SpcfRoundingType.HalfUp).toString());
            /**
             * PSP-3156 - Because of a bug in QB, sending % and $ in a single OFX is causing failures/crashes. Unless
             * this issue is fixed we cannot send $ amounts back to QB, please note PSP will continue to store $ rates
             * and will send them out as percentage same as before PSP-3156. e.g.
             * Input Rate   Output Rate
             * 5%           5%
             * $5           5%
             *
             * Once the issue in QB is fixed simply uncomment the line below and comment the "temporary fix" line of
             * code
             */
            //rateString = (rateType == null || rateType.equals(QbdtNumericType.Percentage)) ? rateString + "%"  : "$" + rateString;
            //PSP-3156 - Temporary Fix
            rateString = rateString + "%";

            itaxitem.setIRATE(rateString);
        } else  {
            IRATECHANGE iratechange = new IRATECHANGE();
            String rateChangeString = QBOFX.truncZeros(SpcfDecimal.createInstance(rate).multiply(rateMultiplier).setScale(6, SpcfDecimal.SpcfRoundingType.HalfUp).toString());
            rateChangeString = (rateType == null || rateType.equals(QbdtNumericType.Percentage)) ? rateChangeString + "%" : "$" + rateChangeString;
            iratechange.setIRATE(rateChangeString);
            SpcfCalendar sunsetDate = expirationDate.copy();
            sunsetDate.addDays(1);
            iratechange.setIDTSUNSET(QBOFX.convertToOFXDate(sunsetDate));
            itaxitem.getIRATECHANGE().add(iratechange);
        }
    }
}
