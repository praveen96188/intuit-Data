package com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.dtos;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlEnum;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jan 11, 2010
 * Time: 2:38:54 PM
 *
 * Copied from com.intuit.sbd.payroll.psp.domain.OfferingServiceChargeType
 */
@XmlType(name = "FeeTypeEnum")
@XmlEnum()
public enum FeeTypeEnum {
    PerPayroll,

    PerPaycheck,

    PerTransmission,

    ReversalFee,

    DebitReturnFee,

    ManualServicingFee,

    ChaseReportFeeUpTo3Payrolls,

    ChaseReportFeeUpTo6Payrolls,

    ChaseReportFeeUpTo15Payrolls,

    ChaseReportFeeUpTo20Payrolls,

    ChaseReportFeeOver20Payrolls,

    BankVerificationDebit,

    PaymentArrangementFee,

    FundsNotRecovered
}
