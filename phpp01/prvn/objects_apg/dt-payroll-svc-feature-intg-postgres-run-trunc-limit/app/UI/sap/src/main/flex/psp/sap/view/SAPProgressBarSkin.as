package psp.sap.view
{
	import mx.graphics.RectangularDropShadow;
	import mx.skins.halo.ProgressIndeterminateSkin;
    import flash.display.Graphics;
    import mx.skins.RectangularBorder;
    import mx.utils.ColorUtil;
    import mx.styles.StyleManager;
    
	public class SAPProgressBarSkin extends ProgressIndeterminateSkin
	{
		public function SAPProgressBarSkin()
		{
			super();
		}
		
		private var countColor:int = 0;
		private var colorDirection:int = 0;
		
		private var barAlpha:Number = 0;
		private var alphaDirection:int = 0;
		 private var dropShadow:RectangularDropShadow;
		 
		private function changeColor():void {
			if(colorDirection == 0) {
				countColor+=5;
				if(countColor > 160) colorDirection = 1;
			} else {
				countColor--;
				if(countColor < 6) colorDirection = 0;
			}
		}
		
		private function changeAlpha():void {
			if(alphaDirection == 0) {
				barAlpha+=0.1;
				if(barAlpha > 0.9) alphaDirection = 1;
			} else {
				barAlpha-=0.1;
				if(barAlpha < 0.1) alphaDirection = 0;
			}
		}
		
		
		override protected function updateDisplayList(w:Number, h:Number):void
		{
			super.updateDisplayList(w, h);
	
			// User-defined styles
			var barColorStyle:* = getStyle("barColor");
			var barColor:uint = StyleManager.isValidStyleValue(barColorStyle) ?
								barColorStyle :
								getStyle("themeColor");
				
			//var barColor0:Number = ColorUtil.adjustBrightness2(barColor, 60);
			var hatchInterval:Number = getStyle("indeterminateMoveInterval");
			
			if (isNaN(hatchInterval))
				hatchInterval = 28;
	
			var g:Graphics = graphics;
			
			g.clear();
			
			// Hatches
			for (var i:int = 0; i < w; i += hatchInterval)
			{
			    changeColor();
				changeAlpha();
				var barColor0:Number = ColorUtil.adjustBrightness2(barColor, countColor);
				g.beginFill(barColor0, barAlpha);
				g.moveTo(i, 1);
				g.lineTo(Math.min(i + 14, w), 1);
				g.lineTo(Math.min(i + 10, w), h - 1);
				g.lineTo(Math.max(i - 4, 0), h - 1);
				g.lineTo(i, 1);
				g.endFill();
			}
		}
		
	}
}