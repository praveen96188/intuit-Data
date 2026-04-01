package psp.sap.view.controls {
    import flash.events.KeyboardEvent;
    import flash.ui.Keyboard;

    import mx.collections.ArrayCollection;
    import mx.controls.ComboBox;
    import mx.core.UITextField;

    /**
     * Type-ahead Combobox allows the user to match ComboBox options by typing in the first few characters.
     *
     * @author Oliver Merk - (oliverm@olivermerk.com)
     * @source http://blog.olivermerk.ca/index.cfm/2008/8/23/ComboBox-with-TypeAhead
     * @license Feel free to use this in your own projects, make any changes you want, and tell your boss that you created it;) Enjoy..
     */
    public class ComboBoxTypeAhead extends ComboBox {

        /**
         * By default, the match is from the beginning of the string only.
         * Set searchFromBeginning to false to search the entire string for a match.
         */
        public var searchFromBeginning:Boolean = true;
        private var searchRegEx:RegExp;

        public function ComboBoxTypeAhead( ) {
            super( );

            this.editable = true;
            this.text = "";

            // Listen for key-up events to engage the filter
            this.addEventListener( KeyboardEvent.KEY_UP, onKeyUp );
        }

        /** Each time the user presses a key, filter the ComboBox items to match. */
        private function onKeyUp( event:KeyboardEvent ):void {
            // Number or letter entered
            if ( isAlphaChar( event.keyCode ) ) {

                var textBox:UITextField = UITextField ( event.target );
                var searchText:String = event.target.text;

                // Set up the search expression
                searchRegEx = new RegExp( textBox.text, 'i' );

                // Filter the ArrayCollection
                ArrayCollection( this.dataProvider ).filterFunction = filter;
                ArrayCollection( this.dataProvider ).refresh( );

                // Drop open the ComboBox
                this.open( );

                // Set the ComboBox's search text
                textBox.text = searchText;

                // Select the current search text
                textBox.setSelection( searchText.length, searchText.length );
            } else if ( event.keyCode == Keyboard.ESCAPE ) {
                this.text = "";
                this.setFocus( );
                this.selectedItem = null;
            } else if (event.keyCode == Keyboard.ENTER) {
                if (this.dataProvider.length > 0) {
                    this.selectedItem = this.dataProvider.getItemAt(0);
                }
            }
        }

        /** The ArrayCollection filter function. Each item gets passed into this. */
        private function filter( item:Object ):Boolean {

            var found:Boolean = false;

            // Determine if the search string is contained in the label of each ComboBox item
            if ( searchFromBeginning ) {
                if ( item is String ) {
                    found = ( item.search( searchRegEx ) == 0 );
                } else {
                    found = ( String( item[ this[ "labelField" ] ] ).search( searchRegEx ) == 0 );
                }
            } else {
                if ( item is String ) {
                    found = ( item.search( searchRegEx ) >= 0 );
                } else {
                    found = ( String( item[ this[ "labelField" ] ] ).search( searchRegEx ) >= 0 );
                }
            }

            return found;
        }

        /** Filter out any non-alphanumeric key strokes */
        private function isAlphaChar( keyCode:int ):Boolean {

            var isAlpha:Boolean = false;

            if (
                ( keyCode > 64 && keyCode < 91 )
                ||
                ( keyCode > 96 && keyCode < 123 )
                ||
                ( keyCode == Keyboard.BACKSPACE )
                ||
                ( keyCode == Keyboard.DELETE)
                ) {
                isAlpha = true;
            }

            return isAlpha;
        }
    }

}