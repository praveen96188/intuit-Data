package com.intuit.sbd.payroll.psp.adapters.cdmadapter.managers;

import com.intuit.ems.dataservice.v1.exception.*;
import com.intuit.ems.dataservice.v1.manager.IPayrollEmployeeManager;
import com.intuit.ems.dataservice.v1.resource.EmployeeIdentificationParams;
import com.intuit.ems.dataservice.v1.resource.EmployeePreferenceParams;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.factories.CdmFactory;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.factories.DomainFactory;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.finders.EmployeeFinder;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.finders.PaystubFinder;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.util.CdmHelper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.schema.ems.v3.EmployeePreference;
import com.intuit.schema.ems.v3.PayrollEmployee;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;
import org.hibernate.exception.ConstraintViolationException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PayrollEmployeeManager implements IPayrollEmployeeManager {
    private static SpcfLogger logger = null;

    static {
        logger = Application.getLogger(PayrollEmployeeManager.class);
    }

    @Override
    public List<PayrollEmployee> getForRealm(String consumerRealmID) {
        try {
            PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContext(null, RequestType.REST, "getForRealm");
            logger.info("Request received to find PayrollEmployees for consumerRealmId="+consumerRealmID);
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            List<Employee> employees = EmployeeFinder.findEmployees(consumerRealmID);
            List<PayrollEmployee> payrollEmployees = new ArrayList<PayrollEmployee>();
            if(employees != null && !employees.isEmpty()) {
                for(Employee employee : employees) {
                    payrollEmployees.add(CdmFactory.createPayrollEmployee(employee));
                }
                return payrollEmployees;
            } else {
                List<VmpEmployeeInfo> vmpEmployeeInfos = EmployeeFinder.findVmpEmployeeInfo(consumerRealmID);
                if(vmpEmployeeInfos != null && !vmpEmployeeInfos.isEmpty()){
                    for(VmpEmployeeInfo vmpEmployeeInfo : vmpEmployeeInfos){
                        payrollEmployees.add(CdmFactory.createPayrollEmployee(vmpEmployeeInfo));
                    }
                    return payrollEmployees;
                }
                logger.info("Could not find PayrollEmployees for consumerRealmId="+consumerRealmID+". Error Code="+DataServiceException.ERRNUM_PAYROLLEMPLOYEE_RESOURCE_NOT_FOUND);
                throw new ResourceNotFoundException(DataServiceException.ERRNUM_PAYROLLEMPLOYEE_RESOURCE_NOT_FOUND);
            }
        } catch(RuntimeException runTimeException) {
            String errorMessage = "Error getting employees for realmId=" + consumerRealmID;
            CdmHelper.logRunTimeException(logger, errorMessage, runTimeException);
            throw runTimeException;
        } finally {
            PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContext();
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Override
    public PayrollEmployee associateEmployeeWithRealm(String consumerRealmId, EmployeeIdentificationParams employeeIdentificationParams) {
        PayrollEmployee payrollEmployee = null;
        try {
            logger.info("Request received to connect to Payroll for consumerRealmId="+consumerRealmId);
            PayrollServices.beginUnitOfWork();
            //Need to strip out any dashes from the ssn
            String ssn = employeeIdentificationParams.getSsn();
            if(ssn != null) {
                ssn = ssn.replaceAll("-", "");
            }
            //PSP-3838 Don't allow query with default SSN, it will match a huge number of employees and cause memory issues
            if(Employee.DEFAULT_SSN.equals(ssn)) {
                logger.error("Invalid SSN, 000000000 is the default value for employees with no SSN");
                throw new ResourceNotFoundException(DataServiceException.ERRNUM_PAYROLLEMPLOYEE_RESOURCE_NOT_FOUND_DUE_TO_WRONG_SSN);
            }
            employeeIdentificationParams.setSsn(ssn);
            List<Employee> employees = EmployeeFinder.findEmployeesByTaxId(employeeIdentificationParams.getSsn());
            if(employees == null || employees.isEmpty()) {
                logger.info("Could not find Employee with SSN=*********"+". Error Code=" + DataServiceException.ERRNUM_PAYROLLEMPLOYEE_RESOURCE_NOT_FOUND_DUE_TO_WRONG_SSN);
                throw new ResourceNotFoundException(DataServiceException.ERRNUM_PAYROLLEMPLOYEE_RESOURCE_NOT_FOUND_DUE_TO_WRONG_SSN);
            }
            List<Paystub> lastTwoPaystubs;
            Paystub matchingPaystub = null;
            //This is the case where the same consumerRealmId already exists for an employee that matches the SSN / last paystub amount
            boolean matchingConsumerRealmIdExists = false;
            //This is the case where a different consumerRealmId already exists for an employee that matches the SSN / last paystub amount
            boolean consumerRealmIdMismatch = false;
            String mismatchedCompanyLegalName = "";
            for(Employee employee : employees) {
                lastTwoPaystubs = PaystubFinder.findLastTwoPaystubs(employee);
                matchingPaystub = matchPaystubNetAmount(employeeIdentificationParams.getLastPaycheckNetAmount(), lastTwoPaystubs);

                if(matchingPaystub != null) {
                    //Only want matches that do not already have a consumer realm id.  If in the end the only match we
                    // find already has a consumer realm id we will throw an error
                    if(employee.getConsumerRealmId() == null) {
                        if(employeeIdentificationParams.getLastName() != null) {
                            if(employee.getLastName() == null || !employee.getLastName().equalsIgnoreCase(employeeIdentificationParams.getLastName())) {
                                logger.info("Employee last name does not match or is null.  ExpectedEmployeeLastName=" + employee.getLastName() + " ProvidedEmployeeLastName=" + employeeIdentificationParams.getLastName());
                                throw new ResourceNotFoundException(DataServiceException.ERRNUM_PAYROLLEMPLOYEE_RESOURCE_NOT_FOUND);
                            }
                        }
                        //If there is a matching paystub, make sure the employer allows employees to self service sign up
                        if(isSelfServiceSignInEnabled(employee.getCompany().getIAMRealmId())) {
                            payrollEmployee = CdmFactory.createPayrollEmployee(employee);
                            employee.setConsumerRealmId(consumerRealmId);
                            Application.save(employee);
                            PayrollServices.commitUnitOfWork();
                            sendVmpEmployeeSignUpEmails(employee, employeeIdentificationParams.getIamEmail());
                            //Once we find a match no need to continue, make sure to remove any earlier found mismatches as we now had a success
                            consumerRealmIdMismatch = false;
                            break;
                        } else {
                            logger.info("Employee not allowed to Self-Service Sign-in. consumerRealmId=" + consumerRealmId +
                                                ". Error Code=" + DataServiceException.ERRNUM_EMPLOYEE_NOT_ALLOWED_TO_SELF_SERVICE_SIGN_IN);
                            throw new AccessDeniedException(DataServiceException.ERRNUM_EMPLOYEE_NOT_ALLOWED_TO_SELF_SERVICE_SIGN_IN);
                        }
                    } else if(!consumerRealmId.equalsIgnoreCase(employee.getConsumerRealmId())) {
                        consumerRealmIdMismatch = true;
                        mismatchedCompanyLegalName = employee.getCompany().getLegalName();
                    } else {
                        payrollEmployee = CdmFactory.createPayrollEmployee(employee);
                        matchingConsumerRealmIdExists = true;
                    }
                }
            }

            //If we go through all the employees attempting to match the net amount and find no matches it is the wrong amount
            if(matchingPaystub == null) {
                logger.info("Could not find matching paystub due to incorrect net amount for consumerRealmId=" + consumerRealmId +
                                    ". Error Code=" + DataServiceException.ERRNUM_PAYROLLEMPLOYEE_RESOURCE_NOT_FOUND_DUE_TO_WRONG_NET_AMOUNT);
                throw new ResourceNotFoundException(DataServiceException.ERRNUM_PAYROLLEMPLOYEE_RESOURCE_NOT_FOUND_DUE_TO_WRONG_NET_AMOUNT);
            }

            //We will throw an error for the most recent mismatch, assuming we did not find a match at some point
            if(!matchingConsumerRealmIdExists && consumerRealmIdMismatch) {
                logger.info("Employee already connected to consumerRealmId=" + consumerRealmId +
                                    ". Error Code=" + DataServiceException.ERRNUM_REALMID_ALREADY_EXIST);
                throw new AccessDeniedException(mismatchedCompanyLegalName, DataServiceException.ERRNUM_REALMID_ALREADY_EXIST);
            }
        } catch (RuntimeException runTimeException) {
            String errorMessage = "Error during associateEmployeeWithRealm for consumerRealmId=" + consumerRealmId;
            CdmHelper.logRunTimeException(logger, errorMessage, runTimeException);
            throw runTimeException;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        logger.info("Connect To Payroll successful for consumerRealmId="+consumerRealmId);
        return payrollEmployee;
    }

    private void sendVmpEmployeeSignUpEmails(Employee employee, String iamEmailAddress) {
        //Send emails, we don't want failures saving email info to the DB to prevent the consumer realm id
        // from being saved so each email is committed separately
        String employeeEmailAddress = employee.getEmail();
        //If employee email address is not empty send an email to that address
        if(employeeEmailAddress != null && !employeeEmailAddress.isEmpty()) {
            if(Validator.isValidEmail(employeeEmailAddress)) {
                try {
                    PayrollServices.beginUnitOfWork();
                    Application.refresh(employee);
                    CompanyEvent.createVmpSignUpEmployeeEmailEvent(employee, employeeEmailAddress);
                    PayrollServices.commitUnitOfWork();
                } catch (RuntimeException runTimeException) {
                    String errorMessage = "Error creating VmpSignUpEmployeeEmail for employee email address";
                    CdmHelper.logRunTimeException(logger, errorMessage, runTimeException);
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } else {
                logger.warn("Unable to send email, invalid employee emailAddress=" + employeeEmailAddress);
            }
        }
        //Send an email to the IAM email as long as it does not match the employee email address
        if(iamEmailAddress != null && !iamEmailAddress.isEmpty() && !iamEmailAddress.equalsIgnoreCase(employeeEmailAddress)) {
            if(Validator.isValidEmail(iamEmailAddress)) {
                try {
                    PayrollServices.beginUnitOfWork();
                    Application.refresh(employee);
                    CompanyEvent.createVmpSignUpEmployeeEmailEvent(employee, iamEmailAddress);
                    PayrollServices.commitUnitOfWork();
                } catch (RuntimeException runTimeException) {
                    String errorMessage = "Error creating VmpSignUpEmployeeEmail for IAM email address";
                    CdmHelper.logRunTimeException(logger, errorMessage, runTimeException);
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } else {
                logger.warn("Unable to send email, invalid  iamEmailAddress=" + iamEmailAddress);
            }
        }
        //Send the employer an email, only if they have active ViewMyPaycheck service. WHUB-862
        boolean hasActiveViewMyPaycheck = employee.getCompany().isCompanyOnService(ServiceCode.ViewMyPaycheck);
        if(hasActiveViewMyPaycheck) {
            try {
                PayrollServices.beginUnitOfWork();
                Application.refresh(employee);
                CompanyEvent.createVmpSignUpEmployerEmailEvent(employee);
                PayrollServices.commitUnitOfWork();
            } catch(RuntimeException runTimeException) {
                String errorMessage = "Error creating VmpSignUpEmployerEmail";
                CdmHelper.logRunTimeException(logger, errorMessage, runTimeException);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    //If a paystub in the list matches the passed in net amount return that paystub, otherwise return null
    private Paystub matchPaystubNetAmount(String inputNetAmountString, List<Paystub> paystubs) {
        BigDecimal inputNetAmount = CdmHelper.convertToFormattedBigDecimal(SpcfDecimal.createInstance(inputNetAmountString));
        for(Paystub paystub : paystubs) {
            if(inputNetAmount.equals(SpcfUtils.convertToBigDecimal(paystub.getNetPay()))) {
                return paystub;
            }
        }
        return null;
    }

    @Override
    public List<EmployeePreference> getEmployeePreferencesByApp(String consumerRealmId, String employeeId, String app) {
        List<EmployeePreference> employeePreferences = null;
        try {
            logger.info("Request received to get EmployeePreferences for consumerRealmId="+consumerRealmId+", EmployeeId="+employeeId+", for App="+app);
            List<Employee> employees = EmployeeFinder.findEmployees(consumerRealmId);
            if(employees != null && employees.size() != 0) {
                Employee employee = null;
                for (Employee ee : employees) {
                    if(ee.getId().toString().equals(employeeId))  {
                        employee = ee;
                        break;
                    }
                }

                List<PstubEmployeePreference> pstubEmployeePreferences = PstubEmployeePreference.getEmployeePreferencesByApp(employee, app);
                employeePreferences = new ArrayList<EmployeePreference>();
                if(pstubEmployeePreferences != null && pstubEmployeePreferences.size() > 0) {
                    for(PstubEmployeePreference pspPref : pstubEmployeePreferences) {
                        employeePreferences.add(CdmFactory.createEmployeePreference(pspPref));
                    }
                }
            }

            if(employeePreferences == null || employeePreferences.size() == 0) {
                logger.info("Could not find employee Preferences for EmployeeID="+employeeId+". Error Code="+DataServiceException.ERRNUM_PREFERENCE_NOT_FOUND);
                throw new ResourceNotFoundException(DataServiceException.ERRNUM_PREFERENCE_NOT_FOUND);
            }
        } catch (RuntimeException runTimeException) {
            String errorMessage = "Error getting employee preferences";
            CdmHelper.logRunTimeException(logger, errorMessage, runTimeException);
            throw runTimeException;
        }
        logger.info("Found EmployeePreferences for consumerRealmId="+consumerRealmId+", EmployeeId="+employeeId+", for App="+app);
        return employeePreferences;
    }

    @Override
    public List<EmployeePreference> createOrUpdateEmployeePreferences(String realmId, String employeeId, String appName, EmployeePreferenceParams preferenceParams) {
        List<EmployeePreference> employeePreferences = new ArrayList<EmployeePreference>();
        try {
            logger.info("Request received to create or update EmployeePreferences for consumerRealmId="+realmId+", EmployeeId="+employeeId+", for App="+appName+
                                ", Preference="+preferenceParams.getPreferenceName()+", Value="+preferenceParams.getPreferenceValue());
            List<Employee> employees = EmployeeFinder.findEmployees(realmId);
            if(employees != null && employees.size() != 0) {
                Employee employee = null;
                for (Employee ee : employees) {
                    if(ee.getId().toString().equals(employeeId))  {
                        employee = ee;
                        break;
                    }
                }
                if(employee == null)
                    throw new ResourceNotFoundException(DataServiceException.ERRNUM_PAYROLLEMPLOYEE_RESOURCE_NOT_FOUND);
                List<PstubEmployeePreference> pstubEmployeePreferences = new ArrayList<PstubEmployeePreference>();
                pstubEmployeePreferences.add(DomainFactory.createEmployeePreference(appName, preferenceParams, employee));

                for(PstubEmployeePreference pspPref : pstubEmployeePreferences) {
                    employeePreferences.add(CdmFactory.createEmployeePreference(PstubEmployeePreference.updateEmployeePreference(pspPref)));
                }
            }

            if(employeePreferences.size() == 0) {
                logger.info("Could not find employee Preferences for EmployeeID="+employeeId+". Error Code="+DataServiceException.ERRNUM_PREFERENCE_NOT_FOUND);
                throw new ResourceNotFoundException(DataServiceException.ERRNUM_PREFERENCE_NOT_FOUND);
            }
            logger.info("EmployeePreferences updated successfully for EmployeeId="+employeeId);
        } catch (ConstraintViolationException e) {
            String errorMessage = "Unable to create preferences. Another record with same employee, preference name & app name found.";
            CdmHelper.logRunTimeException(logger, errorMessage, e);
            throw new DataIntegrityViolationException(errorMessage, e);
        } catch (RuntimeException runTimeException) {
            String errorMessage = "Error creating / updating employee preference";
            CdmHelper.logRunTimeException(logger, errorMessage, runTimeException);
            throw runTimeException;
        }
        return employeePreferences;
    }

    @Override
    public List<PayrollEmployee> getPayrollEmployeesByCompanyRealmId(String companyRealmId) {
        try {
            logger.info("Request received to get PayrollEmployees by CompanyRealmId="+companyRealmId);
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            CdmHelper.checkIfCompanyIsOnVmpService(companyRealmId);
            List<Employee> employees = EmployeeFinder.findEmployeesByCompanyRealm(companyRealmId);
            List<VmpEmployeeInfo> vmpEmployeeInfos = EmployeeFinder.findVmpEmployeeInfosByCompanyRealm(companyRealmId);
            List<PayrollEmployee> payrollEmployees = new ArrayList<PayrollEmployee>();
            if((employees != null && !employees.isEmpty()) || (vmpEmployeeInfos != null && !vmpEmployeeInfos.isEmpty())) {
                for(Employee employee : employees) {
                    payrollEmployees.add(CdmFactory.createPayrollEmployee(employee));
                }
                for(VmpEmployeeInfo vmpEmployeeInfo : vmpEmployeeInfos){
                    payrollEmployees.add(CdmFactory.createPayrollEmployee(vmpEmployeeInfo));
                }
                logger.info("Found PayrolLEmployees for CompanyRealmId="+companyRealmId);
                return payrollEmployees;
            } else {
                logger.info("Could not find PayrollEmployees for CompanyRealmId="+companyRealmId+". Error Code="+DataServiceException.ERRNUM_PAYROLLEMPLOYEE_RESOURCE_NOT_FOUND);
                throw new ResourceNotFoundException(DataServiceException.ERRNUM_PAYROLLEMPLOYEE_RESOURCE_NOT_FOUND);
            }
        } catch(RuntimeException runTimeException) {
            String errorMessage = "Error during getPayrollEmployeesByCompanyRealmId";
            CdmHelper.logRunTimeException(logger, errorMessage, runTimeException);
            throw runTimeException;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private boolean isSelfServiceSignInEnabled(String companyRealmId) {
        //Default if no value is set is true
        boolean selfServiceSignInEnabled = true;
        if(companyRealmId != null && !companyRealmId.isEmpty()) {
            EmployerPreference employerPreference =
                    EmployerPreference.findEmployerPreference(companyRealmId, EmployerPreference.VMP, EmployerPreference.SELF_SERVICE_SIGN_IN);
            if(employerPreference != null) {
                if(EmployerPreference.OFF.equalsIgnoreCase(employerPreference.getPreferenceValue())) {
                    selfServiceSignInEnabled = false;
                }
            }
        }
        return selfServiceSignInEnabled;
    }

    @Override
    public PayrollEmployee updateEmployeeIsViewingPaystubDisabled(String companyRealmId, String employeeId, PayrollEmployee updateEmployee) {
        String identifyingLogInfo = " companyRealmId=" + companyRealmId + " employeeId=" + employeeId + " ";
        logger.info("Request received to update payroll employee" + identifyingLogInfo);
        PayrollEmployee payrollEmployee = null;
        if(updateEmployee != null) {
            try {
                Application.beginUnitOfWork();
                CdmHelper.checkIfCompanyIsOnVmpService(companyRealmId);
                SpcfUniqueId spcfEmployeeId = CdmHelper.createSpcfUniqueId(employeeId, DataServiceException.ERRNUM_PAYROLLEMPLOYEE_RESOURCE_NOT_FOUND);
                //Populate existing employee
                Employee employee = EmployeeFinder.findEmployeeByCompanyRealmAndEmployeeId(companyRealmId, spcfEmployeeId);
                if(employee == null) {
                    throw new ResourceNotFoundException(DataServiceException.ERRNUM_PAYROLLEMPLOYEE_RESOURCE_NOT_FOUND);
                }
                //We only allow updating whether the employee can view their paystubs online
                employee.setIsViewingPaystubDisabled(updateEmployee.isViewingPaystubDisabled());
                Application.save(employee);
                payrollEmployee = CdmFactory.createPayrollEmployee(employee);
                Application.commitUnitOfWork();
            } catch (RuntimeException runtimeException) {
                String errorMessage = "Error updating employee " + identifyingLogInfo;
                CdmHelper.logRunTimeException(logger, errorMessage, runtimeException);
                PayrollServices.rollbackUnitOfWork();
                throw runtimeException;
            }
        } else {
            //No employee to update passed in so invalid request
            throw new ValidationException(DataServiceException.ERRNUM_INVALID_REQUEST);
        }

        return payrollEmployee;
    }

    @Override
    public PayrollEmployee getPayrollEmployeeByCompanyRealmIdAndConsumerRealmId(String companyRealmId, String consumerRealmId) {
        try {
            logger.info(String.format("v4log Request received to get PayrollEmployee by CompanyRealmId=%s and consumerRealmId=%s",
                    companyRealmId, consumerRealmId));
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            CdmHelper.checkIfCompanyIsOnVmpService(companyRealmId);
            Employee employee = EmployeeFinder.findEmployeeByCompanyRealmIdAndConsumerRealmId(companyRealmId, consumerRealmId);
            if (employee != null) {
                logger.info(String.format("v4log Found PayrollEmployee for CompanyRealmId=%s and consumerRealmId=%s",
                        companyRealmId, consumerRealmId));
                return CdmFactory.createPayrollEmployee(employee);
            } else {
                VmpEmployeeInfo vmpEmployeeInfo = EmployeeFinder.findVmpEmployeeInfoByCompanyRealmIdAndConsumerRealmId(companyRealmId, consumerRealmId);
                if(vmpEmployeeInfo != null){
                    logger.info(String.format("v4log Found New PayrollEmployee for CompanyRealmId=%s and consumerRealmId=%s",
                            companyRealmId, consumerRealmId));
                    return CdmFactory.createPayrollEmployee(vmpEmployeeInfo);
                }
                logger.info(String.format("v4log Could not find PayrollEmployee for CompanyRealmId=%s and consumerRealmId=%s. Error Code=%s",
                        companyRealmId, consumerRealmId, DataServiceException.ERRNUM_PAYROLLEMPLOYEE_RESOURCE_NOT_FOUND));
                throw new ResourceNotFoundException(DataServiceException.ERRNUM_PAYROLLEMPLOYEE_RESOURCE_NOT_FOUND);
            }
        } catch(RuntimeException runTimeException) {
            String errorMessage = "v4log Error during getPayrollEmployeeByCompanyRealmIdAndConsumerRealmId";
            CdmHelper.logRunTimeException(logger, errorMessage, runTimeException);
            throw runTimeException;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    /**
     * VMP Hot-Fix for v4 APIs
     * corresponding to getPayrollEmployeeByCompanyRealmIdAndConsumerRealmId
     * @param companyUniqueId- can be either company seq or company realm id
     * @param consumerRealmId
     * @return
     */
    @Override
    public PayrollEmployee getPayrollEmployeeByCompanyUniqueIdAndConsumerRealmId(String companyUniqueId, String consumerRealmId) {
        logger.info("VMP fix is enabled. Executing getPayrollEmployeeByCompanyUniqueIdAndConsumerRealmId");
        try {
            logger.info(String.format("Request received to get PayrollEmployee by company unique identifier=%s and consumerRealmId=%s",
                    companyUniqueId, consumerRealmId));
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            CdmHelper.checkIfCompanyIsOnVmpServiceByCompanyUniqueId(companyUniqueId);
            Employee employee = EmployeeFinder.findEmployeeByCompanyUniqueIdAndConsumerRealmId(companyUniqueId, consumerRealmId);
            if (employee != null) {
                logger.info(String.format("Found PayrollEmployee for company unique identifier=%s and consumerRealmId=%s",
                        companyUniqueId, consumerRealmId));
                return CdmFactory.createPayrollEmployee(employee);
            } else {
                VmpEmployeeInfo vmpEmployeeInfo = EmployeeFinder.findVmpEmployeeInfoByCompanyUniqueIdAndConsumerRealmId(companyUniqueId, consumerRealmId);
                if(vmpEmployeeInfo != null){
                    logger.info(String.format("Found New PayrollEmployee for company unique identifier=%s and consumerRealmId=%s",
                            companyUniqueId, consumerRealmId));
                    return CdmFactory.createPayrollEmployee(vmpEmployeeInfo);
                }
                logger.info(String.format("Could not find PayrollEmployee for company unique identifier=%s and consumerRealmId=%s. Error Code=%s",
                        companyUniqueId, consumerRealmId, DataServiceException.ERRNUM_PAYROLLEMPLOYEE_RESOURCE_NOT_FOUND));
                throw new ResourceNotFoundException(DataServiceException.ERRNUM_PAYROLLEMPLOYEE_RESOURCE_NOT_FOUND);
            }
        } catch(RuntimeException runTimeException) {
            String errorMessage = "Error during getPayrollEmployeeByCompanyUniqueIdAndConsumerRealmId";
            CdmHelper.logRunTimeException(logger, errorMessage, runTimeException);
            throw runTimeException;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }
}
