package com.intuit.sbd.payroll.psp.adapters.mobile.utils;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Jeff Jones
 */
public class DemoData {

    private static final SpcfLogger logger = PayrollServices.getLogger(DemoData.class);

    private static RSCompany mRSCompany;

    private static Map<String, RSTransmission> mTransmissionMap;
    private static Map<String, RSPayee> mPayeeMap;
    private static Map<String, RSEmployeeTransaction> mTransactionMap;
    private static Map<String, RSEvent> mEventMap;

    public static RSResponse getAuthResponse() {
        RSResponse rsResponse = new RSResponse();

        rsResponse.setRecentEventCount(0);
        rsResponse.setRecentTransmissionCount(0);
        
        return rsResponse;
    }

    public static RSCompany getCompanyResponse() {
        return mRSCompany;
    }

    public static Collection<RSTransmission> getTransmissions(int pDays) {
        if (pDays == 0)
            return mTransmissionMap.values();

        HashSet<RSTransmission> returnSet = new HashSet<RSTransmission>();
        SpcfCalendar xDaysAgo = PSPDate.getPSPTime();
        xDaysAgo.addDays(pDays * -1);

        for (RSTransmission rsTransmission : mTransmissionMap.values()) {
            SpcfCalendar runDate;
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = inputFormat.parse(rsTransmission.getRunDate());
                runDate = SpcfCalendar.createInstance(date.getTime());
            } catch (Exception e) {
                logger.warn(e);
                return null;
            }

            if (runDate.after(xDaysAgo)) {
                returnSet.add(rsTransmission);
            }
        }

