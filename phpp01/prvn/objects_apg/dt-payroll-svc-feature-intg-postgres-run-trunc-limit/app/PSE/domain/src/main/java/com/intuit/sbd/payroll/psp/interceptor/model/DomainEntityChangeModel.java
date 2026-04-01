package com.intuit.sbd.payroll.psp.interceptor.model;

import com.intuit.sbd.payroll.psp.DomainEntity;
import lombok.Getter;

import java.util.Map;

@Getter
public class DomainEntityChangeModel {
    private Class<?> clazz;

    private DomainEntity domainEntity;

    public DomainEntityChangeModel(Class<?> clazz, DomainEntity domainEntity) {
        this.clazz = clazz;
        this.domainEntity = domainEntity;
    }
}
