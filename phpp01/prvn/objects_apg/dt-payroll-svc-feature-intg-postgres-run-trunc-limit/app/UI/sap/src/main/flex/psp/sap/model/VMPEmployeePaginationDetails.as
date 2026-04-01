/**
 * Created by arajendradeshpande on 4/24/2017.
 */
package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPVMPEmployeePaginationDetails")]
    public class VMPEmployeePaginationDetails {
        public var currentPage:Number=0;
        public var pageSize:Number=0;
        public var sortBy:String="";
        public var sortDesc:Boolean=false;
    }
}
