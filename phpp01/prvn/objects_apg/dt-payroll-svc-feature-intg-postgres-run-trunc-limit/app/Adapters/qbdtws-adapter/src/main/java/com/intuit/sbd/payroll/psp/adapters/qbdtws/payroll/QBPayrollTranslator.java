package com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.*;
import com.intuit.sbd.payroll.psp.domain.CompanyService;
import com.intuit.sbd.payroll.psp.domain.EmployeeTaxType;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.domain.PayrollItemCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * User: rnorian
 * Date: Dec 16, 2009
 * Time: 3:24:38 PM
 */
public class QBPayrollTranslator {
    /**
     * @param companyDTO
     * @param pQBCompany
     * @return
     */
    public static CompanyDTO updateCoreDTO(CompanyDTO companyDTO, QBCompany pQBCompany) {

        if (pQBCompany == null)
            return companyDTO;

        // QBDT wants to remain 'dumb' so adapter handles all formatting
        //   DD OFX does not include country; intentionally null out to prevent 'address change' event
        //   capitalizing all values in Company, LegalAddress is done in ToUpperStringAdapter
        if (pQBCompany.getLegalAddress() != null)
            pQBCompany.getLegalAddress().setCountry(null);

        companyDTO.setFein(pQBCompany.getFein());
        companyDTO.setLegalName(pQBCompany.getLegalName());
        companyDTO.setLegalAddress(updateCoreDTO(companyDTO.getLegalAddress(), pQBCompany.getLegalAddress()));
        companyDTO.setQuickBooksInfo(updateCoreDTO(companyDTO.getQuickBooksInfo(), pQBCompany));
     
        return companyDTO;
    }

    private static AddressDTO updateCoreDTO(AddressDTO addressDTO, QBAddress pQBAddress) {

        if (pQBAddress == null)
            return null;

        if (addressDTO == null)
            addressDTO = new AddressDTO();

        addressDTO.setAddressLine1(pQBAddress.getAddressLine1());
        addressDTO.setAddressLine2(pQBAddress.getAddressLine2());
        addressDTO.setAddressLine3(pQBAddress.getAddressLine3());
        addressDTO.setCity(pQBAddress.getCity());
        addressDTO.setState(pQBAddress.getState());
        addressDTO.setZipCode(pQBAddress.getZipCode());
        addressDTO.setZipCodeExtension(pQBAddress.getZipCodeExtension());
        addressDTO.setCountry(pQBAddress.getCountry());

        return addressDTO;
    }

    private static QuickbooksInfoDTO updateCoreDTO(QuickbooksInfoDTO pQuickBooksInfoDTO, QBCompany pQBCompany) {

        if (pQBCompany == null)
            return pQuickBooksInfoDTO;

        if (pQuickBooksInfoDTO == null)
            pQuickBooksInfoDTO = new QuickbooksInfoDTO();

        //pQuickBooksInfoDTO.setApplicationId(pQBCompany.getApplicationId());

        pQuickBooksInfoDTO.setApplicationVersion(pQBCompany.getQBVersion());
        pQuickBooksInfoDTO.setTaxTableId(pQBCompany.getTaxTableId());

        return pQuickBooksInfoDTO;
    }

