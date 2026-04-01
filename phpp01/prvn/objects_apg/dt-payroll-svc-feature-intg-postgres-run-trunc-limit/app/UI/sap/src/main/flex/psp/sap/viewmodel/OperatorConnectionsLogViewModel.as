package psp.sap.viewmodel {
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.collections.PaginationCollection;
    import psp.sap.application.enums.OperatorInspectorTopicEnum;
    import psp.sap.model.CompanyKey;
    import psp.sap.model.SearchResults;
    import psp.sap.model.SourceSystemEnum;
    import psp.sap.model.Transmission;

    public class OperatorConnectionsLogViewModel extends AbstractPartViewModel {

        /*  Constants   */
        private static const DEFAULT_SOURCE_SYSTEM:SourceSystemEnum = SourceSystemEnum.QBDT;

        /*  Instance variables, fields  */
        [Bindable]
        public var dateSelectionViewModel:DateTimeSelectionViewModel;
        [Bindable]
        public var sourceSystems:Array;
        private var mSourceSystem:SourceSystemEnum;

        private var mTransmissionSearchResults:PaginationCollection = new PaginationCollection(null, 15);
        [Bindable]
        public var useEndDateFromData:Boolean = false;

        private var endDateToUse:Date ;
        private var nextAlreadyClicked:Boolean=false;

        /*  Constructor   */
        public function OperatorConnectionsLogViewModel() {
            super();
            this.label = OperatorInspectorTopicEnum.CONNECTIONS;
            this.reloadOnActivate = false;
            dateSelectionViewModel = new DateTimeSelectionViewModel(this as AbstractPartViewModel);
            sourceSystems = SourceSystemEnum.connectionLogList;
            sourceSystem = DEFAULT_SOURCE_SYSTEM;

        }

        /*  Getters, Setters & Properties */

        [Bindable]
        public function get sourceSystem():SourceSystemEnum {
            return mSourceSystem;
        }

        public function set sourceSystem(value:SourceSystemEnum):void {
            if (value == null) {
                value = DEFAULT_SOURCE_SYSTEM;
            }
            mSourceSystem = value;
        }

        [Bindable]
        public function get transmissionSearchResults():PaginationCollection {
            return mTransmissionSearchResults;
        }

        public function set transmissionSearchResults(value:PaginationCollection):void {
            if (value == null) {
                value = new PaginationCollection();
            }
            mTransmissionSearchResults = value;
        }

        /*  Methods */

        /**  searchTransmission, causes data to be (re)loaded    */
        public function searchTransmissions():void {
            this.nextAlreadyClicked=false;
            transmissionSearchResults.startIndex=0;
            refresh();
        }

        override public function get hasChanged():Boolean {
            return true;
        }

        override protected function loadModelData():void {
            isDataLoading = true;
            saveMsg = "";
            saveFaulted = false;
            if (useEndDateFromData) {
                if (!nextAlreadyClicked && transmissionSearchResults.length > 0) {
                    endDateToUse = transmissionSearchResults.getItemAt(0).initializeDateTime;
                }
                nextAlreadyClicked = true;
            }
            else {
                transmissionSearchResults.startIndex = 0;
                endDateToUse = dateSelectionViewModel.endDateValue;
            }
            useEndDateFromData = false;
            SAP.instance.administrationService.getAllTransmissions(sourceSystem.code,
                    dateSelectionViewModel.startDateValue,
                    endDateToUse,
                    transmissionSearchResults.startIndex,
                    transmissionSearchResults.pageSize,
                    createLoadModelDataResponder(onSearchCompleted));
        }

        /*  Callback function for searchComplete    */
        private function onSearchCompleted(e:ResultEvent):void {
            var searchReturn:SearchResults = e.result as SearchResults;
            transmissionSearchResults.totalRecords = searchReturn.totalRecords;
            transmissionSearchResults.source = searchReturn.returnsList.source;
        }

        public function changePage(newPageEnum:String):void {
            inspector.getPage(newPageEnum).activate();
        }

        public function goToCompanyInfo(transmission:Transmission):void {
            var companyKey:CompanyKey = transmission.companyKey;
            companyKey.display();
        }
    }
}
