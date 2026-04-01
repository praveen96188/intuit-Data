package psp.sap.viewmodel
{
	import psp.sap.application.enums.AccountingInspectorPageEnum;
	import psp.sap.application.enums.AccountingInspectorTopicEnum;

	public class UploadToGemsTopicViewModel extends InspectorTopicViewModel
	{	
		public function UploadToGemsTopicViewModel(inspector:AbstractInspectorViewModel)
		{
			super(inspector, AccountingInspectorTopicEnum.UPLOAD_TO_GEMS);

            addSinglePart(AccountingInspectorPageEnum.UPLOAD_TO_GEMS, UploadToGemsViewModel);
		}
		
	}
}