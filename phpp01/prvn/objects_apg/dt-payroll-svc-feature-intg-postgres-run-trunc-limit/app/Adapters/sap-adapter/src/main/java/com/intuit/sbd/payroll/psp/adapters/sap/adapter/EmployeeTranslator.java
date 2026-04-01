package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeBankAccountFraud;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeComplianceData;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeInfo;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeTaxabilityInfo;
import com.intuit.sbd.payroll.psp.api.dtos.WagePlanDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.PIIMask;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * User: cyoder
 * Date: May 27, 2009
 * Time: 5:33:05 PM
 */
public class EmployeeTranslator {

    public static SAPEmployeeBankAccountFraud getSAPEmployeeBankAccountFraud(ArrayList<Paycheck> sameBankAccountPaychecks, EmployeeBankAccount employeeBankAccount, boolean canViewSSN){
        SAPEmployeeBankAccountFraud sapEmployeeBankAccountFraud = new SAPEmployeeBankAccountFraud();
        ArrayList<SAPEmployeeInfo> employeeInfoArray = new ArrayList<SAPEmployeeInfo>();

        for(Paycheck paycheck : sameBankAccountPaychecks){
            SAPEmployeeInfo employeeInfo = new SAPEmployeeInfo();
            employeeInfo.setFirstName(paycheck.getDDEmployee().getFirstName());
            employeeInfo.setLastName(paycheck.getDDEmployee().getLastName());
            employeeInfo.setSocialSecurityNumber(PIIMask.maskText(employeeBankAccount.getEmployee().getTaxId(), !canViewSSN));
            employeeInfoArray.add(employeeInfo);
        }

        sapEmployeeBankAccountFraud.setBankName(employeeBankAccount.getBankAccount().getBankName());
        sapEmployeeBankAccountFraud.setBankAccountNumber(employeeBankAccount.getBankAccount().getAccountNumber());
        sapEmployeeBankAccountFraud.setEmployeeInfo(employeeInfoArray);

        return sapEmployeeBankAccountFraud;
    }

     public static String getFilingStatusDescription(String pFilingStatusCode) {
        HashMap<String, String> mFilingStatusMap = new HashMap<String, String>();
        mFilingStatusMap.put("Single","Single");
        mFilingStatusMap.put("MarriedFilingJointly","Married Filing Jointly");
        mFilingStatusMap.put("MarriedFilingSeparately","Married Filing Separately");
        mFilingStatusMap.put("UNKNOWN", "Unknown");
        mFilingStatusMap.put("0","Single");
        mFilingStatusMap.put("1","Married");
        mFilingStatusMap.put("2","Jointly");
        mFilingStatusMap.put("3","Head of Household");
        mFilingStatusMap.put("4","Widowed");
        mFilingStatusMap.put("5","A");
        mFilingStatusMap.put("6","B");
        mFilingStatusMap.put("7","C");
        mFilingStatusMap.put("8","D");
        mFilingStatusMap.put("9","E");
        mFilingStatusMap.put("10","Default");
        mFilingStatusMap.put("11","X");
        mFilingStatusMap.put("12","Count");
        mFilingStatusMap.put("SINGLE","Single");
        mFilingStatusMap.put("MARRIED","Married");
        String filingStatus = null;
        if (mFilingStatusMap.containsKey(pFilingStatusCode)) {
            filingStatus = mFilingStatusMap.get(pFilingStatusCode);
        }
        return filingStatus;
    }

