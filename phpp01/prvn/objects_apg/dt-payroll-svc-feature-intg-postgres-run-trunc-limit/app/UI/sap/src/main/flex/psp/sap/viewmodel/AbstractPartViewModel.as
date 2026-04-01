package psp.sap.viewmodel {
    import flash.events.Event;
    import flash.events.EventDispatcher;

    import intuit.sbd.flex.framework.application.Call;

    import intuit.sbd.flex.framework.service.ServiceUtils;

    import mx.binding.utils.BindingUtils;
    import mx.collections.ArrayCollection;
    import mx.collections.IList;
import mx.controls.Alert;
import mx.core.ClassFactory;
    import mx.events.CollectionEvent;
    import mx.events.PropertyChangeEvent;
    import mx.formatters.DateFormatter;
    import mx.logging.ILogger;
    import mx.logging.LogEventLevel;
    import mx.rpc.AsyncResponder;
    import mx.rpc.IResponder;
    import mx.rpc.Responder;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    import mx.utils.ObjectUtil;
    import mx.validators.Validator;

    import org.as3commons.reflect.Field;
    import org.as3commons.reflect.Type;

    import psp.sap.application.ClientLoggingTarget;
    import psp.sap.application.SAP;
    import psp.sap.application.collections.PartViewModelCollection;
    import psp.sap.application.enums.ViewModelActivationStateEnum;
    import psp.sap.model.ActionEvent;
    import psp.sap.model.ActionEventCode;
    import psp.sap.model.Company;
    import psp.sap.model.CompanyKey;
    import psp.sap.model.EmployeeInfo;
    import psp.sap.model.PayrollRun;
    import psp.sap.model.SourceSystemEnum;
    import psp.sap.validators.SAPValidators;
    import psp.sap.viewmodel.events.EntityChangeEvent;
    import psp.sap.viewmodel.events.ViewModelEvent;

    [Event(name="activated", type="psp.sap.viewmodel.events.ViewModelEvent")]
    [Event(name="modelDataSetupCompleted", type="psp.sap.viewmodel.events.ViewModelEvent")]
    [Event(name="modelDataLoaded", type="psp.sap.viewmodel.events.ViewModelEvent")]
    [Event(name="deactivated", type="psp.sap.viewmodel.events.ViewModelEvent")]
    [Event(name="saveSucceeded", type="psp.sap.viewmodel.events.ViewModelEvent")]
    [Event(name="close", type="psp.sap.viewmodel.events.ViewModelEvent")]
    [Event(name="backingPropertyChanged", type="psp.sap.viewmodel.events.ViewModelEvent")]
    [Event(name="contextPropertyChanged", type="psp.sap.viewmodel.events.ViewModelEvent")]
    public class AbstractPartViewModel extends EventDispatcher {
        public function AbstractPartViewModel():void {
            if (SAP != null && SAP.instance != null) {
                SAP.instance.addEventListener(EntityChangeEvent.ENTITY_SAVED, onEntitySaved, false, 0, true);
            }
        }

        private var logger:ILogger = ClientLoggingTarget.getLogger(this);

        //all of these may be null sometimes or all the time
        //they represent the "context" of the part
        [Bindable] public var company:Company;
        [Bindable] public var companyKey:CompanyKey;
        [Bindable] public var inspector:AbstractInspectorViewModel;
        [Bindable] public var topic:InspectorTopicViewModel;
        [Bindable] public var host:AbstractPartViewModel;
        [Bindable] public var partViewModels:PartViewModelCollection = new PartViewModelCollection();

        //to find in collection by label
        [Bindable] public var label:String;

        [Bindable] public var saveFaulted:Boolean = false;
        [Bindable] public var saveMsg:String = ""; //may also be a fault message when (saveFaulted); may be a load error too
        [Bindable] public var hasSaved:Boolean;

        //these variables handle activation state and
        //prevent multiple activations/loads
        [Bindable] public var isDataLoading:Boolean = false;
        private var mGuardActivate:Boolean = false;

        //set this during loadModelData to determine how many responders should fire before continuing activation
        //use this if you are calling multiple, unrelated service calls
        protected var loadCount:int;


        [Bindable] public var activationState:String = ViewModelActivationStateEnum.NEW;
        [Bindable] public var backingPropertiesInitialized:Boolean = false;

        //these variables handle saving to
        //prevent saves when in an illegal state
        [Bindable] public var canSave:Boolean = false;

        private var mIsValid:Boolean = true;
        private var mValidators:Array = [];

        protected var entityChanged:Boolean = false;
        protected var watchedEntities:Array = [EntityChangeEvent.COMPANY];
        protected var reloadOnSave:Boolean = false;
        protected var reloadOnActivate:Boolean = true;
        protected var loadOnActivate:Boolean = true;
        protected var bindSaveMessageWithChildren:Boolean = false;

        // Map<backingPropertyName, copy>
        protected var backingPropertiesSnapshot:Object;

        // backing properties collection
        public var viewModelMetaData:ViewModelMetaData;

        // set this to be able to deep link when objects are in the activator but only need some fields
        public var shallowCopyFields:Array = null;
        //functions to call to populate the context variables before activating.
        [ArrayElementType("Function")]
        public var contextPopulateFunctions:ArrayCollection = new ArrayCollection();
        protected var preLoadCount:int;

        // override for custom saving message
        protected function get savingMessage():String {
            return null;
        }

        private function onEntitySaved(e:EntityChangeEvent):void {
            for each(var entityType:String in watchedEntities) {
                if (entityType == e.entityType) {
                    entityChanged = true;
                }
            }
        }

        //Activation Sequence
        public function setActivator(activator:Object):void {
            if (activator != null) {
                for (var key:String in activator) {
                    this[key] = activator[key];
                }
            }
        }

        /*
         activate() is called when the corresponding part view
         is made visible to the user.  The host is responsible for calling
         this function.
         */
        public final function activate():void {
            if (mGuardActivate) {
                //do not have two activation sequences going at once.
                return;
            }
            mGuardActivate = true;

            hasSaved = false;

            loadMetadata();

            new Call(preActivation).andOn(this, ViewModelEvent.PRE_ACTIVATION_COMPLETE).call(activate_afterPreActivationCompleted);
        }

        protected function preActivation():void {
            //default impl. is to do nothing and continue.
            preActivationComplete();
        }

        protected final function preActivationComplete():void {
            dispatchEvent(ViewModelEvent.createPreActivationEvent());
        }

        private final function activate_afterPreActivationCompleted():void {
            saveMsg = "";

            onActivating();

            if (contextPopulateFunctions.length > 0) {
                preLoadCount = contextPopulateFunctions.length;
                for each (var f:Function in contextPopulateFunctions) {
                    new Call(f).andOn(this, ViewModelEvent.CONTEXT_PROPERTY_CHANGED).call(onContextPropertyPopulated);
                }
            } else {
                activateAfterContextPopulated();
            }
        }

        private function get thisViewModel():AbstractPartViewModel {
            return this;
        }

        public function get guardActivate():Boolean {
            return mGuardActivate;
        }


        //Optional override
        protected function onActivating():void {
        }

        protected final function activate_afterSetupModelDataCompleted():void {
            activationState = ViewModelActivationStateEnum.ACTIVATED;

            onActivated();

            mGuardActivate = false;
            dispatchEvent(ViewModelEvent.createActivatedEvent());
            onActivationComplete();
        }

        //Optional override
        protected function onActivated():void {
        }

        protected function onActivationComplete():void {
        }

        private final function onContextPropertyPopulated():void {
            preLoadCount--;
            if (preLoadCount == 0) {
                contextPopulateFunctions.removeAll();
                activateAfterContextPopulated();
            }
        }

        private final function activateAfterContextPopulated():void {
            validateContextSet();

            //if !reloadOnActivate, do not reload unless the part has not been loaded once.
            if ((activationState != ViewModelActivationStateEnum.NEW && (entityChanged || reloadOnActivate)) || (activationState == ViewModelActivationStateEnum.NEW && loadOnActivate)) {
                entityChanged = false;
                new Call(setupModelData).andOn(this, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED).call(activate_afterSetupModelDataCompleted);
            } else {
                dispatchEvent(ViewModelEvent.createModelDataSetupCompletedEvent());
                activate_afterSetupModelDataCompleted();
            }
        }


        //Setup Model Data

        //Occasionally we may wish to setup model data without loading it, if so, call with doFetch false
        private final function setupModelData():void {
            if (isDataLoading) {
                //this should only happen if refresh is called twice (in which case we let the existing refresh finish by returning here)
                //it should not happen in activation because of mGuardActivate

                return;
            }

            isDataLoading = true;
            loadCount = 1; //default load count--if not set in loadModelData, will expect 1 call

            new Call(loadModelData).andOn(this, ViewModelEvent.MODEL_DATA_LOADED).call(setupModelData_afterModelDataLoaded);

        }

        private final function setupModelData_afterModelDataLoaded():void {
            initializeDefaults();
            coreInitializeBackingProperties();
            snapshotBackingProperties();
            isDataLoading = false;
            dispatchEvent(ViewModelEvent.createModelDataSetupCompletedEvent());
        }

        private function snapshotBackingProperties():void {
            if (viewModelMetaData == null) {
                return;
            }

            backingPropertiesSnapshot = new Object();
            for each(var backingProperty:BackingPropertyMetaData in viewModelMetaData.backingProperties) {
                if (backingProperty.trackHasChanged) {
                    backingPropertiesSnapshot[backingProperty.viewModelPropertyName] = ObjectUtil.copy(this[backingProperty.viewModelPropertyName]);
                    if (backingPropertiesSnapshot[backingProperty.viewModelPropertyName] is ArrayCollection) {
                        ArrayCollection(backingPropertiesSnapshot[backingProperty.viewModelPropertyName]).sort = ArrayCollection(this[backingProperty.viewModelPropertyName]).sort;
                        ArrayCollection(backingPropertiesSnapshot[backingProperty.viewModelPropertyName]).refresh();
                    }
                }
            }
            updateCanSave();
        }

        protected function initializeDefaults():void {
        }

        protected final function coreInitializeBackingProperties():void {
            initializeBackingProperties();
            updateCanSave();
            backingPropertiesInitialized = true;
            dispatchEvent(ViewModelEvent.createBackingPropertiesInitializedEvent());
        }

        protected function initializeBackingProperties():void {
        }

        protected function loadMetadata():void {
            viewModelMetaData = new ViewModelMetaData(this, updateCanSave);

            if (viewModelMetaData.backingProperties.length > 0) {
                // ** Careful the event listeners here are purposely set to not be weak references. **
                addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, onBackingPropertyChange, false, int.MAX_VALUE, false);
                for each (var backingProperty:BackingPropertyMetaData in viewModelMetaData.backingProperties) {
                    addBackingPropertyEventListeners(this[backingProperty.viewModelPropertyName], backingProperty);
                }
            }

            dispatchEvent(ViewModelEvent.createBackingPropertyChangedEvent());
            dispatchEvent(ViewModelEvent.createContextPropertyChangedEvent());
        }

        /**
         * Careful the event listeners here are purposely set to not be weak references
         * for some reason if the same view model is updates several times it loses this
         * property change event if it is not a strong reference listener.
         */
        private function onBackingPropertyChange(e:PropertyChangeEvent):void {
            var backingProperty:BackingPropertyMetaData = viewModelMetaData.backingProperties.getBackingProperty(e.property as String);
            if (backingProperty) {
                removeBackingPropertyEventListeners(e.oldValue, backingProperty);
                addBackingPropertyEventListeners(e.newValue, backingProperty);

                updateCanSave();
                dispatchEvent(ViewModelEvent.createBackingPropertyChangedEvent());
                if (backingProperty.contextProperty) {
                    dispatchEvent(ViewModelEvent.createContextPropertyChangedEvent());
                }
            }
        }

        private function removeBackingPropertyEventListeners(value:Object, backingProperty:BackingPropertyMetaData):void {
            if (value != null && "removeEventListener" in value) {
                if (value is IList) {
                    if (backingProperty.recursiveProperty) {
                        for each (var element:Object in value) {
                            if (!backingProperty.recursiveNoHasChangeTrackedProperties.contains(Type.forInstance(element).name)) {
                                removeBackingPropertyEventListeners(element, backingProperty);
                            }
                        }
                    } else {
                        value.removeEventListener(CollectionEvent.COLLECTION_CHANGE, propertyChangeUpdateCanSave);
                    }
                } else {
                    value.removeEventListener(PropertyChangeEvent.PROPERTY_CHANGE, propertyChangeUpdateCanSave);
                    if (backingProperty.recursiveProperty) {
                        var type:Type = Type.forInstance(value);
                        for each (var field:Field in type.fields) {
                            if (field.name in value) {
                                if (!backingProperty.recursiveNoHasChangeTrackedProperties.contains(field.name)) {
                                    removeBackingPropertyEventListeners(value[field.name], backingProperty);
                                }
                            }
                        }
                    }
                }
            }
        }

        private function addBackingPropertyEventListeners(value:Object, backingProperty:BackingPropertyMetaData):void {
            if (value != null && "addEventListener" in value) {
                if (value is IList) {
                    if (backingProperty.recursiveProperty) {
                        for each (var element:Object in value) {
                            if (!backingProperty.recursiveNoHasChangeTrackedProperties.contains(Type.forInstance(element).name)) {
                                addBackingPropertyEventListeners(element, backingProperty);
                            }
                        }
                    } else {
                        value.addEventListener(CollectionEvent.COLLECTION_CHANGE,
                                propertyChangeUpdateCanSave, false, int.MAX_VALUE, false);
                    }
                } else {
                    value.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE,
                            propertyChangeUpdateCanSave, false, int.MAX_VALUE, false);
                    if (backingProperty.recursiveProperty) {
                        var type:Type = Type.forInstance(value);
                        for each (var field:Field in type.fields) {
                            if (field.name in value) {
                                if (!backingProperty.recursiveNoHasChangeTrackedProperties.contains(field.name)) {
                                    addBackingPropertyEventListeners(value[field.name], backingProperty);
                                }
                            }
                        }
                    }
                }
            }
        }


        private function validateContextSet():void {
            for each (var backingProperty:BackingPropertyMetaData in viewModelMetaData.backingProperties) {
                if (backingProperty.contextProperty && backingProperty.requiredProperty) {
                    if (this[backingProperty.viewModelPropertyName] == null) {
                        logger.log(LogEventLevel.ERROR, "Context property {0}({1}) must be set to activate '{2}'",
                                backingProperty.viewModelPropertyName,
                                backingProperty.viewModelPropertyType,
                                this.label);
                        throw new Error("Context property " + backingProperty.viewModelPropertyName + " not set");
                    }
                }
            }
        }

        //loadModelData

        protected function loadModelData():void {
            //default impl. is to do nothing and continue.
            //@see modelDataLoaded
            modelDataLoaded();
        }

        protected final function createLoadModelDataResponder(result:Function = null, fault:Function = null, token:Object = null):IResponder {
            if (result == null) {
                result = emptyHandler;
            }
            if (fault == null) {
                fault = emptyHandler;
            }

            var pageResponder:IResponder = null;
            if (token != null)
                pageResponder = new AsyncResponder(result, fault, token);
            else
                pageResponder = new Responder(result, fault);

            pageResponder =
                    new AsyncResponder(onLoadModelDataSucceeded,
                            onLoadModelDataFaulted,
                            pageResponder);

            return pageResponder;
        }

        //The following two functions are final--additional behavior
        //goes in its own handler passed to createLoadModelDataResponder
        protected final function onLoadModelDataSucceeded(e:ResultEvent, token:Object):void {
            var pageResponder:IResponder = IResponder(token);
            pageResponder.result(e);
            loadCount--;
            if (loadCount == 0) {
                modelDataLoaded();
            }
        }

        protected final function onLoadModelDataFaulted(e:FaultEvent, token:Object = null):void {
            saveFaulted = true;
            saveMsg = ServiceUtils.getFaultMessage(e);
            isDataLoading = false;
            if (token) {
                var pageResponder:IResponder = IResponder(token);
                if (pageResponder.fault != null) {
                    pageResponder.fault(e);
                }
            }

            //also end the setup model data so activation can finish (though without modifying backing properties)
            dispatchEvent(ViewModelEvent.createModelDataSetupCompletedEvent());

            logger.error(" onModelDataLoadedFaulted Details:" + ServiceUtils.getFaultDetails(e));
        }

        //subclasses must use createLoadModelDataResponder to create a responder for the remote method,
        //dispatch the loaded event when done loading, or call this function.
        //Note that any treatment besides createLoadModelDataResponder should be resiliant to faults
        protected final function modelDataLoaded():void {
            dispatchEvent(ViewModelEvent.createModelDataLoadedEvent());
        }


        //Deactivate


        //called when the part is no longer being displayed to the user
        //again, the host is responsible for actually hiding it and also
        //calling this method
        public final function deactivate():void {
            activationState = ViewModelActivationStateEnum.DEACTIVATED;

            // remove backing property change event listener
            removeEventListener(PropertyChangeEvent.PROPERTY_CHANGE, onBackingPropertyChange);

            dispatchEvent(ViewModelEvent.createDeactivatedEvent());
            onDeactivated();
        }

        protected function onDeactivated():void {
        }


        //Savable state checking

        public function get validators():Array {
            return mValidators;
        }

        public function clearValidators():void {
            for each (var validator:Validator in mValidators)
                validator.listener = null;

            mValidators = [];
        }

        public function getValidator(source:Object, property:String):Validator {
            var validators:ArrayCollection = getValidators(source, property);
            return validators.length > 0 ? Validator(validators.getItemAt(0)) : null;
        }

        [ArrayElementType("mx.validators.Validator")]
        public function getValidators(source:Object, property:String):ArrayCollection {
            var matchedValidators:ArrayCollection = new ArrayCollection();
            for each (var validator:Validator in validators) {
                if (validator.source == source && validator.property == property) {
                    matchedValidators.addItem(validator);
                }
            }
            return matchedValidators;
        }

        [Bindable]
        public function get isValid():Boolean {
            return mIsValid;
        }

        public function set isValid(value:Boolean):void {
            mIsValid = value;
            if (!mIsValid)
                updateCanSave();
        }

        //final
        protected function updateIsValid():void {
            isValid = evaluateIsValid();
        }

        //final
        public function updateCanSave():void {
            canSave = evaluateCanSave();
        }

        private function propertyChangeUpdateCanSave(e:Event):void {
            updateCanSave();
        }

        protected function evaluateCanSave():Boolean {
            updateIsValid();
            return isValid && hasChanged;
        }

        //override
        public function get hasChanged():Boolean {
            return backingDTOHasChanged;
        }

        protected function get backingDTOHasChanged():Boolean {
            if (backingPropertiesSnapshot == null || viewModelMetaData.backingProperties == null) {
                return false;
            }

            for each(var backingProperty:BackingPropertyMetaData in viewModelMetaData.backingProperties) {
                if (backingProperty.trackHasChanged && ObjectUtil.compare(this[backingProperty.viewModelPropertyName], backingPropertiesSnapshot[backingProperty.viewModelPropertyName]) != 0) {
                    return true;
                }
            }

            return false;
        }

        protected function evaluateIsValid(fireEvents:Boolean = true):Boolean {
            return SAPValidators.validateAll(mValidators, !fireEvents).length == 0;
        }


        //Saving


        //normal save--check if canSave, then save
        public final function save():void {
            logger.info(" save() called canSave = " + canSave);
            if (!canSave)
                return;

            doSave();

        }


        //e.g. if hasChanged or isValid is not true, but save should be done.
        protected final function forceSave():void {
            doSave();
        }

        private final function doSave():void {
            saveMsg = "";
            hasSaved = true;

            SAP.instance.showProgress(savingMessage);

            executeSave();
        }

        //Execute save must call onSaveSucceeded_internal or onSaveFaulted_internal
        //or use createSaveResponder (recommended)
        protected function executeSave():void {
        }


        protected final function createSaveResponder(result:Function = null, fault:Function = null, token:Object = null):IResponder {
            if (result == null) {
                result = emptyHandler;
            }

            if (fault == null) {
                fault = emptyHandler;
            }

            var pageResponder:IResponder = null;
            if (token != null)
                pageResponder = new AsyncResponder(result, fault, token);
            else
                pageResponder = new Responder(result, fault);

            pageResponder =
                    new AsyncResponder(onSaveSucceeded_internal,
                            onSaveFaulted_internal,
                            pageResponder);

            return pageResponder;
        }

        protected final function createRtbAutomationSaveResponder(result:Function = null, fault:Function = null, token:Object = null):IResponder {
            if (result == null) {
                result = emptyHandler;
            }

            if (fault == null) {
                fault = emptyHandler;
            }

            var pageResponder:IResponder = null;
            if (token != null)
                pageResponder = new AsyncResponder(result, fault, token);
            else
                pageResponder = new Responder(result, fault);

            pageResponder =
                    new AsyncResponder(onSaveSucceeded_rtbAutomation,
                            onSaveFaulted_rtbAutomation,
                            pageResponder);

            return pageResponder;
        }
        //some reason null handlers can't be detected
        private function emptyHandler(e:Event):void {
        }


        //calling these _internal so that there aren't naming clashes in all the functions
        //that are already using an onSaveSucceeded
        protected final function onSaveSucceeded_internal(e:ResultEvent = null, token:Object = null):void {
            logger.info(" onSaveSucceeded called");
            SAP.instance.hideProgress();

            saveFaulted = false;
            saveMsg = SAP.instance.configuration.defaultSaveSucceededMsg;

            if (token != null) {
                var pageResponder:IResponder = IResponder(token);
                pageResponder.result(e);
            }

            dispatchEvent(ViewModelEvent.createSaveSucceededEvent());

            //broadcast change for pages to update
            SAP.instance.dispatchEvent(
                    EntityChangeEvent.createEvent(
                            EntityChangeEvent.ENTITY_SAVED, EntityChangeEvent.COMPANY));

            // reload data
            if (reloadOnSave) {
                setupModelData();
            } else {
                //otherwise only refresh backing properties
                coreInitializeBackingProperties();
            }
        }

        protected final function onSaveFaulted_internal(e:FaultEvent = null, token:Object = null):void {
            SAP.instance.hideProgress();
            saveFaulted = true;
            saveMsg = ServiceUtils.getFaultMessage(e);
            logger.error(" onSaveFaulted called Details: " + ServiceUtils.getFaultDetails(e));

            if (token != null) {
                var pageResponder:IResponder = IResponder(token);
                pageResponder.fault(e);

            }

        }

