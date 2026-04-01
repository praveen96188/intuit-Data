package com.intuit.sbd.payroll.psp.query;

import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kCompanyPolicy;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kCompanyQbdtPitem;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kEmployeeDeduction;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

public class Hcm401kCompanyPolicyExpression<Q> extends DomainEntityProperty<Q, Hcm401kCompanyPolicy> {
    public Hcm401kCompanyPolicyExpression() {
        super(null, "");
    }

    public Hcm401kCompanyPolicyExpression(DomainEntityProperty<Q, ?> parentDataEntity, String propertyName) {
        super(parentDataEntity, propertyName);
    }

    public Hcm401kCompanyPolicyExpression(DomainEntityProperty<Q, ?> parentDataEntity, String propertyName, boolean isImplementedAsCollection) {
           super(parentDataEntity, propertyName, isImplementedAsCollection);
    }

    public final ScalarProperty<Q, SpcfCalendar> CreatedDate() {return new ScalarProperty<Q, SpcfCalendar>(this, "CreatedDate");};
    public final ScalarProperty<Q, SpcfCalendar> ModifiedDate() {return new ScalarProperty<Q, SpcfCalendar>(this, "ModifiedDate");};
    public final ScalarProperty<Q, SpcfUniqueId> Id() {return new ScalarProperty<Q, SpcfUniqueId>(this, "Id");};
    public final ScalarProperty<Q, String> ModifierId() {return new ScalarProperty<Q, String>(this, "ModifierId");};
    public final ScalarProperty<Q, String> CreatorId() {return new ScalarProperty<Q, String>(this, "CreatorId");};

    public final ScalarProperty<Q, String> CompanyId() {return new ScalarProperty<Q, String>(this, "CompanyId");}public final ScalarProperty<Q, Boolean> Active() {return new ScalarProperty<Q, Boolean>(this, "Active");}
    public final com.intuit.sbd.payroll.psp.query.Hcm401kPolicyExpression<Q> Hcm401kPolicy() {return new Hcm401kPolicyExpression<Q>(this, "Hcm401kPolicy");};
    public final com.intuit.sbd.payroll.psp.query.Hcm401kCompanyQbdtPitemDomainEntitySetProperty<Q, Hcm401kCompanyQbdtPitem> Hcm401kCompanyQbdtPitemSet() {return new Hcm401kCompanyQbdtPitemDomainEntitySetProperty<Q, Hcm401kCompanyQbdtPitem>(this, "Hcm401kCompanyQbdtPitemSet", "Hcm401kCompanyPolicy");}; public final com.intuit.sbd.payroll.psp.query.Hcm401kEmployeeDeductionDomainEntitySetProperty<Q, Hcm401kEmployeeDeduction> Hcm401kEmployeeDeductionSet() {return new Hcm401kEmployeeDeductionDomainEntitySetProperty<Q, Hcm401kEmployeeDeduction>(this, "Hcm401kEmployeeDeductionSet", "Hcm401kCompanyPolicy");};
}