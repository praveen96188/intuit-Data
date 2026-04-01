package com.intuit.sbd.payroll.psp.interceptor.constants;

public class InterceptorConstant {

    public static final String WHERE = " where ";
    public static final String CREATED_DATE = "CreatedDate";
    public static final String SELECT_QUERY_TEMPLATE = "select";
    public static final String UPDATE_QUERY_TEMPLATE = "update %s";
    public static final String DELETE_QUERY_TEMPLATE = "delete from %s";
    public static final String DATE_LITERAL = " created_date >= date '%s' and ";
    public static final String LICENSE_NUMBER_LITERAL = " license_number = '%s' and ";
    public static final String COMPANY_FK_LITERAL = " company_fk = '%s' and ";
    public static final String CREATED_DATE_SQL = "created_date";
    public static final String COMPANY_FK_SQL = "company_fk";
    public static final String LICENSE_NUMBER_SQL = "license_number";
    //Refactor to map in case more use cae comes up
    public static final String SST_CLASS_NAME = "com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission";
    public static final String SST_TABLE_NAME = "PSP_SOURCE_SYSTEM_TRANSMISSION";
    //REGEX_TEMPLATE is for findById Queries, it checks for an optional query comment and then for a select query only on SEQ
    public static final String REGEX_TEMPLATE = "(\\/\\*.*\\*\\/ )?select .* from %s ([^\\s]+) where ([^.|^\\s]+).%s=\\?";


}