//calling these _internal so that there aren't naming clashes in all the functions
        //that are already using an onSaveSucceeded
        protected final function onSaveSucceeded_rtbAutomation(e:ResultEvent = null, token:Object = null):void {
            SAP.instance.hideProgress();

            saveFaulted = false;
            saveMsg = SAP.instance.configuration.defaultSaveSucceededMsg;

            if (token != null) {
                var pageResponder:IResponder = IResponder(token);
                pageResponder.result(e);
                Alert.show(saveMsg);
            }else{
                Alert.show(saveMsg);
            }

            dispatchEvent(ViewModelEvent.createSaveSucceededEvent());

            //broadcast change for pages to update
            SAP.instance.dispatchEvent(
                    EntityChangeEvent.createEvent(
                            EntityChangeEvent.ENTITY_SAVED, EntityChangeEvent.COMPANY));

            // reload data
            if (reloadOnSave) {
                setupModelData();
            } else {
                //otherwise only refresh backing properties
                coreInitializeBackingProperties();
            }
        }

        protected final function onSaveFaulted_rtbAutomation(e:FaultEvent = null, token:Object = null):void {
            SAP.instance.hideProgress();
            saveFaulted = true;
            saveMsg = ServiceUtils.getFaultMessage(e);
            logger.error(" onSaveFaulted called Details: " + ServiceUtils.getFaultDetails(e));

            if (token != null) {
                var pageResponder:IResponder = IResponder(token);
                pageResponder.fault(e);
                Alert.show(saveMsg);
            }else{
                Alert.show(saveMsg);
            }

        }

        //Other

        public function reset():void {
            coreInitializeBackingProperties();
        }

        public function cancel():void {
            saveFaulted = false;
            saveMsg = "";
            hasSaved = false;
            dispatchEvent(new ViewModelEvent(ViewModelEvent.CLOSE, false, true));
        }

        public function refresh(resetSaveMessage:Boolean = true):void {
            if (resetSaveMessage) {
                saveFaulted = false;
                saveMsg = "";
            }
            setupModelData();
            //this happens simultaneously
            onRefresh();
        }

        protected function onRefresh():void {
        }


        public function addNewChildPartViewModel(partClass:Class, label:String = null):AbstractPartViewModel {
            var part:AbstractPartViewModel = createNewChildPartViewModel(partClass, label);
            this.partViewModels.addItem(part);
            return part;
        }

        public function createNewChildPartViewModel(partClass:Class, label:String = null):AbstractPartViewModel {
            var partFactory:ClassFactory = new ClassFactory(partClass);
            var part:AbstractPartViewModel = partFactory.newInstance();

            if (part.hasOwnProperty("companyKey"))
                BindingUtils.bindProperty(part, "companyKey", this, "companyKey");

            BindingUtils.bindProperty(part, "company", this, "company");
            BindingUtils.bindProperty(part, "inspector", this, "inspector");
            BindingUtils.bindProperty(part, "topic", this, "topic");

            part.host = this;

            if (label != null) {
                part.label = label;
            }

            if (bindSaveMessageWithChildren) {
                BindingUtils.bindProperty(part, "saveMsg", this, "saveMsg");
                BindingUtils.bindProperty(part, "saveFaulted", this, "saveFaulted");
                BindingUtils.bindProperty(this, "saveMsg", part, "saveMsg");
                BindingUtils.bindProperty(this, "saveFaulted", part, "saveFaulted");
            }
            return part;
        }


        /**
         * recursively find part by label
         */
        public function findPartByLabel(label:String):AbstractPartViewModel {
            if (this.label == label) {
                return this;
            }
            for each(var part:AbstractPartViewModel in partViewModels) {
                if (part.label == label) {
                    return part;
                } else {
                    var subPart:AbstractPartViewModel = part.findPartByLabel(label);
                    if (subPart != null) {
                        return subPart;
                    }
                }
            }

            return null;
        }

        //override this to handle custom model objects
        public function serializeActivatorValue(value:Object):String {
            if (value == null) {
                return "";
            }
            if (value is String) {
                return value as String;
            } else if (value is Number) {
                return (value as Number).toString();
            } else if (value is int) {
                return (value as int).toString();
            } else if (value is Date) {
                var dateFormatter:DateFormatter = new DateFormatter();
                dateFormatter.formatString = "YYYYMMDDHHNNSS";
                return dateFormatter.format(value);
            } else if (value is Boolean) {
                return value ? "Y" : "N";
            } else if (value is SourceSystemEnum) {
                return (value as SourceSystemEnum).label;
            } else if (value is ActionEvent) {
                return (value as ActionEvent).code.toString();
            } else if (value is PayrollRun) {
                return (value as PayrollRun).sourcePayRunId;
            } else if (value is EmployeeInfo) {
                return (value as EmployeeInfo).employeeId.toString();
            } else if (shallowCopyFields != null) {
                var serializedString:String = "";
                for each (var field:String in shallowCopyFields) {
                    if (field in value) {
                        serializedString += serializeActivatorValue(value[field]) + "~";
                    } else {
                        serializedString += "~";
                    }
                }
                return serializedString.substr(0, serializedString.length - 1);
            } else {
                return "";
            }
        }

        public function deserializeActivatorValue(value:String, target:Class, field:String):Object {
            if (target == String) {
                return value;
            } else if (target == Number) {
                return parseFloat(value);
            } else if (target == int) {
                return parseInt(value);
            } else if (target == Date) {
                var d:Date = new Date();
                d.fullYear = parseInt(value.substr(0, 4));
                d.month = parseInt(value.substr(4, 2));
                d.date = parseInt(value.substr(6, 2));
                d.hours = parseInt(value.substr(8, 2));
                d.minutes = parseInt(value.substr(10, 2));
                d.seconds = parseInt(value.substr(12, 2));
                return d;
            } else if (target == Boolean) {
                return value == "Y";
            } else if (target == SourceSystemEnum) {
                return SourceSystemEnum.fromLabel(value);
            } else if (target == ActionEvent) {
                return new ActionEvent(ActionEventCode.fromLabel(value));
            } else if (target == PayrollRun) {
                //if it's a history event, don't want to reload, so see if already there
                if (this[field] != null && (this[field] as PayrollRun).sourcePayRunId == value) {
                    return this[field];
                } else {
                    this.contextPopulateFunctions.addItem(function ():void {
                        SAP.instance.payrollRunService.findPayrollRun(company.sourceSystemCd, company.companyId, value, new Responder(
                                function (e:ResultEvent):void {
                                    thisViewModel[field] = e.result;
                                }, onLoadModelDataFaulted));
                    });
                    return null;
                }
            } else if (target == EmployeeInfo) {
                this.contextPopulateFunctions.addItem(function ():void {
                    SAP.instance.companyService.getEmployee(company.companyId, company.sourceSystemCd, value, new Responder(
                            function (e:ResultEvent):void {
                                thisViewModel[field] = e.result;
                            }, onLoadModelDataFaulted));
                });
                return null;
            } else if (shallowCopyFields != null) {
                var fields:Array = value.split("~");
                var object:* = new target();
                var anythingSet:Boolean = false;
                for (var i:int = 0; i < fields.length; i++) {
                    if (shallowCopyFields[i] in object) {
                        //recursive-ish, but not really designed for objects inside objects
                        object[shallowCopyFields[i]] = deserializeActivatorValue(fields[i], Type.forInstance(object).getField(shallowCopyFields[i]).type.clazz, field);
                        anythingSet = true;
                    }
                }
                return anythingSet ? object : null;
            } else {
                return null;
            }
        }

    }
}

