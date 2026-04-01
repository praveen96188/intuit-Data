package psp.sap.application.collections
{
	import intuit.sbd.flex.framework.application.collections.ArrayCollectionExt;
	
	import psp.sap.viewmodel.AbstractPartViewModel;

	public class PartViewModelCollection extends ArrayCollectionExt
	{
		public function PartViewModelCollection( source:Array=null)
		{
			super(AbstractPartViewModel, source);
		}
		
		public function getPart(partLabel:String, throwNotFound:Boolean = true):AbstractPartViewModel {
			for each (var viewModel:AbstractPartViewModel in this) {
				if (viewModel.label == partLabel)
					return viewModel;
			}
			if(throwNotFound){
				throw new Error("could not find page with label: " + partLabel);
			}
			return null;
		}
		
	}
}