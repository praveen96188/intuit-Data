package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPAddCompany")]
    public class AddCompany {

        public var licenseNumber:String;
        public var eoc:String;
        public var itemNumber:String;
        public var serviceAccountId:String;
        public var priceType:String;
        public var offerCode:String;
        public var oldEIN:String;
        public var einEffectiveDate:Date;
        public var isSuccessorEntityChange:Boolean;

        [ArrayElementType("psp.sap.model.Contact")]
        public var contacts:ArrayCollection;
        public var legalInfo:CompanyLegalInfo;


    }
}