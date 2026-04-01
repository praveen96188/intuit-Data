/**
 * User: dweinberg
 * Date: 11/20/11
 * Time: 11:01 PM
 */
package psp.sap.viewmodel {

    public class PaymentsExceptionListViewModel extends AbstractPaymentsListViewModel {

        public const statusList:Array = ["","Rejected", "Returned"];

        public function PaymentsExceptionListViewModel() {
            super();

            agencyRequiredValidator.required = false;
            paymentTemplateRequiredValidator.required = false;

            searchTypeString = "Rejected";
            statusFieldDescription = "Status";
        }

    }
}
