package com.intuit.sbd.payroll.psp;

import com.intuit.spc.foundations.portability.collections.SpcfMap;
import com.intuit.spc.foundations.portabilitySpecific.collections.SpcfHashMapImpl;
import com.intuit.spc.foundations.primary.config.ISpcfConfiguration;
import com.intuit.spc.foundations.primary.config.util.SpcfConfigurationProviderAttribute;
import com.intuit.spc.foundations.primarySpecific.config.xml.SpcfXmlConfigurationProvider;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 2/26/12
 * Time: 6:04 PM
 */
public class QueryPlanRunner {
    private static final String SORT_TOTAL_TIME = "total";
    private static final String SORT_QUERY_COST = "cost";
    private static final String SORT_QUERY_COUNT = "count";
    
    private static final String INPUT_FILE_NAME_COMMAND = "-inputFileName";
    private static final String OUTPUT_FILE_NAME_COMMAND = "-outputFileName";
    private static final String ENV_COMMAND = "-env";
    private static final String SORT_COMMAND = "-sort";
    private static final String DB_URL_COMMAND = "-dbUrl";
    private static final String DB_USER_COMMAND = "-dbUser";
    private static final String DB_PASSWORD_COMMAND = "-dbPassword";
    private static final String START_AT_COMMAND = "-startAt";
    private static final String END_AT_COMMAND = "-endAt";

    
    private static String mInputFileName = System.getProperty("user.dir") + File.separatorChar + "hibernate.log";
    private static String mOutputFileName = System.getProperty("user.dir") + File.separatorChar + "queryPlan.log";
    private static String mEnv = "local";
    private static String mSortOrder = SORT_TOTAL_TIME;
    private static String mURL = null;
    private static String mUser = null;
    private static String mPassword = null;
    private static String mStartAt = null;
    private static String mEndAt = null;

    private static PrintWriter mLogFile;
    
    public static String usage() {
        return "QueryPlanRunner [" + INPUT_FILE_NAME_COMMAND + "] " +
                "[" + ENV_COMMAND + "] " +
                "[" + SORT_COMMAND + " (" + SORT_TOTAL_TIME + ", " + SORT_QUERY_COST + ", " + SORT_QUERY_COUNT + ")] " +
                "[" + OUTPUT_FILE_NAME_COMMAND + "] " +
                "[" + DB_URL_COMMAND + "] " +
                "[" + DB_USER_COMMAND + "] " +
                "[" + DB_PASSWORD_COMMAND + "]";
    }

    // PRDQ: -dbUrl=jdbc:oracle:thin:@qysprdpspscl01-scan.ie.intuit.net:1521/pspprod -dbUser=pspread -dbPassword=pspread01
    // LTQ:  -dbUrl=jdbc:oracle:thin://@localhost:9912:pspperf1 -dbUser=pspapp -dbPassword=pspapp01
    public static void main(String[] args) {
        try {
            parseArgs(args);

            System.out.println("Input File: " + mInputFileName);
            System.out.println("Output File: " + mOutputFileName);
            if(mURL == null) {
                System.out.println("Env: " + mEnv);
            } else {
                System.out.println("URL: " + mURL);
                System.out.println("User: " + mUser);
                System.out.println("Password: " + mPassword);
            }
            System.out.println("Sort order: " + mSortOrder);

            try {
                mLogFile = new PrintWriter(new FileWriter(mOutputFileName, false));
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }

            Map<String, Integer> queriesMap = parseInputFile();

            generateQueryPlanOutput(queriesMap);
        } catch (Exception e) {
            System.out.println(usage());
            e.printStackTrace();
            System.exit(-1);
        } finally {
            if(mLogFile != null) {
                mLogFile.close();
            }
        }
    }

    private static void parseArgs(String[] args) {
        for (String arg : args) {
            String[] argParts = arg.split("=");
            if(argParts.length == 2) {
                if(argParts[0].equals(INPUT_FILE_NAME_COMMAND)) {
                    mInputFileName = argParts[1];
                } else if(argParts[0].equals(OUTPUT_FILE_NAME_COMMAND)) {
                    mOutputFileName = argParts[1];
                } else if(argParts[0].equals(ENV_COMMAND)) {
                    mEnv = argParts[1];
                } else if(argParts[0].equals(SORT_COMMAND)) {
                    mSortOrder = argParts[1];
                    if(!mSortOrder.equals(SORT_TOTAL_TIME) &&
                            !mSortOrder.equals(SORT_QUERY_COST) &&
                            !mSortOrder.equals(SORT_QUERY_COUNT)) {
                        throw new RuntimeException("Invalid sort order: " + mSortOrder);
                    }
                } else if(argParts[0].equals(DB_URL_COMMAND)) {
                    mURL = argParts[1];
                } else if(argParts[0].equals(DB_USER_COMMAND)) {
                    mUser = argParts[1];
                } else if(argParts[0].equals(DB_PASSWORD_COMMAND)) {
                    mPassword = argParts[1];
                } else if(argParts[0].equals(START_AT_COMMAND)) {
                    mStartAt = argParts[1];
                } else if(argParts[0].equals(END_AT_COMMAND)) {
                    mEndAt = argParts[1];
                } else {
                    throw new RuntimeException("Invalid command: " + argParts[0]);
                }
            } else {
                throw new RuntimeException("Invalid argument: " + arg);
            }
        }
    }

