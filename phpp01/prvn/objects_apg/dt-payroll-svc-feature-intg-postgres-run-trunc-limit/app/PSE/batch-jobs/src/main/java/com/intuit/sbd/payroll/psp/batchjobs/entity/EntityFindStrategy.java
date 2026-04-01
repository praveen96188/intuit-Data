package com.intuit.sbd.payroll.psp.batchjobs.entity;

import com.intuit.sbd.payroll.psp.Application;
import java.util.List;

public interface EntityFindStrategy<T> {
    default List<T> getEntityList() {
        List<T> entityList;
        try {
            Application.beginUnitOfWork();
            entityList = getCompanyList();
            Application.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }
        return entityList;
    }

    List<T> getCompanyList();
}