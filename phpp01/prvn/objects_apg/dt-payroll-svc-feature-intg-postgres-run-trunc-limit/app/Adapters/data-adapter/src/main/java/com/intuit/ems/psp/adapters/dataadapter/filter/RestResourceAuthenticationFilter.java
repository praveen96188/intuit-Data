package com.intuit.ems.psp.adapters.dataadapter.filter;

import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

/**
 * Created by charithah418 on 11/11/15.
 */

public class RestResourceAuthenticationFilter implements ResourceFilter {
    public ContainerRequestFilter getRequestFilter() {
        return new RestAuthenticationFilter();
    }

    public ContainerResponseFilter getResponseFilter() {
        return null;
    }
}
