package psp.taxcredits.view.skins
{
    import flash.geom.Matrix;

    public class GradientBackground extends SpriteProgrammaticSkin {
    	
    	private var uswidth: Number;
    	private var usheight: Number;
    	
    	override public function get measuredWidth():Number
        {
            return 40;
        }
        
        override public function get measuredHeight():Number
        {
            return 40;
        }
    	
        override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void
        {
        	unscaledWidth = getStyle("bgWidth");
        	unscaledHeight = getStyle("bgHeight");
        	
        	//trace(unscaledHeight + " x " + unscaledWidth);
        	
        	if(isNaN(unscaledWidth)) unscaledWidth = 40;
        	if(isNaN(unscaledHeight)) unscaledHeight = 40;
        	
        	uswidth = unscaledWidth;
        	usheight = unscaledHeight;
        	
            var fillColors:Array = getStyle("fillColors");
            var fillAlphas:Array = getStyle("fillAlphas");
            var cornerRadius:int = getStyle("cornerRadius");
            var gradientType:String = getStyle("gradientType");
            var angle:Number = getStyle("angle");
            var focalPointRatio:Number = getStyle("focalPointRatio");
            
            var showBorder:Boolean = getStyle("showBorder");
            
            var getCustomCorners:Boolean = getStyle("roundCornersOn");
            //trace("CORNERS: " + getCustomCorners);
            // Default values, if styles aren't defined
            if (fillColors == null)
                fillColors = [0xEEEEEE, 0x999999];
            
            if (fillAlphas == null)
                fillAlphas = [1, 1];
            
            if (gradientType == "" || gradientType == null)
                gradientType = "linear";
            
            if (isNaN(angle))
                angle = 90;
            
            if (isNaN(focalPointRatio))
                focalPointRatio = 0.5;
            
            var matrix:Matrix = new Matrix();
            matrix.createGradientBox(unscaledWidth, unscaledHeight, angle * Math.PI / 180);
            var tr:int,tl:int,br:int,bl:int;
            tr = tl = br = bl = cornerRadius;
            /*if(getCustomCorners){
            	trace("GOT CORNERS");
            	tr = getStyle("roundCornersTR");
            	tl = getStyle("roundCornersTL");
            	br = getStyle("roundCornersBR");
            	bl = getStyle("roundCornersBL");
            }
            */
            //trace("Corners: [" + tl + ","+ tr + ","+ br + ","+ bl + "]");
            graphics.beginGradientFill(gradientType, fillColors, fillAlphas, [0, 255] , matrix, "pad", "rgb", focalPointRatio);
            //graphics.drawRect(0,0,unscaledWidth, unscaledHeight);
            var rs:Number = 0.2;
            graphics.drawRoundRectComplex(0, 0, unscaledWidth, unscaledHeight,tl*rs,tr*rs,bl*rs,br*rs);
            graphics.endFill();
            if(showBorder){
	            makeBorder(unscaledWidth,unscaledHeight);	                       
	        }
        }
        
        protected function makeBorder(unscaledWidth:Number, unscaledHeight:Number):void {
        	var tt:Number = getStyle("borderTopThickness");
        	var rt:Number = getStyle("borderRightThickness");
        	var bt:Number = getStyle("borderBottomThickness");
        	var lt:Number = getStyle("borderLeftThickness");
        	var c:Number = getStyle("borderColor");
        	if(tt > 0) makeBorderTop(tt,c);
        	if(rt > 0) makeBorderRight(rt,c);
        	if(bt > 0) makeBorderBottom(bt,c);
        	if(lt > 0) makeBorderLeft(lt,c);	
        }
        
        protected function makeBorderTop(thickness:Number, color:uint):void {
        	graphics.moveTo(0,0);
            graphics.lineStyle(thickness/10,color);
            graphics.lineTo(uswidth, 0);        	
        }
        
        protected function makeBorderRight(thickness:Number, color:uint):void {
        	graphics.moveTo(uswidth,0);
            graphics.lineStyle(thickness/10,color);
            graphics.lineTo(uswidth, usheight);        	
        }
        
        protected function makeBorderBottom(thickness:Number, color:uint):void {
        	graphics.moveTo(0,usheight);
            graphics.lineStyle(thickness/10,color);
            graphics.lineTo(uswidth, usheight);        	
        }
        
        protected function makeBorderLeft(thickness:Number, color:uint):void {
        	graphics.moveTo(0,0);
            graphics.lineStyle(thickness/10,color);
            graphics.lineTo(0, usheight);        	
        }
        
        
    }
}