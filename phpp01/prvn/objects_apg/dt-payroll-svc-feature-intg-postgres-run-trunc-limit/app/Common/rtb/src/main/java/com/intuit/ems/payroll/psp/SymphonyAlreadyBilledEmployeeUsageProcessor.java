package com.intuit.ems.payroll.psp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.Bill;
import com.intuit.sbd.payroll.psp.domain.CompanyUsage;
import com.intuit.sbd.payroll.psp.domain.EmployeeUsage;
import com.intuit.sbd.payroll.psp.domain.EmployeeUsage.UsageBillingEventCode;
import com.intuit.sbd.payroll.psp.domain.PaycheckUsage;
import com.intuit.sbd.payroll.psp.domain.PaycheckUsageHist;
import com.intuit.sbd.payroll.psp.domain.ReasonForFreeChargeCode;
import com.intuit.sbd.payroll.psp.domain.UsagePeriod;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * Customers are getting over billed in case of a paycheck Recreate/Resend of a closed bill and if the employee contains multiple paychecks
 *
 * @author kmuthurangam
 */
public class SymphonyAlreadyBilledEmployeeUsageProcessor {

    private static final String REPORT_NAME_FORMAT ="already_billed_employee_usage_report_$date$.csv";
    private static final String REPORT_DIR =".";
    private static final String DELIMITER= ",";
    private static final String DOUBLE_QUOTE = "\"";
    private static final String DATE_FORMAT = "MMddyyyy";

    public enum RunMode {
        GENERATE_REPORT,
        PROCESS_ALREADY_BILLED_FIX
    }

    public static void main(String[] args) throws IOException {
        if(args.length != 2){
            printUsage();
            System.exit(0);
        }

        String runModeString = args[0];
        String billStartDateString = args[1];

        RunMode runMode = null;
        SpcfCalendar billStartDate = null;
        try {
            runMode = RunMode.valueOf(runModeString);
            billStartDate = SpcfCalendar.parse(DATE_FORMAT, billStartDateString);
        } catch (Exception e){
            System.out.println("Invalid arguments passed");
            printUsage();
            System.exit(0);
        }

        SymphonyAlreadyBilledEmployeeUsageProcessor symphonyOverBillingResend = new SymphonyAlreadyBilledEmployeeUsageProcessor();
        symphonyOverBillingResend.processAlreadyBilledEmployeeUsages(runMode, billStartDate);
    }

    public static void printUsage(){
        System.out.println("java -jar com.intuit.ems.payroll.psp.SymphonyAlreadyBilledEmployeeUsageProcessor (GENERATE_REPORT|PROCESS_ALREADY_BILLED_FIX) 04152019");
    }

    public void processAlreadyBilledEmployeeUsages(RunMode runMode, SpcfCalendar billStartDate) throws IOException{
        Application.setCurrentPrincipal(new PspPrincipal("SymphonyAlreadyBilledEU", "SymphonyAlreadyBilledEmployeeUsageProcessor"));
        Application.beginUnitOfWork();
        DomainEntitySet<EmployeeUsage> employeeUsages = findAlreadyBilledEmployeeUsage(billStartDate);
        switch (runMode){
            case GENERATE_REPORT:
                generateAlreadyBilledEmployeeUsageReport(billStartDate, employeeUsages);
                Application.rollbackUnitOfWork();
                break;
            case PROCESS_ALREADY_BILLED_FIX:
                fixAlreadyBilledEmployeeUsages(billStartDate, employeeUsages);
                Application.rollbackUnitOfWork();
                break;
        }
    }

