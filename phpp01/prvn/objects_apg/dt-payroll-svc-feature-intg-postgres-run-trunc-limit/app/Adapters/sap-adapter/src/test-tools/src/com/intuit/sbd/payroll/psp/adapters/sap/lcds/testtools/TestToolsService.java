package com.intuit.sbd.payroll.psp.adapters.sap.lcds.testtools;


import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.testtools.TestToolsAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.testtools.TTExceptionFactory;
import com.intuit.sbd.payroll.psp.adapters.sap.lcds.DataServiceExceptionFactory;
import com.intuit.sbd.payroll.psp.adapters.sap.lcds.proxy.PSPEntityProxy;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.*;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.testtools.*;

import java.util.Date;
import java.util.ArrayList;

import flex.messaging.MessageException;
import flex.messaging.io.PropertyProxyRegistry;

/**
 * BankReturnRemoteService - LDCS Flex remote service for access to bank return info.
 *
 * @author Joe Warmelink
 */
public class TestToolsService {
    private static final SpcfLogger logger = PayrollServices.getLogger(TestToolsService.class);
    private static final TTExceptionFactory tteFactory = new TTExceptionFactory(logger);
    private static final String CLASS_NAME = TestToolsService.class.getName();

    public TestToolsService() {
        registerProxies();
    }

    /* Insert all beans into here to convert enums to strings */
    private void registerProxies() {
        PSPEntityProxy entityProxy = new PSPEntityProxy();

        PropertyProxyRegistry.getRegistry().register(TTOffloadGroup.class, entityProxy);
    }

    public void setPSPDate(Date newDate) throws MessageException {
        try{
             TestToolsAdapter testToolsAdapter = new TestToolsAdapter();
             testToolsAdapter.setPSPDate(newDate);
        } catch (Exception ex) {
            tteFactory.rethrowException(CLASS_NAME,
                    "setPSPDate",
                    ex);
        }
    }

    public ArrayList<TTOffloadGroup> findOffloadGroups() throws MessageException {
        try {
            TestToolsAdapter testToolsAdapter = new TestToolsAdapter();
            return testToolsAdapter.findOffloadGroups();
        } catch (Exception ex) {
            tteFactory.rethrowException(CLASS_NAME,
                    "findOffloadGroups",
                    ex);
            return null;
        }
    }

    public ArrayList<TTOffloadBatch> findOffloadBatches() throws MessageException {
        try {
            TestToolsAdapter testToolsAdapter = new TestToolsAdapter();
            return testToolsAdapter.findOffloadBatches();
        } catch (Exception ex) {
            tteFactory.rethrowException(CLASS_NAME,
                    "findOffloadBatches",
                    ex);
            return null;
        }
    }

    public void saveOffloadGroup(String groupCode,
                                 String groupName,
                                 String groupDescription,
                                 String cutoffTime) throws MessageException {
        try {
            TestToolsAdapter testToolsAdapter = new TestToolsAdapter();
            testToolsAdapter.saveOffloadGroup(groupCode,
                    groupName, groupDescription, cutoffTime);
        } catch (Exception ex) {
            tteFactory.rethrowException(CLASS_NAME,
                    "saveOffloadGroup",
                    ex);
        }
    }

    public void addOffloadGroup(String groupCode,
                                String groupName,
                                String groupDescription,
                                String cutoffTime) throws MessageException {
        try {
            TestToolsAdapter testToolsAdapter = new TestToolsAdapter();
            testToolsAdapter.addOffloadGroup(groupCode,
                    groupName, groupDescription, cutoffTime);
        } catch (Exception ex) {
            tteFactory.rethrowException(CLASS_NAME,
                    "addOffloadGroup",
                    ex);
        }
    }

    public void generateNACHAFiles(String offloadGroup) throws MessageException {
        try {
            TestToolsAdapter testToolsAdapter = new TestToolsAdapter();
            testToolsAdapter.generateNACHAFiles(offloadGroup);
        } catch (Exception ex) {
            tteFactory.rethrowException(CLASS_NAME,
                    "generateNACHAFiles",
                    ex);
        }
    }

    public void changeCompanyOffloadGroup(String sourceSystemCd, String companyId, String offloadGrpCd) throws MessageException {
        try {
            TestToolsAdapter testToolsAdapter = new TestToolsAdapter();
            testToolsAdapter.changeCompanyOffloadGroup(sourceSystemCd, companyId, offloadGrpCd);
        } catch (Exception ex) {
            tteFactory.rethrowException(CLASS_NAME, "changeCompanyOffloadGroup", ex);
        }
    }

    public ArrayList<TTEntryDetailRecord> getEntryDetailRecords(Date fromDate,
                                                                Date toDate,
                                                                String offloadGroupCd,
                                                                int firstResult,
                                                                int maxResults) throws MessageException {
        try {
            TestToolsAdapter testToolsAdapter = new TestToolsAdapter();
            return testToolsAdapter.getEntryDetailRecords(fromDate,
                                                    toDate,
                                                    offloadGroupCd,
                                                    firstResult,
                                                    maxResults);
        } catch (Exception ex) {
            tteFactory.rethrowException(CLASS_NAME, "changeCompanyOffloadGroup", ex);
        }
        return null;
    }

    public void createBankReturnsForMoneyMovementTransactions(ArrayList<TTBankReturn> bankReturns) throws MessageException {
        try {
            TestToolsAdapter testToolsAdapter = new TestToolsAdapter();
            testToolsAdapter.createBankReturnsForMoneyMovementTransactions(bankReturns);
        } catch (Exception ex) {
            tteFactory.rethrowException(CLASS_NAME, "createBankReturnsForMoneyMovementTransactions", ex);
        }
    }
}
