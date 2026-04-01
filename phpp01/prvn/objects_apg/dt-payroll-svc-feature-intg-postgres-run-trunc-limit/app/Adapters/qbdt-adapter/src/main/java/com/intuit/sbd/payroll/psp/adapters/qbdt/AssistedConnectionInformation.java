package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.api.dtos.QBDTRequestInfoDTO;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jan 29, 2011
 * Time: 10:37:48 AM
 */
public class AssistedConnectionInformation {
    private static final String BALANCE_FILE_MESSAGE =      "Sent Balance File";
    private static final String NEW_PAYROLL_MESSAGE =       "Sent %1$d Paychecks";
    private static final String NEW_VOID_PAYROLL_MESSAGE =  "Sent New and Voided Paychecks";
    private static final String VOID_PAYROLL_MESSAGE =      "Voided %1$d Paychecks";
    private static final String MODIFIED_PAYROLL_MESSAGE =  "Modified %1$d Paychecks";
    private static final String MAINTENANCE_MESSAGE =       "Sent Maintenance";
    private static final String DATA_RECOVERY_MESSAGE =     "Data Recovery";
    private static final String SIGN_ON_REJECTED_MESSAGE =  "Signon Rejected";
    private static final String AS400_REJECTED_MESSAGE =    "AS400 Rejected";
    private static final String SYNC_MESSAGE =              "Sent Sync";
    private static final String UNKNOWN_MESSAGE =           "Unknown Request Type";
    private static final String ZERO_PAYROLL_MESSAGE =      "Zero Payroll";
    private static final String SKIP_PROCESSING_MESSAGE =   "Skipped processing";
    private static final String QUEUED_MESSAGE =            "Request queued to be reprocessed";

    private TransmissionType mTransmissionType = null;
    private int mNewEmployeesCount = 0;
    private SpcfCalendar mNewEmployeesProcessingStart;
    private SpcfCalendar mNewEmployeesProcessingEnd;
    private int mEmployeeModsCount = 0;
    private SpcfCalendar mEmployeeModsProcessingStart;
    private SpcfCalendar mEmployeeModsProcessingEnd;
    private int mNewPayrollItemsCount = 0;
    private SpcfCalendar mNewPayrollItemsProcessingStart;
    private SpcfCalendar mNewPayrollItemsProcessingEnd;
    private int mPayrollItemModsCount = 0;
    private SpcfCalendar mPayrollItemModsProcessingStart;
    private SpcfCalendar mPayrollItemModsProcessingEnd;
    private SpcfCalendar mPayrollProcessingStart;
    private SpcfCalendar mPayrollProcessingEnd;
    private int mNewPayrollTransactionsCount = 0;
    private SpcfCalendar mNewPayrollTransactionsProcessingStart;
    private SpcfCalendar mNewPayrollTransactionsProcessingEnd;
    private int mPayrollTransactionModsCount = 0;
    private SpcfCalendar mPayrollTransactionModsProcessingStart;
    private SpcfCalendar mPayrollTransactionModsProcessingEnd;
    private int mEmployeeDeletesCount = 0;
    private int mPayrollItemDeletesCount = 0;
    private int mPaycheckDeletesCount = 0;
    private int mPayrollTransactionDeletesCount = 0;
    private SpcfCalendar mDeletesProcessingStart;
    private SpcfCalendar mDeletesProcessingEnd;

    private int mNumberOfNewPaychecks = 0;
    private int mNumberOfPaycheckMods = 0;
    private int mNumberOfVoids = 0;
    private boolean mIsAs400Rejection = false;
    private boolean mProcessedUpdatesOrAdds = false ;
    private boolean mIsDataRecovery = false;
    private boolean mProcessedRequest = true;
    private boolean mRequestQueuedToBeReprocessed = false;
    private boolean mIsAssistedRequest = false;

    public void setTransmissionType(TransmissionType pTransmissionType) {
        mTransmissionType = pTransmissionType;
    }

    public TransmissionType getTransmissionType() {
        return mTransmissionType;
    }

    public boolean isAssistedRequest() {
        return mIsAssistedRequest;
    }

    public void setIsAssistedRequest(boolean pIsAssistedRequest) {
        mIsAssistedRequest = pIsAssistedRequest;
    }

