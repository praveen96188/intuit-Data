package psp.sap.viewmodel
{
	import psp.sap.application.SAP;
	import psp.sap.application.enums.OperationsEnum;
	
	public class AccountingInspectorViewModel extends AbstractInspectorViewModel
	{
		public function AccountingInspectorViewModel(explorer:AbstractExplorer)
		{
			super(explorer);            
			
			if(SAP.canPerformOperation(OperationsEnum.UPLOAD_TO_GEMS)) {
				// commenting out tax stuff topics.addItem(new NonACHTaxPaymentsClearingTopicViewModel(this));
				topics.addItem( new UploadToGemsTopicViewModel(this));
			}					

            if(SAP.canPerformOperation(OperationsEnum.CREATE_GLOBAL_BOOK_TRANSFER)) {
            	topics.addItem( new BookTransfersTopicViewModel(this));
			}
		}

	}
}
