package psp.taxcredits.view.controls {
    import flash.events.Event;

    import mx.controls.Text;
    import mx.events.FlexEvent;

    public class ErrorText extends Text {
        public function ErrorText() {
            super();
            this.visible = false;
            this.includeInLayout = false;
            this.addEventListener(FlexEvent.VALID, function(e:Event):void {visible=false; includeInLayout=false; errorString="";});
            this.addEventListener(FlexEvent.INVALID, function(e:Event):void {visible=true; includeInLayout=true;});
        }


    }
}