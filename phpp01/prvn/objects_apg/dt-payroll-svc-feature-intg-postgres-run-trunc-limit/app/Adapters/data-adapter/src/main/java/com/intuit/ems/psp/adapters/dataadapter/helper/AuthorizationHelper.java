package com.intuit.ems.psp.adapters.dataadapter.helper;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.Set;

/**
 * Created by charithah418 on 11/12/15.
 */
public class AuthorizationHelper implements SecurityContext {

    Set<String> roles;
    String username;
    boolean isSecure;
    public AuthorizationHelper(Set<String> roles, final String username,
                      boolean isSecure) {
        this.roles = roles;
        this.username = username;
        this.isSecure = isSecure;
    }


    public Principal getUserPrincipal() {
        return new User(username);
    }

    public boolean isUserInRole(String role) {
        return roles.contains(role);
    }


    public boolean isSecure() {
        return isSecure;
    }

    public String getAuthenticationScheme() {
        return "HMAC";
    }


    public  class User implements Principal {
        String name;

        public User(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }
}

