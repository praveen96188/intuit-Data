package psp.sap.viewmodel
{
    import flash.events.Event;

    import mx.rpc.Responder;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.model.PINInfo;

    public class CompanyPINViewModel extends AbstractPartViewModel
	{

        [Bindable] public var pinInfo:PINInfo;

		public var pin:String;

        [Bindable]
        public var caseId:String;
		
		public function generateRandomPin():void {
			// show progress bar			
			SAP.instance.showProgress("Generating...");
			// reset error string
			saveMsg = "";
			SAP.instance.companyService.generateRandomPin(company.sourceSystemCd, 
														  company.companyId, caseId,
														  new Responder(onGenerateSuccess, onSaveFaulted_internal));
		}

		public function onGenerateSuccess(e:ResultEvent):void {
			pin = e.result as String;
			// hide progress bar
			SAP.instance.hideProgress();
			refresh();
			dispatchEvent(new Event("PinGenerated"));
		} 				
		


        public function unlockOnce():void {
            SAP.instance.showProgress("Unlocking...");
            saveMsg = "";
            SAP.instance.companyService.unlockCompany(company.sourceSystemCd,
                                                          company.companyId,
                                                          new Responder(onUnlockSuccess, onSaveFaulted_internal));
        }

        public function onUnlockSuccess(e:ResultEvent):void {
			// hide progress bar
			SAP.instance.hideProgress();
			saveMsg = "PIN unlocked";
            refresh(false);
		}

		
		override protected function loadModelData():void {
            SAP.instance.companyService.getPINInfo(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onPINInfoLoaded));
		}

        private function onPINInfoLoaded(e:ResultEvent):void {
            pinInfo = PINInfo(e.result);
        }
		
		public function canPerformOperation(operation:String):Boolean {
			return SAP.canPerformOperation(operation);
		}		
	}
}
