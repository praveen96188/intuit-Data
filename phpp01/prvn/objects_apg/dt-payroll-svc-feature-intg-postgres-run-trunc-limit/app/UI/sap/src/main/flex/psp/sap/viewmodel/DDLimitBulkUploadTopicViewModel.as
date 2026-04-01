package psp.sap.viewmodel
{
    import psp.sap.application.enums.RiskInspectorPageEnum;
    import psp.sap.application.enums.RiskInspectorTopicEnum;

    public class DDLimitBulkUploadTopicViewModel extends InspectorTopicViewModel
	{
		public function DDLimitBulkUploadTopicViewModel(inspector:AbstractInspectorViewModel)
		{
			super(inspector, RiskInspectorTopicEnum.DD_LIMIT_BULK_UPLOAD);
			addSinglePart(RiskInspectorPageEnum.DD_LIMIT_BULK_UPLOAD, DDLimitBulkUploadViewModel);
		}

	}
}