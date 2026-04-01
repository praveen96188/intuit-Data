/**
 * User: dweinberg
 * Date: 9/14/12
 * Time: 10:05 AM
 */
package psp.sap.view.controls {
    import mx.containers.TabNavigator;

    public class TabNavigatorEx extends TabNavigator {
        override protected function commitSelectedIndex(newIndex:int):void
        {
            super.commitSelectedIndex(newIndex);
            tabBar.selectedIndex = newIndex;
        }
    }
}
