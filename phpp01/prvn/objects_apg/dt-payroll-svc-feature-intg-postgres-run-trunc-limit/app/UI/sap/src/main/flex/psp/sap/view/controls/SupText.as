package psp.sap.view.controls
{
    import flash.events.Event;
    import flash.text.StyleSheet;
    import flash.text.TextFormat;

    import mx.controls.Text;
    import mx.core.mx_internal;

    public class SupText extends Text
    {

        use namespace mx_internal;

        public var linkColor:String = "#355EBF";

        private var mUnderline:Boolean=true;

        private var format:TextFormat;

        public function SupText()
        {
            super();
            resourceManager.addEventListener(Event.CHANGE, function(e:Event):void {
                callLater(function():void {
                    var oldText:String = htmlText;
                    setStyleSheet();
                    htmlText = "";
                    htmlText = null;
                    setStyleSheet();
                    htmlText = oldText;
                });
            }, false, 0, true);
        }

        override protected function createChildren():void {
            super.createChildren();
            setStyleSheet();
        }

        override public function set htmlText(value:String):void {
            if (textField != null && format != null) {
                //glorious hack for style problem
                textField.styleSheet = null;
                textField.defaultTextFormat = format;
                setStyleSheet();
            }

            super.htmlText = value;

            if (textField != null && textField.defaultTextFormat.font != "Times New Roman") {
                format = textField.defaultTextFormat;
            }
        }

        public function set bbText(value:String):void {
            htmlText = value.replace(/\[/g,"<").replace(/\]/g, ">");
        }

        public function setStyleSheet():void {
            var ss:StyleSheet = textField.styleSheet;

            if(textField.styleSheet == null){
                textField.styleSheet = new StyleSheet();
            }
            textField.styleSheet.setStyle("sup", { display: "inline", fontFamily: "ArialSup", fontWeight:"normal"});
            textField.styleSheet.setStyle("a:link", { textDecoration: "none", color: linkColor });            
            if (underline) {
                textField.styleSheet.setStyle("a:hover", { textDecoration: "underline" });
                textField.styleSheet.setStyle("a:active", { textDecoration: "underline" });
            } else {
                textField.styleSheet.setStyle("a:hover", { textDecoration: "none" });
                textField.styleSheet.setStyle("a:active", { textDecoration: "none" });
            }



        }

        [Bindable]
        public function get underline():Boolean {
            return mUnderline;
        }

        public function set underline(value:Boolean):void {
            mUnderline = value;
            setStyleSheet();
        }

    }






}
