package psp.sap.view
{
	import flash.display.Graphics;
	
	import mx.skins.Border;

	public class FlatComboBoxSkin extends Border
	{
		
		public function FlatComboBoxSkin()
		{
			super();
		}
		

		    /**
             *  @private
             */    
            override public function get measuredWidth():Number
            {
                return 22;
            }
            
            
            /**
             *  @private
             */        
            override public function get measuredHeight():Number
            {
                return 22;
            }
            
            
            /**
             * 
             * @param w
             * @param h
             * 
             */             
            override protected function updateDisplayList(w:Number, h:Number):void
            {
                var g:Graphics = graphics;
	            g.clear();
	            
	            var rectW:Number = w;
                var rectH:Number = h;
                var hightlightAlphas:Array = [1.0, 1.0];
                var innerRectColors:Array = [0xFFFFFF, 0xFFFFFF];
                var innerRectAlphas:Array = [1.0, 1.0];
                var arrowColors:Array = [0x000000, 0x000000];

                var editable:Boolean = true;
                if (name.indexOf("editable") < 0)
                {
                        editable = false;
                }
                
                switch(name)
                {
                        case "editableUpSkin":
                        case "upSkin":
                        {
                                break;
                        }
                        case "editableOverSkin":
                        case "overSkin":
                        {
                                innerRectAlphas = [1.0, 1.0];
                                arrowColors = [0x444444, 0x444444];
                                break;
                        }
                        case "editableDownSkin":
                        case "downSkin":
                        {
                                hightlightAlphas = [0.15, 0.15];
                                innerRectAlphas = [1.0, 1.0];
                                arrowColors = [0x000000, 0x000000];
                                break;
                        }
                        case "editableDisabledSkin":
                        case "disabledSkin":
                        {
                                hightlightAlphas = [0.05, 0.05];
                                innerRectColors = [0xCCCCCC, 0xCCCCCC];
                                innerRectAlphas = [1.0, 1.0];
                                arrowColors = [0x000000, 0x000000];
                                break;
                        }
                }
                
                if(!editable)
                {
                        //
                        DrawUtil.drawDoubleRect(g, [0xFFFFFF, 0xFFFFFF], hightlightAlphas, 0, 0, rectW, rectH, 1, 1, rectW-2, rectH-2);
                        DrawUtil.drawDoubleRect(g, [0x000000, 0x000000], [0.6, 0.6], 1, 1, rectW-2, rectH-2, 2, 2, rectW-4, rectH-4);
                        DrawUtil.drawSingleRect(g, innerRectColors, innerRectAlphas, 2, 2, rectW-4, rectH-4);
                        DrawUtil.drawDoubleRect(g, [0xFFFFFF, 0xFFFFFF], [0.08, 0.03], 2, 2, rectW-4, rectH-4, 3, 3, rectW-6, rectH-6);
                        //
                        //DrawUtil.drawSingleRect(g, [0x000000, 0x000000], [0.85, 0.6], rectW-18, 4, 1, rectH-8);
                        //DrawUtil.drawSingleRect(g, [0x333333, 0x333333], [0.8, 0.8], rectW-18+1, 4, 1, rectH-8);
                        DrawUtil.drawSingleRect(g, [0xCCCCCC, 0xCCCCCC], [0.9, 0.9], rectW-18, 2, 16, rectH-4);
                        //
                        DrawUtil.drawArrow(g, 5, arrowColors, [1, 1], rectW - 13, Math.floor(rectH/2) - Math.floor(5/2));
                }
                else
                {
                        DrawUtil.drawDoubleRect(g, [0xFFFFFF, 0xFFFFFF], hightlightAlphas, 0, 0, rectW, rectH, 0, 1, rectW-1, rectH-2);
                        DrawUtil.drawDoubleRect(g, [0x000000, 0x000000], [0.6, 0.6], 0, 1, rectW-1, rectH-2, 0, 2, rectW-2, rectH-4);
                        DrawUtil.drawSingleRect(g, innerRectColors, innerRectAlphas, 0, 2, rectW-2, rectH-4);
                        DrawUtil.drawDoubleRect(g, [0xFFFFFF, 0xFFFFFF], [0.08, 0.03], 0, 2, rectW-2, rectH-4, 1, 3, rectW-4, rectH-6);
                        //
                        //DrawUtil.drawSingleRect(g, [0x000000, 0x000000], [0.85, 0.65], 0, 1, 1, rectH-2);
                        //
                        DrawUtil.drawArrow(g, 5, arrowColors, [1, 1], Math.floor(((rectW-1) - 7)/2), Math.floor(rectH/2) - Math.floor(5/2));
                }
        } 
		
		
		
		
		
		
	    
  	}
  
  
}