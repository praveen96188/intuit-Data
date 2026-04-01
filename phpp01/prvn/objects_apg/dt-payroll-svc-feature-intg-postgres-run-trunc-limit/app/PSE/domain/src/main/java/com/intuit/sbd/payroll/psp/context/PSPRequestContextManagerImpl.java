package com.intuit.sbd.payroll.psp.context;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.constants.CommonConstants;
import com.intuit.sbd.payroll.psp.context.helper.CompanyContextHelper;
import com.intuit.sbd.payroll.psp.context.model.CompanyInfo;
import com.intuit.sbd.payroll.psp.context.model.RequestContext;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.filter.CommonFilterStrategy;
import com.intuit.sbd.payroll.psp.filter.CompanyFilterStrategy;
import com.intuit.sbd.payroll.psp.filter.constants.FilterStrategyType;
import com.intuit.sbd.payroll.psp.filter.factory.CommonFilterStrategyFactory;
import com.intuit.sbd.payroll.psp.filter.factory.CompanyFilterStrategyFactory;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;


import java.util.Objects;

@Slf4j
public class PSPRequestContextManagerImpl implements PSPRequestContextManager {
 private CompanyFilterStrategy companyFilterStrategy;

 private CommonFilterStrategy dateFilterStrategy;
 private CompanyContextHelper companyContextHelper;

    public PSPRequestContextManagerImpl(CompanyFilterStrategyFactory companyFilterStrategyFactory, CommonFilterStrategyFactory commonFilterStrategyFactory, CompanyContextHelper companyContextHelper) {
        companyFilterStrategy = companyFilterStrategyFactory.getCompanyFilterStrategy(FilterStrategyType.SESSION_FILTER);
        dateFilterStrategy = commonFilterStrategyFactory.getFilterStrategy(FilterStrategyType.DATE_SESSION_FILTER);
        this.companyContextHelper = companyContextHelper;
    }

    /**
     * creates a new request context with input company and request type and operation from the
     * previous context and sets the created context as current
     * applies the session filter if any active session exists
     * takes care of uninitialized company hibernate object
     * @param company
     */
    public void setRequestContextCompany(Company company) {
        RequestContext requestContext = RequestAttributesUtils.getAttribute(CommonConstants.REQUEST_CONTEXT_ATTRIBUTE, RequestContext.class);
        if(Objects.isNull(requestContext) && Application.isIntegrationTestEnvironment()) {
            requestContext = createRequestContext(company, RequestType.TEST, "Test");
        } else if (Objects.isNull(requestContext)) {
            log.info("Event=SetRequestContextCompany Status=Failed Reason=RequestContextNull_SoCantSetCompanyInfo");
            return;
        }
        RequestType requestType = requestContext.getRequestType();
        String requestOperation = requestContext.getRequestOperation();
        setRequestContext(company, requestType, requestOperation);
    }

    /**
     * clears the current request context and sets to null
     * also removes the session filter if any active session exists
     */
    public void clearRequestContextCompany() {
        try {
            RequestContext requestContext = RequestAttributesUtils.getAttribute(CommonConstants.REQUEST_CONTEXT_ATTRIBUTE, RequestContext.class);
            if(Objects.isNull(requestContext)) {
                log.info("Event=ClearRequestContextCompany Status=RequestContextAlreadyNull");
                return;
            }
            requestContext.setCompanyInfo(null);
            boolean isActiveTransaction = Application.hasActiveTransaction();
            if(isActiveTransaction) {
                companyFilterStrategy.clearFilter();
            }
            log.info("Event=ClearRequestContextCompany Type={} Operation={} Status=Completed", requestContext.getRequestType(), requestContext.getRequestOperation());
        } catch (Exception e) {
            log.error("Event=ClearRequestContextCompany Status=Error", e);
        }
    }

    /**
     * sets the input request context in the thread local.
     * also applies the session filter if any active session exists
     * @param requestContext
     */
    public void setRequestContext(RequestContext requestContext) {
        RequestContext newRequestContext = new RequestContext(requestContext);
        RequestAttributesUtils.setAttribute(CommonConstants.REQUEST_CONTEXT_ATTRIBUTE, newRequestContext);
        boolean isActiveTransaction = Application.hasActiveTransaction();
        if(isActiveTransaction) {
            companyFilterStrategy.applyFilter(null);
        }
    }

    /**
     * creates new request context and sets it in the thread local
     * applies the session filter if any active session exists
     * takes care of uninitialized company hibernate object
     * @param company
     * @param requestType
     * @param requestOperation
     */
    public void setRequestContext(Company company, RequestType requestType, String requestOperation) {
        try {
            boolean isActiveTransaction = Application.hasActiveTransaction();
            if(Objects.nonNull(company) && !Hibernate.isInitialized(company)) {
                try {
                    if(!isActiveTransaction)
                        Application.beginUnitOfWork();
                    company = Application.findById(Company.class, company.getId());
                } finally {
                    if(!isActiveTransaction)
                        Application.rollbackUnitOfWork();
                }
            }
            RequestContext requestContext = createRequestContext(company, requestType, requestOperation);
            RequestAttributesUtils.setAttribute(CommonConstants.REQUEST_CONTEXT_ATTRIBUTE, requestContext);

            if(isActiveTransaction) {
                companyFilterStrategy.applyFilter(null);
            }

            log.info("Event=SetRequestContext Type={} Status=Completed Operation={} RequestContext={}", requestContext.getRequestType(),
                    requestContext.getRequestOperation(), requestContext.toString());
        } catch (Exception e) {
            log.error("Event=SetRequestContext Status=Error", e);
        }
    }

    /**
     * clears the request context
     * also removes the session filter if any active session exists
     */
    public void clearRequestContext() {
        try {
            RequestAttributesUtils.removeAttribute(CommonConstants.REQUEST_CONTEXT_ATTRIBUTE);
            boolean isActiveTransaction = Application.hasActiveTransaction();
            if(isActiveTransaction) {
                companyFilterStrategy.clearFilter();
            }
            log.info("Event=ClearRequestContext Status=Completed");
        } catch (Exception e) {
            log.error("Event=ClearRequestContext Status=Error", e);
        }

    }

    /**
     * gets the request context from thread local, if any.
     * @return request context
     */
    public RequestContext getRequestContext() {
        return RequestAttributesUtils.getAttribute(CommonConstants.REQUEST_CONTEXT_ATTRIBUTE, RequestContext.class);
    }

    /**
     * creates a new request context with input company psid and request type and operation from the
     * current context and sets the created context as current context in the thread local
     * also applies the session filter if any active session exists
     * @param psid
     */
    public void setRequestContextCompanyFromPSID(String psid) {
        Company company = companyContextHelper.getCompanyByPSID(psid);
        setRequestContextCompany(company);
    }

    /**
     * creates a new request context with input company seq and request type and operation from the
     * current context and sets the created context as current context in the thread local
     * also applies the session filter if any active session exists
     * @param companySeq
     */
    public void setRequestContextCompanyFromSeq(String companySeq) {
        Company company = companyContextHelper.getCompanyByID(SpcfUniqueId.createInstance(companySeq));
        setRequestContextCompany(company);
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
}
