package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Property;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.query.ScalarProperty;
import com.intuit.sbd.payroll.psp.query.SortableProperty;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.VMPEmployeePaginationDetails;
import com.intuit.sbd.payroll.psp.workflows.processflag.ProcessFlagWorkflowPackager;
import com.intuit.sbd.payroll.psp.workflows.processflag.ProcessFlagWorkflowState;
import com.intuit.sbd.payroll.psp.workflows.processflag.employee.EmployeeProcessFlagWorkflows;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.PublishStatusWorkflowPackager;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.PublishStatusWorkflowState;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.employee.EmployeePublishStatusWorkflows;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portabilitySpecific.SpcfUniqueIdImpl;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.SQLQuery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Hand-written business logic
 */
public class Employee extends BaseEmployee implements IUpdatable {
    // optimization for balance files when all employees will be in memory
    public static final String ALL_EMPLOYEES_IN_MEMORY_CACHE_KEY = "EmployeesInMemory";
    private static final Pattern ALL_DIGITS = Pattern.compile("^[0-9]+$");
    public static final String DEFAULT_SSN = "000000000";
    public static String TaxIdKeyName="Employee_TaxId";
    public static String BirthDateKeyName="Employee_BirthDate";
    private PublishStatusWorkflowPackager publishStatusWorkflowPackager = null;
    private ProcessFlagWorkflowPackager processFlagWorkflowPackager = null;
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static Employee findEmployee(Company pCompany, String pSourceEmployeeId) {
        Employee foundEmployee = null;

        NaturalKey naturalKey = new NaturalKey(Employee.class, pCompany.getId(), pSourceEmployeeId);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            foundEmployee = Application.findById(Employee.class, primaryKey);
        } else {

            // Optimization for balance files. All of the employees will be in memory, so there is no need to go to the database.
            Boolean allEmployeesInMemory = Application.getSessionCache().getNonHibernateObject(ALL_EMPLOYEES_IN_MEMORY_CACHE_KEY + ":" + pCompany.getId());
            if(allEmployeesInMemory != null && allEmployeesInMemory) {
                return foundEmployee;
            }

            DomainEntitySet<Employee> employees =
                    Application.find(Employee.class,
                            new Query<Employee>().Where(Employee.Company().equalTo(pCompany)
                                    .And(Employee.SourceEmployeeId().equalTo(pSourceEmployeeId)))
                                    .EagerLoad(Employee.QbdtEmployeeInfo()));

            if (employees.size() > 1) {
                throw new RuntimeException(
                        "Query for employee by company " + pCompany + " and source employee id " + pSourceEmployeeId + " did not return 0 or 1 results as expected");
            }

            if (!employees.isEmpty()) {
                foundEmployee = employees.get(0);
                foundEmployee.cache();
            }
        }
        return foundEmployee;
    }
    // Collects active employees who has not deleted from QBDT and not de-activated.
    public static DomainEntitySet<Employee> findActiveEmployees(Company pCompany) {

        Criterion<Employee> where = Employee.Company().equalTo(pCompany).
                And(Employee.QbdtEmployeeInfo().IsDeleted().equalTo(false)
                        .And(Employee.StatusCd().equalTo(EmployeeStatus.Active)));

        Expression<Employee> query = new Query<Employee>().Where(where);

        return Application.find(Employee.class, query);
    }

    public static Employee findEmployeeByQBListId(Company pCompany, String pListId) {
        // Optimization for balance files. All of the employees will be in memory, so there is no need to go to the database.
        Boolean allEmployeesInMemory = Application.getSessionCache().getNonHibernateObject(ALL_EMPLOYEES_IN_MEMORY_CACHE_KEY + ":" + pCompany.getId());
        if(allEmployeesInMemory != null && allEmployeesInMemory) {
            return null;
        }

        Employee foundEmployee = null;

        DomainEntitySet<Employee> employees =
                Application.find(Employee.class,
                        Employee.Company().equalTo(pCompany)
                                .And(Employee.QbdtEmployeeInfo().ListId().equalTo(pListId)));

        if (employees.size() > 1) {
            throw new RuntimeException(
                    "Query for employee by company " + pCompany + " and source employee id " + pListId + " did not return 0 or 1 results as expected");
        }

        if (!employees.isEmpty()) {
            foundEmployee = employees.get(0);
            foundEmployee.cache();
        }

        return foundEmployee;
    }

    public static void eagerlyLoadEmployees(Company pCompany, List<String> pSourceEmployeeIds) {
        Property[] eagerLoadProperties = new Property[] {
                Employee.MailingAddress(),
                Employee.QbdtEmployeeInfo(),
                Employee.EmployeeAccrualSet(),
                Employee.EmployeeBankAccountSet()
        };

        Criterion<Employee> where = Employee.Company().equalTo(pCompany);
        if(pSourceEmployeeIds != null && pSourceEmployeeIds.size() > 0) {
            SpcfUniqueId companyId = pCompany.getId();
            // if the employees are already cached don't re-fetch them
            for (Iterator<String> iterator = pSourceEmployeeIds.iterator(); iterator.hasNext(); ) {
                String sourceEmployeeId = iterator.next();
                NaturalKey naturalKey = new NaturalKey(Employee.class, companyId, sourceEmployeeId);
                SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);
                if(primaryKey != null) {
                    iterator.remove();
                }
            }

            if(pSourceEmployeeIds.size() > 0) {
                where = where.And(Employee.SourceEmployeeId().in(pSourceEmployeeIds.toArray(new String[pSourceEmployeeIds.size()])));
            }
        } else {
            // load all non deleted and active employees
            where = where.And(Employee.QbdtEmployeeInfo().IsDeleted().equalTo(false)
                    .And(Employee.StatusCd().equalTo(EmployeeStatus.Active)));
        }


        // eager load employee
        @SuppressWarnings("unchecked")
        Expression<Employee> employeeQuery =
                new Query<Employee>()
                        .Where(where)
                        .EagerLoad(eagerLoadProperties);

        for (Employee employee : Application.<Employee>find(Employee.class, employeeQuery)) {
            employee.cache();
        }
    }

    @Deprecated
    public static void eagerlyLoadEmployeesAndAssociatedEntities(Company pCompany) {
        eagerlyLoadEmployees(pCompany, null);
    }

    @Deprecated
    public static void eagerlyLoadEmployeesAndAssociatedEntities(Company pCompany, List<String> pSourceEmployeeIds) {
        eagerlyLoadEmployees(pCompany, pSourceEmployeeIds);
    }

    public static DomainEntitySet<Employee> findEmployees(Company pCompany) {
        Expression<Employee> query =
                new Query<Employee>()
                        .Where(Employee.Company().equalTo(pCompany))
                        .OrderBy(Employee.StatusEffectiveDate());

        return Application.find(Employee.class, query);
    }

    public static DomainEntitySet<Employee> findCloudEmployees(Company pCompany) {
        Criterion<Employee> where = Employee.Company().equalTo(pCompany)
                .And(Employee.QbdtEmployeeInfo().IsAssisted().equalTo(true));

        Expression<Employee> query =
                new Query<Employee>()
                        .Where(where)
                        .OrderBy(Employee.StatusEffectiveDate());

        return Application.find(Employee.class, query);
    }

    public static DomainEntitySet<Employee> findEmployeesBySourceSystemAndService(SourceSystemCode pSourceSystemcode, ServiceCode pServiceCode){
        String[] paramNames = new String[2];
        paramNames[0] = "sourceSystemCd";
        paramNames[1] = "serviceCd";

        Object[] paramValues = new Object[2];
        paramValues[0] = pSourceSystemcode;
        paramValues[1] = pServiceCode;

        DomainEntitySet<Employee> retList =
                Application.findByNamedQuery("findEmployeesBySourceSystemAndService", paramNames, paramValues);

        return retList;
    }

    public static List<SpcfUniqueId> findWorkforceEligibleEmployees(String companySeq, int lastPaidDurationEmployee,
                                                                    int settlementDateDuration, boolean isDDEmployee) {
        lastPaidDurationEmployee = - lastPaidDurationEmployee;
        SpcfCalendar fromDate = PSPDate.getPSPTime();
        fromDate.addDays(lastPaidDurationEmployee);

        settlementDateDuration = - settlementDateDuration;
        SpcfCalendar settlementFromDate = PSPDate.getPSPTime();
        settlementFromDate.addDays(settlementDateDuration);

        SpcfCalendar settlementToDate = PSPDate.getPSPTime();

        String[] paramNames = new String[5];
        paramNames[0] = "companySeq";
        paramNames[1] = "fromDate";
        paramNames[2] = "settlementFromDate";
        paramNames[3] = "settlementToDate";
        paramNames[4] = "isDD";

        Object[] paramValues = new Object[5];
        paramValues[0] = companySeq;
        paramValues[1] = fromDate;
        paramValues[2] = settlementFromDate;
        paramValues[3] = settlementToDate;
        paramValues[4] = isDDEmployee;

        return Application.executeNamedQuery("findEligibleEmployeesForWorkforceInvitations", paramNames, paramValues);
    }

    public static List<Employee> findActiveEmployeesByCompanyPSID(String psId) {
        Criterion<Employee> employeeCriterion = Employee.Company().SourceCompanyId().equalTo(psId)
                .And(Employee.StatusCd().in(EmployeeStatus.Active));
        return Application.executeQuery(Employee.class, employeeCriterion);
    }

    /**
     * SQL query to get employees eligible for re-invite
     * @param isDDQuery  should employee have been paid via DD
     * @param shouldReEngageERInvited should we get employees previously invited by ER (if false we return employees previously invited via batch job)
     * @return list of eligible employees
     * @return
     */
    public static String getSQLQueryStringForWorkforceReInvite(boolean isDDQuery, boolean shouldReEngageERInvited) {

        StringBuilder stringBuilder = new StringBuilder();
        String queryTop = "with comp_inv_table as (\n" +
                "    select max(case when pced.EVENT_DETAIL_TYPE_CD = 'EmployeeId' then pced.VALUE end) employeeSeq,\n" +
                "           max(case when (pced.EVENT_DETAIL_TYPE_CD = 'InvitationSource'\n" +
                "               and regexp_like(pced.VALUE, 'BulkWorkforceInviteProcessor')) then 'Bulk'\n" +
                "                    when (pced.EVENT_DETAIL_TYPE_CD = 'InvitationSource' and pced.VALUE='PayrollAPI') then 'ER' end) invSource,\n" +
                "           max(case when (pced.EVENT_DETAIL_TYPE_CD = 'InvitationSource'\n" +
                "               and regexp_like(pced.VALUE, 'BulkWorkforceInviteProcessor')) then pce.EVENT_TIME_STAMP end) bulkInvDate,\n" +
                "           max(case when (pced.EVENT_DETAIL_TYPE_CD='InvitationSource' and pced.VALUE='PayrollAPI') then pce.EVENT_TIME_STAMP end ) ERinviteDate\n" +
                "    from PSP_COMPANY_EVENT pce\n" +
                "             join PSP_COMPANY_EVENT_DETAIL pced on pce.COMPANY_EVENT_SEQ = pced.COMPANY_EVENT_FK\n" +
                "    where pce.COMPANY_FK = :companySeq\n" +
                "      and pced.COMPANY_FK = :companySeq\n"+
                "      and pce.EVENT_TYPE_CD = 'EmployeeInvited'\n" +
                "    group by pce.COMPANY_EVENT_SEQ),\n" +
                "     emp_inv_table as (\n" +
                "         select comp_inv_table.employeeSeq,\n" +
                "                max(case when invSource='ER' then ERinviteDate end) latestAutoInviteDate,\n" +
                "                max(case when invSource='Bulk' then bulkInvDate end) latestBulkInvDate,\n" +
                "                max(case when invSource='Bulk' then bulkInvDate\n" +
                "                         when invSource='ER' then ERinviteDate end) latestInvDate,\n" +
                "                sum(case when invSource='ER' then 1 else 0 end) totalAutoInvites,\n" +
                "                sum(case when invSource='Bulk' then 1 else 0 end) totalBulkINvites,\n" +
                "                count(*) totalInvites\n" +
                "         from comp_inv_table\n" +
                "         group by comp_inv_table.employeeSeq),\n" +
                "     elg_emps as (\n" +
                "         select employeeSeq\n" +
                "         from emp_inv_table\n" +
                "         where latestInvDate < :latestInvDate\n" +
                "           and totalBulkINvites <= :maxPreviousBulkInviteLimit\n";


        stringBuilder.append(queryTop);

        String shouldReEngageMode;
        if(shouldReEngageERInvited) {
            shouldReEngageMode =    "           and totalBulkINvites < totalInvites\n     )\n";
        } else {
            shouldReEngageMode =    "           and totalBulkINvites = totalInvites\n     )\n";
        }
        stringBuilder.append(shouldReEngageMode);


        //if it is DD query we will use FK1 index, else we will use FK4 index
        String queryHint;
        if(isDDQuery) {
            queryHint =                     "select /*+ INDEX(paycheck0_ PSP_PAYCHECK_FK1) */ ";
        } else {
            queryHint =                     "select /*+ INDEX(paycheck0_ PSP_PAYCHECK_FK4) */ ";
        }

        stringBuilder.append(queryHint);

        String querySelect  =       "distinct employee2_.EMPLOYEE_SEQ\n" +
                                    "from PSP_PAYCHECK paycheck0_,\n" +
                                    "     PSP_PAYROLL_RUN payrollrun1_,\n" +
                                    "     PSP_EMPLOYEE employee2_\n" +
                                    "         inner join PSP_INDIVIDUAL employee2_1_ on employee2_.EMPLOYEE_SEQ = employee2_1_.INDIVIDUAL_SEQ\n" +
                                    "         join elg_emps on EMPLOYEE_SEQ = elg_emps.employeeSeq\n" +
                                    "where (employee2_1_.EMAIL is not null)\n" +
                                    "  and employee2_.CONSUMER_REALM_ID is null\n" +
                                    "  and employee2_.IS_DG_DISASSOCIATED = 0\n" +
                                    "  and paycheck0_.PAYROLL_RUN_FK = payrollrun1_.PAYROLL_RUN_SEQ\n";
        /*DD EMPLOYEE COND*/

        stringBuilder.append(querySelect);

        String paycheckDDCond;
        if(isDDQuery) {
            paycheckDDCond =        "  and paycheck0_.D_D_EMPLOYEE_FK = employee2_.EMPLOYEE_SEQ\n";
        } else {
            paycheckDDCond =        "  and paycheck0_.SOURCE_EMPLOYEE_FK = employee2_.EMPLOYEE_SEQ \n";
        }

        stringBuilder.append(paycheckDDCond);
        
        String queryEnd =           "  and paycheck0_.CREATED_DATE > :lastPaycheckDate\n" +
                                    "  and payrollrun1_.PAYCHECK_SETTLEMENT_DATE < :currentDate\n" +
                                    "  and payrollrun1_.PAYCHECK_SETTLEMENT_DATE >= :lastPaySettlementDate";
        
        stringBuilder.append(queryEnd);

        return stringBuilder.toString();
    }

    /**
     * Gets the list of employees eligible for re-invites for a given company
     * @param companyId
     * @param lastPaidDurationEmployee paycheck should be created within these many days
     * @param settlementDateDuration   latest paycheck settlement date should be within these many days
     * @param isDDQuery  should employee have been paid via DD
     * @param shouldReEngageERInvited should we get employees previously invited by ER (if false we return employees previously invited via batch job)
     * @param latestInviteDate cooling period- latest invite for employees shouldn't have been within last these many days
     * @param maxPreviousBulkInviteLimit maximum number of previous bulk invites allowed, we don't want to pester employees with multiple invites
     * @return list of eligible employees
     */
    public static List<SpcfUniqueId> getWorkforceReInviteEmployeeListForCompany(SpcfUniqueId companyId, int lastPaidDurationEmployee, int settlementDateDuration, boolean isDDQuery, boolean shouldReEngageERInvited, int latestInviteDate, int maxPreviousBulkInviteLimit) {
        List<SpcfUniqueId> employeeIdList = new ArrayList<>();
        //get the SQL query string and then create SQLQuery object


        String empReInviteSQlStr = getSQLQueryStringForWorkforceReInvite(isDDQuery,shouldReEngageERInvited);
        SQLQuery empReInviteSQLQuery = null;
        try {
            Application.beginUnitOfWork();
            empReInviteSQLQuery = Application.getHibernateSession().createSQLQuery(empReInviteSQlStr);

            //set the parameters
            empReInviteSQLQuery.setParameter("companySeq",companyId.toString());


            SpcfCalendar latestInvDate = PSPDate.getPSPTime();
            latestInvDate.addDays(- latestInviteDate);
            empReInviteSQLQuery.setParameter("latestInvDate",latestInvDate.toDate());

            empReInviteSQLQuery.setParameter("maxPreviousBulkInviteLimit",maxPreviousBulkInviteLimit);

            SpcfCalendar lastPaycheckDate = PSPDate.getPSPTime();
            lastPaycheckDate.addDays(- lastPaidDurationEmployee);
            empReInviteSQLQuery.setParameter("lastPaycheckDate",lastPaycheckDate.toDate());

            empReInviteSQLQuery.setParameter("currentDate",PSPDate.getPSPTime().toDate());

            SpcfCalendar lastPaySettlementDate = PSPDate.getPSPTime();
            lastPaySettlementDate.addDays(- settlementDateDuration);
            empReInviteSQLQuery.setParameter("lastPaySettlementDate",lastPaySettlementDate.toDate());

            List<String> empIdsListString=  empReInviteSQLQuery.list();
            employeeIdList = empIdsListString.stream().map(SpcfUniqueIdImpl::new).collect(Collectors.toList());

            Application.commitUnitOfWork();
        }
        finally {
            Application.rollbackUnitOfWork();
        }

        return employeeIdList;
    }

    public static DomainEntitySet<Employee> findEmployeesWithoutCurrentYearPaychecks(Company company) {
        SpcfCalendar firstDayOfYear = CalendarUtils.getFirstDayOfTheYear(PSPDate.getPSPTime());

        return Application.findByNamedQuery("findEmployeesWithoutCurrentYearPaychecks",
                new String[]{"company", "checkDate"},
                new Object[]{company, firstDayOfYear});

    }

    public static DomainEntitySet<Employee> findEmployeeBySearchText(final String searchMethod, final String searchInput, final VMPEmployeePaginationDetails vmpEmployeePaginationDetails) throws Exception{
        SortableProperty<? extends Individual, String> employeeStringSortableProperty = null;
        List<ScalarProperty<? extends Individual, String>> employeeStringScalarProperty = new ArrayList<ScalarProperty<? extends Individual, String>>();
        Query<Employee> searchQuery=new Query<Employee>();
        if(searchMethod.equals("findVmpEmployeeBySSN")){
            List<String> empSsnEncList= EncryptionUtils.deterministicEncryptWithAllKeys(Employee.TaxIdKeyName,searchInput);
            searchQuery = (Query<Employee>) searchQuery.Where(
                    (Employee.Company().CompanyServiceSet().Exists(CompanyService.Service().ServiceCd().equalTo(com.intuit.sbd.payroll.psp.domain.ServiceCode.ViewMyPaycheck))).And(Employee.TaxIdEnc().in(empSsnEncList)));
        }
        else if (searchMethod.equals("findVmpEmployeeByEmail")){
            searchQuery=(Query<Employee>)searchQuery.Where(
                    (Employee.Company().CompanyServiceSet().Exists(CompanyService.Service().ServiceCd().equalTo(com.intuit.sbd.payroll.psp.domain.ServiceCode.ViewMyPaycheck))).And(Employee.Email().like(searchInput + "%", true)));
        } else if (searchMethod.equals("findByCFR")) {
        searchQuery = (Query<Employee>) searchQuery.Where(
                (Employee.Company().CompanyServiceSet().Exists(CompanyService.Service().ServiceCd().equalTo(com.intuit.sbd.payroll.psp.domain.ServiceCode.ViewMyPaycheck))).And(Employee.ConsumerRealmId().equalTo(searchInput)));
        }
        else {
            throw new Exception(searchMethod + " not valid search method");
        }

        if (vmpEmployeePaginationDetails.getSortBy().equals("employeeName")) {
            employeeStringScalarProperty.add(Employee.LastName());
            employeeStringScalarProperty.add(Employee.FirstName());
            employeeStringScalarProperty.add(Employee.MiddleName());
        } else if (vmpEmployeePaginationDetails.getSortBy().equals("companyName")) {
            employeeStringScalarProperty.add(Employee.Company().LegalName());
        } else if (vmpEmployeePaginationDetails.getSortBy().equals("employeeEmail")) {
            employeeStringScalarProperty.add(Employee.Email());
        } else if (searchMethod.equals("findVmpEmployeeByEmail")){
            employeeStringScalarProperty.add(Employee.Email());
        } else { // default sorting by employee Email ( also when sorting needs to be done on SSN)
            employeeStringScalarProperty.add(Employee.Email());
        }

        for(ScalarProperty<? extends Individual, String> scalarProperty: employeeStringScalarProperty) {
            if (vmpEmployeePaginationDetails.isSortDesc()) {
                employeeStringSortableProperty = scalarProperty.Descending();
            } else {
                employeeStringSortableProperty = scalarProperty;
            }
            searchQuery=(Query<Employee>)searchQuery.OrderBy((ScalarProperty<Employee, String>)employeeStringSortableProperty);
        }

        searchQuery=(Query<Employee>)searchQuery.EagerLoad(Employee.Company()).LimitResults(vmpEmployeePaginationDetails.getCurrentPage() * vmpEmployeePaginationDetails.getPageSize(), vmpEmployeePaginationDetails.getPageSize());
        return Application.<Employee>find(Employee.class, searchQuery);
    }

    public static long findEmployeeCountBySearchText(final String searchMethod, final String searchInput) throws Exception {
        Query<Employee> searchQuery=(Query<Employee>)new Query<Employee>().Select(Employee.Id().Count());
        if(searchMethod.equals("findVmpEmployeeBySSN")){
            List<String> empSsnEncList= EncryptionUtils.deterministicEncryptWithAllKeys(Employee.TaxIdKeyName,searchInput);
            searchQuery = (Query<Employee>) searchQuery.Where(
                    (Employee.Company().CompanyServiceSet().Exists(CompanyService.Service().ServiceCd().equalTo(com.intuit.sbd.payroll.psp.domain.ServiceCode.ViewMyPaycheck))).And(Employee.TaxIdEnc().in(empSsnEncList)));
        }
        else if (searchMethod.equals("findVmpEmployeeByEmail")){
            searchQuery=(Query<Employee>)searchQuery.Where(
                    (Employee.Company().CompanyServiceSet().Exists(CompanyService.Service().ServiceCd().equalTo(com.intuit.sbd.payroll.psp.domain.ServiceCode.ViewMyPaycheck))).And(Employee.Email().like(searchInput + "%", true)));
        } else if (searchMethod.equals("findByCFR")) {
            searchQuery = (Query<Employee>) searchQuery.Where(
                    (Employee.Company().CompanyServiceSet().Exists(CompanyService.Service().ServiceCd().equalTo(com.intuit.sbd.payroll.psp.domain.ServiceCode.ViewMyPaycheck))).And(Employee.ConsumerRealmId().equalTo(searchInput)));
        }
        else {
            throw new Exception(searchMethod + " not valid search method");
        }
        return Application.executeScalarAggQuery(Employee.class, searchQuery);
    }
    //todo name search implementation
    /*public static DomainEntitySet<Employee> findEmployeeByNameSearchText(final String searchInput) {
        int maxResults = SystemParameter.findIntValue(SystemParameter.Code.PSPUI_MAX_COMPANY_SEARCH_RESULTS, 100);

        return Application.find(Employee.class, new Query<Employee>().Where(
                (Employee.Company().CompanyServiceSet().Exists(CompanyService.Service().ServiceCd().equalTo(com.intuit.sbd.payroll.psp.domain.ServiceCode.ViewMyPaycheck)))
                .And(Employee.FirstName().like("%" + searchInput + "%", false)
                        .Or(Employee.MiddleName().like("%" + searchInput + "%", false))
                        .Or(Employee.LastName().like("%" + searchInput + "%", false))))
                .EagerLoad(Employee.Company()).LimitResults(0, maxResults));
    }
*/
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Employee()
    {
        super();
    }

    public SpcfCalendar getFirstPayDate()
    {
        Criterion<Paycheck> where = Paycheck.DDEmployee().equalTo(this)
                .And(Paycheck.PayrollRun().PaycheckDate().isNotNull());
        Expression<Paycheck> query = new Query<Paycheck>().Where(where).OrderBy(Paycheck.PayrollRun().PaycheckDate()).LimitResults(0, 1);
        DomainEntitySet<Paycheck> paycheckSet = Application.find(Paycheck.class, query);
        return (paycheckSet.size() > 0) ? paycheckSet.get(0).getPayrollRun().getPaycheckDate() : null;

    }

    public SpcfCalendar getLastPayDate()
    {
        Criterion<Paycheck> where = Paycheck.DDEmployee().equalTo(this)
                .And(Paycheck.PayrollRun().PaycheckDate().isNotNull());
        Expression<Paycheck> query = new Query<Paycheck>().Where(where).OrderBy(Paycheck.PayrollRun().PaycheckDate().Descending()).LimitResults(0, 1);
        DomainEntitySet<Paycheck> paycheckSet = Application.find(Paycheck.class, query);
        return (paycheckSet.size() > 0) ? paycheckSet.get(0).getPayrollRun().getPaycheckDate() : null;

    }

    public SpcfCalendar getLastTOKModifiedDate() {
        String[] individualPropertyNames = {Individual.FirstName().getPropertyName(),
                Individual.MiddleName().getPropertyName(),
                Individual.LastName().getPropertyName(),
                Individual.Phone().getPropertyName(),
                Individual.Email().getPropertyName()};
        Criterion<PropertyAudit> individualProperties =
                PropertyAudit.ClassName().equalTo(Individual.class.getSimpleName())
                        .And(PropertyAudit.PropertyName().in(individualPropertyNames));

        String[] employeePropertyNames = {
                "BirthDate","TaxId",
                Employee.BirthDateEnc().getPropertyName(),
                Employee.HireDate().getPropertyName(),
                Employee.TerminationDate().getPropertyName(),
                Employee.StatusCd().getPropertyName(),
                Employee.TaxIdEnc().getPropertyName(),
                ThirdParty401kInfo.class.getSimpleName() + "." + Employee.ThirdParty401kInfo().IsFamilyMember().getPropertyName(),
                ThirdParty401kInfo.class.getSimpleName() + "." + Employee.ThirdParty401kInfo().IsHighlyCompensated().getPropertyName(),
                ThirdParty401kInfo.class.getSimpleName() + "." + Employee.ThirdParty401kInfo().OwnershipPercentage().getPropertyName()};
        Criterion<PropertyAudit> employeeProperties =
                PropertyAudit.ClassName().equalTo(Employee.class.getSimpleName())
                        .And(PropertyAudit.PropertyName().in(employeePropertyNames));


        Criterion<PropertyAudit> where = PropertyAudit.Company().equalTo(getCompany())
                .And(PropertyAudit.ObjectIdentifier().equalTo(getId().toString()))
                .And(individualProperties.Or(employeeProperties));

        Expression<PropertyAudit> query = new Query<PropertyAudit>().Select(PropertyAudit.CreatedDate().Max()).Where(where);
        return Application.executeObjectAggQuery(PropertyAudit.class, query);
    }

    public SpcfCalendar getLastTOKEmployeeSendDate() {
        Criterion<ThirdParty401kPaycheck> where = ThirdParty401kPaycheck.Paycheck().SourceEmployee().equalTo(this);
        Expression<ThirdParty401kPaycheck> query = new Query<ThirdParty401kPaycheck>().Where(where).OrderBy(ThirdParty401kPaycheck.InitiationDate().Descending()).LimitResults(0, 1);
        DomainEntitySet<ThirdParty401kPaycheck> batchSet = Application.find(ThirdParty401kPaycheck.class, query);
        return (batchSet.size() > 0) ? batchSet.get(0).getInitiationDate() : null;
    }

    public SpcfCalendar getLastTOKPaycheckSendDate() {
        Criterion<ThirdParty401kPaycheck> where = ThirdParty401kPaycheck.Paycheck().SourceEmployee().equalTo(this);
        Expression<ThirdParty401kPaycheck> query = new Query<ThirdParty401kPaycheck>().Where(where).OrderBy(ThirdParty401kPaycheck.InitiationDate().Descending()).LimitResults(0, 1);
        DomainEntitySet<ThirdParty401kPaycheck> batchSet = Application.find(ThirdParty401kPaycheck.class, query);
        return (batchSet.size() > 0) ? batchSet.get(0).getInitiationDate() : null;
    }

    public SpcfCalendar getLastPayrollReceivedDate() {
        Criterion<Paycheck> where = Paycheck.SourceEmployee().equalTo(this)
                .And(Paycheck.PayrollRun().PaycheckDate().isNotNull());
        Expression<Paycheck> query = new Query<Paycheck>().Where(where).OrderBy(Paycheck.PayrollRun().PaycheckDate().Descending()).LimitResults(0, 1);
        DomainEntitySet<Paycheck> paycheckSet = Application.find(Paycheck.class, query);
        return (paycheckSet.size() > 0) ? paycheckSet.get(0).getPayrollRun().getPaycheckDate() : null;
    }

    public SpcfCalendar getFirstPayrollReceivedDate() {
        Criterion<Paycheck> where = Paycheck.SourceEmployee().equalTo(this)
                .And(Paycheck.PayrollRun().PaycheckDate().isNotNull());
        Expression<Paycheck> query = new Query<Paycheck>().Where(where).OrderBy(Paycheck.PayrollRun().PaycheckDate()).LimitResults(0, 1);
        DomainEntitySet<Paycheck> paycheckSet = Application.find(Paycheck.class, query);
        return (paycheckSet.size() > 0) ? paycheckSet.get(0).getPayrollRun().getPaycheckDate() : null;
    }

    /**
     * Validates the employee BE. The process result returns immediately if
     * either the Employee domain object is null or the Company domain object
     * contained within the employee is null
     *
     * @return Result of validating employee
     */
    public ProcessResult validateEmployee() {
        ProcessResult validationResult = new ProcessResult();

        //  Validate the employee is specified
        if (this == null) {
            validationResult.getMessages().EmployeeNotSpecified(
                    EntityName.Employee, null);
            return validationResult;
        }

        //  Validate the employee is specified
        if (getSourceEmployeeId() == null) {
            validationResult.getMessages().EmployeeIdNotSpecified(
                    EntityName.Employee, null);
            return validationResult;
        }

        //  Validate the company is specified
        Company incCompany = getCompany();
        if (incCompany == null) {
            validationResult.getMessages().CompanyNotSpecified(
                    EntityName.Company, null);
            return validationResult;
        }

        // Validate Company Exists
        Company company = Company.findCompany(incCompany
                .getSourceCompanyId(), incCompany.getSourceSystemCd());
        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(
                    EntityName.Company, incCompany.getSourceCompanyId(),
                    incCompany.getSourceSystemCd().toString(),
                    incCompany.getSourceCompanyId());
            return validationResult;
        } else {
            //  Reset to a fully qualified company
            setCompany(company);
        }

        return validationResult;
    }

    /**
     * Deactivates all the active employee bank accounts with a status effective date of today
     *
     * @return
     */

    public ProcessResult deactivateBankAccounts() {
        return deactivateBankAccounts( PSPDate.getPSPTime());
    }

    /**
     * Deactivates all the active employee bank accounts with a specific status effective date
     *
     * @param pStatusEffectiveDate
     * @return
     */

    public ProcessResult deactivateBankAccounts(SpcfCalendar pStatusEffectiveDate) {
        ProcessResult deactivateBankAccountsResult = new ProcessResult();
        DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = getEmployeeBankAccountCollection();

        for (EmployeeBankAccount employeeBankAccount : employeeBankAccounts) {
            if (employeeBankAccount.getStatusCd() == BankAccountStatus.Active) {
                employeeBankAccount.deactivate(pStatusEffectiveDate);
            }
        }
        return deactivateBankAccountsResult;
    }

    public boolean isTerminated() {
        if (this !=null && getStatusCd() == EmployeeStatus.Inactive && getTerminationDate() != null) {
            return true;
        } else {
            return false;
        }
    }

    public NaturalKey getNaturalKey() {
        return new NaturalKey(Employee.class, getCompany().getId(), getSourceEmployeeId());
    }

    public DomainEntitySet<Employee> findEmployeesWithSameNameFromTerminatedCompanies() {
        String[] paramNames = new String[3];
        paramNames[0] = "firstName";
        paramNames[1] = "lastName";
        paramNames[2] = "statusCd";

        Object[] paramValues = new Object[3];
        paramValues[0] = getFirstName();
        //paramValues[1] = getLastName() == null ? "" :  getLastName();
        paramValues[1] = getLastName();
        paramValues[2] = ServiceStatusCode.Terminated;

        return Application.findByNamedQuery("findEmployeesByNameAndCompanyServiceStatus", paramNames, paramValues);
    }

    public ArrayList<String> isValidForCensusFile() {
        ArrayList<String> validationErrors = new ArrayList<String>();
        if (getCompany().getFedTaxId() == null || getCompany().getFedTaxId().length() == 0) {
            validationErrors.add("Field 'FEIN' contains invalid 401k data");
        }

        ThirdParty401kCompanyServiceInfo tp401kInfo = (ThirdParty401kCompanyServiceInfo) CompanyService.findCompanyService(getCompany(), ServiceCode.ThirdParty401k);

        if (tp401kInfo == null || tp401kInfo.getCustodialId() == null || tp401kInfo.getCustodialId().length() == 0) {
            validationErrors.add("Field 'Custodial Account Id' contains invalid 401k data");
        }

        if (getFirstName() == null || getFirstName().length() == 0) {
            validationErrors.add("Field 'First Name' contains invalid 401k data");
        }

        if (getTaxId() == null || getTaxId().length() == 0) {
            validationErrors.add("Field 'Tax Id' contains invalid 401k data");
        }

        if (getBirthDate() == null) {
            validationErrors.add("Field 'Birth Date' contains invalid 401k data");
        }

        if (getHireDate() == null) {
            validationErrors.add("Field 'Date of Hire' contains invalid 401k data");
        }

        if (getLastName() == null || getLastName().length() == 0) {
            validationErrors.add("Field 'Last Name' contains invalid 401k data");
        }

        return validationErrors;
    }

    public String toString() {
        return super.toString() + "  Employee  SrcId: " + getSourceEmployeeId() + "  Status: " + (getStatusCd() != null ? getStatusCd().name() : "null");
    }

    public boolean canBeRecoveredByQB() {
        return getQbdtEmployeeInfo() != null && getQbdtEmployeeInfo().getIsAssisted();
    }

    public void cache() {
        Application.getSessionCache().addPrimaryKey(getNaturalKey(), getId());
    }

    // ----- QBDT Token overrides -----

    @Override
    public void setSourceEmployeeId(String pSourceEmployeeId) {
        if(!ObjectUtils.equals(getSourceEmployeeId(), pSourceEmployeeId)) {
            onUpdate();
        }

        super.setSourceEmployeeId(pSourceEmployeeId);

        if(getCompany() != null && getSourceEmployeeId() != null) {
            getCompany().usedEmployeeId(getSourceEmployeeId());
        }
    }

    @Override
    public void setStatusCd(EmployeeStatus pStatusCd) {
        if(!ObjectUtils.equals(getStatusCd(), pStatusCd)) {
            onUpdate();
        }
        super.setStatusCd(pStatusCd);
    }


    public void setTaxId(String pTaxId) {
        if(!ObjectUtils.equals(getTaxId(), pTaxId)) {
            onUpdate();
        }
        super.setTaxIdEnc(EncryptionUtils.deterministicEncrypt(TaxIdKeyName,pTaxId));
    }

    @Override
    public void setHireDate(SpcfCalendar pHireDate) {
        if(!ObjectUtils.equals(getHireDate(), pHireDate)) {
            onUpdate();
        }
        super.setHireDate(pHireDate);
    }

    @Override
    public void setFedFilingStatus(String pFedFilingStatus) {
        if(!ObjectUtils.equals(getFedFilingStatus(), pFedFilingStatus)) {
            onUpdate();
        }
        super.setFedFilingStatus(pFedFilingStatus);
    }

    @Override
    public void setWorkState(String pWorkState) {
        if(!ObjectUtils.equals(getWorkState(), pWorkState)) {
            onUpdate();
        }
        super.setWorkState(pWorkState);
    }

    @Override
    public void setTerminationDate(SpcfCalendar pTerminationDate) {
        if(!ObjectUtils.equals(getTerminationDate(), pTerminationDate)) {
            onUpdate();
        }
        super.setTerminationDate(pTerminationDate);
    }

    @Override
    public void setFedAllowances(int pFedAllowances) {
        if(!ObjectUtils.equals(getFedAllowances(), pFedAllowances)) {
            onUpdate();
        }
        super.setFedAllowances(pFedAllowances);
    }

    @Override
    public void setQualifiesForAeic(boolean pQualifiesForAeic) {
        if(!ObjectUtils.equals(getQualifiesForAeic(), pQualifiesForAeic)) {
            onUpdate();
        }
        super.setQualifiesForAeic(pQualifiesForAeic);
    }

    @Override
    public void setIsDeceased(boolean pIsDeceased) {
        if(!ObjectUtils.equals(getIsDeceased(), pIsDeceased)) {
            onUpdate();
        }
        super.setIsDeceased(pIsDeceased);
    }

    @Override
    public void setFedExtraWithholding(SpcfMoney pFedExtraWithholding) {
        if(!ObjectUtils.equals(getFedExtraWithholding(), pFedExtraWithholding)) {
            onUpdate();
        }
        super.setFedExtraWithholding(pFedExtraWithholding);
    }

    @Override
    public void setFedClaimDependents(SpcfMoney pFedClaimDependents) {
        if(!ObjectUtils.equals(getFedClaimDependents(), pFedClaimDependents)) {
            onUpdate();
        }
        super.setFedClaimDependents(pFedClaimDependents);
    }

    @Override
    public void setFedOtherIncome(SpcfMoney pFedOtherIncome) {
        if(!ObjectUtils.equals(getFedOtherIncome(), pFedOtherIncome)) {
            onUpdate();
        }
        super.setFedOtherIncome(pFedOtherIncome);
    }

    @Override
    public void setFedDeductions(SpcfMoney pFedDeductions) {
        if(!ObjectUtils.equals(getFedDeductions(), pFedDeductions)) {
            onUpdate();
        }
        super.setFedDeductions(pFedDeductions);
    }

    @Override
    public void setFedMultipleJobs(boolean pFedMultipleJobs) {
        if(!ObjectUtils.equals(getFedMultipleJobs(), pFedMultipleJobs)) {
            onUpdate();
        }
        super.setFedMultipleJobs(pFedMultipleJobs);
    }

    @Override
    public void setFedW4EmployeePref(String pFedW4EmployeePref){
        if(!ObjectUtils.equals(getFedW4EmployeePref(),pFedW4EmployeePref)){
            onUpdate();
        }
        super.setFedW4EmployeePref(pFedW4EmployeePref);
    }

    @Override
    public void setLiveState(String pLiveState) {
        if(!ObjectUtils.equals(getLiveState(), pLiveState)) {
            onUpdate();
        }
        super.setLiveState(pLiveState);
    }

    @Override
    public void setQbdtEmployeeInfo(QbdtEmployeeInfo pQbdtEmployeeInfo) {
        if(!ObjectUtils.equals(getQbdtEmployeeInfo(), pQbdtEmployeeInfo)) {
            onUpdate();
        }
        super.setQbdtEmployeeInfo(pQbdtEmployeeInfo);
    }

    @Override
    public void setCompany(Company pCompany) {
        if(!ObjectUtils.equals(getCompany(), pCompany)) {
            onUpdate();
        }

        super.setCompany(pCompany);

        if(getCompany() != null && getSourceEmployeeId() != null) {
            getCompany().usedEmployeeId(getSourceEmployeeId());
        }
    }

    @Override
    public void addEmployeeAccrual(EmployeeAccrual pEmployeeAccrual) {
        super.addEmployeeAccrual(pEmployeeAccrual);
        onUpdate();
    }

    @Override
    public void removeEmployeeAccrual(EmployeeAccrual pEmployeeAccrual) {
        super.removeEmployeeAccrual(pEmployeeAccrual);
        onUpdate();
    }

    @Override
    public void addEmployeeCustomField(EmployeeCustomField pEmployeeCustomField) {
        super.addEmployeeCustomField(pEmployeeCustomField);
        onUpdate();
    }

    @Override
    public void removeEmployeeCustomField(EmployeeCustomField pEmployeeCustomField) {
        super.removeEmployeeCustomField(pEmployeeCustomField);
        onUpdate();
    }

    @Override
    public void addEmployeePayrollItem(EmployeePayrollItem pEmployeePayrollItem) {
        super.addEmployeePayrollItem(pEmployeePayrollItem);
        onUpdate();
    }

    @Override
    public void removeEmployeePayrollItem(EmployeePayrollItem pEmployeePayrollItem) {
        super.removeEmployeePayrollItem(pEmployeePayrollItem);
        onUpdate();
    }

    @Override
    public void addEmployeeTax(EmployeeTax pEmployeeTax) {
        super.addEmployeeTax(pEmployeeTax);
        onUpdate();
    }

    @Override
    public void removeEmployeeTax(EmployeeTax pEmployeeTax) {
        super.removeEmployeeTax(pEmployeeTax);
        onUpdate();
    }

    @Override
    public void addEmployeeWagePlan(EmployeeWagePlan pEmployeeWagePlan) {
        super.addEmployeeWagePlan(pEmployeeWagePlan);
        onUpdate();
    }

    @Override
    public void removeEmployeeWagePlan(EmployeeWagePlan pEmployeeWagePlan) {
        super.removeEmployeeWagePlan(pEmployeeWagePlan);
        onUpdate();
    }

    @Override
    public void addEmployeeBankAccount(EmployeeBankAccount pEmployeeBankAccount) {
        super.addEmployeeBankAccount(pEmployeeBankAccount);
        onUpdate();
    }

    @Override
    public void removeEmployeeBankAccount(EmployeeBankAccount pEmployeeBankAccount) {
        super.removeEmployeeBankAccount(pEmployeeBankAccount);
        onUpdate();
    }

    @Override
    public void setFirstName(String pFirstName) {
        if(!ObjectUtils.equals(getFirstName(), pFirstName)) {
            onUpdate();
        }
        super.setFirstName(pFirstName);
    }

    @Override
    public void setGenderCd(Gender pGenderCd) {
        if(!ObjectUtils.equals(getGenderCd(), pGenderCd)) {
            onUpdate();
        }
        super.setGenderCd(pGenderCd);
    }

    @Override
    public void setLastName(String pLastName) {
        if(!ObjectUtils.equals(getLastName(), pLastName)) {
            onUpdate();
        }
        super.setLastName(pLastName);
    }

    @Override
    public void setMiddleName(String pMiddleName) {
        if(!ObjectUtils.equals(getMiddleName(), pMiddleName)) {
            onUpdate();
        }
        super.setMiddleName(pMiddleName);
    }

    @Override
    public void setSuffix(String pSuffix) {
        if(!ObjectUtils.equals(getSuffix(), pSuffix)) {
            onUpdate();
        }
        super.setSuffix(pSuffix);
    }

    @Override
    public void setMailingAddress(Address pMailingAddress) {
        if(!ObjectUtils.equals(getMailingAddress(), pMailingAddress)) {
            onUpdate();
        }
        super.setMailingAddress(pMailingAddress);
    }

    public void onUpdate() {
        if(getQbdtEmployeeInfo() != null) {
            getQbdtEmployeeInfo().onUpdate();
        }
    }

    public String getTaxId() {
        return EncryptionUtils.deterministicDecrypt(TaxIdKeyName,getTaxIdEnc());
    }

    public void setBirthDate(SpcfCalendar pBirthDate) {
        super.setBirthDateEnc(EncryptionUtils.probabilisticEncryptDate(BirthDateKeyName, pBirthDate, getId().toString()));
    }


    public SpcfCalendar getBirthDate() {
        return EncryptionUtils.probabilisticDecryptDate(BirthDateKeyName, getBirthDateEnc());
    }

    public String getIsSeasonal() {
        if (getQbdtEmployeeInfo() != null) {
            return  getQbdtEmployeeInfo().getEmployeeSeasonal().toString();

        }
        return null;
    }

    public PublishStatusWorkflowPackager getPublishStatusWorkFlowPackager() {

        if(Objects.isNull(this.publishStatusWorkflowPackager)){
            String workFlowsFlag = super.getPublishStatus();
            this.publishStatusWorkflowPackager = new PublishStatusWorkflowPackager(workFlowsFlag);
        }

        return this.publishStatusWorkflowPackager;
    }

    public void setPublishStatusWorkflowState(EmployeePublishStatusWorkflows workflow, PublishStatusWorkflowState workflowState) {
        this.getPublishStatusWorkFlowPackager().setWorkflowState(workflow, workflowState);
    }

    public String getPublishStatus() {
        String workFlowsFlag = this.getPublishStatusWorkFlowPackager().getWorkFlowsFlagString();
        return workFlowsFlag;
    }

    public ProcessFlagWorkflowPackager getProcessFlagWorkflowPackager() {

        if(Objects.isNull(this.processFlagWorkflowPackager)){
            String workflowsFlag = super.getProcessFlag();
            this.processFlagWorkflowPackager = new ProcessFlagWorkflowPackager(workflowsFlag);
        }
        return this.processFlagWorkflowPackager;
    }

    public void setProcessFlagWorkflowState(EmployeeProcessFlagWorkflows workflow, ProcessFlagWorkflowState workflowState) {
        this.getProcessFlagWorkflowPackager().setWorkflowState(workflow, workflowState);
    }

    public String getProcessFlag() {
        String workFlowsFlag = this.getProcessFlagWorkflowPackager().getWorkFlowsFlagString();
        return workFlowsFlag;

    }

    public static List<Employee> findEmployeesByNonAuthCriteria(String ssn, String firstName, String lastName,
                                                                String sourceSystemCode, String email, String phoneNumber) {
        String unformattedSsn = ssn.replaceAll("[^0-9]", "");
        String unformattedPhone = phoneNumber.replaceAll("[^0-9]", "");
        List<String> encryptedSsnList = EncryptionUtils.deterministicEncryptWithAllKeys(Employee.TaxIdKeyName, unformattedSsn);

        String[] paramNames = new String[]{
                "firstName",
                "lastName",
                "ssnList",
                "email",
                "phone",
                "sourceSystemCode"};

        Object[] paramValues = new Object[]{
                StringUtils.lowerCase(firstName),
                StringUtils.lowerCase(lastName),
                encryptedSsnList,
                StringUtils.lowerCase(email),
                unformattedPhone,
                SourceSystemCode.valueOf(sourceSystemCode)};
        return Application.executeNamedQuery(Application.getQueryName("findEmployeesByNonAuthCriteria"), paramNames, paramValues);
    }

}