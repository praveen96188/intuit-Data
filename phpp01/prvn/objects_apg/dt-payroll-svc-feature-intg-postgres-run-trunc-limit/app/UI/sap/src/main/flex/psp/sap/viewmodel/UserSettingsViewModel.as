package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.SettingsEnum;
    import psp.sap.model.UserSetting;

    public class UserSettingsViewModel extends AbstractPartViewModel{

        private static const SAVE_SETTINGS:int = 0;
        private static const RESET_DEFAULTS:int = 1;

        //key->UserSetting
        [Bindable] [BackingProperty] public var settingsMap:Object;

        [ArrayElementType("psp.sap.model.UserSetting")]
        private var settings:ArrayCollection;

        [Bindable] [BackingProperty] public var displayInlineSettings:Boolean;
        [Bindable] [BackingProperty] public var initialPage:String;
        [Bindable] [BackingProperty] public var includePossibleBackdateYears:Boolean;

        [Bindable] public var possibleInitialPages:ArrayCollection = new ArrayCollection();

        private var saveMode:int;

        public function UserSettingsViewModel() {
            super();
            this.reloadOnSave = true;
        }

        override protected function loadModelData():void {
            SAP.instance.userService.getUserSettings(SAP.instance.session.user.corpId, createLoadModelDataResponder(onSettingsLoaded));
        }

        private function onSettingsLoaded(e:ResultEvent):void {
            settings = e.result as ArrayCollection;    
        }

        override protected function initializeBackingProperties():void {
            //might as well refresh the user (this setter copies the result and does not modify it, so assignment OK)
            SAP.instance.session.user.settings = settings;

            settingsMap = new Object();
            for each (var setting:UserSetting in settings) {
                settingsMap[setting.key] = setting;
            }

            displayInlineSettings = UserSetting(settingsMap[SettingsEnum.INLINE_SETTINGS]).booleanValue;
            includePossibleBackdateYears = UserSetting(settingsMap[SettingsEnum.INCLUDE_POSSIBLE_BACKDATE_YEARS]).booleanValue;

            possibleInitialPages = new ArrayCollection();
            for each (var explorer:AbstractExplorer in SAP.instance.explorers) {
                if (explorer is CompanyExplorerViewModel) {
                    possibleInitialPages.addItem("Company Search");
                } else if (! (explorer is SettingsExplorerViewModel)) {
                    if (explorer.permissionGranted()) {
                        var inspector:AbstractInspectorViewModel = explorer.inspectors.getInspectorAt(0);
                        for each (var topic:InspectorTopicViewModel in inspector.topics) {                            
                            possibleInitialPages.addItem(topic.label);
                        }
                    }
                }
            }
            possibleInitialPages.addItem("None");

            initialPage = UserSetting(settingsMap[SettingsEnum.INITIAL_PAGE]).value;

        }

        override protected function executeSave():void {
            switch(saveMode) {
                case SAVE_SETTINGS:
                    var settingsToSave:ArrayCollection = new ArrayCollection();

                    settingsMap[SettingsEnum.INLINE_SETTINGS].booleanValue = displayInlineSettings;
                    settingsMap[SettingsEnum.INITIAL_PAGE].value = initialPage;
                    settingsMap[SettingsEnum.INCLUDE_POSSIBLE_BACKDATE_YEARS].value = includePossibleBackdateYears;

                    for (var p:String in settingsMap) {
                        if (UserSetting(settingsMap[p]).value != UserSetting(backingPropertiesSnapshot["settingsMap"][p]).value) {
                            settingsToSave.addItem(settingsMap[p]);
                        }
                    }
                    SAP.instance.userService.updatePreferences(SAP.instance.session.user.corpId, settingsToSave, createSaveResponder());
                    break;
                case RESET_DEFAULTS:
                    SAP.instance.userService.resetSettings(SAP.instance.session.user.corpId, createSaveResponder());
                    break;
            }
        }

        public function saveSettings():void {
            saveMode = SAVE_SETTINGS;
            save();
        }

        public function resetDefaults():void {
            saveMode = RESET_DEFAULTS;
            forceSave();
        }

    }
}
