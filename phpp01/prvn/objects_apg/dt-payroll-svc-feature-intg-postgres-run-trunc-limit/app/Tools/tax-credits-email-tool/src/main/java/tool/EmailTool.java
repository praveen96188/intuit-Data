package tool;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: dweinberg
 * Date: Apr 20, 2010
 * Time: 1:13:30 PM
 */
public class EmailTool {
    private JLabel fileName;
    private JButton browseButton;
    private JTextField runDateText;
    private JButton loadButton;
    private JLabel totalCustomers;
    private JLabel earliestHireDate;
    private JLabel latestHireDate;
    private JLabel initialEmailUnsent;
    private JLabel reminderEmailUnsent;
    private JLabel reminderNotGenerated;
    private JLabel reminderEmailsForRunDate;
    private JButton initialEmailButton;
    private JButton reminderEmailButton;
    private JPanel toolForm;
    private JLabel statusLabel;
    private JLabel noInitialEmail;
    private JRadioButton TC555RadioButton;
    private JRadioButton TC101RadioButton;

    private File customerFile;
    private TaxCreditsEmailCollection emailCollection;
    private String fileNameString;
    private String runDateString;
    private Date runDate;
    private boolean loading;
    private boolean loaded;
    private boolean working;
    private boolean stale;
    private String status="";
    private boolean tc555;

