package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPItem")]
    public class PItem {
        public var pitemNumber:Number;
        public var pitemName:String;
        public var pitemDescription:String;
        public var pitemType:String;
        public var status:String;
        public var taxFormLine:String;
        public var w2Code:String;
        public var taxability:String;
        public var coaExpense:String;
        public var coaLiability:String;
        public var taxableToLawIds:ArrayCollection;
        public var latestId:String;
        public var taxabilityHeader:String;
        public var groupTitle:String;
        public var deleteStatus:String;
        public var token:String;
    }
}