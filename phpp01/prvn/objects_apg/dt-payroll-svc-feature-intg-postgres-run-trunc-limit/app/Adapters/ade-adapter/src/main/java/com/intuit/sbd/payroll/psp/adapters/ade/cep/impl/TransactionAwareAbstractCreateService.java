package com.intuit.sbd.payroll.psp.adapters.ade.cep.impl;

import com.intuit.ems.cep.api.AbstractCreateService;
import com.intuit.ems.cep.api.ServiceParams;
import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import org.hibernate.FlushMode;

/**
 * Created with IntelliJ IDEA.
 * User: znorcross
 * Date: 4/23/14
 * Time: 2:03 PM
 */
public abstract class TransactionAwareAbstractCreateService<T, S extends ServiceParams> extends AbstractCreateService<T, S> {

    @Override
    protected ServiceResult validate() {
        boolean manageSession = !Application.hasActiveTransaction();

        try {
            if (manageSession) {
                Application.beginUnitOfWork(FlushMode.MANUAL);
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AgencyDataExchange);
            }


            ServiceResult serviceResult = validateDelegate();

            if (serviceResult.isSuccess() && manageSession) {
                Application.commitUnitOfWork();
            }

            return serviceResult;
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
                Application.beginUnitOfWork(FlushMode.MANUAL);
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AgencyDataExchange);
            }

            ServiceResult<T> serviceResult = executeDelegate();

            if (serviceResult.isSuccess() && manageSession) {
                Application.commitUnitOfWork();
            }

            return serviceResult;
        } finally {
            if (manageSession) {
                Application.rollbackUnitOfWork();
            }
        }
    }

    protected abstract ServiceResult validateDelegate();

    protected abstract ServiceResult<T> executeDelegate();
}
