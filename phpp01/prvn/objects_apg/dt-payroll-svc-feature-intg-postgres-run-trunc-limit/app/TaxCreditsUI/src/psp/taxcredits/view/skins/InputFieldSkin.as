package psp.taxcredits.view.skins
{

    import mx.skins.RectangularBorder;
    import flash.display.Graphics;
    import mx.graphics.RectangularDropShadow;

	import flash.display.DisplayObject;
    import flash.display.Shape;
    
    import mx.core.EdgeMetrics;
    import mx.core.IChildList;
    import mx.core.IContainer;
    import mx.core.IRawChildrenContainer;
    import mx.skins.halo.HaloBorder;

    public class InputFieldSkin extends HaloBorder
    {

        private var dropShadow:RectangularDropShadow;

        override protected function updateDisplayList 
        (unscaledWidth:Number, unscaledHeight:Number):void 
        {

            super.updateDisplayList(unscaledWidth, unscaledHeight);
            var cornerRadius:Number = getStyle("cornerRadius");
            var backgroundColor:int = getStyle("backgroundColor");
            var borderColor:int = getStyle("borderColor");
            var backgroundAlpha:Number = getStyle("backgroundAlpha");
            var style:Object = getStyle("backgroundPosition");
            graphics.clear();     
            // Background
			graphics.lineStyle(1,borderColor,1,true);        
            drawRoundRect
            (
                0, 0, unscaledWidth, unscaledHeight, 
                {tl: cornerRadius, tr: cornerRadius, bl: cornerRadius, br: cornerRadius},
                backgroundColor, backgroundAlpha
            );
            graphics.lineStyle(0,0xCCCCCC,0);            
            
            
            // Shadow

            if (!dropShadow)
                dropShadow = new RectangularDropShadow();
            
            dropShadow.distance = 2;
            dropShadow.angle = 45;
            dropShadow.color = 0xCCCCCC;
            dropShadow.alpha = 0.65;
            dropShadow.tlRadius = cornerRadius;
            dropShadow.trRadius = cornerRadius;
            dropShadow.blRadius = cornerRadius;
            dropShadow.brRadius = cornerRadius;
            
            dropShadow.drawShadow(graphics, 0, 0, unscaledWidth, unscaledHeight);
           
        }
        /*
        override public function layoutBackgroundImage():void{
             super.layoutBackgroundImage();
             if(!hasBackgroundImage) return;
             
             var style:Object = getStyle("backgroundPosition");
             // the default alignment is center center
             if(!(style is Array) || (style[0]=='center' && style[1]=='center')) return;
             var posHorizontal:String = style[0];
             var posVertical:String = style[1];
             
             var p:DisplayObject = parent;
             var bm:EdgeMetrics = p is IContainer ?
                             IContainer(p).viewMetrics :
                             borderMetrics;
            var childrenList:IChildList = parent is IRawChildrenContainer ?
                                         IRawChildrenContainer(parent).rawChildren :
                                         IChildList(parent);             
             
             var backgroundImage:DisplayObject = childrenList.getChildAt(1);

            // default position is center center, or middle,middle
            var bgX:int = backgroundImage.x;
            var bgY:int = backgroundImage.y;
            
            if(posHorizontal == 'left') bgX = bm.left;
            if(posHorizontal == 'right') bgX = p.width-bm.right-backgroundImage.width;
            
            if(posVertical == 'top') bgY = bm.top;
            if(posVertical == 'bottom') bgY = p.height-bm.bottom-backgroundImage.height;
             
             backgroundImage.x = bgX;
            backgroundImage.y = bgY;

            const backgroundMask:Shape = Shape(backgroundImage.mask);
            backgroundMask.x = bgX;
            backgroundMask.y = bgY;
         }
         */

    }
}