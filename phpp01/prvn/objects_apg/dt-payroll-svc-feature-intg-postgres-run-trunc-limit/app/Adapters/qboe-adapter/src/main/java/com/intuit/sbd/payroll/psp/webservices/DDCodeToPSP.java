package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

import java.util.Collection;

/**
 * @author Allen Chaves
 * @author Wiktor Kozlik
 */
public class DDCodeToPSP {
    public static BankAccountType getBankAccountType(String pBankAccountTypeCd) {
        if (null == pBankAccountTypeCd) {
            return null;
        } else if ("C".equalsIgnoreCase(pBankAccountTypeCd)) {
            return BankAccountType.Checking;
        } else if ("S".equalsIgnoreCase(pBankAccountTypeCd)) {
            return BankAccountType.Savings;
        } else {
            return BankAccountType.valueOf(pBankAccountTypeCd);
        }
    }

    public static CommunicationType getDomainCommunicationType(String pXMLCommunicationTypeCode) {
        if ("PHONE".equalsIgnoreCase(pXMLCommunicationTypeCode)) {
            return CommunicationType.Phone;
        } else {
            return CommunicationType.Email;
        }
    }

    public static String getQBOEBankAccountStatus(BankAccountStatus pBankAccountStatus) {
        switch (pBankAccountStatus) {
            case Active:
                return "ACTV";
            case Inactive:
                return "INACTV";
            case PendingVerification:
                return "PNDVER";
            default:
                return null;
        }
    }

    public static BankAccountStatus getBankAccountStatus(String pBankAccountStatusCd) {
        if (null == pBankAccountStatusCd) {
            return null;
        } else if ("ACTV".equalsIgnoreCase(pBankAccountStatusCd)) {
            return BankAccountStatus.Active;
        } else if ("INACTV".equalsIgnoreCase(pBankAccountStatusCd)) {
            return BankAccountStatus.Inactive;
        } else if ("PNDVER".equalsIgnoreCase(pBankAccountStatusCd)) {
            return BankAccountStatus.PendingVerification;
        } else {
            return BankAccountStatus.valueOf(pBankAccountStatusCd);
        }
    }

    public static Gender getGender(String pGenderCd) {
        if ("F".equalsIgnoreCase(pGenderCd)) {
            return Gender.Female;
        } else if ("M".equalsIgnoreCase(pGenderCd)) {
            return Gender.Male;
        } else {
            return null;
        }
    }

    /**
     * Translates PSP service status to QBOE DD service status.
     *
     * @param pDDServiceSubStatusCd
     * @param pOnHoldReasonCodes
     * @return
     */
    public static String getQBOEServiceStatus(
            final ServiceSubStatusCode pDDServiceSubStatusCd,
            final Collection<ServiceSubStatusCode> pOnHoldReasonCodes) {

        if (null != pDDServiceSubStatusCd) {
            switch (pDDServiceSubStatusCd) {
                case Cancelled:
                    return "CNCLD";

                case Terminated:
                    return "TERMD";
            }
        }

        String qboeStatus = null;

        if (pOnHoldReasonCodes != null && pOnHoldReasonCodes.isEmpty() == false) {
            for (ServiceSubStatusCode onHoldReasonCd : pOnHoldReasonCodes) {
                switch (onHoldReasonCd) {
                    case DirectDepositLimit:
                    case SuspendedDirectDeposit:
                        // Suspended doesn't override any other status
                        if (qboeStatus == null) {
                            qboeStatus = "SSPND";
                        }
                        break;

                    case FraudReview:
                        // Pending Activation overrides Suspended
                        if (qboeStatus == null || "SSPND".equals(qboeStatus)) {
                            qboeStatus = "PNDACTVN";
                        }
                        break;

                    case AchRejectR1R9:
                    case AchRejectOther:
                    case RiskCollections:
                    case Fraud:
                    case AMLHold:
                    case IntuitCollections:
                        // On Hold doesn't override Pending Termination
                        if ("PNDTERMN".equals(qboeStatus) == false) {
                            qboeStatus = "HOLD";
                        }
                        break;

                    case PendingTermination:
                        // Pending Termination overrides all the other statuses
                        qboeStatus = "PNDTERMN";
                        break;

                    case PendingPrefundingWire:
                        qboeStatus = "ACTV";
                        break;

                    default:
                        throw new RuntimeException("This on hold service status is not allowed in QBOE DD: " + onHoldReasonCd);

                }
            }
        }
        if (qboeStatus != null) {
            return qboeStatus;
        }

        // Company is not on hold
        if (null != pDDServiceSubStatusCd) {
            switch (pDDServiceSubStatusCd) {
                case PendingBankVerification:
                case PendingFirstPayroll:
                case ActiveCurrent:
                    return "ACTV";

                default:
                    throw new RuntimeException("The service status is not allowed in QBOE DD: " + pDDServiceSubStatusCd);
            }
        }

        return null;
    }

