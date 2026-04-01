package com.intuit.sbd.payroll.psp.query;

import com.intuit.sbd.payroll.psp.domainsecondary.DeductionItemPolicy;
import com.intuit.sbd.payroll.psp.domainsecondary.DeductionItemProvider;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kCompanyPolicy;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kPolicy;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

public class Hcm401kPolicyExpression<Q> extends DomainEntityProperty<Q, Hcm401kPolicy> {
    public Hcm401kPolicyExpression() {
        super(null, "");
    }

    public Hcm401kPolicyExpression(DomainEntityProperty<Q, ?> parentDataEntity, String propertyName) {
        super(parentDataEntity, propertyName);
    }

    public Hcm401kPolicyExpression(DomainEntityProperty<Q, ?> parentDataEntity, String propertyName, boolean isImplementedAsCollection) {
           super(parentDataEntity, propertyName, isImplementedAsCollection);
    }

    public final ScalarProperty<Q, SpcfCalendar> CreatedDate() {return new ScalarProperty<Q, SpcfCalendar>(this, "CreatedDate");};
    public final ScalarProperty<Q, SpcfCalendar> ModifiedDate() {return new ScalarProperty<Q, SpcfCalendar>(this, "ModifiedDate");};
    public final ScalarProperty<Q, SpcfUniqueId> Id() {return new ScalarProperty<Q, SpcfUniqueId>(this, "Id");};
    public final ScalarProperty<Q, String> ModifierId() {return new ScalarProperty<Q, String>(this, "ModifierId");};
    public final ScalarProperty<Q, String> CreatorId() {return new ScalarProperty<Q, String>(this, "CreatorId");};

    public final ScalarProperty<Q, DeductionItemPolicy> DeductionItemPolicy() {return new ScalarProperty<Q, DeductionItemPolicy>(this, "DeductionItemPolicy");}public final ScalarProperty<Q, String> Description() {return new ScalarProperty<Q, String>(this, "Description");}public final ScalarProperty<Q, DeductionItemProvider> DeductionItemProvider() {return new ScalarProperty<Q, DeductionItemProvider>(this, "DeductionItemProvider");}
    public final Hcm401kCompanyPolicyDomainEntitySetProperty<Q, Hcm401kCompanyPolicy> Hcm401kCompanyPolicySet() {return new Hcm401kCompanyPolicyDomainEntitySetProperty<Q, Hcm401kCompanyPolicy>(this, "Hcm401kCompanyPolicySet", "Hcm401kPolicy");};
}