package psp.sap.viewmodel {
import psp.sap.application.enums.PaymentsPageEnum;

    public class PaymentsTopicViewModel extends InspectorTopicViewModel {
        public function PaymentsTopicViewModel(inspector:AbstractInspectorViewModel) {
            super(inspector, PaymentsPageEnum.TAX_PAYMENTS);

            addSinglePart(PaymentsPageEnum.TAX_PAYMENTS, PaymentsViewModel);
        }
    }
}