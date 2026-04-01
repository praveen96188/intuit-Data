package com.intuit.sbd.payroll.psp.adapters.ade.cep.impl;

import com.intuit.ems.cep.api.AbstractUpdateService;
import com.intuit.ems.cep.api.ServiceParams;
import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import org.hibernate.FlushMode;

/**
 * Created with IntelliJ IDEA.
 * User: znorcross
 * Date: 4/23/14
 * Time: 2:05 PM
 */
public abstract class TransactionAwareAbstractUpdateService<T, S extends ServiceParams> extends AbstractUpdateService<T, S> {
    protected PSPRequestContextManager pspRequestContextManager;

    public TransactionAwareAbstractUpdateService(){
        pspRequestContextManager= PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }
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

    @SuppressWarnings("unchecked")
    @Override
    protected ServiceResult<T> execute() {
        boolean manageSession = !Application.hasActiveTransaction();

        try {
            ServiceResult<T> serviceResult = new ServiceResult<T>();
            if (manageSession) {
                Application.beginUnitOfWork(FlushMode.MANUAL);
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AgencyDataExchange);
            }

           serviceResult.merge(executeDelegate());

            if (serviceResult.isSuccess() && manageSession) {
                Application.commitUnitOfWork();
                // fetch method has to use get service to fetch the updated data, Get service will open new session
                serviceResult.setResult(refreshEntity());
            }

            return serviceResult;
        } finally {
            if (manageSession) {
                Application.rollbackUnitOfWork();
            }
        }
    }

    protected abstract ServiceResult validateDelegate();

    protected abstract ServiceResult executeDelegate();

    protected abstract T refreshEntity();
}
