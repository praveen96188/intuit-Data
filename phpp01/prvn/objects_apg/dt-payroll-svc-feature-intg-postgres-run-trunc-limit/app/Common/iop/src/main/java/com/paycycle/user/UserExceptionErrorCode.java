package com.paycycle.user;

public enum UserExceptionErrorCode {
    NoCode,                                                            // The UserException has not error code.
    AuthMgr_UserLockedOut,                                            // AuthMgr user is locked out.
    AuthMgr_FailedAttempt,                                            // AuthMgr failed attempt.
    TimeClock_InvalidPin_TryAgain,                                    // TimeClock invalid pin - try again.
    TimeClock_InvalidPin_InformEmployer,                            // TimeClock invalid pin - inform the employer.
    TimeClock_InvalidPin_ExceededRetryLimit,                         // TimeClock invalid pin - exceeded retry limit.
    TaxSetup_CannotChangeFilingNameDueToPendingTaxPayments_Error,    // Cannot change the filing name when tax payments are pending.
    TaxSetup_eServicesWillBeDisabled_Warning,                        // EServices will be disabled - call again with override.
    TaxSetup_CannotChangeEinDueToEservicesEnabled_Error,            // Cannot change EIN because EServices is enabled.
    TaxSetup_CannotChangeEinDueToPendingTaxPayments_Error,            // Cannot change EIN when tax payments are pending.
    TaxSetup_InvalidEinFormat_Error,                                // Cannot change EIN - invalid format.
    AuthMgr_Multiple_Users_Login,                                    // Two different users try to log into same Payroll company which we currently don't allow
    AuthMgr_Multiple_session_login,                                    // Two different login sessions for one user
    SetupWebService_WorkLocation_MustBeUSState,                        // Work Location must be a US State or District of Columbia
    ;
}
