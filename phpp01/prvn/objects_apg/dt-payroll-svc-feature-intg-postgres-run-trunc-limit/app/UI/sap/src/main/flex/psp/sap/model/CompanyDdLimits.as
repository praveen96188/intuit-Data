package psp.sap.model {
    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyDdLimits")]
    public class CompanyDdLimits {
        public var perPayrollLimit:Number;
        public var perEmployeeLimit:Number;

        public var defaultEmployeeLimit:Number;
        public var defaultPayrollLimit:Number;

        [Transient]
        public function get isUsingDefaultEmployeeLimit():Boolean {
            return perEmployeeLimit == -1;
        }

        [Transient]
        public function get isUsingDefaultPayrollLimit():Boolean {
            return perPayrollLimit == -1;
        }
    }
}