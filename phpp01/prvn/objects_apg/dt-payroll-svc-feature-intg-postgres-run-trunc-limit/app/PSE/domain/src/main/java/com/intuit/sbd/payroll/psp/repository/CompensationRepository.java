package com.intuit.sbd.payroll.psp.repository;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.Compensation;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
public class CompensationRepository {

    public DomainEntitySet<Compensation> getCompensationsForPaychecks(DomainEntitySet<Paycheck> paychecks) {
        DomainEntitySet<Compensation> compensations = new DomainEntitySet<>();
        for(Paycheck paycheck: paychecks) {
            DomainEntitySet<Compensation> paycheckCompensationSet = paycheck.getCompensationCollection();
            compensations.addAll(paycheckCompensationSet);
        }

        return compensations;
    }
}
