/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intuit.sbd.payroll.psp.ach.security;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author shivanandad069
 */
public class Encryption {
    public static String decrypt (File src) throws IOException  {
        return new Crypto().decrypt(src);
    }
}
