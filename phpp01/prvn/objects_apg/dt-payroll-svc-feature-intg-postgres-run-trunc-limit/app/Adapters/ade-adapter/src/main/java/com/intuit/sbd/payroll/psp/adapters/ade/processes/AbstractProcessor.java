package com.intuit.sbd.payroll.psp.adapters.ade.processes;

/**
 * Created with IntelliJ IDEA.
 * User: shivanandad069
 * Date: 10/2/13
 * Time: 2:17 AM
 * To change this template use File | Settings | File Templates.
 */

import com.intuit.ems.cep.api.ServiceResult;

public abstract class AbstractProcessor<T> {

    public ServiceResult<T> execute() {
        ServiceResult<T> serviceResult = validate();
        if(serviceResult.notSuccess()) {
            return serviceResult;
        }

        ServiceResult<T> processServicesResult = process();
        processServicesResult.merge(serviceResult);
        return processServicesResult;
    }

    public abstract ServiceResult<T> validate();
    public abstract ServiceResult<T> process();
}