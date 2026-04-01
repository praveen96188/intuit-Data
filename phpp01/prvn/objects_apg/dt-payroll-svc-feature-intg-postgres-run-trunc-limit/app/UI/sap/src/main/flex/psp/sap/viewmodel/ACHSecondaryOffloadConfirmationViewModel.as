package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.formatters.DateFormatter;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.application.enums.OperatorPageEnum;


    public class ACHSecondaryOffloadConfirmationViewModel extends AbstractPartViewModel
    {
        private var mDate:String = "";
        private var mPerformSecondOffload:Boolean ;

        [ArrayElementType("psp.sap.model.NachaFile")]
        private var mACHFileList:ArrayCollection =  null;

        private var dateFormatterForInput:DateFormatter = new DateFormatter();
        private var dateFormatterForTime:DateFormatter = new DateFormatter();

        public function ACHSecondaryOffloadConfirmationViewModel()
        {
            dateFormatterForTime.formatString = SAP.instance.configuration.timeFormat;
            performSecondOffload = true;
        }

        [Bindable("propertyChange")]
        public function get ACHFileList():ArrayCollection {
            return mACHFileList;
        }

        public function set ACHFileList(value:ArrayCollection):void {
            mACHFileList = value;
        }


        [Bindable]
        public function get date():String {
            return mDate;
        }

        public function set date(value:String):void {
            mDate = value;
        }

        [Bindable]
        public function get performSecondOffload():Boolean {
            return mPerformSecondOffload;
        }

        public function set performSecondOffload(value:Boolean):void {
            mPerformSecondOffload = value ;
        }

        override protected function initializeBackingProperties():void {
            var pspDate:Date = 	SAP.instance.PSPDate;
            pspDate.setHours(19, 0, 0, 0);
            date = "Today ("+dateFormatterForInput.format(pspDate)+") "+dateFormatterForTime.format(pspDate);

        }

		override protected function evaluateCanSave():Boolean {
			return performSecondOffload && SAP.canPerformOperation(OperationsEnum.REQUEST_SECOND_OFFLOAD);
		}

        override protected function executeSave():void {
            SAP.instance.administrationService.scheduleSecondaryOffload(createSaveResponder(onUpdatePreformOffload));
        }

        protected function onUpdatePreformOffload(e:ResultEvent):void {
            performSecondOffload = false;
            updateCanSave();
        }
    }
}
