package com.intuit.sbd.payroll.psp.batchjobs.entity;

public enum NamedQueryEnum {
    FIND_COMPANIES_FOR_INITIAL_LOAD("findCompaniesForInitialLoad"),
    FIND_ACTIVE_COMPANIES("findActiveCompanies"),
    FIND_ACTIVE_COMPANIES_WITH_REALM("findActiveCompaniesHavingRealm"),
    FIND_INACTIVE_COMPANIES_WITH_REALM("findInActiveCompaniesHavingRealm");

    private String namedQuery;

    NamedQueryEnum(String namedQuery) {
        this.namedQuery = namedQuery;
    }

    public String value() {
        return this.namedQuery;
    }

    @Override
    public String toString() {
        return this.value();
    }

    public static NamedQueryEnum fromValue(String value) {
        for (NamedQueryEnum f : values()) {
            if (f.value().equalsIgnoreCase(value)) {
                return f;
            }
        }
        return null;
    }
}
