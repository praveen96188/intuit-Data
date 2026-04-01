package com.intuit.spc.foundations.primarySpecific.config.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author yzhang [Created on Jul 12, 2005]
 * @version $Id: //psp/dev/Common/ConfigurationManager/src/com/intuit/spc/foundations/primarySpecific/config/xml/SpcfConfigXmlUtils.java#1 $
 */
class SpcfConfigXmlUtils
{
	/**
	 * To remove all the children 
	 * @param element Element
	 */
    static void removeAllChildren(Element element)
    {
        NodeList children = element.getChildNodes();
        int size = children.getLength();
        /**
         * Deleting from the first child does not work as expected because after the first
         * child is deleted, the list is updated so it contains one fewer object, and the
         * indexes of all remaining objects are decremented by one. As the loop continues,
         * some items are not deleted, and eventually an error occurs when the loop index
         * i exceeds the length of the shortened list.
         * <p>
         * Must delete from the end of the list because the list is shrinking as we are
         * deleting
         */
        for (int i = size - 1; i >= 0; i--)
        {
            Node child = children.item(i);
            element.removeChild(child);
        }
    }   
}