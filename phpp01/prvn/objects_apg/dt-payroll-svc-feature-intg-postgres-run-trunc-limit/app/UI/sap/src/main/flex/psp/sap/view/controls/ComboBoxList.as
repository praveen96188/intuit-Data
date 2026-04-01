package psp.sap.view.controls
{
	import mx.controls.List;

	public class ComboBoxList extends List
	{
		public function ComboBoxList()
		{
			super();
		}
		
		public function getScrollbarWidth():int
        {
            var scrollbarWidth:int = 0;
            if (this.verticalScrollBar != null)
            {
                scrollbarWidth = this.verticalScrollBar.width;
            }
            return scrollbarWidth;
        }
		
	}
}