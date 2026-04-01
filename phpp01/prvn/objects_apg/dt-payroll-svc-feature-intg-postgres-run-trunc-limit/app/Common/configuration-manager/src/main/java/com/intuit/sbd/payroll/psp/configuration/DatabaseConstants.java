package com.intuit.sbd.payroll.psp.configuration;

public class DatabaseConstants {

    public static final String MonolithDbKey = "database.monolith";
    public static final String AuditDbKey = "database.audit";
    public static final String MonolithTokenPrefix = "SpcfDataAccessComponent-1-";
    public static final String HibernateTokenPrefix = "Hibernate-1-";
    public static final String AuditTokenPrefix = "SpcfDataAccessComponent-2-";
    public static final String DefaultMonolithDb = DatabaseType.ORACLE.toString();
    public static final String DefaultAuditDb = DatabaseType.ORACLE.toString();
}
