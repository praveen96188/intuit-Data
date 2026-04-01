package psp.sap.model {
    import flash.events.Event;

    public class ActionClickEvent extends Event {

        public const ACTION_CLICKED_EVENT:String = "actionClickedEvent";

        public var actionEvent:ActionEvent;
        public var itemTarget:Object;

        public function ActionClickEvent(actionEvent:ActionEvent, itemTarget:Object=null) {
            super(ACTION_CLICKED_EVENT, false, false);
            this.actionEvent = actionEvent;
            this.itemTarget = itemTarget;
        }
    }
}