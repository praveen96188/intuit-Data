package com.intuit.sbd.payroll.psp.query;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.sbd.payroll.psp.domain.*;

public class $bedlDataEntity.className$Expression<Q> extends DomainEntityProperty<Q, $bedlDataEntity.className$> {
    public $bedlDataEntity.className$Expression() {
        super(null, "");
    }

    public $bedlDataEntity.className$Expression(DomainEntityProperty<Q, ?> parentDataEntity, String propertyName) {
        super(parentDataEntity, propertyName);
    }

    public $bedlDataEntity.className$Expression(DomainEntityProperty<Q, ?> parentDataEntity, String propertyName, boolean isImplementedAsCollection) {
           super(parentDataEntity, propertyName, isImplementedAsCollection);
    }

    $if(bedlDataEntity.isEntity)$
    public final ScalarProperty<Q, SpcfCalendar> CreatedDate() {return new ScalarProperty<Q, SpcfCalendar>(this, "CreatedDate");};
    public final ScalarProperty<Q, SpcfCalendar> ModifiedDate() {return new ScalarProperty<Q, SpcfCalendar>(this, "ModifiedDate");};
    public final ScalarProperty<Q, SpcfUniqueId> Id() {return new ScalarProperty<Q, SpcfUniqueId>(this, "Id");};
    public final ScalarProperty<Q, String> ModifierId() {return new ScalarProperty<Q, String>(this, "ModifierId");};
    public final ScalarProperty<Q, String> CreatorId() {return new ScalarProperty<Q, String>(this, "CreatorId");};
    $endif$

    $bedlDataEntity.ScalarProperties:{k|public final $if(bedlDataEntity.isComplexObject)$Component$endif$ScalarProperty<Q, $if(k.isClob)$ String $else$ $k.PropertyReferenceType$ $endif$> $k.PropertyName$() {return new $if(bedlDataEntity.isComplexObject)$Component$endif$ScalarProperty<Q, $if(k.isClob)$ String $else$ $k.PropertyReferenceType$ $endif$>(this, "$k.PropertyName$");}}; separator=""$
    $bedlDataEntity.OneToOneProperties:{k|public final $k.PropertyReferenceType$Expression<Q> $k.PropertyName$() {return new $k.PropertyReferenceType$Expression<Q>(this, "$k.PropertyName$", $k.isNotOwner$);}; }; separator=""$
    $bedlDataEntity.ManyToOneProperties:{k|public final $k.PropertyReferenceType$Expression<Q> $k.PropertyName$() {return new $k.PropertyReferenceType$Expression<Q>(this, "$k.PropertyName$");}; }; separator=""$
    $bedlDataEntity.OneToManyProperties:{k|public final $k.PropertyReferenceType$DomainEntitySetProperty<Q, $k.PropertyReferenceType$> $k.PropertyName$Set() {return new $k.PropertyReferenceType$DomainEntitySetProperty<Q, $k.PropertyReferenceType$>(this, "$k.PropertyName$Set", "$k.InversePropertyName$");}; }; separator=""$
    $bedlDataEntity.ManyToManyProperties:{k|public final DomainEntitySetProperty<Q, $k.PropertyReferenceType$> $k.PropertyName$Set() {return new DomainEntitySetProperty<Q, $k.PropertyReferenceType$>(this, "$k.PropertyName$Set");}; }; separator=""$
}
