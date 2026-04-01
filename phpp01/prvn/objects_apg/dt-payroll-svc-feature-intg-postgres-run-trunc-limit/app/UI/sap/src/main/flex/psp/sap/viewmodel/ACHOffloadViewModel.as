package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.events.CollectionEvent;
    import mx.rpc.Responder;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.collections.NachaFileGroup;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.application.enums.OperatorPageEnum;
    import psp.sap.model.NachaFile;

    public class ACHOffloadViewModel extends AbstractPartViewModel
    {
        private var mSecondaryOffloadInitiated:Boolean = false;

        [ArrayElementType("psp.sap.model.NachaFile")]
        private var mACHFiles:ArrayCollection=null;

        [Bindable]
        [ArrayElementType("psp.sap.application.collections.NachaFileGroup")]
        public var ACHFileGroups:ArrayCollection=null;

        [Bindable] public var cannotRequestSecondaryOffloadReason:String = "";
        [Bindable] public var canRequestSecondaryOffload:Boolean = true;


        public function ACHOffloadViewModel()
        {
            reloadOnSave = true;

            if (!SAP.canPerformOperation(OperationsEnum.REQUEST_SECOND_OFFLOAD)) {
                canRequestSecondaryOffload = false;
                cannotRequestSecondaryOffloadReason = "You do not have permission to request Second Offload";
            }
        }

        override protected function loadModelData():void {
            SAP.instance.administrationService.getNachaFilesForOffload(new Responder(onLoadSucceeded,onLoadModelDataFaulted));
        }

        protected function onLoadSucceeded(e:ResultEvent):void{
            mACHFiles = e.result as ArrayCollection;

            var timeNow:Date = SAP.instance.PSPDate;
            // Check if current time > 5PM , disable the schedule second offload link
            if (!canRequestSecondaryOffload) {
                modelDataLoaded();
                //no need to find another reason to disable
            } else if(timeNow.hours > 17 ) {
                canRequestSecondaryOffload = false;
                cannotRequestSecondaryOffloadReason = "Second Offload can be initiated before 5:00 PM only";
                modelDataLoaded();
            } else {
                // check if the second offload has already been scheduled for the day - if so, disable second offload link
                SAP.instance.administrationService.isSecondOffloadScheduled(createLoadModelDataResponder(onLoadSecondOffloadState));
            }

        }

        protected function onLoadSecondOffloadState(e:ResultEvent):void{
            mSecondaryOffloadInitiated = e.result as Boolean;

            canRequestSecondaryOffload = !mSecondaryOffloadInitiated;
            if(mSecondaryOffloadInitiated) {
                cannotRequestSecondaryOffloadReason = "Second Offload has already been scheduled for today";
            } else {
                cannotRequestSecondaryOffloadReason = "";
            }

        }

        override protected function initializeBackingProperties():void {
            ACHFileGroups = NachaFileGroup.group(mACHFiles);

            //Bindings from the UI will fire when the confirmation code changes; this will then trigger a collection change
            //so we can updateCanSave
            mACHFiles.addEventListener(CollectionEvent.COLLECTION_CHANGE,onACHFileCollectionChanged,false,0,true);
        }


        protected function onACHFileCollectionChanged(event:CollectionEvent):void {
            updateCanSave();
        }


        public function onSecondaryOffload():void {
            inspector.getPage(OperatorPageEnum.ACH_SECONDARY_OFFLOAD_CONFIRMATION).activatePage();
        }

        //strip off the directories leaving only the file name
        public function shortFormatFile(fileName:String):String {
            var tokens:Array = fileName.split(/[\\\/]/); //forward and back slashes
            if (tokens.length > 1) {
                return tokens[tokens.length-1];
            } else {
                return fileName;
            }
        }
    }
}
