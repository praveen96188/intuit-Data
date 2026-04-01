/**
 * User: dweinberg
 * Date: 10/11/12
 * Time: 10:40 AM
 */
package psp.sap.model {
    import psp.sap.formatters.SAPDateFormatters;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPQuickBooksFileId")]
    public class QuickBooksFileId {
        public var fileId:String;
        public var lastDate:Date;

        [Transient] public function get label():String {
            if (fileId == null || fileId == "") {
                return "";
            }
            return fileId + " (Last sent " + SAPDateFormatters.dateFormatShort.format(lastDate) + ")";
        }
    }
}
