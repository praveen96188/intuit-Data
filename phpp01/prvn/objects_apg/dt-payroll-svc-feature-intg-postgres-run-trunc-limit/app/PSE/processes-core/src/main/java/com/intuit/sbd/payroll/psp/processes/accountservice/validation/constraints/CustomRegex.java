package com.intuit.sbd.payroll.psp.processes.accountservice.validation.constraints;





import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import javax.validation.Constraint;
import javax.validation.Payload;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import static java.lang.annotation.ElementType.*;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {com.intuit.sbd.payroll.psp.processes.accountservice.validation.constraints.CustomRegexValidator.class })
public @interface CustomRegex {

    String message() default "Must match";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };

    String value();

    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
    @Retention(RUNTIME)
    @Documented
    @interface List {
        CustomRegex[] value();
    }
}