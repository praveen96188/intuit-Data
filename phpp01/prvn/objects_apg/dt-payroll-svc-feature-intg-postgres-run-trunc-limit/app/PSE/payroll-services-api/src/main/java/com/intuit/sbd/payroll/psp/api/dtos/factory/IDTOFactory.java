/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/factory/IDTOFactory.java#5 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos.factory;

import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;

/**
 * @author Wiktor Kozlik
 */
public interface IDTOFactory {

    public AddressDTO create(final Address pAddress);
    public BankAccountDTO create(final BankAccount pBankAccount);
    public CompanyBankAccountDTO create(final CompanyBankAccount pCompanyBankAccount);
    public CompanyDTO create(final Company pCompany);
    public ContactDTO create(final Contact pContact);
    public DDServiceInfoDTO create(final DDCompanyServiceInfo pDDCompanyServiceInfo);
    public PayeeBankAccountDTO create(final PayeeBankAccount pPayeeBankAccount);
    public EmployeeBankAccountDTO create(final EmployeeBankAccount pEmployeeBankAccount);
    public EmployeeDTO create(final Employee pEmployee);
    public EmployeeDTO create(final Employee pEmployee, boolean pLoadQBDTCollections);
    public WagePlanDTO create(final EmployeeWagePlan pEmployeeWagePlan);
    public OfferDTO create(final Offer pOffer);
    public OfferingServiceChargeDTO create(final OfferingServiceCharge pCharge);
    public OfferingServiceChargeGroupDTO create(final OfferingServiceChargeGroup pGroup);
    public OfferingServiceChargePriceDTO create(final OfferingServiceChargePrice pPrice);
    public PaycheckDTO create(final Paycheck pPaycheck);
    public QBDTPaycheckInfoDTO create(final QbdtPaycheckInfo pQbdtPaycheckInfo);
    public PayrollRunDTO create(final PayrollRun pPayrollRun);
    public CompanyPayrollItemDTO create(final CompanyPayrollItem pCompanyPayrollItem);
    public CompanyLawDTO create(CompanyLaw pCompanyLaw);
    public QBDTTransactionInfoDTO create(QbdtTransactionInfo pQBDTTransactionInfo);
    public LiabilityCheckDTO create(LiabilityCheck pLiabilityCheck);
    public LiabilityCheckLineDTO create(LiabilityCheckLine pLiabilityCheckLine);
    public CompanyAgencyDTO create(CompanyAgency pCompanyAgency);
    public ServiceInfoDTO create(CompanyService pCompanyService);
    public EntitlementDTO create(Entitlement pEntitlement);
    public EntitlementUnitDTO create(EntitlementUnit pEntitlementUnit);
    public EntitlementUnitDTO create(Entitlement pEntitlement, EntitlementUnitDTO pEntitlementUnitDTO);
    public EffectiveDepositFrequencyDTO create(EffectiveDepositFrequency pCompanyAgency);
    public QBDTPayrollTransactionDTO create(QbdtPayrollTransaction pQbdtPayrollTransaction);
    public OfferingInfoDTO create(final Offering pOffering);
}
