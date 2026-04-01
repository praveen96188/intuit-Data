/**
 * User: sshetty
 * Date: 9/11/13
 * Time: 3:47 AM
 */
package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.model.LedgerOperationJob;

    public class OperatorJobsListViewModel extends CompositePartViewModel {
        [ArrayElementType("psp.sap.model.LedgerOperationJob")]
        [Bindable]
        public var currentJobs:ArrayCollection;
        [Bindable]
        public var isCompleted:Boolean;
        private var actionType:String;

        private var selectedJob:LedgerOperationJob;
        public static const FILE_UPLOADED_EVENT:String = "FileUploaded";
        private static const QUEUE_CURRENT_JOB:String = "QueueCurrentJob";
        private static const DELETE_CURRENT_JOB:String = "DeleteCurrentJob";

        public function OperatorJobsListViewModel() {
            this.reloadOnSave = true;
        }

        private function onCurrentJobsLoaded(e:ResultEvent):void {
            var tempJobs:ArrayCollection;
            tempJobs = ArrayCollection(e.result);
            tempJobs.filterFunction = filterJobs;
            tempJobs.refresh();
            currentJobs = tempJobs;
        }

        public function filterJobs(item:LedgerOperationJob):Boolean {
            return (isCompleted ? item.isProcessed : !(item.isProcessed || item.isDeleted));
        }

        override protected function loadModelData():void {
            SAP.instance.administrationService.getLedgerOperationJobs(createLoadModelDataResponder(onCurrentJobsLoaded));
        }

        public function queue(job:LedgerOperationJob):void {
            selectedJob = job;
            actionType = QUEUE_CURRENT_JOB;
            forceSave();
        }

        public function deleteJob(job:LedgerOperationJob):void {
            selectedJob = job;
            actionType = DELETE_CURRENT_JOB;
            forceSave();
        }

        override protected function executeSave():void {
            if (actionType == QUEUE_CURRENT_JOB) {
                SAP.instance.administrationService.queueLedgerOperationJob(selectedJob.id, createSaveResponder());
            } else if (actionType == DELETE_CURRENT_JOB) {
                SAP.instance.administrationService.deleteLedgerOperationJob(selectedJob.id, createSaveResponder());
            }
            actionType = null;
        }

    }
}
