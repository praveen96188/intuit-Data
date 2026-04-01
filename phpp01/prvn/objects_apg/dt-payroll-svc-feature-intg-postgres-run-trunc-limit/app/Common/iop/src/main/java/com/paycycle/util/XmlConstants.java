/**
 * XmlConstants.java
 *
 * Copyright (c) 1999-2000 PayCycle, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * PayCycle, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with PayCycle.
 *
 * PAYCYCLE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. PAYCYCLE SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
 */

package com.paycycle.util;

/**
 * Commonly used identifiers and XML element tags.
 *
 * @author Kin-Hong Wong
 */
public interface XmlConstants {
    // Convention for XML element tags are initial caps -------------------------
    static final String XML_COMPONENT = "Component";
    static final String XML_DESCRIPTION = "Description";
    static final String XML_ENTRY = "Entry";
    static final String XML_FIELD = "Field";
    static final String XML_PROPERTY = "Property";
    static final String XML_RECORD = "Record";
    static final String XML_RESOURCE = "Resource";
    static final String XML_SECTION = "Section";

    // Contention for XML attributes are all lower cases ------------------------
    static final String XML_CLASS = "class";
    static final String XML_CONSTANT = "constant";
    static final String XML_DATATYPE = "datatype";
    static final String XML_LENGTH = "length";
    static final String XML_HANDLER = "handler";
    static final String XML_ID = "id";
    static final String XML_IDREF = "idref";
    static final String XML_INCLUSION = "inclusion";
    static final String XML_KEY = "key";
    static final String XML_NAME = "name";
    static final String XML_STARTPOS = "startpos";
    static final String XML_VALUE = "value";
    static final String XML_VERSION = "version";

    // Others
    static final String XML_PROLOG = "<xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
}
