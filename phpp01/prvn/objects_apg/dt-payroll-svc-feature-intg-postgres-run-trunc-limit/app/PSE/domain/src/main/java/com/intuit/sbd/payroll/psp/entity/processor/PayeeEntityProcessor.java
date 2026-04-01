package com.intuit.sbd.payroll.psp.entity.processor;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.entity.EntityContext;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.event.spi.AbstractEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PayeeEntityProcessor extends BaseEntityProcessor<Payee> {

    private final List<Class> interestedEntityClasses;
    private final Map<String, String> attributesCdmMap;

    @Autowired
    public PayeeEntityProcessor(EntityProcessorUtility entityProcessorUtility) {
        super(entityProcessorUtility);
        interestedEntityClasses = Arrays.asList(Payee.class, PayeeBankAccount.class, BankAccount.class, Address.class);
        attributesCdmMap = new HashMap<String, String>()
        {{
            put("Name", "BusinessName");
            put("Is1099", "ContractorType");
            put("BankAccount", "BankAccount");
            put("BankAccount.AccountTypeCd", "BankAccount");
            put("BankAccount.AccountNumberEnc", "BankAccount");
            put("PayeeBankAccount", "BankAccount");
            put("PayeeBankAccount.AccountTypeCd", "BankAccount");
            put("PayeeBankAccount.AccountNumberEnc", "BankAccount");
            put("Address", "Address");
            put("Address.City", "Address");
            put("Address.Country", "Address");
            put("Address.State", "Address");
            put("Address.ZipCode", "Address");
            put("Address.AddressLine1", "Address");
            put("Address.AddressLine2", "Address");
            put("Address.AddressLine3", "Address");
            put("TaxIdEnc", "TaxId");
            put("Phone", "BusinessPhone");
            put("Email", "BusinessEmail");
        }};
    }

    @Override
    public List<Class> getInterestedEntities() {
        return interestedEntityClasses;
    }

    @Override
    protected Set<String> getAttributeFilters() {
        return attributesCdmMap.keySet();
    }

    @Override
    protected String getCdmAttributeName(String pspAttribute) {
        return attributesCdmMap.get(pspAttribute);
    }

    @Override
    public EntityContext<Payee> process(AbstractEvent abstractEvent) {
        return createEntityContext(abstractEvent);
    }

    @Override
    protected Class<?> getEntityType() {
        return Payee.class;
    }

    @Override
    protected Company getCompany(Payee entity) {
        return entity.getCompany();
    }

    @Override
    protected Payee getEntity(Object entity) {
        if (entity instanceof Payee) {
            return (Payee) entity;
        } else if (entity instanceof BankAccount) {
            return getPayee((BankAccount) entity);
        } else if (entity instanceof PayeeBankAccount) {
            return ((PayeeBankAccount) entity).getPayee();
        } else if(entity instanceof Address){
            return getPayeeFromAddress((Address) entity);
        }
        return null;
    }

    private Payee getPayee(BankAccount entity) {
        DomainEntitySet<PayeeBankAccount> payeeBankAccount = PayeeBankAccount.findPayeeBankAccount(entity);
        if (CollectionUtils.isEmpty(payeeBankAccount)){
            return null;
        }
        return payeeBankAccount.getFirst().getPayee();
    }

    private Payee getPayeeFromAddress(Address entity) {
        DomainEntitySet<Payee> payees = Payee.findPayees(entity);
        if (CollectionUtils.isEmpty(payees))
            return null;
        return payees.get(0);
    }
}
