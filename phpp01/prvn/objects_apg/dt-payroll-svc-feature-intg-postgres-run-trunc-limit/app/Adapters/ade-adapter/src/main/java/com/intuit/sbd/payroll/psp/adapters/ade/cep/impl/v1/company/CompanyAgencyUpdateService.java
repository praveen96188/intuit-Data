package com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.v1.company;

import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.ems.cep.company.v1.resource.CompanyResource;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxSetupUpdateServiceParams;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.TransactionAwareAbstractUpdateService;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.AgencyIdMapper;
import com.intuit.sbd.payroll.psp.adapters.ade.tools.ServiceHelper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyAgencyDTO;
import com.intuit.sbd.payroll.psp.domain.CompanyAgency;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.schema.payroll.v3.company.Agency;

/**
 * Created with IntelliJ IDEA.
 * User: znorcross
 * Date: 4/24/14
 * Time: 3:17 PM
 */
public class CompanyAgencyUpdateService extends TransactionAwareAbstractUpdateService<Agency, TaxSetupUpdateServiceParams> {
    @Override
    protected ServiceResult validateDelegate() {
        ServiceResult validationResult = new ServiceResult();

        if (serviceParams.getCompanyId() == null) {
            validationResult.getMessages().NullProperty(getClass(), "", CompanyResource.PATH_PARAM_COMPANY_ID);
            return validationResult;
        }

        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(SourceSystemCode.QBDT, serviceParams.getCompanyId(), AgencyIdMapper.getPSPAgencyIdByComplianceAgencyId(cdmEntity.getId()));
        if(companyAgency == null) {
            validationResult.getMessages().EntityDoesNotExist(Agency.class, cdmEntity.getId());
            return validationResult;
        }

        return validationResult;
    }

    @Override
    protected ServiceResult executeDelegate() {
        ServiceResult serviceResult = new ServiceResult();

        if(cdmEntity.getEmployerAccountNumber() != null) {
            CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(SourceSystemCode.QBDT, serviceParams.getCompanyId(), AgencyIdMapper.getPSPAgencyIdByComplianceAgencyId(cdmEntity.getId()));
            CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgency);
            companyAgencyDTO.setAgencyTaxpayerId(cdmEntity.getEmployerAccountNumber());
            ServiceHelper.mergeServiceResultWithProcessResult(serviceResult, PayrollServices.companyManager.updateCompanyAgency(SourceSystemCode.QBDT, serviceParams.getCompanyId(), AgencyIdMapper.getPSPAgencyIdByComplianceAgencyId(cdmEntity.getId()), companyAgencyDTO));
        }

        return serviceResult;
    }

    @Override
    protected Agency refreshEntity() {
        // we're not refreshing entity here, because this service is never called by it's self. The parent service is responsible for returning the correct cdm
        return null;
    }
}
