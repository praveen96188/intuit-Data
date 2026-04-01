package com.intuit.ems.payroll.psp.clobPerformance.builder;

import com.intuit.sbd.payroll.psp.api.dtos.AddressDTO;
import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.PayrollFrequencyCode;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.*;

public class EmployeeBuilder {

    public static List<IEMP> generateNewEmployees(int pNumberOfEmployees,
                                                  List<IPITEM> pWageItems,
                                                  List<IPITEM> pAdjustments,
                                                  List<IPITEM> pTaxes) {
        List<IEMPOTHERTAX> iempothertaxes = new ArrayList<IEMPOTHERTAX>();
        for (IPITEM tax : pTaxes) {
            if (tax.getITAXITEM().getIOTHERTAX() != null) {
                IEMPOTHERTAX iempothertax = new IEMPOTHERTAX();
                iempothertax.setITAXLAWVER("9701");
                iempothertax.setIPITEMID(tax.getIPITEMID());
            }
        }

        Map<String, String> customFields = new HashMap<String, String>();
        customFields.put("Birthday", "10/24/1901");
        int mEmployeeId = 0;
        List<IEMP> iemps = new ArrayList<IEMP>();
        for (int i = 0; i < pNumberOfEmployees; i++) {
            IEMP iemp = generateEmployee(Integer.toString(++mEmployeeId),
                    false,
                    generateAddressInfo("Mr.",
                            "First" + mEmployeeId,
                            "M",
                            "Last" + mEmployeeId,
                            "Address 1" + mEmployeeId,
                            "Address 2" + mEmployeeId,
                            "Reno",
                            "NV",
                            "89511",
                            "CA",
                            "NV",
                            "201-123-1234",
                            "212-123-6543",
                            "QWS",
                            "this@that.com"),
                    "000-00-" + String.format("%04d", mEmployeeId),
                    Calendar.getInstance().getTime(),
                    null,
                    generateEmployeePayroll(PayrollFrequencyCode.Annually,
                            null,
                            false,
                            false,
                            generateWages(pWageItems),
                            generateAdjustments(pAdjustments)),
                    generateEmployeeTax(false,
                            2,
                            "SINGLE",
                            new SpcfMoney("10.10"),
                            Arrays.<String>asList("0", "1"),
                            "CA",
                            1,
                            "MARRIED",
                            new SpcfMoney("5.05"),
                            "12345",
                            Arrays.<String>asList("3", "4"),
                            "CA",
                            "CA",
                            true,
                            false,
                            true,
                            true,
                            true,
                            true,
                            iempothertaxes),
                    generateAccrual("YEARLY",
                            0.0,
                            5.25,
                            150,
                            true),
                    generateAccrual("YEARLY",
                            0.0,
                            7.11,
                            100,
                            true),
                    generateEmployeeDD(2),
                    generateWagePlan(1),
                    false,
                    "Bill Pay",
                    "REG",
                    "MALE",
                    customFields);
            iemps.add(iemp);
        }
        return iemps;
    }

