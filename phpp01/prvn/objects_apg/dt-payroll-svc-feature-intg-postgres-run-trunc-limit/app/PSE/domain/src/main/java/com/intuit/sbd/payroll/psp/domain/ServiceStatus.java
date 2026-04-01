package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

/**
 * Hand-written business logic
 */
public class ServiceStatus extends BaseServiceStatus {// Valid transaction types per each credit-debit combination of bank account owner types and settlement type

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Verifies whether the specified service status code can be associated with the source system.
     *
     * @param pServiceSubStatusCd
     * @param pSourceSystemCode
     * @return
     */
    public static boolean isAllowedServiceStatus(ServiceSubStatusCode pServiceSubStatusCd, SourceSystemCode pSourceSystemCode) {
        SourceSystem sourceSystem = Application.findById(SourceSystem.class, pSourceSystemCode);
        ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, pServiceSubStatusCd);
        return serviceSubStatus.getSourceSystemCollection().contains(sourceSystem);
    }

    /**
     * Verifies whether the specified service and service status can be associated.
     *
     * @param pServiceSubStatusCd
     * @param pServiceCode
     * @return
     */
    public static boolean isAllowedServiceStatus(ServiceSubStatusCode pServiceSubStatusCd, ServiceCode pServiceCode) {
        Service service = Application.findById(Service.class, pServiceCode);
        ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, pServiceSubStatusCd);
        return serviceSubStatus.getServiceCollection().contains(service);
    }

    public static TransactionTypeCode[] getImpoundTypes() {
        DomainEntitySet<TransactionType> txnTypeList = TransactionType.findTransactionTypeByAssociationType(TransactionAssociationType.Impound);
        return TransactionType.build_TransactionTypeArray(txnTypeList);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public ServiceStatus() {
        super();
    }

}