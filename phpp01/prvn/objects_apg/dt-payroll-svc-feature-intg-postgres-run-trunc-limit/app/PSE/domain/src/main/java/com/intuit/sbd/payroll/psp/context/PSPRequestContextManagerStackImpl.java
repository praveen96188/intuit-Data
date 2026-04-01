package com.intuit.sbd.payroll.psp.context;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.context.exception.DifferentCompanyAlreadyPresentException;
import com.intuit.sbd.payroll.psp.constants.CommonConstants;
import com.intuit.sbd.payroll.psp.context.helper.CompanyContextHelper;
import com.intuit.sbd.payroll.psp.context.helper.IRequestContextHelper;
import com.intuit.sbd.payroll.psp.context.model.CompanyInfo;
import com.intuit.sbd.payroll.psp.context.model.RequestContext;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.filter.CommonFilterStrategy;
import com.intuit.sbd.payroll.psp.filter.CompanyFilterStrategy;
import com.intuit.sbd.payroll.psp.filter.constants.FilterStrategyType;
import com.intuit.sbd.payroll.psp.filter.factory.CommonFilterStrategyFactory;
import com.intuit.sbd.payroll.psp.filter.factory.CompanyFilterStrategyFactory;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
public class PSPRequestContextManagerStackImpl implements PSPRequestContextManager {

    private CompanyFilterStrategy companyFilterStrategy;
    private CommonFilterStrategy dateFilterStrategy;
    private IRequestContextHelper requestContextHelper;
    private CompanyContextHelper companyContextHelper;

    public PSPRequestContextManagerStackImpl(CompanyFilterStrategyFactory companyFilterStrategyFactory, CommonFilterStrategyFactory commonFilterStrategyFactory, IRequestContextHelper requestContextHelper, CompanyContextHelper companyContextHelper) {
        companyFilterStrategy = companyFilterStrategyFactory.getCompanyFilterStrategy(FilterStrategyType.SESSION_FILTER);
        dateFilterStrategy = commonFilterStrategyFactory.getFilterStrategy(FilterStrategyType.DATE_SESSION_FILTER);
        this.requestContextHelper = requestContextHelper;
        this.companyContextHelper = companyContextHelper;
    }

    /**
     * resets the request context state and sets fresh request context.
     * also applies the session filter if any active session exists
     * @param requestContext
     */
    public void setRequestContext(RequestContext requestContext) {
        try {
            logCurrentRequestContext("setRequestContext_started");
            requestContextHelper.resetRequestContext();
            clearHibernateFilterIfRequired();
            RequestContext newRequestContext = new RequestContext(requestContext);
            requestContextHelper.setCurrentRequestContext(newRequestContext);
            applyHibernateFilterIfRequired();
            logCurrentRequestContext("setRequestContext_completed");
        } catch (Exception e) {
            handleException("setRequestContext", e);
        }
    }

    /**
     * resets the request context state and sets fresh request context
     * applies the session filter if any active session exists
     * takes care of uninitialized company hibernate object
     * @param company
     * @param requestType
     * @param requestOperation
     */
    public void setRequestContext(Company company, RequestType requestType, String requestOperation) {
        try {//clear and reinit context attribute
            logCurrentRequestContext("setRequestContext_started");
            requestContextHelper.resetRequestContext();
            clearHibernateFilterIfRequired();
            setRequestContextImpl(company, requestType, requestOperation);
            logCurrentRequestContext("setRequestContext_completed");
        } catch (Exception e){
            handleException("setRequestContext", e);
        }
    }

    /**
     * clears the request context
     * also removes the session filter if any active session exists
     */
    public void clearRequestContext() {
        try {
            logCurrentRequestContext("clearRequestContext_started");
            requestContextHelper.removeRequestContext();
            clearHibernateFilterIfRequired();
            logCurrentRequestContext("clearRequestContext_completed");
        } catch (Exception e) {
            log.error("Event=ClearRequestContext Status=Error", e);
        }
    }

    /**
     * gets the recently set request context, if any.
     * @return request context
     */
    public RequestContext getRequestContext() {
        return requestContextHelper.getCurrentRequestContext();
    }

