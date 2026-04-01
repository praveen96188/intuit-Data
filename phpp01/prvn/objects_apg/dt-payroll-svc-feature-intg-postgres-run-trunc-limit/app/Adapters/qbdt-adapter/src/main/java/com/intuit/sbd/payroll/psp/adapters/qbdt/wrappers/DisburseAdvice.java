package com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers;

import com.intuit.sbd.payroll.psp.common.ofx.request.IDISBURSEADVICE;
import com.intuit.sbd.payroll.psp.common.ofx.request.ITAXLIAB;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 15, 2010
 * Time: 3:03:33 PM
 */
public class DisburseAdvice {
    private Collection<TaxLiability> mTaxLiabilities;
    private SpcfCalendar mCheckDate;
    private IDISBURSEADVICE mDisburseAdvice;

    public DisburseAdvice(IDISBURSEADVICE pDisburseAdvice, String checkDateString) {
        mDisburseAdvice = pDisburseAdvice;

        SpcfCalendar checkDate = SpcfCalendar.parse("yyyyMMdd", checkDateString);
        mCheckDate = SpcfCalendar.createInstance(checkDate.getYear(), checkDate.getMonth(), checkDate.getDay(),
                                                                   SpcfTimeZone.getLocalTimeZone());
    }

    public IDISBURSEADVICE getDisburseAdvice() {
        getTaxLiabilities();
        return mDisburseAdvice;
    }

    public SpcfMoney getTaxLiabilityAmount() {
        return QBOFX.mapOFXStringToMoney(mDisburseAdvice.getITAXLIABAMT());
    }

    public int getTaxQuarter() {
        return QBOFX.mapOFXStringToInt(mDisburseAdvice.getITAXQTR());
    }

    public SpcfCalendar getCheckDate() {
        return mCheckDate;
    }

    public void setCheckDate(SpcfCalendar pCheckDate) {
        mCheckDate = pCheckDate;
    }

    public Collection<TaxLiability> getTaxLiabilities() {
        if(mTaxLiabilities == null) {
            Map<String, TaxLiability> taxLiabilityMap = new HashMap<String, TaxLiability>();
            Map<String, TaxLiability> tipsLiabilityMap = new HashMap<String, TaxLiability>();

            TaxLiability taxLiability;
            boolean isFedTax;
            boolean isTipsLiability;

            // the tax liability items repeat for every paycheck using a map skips the duplicates
            // there is special logic for tips items (SS_ER_TIPS, SS_EE_TIPS, MED_ER_TIPS, MED_EE_TIPS)
            for (ITAXLIAB itaxliab : mDisburseAdvice.getITAXLIAB()) {
                isFedTax = itaxliab.getIFEDTAX() != null;
                String liabilityKey = getKey(itaxliab);

                isTipsLiability = isFedTax && itaxliab.getIFEDTAX().contains("_TIPS");

                TaxLiability existingTaxLiability = taxLiabilityMap.get(liabilityKey);
                if(existingTaxLiability == null || (isTipsLiability && existingTaxLiability.getTipsLiability() == null)) {
                    taxLiability = new TaxLiability(itaxliab.getIPITEMID(),
                                                    QBOFX.mapOFXStringToMoney(itaxliab.getICURAMT()),
                                                    QBOFX.mapOFXStringToMoney(itaxliab.getIQTRAMT()),
                                                    QBOFX.mapOFXStringToMoney(itaxliab.getIYTDAMT()),
                                                    QBOFX.mapOFXStringToMoney(itaxliab.getICURWB()),
                                                    QBOFX.mapOFXStringToMoney(itaxliab.getIQTRWB()),
                                                    QBOFX.mapOFXStringToMoney(itaxliab.getIYTDWB()),
                                                    itaxliab.getIFEDTAX(),
                                                    itaxliab.getISTATETAXDESC() == null ? null : itaxliab.getISTATETAXDESC().getISTATE(),
                                                    itaxliab.getISTATETAXDESC() == null ? null : itaxliab.getISTATETAXDESC().getISTATETAX(),
                                                    itaxliab.getIOTHERTAX(),
                                                    itaxliab);


                    if(isTipsLiability && existingTaxLiability != null) {
                        existingTaxLiability.setTipsLiability(taxLiability);
                    } else if(isTipsLiability) {
                        // the compliment liability has not been found yet, store this one to match up later
                        tipsLiabilityMap.put(liabilityKey, taxLiability);
                    } else if(isFedTax && !isTipsLiability) {
                        // get tips liability
                        taxLiability.setTipsLiability(tipsLiabilityMap.get(liabilityKey));
                        taxLiabilityMap.put(liabilityKey, taxLiability);
                    } else {
                        taxLiabilityMap.put(liabilityKey, taxLiability);
                    }
                }
            }
            mTaxLiabilities = taxLiabilityMap.values();
            mDisburseAdvice.getITAXLIAB().clear();
            for (TaxLiability liability : mTaxLiabilities) {
                mDisburseAdvice.getITAXLIAB().add(liability.getITAXLIAB());
                if(liability.getTipsLiability() != null) {
                    mDisburseAdvice.getITAXLIAB().add(liability.getTipsLiability().getITAXLIAB());
                }
            }
        }

        return mTaxLiabilities;
    }

