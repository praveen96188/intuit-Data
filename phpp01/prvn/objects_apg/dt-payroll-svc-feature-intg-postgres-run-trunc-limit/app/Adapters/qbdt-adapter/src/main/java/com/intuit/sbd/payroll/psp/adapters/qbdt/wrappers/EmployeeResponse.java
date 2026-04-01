package com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers;

import com.intuit.sbd.payroll.psp.common.ofx.response.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.QbdtEmployeeSeasonal;
import com.intuit.sbd.payroll.psp.util.OFXAPPVERObject;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 19, 2011
 * Time: 1:24:15 PM
 */
public class EmployeeResponse {
    private IEMP mIEMP;
    
    public IEMP getIEMP() {
        return mIEMP;
    }

    public EmployeeResponse(Employee employee, OFXAPPVERObject pAppVerion) {
        mIEMP = new IEMP();
        mIEMP.setIEMPID(employee.getSourceEmployeeId());

        QbdtEmployeeInfo qbdtEmployeeInfo = employee.getQbdtEmployeeInfo();
        if(qbdtEmployeeInfo != null) {
            mIEMP.setIBILLPAYACCT(qbdtEmployeeInfo.getBillPayAccount());
            mIEMP.setIEMPTYPE(QBOFX.convertNullToOFXString(qbdtEmployeeInfo.getEmployeeType()));
            mIEMP.setIEMPDD(new IEMPDD());
            mIEMP.getIEMPDD().setIUSEDD(QBOFX.Y_N(qbdtEmployeeInfo.getUseDD()));
            if (pAppVerion.listIdLoopBackSupported() && qbdtEmployeeInfo.getListId() != null) {
                mIEMP.setIQBUNIQUEID(qbdtEmployeeInfo.getListId());
            }

            if(qbdtEmployeeInfo.getEmployeeSeasonal() != null) {
                mIEMP.setIEMPSEASONAL(String.valueOf(qbdtEmployeeInfo.getEmployeeSeasonal()));
            }
        }

        mIEMP.setIHASCUSTOMFLD(QBOFX.Y_N(false));
        mIEMP.setIDTHIRE(QBOFX.convertToOFXDate(employee.getHireDate()));
        mIEMP.setIDTRELEASE(QBOFX.convertToOFXDate(employee.getTerminationDate()));
        mIEMP.setIBIRTHDATE(QBOFX.convertToOFXDate(employee.getBirthDate()));
        mIEMP.setIEMPGENDER(QBOFX.mapGender(employee.getGenderCd()));
        mIEMP.setIEMPNAME(employee.getFullName());
        mIEMP.setIINACTIVE(QBOFX.Y_N(employee.getStatusCd() == EmployeeStatus.Inactive));
        if(employee.getTaxId() != null) {
            String formattedSSN = employee.getTaxId().substring(0, 3) + "-" + employee.getTaxId().substring(3, 5) + "-" + employee.getTaxId().substring(5, 9);
            mIEMP.setISSN(formattedSSN);
        }

        populateAddressInfo(employee, qbdtEmployeeInfo);
        populateAccruals(employee);
        populateComplianceInfo(employee);
        populatePayrollInfo(employee);
        populateTaxInfo(employee, qbdtEmployeeInfo);
    }

    private void populateComplianceInfo(Employee pEmployee) {
        if(pEmployee.getEmployeeWagePlanCollection().size() > 0) {
            IEMPCOMPLIANCE iempcompliance = new IEMPCOMPLIANCE();
            for (EmployeeWagePlan employeeWagePlan : pEmployee.getEmployeeWagePlanCollection()) {
                ISETTING isetting = new ISETTING();
                isetting.setIDESCRIPTION(QBOFX.convertNullToOFXString(employeeWagePlan.getDescription()));
                isetting.setIDOMAIN(QBOFX.convertNullToOFXString(employeeWagePlan.getWagePlanDomain()));
                isetting.setINAME(QBOFX.convertNullToOFXString(employeeWagePlan.getName()));
                isetting.setIRULESVERSION(QBOFX.convertNullToOFXString(employeeWagePlan.getRulesVersion()));
                isetting.setISTATE(QBOFX.convertNullToOFXString(employeeWagePlan.getState()));
                isetting.setIVALUE(QBOFX.convertNullToOFXString(employeeWagePlan.getWagePlanValue()));
                iempcompliance.getISETTING().add(isetting);
            }
            mIEMP.setIEMPCOMPLIANCE(iempcompliance);
        }
    }

