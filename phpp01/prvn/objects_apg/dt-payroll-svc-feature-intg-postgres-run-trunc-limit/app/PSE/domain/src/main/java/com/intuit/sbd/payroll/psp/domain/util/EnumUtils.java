package com.intuit.sbd.payroll.psp.domain.util;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;

import java.util.HashMap;
import java.lang.reflect.Method;

/**
 * @author Wiktor Kozlik
 */
public class EnumUtils {

    private static SpcfLogger logger = SpcfLogManager.getLogger(EnumUtils.class);

    private static HashMap<Enum, String> enumToReadableName = new HashMap<Enum, String>();

    static {
        // bank Account Status
        enumToReadableName.put(BankAccountStatus.PendingVerification, "Pending Verification");

        // bank Account Type
        enumToReadableName.put(BankAccountType.Checking, "Checking");
        enumToReadableName.put(BankAccountType.Savings, "Savings");

        // Cancellation Reason Codes
        enumToReadableName.put(CancellationReasonCode.CompanyOnHold, "Company on Hold");
        enumToReadableName.put(CancellationReasonCode.CompanyPendingTermination, "Company Pending Termination");

        // Cancellation Scope Codes
        enumToReadableName.put(CancellationScopeCode.EntirePayroll, "Entire Payroll");
        enumToReadableName.put(CancellationScopeCode.OnlyPaychecks, "Only Paychecks");

        // Event Limit Code
        enumToReadableName.put(EventLimitCode.BankAccount, "Bank Account");

        // Strike Reasons
        enumToReadableName.put(StrikeReason.DebitReturned, "Debit Returned");
        enumToReadableName.put(StrikeReason.DebitReturnedCanceled, "Debit Returned - Canceled");
        enumToReadableName.put(StrikeReason.Manual, "Manual Strike");
        enumToReadableName.put(StrikeReason.NSFAutoRedebit, "NSF Auto Redebit");
        enumToReadableName.put(StrikeReason.NSFPayrollCancelled, "NSF Payroll Cancelled");
        enumToReadableName.put(StrikeReason.SecondNSF, "2nd NSF");

        // Refund Status Reason Type
        enumToReadableName.put(RefundStatusReasonType.BankAccountInactive, "Bank Account Inactive");
        enumToReadableName.put(RefundStatusReasonType.CompanyCancelled, "Company Cancelled");
        enumToReadableName.put(RefundStatusReasonType.CompanyOnHold, "Company on Hold");
        enumToReadableName.put(RefundStatusReasonType.CompanyPendingTermination, "Company Pending Termination");
        enumToReadableName.put(RefundStatusReasonType.CompanyTerminated, "Company Terminated");

        // Refund Status Type
        enumToReadableName.put(RefundStatusType.NotIssued, "Not Issued");

        // Payroll SubType Codes
        enumToReadableName.put(PayrollSubtypeCode.BasicLimited, "QB Payroll Basic Limited");
        enumToReadableName.put(PayrollSubtypeCode.BasicUnlimited, "QB Payroll Basic Unlimited");
        enumToReadableName.put(PayrollSubtypeCode.Enhanced, "QB Payroll Enhanced");
        enumToReadableName.put(PayrollSubtypeCode.EnhancedAccountant, "QB Payroll Enhanced Accountant");
        enumToReadableName.put(PayrollSubtypeCode.EnhancedUnlimited, "QB Payroll Enhanced Unlimited");
        enumToReadableName.put(PayrollSubtypeCode.NewBasicUnlimited, "QB Payroll New Basic Unlimited");
        enumToReadableName.put(PayrollSubtypeCode.Standard, "QB Payroll Standard");
        enumToReadableName.put(PayrollSubtypeCode.Basic0to3Emp, "QB Payroll Basic 0-3 Emp");
        enumToReadableName.put(PayrollSubtypeCode.Enhanced0to3Emp, "QB Payroll Enhanced 0-3 Emp");
        enumToReadableName.put(PayrollSubtypeCode.PAPEnhAcct, "QB Payroll PAP Enh Acct");
        enumToReadableName.put(PayrollSubtypeCode.Assisted, "QB Payroll Assisted");
        enumToReadableName.put(PayrollSubtypeCode.AssistedAdv, "QB Payroll Assisted Adv");
        enumToReadableName.put(PayrollSubtypeCode.FreeBasic1, "QB Payroll Free Basic 1");
        enumToReadableName.put(PayrollSubtypeCode.MonthlyBasic0to3Emp, "Monthly Basic 0-3");
        enumToReadableName.put(PayrollSubtypeCode.MonthlyBasicUnlimited, "Monthly Basic Unlimited");
        enumToReadableName.put(PayrollSubtypeCode.MonthlyEnhanced0to3Emp, "Monthly Enhanced 0-3");
        enumToReadableName.put(PayrollSubtypeCode.MonthlyEnhancedUnlimited, "Monthly Enhanced Unlimited");

        // ACH Return Types
        enumToReadableName.put(ACHReturnType.CBAVerificationReturn, "CBA Verification Return");
        enumToReadableName.put(ACHReturnType.DDDebitReturn, "DD Debit Return");
        enumToReadableName.put(ACHReturnType.DDReject, "DD Reject");
        enumToReadableName.put(ACHReturnType.ERRefundReturn, "ER Refund Return");
        enumToReadableName.put(ACHReturnType.FeeReturn, "Fee Return");
        enumToReadableName.put(ACHReturnType.SalesTaxReturn, "Sales Tax Return");
        enumToReadableName.put(ACHReturnType.ReversalReturn, "Reversal Return");

        // Verification Status Types
        enumToReadableName.put(VerificationStatusType.AutoRedebit, "Auto Redebit");
        enumToReadableName.put(VerificationStatusType.CBADeactivated, "CBA Deactivated");
        enumToReadableName.put(VerificationStatusType.PendingVerification, "Pending Verification");

        // NSF Subtypes
        enumToReadableName.put(NSFSubTypeType.NSFAutoRedebit, "NSF Auto Redebit");
        enumToReadableName.put(NSFSubTypeType.NSFPayrollCancelled, "NSF Payroll Canceled");
        enumToReadableName.put(NSFSubTypeType.SecondNSF, "2nd NSF");

        // Payroll Status
        enumToReadableName.put(PayrollStatus.DebitReturned, "Debit Returned");
        enumToReadableName.put(PayrollStatus.DebitReturnedCanceled, "Debit Returned - Canceled");

    }