    private String getKey(ITAXLIAB iTaxLiab) {

        return iTaxLiab.getITAXPMTTYPE() + iTaxLiab.getIPITEMID();
    }
    
    public class TaxLiability {
        private String mPayrollItemId;
        private SpcfMoney mCurrentAmount;
        private SpcfMoney mQuarterAmount;
        private SpcfMoney mYTDAmount;
        private SpcfMoney mCurrentTaxableAmount;
        private SpcfMoney mQuarterTaxableAmount;
        private SpcfMoney mYTDTaxableAmount;
        private String mFedTaxDesc;
        private String mState;
        private String mStateTaxDesc;
        private String mOtherTaxDesc;
        private TaxLiability mTipsLiability;
        private ITAXLIAB mITAXLIAB;

        public TaxLiability(String pPayrollItemId, SpcfMoney pCurrentAmount, SpcfMoney pQuarterAmount, SpcfMoney pYTDAmount, SpcfMoney pCurrentTaxableAmount, SpcfMoney pQuarterTaxableAmount, SpcfMoney pYTDTaxableAmount, String pFedTaxDesc, String pState, String pStateTaxDesc, String pOtherTaxDesc, ITAXLIAB pITAXLIAB) {
            mPayrollItemId = pPayrollItemId;
            mCurrentAmount = pCurrentAmount;
            mQuarterAmount = pQuarterAmount;
            mYTDAmount = pYTDAmount;
            mCurrentTaxableAmount = pCurrentTaxableAmount;
            mQuarterTaxableAmount = pQuarterTaxableAmount;
            mYTDTaxableAmount = pYTDTaxableAmount;
            mFedTaxDesc = pFedTaxDesc;
            mState = pState;
            mStateTaxDesc = pStateTaxDesc;
            mOtherTaxDesc = pOtherTaxDesc;
            mITAXLIAB = pITAXLIAB;
        }

        public String getPayrollItemId() {
            return mPayrollItemId;
        }

        public SpcfMoney getCurrentAmount() {
            return mCurrentAmount;
        }

        public SpcfMoney getQuarterAmount() {
            return mQuarterAmount;
        }

        public SpcfMoney getYTDAmount() {
            return mYTDAmount;
        }

        public SpcfMoney getCurrentTaxableAmount() {
            return mCurrentTaxableAmount;
        }

        public SpcfMoney getQuarterTaxableAmount() {
            return mQuarterTaxableAmount;
        }

        public SpcfMoney getYTDTaxableAmount() {
            return mYTDTaxableAmount;
        }

        public TaxLiability getTipsLiability() {
            return mTipsLiability;
        }

        public void setTipsLiability(TaxLiability pTipsLiability) {
            mTipsLiability = pTipsLiability;
        }

        public ITAXLIAB getITAXLIAB() {
            return mITAXLIAB;
        }
        
        public String getOtherTaxDesc() {
            return mOtherTaxDesc;
        }

        public String getFedTaxDesc() {
            return mFedTaxDesc;
        }

        public String getState() {
            return mState;
        }

        public String getStateTaxDesc() {
            return mStateTaxDesc;
        }

    }
}
