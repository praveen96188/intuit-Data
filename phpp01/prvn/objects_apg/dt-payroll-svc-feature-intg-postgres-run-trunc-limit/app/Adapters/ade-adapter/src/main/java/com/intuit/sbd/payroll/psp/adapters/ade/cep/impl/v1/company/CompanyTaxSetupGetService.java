package com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.v1.company;

import com.intuit.ems.cep.api.ResourceNameEnum;
import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.ems.cep.company.v1.resource.CompanyResource;
import com.intuit.ems.cep.company.v1.service.ShowHistory;
import com.intuit.ems.cep.company.v1.service.params.CompanyServiceParams;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxItemGetListServiceParams;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxPaymentGroupGetListServiceParams;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxSetupGetServiceParams;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.ServiceFactory;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.TransactionAwareAbstractGetService;
import com.intuit.sbd.payroll.psp.adapters.ade.dg.DGCompanyValidator;
import com.intuit.sbd.payroll.psp.adapters.ade.translators.CompanyTranslator;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.schema.payroll.v3.company.*;

import java.util.List;
import java.util.Objects;

/**
 * User: ihannur
 * Date: 4/23/14
 * Time: 3:30 PM
 */
public class CompanyTaxSetupGetService extends TransactionAwareAbstractGetService<TaxSetup, TaxSetupGetServiceParams> {

    @Override
    protected ServiceResult validateDelegate() {
        ServiceResult validationResult = new ServiceResult();

        if (serviceParams.getCompanyId() == null) {
            validationResult.getMessages().NullProperty(serviceParams.getClass(), "", CompanyResource.PATH_PARAM_COMPANY_ID);
            return validationResult;
        }

        if (DGCompanyValidator.validateDG(validationResult, serviceParams.getCompanyId())) return validationResult;

        return validationResult;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ServiceResult<TaxSetup> executeDelegate() {
        ServiceResult<TaxSetup> serviceResult = new ServiceResult<TaxSetup>();

        Company company = Company.findCompanyNoEagerLoad(serviceParams.getCompanyId(), SourceSystemCode.QBDT);
        TaxSetup taxSetup = new TaxSetup();
        taxSetup.setLegalName(company.getLegalName());
        taxSetup.setLegalAddress(CompanyTranslator.createAddress(company.getLegalAddress()));
        taxSetup.setCountry(company.getLegalAddress().getCountry());

        CompanyServiceParams companyServiceParams = new CompanyServiceParams();
        companyServiceParams.setCompanyId(String.valueOf(serviceParams.getCompanyId()));

        //Populate list of Company Agency
        ServiceResult serviceResultAgencyList = ServiceFactory.getInstance().constructGetListServiceInstance(ResourceNameEnum.AGENCIES).service(companyServiceParams);
        if (serviceResultAgencyList.isSuccess()) {
            taxSetup.setAgencies((List<Agency>) serviceResultAgencyList.getResult());
        } else {
            serviceResult.merge(serviceResultAgencyList); //merge all failure messages
            return serviceResult;
        }

        //Populate list of Tax Items
        TaxItemGetListServiceParams taxItemGetListServiceParams = new TaxItemGetListServiceParams();
        taxItemGetListServiceParams.setCompanyId(serviceParams.getCompanyId());
        if (serviceParams.isShowAllRates()) {
            taxItemGetListServiceParams.setShowAll(ShowHistory.TAXRATE.name());
        }
        ServiceResult serviceResultTaxItemList = ServiceFactory.getInstance().constructGetListServiceInstance(ResourceNameEnum.TAXITEMS).service(taxItemGetListServiceParams);
        if (serviceResultTaxItemList.isSuccess()) {
            taxSetup.setTaxItems((List<TaxItem>) serviceResultTaxItemList.getResult());
        } else {
            serviceResult.merge(serviceResultTaxItemList); //merge all failure messages
            return serviceResult;
        }

        //Populate list of Tax Jurisdictions
        ServiceResult serviceResultTaxJurisdictions = ServiceFactory.getInstance().constructGetListServiceInstance(ResourceNameEnum.TAXJURISDICTIONS).service(companyServiceParams);
        if (serviceResultTaxJurisdictions.isSuccess()) {
            taxSetup.setTaxJurisdictions((List<TaxJurisdiction>) serviceResultTaxJurisdictions.getResult());
        } else {
            serviceResult.merge(serviceResultTaxJurisdictions);
            return serviceResult;
        }

        //populate list of tax payment groups
        TaxPaymentGroupGetListServiceParams taxPaymentGroupGetListServiceParams = new TaxPaymentGroupGetListServiceParams();
        taxPaymentGroupGetListServiceParams.setCompanyId(serviceParams.getCompanyId());
        if(serviceParams.isShowAllDepositFrequencies()) {
            taxPaymentGroupGetListServiceParams.setShowAll(ShowHistory.TAXDEPOSITFREQUENCY.name());
        }

        ServiceResult serviceResultTaxPaymentTemplates = ServiceFactory.getInstance().constructGetListServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroupGetListServiceParams);
        if (serviceResultTaxPaymentTemplates.isSuccess()) {
            taxSetup.setTaxPaymentGroups((List<TaxPaymentGroup>) serviceResultTaxPaymentTemplates.getResult());
        }
        serviceResult.merge(serviceResultTaxPaymentTemplates);

        ServiceResult<List<TaxFilingType>> serviceResultFilingTypeList = ServiceFactory.getInstance().<TaxFilingType, CompanyServiceParams>
                constructGetListServiceInstance(ResourceNameEnum.TAXFILINGTYPES).service(serviceParams);
        if (serviceResultFilingTypeList.isSuccess()) {
            taxSetup.setTaxFilingTypes(serviceResultFilingTypeList.getResult());
        }
        serviceResult.merge(serviceResultFilingTypeList); //merge all messages whether success or failure

        serviceResult.setResult(taxSetup);
        return serviceResult;
    }


}
