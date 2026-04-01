package com.intuit.sbd.payroll.psp.mapper.orika;

import com.intuit.payroll.api.company.model.*;
import com.intuit.payroll.api.shared.model.AddressCDM;
import com.intuit.payroll.api.shared.model.BankAccountSubCDM;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.ContactRole;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import ma.glasnost.orika.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to convert Company data to CompanyCDM data
 *
 * @author dchoudhary1
 */
@Component
public class CompanyToCompanyCDMMapper extends BeanMapper<Company, CompanyCDM> {

	public static Logger LOGGER = LoggerFactory.getLogger(CompanyToCompanyCDMMapper.class);

	@Override
	public void directFieldToFieldMapping() {
		addBidirectionalFieldMapping("legalName", "legalName");
		addBidirectionalFieldMapping("fedTaxId", "taxIdentificationNumber");
		addBidirectionalFieldMapping("dbaName", "businessName");
		addBidirectionalFieldMapping("signUpDate", "signupDate");
		addBidirectionalFieldMapping("sourceCompanyId", "backOfficeId");
		addBidirectionalFieldMapping("fundingModel", "preFundDays");
		addBidirectionalFieldMapping("id", "id");
		addBidirectionalFieldMapping("version", "entityVersion");
	}

	@Override
	public void mapAtoB(Company company, CompanyCDM companyCDM, MappingContext context) {
		companyCDM.setAccountantWholesale(false);
		companyCDM.setLegalAddress(getEntityCDMMapper().mapToTarget(company.getLegalAddress(), AddressCDM.class));
		companyCDM.setMainAddress(getEntityCDMMapper().mapToTarget(company.getMailingAddress(), AddressCDM.class));
		companyCDM.setBusinessAddress(getEntityCDMMapper().mapToTarget(company.getLegalAddress(), AddressCDM.class));
		if (company.getContactCollection().size() > 0) {

			Contact contact = this.getContactByRoleCheck(company, ContactRole.PrimaryPrincipal);
			Contact contactAdmin = this.getContactByRoleCheck(company, ContactRole.PayrollAdmin);
			ContactCDM contactcdm = null;

			if (contact != null) {
				contactcdm = getEntityCDMMapper().mapToTarget(contact, ContactCDM.class);
			}
			if (contactcdm != null && contactAdmin != null) {
				contactcdm.setWorkPhone(contactAdmin.getPhone());
			}

			if (contactcdm != null) {
				companyCDM.setContact(contactcdm);
			} else {
				companyCDM.setContact(
						getEntityCDMMapper().mapToTarget(company.getContactCollection().get(0), ContactCDM.class));
			}

		}
		addBankAccounts(company, companyCDM);
		addHoldStatuses(company, companyCDM);
		addStatus(company, companyCDM);
		addPaymentLiabilityType(companyCDM);
		addTaxIdentifiers(company, companyCDM);
	}

	private void addPaymentLiabilityType(CompanyCDM companyCDM) {
		companyCDM.setPaymentLiabilityType(PaymentLiabilityType.CLIENT);
	}

	/**
	 * Note: This is not currently called, it might be used in the future, in the
	 * mapAToB function
	 *
	 * @param company
	 * @param companyCDM
	 */
	private void addTaxIdentifiers(Company company, CompanyCDM companyCDM) {
		List<CompanyTaxIdentifierCDM> companyTaxIdentifier = new ArrayList<>();
		DomainEntitySet<CompanyAgency> agencyCollection = company.getCompanyAgencyCollection();
		if (agencyCollection != null && agencyCollection.size() >= 1) {
			for (int iAgency = 0, n = agencyCollection.size(); iAgency < n; iAgency++) {
				DomainEntitySet<CompanyAgencyPaymentTemplate> agencyPaymentCollection = agencyCollection.get(iAgency)
						.getCompanyAgencyPaymentTemplateCollection();
				for (int iPayment = 0, sze = agencyPaymentCollection.size(); iPayment < sze; iPayment++) {
					CompanyTaxIdentifierCDM companyTaxIdentifierCDM = new CompanyTaxIdentifierCDM();
					companyTaxIdentifierCDM
							.setTaxIdentifier(agencyPaymentCollection.get(iPayment).getAgencyTaxpayerId());
					companyTaxIdentifier.add(companyTaxIdentifierCDM);
				}
			}
		} else {
			LOGGER.debug("No taxidentifiers for companyId={}", company.getId());
		}
	}

