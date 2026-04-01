package com.intuit.sbd.payroll.psp;

import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: mamin
 * Date: Apr 7, 2009
 * Time: 5:31:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class SQLParser {
    private static final String REC_START = "INSERT INTO TEMP_PSP_AGENCY (";
    private static final String VALUES_START = "VALUES (";
    public Map<String, Map<String,String>> parse(String pAgenciesSql) throws IOException {
        Map<String, Map<String,String>> agencies = new HashMap<String, Map<String,String>>();
        if (pAgenciesSql == null) return agencies;
        BufferedReader reader = new BufferedReader(new FileReader(pAgenciesSql));
        try {
            String sql;
            while ((sql = readRecord(reader)) != null) {
                Map<String, String> agency = parseRecord(sql);
                agencies.put(agency.get("AGENCY_ID"), agency);
            }
        } finally {
            reader.close();
        }
        //
        return agencies;
    }
    private String readRecord(BufferedReader pReader) throws IOException {
        StringBuffer buf = new StringBuffer();
        String line;
        boolean inRec = false;
        while ((line = pReader.readLine()) != null) {
            if (!inRec && !line.startsWith(REC_START)) continue;
            inRec = true;
            buf.append(" " + line);
            if (line.trim().endsWith("/")) break;
        }
        //
        return (buf.length() == 0) ? null : buf.toString().trim();
    }
    private Map<String, String> parseRecord(String rec) {
        String columnStr = rec.substring(REC_START.length());
        columnStr = columnStr.substring(0, columnStr.indexOf(")"));
        String valueStr = rec.substring(rec.indexOf(VALUES_START) + VALUES_START.length());
        valueStr = valueStr.substring(0, valueStr.indexOf(")"));
        StringTokenizer columns = new StringTokenizer(columnStr, ",");
        StringTokenizer values = new StringTokenizer(valueStr, ",");
        Map<String, String> map = new HashMap<String, String>();
        while (columns.hasMoreTokens())  {
            String col = columns.nextToken().trim();
            String val = values.nextToken().trim();
            if (!"null".equalsIgnoreCase(val)) {
                if (val.startsWith("'")) {
                    while (!val.endsWith("'")) {
                        val += " " + values.nextToken().trim();
                    }
                    val = val.substring(1);
                    val = val.substring(0,val.length()-1);
                }
                map.put(col, val);
            }
        }
        return map;
    }
}
