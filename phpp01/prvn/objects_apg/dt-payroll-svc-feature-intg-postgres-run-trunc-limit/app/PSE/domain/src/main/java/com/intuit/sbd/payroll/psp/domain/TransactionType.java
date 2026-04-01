package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Criterion;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Hand-written business logic
 */
public class TransactionType extends BaseTransactionType {
    public static List<TransactionTypeCode> ADDS_TO_PAYMENT_TRANSACTION_TYPES = Arrays.asList(TransactionTypeCode.AgencyTaxCredit,
                                                                                              TransactionTypeCode.AgencyDirectCredit,
                                                                                              TransactionTypeCode.AgencyHPDETaxPayment);

    public static List<TransactionTypeCode> SUBTRACTS_FROM_PAYMENT_TRANSACTION_TYPES = Arrays.asList(TransactionTypeCode.AgencyTaxDebit,
                                                                                                     TransactionTypeCode.AgencyDirectDebit,
                                                                                                     TransactionTypeCode.AgencyHPDETaxRefund,
                                                                                                     TransactionTypeCode.AgencyTaxOverpaymentApplied);

    public static List<TransactionTypeCode> TEMP_FLA_TRANSACTION_TYPES = Arrays.asList(TransactionTypeCode.FLATemp1,
                                                                                                     TransactionTypeCode.FLATemp2,
                                                                                                     TransactionTypeCode.FLATemp3,
                                                                                                     TransactionTypeCode.FLATemp4,
                                                                                                     TransactionTypeCode.FLATemp5);

    public static List<String> TEMP_FLA_TRANSACTION_NAMES = Arrays.asList("FLATemp1", "FLATemp2","FLATemp3","FLATemp4","FLATemp5");

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static TransactionTypeCode[] getRefundTypes() {
        DomainEntitySet<TransactionType> txnTypeList = findTransactionTypeByAssociationType(TransactionAssociationType.Refund);
        return build_TransactionTypeArray(txnTypeList);
    }

    public static TransactionTypeCode[] getRedebitTypes() {
        DomainEntitySet<TransactionType> txnTypeList = findTransactionTypeByAssociationType(TransactionAssociationType.Redebit);
        return build_TransactionTypeArray(txnTypeList);
    }

    public static TransactionTypeCode[] getRefundTypesForService(Service pService) {
        DomainEntitySet<TransactionType> txnTypeList = findTransactionTypeByAssociationTypeAndService(TransactionAssociationType.Refund, pService);
        return build_TransactionTypeArray(txnTypeList);
    }

    /**
     * Determines if the code provided is an employer transaction type.  (Ported directly from DD code)
     *
     * @param code Transaction Type Code
     * @return true if code is for Employer Transaction Type.
     */
    public static boolean isEmployerTransactionType(TransactionTypeCode code) {
        DomainEntitySet<TransactionType> txnTypeList = findTransactionTypeByTxnCategory(TransactionCategory.Employer);
        return Arrays.asList(build_TransactionTypeArray(txnTypeList)).contains(code);
    }

    /**
     * Determines if the code provided is an employee transaction type.  (Ported directly from DD code)
     *
     * @param code Transaction Type Code
     * @return true if code is for Employee Transaction Type.
     */
    public static boolean isEmployeeTransactionType(TransactionTypeCode code) {
        DomainEntitySet<TransactionType> txnTypeList = findTransactionTypeByTxnCategory(TransactionCategory.Employee);
        return Arrays.asList(build_TransactionTypeArray(txnTypeList)).contains(code);
    }

    /**
     * Determines if the code provided is an intuit transaction type
     *
     * @param code Transaction type code
     * @return true if code is for Intuit Transaction Type.
     */
    public static boolean isIntuitTransactionType(TransactionTypeCode code) {
        DomainEntitySet<TransactionType> txnTypeList = findTransactionTypeByTxnCategory(TransactionCategory.Intuit);
        return Arrays.asList(build_TransactionTypeArray(txnTypeList)).contains(code);
    }

    /**
     * Determines if the code provided is an refund transaction type
     *
     * @param code Transaction type code
     * @return true if code is for refund Transaction Type.
     */
    public static boolean isRefundTransactionType(TransactionTypeCode code) {
        return Arrays.asList(getRefundTypes()).contains(code);
    }