    public void addBankAccount(EmployeeBankAccount pEmployeeBankAccount) {
        if(mIEMP.getIEMPDD() == null) {
            mIEMP.setIEMPDD(new IEMPDD());
        }
        IEMPDD iempdd = mIEMP.getIEMPDD();

        IDDACCT iddacct = new IDDACCT();
        if(pEmployeeBankAccount.getBankAccount() != null) {
            BANKACCT bankacct = new BANKACCT();
            bankacct.setACCTID(QBOFX.convertNullToOFXString(pEmployeeBankAccount.getBankAccount().getAccountNumber()));
            bankacct.setACCTTYPE(QBOFX.mapOFXBankAccountType(pEmployeeBankAccount.getBankAccount().getAccountTypeCd()));
            bankacct.setBANKID(QBOFX.convertNullToOFXString(pEmployeeBankAccount.getBankAccount().getRoutingNumber()));
            iddacct.setBANKACCTTO(bankacct);
            iddacct.setIACCTNAME(QBOFX.convertNullToOFXString(pEmployeeBankAccount.getBankAccount().getBankName()));
        }
        iddacct.setIAMT(QBOFX.mapNumericTypeToString(pEmployeeBankAccount.getAmountType(), pEmployeeBankAccount.getAmount()));
        iempdd.getIDDACCT().add(iddacct);
    }

    public void populateTaxInfo(Employee pEmployee, QbdtEmployeeInfo pQbdtEmployeeInfo) {
        IEMPTAX iemptax = new IEMPTAX();
        iemptax.setIDECEASED(QBOFX.Y_N(pEmployee.getIsDeceased()));
        iemptax.setIQUALFORAEIC(QBOFX.Y_N(pEmployee.getQualifiesForAeic()));

        if(pQbdtEmployeeInfo != null) {
            iemptax.setIENFORCESUBJECTTO(QBOFX.Y_N(pQbdtEmployeeInfo.getEnforceSubjectTo()));
        }

        IEMPFIT iempfit;
        if(pEmployee.getFedFilingStatus() != null) {
            iempfit = new IEMPFIT();
            iempfit.setIALLOWANCES(Integer.toString(pEmployee.getFedAllowances()));
            if(pEmployee.getFedExtraWithholding() != null) {
                iempfit.setIEXTRAWITHHOLD("$" + pEmployee.getFedExtraWithholding().toString());
            } else {
                iempfit.setIEXTRAWITHHOLD(QBOFX.NULL);
            }
            if(pEmployee.getFedClaimDependents() != null){
                iempfit.setICLAIMDEPENDENTS("$" + pEmployee.getFedClaimDependents().toString());
            } else {
                iempfit.setICLAIMDEPENDENTS(QBOFX.NULL);
            }
            if(pEmployee.getFedOtherIncome() != null){
                iempfit.setIOTHERINCOME("$" + pEmployee.getFedOtherIncome().toString());
            } else {
                iempfit.setIOTHERINCOME(QBOFX.NULL);
            }
            if(pEmployee.getFedDeductions() != null){
                iempfit.setIDEDUCTIONS("$" + pEmployee.getFedDeductions().toString());
            } else {
                iempfit.setIDEDUCTIONS(QBOFX.NULL);
            }
            iempfit.setIMULTIPLEJOBS(QBOFX.Y_N(pEmployee.getFedMultipleJobs()));
            if(pEmployee.getFedW4EmployeePref() != null) {
                iempfit.setIFEDW4EMPLOYEEPREF(pEmployee.getFedW4EmployeePref());
            } else {
                iempfit.setIFEDW4EMPLOYEEPREF("2019ORBEFORE");
            }
            iempfit.setIFEDFILESTATUS(QBOFX.convertNullToOFXString(pEmployee.getFedFilingStatus()));
            iemptax.setIEMPFIT(iempfit);
        }

        // init subject to
        iemptax.setISUBJTOFIT(QBOFX.Y_N(false));
        iemptax.setISUBJTOFUTA(QBOFX.Y_N(false));
        iemptax.setISUBJTOMCARE(QBOFX.Y_N(false));
        iemptax.setISUBJTOSS(QBOFX.Y_N(false));
        iemptax.setIQUALFORAEIC(QBOFX.Y_N(false));

        mIEMP.setIEMPTAX(iemptax);
    }