    public static SAPEmployeeTaxabilityInfo getEmployeeTaxFromDomainEntity(EmployeeTax pEmployeeTax, Employee pEmployee){
        SAPEmployeeTaxabilityInfo sapEmployeeTaxabilityInfo =  new SAPEmployeeTaxabilityInfo();
        EmployeeTaxType taxType = pEmployeeTax.getTaxType();
        if(taxType.in(EmployeeTaxType.FIT)){
            sapEmployeeTaxabilityInfo.setAllowances(pEmployee.getFedAllowances());
            sapEmployeeTaxabilityInfo.setFilingStatus(pEmployee.getFedFilingStatus());
            sapEmployeeTaxabilityInfo.setJurisdiction("FED");
            sapEmployeeTaxabilityInfo.setSubjectTo(pEmployeeTax.getSubjectTo());
            sapEmployeeTaxabilityInfo.setTaxType(taxType.name());
            sapEmployeeTaxabilityInfo.setExtraWithHolding(SAPTranslator.getDoubleFromSpcfMoney(pEmployee.getFedExtraWithholding()));
            sapEmployeeTaxabilityInfo.setClaimDependents(SAPTranslator.getDoubleFromSpcfMoney(pEmployee.getFedClaimDependents()));
            sapEmployeeTaxabilityInfo.setOtherIncome(SAPTranslator.getDoubleFromSpcfMoney(pEmployee.getFedOtherIncome()));
            sapEmployeeTaxabilityInfo.setDeductions(SAPTranslator.getDoubleFromSpcfMoney(pEmployee.getFedDeductions()));
            sapEmployeeTaxabilityInfo.setMultipleJobs(pEmployee.getFedMultipleJobs());
            sapEmployeeTaxabilityInfo.setFedW4EmployeePref(pEmployee.getFedW4EmployeePref());
            return sapEmployeeTaxabilityInfo;
        }  else if(taxType.in(EmployeeTaxType.FICA, EmployeeTaxType.MED, EmployeeTaxType.FUTA)) {
            sapEmployeeTaxabilityInfo.setTaxType(taxType.name());
            sapEmployeeTaxabilityInfo.setJurisdiction("FED");
        } else if (pEmployeeTax.getTaxType().name().equalsIgnoreCase("other")){
            CompanyLaw companyLaw = pEmployeeTax.getCompanyLaw();
            sapEmployeeTaxabilityInfo.setTaxType(companyLaw != null ? companyLaw.getLaw().getLawAbbrev() : "Other");
            String state = companyLaw != null ? companyLaw.getLaw().getPaymentTemplate().getPaymentTemplateCd().trim().substring(0,2) : "";
            if(!state.matches("[a-zA-Z]{2}")){
                state = "";
            }
            sapEmployeeTaxabilityInfo.setJurisdiction(state);

        } else {
            sapEmployeeTaxabilityInfo.setTaxType(pEmployeeTax.getTaxType().name());
            sapEmployeeTaxabilityInfo.setJurisdiction(pEmployeeTax.getState());
        }
        sapEmployeeTaxabilityInfo.setAllowances(pEmployeeTax.getAllowances());
        sapEmployeeTaxabilityInfo.setFilingStatus(getFilingStatusDescription(pEmployeeTax.getFilingStatus()));
        sapEmployeeTaxabilityInfo.setSubjectTo(pEmployeeTax.getSubjectTo());
        sapEmployeeTaxabilityInfo.setExtraWithHolding((int) pEmployeeTax.getExtraWithholding());

        return  sapEmployeeTaxabilityInfo;
    }


    public static SAPEmployeeComplianceData getEmployeeComplianceFromDomainEntity(EmployeeWagePlan pEmployeeWagePlan){
        SAPEmployeeComplianceData sapEmployeeComplianceData =  new SAPEmployeeComplianceData();
        sapEmployeeComplianceData.setState(pEmployeeWagePlan.getState());
        sapEmployeeComplianceData.setWagePlanDomain(pEmployeeWagePlan.getWagePlanDomain().name());
        sapEmployeeComplianceData.setName(pEmployeeWagePlan.getName().name());
        sapEmployeeComplianceData.setWagePlanValue( pEmployeeWagePlan.getWagePlanValue());
        sapEmployeeComplianceData.setDescription(pEmployeeWagePlan.getDescription());
        sapEmployeeComplianceData.setRulesVersion(pEmployeeWagePlan.getRulesVersion());
        sapEmployeeComplianceData.setId(pEmployeeWagePlan.getId().toString());

        return  sapEmployeeComplianceData;
    }

     public static WagePlanDTO buildWagePlanDTOFromSAPComplianceData(SAPEmployeeComplianceData pComplianceData){
         WagePlanDTO wagePlanDTO = new WagePlanDTO();
         wagePlanDTO.setState(pComplianceData.getState());
         wagePlanDTO.setDomainCode(WagePlanDomainCode.valueOf(pComplianceData.getWagePlanDomain()));
         wagePlanDTO.setName(WagePlanNameCode.valueOf(pComplianceData.getName()));
         wagePlanDTO.setDescription(pComplianceData.getDescription());
         wagePlanDTO.setWagePlanValue(pComplianceData.getWagePlanValue());
         wagePlanDTO.setRulesVersion(pComplianceData.getRulesVersion());

         return wagePlanDTO;
     }

}
