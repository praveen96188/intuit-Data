/*
 * $Id: //psp/dev/PSE/BatchJobs/src/com/intuit/sbd/payroll/psp/batchjobs/offload/nachaobjects/NACHATotals.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.batchjobs.offload.nachaobjects;

import com.intuit.spc.foundations.primary.SpcfMoney;

public class NACHATotals {
        private SpcfMoney totalDebitAmount;
        private SpcfMoney totalCreditAmount;
        private Long rdfiSum;

        public NACHATotals() {
            totalDebitAmount = new SpcfMoney("0");
            totalCreditAmount = new SpcfMoney("0");
            rdfiSum = 0L;
        }

        public SpcfMoney getTotalDebitAmount() {
            return totalDebitAmount;
        }

        public SpcfMoney getTotalCreditAmount() {
            return totalCreditAmount;
        }

        public void addToTotalCredit(SpcfMoney pAmount) {
            totalCreditAmount = (SpcfMoney) totalCreditAmount.add(pAmount);
        }

        public void addToTotalDebit(SpcfMoney pAmount) {
            totalDebitAmount = (SpcfMoney) totalDebitAmount.add(pAmount);
        }

        public void updateHash(String pRDFIString) {
            rdfiSum += Integer.parseInt(pRDFIString);
        }

        public long getEntryHash() {
            // the Entry Hash is the sum of the receiving DFI identification fields in Entry Detail Records in the
            // batch.  This field contains the 8 digit routing number of the receiving depository institution.
            // The hash is the  arithmetic sum of the 8 digit routing numbers, with overflow out of the high order
            // (leftmost) position ignored.
            long retValue = rdfiSum;
            if (retValue > 9999999999L) {
                retValue = retValue % 10000000000L;
            }
            return retValue;
        }
    }