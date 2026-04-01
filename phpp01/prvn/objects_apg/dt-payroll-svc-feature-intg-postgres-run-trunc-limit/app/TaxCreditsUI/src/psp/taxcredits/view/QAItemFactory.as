package psp.taxcredits.view {
    import psp.taxcredits.model.QAItemCollection;

    public class QAItemFactory {

        [Embed(source="assets/learnMoreQAItems.xml", mimeType="application/octet-stream")]
        private static const qaItemsEmbedded:Class;
        [Bindable] public static var qaItems:QAItemCollection = QAItemCollection.fromXML(XML(new qaItemsEmbedded()));

        [Embed(source="assets/finalStepsQAItems.xml", mimeType="application/octet-stream")]
        private static const finalStepsEmbedded:Class;
        [Bindable] public static var finalStepsQAItems:QAItemCollection = QAItemCollection.fromXML(XML(new finalStepsEmbedded()));

        

    }
}