    public static EmployeeDTO updateCoreDTO(EmployeeDTO pEmployeeDTO, QBEmployee pQBEmployee) {

        // QBDT does not fill in any QBEmployee fields (outside of the source employee id) when an employee is not active
        // (do not overwrite existing values)
        if (pQBEmployee == null || !pQBEmployee.isActive())
            return pEmployeeDTO;

        if (pEmployeeDTO == null)
            pEmployeeDTO = new EmployeeDTO();

        // do not overwrite an existing source employee Id in translator; that is adapter logic specific to scenarios
        if (pEmployeeDTO.getEmployeeId() == null) {
            pEmployeeDTO.setEmployeeId(pQBEmployee.getPspEmployeeId());
        }

        pEmployeeDTO.setSocialSecurityNumber(pQBEmployee.getSocialSecurityNumber());

        if (pQBEmployee.getFedAllowances() != null) {
            pEmployeeDTO.setFedAllowances(pQBEmployee.getFedAllowances().intValue());
        }

        // if employee is Assisted (presence of OFXEmployeeId) don't overwrite FilingStatus
        // since they are not DR compatible
        if (pQBEmployee.getFedFilingStatus() != null && !pQBEmployee.isOFXEmployee()) {
            pEmployeeDTO.setFedFilingStatus(pQBEmployee.getFedFilingStatus().value());
        }

        pEmployeeDTO.setFirstName(pQBEmployee.getFirstName());
        pEmployeeDTO.setMiddleName(pQBEmployee.getMiddleName());
        pEmployeeDTO.setLastName(pQBEmployee.getLastName());
        pEmployeeDTO.setSuffix(pQBEmployee.getSuffix());


        if (pQBEmployee.getHasRetirementPlan() != null) {
            pEmployeeDTO.setHasRetirementPlan(pQBEmployee.getHasRetirementPlan());
        }

        if (pQBEmployee.getHasThirdPartySickPay() != null) {
            pEmployeeDTO.setHasThirdPartySickPay(pQBEmployee.getHasThirdPartySickPay());
        }

        pEmployeeDTO.setBirthDate(createCoreDTO(pQBEmployee.getBirthDate()));
        pEmployeeDTO.setHireDate(createCoreDTO(pQBEmployee.getHireDate()));
        pEmployeeDTO.setLiveAddress(updateCoreDTO(pEmployeeDTO.getLiveAddress(), pQBEmployee.getLiveAddress()));

        if (pQBEmployee.isStatutory() != null) {
            pEmployeeDTO.setStatutory(pQBEmployee.isStatutory());
        }

        pEmployeeDTO.setWorkState(pQBEmployee.getWorkState());

        pEmployeeDTO.setTerminationDate(createCoreDTO(pQBEmployee.getTerminationDate()));

        List<EmployeeTaxDTO> employeeTaxDTOs = pEmployeeDTO.getEmployeeTaxDTOs();
        EmployeeTaxDTO sitEmployeeTaxDTO = null;
        for (EmployeeTaxDTO employeeTaxDTO : employeeTaxDTOs) {
            if(employeeTaxDTO.getTaxType() == EmployeeTaxType.SIT) {
                sitEmployeeTaxDTO = employeeTaxDTO;
                if (pQBEmployee.getStateAllowances() != null)
                    employeeTaxDTO.setAllowances(pQBEmployee.getStateAllowances());
                if (pQBEmployee.getStateFilingStatus() != null && !pQBEmployee.isOFXEmployee())
                    employeeTaxDTO.setFilingStatus(pQBEmployee.getStateFilingStatus().value());
            }
        }
        if(sitEmployeeTaxDTO == null) {
            EmployeeTaxDTO employeeTaxDTO = new EmployeeTaxDTO();
            employeeTaxDTO.setTaxType(EmployeeTaxType.SIT);
            if (pQBEmployee.getStateAllowances() != null)
                employeeTaxDTO.setAllowances(pQBEmployee.getStateAllowances());
            if (pQBEmployee.getStateFilingStatus() != null && !pQBEmployee.isOFXEmployee())
                employeeTaxDTO.setFilingStatus(pQBEmployee.getStateFilingStatus().value());
            pEmployeeDTO.getEmployeeTaxDTOs().add(employeeTaxDTO);
        }

        if (pEmployeeDTO.getQBDTEmployeeInfoDTO() == null) {
            pEmployeeDTO.setQBDTEmployeeInfoDTO(new QBDTEmployeeInfoDTO());
            pEmployeeDTO.getQBDTEmployeeInfoDTO().setListId(pQBEmployee.getSourceEmployeeId());
        }

        updateCoreThirdParty401kDTO(pEmployeeDTO, pQBEmployee);

        return pEmployeeDTO;
    }

