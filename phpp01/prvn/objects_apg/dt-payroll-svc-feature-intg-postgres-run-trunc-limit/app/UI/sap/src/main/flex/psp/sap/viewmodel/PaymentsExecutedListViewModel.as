/**
 * User: dweinberg
 * Date: 11/20/11
 * Time: 10:22 AM
 */
package psp.sap.viewmodel {
    public class PaymentsExecutedListViewModel extends AbstractPaymentsListViewModel {

        public const paymentMethodListExecuted:Array = ["", "CheckPayment", "SuperCheck", "ACHCredit", "ACHDebit"];

        public function PaymentsExecutedListViewModel() {
            super();
            agencyList.filterFunction = function(agency:String):Boolean {
                return agency != "IRS";
            };
            searchTypeString = "Executed";
        }
    }
}
