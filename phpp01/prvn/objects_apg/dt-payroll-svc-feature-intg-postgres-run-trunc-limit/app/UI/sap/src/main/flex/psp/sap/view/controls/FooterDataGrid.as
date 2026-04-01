package psp.sap.view.controls
{
    import flash.display.DisplayObject;

    import mx.core.EdgeMetrics;
    import mx.core.IUIComponent;
    import mx.core.mx_internal;
    import mx.styles.ISimpleStyleClient;

    public class FooterDataGrid extends SAPDataGrid
	{
        use namespace mx_internal;

		public var fudgeFactor:Number = 1;
		
		public function FooterDataGrid()
		{
			super();
		}
	
		override public function get borderMetrics():EdgeMetrics
		{
			return (border as FooterBorder).borderMetrics;
		}
	
		override protected function createBorder():void
		{
	        if (!border)
	        {
	            var borderClass:Class = FooterBorder;
	
	            border = new borderClass();
	
	            if (border is IUIComponent)
	                IUIComponent(border).enabled = enabled;
	            if (border is ISimpleStyleClient)
	                ISimpleStyleClient(border).styleName = this;
	
	            // Add the border behind all the children.
	            addChildAt(DisplayObject(border), 0);
	
	            invalidateDisplayList();
	        }
		}
		
		override protected function measure():void
	    {
	        super.measure();
			
			// I WIN!!! this is a hack to get the stupid data grid to stop adding a scrollbar 
			// when the rowCount == the number of items in the collection 
	        if (explicitRowCount != -1)
	        {
	            measuredHeight += fudgeFactor;
	            measuredMinHeight += fudgeFactor;
	        }		       	
	    }
	    
	    override protected function updateDisplayList(unscaledWidth:Number,
                                                  unscaledHeight:Number):void
    	{
	        //I'm not entirely sure what's going on here, but without this, there is an NPE
            if (collectionIterator == null) {
                return;
            }

            super.updateDisplayList(unscaledWidth, unscaledHeight);
	        if (border && border is FooterBorder)
	        {
	            FooterBorder(border).externalUpdateDisplayList(unscaledWidth, unscaledHeight);           
	        }	        
	    }
	
	}

}