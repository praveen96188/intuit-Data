package com.intuit.sbd.payroll.psp.filter.filtervalidator;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class FilterValidatorCache {
    public Set<Integer> sqlAndWorkflowHashSet;

    public FilterValidatorCache() {
        sqlAndWorkflowHashSet = new HashSet<>();
    }

    public void addToCache(int sqlAndWorkflowHash) {
        sqlAndWorkflowHashSet.add(sqlAndWorkflowHash);
    }

    public boolean contains(int sqlAndWorkflowHash) {
        return sqlAndWorkflowHashSet.contains(sqlAndWorkflowHash);
    }

    public void deleteFromCache(int sqlAndWorkflowHash) {
        sqlAndWorkflowHashSet.remove(sqlAndWorkflowHash);
    }

}
