package psp.sap.viewmodel {
	import psp.sap.application.enums.AccountingInspectorPageEnum;
	import psp.sap.application.enums.AccountingInspectorTopicEnum;

	public class BookTransfersTopicViewModel extends InspectorTopicViewModel {
		public function BookTransfersTopicViewModel(inspector:AbstractInspectorViewModel) {
			super(inspector, AccountingInspectorTopicEnum.BOOK_TRANSFERS);

			addSinglePart(AccountingInspectorPageEnum.BOOK_TRANSFERS, BookTransfersViewModel);
		}
	}
}
