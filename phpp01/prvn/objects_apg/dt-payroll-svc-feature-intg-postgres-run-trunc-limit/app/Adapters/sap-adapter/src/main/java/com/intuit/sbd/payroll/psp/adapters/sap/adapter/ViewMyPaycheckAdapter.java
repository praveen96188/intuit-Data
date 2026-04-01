package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.platform.integration.ius.common.types.User;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexMethod;
import com.intuit.sbd.payroll.psp.adapters.sap.Operation;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.context.aspect.CompanyIdentifierType;
import com.intuit.sbd.payroll.psp.context.aspect.TenantId;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.OperationId;
import com.intuit.sbd.payroll.psp.gateways.iam.ConsumerRealm;
import com.intuit.sbd.payroll.psp.gateways.iam.IDLMClientWrapper;
import com.intuit.sbd.payroll.psp.gateways.iam.IamUser;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.*;
import com.intuit.sbd.payroll.psp.util.VMPEmployeePaginationDetails;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;
import org.hibernate.FlushMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * User: ihannur
 * Date: 6/20/13
 * Time: 1:45 PM
 */
public class ViewMyPaycheckAdapter {

    private static final SpcfLogger logger = PayrollServices.getLogger(ViewMyPaycheckAdapter.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);

    private IDLMClientWrapper idlmClientWrapper;

    public ViewMyPaycheckAdapter() {
        idlmClientWrapper = PayrollApplicationBeanFactory.getBean(IDLMClientWrapper.class);
    }

    private IamUser getIamAccountForConsumerRealmId(ConsumerRealm consumerRealm, String consumerRealmId) {
        logger.info(String.format("action=getIamAccountForConsumerRealmId, consumerRealmId=%s",consumerRealmId));
        User user = consumerRealmId != null ? consumerRealm.getUserForConsumerRealmId(consumerRealmId) : null;
        IamUser iamUser = new IamUser();
        if (user != null) {
            String email = user.getEmail() != null ? user.getEmail().getAddress() : null;
            iamUser.setEmailAddress(email);
            iamUser.setLoginName(user.getUsername());
        }
        return iamUser;
    }

