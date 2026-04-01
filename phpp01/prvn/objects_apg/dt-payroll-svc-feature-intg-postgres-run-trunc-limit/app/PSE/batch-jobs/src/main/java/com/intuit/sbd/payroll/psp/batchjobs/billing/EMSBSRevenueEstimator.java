package com.intuit.sbd.payroll.psp.batchjobs.billing;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.hibernate.Query;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: YifengS302
 * Date: 10/12/12
 * Time: 11:16 AM
 * To change this template use File | Settings | File Templates.
 */

/* Inputs:
    Usage Period Start Month    e.g. 201211 (default is Nov 1, 2012)
    Usage Period End Month      e.g. 201211 (default is the last day of the current month)
    Usage Unit Price ($)        e.g. 1.5 (default)
    Free Charges To Remove      e.g. [,T,U,U|T] (Note: removing 'U' doesn't recalc if the paycheck qualifies for 'T' and thus the outcome will be slightly off)

   Outputs:
    # of Symphony licenses in total
    # of EINs which had usage
    # of Licenses which had usage
    Total Usage
    Total Revenue
 */
public class EMSBSRevenueEstimator {
    private static String[] USAGE_ENTITLEMENT_CODE = {
            "80000000-0000-0000-0000-000000000001",
            "80000000-0000-0000-0000-000000000002",
            "80000000-0000-0000-0000-000000000004",
            "80000000-0000-0000-0000-000000000005"
    };

    private SpcfCalendar mStartDate = SpcfCalendar.parse("yyyyMM", "201211") ;;
    private SpcfCalendar mEndDate = SpcfCalendar.getNow();
    private double mUnitPrice = 1.5;
    private EnumSet<ReasonForFreeChargeCode> mFreeChargesToRemove = EnumSet.noneOf(ReasonForFreeChargeCode.class);

    private int mLicenseNum = -1;
    private int mPeriodEINNum = -1;
    private int mPeriodLicenseNum = -1;
    private int mPeriodUsageNum = -1;
    private double mPeriodRevenue = -1;

    private void readParameters() throws Exception {
        BufferedReader bin = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Inputs:");

        System.out.print("Usage Period Start Month (e.g. 201211): ");
        String aLine = bin.readLine();
        if (aLine != null && !aLine.isEmpty()) {
            mStartDate = SpcfCalendar.parse("yyyyMM", aLine) ;
        }
        mStartDate = CalendarUtils.getFirstDayOfMonth(mStartDate);
        System.out.println(mStartDate.format("yyyyMMddHHmmss"));

        System.out.print("Usage Period End Month (e.g. 201211): ");
        aLine = bin.readLine();
        if (aLine != null && !aLine.isEmpty()) {
            mEndDate = SpcfCalendar.parse("yyyyMM", aLine);
        }
        mEndDate = CalendarUtils.getLastDayOfMonth(mEndDate);
        System.out.println(mEndDate.format("yyyyMMddHHmmss"));

        System.out.print("Usage Unit Price ($): ");
        aLine = bin.readLine();
        if (aLine != null && !aLine.isEmpty()) {
            mUnitPrice = Double.parseDouble(aLine);
        }
        System.out.println(mUnitPrice);

        System.out.print("Free charges to remove (e.g. T|U): ");
        aLine = bin.readLine();
        if (aLine != null) {
            setFreeChargeBits(mFreeChargesToRemove, aLine);
        }
        System.out.println(mFreeChargesToRemove.toString());

        System.out.flush();
    }

    private void outputReport() throws Exception {
        calcSymphonyLicenseNum();
        calcPeriodNumbers();

        System.out.println("\n\n");
        System.out.println("=========================== Report =====================================");

        System.out.print("\tTotal Symphony Licenses: ");
        System.out.println(mLicenseNum);

        System.out.print("\tTotal Number of EINs with Positive Usage during the Period: ");
        System.out.println(mPeriodEINNum);

        System.out.print("\tTotal Number of Licenses with Positive Usage during the Period: ");
        System.out.println(mPeriodLicenseNum);

        System.out.print("\tTotal Number of Usages during the Period: ");
        System.out.println(mPeriodUsageNum);

        System.out.print("\tTotal Estimated Revenue during the Period: ");
        System.out.println(String.format("$%.2f", mPeriodRevenue));

        System.out.println("======================== End of Report =================================");
    }

