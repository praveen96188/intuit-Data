package psp.sap.model {
[Bindable]
[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPReportJob")]
public class ReportJob {
    public var reportName:String;
    public var shortDescription:String;
    public var description:String;

    [Transient]
    public function toString():String {
        return shortDescription;
    }
}
}