    public static EmployeeDTO updateCoreThirdParty401kDTO(EmployeeDTO pEmployeeDTO, QBEmployee pQBEmployee) {
        if (pEmployeeDTO.getEmployee401kInfo() == null) {
            pEmployeeDTO.setEmployee401kInfo(new ThirdParty401kEmployeeInfoDTO());
        }

        pEmployeeDTO.getEmployee401kInfo().setEmail(pQBEmployee.getEmailAddress());
        pEmployeeDTO.getEmployee401kInfo().setPhoneNumber(pQBEmployee.getPhoneNumber());

        if (pQBEmployee.isFamilyMember() != null)
            pEmployeeDTO.getEmployee401kInfo().setFamilyMember(pQBEmployee.isFamilyMember().booleanValue());

        if (pQBEmployee.isHighlyCompensatedEmployee() != null)
            pEmployeeDTO.getEmployee401kInfo().setHighlyCompensatedEmployee(pQBEmployee.isHighlyCompensatedEmployee().booleanValue());

        if (pQBEmployee.getOwnerPercent() != null)
            pEmployeeDTO.getEmployee401kInfo().setOwnershipPercent(new BigDecimal(pQBEmployee.getOwnerPercent()));

        if (pQBEmployee.getPaySchedule() != null)
            ; //TODO: add PaySchedule to EmployeeDTO

        return pEmployeeDTO;

    }

    public static DateDTO createCoreDTO(QBDate pQBDate) {
        if (pQBDate == null)
            return null;

        DateDTO dateDTO = new DateDTO();

        dateDTO.setDay(pQBDate.getDay());
        dateDTO.setMonth(pQBDate.getMonth() - 1);
        dateDTO.setYear(pQBDate.getYear());

        return dateDTO;
    }

    public static ProcessResult<PaycheckDTO> createCoreDTO(QBPaycheck pPaycheck, String pPSPSourceEmployeeId) {
        ProcessResult<PaycheckDTO> processResult = new ProcessResult<PaycheckDTO>();

        PaycheckDTO paycheckDTO = new PaycheckDTO();
        paycheckDTO.setPaycheckId(pPaycheck.getPspPaycheckId());
        paycheckDTO.setEmployeeId(pPSPSourceEmployeeId);

        // BELOW SHOULD NOT OCCUR -- ERROR IN OUR CONTRACT w/QB (can't enforce at schema level since info is unavailable on deleted paychecks)
        if (pPaycheck.getOperation() != QBPaycheckOperationEnum.DELETE) {
            if (pPaycheck.getPayDate() == null) {
                processResult.getMessages().RequiredAttribute(EntityName.PayCheck, pPaycheck.getPaycheckID(), "PayDate", MessageInfo.MessageLevel.ERROR);
            }
            if (pPaycheck.getPeriodStartDate() == null) {
                processResult.getMessages().RequiredAttribute(EntityName.PayCheck, pPaycheck.getPaycheckID(), "PeriodStartDate", MessageInfo.MessageLevel.ERROR);
            }
            if (pPaycheck.getPeriodEndDate() == null) {
                processResult.getMessages().RequiredAttribute(EntityName.PayCheck, pPaycheck.getPaycheckID(), "PeriodEndDate", MessageInfo.MessageLevel.ERROR);
            }
            if (!processResult.isSuccess())
                return processResult;
        }

        paycheckDTO.setPayPeriodBeginDate(createCoreDTO(pPaycheck.getPeriodStartDate()));
        paycheckDTO.setPayPeriodEndDate(createCoreDTO(pPaycheck.getPeriodEndDate()));
        paycheckDTO.setPaycheckGrossAmount(SpcfUtils.convertToSpcfMoney(pPaycheck.getGrossPay()));
        paycheckDTO.setPaycheckYTDGrossAmount(SpcfUtils.convertToSpcfMoney(pPaycheck.getYTDGrossPay()));
        paycheckDTO.setPaycheckNetAmount(SpcfUtils.convertToSpcfMoney(pPaycheck.getNetPay()));
        paycheckDTO.setPaycheckYTDNetAmount(SpcfUtils.convertToSpcfMoney(pPaycheck.getYTDNetPay()));

        // dd transactions intentionally excluded
        paycheckDTO.setCompensationTransactions(new ArrayList<CompensationTransactionDTO>());
        paycheckDTO.setDeductionTransactions(new ArrayList<DeductionTransactionDTO>());
        paycheckDTO.setLiabilityTransactions(new ArrayList<LiabilityTransactionDTO>());
        paycheckDTO.setEmployerContributionTransactions(new ArrayList<EmployerContributionTransactionDTO>());

        processResult.setResult(paycheckDTO);
        return processResult;
    }

