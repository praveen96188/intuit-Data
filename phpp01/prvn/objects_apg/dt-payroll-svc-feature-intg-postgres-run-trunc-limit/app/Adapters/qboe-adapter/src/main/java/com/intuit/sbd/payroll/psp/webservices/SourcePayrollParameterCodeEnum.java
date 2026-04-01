package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.domain.SourcePayrollParameterCode;

/**
 * @author Ken Paul
 */
public enum SourcePayrollParameterCodeEnum {
    /**
     * Batch Entry Description for Payroll
     */
    PYRLENTRDESC {
        public SourcePayrollParameterCode toPspCode() {
            return SourcePayrollParameterCode.PayrollEntryDescription;
        }
    },

    /**
     * Batch Entry Description for Book Transfers
     */
    BOOKTRSFRENTRDESC {
        public SourcePayrollParameterCode toPspCode() {
            return SourcePayrollParameterCode.BookTransferEntryDescription;
        }
    },

    /**
     * Batch Entry Description for Reversals
     */
    REVENTRDESC {
        public SourcePayrollParameterCode toPspCode() {
            return SourcePayrollParameterCode.ReversalEntryDescription;
        }
    },

    DEFAULTFUNDINGMODEL {
        public SourcePayrollParameterCode toPspCode() {
            return SourcePayrollParameterCode.DefaultFundingModel;
        }
    },

    ALLOWMULTIPLEFDMODELS {
        public SourcePayrollParameterCode toPspCode() {
            return SourcePayrollParameterCode.AllowMultipleFundingModels;
        }
    },

    MAXWAREHOUSETXDAYS{
        public SourcePayrollParameterCode toPspCode() {
            return SourcePayrollParameterCode.MaxWarehouseTransactionDays;
        }
    },
    MAXFAILEDLOGINATTEMPTS{
        public SourcePayrollParameterCode toPspCode() {
            return SourcePayrollParameterCode.MaxNumberOfFailedLoginAttempts;
        }
    },
    LOCKACCOUNTDURATION{
        public SourcePayrollParameterCode toPspCode() {
            return SourcePayrollParameterCode.LockAccountDuration;
        }
    },
    MINPAYROLLRUNDAYS{
        public SourcePayrollParameterCode toPspCode() {
            return SourcePayrollParameterCode.MinimumEarliestPayrollRunDays;
        }
    },
    DEACTIVATEBANKACCOUNTONRETURNEDVERFICIATIONDEBIT{
        public SourcePayrollParameterCode toPspCode() {
            return SourcePayrollParameterCode.DeactiveBankAccountOnReturnedVerificationDebit;
        }
    },
    ALLOWDUPLICATEPAYCHECKIDSIFSTATUSISCANCELLED{
        public SourcePayrollParameterCode toPspCode() {
            return SourcePayrollParameterCode.AllowDuplicatePaycheckIdsIfStatusIsCancelled;
        }
    },
    SHOULDADDCOMPANYTOPSP{
        public SourcePayrollParameterCode toPspCode() {
            return SourcePayrollParameterCode.ShouldAddCompanyToPSP;
        }
    },
    ALLOWBACKDATEDPAYROLLS{
        public SourcePayrollParameterCode toPspCode() {
            return SourcePayrollParameterCode.AllowBackdatedPayrolls;
        }
    },
    ALLOWONEOFFUNTIMELYPAYROLLS{
        public SourcePayrollParameterCode toPspCode() {
            return SourcePayrollParameterCode.AllowOneOffUntimelyPayrolls;
        }
    },
    ALLOWREVERIFYBANKACCOUNT{
        public SourcePayrollParameterCode toPspCode() {
            return SourcePayrollParameterCode.AllowReverifyBankAccount;
        }
    };

    // Force enum elements to provide an implementation...
    public abstract SourcePayrollParameterCode toPspCode();

    /**
     * Translate the Source Payroll Parameter Code from the DTO into the associated code for QBOE
     * @param pDTOParamCode The parameter code from the DTO
     * @return The enumeration value for the QBOE code or null if there is no translation.
     */
    static public String toQBOECode(SourcePayrollParameterCode pDTOParamCode) {
        return pDTOParamCode.toString();
    }
}
