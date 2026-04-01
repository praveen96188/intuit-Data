package psp.sap.view
{
    import flash.events.MouseEvent;
    import flash.geom.Point;
    import flash.system.System;

    import mx.controls.LinkButton;
    import mx.controls.Text;
    import mx.core.Application;
    import mx.core.UITextField;
    import mx.core.mx_internal;
    import mx.effects.Fade;

    import psp.sap.application.SAP;
    import psp.sap.view.controls.WindowManager;

    use namespace mx_internal;

    public class TruncatedText extends Text {
        
        protected static const TRUNCATION_INDICATOR : String = "...";
		private const BLANK_STRING:String = "";
        private var truncated:Boolean = false;
        private var fullText:String;

        public function TruncatedText() {
            super();
            
            truncateToFit = true;

            var fade:Fade = new Fade();
            fade.alphaFrom = 1;
            fade.alphaTo = 0;
            fade.startDelay = 100;
            fade.duration = 750;

            var copyButton:LinkButton = new LinkButton();
            copyButton.setStyle("paddingLeft", 1);
            copyButton.setStyle("paddingRight", 1);
            copyButton.setStyle("icon", SAP.instance.icons.copy);
            copyButton.setStyle("hideEffect", fade);
            copyButton.addEventListener(MouseEvent.CLICK, function(e:MouseEvent):void {
                System.setClipboard(fullText);
            });
            copyButton.addEventListener(MouseEvent.MOUSE_OVER, function(e:MouseEvent):void {
                fade.stop();
                copyButton.visible = true;
                copyButton.alpha = 1.;
            });
            copyButton.addEventListener(MouseEvent.MOUSE_OUT, function(e:MouseEvent):void {
                copyButton.visible = false;
            });

            this.addEventListener(MouseEvent.MOUSE_OVER, function(e:MouseEvent):void {
                if (truncated) {
                    fade.stop();
                    copyButton.toolTip = "Copy '" + fullText + "' to clipboard";
                    copyButton.visible = true;
                    copyButton.alpha = 1.;
                    WindowManager.add(copyButton, SAPApp(Application.application), false);
                    var target:UITextField = UITextField(e.target);
                    var localPoint:Point = new Point(target.x, target.y);
                    var globalPoint:Point = target.localToGlobal(localPoint);
                    WindowManager.position(copyButton, globalPoint.x + target.width, globalPoint.y);
                }
            });

            this.addEventListener(MouseEvent.MOUSE_OUT, function(e:MouseEvent):void {
                copyButton.visible = false;
            });
        }

        override public function set text(text : String) : void {
            _isHTML = false;

            super.text = text;
        }
        
        override public function set htmlText(htmlText : String) : void {
            _isHTML = true;

             super.htmlText = htmlText;
        }
        
        protected function get truncationRequired() : Boolean {
            return (textField.height < textField.textHeight + UITextField.TEXT_HEIGHT_PADDING);
        }
        
        override protected function updateDisplayList(w : Number, h : Number) : void {
            super.updateDisplayList(w, h);
            
            textField.toolTip = BLANK_STRING;
            if (truncateToFit) {
                if (!_isHTML) {
                    var originalText : String = textField.text = super.text;
                    if (truncationRequired) {
                        truncated = true;
                        fullText = super.text;
                    	textField.toolTip = super.text;
                        var l : int = 0;
                        var r : int = textField.text.length;
                        while (r - l > 1) {
                            var median : Number = Math.floor((l + r) / 2);
                            textField.text = originalText.substr(0, median) + TRUNCATION_INDICATOR;
                            if (truncationRequired) {
                                r = median;
                            } else {
                                l = median;
                            }
                        }
                        while (truncationRequired && median > 0) {
                            median--;
                            textField.text = originalText.substr(0, median) + TRUNCATION_INDICATOR;
                        }
                    }
                }
            }
            
        }
        
        private var _isHTML : Boolean;
        
    }

}