    public static IEMP generateEmployee(String pEmployeeId,
                                        boolean pIsAccrualOnly,
                                        IADDRINFO pIADDRINFO,
                                        String pSSN,
                                        Date pHireDate,
                                        Date pReleaseDate,
                                        IPAYROLL pIPAYROLL,
                                        IEMPTAX pEMPTAX,
                                        ACCRUAL pSick,
                                        ACCRUAL pVacation,
                                        IEMPDD pIEMPDD,
                                        IEMPCOMPLIANCE pEmployeeCompliance,
                                        boolean pEmployeeInactive,
                                        String pBillPayAccount,
                                        String pEmployeeType,
                                        String pGender,
                                        Map<String, String> pCustomFields) {
        IEMP iemp = new IEMP();
        iemp.setIEMPID(pEmployeeId);
        if (pIsAccrualOnly) {
            iemp.setISICK(pSick);
            iemp.setIVAC(pVacation);
        } else {
            iemp.setIADDRINFO(pIADDRINFO);
            iemp.setIBILLPAYACCT(pBillPayAccount);
            iemp.setIDTHIRE(QBOFX.getDTTXResponse(pHireDate));
            iemp.setIDTRELEASE(pReleaseDate != null ? QBOFX.getDTTXResponse(pReleaseDate) : "");
            iemp.setIEMPCOMPLIANCE(pEmployeeCompliance);
            iemp.setIEMPDD(pIEMPDD);
            iemp.setIEMPGENDER(pGender);
            if (pIADDRINFO != null) {
                iemp.setIEMPNAME(String.format("%s, %s %s", pIADDRINFO.getILAST(), pIADDRINFO.getIFIRST(), pIADDRINFO.getIMI()));
            }
            iemp.setIEMPTAX(pEMPTAX);
            iemp.setIEMPTYPE(pEmployeeType);
            iemp.setIHASCUSTOMFLD(QBOFX.Y_N(pCustomFields.size() > 0));
            iemp.setIINACTIVE(QBOFX.Y_N(pEmployeeInactive));
            iemp.setIPAYROLL(pIPAYROLL);
            iemp.setISICK(pSick);
            iemp.setISSN(pSSN);
            iemp.setIVAC(pVacation);
            for (String name : pCustomFields.keySet()) {
                ICUSTOMFLD icustomfld = new ICUSTOMFLD();
                icustomfld.setIFLDNAME(name);
                icustomfld.setIFLDVALUE(pCustomFields.get(name));
                iemp.getICUSTOMFLD().add(icustomfld);
            }
        }

        return iemp;
    }

    public static IADDRINFO generateAddressInfo(String pTitle,
                                                String pFirstName,
                                                String pMiddleInitial,
                                                String pLastName,
                                                String pAddressLine1,
                                                String pAddressLine2,
                                                String pCity,
                                                String pState,
                                                String pZipCode,
                                                String pStateWorked,
                                                String pStateLived,
                                                String pPhone,
                                                String pAltPhone,
                                                String pInitials,
                                                String pEmail) {
        IADDRINFO iaddrinfo = new IADDRINFO();
        iaddrinfo.setIADDR1(pAddressLine1);
        iaddrinfo.setIADDR2(pAddressLine2);
        iaddrinfo.setIALTPHONE(pAltPhone);
        iaddrinfo.setICITY(pCity);
        iaddrinfo.setIEMAIL(pEmail);
        iaddrinfo.setIFIRST(pFirstName);
        iaddrinfo.setIINITIALS(pInitials);
        iaddrinfo.setILAST(pLastName);
        iaddrinfo.setIMI(pMiddleInitial);
        iaddrinfo.setIPHONE(pPhone);
        iaddrinfo.setIPOSTALCODE(pZipCode);
        iaddrinfo.setIPRINTASNAME(pFirstName + pMiddleInitial + pLastName);
        iaddrinfo.setISTATE(pState);
        iaddrinfo.setISTATELIVED(pStateLived);
        iaddrinfo.setISTATEWORKED(pStateWorked);
        iaddrinfo.setITITLE(pTitle);
        return iaddrinfo;
    }

    public static IPAYROLL generateEmployeePayroll(PayrollFrequencyCode pPayPeriod,
                                                   String pClass,
                                                   boolean pUseTime,
                                                   boolean pHasRetirementPlan,
                                                   List<IWAGE> pWages,
                                                   List<IADJ> pAdjustments) {
        IPAYROLL ipayroll = new IPAYROLL();
        ipayroll.setICLASS(pClass);
        if (pPayPeriod != null) {
            ipayroll.setIPAYPD(QBOFX.mapPayrollFrequencyToOFXValue(pPayPeriod));
        }
        ipayroll.setIPPLANOVERRIDE(QBOFX.Y_N(pHasRetirementPlan));
        ipayroll.setIUSETIME(QBOFX.Y_N(pUseTime));
        ipayroll.getIADJ().addAll(pAdjustments);
        ipayroll.getIWAGE().addAll(pWages);
        return ipayroll;
    }

