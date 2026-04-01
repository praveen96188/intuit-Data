package com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.v1.company;

import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.ems.cep.company.v1.resource.CompanyResource;
import com.intuit.ems.cep.company.v1.service.params.CompanyServiceParams;
import com.intuit.ems.cep.util.CEPLogger;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.TransactionAwareAbstractUpdateService;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.AgencyIdMapper;
import com.intuit.sbd.payroll.psp.adapters.ade.tools.ServiceHelper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyAgencyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.FormTemplateDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.schema.payroll.v3.company.TaxFilingType;
import com.intuit.schema.payroll.v3.compliance.FilingTypeEnum;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

import java.util.Date;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: shivanandad069
 * Date: 3/2/15
 * Time: 3:37 PM
 */
public class CompanyTaxFilingTypeUpdateService extends TransactionAwareAbstractUpdateService<TaxFilingType, CompanyServiceParams> {

    private Company company;
    private FilingTypeEnum filingTypeEnum;
    private Date startDate;
    private SpcfCalendar effectiveDate;
    private CompanyAgency companyIRSAgency;

    @Override
    protected ServiceResult validateDelegate() {
        ServiceResult validationResult = new ServiceResult();

        if (serviceParams.getCompanyId() == null) {
            validationResult.getMessages().NullProperty(getClass(), "", CompanyResource.PATH_PARAM_COMPANY_ID);
            return validationResult;
        }

        company = Company.findCompanyNoEagerLoad(serviceParams.getCompanyId(), SourceSystemCode.QBDT);
        if (company == null) {
            validationResult.getMessages().EntityDoesNotExist(com.intuit.schema.payroll.v3.company.Company.class, serviceParams.getCompanyId());
            return validationResult;
        }

        if (cdmEntity.getAgencyName() == null) {
            validationResult.getMessages().NullProperty(TaxFilingType.class, null, "agencyName");
        } else if (!(cdmEntity.getAgencyName().trim().equalsIgnoreCase("Internal Revenue Service") || cdmEntity.getAgencyName().equalsIgnoreCase(com.intuit.sbd.payroll.psp.domain.Agency.IRS) ||
                AgencyIdMapper.getPSPAgencyIdByComplianceAgencyId(cdmEntity.getAgencyName()).equalsIgnoreCase(com.intuit.sbd.payroll.psp.domain.Agency.IRS))) {
            validationResult.getMessages().GenericValidationMessage(CompanyTaxFilingTypeUpdateService.class, null, "Only agency 'Internal Revenue Service' can update filer type and '" + cdmEntity.getAgencyName() + "' was passed in.");
        }

        if (cdmEntity.getFilingType() == null) {
            validationResult.getMessages().NullProperty(TaxFilingType.class, null, "filingType");
            return validationResult;
        }

        filingTypeEnum = cdmEntity.getFilingType();
        companyIRSAgency = CompanyAgency.findCompanyAgency(company, Agency.IRS);
        if (companyIRSAgency == null) {
            validationResult.getMessages().GenericValidationMessage(CompanyTaxFilingTypeUpdateService.class, null, "Agency '" + cdmEntity.getAgencyName() + "' not found for given company : '" + serviceParams.getCompanyId() + "'");
        }
        if (cdmEntity.getStartDate() == null) {
            validationResult.getMessages().NullProperty(TaxFilingType.class, null, "startDate");
        } else {
            startDate = cdmEntity.getStartDate();
        }
        effectiveDate = CalendarUtils.getFirstDayOfQuarter(SpcfCalendar.createInstance(startDate.getTime(), SpcfTimeZone.getLocalTimeZone()));
        return validationResult;
    }

    @Override
    protected ServiceResult<TaxFilingType> executeDelegate() {
        ServiceResult<TaxFilingType> serviceResult = new ServiceResult<TaxFilingType>();
        CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(companyIRSAgency);
        //remove 941/944 that are on or after the specified.
        Iterator<FormTemplateDTO> formTemplateIterator = companyAgencyDTO.getFormTemplateDtoList().iterator();
        while (formTemplateIterator.hasNext()) {
            FormTemplateDTO formTemplateDTO = formTemplateIterator.next();
            if (formTemplateDTO.is941944() && !effectiveDate.after(formTemplateDTO.getEffectiveDate().toLocal())) {
                formTemplateIterator.remove();
            }
        }
        //then add the specified one in
        companyAgencyDTO.getFormTemplateDtoList().add(getFormTemplateDTOFilerType());
        ProcessResult pr = PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), companyIRSAgency.getAgency().getAgencyId(), companyAgencyDTO);
        ServiceHelper.mergeServiceResultWithProcessResult(serviceResult, pr);


        return serviceResult;
    }

    @Override
    protected TaxFilingType refreshEntity() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public FormTemplateDTO getFormTemplateDTOFilerType() {
        FormTemplateDTO dto = new FormTemplateDTO();
        dto.setEffectiveDate(effectiveDate);
        dto.setFilerType(filingTypeEnum.compareTo(FilingTypeEnum.form944) == 0 ? FormTemplate.IRS_944 : FormTemplate.IRS_941);
        return dto;
    }


}
