package com.intuit.sbd.payroll.psp.adapters.cdmadapter.finders;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import org.apache.commons.collections4.CollectionUtils;

import com.intuit.ems.dataservice.v1.exception.DataServiceException;
import com.intuit.ems.dataservice.v1.exception.ResourceNotFoundException;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.util.CdmHelper;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EmployerPreference;
import com.intuit.sbd.payroll.psp.domain.PaycheckStatusCode;
import com.intuit.sbd.payroll.psp.domain.Paystub;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Property;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.kafka.common.protocol.types.Field;

public class PaystubFinder {
    private static SpcfLogger logger = null;

    static {
        logger = Application.getLogger(PaystubFinder.class);
    }

    public static Paystub findPaystubForEmployee(String consumerRealmId, String id) {
        logger.info(String.format("v4log findPaystubForEmployee started consumerRealmId=%s",consumerRealmId));
        Expression<Paystub> query = new Query<Paystub>()
                .Where(Paystub.PstubEmployeeInfo().Employee().ConsumerRealmId().equalTo(consumerRealmId)
                .And(Paystub.Id().equalTo(CdmHelper.createSpcfUniqueId(id, DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND))))
                .EagerLoad(getPaystubEagerLoadProperties());
        DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class, query);
        if (paystubs.size() > 1) {
            logger.error("v4log Query for paystub with id=" + id + " and consumerRealmId=" + consumerRealmId +
                            " returned multiple results, when one was expected.");
        }
        Paystub paystub = paystubs.getFirst();
        if (paystub == null) {
            logger.info(String.format("v4log Paystub not found. consumerRealmId=%s ErrorCode=%s",consumerRealmId,
                    DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND));
            throw new ResourceNotFoundException(DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND);
        }
        return paystub;
    }

    public static List<Paystub> findPaystubsForEmployee(String consumerRealmId, Collection<String> ids) {
        logger.info(String.format("v4log findPaystubsForEmployee started consumerRealmId=%s",consumerRealmId));
        if(ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<SpcfUniqueId> uIds = new ArrayList<>();
        for(String id : ids) {
            uIds.add(CdmHelper.createSpcfUniqueId(id, DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND));
        }
        Expression<Paystub> query = new Query<Paystub>()
                .Where(Paystub.PstubEmployeeInfo().Employee().ConsumerRealmId().equalTo(consumerRealmId)
                        .And(Paystub.Id().in(uIds)))
                .OrderBy(Paystub.PaycheckDate().Descending())
                .EagerLoad(getPaystubEagerLoadProperties());
        DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class, query);
        List<Paystub> paystubList = new ArrayList<>();
        paystubList.addAll(paystubs);
        if(paystubList.isEmpty()) {
            logger.info(String.format("v4log Paystub not found. consumerRealmId=%s ErrorCode=%s",consumerRealmId,
                    DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND));
            throw new ResourceNotFoundException(DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND);
        }
        return paystubList;
    }

    public static Paystub findPaystubForCompany(String companyRealmId, String id) {
        logger.info("v4log Finding paystub for companyRealmId="+companyRealmId+" and paystubId="+id);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Expression<Paystub> query = new Query<Paystub>()
                .Where(Paystub.PstubEmployeeInfo().Employee().Company().IAMRealmId().equalTo(companyRealmId)
                .And(Paystub.Id().equalTo(CdmHelper.createSpcfUniqueId(id, DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND))))
                .OrderBy(Paystub.PaycheckDate().Descending())
                .EagerLoad(getPaystubEagerLoadProperties());
        DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class, query);
        if (paystubs.size() > 1) {
            logger.error("v4log Query for paystub with id=" + id + " and companyRealmId=" + companyRealmId +
                                " returned multiple results, when one was expected.");
        }
        Paystub paystub = paystubs.getFirst();
        if (paystub == null) {
            stopWatch.stop();
            logger.info(String.format("v4log Paystub not found. companyRealmId=%s paystubId=%s ErrorCode=%s Elapsed_time=%s",
                    companyRealmId, id,+DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND, stopWatch ));
            throw new ResourceNotFoundException(DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND);
        }
        stopWatch.stop();
        logger.info("v4log Found paystub for companyRealmId="+companyRealmId+" and paystubId="+id+". Elapsed_time="+stopWatch);
        return paystub;
    }

    public static List<Paystub> findPaystubsByConsumerRealmAndEmployeeId(String consumerRealmId,
                                                                         String employeeId,
                                                                         String checkDateStart,
                                                                         String checkDateEnd,
                                                                         int start,
                                                                         int size) {
        logger.info(String.format("v4log findPaystubsByConsumerRealmAndEmployeeId started consumerRealmId=%s employeeId=%s checkDateStart=%s checkDateEnd=%s",
                consumerRealmId, employeeId, checkDateStart, checkDateEnd));
        List<Paystub> paystubList = new ArrayList<Paystub>();
        Employee employee = Application.findById(Employee.class, CdmHelper.createSpcfUniqueId(employeeId, DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND));
        if(employee != null) {
            Company company = employee.getCompany();
            Expression<Paystub> query = new Query<Paystub>()
                    .Where(Paystub.PstubEmployeeInfo().Employee().ConsumerRealmId().equalTo(consumerRealmId)
                    .And(Paystub.PstubEmployeeInfo().Employee().Id().equalTo(CdmHelper.createSpcfUniqueId(employeeId, DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND)))
                    .And((Paystub.Paycheck().SourcePaycheckId().like("-%")).Not())
                    .And(Paystub.PaycheckDate().between(CdmHelper.createSpcfCalendar(checkDateStart, SpcfCalendar.MinMillisecond),
                                                        calculateCheckDateEnd(company.getIAMRealmId(), checkDateEnd)))
                    .And(Paystub.Paycheck().Status().equalTo(PaycheckStatusCode.Active)))
                    .OrderBy(Paystub.PaycheckDate().Descending())
                    .EagerLoad(getPaystubEagerLoadProperties())
                    .LimitResults(start, size);
            DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class, query);
            paystubList.addAll(paystubs);
        }

        if(paystubList.isEmpty()) {
            logger.info(String.format("v4log Paystub not found. consumerRealmId=%s ErrorCode=%s",consumerRealmId,
                    DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND));
            throw new ResourceNotFoundException(DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND);
        }
        return paystubList;
    }

    public static List<Paystub> findPaystubsByCompanyRealmidAndEmployeeId(String companyRealmId,
                                                                          String employeeId,
                                                                          String checkDateStart,
                                                                          String checkDateEnd,
                                                                          int start,
                                                                          int size) {
        Expression<Paystub> query = new Query<Paystub>()
                .Where(Paystub.Paycheck().Company().IAMRealmId().equalTo(companyRealmId)
                .And(Paystub.PstubEmployeeInfo().Employee().Id().equalTo(CdmHelper.createSpcfUniqueId(employeeId, DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND)))
                .And(Paystub.Paycheck().SourcePaycheckId().greaterThan("0"))
                .And(Paystub.PaycheckDate().between(CdmHelper.createSpcfCalendar(checkDateStart, SpcfCalendar.MinMillisecond),
                                                    calculateCheckDateEnd(companyRealmId, checkDateEnd)))
                .And(Paystub.Paycheck().Status().equalTo(PaycheckStatusCode.Active)))
                .OrderBy(Paystub.PaycheckDate().Descending())
                .EagerLoad(getPaystubEagerLoadProperties())
                .LimitResults(start, size);
        DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class, query);
        if (paystubs == null || paystubs.isEmpty()) {
            logger.info(String.format("v4log Paystub not found. companyRealmId=%s employeeId=%s ErrorCode=%s",
                    companyRealmId, employeeId, DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND));
            throw new ResourceNotFoundException(DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND);
        }
        List<Paystub> paystubList = new ArrayList<Paystub>();
        paystubList.addAll(paystubs);
        return paystubList;
    }

    public static Paystub findFirstPaystub(Employee employee) {
        logger.info(String.format("v4log findFirstPaystub started consumerRealmId=%s employeeId=%s",
                employee.getConsumerRealmId(), employee.getId()));
        long startTime = System.currentTimeMillis();//Save start time to calculate final elapsed time
        Paystub paystub = null;
        Expression<Paystub> query = new Query<Paystub>()
            .Where(Paystub.PstubEmployeeInfo().Employee().equalTo(employee)
            .And(Paystub.Paycheck().SourcePaycheckId().greaterThan("0"))
            .And(Paystub.Paycheck().Status().equalTo(PaycheckStatusCode.Active)))
            .OrderBy(Paystub.PaycheckDate())
            .LimitResults(0, 1);
        DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class, query);
        if(paystubs != null) {
            paystub = paystubs.getFirst();
        }
        long elapsedTime = System.currentTimeMillis() - startTime;//Calculate the elapsed time and log the same
        logger.info("v4log findFirstPaystub - Elapsed time of the function in ms: " + elapsedTime + ", employeeId= " + employee.getId() + ", consumerRealmId= " + employee.getConsumerRealmId());
        return paystub;
    }

    public static Paystub findLastPaystub(Employee employee) {
        try {
            logger.info(String.format("v4log findLastPaystub started consumerRealmId=%s",employee.getConsumerRealmId()));
            PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContextCompany(employee.getCompany());
            long startTime = System.currentTimeMillis();//Save start time to calculate final elapsed time
            Paystub paystub = null;
            Expression<Paystub> query = new Query<Paystub>()
                    .Where(Paystub.PstubEmployeeInfo().Employee().equalTo(employee)
                            .And(Paystub.Paycheck().SourcePaycheckId().greaterThan("0"))
                            .And(Paystub.Paycheck().Status().equalTo(PaycheckStatusCode.Active)))
                    .OrderBy(Paystub.PaycheckDate().Descending())
                    .LimitResults(0, 1);
            DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class, query);
            if (paystubs != null) {
                paystub = paystubs.getFirst();
            }
            long elapsedTime = System.currentTimeMillis() - startTime;//Calculate the elapsed time and log the same
            logger.info("v4log findLastPaystub - Elapsed time of the function in ms: " + elapsedTime + ", employeeId= " + employee.getId() + ", consumerRealmId= " + employee.getConsumerRealmId());
            return paystub;
        }finally {
            PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContextCompany();
        }
    }

    public static List<Paystub> findLastTwoPaystubs(Employee employee) {
        long startTime = System.currentTimeMillis();//Save start time to calculate final elapsed time
        logger.info(String.format("v4log findLastTwoPaystubs started consumerRealmId=%s",employee.getConsumerRealmId()));
        Expression<Paystub> query = new Query<Paystub>()
            .Where(Paystub.PstubEmployeeInfo().Employee().equalTo(employee)
            .And(Paystub.Paycheck().SourcePaycheckId().greaterThan("0"))
            .And(Paystub.Paycheck().Status().equalTo(PaycheckStatusCode.Active)))
            .OrderBy(Paystub.PaycheckDate().Descending())
            .LimitResults(0, 2);
        DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class, query);
        List<Paystub> paystubList = null;
        if(paystubs != null) {
            paystubList = new ArrayList<Paystub>();
            paystubList.addAll(paystubs);
        }
        long elapsedTime = System.currentTimeMillis() - startTime;//Calculate the elapsed time and log the same
        logger.info("v4log findLastTwoPaystubs - Elapsed time of the function in ms: " + elapsedTime + ", employeeId= " + employee.getId() + ", consumerRealmId= " + employee.getConsumerRealmId());
        return paystubList;
    }

    public static Paystub findPaystubById(SpcfUniqueId id) {
        logger.info(String.format("v4log findPaystubById paystubId=%s",id));
        Paystub paystub = null;
        Expression<Paystub> query = new Query<Paystub>()
            .Where(Paystub.Id().equalTo(id))
            .EagerLoad(getPaystubEagerLoadProperties());
        DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class, query);
        if(paystubs != null) {
            paystub = paystubs.getFirst();
        }
        logger.info(String.format("v4log findPaystubById completed paystubId=%s paystubFound=%s",id, Objects.nonNull(paystub)));
        return paystub;
    }

    private static Property[] getPaystubEagerLoadProperties() {
        return new Property[]{
            Paystub.PstubEmployerInfo(),
            Paystub.PstubEmployerInfo().PstubAddress(),
            Paystub.PstubEmployeeInfo(),
            Paystub.PstubEmployeeInfo().PstubAddress(),
            Paystub.Paycheck()
        };
    }

    //Check date end may need to be adjusted to the current day if the employer has future dated paychecks hidden
    //In addition if no value is passed into the API it defaults to the max value which may be either the max calendar value or today if the previous preference is enabled
    private static SpcfCalendar calculateCheckDateEnd(String companyRealmId, String checkDateEnd) {
        SpcfCalendar checkDateEndCalendar = CdmHelper.createSpcfCalendar(checkDateEnd, SpcfCalendar.MaxMillisecond);
        //The end date is inclusive so we need to set the time as the latest possible for this day
        checkDateEndCalendar.setValues(checkDateEndCalendar.getYear(), checkDateEndCalendar.getMonth(), checkDateEndCalendar.getDay(), 23, 59, 59, 999);

        SpcfCalendar currentTime = PSPDate.getPSPTime();

        /**
         * Commenting below condition to disable hiding of future paychecks
         *
         * if(checkDateEndCalendar.compareTo(currentTime) > 0 && hideFutureDatedPaystubs(companyRealmId)) {
         * checkDateEndCalendar = currentTime;
         * }
         */

        return checkDateEndCalendar;
    }

    private static boolean hideFutureDatedPaystubs(String companyRealmId) {
        //Default behavior is to not hide future dated paystubs if a preference is not found
        boolean hideFutureDatedPaystubs = false;

        EmployerPreference employerPreference = EmployerPreference.findEmployerPreference(companyRealmId, EmployerPreference.VMP, EmployerPreference.HIDE_PAYCHECKS_DATED_IN_THE_FUTURE);
        if(employerPreference != null) {
            if(EmployerPreference.ON.equalsIgnoreCase(employerPreference.getPreferenceValue())) {
                hideFutureDatedPaystubs = true;
            }
        }
        return hideFutureDatedPaystubs;
    }

    /**
     * VMP Hot-Fix for v4 APIs
     * corresponding to findPaystubsByCompanyRealmidAndEmployeeId
     * @param companyUniqueId- can be either company seq or company realm id
     * @param employeeId
     * @param checkDateStart
     * @param checkDateEnd
     * @param start
     * @param size
     * @return
     */
    public static List<Paystub> findPaystubsByCompanyUniqueIdAndEmployeeId(String companyUniqueId,
                                                                          String employeeId,
                                                                          String checkDateStart,
                                                                          String checkDateEnd,
                                                                          int start,
                                                                          int size) {
        logger.info(String.format("v4log findPaystubsByCompanyUniqueIdAndEmployeeId started companyUniqueId=%s employeeId=%s checkDateStart=%s checkDateEnd=%s",
                companyUniqueId, employeeId, checkDateStart, checkDateEnd));
        boolean isSpcfUniqueId=CdmHelper.isSpcfUniqueId(companyUniqueId);
        Criterion<Paystub> paystubCriterion;
        if(isSpcfUniqueId){
            paystubCriterion=Paystub.Paycheck().Company().Id().equalTo(SpcfUniqueId.createInstance(companyUniqueId));
        }else{
            paystubCriterion=Paystub.Paycheck().Company().IAMRealmId().equalTo(companyUniqueId);
        }

        paystubCriterion=paystubCriterion
                .And(Paystub.PstubEmployeeInfo().Employee().Id().equalTo(CdmHelper.createSpcfUniqueId(employeeId, DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND)))
                .And(Paystub.Paycheck().SourcePaycheckId().greaterThan("0"));

        if(isSpcfUniqueId){
            paystubCriterion=paystubCriterion
                    .And(Paystub.PaycheckDate().between(CdmHelper.createSpcfCalendar(checkDateStart, SpcfCalendar.MinMillisecond),
                    calculateCheckDateEndByCompanyUniqueId(companyUniqueId, checkDateEnd)));
        }else{
            paystubCriterion=paystubCriterion
                    .And(Paystub.PaycheckDate().between(CdmHelper.createSpcfCalendar(checkDateStart, SpcfCalendar.MinMillisecond),
                    calculateCheckDateEnd(companyUniqueId, checkDateEnd)));
        }

        Expression<Paystub> query= new Query<Paystub>()
                .Where(paystubCriterion.And(Paystub.Paycheck().Status().equalTo(PaycheckStatusCode.Active)))
                .OrderBy(Paystub.PaycheckDate().Descending())
                .EagerLoad(getPaystubEagerLoadProperties())
                .LimitResults(start, size);

        DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class, query);
        Predicate<Paystub> isVmpPaystub = paystub -> paystub.getPstubEmployeeInfo().getEmployee().getCompany().isCompanyOnService(ServiceCode.ViewMyPaycheck);
        Set<Paystub> paystubSet = paystubs.stream().filter(isVmpPaystub).collect(Collectors.toSet());
        List<Paystub> paystubList = new ArrayList<Paystub>();
        if (CollectionUtils.isNotEmpty(paystubSet)) {
            paystubList.addAll(paystubSet);
        }
        logger.info(String.format("v4log findPaystubsByCompanyUniqueIdAndEmployeeId finished foundPaystubs=%s companyUniqueId=%s employeeId=%s checkDateStart=%s checkDateEnd=%s",
                CollectionUtils.isNotEmpty(paystubList), companyUniqueId, employeeId, checkDateStart, checkDateEnd));

        return paystubList;
    }

    /**
     * VMP Hot-Fix for v4 APIs
     * corresponding to calculateCheckDateEnd
     * handles only company seq in companyRealmId
     * @param companyUniqueId- can be either company seq or company realm id
     * @param checkDateEnd
     * @return
     */
    private static SpcfCalendar calculateCheckDateEndByCompanyUniqueId(String companyUniqueId, String checkDateEnd) {
        SpcfCalendar checkDateEndCalendar = CdmHelper.createSpcfCalendar(checkDateEnd, SpcfCalendar.MaxMillisecond);
        //The end date is inclusive so we need to set the time as the latest possible for this day
        checkDateEndCalendar.setValues(checkDateEndCalendar.getYear(), checkDateEndCalendar.getMonth(), checkDateEndCalendar.getDay(), 23, 59, 59, 999);

        SpcfCalendar currentTime = PSPDate.getPSPTime();

        /**
         * Commenting below condition to disable hiding of future paychecks
         *
         * if(checkDateEndCalendar.compareTo(currentTime) > 0 && hideFutureDtdPstubsByCompanyUniqueId(companyUniqueId)) {
         * checkDateEndCalendar = currentTime;
         * }
         */
        return checkDateEndCalendar;
    }

    /**
     * VMP Hot-Fix for v4 APIs
     * corresponding to hideFutureDatedPaystubs
     * @param companyUniqueId- can be either company seq or company realm id
     * @return
     */
    private static boolean hideFutureDtdPstubsByCompanyUniqueId(String companyUniqueId) {
        //Default behavior is to not hide future dated paystubs if a preference is not found
        boolean hideFutureDatedPaystubs = false;

        EmployerPreference employerPreference = EmployerPreference.findEmployerPreferenceByCompanyUniqueId(companyUniqueId, EmployerPreference.VMP, EmployerPreference.HIDE_PAYCHECKS_DATED_IN_THE_FUTURE);
        if(employerPreference != null) {
            if(EmployerPreference.ON.equalsIgnoreCase(employerPreference.getPreferenceValue())) {
                hideFutureDatedPaystubs = true;
            }
        }
        return hideFutureDatedPaystubs;
    }


    /**
     * VMP Hot-Fix for v4 APIs
     * corresponding to findPaystubForCompany
     * @param companyUniqueId- can be either company seq or company realm id
     * @param id
     * @return
     */
    public static Paystub findPaystubForCompanyByCompanyUniqueId(String companyUniqueId, String id) {
        logger.info("v4log Finding paystub for companyUniqueId="+companyUniqueId+" and paystubId="+id);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Criterion<Paystub> paystubCriterion;
        if (CdmHelper.isSpcfUniqueId(companyUniqueId)) {
            paystubCriterion = Paystub.PstubEmployeeInfo().Employee().Company().Id().equalTo(SpcfUniqueId.createInstance(companyUniqueId));
        } else {
            boolean isVmpFilterEnabled = FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_VMP_MULTI_COMPANY_FILTER);
            if (isVmpFilterEnabled
                    && (Objects.nonNull(PSPRequestContextManagerHelper.getPSPRequestContextManager().getRequestContext()) &&
                    Objects.isNull(PSPRequestContextManagerHelper.getPSPRequestContextManager().getRequestContext().getCompanyInfo()))
            ) {
                //all paystub where company.realmID in list of all companies
                DomainEntitySet<Company> companies = Company.findAllCompaniesByRealmId(companyUniqueId);
                Company[] companyArray = new Company[companies.size()];
                companyArray=companies.toArray(companyArray);
                paystubCriterion = Paystub.PstubEmployeeInfo().Employee().Company().in(companyArray).And(
                        Paystub.PstubEmployeeInfo().Company().in(companyArray).And(Paystub.Paycheck().Company().in(companyArray))
                                .And(Paystub.Company().in(companyArray))
                );
            }else {
                paystubCriterion = Paystub.PstubEmployeeInfo().Employee().Company().IAMRealmId().equalTo(companyUniqueId);
            }
        }
        Expression<Paystub> query=new Query<Paystub>()
                .Where(paystubCriterion
                        .And(Paystub.Id().equalTo(CdmHelper.createSpcfUniqueId(id, DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND))))
                .OrderBy(Paystub.PaycheckDate().Descending())
                .EagerLoad(getPaystubEagerLoadProperties());

        DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class, query);
        if (paystubs.size() > 1) {
            logger.error("v4log Query for paystub with id=" + id + " and companyUniqueId=" + companyUniqueId +
                    " returned multiple results, when one was expected.");
        }
        Paystub paystub = paystubs.getFirst();
        if (paystub == null) {
            stopWatch.stop();
            logger.info(String.format("v4log Paystub not found. companyUniqueId=%s ErrorCode=%s Elapsed_time=%s",companyUniqueId,
                    DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND, stopWatch));
            throw new ResourceNotFoundException(DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND);
        }
        stopWatch.stop();
        logger.info("v4log Found paystub for companyUniqueId="+companyUniqueId+" and paystubId="+id+". Elapsed_time="+stopWatch);
        return paystub;
    }

}
