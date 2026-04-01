package psp.sap.model
{
	[Bindable]
	public class CompanyEventItemChild
	{
		public function CompanyEventItemChild(companyEventItem:CompanyEventItem)
		{
			item = companyEventItem;
		}
		
		public var item:CompanyEventItem;
	}
}