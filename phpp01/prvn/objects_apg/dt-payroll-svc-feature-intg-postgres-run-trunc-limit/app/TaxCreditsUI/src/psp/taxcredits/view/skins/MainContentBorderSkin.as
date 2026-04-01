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

    public class MainContentBorderSkin extends HaloBorder
    {

        private var dropShadow:RectangularDropShadow;

        override protected function updateDisplayList 
        (unscaledWidth:Number, unscaledHeight:Number):void 
        {

            super.updateDisplayList(unscaledWidth, unscaledHeight);
            var cornerRadius:Number = getStyle("cornerRadius");
            var backgroundColor:int = getStyle("backgroundColor");
            var backgroundAlpha:Number = getStyle("backgroundAlpha");
            var style:Object = getStyle("backgroundPosition");
            graphics.clear();
            
            // Background
			graphics.lineStyle(1,0xB7B7B7,1,true);
            drawRoundRect
            (
                0, 0, unscaledWidth, unscaledHeight, 
                {tl: 0, tr: 0, bl: cornerRadius, br: cornerRadius}, 
                backgroundColor, backgroundAlpha
            );
            graphics.moveTo(0,0);
            graphics.lineStyle(1,0xE8E8E8,1,true);
            graphics.lineTo(unscaledWidth,0);
            graphics.lineStyle(0,0xB7B7B7,0,true);            
            
            /*
            // Shadow

            if (!dropShadow)
                dropShadow = new RectangularDropShadow();
            
            dropShadow.distance = 8;
            dropShadow.angle = 45;
            dropShadow.color = 0;
            dropShadow.alpha = 0.4;
            dropShadow.tlRadius = 0;
            dropShadow.trRadius = cornerRadius;
            dropShadow.blRadius = cornerRadius;
            dropShadow.brRadius = 0;
            
            dropShadow.drawShadow(graphics, 0, 0, unscaledWidth, unscaledHeight);
            */
        }
    }
}