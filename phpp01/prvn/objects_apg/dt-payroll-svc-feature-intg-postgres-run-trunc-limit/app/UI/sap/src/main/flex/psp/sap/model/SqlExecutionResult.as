package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPSqlExecutionResult")]
    public class SqlExecutionResult {
        public var sqlStatement:String;
        public var reason:String;
        public var expectedRowCount:int;
        public var rowCount:int;
        public var executionTime:String;
        public var errorMessage:String;
    }
}