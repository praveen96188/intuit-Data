package com.intuit.sbd.payroll.psp.adapters.qbdt.translators;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Employee;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Paycheck;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.EmployeeTaxType;
import com.intuit.sbd.payroll.psp.domain.PaylineType;
import com.intuit.sbd.payroll.psp.domain.QbdtEmployeeType;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 15, 2010
 * Time: 8:25:23 AM
 */
public class EmployeeTranslator {
    public static final String NULL_EMP_NAME_STR = "<EMPTY>";
    private static SpcfLogger logger = Application.getLogger(EmployeeTranslator.class);

    public static void populateEmployeeDTO(Employee pEmployee, EmployeeDTO pEmployeeDTO, boolean pIsAssisted) {
        pEmployeeDTO.setEmployeeId(pEmployee.getSourceId());

        if (pEmployee.getSSN() == null || pEmployee.getSSN().trim().equals("")) {
            pEmployeeDTO.setSocialSecurityNumber("000000000");
        } else {
            pEmployeeDTO.setSocialSecurityNumber(pEmployee.getSSN());
        }

        pEmployeeDTO.setAccrualOnlyMod(pEmployee.isAccrualOnlyMod());

        pEmployeeDTO.setFirstName(pEmployee.getFirstName());
        pEmployeeDTO.setMiddleName(pEmployee.getMiddleInitial());
        pEmployeeDTO.setLastName(pEmployee.getLastName());
        pEmployeeDTO.setLiveAddress(buildAddressDTO(pEmployee));

        pEmployeeDTO.setFedAllowances(pEmployee.getFedFilingAllowances());
        pEmployeeDTO.setFedExtraWithholding(pEmployee.getFedExtraWithholding());
        pEmployeeDTO.setFedFilingStatus(pEmployee.getFedFilingStatus());
        pEmployeeDTO.setFedClaimDependents(pEmployee.getFedClaimDependents());
        pEmployeeDTO.setFedOtherIncome(pEmployee.getFedOtherIncome());
        pEmployeeDTO.setFedDeduction(pEmployee.getFedDeduction());
        pEmployeeDTO.setFedMultipleJobs(pEmployee.getFedMultipleJobs());
        pEmployeeDTO.setFedW4EmployeePref(pEmployee.getFedW4EmployeePref());
        if (pEmployee.getHireDate() != null) {
            pEmployeeDTO.setHireDate(new DateDTO(pEmployee.getHireDate()));
        } else {
            pEmployeeDTO.setHireDate(null);
        }

        if (pEmployee.getReleaseDate() != null) {
            pEmployeeDTO.setTerminationDate(new DateDTO(pEmployee.getReleaseDate()));
        } else {
            pEmployeeDTO.setTerminationDate(null);
        }

        if (pEmployee.getBirthDate() != null) {
            pEmployeeDTO.setBirthDate(new DateDTO(pEmployee.getBirthDate()));
        }

        pEmployeeDTO.setLiveState(pEmployee.getLiveState());
        pEmployeeDTO.setWorkState(pEmployee.getWorkState());

        pEmployeeDTO.setDeceased(pEmployee.isDeceased());
        pEmployeeDTO.setHasRetirementPlan(pEmployee.getHasRetirementPlan());
        pEmployeeDTO.setPayPeriod(pEmployee.getPayPeriod());
        pEmployeeDTO.setQualifiesForAEIC(pEmployee.qualifiesForAEIC());
        pEmployeeDTO.setPhoneNumber(pEmployee.getPhone());
        pEmployeeDTO.setEmail(pEmployee.getEmail());
        pEmployeeDTO.setStatusCd(pEmployee.getEmployeeStatus());
        pEmployeeDTO.setGender(pEmployee.getGender());
        pEmployeeDTO.setStatutory(pEmployee.getEmployeeType() == QbdtEmployeeType.STATUTORY);

        pEmployeeDTO.setEmployeeAccrualDTOs(buildEmployeeAccrualDTOs(pEmployee));

        pEmployeeDTO.setEmployeeCustomFields(buildEmployeeCustomFields(pEmployee));

        pEmployeeDTO.setEmployeeTaxDTOs(buildEmployeeTaxDTOs(pEmployee));

        pEmployeeDTO.setEmployeePayrollItemDTOs(buildEmployeePayrollItemDTOs(pEmployee));

        if (pEmployeeDTO.getQBDTEmployeeInfoDTO() == null) {
            pEmployeeDTO.setQBDTEmployeeInfoDTO(new QBDTEmployeeInfoDTO());
        }
        populateQBDTEmployeeInfo(pEmployee, pEmployeeDTO.getQBDTEmployeeInfoDTO());
        pEmployeeDTO.getQBDTEmployeeInfoDTO().setIsAssisted(pIsAssisted);

        pEmployeeDTO.setWagePlanDTOs(buildWagePlanDTOs(pEmployee));
        pEmployeeDTO.setEmployeeBankAccountDTOs(buildEmployeeBankAccountDTOs(pEmployee));
    }

