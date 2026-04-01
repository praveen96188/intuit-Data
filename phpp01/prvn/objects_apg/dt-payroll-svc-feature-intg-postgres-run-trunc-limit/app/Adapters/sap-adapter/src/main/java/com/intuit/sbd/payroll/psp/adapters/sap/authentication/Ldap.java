/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/authentication/Ldap.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.authentication;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Ldap - Ldap Adapter class for SAP authentication
 * 
 * @author Joe Warmelink
 *
 *  */
public class Ldap {

    private static SpcfLogger logger = SpcfLogManager.getLogger(Ldap.class);

    private static final String CTXINIT = "com.sun.jndi.ldap.LdapCtxFactory";
    private static final String CTXAUTH = "simple";
    private Hashtable ldapEnvironmentVariables;

    public static Ldap createInstance() throws Exception {
        String ldapDn;
        String ldapPassword;
        String ldapUrl;
        boolean ldapSecure = false;

        try {
            ldapDn= SystemParameter.findValue(SystemParameter.Code.PSPUI_LDAP_DN);
            ldapPassword = SystemParameter.findValue(SystemParameter.Code.PSPUI_LDAP_PASSWORD);
            ldapUrl = SystemParameter.findValue(SystemParameter.Code.PSPUI_LDAP_URL);
            ldapSecure = SystemParameter.findBooleanValue(SystemParameter.Code.PSPUI_LDAP_ENABLE_SSL);
        }
        catch (Throwable ex) {
            throw new Exception("Could not retrieve Directory Servier/LDAP connection parameters");
        }

        return new Ldap(ldapDn, ldapPassword, ldapUrl, ldapSecure);
    }

    /**
     * Constructs a new Ldap object.
     *
     * @param userDN        Enterprise Directory distinguished name
     * @param password      Enterprise Directory password for distinguished name
     * @param ldapURL       URL for ldap server
     * @param useSecurePort boolean indicating whether communication(s) will be
     *                      transmitted over SSL or unencrypted
     */
    private Ldap(String userDN, String password, String ldapURL, boolean useSecurePort) {
        ldapEnvironmentVariables = new Hashtable();
        ldapEnvironmentVariables.put(Context.INITIAL_CONTEXT_FACTORY, CTXINIT);
        ldapEnvironmentVariables.put(Context.PROVIDER_URL, ldapURL);
        ldapEnvironmentVariables.put(Context.SECURITY_AUTHENTICATION, CTXAUTH);
        ldapEnvironmentVariables.put(Context.SECURITY_PRINCIPAL, userDN);
        ldapEnvironmentVariables.put(Context.SECURITY_CREDENTIALS, password);

        if (useSecurePort) {
            ldapEnvironmentVariables.put(Context.SECURITY_PROTOCOL, "ssl");
        }
    }

