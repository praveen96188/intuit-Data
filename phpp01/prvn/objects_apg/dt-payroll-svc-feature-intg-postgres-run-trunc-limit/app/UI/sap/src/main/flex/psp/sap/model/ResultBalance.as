/**
 * Created by IntelliJ IDEA.
 * User: SachinS472
 * Date: 1/18/12
 * Time: 6:48 PM
 * To change this template use File | Settings | File Templates.
 */
package psp.sap.model {
[Bindable]
public class ResultBalance {
    public function ResultBalance() {

    }
        public var accountName:String;
        public var accountCode:String;
	    public var payrollBalance:Number;
	    public var companyBalance:Number;
    	public var lawBalance:Number;
        public var curPayrollBalance:Number;
	    public var curCompanyBalance:Number;
    	public var curLawBalance:Number;
        public var credit:Boolean;
        public var accountType:String;

}

}
