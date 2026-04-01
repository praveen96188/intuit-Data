package psp.sap.view
{
    import flash.events.MouseEvent;

    import mx.controls.LinkButton;

    /**
     * Extend the LinkButton to act more as a straight-hyperlink
     * by setting the selectionColor and rollOverColor styles to
     * the background of the DataGrid.
     *
     * The LinkButton broadcasts its click event through the
     * owning DataGrid via a "companySelected" event.
     */
    public class ActionLink extends LinkButton
    {
        private var mEventAction:String = "";
        private var mArgument:Object = null;

        public function ActionLink()
        {
            super();
            this.useHandCursor = true;
            this.setStyle("color", "blue");
            this.setStyle("verticalAlign", "middle");

            this.addEventListener(MouseEvent.CLICK, onClickMethod, false, 0, true);
            this.addEventListener(MouseEvent.ROLL_OVER, onRollover, false, 0, true);
            this.addEventListener(MouseEvent.ROLL_OUT, onRollout, false, 0, true);
        }

        public function get action():String {
            return mEventAction;
        }

        public function set action(value:String):void {
            mEventAction = value;
        }

        public function set actionClick(value:Function):void
        {
            this.addEventListener(ActionLinkEvent.ACTION_FIRE, value, false, 0, true);
        }

        override protected function rollOverHandler(e:MouseEvent):void {
            // disable the roll over painting
        }

        override protected function mouseDownHandler(e:MouseEvent):void {
            // disable click down painting
        }

        override protected function mouseUpHandler(e:MouseEvent):void {
            // disable click up painting
        }

        protected function onClickMethod(e:MouseEvent):void {
            var actionEvent:ActionLinkEvent = new ActionLinkEvent(mEventAction, this.data);
            this.dispatchEvent(actionEvent);
        }

        protected function onRollover(e:MouseEvent):void {
            if(enabled){
                this.setStyle("textDecoration", "underline");
            }
        }

        protected function onRollout(e:MouseEvent):void {
            if(enabled){
                this.setStyle("textDecoration", "none");
            }
        }

        override public function set data(value:Object):void {
            super.data = value;
        }

        override public function set enabled(value:Boolean):void {
            super.enabled = value;
            this.useHandCursor = value;
            if(!value) {
                this.setStyle("textDecoration", "none");
            }
        }

    }
}