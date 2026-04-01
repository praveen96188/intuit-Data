package com.intuit.sbd.payroll.psp.adapters.cdmadapter.managers;

import com.amazonaws.util.StringUtils;
import com.intuit.ems.dataservice.v1.manager.IRequestContextManager;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.constants.MultiTenantContextConstants;
import com.intuit.sbd.payroll.psp.context.exception.NoVmpCompanyFoundException;
import com.intuit.sbd.payroll.psp.context.helper.VmpCompanyContextHelper;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;

import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class WorkforceContextManager implements IRequestContextManager {

    private VmpCompanyContextHelper companyContextHelper;
    private PSPRequestContextManager pspRequestContextManager;

    public WorkforceContextManager() {
        companyContextHelper = PayrollApplicationBeanFactory.getBean(VmpCompanyContextHelper.class);
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    @Override
    public void setRequestContextFromConsumerRealmId(String consumerRealmId, String provider) {
        setRequestContextFromConsumerRealmId(consumerRealmId,provider,false);
    }

    public void setRequestContextFromConsumerRealmId(String consumerRealmId, String provider, boolean fallbackToExplicitlyServeFromReadWrite) {
        try {
            clearRequestContext();
            pspRequestContextManager.setRequestContext(null, RequestType.GRAPHQL, provider );
            //if it is requested to explicitly serve the request from read-write tenant instead of read-only tenant,
            //set the request attribute
            setMultiTenantThreadLocalContextIfExplicitFallbackRequested(fallbackToExplicitlyServeFromReadWrite);
            Company company = companyContextHelper.getCompanyByConsumerRealmId(consumerRealmId);
            checkAndLogFallbackSuccess(fallbackToExplicitlyServeFromReadWrite, company, consumerRealmId);

            pspRequestContextManager.setRequestContextCompany(company);
        } catch (NoVmpCompanyFoundException e){
            log.info(String.format("v4log Unable to find VMP company for consumerRealmId=%s fallbackToExplicitlyServeFromReadWrite=%s",
                    consumerRealmId,fallbackToExplicitlyServeFromReadWrite));
            //if currently serving from READ tenant, attempt explicit fallback to READ_WRITE tenant
            //reason-there might be replication lag between R/W and R tenants
            if(!fallbackToExplicitlyServeFromReadWrite) {
                attemptExplicitFallBackToReadWriteTenant(consumerRealmId, provider);
            }
        } catch (Exception e) {
            log.warn("v4log Event=SetRequestContext Type=GRAPHQL Operation={} Status=Error ConsumerRealmId={}", provider, consumerRealmId, e);
        }
    }



    @Override
    public void setRequestContextFromCompanyUniqueId(String companyUniqueId, String provider) {
        try {
            clearRequestContext();
            pspRequestContextManager.setRequestContext(null, RequestType.GRAPHQL, provider );
            Company company = companyContextHelper.getCompanyFromCompanyUniqueId(companyUniqueId);
            pspRequestContextManager.setRequestContextCompany(company);
        }catch (NoVmpCompanyFoundException e){
            log.info(String.format("v4log Unable to find VMP company for companyUniqueId=%s",companyUniqueId));
        } catch (Exception e) {
            log.warn("v4log Event=SetRequestContext Type=GRAPHQL Operation={} Status=Error CompanyUniqueId={}", provider, companyUniqueId, e);
        }
    }

    public void setRequestContextFromCFRCompanyUniqueId(String companyUniqueId, String consumerRealmId, String provider) {
        setRequestContextFromCFRCompanyUniqueId(companyUniqueId,consumerRealmId,provider,false);
    }

    public void setRequestContextFromCFRCompanyUniqueId(String companyUniqueId, String consumerRealmId, String provider, boolean fallbackToExplicitlyServeFromReadWrite) {
        try {
            clearRequestContext();
            pspRequestContextManager.setRequestContext(null, RequestType.GRAPHQL, provider );

            setMultiTenantThreadLocalContextIfExplicitFallbackRequested(fallbackToExplicitlyServeFromReadWrite);
            Company company = companyContextHelper.getCompanyByCFRCompanyUniqueId(companyUniqueId, consumerRealmId);
            checkAndLogFallbackSuccess(fallbackToExplicitlyServeFromReadWrite, company, consumerRealmId, null);

            pspRequestContextManager.setRequestContextCompany(company);
        } catch (NoVmpCompanyFoundException e){
            log.info(String.format("v4log Unable to find VMP company for consumerRealmId=%s companyUniqueId=%s fallbackToExplicitlyServeFromReadWrite=%s",
                    consumerRealmId, companyUniqueId, fallbackToExplicitlyServeFromReadWrite));
            //if currently serving from READ tenant, attempt explicit fallback to READ_WRITE tenant
            //reason-there might be replication lag between R/W and R tenants
            if(!fallbackToExplicitlyServeFromReadWrite) {
                attemptExplicitFallBackToReadWriteTenant(consumerRealmId, provider, companyUniqueId);
            }
        } catch (Exception e) {
            log.warn("Event=SetRequestContext Type=GRAPHQL Operation={} Status=Error CompanyUniqueId={} ConsumerRealmId={}", provider, companyUniqueId, consumerRealmId, e);
        }
    }

    @Override
    public void clearRequestContext() {
        try {
            pspRequestContextManager.clearRequestContext();
            RequestAttributesUtils.removeAttribute(MultiTenantContextConstants.WORKFORCE_EXPLICIT_SERVE_FROM_READ_WRITE);
        } catch (Exception e) {
            log.error("v4log Event=ClearRequestContext Type=GRAPHQL Status=Error", e);
        }
    }

    private void setMultiTenantThreadLocalContextIfExplicitFallbackRequested(boolean fallbackToExplicitlyServeFromReadWrite) {
        if(fallbackToExplicitlyServeFromReadWrite) {
            log.info(String.format("Setting WORKFORCE_EXPLICIT_SERVE_FROM_READ_WRITE to true"));
            RequestAttributesUtils.setAttribute(MultiTenantContextConstants.WORKFORCE_EXPLICIT_SERVE_FROM_READ_WRITE,
                    Boolean.TRUE);
        }
    }

    private void checkAndLogFallbackSuccess(boolean fallbackToExplicitlyServeFromReadWrite, Company company, String consumerRealmId, String requestedCompanyUniqueId) {
        if(fallbackToExplicitlyServeFromReadWrite && Objects.nonNull(company)) {
            log.info(String.format("v4log FallbackSucceeded VMP company found for consumerRealmId=%s %s companySeq=%s psid=%s",
                    consumerRealmId, StringUtils.isNullOrEmpty(requestedCompanyUniqueId) ? "":"companyUniqueId="+requestedCompanyUniqueId,
                    company.getId(), company.getSourceCompanyId()));
        }
    }

    private void checkAndLogFallbackSuccess(boolean fallbackToExplicitlyServeFromReadWrite, Company company, String consumerRealmId) {
        checkAndLogFallbackSuccess(fallbackToExplicitlyServeFromReadWrite,company,consumerRealmId,null);
    }

    private void attemptExplicitFallBackToReadWriteTenant(String consumerRealmId, String provider, String companyUniqueId) {
        log.info("v4log attempt fallbackToReadWrite");
        if(StringUtils.isNullOrEmpty(companyUniqueId)) {
            setRequestContextFromConsumerRealmId(consumerRealmId, provider, true);
        } else {
            setRequestContextFromCFRCompanyUniqueId(companyUniqueId, consumerRealmId, provider, true);
        }
    }
    private void attemptExplicitFallBackToReadWriteTenant(String consumerRealmId, String provider) {
        attemptExplicitFallBackToReadWriteTenant(consumerRealmId, provider, null);
    }

}
