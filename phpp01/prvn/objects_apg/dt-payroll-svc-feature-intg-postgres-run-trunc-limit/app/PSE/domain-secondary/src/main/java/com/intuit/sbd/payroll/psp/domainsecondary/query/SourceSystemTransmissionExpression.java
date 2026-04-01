package com.intuit.sbd.payroll.psp.domainsecondary.query;

import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.query.DomainEntityProperty;
import com.intuit.sbd.payroll.psp.query.ScalarProperty;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

public class SourceSystemTransmissionExpression<Q> extends DomainEntityProperty<Q, SourceSystemTransmission> {
    public SourceSystemTransmissionExpression() {
        super(null, "");
    }

    public SourceSystemTransmissionExpression(DomainEntityProperty<Q, ?> parentDataEntity, String propertyName) {
        super(parentDataEntity, propertyName);
    }

    public SourceSystemTransmissionExpression(DomainEntityProperty<Q, ?> parentDataEntity, String propertyName, boolean isImplementedAsCollection) {
           super(parentDataEntity, propertyName, isImplementedAsCollection);
    }

    public final ScalarProperty<Q, SpcfCalendar> CreatedDate() {return new ScalarProperty<Q, SpcfCalendar>(this, "CreatedDate");};
    public final ScalarProperty<Q, SpcfCalendar> ModifiedDate() {return new ScalarProperty<Q, SpcfCalendar>(this, "ModifiedDate");};
    public final ScalarProperty<Q, SpcfUniqueId> Id() {return new ScalarProperty<Q, SpcfUniqueId>(this, "Id");};
    public final ScalarProperty<Q, String> ModifierId() {return new ScalarProperty<Q, String>(this, "ModifierId");};
    public final ScalarProperty<Q, String> CreatorId() {return new ScalarProperty<Q, String>(this, "CreatorId");};

    public final ScalarProperty<Q, String> Host() {return new ScalarProperty<Q, String>(this, "Host");}public final ScalarProperty<Q, SourceSystemCode> FromSourceSystem() {return new ScalarProperty<Q, SourceSystemCode>(this, "FromSourceSystem");}public final ScalarProperty<Q, SpcfCalendar> FinalizeDateTime() {return new ScalarProperty<Q, SpcfCalendar>(this, "FinalizeDateTime");}public final ScalarProperty<Q, Long> RequestToken() {return new ScalarProperty<Q, Long>(this, "RequestToken");}public final ScalarProperty<Q, Long> ResponseToken() {return new ScalarProperty<Q, Long>(this, "ResponseToken");}public final ScalarProperty<Q, String> RequestDocument() {return new ScalarProperty<Q, String>(this, "RequestDocument");}public final ScalarProperty<Q, String> ResponseDocument() {return new ScalarProperty<Q, String>(this, "ResponseDocument");}public final ScalarProperty<Q, TransmissionType> Type() {return new ScalarProperty<Q, TransmissionType>(this, "Type");}public final ScalarProperty<Q, SpcfCalendar> InitializeDateTime() {return new ScalarProperty<Q, SpcfCalendar>(this, "InitializeDateTime");}public final ScalarProperty<Q, String> Description() {return new ScalarProperty<Q, String>(this, "Description");}public final ScalarProperty<Q, SourceSystemCode> ToSourceSystem() {return new ScalarProperty<Q, SourceSystemCode>(this, "ToSourceSystem");}public final ScalarProperty<Q, String> TransmissionIdentifier() {return new ScalarProperty<Q, String>(this, "TransmissionIdentifier");}public final ScalarProperty<Q, String> IPAddress() {return new ScalarProperty<Q, String>(this, "IPAddress");}public final ScalarProperty<Q, String> ApplicationVersion() {return new ScalarProperty<Q, String>(this, "ApplicationVersion");}public final ScalarProperty<Q, String> ApplicationId() {return new ScalarProperty<Q, String>(this, "ApplicationId");}public final ScalarProperty<Q, String> TaxTableId() {return new ScalarProperty<Q, String>(this, "TaxTableId");}public final ScalarProperty<Q, String> CompanyId() {return new ScalarProperty<Q, String>(this, "CompanyId");}
}