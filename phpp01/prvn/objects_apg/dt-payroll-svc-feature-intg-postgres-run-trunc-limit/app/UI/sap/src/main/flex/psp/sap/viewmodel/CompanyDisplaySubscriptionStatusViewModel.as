package psp.sap.viewmodel {
    import flash.display.Bitmap;
    import flash.display.Loader;
    import flash.events.Event;
    import flash.events.IOErrorEvent;
    import flash.utils.ByteArray;

    import mx.rpc.Responder;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.model.ServiceCodeEnum;

    public class CompanyDisplaySubscriptionStatusViewModel extends CompanyEditSubscriptionStatusViewModel{

        private var mLoader:Loader;

        [Bindable] public var signatureBitmap:Bitmap;
        [Bindable] public var workersCompInfo:XML;

        [Bindable] public var caseId:String;

        public function CompanyDisplaySubscriptionStatusViewModel() {
            mLoader = new Loader();
            mLoader.contentLoaderInfo.addEventListener(Event.COMPLETE, onLoaderComplete);
            mLoader.contentLoaderInfo.addEventListener(IOErrorEvent.IO_ERROR, onIOError);
        }

        private function onLoaderComplete(e:Event):void {
            var loader:Loader = Loader(e.target.loader);
            signatureBitmap = Bitmap(loader.content);
            dispatchEvent(new Event("bitmapReady"));
        }

        private function onIOError(e:IOErrorEvent):void {
            saveFaulted = true;
            saveMsg = "Error creating signature bitmap" + e.text;
        }

        override protected function get findTransitions():Boolean {
            return false;
        }

        public function addService(serviceCode:String):void {
            // show progress bar
			SAP.instance.showProgress("Adding Service...");
			// reset error string
			saveMsg = "";
			SAP.instance.companyService.addServiceToCompany(companyKey.sourceSystemCd,
                                                              companyKey.companyId,
                                                              serviceCode,
                                                              caseId,
                                                              createSaveResponder(onServiceAdded));
        }

		protected function onServiceAdded(e:ResultEvent=null):void {
			super.dispatchCompanySavedEvent();			
			refresh();
		}

        public function getSignatureImage():void {
            SAP.instance.showProgress("Loading...");
            saveMsg = "";
            SAP.instance.companyService.getCompanySignatureImage(companyKey.sourceSystemCd,
                                                                  companyKey.companyId,
                                                                  new Responder(onSignatureLoaded, onSaveFaulted_internal));
        }

        private function onSignatureLoaded(e:ResultEvent):void {
            SAP.instance.hideProgress();
            var image:ByteArray = e.result as ByteArray;
            if(image != null) {
                mLoader.loadBytes(image);
            }
        }

        public function addTestPrintBatch():void {
            // show progress bar
			SAP.instance.showProgress("Adding Test Batch...");
			// reset error string
			saveMsg = "";
			SAP.instance.companyService.addCheckPrintTestBatch(companyKey.sourceSystemCd,
                                                              companyKey.companyId,
                                                              new Responder(onTestBatchAdded, onSaveFaulted_internal));
        }

        private function onTestBatchAdded(e:ResultEvent):void {
            SAP.instance.hideProgress();
            saveMsg = "Test print batch added";
        }

        public function canAddService(serviceCd:String):Boolean {
            var serviceCodeEnum:ServiceCodeEnum = ServiceCodeEnum.valueOf(serviceCd);
            switch(serviceCodeEnum) {
                case ServiceCodeEnum.BILL_PAYMENT:
                    return SAP.canPerformOperation(OperationsEnum.ADD_VENDOR_PAYMENT_SERVICE);
                case ServiceCodeEnum.CHECK_DISTRIBUTION:
                    return SAP.canPerformOperation(OperationsEnum.ADD_CHECK_DISTRIBUTION_SERVICE);
                default:
                    return false;
            }
        }

        public function editOfferings(serviceCd:String):void {
            topic.findPage(CompanyInspectorPageEnum.COMPANY_OFFERINGS).activatePage(CompanyOfferingsViewModel.createActivator(serviceCd));
        }

        public function editOffers(serviceCd:String):void {
            topic.findPage(CompanyInspectorPageEnum.COMPANY_OFFERS).activatePage(CompanyOffersViewModel.createActivator(serviceCd));
        }

        public function getWorkersCompInfo():void {
            SAP.instance.showProgress("Retrieving information");
            SAP.instance.companyService.getWorkersCompServiceInfo(
                    companyKey.sourceSystemCd,
                    companyKey.companyId,
                    new mx.rpc.Responder(onGetWorkersCompInfo, onSaveFaulted_internal))
        }

        private function onGetWorkersCompInfo(e:ResultEvent):void {
            if (e != null && e.result != null) {
                var result:String = e.result as String;
                workersCompInfo =  new XML(result);
            }
            SAP.instance.hideProgress();
        }
    }
}