package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.lowagie.text.Font;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 24, 2011
 * Time: 3:33:11 PM
 */
public class PrintConstants {    
    // formats
    public static final String MONEY_FORMAT = "###,###,###.00";
    public static final String ZERO_MONEY_FORMAT = "###,###,##0.00";
    public static final String RATE_FORMAT = "###,###,###.####";
    public static final String CHECK_NUMBER_FORMAT = "00000000";

    // lengths
    public static final int MAX_PAYEE_LENGTH = 36;
    
    // fonts
    public static final String MICR = "MICR";
    public static final String IDAutomationMICR = "IDAutomationMICR";

    // helvetica - normal
    public static final Font FONT_HELVETICA_NORMAL_8PT = new Font(Font.HELVETICA, 8, Font.NORMAL);
    public static final Font FONT_HELVETICA_NORMAL_7PT = new Font(Font.HELVETICA, 7, Font.NORMAL);
    public static final Font FONT_HELVETICA_NORMAL_12PT = new Font(Font.HELVETICA, 12, Font.NORMAL);
    public static final Font FONT_HELVETICA_NORMAL_16PT = new Font(Font.HELVETICA, 16, Font.NORMAL);
    public static final Font FONT_HELVETICA_NORMAL_38PT = new Font(Font.HELVETICA, 38, Font.NORMAL);
    public static final Font FONT_HELVETICA_NORMAL_18PT = new Font(Font.HELVETICA, 18, Font.NORMAL);

    // helvetica - bold
    public static final Font FONT_HELVETICA_BOLD_16PT = new Font(Font.HELVETICA, 16, Font.BOLD);

    // courier - normal
    public static final Font FONT_COURIER_NORMAL_4PT = new Font(Font.COURIER, 4, Font.NORMAL);
    public static final Font FONT_COURIER_NORMAL_6PT = new Font(Font.COURIER, 6, Font.NORMAL);
    public static final Font FONT_COURIER_NORMAL_8PT = new Font(Font.COURIER, 8, Font.NORMAL);
    public static final Font FONT_COURIER_NORMAL_12PT = new Font(Font.COURIER, 12, Font.NORMAL);
    public static final Font FONT_COURIER_NORMAL_10PT = new Font(Font.COURIER, 10, Font.NORMAL);

    // courier - bold
    public static final Font FONT_COURIER_BOLD_6PT = new Font(Font.COURIER, 6, Font.BOLD);
    public static final Font FONT_COURIER_BOLD_8PT = new Font(Font.COURIER, 8, Font.BOLD);
    public static final Font FONT_COURIER_BOLD_10PT = new Font(Font.COURIER, 10, Font.BOLD);
    public static final Font FONT_COURIER_BOLD_12PT = new Font(Font.COURIER, 12, Font.BOLD);
    public static final Font FONT_COURIER_BOLD_14PT = new Font(Font.COURIER, 14, Font.BOLD);
}
