package com.intuit.sbd.payroll.psp.adapters.ptc.webservices;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ptc.dto.*;
import com.intuit.sbd.payroll.psp.adapters.ptc.exception.PTCAdapterException;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.TaxServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * User: dweinberg
 * Date: 8/13/12
 * Time: 3:49 PM
 */
@WebService()
public class PTCWebServices {
    private static final SpcfLogger logger = PayrollServices.getLogger(PTCWebServices.class);

    public static final String ERR_MISSING_INPUT ="missingInput";
    public static final String ERR_INVALID_INPUT ="invalidInput";
    public static final String ERR_INVALID_PIN = "invalidPIN";
	public static final String ERR_PIN_LOCKOUT = "accountLockout";
    public static final String EX_VALIDATION_FAILED = "unexpectedFailure";
    public static final String VALID = "valid";

    @WebMethod
    @WebResult(name = "PINValidationResponse")
    /*
    1) Validates inputs
    2) Validates that EIN and PSID match
    3) Validates that the user is not locked out
    4) Validates that PIN is correct
    5) If PIN is correct, clear retries
    6) If PIN is not correct, retry count incremented and lockout logic applied
     */
    public PINValidationResponse validatePIN(@WebParam(name = "PINValidationRequest") PINValidationRequest pRequest) {
        PINValidationResponse response = new PINValidationResponse();
        try {
            PayrollServices.beginUnitOfWork();

            if (StringUtils.isEmpty(pRequest.getEin()) || StringUtils.isEmpty(pRequest.getSourceSystemCode()) || StringUtils.isEmpty(pRequest.getPsid()) || StringUtils.isEmpty(pRequest.getPin())) {
                response.setStatus(ERR_MISSING_INPUT);
                logger.info("Invalid PIN request: " + pRequest.toString());
                return response;
            }

            Company company = Company.findCompany(pRequest.getPsid(), SourceSystemCode.valueOf(pRequest.getSourceSystemCode()));

            if (company == null) {
                response.setStatus(ERR_INVALID_INPUT);
                logger.info("Invalid Company: " + pRequest.toString());
                return response;
            }

            if (!company.getFedTaxId().equals(pRequest.getEin())) {
                response.setStatus(ERR_INVALID_INPUT);
                logger.error("EIN/PSID mismatch: " + pRequest.toString());
                return response;
            }


            ProcessResult<Company> pinResult = PayrollServices.subscriptionManager.verifyCompanyPIN(company.getSourceSystemCd(), company.getSourceCompanyId(), pRequest.getPin());
            if (pinResult.isSuccess()) {
                response.setStatus(VALID);
            } else {
                if (!pinResult.getMessages("293").isEmpty()) {
                    response.setStatus(ERR_PIN_LOCKOUT);
                } else {
                    response.setStatus(ERR_INVALID_PIN);
                }
            }

            PayrollServices.commitUnitOfWork();

            return response;
        } catch (Exception e) {
            logger.warn(e);
            response.setStatus(EX_VALIDATION_FAILED);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return response;
    }

    @WebMethod
    @WebResult(name="W2ServiceChargePrice")
    public ServiceChargePrice getW2ServiceChargePrice(@WebParam(name="PSPCompanyRequest") PSPCompanyRequest pCompany) {
        try {
            PayrollServices.beginUnitOfWork();
            ServiceChargePrice w2ServiceChargePrice = new ServiceChargePrice();

            Company company = Company.findCompany(pCompany.getPsid(), SourceSystemCode.valueOf(pCompany.getSourceSystemCode()));
            checkCompanyStatus(company, pCompany.getPsid());
            Offering offering = company.getOffering(ServiceCode.DirectDeposit).getOffering();
            OfferingServiceCharge baseCharge = offering.getCharge(OfferingServiceChargeType.W2BaseFee, 1);//W-2s are not tiered so will not try to guess number
            OfferingServiceCharge unitCharge = offering.getCharge(OfferingServiceChargeType.W2Fee, 1);//W-2s are not tiered so will not try to guess number

            SpcfCalendar nextW2ChargeDate = SpcfCalendar.createInstance(PSPDate.getPSPTime().getYear() + 1, 1, 15, SpcfTimeZone.getLocalTimeZone());
            OfferingServiceChargePrice unitPrice = unitCharge.getEffectivePrice(nextW2ChargeDate);
            OfferingServiceChargePrice basePrice = baseCharge.getEffectivePrice(nextW2ChargeDate);
            w2ServiceChargePrice.setBasePrice("$" + basePrice.getBasePrice().toString());
            w2ServiceChargePrice.setUnitPrice("$" + unitPrice.getUnitPrice().toString());

            return w2ServiceChargePrice;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private void checkCompanyStatus(Company company, String psid) {
        if (Objects.isNull(company)) {
            throw new PTCAdapterException("Company with PSID : " + psid + " not found. Company might be DG Disassociated.");
        }
    }

    @WebMethod
    @WebResult(name="isActiveOnService")
    public boolean isActiveOnService(@WebParam(name="PSPCompanyRequest") PSPCompanyRequest pCompany) {
        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pCompany.getPsid(), SourceSystemCode.valueOf(pCompany.getSourceSystemCode()));
            checkCompanyStatus(company, pCompany.getPsid());
            return company.isCompanyOnService(ServiceCode.Tax);

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    @WebResult(name="PSID")
    public String getPSIDForEIN(@WebParam(name="sourceSystemCode") String sourceSystemCode, @WebParam(name="EIN") String ein) {
        try {
            PayrollServices.beginUnitOfWork();


            Criterion<CompanyService> companyServiceCriterion = CompanyService.Service().ServiceCd().equalTo(ServiceCode.Tax)
                    .And(CompanyService.Company().SourceSystemCd().equalTo(SourceSystemCode.valueOf(sourceSystemCode)));
            if (ein == null){
                companyServiceCriterion = companyServiceCriterion.And(CompanyService.Company().FedTaxIdEnc().isNull());
            } else {
                List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName, ein);
                companyServiceCriterion = companyServiceCriterion.And(CompanyService.Company().FedTaxIdEnc().in(fedTaxIdEncList));
            }

            if(!AuthUser.hasSAPAdminAccess()) {
                companyServiceCriterion = companyServiceCriterion.And(CompanyService.Company().IsDgDisassociated().equalTo(Boolean.FALSE));
            }

            DomainEntitySet<CompanyService> companyServices =
                    Application.find(CompanyService.class, new Query<CompanyService>().Where(companyServiceCriterion)
                            .OrderBy(CompanyService.StatusEffectiveDate().Descending()));


            Company company = findBestCompanyMatch(companyServices);
            if (company == null) {
                return null;
            } else {
                return company.getSourceCompanyId();
            }

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    /**
     * @param companyServices list of company services with companies matching EIN sorted by status effective date desc
     * @return best matching company or null
     */
    private Company findBestCompanyMatch(DomainEntitySet<CompanyService> companyServices) {
        for (CompanyService companyService : companyServices) {
            if (companyService.isActive()) {
                return companyService.getCompany();
            }
        }
        if (companyServices.isNotEmpty()) {
            Company company = companyServices.getFirst().getCompany();
            logger.info(String.format("PTC termed login with EIN had multiple results.  Guessing PSID with most recent service status change: %s",company.getSourceCompanyId()));
            return company;
        }
        return null;
    }

    @WebMethod
    @WebResult(name="IsEligibleForTermedForms")
    //Eligible until june 30th of the year following cancellation
    public boolean isEligibleForCancelForms(@WebParam(name="PSPCompanyRequest") PSPCompanyRequest pCompany) {
        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pCompany.getPsid(), SourceSystemCode.valueOf(pCompany.getSourceSystemCode()));
            checkCompanyStatus(company, pCompany.getPsid());
            if (company.isCompanyOnService(ServiceCode.Tax)) {
                return true; //allow users to log in through termed even if active
            }
            if (!company.hasService(ServiceCode.Tax)) {
                return false;
            }
            //otherwise cancelled.  Assume cancelled date is status effective date.
            CompanyService companyService = company.getCompanyService(ServiceCode.Tax);
            SpcfCalendar firstDayWithNoAccess = SpcfCalendar.createInstance(companyService.getStatusEffectiveDate().getYear() + 1, 7, 1, SpcfTimeZone.getLocalTimeZone());
            return PSPDate.getPSPTime().before(firstDayWithNoAccess);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    @WebResult(name="W2PrintingPreference")
    public String getW2PrintingPreference(@WebParam(name="PSPCompanyRequest") PSPCompanyRequest pCompany) {
        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pCompany.getPsid(), SourceSystemCode.valueOf(pCompany.getSourceSystemCode()));
            checkCompanyStatus(company, pCompany.getPsid());
            return ((TaxCompanyServiceInfo)company.getCompanyService(ServiceCode.Tax)).getW2DeliveryPreferenceCd().toString();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void updateW2PrintingPreference(@WebParam(name="PSPCompanyRequest") PSPCompanyRequest pCompany,
                                           @WebParam(name="w2PrintingPreference") String w2PrintingPreference) {
        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pCompany.getPsid(), SourceSystemCode.valueOf(pCompany.getSourceSystemCode()));
            checkCompanyStatus(company, pCompany.getPsid());
            CompanyService taxService = CompanyService.findCompanyService(company, ServiceCode.Tax);
            TaxCompanyServiceInfo taxCompanyServiceInfo = (TaxCompanyServiceInfo) taxService;

            TaxServiceInfoDTO taxServiceInfoDTO = (TaxServiceInfoDTO) PayrollServices.dtoFactory.create(taxCompanyServiceInfo);

            taxServiceInfoDTO.setW2DeliveryPreferenceCd(DeliveryPreferenceCode.valueOf(w2PrintingPreference));

            ProcessResult pr = PayrollServices.companyManager.updateService(company.getSourceSystemCd(), company.getSourceCompanyId(), taxServiceInfoDTO);
            if (pr.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                throw new RuntimeException(pr.toString());
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    @WebResult(name="LegalInfo")
    public LegalInfo getLegalInfo(@WebParam(name="PSPCompanyRequest") PSPCompanyRequest pCompany) {
        try {
            LegalInfo legalInfo = new LegalInfo();
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pCompany.getPsid(), SourceSystemCode.valueOf(pCompany.getSourceSystemCode()));
            checkCompanyStatus(company, pCompany.getPsid());
            legalInfo.setLegalName(company.getLegalName());
            legalInfo.setLegalAddress(getAddressDTO(company.getLegalAddress()));

            return legalInfo;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    @WebResult(name="EmployeeInfo")
    //Attempt to match by SSN.  If not there, attempt to match by name.  Otherwise, empty
    public EmployeeInfo getEmployeeInfo(@WebParam(name="EmployeeInfoRequest") EmployeeInfoRequest pEmployeeInfoRequest) {
        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pEmployeeInfoRequest.getPsid(), SourceSystemCode.valueOf(pEmployeeInfoRequest.getSourceSystemCode()));
            checkCompanyStatus(company, pEmployeeInfoRequest.getPsid());

            List<String> encSsnList = EncryptionUtils.deterministicEncryptWithAllKeys(Employee.TaxIdKeyName, pEmployeeInfoRequest.getSsn().replaceAll("/", ""));
            DomainEntitySet<Employee> employeesMatchingSSN = company.getEmployees().find(Employee.TaxIdEnc().in(encSsnList)
                    .And(Employee.QbdtEmployeeInfo().IsAssisted().equalTo(true)));

            if (employeesMatchingSSN.isNotEmpty()) {
                return getEmployeeInfoFromEmployee(findByName(employeesMatchingSSN, pEmployeeInfoRequest.getEmployeeName()));
            } else {
                return getEmployeeInfoFromEmployee(findByName(company.getEmployees().find(Employee.QbdtEmployeeInfo().IsAssisted().equalTo(true)), pEmployeeInfoRequest.getEmployeeName()));
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private Employee findByName(DomainEntitySet<Employee> employees, String name) {
        employees = employees.sort(Employee.StatusEffectiveDate().Descending());
        for (Employee employee : employees) {
            if (name.matches(String.format("(?i).*%s.+%s*", Pattern.quote(employee.getFirstName()), Pattern.quote(employee.getLastName())))) {
                return employee;
            }
        }
        return null;
    }

    private EmployeeInfo getEmployeeInfoFromEmployee(Employee employee) {
        if (employee == null) {
            return null;
        }
        EmployeeInfo info = new EmployeeInfo();
        info.setAddress(getAddressDTO(employee.getMailingAddress()));
        info.setFirstName(employee.getFirstName());
        info.setLastName(employee.getLastName());
        info.setFullName(employee.getFullName());
        info.setSsn(employee.getTaxId());
        return info;
    }

    private AddressDTO getAddressDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setAddress1(address.getAddressLine1());
        dto.setAddress2(address.getAddressLine2());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setZipCode(address.getZipCode());
        return dto;
    }

}