    private static Map<String, Integer> parseInputFile() throws Exception {
        Map<String, Integer> queriesMap = new HashMap<String, Integer>();

        File f = new File(mInputFileName);
        FileReader fileReader = new FileReader(f);
        BufferedReader input =  new BufferedReader(fileReader);

        String year = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
        StringBuilder query = new StringBuilder();
        String line;
        boolean readingQuery = false;
        boolean finishedReadingQuery = false;
        boolean parseLine = mStartAt == null;
        while (( line = input.readLine()) != null){
            if(mEndAt != null && line.contains(mEndAt)) {
                break;
            }

            if(parseLine) {
                boolean startOfQuery = line.contains("Hibernate:");
                boolean startOfLoggingStatement = line.startsWith(year);

                // Hibernate: always proceeds a query
                // YYYY-.... always proceeds a log statement
                if(!readingQuery && startOfQuery) {
                    readingQuery = true;
                } else if(readingQuery && startOfQuery) {
                    finishedReadingQuery = true;
                } else if(readingQuery && startOfLoggingStatement) {
                    finishedReadingQuery = true;
                    readingQuery = false;
                }

                if(readingQuery && !startOfQuery) {
                    query.append(line).append("\n");
                }

                if(finishedReadingQuery) {
                    addQueryToMap(queriesMap, query);
                    query = new StringBuilder();
                    finishedReadingQuery = false;
                }
            }
            
            if(!parseLine && line.contains(mStartAt)) {
                parseLine = true;
            }
        }

        if(query.length() > 0) {
            addQueryToMap(queriesMap, query);
        }

        logln("Found " + queriesMap.size() + " queries");
        return queriesMap;
    }

    private static void addQueryToMap(Map<String, Integer> pQueriesMap, StringBuilder pQuery) {
        int count = 0;
        String queryString = pQuery.toString();
        if(pQueriesMap.containsKey(queryString)) {
            count = pQueriesMap.get(queryString);
        }
        pQueriesMap.put(queryString, ++count);
    }