    /**
     * Maps PSP status to QBOE status
     *
     * @param pCompanyService PSP Company Service
     * @return DD V0 Company or Service status
     *         <p/>
     */
    public static String getQBOEServiceStatus(final CompanyService pCompanyService) {
        return getQBOEServiceStatus(pCompanyService.getStatusCd(), pCompanyService.getCompany().getCurrentOnHoldReasonCodes());
    }

    /**
     * Maps PSP status to QBOE status
     *
     * @param pCompanyService PSP Company Service
     * @return DD V0 Company status
     */
    public static String getQBOECompanyStatus(final CompanyService pCompanyService) {
        switch (pCompanyService.getStatusCd()) {
            case Cancelled:
            case Terminated:
                return "INACTV";
            default:
                return "ACTV";
        }
    }

    public static String getQBOEContactRole(ContactRole pContactRole) {
        if (null == pContactRole) {
            return null;
        }
        switch (pContactRole) {
            case Other:
                return "OTHR";
            case PrimaryPrincipal:
                return "OWNR";
            case PayrollAdmin:
                return "PRA";
            default:
                return null;
        }
    }

    /**
     * Converts the PSP Employee Status to DD V0 Employee Status
     *
     * @param pEmployeeStatus PSP Employee Status
     * @return Legacy Employee Status (Active = ACTV,  Inactive = INACTV)
     */
    public static String getQBOEEmployeeStatus(EmployeeStatus pEmployeeStatus) {
        if (null == pEmployeeStatus) {
            return null;
        }
        switch (pEmployeeStatus) {
            case Active:
                return "ACTV";
            case Inactive:
                return "INACTV";
            default:
                return null;
        }
    }

    /**
     * Converts the DD V0 Employee Status to the PSP Employee Status
     *
     * @param pEmployeeStatus DD V0 Employee Status
     * @return PSP Employee Status (ACTV = Active, INACTV = Inactive)
     */
    public static EmployeeStatus getEmployeeStatus(String pEmployeeStatus) {
        EmployeeStatus status = null;
        if ("INACTV".equals(pEmployeeStatus)) {
            status = EmployeeStatus.Inactive;
        } else if ("ACTV".equals(pEmployeeStatus)) {
            status = EmployeeStatus.Active;
        } else {
            status = EmployeeStatus.valueOf(pEmployeeStatus);
        }
        return status;
    }

    public static ContactRole getContactRole(String pContactRoleCd) {
        if ("OTHR".equalsIgnoreCase(pContactRoleCd)) {
            return ContactRole.Other;
        } else if ("OWNR".equalsIgnoreCase(pContactRoleCd)) {
            return ContactRole.PrimaryPrincipal;
        } else if ("PRA".equalsIgnoreCase(pContactRoleCd)) {
            return ContactRole.PayrollAdmin;
        } else {
            return null;
        }
    }

    public static PayrollFrequencyDTO getPayrollFrequencyDTO(String pPayrollFrequencyCd) {
        if (null == pPayrollFrequencyCd) {
            return null;
        } else if ("260".equals(pPayrollFrequencyCd)) {
            return PayrollFrequencyDTO.Daily;
        } else if ("52".equals(pPayrollFrequencyCd)) {
            return PayrollFrequencyDTO.Weekly;
        } else if ("26".equals(pPayrollFrequencyCd)) {
            return PayrollFrequencyDTO.BiWeekly;
        } else if ("24".equals(pPayrollFrequencyCd)) {
            return PayrollFrequencyDTO.SemiMonthly;
        } else if ("12".equals(pPayrollFrequencyCd)) {
            return PayrollFrequencyDTO.Monthly;
        } else if ("4".equals(pPayrollFrequencyCd)) {
            return PayrollFrequencyDTO.Quarterly;
        } else if ("2".equals(pPayrollFrequencyCd)) {
            return PayrollFrequencyDTO.SemiAnnual;
        } else if ("1".equals(pPayrollFrequencyCd)) {
            return PayrollFrequencyDTO.Annual;
        } else {
            return PayrollFrequencyDTO.valueOf(pPayrollFrequencyCd);
        }
    }

    public static OffloadGroup getOffloadGroup(String pOffloadGroupCd) {
        return PayrollServices.entityFinder.<OffloadGroup>findById(OffloadGroup.class, pOffloadGroupCd);
    }

    /**
     * Converts the PSP Settlement Type to DD V0 Settlement Type
     *
     * @param pSettlementType PSP Settlement Type
     * @return Settlement Type (ACH = ACH,  Wire= WIRE, Cash=CASH, CheckType = CHKTYPE, Other=OTH)
     */
    public static String getQBOESettlementType(SettlementType pSettlementType) {
        if (null == pSettlementType) {
            return null;
        }
        switch (pSettlementType) {
            case ACH:
                return "ACH";
            case Wire:
                return "WIRE";
            case Cash:
                return "CASH";
            case CheckType:
                return "CHECK";
            case Other:
                return "OTHER";
            default:
                return null;
        }
    }