    public static void populateQBDTEmployeeInfo(Employee pEmployee, QBDTEmployeeInfoDTO pQBDTEmployeeInfoDTO) {
        pQBDTEmployeeInfoDTO.setAltPhone(pEmployee.getAltPhone());
        pQBDTEmployeeInfoDTO.setBillPayAccount(pEmployee.getBillPayAccount());
        pQBDTEmployeeInfoDTO.setEnforceSubjectTo(pEmployee.enforceSubjectTo());
        pQBDTEmployeeInfoDTO.setInitials(pEmployee.getInitials());
        pQBDTEmployeeInfoDTO.setPrintAsName(pEmployee.getPrintAsName());
        pQBDTEmployeeInfoDTO.setQBDTEmployeeType(pEmployee.getEmployeeType());
        pQBDTEmployeeInfoDTO.setTitle(pEmployee.getTitle());
        pQBDTEmployeeInfoDTO.setTrackingClass(pEmployee.getClassTracking());
        pQBDTEmployeeInfoDTO.setUseTime(pEmployee.useTime());
        pQBDTEmployeeInfoDTO.setUseDD(pEmployee.useDD());
        pQBDTEmployeeInfoDTO.setListId(pEmployee.getListId());
        pQBDTEmployeeInfoDTO.setIsSeasonal(pEmployee.getIsSeasonal());

    }

