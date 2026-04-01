package tool;

/**
 * User: dweinberg
 * Date: Apr 20, 2010
 * Time: 3:39:03 PM
 */
public class EmailTrigger {
    private String triggerId;

    public static EmailTrigger INTRO_TRIGGER = createIntroTrigger();
    public static EmailTrigger REMINDER_TRIGGER = createReminderTrigger();

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

    private EmailTrigger() {
    }

    private static EmailTrigger createIntroTrigger() {
        EmailTrigger et = new EmailTrigger();
        et.setTriggerId("TaxCreditsIntro");
        return et;
    }

    private static EmailTrigger createReminderTrigger() {
        EmailTrigger et = new EmailTrigger();
        et.setTriggerId("TaxCreditsReminder");
        return et;        
    }
}