    /**
     * Determines if the code provided is an refund transaction type
     *
     * @param code Transaction type code
     * @return true if code is for refund Transaction Type.
     */
    public static boolean isRedebitTransactionType(TransactionTypeCode code) {
        return Arrays.asList(getRedebitTypes()).contains(code);
    }

    /**
     * Determines if the code provided is an Fee transaction type
     *
     * @param code Transaction type code
     * @return true if code is for Fee Transaction Type.
     */
    public static boolean isFeeTransactionType(TransactionTypeCode code) {
        DomainEntitySet<TransactionType> txnTypeList = findTransactionTypeByFeeIndicator(true);
        return Arrays.asList(build_TransactionTypeArray(txnTypeList)).contains(code);
    }

    /**
     * Obtains the transaction type object from the database based on the code
     *
     * @param pTransactionTypeCd Transaction Type code (such as EEDDCR)
     * @return
     */
    public static TransactionType findTransactionType(TransactionTypeCode pTransactionTypeCd) {
        return Application.findById(TransactionType.class, pTransactionTypeCd);
    }

    /**
     * Obtains the list of transaction types from the database based on the Transaction Category
     *
     * @param pTransactionCategory Transaction Category (such as Employee, Employer or Intuit)
     * @return DomainEntitySet<TransactionType>
     */
    public static DomainEntitySet<TransactionType> findTransactionTypeByTxnCategory(TransactionCategory pTransactionCategory) {
        DomainEntitySet<TransactionType> allTxnTypes = Application.findObjects(TransactionType.class);
        DomainEntitySet<TransactionType> txnTypes = new DomainEntitySet<TransactionType>();

        // Filtering
        for (TransactionType txnType : allTxnTypes) {
            if (txnType.getTransactionCategory() == pTransactionCategory) {
                txnTypes.add(txnType);
            }
        }

        // Sorting
        txnTypes = txnTypes.sort(TransactionTypeCd());

        return txnTypes;
    }

    /**
     * Obtains the list of transaction types from the database based on the Transaction Association Type
     *
     * @param pAssociationType Transaction Association Type (such as Reversal, Refund or Reissue)
     * @return DomainEntitySet<TransactionType>
     */
    public static DomainEntitySet<TransactionType> findTransactionTypeByAssociationType(TransactionAssociationType pAssociationType) {
        DomainEntitySet<TransactionType> allTxnTypes = Application.findObjects(TransactionType.class);
        DomainEntitySet<TransactionType> txnTypes = new DomainEntitySet<TransactionType>();

        // Filtering
        for (TransactionType txnType : allTxnTypes) {
            if (txnType.getAssociationType() == pAssociationType) {
                txnTypes.add(txnType);
            }
        }

        // Sorting
        txnTypes = txnTypes.sort(TransactionTypeCd());

        return txnTypes;
    }

    /**
     * Obtains the list of transaction types from the database based on the Transaction Association Type
     *
     * @param pAssociationType Transaction Association Type (such as Reversal, Refund or Reissue)
     * @param pService         Transaction is associated with this service (such as DirectDeposit or Tax)
     * @return DomainEntitySet<TransactionType>
     */
    public static DomainEntitySet<TransactionType> findTransactionTypeByAssociationTypeAndService(TransactionAssociationType pAssociationType, Service pService) {
        DomainEntitySet<TransactionType> allTxnTypes = Application.findObjects(TransactionType.class);
        DomainEntitySet<TransactionType> txnTypes = new DomainEntitySet<TransactionType>();

        // Filtering
        for (TransactionType txnType : allTxnTypes) {
            if (txnType.getAssociationType() == pAssociationType && txnType.getServiceCollection().contains(pService)) {
                txnTypes.add(txnType);
            }
        }

        // Sorting
        txnTypes = txnTypes.sort(TransactionTypeCd());

        return txnTypes;
    }

    /**
     * Obtains the list of transaction types from the database based on the Fee Indicator
     *
     * @param pFeeIndicator boolean
     * @return DomainEntitySet<TransactionType>
     */
    public static DomainEntitySet<TransactionType> findTransactionTypeByFeeIndicator(boolean pFeeIndicator) {
        DomainEntitySet<TransactionType> allTxnTypes = Application.findObjects(TransactionType.class);
        DomainEntitySet<TransactionType> txnTypes = new DomainEntitySet<TransactionType>();

        // Filtering
        for (TransactionType txnType : allTxnTypes) {
            if (txnType.getFeeInd() == pFeeIndicator) {
                txnTypes.add(txnType);
            }
        }

        // Sorting
        txnTypes = txnTypes.sort(TransactionTypeCd());

        return txnTypes;
    }

