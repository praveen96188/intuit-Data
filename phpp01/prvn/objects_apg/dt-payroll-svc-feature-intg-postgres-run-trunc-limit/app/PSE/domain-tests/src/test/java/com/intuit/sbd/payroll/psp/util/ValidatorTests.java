package com.intuit.sbd.payroll.psp.util;

import org.junit.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Contains the unit tests for the <CODE>Validator</CODE> class.
 *
 * @author: chetzler
 * @version: Jun 18, 2007
 */

public class ValidatorTests {
    @Before
    public void runBeforeEachTest()
    {
    }

    @After
    public void runAfterEachTest(){
    }


    @Test
    public void isMatchingPatternPatternMatches()
    {
        assertTrue(com.intuit.sbd.payroll.psp.util.Validator.isMatchingPattern("a", "[a-z]"));
    }

    @Test
    public void isMatchingPatternPatternDoesNotMatch()
    {
        assertFalse(Validator.isMatchingPattern("7754244338", "[A-Z0-9]"));
    }

    @Test
    public void isMatchingPatternPatternMatchesWithFlags()
    {
        assertTrue(Validator.isMatchingPattern("a", "[a-z]", 1));
    }

    @Test
    public void isMatchingPatternPatternDoesNotMatchWithoutFlags()
    {
        assertFalse(Validator.isMatchingPattern("7754244338", "[A-Z0-9]", 1));
    }

    @Test
    public void isValidLengthLengthValid()
    {
        assertTrue(Validator.isValidLength("", 0, 0));
    }

    @Test
    public void isValidLengthLengthInvalid()
    {
        assertFalse(Validator.isValidLength("test string", 6, 7));
    }

    @Test
    public void isValidEmailValidEmail()
    {
        assertTrue(Validator.isValidEmail("test@intuit.com"));
    }

    @Test
    public void isValidEmailSingleDigitDomain()
    {
        assertTrue(Validator.isValidEmail("test@q.com"));
    }

    @Test
    public void isValidEmailSingleDigitDomain2()
    {
        assertTrue(Validator.isValidEmail("test@q.q-a.com"));
    }

    @Test
    public void isValidEmailDashesEverywhere()
    {
        assertTrue(Validator.isValidEmail("test@q-a.q-b.q-c.com"));
    }

    @Test
    public void isValidEmailSingleDigitDomainTrailingHyphen()
    {
        assertFalse(Validator.isValidEmail("test@q.q-.com"));
    }

    @Test
    public void isValidEmailSingleDigitDomainLeadingHyphen()
    {
        assertFalse(Validator.isValidEmail("test@q.-q.com"));
    }

    @Test
    public void isValidEmailLeadingHyphen()
    {
        assertFalse(Validator.isValidEmail("test@-q.com"));
    }

    @Test
    public void isValidEmailMoreComplex()
    {
        assertTrue(Validator.isValidEmail("test@worldnet.att.net"));
    }

    @Test
    public void isValidEmailTooShort()
    {
        assertFalse(Validator.isValidEmail("@q.cm"));
    }

    @Test
    public void isValidEmailMaxLength()
    {
        assertTrue(Validator.isValidEmail("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@q.cm"));
    }
    @Test
    public void isValidEmailTooLong()
    {
        assertFalse(Validator.isValidEmail("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@q.cm"));
    }

    //te_s-t@ts.co.in
    @Test
    public void isValidEmailExoticNameField()
    {
        assertTrue(Validator.isValidEmail("te_s-t@ts.co.in"));
    }

    @Test
    public void isValidEmailInvalidEmail()
    {
        assertFalse(Validator.isValidEmail("test@"));
    }


    // dummy enum is used to avoid dependency on the Domain module
    private enum DummyEnum {
        QBOE;
    }

}

