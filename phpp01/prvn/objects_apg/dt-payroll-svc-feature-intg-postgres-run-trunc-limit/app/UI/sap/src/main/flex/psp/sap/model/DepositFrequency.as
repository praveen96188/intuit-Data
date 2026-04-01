package psp.sap.model {
    import flash.events.EventDispatcher;

    import psp.sap.application.SAP;
    import psp.sap.formatters.SAPDateFormatters;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPDepositFrequency")]
    public class DepositFrequency extends EventDispatcher {
        public var depositFrequency:String;

        public var effectiveDate:Date;
        public var modifiedDate:Date;
        public var modifierId:String;
        public var invalidDate:Date;
        public var isCurrent:Boolean;
        public var obsoleteFrequency:String;

        [Transient]
        public var effectiveDateString:String;
        [Transient]
        public var newDepositFrequency:String;
        [Transient]
        public var canDelete:Boolean;

        [Transient]
        public function get newEffectiveDateValue():Date {
            var formattedDate:String = SAPDateFormatters.dateFormatShort.format(effectiveDateString);
            var txDate:Date = SAP.instance.PSPDate;
            var time:Number = Date.parse(formattedDate);
            txDate.setTime(time);
            return txDate;
        }

        public function synchronizeTransients():void {
            effectiveDate = newEffectiveDateValue;
            depositFrequency = newDepositFrequency;
        }
    }
}