    public void addNumberOfNewPaychecks(int pNumberOfNewPaychecks) {
        mNumberOfNewPaychecks += pNumberOfNewPaychecks;
    }

    public void addNumberOfPaycheckMods(int pNumberOfPaycheckMods) {
        mNumberOfPaycheckMods += pNumberOfPaycheckMods;
    }

    public void addNumberOfVoids(int pNumberOfVoids) {
        mNumberOfVoids += pNumberOfVoids;
    }

    public void setIsAs400Rejection(boolean pIsAs400Rejection) {
        mIsAs400Rejection = pIsAs400Rejection;
    }

    public void setProcessedAddsOrUpdates(boolean pProcessedUpdatesOrAdds) {
        mProcessedUpdatesOrAdds = pProcessedUpdatesOrAdds;
    }

    public void setIsDataRecovery(boolean pIsDataRecovery) {
        mIsDataRecovery = pIsDataRecovery;
    }

    public void setNewEmployeesCount(int pNewEmployeesCount) {
        mNewEmployeesCount = pNewEmployeesCount;
    }

    public void setNewEmployeesProcessingStart(SpcfCalendar pNewEmployeesProcessingStart) {
        mNewEmployeesProcessingStart = pNewEmployeesProcessingStart;
    }

    public void setNewEmployeesProcessingEnd(SpcfCalendar pNewEmployeesProcessingEnd) {
        mNewEmployeesProcessingEnd = pNewEmployeesProcessingEnd;
    }

    public void setEmployeeModsCount(int pEmployeeModsCount) {
        mEmployeeModsCount = pEmployeeModsCount;
    }

    public void setEmployeeModsProcessingStart(SpcfCalendar pEmployeeModsProcessingStart) {
        mEmployeeModsProcessingStart = pEmployeeModsProcessingStart;
    }

    public void setEmployeeModsProcessingEnd(SpcfCalendar pEmployeeModsProcessingEnd) {
        mEmployeeModsProcessingEnd = pEmployeeModsProcessingEnd;
    }

    public void setNewPayrollItemsCount(int pNewPayrollItemsCount) {
        mNewPayrollItemsCount = pNewPayrollItemsCount;
    }

    public void setNewPayrollItemsProcessingStart(SpcfCalendar pNewPayrollItemsProcessingStart) {
        mNewPayrollItemsProcessingStart = pNewPayrollItemsProcessingStart;
    }

    public void setNewPayrollItemsProcessingEnd(SpcfCalendar pNewPayrollItemsProcessingEnd) {
        mNewPayrollItemsProcessingEnd = pNewPayrollItemsProcessingEnd;
    }

    public void setPayrollItemModsCount(int pPayrollItemModsCount) {
        mPayrollItemModsCount = pPayrollItemModsCount;
    }

    public void setPayrollItemModsProcessingStart(SpcfCalendar pPayrollItemModsProcessingStart) {
        mPayrollItemModsProcessingStart = pPayrollItemModsProcessingStart;
    }

    public void setPayrollItemModsProcessingEnd(SpcfCalendar pPayrollItemModsProcessingEnd) {
        mPayrollItemModsProcessingEnd = pPayrollItemModsProcessingEnd;
    }

    public void setPayrollProcessingStart(SpcfCalendar pNewPaychecksProcessingStart) {
        mPayrollProcessingStart = pNewPaychecksProcessingStart;
    }

    public void setPayrollProcessingEnd(SpcfCalendar pNewPaychecksProcessingEnd) {
        mPayrollProcessingEnd = pNewPaychecksProcessingEnd;
    }

    public void setNewPayrollTransactionsCount(int pNewPayrollTransactionsCount) {
        mNewPayrollTransactionsCount = pNewPayrollTransactionsCount;
    }

    public void setNewPayrollTransactionsProcessingStart(SpcfCalendar pNewPayrollTransactionsProcessingStart) {
        mNewPayrollTransactionsProcessingStart = pNewPayrollTransactionsProcessingStart;
    }

    public void setNewPayrollTransactionsProcessingEnd(SpcfCalendar pNewPayrollTransactionsProcessingEnd) {
        mNewPayrollTransactionsProcessingEnd = pNewPayrollTransactionsProcessingEnd;
    }

    public void setPayrollTransactionModsCount(int pPayrollTransactionModsCount) {
        mPayrollTransactionModsCount = pPayrollTransactionModsCount;
    }

