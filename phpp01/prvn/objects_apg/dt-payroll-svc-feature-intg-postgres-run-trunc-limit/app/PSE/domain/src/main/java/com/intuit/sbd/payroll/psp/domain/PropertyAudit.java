package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.Map;
import java.util.HashMap;

/**
 * Hand-written business logic
 */
public class PropertyAudit extends BasePropertyAudit {
    private static final Map fieldNameToTableNameMap = new HashMap();
    private static final Map fieldNameToColumnNameMap = new HashMap();

    static {
        PropertyAudit.fieldNameToTableNameMap.put(
				PropertyAudit.FieldNames.COMPANY_DD_STATUS,
				PropertyAudit.TableNames.COMPANY_SERVICE);
		PropertyAudit.fieldNameToTableNameMap.put(
				PropertyAudit.FieldNames.COMPANY_LIMIT_AMT,
				PropertyAudit.TableNames.DD_COMPANY_SERVICE_INFO);
		PropertyAudit.fieldNameToTableNameMap.put(
				PropertyAudit.FieldNames.EMPLOYEE_LIMIT_AMT,
				PropertyAudit.TableNames.DD_COMPANY_SERVICE_INFO);
        PropertyAudit.fieldNameToTableNameMap.put(
				PropertyAudit.FieldNames.BP_COMPANY_LIMIT_AMT,
				PropertyAudit.TableNames.BP_COMPANY_SERVICE_INFO);
        PropertyAudit.fieldNameToTableNameMap.put(
				PropertyAudit.FieldNames.PAYEE_LIMIT_AMT,
				PropertyAudit.TableNames.BP_COMPANY_SERVICE_INFO);
		PropertyAudit.fieldNameToTableNameMap.put(
				PropertyAudit.FieldNames.FUNDING_MODEL_CD,
				PropertyAudit.TableNames.COMPANY);
        PropertyAudit.fieldNameToTableNameMap.put(
				PropertyAudit.FieldNames.NOTIFICATION_EMAIL_ADDRESS,
				PropertyAudit.TableNames.COMPANY);
	}

    static {
		PropertyAudit.fieldNameToColumnNameMap.put(
				PropertyAudit.FieldNames.COMPANY_DD_STATUS,
				PropertyAudit.ColumnNames.DD_COMP_STATUS_CD);
		PropertyAudit.fieldNameToColumnNameMap.put(
				PropertyAudit.FieldNames.COMPANY_LIMIT_AMT,
				PropertyAudit.ColumnNames.OVERRIDE_COMP_LIMIT_AMT);
		PropertyAudit.fieldNameToColumnNameMap.put(
				PropertyAudit.FieldNames.EMPLOYEE_LIMIT_AMT,
				PropertyAudit.ColumnNames.OVERRIDE_EMP_LIMIT_AMT);
        PropertyAudit.fieldNameToColumnNameMap.put(
				PropertyAudit.FieldNames.BP_COMPANY_LIMIT_AMT,
				PropertyAudit.ColumnNames.OVERRIDE_COMP_LIMIT_AMT);
		PropertyAudit.fieldNameToColumnNameMap.put(
				PropertyAudit.FieldNames.PAYEE_LIMIT_AMT,
				PropertyAudit.ColumnNames.OVERRIDE_PAYEE_LIMIT_AMT);
		PropertyAudit.fieldNameToColumnNameMap.put(
				PropertyAudit.FieldNames.FUNDING_MODEL_CD,
				PropertyAudit.ColumnNames.FUNDING_MODEL_CD);
		PropertyAudit.fieldNameToColumnNameMap.put(
				PropertyAudit.FieldNames.NOTIFICATION_EMAIL_ADDRESS,
				PropertyAudit.ColumnNames.COMMUNICATION_VALUE);
	}

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static DomainEntitySet<PropertyAudit> findCompanyBankAccountPropertyAudits(Company pCompany,
                                                               String pSourceCompanyBankAccountId) {

        String[] paramNames = new String[2];
        paramNames[0] = "company";
        paramNames[1] = "sourceCBAId";

        Object[] paramValues = new Object[2];
        paramValues[0] = pCompany;
        paramValues[1] = pSourceCompanyBankAccountId;

        return Application.findByNamedQueryUsingCache(PropertyAudit.class, "findCBAStatusAuditsByCompanyAndCBAId", paramNames, paramValues);

    }

    public static DomainEntitySet<PropertyAudit> findPropertyAudits(Company pCompany,
                                                                            String pTableName,
                                                                            String pColumnName,
                                                                            SpcfCalendar pFromDate) {

        return findPropertyAudits(pCompany, pTableName, pColumnName, null, pFromDate);
    }

