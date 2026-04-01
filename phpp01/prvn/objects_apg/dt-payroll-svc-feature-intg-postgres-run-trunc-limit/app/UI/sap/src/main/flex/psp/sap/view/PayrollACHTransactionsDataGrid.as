package psp.sap.view
{
	import mx.controls.DataGrid;
	import mx.controls.listClasses.IListItemRenderer;
    import psp.sap.model.MoneyMovementTransaction;

	public class PayrollACHTransactionsDataGrid extends DataGrid
	{
		public function PayrollACHTransactionsDataGrid()
		{
			super();
		}
		
		public function selectRow(mmt:MoneyMovementTransaction):void {
            var rowIndex:int = 0;
            for(var i:int=0; i<listItems.length; i++){
               if ((listItems[i][0].data as MoneyMovementTransaction) == mmt){
                   rowIndex = i;
                   break;
               }
           }
            var listItem:IListItemRenderer = listItems[rowIndex][0];
			var uid:String = itemToUID(listItem.data);
			listItem = UIDToItemRenderer(uid);
			selectItem(listItem, false, false);
		}
		
		public function clearSelections():void {
			clearSelected();
		}
				
		override public function isItemSelectable(data:Object):Boolean
	    {	        
	        if (data == null)
	            return false;
	
	        return true;
	    }
		
	}
}