    public static AddressDTO buildAddressDTO(Employee pEmployee) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressLine1(pEmployee.getAddressLine1());
        addressDTO.setAddressLine2(pEmployee.getAddressLine2());
        addressDTO.setCity(pEmployee.getCity());
        addressDTO.setState(pEmployee.getState());
        addressDTO.setZipCode(pEmployee.getZipCode());
        return addressDTO;
    }

    public static List<EmployeeAccrualDTO> buildEmployeeAccrualDTOs(Employee pEmployee) {
        List<EmployeeAccrualDTO> employeeAccrualDTOs = new ArrayList<EmployeeAccrualDTO>();

        Employee.EmployeeAccrual sickAccrual = pEmployee.getSickAccrual();
        if (sickAccrual != null) {
            EmployeeAccrualDTO sickAccrualDTO = new EmployeeAccrualDTO();
            sickAccrualDTO.setAccrualPeriod(sickAccrual.getAccrualPeriod());
            sickAccrualDTO.setAccrualType(sickAccrual.getAccrualType());
            sickAccrualDTO.setHours(sickAccrual.getHours());
            sickAccrualDTO.setHoursPerPeriod(sickAccrual.getHoursPerPeriod());
            sickAccrualDTO.setMaxHours(sickAccrual.getMaxHours());
            sickAccrualDTO.setNewYearReset(sickAccrual.isNewYearReset());
            employeeAccrualDTOs.add(sickAccrualDTO);
        }

        Employee.EmployeeAccrual vacationAccrual = pEmployee.getVacationAccrual();
        if (vacationAccrual != null) {
            EmployeeAccrualDTO vacationAccrualDTO = new EmployeeAccrualDTO();
            vacationAccrualDTO.setAccrualPeriod(vacationAccrual.getAccrualPeriod());
            vacationAccrualDTO.setAccrualType(vacationAccrual.getAccrualType());
            vacationAccrualDTO.setHours(vacationAccrual.getHours());
            vacationAccrualDTO.setHoursPerPeriod(vacationAccrual.getHoursPerPeriod());
            vacationAccrualDTO.setMaxHours(vacationAccrual.getMaxHours());
            vacationAccrualDTO.setNewYearReset(vacationAccrual.isNewYearReset());
            employeeAccrualDTOs.add(vacationAccrualDTO);
        }

        return employeeAccrualDTOs;
    }

    private static List<EmployeeCustomFieldDTO> buildEmployeeCustomFields(Employee pEmployee) {
        List<EmployeeCustomFieldDTO> employeeCustomFieldDTOs = new ArrayList<EmployeeCustomFieldDTO>();
        int fieldOrder = 0;
        for (String name : pEmployee.getCustomFields().keySet()) {
            EmployeeCustomFieldDTO employeeCustomFieldDTO = new EmployeeCustomFieldDTO();
            employeeCustomFieldDTO.setName(name);
            employeeCustomFieldDTO.setValue(pEmployee.getCustomFields().get(name));
            employeeCustomFieldDTO.setOrder(fieldOrder++);
            employeeCustomFieldDTOs.add(employeeCustomFieldDTO);
        }
        return employeeCustomFieldDTOs;
    }

    public static List<EmployeeTaxDTO> buildEmployeeTaxDTOs(Employee pEmployee) {
        List<EmployeeTaxDTO> employeeTaxDTOs = new ArrayList<EmployeeTaxDTO>();
        int taxOrder = 0;
        for (Employee.EmployeeTax employeeTax : pEmployee.getEmployeeTaxes()) {
            EmployeeTaxDTO employeeTaxDTO = new EmployeeTaxDTO();
            employeeTaxDTO.setCompanyLawId(employeeTax.getCompanyLawId());
            employeeTaxDTO.setState(employeeTax.getState());
            employeeTaxDTO.setSubjectTo(employeeTax.isSubjectTo());
            employeeTaxDTO.setTaxLawVersion(employeeTax.getTaxLawVersion());
            employeeTaxDTO.setTaxTableMiscData(employeeTax.getMiscData());
            employeeTaxDTO.setTaxType(employeeTax.getTaxType());
            employeeTaxDTO.setW2Name(employeeTax.getW2Name());
            employeeTaxDTO.setOrder(taxOrder++);

            if (employeeTax.getTaxType() == EmployeeTaxType.SIT) {
                employeeTaxDTO.setAllowances(pEmployee.getStateFilingAllowances());
                employeeTaxDTO.setExtraWithholding(pEmployee.getStateExtraWithholding());
                employeeTaxDTO.setExtraWithholdingType(pEmployee.getStateExtraWithholdingType());
                employeeTaxDTO.setFilingStatus(pEmployee.getStateFilingStatus());
            }
            employeeTaxDTOs.add(employeeTaxDTO);
        }
        return employeeTaxDTOs;
    }

    public static List<EmployeePayrollItemDTO> buildEmployeePayrollItemDTOs(Employee pEmployee) {
        List<EmployeePayrollItemDTO> employeePayrollItemDTOs = new ArrayList<EmployeePayrollItemDTO>();

        int payrollItemCount = 0;
        for (Employee.EmployeePayrollItem employeePayrollItem : pEmployee.getWages()) {
            if (!employeePayrollItem.getPayrollItemId().equals("0")) {
                EmployeePayrollItemDTO employeePayrollItemDTO = new EmployeePayrollItemDTO();
                employeePayrollItemDTO.setAmount(employeePayrollItem.getAmount());
                employeePayrollItemDTO.setAmountType(employeePayrollItem.getAmountType());
                employeePayrollItemDTO.setOrder(payrollItemCount++);
                employeePayrollItemDTO.setPaylineType(PaylineType.Wage);
                employeePayrollItemDTO.setPayrollItemId(employeePayrollItem.getPayrollItemId());
                employeePayrollItemDTOs.add(employeePayrollItemDTO);
            }
        }

        for (Employee.EmployeePayrollItem employeePayrollItem : pEmployee.getAdjustments()) {
            if (!employeePayrollItem.getPayrollItemId().equals("0")) {
                EmployeePayrollItemDTO employeePayrollItemDTO = new EmployeePayrollItemDTO();
                employeePayrollItemDTO.setAmount(employeePayrollItem.getAmount());
                employeePayrollItemDTO.setAmountType(employeePayrollItem.getAmountType());
                employeePayrollItemDTO.setOrder(payrollItemCount++);
                employeePayrollItemDTO.setPaylineType(PaylineType.Adjustment);
                employeePayrollItemDTO.setPayrollItemId(employeePayrollItem.getPayrollItemId());
                employeePayrollItemDTO.setItemLimit(employeePayrollItem.getItemLimit());
                employeePayrollItemDTO.setLimitType(employeePayrollItem.getLimitType());
                employeePayrollItemDTOs.add(employeePayrollItemDTO);
            }
        }

        return employeePayrollItemDTOs;
    }

    public static List<WagePlanDTO> buildWagePlanDTOs(Employee pEmployee) {
        List<WagePlanDTO> wagePlanDTOs = new ArrayList<WagePlanDTO>();
        for (Employee.EmployeeWagePlan employeeWagePlan : pEmployee.getEmployeeWagePlans()) {
            WagePlanDTO wagePlanDTO = new WagePlanDTO();
            wagePlanDTO.setDescription(employeeWagePlan.getDescription());
            wagePlanDTO.setDomainCode(QBOFX.mapOFXWagePlanDomainCode(employeeWagePlan.getDomain()));
            wagePlanDTO.setName(QBOFX.mapOFXWagePlanNameCode(employeeWagePlan.getName()));
            wagePlanDTO.setRulesVersion(employeeWagePlan.getRulesVersion());
            wagePlanDTO.setState(employeeWagePlan.getState());
            wagePlanDTO.setWagePlanValue(employeeWagePlan.getValue());
            wagePlanDTOs.add(wagePlanDTO);
        }
        return wagePlanDTOs;
    }

    private static List<EmployeeBankAccountDTO> buildEmployeeBankAccountDTOs(Employee pEmployee) {
        List<EmployeeBankAccountDTO> employeeBankAccountDTOs = new ArrayList<EmployeeBankAccountDTO>();
        int accountCounter = 0;
        for (Employee.EmployeeBankAccount employeeBankAccount : pEmployee.getEmployeeBankAccounts()) {
            if (employeeBankAccount.getAccountNumber() != null && employeeBankAccount.getRoutingNumber() != null) {
                EmployeeBankAccountDTO employeeBankAccountDTO = new EmployeeBankAccountDTO();
                employeeBankAccountDTO.setAmount(employeeBankAccount.getAmount());
                employeeBankAccountDTO.setAmountType(employeeBankAccount.getAmountType());
                employeeBankAccountDTO.setOrder(accountCounter++);
                employeeBankAccountDTO.setGenerateNewSourceId(true);
                employeeBankAccountDTO.setSessionId(pEmployee.sessionId());
                BankAccountDTO bankAccountDTO = new BankAccountDTO();
                bankAccountDTO.setAccountNumber(employeeBankAccount.getAccountNumber());
                bankAccountDTO.setAccountType(employeeBankAccount.getAccountType());
                bankAccountDTO.setBankName(QBOFX.nullStringCheck(employeeBankAccount.getBankName()));
                bankAccountDTO.setRoutingNumber(employeeBankAccount.getRoutingNumber());
                employeeBankAccountDTO.setBankAccount(bankAccountDTO);

                employeeBankAccountDTOs.add(employeeBankAccountDTO);
            }
        }
        return employeeBankAccountDTOs;
    }

    public static EmployeeDTO buildEmployeeDTOFromPaycheck(EmployeeDTO pEmployeeDTO, Paycheck pPaycheck, Company pCompany, boolean pIsAssistedRequest) {
        if (pEmployeeDTO == null) {
            pEmployeeDTO = new EmployeeDTO();
        }


        pEmployeeDTO.setEmployeeId(pPaycheck.getSourceEmployeeId());
        if (pPaycheck.getEmployeeName() != null && pPaycheck.getEmployeeName().length() > 0) {
            separateEmployeeNameIntoEmpDTO(pPaycheck.getEmployeeName(), pEmployeeDTO);
        }

        if (!pIsAssistedRequest && (pEmployeeDTO.getQBDTEmployeeInfoDTO() == null || !pEmployeeDTO.getQBDTEmployeeInfoDTO().getIsAssisted())) {
            pEmployeeDTO.setValidator(new EmployeeDTODDValidator());
        } else {
            EmployeeDTOAssistedValidator employeeDTOAssistedValidator = new EmployeeDTOAssistedValidator(pCompany);
            pEmployeeDTO.setValidator(employeeDTOAssistedValidator);
        }
        return pEmployeeDTO;
    }

    private static void separateEmployeeNameIntoEmpDTO(String fullName, EmployeeDTO empDTO) {
        // split using contiguous whitespace blocks as separator
        String nameParts[] = fullName.trim().split("[\\s]+");

        // Only last name is required in QB.
        if (nameParts.length == 1) {
            empDTO.setFirstName(NULL_EMP_NAME_STR);
            empDTO.setLastName(nameParts[0]);
        }

        // Check for Pattern Last Name, First Middle
        // Or Pattern First Name Middle Last

        int firstNameIndex;
        int lastNameIndex;


        if (nameParts[0].endsWith(",")) {
            nameParts[0] = nameParts[0].substring(0, nameParts[0].length() - 1);
            firstNameIndex = 1;
            lastNameIndex = 0;
        } else {
            firstNameIndex = 0;
            lastNameIndex = nameParts.length - 1;
        }

        if (nameParts.length == 2) {
            empDTO.setFirstName(nameParts[firstNameIndex]);
            empDTO.setLastName(nameParts[lastNameIndex]);
        }

        if (nameParts.length == 3) {
            empDTO.setFirstName(nameParts[firstNameIndex]);
            empDTO.setMiddleName(nameParts[firstNameIndex + 1]);
            empDTO.setLastName(nameParts[lastNameIndex]);
        }

        if (nameParts.length > 3) {
            empDTO.setFirstName(nameParts[firstNameIndex]);
            empDTO.setMiddleName(nameParts[firstNameIndex + 1]);
            StringBuilder lastName = new StringBuilder();
            int lastIndex = (lastNameIndex == 0 ? nameParts.length : nameParts.length - 1);
            for (int i = firstNameIndex + 2; i < lastIndex; i++) {
                lastName.append(nameParts[i]);
                lastName.append(" ");
            }

            lastName.append(nameParts[lastNameIndex]);
            empDTO.setLastName(lastName.toString());
        }
    }
}