    public static IWAGE generateWage(String pPayrollItemId,
                                     String pPayrollItemName,
                                     String pRate) {
        IWAGE iwage = new IWAGE();
        iwage.setIPITEMID(pPayrollItemId);
        iwage.setIPITEMNAME(pPayrollItemName);
        iwage.setIRATE(pRate);
        return iwage;
    }

    public static IADJ generateAdjustment(String pPayrollItemId,
                                          String pAmount,
                                          String pLimit) {
        IADJ iadj = new IADJ();
        iadj.setIPITEMID(pPayrollItemId);
        iadj.setIAMT(pAmount);
        iadj.setILIMIT(pLimit);
        return iadj;
    }

    public static IEMPTAX generateEmployeeTax(boolean pIsDeceased,
                                              int pFederalAllowances,
                                              String pFederalFilingStatus,
                                              SpcfMoney pFederalExtraWithholding,
                                              List<String> pFITTableMiscData,
                                              String pSITState,
                                              int pStateAllowances,
                                              String pStateFilingStatus,
                                              SpcfMoney pStateExtraWithholding,
                                              String pSITTaxLawVersion,
                                              List<String> pSITTableMiscData,
                                              String pSDIState,
                                              String pSUIState,
                                              boolean pEnforceSubjectTo,
                                              boolean pQualifiesForAEIC,
                                              boolean pSubjectToFIT,
                                              boolean pSubjectToFUTA,
                                              boolean pSubjectToMED,
                                              boolean pSubjectToSS,
                                              List<IEMPOTHERTAX> pOtherTaxes) {
        IEMPTAX iemptax = new IEMPTAX();
        iemptax.setIDECEASED(QBOFX.Y_N(pIsDeceased));

        IEMPFIT iempfit = new IEMPFIT();
        iempfit.setIALLOWANCES(Integer.toString(pFederalAllowances));
        iempfit.setIEXTRAWITHHOLD("$" + pFederalExtraWithholding.toString());
        iempfit.setIFEDFILESTATUS(pFederalFilingStatus);
        for (String data : pFITTableMiscData) {
            iempfit.getITAXTBLMISCDATA().add(data);
        }
        iemptax.setIEMPFIT(iempfit);

        IEMPSIT iempsit = new IEMPSIT();
        iempsit.setIALLOWANCES(Integer.toString(pStateAllowances));
        iempsit.setIEXTRAWITHHOLD("$" + pStateExtraWithholding.toString());
        iempsit.setISTATE(pSITState);
        iempsit.setISTATEFILESTATUS(pStateFilingStatus);
        iempsit.setITAXLAWVER(pSITTaxLawVersion);
        for (String data : pSITTableMiscData) {
            iempsit.getITAXTBLMISCDATA().add(data);
        }
        iemptax.setIEMPSIT(iempsit);

        IEMPSDI iempsdi = new IEMPSDI();
        iempsdi.setISTATE(pSDIState);
        iemptax.setIEMPSDI(iempsdi);

        IEMPSUI iempsui = new IEMPSUI();
        iempsui.setISTATE(pSUIState);
        iemptax.setIEMPSUI(iempsui);

        iemptax.setIENFORCESUBJECTTO(QBOFX.Y_N(pEnforceSubjectTo));
        iemptax.setIQUALFORAEIC(QBOFX.Y_N(pQualifiesForAEIC));
        iemptax.setISUBJTOFIT(QBOFX.Y_N(pSubjectToFIT));
        iemptax.setISUBJTOFUTA(QBOFX.Y_N(pSubjectToFUTA));
        iemptax.setISUBJTOMCARE(QBOFX.Y_N(pSubjectToMED));
        iemptax.setISUBJTOSS(QBOFX.Y_N(pSubjectToSS));
        iemptax.getIEMPOTHERTAX().addAll(pOtherTaxes);

        return iemptax;
    }

