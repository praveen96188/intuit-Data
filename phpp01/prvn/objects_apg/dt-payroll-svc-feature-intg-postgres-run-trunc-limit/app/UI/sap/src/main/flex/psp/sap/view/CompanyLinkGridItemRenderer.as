package psp.sap.view
{
	import flash.display.DisplayObjectContainer;
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	import flash.ui.Keyboard;
	
	import mx.controls.DataGrid;
	import mx.controls.LinkButton;
	import mx.events.DataGridEvent;
	
	import psp.sap.model.Company;
	import psp.sap.view.controls.CompanyDataGridColumn;

	/**
	 * Extend the LinkButton to act more as a straight-hyperlink
	 * by setting the selectionColor and rollOverColor styles to
	 * the background of the DataGrid.
	 * 
	 * The LinkButton broadcasts its click event through the
	 * owning DataGrid via a "companySelected" event.
	 */
	 
	[Event(name="companyClick", type="mx.events.DataGridEvent")]	 
	public class CompanyLinkGridItemRenderer extends LinkButton
	{
		public function CompanyLinkGridItemRenderer()
		{
			super();
			this.useHandCursor = true;
			this.setStyle("textAlign", "left");
			this.setStyle("color", "blue");
			
			this.addEventListener(MouseEvent.CLICK, onCompanySelected, false, 0, true);
			this.addEventListener(MouseEvent.ROLL_OVER, onRollover, false, 0, true);
			this.addEventListener(MouseEvent.ROLL_OUT, onRollout, false, 0, true);
			this.addEventListener(KeyboardEvent.KEY_DOWN, onKeyDown,false,0,true);
		}
		
		private function onKeyDown(e:KeyboardEvent):void {
			if (e.keyCode == Keyboard.ENTER) {
				callLater(onCompanySelected);
			}	
		}
		
		override public function set owner(value:DisplayObjectContainer):void {
			super.owner = value;
			var dg:DataGrid = DataGrid(this.owner);
			var backColor:Object = dg.getStyle("backgroundColor");
			this.setStyle("selectionColor", backColor);
			this.setStyle("rollOverColor", backColor);
		}
		
		override protected function rollOverHandler(e:MouseEvent):void {
			// disable the roll over painting
		}
		
		private function onCompanySelected(e:MouseEvent=null):void {
			var dge:DataGridEvent = new DataGridEvent("companyClick");			
			dge.itemRenderer = this;
			/*
			Dear future maintainer,
				You may be wondering what the hell this is doing.
			I will tell you.  The event needs to be received on the column.
			But you can't bubble because it never gets there.
			And the "owner" of the renderer is the datagrid.
			So where is the column?  Well, the column is stored in,
			you guessed it, the styleName property.  I think even Raffi would
			have to comment this one.
			
			Sincerely,
			David
			*/
			(this.styleName as CompanyDataGridColumn).dispatchEvent(dge);						
		}
		
		private function onRollover(e:MouseEvent):void {
			this.setStyle("textDecoration", "underline");
		}
		
		private function onRollout(e:MouseEvent):void {
			this.setStyle("textDecoration", "none");
		}
		
		override public function set data(value:Object):void {
			super.data = value;
			//might be a Company, might be an ActivationsChecklistSummary
			//but they both have legalName, so we won't cast.
			this.label = value.legalName;
		}
		
		override public function get automationName():String
	    {
	        return "companyLink_" + label;	            
	    }		
	    
	    //this magical beast makes the DG not crash when tabbing
	    //further it makes the link not disappear
	    //magic!
	    public function get text():Object {
	    	return this.label;
	    }
		
	}
}