	private void addHoldStatuses(Company company, CompanyCDM companyCDM) {
		List<HoldStatusCDM> holdStatusCDMs = new ArrayList<>();

		DomainEntitySet<OnHoldReason> onHoldReason = company.getCurrentOnHoldReasonsDomainEntitySet();
		if (onHoldReason != null && onHoldReason.size() >= 1) {
			for (int i = 0, n = onHoldReason.size(); i < n; i++) {
				ServiceSubStatusCode serviceSubStatusCode = onHoldReason.get(i).getOnHoldReasonCd();
				LOGGER.info("The serviceSubStatusCode is  ={}", serviceSubStatusCode);
				HoldStatusCDM holdStatusCDM = getEntityCDMMapper().mapToTarget(serviceSubStatusCode,
						HoldStatusCDM.class);
				if (holdStatusCDM.getHoldName() != null) {
					holdStatusCDMs.add(holdStatusCDM);
				}
			}
			companyCDM.setHoldStatuses(holdStatusCDMs);
		} else {
			LOGGER.info("No OnHold Reason for companyId={}", company.getId());
		}
	}

	private void addBankAccounts(Company company, CompanyCDM companyCDM) {
		// Map sensitized version of bank accounts
		DomainEntitySet<CompanyBankAccount> bankAccount = company.getCompanyBankAccountCollection();
		if (bankAccount != null && bankAccount.size() >= 1) {
			List<BankAccountSubCDM> bankAccountSubCDMs = new ArrayList<BankAccountSubCDM>();
			for (int i = 0, n = bankAccount.size(); i < n; i++) {
				BankAccountSubCDM bankAccountSubCDM = getEntityCDMMapper().mapToTarget(bankAccount.get(i),
						BankAccountSubCDM.class);
				bankAccountSubCDMs.add(bankAccountSubCDM);
			}
			companyCDM.setBankAccounts(bankAccountSubCDMs);
		} else {
			LOGGER.warn("BankAccount is null for companyId={}", company.getId());
		}
	}

	private void addStatus(Company company, CompanyCDM companyCDM) {

		DomainEntitySet<CompanyService> companyServiceSet = company.getCompanyServiceCollection();
		if (companyServiceSet != null && companyServiceSet.size() >= 1) {
			for (int i = 0, n = companyServiceSet.size(); i < n; i++) {
				LOGGER.info("companyServiceSet.get(i).getService().getServiceCd()"
						+ companyServiceSet.get(i).getService().getServiceCd());
				if (companyServiceSet.get(i).getService().getServiceCd().in(ServiceCode.DirectDeposit)) {
					ServiceSubStatusCode serviceSubStatusCode = companyServiceSet.get(i).getStatusCd();
					if (serviceSubStatusCode.in(ServiceSubStatusCode.ActiveCurrent)) {
						companyCDM.setStatus(CompanyStatusType.ACTIVE);
					} else if (serviceSubStatusCode.in(ServiceSubStatusCode.Terminated)) {
						companyCDM.setStatus(CompanyStatusType.TERMINATED);
					} else {
						companyCDM.setStatus(CompanyStatusType.INACTIVE);
					}
					break;
				}
			}

		} else {
			LOGGER.debug("No CompanyService for companyId={}", company.getId());
		}
	}

	private DomainEntitySet<Contact> findContactInfo(Company company, final ContactRole contactRole) {
		return Application.find(Contact.class,
				Contact.Company().equalTo(company).And(Contact.ContactRoleCd().equalTo(contactRole)));
	}

	private Contact getContactByRoleCheck(Company company, ContactRole role) {
		DomainEntitySet<Contact> contact = findContactInfo(company, role);
		if (contact.size() > 1) {
			LOGGER.warn("More than one contact exists for company source id {}", company.getSourceCompanyId());
		}
		if (!contact.isEmpty()) {
			return contact.get(0);
		}
		LOGGER.warn("No contact exists for company source id {}", company.getSourceCompanyId());
		return null;
	}

}