    public static void main(String[] args) {
        EMSBSRevenueEstimator anEstimator = new EMSBSRevenueEstimator();

        try {
            anEstimator.readParameters();
        } catch (Exception e) {
            System.out.println("Invalid parameter.");
            System.exit(-1);
        }

        try {
            anEstimator.outputReport();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void setFreeChargeBits(EnumSet<ReasonForFreeChargeCode> pFreeCharges, String pStr) {
        String[] elements = pStr.split("|");
        for (String anElement : elements) {
            if ("T".equals(anElement)) {
                pFreeCharges.add(ReasonForFreeChargeCode.Trial);
            } else if ("U".equals(anElement)) {
                pFreeCharges.add(ReasonForFreeChargeCode.Upgrade);
            }
        }
    }

    private void calcSymphonyLicenseNum() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT (DISTINCT LICENSE_NUMBER)\n")
           .append("FROM PSP_ENTITLEMENT E\n")
           .append("        JOIN\n")
           .append("    PSP_ENTITLEMENT_UNIT EU\n")
           .append("        ON E.ENTITLEMENT_SEQ = EU.ENTITLEMENT_FK\n")
           .append("WHERE E.ENTITLEMENT_STATE = 'Enabled'\n")
           .append("    AND E.ENTITLEMENT_CODE_FK IN\n")
           .append("        (");
        for (String aCode : USAGE_ENTITLEMENT_CODE) {
            sql.append("'").append(aCode).append("',");
        }
        sql.deleteCharAt(sql.length() - 1).append(")\n");
        sql.append("    AND EU.ENTITLEMENT_UNIT_STATUS = 'Activated'\n");

        try {
            Application.beginUnitOfWork();
            Query query = Application.getHibernateSession().createSQLQuery(sql.toString());
            //TODO: Testing for PG whether BigInteger can be casted in BigDecimal
            mLicenseNum = Application.isOracleDB() ? ((BigDecimal) query.list().get(0)).intValue() :  ((BigInteger) query.list().get(0)).intValue();
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    private EnumSet<ReasonForFreeChargeCode> convertFreeChargeCodeToEnumSet(ReasonForFreeChargeCode pReasonForFreeChargeCode) {
        EnumSet<ReasonForFreeChargeCode> aSet = EnumSet.noneOf(ReasonForFreeChargeCode.class);
        switch (pReasonForFreeChargeCode) {
            case Trial:
                aSet.add(ReasonForFreeChargeCode.Trial);
                break;
            case Upgrade:
                aSet.add(ReasonForFreeChargeCode.Upgrade);
                break;
            case TrialUpgrade:
                aSet.add(ReasonForFreeChargeCode.Trial);
                aSet.add(ReasonForFreeChargeCode.Upgrade);
                break;
        }
        return aSet;
    }

    private ReasonForFreeChargeCode convertEnumSetToFreeChargeCode(EnumSet<ReasonForFreeChargeCode> pSet) {
        if (pSet.contains(ReasonForFreeChargeCode.Trial)) {
            if (pSet.contains(ReasonForFreeChargeCode.Upgrade)) {
                return ReasonForFreeChargeCode.TrialUpgrade;
            } else {
                return ReasonForFreeChargeCode.Trial;
            }
        } else {
            if (pSet.contains(ReasonForFreeChargeCode.Upgrade)) {
                return ReasonForFreeChargeCode.Upgrade;
            } else {
                return ReasonForFreeChargeCode.None;
            }
        }
    }

    private void calcPeriodNumbers() {
        try {
            Application.beginUnitOfWork();
            DomainEntitySet<PaycheckUsage> paycheckUsages = Application.find(PaycheckUsage.class, PaycheckUsage.EmployeeUsage().UsagePeriod().StartDate().greaterOrEqualThan(mStartDate).And(PaycheckUsage.EmployeeUsage().UsagePeriod().EndDate().lessOrEqualThan(mEndDate)));

            // adjust free charges and collect employeeUsage
            DomainEntitySet<EmployeeUsage> employeeUsages = new DomainEntitySet<EmployeeUsage>();
            for (PaycheckUsage paycheckUsage : paycheckUsages) {
                EnumSet<ReasonForFreeChargeCode> originalSet = convertFreeChargeCodeToEnumSet(paycheckUsage.getReasonForFreeCharge());
                originalSet.removeAll(mFreeChargesToRemove);
                paycheckUsage.setReasonForFreeCharge(convertEnumSetToFreeChargeCode(originalSet));

                employeeUsages.add(paycheckUsage.getEmployeeUsage());
            }

            // recalc
            mPeriodUsageNum = 0;
            DomainEntitySet<CompanyUsage> periodCompanyUsage = new DomainEntitySet<CompanyUsage>();
            HashSet<String> periodLicenses = new HashSet<String>();
            for (EmployeeUsage employeeUsage : employeeUsages) {
                int usageCount = 0;
                for (PaycheckUsage paycheckUsage : employeeUsage.getPaycheckUsageCollection()) {
                    if (!paycheckUsage.isCancelled() && !paycheckUsage.isFree()) {
                        usageCount++;
                    }
                }

                if (usageCount > 0) {
                    mPeriodUsageNum++;
                    periodCompanyUsage.add(employeeUsage.getUsagePeriod().getCompanyUsage());
                    periodLicenses.add(employeeUsage.getUsagePeriod().getCompanyUsage().getLicenseId());
                }
            }

            // sum
            mPeriodEINNum = periodCompanyUsage.size();
            mPeriodLicenseNum = periodLicenses.size();
            mPeriodRevenue = mPeriodUsageNum * mUnitPrice;
        } finally {
            Application.rollbackUnitOfWork();
        }
    }
}
