package com.intuit.sbd.payroll.psp.adapters.ade.cep.impl;

import com.intuit.ems.cep.api.AbstractGetService;
import com.intuit.ems.cep.api.ServiceParams;
import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

/**
 * Created with IntelliJ IDEA.
 * User: znorcross
 * Date: 4/23/14
 * Time: 9:55 AM
 */
public abstract class TransactionAwareAbstractGetService<T, S extends ServiceParams> extends AbstractGetService<T, S> {

    private static final SpcfLogger logger = PayrollServices.getLogger(TransactionAwareAbstractGetService.class);
    @Override
    protected ServiceResult validate() {
        boolean manageSession = !Application.hasActiveTransaction();

        try {
            if (manageSession) {
                Application.beginUnitOfWork(FlushMode.MANUAL, true);
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AgencyDataExchange);
            }

            return validateDelegate();

        } finally {
            if (manageSession) {
                Application.rollbackUnitOfWork();
            }
        }
    }

    @Override
    protected ServiceResult<T> execute() {
        boolean manageSession = !Application.hasActiveTransaction();

        try {
            if (manageSession) {
                Application.beginUnitOfWork(FlushMode.MANUAL, true);
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AgencyDataExchange);
            }

            return  executeDelegate();

        } finally {
            if (manageSession) {
                Application.rollbackUnitOfWork();
            }
        }
    }

    protected abstract ServiceResult validateDelegate();

    protected abstract ServiceResult<T> executeDelegate();
}
