package com.intuit.sbd.payroll.psp.accountservices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intuit.payments.cdm.v2.client.PaymentsAccount;
import com.intuit.platform.integration.ius.common.types.IAMTicket;
import com.intuit.platform.integration.ius.common.types.Realm;
import com.intuit.sbd.payroll.psp.ius.IUSCompany;
import com.intuit.sbd.payroll.psp.ius.IUSDataGenerator;
import com.intuit.sbd.payroll.psp.ius.TestAuthorizationManager;
import com.intuit.sbd.payroll.psp.util.FileUtils;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.accountservices.AccountServicesEndpointConfig;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceClient;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceResponse;
import com.intuit.sbg.psp.webserviceclient.support.json.JsonConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AccountServicesDataGenerator {

    private static final String CREATE_PAYMENTS_ACCOUNT_TEMPLATE_PATH = "accountservice/create_payments_account.json";

    private static final String CREATE_PAYMENTS_ACCOUNT_ENDPOINT = "/v2/accounts";

    private static final String ONBOARDING_DECISION ="onboarding_decision";

    private static final Collection<String> ALLOWED_ONBOARDING_DECISIONS = Arrays.asList("Pending", "Approved", "Declined");

    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

    private HttpServiceClient httpServiceClient;
    private JsonConverter jsonConverter;
    private IUSDataGenerator iusDataGenerator;
    private TestAuthorizationManager testAuthorizationManager;
    private AccountServicesEndpointConfig accountServicesEndpointConfig;

    public AccountServicesDataGenerator() {
        httpServiceClient = PayrollApplicationBeanFactory.getBean(HttpServiceClient.class);
        jsonConverter = new JsonConverter(gson);
        iusDataGenerator = new IUSDataGenerator();
        testAuthorizationManager = new TestAuthorizationManager();
        accountServicesEndpointConfig = PayrollApplicationBeanFactory.getBean(AccountServicesEndpointConfig.class);
    }

    public PaymentsAccount createPaymentsAccount() {
        return createPaymentsAccount("Approved");
    }

    public PaymentsAccount createPaymentsAccount(String onboardingDecision) {
        Validate.isTrue(ALLOWED_ONBOARDING_DECISIONS.contains(onboardingDecision), String.format("Invalid onboardingDecision=%s", onboardingDecision));

        PaymentsAccount paymentsAccount = null;
        IUSCompany iusCompany =  iusDataGenerator.createCompany();
        try {
           IAMTicket iamTicket = iusCompany.getIamTicket();
           testAuthorizationManager.setUserAuthorizationContext(iamTicket.getUserId(), iamTicket.getTicket());
           paymentsAccount = createPaymentsAccount(iusCompany.getRealm(), onboardingDecision);
        } finally {
           testAuthorizationManager.removeUserAuthorizationContext();
        }
       return paymentsAccount;
    }

    /**
     *Create Payments account with a template file.
     *
     * Template file path should be like: "accountservice/{template_file_name}"
     *
     * @param onboardingDecision
     * @param templateFilePath - the path of the template file
     * @return a new PaymentsAccount object
     */
    public  PaymentsAccount createPaymentsAccount(String onboardingDecision, String templateFilePath) {
        Validate.isTrue(ALLOWED_ONBOARDING_DECISIONS.contains(onboardingDecision), String.format("Invalid onboardingDecision=%s", onboardingDecision));

        PaymentsAccount paymentsAccount = null;
        IUSCompany iusCompany =  iusDataGenerator.createCompany();
        try {
            IAMTicket iamTicket = iusCompany.getIamTicket();
            testAuthorizationManager.setUserAuthorizationContext(iamTicket.getUserId(), iamTicket.getTicket());
            paymentsAccount = createPaymentsAccount(iusCompany.getRealm(), onboardingDecision, templateFilePath);
        } finally {
            testAuthorizationManager.removeUserAuthorizationContext();
        }
        return paymentsAccount;
    }

    public PaymentsAccount createPaymentsAccount(Realm realm, String onboardingDecision) {
        Validate.notNull(realm, "realm cannot be null");
        Validate.notEmpty(realm.getRealmId(), "realmId cannot be null or empty");
        Validate.notEmpty(onboardingDecision, "onboardingDecision cannot be null or empty");

        PaymentsAccount templatePaymentsAccount = getPaymentsAccountPayload(realm);
        String payload = jsonConverter.serialize(templatePaymentsAccount);
        HttpServiceResponse httpServiceResponse = httpServiceClient.post(getCreatePaymentsAccountUrl(), payload, getCommonHeaders(onboardingDecision));
        PaymentsAccount paymentsAccount = jsonConverter.deserialize(httpServiceResponse.getBody(), PaymentsAccount.class);
        return paymentsAccount;
    }

    /**
     * Create Payments account with a template file.
     *
     * Template file path should be like: "accountservice/{template_file_name}"
     *
     * @param realm - realm id of the company
     * @param onboardingDecision
     * @param templateFilePath - the path of the template file
     * @return
     */
    public PaymentsAccount createPaymentsAccount(Realm realm, String onboardingDecision, String templateFilePath) {
        Validate.notNull(realm, "realm cannot be null");
        Validate.notEmpty(realm.getRealmId(), "realmId cannot be null or empty");
        Validate.notEmpty(onboardingDecision, "onboardingDecision cannot be null or empty");

        PaymentsAccount templatePaymentsAccount = getPaymentsAccountPayload(realm, templateFilePath);
        String payload = jsonConverter.serialize(templatePaymentsAccount);
        HttpServiceResponse httpServiceResponse = httpServiceClient.post(getCreatePaymentsAccountUrl(), payload, getCommonHeaders(onboardingDecision));
        PaymentsAccount paymentsAccount = jsonConverter.deserialize(httpServiceResponse.getBody(), PaymentsAccount.class);
        return paymentsAccount;
    }

    private PaymentsAccount getPaymentsAccountPayload(Realm realm) {
        String fileContents = FileUtils.readClasspathFileContent(CREATE_PAYMENTS_ACCOUNT_TEMPLATE_PATH);
        PaymentsAccount paymentsAccount =  jsonConverter.deserialize(fileContents, PaymentsAccount.class);
        paymentsAccount.setRealmId(realm.getRealmId());
        return paymentsAccount;
    }

    private PaymentsAccount getPaymentsAccountPayload(Realm realm, String templateFilePath) {
        String fileContents = FileUtils.readClasspathFileContent(templateFilePath);
        PaymentsAccount paymentsAccount =  jsonConverter.deserialize(fileContents, PaymentsAccount.class);
        paymentsAccount.setRealmId(realm.getRealmId());
        return paymentsAccount;
    }

    private String getCreatePaymentsAccountUrl() {
        return StringUtils.join(accountServicesEndpointConfig.getBaseUrl(), CREATE_PAYMENTS_ACCOUNT_ENDPOINT);
    }

    private Map<String, String> getCommonHeaders(String onboardingDecision) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        headers.put(ONBOARDING_DECISION, onboardingDecision);
        return headers;
    }
}