    private static void generateQueryPlanOutput(Map<String, Integer> pQueriesMap) {
        try {
            Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;
            List<QueryHolder> queryHolders = new ArrayList<QueryHolder>();
            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");

                SpcfMap<String, Object> properties = new SpcfHashMapImpl<String, Object>();
                properties.add(SpcfConfigurationProviderAttribute.UseClasspath, false);
                properties.add(SpcfConfigurationProviderAttribute.Optional, false);
                properties.add(SpcfConfigurationProviderAttribute.File, System.getProperty("user.dir") + File.separatorChar + "PSE\\Configuration\\" + mEnv + "\\batch-conf\\spcf-dac-conf.xml");
                SpcfXmlConfigurationProvider spcfXmlConfigurationProvider = new SpcfXmlConfigurationProvider();
                spcfXmlConfigurationProvider.init(properties);
                ISpcfConfiguration config = spcfXmlConfigurationProvider.load(null);

                if(mURL == null) {
                    mURL = config.getString("dataAccess.connection.url");
                }

                if(mUser == null) {
                    mUser = config.getString("dataAccess.connection.username");
                }

                if(mPassword == null) {
                    mPassword = config.getString("dataAccess.connection.password");
                }

                connection = DriverManager.getConnection(mURL, mUser, mPassword);

                statement = connection.createStatement();

                int queryCount = pQueriesMap.size();
                int processedCount = 0;
                for (String query : pQueriesMap.keySet()) {
                    StringBuilder queryPlan = null;
                    String queryTime = null;
                    String totalQueryTime = null;
                    try {
                        queryPlan = new StringBuilder();
                        queryTime = "00:00:00";
                        totalQueryTime = "00:00:00";

                        resultSet = statement.executeQuery("EXPLAIN PLAN FOR\n" + query);
                        resultSet = statement.executeQuery("SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY)");

                        int timeIndex = 0;
                        while (resultSet.next()) {
                            String line = resultSet.getString(1);
                            if(line.startsWith("| Id  |")) {
                                String[] lineParts = line.split("\\|");
                                for (int i = 0; i < lineParts.length; i++) {
                                    String linePart = lineParts[i];
                                    if(linePart.contains("Time")) {
                                        timeIndex = i;
                                        break;
                                    }
                                }
                            }else if(line.startsWith("|   0 |")) {
                                String[] lineParts = line.split("\\|");
                                queryTime = lineParts[timeIndex].trim();
                            }
                            queryPlan.append("\t").append(line).append("\n");
                        }

                        totalQueryTime = multiplyQueryTime(queryTime, pQueriesMap.get(query));
                    } catch (Throwable t) {
                        Writer writer = new StringWriter();
                        PrintWriter printWriter = new PrintWriter(writer);
                        t.printStackTrace(printWriter);
                        queryPlan.append(writer.toString());
                    }
                    Integer count = pQueriesMap.get(query);
                    StringBuilder queryText = new StringBuilder();
                    queryText.append("Query:").append("\n");
                    queryText.append("\tCount: ").append(count).append("\n");
                    queryText.append("\tCost Per Query: ").append(queryTime).append("\n");
                    queryText.append("\tTotal time for all queries: ").append(totalQueryTime).append("\n");
                    queryText.append(query).append("\n");
                    queryText.append("Explain Plan:").append("\n");
                    queryText.append(queryPlan.toString()).append("\n");

                    QueryHolder queryHolder = new QueryHolder();
                    queryHolder.queryText = queryText.toString();
                    queryHolder.count = count;
                    queryHolder.queryTime = convertTimeStringToSeconds(queryTime);
                    queryHolder.totalQueryTime = convertTimeStringToSeconds(totalQueryTime);
                    queryHolders.add(queryHolder);

                    processedCount++;
                    if(processedCount % 10 == 0) {
                        System.out.println("Processed " + processedCount + " of " + queryCount + " queries.");
                    }
                }

            } finally {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            }

            if(mSortOrder.equals(SORT_QUERY_COUNT)) {
                Collections.sort(queryHolders, new CountComparator());
            } else if(mSortOrder.equals(SORT_QUERY_COST)) {
                Collections.sort(queryHolders, new QueryCostComparator());
            } else if(mSortOrder.equals(SORT_TOTAL_TIME)) {
                Collections.sort(queryHolders, new TotalTimeComparator());
            }

            for (QueryHolder queryHolder : queryHolders) {
                logln(queryHolder.queryText);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static String multiplyQueryTime(String pQueryTime, Integer pCount) {        
        String totalQueryTime = "00:00:00";
        String[] parts = pQueryTime.split(":");
        if(parts.length == 3) {
            int hours = 0;
            int minutes = 0;

            int seconds = Integer.parseInt(parts[2]) * pCount;
            if(seconds > 59) {
                double secondsIntoMins = seconds/60;
                secondsIntoMins = Math.floor(secondsIntoMins);
                minutes += secondsIntoMins;
                seconds -= secondsIntoMins * 60;
            }

            minutes += Integer.parseInt(parts[1]) * pCount;
            if(minutes > 59) {
                double minsIntoHours = minutes/60;
                minsIntoHours = Math.floor(minsIntoHours);
                hours += minsIntoHours;
                minutes -= minsIntoHours * 60;
            }

            hours += Integer.parseInt(parts[0]) * pCount;
            totalQueryTime = String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
        }
                
        return totalQueryTime;        
    }

    public static void logln(String pMessage) {
        mLogFile.println(pMessage);
        mLogFile.flush();
        //System.out.println(pMessage);
    }
    
    private static long convertTimeStringToSeconds(String pQueryTime) {
        long totalQueryTime = 0;
        String[] parts = pQueryTime.split(":");
        if(parts.length == 3) {
            long hoursToSeconds = Integer.parseInt(parts[0]) * 3600;
            long minutesToSeconds = Integer.parseInt(parts[1]) * 60;
            long seconds = Integer.parseInt(parts[2]);
            totalQueryTime = hoursToSeconds + minutesToSeconds + seconds;
        }

        return totalQueryTime;
    }
    
    
    private static class QueryHolder {
        public String queryText;
        public long totalQueryTime;
        public int count;
        public long queryTime;
    }

    private static class TotalTimeComparator implements Comparator<QueryHolder> {
        public int compare(QueryHolder a, QueryHolder b) {
            if(a == null && b == null) {
                return 0;
            } else if(a == null) {
                return -1;
            } else if(b == null) {
                return 1;
            } else if(a.totalQueryTime == b.totalQueryTime) {
                return 0;
            } else if(a.totalQueryTime < b.totalQueryTime) {
                return 1;
            } else if(a.totalQueryTime > b.totalQueryTime) {
                return -1;
            }

            return 0;
        }
    }

    private static class QueryCostComparator implements Comparator<QueryHolder> {
        public int compare(QueryHolder a, QueryHolder b) {
            if(a == null && b == null) {
                return 0;
            } else if(a == null) {
                return -1;
            } else if(b == null) {
                return 1;
            } else if(a.queryTime == b.queryTime) {
                return 0;
            } else if(a.queryTime < b.queryTime) {
                return 1;
            } else if(a.queryTime > b.queryTime) {
                return -1;
            }

            return 0;
        }
    }

    private static class CountComparator implements Comparator<QueryHolder> {
        public int compare(QueryHolder a, QueryHolder b) {
            if(a == null && b == null) {
                return 0;
            } else if(a == null) {
                return -1;
            } else if(b == null) {
                return 1;
            } else if(a.count == b.count) {
                return 0;
            } else if(a.count < b.count) {
                return 1;
            } else if(a.count > b.count) {
                return -1;
            }

            return 0;
        }
    }
}