    public static List<IWAGE> generateWages(List<IPITEM> pPayrollItems) {
        List<IWAGE> iwages = new ArrayList<IWAGE>();
        for (IPITEM payrollItem : pPayrollItems) {
            iwages.add(generateWage(payrollItem.getIPITEMID(),
                    payrollItem.getIPITEMNAME(),
                    "$10.00"));
        }
        return iwages;
    }

    public static List<IADJ> generateAdjustments(List<IPITEM> pPayrollItems) {
        List<IADJ> iadjs = new ArrayList<IADJ>();
        for (IPITEM payrollItem : pPayrollItems) {
            iadjs.add(generateAdjustment(payrollItem.getIPITEMID(),
                    "$10.00",
                    "$1000.00"));
        }
        return iadjs;
    }

    private static ACCRUAL generateAccrual(String pAccrualPeriod,
                                           double pHours,
                                           double pHoursPerPeriod,
                                           double pMaxHours,
                                           boolean pNewYearReset) {
        ACCRUAL accrual = new ACCRUAL();
        accrual.setIACCRUALPD(pAccrualPeriod);
        accrual.setIHRS(Double.toString(pHours));
        accrual.setIHRSPERPD(Double.toString(pHoursPerPeriod));
        accrual.setIMAXHRS(Double.toString(pMaxHours));
        accrual.setINEWYRRESET(QBOFX.Y_N(pNewYearReset));
        return accrual;
    }

    private static IEMPCOMPLIANCE generateWagePlan(int numberOfPlans) {
        IEMPCOMPLIANCE iempcompliance = new IEMPCOMPLIANCE();
        for (int i = 0; i < numberOfPlans; i++) {
            ISETTING isetting = new ISETTING();
            isetting.setIDESCRIPTION("blah blah " + i);
            isetting.setIDOMAIN("WorkOrLiveState");
            isetting.setINAME("WPC");
            isetting.setIRULESVERSION("R" + i);
            isetting.setISTATE("AZ");
            isetting.setIVALUE("" + i);
            iempcompliance.getISETTING().add(isetting);
        }
        return iempcompliance;
    }

    private static IEMPDD generateEmployeeDD(int numberOfAccounts) {
        IEMPDD iempdd = new IEMPDD();
        if (numberOfAccounts > 0) {
            iempdd.setIUSEDD(QBOFX.Y_N(true));
            for (int i = 0; i < numberOfAccounts; i++) {
                iempdd.getIDDACCT().add(generateDDAccount(i));
            }
        } else {
            iempdd.setIUSEDD(QBOFX.Y_N(false));
        }
        return iempdd;
    }

    public static IDDACCT generateDDAccount(int i) {
        IDDACCT iddacct = new IDDACCT();
        iddacct.setIACCTNAME("bank " + i);
        iddacct.setIAMT((i % 2 == 0) ? "$" + i + ".00" : i + ".0" + "%");
        BANKACCT bankacct = new BANKACCT();
        bankacct.setACCTID("12345678" + i);
        bankacct.setBANKID("111000025");
        bankacct.setACCTTYPE(i % 2 == 0 ? "CHECKING" : "SAVINGS");
        iddacct.setBANKACCTTO(bankacct);
        return iddacct;
    }

    public static AddressDTO createAddress(String linePrefix) {
        if (linePrefix != null && linePrefix.length() > 0)
            linePrefix += "_";

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressLine1(linePrefix + "AddressLine1");
        addressDTO.setAddressLine2(linePrefix + "AddressLine2");
        addressDTO.setAddressLine3(linePrefix + "AddressLine3");
        addressDTO.setCity("Ridgewood");
        addressDTO.setState("NJ");
        addressDTO.setCountry("USA");
        addressDTO.setZipCode("07450");
        addressDTO.setZipCodeExtension("4444");

        return addressDTO;
    }

