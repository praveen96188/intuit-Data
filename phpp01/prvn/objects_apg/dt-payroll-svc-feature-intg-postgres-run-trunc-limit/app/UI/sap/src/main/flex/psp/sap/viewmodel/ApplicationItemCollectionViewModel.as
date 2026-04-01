package psp.sap.viewmodel
{
	import intuit.sbd.flex.framework.viewmodel.CollectionViewModel;
	
	import psp.sap.application.IApplicationItem;

	public class ApplicationItemCollectionViewModel extends CollectionViewModel
	{
		public function ApplicationItemCollectionViewModel(source:Array=null)
		{
			super(IApplicationItem, source);
		}
	
		public function getApplicationItem(index:int):IApplicationItem {
			return this.getItemAt(index) as IApplicationItem;
		}
	
		public function editSelected(): void {
			this.editItem(this.selectedIndex);
		}
		
		public function editItem(index: int): void {
			// TODO: argument checking here
			this.getApplicationItem(index).display();
		}	
		
	}
}