    public static PayrollRunDTO createCorePayrollDTO(String pTransmissionId, QBPaycheck pPaycheck) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        payrollRunDTO.setPayrollTXBatchId(UUID.randomUUID().toString());
        payrollRunDTO.setSettlementDate(createCoreDTO(pPaycheck.getPayDate()));
        payrollRunDTO.setTargetPayrollTXDate(createCoreDTO(pPaycheck.getPayDate()));
        payrollRunDTO.setTransmissionId(pTransmissionId);
        payrollRunDTO.setPaychecks(new ArrayList<PaycheckDTO>());

        return payrollRunDTO;
    }

    public static CompanyPayrollItemDTO createCoreDTO(QBPayrollItem pQBPayrollItem) {
        CompanyPayrollItemDTO companyPayrollItemDTO = new CompanyPayrollItemDTO();
        companyPayrollItemDTO.setSourcePayrollItemId(pQBPayrollItem.getPspPayrollItemId());
        companyPayrollItemDTO.setSourcePayrollItemDescription(pQBPayrollItem.getName());
        companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(new QBDTPayrollItemInfoDTO());
        companyPayrollItemDTO.getQBDTPayrollItemInfoDTO().setListId(pQBPayrollItem.getID());
        companyPayrollItemDTO.setPayrollItemCode(getDefaultPayrollItemCode(pQBPayrollItem.getPayrollItemCategory()));
        return companyPayrollItemDTO;
    }

    public static PayrollItemCode getDefaultPayrollItemCode(QBPayrollItemCategory pQBPayrollItemCategory) {
        switch (pQBPayrollItemCategory) {
            case EARNING_ITEM:
                return PayrollItemCode.Compensation;
            case PRE_TAX_ITEM:
                return PayrollItemCode.OtherPreTaxDeduction;
            case ADJ_NET_PAY_ITEM:
                return PayrollItemCode.OtherPostTaxDeduction;
            case NON_TAX_COMPANY_ITEM:
                return PayrollItemCode.OtherNonTaxableEmployerContribution;
            case TAX_COMPANY_ITEM:
                return PayrollItemCode.OtherTaxableEmployerContribution;
            case TAX_ITEM:
                // intentionally skipped
                break;
        }
        return null;
    }

    public static QBPayrollItemCategory getPayrollItemCategory(PayrollItemCode pPayrollItemCode) {
        switch (pPayrollItemCode) {
            case Compensation:
            case Salary:
            case Bonus:
            case Hourly:
                return QBPayrollItemCategory.EARNING_ITEM;
            case DirectDeposit:
                return QBPayrollItemCategory.DIRECT_DEPOSIT_ITEM;
            case OtherPreTaxDeduction:
            case Tp401kEmployeeDeferral:
                return QBPayrollItemCategory.PRE_TAX_ITEM;
            case OtherPostTaxDeduction:
            case Tp401kLoanPayment:
                return QBPayrollItemCategory.ADJ_NET_PAY_ITEM;
            case OtherNonTaxableEmployerContribution:
                return QBPayrollItemCategory.NON_TAX_COMPANY_ITEM;
            case OtherTaxableEmployerContribution:
                return QBPayrollItemCategory.TAX_COMPANY_ITEM;
            default:
                // intentionally skipped
                return null;
        }
    }

    public static List<QBCompanyService> createWSDTO(DomainEntitySet<CompanyService> pCompanyServices) {
        List<QBCompanyService> qbCompanyServices = new ArrayList<QBCompanyService>();
        if (pCompanyServices != null) {
            for (CompanyService companyService : pCompanyServices) {
                QBCompanyService qbCompanyService = new QBCompanyService();
                qbCompanyService.setName(QBCompanyServiceEnum.fromServiceCode(companyService.getService().getServiceCd()));
                qbCompanyService.setStatus(companyService.getStatusCd().name());
                qbCompanyServices.add(qbCompanyService);
            }
        }
        return qbCompanyServices;
    }
}
