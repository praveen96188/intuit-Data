/**
 * --------------------------------------------------------------------------
 * Copyright (c) 2008 Intuit, Inc. All rights reserved.
 * Unauthorized reproduction is a violation of applicable law.
 * --------------------------------------------------------------------------
 *
 * --------------------------------------------------------------------------
 *
 * Author	PSP CodeGen
 * Model Version	$modelVersion$
 *
 * --------------------------------------------------------------------------
 */
 package com.intuit.sbd.payroll.psp.domain;

 public enum $enumerationName$ {
    $enumValues$;

    public boolean in($enumerationName$... p$enumerationName$) {
        for ($enumerationName$ cur$enumerationName$ : p$enumerationName$) {
            if (this == cur$enumerationName$) {
                return true;
            }
        }
        return false;
    }

    public boolean notIn($enumerationName$... p$enumerationName$) {
        return !in(p$enumerationName$);
    }

 }