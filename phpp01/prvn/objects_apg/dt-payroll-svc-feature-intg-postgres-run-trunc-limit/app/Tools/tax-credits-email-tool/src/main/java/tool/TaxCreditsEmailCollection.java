package tool;

import com.Ostermiller.util.ExcelCSVParser;
import com.Ostermiller.util.ExcelCSVPrinter;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: dweinberg
 * Date: Apr 21, 2010
 * Time: 10:15:48 AM
 * Collection of emails backed by a csv file
 */
public class TaxCreditsEmailCollection {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    static {
        sdf.setLenient(false);
    }

    private File csvFile;
    private Date runDate;

    private List<TaxCreditsEmail> emails;

    //stats
    Date earliestHireDate;
    Date earliestReminderDate;
    Date latestHireDate;
    Date latestReminderDate;
    int totalEmails;
    int numNoInitialEmail;
    int numInitialUnsent;
    int numReminderUnsent;
    int numNoReminder;
    int numReminderThisRun;


    public TaxCreditsEmailCollection(File csvFile) {
        this.csvFile = csvFile;
    }

    public void loadFromFile(Date runDate){
        try {
            this.runDate = runDate;

            emails = new ArrayList<TaxCreditsEmail>();

            if (! csvFile.canWrite()) {
                throw new RuntimeException ("Cannot write to file");
            }

            String[][] csvStrings = ExcelCSVParser.parse(new FileReader(csvFile));


            //reset stats
            earliestHireDate=null;
            earliestReminderDate=null;
            latestHireDate=null;
            latestReminderDate=null;
            numNoInitialEmail=0;
            numInitialUnsent=0;
            numReminderUnsent=0;
            numNoReminder=0;
            numReminderThisRun=0;
                

            //excluding header row
            for (int i = 1; i < csvStrings.length; i++) {
                String[] csvLine = csvStrings[i];
                TaxCreditsEmail email = new TaxCreditsEmail();
                email.setEmailAddress(csvLine[CsvColumns.EMAIL.ordinal()]);
                email.setName(csvLine[CsvColumns.FIRST_NAME.ordinal()] + " " + csvLine[CsvColumns.LAST_NAME.ordinal()]);
                email.setFirstName(csvLine[CsvColumns.FIRST_NAME.ordinal()]);
                String initialEmailSentDateString = csvLine[CsvColumns.INITIAL_SENT.ordinal()];
                if (initialEmailSentDateString.equals("")) {
                    email.setInitialEmailSent(null);
                } else {
                    email.setInitialEmailSent(sdf.parse(initialEmailSentDateString));
                }                
                email.setReminderEmailSent(csvLine[CsvColumns.REMINDER_SENT.ordinal()].equals("Y"));

                email.setHireDate(sdf.parse(csvLine[CsvColumns.HIRE_DATE.ordinal()]));                                
                email.setCanEmail(csvLine[CsvColumns.EMAILABLE.ordinal()].equals("Y"));
                email.setTc555(csvLine[CsvColumns.GROUP.ordinal()].equals("TC555"));
                
                email.calculate(runDate);

                //update stats
                if (earliestHireDate == null || email.getHireDate().before(earliestHireDate)) {
                    earliestHireDate = email.getHireDate();
                    earliestReminderDate = email.getReminderDate();
                }
                if (latestHireDate == null || email.getHireDate().after(latestHireDate)) {
                    latestHireDate = email.getHireDate();
                    latestReminderDate = email.getReminderDate();
                }
                if (email.getInitialEmailSent() == null && ! email.isWillNeverSendInitialEmail()) {
                    numInitialUnsent++;
                }
                if (email.getInitialEmailSent() == null && email.isWillNeverSendInitialEmail()) {
                    numNoInitialEmail++;
                }
                if (! email.isReminderEmailSent() && ! email.isWillNeverSendReminderEmail()) {
                    numReminderUnsent++;
                }
                if (email.isWillNeverSendReminderEmail()) {
                    numNoReminder++;
                }
                if (email.isWillSendReminderEmail()) {
                    numReminderThisRun++;
                }

                emails.add(email);
            }

            totalEmails = emails.size();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void markInitialSent() {
        for (TaxCreditsEmail email : emails) {
            if (email.isWillSendInitialEmail()) {
                email.setInitialEmailSent(runDate);
            }
        }
    }

    public void markReminderSent() {
        for (TaxCreditsEmail email : emails) {
            if (email.isWillSendReminderEmail()) {
                email.setReminderEmailSent(true);
            }
        }        
    }

    public void persistCsv() {
        try {
            String[][] csvStrings = ExcelCSVParser.parse(new FileReader(csvFile));

            for (int i = 1; i < csvStrings.length; i++) {
                TaxCreditsEmail email = emails.get(i-1);
                csvStrings[i][CsvColumns.INITIAL_SENT.ordinal()] = email.getInitialEmailSent() == null ? "" : sdf.format(email.getInitialEmailSent());
                csvStrings[i][CsvColumns.REMINDER_SENT.ordinal()] = email.isReminderEmailSent() ? "Y" : "";
                if (email.getInitialEmailSent() == null) {
                    csvStrings[i][CsvColumns.GROUP.ordinal()] = "";
                } else {
                    csvStrings[i][CsvColumns.GROUP.ordinal()] = email.isTc555() ? "TC555" : "TC101";
                }

            }

            FileOutputStream fos = new FileOutputStream(csvFile);
            ExcelCSVPrinter csvWriter = new ExcelCSVPrinter(fos);

            for (String[] csvString : csvStrings) {
                csvWriter.writeln(csvString);
            }
            csvWriter.flush();
            csvWriter.close();


            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<TaxCreditsEmail> getEmails() {
        return emails;
    }

    public Date getEarliestHireDate() {
        return earliestHireDate;
    }

    public Date getEarliestReminderDate() {
        return earliestReminderDate;
    }

    public Date getLatestHireDate() {
        return latestHireDate;
    }

    public Date getLatestReminderDate() {
        return latestReminderDate;
    }

    public int getNumInitialUnsent() {
        return numInitialUnsent;
    }

    public int getNumReminderUnsent() {
        return numReminderUnsent;
    }

    public int getNumNoReminder() {
        return numNoReminder;
    }

    public int getNumReminderThisRun() {
        return numReminderThisRun;
    }

    public int getTotalEmails() {
        return totalEmails;
    }

    public int getNumNoInitialEmail() {
        return numNoInitialEmail;
    }

    public static boolean hasAllColumns(File file) throws Exception {
        String[][] csvStrings = ExcelCSVParser.parse(new FileReader(file));
        if (csvStrings.length==0){
            throw new Exception("Empty file");
        }
        if (csvStrings[0].length == CsvColumns.values().length) {
            return true;
        } else if (csvStrings[0].length == CsvColumns.values().length - 3) {
            return false;
        } else {
            throw new Exception("Incorrect number of columns");
        }
    }

    public static void addReportingColumns(File file) throws Exception {
        String[][] csvStrings = ExcelCSVParser.parse(new FileReader(file));

        FileOutputStream fos = new FileOutputStream(file);
        ExcelCSVPrinter csvWriter = new ExcelCSVPrinter(fos);

        boolean firstLine = true;
        for (String[] csvString : csvStrings) {
            String[] newLine = new String[csvString.length+3];
            System.arraycopy(csvString, 0, newLine, 0, csvString.length);
            if (firstLine) {
                newLine[newLine.length-3] = "Initial Email Sent";
                newLine[newLine.length-2] = "Reminder Email Sent";
                newLine[newLine.length-1] = "Test Group";
                firstLine = false;                                
            } else {
                newLine[newLine.length-3] = "";
                newLine[newLine.length-2] = "";
                newLine[newLine.length-1] = "";
            }
            csvWriter.writeln(newLine);
            
        }
        csvWriter.flush();
        csvWriter.close();
    }

    public static void appendFile(File mainFile, File newFile) throws Exception {
        String[][] csvStrings = ExcelCSVParser.parse(new FileReader(newFile));


        FileOutputStream fos = new FileOutputStream(mainFile, true);
        ExcelCSVPrinter csvWriter = new ExcelCSVPrinter(fos);

        //skip header
        for (int i=1; i< csvStrings.length; i++) {
            csvWriter.writeln(csvStrings[i]);

        }
        csvWriter.flush();
        csvWriter.close();
    }

    private enum CsvColumns {
        EMAIL,
        FIRST_NAME,
        LAST_NAME,
        DAY_PHONE,
        EVE_PHONE,
        PSID,
        FEIN,
        LEGAL_NAME,
        ADDR1,
        ADDR2,
        CITY,
        STATE,
        ZIP,
        HIRE_DATE,
        PAY_FREQ,
        EMP_ID,
        EMP_FIRST_NAME,
        EMP_LAST_NAME,
        EMP_ADDR1,
        EMP_ADDR2,
        EMP_CITY,
        EMP_STATE,
        EMP_ZIP,
        USERID1,
        SYS_DATE,
        SYS_TIME,
        NEW_EMP,
        TOT_EMP,
        EMAILABLE,        
        INITIAL_SENT,
        REMINDER_SENT,
        GROUP
    }

}
