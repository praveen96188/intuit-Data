import static junit.framework.Assert.*;
import static junit.framework.Assert.assertEquals;

import org.junit.Test;
import tool.TaxCreditsEmail;
import tool.TaxCreditsEmailCollection;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * User: dweinberg
 * Date: Apr 21, 2010
 * Time: 10:50:52 AM
 */
public class EmailCollectionTests {

    @Test
    public void testLoadFile() throws Exception {

        File testFile = new File("D:\\dev\\PSP\\main\\Tools\\Tax Credits Email Tool\\test", "TestCustomerList.csv");

        TaxCreditsEmailCollection collection = new TaxCreditsEmailCollection(testFile);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
        Date runDate = sdf.parse("04/20/2010");
        
        collection.loadFromFile(runDate);

        assertEquals(3132, collection.getEmails().size());

        TaxCreditsEmail firstEmail = collection.getEmails().get(0);

        assertEquals(firstEmail.getEmailAddress(), "DAVID_WEINBERG@INTUIT.COM");
        assertEquals(firstEmail.getName(), "MICHELLE WARD");
        assertNull(firstEmail.getInitialEmailSent());
        assertFalse(firstEmail.isReminderEmailSent());
        assertMDYEquals(3, 7, 2010, firstEmail.getHireDate());
        //test logic
        assertEquals(13, firstEmail.getDaysSinceHire());
        assertMDYEquals(3, 30, 2010, firstEmail.getPostmarkDate());
        assertMDYEquals(3, 25, 2010, firstEmail.getReminderDate());
        assertTrue(firstEmail.isWillSendInitialEmail());
        assertFalse(firstEmail.isWillSendReminderEmail());
        assertFalse(firstEmail.isWillNeverSendReminderEmail());
        

        TaxCreditsEmail lastEmail = collection.getEmails().get(collection.getEmails().size() -1);

        assertEquals(lastEmail.getEmailAddress(), "DAVID_WEINBERG@INTUIT.COM");
        assertEquals(lastEmail.getName(), "C ZHELLYDHAY FLOOD");
        assertNotNull(lastEmail.getInitialEmailSent());
        assertTrue(lastEmail.isReminderEmailSent());

        TaxCreditsEmail remindTodayEmail = collection.getEmails().get(34);
        assertMDYEquals(3, 2, 2010, remindTodayEmail.getHireDate());
        assertEquals(18, remindTodayEmail.getDaysSinceHire());
        assertMDYEquals(3, 25, 2010, remindTodayEmail.getPostmarkDate());
        assertMDYEquals(3, 20, 2010, remindTodayEmail.getReminderDate());
        assertTrue(remindTodayEmail.isWillSendInitialEmail());
        assertTrue(remindTodayEmail.isWillSendReminderEmail());
        assertFalse(remindTodayEmail.isWillNeverSendReminderEmail());

        TaxCreditsEmail remindTodayEmailButAlreadySent = collection.getEmails().get(52);
        assertMDYEquals(3, 2, 2010, remindTodayEmailButAlreadySent.getHireDate());
        assertEquals(18, remindTodayEmailButAlreadySent.getDaysSinceHire());
        assertMDYEquals(3, 25, 2010, remindTodayEmailButAlreadySent.getPostmarkDate());
        assertMDYEquals(3, 20, 2010, remindTodayEmailButAlreadySent.getReminderDate());
        assertFalse(remindTodayEmailButAlreadySent.isWillSendInitialEmail());
        assertFalse(remindTodayEmailButAlreadySent.isWillSendReminderEmail());
        assertFalse(remindTodayEmailButAlreadySent.isWillNeverSendReminderEmail());

        TaxCreditsEmail tooLateForReminder = collection.getEmails().get(14);
        assertMDYEquals(3, 1, 2010, tooLateForReminder.getHireDate());
        assertEquals(19, tooLateForReminder.getDaysSinceHire());
        assertMDYEquals(3, 24, 2010, tooLateForReminder.getPostmarkDate());
        assertMDYEquals(3, 19, 2010, tooLateForReminder.getReminderDate());
        assertTrue(tooLateForReminder.isWillSendInitialEmail());
        assertFalse(tooLateForReminder.isWillSendReminderEmail());
        assertTrue(tooLateForReminder.isWillNeverSendReminderEmail());

        //test stats
        assertEquals(3132, collection.getTotalEmails());
        assertMDYEquals(2, 29,2010, collection.getEarliestHireDate());
        assertMDYEquals(3, 16,2010, collection.getEarliestReminderDate());
        assertMDYEquals(2, 26,2020, collection.getLatestHireDate());
        assertMDYEquals(3, 13,2020, collection.getLatestReminderDate());
        assertEquals(3129, collection.getNumInitialUnsent());
        assertEquals(3130, collection.getNumReminderUnsent());
        assertEquals(102, collection.getNumReminderThisRun());
        assertEquals(1013, collection.getNumNoReminder());
    }

    //0-indexed month
    private static void assertMDYEquals(int month, int day, int year, Date actual) {
        Calendar expectedCal = Calendar.getInstance();
        expectedCal.setTime(actual);
        assertEquals(month,expectedCal.get(Calendar.MONTH));
        assertEquals(day,expectedCal.get(Calendar.DATE));
        assertEquals(year,expectedCal.get(Calendar.YEAR));
    }

    @Test
    public void testPersistState() throws Exception {
        File testFile = new File("D:\\dev\\PSP\\main\\Tools\\Tax Credits Email Tool\\test", "TestCustomerList2.csv");

        revertState(testFile);

        TaxCreditsEmailCollection collection = new TaxCreditsEmailCollection(testFile);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
        Date runDate = sdf.parse("04/25/2010");

        collection.loadFromFile(runDate);

        assertEquals(3, collection.getNumInitialUnsent());
        assertEquals(2, collection.getNumReminderThisRun());

        collection.markInitialSent();

        collection.persistCsv();

        TaxCreditsEmailCollection newCollection = new TaxCreditsEmailCollection(testFile);
        newCollection.loadFromFile(sdf.parse("04/25/2010"));
        assertEquals(0, newCollection.getNumInitialUnsent());
        assertEquals(2, newCollection.getNumReminderThisRun());

        collection.markReminderSent();
        collection.persistCsv();

        TaxCreditsEmailCollection newCollection2 = new TaxCreditsEmailCollection(testFile);
        newCollection2.loadFromFile(sdf.parse("04/25/2010"));
        assertEquals(0, newCollection2.getNumInitialUnsent());
        assertEquals(0, newCollection2.getNumReminderThisRun());
        assertEquals(1, newCollection2.getNumReminderUnsent());
        

        revertState(testFile);


    }

    private void revertState(File testFile) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
        Date runDate = sdf.parse("04/25/2010");

        TaxCreditsEmailCollection collection = new TaxCreditsEmailCollection(testFile);
        collection.loadFromFile(runDate);

        for (TaxCreditsEmail email : collection.getEmails()) {
            email.setInitialEmailSent(null);
            email.setReminderEmailSent(false);
        }

        collection.persistCsv();
    }

}