    /**
     * if the same company is set as current request context, then increments the request context ref count.
     * else creates a new request context with input company and request type and operation from the
     * previous context and sets the created context as current
     * applies the session filter if any active session exists
     * takes care of uninitialized company hibernate object
     * @param company
     */
    public void setRequestContextCompany(Company company) {
        try {
            logCurrentRequestContext("setRequestContextCompany_started");
            if (isCompanyInfoAlreadySet(company)) {
                requestContextHelper.incrementCurrentRequestContextRefCount();
                logCurrentRequestContext("setRequestContextCompany_completed");
                return;
            }

            clearHibernateFilterIfRequired();
            RequestContext requestContext = requestContextHelper.getCurrentRequestContext();

            if(Objects.isNull(requestContext) && Application.isIntegrationTestEnvironment()) {
                requestContext = createRequestContext(company, RequestType.TEST, "Test");
            } else if (Objects.isNull(requestContext)) {
                log.info("Event=SetRequestContextCompany Status=Failed Reason=RequestContextNull_SoCantSetCompanyInfo");
                return;
            }
            RequestType requestType = requestContext.getRequestType();
            String requestOperation = requestContext.getRequestOperation();

            setRequestContextImpl(company, requestType, requestOperation);
            logCurrentRequestContext("setRequestContextCompany_completed");
        } catch (Exception e){
            handleException("setRequestContextCompany", e);
        }
    }

    /**
     * decrements the ref count of the current request context if refCount > 1
     * else reverts the current request context to the prior value, if any
     * also removes the session filter if any active session exists
     */
    public void clearRequestContextCompany() {
        try {
            logCurrentRequestContext("clearRequestContextCompany_started");
            RequestContext requestContext = requestContextHelper.clearCurrentRequestContext();
            clearHibernateFilterIfRequired();
            applyHibernateFilterIfRequired();
            logCurrentRequestContext("clearRequestContextCompany_completed");
        } catch (Exception e) {
            handleException("clearRequestContextCompany", e);
        }
    }

    /**
     * if the same company is set as current request context, then increments the request context ref count.
     * else creates a new request context with input company psid and request type and operation from the
     * previous context and sets the created context as current
     * also applies the session filter if any active session exists
     * @param psid
     */
    public void setRequestContextCompanyFromPSID(String psid) {
        try {
            log.info("setting context for psid={}", psid);
            logCurrentRequestContext("setRequestContextCompanyFromPSID_started");
            if (isCompanyInfoAlreadySet(psid)) {
                requestContextHelper.incrementCurrentRequestContextRefCount();
                logCurrentRequestContext("setRequestContextCompanyFromPSID_completed");
                return;
            }

            Company company = companyContextHelper.getCompanyByPSID(psid);

            setRequestContextCompany(company);
            logCurrentRequestContext("setRequestContextCompanyFromPSID_completed");
        } catch (Exception e){
            handleException("setRequestContextCompanyFromPSID", e);
        }
    }

    /**
     * if the same company is set as current request context, then increments the request context ref count.
     * else creates a new request context with input company seq and request type and operation from the
     * previous context and sets the created context as current
     * also applies the session filter if any active session exists
     * @param seq
     */
    public void setRequestContextCompanyFromSeq(String seq) {
        try {
            logCurrentRequestContext("setRequestContextCompanyFromSeq_started");
            if (isCompanyInfoAlreadySet(SpcfUniqueId.createInstance(seq))) {
                requestContextHelper.incrementCurrentRequestContextRefCount();
                logCurrentRequestContext("setRequestContextCompanyFromSeq_completed");
                return;
            }

            Company company = companyContextHelper.getCompanyByID(SpcfUniqueId.createInstance(seq));

            setRequestContextCompany(company);
            logCurrentRequestContext("setRequestContextCompanyFromSeq_completed");
        } catch (Exception e){
            handleException("setRequestContextCompanyFromSeq", e);
        }
    }

    public void setCreatedDate(SpcfCalendar createdDate) {
        RequestAttributesUtils.setAttribute(CommonConstants.CREATED_DATE_ATTRIBUTE, createdDate);
        boolean isActiveTransaction = Application.hasActiveTransaction();
        if(isActiveTransaction) {
            dateFilterStrategy.applyFilter(null);
        }
    }

    public void clearCreatedDate() {
        RequestAttributesUtils.removeAttribute(CommonConstants.CREATED_DATE_ATTRIBUTE);
        boolean isActiveTransaction = Application.hasActiveTransaction();
        if(isActiveTransaction) {
            dateFilterStrategy.clearFilter();
        }
    }

    /**
     * clears the request context
     * @param event
     * @param e
     */
    private void handleException(String event, Exception e){
        clearRequestContext();
        log.error(String.format("Event=%s Status=Error", event), e);
    }

    /**
     * creates and returns a new request context object
     * @param company
     * @param requestType
     * @param requestOperation
     * @return
     */
    private RequestContext createRequestContext(Company company, RequestType requestType, String requestOperation){
        RequestContext requestContext = RequestContext.builder()
                .requestType(requestType)
                .requestOperation(requestOperation)
                .build();
        if(company == null)
            return requestContext;

        CompanyInfo companyInfo = CompanyInfo.builder()
                .psid(company.getSourceCompanyId())
                .realmId(company.getIAMRealmId())
                .companySequence(company.getId())
                .build();

        requestContext.setCompanyInfo(companyInfo);
        return requestContext;
    }

