package psp.sap.viewmodel
{
	import psp.sap.application.SAP;
	import psp.sap.application.enums.OperationsEnum;

	public class OperatorInspectorViewModel extends AbstractInspectorViewModel
	{
		public function OperatorInspectorViewModel(explorer:AbstractExplorer)
		{
			super(explorer);
 			if(SAP.canPerformOperation(OperationsEnum.CONFIRM_OFFLOAD) && SAP.canPerformOperation(OperationsEnum.REQUEST_SECOND_OFFLOAD))
 			{
 				topics.addItem( new OperatorTopicViewModel(this) );
 			}

 			if(SAP.canPerformOperation(OperationsEnum.VIEW_OFFLOAD_STATUS))
 			{
 				topics.addItem( new ACHOffloadStatusTopicViewModel(this) );
 			}

            if(SAP.canPerformOperation(OperationsEnum.VIEW_CHECK_PRINT_QUEUE))
 			{
 				topics.addItem( new CheckPrintingTopicViewModel(this) );
 			}

            if (SAP.canPerformOperation(OperationsEnum.VIEW_OFFLOAD_STATUS)) {
                topics.addItem(new OperatorConnectionsTopicViewModel(this));
            }

            if (SAP.canPerformOperation(OperationsEnum.CONFIRM_OFFLOAD)) {
                topics.addItem(new OperatorEnrollmentsTopicViewModel(this));
            }
            if (SAP.canPerformOperation(OperationsEnum.SCHEDULE_ATF_EXTRACT)) {
                topics.addItem(new OperatorExtractsTopicViewModel(this));
            }
            if (SAP.canPerformOperation(OperationsEnum.LEDGER_OPERATIONS)) {
                topics.addItem(new OperatorLedgerTopicViewModel(this));
            }
            if (SAP.canPerformOperation(OperationsEnum.LEDGER_OPERATIONS)) {
                topics.addItem(new OperatorSUICreditsTopicViewModel(this));
            }


		}
	}
}