    public static String getQBOECommunicationTypePreference(CommunicationType pCommunicationType) {
        if (CommunicationType.Phone == pCommunicationType) {
            return "PHONE";
        } else if (CommunicationType.Email == pCommunicationType) {
            return "EMAIL";
        } else {
            return null;
        }
    }

    public static String getQBOEPayrollStatus(PayrollRun pPayrollRun) {
        if (null == pPayrollRun || pPayrollRun.getPayrollRunStatus() == null) {
            return null;
        }

        PayrollStatus payrollStatus = pPayrollRun.getPayrollRunStatus();
        DomainEntitySet<TransactionReturn> txnRets = TransactionReturn.findTransactionReturns(pPayrollRun.getSourcePayRunId(), pPayrollRun.getCompany());
        int numNSFs = 0;

        for (TransactionReturn currRet : txnRets) {
            boolean bIsNSF = currRet.isNSF();
            if (bIsNSF) {
                numNSFs++;
            }
        }

        switch (payrollStatus) {
            case PendingWire:
                if (numNSFs >= 1) {
                    return "NSF";
                } else {
                    return "DBRETURNED";
                }
            case PendingReversals:
                if (numNSFs >= 1) {
                    return "NSF";
                } else {
                    return "DBRETURNED";
                }
            case ReversalsOffloaded:
                if (numNSFs >= 1) {
                    return "NSF";
                } else {
                    return "DBRETURNED";
                }
            case ReversalsFinished:
                if (numNSFs >= 1) {
                    return "NSF";
                } else {
                    return "DBRETURNED";
                }
            case ReturnedTwice:
                if (numNSFs >= 2) {
                    return "NSFTWICE";
                } else {
                    return "DBRETURNED";
                }
            case WrittenOff:
                return "WRITTENOFF";
            case Pending:
                return "PENDING";
            case OffloadedDebit:
                return "OFFLDDEBIT";
            case OffloadedAll:
                return "OFFLDALL";
            case PendingRedebit:
                if (numNSFs >= 1) {
                    return "NSFREDBPND";
                } else {
                    return "DBRTNRDBPE";
                }
            case NSFCanceled:
                return "NSFCANCEL";
            case PendingAutoRedebit:
                return "NSFREDBPND";
            case RedebitOffloaded:
                if (numNSFs >= 1) {
                    return "NSFREDBOFF";
                } else {
                    return "DBRTNRDBOF";
                }
            case AutoRedebitOffloaded:
                return "NSFREDBOFF";
            case DebitReturned:
                return "DBRETURNED";
            case DebitReturnedCanceled:
                return "DBRTNCANCL";
            case Canceled:
                return "CANCELED";
            case Complete:
                return "COMPLETE";
            default:
                return null;

        }

    }

    /**
     * Converts the DD V0 Settlement Type to PSP Settlement Type
     *
     * @param pSettlementTypeCd DD V0 Settlement Type Code
     * @return Settlement Type (ACH = ACH,  Wire= WIRE, Cash=CASH, CheckType = CHKTYPE, Other=OTH)
     */
    public static SettlementType getSettlementType(String pSettlementTypeCd) {
        if ("ACH".equalsIgnoreCase(pSettlementTypeCd)) {
            return SettlementType.ACH;
        } else if ("WIRE".equalsIgnoreCase(pSettlementTypeCd)) {
            return SettlementType.Wire;
        } else if ("CASH".equalsIgnoreCase(pSettlementTypeCd)) {
            return SettlementType.Cash;
        } else if ("CHECK".equalsIgnoreCase(pSettlementTypeCd)) {
            return SettlementType.CheckType;
        } else if ("OTHER".equalsIgnoreCase(pSettlementTypeCd)) {
            return SettlementType.Other;
        } else {
            return SettlementType.valueOf(pSettlementTypeCd);
        }
    }

    /**
     * Converts the DD V0 Settlement Type to PSP Settlement Type
     *
     * @param pSettlementTypeCd DD V0 Settlement Type Code
     * @return Settlement Type (ACH = ACH,  Wire= WIRE, Cash=CASH, CheckType = CHKTYPE, Other=OTH)
     */
    public static SettlementTypeDTO getSettlementTypeDTO(String pSettlementTypeCd) {
        if ("ACH".equalsIgnoreCase(pSettlementTypeCd)) {
            return SettlementTypeDTO.ACH;
        } else if ("WIRE".equalsIgnoreCase(pSettlementTypeCd)) {
            return SettlementTypeDTO.Wire;
        } else if ("CASH".equalsIgnoreCase(pSettlementTypeCd)) {
            return SettlementTypeDTO.Cash;
        } else if ("CHECK".equalsIgnoreCase(pSettlementTypeCd)) {
            return SettlementTypeDTO.CheckType;
        } else if ("OTHER".equalsIgnoreCase(pSettlementTypeCd)) {
            return SettlementTypeDTO.Other;
        } else {
            return SettlementTypeDTO.valueOf(pSettlementTypeCd);
        }
    }

