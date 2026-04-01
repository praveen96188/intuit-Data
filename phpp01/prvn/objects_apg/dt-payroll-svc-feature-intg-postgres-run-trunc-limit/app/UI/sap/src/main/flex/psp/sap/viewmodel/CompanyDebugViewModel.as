package psp.sap.viewmodel
{
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.model.QuickbooksInfo;

    public class CompanyDebugViewModel extends AbstractPartViewModel
	{
        public static var SAVE_DEBUG_LOGGING:String = "saveDebugLogging";
        public static var SAVE_PROCESS_REQUESTS:String = "saveProcessRequests";
        public static var SAVE_ALLOW_TRANSMISSIONS:String = "saveAllowTransmissions";
        private var mSaveMethod:String;

		[Bindable] public var debugLogging:Boolean = false;
        [Bindable] public var processTransmissions:Boolean = false;
        [Bindable] public var allowTransmissions:Boolean = true;

		public function CompanyDebugViewModel()
		{
			this.label = CompanyInspectorPageEnum.COMPANY_DEBUGGING;
			
			reloadOnSave = true;			
		}
		
		override protected function loadModelData():void {			
			loadCount = 2;
            SAP.instance.companyService.isDebugLogging(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onDebugLoggingLoaded));
            SAP.instance.companyService.getQuickbooksInfo(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onQuickbooksInfoLoaded));
		}

        private function onDebugLoggingLoaded(e:ResultEvent):void {
            debugLogging = Boolean(e.result);
        }

        private function onQuickbooksInfoLoaded(e:ResultEvent):void {
            processTransmissions = QuickbooksInfo(e.result).processTransmissions;
            allowTransmissions = QuickbooksInfo(e.result).allowTransmissions;
        }

		override public function get hasChanged():Boolean {
			return true;
		}

        public function callSave(pSaveMethod:String):void {
            mSaveMethod = pSaveMethod;
            save();
        }
		
		override protected function get savingMessage():String {
            if(mSaveMethod == SAVE_DEBUG_LOGGING) {
                return (debugLogging ? "Disabling" : "Enabling") + " debug logging";
            } else if(mSaveMethod == SAVE_PROCESS_REQUESTS) {
                return (processTransmissions ? "Disabling" : "Enabling") + " processing requests";
            } else if(mSaveMethod == SAVE_PROCESS_REQUESTS) {
                return (allowTransmissions ? "Disabling" : "Enabling") + " allow transmissions";
            }

            return null;
		}
		
		override protected function executeSave():void {
            if(mSaveMethod == SAVE_DEBUG_LOGGING) {
                SAP.instance.companyService.switchDebugLogging(	company.sourceSystemCd,
                        company.companyId,
                        !debugLogging,
                        createSaveResponder());
            } else if(mSaveMethod == SAVE_PROCESS_REQUESTS) {
                SAP.instance.companyService.switchProcessTransmissions(	company.sourceSystemCd,
                        company.companyId,
                        !processTransmissions,
                        createSaveResponder());
            } else if(mSaveMethod == SAVE_ALLOW_TRANSMISSIONS) {
                SAP.instance.companyService.switchAllowTransmissions( company.sourceSystemCd,
                        company.companyId,
                        !allowTransmissions,
                        createSaveResponder());
            }
		}
		
		public function canPerformOperation(operation:String):Boolean {
			return SAP.canPerformOperation(operation);
		}		
	}
}