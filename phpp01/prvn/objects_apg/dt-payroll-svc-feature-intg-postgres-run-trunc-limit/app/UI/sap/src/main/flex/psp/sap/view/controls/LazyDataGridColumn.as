package psp.sap.view.controls {
    import mx.controls.dataGridClasses.DataGridColumn;
    import mx.core.ClassFactory;

    public class LazyDataGridColumn extends DataGridColumn {

        [Bindable] public var textFunction:Function;
        [Bindable] public var colorFunction:Function;
        [Bindable] public var loadFunction:Function;

        public function LazyDataGridColumn() {
            super();
            this.itemRenderer = new ClassFactory(LazyItemRenderer);
        }
    }
}