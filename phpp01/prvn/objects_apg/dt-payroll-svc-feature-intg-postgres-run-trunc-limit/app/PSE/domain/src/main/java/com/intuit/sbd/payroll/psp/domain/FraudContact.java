package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Criterion;

/**
 * Hand-written business logic
 */
public class FraudContact extends BaseFraudContact {

	/**
	 * Default constructor.
	 */
	public FraudContact()
	{
		super();
	}

    public static boolean exists(Company pCompany, Contact pContact) {
        return findFraudContact(pCompany, pContact) != null;
    }   

    public FraudContact(Company pCompany, Contact pContact) {
        super();
        
        if (pCompany != null && pContact != null) {
            this.setCompany(pCompany);
            this.setFirstName(pContact.getFirstName());
            this.setLastName(pContact.getLastName());
            this.setEmail(pContact.getEmail());
            this.setPhone(pContact.getPhone());
        }        
    }

    public static FraudContact findFraudContact(Company pCompany, Contact pContact) {
        Criterion<FraudContact> where =
                FraudContact.Company().equalTo(pCompany)
                        .And(FraudContact.FirstName().equalTo(pContact.getFirstName()))
                        .And(FraudContact.LastName().equalTo(pContact.getLastName()))
                        .And(FraudContact.Email().equalTo(pContact.getEmail()))
                        .And(FraudContact.Phone().equalTo(pContact.getPhone()));

        DomainEntitySet<FraudContact> fraudContacts = Application.find(FraudContact.class, where);

        return fraudContacts.isEmpty() ? null : fraudContacts.get(0);
    }
}