    public static BankAccountOwnerType getBankAccountOwnerType(String pBAOwnerTypeCd) {
        if (null == pBAOwnerTypeCd) {
            return null;
        } else if ("C".equalsIgnoreCase(pBAOwnerTypeCd)) {
            return BankAccountOwnerType.Company;
        } else if ("E".equalsIgnoreCase(pBAOwnerTypeCd)) {
            return BankAccountOwnerType.Employee;
        } else if ("I".equalsIgnoreCase(pBAOwnerTypeCd)) {
            return BankAccountOwnerType.Intuit;
        } else {
            return BankAccountOwnerType.valueOf(pBAOwnerTypeCd);
        }
    }

    public static TransactionStateDTO getTransactionStateDTO(String pTransactionStateCd) {
        if (null == pTransactionStateCd) {
            return null;
        } else if ("CR".equalsIgnoreCase(pTransactionStateCd)) {
            return TransactionStateDTO.Pending;
        } else if ("EX".equalsIgnoreCase(pTransactionStateCd)) {
            return TransactionStateDTO.Executed;
        } else if ("CLD".equalsIgnoreCase(pTransactionStateCd)) {
            return TransactionStateDTO.Canceled;
        } else if ("RTN".equalsIgnoreCase(pTransactionStateCd)) {
            return TransactionStateDTO.Returned;
        } else if ("CP".equalsIgnoreCase(pTransactionStateCd)) {
            return TransactionStateDTO.Completed;
        } else if ("VOID".equalsIgnoreCase(pTransactionStateCd)) {
            return TransactionStateDTO.Voided;
        } else {
            return TransactionStateDTO.valueOf(pTransactionStateCd);
        }
    }

    public static PayrollStatus getPayrollStatus(String pPayrollStatusCd) {
        if (null == pPayrollStatusCd) {
            return null;
        } else if ("DBRTNRDBPE".equalsIgnoreCase(pPayrollStatusCd)) {
            return PayrollStatus.PendingRedebit;
        } else if ("DBRTNRDBOF".equalsIgnoreCase(pPayrollStatusCd)) {
            return PayrollStatus.RedebitOffloaded;
        } else if ("WRITTENOFF".equalsIgnoreCase(pPayrollStatusCd)) {
            return PayrollStatus.WrittenOff;
        } else if ("PENDING".equalsIgnoreCase(pPayrollStatusCd)) {
            return PayrollStatus.Pending;
        } else if ("OFFLDDEBIT".equalsIgnoreCase(pPayrollStatusCd)) {
            return PayrollStatus.OffloadedDebit;
        } else if ("OFFLDALL".equalsIgnoreCase(pPayrollStatusCd)) {
            return PayrollStatus.OffloadedAll;
        } else if ("NSF".equalsIgnoreCase(pPayrollStatusCd)) {
            return PayrollStatus.DebitReturned;
        } else if ("NSFCANCEL".equalsIgnoreCase(pPayrollStatusCd)) {
            return PayrollStatus.NSFCanceled;
        } else if ("NSFREDBPND".equalsIgnoreCase(pPayrollStatusCd)) {
            return PayrollStatus.PendingAutoRedebit;
        } else if ("NSFREDBOFF".equalsIgnoreCase(pPayrollStatusCd)) {
            return PayrollStatus.AutoRedebitOffloaded;
        } else if ("NSFTWICE".equalsIgnoreCase(pPayrollStatusCd)) {
            return PayrollStatus.ReturnedTwice;
        } else if ("DBRETURNED".equalsIgnoreCase(pPayrollStatusCd)) {
            return PayrollStatus.DebitReturned;
        } else if ("DBRTNCANCL".equalsIgnoreCase(pPayrollStatusCd)) {
            return PayrollStatus.DebitReturnedCanceled;
        } else if ("CANCELED".equalsIgnoreCase(pPayrollStatusCd)) {
            return PayrollStatus.Canceled;
        } else if ("COMPLETE".equalsIgnoreCase(pPayrollStatusCd)) {
            return PayrollStatus.Complete;
        } else {
            return PayrollStatus.valueOf(pPayrollStatusCd);
        }
    }

    /**
     * Converts the PSP BankAccountType to DD V0 BankAccountType
     *
     * @param pBankAccountType PSP BankAccount Type
     * @return BankAccount Type (Savings = S,  Checking= C)
     */
    public static String getQBOEBankAccountType(BankAccountType pBankAccountType) {

        if (null == pBankAccountType) {
            return null;
        }

        switch (pBankAccountType) {
            case Savings:
                return "S";
            case Checking:
                return "C";
            default:
                return null;
        }
    }

