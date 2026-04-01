package com.intuit.sbd.payroll.psp.hibernate;

import java.io.Serializable;
import java.util.Objects;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BankAccountStatus;
import com.intuit.sbd.payroll.psp.domain.EventEnumType;
import com.intuit.sbd.payroll.psp.domain.Status;

import org.hibernate.event.internal.DefaultSaveEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.apache.commons.lang3.EnumUtils;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.SaveOrUpdateEvent;

import com.google.gson.JsonObject;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.util.EntityListenerFactory;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.Session;
/**
 * This class is a custom hibernate listener which gets triggered for all
 * inserts and used to update table PSP_ENTITY_UPDATE with event updates for the
 * entities configured
 *
 * @author dchoudhary1
 *
 */
public class PSPEntityInsertEventListener extends DefaultSaveEventListener {

	private static final SpcfLogger logger = Application.getLogger(PSPEntityInsertEventListener.class);

	@Override
	public Serializable performSaveOrUpdate(SaveOrUpdateEvent event) {
		try {
			logger.debug("Begin PSPEntityInsertEventListener");

			Company company = null;

			Object entity = event.getEntity();
			DomainEntitySet<EntityUpdate> entityMessage = null;

			// By default set the EventType as EntityCreate and override if its update event
			EventEnumType evnttype = EventEnumType.EntityCreate;
			EntityChangeListener entityChange = EntityListenerFactory.getentityClass(entity);

			// save entity in cache if entity and id exists then we should
			// override the value
			// Note tat to update the table - we will be seeing
			// when entityupdate
			// --com.intuit.sbd.payroll.psp.domain.EntityUpdate
			// and that the same entity update id then we get id and update the
			// changed attribues
			// always latest value will override

			if ((entityChange != null) && (entityChange.getEntitiesName() != null
					&& !entityChange.getEntitiesName().equals("EntityUpdate"))) {

				String changedAttr = entityChange.getChangedAttribute().toString();


				if (entityChange.getDuplicate()) {
					// search for the record in entityupdate

					String cacheKey = "EntityUpdate:" + "Entityid" + entityChange.getuniqueId();

					String oldId = Application.getSessionCache().getNonHibernateObject(cacheKey);

					if (oldId != null) {

						EntityUpdate findEntityUpdate = Application.findById(EntityUpdate.class,
								SpcfUniqueId.createInstance(oldId));

						//findEntityUpdate is not null
						if (findEntityUpdate != null) {
							findEntityUpdate.setChangedAttributes(changedAttr);
							Application.save(findEntityUpdate);
							return super.performSaveOrUpdate(event);
						}
					}

				}

				// create an entry in the entity update table
				// Entity update is determined by checking if the concerned entity exists in the DB else its entity insert
				if(entityChange.getEntitiesName().equals("EmployeeBankAccount")){
					EmployeeBankAccount empBankAcct = getEntityOld(EmployeeBankAccount.class, entityChange.getuniqueId());
					EmployeeBankAccount employeeBankAccountToUpdate=(EmployeeBankAccount)entityChange;
					 if(empBankAcct!=null){
						 evnttype = EventEnumType.EntityUpdate;
						 if(empBankAcct.isEqual(employeeBankAccountToUpdate)){
							 logger.info("skip this bank account update as its same copy");
							 return super.performSaveOrUpdate(event);
						 }
					 }

					Employee employee = employeeBankAccountToUpdate.getEmployee();
					if(Objects.nonNull(employee)){
						company = employee.getCompany();
					}
				}else if(entityChange.getEntitiesName().equals("Paycheck")){
					Paycheck paycheck = getEntityOld(Paycheck.class, entityChange.getuniqueId());
					Paycheck paycheckToUpdate=(Paycheck)entityChange;
					 if(paycheck!=null){
						 evnttype = EventEnumType.EntityUpdate;
						 if(paycheck.isEqual(paycheckToUpdate)){
							 logger.info("skip this paycheck update as its same copy");
							 return super.performSaveOrUpdate(event);
						 }
					 }
					company = paycheckToUpdate.getCompany();
				}else if(entityChange.getEntitiesName().equals("PayeeBankAccount")){
					PayeeBankAccount payeeBankAccount = getEntityOld(PayeeBankAccount.class, entityChange.getuniqueId());
					if(payeeBankAccount != null){
						logger.info("skip updates for PayeeBankAccount");
						return super.performSaveOrUpdate(event);
					}
					PayeeBankAccount payeeBankAccountToUpdate=(PayeeBankAccount)entityChange;
					Payee payee = payeeBankAccountToUpdate.getPayee();
					if(Objects.nonNull(payee)){
						company = payee.getCompany();
					}
				}else if(entityChange.getEntitiesName().equals("BankAccount")){
					BankAccount bankAccount = getEntityOld(BankAccount.class, entityChange.getuniqueId());
					 if(bankAccount!=null){
						 evnttype = EventEnumType.EntityUpdate;
						 BankAccount bankaccountToUpdate=(BankAccount)entityChange;
						 company = bankaccountToUpdate.getAssociatedCompany();
						 if(bankAccount.isEqual(bankaccountToUpdate)){
							 logger.info("skip this bankaccount update as its same copy");
							 return super.performSaveOrUpdate(event);
						 }
					 } else{
						 // skip events for bank account
						 logger.info("skip create events for bankaccount to avoid duplicates");
						 return super.performSaveOrUpdate(event);
					 }
				} else if (entityChange.getEntitiesName().equals("CompanyBankAccount")) {
					CompanyBankAccount companyBankAccount = getEntityOld(CompanyBankAccount.class, entityChange.getuniqueId());
					CompanyBankAccount companyBankAccountToUpdate=(CompanyBankAccount)entityChange;
					company = companyBankAccountToUpdate.getCompany();
					if(companyBankAccount != null){
						evnttype = EventEnumType.EntityUpdate;
					}
				} else if (entityChange.getEntitiesName().equals("BillPayment")) {
					BillPayment billPayment = getEntityOld(BillPayment.class, entityChange.getuniqueId());
					BillPayment billPaymentToUpdate = (BillPayment)entityChange;
					Payee payee = billPaymentToUpdate.getPayee();
					if(Objects.nonNull(payee)){
						company = payee.getCompany();
					}
					if(billPayment != null){
						evnttype = EventEnumType.EntityUpdate;
					}
				}


				logger.info("Action=performSaveOrUpdate entityUpdate is saved with EntityId="+entityChange.getuniqueId()
						+ " EntityType="+entityChange.getEntitiesName() + " EventType=" + evnttype);

				EntityUpdate entityUpdate = new EntityUpdate();
				entityUpdate.setCompany(company);
				entityUpdate.setEventType(evnttype);
				entityUpdate.setEntityId(entityChange.getuniqueId());
				entityUpdate.setEntityName(entityChange.getEntitiesName());
				entityUpdate.setChangedAttributes(changedAttr);
				entityUpdate.setRetryCount(0);
				entityUpdate.setStatus(Status.Created);
				Application.save(entityUpdate);

			}

			return super.performSaveOrUpdate(event);

		} catch (Exception exception) {
			logger.error("Exception occurred while saving entity", exception);
			return super.performSaveOrUpdate(event);
		}

	}

	/**
	 * Fetches the entity from the DB
	 * @param entityType Type of the entity to fetch
	 * @param id Primarykey of the entity
	 * @return Entity
	 * @param <T>
	 */
	private <T> T getEntityOld(Class<T> entityType, String id) {

		T entity = null;
		Session session = null;

		try {
			session = HibernateUtils.getSessionFactory().openSession();
			session.beginTransaction();
			entity = Application.getActualObject(session.get(entityType, SpcfUniqueId.createInstance(id)));
		}catch (Exception e){
			logger.warn("Action=getEntityOld_Exception, EntityType="+entityType.getClass().getName() + " ,EntityId=" + id, e);
		} finally {
			session.close();
		}
		return entity;
	}


}