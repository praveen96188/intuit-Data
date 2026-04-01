package psp.sap.application.collections
{
	import intuit.sbd.flex.framework.application.collections.ArrayCollectionExt;
	
	import psp.sap.viewmodel.InspectorTopicViewModel;

	public class CompanyInspectorTopicCollection extends ArrayCollectionExt
	{
		public function CompanyInspectorTopicCollection(source:Array=null)
		{
			super(InspectorTopicViewModel, source);
		}
		
		public function getTopic(inspectorTopicEnumVal:String):InspectorTopicViewModel {
			for each (var viewModel:InspectorTopicViewModel in this) {
				if (viewModel.label == inspectorTopicEnumVal) {
					return viewModel;
				}
			}
			
			throw new Error("could not find requested topic: " + inspectorTopicEnumVal);
		}
	}
}