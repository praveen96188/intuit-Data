package com.intuit.sbd.payroll.psp.mapper.guideline401k.company;

import com.google.common.collect.ImmutableMap;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyAdditionalInfo;
import com.intuit.sbd.payroll.psp.domain.OwnershipType;
import com.intuit.sbd.payroll.psp.mapper.cdm.BeanMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import scala.reflect.internal.Trees;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;

@Component
public class PspCompanyToV4TaxFormMapper extends BeanMapper<Company, TaxForm> {

    private Map<String, EcosystemOwnershipTypeEnum> pspV4OwnershipTypeMap;

    @PostConstruct
    private void setPspV4OwnershipMap(){
        pspV4OwnershipTypeMap = ImmutableMap.<String, EcosystemOwnershipTypeEnum>builder()
                .put("Sole Proprietorship", EcosystemOwnershipTypeEnum.SOLE_PROPRIETOR)
                .put("Partnership", EcosystemOwnershipTypeEnum.PARTNERSHIP_OR_LTD_LIABILITY)
                .put("Limited Liability Corp", EcosystemOwnershipTypeEnum.LTD_LIABILITY)
                .put("Corporation", EcosystemOwnershipTypeEnum.CORPORATION)
                .put("Non-Profit Organization", EcosystemOwnershipTypeEnum.NONPROFIT)
                .put("Private Limited Company", EcosystemOwnershipTypeEnum.LTD_LIABILITY)
                .put("Public Limited Company", EcosystemOwnershipTypeEnum.NONPROFIT)
                .put("Limited Liability Partnership", EcosystemOwnershipTypeEnum.PARTNERSHIP_OR_LTD_LIABILITY)
                .put("Foreign Company", EcosystemOwnershipTypeEnum.NOT_SURE)
                .put("Registered Charity", EcosystemOwnershipTypeEnum.NONPROFIT)
                .put("Unregistered Charity", EcosystemOwnershipTypeEnum.NONPROFIT)
                .put("Clubs And Societies", EcosystemOwnershipTypeEnum.NOT_SURE)
                .build();
    }

    @Override
    public TaxForm mapToTarget(Company pspCompany, Class<TaxForm> t) {

        TaxForm taxForm = new TaxForm();
        CompanyAdditionalInfo companyAdditionalInfo = pspCompany.getCompanyAdditionalInfo();

        if (Objects.isNull(companyAdditionalInfo)) return taxForm;

        OwnershipType pspOwnershipType = companyAdditionalInfo.getOwnershipType();

        if (Objects.isNull(pspOwnershipType)) return taxForm;

        String pspOwnershipTypeName = pspOwnershipType.getOwnership();

        if(StringUtils.isEmpty(pspOwnershipTypeName)) return taxForm;

        EcosystemOwnershipTypeEnum taxFormEnum = pspV4OwnershipTypeMap.get(pspOwnershipTypeName);
        taxForm.setId(taxFormEnum.ordinal());


        return taxForm;
    }
}
