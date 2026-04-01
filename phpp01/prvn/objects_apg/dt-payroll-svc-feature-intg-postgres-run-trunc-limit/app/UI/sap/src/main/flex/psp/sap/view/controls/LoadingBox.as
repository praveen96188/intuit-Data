package psp.sap.view.controls {
    import flash.display.DisplayObject;
    import flash.events.Event;

    import mx.binding.utils.BindingUtils;
    import mx.containers.Canvas;
    import mx.controls.Image;

    import mx.core.Application;
    import mx.events.FlexEvent;

    import psp.sap.view.SAPIcons;
    import psp.sap.viewmodel.AbstractPartViewModel;

    public class LoadingBox extends Canvas {

        private var spinner:Image = new Image();        

        public function LoadingBox() {
            super();

            spinner.source = SAPIcons.spinner;

            this.addEventListener(FlexEvent.INITIALIZE, function(e:Event):void {
                addChild(spinner);
            }, false, 0, true);

            this.addEventListener(Event.RESIZE, onResize, false, 0, true);
        }

        public function onResize(e:Event):void {
            spinner.x = (width - spinner.width) / 2;
            spinner.y = (height - spinner.height) / 2;
        }

        public function set viewModel(value:AbstractPartViewModel):void {
            BindingUtils.bindProperty(this, "isDataLoading",value, "isDataLoading");
        }

        public function set isDataLoading(value:Boolean):void {
            if (value) {                
                spinner.visible = true;
                for each (var child:DisplayObject in getChildren()) {
                    if (child != spinner) {
                        child.alpha = 0.3;
                    }
                }
            } else {
                spinner.visible = false;
                for each (var child2:DisplayObject in getChildren()) {
                    if (child != spinner) {
                        child2.alpha = 1;
                    }
                }

            }

        }

    }
}