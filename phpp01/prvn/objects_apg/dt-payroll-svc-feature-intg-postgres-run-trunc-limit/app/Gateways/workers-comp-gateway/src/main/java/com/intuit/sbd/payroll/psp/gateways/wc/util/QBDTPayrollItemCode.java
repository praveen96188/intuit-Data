package com.intuit.sbd.payroll.psp.gateways.wc.util;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: Sriram Nutakki
 * Date created: 5/15/13
 */
public enum QBDTPayrollItemCode {

        NONE(0),
        LAW_MINIMUM(1),
        LAW_AEIC(143),
        LAW_AKSUIE(135),
        LAW_ALESA(144),
        LAW_AK(2),
        LAW_AZ(5),
        LAW_AZJTT(179),
        LAW_AZSUI(86),
        LAW_CA(6),
        LAW_CAETT(142),
        LAW_CASDI(67),
        LAW_CASUI(87),
        LAW_INSWH(17),
        LAW_INCOUNTY(59),
        LAW_COBRA(196),
        LAW_COST(153),
        LAW_FICE(61),
        LAW_FICR(62),
        LAW_FUTAN(65),
        LAW_FUTAY(66),
        LAW_GAADA(154),
        LAW_HIETA(155),
        LAW_HISDI(68),
        LAW_MEDE(63),
        LAW_MEDR(64),
        LAW_MEDEAT(200),
        LAW_MTAFT(147),
        LAW_NJHCSFE(170),
        LAW_NJSDI(69),
        LAW_NJSDIE(73),
        LAW_NJSUIE(136),
        LAW_NJWDF(167),
        LAW_NJWDFE(168),
        LAW_NVCEP(159),
        LAW_PASUIE(137),
        LAW_RIJDF(160),
        LAW_SD(44),
        LAW_SCCA(161),
        LAW_SDIF(162),
        LAW_US(1),
        LAW_MAXIMUM(10000),
        UNSUPPORTED(911000),
        UNSUPPORTED_ADDITION(911001),
        ADOPTIONBENEFIT(1000065),
        ALLOCATEDTIPS(1000048),
        BONUS(1000005),
        CASHADVANCE(1000044),
        CASHADVANCEREPAYMENT(1000043),
        CASHTIPS(1000041),
        CASHTIPSOUT(1000042),
        CHARITYDONATIONS(1000052),
        COCUSTOMTAX(1000017),
        COMMISSION(1000025),
        DENTALCOPAIDINSURANCE(1000059),
        DENTALINSURANCE(1000011),
        DEPCARECOPAIDINSURANCE(1000063),
        DOUBLETIMEHOURLY(1000003),
        EMPCUSTOMTAX(1000016),
        FIVE01C(1000066),
        FOUR01K(1000006),
        FOUR01KMATCH(1000014),
        FOUR03B(1000035),
        FOUR03BMATCH(1000038),
        FOUR08K6SEP(1000036),
        FOUR08K6SEPMATCH(1000039),
        FOUR57PLAN(1000067),
        FOUR57PLANDIST(1000068),
        HEALTHCOPAIDINSURANCE(1000060),
        HEALTHINSURANCE(1000010),
        HSACOMPANY(1000057),   //Company pre-tax contribution. (HSA)
        HSAEMPLOYEE(1000058),  //Employee taxable contribution. (HSA)
        MEDFSACOPAIDINSURANCE(1000064),
        MILEAGEREIMBURSEMENT(1000002),
        NONQUALIFIEDPLAN(1000069),
        NONTAXABLEGROUPTERMLIFE(1000056),
        NONTAXABLESICK(1000070),
        OTHERCOPAID(1000072),
        OTHERCOPAIDINS(1000034),
        OTHERDEDUCTION(1000073),
        OTHERINSURANCE(1000028),
        OTHERMOVINGEXPENSE(1000076),
        OTHERMOVINGEXPENSEOUT(1000077),
        OTHERMOVINGEXPENSEREIMB(1000075),
        OTHERNETDEDUCTION(1000033),
        OTHERPAYMENT(1000074),
        OTHERREIMBURSEMENT(1000045),
        OVERTIMEHOURLY(1000026),
        PAYCHECKTIPS(1000004),
        PIECEWORK(1000015),
        QUALIFIEDMOVINGEXPENSE(1000071),
        REGULARHOURLY(1000012),
        REGULARSALARY(1000013),
        SCORPSMEDICALBENEFIT(1000030),
        SEC125DENTAL(1000007),
        SEC125DEPENDENTCARE(1000031),
        SEC125HEALTH(1000001),
        SEC125MEDFSA(1000032),
        SEC125OTHER(1000009),
        SEC125VISION(1000008),
        SICKACCRUE(1000020),
        SICKDISBURSE(1000021),
        SICKPAYOUT(1000023),
        SIMPLEIRA(1000037),
        SIMPLEIRAMATCH(1000040),
        TAXABLEFRINGEBENEFIT(1000046),
        TAXABLEFRINGEBENEFITOUT(1000047),
        TAXABLEGROUPTERMLIFE(1000029),
        UNIONDUES(1000051),
        UNIONDUESHOURS(1000053),
        UNPAIDTIMEOFFDISBURSE(1000022),
        VACATIONACCRUE(1000018),
        VACATIONDISBURSE(1000019),
        VACATIONPAYOUT(1000024),
        VISIONCOPAIDINSURANCE(1000061),
        VISIONINSURANCE(1000027),
        WAGEGARNISHMENT(1000050),
        ROTH401K(1000084),
        ROTH403B(1000085),
        HSACOMPANYPO(1000087), //Company taxable contribution. (HSA)
        HSAEMPLOYEEPR(1000088), //Employee pre-tax contribution. (HSA)
        HSACOMPANYPR(1000089),  //Company pre-tax contribution. (HSA)
        ROTHFOUR57PLAN(1000090),  //Roth 457 Plan
        FOUR57MATCH(1000091);      //457B plan company match

    private static Map<Long, QBDTPayrollItemCode> eMap = new HashMap<Long, QBDTPayrollItemCode>() ;
    private int itemCode;

    static {
        for (QBDTPayrollItemCode v :  EnumSet.allOf(QBDTPayrollItemCode.class)) {
            eMap.put( new Long( v.itemCode), v);
        }
    }

    private QBDTPayrollItemCode(int itemCode) {
        this.itemCode = itemCode;
    }

    public static QBDTPayrollItemCode getItemByCode(long code) {

        return eMap.get(code);
    }
}
