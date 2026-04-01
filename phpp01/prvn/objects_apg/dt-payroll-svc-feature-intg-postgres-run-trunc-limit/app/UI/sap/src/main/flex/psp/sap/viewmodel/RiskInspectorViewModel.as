package psp.sap.viewmodel
{
	import psp.sap.application.SAP;
	import psp.sap.application.enums.OperationsEnum;
	
	public class RiskInspectorViewModel extends AbstractInspectorViewModel
	{
		public function RiskInspectorViewModel(explorer:AbstractExplorer)
		{
			super(explorer);
			
			topics.addItem(new CompanyStatusSearchTopicViewModel(this));
            if (SAP.canPerformOperation(OperationsEnum.IP_BASED_FRAUD_FILTERING_VIEW)) {
                topics.addItem( new IPBasedFSTopicViewModel(this));
            }


            if (SAP.canPerformOperation(OperationsEnum.VIEW_SIGNUP_FRAUD_QUEUE)) {
				topics.addItem(new FraudTopicViewModel(this));
                topics.addItem(new RiskBankAccountSearchTopicViewModel(this))
			}
			
			if(SAP.canPerformOperation(OperationsEnum.VIEW_BANK_RETURNS)) {
				topics.addItem(new BankReturnsTopicViewModel(this));	
			}

            if(SAP.canPerformOperation(OperationsEnum.REMOVE_FROM_SIGNUP_FRAUD_HOLD)) {
                topics.addItem(new DDLimitBulkUploadTopicViewModel(this));
            }

		}
		
		override public function permissionGranted():Boolean {
			//todo who can see this one?
			return SAP.canPerformOperation(OperationsEnum.ACCESS_APPLICATION);						
		}		

	}
}