    private IamUser getIamAccountForUserAuthId(String userAuthId) {
        try {
            logger.info(String.format("action=getIamAccountForUserAuthId, userAuthId=%s",userAuthId));
            return idlmClientWrapper.getUserDetailsForAuthId(userAuthId);
        } catch (Exception ex) {
            return null;
        }
    }
    //getAuthId is a throwaway code and hence it is present at 2 places, this will be removed once CFR cleanup is done.
    private String getAuthId(Employee employee) {
        if(employee.getUserAuthId() != null) {
            logger.info(String.format("action=getAuthId, AuthId found in employeeRecord, userAuthId=%s, employeeId=%s",employee.getUserAuthId(), employee.getId()));
            return employee.getUserAuthId();
        } else if(employee.getConsumerRealmId() != null) {
            ConsumerRealm consumerRealm = new ConsumerRealm();
            logger.info(String.format("AuthId is empty in employee record, fetching it from consumerRealmId=%s", employee.getConsumerRealmId()));
            return consumerRealm.getAuthIdFromConsumerRealmId(employee.getConsumerRealmId());
        }
        else {
            return null;
        }
    }
    private IamUser getUserDetails(Employee employee){
        if(FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_IDLM_ENABLED_FOR_FETCHING_USER_DETAILS, true)) {
            String userAuthId = getAuthId(employee);
            return userAuthId != null ? getIamAccountForUserAuthId(userAuthId) : null;
        } else {
            ConsumerRealm consumerRealm = new ConsumerRealm();
            return employee.getConsumerRealmId() != null ? getIamAccountForConsumerRealmId(consumerRealm, employee.getConsumerRealmId()) : null;
        }
    }

    @SuppressWarnings("ConstantConditions")
    @FlexMethod
    @Operation(operationIds = OperationId.ViewVMPData)
    public SAPSearchResults<SAPVMPEmployeeInfo> getEmployeesInfo(String sourceSystemCd, String companyId, int pFirstIndex, int pMaxResults, String pSortColumn, Boolean pSortDescending) throws Throwable {
        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

        SAPSearchResults<SAPVMPEmployeeInfo> searchResults = new SAPSearchResults<SAPVMPEmployeeInfo>();
        ArrayList<SAPVMPEmployeeInfo> employeeSearchResult = new ArrayList<SAPVMPEmployeeInfo>();
        try {
            Criterion<Employee> employeeCriterion = Employee.Company().SourceCompanyId().equalTo(companyId)
                                                            .And(Employee.Company().SourceSystemCd().equalTo(SourceSystemCode.valueOf(sourceSystemCd)))
                                                            .And(Employee.SourceEmployeeId().regexpLike("^[0-9]+$"));

            Expression<Employee> employeeQuery = new Query<Employee>().Where(employeeCriterion);
            //default sorting is by name
            if (pSortColumn == null || StringUtils.equals(pSortColumn, "fullNameForward")) {
                if(pSortDescending) {
                    employeeQuery = ((Query<Employee>) employeeQuery).OrderBy(Employee.FirstName().Descending(), Employee.LastName().Descending(), Employee.Id());
                } else {
                    employeeQuery = new Query<Employee>().Where(employeeCriterion).OrderBy(Employee.FirstName(), Employee.LastName(), Employee.Id());
                }
            } else if (StringUtils.equals(pSortColumn, "socialSecurityNumber")) {
                    //Sorting by PII fields will not work post encryption
                employeeQuery = ((Query<Employee>) employeeQuery).OrderBy (Employee.Id());
            } else if (StringUtils.equals(pSortColumn, "consumerId")) {
                if(pSortDescending) {
                    employeeQuery = ((Query<Employee>) employeeQuery).OrderBy (Employee.ConsumerRealmId().Descending(), Employee.Id());
                } else {
                    employeeQuery = ((Query<Employee>) employeeQuery).OrderBy (Employee.ConsumerRealmId(), Employee.Id());
                }
            }

            DomainEntitySet<Employee> employees = Application.find(Employee.class, ((Query<Employee>) employeeQuery).LimitResults(pFirstIndex, pMaxResults));
            Long totalRecords = Application.executeScalarAggQuery(Employee.class, new Query<Employee>()
                    .Select(Employee.Id().Count())
                    .Where(employeeCriterion));
            searchResults.setTotalRecords(totalRecords);

            AuthUser foundUser = AuthUser.findUser(Application.getCurrentPrincipal().getId());
            boolean canViewEEPII = foundUser.hasOperation(OperationId.ViewEEPII);

            for (Employee employee : employees) {
                IamUser iamUser = getUserDetails(employee);
                employeeSearchResult.add(ViewMyPaycheckTranslator.getSAPEmployeeInfoFromDomainEntity(employee, iamUser, canViewEEPII));
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding VMP Employees for Company Id:" + companyId + " Code:" + sourceSystemCd, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        searchResults.setReturnsList(employeeSearchResult);
        return searchResults;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewVMPData)
    public SAPVMPEmployeeInfo getEmployeeInfo(String pEmployeeId) throws Throwable {
        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
        SAPVMPEmployeeInfo sapvmpEmployeeInfo = null;
        try {
            Employee employee = Application.findById(Employee.class, SpcfUniqueId.createInstance(pEmployeeId));
            AuthUser foundUser = AuthUser.findUser(Application.getCurrentPrincipal().getId());
            boolean canViewEEPII = foundUser.hasOperation(OperationId.ViewEEPII);

            IamUser iamUser = getUserDetails(employee);
            sapvmpEmployeeInfo = ViewMyPaycheckTranslator.getSAPEmployeeInfoFromDomainEntity(employee, iamUser, canViewEEPII);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting employee info for EmployeeSeq:" + pEmployeeId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapvmpEmployeeInfo;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewVMPData)
    public ArrayList<SAPPaystub> findPaystubs(String pEmployeeId, Date pFromDate, Date pToDate) throws Throwable {
        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
        ArrayList<SAPPaystub> sapPaystubs = new ArrayList<SAPPaystub>();
        try {
            Criterion<Paystub> whereClause = Paystub.PstubEmployeeInfo().Employee().Id().equalTo(SpcfUniqueId.createInstance(pEmployeeId))
                    .And(Paystub.PstubEmployeeInfo().Employee().Company().Id().equalTo(Paystub.PstubEmployeeInfo().Company().Id()))
                    .And(Paystub.PstubEmployeeInfo().Company().Id().equalTo(Paystub.Company().Id()))
                    .And(Paystub.Paycheck().Company().Id().equalTo(Paystub.Company().Id()))
                                                    .And(Paystub.Paycheck().Status().equalTo(PaycheckStatusCode.Active))
                                                    .And(Paystub.Paycheck().SourcePaycheckId().greaterThan("0"));
            if (pFromDate != null) {
                whereClause = whereClause.And(Paystub.PaycheckDate().greaterOrEqualThan(SAPTranslator.getSpcfCalendarFromDate(pFromDate)));
            }
            if (pToDate != null) {
                whereClause = whereClause.And(Paystub.PaycheckDate().lessOrEqualThan(SAPTranslator.getSpcfCalendarFromDate(pToDate)));
            }

            DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class, new Query<Paystub>().Where(whereClause).OrderBy(Paystub.PaycheckDate()));
            for (Paystub paystub : paystubs) {
                sapPaystubs.add(ViewMyPaycheckTranslator.getSAPPaystubFromEntity(pEmployeeId, paystub));
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding Paystubs for Employee" + pEmployeeId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapPaystubs;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewVMPData)
    public SAPPaystubDetails getPaystubDetails(String pPaystubId, @TenantId(IdType = CompanyIdentifierType.PSID) String companyId) throws Throwable {
        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
        SAPPaystubDetails sapPaystubDetails = null;
        try {
            Expression<Paystub> query = new Query<Paystub>().Where(Paystub.Id().equalTo(SpcfUniqueId.createInstance(pPaystubId)))
                                                            .EagerLoad(Paystub.PstubPayItemSet(), Paystub.PstubPaidTimeoffItemSet(), Paystub.PstubEmployeeInfo().PstubAddress());
            Paystub paystub = Application.find(Paystub.class, query).getFirst();
            if (paystub != null) {
                sapPaystubDetails = ViewMyPaycheckTranslator.getSAPPaystubDetailsFromEntity(paystub);
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding Paystub details for PaystubSeq:" + pPaystubId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapPaystubDetails;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EditVMPData)
    public void removeConsumerId(String pEmployeeId) throws Throwable {
        PayrollServices.beginUnitOfWork();
        try {
            ProcessResult processResult = PayrollServices.employeeManager.removeConsumerRealmId(pEmployeeId);
            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error removing Consumer Id", processResult);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error removing Consumer Id for EmployeeSeq:" + pEmployeeId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.SearchBySSN)
    public ArrayList<SAPEmployeeSearchResult> findVMPEmployee(String searchMethod, String searchInput, SAPVMPEmployeePaginationDetails sapVMPEmployeePaginationDetails) throws Throwable {
        VMPEmployeePaginationDetails vmpEmployeePaginationDetails=ViewMyPaycheckTranslator.translateSAPObjectForDomain(sapVMPEmployeePaginationDetails);
        ArrayList<SAPEmployeeSearchResult> retList = new ArrayList<SAPEmployeeSearchResult>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            AuthUser foundUser = AuthUser.findUser(Application.getCurrentPrincipal().getId());
            boolean canViewEEPII = foundUser.hasOperation(OperationId.ViewEEPII);

            DomainEntitySet<Employee> employees=Employee.findEmployeeBySearchText(searchMethod, searchInput, vmpEmployeePaginationDetails);

            for (Employee employee : employees) {
                SAPEmployeeSearchResult employeeInfo = ViewMyPaycheckTranslator.getSAPEmployeeSearchResult(employee, canViewEEPII);
                retList.add(employeeInfo);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException(String.format("Error occurred while searching for employee. Method: %s. Input: %s.", searchMethod, searchInput), t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        if (((searchMethod.equals("findVmpEmployeeBySSN") && (vmpEmployeePaginationDetails.getSortBy() == null
                || "".equals(vmpEmployeePaginationDetails.getSortBy())))
                || vmpEmployeePaginationDetails.getSortBy().equals("employeeSSN"))) {
            Collections.sort(retList, (SAPEmployeeSearchResult a, SAPEmployeeSearchResult b) -> b.getEmployeeSSN()
                    .compareTo(a.getEmployeeSSN()));
            if (!vmpEmployeePaginationDetails.isSortDesc()) {
                Collections.reverse(retList);
            }
        }
        return retList;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.SearchBySSN)
    public long findVMPEmployeeCount(String searchMethod, String searchInput) throws Throwable {
        long employeeCount=0;
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            employeeCount=Employee.findEmployeeCountBySearchText(searchMethod, searchInput);
        } catch (Throwable t) {
            aeFactory.throwGenericException(String.format("Error occurred while searching for employee. Method: %s. Input: %s.", searchMethod, searchInput), t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return employeeCount;
    }

}
