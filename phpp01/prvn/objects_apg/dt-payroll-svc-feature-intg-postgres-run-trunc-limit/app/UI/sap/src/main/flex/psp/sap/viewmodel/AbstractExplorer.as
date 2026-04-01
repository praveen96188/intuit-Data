package psp.sap.viewmodel
{
    import flash.events.EventDispatcher;
    import flash.system.System;
    import flash.utils.getQualifiedClassName;

    import mx.events.PropertyChangeEvent;
    import mx.events.PropertyChangeEventKind;
    import mx.logging.ILogger;
    import mx.rpc.events.FaultEvent;

    import psp.sap.application.ClientLoggingTarget;
    import psp.sap.application.IApplicationItem;
    import psp.sap.application.SAP;
    import psp.sap.application.collections.InspectorCollection;
    import psp.sap.application.events.InspectorActionEvent;

    public class AbstractExplorer
		extends EventDispatcher		
	{
		private var logger:ILogger = ClientLoggingTarget.getLogger(this);
		public static const ACTIVE_INSPECTOR_CHANGED:String = "activeInspectorChanged";
		
		private var mName:String = "";
		private var mLabel:String = "";
		protected var mShowInMenu:Boolean = true;
		
		private var mInspectors:InspectorCollection = new InspectorCollection();
		protected var mActiveInspector:AbstractInspectorViewModel = null;
		
		/**
		 * The inspector to activate when the explorer is activated and
		 * no inspector is specified.
		 *
		 * @see activate(inspectorToActivate:AbstractInspectorViewModel2)
		 */
		protected var mDefaultInspector:AbstractInspectorViewModel = null;
		
		public function AbstractExplorer(name:String, label:String = null, showInMenu:Boolean = true):void
		{
			super();
			mName = name;
			
			if (label != null)
				mLabel = label;
			else
				mLabel = mName;
				
			mShowInMenu = showInMenu;
			
			
			mEnabled = true;			
							
			mInspectors.addEventListener(InspectorActionEvent.CLOSED_EVENT, onInspectorClosed, false, 0, true);	
		}


		public function get name():String {
			return mName;
		}
		
		public function get label():String {
			return mName;
		}
		
		public function get showInMenu():Boolean {
			return mShowInMenu && permissionGranted();
		}
		
		private var mEnabled:Boolean;
		
		[Bindable]
		virtual public function get enabled():Boolean {
			return mEnabled && permissionGranted();
		}
		
		virtual public function set enabled(value:Boolean):void {
			mEnabled = value;
		}
		
		public function get defaultInspector():AbstractInspectorViewModel {
			if (mDefaultInspector != null) {
                return mDefaultInspector;
            } else if (mInspectors.length == 0) {
                return null;
            } else {
                return AbstractInspectorViewModel(mInspectors.getItemAt(0));
            }
		}
		
		public function set defaultInspector(value:AbstractInspectorViewModel):void {
			if (!mInspectors.contains(value)) {
				throw new Error("The default inspector cannot be set.  It first must added as a member of the inspectors() collection.");
			}
			
			mDefaultInspector = value;
		}
		
		public function get isActive():Boolean {
			return SAP.instance.activeExplorer == this;
		}
		
		/**
		 * Sets this explorer as the <code>activeExplorer</code> if it is not already active.
		 * 
		 * Sets the <code>activeInspector</code> to the inspectorToActivate, if not null.  If
		 * the inspectorToActivate is null, then the <code>defaultInspector</code> is used.
		 * 
		 * If the <code>activeInspector</code> is set to a non-null value (in accordance with the steps described
		 * above), then the activation call is cascaded to the activeInspector.
		 */
		virtual public function activate(inspectorToActivate:AbstractInspectorViewModel = null):void {
			var inspectorToActivate2:AbstractInspectorViewModel = inspectorToActivate as AbstractInspectorViewModel;
            var inspectorName:String = inspectorToActivate2 != null ? getQualifiedClassName(inspectorToActivate2) : "null";
			logger.info("activate called with inspector: " + inspectorName);
						
			if (!isActive) { 
				SAP.instance.activeExplorer = this;
			}
			
			if(inspectorToActivate2 == null && activeInspector != null){
				// do nothing keep the current inspector active
				return; 
			}

			// use default if configured and no inspector is specified
			if (inspectorToActivate2 == null && defaultInspector != null) {
				inspectorToActivate2 = defaultInspector;
			}

			if (inspectorToActivate2 != activeInspector) {
				var activeInspectorChanged:PropertyChangeEvent = activateInspector(inspectorToActivate2);
				dispatchEvent(activeInspectorChanged);
				dispatchActiveInspectorChangedEvent(activeInspectorChanged.oldValue, activeInspectorChanged.newValue);
				dispatchActiveInspectorIndexChangedEvent();														
			}
		}
			
		private function activateInspector(value:AbstractInspectorViewModel):PropertyChangeEvent {
			var inspectorName:String = value != null ? getQualifiedClassName(value) : "null";
			logger.info("activateInspector called inspector: " + inspectorName);
			
			var oldValue:AbstractInspectorViewModel = mActiveInspector;
			
			if (oldValue != null)
				oldValue.deactivate();

			if (value != null && !this.inspectors.contains(value))
				addInspector(value);

			mActiveInspector = value;
			
			if (mActiveInspector != null)
				mActiveInspector.activate();
			
			return PropertyChangeEvent.createUpdateEvent(this, "activeInspector", oldValue, mActiveInspector);
		}
		
		private function dispatchActiveInspectorChangedEvent(oldValue:Object, newValue:Object):void {
			var event: PropertyChangeEvent = 
				new PropertyChangeEvent("activeInspectorChanged",
										false,
										false,
										PropertyChangeEventKind.UPDATE,
										"activeInspector",
										oldValue,
										newValue,
										this);
			
			dispatchEvent(event);			
		}
		
		protected function dispatchActiveInspectorIndexChangedEvent():void {
			dispatchEvent( PropertyChangeEvent.createUpdateEvent(this, "activeInspectorIndex", null, activeInspectorIndex) );					
		}
			
		virtual public function deactivate():void {
		}	
		
		[Bindable("activeInspectorChanged")]		
		public function get activeInspector():AbstractInspectorViewModel {
			return mActiveInspector;
		}
		
		public function set activeInspector(value:AbstractInspectorViewModel):void {
			var inspectorName:String = value != null ? getQualifiedClassName(value) : "null";
			logger.info("activeInspector changed inspector: " + inspectorName);
			
			var oldValue:Object = mActiveInspector;
			activateInspector(value);
		}
		
		[Bindable]
		public function get activeInspectorIndex():int {
			if (mActiveInspector == null)
				return -1;
							
			return inspectors.getItemIndex(mActiveInspector);	
		}
		
		public function set activeInspectorIndex(value:int):void {
			if (!enabled)
				return;
			
			if (value > inspectors.length) {
				trace("WARNING: " + this.label + "::activeInspectorIndex (" + value + ") out of range");
				return;
			}

			var inspectorToActivate:AbstractInspectorViewModel = null;
			if (value > -1 && inspectors.length > 0){
				inspectorToActivate = inspectors.getItemAt(value) as AbstractInspectorViewModel;
			}			
				
			activate(inspectorToActivate);
			
		}
		
		[Bindable ("propertyChange")]
		public function get inspectors():InspectorCollection {
			return mInspectors;
		}				
		
		virtual public function display(applicationItem:IApplicationItem, pageEnumValue:String = null):AbstractInspectorViewModel {
			var applicationItemName:String = applicationItem != null ? getQualifiedClassName(applicationItem) : "null";
			var pageLabel:String = pageEnumValue != null ? pageEnumValue : "null"; 
			logger.info("Display called applicationItem = " + applicationItemName + " pageEnumValue = " + pageLabel);
			
			if (applicationItem == null) 
				return null;
					
			// if the inspector is already loaded, make it active			
			var inspector:AbstractInspectorViewModel = inspectors.findByApplicationItem(applicationItem);
			if (inspector == null) {
				// the below ordering of assignments is important;
				// the company should be set on the inspector ViewModel before
				// any registered listeners are alerted to the ViewModel's
				// presence (via inspectors.add and setting activeInspector) 
				inspector = createInspector(applicationItem);
				if (inspector == null)
					throw new Error("createInspector() returned null -- sub-classes of AbstractExplorer that support display(IApplicationItem) must implement this method.");
				addInspector(inspector);
			}
			
			
			if(inspector.enabled){
				if (pageEnumValue != null) {
					var page:AbstractPartViewModel = inspector.getPage(pageEnumValue);
					page.activate();
				}
				else {				
					activate(inspector);				
				}
			}

			return inspector;
		}
		
		virtual protected function createInspector(applicationItem:IApplicationItem):AbstractInspectorViewModel {
			return null;
		}
		
		virtual protected function addInspector(inspector:AbstractInspectorViewModel):void {
			inspectors.addItem(inspector);			
		}
		
		private function onInspectorClosed(e:InspectorActionEvent):void {
			if(inspectors.length ==0) {
				activeInspector = null;
			}
			else if(!inspectors.contains(activeInspector)){					
				activeInspectorIndex = inspectors.length-1;					
			}
			
			dispatchActiveInspectorIndexChangedEvent();
			
			// try to reclaim the memory from the closed inspector
			System.gc();						
		}	
			
		virtual public function permissionGranted():Boolean {
			var bPermissionGranted:Boolean = true;
			
			for each(var inspectorObj:AbstractInspectorViewModel in mInspectors)
			{
				bPermissionGranted = bPermissionGranted && inspectorObj.permissionGranted()
			}
			
			return bPermissionGranted;
		}		
		
		protected function getFaultMessage(e:FaultEvent):String {
			var faultMessage:String = "An unknown error has occurred.";
			if (e.fault != null) {
				if (e.fault.faultDetail != null)
					faultMessage = e.fault.faultDetail;
				else if (e.fault.faultString != null)
					faultMessage = e.fault.faultString;
			}
			return faultMessage;
		}
		
	}
}