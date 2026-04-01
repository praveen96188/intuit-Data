package psp.sap.viewmodel {
import psp.sap.application.enums.PaymentsPageEnum;

public class PaymentsViewModel extends CompositePartViewModel
	{
		public function PaymentsViewModel()
		{
			super();

			bindSaveMessageWithChildren = true;

			this.label = PaymentsPageEnum.TAX_PAYMENTS;			

			var tabNavigatorVM:PartsTabNavigatorViewModel = addPartsTabNavigator(PaymentsPageEnum.TAX_PAYMENTS);

			var pendingListViewModel:PaymentsPendingListViewModel = tabNavigatorVM.addNewPart(PaymentsPendingListViewModel, PaymentsPageEnum.PENDING) as PaymentsPendingListViewModel;
            tabNavigatorVM.addNewPart(PaymentsExecutedListViewModel, PaymentsPageEnum.EXECUTED_PAYMENTS);
			tabNavigatorVM.addNewPart(PaymentsExceptionListViewModel, PaymentsPageEnum.EXCEPTIONS);

			tabNavigatorVM.defaultSinglePart = pendingListViewModel;
		}

	}

}