    public static DomainEntitySet<PropertyAudit> findPropertyAudits(Company pCompany,
                                                             String pTableName,
                                                             String pColumnName,
                                                             String pObjectId,
                                                             SpcfCalendar pFromDate) {

        Criterion<PropertyAudit> where =
                PropertyAudit.Company().equalTo(pCompany)
                .And(PropertyAudit.ClassName().equalTo(pTableName))
                .And(PropertyAudit.PropertyName().equalTo(pColumnName));

        if (pObjectId != null) {
            where = where.And(PropertyAudit.ObjectIdentifier().equalTo(pObjectId));
        }

        if (pFromDate != null) {
            where = where.And(PropertyAudit.AuditDate().greaterOrEqualThan(pFromDate));
        }

        return Application.find(PropertyAudit.class, new Query<PropertyAudit>().Where(where).OrderBy(PropertyAudit.AuditDate().Descending()));

    }

    public static DomainEntitySet<PropertyAudit> findPropertyAudits(Company pCompany, String pFieldName, SpcfCalendar pFromDate) {
        String tableName = (String) PropertyAudit.getFieldNameToTableNameMap().get(pFieldName);
        String columnName = (String) PropertyAudit.getFieldNameToColumnNameMap().get(pFieldName);

        return findPropertyAudits(pCompany, tableName, columnName, pFromDate);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Static create/update
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static PropertyAudit createPropertyAudit(String className, Company company, String propertyName, String oldValue, String newValue, String userId, String objectIdentifier){
        PropertyAudit pa = new PropertyAudit();
        pa.setAuditDate(PSPDate.getPSPTime());
        pa.setClassName(className);
        pa.setCompany(company);
        pa.setOldPropertyValue(oldValue);
        pa.setNewPropertyValue(newValue);
        pa.setPropertyName(propertyName);
        pa.setUserId(userId);
        pa.setObjectIdentifier(objectIdentifier);
        return Application.save(pa);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public PropertyAudit()
	{
		super();
	}

    public static Map getFieldNameToTableNameMap() {
        return fieldNameToTableNameMap;
    }

    public static Map getFieldNameToColumnNameMap() {
        return fieldNameToColumnNameMap;
    }

    /**
	 *
	 */
	public interface TableNames {
		public static final String COMPANY = "Company";
		public static final String COMP_BANK_ACCT = "CompanyBankAccount";
        public static final String COMPANY_SERVICE = "CompanyService";
//		public static final String COMP_COMM_ASSOC = "COMP_COMM_ASSOC";
		public static final String DD_COMPANY_SERVICE_INFO = "DDCompanyServiceInfo";
        public static final String BP_COMPANY_SERVICE_INFO = "BPCompanyServiceInfo";
	}

    /**
	 *
	 */
	public interface ColumnNames {
		public static final String COMPANY_GSEQ = "COMPANY_GSEQ";
		public static final String COMMUNICATION_VALUE = "NotificationEmail";
		public static final String COMP_BANK_ACCT_UNIQUE_ID = "COMP_BANK_ACCT_UNIQUE_ID";
//		public static final String COMP_COMM_ASSOC_GSEQ = "COMP_COMM_ASSOC_GSEQ";
		public static final String DD_COMP_STATUS_CD = "StatusCd";
		public static final String FUNDING_MODEL_CD = "FundingModel";
		public static final String OVERRIDE_PAYEE_LIMIT_AMT = "OverridePayeeLimitAmount";
        public static final String OVERRIDE_COMP_LIMIT_AMT = "OverrideCompanyLimitAmount";
		public static final String OVERRIDE_EMP_LIMIT_AMT = "OverrideEmployeeLimitAmount";
		public static final String STATUS_CD = "StatusCd";
		public static final String STATUS_EFF_DATE = "StatusEffectiveDate";
	}

    /**
	 *
	 */
	public interface FieldNames {
		public static final String FUNDING_MODEL_CD = "FundingModelCd";

		public static final String EMPLOYEE_LIMIT_AMT = "EmployeeLimitAmt";

		public static final String COMPANY_LIMIT_AMT = "CompanyLimitAmt";

		public static final String NOTIFICATION_EMAIL_ADDRESS = "NotificationEmailAddress";

		public static final String COMPANY_DD_STATUS = "CompanyDDStatus";

        public static final String BP_COMPANY_LIMIT_AMT = "BPCompanyLimitAmt";

        public static final String PAYEE_LIMIT_AMT = "PayeeLimitAmt";
	}
}
