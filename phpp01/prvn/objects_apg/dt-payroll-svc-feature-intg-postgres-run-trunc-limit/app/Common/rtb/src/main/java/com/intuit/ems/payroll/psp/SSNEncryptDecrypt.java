package com.intuit.ems.payroll.psp;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationProxy;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Contact;
import com.intuit.sbd.payroll.psp.domain.ContactRole;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ssaxena2
 * Date: 4/11/17
 * Time: 10:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class SSNEncryptDecrypt {

    private static final String FILE_NAME_COMMAND = "-file";
    private static final String COMMIT_COMMAND = "-commit";

    private File mFileName;

    private boolean mCommit;

    public static void main(String[] args) {
        try {
            SSNEncryptDecrypt ssnEncryptDecrypt = new SSNEncryptDecrypt();
            ssnEncryptDecrypt.parseArgs(args);
            List<String> records = FileUtils.readLines(ssnEncryptDecrypt.getmFileName());
            String[] record;
            for (String s : records) {
                record = s.split(",");
                if (record.length != 3) {
                    ssnEncryptDecrypt.print("Can't process record %s no.of arguments not correct", s);
                    continue;
                }
                PayrollServices.beginUnitOfWork();
                Company company = Company.findCompany(record[0], SourceSystemCode.QBDT);

                Contact contact = company.getContactByRoleCode(ContactRole.PrimaryPrincipal);
                ssnEncryptDecrypt.print("PSID %s SSNPlainTextOld %s SSNOldSaved %s SSNOldinSheet %s SSNNewInSheet %s", record[0], contact.getSocialSecurityNumberPlainText(), contact.getSocialSecurityNumber(), record[1], record[2]);
                contact.setSocialSecurityNumberPlainText(record[2]);
                if (ssnEncryptDecrypt.ismCommit())
                    PayrollServices.commitUnitOfWork();
                else
                    PayrollServices.rollbackUnitOfWork();
            }
        } catch (IOException exception) {
            System.out.println("ERROR: Error in main");
            exception.printStackTrace();
            System.exit(-1);
        }
    }

    private void parseArgs(String[] args) {

        final String usage = "SSNEncryptDecrypt -file=FullPathOfFile -commit=[true|false]";

        for (String arg : args) {
            String[] argParts = arg.split("=");
            if (argParts.length == 2) {
                if (argParts[0].equals(FILE_NAME_COMMAND)) {
                    mFileName = new File(argParts[1]);
                    print("-file %s",mFileName);
                } else if (argParts[0].equals(COMMIT_COMMAND)) {
                    mCommit = Boolean.valueOf(argParts[1]);
                    print("-commit %s",mCommit);
                }
            } else {
                print("ERROR: Invalid Argument, Usage - " + usage);
                throw new RuntimeException("Invalid argument: " + arg);
            }
        }

        if (mFileName == null) {
            print("ERROR: Invalid parameters - Must provide filename. Usage " + usage);
            System.exit(-1);
        }
    }


    public File getmFileName() {
        return mFileName;
    }

    public boolean ismCommit() {
        return mCommit;
    }

    private synchronized void print(String format, Object... args) {
        String message = String.format(format, args);
        System.out.println(message);
    }
}
