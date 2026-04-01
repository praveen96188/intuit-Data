package com.intuit.sbd.payroll.sap.userutils;

import javax.naming.directory.*;
import javax.naming.*;
import java.util.Hashtable;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: cyoder
 * Date: Oct 29, 2008
 * Time: 12:51:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class LdapHelper {

    public static Hashtable ldapEnvironmentVariables = new Hashtable();

    static {
        try {
            ldapUrl = ConfigurationManager.getSettingValue(ConfigurationModule.SAPAdapter, "ldapUrl");
            ldapEnvironmentVariables.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            ldapEnvironmentVariables.put(Context.PROVIDER_URL, ldapUrl);
            ldapEnvironmentVariables.put(Context.SECURITY_AUTHENTICATION, "simple");
            ldapEnvironmentVariables.put(Context.SECURITY_PRINCIPAL, "uid=automatedtaxforms,ou=apps,o=intuit.com");
            ldapEnvironmentVariables.put(Context.SECURITY_CREDENTIALS, "AutomatedTaxFormsZHZ");
            ldapEnvironmentVariables.put(Context.SECURITY_PROTOCOL, "ssl");
        }catch(Throwable ex) {
            throw new Exception("Could not retrieve Directory Servier/LDAP connection parameters");
        }
    }

    public static ArrayList<Hashtable> getUsersFromNameSearch(String name) throws Exception {
        ArrayList<Hashtable> resultList = new ArrayList<Hashtable>();

        String corpId = null;
        DirContext dirContext = null;

        String[] attrIDs = {"intuitCorpID", "cn", "uid", "mail", "givenname", "sn"}; //This can be extended to return multiple attributes.

        try {
            String filter = "(cn=*" + name + "*)";   // Filter for searching.

            dirContext = new InitialDirContext(ldapEnvironmentVariables);

            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(attrIDs);
            constraints.setCountLimit(50);
            constraints.setTimeLimit(10000);

            //Execute search. Starting at search base with our filter.
            NamingEnumeration results = dirContext.search("o=Intuit.com", filter, constraints);

            if(results != null && results.hasMore()) {
                while(results.hasMore())
                {
                    Hashtable returnedValues = new Hashtable();
                    SearchResult sr = (SearchResult) results.next();
                    Attributes a = sr.getAttributes();
                    for (NamingEnumeration ae = a.getAll(); ae.hasMore();) {
                        Attribute attr = (Attribute) ae.next();
                        String attrName = attr.getID();

                        for (NamingEnumeration e = attr.getAll(); e.hasMore();) {
                            Object attrValue = e.next();
                            returnedValues.put(attrName, (String) attrValue);
                        }
                    }
                    resultList.add(returnedValues);
                }

            }

            return resultList;

        } catch(Exception e) {
            throw e;
        }
    }

    public static Hashtable getUserLDAPInformation(String userId) throws Exception {
        Hashtable returnedValues = new Hashtable();;
        String corpId = null;
        DirContext dirContext = null;

        String[] attrIDs = {"intuitCorpID", "cn", "uid", "mail", "givenname", "sn"}; //This can be extended to return multiple attributes.

        try {
            String filter = "(uid=" + userId + ")";   // Filter for searching.

            dirContext = new InitialDirContext(ldapEnvironmentVariables);

            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(attrIDs);
            constraints.setCountLimit(5);
            constraints.setTimeLimit(10000);

            //Execute search. Starting at search base with our filter.
            NamingEnumeration results = dirContext.search("o=Intuit.com", filter, constraints);

            if(results != null && results.hasMore()) {
                SearchResult sr = (SearchResult) results.next();
                Attributes a = sr.getAttributes();
                for (NamingEnumeration ae = a.getAll(); ae.hasMore();) {
                    Attribute attr = (Attribute) ae.next();
                    String attrName = attr.getID();

                    for (NamingEnumeration e = attr.getAll(); e.hasMore();) {
                        Object attrValue = e.next();
                        returnedValues.put(attrName, (String) attrValue);
                    }
                }
            }

            return returnedValues;

        } catch(Exception e) {
            throw e;
        }
    }
    



     public static Hashtable getUserLDAPInformationFromCorpId(String corpId) throws Exception {
        Hashtable returnedValues = new Hashtable();
        DirContext dirContext = null;

        String[] attrIDs = {"intuitCorpID", "cn", "uid", "mail", "givenname", "sn"}; //This can be extended to return multiple attributes.

        try {
            String filter = "(intuitCorpID=" + corpId + ")";   // Filter for searching.

            dirContext = new InitialDirContext(ldapEnvironmentVariables);

            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(attrIDs);
            constraints.setCountLimit(5);
            constraints.setTimeLimit(10000);

            //Execute search. Starting at search base with our filter.
            NamingEnumeration results = dirContext.search("o=Intuit.com", filter, constraints);

            if(results != null && results.hasMore()) {
                SearchResult sr = (SearchResult) results.next();
                Attributes a = sr.getAttributes();
                for (NamingEnumeration ae = a.getAll(); ae.hasMore();) {
                    Attribute attr = (Attribute) ae.next();
                    String attrName = attr.getID();

                    for (NamingEnumeration e = attr.getAll(); e.hasMore();) {
                        Object attrValue = e.next();
                        returnedValues.put(attrName, (String) attrValue);
                    }
                }
            }

            return returnedValues;

        } catch(Exception e) {
            throw e;
        }
    }


    public static boolean isAuthenticated(String userId, String password) throws Exception {
        boolean authRet = false;

        String userDn = getUserDn(userId);
        if(userDn == null)
            throw new Exception("Invalid Credentials.");

        return isAuthenticated(userDn, userId, password);
    }

    private static boolean  isAuthenticated(String userDN, String userName, String password) throws Exception {
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
        } catch (Exception e) {
            throw e;
        }
    }

    private static String getUserDn(String userId) throws Exception {

        String dn = null;
        DirContext dirContext = null;

        String[] attrIDs = {"dn"}; //This can be extended to return multiple attributes.

        try {
            String filter = "(uid=" + userId + ")";   // Filter for searching.

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

        } catch(Exception e) {
            throw e;
        }
    }



}