    public void setPayrollTransactionModsProcessingStart(SpcfCalendar pPayrollTransactionModsProcessingStart) {
        mPayrollTransactionModsProcessingStart = pPayrollTransactionModsProcessingStart;
    }

    public void setPayrollTransactionModsProcessingEnd(SpcfCalendar pPayrollTransactionModsProcessingEnd) {
        mPayrollTransactionModsProcessingEnd = pPayrollTransactionModsProcessingEnd;
    }

    public void setEmployeeDeletesCount(int pEmployeeDeletesCount) {
        mEmployeeDeletesCount = pEmployeeDeletesCount;
    }

    public void setPayrollItemDeletesCount(int pPayrollItemDeletesCount) {
        mPayrollItemDeletesCount = pPayrollItemDeletesCount;
    }

    public void setPaycheckDeletesCount(int pPaycheckDeletesCount) {
        mPaycheckDeletesCount = pPaycheckDeletesCount;
    }

    public void setPayrollTransactionDeletesCount(int pPayrollTransactionDeletesCount) {
        mPayrollTransactionDeletesCount = pPayrollTransactionDeletesCount;
    }

    public void setDeletesProcessingStart(SpcfCalendar pDeletesProcessingStart) {
        mDeletesProcessingStart = pDeletesProcessingStart;
    }

    public void setDeletesProcessingEnd(SpcfCalendar pDeletesProcessingEnd) {
        mDeletesProcessingEnd = pDeletesProcessingEnd;
    }

    public void setProcessedRequest(boolean pProcessedRequest) {
        mProcessedRequest = pProcessedRequest;
    }

    public void setRequestQueuedToBeReprocessed(boolean pRequestQueuedToBeReprocessed) {
        mRequestQueuedToBeReprocessed = pRequestQueuedToBeReprocessed;
    }

    public static String getSkippedProcessingMessage() {
        return SKIP_PROCESSING_MESSAGE;
    }

    public static String getQueuedMessage() {
        return QUEUED_MESSAGE;
    }

    public static String getBalanceFileMessage() {
        return BALANCE_FILE_MESSAGE;
    }

    public static String getNewPayrollMessage(int pNumberOfPaychecks) {
        return String.format(NEW_PAYROLL_MESSAGE, pNumberOfPaychecks);
    }

    public static String getNewVoidPayrollMessage() {
        return NEW_VOID_PAYROLL_MESSAGE;
    }

    public static String getVoidPayrollMessage(int pNumberOfVoids) {
        return String.format(VOID_PAYROLL_MESSAGE, pNumberOfVoids);
    }

    public static String getModifiedPayrollMessage(int pNumberOfPaycheckMods) {
        return String.format(MODIFIED_PAYROLL_MESSAGE, pNumberOfPaycheckMods);
    }

    public static String getMaintenanceMessage() {
        return MAINTENANCE_MESSAGE;
    }

    public static String getDataRecoveryMessage() {
        return DATA_RECOVERY_MESSAGE;
    }

    public static String getSignOnRejectedMessage() {
        return SIGN_ON_REJECTED_MESSAGE;
    }

    public static String getAs400RejectedMessage() {
        return AS400_REJECTED_MESSAGE;
    }

    public static String getSyncMessage() {
        return SYNC_MESSAGE;
    }

    public static String getUnknownMessage() {
        return UNKNOWN_MESSAGE;
    }

    public static String getZeroPayrollMessage() {
        return ZERO_PAYROLL_MESSAGE;
    }

    public boolean ismIsDataRecovery() { return mIsDataRecovery; }
    public boolean isZeroPayroll() {
        return getZeroPayrollMessage().equals(getConnectionMessage());
    }

