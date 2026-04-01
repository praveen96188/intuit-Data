package psp.sap.viewmodel {

    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.model.EnrollmentFile;
    import psp.sap.model.Quarter;

    public class OperatorEnrollmentsViewModel extends AbstractPartViewModel {

        [ArrayElementType("psp.sap.model.EnrollmentFile")]
        private var mEnrolledFilesList:ArrayCollection;
        [ArrayElementType("psp.sap.model.EnrollmentFile")]
        private var mDeletedFilesList:ArrayCollection;

        private var mCurrentActionCode:String = "";
        [Bindable]
        public var selectedEnrollFile:EnrollmentFile;
        [Bindable]
        public var selectedDeleteFile:EnrollmentFile;

        [Bindable]
        [ArrayElementType("psp.sap.model.Quarter")]
        public var addQuarterList:ArrayCollection;

        [Bindable]
        [ArrayElementType("psp.sap.model.Quarter")]
        public var deleteQuarterList:ArrayCollection;

        public var tapeCreationType:String;

        [Bindable] [BackingProperty]
        public var addQuarter:Quarter;

        [Bindable] [BackingProperty]
        public var deleteQuarter:Quarter;

        [Bindable]
        public function get enrolledFilesList():ArrayCollection {
            return mEnrolledFilesList;
        }

        public function set enrolledFilesList(value:ArrayCollection):void {
            if (value == null) {
                value = new ArrayCollection();
            }
            mEnrolledFilesList = value;
        }

        public function OperatorEnrollmentsViewModel() {
            reloadOnSave = true;
        }

        [Bindable]
        public function get deletedFilesList():ArrayCollection {
            return mDeletedFilesList;
        }

        public function set deletedFilesList(value:ArrayCollection):void {
            if (value == null) {
                value = new ArrayCollection();
            }
            mDeletedFilesList = value;
        }

        protected function onSearchCompletedEnrolled(e:ResultEvent):void {
            enrolledFilesList = e.result as ArrayCollection;
        }

        protected function onSearchCompletedDeleted(e:ResultEvent):void {
            deletedFilesList = e.result as ArrayCollection;
        }

        protected function onGetQuartersCompletedAdd(e:ResultEvent):void {
            addQuarterList = e.result as ArrayCollection;
            addQuarterList.addItemAt(Quarter.EMPTY_QUARTER, 0);
            addQuarter = Quarter(addQuarterList.getItemAt(0));
        }

        protected function onGetQuartersCompletedDelete(e:ResultEvent):void {
            deleteQuarterList = e.result as ArrayCollection;
            deleteQuarterList.addItemAt(Quarter.EMPTY_QUARTER, 0);
            deleteQuarter = Quarter(deleteQuarterList.getItemAt(0));
        }

        override protected function loadModelData():void {
            loadCount = 4;
            SAP.instance.taxService.findEnrollmentFiles("Add", createLoadModelDataResponder(onSearchCompletedEnrolled));
            SAP.instance.taxService.findEnrollmentFiles("Delete", createLoadModelDataResponder(onSearchCompletedDeleted));
            SAP.instance.taxService.getACHEnrollmentQuarters("Add", createLoadModelDataResponder(onGetQuartersCompletedAdd));
            SAP.instance.taxService.getACHEnrollmentQuarters("Delete", createLoadModelDataResponder(onGetQuartersCompletedDelete));
        }

        override protected function initializeBackingProperties():void {
            selectedEnrollFile = null;
            selectedDeleteFile = null;
            addQuarter = addQuarterList.length == 0 ? Quarter.EMPTY_QUARTER : Quarter(addQuarterList.getItemAt(0));
            deleteQuarter = deleteQuarterList.length == 0 ? Quarter.EMPTY_QUARTER : Quarter(deleteQuarterList.getItemAt(0));
        }


        public function createRAFTape(pActionCode:String):void {
            if (pActionCode !== null && pActionCode != "") {
                mCurrentActionCode = pActionCode;
                forceSave();
            }
        }

        public function createACHFile(pEnrollmentFileType:String):void {
            if (pEnrollmentFileType !== null && pEnrollmentFileType != "") {
                tapeCreationType = "ACH";
                mCurrentActionCode = pEnrollmentFileType;
                forceSave();
            }
        }

        override protected function executeSave():void {
            if (this.tapeCreationType == null || this.tapeCreationType == "") {
                SAP.instance.taxService.initiateRAFTapeCreation(mCurrentActionCode, createSaveResponder());
                mCurrentActionCode = null;
            } else if (this.tapeCreationType == "ACH") {
                if (this.mCurrentActionCode == "Add") {
                    SAP.instance.taxService.initiateACHFileCreation(mCurrentActionCode, selectedAddQuarter, createSaveResponder());
                } else if (this.mCurrentActionCode == "Delete") {
                    SAP.instance.taxService.initiateACHFileCreation(mCurrentActionCode, selectedDeleteQuarter, createSaveResponder());
                }
                mCurrentActionCode = null;
                tapeCreationType = null;
            } else {
                if (this.tapeCreationType == "Add") {
                    SAP.instance.taxService.reInitiateRAFTapeCreation(this.selectedEnrollFile.fileId, createSaveResponder());
                } else if (this.tapeCreationType == "Delete") {
                    SAP.instance.taxService.reInitiateRAFTapeCreation(this.selectedDeleteFile.fileId, createSaveResponder());
                }
                this.tapeCreationType = null;
            }
        }

        public function recreateRAFTape(pCreationType:String):void {
            this.tapeCreationType = pCreationType;
            forceSave();
        }

        [Bindable(event="backingPropertyChanged")]
        public function canAdd():Boolean {
            if (selectedAddQuarter == null) {
                return false;
            }
            return SAP.canPerformOperation(OperationsEnum.CREATE_RAF_FILE);
        }

        [Bindable(event="backingPropertyChanged")]
        public function canDelete():Boolean {
            if (selectedDeleteQuarter == null) {
                return false;
            }
            return SAP.canPerformOperation(OperationsEnum.CREATE_RAF_FILE);
        }

        public function get selectedAddQuarter():Quarter {
            if (addQuarter == null || addQuarter == Quarter.EMPTY_QUARTER) {
                return null;
            }
            return addQuarter;
        }

        public function get selectedDeleteQuarter():Quarter {
            if (deleteQuarter == null || deleteQuarter == Quarter.EMPTY_QUARTER) {
                return null;
            }
            return deleteQuarter;
        }
    }
}