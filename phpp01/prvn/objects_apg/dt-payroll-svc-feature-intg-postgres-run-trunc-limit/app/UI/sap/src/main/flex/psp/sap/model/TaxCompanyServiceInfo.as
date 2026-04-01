package psp.sap.model
{
import mx.controls.DateField;

[Bindable]
[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPTaxCompanyServiceInfo")]
public class TaxCompanyServiceInfo extends AbstractCompanyServiceInfo
{
    public var lastTaxQuarter:String;
    public var fileAnnualReturns:Boolean;
    public var isFinal:Boolean;
    public var lastPayrollDate:Date;

    public static const DO_NOT_FILE:int = -1;
    public static const LAST_QUARTER_UNSET:int = 0;

    public function strLastTaxQuarter():String {
        if (lastTaxQuarter == DO_NOT_FILE.toString()) {
            return "Do not file";
        } else if (lastTaxQuarter != null && lastTaxQuarter.length > 4) {
            return lastTaxQuarter.substr(0,4) + " Q" + lastTaxQuarter.charAt(4);
        }
        return "";
    }

    public function strLastPayrollDate():String{
        return(lastPayrollDate == null ? "None" : DateField.dateToString(lastPayrollDate, "MM/DD/YYYY"))
    }

}
}