    public String getConnectionMessage() {
        if(!mProcessedRequest) {
            return getSkippedProcessingMessage();
        }

        if(mRequestQueuedToBeReprocessed) {
            return getQueuedMessage();
        }

        if(mTransmissionType == null) {
            return getUnknownMessage();
        }

        if(mIsAs400Rejection) {
            return getAs400RejectedMessage();
        }

        switch (mTransmissionType) {
            case Sync:
            case UsageSync:
                if(mIsDataRecovery) {
                    return getDataRecoveryMessage();
                } else {
                    return getSyncMessage();
                }

            case BalanceFile:
                return getBalanceFileMessage();

            case PayrollSubmission:
            case UsageSend:
                if(mNumberOfNewPaychecks > 0 && mNumberOfVoids > 0) {
                    return getNewVoidPayrollMessage();
                } else if(mNumberOfNewPaychecks > 0) {
                    return getNewPayrollMessage(mNumberOfNewPaychecks);
                } else if(mNumberOfVoids > 0) {
                    return getVoidPayrollMessage(mNumberOfVoids);
                } else if(mNumberOfPaycheckMods > 0) {
                    return getModifiedPayrollMessage(mNumberOfPaycheckMods);
                } else if(mProcessedUpdatesOrAdds) {
                    return getMaintenanceMessage();
                } else {
                    return getZeroPayrollMessage();
                }

            default:
                return getUnknownMessage();
        }
    }

    public Boolean isBalanceFile() {
        return TransmissionType.BalanceFile.equals(getTransmissionType());
    }

    public QBDTRequestInfoDTO createRequestInfo() {
        QBDTRequestInfoDTO qbdtRequestInfoDTO = new QBDTRequestInfoDTO();
        qbdtRequestInfoDTO.setEmployeeAddCount(mNewEmployeesCount);
        qbdtRequestInfoDTO.setEmployeeAddStart(mNewEmployeesProcessingStart);
        qbdtRequestInfoDTO.setEmployeeAddEnd(mNewEmployeesProcessingEnd);
        qbdtRequestInfoDTO.setEmployeeUpdateCount(mEmployeeModsCount);
        qbdtRequestInfoDTO.setEmployeeUpdateStart(mEmployeeModsProcessingStart);
        qbdtRequestInfoDTO.setEmployeeUpdateEnd(mEmployeeModsProcessingEnd);

        qbdtRequestInfoDTO.setPayrollItemAddCount(mNewPayrollItemsCount);
        qbdtRequestInfoDTO.setPayrollItemAddStart(mNewPayrollItemsProcessingStart);
        qbdtRequestInfoDTO.setPayrollItemAddEnd(mNewPayrollItemsProcessingEnd);
        qbdtRequestInfoDTO.setPayrollItemUpdateCount(mPayrollItemModsCount);
        qbdtRequestInfoDTO.setPayrollItemUpdateStart(mPayrollItemModsProcessingStart);
        qbdtRequestInfoDTO.setPayrollItemUpdateEnd(mPayrollItemModsProcessingEnd);

        qbdtRequestInfoDTO.setPaycheckAddCount(mNumberOfNewPaychecks);
        qbdtRequestInfoDTO.setPaycheckUpdateCount(mNumberOfPaycheckMods);
        qbdtRequestInfoDTO.setPayrollProcessingStart(mPayrollProcessingStart);
        qbdtRequestInfoDTO.setPayrollProcessingEnd(mPayrollProcessingEnd);

        qbdtRequestInfoDTO.setPayrollTransactionAddCount(mNewPayrollTransactionsCount);
        qbdtRequestInfoDTO.setPayrollTransactionAddStart(mNewPayrollTransactionsProcessingStart);
        qbdtRequestInfoDTO.setPayrollTransactionAddEnd(mNewPayrollTransactionsProcessingEnd);
        qbdtRequestInfoDTO.setPayrollTransactionUpdateCount(mPayrollTransactionModsCount);
        qbdtRequestInfoDTO.setPayrollTransactionUpdateStart(mPayrollTransactionModsProcessingStart);
        qbdtRequestInfoDTO.setPayrollTransactionUpdateEnd(mPayrollTransactionModsProcessingEnd);

        qbdtRequestInfoDTO.setEmployeeDeleteCount(mEmployeeDeletesCount);
        qbdtRequestInfoDTO.setPayrollItemDeleteCount(mPayrollItemDeletesCount);
        qbdtRequestInfoDTO.setPaycheckDeleteCount(mPaycheckDeletesCount);
        qbdtRequestInfoDTO.setPayrollTransactionDeleteCount(mPayrollTransactionDeletesCount);
        qbdtRequestInfoDTO.setDeleteProcessingStart(mDeletesProcessingStart);
        qbdtRequestInfoDTO.setDeleteProcessingEnd(mDeletesProcessingEnd);

        return qbdtRequestInfoDTO;
    }
}
