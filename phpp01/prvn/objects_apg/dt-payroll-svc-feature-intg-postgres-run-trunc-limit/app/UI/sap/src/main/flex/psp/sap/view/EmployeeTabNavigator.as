
package psp.sap.view
{
    import flexlib.containers.SuperTabNavigator;
    import flexlib.events.SuperTabEvent;

    import mx.core.Container;
    import mx.core.UIComponent;
    import mx.events.FlexEvent;
    import mx.events.IndexChangedEvent;

    import psp.sap.application.ViewModelSync;
    import psp.sap.application.enums.ViewModelActivationStateEnum;
    import psp.sap.application.events.SAPEvent;
    import psp.sap.model.EmployeeInfo;
    import psp.sap.viewmodel.AbstractPartViewModel;
    import psp.sap.viewmodel.EmployeeInfoTabViewModel;
    import psp.sap.viewmodel.EmployeesViewModel;
    import psp.sap.viewmodel.PartAdditionStrategy;
    import psp.sap.viewmodel.events.ViewModelEvent;

    public class EmployeeTabNavigator extends SuperTabNavigator
	{
		private var mViewModel:EmployeesViewModel;
		private var mTabLabelFunction:Function;
		private var mCreationComplete:Boolean = false;
		
		[Bindable]
		public function get tabLabelFunction():Function
	    {
	        return mTabLabelFunction;
	    }
	
	    public function set tabLabelFunction(value:Function):void
	    {
	        mTabLabelFunction = value;
	        if (tabBar != null)
	        {
	            tabBar.labelFunction = mTabLabelFunction;
	        }	    
	    }		
		
		public function EmployeeTabNavigator()
		{
			super();
			addEventListener(FlexEvent.CREATION_COMPLETE, onCreationComplete, false, 0, true);
		}
		
		[Bindable]
		public function get viewModel():EmployeesViewModel {
			return mViewModel;
		}
		
		public function set viewModel(value:EmployeesViewModel):void {
			if(value != null){
				value.removeEventListener(ViewModelEvent.ACTIVATED, onActivated);
				value.addEventListener(ViewModelEvent.ACTIVATED, onActivated, false, 0, true);
			}
			
			mViewModel = value;
			
			if(mCreationComplete && value!= null)
			{
				syncViewModels(Container(getChildAt(selectedIndex)));
				activate();

			}
		}
		
		private function onActivated(e:ViewModelEvent):void {
			activate();
		}
		
		private function onCreationComplete(e:FlexEvent):void {
			mCreationComplete = true;
			if(viewModel != null)
			{
				syncViewModels(Container(getChildAt(selectedIndex)));
				activate();
			}
			
			//Add event listener for the first one
			getChildAt(0).addEventListener("OpenEmployee", openEmployeeWithEvent, false, 0, true);

		}		

		protected function openEmployeeWithEvent(event:SAPEvent):void {
			var employee:EmployeeInfo = event.data as EmployeeInfo;
			openEmployee(employee);
		}	

		public function openEmployee(employeeInfo:EmployeeInfo):void {

			
			/* we must check to make sure that it doesn't already exist */
			for(var i:int = 1; i < this.numChildren; i++)
			{
				var employeeInfoTabView:EmployeeInfoTabView = this.getChildAt(i) as EmployeeInfoTabView;
				if(employeeInfoTabView.viewModel != null && employeeInfoTabView.viewModel.employeeInfo.employeeId == employeeInfo.employeeId)
				{
					//Select it
					selectedIndex = i;
					return;
				}
			}
			
			//Add tab and add an employee with that id
			var newChild:EmployeeInfoTabView = new EmployeeInfoTabView();
			var newChildViewModel:AbstractPartViewModel =  viewModel.addNewPart(EmployeeInfoTabViewModel, employeeInfo.employeeId, PartAdditionStrategy.LAZY_COMPOSITE);
			newChildViewModel.company = viewModel.company;
			newChildViewModel["employeeInfo"] = employeeInfo;
			newChild["label"] = employeeInfo.fullName;
			newChild["viewModel"] = newChildViewModel;

			//When creation complete, sync the viewModels and set selectedIndex to activate
			newChild.addEventListener(FlexEvent.CREATION_COMPLETE, function(event:FlexEvent):void { 
				ViewModelSync.associatePageViewModels(newChild, newChildViewModel);
				newChild.parent["selectedIndex"] = newChild.parent["numChildren"] - 1;
			}, false, 0, true);

			this.addChild(newChild);		
			
			//Update tab navigator style for customizing
			if(this.popupButton != null)
			{
				popupButton.label = "More";
			}
		}		


		//This is to prevent bad logic. If we close a tab, we want to track which one was recently closed
		//so that when we go to onTabChanged, we can differentiate between a regular click and a 
		//"we closed it and flex changed the index". Otherwise, when we grab the oldIndex, the element will
		//not exist or we'll be selecting the wrong one. (note: is set is onTabClose and is reset in onTabChanged)
		protected var tabIndexWasClosed:Number = -1;


		protected function onTabClose(e:SuperTabEvent):void
		{	
			//Track which tab was recently closed (note: is reset in onTabChanged)
			tabIndexWasClosed = e.tabIndex;
			
			if(e.tabIndex < this.numChildren - 1)
			{
				callLater(selectNewTab, [e.tabIndex]);
			} else {
				callLater(selectNewTab, [e.tabIndex - 1]);
			}
		}		

		protected function selectNewTab(tabIndexToSelect:Number):void {
			this.selectedIndex = tabIndexToSelect;
		}

		override protected function createChildren():void
	    {
	    	super.createChildren();	    		    	
	    	
	    	this.addEventListener(IndexChangedEvent.CHANGE, onTabChanged, false, 0, true);
	    	this.addEventListener(SuperTabEvent.TAB_CLOSE,onTabClose,false,0,true);
	    	
	    	if (tabBar != null && tabLabelFunction != null)
	        {
	            tabBar.labelFunction = tabLabelFunction;
	        }
	    }	    	    
	    	    
	    private function syncViewModels(component:UIComponent):void {
	   		ViewModelSync.associatePageViewModels(component, viewModel);
	    } 	    
	    	    
	    	    
	    private function onTabChanged(event:IndexChangedEvent):void {
	    	//syncViewModels(UIComponent(event.target));
	    	
	    	var child:Container;    	
	    	
	    	// deactivate the old tab (only if the old tab was not removed)
	    	if(tabIndexWasClosed == -1)
	    	{
	    		child = Container(getChildAt(event.oldIndex));
		    	if(child.hasOwnProperty("viewModel") && child["viewModel"] is AbstractPartViewModel){
		    		AbstractPartViewModel(child["viewModel"]).deactivate();
		    	}
	    	} else {
		    	tabIndexWasClosed = -1;
	    	}
	    	
	    	
	    	// activate the new tab
	    	child = Container(getChildAt(event.newIndex));
	    	if(child.hasOwnProperty("viewModel") && child["viewModel"] is AbstractPartViewModel){
	    		AbstractPartViewModel(child["viewModel"]).activate();
	    	}
	    }	    	    
	    
	    public function activate():void {
	    	var child:Container;
	    	if(selectedIndex > -1){
	    		child = Container(getChildAt(selectedIndex));	    		
	    	}
	    	else if(selectedIndex == -1 && numChildren > 1){
	    		selectedIndex = 0;
	    		child = Container(getChildAt(selectedIndex));	    		
	    	}
	    	if(child != null && 
	    		child.hasOwnProperty("viewModel") && 
	    			child["viewModel"] != null &&
	    			child["viewModel"] is AbstractPartViewModel){
	    			if(AbstractPartViewModel(child["viewModel"]).activationState != ViewModelActivationStateEnum.ACTIVATED){
	    				AbstractPartViewModel(child["viewModel"]).activate();
	    			}
	    		}
	    }	    	    
		
	}
}