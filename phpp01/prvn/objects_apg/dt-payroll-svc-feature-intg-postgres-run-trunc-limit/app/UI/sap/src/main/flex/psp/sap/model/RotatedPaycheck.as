/**
 * User: dweinberg
 * Date: 9/6/12
 * Time: 1:00 PM
 */
package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
    public class RotatedPaycheck extends Object {
        public var isColumnHeader:Boolean;
        public var isSubTotal:Boolean;
        public var columnHeader:String;
        public var columnTooltip:String;

        [ArrayElementType("Number")]
        public var amounts:ArrayCollection = new ArrayCollection();

    }
}