    public void generateAlreadyBilledEmployeeUsageReport(SpcfCalendar billStartDate, DomainEntitySet<EmployeeUsage> employeeUsages) throws IOException {
        Path filePath = Files.createFile(Paths.get(REPORT_DIR, getReportName()));

        writeReportHeaders(filePath);

        for (EmployeeUsage employeeUsage: employeeUsages) {
            UsagePeriod usagePeriod = employeeUsage.getUsagePeriod();
            CompanyUsage companyUsage = usagePeriod.getCompanyUsage();

            Object[] values = {companyUsage.getLicenseId(), companyUsage.getEntitlementId(), companyUsage.getSourceCompanyId(), employeeUsage.getId(), employeeUsage.getSourceEmployeeId(),
                    employeeUsage.getEmployeeName(), usagePeriod.getStartDate(), usagePeriod.getEndDate(), employeeUsage.getUsageCount(), null, null, null, null, null, null, null, null, null};


            if(employeeUsage.getPaycheckUsageCollection().isEmpty()) {
                System.out.println(String.format("Paycheck Usage Collection is empty %s", employeeUsage));
                writeToFile(filePath, values);
                continue;
            }

            for (PaycheckUsage paycheckUsage: employeeUsage.getPaycheckUsageCollection()) {

                values[9]=  paycheckUsage.getId();
                values[10]=  paycheckUsage.getPaycheckDate();
                values[11]=  paycheckUsage.getReasonForFreeCharge();


                Bill bill = paycheckUsage.getBill();
                values[12]=  bill.getId();
                values[13]=  bill.getBillDate();
                values[14]=  bill.getUsageCount();
                values[15]=  bill.getSynchedCount();
                values[16]= null;
                values[17]= null;

                if(paycheckUsage.getPaycheckUsageHistCollection().isEmpty()){
                    writeToFile(filePath, values);
                    continue;
                }

                for (PaycheckUsageHist paycheckUsageHist: paycheckUsage.getPaycheckUsageHistCollection()){
                    values[16]=  paycheckUsageHist.getEmployeeUsage().getId();
                    values[17]=  paycheckUsageHist.getNotes();
                    writeToFile(filePath, values);
                }
            }

        }

    }

    public void fixAlreadyBilledEmployeeUsages(SpcfCalendar billStartDate, DomainEntitySet<EmployeeUsage> employeeUsages) throws IOException {
        Path filePath = Files.createFile(Paths.get(REPORT_DIR, getReportName()));

        writeReportHeaders(filePath);

        for (EmployeeUsage employeeUsage: employeeUsages) {
            UsagePeriod usagePeriod = employeeUsage.getUsagePeriod();
            CompanyUsage companyUsage = usagePeriod.getCompanyUsage();

            DomainEntitySet<PaycheckUsage> alreadyBilledPaycheckUsageCollection = employeeUsage.getAlreadyBilledPaychecks();

            Object[] values = {companyUsage.getLicenseId(), companyUsage.getEntitlementId(), companyUsage.getSourceCompanyId(), employeeUsage.getId(), employeeUsage.getSourceEmployeeId(),
                    employeeUsage.getEmployeeName(), usagePeriod.getStartDate(), usagePeriod.getEndDate(), employeeUsage.getUsageCount(), null, null, null, null, null, null, null, null, null};

            for (PaycheckUsage paycheckUsage: employeeUsage.getPaycheckUsageCollection()) {

                values[8] = employeeUsage.getUsageCount();
                values[9] = paycheckUsage.getId();
                values[10] = paycheckUsage.getPaycheckDate();
                values[11] = paycheckUsage.getReasonForFreeCharge();


                Bill bill = paycheckUsage.getBill();
                values[12] = bill.getId();
                values[13] = bill.getBillDate();
                values[14] = bill.getUsageCount();
                values[15] = bill.getSynchedCount();
                values[16] = null;
                values[17] = null;

                if (paycheckUsage.getReasonForFreeCharge() == ReasonForFreeChargeCode.AlreadyBilled) {

                    for (PaycheckUsageHist paycheckUsageHist : paycheckUsage.getPaycheckUsageHistCollection()) {
                        values[16] = paycheckUsageHist.getEmployeeUsage().getId();
                        values[17] = paycheckUsageHist.getNotes();
                    }

                    writeToFile(filePath, values);
                    continue;
                }

                paycheckUsage.setReasonForFreeCharge(ReasonForFreeChargeCode.AlreadyBilled);
                checkAndAssociatePaycheckUsageWithAlreadyBilledEmployeeUsage(alreadyBilledPaycheckUsageCollection,
                        employeeUsage, paycheckUsage);

                values[11] = paycheckUsage.getReasonForFreeCharge();

                for (PaycheckUsageHist paycheckUsageHist : paycheckUsage.getPaycheckUsageHistCollection()) {
                    values[16] = paycheckUsageHist.getEmployeeUsage().getId();
                    values[17] = paycheckUsageHist.getNotes();
                }

                // Decrease Employee Usage
                if (employeeUsage.getUsageCount() <= 0) {
                    writeToFile(filePath, values);
                    continue;
                }

                employeeUsage.decreaseUsageCount();
                values[8] = employeeUsage.getUsageCount();

                if(employeeUsage.getUsageCount() != 0){
                    writeToFile(filePath, values);
                    continue;
                }

                bill.decreaseUsageCount();

                values[14] = bill.getUsageCount();
                values[15] = bill.getSynchedCount();

                writeToFile(filePath, values);
            }
        }
    }

