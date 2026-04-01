package com.intuit.sbd.payroll.psp.batchjobs.printedchecks.dtos;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 27, 2011
 * Time: 5:19:56 PM
 */
public class LineItemDTO {
    private int mLiabilityYear;
        private int mLiabilityQuarter;
        private String mType;
        private BigDecimal mAmount;
        
        public int getLiabilityYear() {
            return mLiabilityYear;
        }

        public void setLiabilityYear(int pLiabilityYear) {
            mLiabilityYear = pLiabilityYear;
        }

        public int getLiabilityQuarter() {
            return mLiabilityQuarter;
        }

        public void setLiabilityQuarter(int pLiabilityQuarter) {
            mLiabilityQuarter = pLiabilityQuarter;
        }

        public String getType() {
            return mType;
        }

        public void setType(String pType) {
            mType = pType;
        }

        public BigDecimal getAmount() {
            return mAmount;
        }

        public void setAmount(BigDecimal pAmount) {
            mAmount = pAmount;
        }
}