    public static String getQBOETransactionStateCode(TransactionStateCode pTransactionStateCd) {

        switch (pTransactionStateCd) {
            case Created:
                return "CR";
            case Cancelled:
                return "CLD";
            case Completed:
                return "CP";
            case Executed:
                return "EX";
            case Returned:
                return "RTN";
            case Voided:
                return "VOID";
            default:
                return null;
        }
    }

    public static String getQBOEEventTypeCode(EventTypeCode pEventTypeCode) {
        switch (pEventTypeCode) {

            case Strike:
                return "STRIKE";
            case ACHReturn:
            case ReversalReturn:
            case DDDebitReturn:
            case CBAVerifyReturn:
            case DDReject:
            case FeeReturn:
            case ERRefundReturn:
            case NSF:
            case NOC:
                return "ACHRETRN";
            case ServiceStatusChange:
                return "DDSTATCHG";
            case CompanyBankAccountStatusChange:
                return "CBASTATCHG";
            case LimitViolation:
                return "DDOVRPRLMT";
            case ReversalOK:
                return "REVERSALOK";
            case DDIncreasePayrollLimit:
                return "DDINCPRLMT";
            case PayrollCancelled:
                return "PYRLCANCLD";
            case ReversalRequested:
                return "REVERSALRQ";
            case TransmissionError:
                return "TRANSMERR";
            default:
                if (null != pEventTypeCode) {
                    return pEventTypeCode.toString();
                }
                return null;
        }
    }

    public static EventTypeCode getEventTypeCode(String pEventTypeCode) {
        if (null == pEventTypeCode) {
            return null;
        } else if ("STRIKE".equalsIgnoreCase(pEventTypeCode)) {
            return EventTypeCode.Strike;
        } else if ("ACHRETRN".equalsIgnoreCase(pEventTypeCode)) {
            return EventTypeCode.ACHReturn;
        } else if ("DDSTATCHG".equalsIgnoreCase(pEventTypeCode)) {
            return EventTypeCode.ServiceStatusChange;
        } else if ("CBASTATCHG".equalsIgnoreCase(pEventTypeCode)) {
            return EventTypeCode.CompanyBankAccountStatusChange;
        } else if ("DDOVRPRLMT".equalsIgnoreCase(pEventTypeCode)) {
            return EventTypeCode.LimitViolation;
        } else if ("REVERSALOK".equalsIgnoreCase(pEventTypeCode)) {
            return EventTypeCode.ReversalOK;
        } else if ("DDINCPRLMT".equalsIgnoreCase(pEventTypeCode)) {
            return EventTypeCode.DDIncreasePayrollLimit;
        } else if ("PYRLCANCLD".equalsIgnoreCase(pEventTypeCode)) {
            return EventTypeCode.PayrollCancelled;
        } else if ("REVERSALRQ".equalsIgnoreCase(pEventTypeCode)) {
            return EventTypeCode.ReversalRequested;
        } else if ("TRANSMERR".equalsIgnoreCase(pEventTypeCode)) {
            return EventTypeCode.TransmissionError;
        } else {
            return EventTypeCode.valueOf(pEventTypeCode);
        }

    }

    public static OfferingServiceChargeType getFeeTypeCode(String pFeeTypeCode) {
        if (null == pFeeTypeCode) {
            return null;
        } else if ("NSFFEEAMT".equalsIgnoreCase(pFeeTypeCode)) {
            return OfferingServiceChargeType.DebitReturnFee;
        } else if ("REVFEEAMT".equalsIgnoreCase(pFeeTypeCode)) {
            return OfferingServiceChargeType.ReversalFee;
        } else {
            return OfferingServiceChargeType.valueOf(pFeeTypeCode);
        }
    }

    public static String getQBOEFeeTypeCode(OfferingServiceChargeType pFeeTypeCodeDTO) {
        switch (pFeeTypeCodeDTO) {

            case DebitReturnFee:
                return "NSFFEEAMT";
            case ReversalFee:
                return "REVFEEAMT";
            default:
                return null;
        }
    }

