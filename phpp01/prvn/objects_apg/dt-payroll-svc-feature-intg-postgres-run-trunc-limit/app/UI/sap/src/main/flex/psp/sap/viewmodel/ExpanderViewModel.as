package psp.sap.viewmodel
{
    import psp.sap.application.SAP;

    public class ExpanderViewModel extends CompositePartViewModel
	{
		
		[Bindable] public var expandOnActivate:Boolean = true;
	    [Bindable] public var middleHtmlText:String;
		[Bindable] public var rightHtmlText:String;
		[Bindable] public var currentState:String;

        public var canCollapse:Boolean = true;



        public var preferenceKey:String=null;
        [Bindable] public var preferenceEnabled:Boolean;
        [Bindable] public var preferenceLocked:Boolean;
    
    	public function ExpanderViewModel() {
    		super();
    		this.subpartStrategy = PartAdditionStrategy.LAZY_COMPOSITE;
    	}
    								
		public function get clientViewModel():AbstractPartViewModel {
			if (partViewModels.length == 0) {
				return null;
			}
			return this.partViewModels.getItemAt(0) as AbstractPartViewModel;
		}
				
		override protected function onActivated():void {
			var doExpand:Boolean;

            //is there a preference for this?
			if (SAP.instance.session.user != null && SAP.instance.session.user.getPreference(preferenceKey) != null) {
                preferenceEnabled = SAP.instance.session.user.inlineSettingsEnabled;
                doExpand = SAP.instance.session.user.getPreferenceBoolean(preferenceKey);
            } else {
                //if not, defer to the coded value
                preferenceEnabled = false;
                doExpand = expandOnActivate;
            }

            if (doExpand) {
				expand();
			} else {
				collapse();
			}
		}
										
	    public function collapse():void {
            if (!canCollapse) return;
            currentState='panelClosed';
			if (clientViewModel != null) {
				clientViewModel.deactivate();
			}
			updateLockedState();
		}
		
		public function expand():void {
			currentState='panelOpen';
			if (clientViewModel != null) {
				clientViewModel.activate();				
			}
            updateLockedState();
				
		}
		
		public function toggle():void{
			if (!opened) {
				expand();
			} else {
				collapse();
			}			
		}

        private function updateLockedState():void {
            if (preferenceKey != null) {
                var value:Boolean = SAP.instance.session.user != null && SAP.instance.session.user.getPreferenceBoolean(preferenceKey);
                preferenceLocked = (value && opened)
                        || (!value && !opened);
            }
        }

        public function savePreference():void {
            if (preferenceKey != null) {
                SAP.instance.session.user.setPreferenceBoolean(preferenceKey, opened);
                updateLockedState();
            }
        }

        public function get opened():Boolean {
            return currentState == "panelOpen";
        }
            
		override protected function onRefresh():void {
			if (opened) {
				if (clientViewModel != null) {
                    clientViewModel.refresh();
                }
			}	
		}

        //want all subparts to be bound except when the are specifically aggregated by the subpart
        //that is only DEH?
        override public function addNewPart(partClass:Class, label:String=null, strategy:String=null):AbstractPartViewModel {
            if (partClass != DisplayEditHistoryViewModel) {
                this.bindSaveMessageWithChildren = true;
            }
            return super.addNewPart(partClass, label, strategy);
        }

	}
}
