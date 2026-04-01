package psp.sap.view.controls {
    import flexlib.controls.SuperTabBar;

    import mx.collections.IList;
    import mx.events.FlexEvent;
    
    public class SuperTabBarExt extends SuperTabBar {

        private var waitingDP:int = -1;

        public function SuperTabBarExt() {
        }

        override public function get selectedIndex():int {            
            //bindings won't fire when they should, so this will prevent later problems
            if (super.selectedIndex >= numChildren) {
                return 0;
            } else {
                return super.selectedIndex;
            }
        }

        override protected function hiliteSelectedNavItem(index:int):void {
            if (index < numChildren) {
                super.hiliteSelectedNavItem(index);
            }            
        }

        override public function set selectedIndex(value:int):void {
            if ((!dataProvider || (dataProvider is IList && (dataProvider as IList).length == 0)) && value) {
                waitingDP = value;
            } else {
                super.selectedIndex = value;
            }
        }

        override public function set dataProvider(value:Object):void
        {
            if (dataProvider == value && dataProvider.length == numChildren) {
                return;
            }

            var tempIndex:int = selectedIndex;

            super.dataProvider = value;

            if (waitingDP != -1) {
                selectedIndex = waitingDP;
                waitingDP = -1;
            } else {
                if (tempIndex != -1) {
                    selectedIndex = tempIndex;
                }
            }
                        
        }
    }
}