    /**
     * get transaction type from the database based on the Name
     *
     * @param pName String
     * @return TransactionType
     */
    public static TransactionType findTransactionTypeByName(String pName) {
        DomainEntitySet<TransactionType> txnTypesByNames = Application.findObjects(TransactionType.class).find(TransactionType.Name().equalTo(pName)).sort(TransactionType.TransactionTypeCd());

        return txnTypesByNames.getFirst();
    }

    /**
     * get unused FLA transaction type from the database
     *
     * @return TransactionType
     */
    public static TransactionType getUnusedFLATransactionType() {

        DomainEntitySet<TransactionType> txnTypesByNames = Application.findObjects(TransactionType.class).find(TransactionType.TransactionTypeCd().in(TEMP_FLA_TRANSACTION_TYPES)
                                                                        .And(TransactionType.Name().in(TEMP_FLA_TRANSACTION_NAMES))).sort(TransactionType.TransactionTypeCd());

        return txnTypesByNames.getFirst();
    }

    /**
     * Method to build the String Array for TransactionTypes.
     *
     * @param pTransactionTypeList DomainEntitySet<TransactionType>
     * @return String[]
     */
    public static TransactionTypeCode[] build_TransactionTypeArray(DomainEntitySet<TransactionType> pTransactionTypeList) {
        //String[] transactionTypeArray = new String[pTransactionTypeList.size()];
        TransactionTypeCode[] transactionTypeArray = new TransactionTypeCode[pTransactionTypeList.size()];
        int index = 0;

        for (TransactionType transactionType : pTransactionTypeList) {
            transactionTypeArray[index++] = transactionType.getTransactionTypeCd();
        }

        return transactionTypeArray;
    }

