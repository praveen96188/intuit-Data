package com.intuit.sbd.payroll.psp.mapper.cdm;
import com.intuit.payroll.api.contractor.model.ContractorCDM;
import com.intuit.payroll.api.contractor.model.PrivilegedContractorCDM;
import com.intuit.payroll.api.contractorpayment.model.ContractorType;
import com.intuit.payroll.api.shared.model.AddressSubCDM;
import com.intuit.payroll.api.shared.model.BankAccountSubCDM;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.Payee;
import com.intuit.sbd.payroll.psp.domain.PayeeBankAccount;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("com.intuit.sbd.payroll.psp.mapper.cdm.PayeeToContractorCDMMapper")
@Slf4j
public class PayeeToContractorCDMMapper extends BeanMapper<Payee, ContractorCDM> {

	private static final String STATUS_ACTIVE = "ACTIVE";

	@Override
	public ContractorCDM mapToTarget(Payee sourceEntity, Class<ContractorCDM> privilegedContractorCDMClass) {
		ContractorCDM result = new PrivilegedContractorCDM();
		result.setId(sourceEntity.getId().toString());
		result.setStatus(STATUS_ACTIVE);
		result.setEntityVersion(String.valueOf(sourceEntity.getVersion()));
		result.setContractorId(sourceEntity.getId().toString());
		result.setBusinessName(sourceEntity.getName());
		result.setContractorType(sourceEntity.getIs1099() ? ContractorType.INDIVIDUAL : ContractorType.BUSINESS);
		addBankAccounts(sourceEntity,result);
		result.setAddress(getMapper().mapToTarget(sourceEntity.getMailingAddress(), AddressSubCDM.class));
		result.setTaxId(sourceEntity.getTaxId());
		result.setBusinessPhone(sourceEntity.getPhone());
		result.setPayrollCompanyId(sourceEntity.getCompany().getSourceCompanyId());
		result.setBusinessEmail(sourceEntity.getEmail());
		return result;
	}

	private void addBankAccounts(Payee payee, ContractorCDM contractorCDM) {
		// Map sensitized version of bank accounts
		DomainEntitySet<PayeeBankAccount> bankAccounts = payee.getPayeeBankAccountCollection();
		if (CollectionUtils.isNotEmpty(bankAccounts)) {
			List<BankAccountSubCDM> bankAccountSubCDMs = new ArrayList<BankAccountSubCDM>();
			for (PayeeBankAccount bankAccount: bankAccounts) {
				BankAccountSubCDM bankAccountSubCDM = getMapper().mapToTarget(bankAccount,
						BankAccountSubCDM.class);
				bankAccountSubCDMs.add(bankAccountSubCDM);
			}
			contractorCDM.setBankAccounts(bankAccountSubCDMs);
		} else {
			log.warn("BankAccount is null for payeeId={}", payee.getId());
		}
	}

}