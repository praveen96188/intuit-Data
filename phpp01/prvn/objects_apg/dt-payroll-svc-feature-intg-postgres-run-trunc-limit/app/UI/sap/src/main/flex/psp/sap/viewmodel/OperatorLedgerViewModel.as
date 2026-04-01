/**
 * User: dweinberg
 * Date: 10/25/12
 * Time: 1:12 PM
 */
package psp.sap.viewmodel {
    import flash.events.Event;

    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;

    import psp.sap.application.enums.OperatorPageEnum;
    import psp.sap.validators.SAPValidators;
    import psp.sap.viewmodel.events.ViewModelEvent;

    public class OperatorLedgerViewModel extends CompositePartViewModel {

        [Bindable]
        [BackingProperty]
        public var fileName:String = "";
        [Bindable]
        [BackingProperty]
        public var description:String = "";
        [Bindable]
        public var currentFileUploaded:Boolean = false;
        private var mCreatedJobsListModel:OperatorJobsListViewModel;

        [Bindable]
        public var torViewModel:OperatorLedgerTORViewModel;
        [Bindable]
        public var tabNavigatorVM:PartsTabNavigatorViewModel;

        public function OperatorLedgerViewModel() {
            this.reloadOnSave = true;

            validators.push(SAPValidators.createRequiredFieldValidator(this, "fileName"));
            validators.push(SAPValidators.createRequiredFieldValidator(this, "description"));

            torViewModel = OperatorLedgerTORViewModel(this.addNewPart(OperatorLedgerTORViewModel, OperatorPageEnum.LEDGER_TOR_OPERATIONS));
            torViewModel.addEventListener(ViewModelEvent.SAVE_SUCCEEDED, function (e:ViewModelEvent):void {
                refresh(false);
            });

            tabNavigatorVM = addPartsTabNavigator(OperatorPageEnum.LEDGER_CURRENT_JOBS);

            mCreatedJobsListModel = tabNavigatorVM.addNewPart(OperatorJobsListViewModel, OperatorPageEnum.LEDGER_CURRENT_JOBS) as OperatorJobsListViewModel;
            mCreatedJobsListModel.addEventListener(OperatorJobsListViewModel.FILE_UPLOADED_EVENT, onFileUploaded, false, 0, true);
            mCreatedJobsListModel.isCompleted = false;

            var mCompletedJobsListModel:OperatorJobsListViewModel = tabNavigatorVM.addNewPart(OperatorJobsListViewModel, OperatorPageEnum.LEDGER_COMPLETED_JOBS) as OperatorJobsListViewModel;
            mCompletedJobsListModel.isCompleted = true;

            tabNavigatorVM.defaultSinglePart = mCreatedJobsListModel;
            bindSaveMessageWithChildren = true;
        }

        private function onFileUploaded(event:Event):void {
            mCreatedJobsListModel.currentJobs.dispatchEvent(new CollectionEvent(CollectionEventKind.UPDATE));
        }


        override protected function onActivating():void {
            fileName = "";
        }

        override public function get hasChanged():Boolean {
            return !currentFileUploaded;
        }


    }
}
