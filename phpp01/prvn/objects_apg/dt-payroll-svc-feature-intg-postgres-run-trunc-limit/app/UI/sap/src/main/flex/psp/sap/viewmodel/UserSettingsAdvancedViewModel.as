package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.model.UserSetting;

    public class UserSettingsAdvancedViewModel extends AbstractPartViewModel {

        [ArrayElementType("psp.sap.model.UserSetting")]
        [Bindable] [BackingProperty] public var settings:ArrayCollection;


        public function UserSettingsAdvancedViewModel() {
            super();
            this.reloadOnSave = true;
        }

        override protected function loadModelData():void {
            SAP.instance.userService.getUserSettings(SAP.instance.session.user.corpId, createLoadModelDataResponder(onSettingsLoaded));
        }

        private function onSettingsLoaded(e:ResultEvent):void {
            var tempSettings:ArrayCollection = ArrayCollection(e.result);

            var sort:Sort = new Sort();
            sort.fields = [new SortField("key", true)];
            tempSettings.sort = sort;
            tempSettings.refresh();
            
            settings = tempSettings;
            
            SAP.instance.session.user.settings = settings;
        }

        override protected function executeSave():void {
            var settingsSnapshot:Object = new Object();
            for each (var snapshotSetting:UserSetting in backingPropertiesSnapshot["settings"]) {
                settingsSnapshot[snapshotSetting.key] = snapshotSetting;
            }

            var settingsToSave:ArrayCollection = new ArrayCollection();

            for each (var setting:UserSetting in settings) {
                if (UserSetting(settingsSnapshot[setting.key]).value != setting.value) {                                        
                    settingsToSave.addItem(setting);
                }
            }

            SAP.instance.userService.updatePreferences(SAP.instance.session.user.corpId, settingsToSave, createSaveResponder());
        }


    }
}