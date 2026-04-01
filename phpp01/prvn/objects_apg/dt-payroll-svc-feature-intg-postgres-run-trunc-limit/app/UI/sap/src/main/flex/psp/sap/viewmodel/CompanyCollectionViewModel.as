package psp.sap.viewmodel
{
	import psp.sap.model.Company;

	public class CompanyCollectionViewModel 
		extends ApplicationItemCollectionViewModel
	{
		public function CompanyCollectionViewModel(source:Array = null)
		{
			super(source);
		}
		
		public function set selectedCompany(company: Company): void {
			this.selectedItem = company;
		}
		
		[Bindable]
		public function get selectedCompany():Company {
			return Company(this.selectedItem);
		}
		
		public function getCompanyAt(index:int, prefetch:int=0): Company {
			return Company(this.getItemAt(index,prefetch));
		}		
	}
}