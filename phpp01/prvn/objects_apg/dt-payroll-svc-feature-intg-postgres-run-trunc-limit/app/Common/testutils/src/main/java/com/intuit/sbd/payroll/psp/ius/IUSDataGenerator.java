package com.intuit.sbd.payroll.psp.ius;

import com.intuit.client.ius.GrantType;
import com.intuit.client.ius.IUSGrantClient;
import com.intuit.client.ius.IUSUserClient;
import com.intuit.platform.integration.ius.common.types.*;
import com.intuit.platform.jsk.security.iam.autoconfig.IntuitSecurityProperties;
import com.intuit.sbd.payroll.psp.gateways.iam.IUSAppCallback;
import com.intuit.sbd.payroll.psp.gateways.iam.IUSClientWrapper;
import com.intuit.sbd.payroll.psp.util.FileUtils;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceClient;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceResponse;
import com.intuit.sbg.psp.webserviceclient.support.json.JsonConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IUSDataGenerator {

    public static final String OFFERING_ID_QUICKBOOKS_DESKTOP = "Intuit.sbg.quickbooks-win-us";

    public static final String REALM_NAME_PREFIX = "Desktop Payroll";

    public static final String DEFAULT_EMAIL_ADDRESS_PREFIX = "qbdttrontest+iamtestpass+{uniqueId}@gmail.com";

    private static final String CREATE_USER_TEMPLATE_PATH = "ius/create_oii_user.json";

    private static final String CREATE_REALM_ENDPOINT = "/realms";

    private IUSAppCallback iusAppCallback;
    private HttpServiceClient httpServiceClient;
    private JsonConverter jsonConverter;
    private IntuitSecurityProperties intuitSecurityProperties;
    private TestAuthorizationManager testAuthorizationManager;

    public IUSDataGenerator() {
        initialize();
    }

    public void initialize() {
        httpServiceClient = PayrollApplicationBeanFactory.getBean(HttpServiceClient.class);
        jsonConverter = PayrollApplicationBeanFactory.getBean(JsonConverter.class);
        intuitSecurityProperties = PayrollApplicationBeanFactory.getBean(IntuitSecurityProperties.class);
        testAuthorizationManager = new TestAuthorizationManager();
        iusAppCallback = IUSInitializer.getIusAppCallback();
    }

    public IUSCompany createCompany() {
        String uniqueId = getUniqueId();
        String emailAddress = generateUniqueEmail(uniqueId);
        String realmName = generateRealmName(uniqueId);
        return createCompany(emailAddress, realmName);
    }

    public IUSCompany createCompany(String emailAddress, String realmName) {
        try {
            Validate.notEmpty(emailAddress, "emailAddress cannot be null or empty");
            Validate.notEmpty(realmName, "realmName cannot be null or empty");

            // Create User
            Pair<User, IAMTicket> userIAMTicketPair = createUser(emailAddress);
            User user = userIAMTicketPair.getLeft();
            IAMTicket iamTicket = userIAMTicketPair.getRight();
            testAuthorizationManager.setUserAuthorizationContext(user.getUserId(), iamTicket.getTicket());

            // Create Realm
            Realm realm = createRealm(realmName, iamTicket.getUserId());

            // Create Grant
            Grant grant = createGrant(iamTicket, realm, OFFERING_ID_QUICKBOOKS_DESKTOP);

            return createIUSCompany(userIAMTicketPair.getLeft(), iamTicket, realm, grant);
        } finally {
            testAuthorizationManager.removeUserAuthorizationContext();
        }
    }

    public IUSCompany createIUSCompany(User user, IAMTicket iamTicket, Realm realm, Grant grant) {
        IUSCompany iusCompany = new IUSCompany();
        iusCompany.setUser(user);
        iusCompany.setIamTicket(iamTicket);
        iusCompany.setRealm(realm);
        iusCompany.setGrant(grant);
        return iusCompany;
    }

    public Pair<User, IAMTicket> createUser(String emailAddress) {
        User user = generateUserPayload(emailAddress);
        SignupResponse signupResponse = IUSUserClient.secureSignUp(user);
        return new ImmutablePair<>(signupResponse.getUser(), signupResponse.getIAMTicket());
    }

    public User generateUserPayload(String emailAddress) {
        String fileContents = FileUtils.readClasspathFileContent(CREATE_USER_TEMPLATE_PATH);
        User user = jsonConverter.deserialize(fileContents, User.class);
        user.setUsername(emailAddress);
        user.getEmail().setAddress(emailAddress);
        return user;
    }

    public Realm createRealm(String realmName, String userId) {
        Validate.notEmpty(realmName, "realmName cannot be null or empty");
        Validate.notEmpty(userId, "userId cannot be null or empty");

        String realmURL = StringUtils.join(iusAppCallback.getIUSRestUri(),  "/v1/users/", userId, CREATE_REALM_ENDPOINT);

        String payload= jsonConverter.serialize(generateRealmPayload(realmName));
        HttpServiceResponse httpServiceResponse = httpServiceClient.post(realmURL, payload, getCommonHeaders());
        Realm realm = jsonConverter.deserialize(httpServiceResponse.getBody(), Realm.class);
        return realm;
    }

    public String createRealmUsingSystemTicket(String numberOfRealms, String realmNamePrefix, String realmEmail) {
        List<RealmIdPersonaIdPair> realmIdPersonaIdPairs = IUSClientWrapper.createRealm(numberOfRealms, realmNamePrefix, realmEmail);

        if (realmIdPersonaIdPairs.isEmpty()) {
            throw new RuntimeException("Error in Realm creation");
        }

        RealmIdPersonaIdPair realmIdPersonaIdPair = realmIdPersonaIdPairs.get(0);
        return realmIdPersonaIdPair.getRealmId();
    }

    private Map<String, String> generateRealmPayload(String realmName) {
        Map<String, String> payload = new HashMap<>();
        payload.put("displayName", realmName);
        return payload;
    }

    public Grant createGrant(IAMTicket iamTicket, Realm realm, String offeringId) {
        Validate.notNull(iamTicket, "iamTicket cannot be null");
        Validate.notNull(realm, "realm cannot be null");
        Validate.notNull(offeringId, "offeringId cannot be null or empty");

        Grant grant = generateGrantPayload(realm, offeringId);
        return IUSGrantClient.addGrantIgnoreExists(iamTicket, grant);
    }

    private Grant generateGrantPayload(Realm realm, String offeringId) {
        Grant grant = new Grant();
        grant.setGrantType(GrantType.OFFERING_APP_GRANT.name());
        grant.setRealmId(realm.getRealmId());
        grant.setOfferingId(offeringId);
        return grant;
    }

    private String generateRealmName(String uniqueId) {
        return StringUtils.join(REALM_NAME_PREFIX, StringUtils.SPACE, uniqueId);
    }

    private String generateUniqueEmail(String uniqueId) {
        return StringUtils.replace(DEFAULT_EMAIL_ADDRESS_PREFIX, "{uniqueId}", uniqueId);
    }

    private String getUniqueId() {
        return Long.toString(new Date().getTime());
    }

    private Map<String, String> getCommonHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        return headers;
    }

}
