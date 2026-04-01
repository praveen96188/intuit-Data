package com.intuit.sbd.payroll.psp.tools;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction;
import com.intuit.sbd.payroll.psp.domain.TaxPaymentStatus;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.hibernate.FlushMode;

/**
 * User: dweinberg
 * Date: 12/20/11
 * Time: 11:00 AM
 */
public class MergeTaxPayments {

    private SpcfUniqueId[] mmtIds;

    public static void main(String[] args) {
        if (args.length < 2) {
            throw new RuntimeException("usage: MergeTaxPayments MMT_ID MMT_ID [MMT_ID...]");
        }

        SpcfUniqueId[] mmtIds = new SpcfUniqueId[args.length];
        int i = 0;
        for (String mmtIdString : args) {
            mmtIds[i++] = SpcfUniqueId.createInstance(mmtIdString);
        }

        MergeTaxPayments mtp = new MergeTaxPayments(mmtIds);

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            mtp.merge();
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

    }

    public MergeTaxPayments(SpcfUniqueId[] mmtIds) {
        this.mmtIds = mmtIds;
    }

    public void merge() {
        DomainEntitySet<MoneyMovementTransaction> mmtSet = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.<MoneyMovementTransaction>Id().in(mmtIds));

        if (mmtSet.size() < 2) {
            throw new RuntimeException("Less than 2 MMTs were present");
        }

        MoneyMovementTransaction firstMmt = mmtSet.getFirst();
        for (MoneyMovementTransaction mmt : mmtSet) {

            if (mmt.getTaxPaymentStatus() != TaxPaymentStatus.ReadyToSend) {
                throw new RuntimeException("MMT is not ReadyToSend");
            }

            if (mmt != firstMmt) {
                //verify relevant properties are the same
                if (!mmt.getCompany().equals(firstMmt.getCompany())
                        || !mmt.getPaymentTemplate().equals(firstMmt.getPaymentTemplate())
                        || !mmt.getDueDate().equals(firstMmt.getDueDate())
                        || !mmt.getInitiationDate().equals(firstMmt.getInitiationDate())
                        || !mmt.getPaymentPeriodBegin().equals(firstMmt.getPaymentPeriodBegin())
                        || !mmt.getPaymentPeriodEnd().equals(firstMmt.getPaymentPeriodEnd())) {
                    throw new RuntimeException("MMTs had different properties and could not be combined");
                }

                firstMmt.combinePayment(mmt);
            }
        }

    }
}