    private void writeToFile(Path filePath, Object[] values) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        for (Object value: values) {
            stringBuffer.append(DOUBLE_QUOTE).append(ObjectUtils.defaultIfNull(value, StringUtils.EMPTY)).append(DOUBLE_QUOTE).append(DELIMITER);
        }
        stringBuffer.deleteCharAt(stringBuffer.length()-1);
        stringBuffer.append(System.lineSeparator());
        FileUtils.write(filePath.toFile(), stringBuffer, true);
    }

    private void writeReportHeaders(Path filePath) throws IOException {
        String[] reportHeaders = {"License", "EOC", "Source Company ID", "Employee Usage ID", "Source Employee Id", "Employee Name", "Usage Period Start Date", "Usage Period End Date",
                "Employee Usage Count", "Paycheck Seq", "Paycheck Date", "Reason for Free Charge",  "Bill Seq", "Bill Date",  "Bill Usage Count", "Bill Synched Count", "Reference Employee Usage", "Employee Notes"};

        writeToFile(filePath, reportHeaders);
    }

    public DomainEntitySet<EmployeeUsage> findAlreadyBilledEmployeeUsage(SpcfCalendar billStartDate){
        String query = "select distinct paycheckUsage.EmployeeUsage" +
                " from com.intuit.sbd.payroll.psp.domain.Bill bill, com.intuit.sbd.payroll.psp.domain.PaycheckUsage paycheckUsage, " +
                "com.intuit.sbd.payroll.psp.domain.PaycheckUsageHist paycheckUsageHist " +
                "where paycheckUsage.Bill = bill and paycheckUsageHist.PaycheckUsage = paycheckUsage " +
                "and paycheckUsage.ReasonForFreeCharge = :reasonForFreeCharge " +
                "and bill.BillDate between :billStartDate and :billEndDate ";
        String[] paramNames = {"reasonForFreeCharge", "billStartDate", "billEndDate"};
        Object[] paramValues = {ReasonForFreeChargeCode.AlreadyBilled, billStartDate, CalendarUtils.getLastDayOfMonth(billStartDate)};
        DomainEntitySet<EmployeeUsage> employeeUsages = Application.findByHQLQuery(query, paramNames, paramValues);
        return employeeUsages;
    }

    private String getReportName(){
        return REPORT_NAME_FORMAT.replace("$date$", SpcfCalendar.getNow().format("MMddyyyyhhmmss"));
    }

    public void checkAndAssociatePaycheckUsageWithAlreadyBilledEmployeeUsage(DomainEntitySet<PaycheckUsage> alreadyBilledPaycheckUsageCollection, EmployeeUsage pEmployeeUsage, PaycheckUsage paycheckUsage) {
        if(alreadyBilledPaycheckUsageCollection.isEmpty()){
            return;
        }

        if(alreadyBilledPaycheckUsageCollection.size() > 1){
            System.out.println(String.format("%s - %s", UsageBillingEventCode.FOUND_MULTIPLE_ALREADY_BILLED_EMPLOYEE_USAGES, pEmployeeUsage));
        }

        DomainEntitySet<EmployeeUsage> alreadyPersistedEmployeeUsages = new DomainEntitySet<EmployeeUsage>();
        for (PaycheckUsage alreadyBilledPaycheckUsage: alreadyBilledPaycheckUsageCollection) {
            paycheckUsage.setReasonForFreeCharge(ReasonForFreeChargeCode.AlreadyBilled);
            for (PaycheckUsageHist alreadyBilledPaycheckUsageHist: alreadyBilledPaycheckUsage.getPaycheckUsageHistCollection()) {

                if(alreadyPersistedEmployeeUsages.contains(alreadyBilledPaycheckUsageHist.getEmployeeUsage())){
                    continue;
                }

                PaycheckUsageHist paycheckUsageHist = new PaycheckUsageHist(paycheckUsage, alreadyBilledPaycheckUsageHist);

                paycheckUsage.addPaycheckUsageHist(paycheckUsageHist);
                Application.save(paycheckUsageHist);
                alreadyPersistedEmployeeUsages.add(alreadyBilledPaycheckUsageHist.getEmployeeUsage());
            }
            Application.save(paycheckUsage);
        }
    }

}
