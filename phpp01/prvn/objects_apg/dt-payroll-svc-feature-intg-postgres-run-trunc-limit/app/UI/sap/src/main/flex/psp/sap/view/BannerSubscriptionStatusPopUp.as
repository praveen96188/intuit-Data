package psp.sap.view {
    import mx.containers.VBox;

    public class BannerSubscriptionStatusPopUp extends VBox{
        public function BannerSubscriptionStatusPopUp() {
            super();
        }

        override public function move(x:Number, y:Number):void {
            super.move(x - width + 15, y);
        }
    }
}