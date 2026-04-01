package psp.sap.viewmodel
{
	public class PartsTabNavigatorViewModel extends CompositePartViewModel
	{
				
		public function PartsTabNavigatorViewModel()
		{
			super();
			this.subpartStrategy = PartAdditionStrategy.SINGLE;
            bindSaveMessageWithChildren = true;
		}
		
		public function validSingleCompositePartCheck():void {
			if(defaultSinglePart == null){
				throw new Error("A Default part is required");
			}
			
			for (var key:Object in partStrategyMap) {
				var part:AbstractPartViewModel = AbstractPartViewModel(key);
				if (partStrategyMap[part] != PartAdditionStrategy.SINGLE){
					throw new Error("Only single parts are allowed for this control");
				}
			}
		}

	}
}