    public static String getQBOELedgerAccountCode(LedgerAccountCode pLedgerAccountCode) {
        switch (pLedgerAccountCode) {

            case BadDebt:
                return "BD";
            case DDCurrentCash:
                return "DDCC";
            case DDCurrentLiability:
                return "DDCL";
            case DDFutureLiability:
                return "DDFL";
            case DDFutureReceivable:
                return "DDFR";
            case EEReturnCash:
                return "EERC";
            case EEReturnLiablility:
                return "EERL";
            case ERReturnCash:
                return "ERRC";
            case ERReturnReceivable:
                return "ERRR";
            case FeeCashBalanceSheet:
                return "FCBS";
            case FeeIncome:
                return "FI";
            case FeeCashRevenue:
                return "FCR";
            case SalesAndUseTax:
                return "SUTAX";
            case TaxFutureReceivable:
                return "TaxFutureReceivable";
            case TaxFutureLiability:
                return "TaxFutureLiability";
            case TaxCurrentCash:
                return "TaxCurrentCash";
            case TaxCurrentLiability:
                return "TaxCurrentLiability";
            case ERPayable:
                return "ERPayable";
            default:
                return null;
        }
    }

    public static LedgerAccountCode getLedgerAccountCode(String pLedgerAccountCode) {
        if (null == pLedgerAccountCode) {
            return null;
        } else if ("BD".equalsIgnoreCase(pLedgerAccountCode)) {
            return LedgerAccountCode.BadDebt;
        } else if ("DDCC".equalsIgnoreCase(pLedgerAccountCode)) {
            return LedgerAccountCode.DDCurrentCash;
        } else if ("DDCL".equalsIgnoreCase(pLedgerAccountCode)) {
            return LedgerAccountCode.DDCurrentLiability;
        } else if ("DDFL".equalsIgnoreCase(pLedgerAccountCode)) {
            return LedgerAccountCode.DDFutureLiability;
        } else if ("DDFR".equalsIgnoreCase(pLedgerAccountCode)) {
            return LedgerAccountCode.DDFutureReceivable;
        } else if ("EERC".equalsIgnoreCase(pLedgerAccountCode)) {
            return LedgerAccountCode.EEReturnCash;
        } else if ("EERL".equalsIgnoreCase(pLedgerAccountCode)) {
            return LedgerAccountCode.EEReturnLiablility;
        } else if ("ERRC".equalsIgnoreCase(pLedgerAccountCode)) {
            return LedgerAccountCode.ERReturnCash;
        } else if ("ERRR".equalsIgnoreCase(pLedgerAccountCode)) {
            return LedgerAccountCode.ERReturnReceivable;
        } else if ("FCBS".equalsIgnoreCase(pLedgerAccountCode)) {
            return LedgerAccountCode.FeeCashBalanceSheet;
        } else if ("FI".equalsIgnoreCase(pLedgerAccountCode)) {
            return LedgerAccountCode.FeeIncome;
        } else if ("FCR".equalsIgnoreCase(pLedgerAccountCode)) {
            return LedgerAccountCode.FeeCashRevenue;
        } else if ("SUTAX".equalsIgnoreCase(pLedgerAccountCode)) {
            return LedgerAccountCode.SalesAndUseTax;
        } else {
            return LedgerAccountCode.valueOf(pLedgerAccountCode);
        }

    }

    public static String getQBOEBankReturnStatus(TransactionReturnStatusCode pBankReturnStatus) {
        switch (pBankReturnStatus) {
            case Created:
                return "CR";
            case Error:
                return "ERR";
            case Open:
                return "OPEN";
            case Resolved:
                return "RSLVD";
            default:
                return null;
        }
    }

    public static TransactionReturnStatusCode getBankReturnStatusCode(String pBankReturnStatus) {
        if (null == pBankReturnStatus) {
            return null;
        } else if ("CR".equalsIgnoreCase(pBankReturnStatus)) {
            return TransactionReturnStatusCode.Created;
        } else if ("ERR".equalsIgnoreCase(pBankReturnStatus)) {
            return TransactionReturnStatusCode.Error;
        } else if ("OPEN".equalsIgnoreCase(pBankReturnStatus)) {
            return TransactionReturnStatusCode.Open;
        } else if ("RSLVD".equalsIgnoreCase(pBankReturnStatus)) {
            return TransactionReturnStatusCode.Resolved;
        } else {
            return TransactionReturnStatusCode.valueOf(pBankReturnStatus);
        }
    }

