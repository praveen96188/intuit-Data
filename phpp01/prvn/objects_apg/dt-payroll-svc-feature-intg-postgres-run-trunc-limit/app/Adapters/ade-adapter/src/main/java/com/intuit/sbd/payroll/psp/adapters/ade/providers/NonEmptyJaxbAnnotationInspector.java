package com.intuit.sbd.payroll.psp.adapters.ade.providers;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
/**
 * Created with IntelliJ IDEA.
 * User: shivanandad069
 * Date: 4/12/14
 * Time: 7:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class NonEmptyJaxbAnnotationInspector extends JaxbAnnotationIntrospector {
    @Override
    public JsonSerialize.Inclusion findSerializationInclusion(Annotated a, JsonSerialize.Inclusion defValue) {
        return JsonSerialize.Inclusion.NON_EMPTY;
    }
}
