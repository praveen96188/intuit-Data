package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	
	public class BanksCompanyAccountHistoryViewModel extends AbstractPartViewModel
	{
		
		
		[ArrayElementType("psp.sap.model.CompanyBankAccountHistory")]
		[Bindable] public var bankAccounts:ArrayCollection = new ArrayCollection();
		[Bindable] public var hasBankAccounts:Boolean = false;
		
		private var mSort:Sort = new Sort();
		
		public function BanksCompanyAccountHistoryViewModel()
		{
			this.label = CompanyInspectorPageEnum.BANKS_COMPANY_ACCOUNT_HISTORY;			
						
			// if the effective date is the same make sure "Active" is first
			mSort.fields = [new SortField("statusEffectiveDate", true, true), new SortField("bankAccountStatusCd", true, false)];
		}				
		
		
		override protected function loadModelData():void {
			bankAccounts.removeAll();
			
			SAP.instance.companyService.getCompanyBankAccountsHistory(
					company.sourceSystemCd, company.companyId,
					createLoadModelDataResponder(onCompanyBankAccountsLoaded));										
		}
		
		public function onCompanyBankAccountsLoaded(e:ResultEvent):void {
			var temp:ArrayCollection = e.result as ArrayCollection;
			temp.sort = mSort;
			temp.refresh();
			bankAccounts = temp;
			
		}
		
		override protected function initializeBackingProperties():void {
			hasBankAccounts = bankAccounts != null && bankAccounts.length > 0;
		}
		
		public function getAccountStatusLabel(status:String):String {
			if (status == "PendingVerification") {
				return "Pending Verification";
			} else {
				return status;
			}
		}

	}
}