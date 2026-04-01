package com.intuit.sbd.payroll.psp.adapters.ade.cep.impl;

import com.intuit.ems.cep.api.AbstractGetListService;
import com.intuit.ems.cep.api.ServiceParams;
import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import org.hibernate.FlushMode;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: znorcross
 * Date: 4/23/14
 * Time: 10:37 AM
 */
public abstract class TransactionAwareAbstractGetListService<T, S extends ServiceParams> extends AbstractGetListService<T, S> {
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
    protected ServiceResult<List<T>> execute() {
        boolean manageSession = !Application.hasActiveTransaction();

        try {
            if (manageSession) {
                Application.beginUnitOfWork(FlushMode.MANUAL, true);
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AgencyDataExchange);
            }

            return executeDelegate();

        } finally {
            if (manageSession) {
                Application.rollbackUnitOfWork();
            }
        }
    }

    protected abstract ServiceResult validateDelegate();

    protected abstract ServiceResult<List<T>> executeDelegate();
}
