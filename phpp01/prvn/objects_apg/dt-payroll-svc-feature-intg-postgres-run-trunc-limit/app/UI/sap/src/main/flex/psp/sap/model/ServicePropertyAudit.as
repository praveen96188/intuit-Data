package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPServicePropertyAudit")]
    public class ServicePropertyAudit extends PropertyAudit {
        public var serviceCd:String;

        [Transient]
        public function get serviceCodeEnum():ServiceCodeEnum {
            return ServiceCodeEnum.valueOf(serviceCd);
        }
    }
}