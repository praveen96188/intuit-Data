package psp.sap.viewmodel
{
	import psp.sap.application.enums.RiskInspectorPageEnum;
	import psp.sap.application.enums.RiskInspectorTopicEnum;
	import psp.sap.model.BankReturn;

	public class BankReturnsTopicViewModel extends InspectorTopicViewModel
	{
		public function BankReturnsTopicViewModel(inspector:AbstractInspectorViewModel)
		{
			super(inspector, RiskInspectorTopicEnum.BANK_RETURNS);
			addSinglePart(RiskInspectorPageEnum.BANK_RETURNS, BankReturnsSearchViewModel);
            addSinglePart(RiskInspectorPageEnum.BANK_RETURN_STATUS_UPDATE, BankReturnsUpdateStatusViewModel);
		}

	}
}