    public static String getReadableName(Enum pEnumValue) {
        if (pEnumValue == null) {
            return null;
        }

        Class dataObjectClass = Enumerations.enumToDataObjectMap.get(pEnumValue.getClass());
        if (dataObjectClass != null) {
            Object dataObj = Application.findById(dataObjectClass, pEnumValue);
            try {
                Method getNameMethod = dataObjectClass.getMethod("getName");
                Object result = getNameMethod.invoke(dataObj);
                return result.toString();
            }
            catch (Exception ex) {
                logger.debug(ex.getMessage(), ex);
            }
        }

        String readableName = enumToReadableName.get(pEnumValue);
        if (readableName != null) {
            return readableName;
        }

        return pEnumValue.toString();
    }

    public static String splitEnumName(Enum pEnumValue) {
        String stringToSplit = pEnumValue.toString();
        String[] str = stringToSplit.split("([A-Z])");
        String newString = "";
        for (int i = 0; i < str.length; i++) {
            newString = newString + str[i];
        }
        return newString;
    }

    public static <E extends Enum> E getEnumForReadableName(Class<E> pEnumClass, String pEnumReadableName) {
        for (E enumVal : pEnumClass.getEnumConstants()) {
            if (getReadableName(enumVal).equals(pEnumReadableName)) {
                return enumVal;
            }
        }
        return null;
    }
}
