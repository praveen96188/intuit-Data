package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

/**
 * Hand-written business logic
 */
public class FraudAddress extends BaseFraudAddress {

	/**
	 * Default constructor.
	 */
	public FraudAddress()
	{
		super();
	}

    public FraudAddress(Company pCompany, Address pAddress) {
        super();

        if (pCompany != null && pAddress != null) {
            this.setCompany(pCompany);
            this.setAddressLine1(pAddress.getAddressLine1());
            this.setAddressLine2(pAddress.getAddressLine2());
            this.setAddressLine3(pAddress.getAddressLine3());
            this.setCity(pAddress.getCity());
            this.setState(pAddress.getState());
            this.setZipCode(pAddress.getZipCode());
        }
    }

    public static boolean exists(Company pCompany, Address pAddress) {
        return findFraudAddress(pCompany, pAddress) != null;
    }
    
    public static FraudAddress findFraudAddress(Company pCompany, Address pAddress) {
        Criterion<FraudAddress> where =
                FraudAddress.Company().equalTo(pCompany);

        if (pAddress.getAddressLine1() != null) {
            where = where.And(FraudAddress.AddressLine1().equalTo(pAddress.getAddressLine1()));
        } else {
            where = where.And(FraudAddress.AddressLine1().isNull());
        }

        if (pAddress.getAddressLine2() != null) {
            where = where.And(FraudAddress.AddressLine2().equalTo(pAddress.getAddressLine2()));
        } else {
            where = where.And(FraudAddress.AddressLine2().isNull());
        }

        if (pAddress.getAddressLine3() != null) {
            where = where.And(FraudAddress.AddressLine3().equalTo(pAddress.getAddressLine3()));
        } else {
            where = where.And(FraudAddress.AddressLine3().isNull());
        }

        if (pAddress.getCity() != null) {
            where = where.And(FraudAddress.City().equalTo(pAddress.getCity()));
        } else {
            where = where.And(FraudAddress.City().isNull());
        }

        if (pAddress.getState() != null) {
            where = where.And(FraudAddress.State().equalTo(pAddress.getState()));
        } else {
            where = where.And(FraudAddress.State().isNull());
        }

        if (pAddress.getZipCode() != null) {
            where = where.And(FraudAddress.ZipCode().equalTo(pAddress.getZipCode()));
        } else {
            where = where.And(FraudAddress.ZipCode().isNull());
        }

        DomainEntitySet<FraudAddress> fraudAddresses = Application.find(FraudAddress.class, where);

        return fraudAddresses.isEmpty() ? null : fraudAddresses.get(0);
    }

    /**
     * Searches for similar addresses.
     *
     * @return
     */
    public static DomainEntitySet<FraudAddress> findFraudAddressesLike(Address pAddress) {
        Criterion<FraudAddress> expr = null;
        if (pAddress.getAddressLine1() == null) {
            expr = AddressLine1().isNull();
        }
        else {
            expr = AddressLine1().like(pAddress.getAddressLine1());
        }

        if (pAddress.getAddressLine2() == null) {
            expr = expr.And(AddressLine2().isNull());
        }
        else {
            expr = expr.And(AddressLine2().like(pAddress.getAddressLine2()));
        }

        if (pAddress.getAddressLine3() == null) {
            expr = expr.And(AddressLine3().isNull());
        }
        else {
            expr = expr.And(AddressLine3().like(pAddress.getAddressLine3()));
        }

        if (pAddress.getCity() == null) {
            expr = expr.And(City().isNull());
        }
        else {
            expr = expr.And(City().like(pAddress.getCity()));
        }

        if (pAddress.getState() == null) {
            expr = expr.And(State().isNull());
        }
        else {
            expr = expr.And(State().like(pAddress.getState()));
        }

        if (pAddress.getZipCode() == null) {
            expr = expr.And(ZipCode().isNull());
        }
        else {
            expr = expr.And(ZipCode().like(pAddress.getZipCode()));
        }

        Expression<FraudAddress> query = new Query<FraudAddress>().Where(expr).EagerLoad(FraudAddress.Company()).LimitResults(0, 25);

        return Application.find(FraudAddress.class, query);
    }
}