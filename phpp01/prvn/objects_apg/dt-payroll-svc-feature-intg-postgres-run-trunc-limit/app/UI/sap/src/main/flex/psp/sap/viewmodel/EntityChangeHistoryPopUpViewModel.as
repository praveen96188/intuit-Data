package psp.sap.viewmodel {
    import flash.events.Event;

    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.PartsEnum;
    import psp.sap.model.EntityChange;
    import psp.sap.viewmodel.CompanyInformationViewModel;
    import psp.sap.viewmodel.events.ViewModelEvent;

    public class EntityChangeHistoryPopUpViewModel extends CompositePartViewModel {

        private var mEntityChanges:ArrayCollection;
        private var editEntityChangePopup:PopUpPartViewModel;
        private var editEntityChangeViewModel:EditEntityChangeEffectiveDateViewModel;

        public function EntityChangeHistoryPopUpViewModel() {
            super();
            this.reloadOnSave = true;
            editEntityChangePopup = addPopUpPart(PartsEnum.EDIT_ENTITY_CHANGE_EFFECTIVE_DATE);
            editEntityChangePopup.closeOnSave = true;

            editEntityChangeViewModel = editEntityChangePopup.addNewPart(EditEntityChangeEffectiveDateViewModel, PartsEnum.EDIT_ENTITY_CHANGE_EFFECTIVE_DATE) as EditEntityChangeEffectiveDateViewModel;
            editEntityChangeViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);
        }

        [Bindable]
        public function get entityChanges():ArrayCollection {
            return mEntityChanges;
        }

        public function set entityChanges(value:ArrayCollection):void {
            if (value == null) {
                value = new ArrayCollection();
            }
            mEntityChanges = value;
        }

        public function viewEditEffectiveDate(entityChange:EntityChange):void {
            editEntityChangeViewModel.oldEin = entityChange.oldEIN;
            editEntityChangeViewModel.newEin = entityChange.newEIN;
            editEntityChangePopup.displayPopUp();
        }

        private function onViewModelDeactivated(e:Event):void {
            PopUpPartViewModel.refreshIfSaved(AbstractPartViewModel(e.target), this);
        }

        override protected function loadModelData():void {
            SAP.instance.companyService.getEntityChangeHistory(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onSearchCompleted));
        }

        protected function onSearchCompleted(e:ResultEvent):void {
            entityChanges = e.result as ArrayCollection;
        }

    }
}