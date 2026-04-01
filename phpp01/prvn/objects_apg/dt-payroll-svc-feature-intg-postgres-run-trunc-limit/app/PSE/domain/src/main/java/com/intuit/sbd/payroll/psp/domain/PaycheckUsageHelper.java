package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.EmployeeUsage.OpenBillEmployeeUsageCode;
import com.intuit.sbd.payroll.psp.domain.EmployeeUsage.UsageBillingEventCode;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class PaycheckUsageHelper {

    public static SpcfLogger logger = SpcfLogManager.getLogger(PaycheckUsageHelper.class);

    public static void processExistingEmployeeUsage(PaycheckUsage currentPaycheckUsage, EmployeeUsage currentEmployeeUsage, AtomicInteger paycheckUsageContribution){
        logger.info(String.format("Processing existing %s", currentEmployeeUsage));

        processClosedBillEmployeeUsages(currentPaycheckUsage, currentEmployeeUsage, paycheckUsageContribution);

        processOpenBillEmployeeUsages(currentPaycheckUsage, currentEmployeeUsage, paycheckUsageContribution);

    }

    private static void processClosedBillEmployeeUsages(PaycheckUsage currentPaycheckUsage, EmployeeUsage currentEmployeeUsage, AtomicInteger paycheckUsageContribution){

        DomainEntitySet<EmployeeUsage> closedBillEmployeeUsages = currentEmployeeUsage.getClosedBillEmployeeUsages();

        if(closedBillEmployeeUsages.isEmpty()){
            return;
        }

        if(closedBillEmployeeUsages.size() > 1){
            logger.warn(String.format("%s %s ", UsageBillingEventCode.MULTIPLE_EMPLOYEE_USAGES_FOUND_FOR_CLOSED_BILL, currentEmployeeUsage));
        }

        int alreadyBilledCount = 0;
        for (EmployeeUsage closedBillEmployeeUsage: closedBillEmployeeUsages) {
            checkAndAssociatePaycheckUsageWithAlreadyBilledEmployeeUsage(currentEmployeeUsage, currentPaycheckUsage, closedBillEmployeeUsage);

            if(closedBillEmployeeUsage.getUsageCount() > 0 ){
                alreadyBilledCount++;
            }
        }

        if(alreadyBilledCount == 0){
            logger.info(String.format("%s %s", UsageBillingEventCode.NO_WAIVE_OFF_BECAUSE_USAGE_NOT_ALREADY_BILLED, currentPaycheckUsage));
            increasePaycheckUsageContribution(currentPaycheckUsage, paycheckUsageContribution);
            return;
        }

        currentPaycheckUsage.setReasonForFreeCharge(ReasonForFreeChargeCode.AlreadyBilled);
        logger.info(String.format("%s %s", UsageBillingEventCode.USAGE_BILLING_WAIVED_OFF_ON_ALREADY_BILLED_USAGE, currentPaycheckUsage));
        Application.save(currentPaycheckUsage);
    }

    private static void processOpenBillEmployeeUsages(PaycheckUsage currentPaycheckUsage, EmployeeUsage currentEmployeeUsage, AtomicInteger paycheckUsageContribution){

        DomainEntitySet<EmployeeUsage> openBillEmployeeUsages = currentEmployeeUsage.getOpenBillEmployeeUsages();

        if(openBillEmployeeUsages.isEmpty()){
            return;
        }

        Map<OpenBillEmployeeUsageCode, DomainEntitySet<EmployeeUsage>> openBillEmployeeUsageMap = categorizeOpenBillEmployeeUsage(openBillEmployeeUsages);

        for (OpenBillEmployeeUsageCode employeeUsageInfoCode: OpenBillEmployeeUsageCode.values()){

            DomainEntitySet<EmployeeUsage> usageTransferEmployeeUsages = openBillEmployeeUsageMap.get(employeeUsageInfoCode);

            if(usageTransferEmployeeUsages.isEmpty()){
                continue;
            }

            switch (employeeUsageInfoCode) {
                case USAGE_ALREADY_TRANSFERRED:
                    processUsageTransferredEmployeeUsages(currentPaycheckUsage, currentEmployeeUsage, usageTransferEmployeeUsages, paycheckUsageContribution);
                    break;
                case ELIGIBLE_FOR_PAYCHECK_USAGE_TRANSFER:
                    processPaycheckUsageTransferforEmployeeUsages(currentPaycheckUsage, currentEmployeeUsage, usageTransferEmployeeUsages, paycheckUsageContribution);
                    break;
                case ELIGIBLE_FOR_EMPLOYEE_USAGE_TRANSFER:
                    processEmployeeUsageTransferForEmployeeUsages(currentPaycheckUsage, currentEmployeeUsage, usageTransferEmployeeUsages, paycheckUsageContribution);
                    break;
                default:
                    logger.warn("No action defined for "+ employeeUsageInfoCode);
                    break;
            }
        }

    }

    private static void processUsageTransferredEmployeeUsages(PaycheckUsage currentPaycheckUsage, EmployeeUsage currentEmployeeUsage, DomainEntitySet<EmployeeUsage> usageTransferEmployeeUsages, AtomicInteger paycheckUsageContribution) {
        // Do Paycheck Usage Transfer again to handle cases like Company File Backup ->  Send Paycheck -> Company File Restore -> Send Paycheck -> Open Original File -> Send Paycheck
        doUsageTransferForAllPaychecks(OpenBillEmployeeUsageCode.USAGE_ALREADY_TRANSFERRED, currentPaycheckUsage, currentEmployeeUsage, usageTransferEmployeeUsages, paycheckUsageContribution);
    }

    private static void processPaycheckUsageTransferforEmployeeUsages(PaycheckUsage currentPaycheckUsage, EmployeeUsage currentEmployeeUsage, DomainEntitySet<EmployeeUsage> usageTransferEmployeeUsages, AtomicInteger paycheckUsageContribution) {
        // Do Paycheck Usage Transfer for Employee Usages with 0 usage count
        if(usageTransferEmployeeUsages.size() > 1 && !EmployeeUsage.isAllEmployeeUsagesOfSameCompany(usageTransferEmployeeUsages)){
            logger.warn(String.format("%s %s, so not doing Paycheck Usage Transfer", UsageBillingEventCode.FOUND_MULIPLE_OPEN_EMPLOYEES_USAGES_ACROSS_COMPANIES, currentEmployeeUsage));

            increasePaycheckUsageContribution(currentPaycheckUsage, paycheckUsageContribution);

            return;
        }

        doUsageTransferForAllPaychecks(OpenBillEmployeeUsageCode.ELIGIBLE_FOR_PAYCHECK_USAGE_TRANSFER, currentPaycheckUsage, currentEmployeeUsage, usageTransferEmployeeUsages, paycheckUsageContribution);

    }

    private static boolean containsEitherMultipleOpenBillsOrBothBills(OpenBillEmployeeUsageCode openBillEmployeeUsageCode, PaycheckUsage currentPaycheckUsage, EmployeeUsage currentEmployeeUsage, AtomicInteger paycheckUsageContribution, EmployeeUsage usageTransferEmployeeUsage) {
        if (usageTransferEmployeeUsage.containsMultipleOpenBills()) {
            logger.warn(String.format("[%s] %s %s, so not attempting to do Employee Usage Transfer", openBillEmployeeUsageCode, UsageBillingEventCode.FOUND_MULTIPLE_OPEN_BILLS_FOR_SAME_EMPLOYEE, currentEmployeeUsage));
            increasePaycheckUsageContribution(currentPaycheckUsage, paycheckUsageContribution);
            return true;
        }

        if (usageTransferEmployeeUsage.containsOpenAndClosedBill()) {
            logger.warn(String.format("[%s] %s %s, so not attempting to do Employee Usage Transfer", openBillEmployeeUsageCode, UsageBillingEventCode.FOUND_BOTH_OPEN_AND_CLOSED_BILL_FOR_SAME_EMPLOYEE, currentEmployeeUsage));
            increasePaycheckUsageContribution(currentPaycheckUsage, paycheckUsageContribution);
            return true;
        }
        return false;
    }

    private static void processEmployeeUsageTransferForEmployeeUsages(PaycheckUsage currentPaycheckUsage, EmployeeUsage currentEmployeeUsage, DomainEntitySet<EmployeeUsage> usageTransferEmployeeUsages, AtomicInteger paycheckUsageContribution) {
        if(usageTransferEmployeeUsages.size() > 1){
            logger.warn(String.format("%s %s, so not attempting to do Employee Usage Transfer", UsageBillingEventCode.FOUND_MULIPLE_OPEN_EMPLOYEES_USAGES_FOR_USAGE_TRANSFER, currentEmployeeUsage));

            increasePaycheckUsageContribution(currentPaycheckUsage, paycheckUsageContribution);

            return;
        }

        for (EmployeeUsage nonUsageTransferEmployeeUsage: usageTransferEmployeeUsages) {

            if (containsEitherMultipleOpenBillsOrBothBills(OpenBillEmployeeUsageCode.ELIGIBLE_FOR_PAYCHECK_USAGE_TRANSFER, currentPaycheckUsage, currentEmployeeUsage, paycheckUsageContribution, nonUsageTransferEmployeeUsage))
                continue;

            doEmployeeUsageTransfer(currentPaycheckUsage, currentEmployeeUsage, nonUsageTransferEmployeeUsage, paycheckUsageContribution);

            for (PaycheckUsage oldPaycheckUsage: nonUsageTransferEmployeeUsage.getPaycheckUsageCollection()) {
                doPaycheckUsageTransfer(currentEmployeeUsage, nonUsageTransferEmployeeUsage, oldPaycheckUsage);
            }
        }
    }

    private static void increasePaycheckUsageContribution(PaycheckUsage currentPaycheckUsage, AtomicInteger paycheckUsageContribution) {
        if (!currentPaycheckUsage.isFree() && currentPaycheckUsage.getPaycheckStatusCode() == BillingPaycheckStatusCode.Active) {
            paycheckUsageContribution.set(1);
        }
    }

    private static void doUsageTransferForAllPaychecks(OpenBillEmployeeUsageCode openBillEmployeeUsageCode, PaycheckUsage currentPaycheckUsage, EmployeeUsage currentEmployeeUsage, DomainEntitySet<EmployeeUsage> usageTransferEmployeeUsages, AtomicInteger paycheckUsageContribution) {
        for (EmployeeUsage usageTransferEmployeeUsage: usageTransferEmployeeUsages) {

            if (containsEitherMultipleOpenBillsOrBothBills(openBillEmployeeUsageCode, currentPaycheckUsage, currentEmployeeUsage, paycheckUsageContribution, usageTransferEmployeeUsage))
                continue;

            for (PaycheckUsage oldPaycheckUsage: usageTransferEmployeeUsage.getPaycheckUsageCollection()) {
                doPaycheckUsageTransfer(currentEmployeeUsage, usageTransferEmployeeUsage, oldPaycheckUsage);
            }
        }
    }

    private static Map<OpenBillEmployeeUsageCode, DomainEntitySet<EmployeeUsage>> categorizeOpenBillEmployeeUsage(DomainEntitySet<EmployeeUsage> openBillEmployeeUsages) {
        Map<OpenBillEmployeeUsageCode, DomainEntitySet<EmployeeUsage>> usageTransferEmployeeUsageMap = new HashMap<>();

        // Initialize the OpenBillEmployeeUsageCode with empty list
        for(OpenBillEmployeeUsageCode employeeUsageInfoCode: OpenBillEmployeeUsageCode.values()) {
            usageTransferEmployeeUsageMap.put(employeeUsageInfoCode, new DomainEntitySet<EmployeeUsage>());
        }

        for (EmployeeUsage openBillEmployeeUsage: openBillEmployeeUsages) {

            OpenBillEmployeeUsageCode employeeUsageInfoCode = OpenBillEmployeeUsageCode.ELIGIBLE_FOR_EMPLOYEE_USAGE_TRANSFER;

            if(openBillEmployeeUsage.getUsageCount() == 0 && openBillEmployeeUsage.isUsageTransfer()){
                employeeUsageInfoCode = OpenBillEmployeeUsageCode.USAGE_ALREADY_TRANSFERRED;
            } else if(openBillEmployeeUsage.getUsageCount() == 0) {
                employeeUsageInfoCode = OpenBillEmployeeUsageCode.ELIGIBLE_FOR_PAYCHECK_USAGE_TRANSFER;
            }

            DomainEntitySet<EmployeeUsage> employeeUsages = usageTransferEmployeeUsageMap.get(employeeUsageInfoCode);

            employeeUsages.add(openBillEmployeeUsage);
        }

        return usageTransferEmployeeUsageMap;
    }

    private static void doEmployeeUsageTransfer(PaycheckUsage currentPaycheckUsage, EmployeeUsage currentEmployeeUsage, EmployeeUsage oldEmployeeUsage, AtomicInteger paycheckUsageContribution) {

        if(oldEmployeeUsage.getUsageCount() <= 0) {
            logger.warn(String.format("%s for %s ", UsageBillingEventCode.EMPLOYEE_USAGE_IS_ALREADY_ZERO, currentEmployeeUsage));
            return;
        }

        decreaseEmployeeUsage(oldEmployeeUsage);

        // Increment the Paycheck Usage Contribution as the old Employee Usage is invalidated
        increasePaycheckUsageContribution(currentPaycheckUsage, paycheckUsageContribution);

    }

    private static void doPaycheckUsageTransfer(EmployeeUsage currentEmployeeUsage, EmployeeUsage oldEmployeeUsage, PaycheckUsage oldPaycheckUsage) {
        if(oldPaycheckUsage.getReasonForFreeCharge() == ReasonForFreeChargeCode.UsageTransfer){
            logger.warn(String.format("%s Old Employee Usage (%s) as part of new Employee Usage (%s)", UsageBillingEventCode.EMPLOYEE_USAGE_ALREADY_TRANSFERRED, oldEmployeeUsage, currentEmployeeUsage));
            return;
        }

        oldPaycheckUsage.setReasonForFreeCharge(ReasonForFreeChargeCode.UsageTransfer);

        PaycheckUsageHist paycheckUsageHist = new PaycheckUsageHist();
        paycheckUsageHist.setPaycheckUsage(oldPaycheckUsage);
        paycheckUsageHist.setCompany(oldPaycheckUsage.getCompany());
        paycheckUsageHist.setEmployeeUsage(currentEmployeeUsage);
        paycheckUsageHist.setNotes(currentEmployeeUsage.getEmployeeUsageFoundCode().getReason());
        Application.save(paycheckUsageHist);

        logger.info(String.format("%s for %s", UsageBillingEventCode.USAGE_TRANSFERRED, oldPaycheckUsage));

        Application.save(oldPaycheckUsage);
    }

    private static void decreaseEmployeeUsage(EmployeeUsage oldEmployeeUsage) {
        DomainEntitySet<Bill> bills = oldEmployeeUsage.getOpenBills();

        if(bills.size() > 1){
            logger.warn(String.format("%s for %s, so not attempting to do Decrease Employee Usage ", UsageBillingEventCode.FOUND_MULTIPLE_OPEN_BILLS_FOR_SAME_EMPLOYEE, oldEmployeeUsage));
            return;
        }

        for (Bill bill: bills) {
            // Reset any old Employee Usage to 0, as the Company will be charged as part of the new Employee Usage
            oldEmployeeUsage.setUsageCount(0);

            bill.decreaseUsageCount();
            Application.save(bill);
            // Decrease the Bill count as it is contributed to Usage Billing
            Application.save(oldEmployeeUsage);
        }
    }

    /**
     * Associate the list of all already billed Employee Usage with the Paycheck Usage for Back Tracking the Usage calculation
     *
     * @param currentEmployeeUsage
     * @param currentPaycheckUsage
     * @param closedBillEmployeeUsage
     */
    public static void checkAndAssociatePaycheckUsageWithAlreadyBilledEmployeeUsage(EmployeeUsage currentEmployeeUsage, PaycheckUsage currentPaycheckUsage, EmployeeUsage closedBillEmployeeUsage){
        PaycheckUsageHist paycheckUsageHist = new PaycheckUsageHist();
        paycheckUsageHist.setPaycheckUsage(currentPaycheckUsage);
        paycheckUsageHist.setCompany(currentPaycheckUsage.getCompany());
        paycheckUsageHist.setEmployeeUsage(closedBillEmployeeUsage);
        paycheckUsageHist.setNotes(currentEmployeeUsage.getEmployeeUsageFoundCode().getReason());
        Application.save(paycheckUsageHist);
    }

    public static void checkAndAssociatePaycheckUsageWithAlreadyBilledEmployeeUsage(DomainEntitySet<PaycheckUsage> alreadyBilledPaycheckUsageCollection, EmployeeUsage pEmployeeUsage, PaycheckUsage paycheckUsage) {
        if(alreadyBilledPaycheckUsageCollection.isEmpty()){
            return;
        }

        if(alreadyBilledPaycheckUsageCollection.size() > 1){
            logger.warn(String.format("%s - %s", UsageBillingEventCode.FOUND_MULTIPLE_ALREADY_BILLED_EMPLOYEE_USAGES, pEmployeeUsage));
        }

        DomainEntitySet<EmployeeUsage> alreadyPersistedEmployeeUsages = new DomainEntitySet<EmployeeUsage>();
        for (PaycheckUsage alreadyBilledPaycheckUsage: alreadyBilledPaycheckUsageCollection) {
            paycheckUsage.setReasonForFreeCharge(ReasonForFreeChargeCode.AlreadyBilled);
            for (PaycheckUsageHist alreadyBilledPaycheckUsageHist: alreadyBilledPaycheckUsage.getPaycheckUsageHistCollection()) {

                if(alreadyPersistedEmployeeUsages.contains(alreadyBilledPaycheckUsageHist.getEmployeeUsage())){
                    continue;
                }

                Application.save(new PaycheckUsageHist(paycheckUsage, alreadyBilledPaycheckUsageHist));
                alreadyPersistedEmployeeUsages.add(alreadyBilledPaycheckUsageHist.getEmployeeUsage());
            }
        }
    }
}
