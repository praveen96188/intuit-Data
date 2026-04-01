package com.intuit.sbd.payroll.sap.userutils;

import oracle.jdbc.pool.OracleDataSource;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: cyoder
 * Date: Oct 28, 2008
 * Time: 1:22:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class PSPUsers {

    private static String[] validEnvironments = {"local", "qa1", "qa2", "qa3", "qa4", "qa5", "qa6", "lt", "dev1", "dev2", "trn", "dev3"};

    //Environment connection information
    private String dbUrl = "";
    private String dbUsername = "";
    private String dbPassword = "";

    private String deployEnvironment = "local"; //Default value

    private Hashtable authRoleData = new Hashtable();

    private ArrayList<UserInfo> users = new ArrayList<UserInfo>();
    private ArrayList<UserInfo> usersInDb = new ArrayList<UserInfo>();

    private boolean multipleRolesEnabled;

    /* MAIN FUNCTION */
    public static void main(String[] args) {
        System.out.println("PSP Users Command Line Utility");

        if (PSPUsers.argPassed("--help", args) >= 0) {
            PSPUsers.doUsage();
            return;
        }

        //Create new pspUsers
        PSPUsers pspUsers = new PSPUsers();

        try {

            //Only specify environment
            if(args.length == 1) {
                pspUsers.typicalSetupForOperations(args[0]);
                pspUsers.displaySummary();
            } else if(args.length > 1) {

                if (    (PSPUsers.argPassed("--search", args) >= 0) )
                {
                    pspUsers.searchUser();
                    return;
                }

                if (    (PSPUsers.argPassed("--listUsers", args) >= 0) ||
                        (PSPUsers.argPassed("--summary", args) >= 0) ||
                        (PSPUsers.argPassed("--addUserToQuickbase", args) >= 0) ||
                        (PSPUsers.argPassed("--loadUsers", args) >= 0) ||
                        (PSPUsers.argPassed("--loadFromCSV", args) >= 0) ||
                        (PSPUsers.argPassed("--listRoles", args) >= 0)) {
                } else {
                    doUsage();
                    throw new Exception("Invalid parameters. Please read usages.");
                }

                if (PSPUsers.argPassed("--loadFromCSV", args) >= 0) {
                    pspUsers.loadFromCSV();
                }
                if (PSPUsers.argPassed("--addUserToQuickbase", args) >= 0) {
                    pspUsers.searchUserAndAddToQuickbase();
                }

                if (PSPUsers.argPassed("--listRoles", args) >= 0) {
                    pspUsers.loadEnvironment(args[0]);
                    pspUsers.loadConnectionInformation();
                    pspUsers.determineIfMultipleRoles();
                    pspUsers.lookupAuthRoleFKs();
                    pspUsers.displayRolesSummary();
                }

                if (PSPUsers.argPassed("--listUsers", args) >= 0) {
                    pspUsers.loadEnvironment(args[0]);
                    pspUsers.loadConnectionInformation();
                    pspUsers.determineIfMultipleRoles();
                    pspUsers.lookupAuthRoleFKs();
                    pspUsers.loadOldUsersFromDb();
                    pspUsers.displayOldUsersSummary();
                }

                if (PSPUsers.argPassed("--summary", args) >= 0) {
                    pspUsers.typicalSetupForOperations(args[0]);
                    pspUsers.displaySummary();
                }

                if (PSPUsers.argPassed("--loadUsers", args) >= 0) {
                    pspUsers.typicalSetupForOperations(args[0]);
                    pspUsers.loadUsersForEnvironment();
                }
            } else {
                doUsage();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("** An error occurred:\n\t " + e.toString());
        }
    }



    public void searchUser() throws Exception {
        System.out.println("Please enter a partial of the name to search for matching user id:");
        String searchString = promptForInput("Search: ");

        ArrayList<Hashtable> results = LdapHelper.getUsersFromNameSearch(searchString);

        for(Hashtable resultHashTable : results)
        {
            String commonName = (String) resultHashTable.get("cn");
            String uid = (String) resultHashTable.get("uid");
            String corpid = (String) resultHashTable.get("intuitCorpID");

            display("Name: " + commonName + ", userId:" + uid + ", corpId:" + corpid);
        }

    }

    public boolean addUserToQuickbaseFromCorpId(String corpId) throws Exception {
        return addUserToQuickbaseFromCorpId(corpId, "Admin");
    }

    public boolean addUserToQuickbaseFromCorpId(String corpId, String roleName) throws Exception {
        Hashtable resultHashTable = LdapHelper.getUserLDAPInformationFromCorpId(corpId);
        
        if(resultHashTable == null) return false;

        String commonName = (String) resultHashTable.get("cn");
        String corpid = (String) resultHashTable.get("intuitCorpID");


        if(corpid != null)
        {
            UserInfo userInfo = new UserInfo();

            String tokens[] = commonName.split(" ");
            userInfo.firstName = tokens[0];
            userInfo.lastName = tokens[tokens.length - 1];


            //Get rid of last name suffix
            if((userInfo.lastName.equals("Jr") || (userInfo.lastName.equals("Sr")) || (userInfo.lastName.equals("II")) || (userInfo.lastName.equals("III"))) || (userInfo.lastName.indexOf(",") > -1 || userInfo.lastName.indexOf(".") > -1)) {
               if(tokens.length > 2)
                   userInfo.lastName = tokens[tokens.length - 2];
            }

            return QuickbaseUtils.insertUserForAllEnvironments(userInfo.firstName, userInfo.lastName, corpid, roleName);
        } else {
            log("WARNING: CorpId " + corpId + " is not a valid corpId. Skipped.");
        }

        return false;
    }


    /*
    Used to load data from a CSV in format of CORPID,ROLENAME
     */
    private void loadFromCSV() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("init.csv"));
        String inputLine;

        while ((inputLine = br.readLine()) != null) {
            inputLine = inputLine.replace("\"", "");
            String[] tokens = inputLine.split(",");
            String corpId = tokens[0].trim();
            String roleName = tokens[1].trim();
            boolean retVal = addUserToQuickbaseFromCorpId(corpId, roleName);
            if(retVal) {
                log("Added " + corpId);
            } else {
                log("Error adding " + corpId);
            }
        }
    }

    public void searchUserAndAddToQuickbase() throws Exception {
        System.out.println("Please enter a partial of the name to search for matching user id:");
        String searchString = promptForInput("Search: ");

        ArrayList<Hashtable> results = LdapHelper.getUsersFromNameSearch(searchString);

        int retIndex = 0;
        for(Hashtable resultHashTable : results)
        {
            String commonName = (String) resultHashTable.get("cn");
            String uid = (String) resultHashTable.get("uid");
            String corpid = (String) resultHashTable.get("intuitCorpID");
            
            display("[" + retIndex + "] Name: " + commonName + ", userId:" + uid + ", corpId:" + corpid);
            retIndex++;
        }

        if(results.size() == 0) {
            display("No results returned.");
        } else {
            String numChoice = promptForInput("Load user into Quickbase [Enter to escape]: ");

            if(numChoice.equals(""))
            {
                 display("No users added.");
            } else {
                int choiceInt = Integer.parseInt(numChoice);

                if(choiceInt > -1 && choiceInt < results.size())
                {
                   display("Adding user to Quickbase (for all environments) ...");

                   String commonName = (String) results.get(choiceInt).get("cn");
                   String corpid = (String) results.get(choiceInt).get("intuitCorpID");

                   UserInfo userInfo = new UserInfo();

                   String tokens[] = commonName.split(" ");
                   userInfo.firstName = tokens[0];
                   userInfo.lastName = tokens[tokens.length - 1];

                   //Get rid of last name suffix
                   if((userInfo.lastName.equals("Jr") || (userInfo.lastName.equals("Sr")) || (userInfo.lastName.equals("II")) || (userInfo.lastName.equals("III"))) || (userInfo.lastName.indexOf(",") > -1 || userInfo.lastName.indexOf(".") > -1)) {
                       if(tokens.length > 2)
                           userInfo.lastName = tokens[tokens.length - 2];
                   }

                   boolean retVal = QuickbaseUtils.insertUserForAllEnvironments(userInfo.firstName, userInfo.lastName, corpid, "Admin");

                   if(retVal) {
                       display("User added.");
                   } else {
                       display("User already exists. No users added.");
                   }

                } else {
                    display("Invalid choice. No users added.");
                }
            }

        }


    }

    private String promptForInput(String msg) throws Exception {
        System.out.print("\n" + msg);

        BufferedReader in =
                new BufferedReader(new InputStreamReader(System.in));

        String s =  in.readLine();
        return s;
    }

    private void typicalSetupForOperations(String environment) throws Exception {
        loadEnvironment(environment);
        loadConnectionInformation();
        determineIfMultipleRoles();
        lookupAuthRoleFKs();
        loadOldUsersFromDb();
        loadUserDataFromQuickbase();
        compareDatabaseAndoLoadedData();
    }

    private void loadEnvironment(String environmentName) throws Exception {
        for (int i = 0; i < validEnvironments.length; i ++) {
            if(validEnvironments[i].equals(environmentName)) {
                log("Command being run for environment: " + environmentName);
                deployEnvironment = environmentName;
                return;
            }
        }
        throw new Exception("Invalid environment specified. Type --help for usages.");
    }

    private void loadUserDataFromQuickbase() throws Exception {
        log("Loading user data from quickbase...");

        //Clear out old users
        users.clear();

        Vector<HashMap> v = QuickbaseUtils.getRecordsForEnvironment(deployEnvironment);

        for(HashMap h : v)
        {
            UserInfo userData = new UserInfo();
            userData.firstName = (String) h.get("First Name");
            userData.lastName = (String) h.get("Last Name");
            userData.userId = userData.firstName + " " + userData.lastName;
            userData.corpId = (String) h.get("CorpID");
            userData.roleId = (String) h.get("Role");
            userData.guid = getRandomGuid();
            
            if(userData.corpId == null) {
                log("Error with corpId for: " + userData.userId);
            } else {
                userData.corpId = userData.corpId.trim(); //Because I don't trust people to not put spaces into the text field.
                users.add(userData);
            }
        }
    }

    private void compareDatabaseAndoLoadedData() throws Exception {
        for(UserInfo userInfo : users) {
            setFlagsForUser(userInfo);
            userInfo.roleFk = getAuthRoleSEQFromName(userInfo.roleId);
            if(userInfo.roleFk == null)  {
                log("Invalid role specified for user: '" + userInfo.userId + "'" + ":" + userInfo.roleId);
            }
        }
    }


    private void loadUsersForEnvironment() throws Exception {
        //Get database connection
        Connection databaseConnection = getDataSource().getConnection();
        databaseConnection.setAutoCommit(false);

        log("Loading users to database...");

        try {
            for(UserInfo userInfo : users) {
                if (userInfo.roleFk == null) {
                    continue;
                }
                if(userInfo.alreadyInDb) {
                    if (userInfo.hasMultipleRoles) {
                        log("Skipping user " + userInfo.userId + " because user has multiple roles");
                    } else if(userInfo.needsUpdating) {
                        log("Updating user: " + userInfo.userId);
                        Statement stmt = databaseConnection.createStatement();
                        if (multipleRolesEnabled) {
                            stmt.executeUpdate("UPDATE PSP_AUTH_USER_AUTH_ROLE__ASSOC SET AUTH_ROLE_FK='" + userInfo.roleFk + "' WHERE AUTH_USER_FK ='" + userInfo.guid + "'");
                        } else {
                            stmt.executeUpdate("UPDATE PSP_AUTH_USER SET AUTH_ROLE_FK='" + userInfo.roleFk + "' WHERE CORP_ID='" + userInfo.corpId + "'");
                        }

                        stmt.close();
                    } else {
                        log("Skipping user: " + userInfo.userId);
                    }
                } else {
                    log("Adding user: " + userInfo.userId);
                    Statement stmt = databaseConnection.createStatement();
                    databaseConnection.setAutoCommit(false);
                    PreparedStatement pstmt;
                    if (multipleRolesEnabled) {
                        pstmt = databaseConnection.prepareStatement("INSERT INTO PSP_AUTH_USER ( AUTH_USER_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, REALM_ID, CORP_ID, FIRST_NAME, LAST_NAME) VALUES (?, ?, (SELECT CURRENT_TIMESTAMP FROM DUAL), (SELECT CURRENT_TIMESTAMP FROM DUAL), ?, ?, ?, ?)");
                    } else {
                        pstmt = databaseConnection.prepareStatement("INSERT INTO PSP_AUTH_USER ( AUTH_USER_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, REALM_ID, CORP_ID, FIRST_NAME, LAST_NAME, AUTH_ROLE_FK ) VALUES (?, ?, (SELECT CURRENT_TIMESTAMP FROM DUAL), (SELECT CURRENT_TIMESTAMP FROM DUAL), ?, ?, ?, ?, ?)");
                    }

                    pstmt.setString(1, userInfo.guid);
                    pstmt.setInt(2, 1);
                    pstmt.setInt(3, -1);
                    pstmt.setString(4, userInfo.corpId);
                    pstmt.setString(5, userInfo.firstName);
                    pstmt.setString(6, userInfo.lastName);
                    if (! multipleRolesEnabled) {
                        pstmt.setString(7, userInfo.roleFk);
                    }
                    pstmt.executeUpdate();
                    pstmt.close();

                    if (multipleRolesEnabled) {
                        PreparedStatement roleAssocStatement = databaseConnection.prepareStatement("INSERT INTO PSP_AUTH_USER_AUTH_ROLE__ASSOC (AUTH_USER_FK, AUTH_ROLE_FK) VALUES (?, ?)");
                        roleAssocStatement.setString(1, userInfo.guid);
                        roleAssocStatement.setString(2, userInfo.roleFk);
                        roleAssocStatement.executeUpdate();
                        roleAssocStatement.close();
                    }


                }
            }
            databaseConnection.commit();
            log("Load done.");
        } catch (Exception e) {
            e.printStackTrace();
            databaseConnection.rollback();
            log("Rollback executed. Exception occurred.");
            throw e;
        } finally {
            databaseConnection.close();
        }

    }



    /* Hard coded configuration information */
    private void loadConnectionInformation() throws Exception {

        if(deployEnvironment.equals("local")) {
            dbUrl = "jdbc:oracle:thin://@localhost:1521:XE";
            dbUsername = "psp_local";
            dbPassword = "psp_local";
        } else if(deployEnvironment.equals("qa1")) {
            dbUrl = "jdbc:oracle:thin:@sdgdbpspqa01.corp.intuit.net:1521:pspqa01";
            dbUsername = "Pspapp";
            dbPassword = "pspapp01";
        } else if(deployEnvironment.equals("qa2")) {
            dbUrl = "jdbc:oracle:thin:@sdgdbpspqa02.corp.intuit.net:1521:pspqa01";
            dbUsername = "Pspapp";
            dbPassword = "pspapp02";
        } else if(deployEnvironment.equals("qa3")) {
            dbUrl = "jdbc:oracle:thin:@sdgdbpspqa03.corp.intuit.net:1521:pspqa01";
            dbUsername = "Pspapp";
            dbPassword = "pspapp03";
        } else if(deployEnvironment.equals("qa4")) {
            dbUrl = "jdbc:oracle:thin:@sdgdbpspqa04.corp.intuit.net:1521:pspqa01";
            dbUsername = "Pspapp";
            dbPassword = "pspapp04";
        } else if(deployEnvironment.equals("qa5")) {
            dbUrl = "jdbc:oracle:thin:@sdgdbpspqa05.corp.intuit.net:1521:pspqa01";
            dbUsername = "Pspapp";
            dbPassword = "pspapp05";
        } else if(deployEnvironment.equals("dev1")) {
            dbUrl = "jdbc:oracle:thin:@rnodevora01.reno.intuit.com:1521:pspdev02";
            dbUsername = "pspapp01";
            dbPassword = "pspapp01";
        } else if(deployEnvironment.equals("dev2")) {
            dbUrl = "jdbc:oracle:thin:@rnodevora01.reno.intuit.com:1521:pspdev02";
            dbUsername = "pspapp";
            dbPassword = "pspapp01";
        } else if(deployEnvironment.equals("qa6")) {
            dbUrl = "jdbc:oracle:thin:@sdgdbpspqa06.corp.intuit.net:1521:pspqa06";
            dbUsername = "Pspapp";
            dbPassword = "pspapp06";
        } else if(deployEnvironment.equals("lt")) {
            dbUrl = "jdbc:oracle:thin:@(DESCRIPTION =" +
                    "(ADDRESS = (PROTOCOL = TCP)(HOST = sdgdbpsplt01-vip.corp.intuit.net)(PORT = 1521))" +
                    "(ADDRESS = (PROTOCOL = TCP)(HOST = sdgdbpsplt02-vip.corp.intuit.net)(PORT = 1521))" +
                    "(ADDRESS = (PROTOCOL = TCP)(HOST = sdgdbpsplt03-vip.corp.intuit.net)(PORT = 1521))" +
                    "(LOAD_BALANCE = yes)" +
                    "(CONNECT_DATA =(SERVER = DEDICATED)(SERVICE_NAME = pspprf)" +
                    "(FAILOVER_MODE =(TYPE = SELECT)(METHOD = BASIC)(RETRIES = 180)(DELAY = 5))))";
            dbUsername = "reladm";
            dbPassword = "reladm01";
        } else if (deployEnvironment.equals("trn")) {
            dbUrl = "jdbc:oracle:thin:@sdgdbpsptrn01.corp.intuit.net:1521:psptrn01";
            dbUsername = "Pspapp";
            dbPassword = "pspapp01";
        } else if (deployEnvironment.equals("dev3")) {
            dbUrl = "jdbc:oracle:thin:@sdgpspdv03.corp.intuit.net:1521:pspdv03";
            dbUsername = "Pspapp";
            dbPassword = "pspapp01";
        } else {
            throw new Exception("Unexpected environment loaded.");
        }
    }

    private void determineIfMultipleRoles() throws Exception {
        Connection databaseConnection = null;
        try {
            databaseConnection = getDataSource().getConnection();
            Statement stmt = databaseConnection.createStatement();
            ResultSet rs = stmt.executeQuery("select * from all_tables where table_name = 'PSP_AUTH_USER_AUTH_ROLE__ASSOC'");
            if (rs.next()) {
                multipleRolesEnabled = true;
            } else {
                multipleRolesEnabled = false;
            }
            rs.close();
        } finally {
            if(databaseConnection != null) databaseConnection.close();
        }
    }

    private DataSource getDataSource() throws Exception {
        OracleDataSource oracleDS = new OracleDataSource();
        oracleDS.setURL(dbUrl);
        oracleDS.setUser(dbUsername);
        oracleDS.setPassword(dbPassword);

        Connection connection = oracleDS.getConnection();
        if (connection == null) {
            throw new Exception("Unable to get connection based on parameters passed.");
        } else {
            connection.close();
        }

        return oracleDS;
    }

    private void setFlagsForUser(UserInfo user) {
        for(UserInfo userInfo : usersInDb) {
            if(userInfo.corpId != null && userInfo.corpId.equals(user.corpId)) {
                user.alreadyInDb = true;

                //Set guid to one already in database
                user.guid = userInfo.guid;

                if(userInfo.roleId.equals(user.roleId)) {
                    user.needsUpdating = false;
                } else {
                    user.needsUpdating = true;
                }

                user.hasMultipleRoles = userInfo.hasMultipleRoles;
                return;
            }
        }
        user.alreadyInDb = false;
    }

    public UserInfo getExistingDBRecordForUser(String corpId) {
        for(UserInfo userInfo : usersInDb) {
            if(userInfo.corpId != null && userInfo.corpId.equals(corpId)) {
                return userInfo;
            }
        }
        return null;
    }

    public void loadOldUsersFromDb() throws Exception {
        log("Loading users already in database...");

        usersInDb.clear();

        Connection databaseConnection = null;
        try {
            databaseConnection = getDataSource().getConnection();
            Statement stmt = databaseConnection.createStatement();
            ResultSet rs;
            if (multipleRolesEnabled) {
                rs = stmt.executeQuery("SELECT au.FIRST_NAME, au.LAST_NAME, au.AUTH_USER_SEQ, au.CORP_ID, min(auth_role_fk) AUTH_ROLE_FK, count(auth_role_fk) NUM_ROLES FROM PSP_AUTH_USER au join PSP_AUTH_USER_AUTH_ROLE__ASSOC userRole on au.auth_user_seq = userRole.auth_user_fk group by au.FIRST_NAME, au.LAST_NAME, au.AUTH_USER_SEQ, au.CORP_ID");
            } else {
                rs = stmt.executeQuery("SELECT * FROM PSP_AUTH_USER");
            }
            while (rs.next()) {
                UserInfo oldDbUser = new UserInfo();
                oldDbUser.firstName = rs.getString("FIRST_NAME");
                oldDbUser.lastName = rs.getString("LAST_NAME");
                oldDbUser.roleId = getAuthRoleNameFromSEQ(rs.getString("AUTH_ROLE_FK"));
                oldDbUser.roleFk = rs.getString("AUTH_ROLE_FK");
                oldDbUser.guid = rs.getString("AUTH_USER_SEQ");
                oldDbUser.corpId = rs.getString("CORP_ID");
                oldDbUser.alreadyInDb = true;
                oldDbUser.hasMultipleRoles = multipleRolesEnabled && rs.getInt("NUM_ROLES") > 1;
                usersInDb.add(oldDbUser);
            }
            rs.close();
        } finally {
            if(databaseConnection != null) databaseConnection.close();
        }
    }



    /* Need the AUTH_ROLE_SEQ to Name lookups */
    public void lookupAuthRoleFKs() throws Exception {
        log("Loading user roles list from database...");
        Connection databaseConnection = null;
        authRoleData.clear();

        try {
            databaseConnection = getDataSource().getConnection();
            Statement stmt = databaseConnection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM PSP_AUTH_ROLE");
            while (rs.next()) {
                authRoleData.put(rs.getString("NAME"), rs.getString("AUTH_ROLE_SEQ"));
            }
            rs.close();
        } finally {
            if(databaseConnection != null) databaseConnection.close();
        }
    }

    public String getAuthRoleSEQFromName(String authRoleName) {
        return (String) authRoleData.get(authRoleName);
    }

    public String getAuthRoleNameFromSEQ(String authRoleSeq) {

        String retVal = null;
        Enumeration e = authRoleData.keys();

        while(e.hasMoreElements()) {
            String localKey = (String) e.nextElement();
            String seqVal = (String) authRoleData.get(localKey);
            if(seqVal.equals(authRoleSeq)) return localKey;
        }

        return retVal;
    }

    protected static void doUsage() {
        System.out.println("Usage: java -jar pspusers.jar environment [options]");
        System.out.println("Environment: [local|qa1|qa2|qa3|qa4|qa5|dev1|dev2|lt]");
        System.out.println("\nA filename called 'users.csv' is loaded in. Please make sure it is in the running directory.\n");
        System.out.println("Options:");
        System.out.println("\t--help\t\t\tdisplay this information");
        System.out.println("\t--summary\t\tdisplays differences between file and environment.");
        System.out.println("\t--loadUsers\t\tload users list into " +
                "specified environment");
        System.out.println("\t--listUsers\t\tlist users " +
                "for specified in an environment");
        System.out.println("\t--loadFromCSV\t\tUsed to bulk import users into the quickbase.");
        System.out.println("\t--listRoles\t\tList role names of roles loaded in database");
        System.out.println("\t--addUserToQuickbase\t\tSearches and adds a user to the quickbase");
        System.out.println("\t--search\t\tSearch against LDAP for userId of employee");
    }


    public void displayOldUsersSummary() {
        display("\n\n**** Users currently in environment:\n\n");
        for(UserInfo userInfo : usersInDb) {
            display("user:" + userInfo.firstName + " " + userInfo.lastName + ", role:" + userInfo.roleId + ", corpId: " + userInfo.corpId + ", guid:" + userInfo.guid);
        }

        display("\nTotal: " + usersInDb.size());
    }

    public void displayRolesSummary() {
        display("\n\n**** Roles in database:\n\n");

        Enumeration e = authRoleData.keys();

        while(e.hasMoreElements())
            display((String) e.nextElement());


        display("\nTotal: " + authRoleData.size());
    }

    public void displaySummary() {
        display("\n\n**** Summary:\n\n");
        int numberSkipped = 0;
        int numberUpdateRole = 0;
        int numberAddUser = 0;

        display("Users skipped (no changes): ");
        for(UserInfo userInfo : users) {
            if(userInfo.alreadyInDb) {
                if(!userInfo.needsUpdating) {
                    display("user:" + userInfo.firstName + " " + userInfo.lastName + ", role:" + userInfo.roleId + ", corpId: " + userInfo.corpId + ", guid:" + userInfo.guid);
                    numberSkipped++;
                }
            }
        }
        display("Total: " + numberSkipped);


        display("\nUsers with role being updated: ");
        for(UserInfo userInfo : users) {
            if(userInfo.alreadyInDb) {
                if(userInfo.needsUpdating) {
                    display("user:" + userInfo.firstName + " " + userInfo.lastName + ", role:" + userInfo.roleId + " (being updated from '" + getExistingDBRecordForUser(userInfo.corpId).roleId + "'), corpId: " + userInfo.corpId + ", guid:" + userInfo.guid);
                    numberUpdateRole++;
                }
            }
        }
        display("Total: " + numberUpdateRole);


        display("\nNew users: ");
        for(UserInfo userInfo : users) {
            if(!userInfo.alreadyInDb) {
                display("user:" + userInfo.firstName + " " + userInfo.lastName + ", role:" + userInfo.roleId + ", corpId: " + userInfo.corpId + ", guid:" + userInfo.guid);
                numberAddUser++;
            }
        }
        display("Total: " + numberAddUser);
    }





    private String getRandomGuid() {
        return UUID.randomUUID().toString();
    }

    private void log(String output) {
        LoggingUtils.log(output);
    }

    private void display(String output) {
        LoggingUtils.display(output);
    }

    /**
     *
     * @param argName
     * @param args
     * @return
     */
    private static int argPassed(String argName, String [] args) {
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i ++) {
                if (argName.equals(args[i])) {
                    return i;
                }
            }
        }
        return -1;
    }



    //Embedded class userInfo
    class UserInfo {
        String guid;
        String userId;
        String firstName;
        String lastName;
        String corpId;
        String roleId;
        String roleFk;
        boolean alreadyInDb;
        boolean needsUpdating;
        boolean hasMultipleRoles;
    }


}
