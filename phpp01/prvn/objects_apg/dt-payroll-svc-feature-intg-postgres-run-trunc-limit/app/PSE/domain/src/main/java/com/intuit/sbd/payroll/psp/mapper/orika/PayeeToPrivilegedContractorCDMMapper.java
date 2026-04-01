package com.intuit.sbd.payroll.psp.mapper.orika;
import com.intuit.payroll.api.contractor.model.PrivilegedContractorCDM;
import com.intuit.payroll.api.contractorpayment.model.PersonInfoCDM;
import com.intuit.payroll.api.shared.model.AddressCDM;
import com.intuit.payroll.api.shared.model.BankAccountSubCDM;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.Payee;
import com.intuit.sbd.payroll.psp.domain.PayeeBankAccount;
import ma.glasnost.orika.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PayeeToPrivilegedContractorCDMMapper extends BeanMapper<Payee, PrivilegedContractorCDM> {

	public static Logger LOGGER = LoggerFactory.getLogger(PayeeToPrivilegedContractorCDMMapper.class);

	@Override
	public void directFieldToFieldMapping() {
		addBidirectionalFieldMapping("id", "contractorId");
		addBidirectionalFieldMapping("name", "businessName");
//        addBidirectionalFieldMapping("lastName", "lastName");
		addBidirectionalFieldMapping("phone", "homePhone");
		addBidirectionalFieldMapping("taxId", "taxId");
	}

	@Override
	public void mapAtoB(Payee payee, PrivilegedContractorCDM privilegedContractorCDM, MappingContext context) {
		privilegedContractorCDM
				.setAddress(getEntityCDMMapper().mapToTarget(payee.getMailingAddress(), AddressCDM.class));
		PersonInfoCDM personInfo = new PersonInfoCDM();
		personInfo.setFirstName(payee.getName());
		privilegedContractorCDM.setPersonInfo(personInfo);
		addBankAccounts(payee, privilegedContractorCDM);

	}

	private void addBankAccounts(Payee payee, PrivilegedContractorCDM privilegedContractorCDM) {
		// Map sensitized version of bank accounts
		DomainEntitySet<PayeeBankAccount> bankAccount = payee.getPayeeBankAccountCollection();
		if (bankAccount != null && bankAccount.size() >= 1) {
			List<BankAccountSubCDM> bankAccountSubCDMs = new ArrayList<BankAccountSubCDM>();
			for (int i = 0, n = bankAccount.size(); i < n; i++) {
				BankAccountSubCDM bankAccountSubCDM = getEntityCDMMapper().mapToTarget(bankAccount.get(i),
						BankAccountSubCDM.class);
				bankAccountSubCDMs.add(bankAccountSubCDM);
			}
			privilegedContractorCDM.setBankAccounts(bankAccountSubCDMs);
		} else {
			LOGGER.warn("BankAccount is null for payeeId={}", payee.getId());
		}
	}
}