    /**
     * Determines if the given transaction type can be offloadable based on the current service status
     *
     * @param pTransactionTypeCode Transaction type code
     * @param pCompany             company
     * @param pSku                 sku
     * @return true if offloadable.
     */
    public static boolean isOffloadable(TransactionTypeCode pTransactionTypeCode, String pSku, Company pCompany, BillingDetail pBillingDetail) {
        TransactionType transactionType = Application.findById(TransactionType.class, pTransactionTypeCode);
        Collection<ServiceSubStatusCode> currentOnHoldReasonCodes = pCompany.getCurrentOnHoldReasonCodes();

        for (ServiceSubStatusCode onHoldReasonCd : currentOnHoldReasonCodes) {
            ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, onHoldReasonCd);
            if (isExcludedFromOffload(transactionType, pSku, serviceSubStatus, pBillingDetail)) {
                return false;
            }
        }
        return true;
    }

    /**
     * PSP-11809: Check if on Hold can be removed after billing details are associated to EmployerFeeDebit and ServiceSalesAndUseTax transactions
     * @param pTransactionType
     * @param pSku
     * @param pServiceSubStatus
     * @param pBillingDetail
     * @return
     */
    public static boolean isOffloadableFeeOnBillingAssociation(TransactionType pTransactionType, String pSku, ServiceSubStatus pServiceSubStatus, BillingDetail pBillingDetail) {
        SkuType skuType = null;
        if (pSku != null) {
            OfferingServiceCharge osc = OfferingServiceCharge.findBySKU(pSku);
            skuType = osc.getSkuType();
        }

        OfferingServiceChargeType chargeType = null;
        if (pBillingDetail != null) {
            chargeType = pBillingDetail.getOfferingServiceChargeType();
        }

        Criterion<ServStatTxnSkuType> where = ServStatTxnSkuType.TransactionType().equalTo(pTransactionType)
                .And(ServStatTxnSkuType.ServiceSubStatus().equalTo(pServiceSubStatus));

        if (skuType != null) {
            where = where.And(ServStatTxnSkuType.SkuType().equalTo(skuType));
        }

        if (chargeType != null && SkuType.NonPayroll.equals(skuType)) {
            where = where.And(ServStatTxnSkuType.OfferingServiceChargeType().equalTo(chargeType));
        }

        DomainEntitySet<ServStatTxnSkuType> offloadableTransactionTypes = Application.find(ServStatTxnSkuType.class, where);
        //Get Fee Types from which on hold needs to be removed after billing association
        String feeTypes = SystemParameter.findStringValue(SystemParameter.Code.REMOVE_ON_HOLD_FEE_TYPES);
        if (feeTypes != null && chargeType != null && skuType!=null && SkuType.NonPayroll.equals(skuType)) {
            for (String feeType : feeTypes.split(",")) {
                if (feeType.equals(chargeType.toString())) {
                    return offloadableTransactionTypes.size() <= 1;
                }
            }
        }

        return false;
    }

    public static boolean isExcludedFromOffload(TransactionType pTransactionTypeCode, String pSku,
                                                ServiceSubStatus pServiceSubStatus, BillingDetail pBillingDetail) {
        SkuType skuType = null;
        if (pSku != null) {
            OfferingServiceCharge osc = OfferingServiceCharge.findBySKU(pSku);
            skuType = osc.getSkuType();
        }

        OfferingServiceChargeType chargeType = null;
        if (pBillingDetail != null) {
            chargeType = pBillingDetail.getOfferingServiceChargeType();
        }

        return isExcludedFromOffload(pTransactionTypeCode, skuType, pServiceSubStatus, chargeType);
    }

    public static boolean isExcludedFromOffload(TransactionType pTransactionTypeCode, SkuType pSkuType,
                                                 ServiceSubStatus pServiceSubStatus, OfferingServiceChargeType pChargeType) {
        Criterion<ServStatTxnSkuType> where = ServStatTxnSkuType.TransactionType().equalTo(pTransactionTypeCode)
                .And(ServStatTxnSkuType.ServiceSubStatus().equalTo(pServiceSubStatus));

        if (pSkuType != null) {
            where = where.And(ServStatTxnSkuType.SkuType().equalTo(pSkuType));
        }

        if (pChargeType != null && SkuType.NonPayroll.equals(pSkuType)) {
            where = where.And(ServStatTxnSkuType.OfferingServiceChargeType().equalTo(pChargeType));
        }

        DomainEntitySet<ServStatTxnSkuType> excludedFromOffload = Application.find(ServStatTxnSkuType.class, where);

        return excludedFromOffload.size() > 0;
    }

    public static boolean isValidTypeToAddRedebit(TransactionTypeCode pTransactionTypeCode) {
/*
        String[] validTransactionTypes = {
            EmployerDdDebit.toString(),
            EmployerFeeDebit.toString(),
            EmployerFeeRedebit.toString(),
            ServiceSalesAndUseTax.toString(),
            ServiceSalesAndUseTaxRedebit.toString()
        };
        List<String> allowedTransactionTypes = new ArrayList<String>(Arrays.asList(validTransactionTypes));
         return allowedTransactionTypes.contains(pTransactionTypeCode.toString());
*/
        return isImpoundTransactionType(pTransactionTypeCode) || isRedebitTransactionType(pTransactionTypeCode)
                || pTransactionTypeCode.equals(TransactionTypeCode.EmployerFeeDebit)
                || pTransactionTypeCode.equals(TransactionTypeCode.ServiceSalesAndUseTax);
    }

    public static boolean isValidTypeToUpdateRedebit(TransactionTypeCode pTransactionTypeCode) {
        return isRedebitTransactionType(pTransactionTypeCode);
    }

    /**
     * Determines if the code provided is an Impound transaction type
     *
     * @param code Transaction type code
     * @return true if code is for Impound Transaction Type.
     */
    public static boolean isImpoundTransactionType(TransactionTypeCode code) {
        return Arrays.asList(ServiceStatus.getImpoundTypes()).contains(code);
    }


    public static boolean addsToPayment(TransactionTypeCode pTransactionTypeCode) {
        return ADDS_TO_PAYMENT_TRANSACTION_TYPES.contains(pTransactionTypeCode);
    }

    public static boolean subtractsFromPayment(TransactionTypeCode pTransactionTypeCode) {
        return SUBTRACTS_FROM_PAYMENT_TRANSACTION_TYPES.contains(pTransactionTypeCode); 
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public TransactionType()
	{
		super();
	}

}