    public void addTax(EmployeeTax pEmployeeTax) {
        IEMPTAX iemptax = mIEMP.getIEMPTAX();
        IEMPFIT iempfit = iemptax.getIEMPFIT();

        switch (pEmployeeTax.getTaxType()) {
            case FIT:
                iemptax.setISUBJTOFIT(QBOFX.Y_N(pEmployeeTax.getSubjectTo()));
                if(iempfit != null) {
                    iempfit.setITAXLAWVER(pEmployeeTax.getTaxLawVersion());
                    for (TaxTableMiscData taxTableMiscData : pEmployeeTax.getTaxTableMiscDataCollection().sort(TaxTableMiscData.MiscDataOrder())) {
                        iempfit.getITAXTBLMISCDATA().add(taxTableMiscData.getValue());
                    }
                }
                break;
            case FICA:
                iemptax.setISUBJTOSS(QBOFX.Y_N(pEmployeeTax.getSubjectTo()));
                break;
            case MED:
                iemptax.setISUBJTOMCARE(QBOFX.Y_N(pEmployeeTax.getSubjectTo()));
                break;
            case FUTA:
                iemptax.setISUBJTOFUTA(QBOFX.Y_N(pEmployeeTax.getSubjectTo()));
                break;


            case SIT:
                IEMPSIT iempsit = new IEMPSIT();
                iempsit.setIALLOWANCES(Integer.toString(pEmployeeTax.getAllowances()));
                iempsit.setIEXTRAWITHHOLD(QBOFX.mapNumericTypeToString(pEmployeeTax.getExtraWithholdingType(), pEmployeeTax.getExtraWithholding()));
                iempsit.setISTATE(pEmployeeTax.getState());
                iempsit.setISTATEFILESTATUS(pEmployeeTax.getFilingStatus());
                iempsit.setITAXLAWVER(pEmployeeTax.getTaxLawVersion());
                for (TaxTableMiscData taxTableMiscData : pEmployeeTax.getTaxTableMiscDataCollection().sort(TaxTableMiscData.MiscDataOrder())) {
                    iempsit.getITAXTBLMISCDATA().add(taxTableMiscData.getValue());
                }
                iemptax.setIEMPSIT(iempsit);
                break;
            case SDI:
                IEMPSDI iempsdi = new IEMPSDI();
                iempsdi.setISTATE(pEmployeeTax.getState());
                iempsdi.setITAXLAWVER(pEmployeeTax.getTaxLawVersion());
                iemptax.setIEMPSDI(iempsdi);
                break;
            case SUI:
                IEMPSUI iempsui = new IEMPSUI();
                iempsui.setISTATE(pEmployeeTax.getState());
                iemptax.setIEMPSUI(iempsui);
                break;
            case Other:
                IEMPOTHERTAX iempothertax = new IEMPOTHERTAX();
                try{
                CompanyLaw companyLaw = pEmployeeTax.getCompanyLaw().getLatestCompanyLaw();
                iempothertax.setIPITEMID(companyLaw.getSourceId());
                iempothertax.setITAXLAWVER(QBOFX.convertNullToOFXString(pEmployeeTax.getTaxLawVersion()));
                iempothertax.setIW2NAME(QBOFX.convertNullToOFXString(pEmployeeTax.getW2Name()));
                for (TaxTableMiscData taxTableMiscData : pEmployeeTax.getTaxTableMiscDataCollection().sort(TaxTableMiscData.MiscDataOrder())) {
                    iempothertax.getITAXTBLMISCDATA().add(taxTableMiscData.getValue());
                }
                iemptax.getIEMPOTHERTAX().add(iempothertax);
                }catch(NullPointerException ne){
                    throw new RuntimeException("Error while adding OTHER TAX to employee response.Company has invalid source id for tax item.  ");
                }
                break;
        }
    }

    private void populateAddressInfo(Employee pEmployee, QbdtEmployeeInfo pQbdtEmployeeInfo) {
        IADDRINFO iaddrinfo = new IADDRINFO();

        iaddrinfo.setIEMAIL(pEmployee.getEmail());
        iaddrinfo.setIFIRST(QBOFX.convertNullToOFXString(pEmployee.getFirstName()));
        iaddrinfo.setILAST(QBOFX.convertNullToOFXString(pEmployee.getLastName()));
        iaddrinfo.setIMI(QBOFX.convertNullToOFXString(pEmployee.getMiddleName()));
        iaddrinfo.setIPHONE(QBOFX.convertNullToOFXString(pEmployee.getPhone()));
        iaddrinfo.setISTATELIVED(QBOFX.convertNullToOFXString(pEmployee.getLiveState()));
        iaddrinfo.setISTATEWORKED(QBOFX.convertNullToOFXString(pEmployee.getWorkState()));

        if(pQbdtEmployeeInfo != null) {
            iaddrinfo.setIALTPHONE(QBOFX.convertNullToOFXString(pQbdtEmployeeInfo.getAltPhone()));
            iaddrinfo.setIINITIALS(pQbdtEmployeeInfo.getInitials());
            iaddrinfo.setIPRINTASNAME(QBOFX.convertNullToOFXString(pQbdtEmployeeInfo.getPrintAsName()));
            iaddrinfo.setITITLE(QBOFX.convertNullToOFXString(pQbdtEmployeeInfo.getTitle()));
        }

        if(pEmployee.getMailingAddress() != null) {
            Address address = pEmployee.getMailingAddress();
            iaddrinfo.setIADDR1(QBOFX.convertNullToOFXString(address.getAddressLine1()));
            iaddrinfo.setIADDR2(QBOFX.convertNullToOFXString(address.getAddressLine2()));
            iaddrinfo.setICITY(QBOFX.convertNullToOFXString(address.getCity()));
            iaddrinfo.setISTATE(QBOFX.convertNullToOFXString(address.getState()));
            iaddrinfo.setIPOSTALCODE(QBOFX.convertNullToOFXString(address.getFullZipCode()));
        }

        mIEMP.setIADDRINFO(iaddrinfo);
    }

