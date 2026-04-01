package com.intuit.sbd.payroll.psp.gateways.iam;

import com.intuit.client.ius.*;
import com.intuit.iam.utilities.IamConfiguration;
import com.intuit.iam.utilities.UserSearchField;
import com.intuit.platform.integration.ius.common.types.*;
import com.intuit.sbd.payroll.psp.common.utils.offlineticket.ConfigType;
import com.intuit.sbd.payroll.psp.common.utils.offlineticket.OfflineTicketGenerator;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbg.psp.webserviceclient.context.ContextConstants;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class IUSClientWrapper {
    private static final Logger logger = LoggerFactory.getLogger(IUSClientWrapper.class);
    final private static IUSRestTransport iusRestTransport;

    private static final String EMPLOYEE_ROLE_NAME = "employee";
    private static final String ROLE_TYPE = "OFFERING";
    private static final String ROLE_ID = "Intuit.ems.Employee";

    static {
        IUSAppCallback appCallback = new IUSAppCallback();

        IamConfiguration.setIUSRestUri(appCallback.getIUSRestUri());
        IamConfiguration.setIUSConnectionTimeOut(appCallback.getIUSConnectionTimeOut());
        IamConfiguration.setIUSReadTimeOut(appCallback.getIUSReadTimeOut());
        IamConfiguration.setLogger(appCallback.getLogger());

        iusRestTransport = new IUSRestTransportImpl(appCallback);
    }

    public static List<RealmIdPersonaIdPair> createRealm(String numberOfRealms, String realmNamePrefix, String realmEmail){
        IUSRealmClient.setTransport(iusRestTransport);
        IAMTicket iamTicketForSystemUser = getIAMTicket(PSPRealmType.INTUIT_FIXED_REALM, null);
        BatchCreateRealmsResponse batchCreateRealmsResponse = IUSRealmClient.batchCreateRealm(numberOfRealms, realmNamePrefix, realmEmail, iamTicketForSystemUser);
        return batchCreateRealmsResponse.getRealmIdPersonaIdPairs();
    }

    public static String getPersonaInRealmIfExists(String userAuthId, String realmId) {
        logger.info("getPersonaInRealmIfExists user={} in realm={}", userAuthId, realmId);
        IUSRealmClient.setTransport(iusRestTransport);
        IAMTicket iamTicket = getIAMTicket(PSPRealmType.CUSTOMER_WIDE_REALM, realmId);

        try {
            List<Persona> personas = IUSRealmClient.getPersonas(iamTicket, realmId);
            for (Persona persona : personas) {
                if (StringUtils.equals(persona.getUserId(), userAuthId)) {
                    return persona.getPersonaId();
                }
            }
        } catch( Exception e) {
            logger.error("Error checking user={} in realm={} ", userAuthId, realmId, e);
        }
        return null;
    }

    public static String addUserToRealm(String userAuthId, String realmId) {
        logger.info("AddUserToRealm UserId={} CompanyRealmId={}", userAuthId, realmId);
        Persona persona = getPersona(userAuthId, realmId);
        MapPersonasRequest mapPersonasRequest = getMapPersonasRequest(userAuthId, persona);
        return mapPersonaWithRealmResponse(userAuthId, realmId, mapPersonasRequest);
    }

    private static String mapPersonaWithRealmResponse(String userAuthId, String realmId, MapPersonasRequest mapPersonasRequest) {
        IUSRealmClient.setTransport(iusRestTransport);
        logger.info("MapPersonaWithRealmResponse UserId={} CompanyRealmId={}", userAuthId, realmId);
        try{
            IAMTicket iamTicket = getIAMTicket(PSPRealmType.CUSTOMER_WIDE_REALM, realmId);
            Personas personas = IUSRealmClient.mapPersonaWithRealm(iamTicket, realmId, mapPersonasRequest);
            List<Persona> personaList = personas.getPersona();
            if(Objects.nonNull(personas) && !personaList.isEmpty() && Objects.nonNull(personaList.get(0))) {
                return personaList.get(0).getPersonaId();
            }
        } catch( Exception e) {
            logger.error("Error Adding user={} in realm={}", userAuthId, realmId, e);
        }
        return null;
    }

    private static MapPersonasRequest getMapPersonasRequest(String userAuthId, Persona persona) {
        MapPersonasRequest mapPersonasRequest = new MapPersonasRequest();
        mapPersonasRequest.getPersona().add(persona);
        mapPersonasRequest.getUserIds().add(userAuthId);
        return mapPersonasRequest;
    }

    private static Persona getPersona(String userAuthId, String realmId) {
        Persona persona = new Persona();
        persona.setUserId(userAuthId);
        persona.setRealmId(realmId);
        persona.setPersonaName(UUID.randomUUID().toString());
        Role role = new Role();
        role.setName(EMPLOYEE_ROLE_NAME);
        role.setRoleId(ROLE_ID);
        role.setRoleType(ROLE_TYPE);
        persona.getRole().add(role);
        return persona;
    }

    public static List<Persona> findAdminPersonaForConsumerRealmId(String consumerRealmId){

        IUSRealmClient.setTransport(iusRestTransport);
        IAMTicket iamTicket = new IAMTicket();
        String customerwideRealmid = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_customerwide_realmid");

        iamTicket.setTicket(
                OfflineTicketGenerator.getInstance().getOfflineTicket(ConfigType.PSP, customerwideRealmid, consumerRealmId)
        );

        List<Persona> personas = IUSRealmClient.getPersonasFilterByRoleType(iamTicket, consumerRealmId, "BUSINESS");
        List<Persona> adminPersonas = new ArrayList<>();
        for(Persona persona : personas){
            List<Role> roles = persona.getRole();
            for(Role role : roles){
                if("ADMIN".equals(role.getName())){
                    adminPersonas.add(persona);
                    break;
                }
            }
        }
        return adminPersonas;
    }

    public static List<User> findUsersForAuthId(String authId){
        IUSUserClient.setTransport(iusRestTransport);
        String intuitFixedRealmId = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_intuitfixed_realmid");

        IAMTicket iamTicketForSystemUser = new IAMTicket();

        iamTicketForSystemUser.setTicket(
                OfflineTicketGenerator.getInstance().getOfflineTicket(ConfigType.PSP, intuitFixedRealmId, null)
        );

        return IUSUserClient.getAllUsers(iamTicketForSystemUser, UserSearchField.USERID, Collections.singletonList(authId));
    }

    public static List<Grant> findAllGrantsForRealmId(String realmId) {
        IUSGrantClient.setTransport(iusRestTransport);
        IAMTicket iamTicketForSystemUser = getIAMTicket(PSPRealmType.INTUIT_FIXED_REALM, null);
        return IUSGrantClient.getAllGrants(iamTicketForSystemUser, realmId);
    }

    public static void addGrant(Grant grant) {
        IUSGrantClient.setTransport(iusRestTransport);
        IAMTicket iamTicketForSystemUser = getIAMTicket(PSPRealmType.INTUIT_FIXED_REALM, null);
        IUSGrantClient.addGrantIgnoreExists(iamTicketForSystemUser, grant);
    }

    //add grant using the user context of user
    public static void addGrantWithUserContext(Grant grant) {
        IUSGrantClient.setTransport(iusRestTransport);
        AuthorizationContext authorizationContext = RequestAttributesUtils.getAttribute(ContextConstants.USER_AUTHORIZATION_CONTEXT, AuthorizationContext.class);
        if(Objects.nonNull(authorizationContext)){
            IAMTicket iamTicketForUser = authorizationContext.getIAMTicket();
            IUSGrantClient.addGrantIgnoreExists(iamTicketForUser, grant);
        }
    }

    public static void updateGrant(Grant grant) {
        IUSGrantClient.setTransport(iusRestTransport);
        IAMTicket iamTicketForSystemUser = getIAMTicket(PSPRealmType.CUSTOMER_WIDE_REALM, grant.getRealmId());
        IUSGrantClient.updateGrant(iamTicketForSystemUser, grant);
    }

    private static IAMTicket getIAMTicket(PSPRealmType realmType, String realmId) {
        String mPSPRealmId = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, realmType.getValue());
        IAMTicket iamTicketForSystemUser = new IAMTicket();
        iamTicketForSystemUser.setTicket(
                OfflineTicketGenerator.getInstance().getOfflineTicket(ConfigType.PSP, mPSPRealmId, realmId)
        );
        return iamTicketForSystemUser;
    }

}
