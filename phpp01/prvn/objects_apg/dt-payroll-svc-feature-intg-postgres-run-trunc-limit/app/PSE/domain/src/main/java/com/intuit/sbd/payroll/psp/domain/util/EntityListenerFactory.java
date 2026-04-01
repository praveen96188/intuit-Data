package com.intuit.sbd.payroll.psp.domain.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.BankAccount;
import com.intuit.sbd.payroll.psp.domain.BillPayment;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount;
import com.intuit.sbd.payroll.psp.domain.EntityUpdate;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.domain.PayeeBankAccount;
import com.intuit.sbd.payroll.psp.hibernate.EntityChangeListener;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Factory class returns reference of object
 * 
 * @author dchoudhary1
 *
 */
public class EntityListenerFactory {

	private static final SpcfLogger logger = Application.getLogger(EntityListenerFactory.class);

	public static EntityChangeListener getentityClass(Object entityName) {
		if (entityName == null) {
			return null;
		}
		// the entity is value like:
		// com.intuit.sbd.payroll.psp.domain.RTBAUTOMATIONBACKUP@3947
		String entityStr = entityName.toString();
		logger.debug("Begin EntityListenerFactory:getentityClass+entityStr" + entityStr);

		String finalEntityClassString = null;

		if (entityStr.indexOf("@") != -1) {
			finalEntityClassString = entityStr.substring(0, entityStr.indexOf("@"));
			logger.debug("finalEntityClassString " + finalEntityClassString);
		} else {
			finalEntityClassString = entityStr;
		}

		if (entityName instanceof Paycheck) {
			logger.debug("Begin inside instanceof" + entityName);

			Paycheck paycheck = (Paycheck) entityName;
			String cacheKey = "Paycheck:" + paycheck.getuniqueId() + ";";
			DomainEntitySet<Paycheck> paychecks = Application.getSessionCache().getEntityCollection(Paycheck.class,
					cacheKey);
			if (paychecks == null) {
				DomainEntitySet<Paycheck> set = new DomainEntitySet<Paycheck>();
				Application.getSessionCache().addEntityCollection(Paycheck.class, cacheKey, set);
			} else {
				paycheck.isDuplicate(true);
			}

			return (Paycheck) entityName;
		} else if (finalEntityClassString.equalsIgnoreCase("com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount")) {
			logger.debug("Begin inside instanceof EmployeeBankAccount" + entityName);

			EmployeeBankAccount employeeBankAccount = (EmployeeBankAccount) entityName;
			String cacheKey = "EmployeeBankAccount:" + employeeBankAccount.getuniqueId() + ";";
			DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = Application.getSessionCache()
					.getEntityCollection(EmployeeBankAccount.class, cacheKey);
			if (employeeBankAccounts == null) {
				DomainEntitySet<EmployeeBankAccount> set = new DomainEntitySet<EmployeeBankAccount>();
				Application.getSessionCache().addEntityCollection(EmployeeBankAccount.class, cacheKey, set);

			} else {
				employeeBankAccount.isDuplicate(true);
			}

			return (EmployeeBankAccount) entityName;
		} else if (finalEntityClassString.equalsIgnoreCase("com.intuit.sbd.payroll.psp.domain.CompanyBankAccount")) {

			CompanyBankAccount companyBankAccount = (CompanyBankAccount) entityName;
			String cacheKey = "CompanyBankAccount:" + companyBankAccount.getuniqueId() + ";";
			DomainEntitySet<CompanyBankAccount> companyBankAccounts = Application.getSessionCache()
					.getEntityCollection(CompanyBankAccount.class, cacheKey);
			if (companyBankAccounts == null) {
				DomainEntitySet<CompanyBankAccount> set = new DomainEntitySet<CompanyBankAccount>();
				Application.getSessionCache().addEntityCollection(CompanyBankAccount.class, cacheKey, set);
			} else {
				companyBankAccount.isDuplicate(true);
			}

			return (CompanyBankAccount) entityName;
		} else if (finalEntityClassString.equalsIgnoreCase("com.intuit.sbd.payroll.psp.domain.BankAccount")) {

			BankAccount bankAccount = (BankAccount) entityName;
			String cacheKey = "BankAccount:" + bankAccount.getuniqueId() + ";";
			DomainEntitySet<BankAccount> bankAccounts = Application.getSessionCache()
					.getEntityCollection(BankAccount.class, cacheKey);
			if (bankAccounts == null) {
				DomainEntitySet<BankAccount> set = new DomainEntitySet<BankAccount>();
				Application.getSessionCache().addEntityCollection(BankAccount.class, cacheKey, set);
			} else {
				bankAccount.isDuplicate(true);
			}

			return (BankAccount) entityName;
		} else if (finalEntityClassString.equalsIgnoreCase("com.intuit.sbd.payroll.psp.domain.PayeeBankAccount")) {

			PayeeBankAccount payeeBankAccount = (PayeeBankAccount) entityName;
			String cacheKey = "PayeeBankAccount:" + payeeBankAccount.getuniqueId() + ";";
			DomainEntitySet<PayeeBankAccount> payeeBankAccounts = Application.getSessionCache()
					.getEntityCollection(PayeeBankAccount.class, cacheKey);
			if (payeeBankAccounts == null) {
				DomainEntitySet<PayeeBankAccount> set = new DomainEntitySet<PayeeBankAccount>();
				Application.getSessionCache().addEntityCollection(PayeeBankAccount.class, cacheKey, set);
			} else {
				payeeBankAccount.isDuplicate(true);
			}

			return (PayeeBankAccount) entityName;
		} else if (finalEntityClassString.equalsIgnoreCase("com.intuit.sbd.payroll.psp.domain.BillPayment")) {

			BillPayment billPayment = (BillPayment) entityName;
			String cacheKey = "BillPayment:" + billPayment.getuniqueId() + ";";
			DomainEntitySet<BillPayment> billPayments = Application.getSessionCache()
					.getEntityCollection(BillPayment.class, cacheKey);
			if (billPayments == null) {
				DomainEntitySet<BillPayment> set = new DomainEntitySet<BillPayment>();
				Application.getSessionCache().addEntityCollection(BillPayment.class, cacheKey, set);
			} else {
				billPayment.isDuplicate(true);

			}

			return (BillPayment) entityName;
		} else if (finalEntityClassString.equalsIgnoreCase("com.intuit.sbd.payroll.psp.domain.EntityUpdate")) {

			EntityUpdate entityUpdate = (EntityUpdate) entityName;
			String cacheKey = "EntityUpdate:" + "Entityid" + entityUpdate.getEntityId();
			DomainEntitySet<EntityUpdate> entityUpdates = Application.getSessionCache()
					.getEntityCollection(EntityUpdate.class, cacheKey);
			if (entityUpdates == null) {
				DomainEntitySet<EntityUpdate> set = new DomainEntitySet<EntityUpdate>();
				set.add(entityUpdate);

				Application.getSessionCache().addNonHibernateObject(cacheKey, entityUpdate.getId().toString());
				Application.getSessionCache().addEntityCollection(EntityUpdate.class, cacheKey, set);

				String value = Application.getSessionCache().getNonHibernateObject(cacheKey);
				DomainEntitySet<EntityUpdate> getVal = Application.getSessionCache()
						.getEntityCollection(EntityUpdate.class, cacheKey);


			} else {
				entityUpdate.isDuplicate(true);
				logger.debug("entityUpdate duplicate is true" + entityUpdate.getChangedAttributes());
			}
			return (EntityUpdate) entityName;

		}
		return null;
	}
}