    /**
     * Extract user's DN from Intuit Enterprise Directory for use as Security Prinicipal in isAuthenticated.
     *
     * @param userName Enterprise Directory username
     * @return String Intuit employee's corporate id (intuitcorpid)
     */
    public String getUserDn(String userName) throws Exception {
        String dn = null;
        DirContext dirContext = null;

        String[] attrIDs = {"dn"}; //This can be extended to return multiple attributes.

        try {
            String filter = "(uid=" + userName + ")";   // Filter for searching.

            dirContext = new InitialDirContext(ldapEnvironmentVariables);

            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(attrIDs);
            constraints.setCountLimit(5);
            constraints.setTimeLimit(10000);

            //Execute search. Starting at search base with our filter.
            NamingEnumeration results = dirContext.search("o=Intuit.com", filter, constraints);

            while (results != null && results.hasMore()) {
                SearchResult searchResult = (SearchResult) results.next();
                dn = searchResult.getName() + ",o=intuit.com";
            }

            return dn;

        } catch (AuthenticationException e) {
            logger.debug("LOGIN_LDAP_AUTHENTICATION_FAILURE AuthenticationException method:getUserDn userName:" + userName);
            logger.debug(e.getMessage(), e);

            throw e;
        }
        catch (CommunicationException e) {
            logger.error("LOGIN_LDAP_AUTHENTICAITON_FAILURE CommunicationException method:getUserDn userName :" + userName);
            logger.error(e.getMessage(), e);

            throw e;
        }
        catch (NamingException e) {
            logger.error("LOGIN_LDAP_AUTHENTICATION_FAILURE NamingException method:getUserDn userName :" + userName);
            logger.error(e.getMessage(), e);

            throw e;
        }
        catch (Exception e) {
            logger.error("LOGIN_LDAP_AUTHENTICATION_FAILURE  UnknownException method:getUserDn userName:" + userName);
            logger.error(e.getMessage(), e);

            throw e;
        } finally {
            if (dirContext != null) {
                try {
                    dirContext.close(); //Cleanup resources.
                } catch (Exception e) {
                    logger.error("LOGIN_LDAP_AUTHENTICATION_FAILURE method:getUserDn Exception trying to close dirContext");
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

     public boolean corpIdExists(String ldapCorpId) throws Exception {
        String corpId = null;
        DirContext dirContext = null;

        String[] attrIDs = {"intuitCorpID"}; //This can be extended to return multiple attributes.

        try {
            String filter = "(intuitCorpID=" + ldapCorpId + ")";   // Filter for searching.

            dirContext = new InitialDirContext(ldapEnvironmentVariables);

            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(attrIDs);
            constraints.setCountLimit(5);
            constraints.setTimeLimit(10000);

            //Execute search. Starting at search base with our filter.
            NamingEnumeration results = dirContext.search("o=Intuit.com", filter, constraints);

            return (results != null && results.hasMore());

        } catch(Throwable t) {
            return false;
        }
     }

    public String getCorpId(String userName) throws Exception {
        String corpId = null;
        DirContext dirContext = null;

        String[] attrIDs = {"intuitCorpID"}; //This can be extended to return multiple attributes.

        try {
            String filter = "(uid=" + userName + ")";   // Filter for searching.

            dirContext = new InitialDirContext(ldapEnvironmentVariables);

            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(attrIDs);
            constraints.setCountLimit(5);
            constraints.setTimeLimit(10000);

            //Execute search. Starting at search base with our filter.
            NamingEnumeration results = dirContext.search("o=Intuit.com", filter, constraints);

            while (results != null && results.hasMore()) {
                SearchResult searchResult = (SearchResult) results.next();
                corpId = searchResult.getName();

                // strip off 'intuitCorpId=' and ',ou=Employees,ou=People' just leaving the corp id
                corpId = corpId.substring(0, corpId.indexOf(","));
                corpId = corpId.substring(corpId.indexOf("=") + 1);
            }

            return corpId;

        } catch (AuthenticationException e) {
            logger.debug("LOGIN_LDAP_AUTHENTICATION_FAILURE AuthenticationException method:getUserDn userName:" + userName);
            logger.debug(e.getMessage(), e);

            throw e;
        }
        catch (CommunicationException e) {
            logger.error("LOGIN_LDAP_AUTHENTICAITON_FAILURE CommunicationException method:getUserDn userName :" + userName);
            logger.error(e.getMessage(), e);

            throw e;
        }
        catch (NamingException e) {
            logger.error("LOGIN_LDAP_AUTHENTICATION_FAILURE NamingException method:getUserDn userName :" + userName);
            logger.error(e.getMessage(), e);

            throw e;
        }
        catch (Exception e) {
            logger.error("LOGIN_LDAP_AUTHENTICATION_FAILURE  UnknownException method:getUserDn userName:" + userName);
            logger.error(e.getMessage(), e);

            throw e;

        } finally {
            if (dirContext != null) {
                try {
                    dirContext.close(); //Cleanup resources.
                } catch (Exception e) {
                    logger.error("LOGIN_LDAP_AUTHENTICATION_FAILURE method:getUserDn Exception trying to close dirContext");
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Authenticate an application or user against the Intuit Enterprise Directory.
     *
     * @param userName Enterprise Directory userDN
     * @param userName Enterprise Directory username
     * @param password Enterprise Directory password
     * @return boolean indicating successful authentication
     */
    public boolean  isAuthenticated(String userDN, String userName, String password) throws Exception {
        boolean auth = false;
        DirContext dirContext = null;  //Interface for accessing directory services.

        try {
            String[] attrIDs = {"mail"};
            String filter = "(uid=" + userName + ")";

            ldapEnvironmentVariables.put(Context.SECURITY_PRINCIPAL, userDN);
            ldapEnvironmentVariables.put(Context.SECURITY_CREDENTIALS, password);

            dirContext = new InitialDirContext(ldapEnvironmentVariables);

            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(attrIDs);
            constraints.setCountLimit(1000);
            constraints.setTimeLimit(10000);

            //Execute search. Starting at search base with our filter.
            NamingEnumeration results = dirContext.search("o=Intuit.com", filter, constraints);

            if (results != null) {
                auth = true;
            }
            return auth;

        } catch (AuthenticationException e) {
            // Authentication fails. most likely an invalid password because user's username was already
            // authenticated by getUserDn()
            logger.debug("LOGIN_LDAP_AUTHENTICATION_FAILURE AuthenticationException method:isAuthenticated userName:" + userName);
            logger.debug(e);

            throw e;
        }
        catch (CommunicationException e) {
            logger.error("LOGIN_LDAP_AUTHENTICAITON_FAILURE CommunicationException method:isAuthenticated userName :" + userName);
            logger.error(e.getMessage(), e);

            throw e;
        }
        catch (NamingException e) {
            // LDAP error.
            logger.error("LOGIN_LDAP_AUTHENTICATION_FAILURE NamingException method:isAuthenticated userName :" + userName);
            logger.error(e.getMessage(), e);

            throw e;
        }
        catch (Exception e) {
            // Unknown error
            logger.error("LOGIN_LDAP_AUTHENTICATION_FAILURE  UnknownException method:isAuthenticated userName:" + userName);
            logger.error(e.getMessage(), e);

            throw e;
        } finally {
            if (dirContext != null) {
                try {
                    dirContext.close(); //Cleanup resources.
                } catch (Exception e) {
                    logger.error("LOGIN_LDAP_AUTHENTICATION_FAILURE method:isAuthenticated Exception trying to close dirContext");
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Extract employee's attributes from Intuit Enterprise Directory.
     *
     * @param userName Enterprise Directory username
     * @param userDN   Enterprise Directory distinguished name
     * @param password Enterprise Directory password for distinguished name
     * @return Map of available attributes for employee
     * @throws Exception
     */
    public Map getAttributesForEmployee(String userDN, String userName, String password) throws Exception {
        try {
            Map m = new HashMap();

            String filter = "(uid=" + userName + ")";    // Filter for searching.

            //This can be extended to return any Intuit directory values for this user.
            //For this example, return all attributes.
            String[] attrIDs = {"*"};

            ldapEnvironmentVariables.put(Context.SECURITY_PRINCIPAL, userDN);
            ldapEnvironmentVariables.put(Context.SECURITY_CREDENTIALS, password);

            DirContext dirContext = new InitialDirContext(ldapEnvironmentVariables);

            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(attrIDs);
            constraints.setCountLimit(1000);
            constraints.setTimeLimit(10000);
            
            //Execute search. Starting at search base with our filter.
            NamingEnumeration results = dirContext.search("o=Intuit.com", filter, constraints);

            //Iterate thru search results.
            while (results != null && results.hasMore()) {
                SearchResult sr = (SearchResult) results.next();
                Attributes a = sr.getAttributes();

                for (NamingEnumeration ae = a.getAll(); ae.hasMore();) {
                    Attribute attr = (Attribute) ae.next();
                    String attrName = attr.getID();

                    for (NamingEnumeration e = attr.getAll(); e.hasMore();) {
                        Object attrValue = e.next();
                        if (attrValue instanceof java.lang.String) {
                            //System.out.println(attrValue);
                            m.put(attrName, attrValue);
                            //System.out.println(attrName + ":" + attrValue);  //for debug only
                        }
                    }
                }
            }

            dirContext.close();

            return m;

        } catch (Exception e) {
            //log.debug(e);
            throw new Exception("LDAPAuthBean.getEmplId: " + e);
        }
    }
}
