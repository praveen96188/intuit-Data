package com.intuit.sbd.payroll.psp.adapters.cdmadapter.util;

import com.intuit.ems.dataservice.v1.exception.AccessDeniedException;
import com.intuit.ems.dataservice.v1.exception.DataServiceException;
import com.intuit.ems.dataservice.v1.exception.ResourceNotFoundException;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.finders.CompanyFinder;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.Paystub;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.VmpEmployeeInfo;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.math.BigDecimal;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;

public class CdmHelper {
    private static SpcfLogger logger = null;

    static {
        logger = Application.getLogger(CdmHelper.class);
    }

    //Allows a default value
    public static BigDecimal convertToFormattedBigDecimal(SpcfDecimal spcfDecimal, SpcfDecimal defaultValue) {
        BigDecimal returnValue = null;
        if(spcfDecimal == null) {
            returnValue = convertToFormattedBigDecimal(defaultValue);
        } else {
            returnValue = convertToFormattedBigDecimal(spcfDecimal);
        }
        return returnValue;
    }

    public static BigDecimal formatBigDecimal(BigDecimal bigDecimal) {
        BigDecimal result = null;
        if(bigDecimal != null) {
            result = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return result;
    }

    //Returns a big decimal with 2 decimal places and rounded up
    public static BigDecimal convertToFormattedBigDecimal(SpcfDecimal spcfDecimal) {
        BigDecimal bigDecimal = null;
        if(spcfDecimal != null) {
            bigDecimal = SpcfUtils.convertToBigDecimal(spcfDecimal).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return bigDecimal;
    }

    public static String formatAndMask(String unmasked) {
        String masked = null;
        if(unmasked != null) {
            masked = "....";
            unmasked = unmasked.replaceAll("[^\\d]", "");
            //Show the last 4
            int startIndex = 0;
            if(unmasked.length() > 4) {
                startIndex = unmasked.length() - 4;
            }
            masked += unmasked.substring(startIndex);
        }
        return  masked;
    }

    public static boolean notBlank(String number) {
        boolean blank = true;
        //Try to parse a decimal value value
        try {
            if(number != null) {
                BigDecimal decimalValue = new BigDecimal(number);
                if(BigDecimal.ZERO.compareTo(decimalValue) != 0) {
                    blank = false;
                }
            }
        } catch(RuntimeException e) {
            //May be in 0:00 format
            if(!"0:00".equals(number)) {
                blank = false;
            }
        }
        return !blank;
    }

    //QuickBooks has preferences for displaying quantity of time as a decimal or as HOURS:MINUTES format.
    // We want to respect the employers preference when returning this data, but we are not passing employer preferences
    // from QB....so instead we have to be able to parse either format so we can calculate total hours on a paystub
    public static String addHoursMinutes(String totalTime, String additionalTime) {
        String newTotalTime;
        try {
            //Determine the format
            if(additionalTime.contains(":")) {
                //HOURS:MINUTES format
                if(totalTime == null) {
                    totalTime = "0:00";
                }
                String[] totalTimeArray = totalTime.split(":");
                String[] additionalTimeArray = additionalTime.split(":");
                int totalHours = Integer.valueOf(totalTimeArray[0]);
                int totalMinutes = Integer.valueOf(totalTimeArray[1]);
                int additionalHours = Integer.valueOf(additionalTimeArray[0]);
                int additonalMinutes = Integer.valueOf(additionalTimeArray[1]);

                int newTotalHours = totalHours + additionalHours;
                int newTotalMinutes = totalMinutes + additonalMinutes;
                if(newTotalMinutes >= 60) {
                    //Add additional whole hours
                    newTotalHours += newTotalMinutes / 60;
                    newTotalMinutes = newTotalMinutes % 60;
                }
                //Reformat to string
                newTotalTime = newTotalHours + ":" + (newTotalMinutes < 10 ? "0" + newTotalMinutes : newTotalMinutes);
            } else {
                //Decimal format
                if(totalTime == null) {
                    totalTime = "0.00";
                }
                BigDecimal totalTimeDecimal = new BigDecimal(totalTime);
                BigDecimal additionalTimeDecimal = new BigDecimal(additionalTime);
                BigDecimal formattedTotalTime = CdmHelper.formatBigDecimal(totalTimeDecimal.add(additionalTimeDecimal));
                newTotalTime = formattedTotalTime.toString();
            }
        } catch (RuntimeException e) {
            //If we encounter a problem while adding we return the previous total
            newTotalTime = totalTime;
        }
        return  newTotalTime;
    }

    public static BigDecimal parseDecimal(String value) {
        BigDecimal decimal = null;
        if(value != null) {
            value = value.replaceAll("%", "");
            decimal = formatBigDecimal(new BigDecimal(value));
        }
        return decimal;
    }

    public static void logRunTimeException(SpcfLogger logger, String errorMessage, RuntimeException runTimeException) {
        //DataServiceException errors are not critical  but informative. Logging at error level sends email alerts out to
        //the PSP team so only do so in the case of unexpected errors
        if(runTimeException instanceof DataServiceException) {
            logger.info(errorMessage, runTimeException);
        } else {
            logger.error(errorMessage, runTimeException);
        }
    }

    //If we get an id that doesn't map to an SpcfUniqueId, this can happen when an iop id is passed in, we throw resource not found
    public static SpcfUniqueId createSpcfUniqueId(String id, int errorCode) {
        SpcfUniqueId spcfId = null;
        try {
            spcfId = SpcfUniqueId.createInstance(id);
        } catch(SpcfIllegalArgumentException e) {
            logger.info("ResourceNotFoundException errorCode=" + errorCode +  " due to invalid format id=" +id);
            throw new ResourceNotFoundException(errorCode);
        }
        return spcfId;
    }


    public static SpcfCalendar createSpcfCalendar(String dateString, long defaultMilliseconds) {
        SpcfCalendar calendar = null;
        if(dateString != null && !dateString.isEmpty()) {
            dateString += "Z";
            calendar = SpcfCalendar.fromISO8601(dateString);
        } else {
            //If no value is passed use the default
            calendar = SpcfCalendar.createInstance(defaultMilliseconds);
        }
        return calendar;
    }

    public static void checkIfCompanyIsOnVmpService(String companyRealmId) {
        Company company = CompanyFinder.findCompanyByCompanyRealmId(companyRealmId);
        checkIfCompanyIsOnVmpService(company);
    }

    public static void checkIfCompanyIsOnVmpService(Company company) { //TODO: Remove this
        if(!company.isCompanyOnService(ServiceCode.ViewMyPaycheck)) {
            logger.info("Company is not active on ViewMyPaycheck service. companyId=" + company.getId());
            throw new AccessDeniedException("Company is not active on ViewMyPaycheck service", DataServiceException.ERRNUM_COMPANY_DEACTIVATED_VIEW_MY_PAYCHECK);
        }
    }

    public static void checkIfEmployeeViewingPaystubDisabledUsingPaystubId(String paystubId) {
        Paystub paystub = Application.findById(Paystub.class, createSpcfUniqueId(paystubId, DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND));
        Employee employee = paystub.getPstubEmployeeInfo().getEmployee();
        checkIfEmployeeViewingPaystubDisabled(employee);
    }

    public static void checkIfEmployeeViewingPaystubDisabledUsingEmployeeId(String employeeId) {
        Employee employee = Application.findById(Employee.class, createSpcfUniqueId(employeeId, DataServiceException.ERRNUM_PAYROLLEMPLOYEE_RESOURCE_NOT_FOUND));
        if(employee == null){
            VmpEmployeeInfo vmpEmployeeInfo = Application.findById(VmpEmployeeInfo.class, createSpcfUniqueId(employeeId, DataServiceException.ERRNUM_PAYROLLEMPLOYEE_RESOURCE_NOT_FOUND));
            if(vmpEmployeeInfo != null){
                //Company must be on service as well for paystub viewing
                logger.info("This is a new employee and needs to be synced from Desktop for viewing paystubs. vmpEmployeeInfoId="+employeeId);
                checkIfCompanyIsOnVmpService(vmpEmployeeInfo.getCompany());
                return;
            }
        }
        checkIfEmployeeViewingPaystubDisabled(employee);
    }

    public static void checkIfEmployeeViewingPaystubDisabled(Employee employee) {
        //Company must be on service as well for paystub viewing
        checkIfCompanyIsOnVmpService(employee.getCompany());
        if(employee.getIsViewingPaystubDisabled()) {
            logger.info("Viewing paystubs is disabled for employeeId=" + employee.getId().toString() + " consumerRealmId=" + employee.getConsumerRealmId());
            throw new AccessDeniedException("Viewing paystubs is disabled for this employee", DataServiceException.ERRNUM_EMPLOYEE_VIEWING_PAYSTUBS_DISABLED);
        }
    }
    public static String maskValue(String value){

        return value.replaceAll("\\b(\\d{2})\\d+(\\d{2})","$1*******$2");
    }

    /**
     * VMP Hot-Fix for v4 APIs
     * corresponding to checkIfCompanyIsOnVmpService(companyRealmId)
     * @param companyUniqueId- can be either company seq or company realm id
     */
    public static void checkIfCompanyIsOnVmpServiceByCompanyUniqueId(String companyUniqueId) {
        //Check if at least one company's VMP is enabled else throw AccessDeniedException
        
        //This list is not empty else this will throw resourcenotfound exception.
        DomainEntitySet<Company> companySet = CompanyFinder.findCompanyByCompanyUniqueId(companyUniqueId);
        
        Predicate<Company> isVmpCompany = company -> company.isCompanyOnService(ServiceCode.ViewMyPaycheck);
        Set<Company> vmpCompanySet = companySet.stream().filter(isVmpCompany).collect(Collectors.<Company>toSet());
        logger.info("Company Unique Id = "+ companyUniqueId + ". VMP enabled companies count = "+ vmpCompanySet.size());
        
        if(CollectionUtils.isEmpty(vmpCompanySet)) {
            throw new AccessDeniedException("Company is not active on ViewMyPaycheck service", DataServiceException.ERRNUM_COMPANY_DEACTIVATED_VIEW_MY_PAYCHECK);
        }
    }

    /**
     * VMP Hot-Fix for v4 APIs
     * Validate if the string value
     * is a SpcfUniqueId or not
     * @param value
     * @return
     */
    public static boolean isSpcfUniqueId(String value){
        try {
            SpcfUniqueId.createInstance(value);
        }catch(SpcfIllegalArgumentException ex){
            return false;
        }
        return true;
    }
}
