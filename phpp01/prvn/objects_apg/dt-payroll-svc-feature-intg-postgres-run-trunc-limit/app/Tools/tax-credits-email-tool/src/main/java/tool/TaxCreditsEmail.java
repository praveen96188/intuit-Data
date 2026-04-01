package tool;

import java.util.Calendar;
import java.util.Date;

/**
 * User: dweinberg
 * Date: Apr 20, 2010
 * Time: 3:29:06 PM
 */
public class TaxCreditsEmail {
    //cust info
    private String emailAddress;
    private String name;
    private String firstName;
    private boolean canEmail;
    
    private Date hireDate;

    //persistent data
    private Date initialEmailSent;
    private boolean reminderEmailSent;
    private boolean tc555;

    //transient data
    private int daysSinceHire;
    private Date postmarkDate;
    private Date reminderDate;    
    private boolean willSendInitialEmail;
    private boolean willNeverSendInitialEmail;
    private boolean willSendReminderEmail;
    private boolean willNeverSendReminderEmail;

    //run business logic to populate transient fields
    public void calculate(Date runDate) {
        if (hireDate == null) {
            throw new RuntimeException("hireDate must not be null: " + emailAddress);
        }

        Calendar hireCal = Calendar.getInstance();
        hireCal.setTime(hireDate);
        Calendar runCal = Calendar.getInstance();
        runCal.setTime(runDate);
        
        daysSinceHire = (int)((runCal.getTimeInMillis() - hireCal.getTimeInMillis()) / (24 * 60 * 60 * 1000));

        Calendar postmarkCal = Calendar.getInstance();
        postmarkCal.setTime(hireDate);
        postmarkCal.add(Calendar.DATE, 23);
        postmarkDate = postmarkCal.getTime();

        Calendar reminderCal = Calendar.getInstance();
        reminderCal.setTime(hireDate);
        reminderCal.add(Calendar.DATE, 18);
        reminderDate = reminderCal.getTime();

        //is the reminder within 2 days of the initial email date?
        //if no initial date, assume run date
        Calendar reminderTestCal = Calendar.getInstance();
        if (initialEmailSent != null) {
            reminderTestCal.setTime(initialEmailSent);
        } else {
            reminderTestCal.setTime(runDate);
        }
        reminderTestCal.add(Calendar.DATE, 2);
        if (! reminderCal.after(reminderTestCal))  {
            reminderDate = null;
        }


        willSendInitialEmail = (initialEmailSent == null && daysSinceHire <= 18 && canEmail);
        willNeverSendInitialEmail = (initialEmailSent == null && (!canEmail || daysSinceHire > 18));
        willSendReminderEmail = initialEmailSent != null && !reminderEmailSent && canEmail && runDate.equals(reminderDate);
        willNeverSendReminderEmail = !reminderEmailSent && (!canEmail || reminderDate == null || runDate.after(reminderDate));
        

    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getInitialEmailSent() {
        return initialEmailSent;
    }

    public void setInitialEmailSent(Date initialEmailSent) {
        this.initialEmailSent = initialEmailSent;
    }

    public boolean isReminderEmailSent() {
        return reminderEmailSent;
    }

    public void setReminderEmailSent(boolean reminderEmailSent) {
        this.reminderEmailSent = reminderEmailSent;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    public int getDaysSinceHire() {
        return daysSinceHire;
    }

    public Date getPostmarkDate() {
        return postmarkDate;
    }

    public Date getReminderDate() {
        return reminderDate;
    }

    public boolean isWillSendInitialEmail() {
        return willSendInitialEmail;
    }

    public boolean isWillSendReminderEmail() {
        return willSendReminderEmail;
    }

    public boolean isWillNeverSendReminderEmail() {
        return willNeverSendReminderEmail;
    }

    public boolean isWillNeverSendInitialEmail() {
        return willNeverSendInitialEmail;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public boolean isCanEmail() {
        return canEmail;
    }

    public void setCanEmail(boolean canEmail) {
        this.canEmail = canEmail;
    }

    public boolean isTc555() {
        return tc555;
    }

    public void setTc555(boolean tc555) {
        this.tc555 = tc555;
    }
}
