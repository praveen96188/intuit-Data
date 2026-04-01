package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;

    import mx.rpc.Responder;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.model.SystemParameter;

    public class SystemParametersViewModel extends AbstractPartViewModel {

        [ArrayElementType("psp.sap.model.SystemParameter")]
        [Bindable]
        [BackingProperty]
        public var systemParameters:ArrayCollection;

        public function SystemParametersViewModel() {
            this.reloadOnActivate = true;
            this.reloadOnSave = true;
        }

        override protected function loadModelData():void {
            SAP.instance.administrationService.getAllSystemParameters(createLoadModelDataResponder(onLoadSucceeded));
        }

        protected function onLoadSucceeded(e:ResultEvent):void {
            systemParameters = e.result as ArrayCollection;
        }


        override protected function executeSave():void {
            var originalParameters:ArrayCollection = backingPropertiesSnapshot["systemParameters"] as ArrayCollection;

            var updatedParameters:ArrayCollection = new ArrayCollection();
            for (var i:int = 0; i < systemParameters.length; i++) {
                var systemParameter:SystemParameter = systemParameters.getItemAt(i) as SystemParameter;
                var originalSP:SystemParameter = originalParameters.getItemAt(i) as SystemParameter;
                if (systemParameter.value != originalSP.value) {
                    updatedParameters.addItem(systemParameter);
                }
            }

            SAP.instance.administrationService.saveSystemParameters(updatedParameters, createSaveResponder())
        }
    }
}
