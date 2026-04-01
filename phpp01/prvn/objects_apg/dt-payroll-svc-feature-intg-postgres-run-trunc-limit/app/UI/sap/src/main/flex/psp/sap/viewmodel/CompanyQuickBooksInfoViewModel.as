package psp.sap.viewmodel
{
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.model.QuickbooksInfo;

    public class CompanyQuickBooksInfoViewModel
	extends AbstractPartViewModel
	{
        [Bindable] [BackingProperty] public var quickbooksInfo:QuickbooksInfo;

        [Bindable] public var isDiscoing:Boolean; //todo boogie!

        [Bindable] public var minSupportedQBVersion:int;

		override protected function loadModelData():void {
            loadCount = 2;
            SAP.instance.companyService.getQuickbooksInfo(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onQuickbooksInfoLoaded));
            SAP.instance.administrationService.getMinSupportedQuickbooksVersion(companyKey.sourceSystemCd, createLoadModelDataResponder(onMinVersionLoaded));
		}

        private function onQuickbooksInfoLoaded(e:ResultEvent):void {
            quickbooksInfo = QuickbooksInfo(e.result);
        }

        private function onMinVersionLoaded(e:ResultEvent):void {
            minSupportedQBVersion = int(e.result);
        }

        override protected function initializeBackingProperties():void {
            try {
                var majorVersion:int = parseInt(quickbooksInfo.applicationVersion.split(/\./)[0]);
                if (majorVersion == 0) { //failed to parse something reasonable
                    isDiscoing = false;
                } else {
                    isDiscoing = majorVersion <= minSupportedQBVersion;
                }
            } catch (e:Error) {
                //if we can't split or parse, then I don't care.
                isDiscoing = false;
            }
        }
    }
}