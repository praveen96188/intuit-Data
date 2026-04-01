package psp.sap.view {
    import mx.containers.Canvas;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;

    import mx.events.FlexEvent;

    import psp.sap.application.SAP;
    import psp.sap.viewmodel.AbstractExplorer;

    public class AbstractExplorerView extends Canvas {
        private var mViewModel:AbstractExplorer;

        [Bindable]
        public var explorerName:String;

        public function AbstractExplorerView():void {
            addEventListener(FlexEvent.CREATION_COMPLETE, onCreationComplete, false, 0, true);
        }                

        // the view model property is split into a getter and setter
        // to allow sub classes to override them
        [Bindable]
        public function set viewModel(value:AbstractExplorer):void {
            mViewModel = value;
        }

        public function get viewModel():AbstractExplorer {
            return mViewModel;
        }

        public function onCreationComplete(e:FlexEvent):void {
            if (SAP.instance.explorers != null) {
                SAP.instance.explorers.removeEventListener(CollectionEvent.COLLECTION_CHANGE, onExplorersChanged);
                SAP.instance.explorers.addEventListener(CollectionEvent.COLLECTION_CHANGE, onExplorersChanged, false, 0, true);
            }

            viewModel = SAP.instance.explorers.getExplorer(explorerName) as AbstractExplorer;
        }

        public function onExplorersChanged(e:CollectionEvent):void {
            if (e.kind == CollectionEventKind.ADD && SAP.instance.explorers.hasExplorer(explorerName)) {
                viewModel = SAP.instance.explorers.getExplorer(explorerName) as AbstractExplorer;
            }
        }        
    }
}