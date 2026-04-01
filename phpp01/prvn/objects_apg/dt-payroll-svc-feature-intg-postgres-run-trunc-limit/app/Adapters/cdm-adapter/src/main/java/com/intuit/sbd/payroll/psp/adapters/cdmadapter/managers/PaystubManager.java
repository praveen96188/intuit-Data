package com.intuit.sbd.payroll.psp.adapters.cdmadapter.managers;

import com.intuit.ems.dataservice.model.PaystubWrapper;
import com.intuit.ems.dataservice.v1.manager.IPaystubManager;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.factories.CdmFactory;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.finders.EmployeeFinder;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.finders.PaystubFinder;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.util.CdmHelper;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.schema.ems.v3.Paystub;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaystubManager implements IPaystubManager {
    private static SpcfLogger logger = null;
    private static String startDateKey = "paycheckStartDate";
    private static String endDateKey = "paycheckEndDate";
    private static final String W4_2019 = "2019ORBEFORE";
    private static final String NO = "FALSE";
    static {
        logger = Application.getLogger(PaystubManager.class);
    }

    @Override
    public List<Paystub> getPaystubs(String realmId, String checkDateStart, String checkDateEnd, int start, int size) {
        throw new UnsupportedOperationException("No longer needed / implemented");
    }

    @Override
    public Paystub getPaystubForEmployee(String consumerRealmId, String paystubId) {
        logger.info(String.format("v4log getPaystubForEmployee consumerRealmId=%s paystubId=%s",consumerRealmId, paystubId));
        Paystub cdmPaystub = null;
        try {
            Application.beginUnitOfWork(FlushMode.MANUAL, true);
            CdmHelper.checkIfEmployeeViewingPaystubDisabledUsingPaystubId(paystubId);
            com.intuit.sbd.payroll.psp.domain.Paystub paystub = PaystubFinder.findPaystubForEmployee(consumerRealmId, paystubId);
            cdmPaystub = CdmFactory.createPaystub(paystub);
        } catch (RuntimeException runTimeException) {
            String errorMessage = "v4log Error getting paystub for consumerRealmId=" + consumerRealmId + " paystubId=" + paystubId;
            CdmHelper.logRunTimeException(logger, errorMessage, runTimeException);
            throw runTimeException;
        } finally {
            Application.rollbackUnitOfWork();
        }
        return cdmPaystub;
    }

    public Paystub findLastPaystub(String realmId, String consumerRealmId){
        try {
            logger.info(String.format("v4log findLastPaystub started consumerRealmId=%s realmId=%s",
                    consumerRealmId, realmId));
            Application.beginUnitOfWork(FlushMode.MANUAL, true);
            Employee employee = EmployeeFinder.findEmployeeByCompanyRealmIdAndConsumerRealmId(realmId, consumerRealmId);
            return CdmFactory.createPaystub(PaystubFinder.findLastPaystub(employee));
        } catch (RuntimeException runTimeException) {
            String errorMessage = "v4log Error getting paystub for consumerRealmId=" + consumerRealmId + " realmId=" + realmId;
            CdmHelper.logRunTimeException(logger, errorMessage, runTimeException);
            throw runTimeException;
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    public List<Paystub> getPaystubsForEmployee(String consumerRealmId, List<String> paystubIds) {
        List<Paystub> paystubList = new ArrayList<>();
        if(paystubIds == null || paystubIds.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            logger.info(String.format("v4log getPaystubsForEmployee started consumerRealmId=%s",consumerRealmId));
            Application.beginUnitOfWork(FlushMode.MANUAL, true);
            //checking only one paystub as it will return right result about whether company is subscrbed to service or not.
            CdmHelper.checkIfEmployeeViewingPaystubDisabledUsingPaystubId(paystubIds.get(0));
            List<com.intuit.sbd.payroll.psp.domain.Paystub> paystubs = PaystubFinder
                    .findPaystubsForEmployee(consumerRealmId, paystubIds);
            for(com.intuit.sbd.payroll.psp.domain.Paystub paystub: paystubs) {
                paystubList.add(CdmFactory.createPaystub(paystub));
            }
        } catch (RuntimeException runTimeException) {
            String errorMessage = "v4log Error getting paystub for consumerRealmId=" + consumerRealmId
                    + " paystubId=" + String.join(", ", paystubIds);
            CdmHelper.logRunTimeException(logger, errorMessage, runTimeException);
            throw runTimeException;
        } finally {
            Application.rollbackUnitOfWork();
        }
        return paystubList;
    }

    @Override
    public Paystub getPaystubForCompany(String companyRealmId, String paystubId) {
        logger.info(String.format("v4log getPaystubForCompany started companyRealmId=%s",companyRealmId));
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Paystub cdmPaystub = null;
        try {
            Application.beginUnitOfWork(FlushMode.MANUAL, true);
            CdmHelper.checkIfCompanyIsOnVmpService(companyRealmId);
            com.intuit.sbd.payroll.psp.domain.Paystub paystub = PaystubFinder.findPaystubForCompany(companyRealmId, paystubId);
            cdmPaystub = CdmFactory.createPaystub(paystub);
        } catch (RuntimeException runTimeException) {
            String errorMessage = "v4log Error getting paystub for companyRealmId=" + companyRealmId + " paystubId=" + paystubId;
            CdmHelper.logRunTimeException(logger, errorMessage, runTimeException);
            throw runTimeException;
        } finally {
            Application.rollbackUnitOfWork();
        }
        stopWatch.stop();
        logger.info("v4log getPaystubForCompany for companyRealmId="+companyRealmId+" paystubId="+paystubId+". Elapsed_time="+stopWatch);
        return cdmPaystub;
    }

    @Override
    public List<Paystub> getPaystubsByConsumerRealmIdAndEmployeeId(String consumerRealmId, String employeeId, String checkDateStart, String checkDateEnd, int start, int size) {
        List<Paystub> cdmPaystubs = new ArrayList<Paystub>();
        try {
            Long startTime = System.currentTimeMillis();
            logger.info("v4log CDMAdapterAPI=PaystubsByConsumerRealmIdAndEmployeeId Started processing request for consumerRealmId=" + (consumerRealmId == null ? "NULL" : consumerRealmId) + " and employeeId=" + (employeeId == null ? "NULL" : employeeId) + " start=" + start + " size=" + size + " checkDateStart=" + (checkDateStart == null ? "NULL" : checkDateStart) + " checkDateEnd=" + (checkDateEnd == null ? "NULL" : checkDateEnd));
            Application.beginUnitOfWork(FlushMode.MANUAL, true);
            CdmHelper.checkIfEmployeeViewingPaystubDisabledUsingEmployeeId(employeeId);
            if(!isSizeWithinThrottlingLimit(size)){
                Map<String, String> dates = getStartAndEndPaycheckDates(checkDateStart, checkDateEnd);
                checkDateStart = dates.get(startDateKey);
                checkDateEnd = dates.get(endDateKey);
            }
            List<com.intuit.sbd.payroll.psp.domain.Paystub> paystubs =
                    PaystubFinder.findPaystubsByConsumerRealmAndEmployeeId(consumerRealmId, employeeId, checkDateStart, checkDateEnd, start, size);
            for (com.intuit.sbd.payroll.psp.domain.Paystub paystub : paystubs) {
                cdmPaystubs.add(CdmFactory.createPaystub(paystub));
            }
            Long endTime = System.currentTimeMillis();
            logger.info("v4log CDMAdapterAPI=PaystubsByConsumerRealmIdAndEmployeeId Finished processing request for consumerRealmId=" + (consumerRealmId == null ? "NULL" : consumerRealmId) + " and employeeId=" + (employeeId == null ? "NULL" : employeeId) + " ELAPSED_TIME=" + (endTime - startTime));
        } catch (RuntimeException runTimeException) {
            String errorMessage = "v4log CDMAdapterAPI=PaystubsByConsumerRealmIdAndEmployeeId cdmstatus=Error getting paystubs for consumerRealmId=" + (consumerRealmId == null ? "NULL" : consumerRealmId) + " and employeeId=" + (employeeId == null ? "" : employeeId) + " start=" + start + " size=" + size + " checkDateStart=" + (checkDateStart == null ? "NULL" : checkDateStart) + " checkDateEnd=" + (checkDateEnd == null ? "NULL" : checkDateEnd);
            CdmHelper.logRunTimeException(logger, errorMessage, runTimeException);
            throw runTimeException;
        } finally {
            Application.rollbackUnitOfWork();
        }
        return cdmPaystubs;
    }

    @Override
    public List<Paystub> getPaystubsByCompanyRealmIdAndEmployeeId(String companyRealmId, String employeeId, String checkDateStart, String checkDateEnd, int start, int size) {
        List<Paystub> cdmPaystubs = new ArrayList<Paystub>();
        try {
            Long startTime = System.currentTimeMillis();
            logger.info("v4log CDMAdaterAPI=PaystubsByCompanyRealmIdAndEmployeeId Started processing request for companyRealmId=" + (companyRealmId == null ? "NULL" : companyRealmId) + " and employeeId=" + (employeeId == null ? "NULL" : employeeId) + " start=" + start + " size=" + size + " checkDateStart=" + (checkDateStart == null ? "NULL" : checkDateStart) + " checkDateEnd=" + (checkDateEnd == null ? "NULL" : checkDateEnd));
            Application.beginUnitOfWork(FlushMode.MANUAL, true);
            CdmHelper.checkIfCompanyIsOnVmpService(companyRealmId);
            if(!isSizeWithinThrottlingLimit(size)){
                Map<String, String> dates = getStartAndEndPaycheckDates(checkDateStart, checkDateEnd);
                checkDateStart = dates.get(startDateKey);
                checkDateEnd = dates.get(endDateKey);
            }
            List<com.intuit.sbd.payroll.psp.domain.Paystub> paystubs =
                    PaystubFinder.findPaystubsByCompanyRealmidAndEmployeeId(companyRealmId, employeeId, checkDateStart, checkDateEnd, start, size);

            for (com.intuit.sbd.payroll.psp.domain.Paystub paystub : paystubs) {
                cdmPaystubs.add(CdmFactory.createPaystub(paystub));
            }
            Long endTime = System.currentTimeMillis();
            logger.info("v4log CDMAdapterAPI=PaystubsByCompanyRealmIdAndEmployeeId Finished processing request for companyRealmId=" + (companyRealmId == null ? "NULL" : companyRealmId) + " and employeeId=" + (employeeId == null ? "" : employeeId) + " ELAPSED_TIME=" + (endTime - startTime));
        } catch (RuntimeException runTimeException) {
            String errorMessage = "v4log CDMAdapterAPI=PaystubsByCompanyRealmIdAndEmployeeId cdmstatus=Error getting paystubs for companyRealmId=" + (companyRealmId == null ? "NULL" : companyRealmId) + " and employeeId=" + (employeeId == null ? "NULL" : employeeId) + " start=" + start + " size=" + size + " checkDateStart=" + (checkDateStart == null ? "NULL" : checkDateStart) + " checkDateEnd=" + (checkDateEnd == null ? "NULL" : checkDateEnd);
            CdmHelper.logRunTimeException(logger, errorMessage, runTimeException);
            throw runTimeException;
        } finally {
            Application.rollbackUnitOfWork();
        }
        return cdmPaystubs;
    }

    /**
     *
     * @param pCheckStartDate
     * @param pCheckEndDate
     * @return
     */
    public static Map<String, String> getStartAndEndPaycheckDates(String pCheckStartDate, String pCheckEndDate) {
        Map<String, String> dateMap = new HashMap<String, String>();

        if (pCheckStartDate != null && pCheckStartDate.trim().length() > 0) {
            dateMap.put(startDateKey, pCheckStartDate);
            dateMap.put(endDateKey, pCheckEndDate);
            return dateMap;
        }
        int lookBackMonths = SystemParameter.findIntValue(SystemParameter.Code.VMP_PAYCHECK_PERIOD_LOOK_BACK_MONTHS, 0);
        if (lookBackMonths <= 0) {
            dateMap.put(startDateKey, pCheckStartDate);
            dateMap.put(endDateKey, pCheckEndDate);
            return dateMap;
        }
        if (pCheckEndDate != null && pCheckEndDate.trim().length() > 0) {
            SpcfCalendar startDate = SpcfCalendar.fromISO8601(pCheckEndDate + "Z");
            startDate.addMonths(-lookBackMonths);
            startDate.setValues(startDate.getYear(), startDate.getMonth(), 1);
            pCheckStartDate = startDate.format("yyyy-MM-dd").toString();
        } else {
            SpcfCalendar startDate = PSPDate.getPSPTime().copy();
            startDate.addMonths(-lookBackMonths);
            startDate.setValues(startDate.getYear(), startDate.getMonth(), 1);
            pCheckStartDate = startDate.format("yyyy-MM-dd").toString();
            pCheckEndDate = ""; //Need to show future  paystubs
        }

        dateMap.put(startDateKey, pCheckStartDate);
        dateMap.put(endDateKey, pCheckEndDate);
        return dateMap;
    }

    /**
     *
     * @param size
     * @return
     */
    private static boolean isSizeWithinThrottlingLimit(int size) {
        //If size in request is less than VMP_SIZE_THROTTLING_VALUE value, then VMP_PAYCHECK_PERIOD_LOOK_BACK_MONTHS will not be used to look back.
        int sizeTrottlingValue = SystemParameter.findIntValue(SystemParameter.Code.VMP_SIZE_THROTTLING_VALUE, 0);
        if (sizeTrottlingValue > 0 && size <= sizeTrottlingValue) {
            return true;
        }
        return false;
    }

    /**
     * VMP Hot-Fix for V4 APIs
     * corresponding to findLastPaystub
     * @param companyUniqueId- can be either company seq or company realm id
     * @param consumerRealmId
     * @return
     */
    @Override
    public Paystub findLastPaystubByCompanyUniqueId(String companyUniqueId, String consumerRealmId){
        logger.info("VMP fix is enabled. Executing findLastPaystubByCompanyUniqueId");
        try {
            Application.beginUnitOfWork(FlushMode.MANUAL, true);
            Employee employee = EmployeeFinder.findEmployeeByCompanyUniqueIdAndConsumerRealmId(companyUniqueId, consumerRealmId);
            return CdmFactory.createPaystub(PaystubFinder.findLastPaystub(employee));
        } catch (RuntimeException runTimeException) {
            String errorMessage = "Error getting paystub for company unique identifier=" + companyUniqueId + " consumerRealmId=" + consumerRealmId;
            CdmHelper.logRunTimeException(logger, errorMessage, runTimeException);
            throw runTimeException;
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    /**
     * VMP Hot-Fix for V4 APIs
     * corresponding to findLastPaystubInfo
     * @param companyUniqueId- can be either company seq or company realm id
     * @param consumerRealmId
     * @return PaystubWrapper (paystub and w42020 federal tax information)
     */
    @Override
    public PaystubWrapper findLastPaystubInfoByCompanyUniqueId(String companyUniqueId, String consumerRealmId){
        try {
            PaystubWrapper paystubWrapper = null;
            Application.beginUnitOfWork(FlushMode.MANUAL, true);
            Employee employee = EmployeeFinder.findEmployeeByCompanyUniqueIdAndConsumerRealmId(companyUniqueId, consumerRealmId);
            boolean isWorkforcePaystubsFixEnabled= FeatureFlags.get().booleanValue(FeatureFlags.Key.WORKFORCE_PAYSTUB_FIX, false);
            if(isWorkforcePaystubsFixEnabled){
                //Employee not found in Employee Table
                if (employee == null) {
                    return paystubWrapper;
                }
            }
            com.intuit.sbd.payroll.psp.domain.Paystub domainPaystub = PaystubFinder.findLastPaystub(employee);
            Paystub paystub =  CdmFactory.createPaystub(domainPaystub);
            if(isWorkforcePaystubsFixEnabled) {
                //Employee Record found but no paystub created
                if (paystub == null) {
                    return paystubWrapper;
                }
            }
            // Fill the paystub wrapper and send to payroll data service
            paystubWrapper = new PaystubWrapper(paystub,
                    CdmHelper.convertToFormattedBigDecimal(domainPaystub.getPstubEmployeeInfo().getFedClaimDependents(), SpcfDecimal.createInstance(0.0)),
                    CdmHelper.convertToFormattedBigDecimal(domainPaystub.getPstubEmployeeInfo().getFedOtherIncome(),  SpcfDecimal.createInstance(0.0)),
                    CdmHelper.convertToFormattedBigDecimal(domainPaystub.getPstubEmployeeInfo().getFedDeductions(), SpcfDecimal.createInstance(0.0)),
                    domainPaystub.getPstubEmployeeInfo().getFedMultipleJobs() == null ? NO: domainPaystub.getPstubEmployeeInfo().getFedMultipleJobs(),
                    domainPaystub.getPstubEmployeeInfo().getFedW4EmployeePref() == null ? W4_2019: domainPaystub.getPstubEmployeeInfo().getFedW4EmployeePref());
            return paystubWrapper;
        } catch (RuntimeException runTimeException) {
            String errorMessage = "Error getting paystub for company unique identifier=" + companyUniqueId + " consumerRealmId=" + consumerRealmId;
            CdmHelper.logRunTimeException(logger, errorMessage, runTimeException);
            throw runTimeException;
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    /**
     * VMP Hot-Fix for V4 APIs
     * coresponding to getPaystubForCompany
     * @param companyUniqueId- can be either company seq or company realm id
     * @param paystubId
     * @return
     */
    @Override
    public Paystub getPaystubForCompanyByCompanyUniqueId(String companyUniqueId, String paystubId) {
        logger.info("VMP fix is enabled. Executing getPaystubForCompanyByCompanyUniqueId");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Paystub cdmPaystub = null;
        try {
            Application.beginUnitOfWork(FlushMode.MANUAL, true);
            CdmHelper.checkIfCompanyIsOnVmpServiceByCompanyUniqueId(companyUniqueId);
            com.intuit.sbd.payroll.psp.domain.Paystub paystub = PaystubFinder.findPaystubForCompanyByCompanyUniqueId(companyUniqueId, paystubId);
            cdmPaystub = CdmFactory.createPaystub(paystub);
        } catch (RuntimeException runTimeException) {
            String errorMessage = "Error getting paystub for company unique identifier=" + companyUniqueId + " paystubId=" + paystubId;
            CdmHelper.logRunTimeException(logger, errorMessage, runTimeException);
            throw runTimeException;
        } finally {
            Application.rollbackUnitOfWork();
        }
        stopWatch.stop();
        logger.info("getPaystubForCompanyByCompanyUniqueId for company unique identifier="+companyUniqueId+" paystubId="+paystubId+". Elapsed_time="+stopWatch);
        return cdmPaystub;
    }

    /**
     * VMP Hot-Fix for V4 APIs
     * coresponding to getPaystubsByCompanyRealmIdAndEmployeeId
     * @param companyUniqueId- can be either company seq or company realm id
     * @param employeeId
     * @param checkDateStart
     * @param checkDateEnd
     * @param start
     * @param size
     * @return
     */
    @Override
    public List<Paystub> getPaystubsByCompanyUniqueIdAndEmployeeId(String companyUniqueId, String employeeId, String checkDateStart, String checkDateEnd, int start, int size) {
        logger.info("VMP fix is enabled. Executing getPaystubsByCompanyUniqueIdAndEmployeeId");
        List<Paystub> cdmPaystubs = new ArrayList<Paystub>();
        try {
            Long startTime = System.currentTimeMillis();
            logger.info("CDMAdapterAPI=getPaystubsByCompanyUniqueIdAndEmployeeId Started processing request for company unique identifier=" + (companyUniqueId == null ? "NULL" : companyUniqueId) + " and employeeId=" + (employeeId == null ? "NULL" : employeeId) + " start=" + start + " size=" + size + " checkDateStart=" + (checkDateStart == null ? "NULL" : checkDateStart) + " checkDateEnd=" + (checkDateEnd == null ? "NULL" : checkDateEnd));
            Application.beginUnitOfWork(FlushMode.MANUAL, true);
            CdmHelper.checkIfCompanyIsOnVmpServiceByCompanyUniqueId(companyUniqueId);
            if(!isSizeWithinThrottlingLimit(size)){
                Map<String, String> dates = getStartAndEndPaycheckDates(checkDateStart, checkDateEnd);
                checkDateStart = dates.get(startDateKey);
                checkDateEnd = dates.get(endDateKey);
            }
            List<com.intuit.sbd.payroll.psp.domain.Paystub> paystubs =
                    PaystubFinder.findPaystubsByCompanyUniqueIdAndEmployeeId(companyUniqueId, employeeId, checkDateStart, checkDateEnd, start, size);

            for (com.intuit.sbd.payroll.psp.domain.Paystub paystub : paystubs) {
                cdmPaystubs.add(CdmFactory.createPaystub(paystub));
            }
            Long endTime = System.currentTimeMillis();
            logger.info("CDMAdapterAPI=getPaystubsByCompanyUniqueIdAndEmployeeId Finished processing request for company unique identifier=" + (companyUniqueId == null ? "NULL" : companyUniqueId) + " and employeeId=" + (employeeId == null ? "" : employeeId) + " ELAPSED_TIME=" + (endTime - startTime));
        } catch (RuntimeException runTimeException) {
            String errorMessage = "CDMAdapterAPI=getPaystubsByCompanyUniqueIdAndEmployeeId cdmstatus=Error getting paystubs for company unique identifier=" + (companyUniqueId == null ? "NULL" : companyUniqueId) + " and employeeId=" + (employeeId == null ? "NULL" : employeeId) + " start=" + start + " size=" + size + " checkDateStart=" + (checkDateStart == null ? "NULL" : checkDateStart) + " checkDateEnd=" + (checkDateEnd == null ? "NULL" : checkDateEnd);
            CdmHelper.logRunTimeException(logger, errorMessage, runTimeException);
            throw runTimeException;
        } finally {
            Application.rollbackUnitOfWork();
        }
        return cdmPaystubs;
    }

    /**
     * VMP Hot-Fix for V4 APIs
     * coresponding to getPaystubsForEmployee
     * @param consumerRealmId
     * @param paystubIds
     * @return
     */
    @Override
    public List<Paystub> getPaystubsForEmployeeByCompanyUniqueId(String consumerRealmId, List<String> paystubIds) {

            logger.info("No impact of company unique id. Calling getPaystubsForEmployee");
            return getPaystubsForEmployee(consumerRealmId,paystubIds);
    }
}
