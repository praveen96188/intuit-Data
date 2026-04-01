package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyAgencyPaymentTemplateAgencyId")]
    public class CompanyAgencyPaymentTemplateAgencyId {
        public var modifiedDate:Date;
        public var name:String;
        public var id:String;
        public var modifiedBy:String;
        [ArrayElementType("psp.sap.model.PaymentMethodAgencyIdRequirements")]
        public var paymentMethodRequirements:ArrayCollection;

        public var warningText:String = null;

        [Bindable("propertyChange")]
        public function get label():String {
            return name == null ? "Main Agency ID" : name;
        }
    }
}