    public EmailTool() {
        ButtonGroup group = new ButtonGroup();
        group.add(TC555RadioButton);
        group.add(TC101RadioButton);

        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fc = new JFileChooser();
                try {
                    fc.setCurrentDirectory(new File(new File(".").getCanonicalPath()));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                int returnVal = fc.showOpenDialog(EmailTool.this.toolForm);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File newFile = fc.getSelectedFile();
                    try {
                        if (! TaxCreditsEmailCollection.hasAllColumns(newFile)){
                            int n = JOptionPane.showOptionDialog(EmailTool.this.toolForm, "Would you like to add reporting columns?", "Reporting columns not found", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Yes","Cancel"}, "Yes");
                            if (n == 1) {
                                return;
                            } else {
                                TaxCreditsEmailCollection.addReportingColumns(newFile);
                            }
                        }

                        if (customerFile != null) {
                            int n = JOptionPane.showOptionDialog(EmailTool.this.toolForm, "Would you like to append '" + newFile.getName() + "' to the current file or replace?", "Existing file found", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Append","Replace"}, "Append");
                            if (n == 1) {
                                setCustomerFile(newFile);
                            } else {
                                TaxCreditsEmailCollection.appendFile(customerFile, newFile);
                                setCustomerFile(customerFile);
                            }
                        } else {
                            setCustomerFile(newFile);
                        }

                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        runDateText.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                getData(EmailTool.this);
            }
            public void removeUpdate(DocumentEvent e) {
                getData(EmailTool.this);
            }
            public void changedUpdate(DocumentEvent e) {
                getData(EmailTool.this);
            }
        });
        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doLoad();
            }
        });
        initialEmailButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doSendInitialEmails();
            }
        });
        reminderEmailButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doSendReminderEmails();
            }
        });
        TC101RadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getData(EmailTool.this);
            }
        });
        TC555RadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getData(EmailTool.this);
            }
        });

        setRunDateString(new SimpleDateFormat("MM/dd/yy").format(new Date()));

        setData(this);
        getData(this);

    }

    public static void main(String[] args) {
        File localJks = new File(".", "pspclient.test.intuit.com.jks");
        if (localJks.exists()) {
            System.setProperty("javax.net.ssl.trustStore", localJks.getAbsolutePath());
            System.setProperty("javax.net.ssl.trustStorePassword", "intuit");
            System.setProperty("javax.net.ssl.keyStore", localJks.getAbsolutePath());
            System.setProperty("javax.net.ssl.keyStorePassword", "intuit");            
        } else {
            File p4Jks = new File("C:/dev/PSP/main/Gateways/Email/resources/pspclient.test.intuit.com.jks");
            if (p4Jks.exists()) {
                System.setProperty("javax.net.ssl.trustStore", "C:/dev/PSP/main/Gateways/Email/resources/pspclient.test.intuit.com.jks");
                System.setProperty("javax.net.ssl.trustStorePassword", "intuit");
                System.setProperty("javax.net.ssl.keyStore", "C:/dev/PSP/main/Gateways/Email/resources/pspclient.test.intuit.com.jks");
                System.setProperty("javax.net.ssl.keyStorePassword", "intuit");                        
            } else {
                throw new RuntimeException("Keystore could not be found");
            }
        }

        JFrame frame = new JFrame("Tax Credits Email Tool");
        frame.setContentPane(new EmailTool().toolForm);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }


    private void doLoad() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");

        runDateString = sdf.format(runDate);
        status = "Loading...";
        loaded=false;
        loading=true;
        setData(this);


        Runnable runnable = new Runnable() {
            public void run() {
                emailCollection = new TaxCreditsEmailCollection(customerFile);
                emailCollection.loadFromFile(runDate);
                loading=false;
                loaded=true;
                stale=false;
                status = "Ready";
                setData(EmailTool.this);
            }
        };
        new Thread(runnable).start();


    }

    private void doSendInitialEmails() {
        working = true;
        status = "Sending initial emails...";
        setData(this);

        Runnable runnable = new Runnable() {
            public void run() {
                int count=0;

                TaxCreditsEmailSender sender = TaxCreditsEmailSender.createInstance();
                for (TaxCreditsEmail email : emailCollection.getEmails()) {
                    if (email.isWillSendInitialEmail()) {
                        count++;
                        email.setTc555(isTc555());
                        sender.sendEmail(email, EmailTrigger.INTRO_TRIGGER);
                    }
                }

                emailCollection.markInitialSent();
                emailCollection.persistCsv();

                working = false;
                stale = true;
                status = String.format("Initial Emails Sent (%d)", count);
                setData(EmailTool.this);
            }
        };
        new Thread(runnable).start();

    }

    private void doSendReminderEmails() {
        working = true;
        status = "Sending reminder emails...";
        setData(this);

        Runnable runnable = new Runnable() {
            public void run() {
                int count=0;
                TaxCreditsEmailSender sender = TaxCreditsEmailSender.createInstance();
                for (TaxCreditsEmail email : emailCollection.getEmails()) {
                    if (email.isWillSendReminderEmail()) {
                        count++;
                        sender.sendEmail(email, EmailTrigger.REMINDER_TRIGGER);
                    }
                }

                emailCollection.markReminderSent();
                emailCollection.persistCsv();

                working = false;
                stale = true;
                status = String.format("Reminder Emails Sent (%d)", count);
                setData(EmailTool.this);
            }
        };
        new Thread(runnable).start();
    }

    public void setCustomerFile(File customerFile) {

        emailCollection = null;
        status = "";

        this.customerFile = customerFile;
        setFileNameString(customerFile.getAbsolutePath());
    }

    public String getFileNameString() {
        return fileNameString;
    }

    public void setFileNameString(String fileNameString) {
        this.fileNameString = fileNameString;
        setData(this);
    }

    public String getRunDateString() {
        return runDateString;
    }

    public void setRunDateString(String runDateString) {
        this.runDateString = runDateString;

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
        sdf.setLenient(false);

        Date oldRunDate = runDate;

        try {
            runDate = sdf.parse(runDateString);
        } catch (ParseException e) {
            runDate = null;
        }

        if (runDate == null || ! runDate.equals(oldRunDate)) {
            //invalidate results
            emailCollection = null;
            status = "";
        }
    }

    public boolean isTc555() {
        return tc555;
    }

    public void setTc555(boolean tc555) {
        this.tc555 = tc555;        
    }

    public void setData(EmailTool data) {
        if (! runDateText.getText().equals(data.getRunDateString())) {
            runDateText.setText(data.getRunDateString());
        }                
        fileName.setText(data.getFileNameString());
        loadButton.setEnabled(customerFile != null && runDate != null && !loading && !working);
        initialEmailButton.setEnabled(loaded && !working);
        reminderEmailButton.setEnabled(loaded && !working);
        statusLabel.setText(status);
        runDateText.setEnabled(!loading);
        TC555RadioButton.setSelected(isTc555());
        TC101RadioButton.setSelected(!isTc555());

        if (emailCollection != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
            totalCustomers.setText(Integer.toString(emailCollection.getTotalEmails()));
            earliestHireDate.setText(String.format("%s (%s)", sdf.format(emailCollection.getEarliestHireDate()), emailCollection.getEarliestReminderDate() == null ? "" : sdf.format(emailCollection.getEarliestReminderDate())));
            latestHireDate.setText(String.format("%s (%s)", sdf.format(emailCollection.getLatestHireDate()), emailCollection.getLatestReminderDate() == null ? "" : sdf.format(emailCollection.getLatestReminderDate())));
            initialEmailUnsent.setText(Integer.toString(emailCollection.getNumInitialUnsent()));
            noInitialEmail.setText(Integer.toString(emailCollection.getNumNoInitialEmail()));
            reminderEmailUnsent.setText(Integer.toString(emailCollection.getNumReminderUnsent()));
            reminderNotGenerated.setText(Integer.toString(emailCollection.getNumNoReminder()));
            reminderEmailsForRunDate.setText(Integer.toString(emailCollection.getNumReminderThisRun()));
            initialEmailButton.setEnabled(!working && !stale);
            reminderEmailButton.setEnabled(!working && !stale);
            if (stale) {
                initialEmailButton.setToolTipText("Reload to perform other actions");
                reminderEmailButton.setToolTipText("Reload to perform other actions");
            }
        } else {
            totalCustomers.setText("");
            earliestHireDate.setText("");
            latestHireDate.setText("");
            initialEmailUnsent.setText("");
            noInitialEmail.setText("");
            reminderEmailUnsent.setText("");
            reminderNotGenerated.setText("");
            reminderEmailsForRunDate.setText("");
            initialEmailButton.setEnabled(false);
            reminderEmailButton.setEnabled(false);
        }
    }

    public void getData(EmailTool data) {
        data.setRunDateString(runDateText.getText());
        data.setTc555(TC555RadioButton.isSelected());
        setData(this);
    }


}