    public static String getQBOEPayrollStatusDesc(String pQBOEPayrollStatusCode) {
        if (null == pQBOEPayrollStatusCode) {
            return null;
        } else if ("DBRTNRDBPE".equalsIgnoreCase(pQBOEPayrollStatusCode)) {
            return "DebitReturnedRedebitPending";
        } else if ("DBRTNRDBOF".equalsIgnoreCase(pQBOEPayrollStatusCode)) {
            return "DebitReturnedRedebitOffloaded";
        } else if ("WRITTENOFF".equalsIgnoreCase(pQBOEPayrollStatusCode)) {
            return "WrittenOff";
        } else if ("PENDING".equalsIgnoreCase(pQBOEPayrollStatusCode)) {
            return "Pending";
        } else if ("OFFLDDEBIT".equalsIgnoreCase(pQBOEPayrollStatusCode)) {
            return "OffloadedDebit";
        } else if ("OFFLDALL".equalsIgnoreCase(pQBOEPayrollStatusCode)) {
            return "OffloadedAll";
        } else if ("NSF".equalsIgnoreCase(pQBOEPayrollStatusCode)) {
            return "NSF";
        } else if ("NSFCANCEL".equalsIgnoreCase(pQBOEPayrollStatusCode)) {
            return "NSFCanceled";
        } else if ("NSFREDBPND".equalsIgnoreCase(pQBOEPayrollStatusCode)) {
            return "NSFRedebitPending";
        } else if ("NSFREDBOFF".equalsIgnoreCase(pQBOEPayrollStatusCode)) {
            return "NSFRedebitOffloaded";
        } else if ("NSFTWICE".equalsIgnoreCase(pQBOEPayrollStatusCode)) {
            return "NSFTwice";
        } else if ("DBRETURNED".equalsIgnoreCase(pQBOEPayrollStatusCode)) {
            return "DebitReturned";
        } else if ("DBRTNCANCL".equalsIgnoreCase(pQBOEPayrollStatusCode)) {
            return "DebitReturnedCanceled";
        } else if ("CANCELED".equalsIgnoreCase(pQBOEPayrollStatusCode)) {
            return "Canceled";
        } else if ("COMPLETE".equalsIgnoreCase(pQBOEPayrollStatusCode)) {
            return "Complete";
        } else {
            return pQBOEPayrollStatusCode;
        }
    }

    public static String getQBOETransactionTypeCode(TransactionTypeCode pTransactionTypeCd, OfferingServiceChargeType pOfferingServiceCharge) {
        //return TransactionTypeBE.getQBOETransactionTypeCode(TransactionTypeCode.valueOf(pTransactionTypeCd.toString()));
        switch (pTransactionTypeCd) {
            case EmployerDoublePaymentRefundCredit:
                return "ER2PRFCR";
            case Intuit5DayReturnTransfer:
                return "INT5DRTXFR";
            case EmployerFeeRedebit:
                if (pOfferingServiceCharge == OfferingServiceChargeType.DebitReturnFee) {
                    return "ERFERTREDB";
                } else if (pOfferingServiceCharge == OfferingServiceChargeType.ReversalFee) {
                    return "ERFERVREDB";
                }
            case IntuitFeeTransfer:
                if (pOfferingServiceCharge == OfferingServiceChargeType.DebitReturnFee) {
                    return "INTERFERTX";
                } else if (pOfferingServiceCharge == OfferingServiceChargeType.ReversalFee) {
                    return "INTERFERVX";
                }
            case IntuitEmployerVerificationReturnTransfer:
                return "INTERVRRTX";
            case EmployeeEscalationCredit:
                return "EEESCR";
            case EmployerEscalationCredit:
                return "ERESCR";
            case EmployerDdDebit:
                return "ERDDDB";
            case EmployeeDdCredit:
                return "EEDDCR";
            case EmployerDdRedebit:
                return "ERDDREDB";
            case EmployeeDdReversalDebit:
                return "EEDDRVDB";
            case EmployerDdRefundCredit:
                return "ERDDRFCR";
            case EmployerDdRejectRefundCredit:
                return "ERDDRJRFCR";
            case EmployerDdReversalRefundCredit:
                return "ERDDRVRFCR";
            case EmployerDdReturnedRefundCredit:
                return "ERDDRTRFCR";
            case EmployerVerificationDebit:
                return "ERVERDB";
            case EmployerWriteOff:
                return "ERWO";
            case IntuitEmployeeReturnTransfer:
                return "INTEERTXFR";
            case BadDebtRecovery:
                return "WODEBTRCVR";
            case EmployerFeeDebit:
                if (pOfferingServiceCharge == OfferingServiceChargeType.DebitReturnFee) {
                    return "ERFERTDB";
                } else if (pOfferingServiceCharge == OfferingServiceChargeType.ReversalFee) {
                    return "ERFERVDB";
                }
            case EmployerFeeRefundCredit:
                if (pOfferingServiceCharge == OfferingServiceChargeType.DebitReturnFee) {
                    return "ERFERTRFCR";
                } else if (pOfferingServiceCharge == OfferingServiceChargeType.ReversalFee) {
                    return "ERFERVRFCR";
                }
            default:
                return null;
        }
    }

