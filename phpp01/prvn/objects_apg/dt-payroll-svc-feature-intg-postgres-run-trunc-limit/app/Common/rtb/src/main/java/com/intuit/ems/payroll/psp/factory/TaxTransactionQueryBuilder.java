package com.intuit.ems.payroll.psp.factory;

import java.util.HashMap;
import java.util.Map;

import com.ibm.icu.util.StringTokenizer;
import com.intuit.ems.payroll.psp.model.QueryModel;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * @author rn5
 */
public class TaxTransactionQueryBuilder {
    private static final String FIND_PENDING_2D_TAX_TRANSACTIONS = "findPending2DTaxTransactions";
    private static final String ARG_DELIMITER = "=";
    private static final String FROM_DATE = "fromDate";
    private static final String TO_DATE = "toDate";
    private static final String ARG_ROLLBACK = "rollback";
    private static final SpcfLogger LOGGER = Application.getLogger(TaxTransactionQueryBuilder.class);

    public QueryModel getQueryForTaxProcessor(String[] args) {
        Map<String, String> argMap = extractArguments(args);
        String[] paramNames = new String[]{FROM_DATE, TO_DATE};
        Object[] paramValues = new Object[]{argMap.get(FROM_DATE), argMap.get(TO_DATE)};

        boolean rollback = argMap.containsKey(ARG_ROLLBACK) ? Boolean.valueOf(argMap.get(ARG_ROLLBACK)) : false;
        String query = FIND_PENDING_2D_TAX_TRANSACTIONS;
        LOGGER.info("Input Args. FromDate="+ paramValues[0] + ", ToDate=" + paramValues[1] + ", Rollback=" + rollback);

        return new QueryModel(query, paramNames, paramValues, rollback);
    }

    private Map<String, String> extractArguments(String[] args) {
        Map<String, String> argMap = new HashMap<String, String>();
        for (String arg : args) {
            StringTokenizer stringTokenizer = new StringTokenizer(arg, ARG_DELIMITER);
            argMap.put(stringTokenizer.nextToken().trim(), stringTokenizer.nextToken().trim());
        }
        return argMap;
    }
}