    public static IPAYROLLTRNRQ generatePayrollRequest(boolean pTaxReady,
                                                       Date pQuarterToStart,
                                                       List<IEMP> pNewEmployees,
                                                       List<IEMP> pEmployeeUpdates,
                                                       List<String> pEmployeeDeletes,
                                                       List<IPITEM> pNewPayrollItems,
                                                       List<IPITEM> pPayrollItemUpdates,
                                                       List<String> pPayrollItemDeletes,
                                                       List<IPAYROLLTX> pNewPayrollTransactions,
                                                       List<IPAYROLLTX> pPayrollTransactionUpdates,
                                                       List<String> pPayrollTransactionDeletes,
                                                       List<IPAYROLLRUN> pPayrolls) {
        IPAYROLLTRNRQ ipayrolltrnrq = new IPAYROLLTRNRQ();
        ipayrolltrnrq.setTRNUID(UUID.randomUUID().toString());

        IPAYROLLRQ ipayrollrq = new IPAYROLLRQ();

        if (pTaxReady && pQuarterToStart != null) {
            ICOINFOMOD icoinfomod = new ICOINFOMOD();
            icoinfomod.setITAXREADY(QBOFX.Y_N(pTaxReady));
            icoinfomod.setIDTFILEQTRSTART(QBOFX.getDTTXResponse(pQuarterToStart));
            ipayrollrq.setICOINFOMOD(icoinfomod);
        }

        // employees
        if (pNewEmployees != null) {
            ipayrollrq.getIEMP().addAll(pNewEmployees);
        }
        if (pEmployeeUpdates != null) {
            ipayrollrq.getIEMPMOD().addAll(pEmployeeUpdates);
        }
        if (pEmployeeDeletes != null) {
            ipayrollrq.getIEMPDELID().addAll(pEmployeeDeletes);
        }

        // payroll items
        if (pNewPayrollItems != null) {
            ipayrollrq.getIPITEM().addAll(pNewPayrollItems);
        }
        if (pPayrollItemUpdates != null) {
            ipayrollrq.getIPITEMMOD().addAll(pPayrollItemUpdates);
        }
        if (pPayrollItemDeletes != null) {
            ipayrollrq.getIPITEMDELID().addAll(pPayrollItemDeletes);
        }

        // payroll transactions
        if (pNewPayrollTransactions != null) {
            ipayrollrq.getIPAYROLLTX().addAll(pNewPayrollTransactions);
        }
        if (pPayrollTransactionUpdates != null) {
            ipayrollrq.getIPAYROLLTXMOD().addAll(pPayrollTransactionUpdates);
        }
        if (pPayrollTransactionDeletes != null) {
            ipayrollrq.getIPAYROLLTXDELID().addAll(pPayrollTransactionDeletes);
        }

        // payrolls
        if (pPayrolls != null) {
            ipayrollrq.getIPAYROLLRUN().addAll(pPayrolls);
        }

        ipayrolltrnrq.setIPAYROLLRQ(ipayrollrq);
        return ipayrolltrnrq;
    }

    public static IPAYROLLMSGSRQV1 generatePayrollMessage(Company pCompany, boolean pRejectIfMissing, IPAYROLLTRNRQ pIPAYROLLTRNRQ) {
        return generatePayrollMessage(Long.toString(pCompany.getCurrentToken()), pRejectIfMissing, pIPAYROLLTRNRQ);
    }

    public static IPAYROLLMSGSRQV1 generatePayrollMessage(String pToken, boolean pRejectIfMissing, IPAYROLLTRNRQ pIPAYROLLTRNRQ) {
        IPAYROLLMSGSRQV1 ipayrollmsgsrqv1 = new IPAYROLLMSGSRQV1();
        IPAYROLLUPDATERQ ipayrollupdaterq = new IPAYROLLUPDATERQ();
        ipayrollupdaterq.setTOKEN(pToken);
        ipayrollupdaterq.setREJECTIFMISSING(QBOFX.Y_N(pRejectIfMissing));
        ipayrollupdaterq.setIPAYROLLTRNRQ(pIPAYROLLTRNRQ);
        ipayrollmsgsrqv1.setIPAYROLLUPDATERQ(ipayrollupdaterq);
        return ipayrollmsgsrqv1;
    }
}
