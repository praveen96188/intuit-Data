/**
 * Created by anandp233 on 2/18/14.
 */
package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;

    import psp.sap.model.RTBJob;

    public class RTBJobViewModel extends AbstractPartViewModel {
        public function RTBJobViewModel() {
        }

        [ArrayElementType("psp.sap.model.RTBJob")]
        [Bindable]
        public var rtbJobList:ArrayCollection;

        private var mSelectedRTBJob:RTBJob;


        override protected function preActivation():void {
            /*
             if (SAP.instance.lookupService.mRTBJobList == null) {
             SAP.instance.lookupService.addEventListener(SAPEvent.DATA_LOAD_COMPLETED, function(e:Event):void {
             setRTBJobListPreActivation();
             });
             } else {
             setRTBJobListPreActivation();
             }
             */
        }

        private function setRTBJobListPreActivation():void {
            //rtbJobList = SAP.instance.lookupService.mRTBJobList;
            preActivationComplete();
        }

        override protected function initializeBackingProperties():void {
            if (rtbJobList != null && rtbJobList.length > 0) {
                //selectedJob = RTBJob(rtbJobList.getItemAt(0));
            }
        }

        override protected function executeSave():void {

        }
    }

}
