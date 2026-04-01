package com.intuit.sbd.payroll.psp.gateways.iam;

import com.intuit.identity.graphql.sdk.client.exceptions.IdentityGraphQLException;
import com.intuit.identity.idlm.model.AccountDigitalIdentityDetails;
import com.intuit.qbdt.identity.authN.offlineticket.OfflineTicketClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import com.intuit.identity.experiences.graphql.client.AddProfileProjectionRoot;
import com.intuit.identity.experiences.graphql.client.AssignRoleProjectionRoot;
import com.intuit.identity.experiences.graphql.client.IdentityDigitalIdentityByLegacyAuthIdProjectionRoot;
import com.intuit.identity.experiences.graphql.model.*;
import com.intuit.identity.idlm.client.IDLMMutation;
import com.intuit.identity.idlm.client.IDLMQuery;
import com.intuit.identity.idlm.exception.IDLMException;
import com.intuit.sbd.payroll.psp.gateways.iam.auth.OfflineAuth;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class IDLMClientWrapper {
    private IDLMQuery idlmQuery;
    private IDLMMutation idlmMutation;
    private OfflineTicketClient offlineTicketClient;
    public static final String DEFAULT_ACCOUNT_ID="-1";
    private static final String ROLE_ID = "Intuit.ems.Employee";


    @Autowired
    IDLMClientWrapper(IDLMQuery idlmQuery, IDLMMutation idlmMutation, OfflineTicketClient offlineTicketClient) {
        this.idlmQuery = idlmQuery;
        this.idlmMutation = idlmMutation;
        this.offlineTicketClient = offlineTicketClient;
    }

    public String addUserToRealm(@NonNull String userAuthId, @NonNull String realmId) throws IdentityGraphQLException, IDLMException {
        log.info("action=addUserToRealm,UserId={},CompanyRealmId={}", userAuthId, realmId);
        String profileId = addProfile(userAuthId, realmId);
        if(StringUtils.isBlank(profileId)) {
            log.error("ProfileId null");
            return null;
        }
        UserAssignment userAssignment = assignRole(profileId, realmId);
        if(userAssignment != null) {
            return userAssignment.getProfileId();
        }
        return null;
    }

    public UserAssignment assignRole(@NonNull String profileId, @NonNull String realmId) throws IDLMException, IdentityGraphQLException {
        log.info("action=assignRole,profileId={},CompanyRealmId={}", profileId, realmId);
        try {
            AssignRoleProjectionRoot assignRoleProjectionRoot = new AssignRoleProjectionRoot().profileId();
            AssignRole assignRole = new AssignRole();
            assignRole.setAccountId(DEFAULT_ACCOUNT_ID);
            assignRole.setProfileId(profileId);
            List<String> roleIds = new ArrayList<String>();
            roleIds.add(ROLE_ID);
            assignRole.setRoles(roleIds);
            OfflineAuth offlineAuth = new OfflineAuth(offlineTicketClient.getOfflineTicket(realmId));
            return idlmMutation.assignRole(assignRole, true, assignRoleProjectionRoot, offlineAuth);
        }
        catch (IDLMException | IdentityGraphQLException ex) {
            log.error("Error assigning role to profile={} in realm={},exception={}", profileId, realmId, ex);
            throw ex;
        }
    }

    public String addProfile(@NonNull String userAuthId, @NonNull String realmId) throws IdentityGraphQLException, IDLMException {
        log.info("action=addProfile,UserId={},CompanyRealmId={}", userAuthId, realmId);
        //addProfile
        try {
            AddProfileProjectionRoot addProfileProjectionRoot = new AddProfileProjectionRoot().id();
            AddProfileInput addProfileInput = addProfileInput(userAuthId, realmId);
            OfflineAuth offlineAuth = new OfflineAuth(offlineTicketClient.getOfflineTicket(realmId));
            Profile profile = idlmMutation.addProfile(addProfileInput, addProfileProjectionRoot, offlineAuth);
            return profile.getId();
        }
        catch (IDLMException | IdentityGraphQLException ex) {
            log.error("Error Adding user={} in realm={}, exception={}", userAuthId, realmId, ex);
            throw ex;
        }
    }

    private AddProfileInput addProfileInput(@NonNull String userAuthId, @NonNull String realmId) {
        ProfileInput profileInput = new ProfileInput();
        profileInput.setClaimedBy(userAuthId);
        String name = UUID.randomUUID().toString();
        profileInput.setDisplayName(name);
        profileInput.setProfileType(ProfileType.PERSON);
        profileInput.setAccountRelationships(Arrays.asList(AccountRelationship.EMPLOYEE));
        PersonInfoInput personInfoInput = new PersonInfoInput.Builder()
                .name(new PersonInfoInput.Builder().name(new PersonNameInput.Builder()
                            .fullName(name).build())
                            .build()
                            .getName())
                .build();
        profileInput.setPersonInfo(personInfoInput);
        AddProfileInput addProfileInput = new AddProfileInput.Builder().profileInput(profileInput).accountId(realmId).build();
        return addProfileInput;
    }

    public IamUser getUserDetailsForAuthId(@NonNull String authId) throws IdentityGraphQLException, IDLMException {
        try {
            AccountDigitalIdentityDetails accountDigitalIdentityDetails = getAccountDigitalIdentityDetails(authId);
            IamUser iamUser = new IamUser();
            if(accountDigitalIdentityDetails != null) {
                String email = getEmailAddress(accountDigitalIdentityDetails.getAccount());
                String userName = accountDigitalIdentityDetails.getIdentityDigitalIdentity()!=null ? accountDigitalIdentityDetails.getIdentityDigitalIdentity().getUsername() : null;
                iamUser.setEmailAddress(email);
                iamUser.setLoginName(userName);
                return iamUser;
            }
            return null;
        }
        catch (IDLMException | IdentityGraphQLException ex) {
            log.error("Error getting details for user={},exception={}", authId, ex);
            throw ex;
        }
   }

    public AccountDigitalIdentityDetails getAccountDigitalIdentityDetails(@NonNull String authId) throws IdentityGraphQLException, IDLMException {
        AccountInput accountInput = new AccountInput.Builder().id(authId).build();
        Identity_DigitalIdentityByLegacyAuthIdFilter identityDigitalIdentityByLegacyAuthIdFilter = Identity_DigitalIdentityByLegacyAuthIdFilter.newBuilder().legacyAuthId(authId).build();
        OfflineAuth offlineAuth = new OfflineAuth(offlineTicketClient.getOfflineTicket());
        return idlmQuery.accountDigitalIdentity(accountInput, identityDigitalIdentityByLegacyAuthIdFilter, offlineAuth);
    }

   private Account getAccountProfiles(@NonNull String userAuthId, @NonNull String realmId) throws IdentityGraphQLException, IDLMException {
       try {
           AccountInput accountInput = new AccountInput.Builder().id(realmId).build();
           ProfilesConnectionFilterInput profilesConnectionFilterInput = new ProfilesConnectionFilterInput();
           ProfilesClaimedByFilterInput profilesClaimedByFilterInput = new ProfilesClaimedByFilterInput();
           StringFilterInput stringFilterInput = new StringFilterInput();
           stringFilterInput.setEq(userAuthId);
           profilesClaimedByFilterInput.setClaimedBy(stringFilterInput);
           profilesConnectionFilterInput.setClaimedByFilter(profilesClaimedByFilterInput);
           OfflineAuth offlineAuth = new OfflineAuth(offlineTicketClient.getOfflineTicket(realmId));
           return idlmQuery.account(accountInput, profilesConnectionFilterInput, null, offlineAuth);
       } catch (IDLMException | IdentityGraphQLException ex) {
           log.error("Error getting profileDetails user={} in realm={},exception={}", userAuthId, realmId, ex);
           throw ex;
       }
   }

   public String getPersonaInRealmIfExists(@NonNull String userAuthId, @NonNull String realmId) throws IdentityGraphQLException, IDLMException {
        Account account = getAccountProfiles(userAuthId, realmId);
        return getProfileId(account);
    }


    private String getProfileId(@NonNull Account account){
        if(account.getProfiles() != null
                && account.getProfiles().getEdges() != null
                && account.getProfiles().getEdges().get(0) !=null
                && account.getProfiles().getEdges().get(0).getNode() != null
                && account.getProfiles().getEdges().get(0).getNode().getId() != null)
            return account.getProfiles().getEdges().get(0).getNode().getId();
        return null;
    }

    private String getEmailAddress(Account account) {
        if(account!=null
                && account.getAccountProfile() != null
                && account.getAccountProfile().getPersonInfo() != null
                && account.getAccountProfile().getPersonInfo().getContactInfo() != null
                && account.getAccountProfile().getPersonInfo().getContactInfo().getEmails() != null
                && account.getAccountProfile().getPersonInfo().getContactInfo().getEmails().size() > 0
                && account.getAccountProfile().getPersonInfo().getContactInfo().getEmails().get(0) != null)
            return account.getAccountProfile().getPersonInfo().getContactInfo().getEmails().get(0).getEmail();
        return null;
    }
}