    /**
     * creates a new request context with input company and request type and operation
     * and sets it as current request context
     * applies the session filter if any active session exists
     * takes care of uninitialized company hibernate object
     * @param company
     * @param requestType
     * @param requestOperation
     */
    private void setRequestContextImpl(Company company, RequestType requestType, String requestOperation){

        company = reloadCompanyIfRequired(company);

        RequestContext requestContext = createRequestContext(company, requestType, requestOperation);
        requestContextHelper.setCurrentRequestContext(requestContext);

        applyHibernateFilterIfRequired();
    }


    /*-----------Utility Methods----------*/

    /**
     * reloads company hibernate object if not properly loaded
     * @param company
     * @return reloaded company
     */
    private Company reloadCompanyIfRequired(Company company){
        boolean manageTransaction = !Application.hasActiveTransaction();
        if(Objects.nonNull(company) && !Hibernate.isInitialized(company)) {
            try {
                if(manageTransaction)
                    Application.beginUnitOfWork();
                company = Application.findById(Company.class, company.getId());
            } finally {
                if(manageTransaction)
                    Application.rollbackUnitOfWork();
            }
        }
        return company;
    }

    /**
     * applies hibernate session filter if active session exists
     */
    private void applyHibernateFilterIfRequired(){
        boolean isActiveTransaction = Application.hasActiveTransaction();
        if(isActiveTransaction) {
            companyFilterStrategy.applyFilter(null);
        }
    }

    /**
     * clears hibernate session filter if active session exists
     */
    private void clearHibernateFilterIfRequired(){
        boolean isActiveTransaction = Application.hasActiveTransaction();
        if(isActiveTransaction) {
            companyFilterStrategy.clearFilter();
        }
    }

    private void logCurrentRequestContext(String event){
        RequestContext requestContext = requestContextHelper.getCurrentRequestContext();
        log.info("Event={} CurrentRequestContext={}", event, requestContext);

    }

    /**
     * checks if the input company is same as the company in top most request context of the requestContextStack in the thread local
     * @param company
     * @return true if requestContextStack's top is for same company, else false
     */
    private boolean isCompanyInfoAlreadySet(Company company){
        if(Objects.isNull(company)){
            return isCompanyInfoAlreadySet((SpcfUniqueId)null);
        }

        return isCompanyInfoAlreadySet(company.getId());
    }

    /**
     * checks if the psid is for same company as the company in top most request context of the requestContextStack in the thread local
     * @param companyId
     * @return true if requestContextStack's top is for same company, else false
     */
    private boolean isCompanyInfoAlreadySet(SpcfUniqueId companyId){
        RequestContext requestContext = requestContextHelper.getCurrentRequestContext();
        if(Objects.isNull(requestContext) || Objects.isNull(requestContext.getCompanyInfo())){
            return false;
        }

        if(Objects.equals(companyId, requestContext.getCompanyInfo().getCompanySequence())){
            return true;
        }

        if(!isMultipleCompaniesAllowed(requestContext.getRequestOperation())) {
            throw new DifferentCompanyAlreadyPresentException(String.format("Another company already present in request context, existingCompanySeq=%s newCompanySeq=%s currentRequestContext=%s", requestContext.getCompanyInfo().getCompanySequence(), companyId, requestContext));
        }
        return false;
    }

    /**
     * checks if the seq is for same company as the company in top most request context of the requestContextStack in the thread local
     * @param psid
     * @return true if requestContextStack's top is for same company, else false
     */
    private boolean isCompanyInfoAlreadySet(String psid) {
        RequestContext requestContext = requestContextHelper.getCurrentRequestContext();
        if (Objects.isNull(requestContext) || Objects.isNull(requestContext.getCompanyInfo())) {
            return false;
        }

        if (Objects.equals(psid, requestContext.getCompanyInfo().getPsid())) {
            return true;
        }

        if(!isMultipleCompaniesAllowed(requestContext.getRequestOperation())) {
            throw new DifferentCompanyAlreadyPresentException(String.format("Another company already present in request context, existingCompanySeq=%s newCompanySeq=%s currentRequestContext=%s", requestContext.getCompanyInfo().getCompanySequence(), psid, requestContext));
        }
        return false;
    }

    protected boolean isMultipleCompaniesAllowed(String requestOperation) {
        String allowedOperations = FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.MULTIPLE_COMPANY_CONTEXT_ALLOWED_OPERATIONS);

        List<String> allowedOperationsList = new ArrayList<>();
        allowedOperationsList.addAll(Arrays.asList(StringUtils.stripAll(allowedOperations.split(","))));

        return allowedOperationsList.contains(requestOperation);
    }

}