    private void populatePayrollInfo(Employee pEmployee) {
        IPAYROLL ipayroll = new IPAYROLL();

        if(pEmployee.getPayPeriod() != null) {
            ipayroll.setIPAYPD(QBOFX.mapPayrollFrequencyToOFXValue(pEmployee.getPayPeriod()));
        }
        ipayroll.setIPPLANOVERRIDE(QBOFX.Y_N(pEmployee.getHasRetirementPlan()));

        if(pEmployee.getQbdtEmployeeInfo() != null) {
            QbdtEmployeeInfo qbdtEmployeeInfo = pEmployee.getQbdtEmployeeInfo();
            ipayroll.setICLASS(qbdtEmployeeInfo.getTrackingClass());
            ipayroll.setIUSETIME(QBOFX.Y_N(qbdtEmployeeInfo.getUseTime()));
        }
        mIEMP.setIPAYROLL(ipayroll);
    }

    public void addPayrollItem(EmployeePayrollItem pEmployeePayrollItem) {
        if(mIEMP.getIPAYROLL() == null) {
            mIEMP.setIPAYROLL(new IPAYROLL());
        }

        CompanyPayrollItem companyPayrollItem = pEmployeePayrollItem.getCompanyPayrollItem().getLatestCompanyPayrollItem();
        switch (pEmployeePayrollItem.getType()) {
            case Adjustment:
                IADJ iadj = new IADJ();
                iadj.setIAMT(QBOFX.mapNumericTypeToString(pEmployeePayrollItem.getAmountType(), pEmployeePayrollItem.getAmount()));
                iadj.setILIMIT(QBOFX.mapNumericTypeToString(pEmployeePayrollItem.getLimitType(), pEmployeePayrollItem.getItemLimit()));
                iadj.setIPITEMID(companyPayrollItem.getSourcePayrollItemId());
                mIEMP.getIPAYROLL().getIADJ().add(iadj);
                break;
            case Wage:
                IWAGE iwage = new IWAGE();
                iwage.setIPITEMID(companyPayrollItem.getSourcePayrollItemId());
                iwage.setIRATE(QBOFX.mapNumericTypeToString(pEmployeePayrollItem.getAmountType(), pEmployeePayrollItem.getAmount()));
                mIEMP.getIPAYROLL().getIWAGE().add(iwage);
                break;
        }
    }

    private void populateAccruals(Employee pEmployee) {
        for (EmployeeAccrual employeeAccrual : pEmployee.getEmployeeAccrualCollection()) {
            switch (employeeAccrual.getAccrualType()) {
                case Sick:
                    mIEMP.setISICK(buildAccrual(employeeAccrual));
                    break;
                case Vacation:
                    mIEMP.setIVAC(buildAccrual(employeeAccrual));
                    break;
            }
        }
    }

    private ACCRUAL buildAccrual(EmployeeAccrual employeeAccrual) {
        ACCRUAL accrual = new ACCRUAL();
        if(employeeAccrual.getAccrualPeriod() != null) {
            accrual.setIACCRUALPD(employeeAccrual.getAccrualPeriod().toString());
        }
        accrual.setIHRS(QBOFX.convertDoubleToOFXString(employeeAccrual.getHours()));
        accrual.setIHRSPERPD(QBOFX.convertDoubleToOFXString(employeeAccrual.getHoursPerPeriod()));
        accrual.setIMAXHRS(QBOFX.convertDoubleToOFXString(employeeAccrual.getMaxHours()));
        accrual.setINEWYRRESET(QBOFX.Y_N(employeeAccrual.getNewYearReset()));
        return accrual;
    }

    public void addCustomField(EmployeeCustomField pEmployeeCustomField) {
        mIEMP.setIHASCUSTOMFLD(QBOFX.Y_N(true));
        ICUSTOMFLD icustomfld = new ICUSTOMFLD();
        icustomfld.setIFLDNAME(pEmployeeCustomField.getName());
        icustomfld.setIFLDVALUE(QBOFX.convertNullToOFXString(pEmployeeCustomField.getValue()));
        mIEMP.getICUSTOMFLD().add(icustomfld);
    }
}
