package com.intuit.sbd.payroll.psp.processes.accountservice.validation.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomRegexValidator implements ConstraintValidator<CustomRegex, String> {

    protected String regex ;

    @Override
    public void initialize(CustomRegex constraintAnnotation) {
        this.regex = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String input, ConstraintValidatorContext constraintValidatorContext) {

        final String regex = this.regex;


        final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        final Matcher matcher = pattern.matcher(input);

       return  !matcher.find();
    }
}
