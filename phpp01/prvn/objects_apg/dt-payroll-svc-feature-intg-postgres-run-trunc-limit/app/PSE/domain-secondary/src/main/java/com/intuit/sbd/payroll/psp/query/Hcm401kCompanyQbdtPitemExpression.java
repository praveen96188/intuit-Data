package com.intuit.sbd.payroll.psp.query;

import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kCompanyQbdtPitem;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kDeductionContributor;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

public class Hcm401kCompanyQbdtPitemExpression<Q> extends DomainEntityProperty<Q, Hcm401kCompanyQbdtPitem> {
    public Hcm401kCompanyQbdtPitemExpression() {
        super(null, "");
    }

    public Hcm401kCompanyQbdtPitemExpression(DomainEntityProperty<Q, ?> parentDataEntity, String propertyName) {
        super(parentDataEntity, propertyName);
    }

    public Hcm401kCompanyQbdtPitemExpression(DomainEntityProperty<Q, ?> parentDataEntity, String propertyName, boolean isImplementedAsCollection) {
           super(parentDataEntity, propertyName, isImplementedAsCollection);
    }

    public final ScalarProperty<Q, SpcfCalendar> CreatedDate() {return new ScalarProperty<Q, SpcfCalendar>(this, "CreatedDate");};
    public final ScalarProperty<Q, SpcfCalendar> ModifiedDate() {return new ScalarProperty<Q, SpcfCalendar>(this, "ModifiedDate");};
    public final ScalarProperty<Q, SpcfUniqueId> Id() {return new ScalarProperty<Q, SpcfUniqueId>(this, "Id");};
    public final ScalarProperty<Q, String> ModifierId() {return new ScalarProperty<Q, String>(this, "ModifierId");};
    public final ScalarProperty<Q, String> CreatorId() {return new ScalarProperty<Q, String>(this, "CreatorId");};

    public final ScalarProperty<Q, String> QbdtPitemId() {return new ScalarProperty<Q, String>(this, "QbdtPitemId");}public final ScalarProperty<Q, String> CompanyPayrollItemId() {return new ScalarProperty<Q, String>(this, "CompanyPayrollItemId");}public final ScalarProperty<Q, Hcm401kDeductionContributor> Hcm401kContributor() {return new ScalarProperty<Q, Hcm401kDeductionContributor>(this, "Hcm401kContributor");}
    public final Hcm401kCompanyPolicyExpression<Q> Hcm401kCompanyPolicy() {return new Hcm401kCompanyPolicyExpression<Q>(this, "Hcm401kCompanyPolicy");};
}