    public static TransactionTypeCode getTransactionTypeCode(String pTransactionTypeCodeCd) {
        TransactionTypeCode transactionType = null;
        if ("ER2PRFCR".equals(pTransactionTypeCodeCd)) {
            transactionType = com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.EmployerDoublePaymentRefundCredit;
        } else if ("INT5DRTXFR".equals(pTransactionTypeCodeCd)) {
            transactionType = com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.Intuit5DayReturnTransfer;
        } else if ("ERFERTREDB".equals(pTransactionTypeCodeCd) ||
                ("ERFERVREDB".equals(pTransactionTypeCodeCd))) {
            transactionType = com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.EmployerFeeRedebit;
        } else if ("INTERFERTX".equals(pTransactionTypeCodeCd) ||
                ("INTERFERVX".equals(pTransactionTypeCodeCd))) {
            transactionType = com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.IntuitFeeTransfer;
        } else if ("INTERVRRTX".equals(pTransactionTypeCodeCd)) {
            transactionType = com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.IntuitEmployerVerificationReturnTransfer;
        } else if ("EEESCR".equals(pTransactionTypeCodeCd)) {
            transactionType = com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.EmployeeEscalationCredit;
        } else if ("ERESCR".equals(pTransactionTypeCodeCd)) {
            transactionType = com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.EmployerEscalationCredit;
        } else if ("ERDDDB".equals(pTransactionTypeCodeCd)) {
            transactionType = com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.EmployerDdDebit;
        } else if ("EEDDCR".equals(pTransactionTypeCodeCd)) {
            transactionType = com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.EmployeeDdCredit;
        } else if ("ERDDREDB".equals(pTransactionTypeCodeCd)) {
            transactionType = com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.EmployerDdRedebit;
        } else if ("EEDDRVDB".equals(pTransactionTypeCodeCd)) {
            transactionType = com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.EmployeeDdReversalDebit;
        } else if ("ERDDRFCR".equals(pTransactionTypeCodeCd)) {
            transactionType = com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.EmployerDdRefundCredit;
        } else if ("ERDDRJRFCR".equals(pTransactionTypeCodeCd)) {
            transactionType = com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.EmployerDdRejectRefundCredit;
        } else if ("ERDDRVRFCR".equals(pTransactionTypeCodeCd)) {
            transactionType = com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.EmployerDdReversalRefundCredit;
        } else if ("ERDDRTRFCR".equals(pTransactionTypeCodeCd)) {
            transactionType = com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.EmployerDdReturnedRefundCredit;
        } else if ("ERVERDB".equals(pTransactionTypeCodeCd)) {
            transactionType = com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.EmployerVerificationDebit;
        } else if ("ERWO".equals(pTransactionTypeCodeCd)) {
            transactionType = com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.EmployerWriteOff;
        } else if ("INTEERTXFR".equals(pTransactionTypeCodeCd)) {
            transactionType = com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.IntuitEmployeeReturnTransfer;
        } else if ("WODEBTRCVR".equals(pTransactionTypeCodeCd)) {
            transactionType = com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.BadDebtRecovery;
        } else if ("ERFERVDB".equals(pTransactionTypeCodeCd) || "ERFERTDB".equals(pTransactionTypeCodeCd)) {
            transactionType = com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.EmployerFeeDebit;
        } else if ("ERFERTRFCR".equals(pTransactionTypeCodeCd) ||"ERFERVRFCR".equals(pTransactionTypeCodeCd)) {
            transactionType = com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.EmployerFeeRefundCredit;
        }
        return transactionType;
    }

    public static String getQBOETransactionTypeName(TransactionType pTransactionType, OfferingServiceChargeType pOfferingServiceCharge) {
        TransactionTypeCode transactionTypeCd = pTransactionType.getTransactionTypeCd();
        switch (transactionTypeCd) {
            case EmployerFeeRedebit:
                if (pOfferingServiceCharge == OfferingServiceChargeType.DebitReturnFee) {
                    return "ER Return Fee Redebit";
                } else if (pOfferingServiceCharge == OfferingServiceChargeType.ReversalFee) {
                    return "ER Reversal Fee Redebit";
                } else {
                    return pTransactionType.getName();
                }
            case IntuitFeeTransfer:
                if (pOfferingServiceCharge == OfferingServiceChargeType.DebitReturnFee) {
                    return "Intuit ER Return Fee Transfer";
                } else if (pOfferingServiceCharge == OfferingServiceChargeType.ReversalFee) {
                    return "Intuit ER Reversal Fee Transfer";
                } else {
                    return pTransactionType.getName();
                }
            case EmployerFeeDebit:
                if (pOfferingServiceCharge == OfferingServiceChargeType.DebitReturnFee) {
                    return "ER Return Fee Debit";
                } else if (pOfferingServiceCharge == OfferingServiceChargeType.ReversalFee) {
                    return "ER Reversal Fee Debit";
                } else {
                    return pTransactionType.getName();
                }
            case EmployerFeeRefundCredit:
                if (pOfferingServiceCharge == OfferingServiceChargeType.DebitReturnFee) {
                    return "ER Return Fee Refund Credit";
                } else if (pOfferingServiceCharge == OfferingServiceChargeType.ReversalFee) {
                    return "ER Reversal Fee Refund Credit";
                } else {
                    return pTransactionType.getName();
                }
            default:
                return pTransactionType.getName();
        }
    }
}