        return returnSet;
    }

    public static RSTransmission getTransmission(String pId) {
        RSTransmission rsTransmission = mTransmissionMap.get(pId);
        rsTransmission.getEmployeeTransactions().clear();

        Iterator<RSPayee> PayeeIterator = mPayeeMap.values().iterator();
        for (RSEmployeeTransaction rsTransaction : mTransactionMap.values()) {
            rsTransaction.setPayee(PayeeIterator.next());
            rsTransmission.getEmployeeTransactions().add(rsTransaction);
        }

        return rsTransmission;
    }

    public static Collection<RSPayee> getPayees() {
        return mPayeeMap.values();
    }

    public static RSPayee getPayee(String pSourceId) {
        return mPayeeMap.get(pSourceId);
    }

    public static Collection<RSEvent> getEvents(int pDays) {
        if (pDays == 0)
            return mEventMap.values();

        HashSet<RSEvent> returnSet = new HashSet<RSEvent>();
        SpcfCalendar xDaysAgo = PSPDate.getPSPTime();
        xDaysAgo.addDays(pDays * -1);

        for (RSEvent rsEvent : mEventMap.values()) {
            SpcfCalendar runDate;
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = inputFormat.parse(rsEvent.getCreatedDate());
                runDate = SpcfCalendar.createInstance(date.getTime());
            } catch (Exception e) {
                logger.warn(e);
                return null;
            }

            if (runDate.after(xDaysAgo)) {
                returnSet.add(rsEvent);
            }
        }

        return returnSet;
    }

    public static RSEvent getEvent(String pId) {
        return mEventMap.get(pId);
    }

    static {
        mRSCompany = getRSCompany();

        mTransmissionMap = new HashMap<String, RSTransmission>();

        RSTransmission rsTransmission = new RSTransmission();
        rsTransmission.setId("417f56e1-313f-4949-b1b6-e20f06baf76e");
        rsTransmission.setAmount(new BigDecimal(1000.00));
        RSFee rsFee = new RSFee();
        rsFee.setFeeType("PerTransactionFee");
        rsFee.setAmount(BigDecimal.valueOf(9.90));
        rsTransmission.setSettlementDate("2010/10/15");
        rsTransmission.setPeriodStart("2010/10/01");
        rsTransmission.setPeriodEnd("2010/10/15");
        rsTransmission.setRunDate("2010/10/12 10:11:43.0");
        rsTransmission.setTransmissionStatus(RSTransmissionStatusCode.Complete);
        rsTransmission.setTransmissionType(RSTransmissionTypeCode.DD);
        mTransmissionMap.put(rsTransmission.getId(), rsTransmission);

        rsTransmission = new RSTransmission();
        rsTransmission.setId("3a624e75-6cf2-40b6-9179-6fae35c8e0ef");
        rsTransmission.setAmount(new BigDecimal(1000.00));
        rsTransmission.setSettlementDate("2010/10/31");
        rsTransmission.setPeriodStart("2010/10/16");
        rsTransmission.setPeriodEnd("2010/10/31");
        rsTransmission.setRunDate("2010/10/28 10:33:44.0");
        rsTransmission.setTransmissionStatus(RSTransmissionStatusCode.Complete);
        rsTransmission.setTransmissionType(RSTransmissionTypeCode.DD);
        mTransmissionMap.put(rsTransmission.getId(), rsTransmission);

        rsTransmission = new RSTransmission();
        rsTransmission.setId("14f2c82c-5d4f-4a47-90ca-251a3eb71a28");
        rsTransmission.setAmount(new BigDecimal(1000.00));
        rsTransmission.setSettlementDate("2010/11/15");
        rsTransmission.setPeriodStart("2010/11/01");
        rsTransmission.setPeriodEnd("2010/11/15");
        rsTransmission.setRunDate("2010/11/12 12:11:35.0");
        rsTransmission.setTransmissionStatus(RSTransmissionStatusCode.Complete);
        rsTransmission.setTransmissionType(RSTransmissionTypeCode.DD);
        mTransmissionMap.put(rsTransmission.getId(), rsTransmission);

        rsTransmission = new RSTransmission();
        rsTransmission.setId("1adf44f5-cfda-4b06-8946-108c30f9149d");
        rsTransmission.setAmount(new BigDecimal(1000.00));
        rsTransmission.setSettlementDate("2010/11/30");
        rsTransmission.setPeriodStart("2010/11/16");
        rsTransmission.setPeriodEnd("2010/11/30");
        rsTransmission.setRunDate("2010/11/28 15:45:12.0");
        rsTransmission.setTransmissionStatus(RSTransmissionStatusCode.Complete);
        rsTransmission.setTransmissionType(RSTransmissionTypeCode.DD);
        mTransmissionMap.put(rsTransmission.getId(), rsTransmission);

        rsTransmission = new RSTransmission();
        rsTransmission.setId("03b0cb26-41a3-4165-aa1c-a9f662bf48ea");
        rsTransmission.setAmount(new BigDecimal(1000.00));
        rsTransmission.setSettlementDate("2010/12/15");
        rsTransmission.setPeriodStart("2010/12/01");
        rsTransmission.setPeriodEnd("2010/12/15");
        rsTransmission.setRunDate("2010/12/12 16:55:21.0");
        rsTransmission.setTransmissionStatus(RSTransmissionStatusCode.Complete);
        rsTransmission.setTransmissionType(RSTransmissionTypeCode.DD);
        mTransmissionMap.put(rsTransmission.getId(), rsTransmission);

        rsTransmission = new RSTransmission();
        rsTransmission.setId("6c3c6d86-9cee-4647-9f68-8966fc557479");
        rsTransmission.setAmount(new BigDecimal(1000.00));
        rsTransmission.setSettlementDate("2010/12/31");
        rsTransmission.setPeriodStart("2010/12/16");
        rsTransmission.setPeriodEnd("2010/12/31");
        rsTransmission.setRunDate("2010/12/27 09:11:32.0");
        rsTransmission.setTransmissionStatus(RSTransmissionStatusCode.Complete);
        rsTransmission.setTransmissionType(RSTransmissionTypeCode.DD);
        mTransmissionMap.put(rsTransmission.getId(), rsTransmission);

        rsTransmission = new RSTransmission();
        rsTransmission.setId("79ab141d-a685-435a-906d-ebbca522ad91");
        rsTransmission.setAmount(new BigDecimal(1000.00));
        rsTransmission.setSettlementDate("2011/01/15");
        rsTransmission.setPeriodStart("2011/01/01");
        rsTransmission.setPeriodEnd("2011/01/15");
        rsTransmission.setRunDate("2011/01/12 11:15:56.0");
        rsTransmission.setTransmissionStatus(RSTransmissionStatusCode.Complete);
        rsTransmission.setTransmissionType(RSTransmissionTypeCode.DD);
        mTransmissionMap.put(rsTransmission.getId(), rsTransmission);

        rsTransmission = new RSTransmission();
        rsTransmission.setId("062ea6a2-5160-4ba2-a699-2bbad82ebd21");
        rsTransmission.setAmount(new BigDecimal(1000.00));
        rsTransmission.setSettlementDate("2011/01/31");
        rsTransmission.setPeriodStart("2011/01/16");
        rsTransmission.setPeriodEnd("2011/01/31");
        rsTransmission.setRunDate("2011/01/28 14:25:14.0");
        rsTransmission.setTransmissionStatus(RSTransmissionStatusCode.Pending);
        rsTransmission.setTransmissionType(RSTransmissionTypeCode.DD);
        mTransmissionMap.put(rsTransmission.getId(), rsTransmission);

        mPayeeMap = new HashMap<String, RSPayee>();

        RSPayee rsPayee = new RSPayee();
        rsPayee.setId("25f28632-8144-4260-aa32-ee77702ee4ba");
        rsPayee.setFirstName("Mary");
        rsPayee.setLastName("Smith");
        rsPayee.setGender(RSGenderCode.Female);
        rsPayee.setBirthDate("1970/01/01");
        rsPayee.setHireDate("2000/01/01");
        rsPayee.setEmail("demo1@intuit.com");
        rsPayee.setPhone("555-555-5555");
        rsPayee.setStatus(RSPayeeStatusCode.Active);
        rsPayee.setType(RSPayeeType.Employee);
        mPayeeMap.put(rsPayee.getId(), rsPayee);

        rsPayee = new RSPayee();
        rsPayee.setId("824ffbb5-a24f-4b29-a070-d00ee481d3e0");
        rsPayee.setFirstName("Paul");
        rsPayee.setLastName("Smith");
        rsPayee.setGender(RSGenderCode.Male);
        rsPayee.setBirthDate("1970/01/01");
        rsPayee.setHireDate("2000/01/01");
        rsPayee.setEmail("demo2@intuit.com");
        rsPayee.setPhone("111-111-11111");
        rsPayee.setStatus(RSPayeeStatusCode.Active);
        rsPayee.setType(RSPayeeType.Employee);
        mPayeeMap.put(rsPayee.getId(), rsPayee);

        rsPayee = new RSPayee();
        rsPayee.setId("b1dcc500-0399-498a-9c45-4fa4ab847962");
        rsPayee.setFirstName("Jane");
        rsPayee.setLastName("Doe");
        rsPayee.setGender(RSGenderCode.Female);
        rsPayee.setBirthDate("1970/01/01");
        rsPayee.setHireDate("2000/01/01");
        rsPayee.setEmail("demo3@intuit.com");
        rsPayee.setPhone("555-555-5555");
        rsPayee.setStatus(RSPayeeStatusCode.Active);
        rsPayee.setType(RSPayeeType.Employee);
        mPayeeMap.put(rsPayee.getId(), rsPayee);

        rsPayee = new RSPayee();
        rsPayee.setId("ab60ad42-3060-4bb2-8a1b-21e57e646159");
        rsPayee.setFirstName("John");
        rsPayee.setLastName("Doe");
        rsPayee.setGender(RSGenderCode.Male);
        rsPayee.setBirthDate("1970/01/01");
        rsPayee.setHireDate("2000/01/01");
        rsPayee.setEmail("demo4@intuit.com");
        rsPayee.setPhone("555-555-5555");
        rsPayee.setStatus(RSPayeeStatusCode.Active);
        rsPayee.setType(RSPayeeType.Employee);
        mPayeeMap.put(rsPayee.getId(), rsPayee);

        rsPayee = new RSPayee();
        rsPayee.setId("43953537-e66f-4103-93f3-e6fa521031b5");
        rsPayee.setFirstName("Melissa");
        rsPayee.setLastName("Cooper");
        rsPayee.setGender(RSGenderCode.Female);
        rsPayee.setBirthDate("1970/01/01");
        rsPayee.setHireDate("2000/01/01");
        rsPayee.setEmail("demo5@intuit.com");
        rsPayee.setPhone("555-555-5555");
        rsPayee.setStatus(RSPayeeStatusCode.Active);
        rsPayee.setType(RSPayeeType.Employee);
        mPayeeMap.put(rsPayee.getId(), rsPayee);

        rsPayee = new RSPayee();
        rsPayee.setId("52bc48ad-d189-4cef-9636-9e6a3686e345");
        rsPayee.setFirstName("Andy");
        rsPayee.setLastName("Cooper");
        rsPayee.setGender(RSGenderCode.Male);
        rsPayee.setBirthDate("1970/01/01");
        rsPayee.setHireDate("2000/01/01");
        rsPayee.setEmail("demo6@intuit.com");
        rsPayee.setPhone("555-555-5555");
        rsPayee.setStatus(RSPayeeStatusCode.Active);
        rsPayee.setType(RSPayeeType.Employee);
        mPayeeMap.put(rsPayee.getId(), rsPayee);

        rsPayee = new RSPayee();
        rsPayee.setId("0674ecac-6ad2-4b91-a4c0-149bfc4c28f2");
        rsPayee.setFirstName("Kim");
        rsPayee.setLastName("Gates");
        rsPayee.setGender(RSGenderCode.Female);
        rsPayee.setBirthDate("1970/01/01");
        rsPayee.setHireDate("2000/01/01");
        rsPayee.setEmail("demo7@intuit.com");
        rsPayee.setPhone("555-555-5555");
        rsPayee.setStatus(RSPayeeStatusCode.Active);
        rsPayee.setType(RSPayeeType.Employee);
        mPayeeMap.put(rsPayee.getId(), rsPayee);

        rsPayee = new RSPayee();
        rsPayee.setId("eaabc3d7-1cf8-441c-82c0-e67f0ffe9a8a");
        rsPayee.setFirstName("Brad");
        rsPayee.setLastName("Gates");
        rsPayee.setGender(RSGenderCode.Male);
        rsPayee.setBirthDate("1970/01/01");
        rsPayee.setHireDate("2000/01/01");
        rsPayee.setEmail("demo8@intuit.com");
        rsPayee.setPhone("555-555-5555");
        rsPayee.setStatus(RSPayeeStatusCode.Active);
        rsPayee.setType(RSPayeeType.Employee);
        mPayeeMap.put(rsPayee.getId(), rsPayee);

        rsPayee = new RSPayee();
        rsPayee.setId("7366e777-f87d-4340-9562-0bbd3d100c5f");
        rsPayee.setFirstName("Karyn");
        rsPayee.setLastName("Cook");
        rsPayee.setGender(RSGenderCode.Female);
        rsPayee.setBirthDate("1970/01/01");
        rsPayee.setHireDate("2000/01/01");
        rsPayee.setEmail("demo9@intuit.com");
        rsPayee.setPhone("555-555-5555");
        rsPayee.setStatus(RSPayeeStatusCode.Active);
        rsPayee.setType(RSPayeeType.Employee);
        mPayeeMap.put(rsPayee.getId(), rsPayee);

        rsPayee = new RSPayee();
        rsPayee.setId("d3a44f62-06fb-46a6-a976-88819b37b479");
        rsPayee.setFirstName("Mark");
        rsPayee.setLastName("Cook");
        rsPayee.setGender(RSGenderCode.Male);
        rsPayee.setBirthDate("1970/01/01");
        rsPayee.setHireDate("2000/01/01");
        rsPayee.setEmail("demo10@intuit.com");
        rsPayee.setPhone("555-555-5555");
        rsPayee.setStatus(RSPayeeStatusCode.Active);
        rsPayee.setType(RSPayeeType.Employee);
        mPayeeMap.put(rsPayee.getId(), rsPayee);

        rsPayee = new RSPayee();
        rsPayee.setId("d3a44f62-06fb-46a6-a976-88819b37b478");
        rsPayee.setFirstName("Cindy");
        rsPayee.setLastName("Jones");
        rsPayee.setGender(RSGenderCode.Female);
        rsPayee.setBirthDate("1970/01/01");
        rsPayee.setHireDate("2000/01/01");
        rsPayee.setEmail("demo10@intuit.com");
        rsPayee.setPhone("555-555-5555");
        rsPayee.setStatus(RSPayeeStatusCode.Inactive);
        rsPayee.setType(RSPayeeType.Employee);
        mPayeeMap.put(rsPayee.getId(), rsPayee);

        rsPayee = new RSPayee();
        rsPayee.setId("d3a44f62-06fb-46a6-a976-88819b37b411");
        rsPayee.setName("Caleb Smith");
        rsPayee.setIs1099(true);
        rsPayee.setEmail("demo11@intuit.com");
        rsPayee.setPhone("555-555-5555");
        rsPayee.setTaxId("000000000");
        rsPayee.setType(RSPayeeType.Vendor);
        mPayeeMap.put(rsPayee.getId(), rsPayee);

        RSBankAccount checking = new RSBankAccount();
        checking.setId("");
        checking.setBankName("Bank of Intuit");
        checking.setAccountNumber("*************1234");
        checking.setRoutingNumber("*****4321");
        checking.setType(RSBankAccountTypeCode.Checking);

        RSBankAccount savings = new RSBankAccount();
        savings.setId("");
        savings.setBankName("Bank of Intuit");
        savings.setAccountNumber("*************7890");
        savings.setRoutingNumber("*****0987");
        savings.setType(RSBankAccountTypeCode.Savings);

        mTransactionMap = new HashMap<String, RSEmployeeTransaction>();
        RSEmployeeTransaction rsTransaction = new RSEmployeeTransaction();
        rsTransaction.setId("fd41c5ee-c090-4d33-acb5-aea261c359a3");
        rsTransaction.setNetAmount(BigDecimal.valueOf(1056.32));
        rsTransaction.setTransactionStatus(RSTransactionStatusCode.Active);
        RSTransactionSplit rsTransactionSplit = new RSTransactionSplit();
        rsTransactionSplit.setAmount(BigDecimal.valueOf(1006.32));
        rsTransactionSplit.setBankAccount(checking);
        rsTransaction.getTransactionSplits().add(rsTransactionSplit);
        rsTransactionSplit = new RSTransactionSplit();
        rsTransactionSplit.setAmount(BigDecimal.valueOf(50.00));
        rsTransactionSplit.setBankAccount(savings);
        rsTransaction.getTransactionSplits().add(rsTransactionSplit);
        mTransactionMap.put(rsTransaction.getId(), rsTransaction);

        rsTransaction = new RSEmployeeTransaction();
        rsTransaction.setId("80823e43-e55d-410d-94d1-971b9fe09542");
        rsTransaction.setNetAmount(BigDecimal.valueOf(2367.12));
        rsTransaction.setTransactionStatus(RSTransactionStatusCode.Active);
        rsTransactionSplit = new RSTransactionSplit();
        rsTransactionSplit.setAmount(BigDecimal.valueOf(2267.12));
        rsTransactionSplit.setBankAccount(checking);
        rsTransaction.getTransactionSplits().add(rsTransactionSplit);
        rsTransactionSplit = new RSTransactionSplit();
        rsTransactionSplit.setAmount(BigDecimal.valueOf(100.00));
        rsTransactionSplit.setBankAccount(savings);
        rsTransaction.getTransactionSplits().add(rsTransactionSplit);
        mTransactionMap.put(rsTransaction.getId(), rsTransaction);

        rsTransaction = new RSEmployeeTransaction();
        rsTransaction.setId("e33f82b0-d037-4e2f-83ec-2a0e92e5a2a3");
        rsTransaction.setNetAmount(BigDecimal.valueOf(1859.33));
        rsTransaction.setTransactionStatus(RSTransactionStatusCode.Active);
        rsTransactionSplit = new RSTransactionSplit();
        rsTransactionSplit.setAmount(BigDecimal.valueOf(1824.33));
        rsTransactionSplit.setBankAccount(checking);
        rsTransaction.getTransactionSplits().add(rsTransactionSplit);
        rsTransactionSplit = new RSTransactionSplit();
        rsTransactionSplit.setAmount(BigDecimal.valueOf(25.00));
        rsTransactionSplit.setBankAccount(savings);
        rsTransaction.getTransactionSplits().add(rsTransactionSplit);
        mTransactionMap.put(rsTransaction.getId(), rsTransaction);

        rsTransaction = new RSEmployeeTransaction();
        rsTransaction.setId("efde9bcb-a7c3-489d-816b-35964b701f01");
        rsTransaction.setNetAmount(BigDecimal.valueOf(563.87));
        rsTransaction.setTransactionStatus(RSTransactionStatusCode.Active);
        rsTransactionSplit = new RSTransactionSplit();
        rsTransactionSplit.setAmount(BigDecimal.valueOf(513.87));
        rsTransactionSplit.setBankAccount(checking);
        rsTransaction.getTransactionSplits().add(rsTransactionSplit);
        rsTransactionSplit = new RSTransactionSplit();
        rsTransactionSplit.setAmount(BigDecimal.valueOf(50.00));
        rsTransactionSplit.setBankAccount(savings);
        rsTransaction.getTransactionSplits().add(rsTransactionSplit);
        mTransactionMap.put(rsTransaction.getId(), rsTransaction);

        rsTransaction = new RSEmployeeTransaction();
        rsTransaction.setId("a53b3302-d4f6-4566-9e80-00ff63fd67bc");
        rsTransaction.setNetAmount(BigDecimal.valueOf(1429.84));
        rsTransaction.setTransactionStatus(RSTransactionStatusCode.Active);
        rsTransactionSplit = new RSTransactionSplit();
        rsTransactionSplit.setAmount(BigDecimal.valueOf(1419.84));
        rsTransactionSplit.setBankAccount(checking);
        rsTransaction.getTransactionSplits().add(rsTransactionSplit);
        rsTransactionSplit = new RSTransactionSplit();
        rsTransactionSplit.setAmount(BigDecimal.valueOf(10.00));
        rsTransactionSplit.setBankAccount(savings);
        rsTransaction.getTransactionSplits().add(rsTransactionSplit);
        mTransactionMap.put(rsTransaction.getId(), rsTransaction);

        rsTransaction = new RSEmployeeTransaction();
        rsTransaction.setId("e07d298b-8731-4bcd-bc9c-2210d3a7cd08");
        rsTransaction.setNetAmount(BigDecimal.valueOf(2830.05));
        rsTransaction.setTransactionStatus(RSTransactionStatusCode.Active);
        rsTransactionSplit = new RSTransactionSplit();
        rsTransactionSplit.setAmount(BigDecimal.valueOf(2780.05));
        rsTransactionSplit.setBankAccount(checking);
        rsTransaction.getTransactionSplits().add(rsTransactionSplit);
        rsTransactionSplit = new RSTransactionSplit();
        rsTransactionSplit.setAmount(BigDecimal.valueOf(50.00));
        rsTransactionSplit.setBankAccount(savings);
        rsTransaction.getTransactionSplits().add(rsTransactionSplit);
        mTransactionMap.put(rsTransaction.getId(), rsTransaction);

        rsTransaction = new RSEmployeeTransaction();
        rsTransaction.setId("dc52cbb8-5718-47f7-9b91-46fbf1e126ec");
        rsTransaction.setNetAmount(BigDecimal.valueOf(1113.59));
        rsTransaction.setTransactionStatus(RSTransactionStatusCode.Active);
        rsTransactionSplit = new RSTransactionSplit();
        rsTransactionSplit.setAmount(BigDecimal.valueOf(1088.59));
        rsTransactionSplit.setBankAccount(checking);
        rsTransaction.getTransactionSplits().add(rsTransactionSplit);
        rsTransactionSplit = new RSTransactionSplit();
        rsTransactionSplit.setAmount(BigDecimal.valueOf(25.00));
        rsTransactionSplit.setBankAccount(savings);
        rsTransaction.getTransactionSplits().add(rsTransactionSplit);
        mTransactionMap.put(rsTransaction.getId(), rsTransaction);

        rsTransaction = new RSEmployeeTransaction();
        rsTransaction.setId("71cfbafa-1288-48c9-959d-0b4a485d645d");
        rsTransaction.setNetAmount(BigDecimal.valueOf(2649.01));
        rsTransaction.setTransactionStatus(RSTransactionStatusCode.Active);
        rsTransactionSplit = new RSTransactionSplit();
        rsTransactionSplit.setAmount(BigDecimal.valueOf(2549.01));
        rsTransactionSplit.setBankAccount(checking);
        rsTransaction.getTransactionSplits().add(rsTransactionSplit);
        rsTransactionSplit = new RSTransactionSplit();
        rsTransactionSplit.setAmount(BigDecimal.valueOf(100.00));
        rsTransactionSplit.setBankAccount(savings);
        rsTransaction.getTransactionSplits().add(rsTransactionSplit);
        mTransactionMap.put(rsTransaction.getId(), rsTransaction);

        rsTransaction = new RSEmployeeTransaction();
        rsTransaction.setId("a1e3039a-364c-4052-9e57-78f45e69acdd");
        rsTransaction.setNetAmount(BigDecimal.valueOf(3895.48));
        rsTransaction.setTransactionStatus(RSTransactionStatusCode.Active);
        rsTransactionSplit = new RSTransactionSplit();
        rsTransactionSplit.setAmount(BigDecimal.valueOf(3845.48));
        rsTransactionSplit.setBankAccount(checking);
        rsTransaction.getTransactionSplits().add(rsTransactionSplit);
        rsTransactionSplit = new RSTransactionSplit();
        rsTransactionSplit.setAmount(BigDecimal.valueOf(50.00));
        rsTransactionSplit.setBankAccount(savings);
        rsTransaction.getTransactionSplits().add(rsTransactionSplit);
        mTransactionMap.put(rsTransaction.getId(), rsTransaction);

        rsTransaction = new RSEmployeeTransaction();
        rsTransaction.setId("1e8ee942-96ba-4b5c-b80e-8e547e7f31de");
        rsTransaction.setNetAmount(BigDecimal.valueOf(4388.54));
        rsTransaction.setTransactionStatus(RSTransactionStatusCode.Active);
        rsTransactionSplit = new RSTransactionSplit();
        rsTransactionSplit.setAmount(BigDecimal.valueOf(4338.54));
        rsTransactionSplit.setBankAccount(checking);
        rsTransaction.getTransactionSplits().add(rsTransactionSplit);
        rsTransactionSplit = new RSTransactionSplit();
        rsTransactionSplit.setAmount(BigDecimal.valueOf(50.00));
        rsTransactionSplit.setBankAccount(savings);
        rsTransaction.getTransactionSplits().add(rsTransactionSplit);
        mTransactionMap.put(rsTransaction.getId(), rsTransaction);

        mEventMap = new HashMap<String, RSEvent>();

        RSEvent rsEvent = new RSEvent();
        rsEvent.setId("aabc152f-63e9-4af8-a411-220e026064ab");
        rsEvent.setCreatedDate("2011/01/31");
        //rsEvent.setEventType(RSEventTypeCode.NoticeOfChange);
        rsEvent.setDescription("A Notice of Change (NOC) is a payroll return that Intuit receives from employees' financial institutions when the banking information for their direct deposit is incorrect. This means that the employee receives the money for their paycheck, but Intuit is given a warning stating that the employee information needs to be updated.");
        rsEvent.setStatus("Open");
        rsEvent.setLinkType(RSLinkType.EmployeeTransaction);
        rsEvent.getLinkIdList().add("1e8ee942-96ba-4b5c-b80e-8e547e7f31de");
        rsEvent.setKbURL("http://payroll.intuit.com/support/kb/1000490.html");

        mEventMap.put(rsEvent.getId(), rsEvent);
    }

    private static RSCompany getRSCompany() {
        RSCompany rsCompany = new RSCompany();
        rsCompany.setEin("123456789");
        rsCompany.setPsid("100000000");
        rsCompany.setLegalName("Acme Software");
        rsCompany.setDba("Acme Systems");
        rsCompany.setSubscriptionNumber("12345");


        RSBankAccount rsBankAccount = new RSBankAccount();
        rsBankAccount.setId("5df3ac77-3e66-44b4-8751-e24abeb85ea3");
        rsBankAccount.setAccountNumber("****5382");
        rsBankAccount.setBankName("Bank of Intuit");
        rsBankAccount.setRoutingNumber("*****3895");
        rsBankAccount.setType(RSBankAccountTypeCode.Checking);
        rsCompany.setBankAccount(rsBankAccount);

        RSService rsService = new RSService();
        rsService.setServiceCd("Direct Deposit");
        //rsService.setStatusCd(RSServiceStatusCode.PendingActivation);
        rsCompany.getServices().add(rsService);

        rsService = new RSService();
        rsService.setServiceCd("Bill Payment");
        //rsService.setStatusCd(RSServiceStatusCode.Active);
        rsCompany.getServices().add(rsService);

        rsService = new RSService();
        rsService.setServiceCd("Tax");
        //rsService.setStatusCd(RSServiceStatusCode.Cancelled);
        rsCompany.getServices().add(rsService);

        rsService = new RSService();
        rsService.setServiceCd("401k");
        //rsService.setStatusCd(RSServiceStatusCode.Active);
        rsCompany.getServices().add(rsService);

        RSContact rsContact = new RSContact();
        rsContact.setFirstName("Jane");
        rsContact.setLastName("Doe");
        rsContact.setEmail("test1@intuit.com");
        rsContact.setPrimaryPhone("555-555-5555");
        //rsContact.setContactType(RSContactTypeCode.PrimaryPrincipal);
        rsCompany.getContacts().add(rsContact);

        rsContact = new RSContact();
        rsContact.setFirstName("John");
        rsContact.setLastName("Doe");
        rsContact.setEmail("test2@intuit.com");
        rsContact.setPrimaryPhone("666-666-6666");
        //rsContact.setContactType(RSContactTypeCode.SecondaryPrincipal);
        rsCompany.getContacts().add(rsContact);

        rsContact = new RSContact();
        rsContact.setFirstName("Jane");
        rsContact.setLastName("Smith");
        rsContact.setEmail("test3@intuit.com");
        rsContact.setPrimaryPhone("777-777-7777");
        //rsContact.setContactType(RSContactTypeCode.PayrollAdmin);
        rsCompany.getContacts().add(rsContact);

        rsContact = new RSContact();
        rsContact.setFirstName("John");
        rsContact.setLastName("Smith");
        rsContact.setEmail("test4@intuit.com");
        rsContact.setPrimaryPhone("888-888-8888");
        //rsContact.setContactType(RSContactTypeCode.Other);
        rsCompany.getContacts().add(rsContact);

        RSAddress rsAddress = new RSAddress();
        rsAddress.setAddressLine1("123 Main Street");
        rsAddress.setAddressLine2("Suite A");
        rsAddress.setCity("Reno");
        rsAddress.setState("NV");
        rsAddress.setZip("89511");
        rsAddress.setAddressType(RSAddressTypeCode.Mailing);
        rsCompany.getAddresses().add(rsAddress);

        rsAddress = new RSAddress();
        rsAddress.setAddressLine1("123 Main Street");
        rsAddress.setAddressLine2("Suite A");
        rsAddress.setCity("Reno");
        rsAddress.setState("NV");
        rsAddress.setZip("89511");
        rsAddress.setAddressType(RSAddressTypeCode.Legal);
        rsCompany.getAddresses().add(rsAddress);

        return rsCompany;
    }
}
