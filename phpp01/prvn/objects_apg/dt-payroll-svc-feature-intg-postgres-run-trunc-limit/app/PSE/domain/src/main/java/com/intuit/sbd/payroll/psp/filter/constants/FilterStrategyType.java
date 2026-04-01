package com.intuit.sbd.payroll.psp.filter.constants;

public enum FilterStrategyType {
    SESSION_FILTER, // Hibernate Filter
    DATE_SESSION_FILTER,
    HIBERNATE_CRITERIA, // Adding Restrictions to Hibernate Criteria
    SQL_STATEMENT_INSPECTOR, // Manually making changes to the Prepared Statement for findById/Lazy Load Queries
    DATE_SQL_STATEMENT_INSPECTOR,
    SQL_STATEMENT_INSPECTOR_DATA_MANIPULATION_QUERY, // Manually making changes to the Prepared Statement for Update/Delete queries
    DATE_SQL_STATEMENT_INSPECTOR_DATA_MANIPULATION_QUERY,
    LICENSE_NUMBER_SQL_STATEMENT_INSPECTOR_DATA_MANIPULATION_QUERY
}
