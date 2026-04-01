package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.formatters.SAPDateFormatters;
    import psp.sap.model.EmployeeBankAccountHistoryItem;
    import psp.sap.model.VendorInfo;

    public class BanksVendorAccountHistoryViewModel extends AbstractPartViewModel
	{


		[Bindable] [BackingProperty(context=true)] public var vendorInfo:VendorInfo;

		[ArrayElementType("psp.sap.model.EmployeeBankAccountHistory")]
		[Bindable] public var vendorBankAccountHistories:ArrayCollection = new ArrayCollection();

		public function BanksVendorAccountHistoryViewModel()
		{
			this.label = CompanyInspectorPageEnum.BANKS_VENDOR_ACCOUNT_HISTORY;
            this.shallowCopyFields = ["sourceId", "name", "phone", "email"];
		}

        public static function createActivator(vendorInfo:VendorInfo):Object {
            return {"vendorInfo":vendorInfo};
        }

		override protected function loadModelData():void {
			vendorBankAccountHistories.removeAll();

			SAP.instance.companyService.getVendorBankAccountHistory(
					companyKey.sourceSystemCd, companyKey.companyId, vendorInfo.sourceId,
					createLoadModelDataResponder(onVendorBankAccountLoaded));
		}

		public function onVendorBankAccountLoaded(e:ResultEvent):void {
			vendorBankAccountHistories = e.result as ArrayCollection;
		}

		public function formatDate(item:EmployeeBankAccountHistoryItem):String {
	    	return SAPDateFormatters.dateTimeFormatDateOverTime.format(item.changeDate);
	  	}

	}
}