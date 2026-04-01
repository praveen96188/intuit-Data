package psp.sap.viewmodel {
import mx.collections.ArrayCollection;

import psp.sap.model.ReportJob;
/**
 * Created by: smodgil on 01/18/20.
 * Description: This is a model class meant for Report job.
 */
public class ReportJobViewModel extends AbstractPartViewModel {
    public function RTBJobViewModel() {
    }

    override protected function preActivation():void {

    }

    private function setRTBJobListPreActivation():void {
        preActivationComplete();
    }

    override protected function initializeBackingProperties():void {

    }

    override protected function executeSave():void {

    }
}

}
