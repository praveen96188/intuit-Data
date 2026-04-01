package psp.sap.view
{
	import flash.display.GradientType;
	
	import mx.core.IButton;
	import mx.skins.halo.ButtonSkin;
	import mx.skins.halo.HaloColors;
	import mx.styles.StyleManager;
	import mx.utils.ColorUtil;

	public class AdministrationButtonSkin extends ButtonSkin
	{
		public function AdministrationButtonSkin()
		{
			super();
		}
		
		override protected function updateDisplayList(w:Number, h:Number):void
		{
			super.updateDisplayList(w, h);
	
			// User-defined styles.
			var borderColor:uint = getStyle("borderColor");
			var cornerRadius:Number = getStyle("cornerRadius");
			var fillAlphas:Array = getStyle("fillAlphas");
			var fillColors:Array = getStyle("fillColors");
			StyleManager.getColorNames(fillColors);
			var highlightAlphas:Array = getStyle("highlightAlphas");				
			var themeColor:uint = getStyle("themeColor");
	
			// Derivative styles.
			var derStyles:Object = calcDerivedStyles(themeColor, fillColors[0],
													 fillColors[1]);
	
			var borderColorDrk1:Number =
				ColorUtil.adjustBrightness2(borderColor, -50);
			
			var themeColorDrk1:Number =
				ColorUtil.adjustBrightness2(themeColor, -25);
			
			var emph:Boolean = false;
			
			if (parent is IButton)
				emph = IButton(parent).emphasized;
				
			var cr:Number = 0;
			var cr1:Number = 0;
			var cr2:Number = 0;
			
			var tmp:Number;
			
			graphics.clear();
													
			switch (name)
			{			
				case "selectedUpSkin":									
				case "upSkin":
				case "downSkin":
				case "selectedDownSkin":
				{
	   				var upFillColors:Array = [ fillColors[0], fillColors[1] ];
	   				
					var upFillAlphas:Array = [ fillAlphas[0], fillAlphas[1] ];
	
					if (emph)
					{
						// button border/edge
						drawRoundRect(
							0, 0, w, h, cr,
							[ themeColor, themeColorDrk1 ], 1,
							verticalGradientMatrix(0, 0, w, h ),
							GradientType.LINEAR, null, 
							{ x: 2, y: 2, w: w - 4, h: h - 4, r: cornerRadius - 2 });
	                            
						// button fill
						drawRoundRect(
							2, 2, w - 4, h - 4, cr2,
							upFillColors, upFillAlphas,
							verticalGradientMatrix(2, 2, w - 2, h - 2));
											  
						// top highlight
						drawRoundRect(
							2, 2, w - 4, (h - 4) / 2,
							{ tl: cr2, tr: cr2, bl: 0, br: 0 },
							[ 0xFFFFFF, 0xFFFFFF ], highlightAlphas,
							verticalGradientMatrix(1, 1, w - 2, (h - 2) / 2)); 
					}
					else
					{
						// button border/edge
						drawRoundRect(
							0, 0, w, h, cr,
							[ borderColor, borderColorDrk1 ], 1,
							verticalGradientMatrix(0, 0, w, h ),
							GradientType.LINEAR, null, 
							{ x: 1, y: 1, w: w - 2, h: h - 2, r: cornerRadius - 1 }); 
	
						// button fill
						drawRoundRect(
							1, 1, w - 2, h - 2, cr1,
							upFillColors, upFillAlphas,
							verticalGradientMatrix(1, 1, w - 2, h - 2)); 
	
						// top highlight
						drawRoundRect(
							1, 1, w - 2, (h - 2) / 2,
							{ tl: cr1, tr: cr1, bl: 0, br: 0 },
							[ 0xFFFFFF, 0xFFFFFF ], highlightAlphas,
							verticalGradientMatrix(1, 1, w - 2, (h - 2) / 2)); 
					}
					break;
				}
							
				case "overSkin":
				case "selectedOverSkin":
				{
					var overFillColors:Array;
					if (fillColors.length > 2)
						overFillColors = [ fillColors[2], fillColors[3] ];
					else
						overFillColors = [ fillColors[0], fillColors[1] ];
	
					var overFillAlphas:Array;
					if (fillAlphas.length > 2)
						overFillAlphas = [ fillAlphas[2], fillAlphas[3] ];
	  				else
						overFillAlphas = [ fillAlphas[0], fillAlphas[1] ];
	
					// button border/edge
					drawRoundRect(
						0, 0, w, h, cr,
						[ themeColor, themeColorDrk1 ], 1,
						verticalGradientMatrix(0, 0, w, h),
						GradientType.LINEAR, null, 
						{ x: 1, y: 1, w: w - 2, h: h - 2, r: cornerRadius - 1 }); 
													
					// button fill
					drawRoundRect(
						1, 1, w - 2, h - 2, cr1,
						overFillColors, overFillAlphas,
						verticalGradientMatrix(1, 1, w - 2, h - 2)); 
											  
					// top highlight
					drawRoundRect(
						1, 1, w - 2, (h - 2) / 2,
						{ tl: cr1, tr: cr1, bl: 0, br: 0 },
						[ 0xFFFFFF, 0xFFFFFF ], highlightAlphas,
						verticalGradientMatrix(1, 1, w - 2, (h - 2) / 2)); 
					
					break;
				}																		
							
				case "disabledSkin":
				case "selectedDisabledSkin":
				{
	   				var disFillColors:Array = [ fillColors[0], fillColors[1] ];
	   				
					var disFillAlphas:Array =
						[ Math.max( 0, fillAlphas[0] - 0.15),
						  Math.max( 0, fillAlphas[1] - 0.15) ];
	
					// button border/edge
					drawRoundRect(
						0, 0, w, h, cr,
						[ borderColor, borderColorDrk1 ], 0.5,
						verticalGradientMatrix(0, 0, w, h ),
						GradientType.LINEAR, null, 
						{ x: 1, y: 1, w: w - 2, h: h - 2, r: cornerRadius - 1 });
	
					// button fill
					drawRoundRect(
						1, 1, w - 2, h - 2, cr1,
						disFillColors, disFillAlphas,
						verticalGradientMatrix(1, 1, w - 2, h - 2)); 
					
					break;
				}
			}
		}
		
		private static var cache:Object = {};
		private static function calcDerivedStyles(themeColor:uint,
											  fillColor0:uint,
											  fillColor1:uint):Object
		{
			var key:String = HaloColors.getCacheKey(themeColor,
													fillColor0, fillColor1);
					
			if (!cache[key])
			{
				var o:Object = cache[key] = {};
				
				// Cross-component styles.
				HaloColors.addHaloColors(o, themeColor, fillColor0, fillColor1);
			}
			
			return cache[key];
		}		
	}
}