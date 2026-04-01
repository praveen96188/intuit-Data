package psp.sap.viewmodel
{
    import mx.rpc.Responder;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.model.PSPSystemInformation;

    public class PSPSystemInformationViewModel extends CompositePartViewModel
	{
		private var mSystemInfo:PSPSystemInformation;
		private var mIsLoading:Boolean = false;
		private var mLoadFaultDetail:String = "";

        private var mPTSDPopUp:PopUpPartViewModel;
        private var mPTSDPopUpViewModel:PaymentTemplateSupportDatePopUpViewModel;

		public function PSPSystemInformationViewModel()
		{
            super();
            mPTSDPopUp = addPopUpPart(CompanyInspectorPageEnum.PSP_PAYMENT_TEMPLATE_SUPPORT_DATES);
            mPTSDPopUpViewModel = mPTSDPopUp.addNewPart(PaymentTemplateSupportDatePopUpViewModel, CompanyInspectorPageEnum.PSP_PAYMENT_TEMPLATE_SUPPORT_DATES) as PaymentTemplateSupportDatePopUpViewModel;
		}
		
		[Bindable("propertyChange")]
		public function get title():String {
			return "PSP System Information";
		}
		
		[Bindable]
		public function get systemInformation():PSPSystemInformation {
			return mSystemInfo;
		}
		
		protected function set systemInformation(value:PSPSystemInformation):void {
			mSystemInfo = value;
		}
		
		[Bindable]
		public function get loadFaultDetail():String {
			return mLoadFaultDetail;
		}
		
		protected function set loadFaultDetail(value:String):void {
			mLoadFaultDetail = value;
		}
		
		[Bindable]
		public function get isLoading():Boolean {
			return mIsLoading;	
		}
		
		protected function set isLoading(value:Boolean):void {
			mIsLoading = value;
		}
		
		public function get clipboardContent():String {
			return systemInformation.toString();
		}
		
		override protected function loadModelData():void {
			mIsLoading = true;
			mLoadFaultDetail = "";
			var responder:Responder = new Responder(onSystemInformationLoaded, onSystemInformationLoadFaulted);			
			SAP.instance.systemInformationService.getSystemInformation(responder);
		}
		
		protected function onSystemInformationLoaded(e:ResultEvent):void {
			systemInformation = e.result as PSPSystemInformation;
			mIsLoading = false;						
		}
		
		protected function onSystemInformationLoadFaulted(e:FaultEvent):void {
			systemInformation = null;
			loadFaultDetail = e.fault.faultString;
			mIsLoading = false;			
		}

        public function showPTSD_PopUp():void {
            mPTSDPopUp.displayPopUp();
        }
        
		
	}
}
