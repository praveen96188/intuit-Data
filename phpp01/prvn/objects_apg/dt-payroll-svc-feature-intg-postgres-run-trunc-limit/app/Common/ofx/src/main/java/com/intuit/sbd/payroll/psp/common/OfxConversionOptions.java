package com.intuit.sbd.payroll.psp.common;

/**
 * QuickBooks passes in <, > and & escaped.  It expects
 *    these values escaped on the response.  The flags below
 *    tell the OFXManager what actions to perform on the request
 *    and the response.
 */
public enum OfxConversionOptions {
    ESCAPE_OFX_FOR_CRIS_RULES,
    ESCAPE_OFX_FOR_QB_RULES
}
