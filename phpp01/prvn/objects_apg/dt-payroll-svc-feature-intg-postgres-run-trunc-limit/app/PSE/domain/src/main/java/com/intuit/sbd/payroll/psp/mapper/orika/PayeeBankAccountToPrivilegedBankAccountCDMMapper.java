package com.intuit.sbd.payroll.psp.mapper.orika;
import com.intuit.payroll.api.shared.model.PrivilegedBankAccountCDM;
import com.intuit.sbd.payroll.psp.domain.PayeeBankAccount;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

@Component
public class PayeeBankAccountToPrivilegedBankAccountCDMMapper
		extends BeanMapper<PayeeBankAccount, PrivilegedBankAccountCDM> {

	@Override
	public void directFieldToFieldMapping() {
		addBidirectionalFieldMapping("id", "id");
		addBidirectionalFieldMapping("bankAccount.accountTypeCd", "accountType");
		addBidirectionalFieldMapping("bankAccount.accountNumber", "accountNumber");
		addBidirectionalFieldMapping("bankAccount.routingNumber", "routingNumber");
		addBidirectionalFieldMapping("modifiedDate", "updated");
		addBidirectionalFieldMapping("effectiveDate", "created");
	}

	@Override
	public void mapAtoB(PayeeBankAccount payeeBankAccount, PrivilegedBankAccountCDM bankAccountCDM,
			MappingContext context) {
		if (payeeBankAccount.getPayee() != null && payeeBankAccount.getPayee().getCompany() != null
				&& payeeBankAccount.getPayee().getCompany().getSourceCompanyId() != null) {
			bankAccountCDM.setCompanyId(Long.valueOf(payeeBankAccount.getPayee().getCompany().getSourceCompanyId()));
		}
		bankAccountCDM.setVerified(false);
	}

}
