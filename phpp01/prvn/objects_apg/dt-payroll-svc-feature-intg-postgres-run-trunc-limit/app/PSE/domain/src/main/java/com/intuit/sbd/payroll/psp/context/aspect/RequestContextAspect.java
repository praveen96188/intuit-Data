package com.intuit.sbd.payroll.psp.context.aspect;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.helper.CompanyContextHelper;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;

@Aspect
@Slf4j
@Setter
public class RequestContextAspect {
    private CompanyContextHelper companyContextHelper;
    private PSPRequestContextManager pspRequestContextManager;

    @Around("execution(* *(..,@com.intuit.sbd.payroll.psp.context.aspect.TenantId (*),..))")
    public Object handleRequestContext(ProceedingJoinPoint joinPoint) throws Throwable {
        boolean isTenantIdAnnotationEnabled = FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_TENANT_ID_ANNOTATION);
        if(!isTenantIdAnnotationEnabled){
            return joinPoint.proceed();
        }
        try{
            setRequestContext(joinPoint);
            return joinPoint.proceed();
        } finally {
            pspRequestContextManager.clearRequestContextCompany();
        }
    }



    private void setRequestContext(JoinPoint joinPoint){
        boolean manageSession = !Application.hasActiveTransaction();
        try {
            String psid = null;
            String ein = null;
            String realmId = null;
            String company_seq = null;
            Company company = null;

            Object[] args = joinPoint.getArgs();
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            Method method = methodSignature.getMethod();
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();


            for (int i=0; i<parameterAnnotations.length; i++) {
                Annotation[] annotations = parameterAnnotations[i];
                for (Annotation annotation : annotations) {
                    System.out.println(annotation);
                    if (annotation.annotationType() == TenantId.class) {
                        CompanyIdentifierType idType = ((TenantId)annotation).IdType();
                        switch (idType){
                            case PSID:
                                psid = (String)args[i];
                                break;
                            case EIN:
                                ein = (String)args[i];
                                break;
                            case REALMID:
                                realmId = (String)args[i];
                                break;
                            case COMPANY_SEQ:
                                company_seq = (args[i] instanceof SpcfUniqueId) ? ((SpcfUniqueId)args[i]).toString() : (String)args[i];
                                break;
                            case COMPANY:
                                company = (Company)args[i];
                                break;
                        }
                    }
                }
            }

            if(Objects.nonNull(company_seq)){
                pspRequestContextManager.setRequestContextCompanyFromSeq(company_seq);
                return;
            }

            if(Objects.nonNull(psid)){
                pspRequestContextManager.setRequestContextCompanyFromPSID(psid);
                return;
            }

            if(manageSession) {
                Application.beginUnitOfWork();
            }

            if(Objects.nonNull(ein)){
                company = companyContextHelper.getCompanyByEIN(ein);
            }

            if(Objects.nonNull(realmId)){
                company = companyContextHelper.getCompanyByRealmId(realmId);
            }

            pspRequestContextManager.setRequestContextCompany(company);

        } catch (Throwable throwable) {
            log.error("TenantAspectException - Unable to extract company", throwable);
        } finally {
            if(manageSession) {
                Application.rollbackUnitOfWork();
            }
        }
    }
}
