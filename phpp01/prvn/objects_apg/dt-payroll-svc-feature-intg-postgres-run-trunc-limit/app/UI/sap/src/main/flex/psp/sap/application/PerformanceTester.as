package psp.sap.application {
    import flash.events.Event;
    import flash.events.MouseEvent;
    import flash.events.TimerEvent;
    import flash.net.getClassByAlias;
    import flash.utils.Timer;

    import mx.controls.Button;
    import mx.core.Application;

    import mx.events.ChildExistenceChangedEvent;
    import mx.events.FlexEvent;

    import psp.sap.application.enums.ExplorerEnum;
    import psp.sap.model.Company;
    import psp.sap.view.DDCompanyExplorerView;
    import psp.sap.view.DDCompanyInspectorView;
    import psp.sap.view.MRUPopUp;
    import psp.sap.view.controls.CompanySuperTab;

    public class PerformanceTester {

        private static var mInstance:PerformanceTester;
        public static function get instance():PerformanceTester {
            if (mInstance == null) {
                mInstance = new PerformanceTester();
            }
            return mInstance;
        }

        /**
         * This function sets up the application for performance testing.  Call it somewhere in the beginning of SAPApp to turn it on.
         * This style prevents this test harness from needing to compile anything in.
         */
        public static function enable():void {
            var explorerView:DDCompanyExplorerView = DDCompanyExplorerView((Application.application as SAPApp).workspace.getChildByLabel(ExplorerEnum.COMPANY));
            explorerView.companyViewStack.addEventListener(ChildExistenceChangedEvent.CHILD_ADD, function(e:ChildExistenceChangedEvent):void {
                if (e.relatedObject is DDCompanyInspectorView) {
                    (e.relatedObject as DDCompanyInspectorView).addEventListener(FlexEvent.UPDATE_COMPLETE, function(e:Event):void {
                        PerformanceTester.instance.onInspectorUpdateComplete();
                    });
                }
            });

            var loadButton:Button = new Button();
            loadButton.addEventListener(MouseEvent.CLICK, function(e:MouseEvent):void {
                PerformanceTester.instance.loadMRUCompany();
            });
            loadButton.label = "Load";

            var closeButton:Button = new Button();
            closeButton.addEventListener(MouseEvent.CLICK, function(e:MouseEvent):void {
                PerformanceTester.instance.closeCompany();
            });
            closeButton.label = "Close";

            var startButton:Button = new Button();
            startButton.addEventListener(MouseEvent.CLICK, function(e:MouseEvent):void {
                PerformanceTester.instance.startTest();
            });
            startButton.label = "Start";

            var endButton:Button = new Button();
            endButton.addEventListener(MouseEvent.CLICK, function(e:MouseEvent):void {
                PerformanceTester.instance.endTest();
            });
            endButton.label = "End";

            (Application.application as SAPApp).titleBox.addChildAt(loadButton, 1);
            (Application.application as SAPApp).titleBox.addChildAt(closeButton, 2);
            (Application.application as SAPApp).titleBox.addChildAt(startButton, 3);
            (Application.application as SAPApp).titleBox.addChildAt(endButton, 4);
        }

        public function PerformanceTester() {
        }

        private var t:Timer;
        private var t2:Timer;

        private var startTime:Number;
        private var updates:int = 0;

        private var times:String="";

        public function loadMRUCompany():void {
            startTime = new Date().getTime();
            updates = 0;
            var company:Company = (((Application.application as SAPApp).itemMRU.popUp as MRUPopUp).closedList[0] as Company);
            company.display();
        }

        //inspector must call this
        public function onInspectorUpdateComplete():void {
            updates++;
            if (updates >= 2) {
                var time:Number = new Date().getTime() - startTime;
                trace(time);
                times = times + "\n" + time;
            }
        }

        public function closeCompany():void {
            ((Application.application as SAPApp).companyTabBar.getChildAt(0) as CompanySuperTab).dispatchEvent(new Event("closeTab"));
        }



        public function startTest():void {
            t = new Timer(10000, 0);
            t.addEventListener(TimerEvent.TIMER, function(e:Event):void {
                closeCompany();
            });

            t2 = new Timer(10000, 0);
            t2.addEventListener(TimerEvent.TIMER, function(e:Event):void {
                loadMRUCompany();
            });

            var t3:Timer = new Timer(1000, 1);
            t3.addEventListener(TimerEvent.TIMER, function(e:Event):void {
                t2.start();
            });
            t.start();
            t3.start();


        }

        public function endTest():void {
            t.stop();
            t2.stop();
            trace("Results:\n" + times);
        }






    }

}