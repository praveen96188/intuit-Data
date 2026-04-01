package com.intuit.sbd.payroll.psp.query;

import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kAmountType;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kDeductionContributor;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kEmployeeDeduction;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

public class Hcm401kEmployeeDeductionExpression<Q> extends DomainEntityProperty<Q, Hcm401kEmployeeDeduction> {
    public Hcm401kEmployeeDeductionExpression() {
        super(null, "");
    }

    public Hcm401kEmployeeDeductionExpression(DomainEntityProperty<Q, ?> parentDataEntity, String propertyName) {
        super(parentDataEntity, propertyName);
    }

    public Hcm401kEmployeeDeductionExpression(DomainEntityProperty<Q, ?> parentDataEntity, String propertyName, boolean isImplementedAsCollection) {
           super(parentDataEntity, propertyName, isImplementedAsCollection);
    }

    public final ScalarProperty<Q, SpcfCalendar> CreatedDate() {return new ScalarProperty<Q, SpcfCalendar>(this, "CreatedDate");};
    public final ScalarProperty<Q, SpcfCalendar> ModifiedDate() {return new ScalarProperty<Q, SpcfCalendar>(this, "ModifiedDate");};
    public final ScalarProperty<Q, SpcfUniqueId> Id() {return new ScalarProperty<Q, SpcfUniqueId>(this, "Id");};
    public final ScalarProperty<Q, String> ModifierId() {return new ScalarProperty<Q, String>(this, "ModifierId");};
    public final ScalarProperty<Q, String> CreatorId() {return new ScalarProperty<Q, String>(this, "CreatorId");};

    public final ScalarProperty<Q, String> EmployeeId() {return new ScalarProperty<Q, String>(this, "EmployeeId");}public final ScalarProperty<Q, Double> Amount() {return new ScalarProperty<Q, Double>(this, "Amount");}public final ScalarProperty<Q, Hcm401kAmountType> Hcm401kAmountType() {return new ScalarProperty<Q, Hcm401kAmountType>(this, "Hcm401kAmountType");}public final ScalarProperty<Q, Double> MaxAmount() {return new ScalarProperty<Q, Double>(this, "MaxAmount");}public final ScalarProperty<Q, Hcm401kDeductionContributor> Hcm401kDeductionContributor() {return new ScalarProperty<Q, Hcm401kDeductionContributor>(this, "Hcm401kDeductionContributor");}public final ScalarProperty<Q, Boolean> Active() {return new ScalarProperty<Q, Boolean>(this, "Active");}
    public final Hcm401kCompanyPolicyExpression<Q> Hcm401kCompanyPolicy() {return new Hcm401kCompanyPolicyExpression<Q>(this, "Hcm401kCompanyPolicy");};
}